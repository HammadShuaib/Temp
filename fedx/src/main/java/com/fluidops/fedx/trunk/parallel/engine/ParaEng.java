package com.fluidops.fedx.trunk.parallel.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections4.ListUtils;

import com.fluidops.fedx.trunk.description.Statistics;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

//import org.apache.jena.graph.query.Element;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpLib;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import com.opencsv.CSVReader;

public class ParaEng extends QueryEngineMain {
public static	String opq=null;
public static	String Optional;
public static String Union;
public static String Filter=null;
public static HashMap<String,String> pConstant= new HashMap<>();
public ParaEng(Query query, DatasetGraph dataset, Binding input, Context context) {
		super(query, dataset, input, context);
	}

	static private QueryEngineFactory factory = new ParaEngineFactory();
	public static int IsUNION;
	
	static public void register() {
	//	System.out.println(
	//			"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$44"
	//					+ "ParaEng%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5555555555"
	//					+ "77777777777777777777777777777777777777777777777777777777777");

		QueryEngineRegistry.addFactory(factory);
	}

	static public void unregister() {
		QueryEngineRegistry.removeFactory(factory);
	}

	static public QueryEngineFactory getFactory() {
		return factory;
	}

	@Override
	public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context) {
		//System.out.println(
		//		"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$44"
		//				+ "ParaEng%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5555555555"
		//				+ "55555555555555555555555555555555555555555555555555555555555555555");
		Vertex.resetVertexPool();
		Symbol property = Symbol.create("config");

		Statistics config = (Statistics) context.get(property);
		StageGenerator sg;
		sg = new StageGen(config);
		StageBuilder.setGenerator(context,  sg);
		ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, QC.getFactory(context));
		QueryIterator qIter1 = QueryIterRoot.create(input, execCxt);
		readFromFile();
		//System.out.println("This is now ParaEng correction:"+OpLib.isProject( op)+"--"+OpLib.isReduced( op)+"--"+OpLib.isSlice( op)+"--"+qIter1+"--"+execCxt);
	//if()
	//	OpAsQuery.asQuery(Op )
	//if(!Optional.toString().contains("OPTIONAL") ) {
	//	Op opu = OpUnion.create(op, op);
	//	Query query = OpAsQuery.asQuery(op);
		;
//		OpCondition;
	//	opq=OpAsQuery.asQuery(OpFilter.ensureFilter(op)).toString();
	//	opq=OpAsQuery.asQuery(op).toString();
		
//	}
//	else opq=" ";
		
		
	
	//OpFilter ops = OpFilter.filter(op);
	System.out.println("This is the sub op:"+opq);
		QueryIterator qIter = QC.execute(op, qIter1, execCxt);


return qIter;
	}

	public static void readFromFile() {
	       StringBuilder sb = new StringBuilder();
	        String strLine = "";
	       List<String> list = new ArrayList<String>();
	       List<String> record = new ArrayList<String>();

	       try {
	            try (CSVReader csvReader = new CSVReader(new FileReader("/mnt/hdd/hammad/hammad/Query.csv"));) {
	                String[] values = null;
	                try {
	    				while ((values = csvReader.readNext()) != null) {
	    					 // records.add(Arrays.asList(values));
	    					record = Arrays.asList(values);
	    					System.out.println("This is the list:"+list);
	    					break;
	    				}
	    			} catch (CsvValidationException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	            } catch (FileNotFoundException e1) {
	    			// TODO Auto-generated catch block
	    			e1.printStackTrace();
	    		} catch (IOException e1) {
	    			// TODO Auto-generated catch block
	    			e1.printStackTrace();
	    		}
				System.out.println("This is the list22:"+record);

	            String fileName=record.toString().replace("[", "").replace("]","");
				System.out.println("This is the list33:"+fileName);

	            String query="query/"+fileName;;
	            System.out.println("This is query in ParaEng:"+query);
	             BufferedReader br = new BufferedReader(new FileReader(query));
	             System.out.println("This is really good now:"+br); 
	             while (strLine != null)
	               {
	                strLine = br.readLine();
	           //     sb.append(strLine)
	             //   sb.append(System.lineSeparator());
	 //               strLine = br.readLine();
	                if (strLine==null)
	                   break;
	               
	                System.out.println("Basic Lines:"+strLine);
	                if(strLine.toString().contains("FILTER"))
                    list.add(strLine.replaceAll("[{}\\[,\\]]","").replaceAll("[$]", "?"));

                    else
	                list.add(strLine.replaceAll("[{}\\[,\\]<>]","").replaceAll("[$]", "?"));
	            }
	      //   System.out.println(Arrays.toString(list.toArray()));
	             br.close();
	        } catch (FileNotFoundException e) {
	            System.err.println("File not found");
	        } catch (IOException e) {
	            System.err.println("Unable to read the file.");
	        }
	//System.out.println("This is the file that is read:"+Arrays.toString(list.toArray()));     
if(Arrays.toString(list.toArray()).toString().contains("OPTIONAL"))
	Optional = Arrays.toString(list.toArray()).substring(Arrays.toString(list.toArray()).indexOf("OPTIONAL"));
else Optional=" ";
if(Arrays.toString(list.toArray()).toString().contains("UNION"))
	Union = Arrays.toString(list.toArray()).substring(Arrays.toString(list.toArray()).indexOf("UNION"));
else Union=" ";
if(Arrays.toString(list.toArray()).toString().contains("FILTER"))
	Filter = Arrays.toString(list.toArray()).substring(Arrays.toString(list.toArray()).indexOf("FILTER"));
else Filter=" ";

String[] abc= Arrays.toString(list.toArray()).split(" ");
for(int i=0;i<abc.length;i++) 
	if(abc[i].equals("?p") && !(abc[i-2].equals("DISTINCT") ||abc[i-2].equals("SELECT") )) {
		pConstant.put(abc[i], abc[i-1].replace(".", "").replace(",","").substring(1));
	//break;
	}
//	else
//		pConstant.put(null, null);
	

//if(Arrays.toString(list.toArray()).toString().contains("?p"))
//	pConstant = Arrays.toString(list.toArray()).substring(Arrays.toString(list.toArray()).indexOf("?p ")-10,Arrays.toString(list.toArray()).length());
//else pConstant=" ";

//System.out.println("This is the file that is read:"+Optional);     	
	}
}

class ParaEngineFactory implements QueryEngineFactory {

	public boolean accept(Query query, DatasetGraph dataset, Context context) {
		return true;
	}

	public Plan create(Query query, DatasetGraph dataset, Binding initial, Context context) {
		ParaEng engine = new ParaEng(query, dataset, initial, context);
		return engine.getPlan();
	}

	public boolean accept(Op op, DatasetGraph dataset, Context context) { // Refuse to accept algebra expressions
																			// directly.
		return false;
	}

	public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) { // Shodul notbe called
																								// because acceept/Op is
																								// false
		throw new ARQInternalErrorException("QueryEngine: factory called directly with an algebra expression");
	}
	

	
}