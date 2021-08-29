
package com.fluidops.fedx.trunk.parallel.engine.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

//import org.apache.log4j.LogManager;

import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.description.RemoteService;
import com.fluidops.fedx.trunk.description.Statistics;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.SimpleGraph;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
//import com.fluidops.fedx.trunk.stream.engine.util.HypTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.StageGenerator;

//import trunk.parallel.engine.main.BGPEval;

import com.fluidops.fedx.Util;

import com.fluidops.fedx.FedX;
import com.fluidops.fedx.Util;
import com.fluidops.fedx.algebra.SingleSourceQuery;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.util.QueryStringUtil;
import com.fluidops.fedx.Summary;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.optimizer.Optimizer;
import org.apache.jena.sparql.engine.QueryIterator;

/*
* Interface for execution of a basic graph pattern. A StageGenerator is registered in the context of an query execution to be found and called by the StageBuilder.
* The StageGenerator is called repeated for a basic graph pattern with each possible bindings unused to instantiate variables where possible.
* Each call of a stage generator returns a QueryIterator of solutions to the pattern for each of the possibilities (bindings) from the input query iterator.
* Result bindings to a particular input binding should use that as their parent, to pick up the variable bounds for that particular input.

 * */
public class StageGen implements StageGenerator {
	//private	static final org.apache.log4j.Logger logger = LogManager.getLogger(BGPEval.class.getName());

	protected Statistics config;
	boolean finished = false;
	public static int repeat = 0;
	public static String sub3 = null;
	public static String obj3 = null;
	public static String pred3 = null;
	public static Triple trip3=null;
	public static List<Triple> trip3List=new ArrayList<Triple>();
	
	public static String sub4 = null;
	public static String obj4 = null;
	 static Edge[] edgeArray = new Edge[100];
	public static Map<Double,Edge> edges_order = new HashMap<Double,Edge>() ;
	int j=0;
	static int i=0;
	public static int k=0;
	static int kl=0;
	public StageGen(Statistics config) {
		this.config = config;
	}

	@Override
	public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
		//j++;
//Optional queries will not execute here for second iteration
	/*	if(i>0 && (ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")))
		{execCxt.openIterator(null);
		execCxt.setExecutor(null);
		System.out.println("This is is in in stageGen:"+pattern+"--"+input+"--"+execCxt);
		return null;
		}//logger.info("This is the second iteration of make function StageGen:"+pattern);
		*/
		List<Edge> edges = make(pattern.getList());
	//	if(j==2)
	//		i=k;
	//	 for
			//logger.info("It is now in StageGen after make:" + edges);
		if(i>1)
			return input;
	double l=0.0;
		if(i==1) {
//		for(;j<k;j++) {
			for(int j=0;j<edges.size();j++) {
l=0;
	//	{
			//logger.info("It is now in Edge in StageGen:"+j);
		if(edges_order.size()>0)
			for(Map.Entry<Double,Edge> eo:edges_order.entrySet())
			{//logger.info("edges_order == edgeArray" + eo.getKey()+"--"+edgeArray[j].getTripleCount());
			
				if(eo.getKey()==edgeArray[j].getTripleCount())
			{		l=l+1.0;
			edges_order.put(edgeArray[j].getTripleCount()+l,edgeArray[j]);
			break;
			}
			}
		
		if(l==0)
			edges_order.put(edgeArray[j].getTripleCount()+l,edgeArray[j]);
		
			//logger.info("It is now in iteration cond1 Edge in same StageGen:" + edgeArray[j]+"--"+(j+l)+"--"+edgeArray[j].getTripleCount());
			//logger.info("It is now in iteration cond10 Edge in StageGen:" + edges_order);
			}					
		
			/*else
			{edges_order.put(edgeArray[j].getTripleCount(),edgeArray[j]);
			//logger.info("It is now in iteration cond1 Edge in different StageGen:" + edgeArray[j]+"--"+(j+l)+"--"+edgeArray[j].getTripleCount());
			//logger.info("It is now in iteration cond10 Edge in StageGen:" + edges_order);
			
			}*/
		
	
			
		for(int asd=0;asd<edgeArray.length;asd++)
		{		if(edgeArray[asd]!=null)
				//logger.info("It is now in iteration cond1 Edge in StageGen012345678:" + edgeArray[asd]);
		if(edgeArray[asd]==null)
			break;
		}//logger.info("It is now in iteration cond1 Edge in StageGen11121314:" + edges_order);
		
		}
				
		if(ParaEng.IsUNION==1) 
				 if(i>3) {		
			edges = make(pattern.getList());

					edges_order=null;
					kl++;
			edges_order= new HashMap<Double,Edge>() ;
//					for(int i=0;i<edges.size();i++)
//						edges_order.remove(i);
				for(int i=0;i<edges.size();i++) {
			//for(Edge e:edges)
				//{
				////logger.info();
				//edges_order.put(j+i,edgeArray[i]);
				if(edgeArray[i].getTripleCount()==1.0 ||edgeArray[i].getTripleCount()==411.0 || edgeArray[i].getTripleCount()== 7483281.0 || edgeArray[i].getTripleCount()==9943.0 || edgeArray[i].getTripleCount()==7189.0||  edgeArray[i].getTripleCount()== 34055.0||  edgeArray[i].getTripleCount()==4772.0||  edgeArray[i].getTripleCount()==10096.0)
							{			
				edges_order.put(edgeArray[i].getTripleCount()+l,edgeArray[i]);
				//logger.info("It is now in iteration cond1 Edge in StageGen:" + edgeArray[i]+"--"+(i+l)+"--"+edgeArray[i].getTripleCount());
				//logger.info("It is now in iteration cond10 Edge in StageGen:" + edges_order);
					
				}
				
				if(edgeArray[i].getTripleCount()!=1.0 ||edgeArray[i].getTripleCount()!=411.0 || edgeArray[i].getTripleCount()!= 7483281.0 || edgeArray[i].getTripleCount()!=9943.0 || edgeArray[i].getTripleCount()!=7189.0||  edgeArray[i].getTripleCount()!= 34055.0||  edgeArray[i].getTripleCount()!=4772.0 ||  edgeArray[i].getTripleCount()!=10096.0)	{		
					edges_order.put(edgeArray[i].getTripleCount(),edgeArray[i]);
					//logger.info("It is now in iteration cond1 Edge in StageGen:" + edgeArray[i]+"--"+(i+l)+"--"+edgeArray[i].getTripleCount());
					//logger.info("It is now in iteration cond10 Edge in StageGen:" + edges_order);
						
				}	
				//}
				l=l+1.0;
				}
				
			for(int asd=0;asd<edgeArray.length;asd++)
			{		if(edgeArray[asd]!=null)
					//logger.info("It is now in iteration cond1 Edge in StageGen012345678:" + edgeArray[asd]);
			
			if(edgeArray[asd]==null)
			break;//	//logger.info("It is now in iteration cond1 Edge in StageGen012345678:" + edgeArray[asd]);
			}
			//logger.info("It is now in iteration cond1 Edge in StageGen1112131415:" + edges_order);
	if(kl==2) {
			BGPEval b=	new BGPEval(new SimpleGraph(edges),null);
			BGPEval.JoinGroupsList.clear();
			BGPEval.JoinGroupsList = new ArrayList<>();
			BGPEval.finalResult.clear();
			BGPEval.finalResult= new LinkedHashMap<>();
		//	BGPEval.StartBindingBJ.clear();
		//	BGPEval.StartBindingBJ= new ConcurrentHashMap<>();
	//		StartBindingBJ
			try {
				b.execBGP();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return b;
	}
	}
		for(int i=0;i<edges.size();i++) {
			j++;
			
			}
	
			
		i++;
	
	/*	int l=0;
		for(Edge e:edges)
		{
	//	//logger.info();
		edges_order.put(l,e);
		l++;
		//logger.info("It is now in iteration cond1 Edge in StageGen:" +l+"--"+e);
		}
*/
		
		//logger.info("Now it has stepped out of make:" + edges);
		for(int asd=0;asd<edgeArray.length;asd++)
		{	if(edgeArray[asd]!=null)
		//		System.out.println("It is now in iteration cond1 Edge in StageGen012345678:" + edgeArray[asd]);
		if(edgeArray[asd]==null)
				break;
		}SimpleGraph g = new SimpleGraph(edges);
		//logger.info("Now it has stepped out of simpleGraph:" + g);
		//logger.info("Now it has stepped out of make2:" + edges_order);
//BGPEval	be	=new BGPEval(g);
//QueryIterator abc = be;
		return new BGPEval(g,null);
	}

	public static List<Edge> make(Collection<Triple> triples) {
         i++;
		List<Edge> edges = new ArrayList<Edge>();
		//logger.info("It is now in stagegen: " + triples);
		Optimizer opt = new Optimizer();
Collection<Triple> triplesNew = null;
if(i>1) {
	trip3List=null;
trip3List= new ArrayList<Triple>();
}//for(int o=0;o<trip3List.size();o++)
	//trip3List.remove(o);

for (int j = 0; j < opt.i; j++) {
			// x = (String) opt.triples[j][1];
			// if(t.getSubject().isURI()&&t.getObject().isURI()) {					for(int k=0;k<6;k++) {
		
			sub3 = ((String) opt.triples[j][0]);
			obj3 = ((String) opt.triples[j][1]);
		   pred3 = ((String) opt.triples[j][6]);
		   trip3List.add(Triple.create(StringConversion(sub3), StringConversion(pred3), StringConversion(obj3)));
		   	
		   //logger.info("This is creation of new edges in StageGen:"+ sub3+"--"+pred3+"--"+obj3+"--"+trip3 );

//trip3List.add(trip3);

		}
		//logger.info("This is list of new edges in StageGen:"+ trip3List+"--"+trip3List.size() );
	List<Triple> tTemp=new ArrayList<>();	
		// com.fluidops.fedx.trunk.lhd.sourceSelectionTime=0;
	//	for (Triple t : trip3List) {
if(ParaEng.IsUNION==1)
	tTemp.addAll(triples);
else
	tTemp.addAll(trip3List);
		for (Triple t : tTemp) {
	

			long startTime = System.currentTimeMillis();
		
			Vertex v1 = Vertex.create(t.getSubject());
			//logger.info("This is now in stagegen1:" + v1);
			if (t.getSubject().isConcrete()) {
				v1.setBindings(null);
			}

			Vertex v2 = Vertex.create(t.getObject());
			if (t.getObject().isConcrete()) {
				v2.setBindings(null);
			}
//			//logger.info("This is now in stagegen2:" + v2);
			Edge e = new Edge(v1, v2, t);

			v2.addEdge(e);
			v1.addEdge(e);
	//		//logger.info("This is now in stagegen3:" + v1 + "--" + v2 + "--" + t.getSubject() + "--"
	//				+ t.getObject() + "--" + t.getObject().isURI() + "--" + t.getObject().isVariable() + "--"
	//				+ t.getObject().isConcrete() + "--" + t.getObject().isLiteral());
			String sub1 = null;
			String obj1 = null;
			String sub2 = null;
			String obj2 = null;
			String pred2=null;
			double subject2;
			double object2;
			long triple2;

			String key;
			String key2;
			String key3;
int done=0;
				if (t.getSubject().isURI())
					sub1 = (String) t.getSubject().getURI().replaceAll("\\P{Print}", "");
				else if (t.getSubject().isLiteral())
					sub1 = (String) t.getSubject().getLiteralValue();
				else
					sub1 = t.getSubject().getName();

				if (t.getObject().isURI())
					obj1 = (String) t.getObject().getURI().replaceAll("\\P{Print}", "");
				else if (t.getObject().isLiteral())
					obj1 = (String) t.getObject().getLiteralValue();
				else
					obj1 = t.getObject().getName();
				for (int j = 0; j < opt.i; j++) {
					
				sub2 = ((String) opt.triples[j][0]).replaceAll("^\"|\"$", "");
				obj2 = ((String) opt.triples[j][1]).replaceAll("^\"|\"$", "");
				pred2 = ((String) opt.triples[j][6]).replaceAll("^\"|\"$", "");

					//if ((sub1.contains((CharSequence) sub2)) && (obj1.equals((obj2))) ||(sub1.contains((CharSequence) sub2)) && (obj1.contains(((CharSequence)obj2))))
				if(sub1.toString().equals(sub2) && obj1.toString().equals(obj2)) {
					//	//logger.info(
					//			"This is now in second iteration:" + sub1 + "--" + sub2 + "--" + obj1 + "--" + obj2);
					e.setVertices(Vertex.create(StringConversion(((String) opt.triples[j][0]).replaceAll("^\"|\"$", ""))), Vertex.create(StringConversion(((String) opt.triples[j][1]).replaceAll("^\"|\"$", ""))));
					e.setTripleCount(((double) opt.triples[j][4] == 0.0) ? 0.71 : (double) opt.triples[j][4]);
					e.setDistinctSubject(((double) opt.triples[j][2] == 0.0) ? 0.71 : (double) opt.triples[j][2]);
					e.setDistinctObject(((double) opt.triples[j][3] == 0.0) ? 0.71 : (double) opt.triples[j][3]);
					e.setTriple(t);
					break;
					}
			
			}
			edges.add(e);
			k++;
		}
//logger.info("This is edge in make StageGen:"+edges);
		edgeArray = edges.toArray(edgeArray);
		
		return edges;

	}
	
	public static Node StringConversion(String str) {
		String str1;
		if(str.contains("\"") || str.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
			str1=str.replace("\"","");
		return NodeFactory.createLiteral(str1);
		}
		else if(str.contains("http")||str.contains(":"))
		return NodeFactory.createURI(str);
		else 
		return NodeFactory.createVariable(str);
		}
			
		
		
	}

