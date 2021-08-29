package com.fluidops.fedx.trunk.parallel.engine.main;

import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.serializer.SerializationContext;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;

import com.fluidops.fedx.optimizer.JoinOrderOptimizer2;
import com.fluidops.fedx.optimizer.Optimizer;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.SimpleGraph;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.exec.BoundQueryTask;
import com.fluidops.fedx.trunk.parallel.engine.exec.QueryTask;
import com.fluidops.fedx.trunk.parallel.engine.exec.TripleExecution;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.BindJoin;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.HashJoin;
import com.fluidops.fedx.trunk.parallel.engine.opt.ExhOptimiser;
//import com.fluidops.fedx.trunk.parallel.engine.opt.ExhOptimiser;
import com.fluidops.fedx.trunk.parallel.engine.opt.Optimiser;
import com.fluidops.fedx.trunk.stream.engine.util.QueryUtil;
import com.google.common.collect.LinkedListMultimap;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import py4j.Gateway;
import py4j.GatewayServer;
import py4j.Protocol;
import py4j.examples.IHello;
import py4j.reflection.ReflectionUtil;
import py4j.reflection.RootClassLoadingStrategy;


public  class BGPEval extends QueryIteratorBase {
//	private	static final com.hp.hpl.log4j.Logger logger = LogManager.getLogger(TripleExecution.class.getName());
	static ResultSet v = null;
	 static List<ArrayList<String>> rowsLeft = new ArrayList<>();

	static String[][] finalTable=null;
   static  InputStream targetStream = null;
   static ArrayList<Binding> resultoutput =new ArrayList<>();
	public static int ExclusivelyExclusive=0;
	public static ConcurrentHashMap<ConcurrentHashMap<Set<Vertex>,Set<Edge>>,ArrayList<Binding>> StartBinding = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<ConcurrentHashMap<Set<Vertex>,Set<Edge>>,ArrayList<Binding>> StartBindingBJ = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<ConcurrentHashMap<Vertex,Edge>,ArrayList<Binding>> StartBindingFinal = new ConcurrentHashMap<>();
	
public static Map<EdgeOperator, Integer>  joinGroupUnion = new HashMap<>();
public static Map<String, String>  HeaderReplacement = new HashMap<>();
int o=0;
static int e=0;
static  List<ArrayList<String>> rightTable=null;
static  List<ArrayList<String>> leftTable = null;
static List<ArrayList<String>> print;

public static HashSet<String> headersAll = new HashSet<>();
public static List<String> OptionalHeadersRight = new ArrayList<>();
public static List<String> OptionalHeadersLeft = new ArrayList<>();
public static List<String> OptionalHeaders= new ArrayList<>();
public static   Map<EdgeOperator,Integer> TreeType = new HashMap<>();
public static	Map< List<EdgeOperator>,Integer> 	joinGroups2= new ConcurrentHashMap<>();
public static	LinkedListMultimap< com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator,Integer> joinGroupsLeft = LinkedListMultimap.create();
public static	LinkedListMultimap<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator,Integer> joinGroupsRight = LinkedListMultimap.create();
public static	Map< List<EdgeOperator>,Integer> joinGroupsOptional1 = new HashMap<>();
public static	LinkedListMultimap< com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator,Integer> joinGroupsLeftOptional = LinkedListMultimap.create();
public static	LinkedListMultimap< com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator,Integer> joinGroupsRightOptional = LinkedListMultimap.create();		
public static	LinkedListMultimap< EdgeOperator,String> TreeOrder = LinkedListMultimap.create();		
public static ResultSet v1;


	public static List<List<EdgeOperator>> JoinGroupsList = new Vector<>();
	public static List<List<EdgeOperator>> JoinGroupsListExclusive = new Vector<>();
	public static List<EdgeOperator> JoinGroupsListLeft = new Vector<>();
	public static List<EdgeOperator> JoinGroupsListRight = new Vector<>();
	public static List<EdgeOperator> JoinGroupsListLeftOptional = new Vector<>();
	public static List<EdgeOperator> JoinGroupsListRightOptional = new Vector<>();
public static ConcurrentHashMap< EdgeOperator,HashSet<Integer>> newFormation = new ConcurrentHashMap<>();
public static HashSet<List<EdgeOperator>>  	JoinGroupsListAll = new HashSet<>();


	static int completionE=0;
public static int finalResultSize=0;
static int mo=0;
	public static List<List<EdgeOperator>> JoinGroupsListOptional = new Vector<>();
public static	List<Set<Binding>> UnProcessedSets = new Vector<>();
public int completion=0;	
	public static ConcurrentHashMap<EdgeOperator,Map<EdgeOperator,Integer>> exchangeOfElements = new ConcurrentHashMap<>();
	public static LinkedHashMap<EdgeOperator,List<EdgeOperator>> orderingElements = new LinkedHashMap<>();
	public static LinkedHashMap<Integer,HashSet<List<EdgeOperator>>> linkingTree = new LinkedHashMap<>();
	public static LinkedHashMap<HashSet<List<EdgeOperator>>,Integer> linkingTreeDup = new LinkedHashMap<>();
    public static int possibleOuter;
	public static ArrayList<ArrayList<Integer>> Treelowestlevel= new ArrayList<ArrayList<Integer>>();
	public static ArrayList<Integer> TreelowestlevelProcessed= new ArrayList<Integer>();
static	int UnprocessedSetRepNo = 0;
public static List<EdgeOperator> operators_BushyTreeOrder= new Vector<>() ;//=optBT.nextStage();
public static HashMap<Vertex, Set<Binding>> StartBinding123 = new HashMap<>();

	public static List<Set<Binding>> ProcessedSets= new ArrayList<Set<Binding>>();
	
	static List<EdgeOperator>  oiSubList = new Vector<>();
	static List<EdgeOperator> ProcessedTriples = new Vector<>();
//	static List<Vertex> ProcessedIndependentSubjects = new Vector<>();

	public static ConcurrentHashMap<ConcurrentHashMap<Set<Vertex>,Set<Edge>>,ArrayList<Binding>> StartBindingSet = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<ConcurrentHashMap<Set<Vertex>,Set<Edge>>,ArrayList<Binding>> StartBindingSetBJ = new ConcurrentHashMap<>();
	public int BushyTreeSize;
	private SimpleGraph g;
	
	//public static Map<EdgeOperator,Set<Binding>> finalResult=Collections.synchronizedMap(new LinkedHashMap<>());
	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResult = new LinkedHashMap<>();
	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResultFinal = new LinkedHashMap<>();
	
	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResultRight = new LinkedHashMap<>();
	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResultLeft = new LinkedHashMap<>();

	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResultRightOptional = new LinkedHashMap<>();
	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResultLeftOptional = new LinkedHashMap<>();

	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResultOptional = new LinkedHashMap<>();
	public static LinkedHashMap<EdgeOperator,List<Binding>> finalResultOptionalFinal = new LinkedHashMap<>();
	
	public List<EdgeOperator> operatorsTemp;
	private Set<Binding> input = null;
	public static Iterator<Binding> results;
	private static ExecutorService es;
	//static int i=0;
	static int i=0;
	static int ij=0;
	private Optimiser optimiser;
	Set<Binding> BindingSets = null;
	public static int HashJoinCompletion=0;
	public static int HashJoinCount;
	public static int BushyTreeCounter=0;
static	 ArrayList<String>  abv= new ArrayList<>();
		
	static int l =0; 
	public static Vector<Binding> a = new  Vector<>();
	public static Vertex b= new Vertex();
	public static boolean OptionalQuery;
	public static  boolean IsRightQuery;
	public static boolean IsLeftQuery;
	
	static ArrayList<ArrayList<Integer>>  UnProcessedSetsRep = new ArrayList<>();
	static ArrayList<Set<Binding>>  Remaining = new ArrayList<>();
	
	public static HashMap<Vertex,Set<Edge>> c = new HashMap<>();
	public static Set<Edge> d = new HashSet<>();
	public static Set<Binding> r3 = new HashSet<>();
	public BGPEval(SimpleGraph g,QueryIterator input) {
		this.g = g;
		this.input=null;
	/*
		//logger.info("This is in BGPEval Constructor:");
			if (!(input instanceof QueryIterRoot)) {
			if (Config.debug) {
				//logger.info("Not QueryIterRoot");
			}
			this.input = new HashSet<Binding>();
			while (input.hasNext()) {
				this.input.add(input.next());
			}
		}*/
	}
	

	
	  protected Object clone() throws CloneNotSupportedException{
	      BGPEval student = (BGPEval) super.clone();
	      student.optimiser = (ExhOptimiser) optimiser.clone();
	      return student;
	   }

	  
//	public void output(IndentedWriter out, SerializationContext sCxt) {
//		//logger.info("These is the list of operators in ExecuteJoins22244441");
//	}
	  

		protected boolean hasNextBindingSet() {
			// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
			// execBGP11!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+results);

			if (results == null) {
				// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
				// execBGP12!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+results);
				//logger.info("This is after BGPEvalConstructor");
			//	if(completionE<1) {
				try {
					execBGP();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
	//			StatementGroupOptimizer2 sgo = new StatementGroupOptimizer2(null);
	//			sgo.meetNJoin(null);
			
				return results.hasNext();
				
		//	}
	//	return false;
		}
		
	static public ExecutorService getCurrentExeService() {
		return es;
	}

	/**
	 * @throws CloneNotSupportedException
	 */
	
	
	public void execBGP() throws CloneNotSupportedException {

		completionE++;
		double distinctSubject = 0;
		double distinctObject = 0;
		Optimizer opt0 = new Optimizer();
		Optimiser opt = null;
		//System.out.println("This is value of ExhOpt:" + opt0.ExhOpt);
		// if(opt0.ExhOpt==1)
		// opt= new ExhOptimiser_NoSubObjCount(g);
		// i/f(opt0.ExhOpt==0)
		opt = new ExhOptimiser(g);
		// if(opt0.ExhOpt==2)
		// opt=new ExhOptimiser_EqualSubObjCount(g);
		// Optimiser opt = new ExhOptimiser_NoSubObjCount(g);
		// Optimiser opt = new GrdyOptimiser(g);
		Optimiser opt1=null;
		HashJoinCompletion=0;
	//	Optimiser opt2=null;
		try {
			opt1 = (Optimiser)opt.clone();
		} catch (CloneNotSupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	final Optimiser	opt2 = (Optimiser)opt.clone();
	final Optimiser	optBT = (Optimiser)opt.clone();
	
	
		List<EdgeOperator> operators = opt.nextStage();
		List<EdgeOperator> operators_dependent =opt2.nextStage();
		List<EdgeOperator> operators_independent =opt1.nextStage();
		List<EdgeOperator> operators_BushyTree= new Vector<>() ;//=optBT.nextStage();
		List<EdgeOperator> operators_optional= new Vector<>() ;//=optBT.nextStage();
		List<EdgeOperator> operators_BushyTreeRight= new Vector<>() ;//=optBT.nextStage();
		List<EdgeOperator> operators_BushyTreeLeft= new Vector<>() ;//=optBT.nextStage();
	
								
		
		ListIterator<EdgeOperator> operatorsIterator = operators_independent.listIterator();
		ListIterator<EdgeOperator> operatorsIteratorDep = operators_dependent.listIterator();
		//ListIterator<EdgeOperator> operatorsIteratorBT ;
		
		//while(operatorsIteratorBT.hasNext())
		//System.out.println("This is the new Bind to HashJoin in BGPEval00:"+operatorsIteratorBT.next());
			
		String a;
		//System.out.println("These are the value of size in BGPEval:"+ExhOptimiser.LabeledSize);
StageGen.kl=0;
		EdgeOperator x ;
		while(operatorsIterator.hasNext()){
		//a=String.valueOf(operatorsIterator.next());
		 x = operatorsIterator.next();
		for(int i=0;i<Optimizer.triples.length;i++) {
			//System.out.println("Getting individual operators 4 BushyTree element before:"+"--"+Optimizer.triples[i][0]+"--"+x.getEdge().getTriple().getSubject().getName()+"--"+Optimizer.triples[i][1]+"--"+x.getEdge().getTriple().getObject().getName());
	String Subject; //= x.getEdge().getTriple().getSubject().isURI()?x.getEdge().getTriple().getSubject().toString():x.getEdge().getTriple().getSubject().getName().toString();
	
	if(x.getEdge().getTriple().getSubject().isURI())
		Subject=x.getEdge().getTriple().getSubject().toString();
		else if(x.getEdge().getTriple().getSubject().isLiteral())
			Subject=x.getEdge().getTriple().getSubject().getLiteral().toString();
		else
			Subject	=x.getEdge().getTriple().getSubject().getName().toString();

	
	String Object;//=x.getEdge().getTriple().getObject().isURI()?x.getEdge().getTriple().getObject().toString():x.getEdge().getTriple().getObject().getName().toString();
	
	if(x.getEdge().getTriple().getObject().isURI())
		Object=x.getEdge().getTriple().getObject().toString();
		else if(x.getEdge().getTriple().getObject().isLiteral())
			Object=x.getEdge().getTriple().getObject().getLiteral().toString();
		else
			Object	=x.getEdge().getTriple().getObject().getName().toString();

	
	String SizeSO = Subject+"--"+Object;
					
	
	//		System.out.println("This is the new generated error:"+Optimizer.triples[i][0].toString()+"--"+x.getEdge().getTriple().getSubject().getName().toString());	
//	System.out.println("This is the new generated error:"+Optimizer.triples[i][1].toString()+"--"+x.getEdge().getTriple().getObject().is);	
	if(Optimizer.triples[i][0]!=null ||Optimizer.triples[i][1]!=null)
			if(Optimizer.triples[i][0].toString().replace("\"", "").equals(Subject) &&Optimizer.triples[i][1].toString().replace("\"", "").equals(Object)) {
		
				int skp=0;
				Vertex BindDep = new Vertex();;
				for(Entry<String, LinkedHashMap<Double, Double>> ls:ExhOptimiser.LabeledSize.entrySet())
				{

			/*		for(EdgeOperator gh:operators_BushyTree)
					if((gh.getEdge().getV1().getNode().getName()+"--"+gh.getEdge().getV1().getNode().getName()).equals(ls.getKey().toString()))
						{skp=1;
						break;
						}
					if(skp==1)
					{
						skp=0;
						continue;
					}*/
						
					// System.out.println("This is the value of BindJoin BGPEval:"+SizeSO+"--"+ls.getKey()+"--"+x.getEdge().getV1()+"--"+x.getEdge().getV2());	
					if(!SizeSO.toString().contains("http")) {	
					 if(SizeSO.equals(ls.getKey())) {
					for(Entry<Double, Double> size:ls.getValue().entrySet())
						{
						
						
						System.out.printf("This is BindDep size::"+x+"--"+size.getKey()+"--"+size.getValue());
							
						if(size.getKey()<=size.getValue() && !(size.getKey()==0.02 && size.getValue()==0.02))
						{
	//						System.out.println("This is now checking every operator head:"+x.getEdge().getV2());
							
				//			for(EdgeOperator io:operators_independent)
					//			if(!io.toString().equals(x.toString()))
						//			if(io.toString().contains(x.getEdge().getV2().toString()))
							//		{	System.out.println("This is now checking every operator:"+io);
							if(size.getKey()==0.71)
								BindDep = x.getEdge().getV1();//}
								else
							BindDep = x.getEdge().getV2();//}
					//				else BindDep = x.getEdge().getV1();
	//System.out.println("This is the value of BindJoin BGPEval000:"+BindDep.getEdges());	
							ConcurrentHashMap<Set<Vertex>,Set<Edge>> cd = new ConcurrentHashMap<>();		
								HashSet<Vertex> ab = new HashSet<>();
								ab.add(BindDep);
								
									cd.put(ab, x.getEdge().getV2().getEdges());
									StartBinding.put(cd, new ArrayList<>());
									
						}
						else	if(size.getKey()>size.getValue() && !(size.getKey()==0.02 && size.getValue()==0.02)) 
						{
					//		for(EdgeOperator io:operators_independent)
					//			if(!io.toString().equals(x.toString()))
					//				if(io.toString().contains(x.getEdge().getV2().toString()))
					//				{	System.out.println("This is now checking every operator:"+io);
							if(size.getValue()==0.71)
								BindDep = x.getEdge().getV2();//}
							
								else
							BindDep = x.getEdge().getV1();//}
						//			else BindDep = x.getEdge().getV2();
	//							BindDep= x.getEdge().getV1();
	//System.out.println("This is the value of BindJoin BGPEval000:"+BindDep.getEdges());	
								ConcurrentHashMap<Set<Vertex>,Set<Edge>> cd = new ConcurrentHashMap<>();		
								HashSet<Vertex> ab = new HashSet<>();
								ab.add(BindDep);
								cd.put(ab, x.getEdge().getV1().getEdges());
								
								StartBinding.put(cd, new ArrayList<>());	
									
								
							}
						}
						}
				}
				}
		//		System.out.println("This is the new Bind to HashJoin in BGPEval11111:"+BindDep);	
					
		if(Optimizer.triples[i][7]=="HashJoin" ||Optimizer.triples[i][7]==null) {
			 operators_BushyTree.add(new HashJoin(x.getEdge()));
			 operators_BushyTreeRight.add(new HashJoin(x.getEdge()));
			 operators_BushyTreeLeft.add(new HashJoin(x.getEdge()));
			 operators_BushyTreeOrder.add(new HashJoin(x.getEdge()));						
				
			// System.out.println("This is the new HashJoin in BGPEval:"+operators_BushyTree);	
				
		}
		if(Optimizer.triples[i][7]=="BindJoin")
			{
			if(BindDep.equals(x.getEdge().getV1()))
		{
			/*	for(Entry<Double, Edge> eo:StageGen.edges_order.entrySet())
					if(eo.getValue().equals(x.getEdge())) 
						if(eo.getKey()>3000000) {
			System.out.println("2This is double value of edges_order:"+eo.getKey());
			operators_BushyTree.add(new HashJoin(x.getEdge()));			
			operators_BushyTreeRight.add(new BindJoin(x.getEdge().getV1(),x.getEdge()));
			 operators_BushyTreeLeft.add((new BindJoin(x.getEdge().getV1(),x.getEdge())));
			 operators_BushyTreeOrder.add((new BindJoin(x.getEdge().getV1(),x.getEdge())));						
				
						}
						else {*/	
					operators_BushyTree.add(new BindJoin(x.getEdge().getV1(),x.getEdge()));			
					//	}
		operators_BushyTreeRight.add(new BindJoin(x.getEdge().getV1(),x.getEdge()));
		 operators_BushyTreeLeft.add((new BindJoin(x.getEdge().getV1(),x.getEdge())));
		 operators_BushyTreeOrder.add((new BindJoin(x.getEdge().getV1(),x.getEdge())));						
				//	}
		//}
			}
			if(BindDep.equals(x.getEdge().getV2())) {
		operators_BushyTree.add(new BindJoin(x.getEdge().getV2(),x.getEdge()));			
			operators_BushyTreeRight.add(new BindJoin(x.getEdge().getV2(),x.getEdge()));					
			 operators_BushyTreeLeft.add(new BindJoin(x.getEdge().getV2(),x.getEdge()));
			 operators_BushyTreeOrder.add(new BindJoin(x.getEdge().getV2(),x.getEdge()));						
						}
		//	}
			//System.out.println("This is the new BindJoin in BGPEval:"+operators_BushyTree);				
			}
			}
	}
		}
	//	Collections.sort(operators_BushyTree,Comparator.comparing(item->operators.indexOf(item)));
	//	Collections.sort(operators_BushyTreeRight,Comparator.comparing(item->operators.indexOf(item)));
	//	Collections.sort(operators_BushyTreeLeft,Comparator.comparing(item->operators.indexOf(item)));
	//	Collections.sort(operators_BushyTreeOrder,Comparator.comparing(item->operators.indexOf(item)));
		
		//System.out.println("Getting individual operators 4 BushyTree element:"+"--"+operators_BushyTree);
			Collection<Edge> list = null;
			//System.out.println("This is the new HashJoin:"+JoinOrderOptimizer2.Join);
			for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, ArrayList<Binding>> e1:StartBinding.entrySet())
				for(Map.Entry<Set<Vertex>,Set<Edge>> e:e1.getKey().entrySet()) {
				list = e.getValue();
		    for(Iterator<Edge> itr = list.iterator(); itr.hasNext();)
		    {
		        if(Collections.frequency(list, itr.next())>1)
		        {
		            itr.remove();
		        }
		    }
				}


			//System.out.println("Getting individual operators 4 dep. element:"+"--"+operators_dependent);
			//System.out.println("Getting individual operators 4 element:"+"--"+operators_BushyTree);
			HashJoinCount=operators_dependent.size();

		String length = "";
		String[] vv =null;
		 Iterator<EdgeOperator> l3=	operators_BushyTreeOrder.iterator();
		while(l3.hasNext()) {
		// BindingSet xz=l1.next();
		//  while(l1.hasNext()) {
		      length=l3.next().toString();
		      vv=length.split(" ");
		for(String v:vv)
		if(v.startsWith("(") ) {
			if(!v.contains("http")&& v.contains(":") && (v.contains("$") || v.contains("?")))
		   headersAll.add(v.substring(1,v.indexOf(":")));

		//break;
		}
		else continue;
		//break;
		  }
		
//	System.out.println("These are all the headers:"+headersAll);

	/*	Collections.sort(operators_BushyTree,Comparator.comparing(item->operators.indexOf(item)));
		Collections.sort(operators_BushyTreeRight,Comparator.comparing(item->operators.indexOf(item)));
		Collections.sort(operators_BushyTreeLeft,Comparator.comparing(item->operators.indexOf(item)));
		Collections.sort(operators_BushyTreeOrder,Comparator.comparing(item->operators.indexOf(item)));
		*/
	//	System.out.println("Getting individual operators 4 BushyTree element:"+"--"+operators_BushyTree);
		//	Collection<Edge> list = null;
		//	//logger.info("This is the new HashJoin:"+JoinOrderOptimizer2.Join);
			/*for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, Set<Binding>> e1:StartBindingSet.entrySet())
				for(Map.Entry<Set<Vertex>,Set<Edge>> e:e1.getKey().entrySet()) {
				list = e.getValue();
		    for(Iterator<Edge> itr = list.iterator(); itr.hasNext();)
		    {
		        if(Collections.frequency(list, itr.next())>1)
		        {
		            itr.remove();
		        }
		    }
				}*/


		//	//logger.info("Getting individual operators 4 dep. element:"+"--"+operators_dependent);
			//logger.info("Getting individual operators 4 element:"+"--"+operators_BushyTree);
	//		HashJoinCount=operators_dependent.size();


for(EdgeOperator o:operators_BushyTree)
		if(!joinGroupUnion.containsKey(o))
	joinGroupUnion.put(o, 0);
	Map< Integer,List<EdgeOperator>>  joinGroupsExcl = new HashMap<>();

	Map< Integer,List<EdgeOperator>>  joinGroupsOptional = new HashMap<>();

	Iterator<EdgeOperator> Operators_BushyIterator = operators_BushyTree.iterator();

//Created Data structure for optional or union clauses after extracting them from file in ParaEng
		if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) {
	String[] ijkl=null;
	ijkl = ParaEng.Optional.replaceAll("[{},\\]\\[]", "").replaceAll("OPTIONAL", "").replaceAll("null","").replaceAll("[$]", "?").split(" ");;
	if(ParaEng.Optional.equals(" "))
	ijkl = ParaEng.Union.split(" ");//.replaceAll("[{},\\]\\[]", "").replaceAll("UNION", "").replaceAll("null","").split(" ");;
  List<Integer> namePosition= new ArrayList<>();
  String name= new String();
  
	String obj1 = new String();
	String sub1 = new String();//
	////logger.info("This is now the StageGenOptional"+ijkl.length+"--"+jk);
	//while(Operators_BushyIterator.hasNext()) 
//		//logger.info("This is bushyTree Subject"+Operators_BushyIterator.next());


	for(int i=0;i<ijkl.length;i++) {
		if(ijkl[i].contains("\'") || ijkl[i].contains("\""))
			namePosition.add(i);
	}
	if(namePosition.isEmpty()==false)
	for(int i=namePosition.get(0);i<=namePosition.get(1);i++)
		name=name+" "+ijkl[i];
	name=name.replaceAll("['\"]", "");
	
	//logger.info("These are the positions of name of object:"+namePosition+"--"+name);	
	
	while(Operators_BushyIterator.hasNext()) {
	EdgeOperator xy = Operators_BushyIterator.next();
				if(xy.getEdge().getTriple().getSubject().isURI())
					sub1=xy.getEdge().getTriple().getSubject().toString().replace("[<>]", "");
					else if(xy.getEdge().getTriple().getSubject().isLiteral())
						sub1=xy.getEdge().getV1().getNode().getName().toString();
					else
						sub1	=xy.getEdge().getV1().getNode().toString();

				
				if(xy.getEdge().getTriple().getObject().isURI())
					obj1=xy.getEdge().getTriple().getObject().toString().replace("[<>]", "");
					else if(xy.getEdge().getTriple().getObject().isLiteral())
						obj1=xy.getEdge().getV2().getNode().getName().toString();
					else
						obj1	=xy.getEdge().getV2().getNode().toString();
				
				for(int i=0;i<ijkl.length;i++) {
					if(!ijkl[i].equals("")) {
						
						if(namePosition.isEmpty()==false && (i+2)==namePosition.get(0)) {
							if( sub1.toString().equals(ijkl[i].toString()) && ((i+2)==namePosition.get(0))&& (" "+obj1).toString().equals(name.toString())) {
								//logger.info("This is bushyTree Subject"+i+"--"+sub1+"--"+obj1);
							operators_optional.add(xy);
							Operators_BushyIterator.remove();
							}
						}
else {	
//	//logger.info("This is bushyTree Subject:"+i+"--"+sub1.toString()+"--"+ijkl[i].toString());
//	//logger.info("This is bushyTree Object:"+i+"--"+obj1.toString()+"--"+ijkl[i+2].toString());
	
			if(sub1.toString().equals(ijkl[i].toString().replace("\t", ""))&&obj1.toString().equals(ijkl[i+2].toString())) {
			//logger.info("This is bushyTree Subject"+i+"--"+sub1+"--"+obj1);
			for(Entry<EdgeOperator, Integer> xz:joinGroupUnion.entrySet())
				if(xz.getKey().equals(xy))
					joinGroupUnion.replace(xy, 1);
			operators_optional.add(xy);
			Operators_BushyIterator.remove();
			}
}		
}
	}
 	}
	}
	
	
	//	for( Entry<EdgeOperator,Integer> xz:joinGroupUnion.entrySet()) {
	//		System.out.println("These are the union status:"+xz);
	//	}
//		System.out.println("1Getting individual operators 4 element:"+"--"+operators_optional);
//		System.out.println("2Getting individual operators 4 element:"+"--"+operators_BushyTree);

		ConcurrentHashMap<EdgeOperator,HashSet<Integer>> newFormationM = new ConcurrentHashMap<>();

		
		
	//	ForkJoinPool fjp = new ForkJoinPool(6);
	//	fjp.submit(()->
		joinGroups2 = CreateBushyTreesExclusive(operators_BushyTree,joinGroupsExcl,operators,operators_BushyTreeOrder);//);
//try {
//	fjp.awaitTermination(100, TimeUnit.MILLISECONDS);
//} catch (InterruptedException e2) {
	// TODO Auto-generated catch block
//	e2.printStackTrace();
//}

//fjp.shutdownNow();

//for(Entry<List<EdgeOperator>, Integer>  ee:joinGroups2.entrySet()) {
//System.out.println("this is very good chance:"+ee);	
//}

int ll=0;
/////////////////////////////Seperating SourceStatements from Exclusive Group and forming Left/Right Bushy Tree ////////////////////////
for(Entry<List<EdgeOperator>,Integer> e4:joinGroups2.entrySet()) {
	for(EdgeOperator e6:e4.getKey()) {
	
		if(newFormation.size()==0 || !newFormation.containsKey(e6)) {
			HashSet<Integer> bb = new HashSet<>();
			bb.add(e4.getValue());
			newFormation.put(e6,bb);
	
		}
		else {
			for(Entry<EdgeOperator, HashSet<Integer>> nf:newFormation.entrySet())
			{
				if(nf.getKey().equals(e6))
			{
				HashSet<Integer> bb = new HashSet<>();
			bb.addAll(nf.getValue());
			bb.add(e4.getValue());
		if(bb.size()>1)
			newFormation.put(e6,bb);
			}
				
			}
		}
	//	//logger.info("This is the new new new:"+e4.getKey()+"--"+e6);
		}
	
	JoinGroupsListExclusive.add(e4.getKey());	
}
Set<EdgeOperator> URIbased = new HashSet<>();
Set<EdgeOperator> uris = new HashSet<>();

for(List<EdgeOperator> nf1:JoinGroupsListExclusive)
for(EdgeOperator nf:nf1)
	if(nf.getEdge().getV1().getNode().isURI() || nf.getEdge().getV2().getNode().isURI())
{URIbased.add(nf);

System.out.println("This is here in uris condition00001111:"+nf);	
}
for(EdgeOperator uri:URIbased)
	for(EdgeOperator uri1:URIbased) {
		System.out.println("This is here in uris condition:"+uri+"--"+uri1);	

	if((uri.getEdge().getV1().equals(uri1.getEdge().getV2()) || uri.getEdge().getV2().equals(uri1.getEdge().getV1()))
			&&( uri.getEdge().getV1().getNode().isURI() || uri.getEdge().getV2().getNode().isURI())
			&& ( uri1.getEdge().getV1().getNode().isURI() || uri1.getEdge().getV2().getNode().isURI()))
		{uris.add(uri);uris.add(uri1);}
	}
	System.out.println("This is here in uris:"+uris);	

//for(Entry<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator, HashSet<Integer>> nf:newFormation.entrySet())
////logger.info("This is the new new new11:"+newFormation);
for(Entry<EdgeOperator, HashSet<Integer>> nf:newFormation.entrySet())
{
	
	
	
	if(nf.getValue().size()>1)
	{		newFormationM.put(nf.getKey(),nf.getValue());

	}
	}
for(EdgeOperator uri:URIbased)
	System.out.println("This is here in uris condition0000:"+uri);	



//System.out.println("This is Old joinGroup2:"+newFormationM);
//System.out.println("This is Old joinGroup333:"+JoinGroupsListExclusive);

//LinkedHashMap<Integer, List<EdgeOperator>> uuu;
//HashSet<EdgeOperator> ExistingEdges = new HashSet<>(); 

operators_BushyTreeLeft.clear();

		operators_BushyTreeRight.clear();
		
		/*		int isDoubled=0;
		int isTripled=0;
		int CompletlyNewFormation=0;
		List<EdgeOperator> leSafe= new ArrayList<>();
		List<EdgeOperator> leSafeNonConcrete= new ArrayList<>();
		
		Iterator<List<EdgeOperator>> jgle = JoinGroupsListExclusive.iterator()	;
		while(jgle.hasNext()) {
			List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator> jglen = jgle.next();
			//CompletlyNewFormation=0;
			for(Entry<EdgeOperator, HashSet<Integer>> nf:newFormationM.entrySet())
			{
				//Iterator<List<EdgeOperator>> le = JoinGroupsListExclusive.iterator()	;
				
			//	while(le.hasNext())
			//	{
			//		List<EdgeOperator> le2 = le.next();
			Iterator<EdgeOperator> le3 = jglen.iterator();
			EdgeOperator le1;
			while(le3.hasNext()) {
			le1=	le3.next();
			if(le1.getEdge().getTriple().getSubject().isURI() ||le1.getEdge().getTriple().getSubject().isLiteral())
			{ isTripled++;
		//	if(!ConcreteEdge.contains(le1.getEdge().getTriple().getObject()))
				
			//ConcreteEdge.add(le1.getEdge().getTriple().getObject());
				continue;
			}
			
			if( le1.getEdge().getTriple().getObject().isURI() ||le1.getEdge().getTriple().getObject().isLiteral())
			{ isTripled++;
			//if(!ConcreteEdge.contains(le1.getEdge().getTriple().getSubject()))
			//C/oncreteEdge.add(le1.getEdge().getTriple().getSubject());
				continue;
			}
			
			
		/*	if(!nf.getKey().getEdge().getTriple().getObject().isConcrete() && !nf.getKey().getEdge().getTriple().getSubject().isConcrete())
			{	for(Node ce: ConcreteEdge) {
				if(nf.getKey().getEdge().getTriple().getObject().equals(ce) )
					CompletlyNewFormation=1;
				//	//logger.info("");
			if( nf.getKey().getEdge().getTriple().getSubject().equals(ce))
					CompletlyNewFormation=1;	
					////logger.info("");
				}
			}	
			
			
				if(CompletlyNewFormation==0)
						{		isDoubled++;
						
						//	//log.info("This is total size of triple group:"+"--"+jglen.size());
							if(!leSafeNonConcrete.contains(nf.getKey()))
							leSafeNonConcrete.add(nf.getKey());
						}
				CompletlyNewFormation=1;
			
							if((nf.getKey().getEdge().getTriple().getSubject().toString().equals(le1.getEdge().getTriple().getObject().toString())
							||	nf.getKey().getEdge().getTriple().getObject().toString().equals(le1.getEdge().getTriple().getSubject().toString()))	
									&& !nf.getKey().getEdge().toString().equals(le1.getEdge().toString())) 
							{
			if ((le1.getEdge().getTriple().getSubject().isConcrete() && !le1.getEdge().getTriple().getObject().isConcrete()))
			//isDoubled++;
				isTripled++;
				if ((le1.getEdge().getTriple().getObject().isConcrete() && !le1.getEdge().getTriple().getSubject().isConcrete()))
				//isDoubled++;
					isTripled++;		
				if ((nf.getKey().getEdge().getTriple().getSubject().isConcrete() && !nf.getKey().getEdge().getTriple().getObject().isConcrete()))
					//isDoubled++;
						isTripled++;
						if ((nf.getKey().getEdge().getTriple().getObject().isConcrete() && !nf.getKey().getEdge().getTriple().getSubject().isConcrete()))
						//isDoubled++;
							isTripled++;		
							}
							leSafe.add(le1);
							
							}
			if(isTripled>1)
			{
				//isTripled=0;
				continue;
			}

				
				
				if(isTripled==0 ) {
					for(Entry<EdgeOperator, HashSet<Integer>> nf1:newFormationM.entrySet())
					{
					
					if(jglen.contains(nf1.getKey()))
			jglen.remove(nf1.getKey());
					if(!operators_BushyTreeLeft.contains(nf1.getKey()) && ((!nf1.getKey().getEdge().getTriple().getSubject().isURI() ||!nf1.getKey().getEdge().getTriple().getSubject().isLiteral()) || (!nf1.getKey().getEdge().getTriple().getObject().isURI() || !nf1.getKey().getEdge().getTriple().getObject().isLiteral())) )
			{operators_BushyTreeLeft.add(nf1.getKey());
			operators_BushyTreeRight.add(nf1.getKey());
			}
			
			if(jglen.size()==1) {
			if(!operators_BushyTreeLeft.toString().contains(jglen.toString()))
			{	operators_BushyTreeLeft.addAll(jglen);
			operators_BushyTreeRight.addAll(jglen);

			}
			
		//	jgle.remove();
			}
				}
			
				


			
				}
		
		
//		isDoubled=0;
		
				}
			}
//		}
		

		
		if(isDoubled>0) {
			//for(Entry<EdgeOperator, HashSet<Integer>> nf1:newFormationM.entrySet())
			//{
			for(EdgeOperator le11:leSafeNonConcrete)
			{
			EdgeOperator a11=	le11;
				for(int i=0;i<JoinGroupsListExclusive.size();i++)
			if(!JoinGroupsListExclusive.get(i).toString().contains("http")) {
					JoinGroupsListExclusive.get(i).remove(a11);
						if(!operators_BushyTreeLeft.contains(le11) )
						{
						operators_BushyTreeLeft.add(le11);
						operators_BushyTreeRight.add(le11);
						}
			}
				//	}	
								
			//		}
		//	}
		//}
			}
		
		
		}

	*/	
	
/*	for(int i=0 ;i<JoinGroupsListExclusive.size();i++)
		for(int j=0;j<JoinGroupsListExclusive.get(i).size();j++)
			for(Node ce:ConcreteEdge)
		if(!JoinGroupsListExclusive.get(i).get(j).getEdge().getTriple().getObject().equals(ce) && !JoinGroupsListExclusive.get(i).get(j).getEdge().getTriple().getSubject().equals(ce))		
			{
			operators_BushyTreeLeft.add(JoinGroupsListExclusive.get(i).get(j));
			operators_BushyTreeRight.add(JoinGroupsListExclusive.get(i).get(j));
			JoinGroupsListExclusive.get(i).remove(j);
			}
*/
List<EdgeOperator> inclusion = new ArrayList<>();		
Iterator<List<EdgeOperator>> jgIterator = JoinGroupsListExclusive.iterator();//joinGroups2.keySet().iterator();
int ij1=0;
while(jgIterator.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator.next();
	for(Entry<EdgeOperator, HashSet<Integer>>  nfm:newFormationM.entrySet())
{
	
		if(aa.contains(nfm.getKey())) {
	inclusion.add(nfm.getKey());
//	jgIterator.remove();;
	if(!uris.contains(nfm.getKey())) {
	operators_BushyTreeLeft.add(nfm.getKey());
	operators_BushyTreeRight.add(nfm.getKey());
	}
	System.out.println("this is problem11111:"+aa);

//}
//}
//}
                  }
	}
	}

jgIterator = JoinGroupsListExclusive.iterator();//joinGroups2.keySet().iterator();

while(jgIterator.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator.next();
	for(EdgeOperator e:inclusion)
 aa.remove(e);
}
List<EdgeOperator> namesList1 = uris.parallelStream().collect(Collectors.toList());
//namesList.addAll(uris);
JoinGroupsListExclusive.add(namesList1);	
//JoinGroupsListExclusive.remove(inclusion);
/*
Iterator<List<EdgeOperator>> jgIterator1 = JoinGroupsListExclusive.iterator();
int ij=0;

while(jgIterator1.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator1.next();
if(aa.size()==1  ) {
for(EdgeOperator aaa:aa) {
//	//log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
	for(List<EdgeOperator> jge:JoinGroupsListExclusive)
		if((aaa.getEdge().getTriple().getObject().isConcrete()  || aaa.getEdge().getTriple().getSubject().isConcrete())
				&& jge.size()>1)
			for(EdgeOperator jgle:jge) {
				if(jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getSubject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getSubject()))
				{	jge.add(aaa);
				aa.remove(aaa);
				}
			ij=1;	
			break;
			}
	if(ij==1)
		break;

	if(ij==1) {ij=0; break;}

}

}
}

Iterator<List<EdgeOperator>> jgIterator11 = joinGroups2.values().iterator();
int ij11=0;

while(jgIterator11.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator11.next();
if(aa.size()==1  ) {
for(EdgeOperator aaa:aa) {
//	//log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
	for(Entry<Integer, List<EdgeOperator>> jgel:joinGroups2.entrySet())
		if((aaa.getEdge().getTriple().getObject().isConcrete()  || aaa.getEdge().getTriple().getSubject().isConcrete())
				&& jgel.getValue().size()>1)
			for(EdgeOperator jgle:jgel.getValue()) {
				if(jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getSubject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getSubject()))
				{	jgel.getValue().add(aaa);
				aa.remove(aaa);
				}
			ij11=1;	
			break;
			}
	if(ij11==1)
		break;

	if(ij11==1) {ij11=0; break;}

}

}
}

*//*
Iterator<List<EdgeOperator>> jgIterator111 = JoinGroupsListExclusive.iterator();
int ij111=0;

while(jgIterator111.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator111.next();
if(aa.size()<=newFormationM.size() && aa.size()>1  ) {
for(EdgeOperator aaa:aa) {
//	//log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
	for(Entry<EdgeOperator, HashSet<Integer>> nfm:newFormationM.entrySet())
		if(aaa.equals(nfm.getKey()) && !aaa.getEdge().getTriple().getSubject().isConcrete()&& !aaa.getEdge().getTriple().getObject().isConcrete())
			ij111++;
if(ij111==aa.size())	
	if(aa.size()>1 || aa.isEmpty()==false)
{

	//	System.out.println("1this is problem:"+aa);
	//if(!operators_BushyTreeRight.equals(aa)) {
	
//	operators_BushyTreeRight.addAll(aa);
//	operators_BushyTreeLeft.addAll(aa);
//	}
//	jgIterator111.remove();
	//System.out.println("1this is problem11:"+aa);

}
}
}
}
*/

/*
Iterator<List<EdgeOperator>> obtIterator99;
Iterator<List<EdgeOperator>> jg2Iterator99;
List<EdgeOperator> obt99;
boolean isString=false;
obtIterator99 = JoinGroupsListExclusive.iterator();
while(obtIterator99.hasNext()) {
   obt99 = obtIterator99.next();
   for(int i=0; i<obt99.size();i++) {
	   if(obt99.get(i).getEdge().getTriple().getObject().isConcrete() || obt99.get(i).getEdge().getTriple().getSubject().isConcrete())
		   {isString=true;
		   break;
		   }
   }
   if(isString==true) {
	   isString=false;
	 jg2Iterator99 = JoinGroupsListExclusive.iterator();
List<EdgeOperator> 	jg99;
while(jg2Iterator99.hasNext())	{	
		jg99=jg2Iterator99.next();
		if(!obt99.equals(jg99))
		
		for(EdgeOperator jg991:jg99)
		{	if(obt99.contains(jg991))
				jg99.remove(jg991);
		}
	}
   }

	}
*/


Iterator<List<EdgeOperator>> jgIterator3 = joinGroups2.keySet().iterator();

while(jgIterator3.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator3.next();
if(aa.size()==1)
{

	if(!operators_BushyTreeRight.equals(aa) ) {
			
		
	operators_BushyTreeRight.addAll(aa);
	operators_BushyTreeLeft.addAll(aa);
	}
	jgIterator3.remove();
}
}
for(EdgeOperator uri:uris) {
	operators_BushyTreeRight.remove(uri);
	operators_BushyTreeLeft.remove(uri);

}
////logger.info("This is the task of eternity:"+joinGroups2);
Iterator<List<EdgeOperator>> jgIterator1111 = JoinGroupsListExclusive.iterator();


while(jgIterator1111.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator1111.next();
if(aa.size()==1) {
	if(!operators_BushyTreeRight.equals(aa)) {
	operators_BushyTreeRight.addAll(aa);
	operators_BushyTreeLeft.addAll(aa);
	}

	jgIterator1111.remove();
}
}	
	
/////////////////////////////Seperating SourceStatements from Exclusive Group and forming Left/Right Bushy Tree ////////////////////////

/////////////////////////////Ordering JoinGroupsListExclusive ////////////////////////



////////////////////////////////////////////////////////////////////////////
/*Iterator<List<EdgeOperator>> jgIterator6 = JoinGroupsListExclusive.iterator();

List<EdgeOperator> jg = new ArrayList<EdgeOperator>();
while(jgIterator6.hasNext())
{int ij6=0;

List<EdgeOperator> aa = new ArrayList<>();
aa=jgIterator6.next();
if(aa.size()>1  ) {
for(Entry<EdgeOperator, HashSet<Integer>> nfm:newFormationM.entrySet())
{
	int isConcrete=0;
	for(List<EdgeOperator> joinG:JoinGroupsListExclusive) {
		for(EdgeOperator joinG1:joinG) {
			if(joinG1.getEdge().getTriple().getObject().equals(nfm.getKey().getEdge().getTriple().getSubject()) && joinG1.getEdge().getTriple().getSubject().isConcrete())
				isConcrete=1;
		}
	}
	
	if(aa.contains(nfm.getKey()) && !nfm.getKey().getEdge().getTriple().getObject().isConcrete() && !nfm.getKey().getEdge().getTriple().getSubject().isConcrete() && isConcrete==0)
{ij6++;
jg.add(nfm.getKey());
}
}
if(aa.size()>=ij6 &&ij6>0)	
//for(EdgeOperator aaa:jg) {
////log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
for(EdgeOperator j:jg) {
if(!operators_BushyTreeRight.contains(j)) {
operators_BushyTreeRight.addAll(jg);
operators_BushyTreeLeft.addAll(jg);
}
aa.remove(j);

}
//}

}

}



Iterator<List<EdgeOperator>> jgIterator7 = joinGroups2.values().iterator();
List<EdgeOperator> jg1 = new ArrayList<EdgeOperator>();

while(jgIterator7.hasNext())
{int ij7=0;
List<EdgeOperator> aa = new ArrayList<>();
aa=jgIterator7.next();
if(aa.size()>1  ) {
for(Entry<EdgeOperator, HashSet<Integer>> nfm:newFormationM.entrySet())
{
	int isConcrete=0;
	for(List<EdgeOperator> joinG:JoinGroupsListExclusive) {
		for(EdgeOperator joinG1:joinG) {
			if(joinG1.getEdge().getTriple().getObject().equals(nfm.getKey().getEdge().getTriple().getSubject()) && joinG1.getEdge().getTriple().getSubject().isConcrete())
				isConcrete=1;
		}
	}
	
	if(aa.contains(nfm.getKey()) && !nfm.getKey().getEdge().getTriple().getObject().isConcrete() && !nfm.getKey().getEdge().getTriple().getSubject().isConcrete() && isConcrete==0)
{ij7++;
jg.add(nfm.getKey());

}
}
if(aa.size()>=ij7 &&ij7>0)	
//for(EdgeOperator aaa:jg) {
////log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
for(EdgeOperator j:jg1) {
if(!operators_BushyTreeRight.contains(j)) {
operators_BushyTreeRight.addAll(jg1);
operators_BushyTreeLeft.addAll(jg1);
}
aa.remove(j);
}
//}

}
}*/

ForkJoinPool fjp98 = new ForkJoinPool();
fjp98.submit(()->
futileTriple());
fjp98.shutdown();



ForkJoinPool fjp97 = new ForkJoinPool();
fjp97.submit(()->
refineTriple(newFormationM,operators_BushyTreeLeft,operators_BushyTreeRight));
fjp97.shutdown();


for(int i=0;i<JoinGroupsListExclusive.size();i++)
{
	if(JoinGroupsListExclusive.get(i).size()==1)
	{
		operators_BushyTreeRight.addAll(JoinGroupsListExclusive.get(i));
		operators_BushyTreeLeft.addAll(JoinGroupsListExclusive.get(i));

		JoinGroupsListExclusive.remove(i);
	}
}
/*
obtIterator=null;
jg2Iterator=null;
ax=null;
obt=null;
obtIterator = joinGroups2.values().iterator();
while(obtIterator.hasNext()) {
	int comparedSize=0;
	obt = obtIterator.next();
jg2Iterator = joinGroups2.values().iterator();
while(jg2Iterator.hasNext())	{
	List<EdgeOperator> 	jg2=jg2Iterator.next();
		size=jg2.size();
	if(obt.size()<size) {
		
		for(EdgeOperator jg22:jg2)
		{	if(obt.contains(jg22))
				comparedSize++;
		}
		ax.add(obt);
		//log.info("This is the size for whole1:"+obt.size()+"--"+size);;

		obtIterator.remove();
		
	}

	//log.info("THis is exclussss1:"+JoinGroupsListExclusive);

	
	}
}
*/////////////////////////////////////////////////////////////////////////////

////logger.info("This is the task of eternity222222222:"+JoinGroupsListExclusive);



////logger.info("This is now operators_BushyTreeRight:"+operators_BushyTreeRight);

////////////////////////////////////////////////////////////////////////////

//logger.info("This is the task of eternity222222222:"+JoinGroupsListExclusive);


List<EdgeOperator> JoinGroupsListExclusiveTemp1 =new Vector<>();
for(EdgeOperator obt1:operators_BushyTreeOrder)
	for(EdgeOperator jg2:operators_BushyTreeRight)
		if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp1.contains(jg2) )
			JoinGroupsListExclusiveTemp1.add(jg2);

operators_BushyTreeRight.clear();
operators_BushyTreeLeft.clear();
Collections.reverse(JoinGroupsListExclusiveTemp1);
operators_BushyTreeRight.addAll(JoinGroupsListExclusiveTemp1);
operators_BushyTreeLeft.addAll(JoinGroupsListExclusiveTemp1);
//operators_BushyTreeRight.parallelStream();


/////////////////////////////Ordering JoinGroupsListExclusive ////////////////////////

//for(List<EdgeOperator> jgle1:JoinGroupsListExclusive)
////logger.info("This is now JoinGroupsListExclusive:"+jgle1);

//for(Entry<Integer, List<EdgeOperator>> jgle1:joinGroups2.entrySet())
////logger.info("This is now JoinGroups2:"+jgle1);

////logger.info("This is now the original order:"+operators_BushyTreeOrder);




////logger.info("This is JoinGroupsListExclusive:"+JoinGroupsListExclusive);
////logger.info("This is operators_BushyTreeLeft:"+operators_BushyTreeLeft);

Iterator<List<EdgeOperator>> jglll = JoinGroupsListExclusive.iterator()	;
while(jglll.hasNext()) {
List<EdgeOperator> jglenl = jglll.next();
//if(jglenl.size()==1) {



Iterator<EdgeOperator> jgl111 = operators_BushyTreeLeft.iterator();
while(jgl111.hasNext()) {
if(jglenl.contains(jgl111.next()))
{	jgl111.remove();  ;

}
}


//}
}


	if(operators_BushyTreeLeft.size()>0)
{joinGroupsLeft = CreateBushyTreesLeft(operators_BushyTreeLeft,operators,operators_BushyTreeOrder);




for(Entry<EdgeOperator, Integer> e: joinGroupsLeft.entries()) {
	//logger.info("This is the new group of queries123123 left:"+e.getKey()+"--"+e.getValue());
		if(!JoinGroupsListLeft.contains(e.getKey()))
	JoinGroupsListLeft.add(e.getKey());
			}	
	
 
Iterator<EdgeOperator> jgll = JoinGroupsListLeft.iterator()	;
while(jgll.hasNext()) {
com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator jglenl = jgll.next();
//if(jglenl.size()==1) {
	if(operators_BushyTreeRight.toString().contains(jglenl.toString()))
{	operators_BushyTreeRight.remove(jglenl);

}
//}
}

Iterator<List<EdgeOperator>> jgrrr = JoinGroupsListExclusive.iterator()	;
while(jgrrr.hasNext()) {
List<EdgeOperator> jglenr = jgrrr.next();
//if(jglenl.size()==1) {



Iterator<EdgeOperator> jgr111 = operators_BushyTreeRight.iterator();
while(jgr111.hasNext()) {
if(jglenr.contains(jgr111.next()))
{	jgr111.remove();  ;

}
}
}
Iterator<EdgeOperator> jgrrl = JoinGroupsListLeft.iterator()	;
while(jgrrl.hasNext()) {
EdgeOperator jglenrl = jgrrl.next();
//if(jglenl.size()==1) {



Iterator<EdgeOperator> jgr1112 = operators_BushyTreeRight.iterator();
while(jgr1112.hasNext()) {
if(jglenrl.equals(jgr1112.next()))
{	jgr1112.remove();  ;

}
}
}
if(operators_BushyTreeRight!=null)
joinGroupsRight = CreateBushyTreesRight(operators_BushyTreeRight,operators,operators_BushyTreeOrder);



if(joinGroupsRight!=null)
for(Entry<EdgeOperator, Integer> e: joinGroupsRight.entries()) {
	//logger.info("This is the new group of queries123123:"+e.getKey()+"--"+e.getValue());
	if(!JoinGroupsListRight.contains(e.getKey()))
	JoinGroupsListRight.add(e.getKey());
			}	
}
		/////logger.info("This is now right bushy tree:"+joinGroups1);

	System.out.println("This is optional material:"+operators_optional);
	if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
			joinGroupsOptional1 = CreateBushyTreesExclusive(operators_optional,joinGroupsOptional,operators,operators_BushyTreeOrder);


		List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator> itr;

	//for(int i=0;i<JoinGroupsList.size();i++)
	/*for(List<EdgeOperator> jglr:JoinGroupsListExclusive)
			for(EdgeOperator jglr1:jglr)
	if(JoinGroupsList.toString().contains(jglr1.toString())) {			
		for(List<EdgeOperator> jgl:JoinGroupsList)
		

	}*/
		//logger.info("This is left group tree:"+JoinGroupsListLeft);

		//logger.info("This is right group tree:"+JoinGroupsListRight);
		Iterator<List<EdgeOperator>>  xyz=JoinGroupsListExclusive.iterator();
		while(xyz.hasNext()) {
			List<EdgeOperator> xyz1 = xyz.next();
			if(xyz1.size()==0)
				xyz.remove();
		}
		
		
		////logger.info("This is the new group list of JoinGroupsListRight:"+operators_BushyTreeRight);

//		//logger.info("This is the new group list of JoinGroupsListLeft:"+operators_BushyTreeLeft);

			//ExecOrder
		//logger.info("This is now operators_BushyTreeRight:"+operators_BushyTreeRight);
		Map< List<EdgeOperator>, Integer> joinGroups2Temp1 = new  HashMap<>();
		List<EdgeOperator> JoinGroupsListExclusiveTemp =new Vector<>();
		List<List<EdgeOperator>> JoinGroupsListExclusiveTempT =new Vector<>();
	/*		
		for(EdgeOperator obt1:operators_BushyTreeOrder)
		{
			for(List<EdgeOperator> jg2:JoinGroupsListExclusive) {
				
				for(EdgeOperator jg22:jg2) {
				if(jg22.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp.contains(jg22) )
					JoinGroupsListExclusiveTemp.add(jg22);
				}
					
			}
				if(!JoinGroupsListExclusiveTempT.contains(JoinGroupsListExclusiveTemp))
					JoinGroupsListExclusiveTempT.add(JoinGroupsListExclusiveTemp);
				JoinGroupsListExclusiveTemp =new	Vector<>();
				
		}

		
	//	Map<List<EdgeOperator>, Integer> JoinGroupsListExclusiveTempTV =new ConcurrentHashMap<>();
			
	//	for(Entry<List<EdgeOperator>, Integer> jg2:joinGroups2.entrySet()) 
	//		for(EdgeOperator obt1:operators_BushyTreeOrder)
	//		if(jg2.getKey().get(0).getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTempTV.containsKey(jg2.getKey()) )
	//			JoinGroupsListExclusiveTempTV.put(jg2.getKey(),jg2.getValue());
		
				
			


		//joinGroups2.clear();

//		joinGroups2.putAll(JoinGroupsListExclusiveTempTV);

		for(EdgeOperator obt1:operators_BushyTreeOrder)
			for(Entry<List<EdgeOperator>, Integer> jg2:joinGroups2.entrySet())
				if(jg2.getKey().size()>0)	
				if(obt1.toString().equals(jg2.getKey().get(0).toString()))
					joinGroups2Temp1.put(jg2.getKey(),jg2.getValue());

		joinGroups2.clear();
		joinGroups2.putAll(joinGroups2Temp1);
*/if(JoinGroupsListRight.size()>0 || !JoinGroupsListRight.isEmpty()) {
		JoinGroupsListExclusiveTemp =new	Vector<>();
		for(EdgeOperator jg2:JoinGroupsListRight) {
			for(EdgeOperator obt1:operators_BushyTreeOrder)
			{
			if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp.contains(jg2) )
				JoinGroupsListExclusiveTemp.add(jg2);
			
				
		}
			
	}


		JoinGroupsListRight.clear();

		JoinGroupsListRight.addAll(JoinGroupsListExclusiveTemp);


}
if(JoinGroupsListLeft.size()>0 || !JoinGroupsListLeft.isEmpty()) {

JoinGroupsListExclusiveTemp =new	Vector<>();
for(EdgeOperator obt1:operators_BushyTreeOrder)
{		
for(EdgeOperator jg2:JoinGroupsListLeft) {
			if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp.contains(jg2) )
				JoinGroupsListExclusiveTemp.add(jg2);
			
				
		}
			
	}


		JoinGroupsListLeft.clear();

		JoinGroupsListLeft.addAll(JoinGroupsListExclusiveTemp);
}
////logger.info("There is only one hashJoin");
		if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
		for(Entry<List<EdgeOperator>, Integer> e: joinGroupsOptional1.entrySet()) {
		//logger.info("This is the new group of queries:"+e.getKey()+"--"+e.getValue());
if(e.getKey().size()>0)
	JoinGroupsListOptional.add(e.getKey());
			
		}
		System.out.println("This is optional group tree:"+joinGroupsOptional1);
		
//		LinkedHashSet<EdgeOperator> lhs = new LinkedHashSet<EdgeOperator>();
		List<EdgeOperator> lhs = new ArrayList<EdgeOperator>();
		if(ParaEng.Optional.contains("OPTIONAL")|| ParaEng.Union.contains("UNION")) 
			if(JoinGroupsListOptional.size()>1)
		{CompleteOptional(newFormationM,operators,operators_optional);
		}
		System.out.println("This is optional group tree:111"+joinGroupsOptional1);
		
//		//logger.info("There is only one hashJoin1");
		
		//Case1: If there is only one record with first element as BindJoin
	//Case2: If most upper record has BindJoin at start then compare it with
	//below one if the total cost of next record is less then let program swap
	//that record
	//Case3: Incase in a record first element is bindJoin and its cost with next HashJoin
	//is less than 500 then swap both
//if(JoinGroupsListLeft!=null)
//	BindJoinCorrection(JoinGroupsListLeft);
//if(JoinGroupsListRight!=null)
//		BindJoinCorrection(JoinGroupsListRight);

		
		


/*		
		JoinGroupsListExclusiveTemp1 =new Vector<>();
		for(EdgeOperator obt1:operators_BushyTreeOrder)
			for(EdgeOperator jg2:JoinGroupsListRight)
				if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp1.contains(jg2) )
					JoinGroupsListExclusiveTemp1.add(jg2);

		operators_BushyTreeRight.clear();
		operators_BushyTreeLeft.clear();
	//	Collections.reverse(JoinGroupsListExclusiveTemp1);
		JoinGroupsListRight.addAll(JoinGroupsListExclusiveTemp1);

		
		JoinGroupsListExclusiveTemp1 =new Vector<>();
		for(EdgeOperator obt1:operators_BushyTreeOrder)
			for(EdgeOperator jg2:JoinGroupsListLeft)
				if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp1.contains(jg2) )
					JoinGroupsListExclusiveTemp1.add(jg2);

		operators_BushyTreeRight.clear();
		operators_BushyTreeLeft.clear();
	//	Collections.reverse(JoinGroupsListExclusiveTemp1);
		JoinGroupsListLeft.addAll(JoinGroupsListExclusiveTemp1);
*/
	HashSet<List<EdgeOperator>>  	JoinGroupsListLeftTemp = new HashSet<>();
	List<List<EdgeOperator>>  	JoinGroupsListRightTemp = new Vector<>();
/*	for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, Set<Binding>>  el:StartBinding.entrySet())
		StartBindingBJ.put(el.getKey(),el.getValue());
		//List<List<EdgeOperator>>  	JoinGroupsListRightTemp = new Vector<>();
		//List<List<EdgeOperator>>  	JoinGroupsListLeftTemp = new Vector<>();
		JoinGroupsListLeftTemp.add(JoinGroupsListLeft);
		JoinGroupsListLeftTemp.add(JoinGroupsListRight);
		JoinGroupsListLeftTemp.addAll(JoinGroupsListExclusive);
		if(ParaEng.Optional.contains("OPTIONAL")|| ParaEng.Union.contains("UNION")) 
		JoinGroupsListLeftTemp.addAll(JoinGroupsListOptional);
			enableBindJoins(JoinGroupsListLeftTemp);//for(List<EdgeOperator> e: JoinGroupsList) 
*/ //	ForkJoinPool fjp1 =new ForkJoinPool(6);
//fjp1.submit(()->{	
/*	if(JoinGroupsListRight.size()>0 || !JoinGroupsListRight.isEmpty())
	BindJoinCorrectionRight(JoinGroupsListRight,0);
	if(JoinGroupsListLeft.size()>0 || !JoinGroupsListLeft.isEmpty())
	BindJoinCorrectionLeft(JoinGroupsListLeft,0);
	
	JoinGroupsListRightTemp.clear();
	JoinGroupsListLeftTemp.clear();

	if(JoinGroupsListLeftOptional.size()>0 || !JoinGroupsListLeftOptional.isEmpty())
	BindJoinCorrectionLeft(JoinGroupsListLeftOptional,1);
	if(JoinGroupsListRightOptional.size()>0 || !JoinGroupsListRightOptional.isEmpty())
	BindJoinCorrectionRight(JoinGroupsListRightOptional,1);
*///});
//	fjp1.shutdown();
	//Incase of 1 element reverse elements in list
/*	int NumberOfHashJoin=0;
	int HashJoinIndex=0;
	for(Entry<List<EdgeOperator>, Integer> e: joinGroups2.entrySet()) {
		for( EdgeOperator e1:e.getKey())	
			if(e1.toString().contains("Hash"))
			{
				
				NumberOfHashJoin++;
				HashJoinIndex = e.getValue();
			}
	}
	
	
//	//logger.info("There is only one hashJoin2");

	if(NumberOfHashJoin==1)
	{	//logger.info("There is only one hashJoin");


	for(Entry<Integer, List<EdgeOperator>> e: joinGroups.entrySet()) {
		if(HashJoinIndex==e.getKey())
			Collections.reverse( e.getValue());
	}
	}
*/
	//logger.info("This is the new group of queries just checking left:"+JoinGroupsListLeft);

	
	//logger.info("This is the new group of queries just checking right:"+JoinGroupsListRight);

	//logger.info("This is the new group of queries just checking excl:"+JoinGroupsListExclusive);
	//if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
	//logger.info("This is the new group of optional queries just checking:"+JoinGroupsListOptional);
	//if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
		//logger.info("This is the new group of optional queries just checking:"+JoinGroupsListLeftOptional);
//	if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
		//logger.info("This is the new group of optional queries just checking:"+JoinGroupsListRightOptional);

	
	List<List<EdgeOperator>> JoinGroupsListExclusiveTempH =new Vector<>();
	List<List<EdgeOperator>> JoinGroupsListExclusiveTempB =new Vector<>();


/*
	for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, Set<Binding>>  el:StartBindingSet.entrySet())	
	{		Set<Edge> v = new HashSet<>();
		for( Entry<Set<Vertex>, Set<Edge>> el1:el.getKey().entrySet())
		for(Edge e11:el1.getValue()) {
		for(List<EdgeOperator> jge:JoinGroupsListExclusive) {
		for(EdgeOperator jge1:jge)
		{	if(e11.equals(jge1.getEdge())) {
			for(EdgeOperator jgee:jge)	
			v.add(jgee.getEdge());
		   
		}
		}
	}
	Set<Edge>	vDup=new HashSet<>();
		for(Edge e111:el1.getValue())
			v.add(e111);
	
			for(Edge v2:v)
				for(EdgeOperator boo:operators_BushyTreeOrder)
				if(boo.getEdge().equals(v2))
					vDup.add(v2);
					el1.setValue(vDup);
		}
	}*/
	//if(xyz.isEmpty()) 
	////logger.info("This is the new group list of queries0:"+StartBindingSet);
for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, ArrayList<Binding>>  el:StartBindingSet.entrySet())
StartBindingSetBJ.put(el.getKey(),el.getValue());	
JoinGroupsListLeftTemp.add(JoinGroupsListLeft);
//JoinGroupsListLeftTemp.add(operators_BushyTreeOrder);
JoinGroupsListLeftTemp.add(JoinGroupsListRight);
JoinGroupsListLeftTemp.addAll(JoinGroupsListExclusive);

System.out.println("THis is oooooo:"+JoinGroupsListLeftTemp);
if(ParaEng.Optional.contains("OPTIONAL")|| ParaEng.Union.contains("UNION")) 
{
JoinGroupsListLeftTemp.addAll(JoinGroupsListOptional);
JoinGroupsListLeftTemp.add(JoinGroupsListLeftOptional);
JoinGroupsListLeftTemp.add(JoinGroupsListRightOptional);
}	
JoinGroupsListAll.clear();
JoinGroupsListAll.addAll(JoinGroupsListLeftTemp);
System.out.println("This is going to work00000");
HashMap<HashSet<List<EdgeOperator>>, Integer> ExecOrder = ExecutionOrder(JoinGroupsListLeftTemp);
ForkJoinPool fjp11 = new ForkJoinPool();
fjp11.submit(()->
enableBindJoins(JoinGroupsListLeftTemp)).join();
fjp11.shutdown();
//for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, ArrayList<Binding>>  b:StartBindingSetBJ.entrySet())
System.out.println("This is going to work:"+StartBindingSetBJ);

//ForkJoinPool fjp1 = new ForkJoinPool();
//fjp1.submit(()->
//BindJoinCorrection(JoinGroupsListLeftTemp));
//fjp1.shutdown();

//System.out.println("This is StartBindingSetBJ:"+StartBindingSetBJ);
//fjp11.shutdown();
	//enableBindJoins(JoinGroupsListRightTemp);//for(List<EdgeOperator> e: JoinGroupsList) {
	//enableBindJoins(JoinGroupsListLeftTemp);//for(List<EdgeOperator> e: JoinGroupsList) {
	//enableBindJoins(JoinGroupsListExclusive);//for(List<EdgeOperator> e: JoinGroupsList) {
	
//	//logger.info("There is only one hashJoin3");

		
	//	//logger.info("..........:"+JoinGroups0);
 JoinGroupsListExclusiveTempH =new Vector<>();
	JoinGroupsListExclusiveTempB =new Vector<>();

	JoinGroupsListExclusiveTempT.clear();
	for(Entry<HashSet<List<EdgeOperator>>, Integer> obt1:ExecOrder.entrySet())
	for(List<EdgeOperator> jg2:JoinGroupsListExclusive) 
	{
		for(List<EdgeOperator> obt2:obt1.getKey())
			for(EdgeOperator obt3:obt2)
		if(jg2.get(0).getEdge().equals(obt3.getEdge()))
		{
			JoinGroupsListExclusiveTempH.add(jg2);
		}
	//	if(jg2.get(0).getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp.contains(jg2) &&jg2.get(0).getStartVertex()!=null)
	//	{
		//	JoinGroupsListExclusiveTempB.add(jg2);
	//	}
		}	
	

	JoinGroupsListExclusive.clear();

	JoinGroupsListExclusive.addAll(JoinGroupsListExclusiveTempH);

	Set<EdgeOperator> JoinGroupsListExclusiveTempA=new HashSet<>();
	/*
	JoinGroupsListExclusiveTempA.clear();
	for(HashSet<List<EdgeOperator>> obt1:ExecOrder.keySet())
		for(List<EdgeOperator> obt2:obt1)
			for(EdgeOperator obt3:obt2)
	for(EdgeOperator jg2:JoinGroupsListLeft) 
	{
		if(jg2.getEdge().equals(obt3.getEdge())&&!JoinGroupsListExclusiveTempA.contains(jg2))
			JoinGroupsListExclusiveTempA.add(jg2);
	
		}	
	JoinGroupsListLeft.clear();
	JoinGroupsListLeft.addAll(JoinGroupsListExclusiveTempA);
*//*

	JoinGroupsListExclusiveTempA.clear();
	//for(HashSet<List<EdgeOperator>> obt1:ExecOrder.keySet())
	for(EdgeOperator obt1:	operators_BushyTreeOrder)	
	for(EdgeOperator jg2:JoinGroupsListRight) 
	{
		if(jg2.equals(obt1)&&!JoinGroupsListExclusiveTempA.contains(jg2))
			JoinGroupsListExclusiveTempA.add(jg2);
	
		}	
	*///JoinGroupsListRight.clear();
	
//	JoinGroupsListRight.addAll(JoinGroupsListExclusiveTempA);

	//JoinGroupsListExclusive.addAll(JoinGroupsListExclusiveTempB);

//	System.out.println("1111..........777:");

//	for(List<EdgeOperator> jgyy:JoinGroupsListExclusive)
//System.out.println("1111..........:"+jgyy);
		if(	 BGPEval.JoinGroupsListRight.size()>0 || BGPEval.JoinGroupsListLeft.size()>0
			|| BGPEval.JoinGroupsListRightOptional.size()>0||  BGPEval.JoinGroupsListLeftOptional.size()>0)													
	ExclusivelyExclusive=1;
System.out.println("This is the new group list of JoinGroupsListRight:"+JoinGroupsListRight);
System.out.println("This is the new group list of JoinGroupsListLeft:"+JoinGroupsListLeft);
System.out.println("This is the new group list of  JoinGroupsListExclusive:"+JoinGroupsListExclusive);
System.out.println("This is the new group list of  JoinGroupsListExclusive:"+ExclusivelyExclusive);
System.out.println("This is the new group list of JoinGroupsListRightOptional:"+JoinGroupsListRightOptional);
System.out.println("This is the new group list of JoinGroupsListLeftOptional:"+JoinGroupsListLeftOptional);
System.out.println("This is the new group list of  JoinGroupsListOptional:"+JoinGroupsListOptional);


length = "";
vv =null;
Iterator<List<EdgeOperator>> l5 = JoinGroupsListOptional.iterator();
while(l5.hasNext()) {
	List<EdgeOperator> l4 = l5.next();
// BindingSet xz=l1.next();
 
     length=l4.toString();
      vv=length.split(" ");
for(String v:vv)
if(v.startsWith("(") ) {
	if(!v.contains("http")&& v.contains(":") && (v.contains("$") || v.contains("?")))
   OptionalHeaders.add(v.substring(1,v.indexOf(":")));

//break;
}
else continue;
  }
//break;
Set<String> a1 = new HashSet<>();
a1.addAll(OptionalHeaders);
OptionalHeaders.clear();
OptionalHeaders.addAll(a1);
length = "";
vv =null;
l3=null;
l3=	JoinGroupsListLeftOptional.iterator();
while(l3.hasNext()) {
// BindingSet xz=l1.next();
//  while(l1.hasNext()) {
      length=l3.next().toString();
      vv=length.split(" ");
for(String v:vv)
if(v.startsWith("(") ) {
	if(!v.contains("http")&& v.contains(":") && (v.contains("$") || v.contains("?")))
		OptionalHeadersLeft.add(v.substring(1,v.indexOf(":")));

//break;
}
else continue;
//break;
  }

length = "";
vv =null;

l3=	JoinGroupsListRightOptional.iterator();
while(l3.hasNext()) {
// BindingSet xz=l1.next();
//  while(l1.hasNext()) {
      length=l3.next().toString();
      vv=length.split(" ");
for(String v:vv)
if(v.startsWith("(") ) {
	if(!v.contains("http")&& v.contains(":") && (v.contains("$") || v.contains("?")))
		OptionalHeadersRight.add(v.substring(1,v.indexOf(":")));

//break;
}
else continue;
//break;
  }
//break;




LinkedListMultimap<EdgeOperator, String>  TreeOrderOpt = LinkedListMultimap.create();
for(Entry<HashSet<List<EdgeOperator>>, Integer>  cde:linkingTreeDup.entrySet())
	for(List<EdgeOperator> cde1:cde.getKey())
	{	    for(List<EdgeOperator>jge:JoinGroupsListExclusive)
		if(jge.equals(cde1)) {
		//	ForkJoinPool fjp111= new ForkJoinPool();
				//fjp111.submit(()->{
					TreeOrder.put(cde1.get(0), "E");//}).join();
		//	fjp111.shutdown();
			}
    for(List<EdgeOperator>jge:JoinGroupsListOptional)
		if(jge.equals(cde1))
			{//ForkJoinPool fjp111= new ForkJoinPool();
				//fjp111.submit(()->{
					TreeOrder.put(cde1.get(0), "O");//}).join();
			//fjp111.shutdown();
			}
    
	for(EdgeOperator cde2:cde1)
	{if(JoinGroupsListRight.contains(cde2))
			{
//		ForkJoinPool fjp111= new ForkJoinPool();
			//fjp111.submit(()->{
				TreeOrder.put(cde2, "R");//}).join();
		//	}
		//fjp111.shutdown();
		}
			
	if(JoinGroupsListLeft.contains(cde2))
	{
		//ForkJoinPool fjp111= new ForkJoinPool();
//		try {
	//		fjp111.submit(()->{
				TreeOrder.put(cde2, "L");//}).join();
	//	} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
	//		e1.printStackTrace();
	//	} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
	//		e1.printStackTrace();
	//	}
//		fjp111.shutdown();
		}
	if(JoinGroupsListRightOptional.contains(cde2))
		{	{
	//		ForkJoinPool fjp111= new ForkJoinPool();
		//	try {
//				fjp111.submit(()->{
					TreeOrder.put(cde2, "OR");//}).join();
		//	} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
		//		e1.printStackTrace();
		//	} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
		//		e1.printStackTrace();
		//	}
//			fjp111.shutdown();
			}}
if(JoinGroupsListLeftOptional.contains(cde2))
	{
	{
	//	ForkJoinPool fjp111= new ForkJoinPool();
		//try {
			//fjp111.submit(()->{
				TreeOrder.put(cde2, "OL");//}).join();
	//	} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
	//		e1.printStackTrace();
	//	} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
	//		e1.printStackTrace();
	//	}
//		fjp111.shutdown();
		}
	}
	}
	}

LinkedListMultimap<EdgeOperator, String> TreeOrderTemp =  LinkedListMultimap.create();
LinkedListMultimap<EdgeOperator, String> TreeOrderOptTemp =  LinkedListMultimap.create();
for(EdgeOperator obt1:operators_BushyTreeOrder)
{	
	for(Entry<EdgeOperator, String> to:TreeOrder.entries())
		if(to.getKey().toString().equals(obt1.toString()) &&!TreeOrderTemp.toString().contains(obt1.toString()))
			TreeOrderTemp.put(to.getKey(),to.getValue());
	}

TreeOrder.clear();
TreeOrder.putAll(TreeOrderTemp);

for(EdgeOperator obt1:operators_BushyTreeOrder)
{	
	for(Entry<EdgeOperator, String> to:TreeOrderOpt.entries())
		if(to.getKey().toString().equals(obt1.toString()) &&!TreeOrderOptTemp.toString().contains(obt1.toString()))
			TreeOrderOptTemp.put(to.getKey(),to.getValue());
	}

TreeOrderOpt.clear();
TreeOrderOpt.putAll(TreeOrderOptTemp);

TreeOrder.putAll(TreeOrderOpt);
for(Entry<EdgeOperator, String> to:TreeOrder.entries())
	System.out.println("This is the required order:"+to);


//new	StatementGroupOptimizer2(null).meetOther(query);
//System.out.println("This is the new group list of StartBindingSetBJ:"+StartBindingSetBJ);
		
		//System.out.println("This is the new group list of StartBindingSet:"+StartBindingSet);

		// }

//		while(operators_independent!=null)
//			ExecuteJoinsDep(opt,operators);
//		ExecuteJoinsDep(opt1,operators_independent);

		
		//BushyTreeSize = operators_BushyTree.size();
		ExecuteJoins(opt1,operators_BushyTree,TreeOrder);

		
		
		}

	@Override
	protected Binding moveToNextBinding() {
		// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
		// execBGP2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: ");
		////logger.info("These is the list of operators in ExecuteJoins44444");

		return (Binding)results.next();
	}
	
	@Override
	protected void closeIterator() {
		// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
		// execBGP2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: ");
//		//logger.info("These is the list of operators in ExecuteJoins12312321");

		g.getEdges().clear();
		results = null;
	}
	public void ExecuteJoins(Optimiser opt,List<EdgeOperator> operators,LinkedListMultimap<EdgeOperator, String>  TreeOrder) {
			
		//	while (operators != null) {
					
			////logger.info("Tis is the latest EdgeOperator List:"+prepareBushyTree(operators));	
		//	Iterator<EdgeOperator> x = operators.iterator();
			LinkedHashSet<EdgeOperator>  opr = new LinkedHashSet<>();
			LinkedHashSet<EdgeOperator>  oprOpt = new LinkedHashSet<>();
			LinkedHashSet<EdgeOperator>  oprOptLeft = new LinkedHashSet<>();
			LinkedHashSet<EdgeOperator>  oprOptRight = new LinkedHashSet<>();
			LinkedHashSet<EdgeOperator>  oprRight = new LinkedHashSet<>();
			LinkedHashSet<EdgeOperator>  oprLeft = new LinkedHashSet<>();
			
			//Because now a Literal/URI is placed at beginning of list if its cost is less than 500 
			//for hashJoin perform
			for(List<EdgeOperator> e:JoinGroupsListExclusive)
			{		
				Map<EdgeOperator,Integer> t = new HashMap<>();
				t.put(findFirstNonLiteral(e), 1);
			exchangeOfElements.put(e.get(0),t);
		//	TreeType.put(e.get(0), 1);
			finalResult.put(findFirstNonLiteral(e),null);
			orderingElements.put(findFirstNonLiteral(e), e);	
			}
			
			for(EdgeOperator e:JoinGroupsListRight)
			{		
				//TreeType.put(e, 0);
				Map<EdgeOperator,Integer> t = new HashMap<>();
				t.put(e, 0);
		
				exchangeOfElements.put(e,t);
				finalResultRight.put(e,null);
		//	orderingElements.put(e, e);	
			}
			
			for(EdgeOperator e:JoinGroupsListLeft)
			{		
				Map<EdgeOperator,Integer> t = new HashMap<>();
				t.put(e, 0);
				exchangeOfElements.put(e,t);
				finalResultLeft.put(e,null);
		//	orderingElements.put(e, e);	
			}
			System.out.println("This is here now:");

			if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
			for(List<EdgeOperator> e:JoinGroupsListOptional)
			{	
				Map<EdgeOperator,Integer> t = new HashMap<>();
				t.put(findFirstNonLiteral(e), 1);
				exchangeOfElements.put(e.get(0),t);
				finalResultOptional.put(findFirstNonLiteral(e),null);
				//System.out.println("this is JoinGroupsListOptional:"+e);	
				
				//System.out.println("this is JoinGroupsListOptional111:"+finalResultOptional);
				
			}
			
			if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
				for(EdgeOperator e:JoinGroupsListLeftOptional)
				{		
					Map<EdgeOperator,Integer> t = new HashMap<>();
					t.put(e, 0);
					exchangeOfElements.put(e,t);
					finalResultLeftOptional.put(e,null);
				}
			
			
			if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
				for(EdgeOperator e:JoinGroupsListRightOptional)
				{		
					Map<EdgeOperator,Integer> t = new HashMap<>();
					t.put(e, 0);
					exchangeOfElements.put(e,t);
					finalResultRightOptional.put(e,null);
				}
	
			System.out.println("This is here now111:");
			
			for(List<EdgeOperator> e:JoinGroupsListExclusive)
			    {		
							opr.add(e.get(0));
				}
			if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
				
			for(List<EdgeOperator> e:JoinGroupsListOptional)
			{		
					oprOpt.add(e.get(0));
							
			}
			for(EdgeOperator e:JoinGroupsListRight)
			{		
					oprRight.add(e);
			}
			
			for(EdgeOperator e:JoinGroupsListLeft)
			{		
					oprLeft.add(e);				
			}
			if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
				
			for(EdgeOperator e:JoinGroupsListRightOptional)
			{		
							oprOptRight.add(e);
			}
			if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
				
			for(EdgeOperator e:JoinGroupsListLeftOptional)
			{		
							oprOptLeft.add(e);				
		    }
			System.out.println("This is here now333333:");

		//	es = Executors.newCachedThreadPool();//newCachedThreadPool(Runtime.getRuntime().availableProcessors());
			
			//opr.add(findFirstNonLiteral(e));
//synchronized(finalResult) {		
	
//			//logger.info("11111111111111111111This this this this this this this this this this:"+LocalTime.now());

		//	//logger.info("000000000000000000000000This this this this this this this this this this:"+LocalTime.now());
		//	  CompletableFuture<String> cfa = CompletableFuture.supplyAsync(() -> );
		//	    CompletableFuture<String> cfb = CompletableFuture.supplyAsync(() -> );
		//	    CompletableFuture<String> cfc = CompletableFuture.supplyAsync(() ->);
			
			//    ExecutorService executor = Executors.newWorkStealingPool();

//			    Set<Callable<String>> callables = new HashSet<Callable<String>>();
System.out.println("This is current problem:"+JoinGroupsListExclusive);
int n=0;



			//	ForkJoinPool fjp = new ForkJoinPool();
					//fjp.submit(()->{				
				LinkedHashMap<EdgeOperator, List<Binding>>  Final	=	new LinkedHashMap<>() ;
				
				LinkedHashMap<EdgeOperator, List<Binding>>  Temp	=	new LinkedHashMap<>() ;
				LinkedHashMap<EdgeOperator, List<Binding>>  TempBind	=	new LinkedHashMap<>() ;
				LinkedHashMap<EdgeOperator, List<Binding>>  TempHash	=	new LinkedHashMap<>() ;
				Temp.putAll(finalResult);
		Temp.putAll(finalResultLeft);
		Temp.putAll(finalResultRight);
	
		for(EdgeOperator bo:operators_BushyTreeOrder)
			for(Entry<EdgeOperator,List<Binding>> t:Temp.entrySet()) {
		//		System.out.println("This is processing:"+t.getKey()+"--"+bo);
				if(bo.toString().equals(t.getKey().toString()) && t.getKey().toString().contains("Hash"))
					TempHash.put(t.getKey(),t.getValue());
			}
		for(EdgeOperator bo:operators_BushyTreeOrder)
			for(Entry<EdgeOperator,List<Binding>> t:Temp.entrySet())
				if(bo.toString().equals(t.getKey().toString()) && t.getKey().toString().contains("Bind"))
					TempBind.put(t.getKey(),t.getValue());
		

		
	
	
	//	for(EdgeOperator bo:operators_BushyTreeOrder)
	//		System.out.println("This is ordered:"+bo);

	//	for(Entry<EdgeOperator, List<Binding>> bo:TempHash.entrySet())
	//		System.out.println("This is Hash:"+bo);

	//	for(Entry<EdgeOperator, List<Binding>> bo:TempBind.entrySet())
	//		System.out.println("This is Bind:"+bo);

		PartitionedExecutions(TempHash);
		
		PartitionedExecutions(TempBind);
		
		//}

				//    }).join();
				
			//	fjp.shutdownNow();


		
		


	finalResult.putAll(finalResultRight);

	////logger.info("aaaaaaaaaa5555:"+UnProcessedSets.parallelStream().limit(1).collect(Collectors.toSet()));

//	for(Entry<EdgeOperator, Set<Binding>> frl:finalResultLeft.entrySet())
	//	if(frl.getValue()!=null)
	//	//logger.info("This is finalResultExclusive Left:"+frl.getValue().size()+"--"+frl.getValue().parallelStream().limit(1).collect(Collectors.toSet()));
	finalResult.putAll(finalResultLeft);
	
	////logger.info("This is the final final final final result:"+finalResult);
	/*for(Map.Entry<EdgeOperator,Set<Binding>> a:finalResult.entrySet())
		for(Map.Entry<EdgeOperator,Set<Binding>> a1:finalResult.entrySet())
			if(a.getKey().getEdge().getTriple().getObject().equals(a1.getKey().getEdge().getTriple().getSubject()))
				{finalResultFinal.put(a1.getKey(),a1.getValue());
				finalResultFinal.put(a.getKey(),a.getValue());
	
				}*/
//	//logger.info("aaaaaaaaaa4444:"+UnProcessedSets.parallelStream().limit(1).collect(Collectors.toSet()));

	//	for(Map.Entry<EdgeOperator,Set<Binding>> a:finalResultFinal.entrySet())
//	for(Map.Entry<EdgeOperator,Set<Binding>> a1:finalResult.entrySet())
//		if(!a.getKey().equals(a1.getKey()))
//			finalResultFinal.put(a1.getKey(), a1.getValue());
	//			//logger.info("Tis is the final finalResult:"+a.getKey());
		

//	for(Entry<EdgeOperator, Set<Binding>> frl:finalResult.entrySet())
//		//logger.info("This is finalResultExclusive Final:"+frl.getKey());
////logger.info("888888888888888This this this this this this this this this this:"+LocalTime.now());
	Set<Binding> r1 = new HashSet<>();
	for(List<Binding>   b:HashJoin.JoinedTriples.values())
	r1.addAll(b); 
	
	//}
	
	//finalResult(finalResult);
//	for(BindingSet rr:r1)
//	//logger.info("77777777777777777This this this this this this this this this this:"+rr);

	//for(Entry<EdgeOperator, Set<Binding>> r:finalResult.entrySet())
	//	//logger.info("This is now the final result:"+r.getValue().size());
/*
		//logger.info("This is now the final result:"+r1.size());

		if(ParaEng.Union.contains("UNION")==true ) {
	if(finalResultRightOptional!=null ||finalResultRightOptional.size()>0)		
			finalResultOptional.putAll(finalResultRightOptional);
	if(finalResultLeftOptional!=null ||finalResultLeftOptional.size()>0)		
	finalResultOptional.putAll(finalResultLeftOptional);
	
			//for(Map.Entry<EdgeOperator,Set<Binding>> a:finalResultOptional.entrySet())
			//	for(Map.Entry<EdgeOperator,Set<Binding>> a1:finalResultOptional.entrySet())
			///		if(a.getKey().getEdge().getTriple().getObject().equals(a1.getKey().getEdge().getTriple().getSubject()))
			//			{finalResultOptionalFinal.put(a1.getKey(),a1.getValue());
			//			finalResultOptionalFinal.put(a.getKey(),a.getValue());
		
		//				}
		//	for(Map.Entry<EdgeOperator,Set<Binding>> a:finalResultOptionalFinal.entrySet())
		//	for(Map.Entry<EdgeOperator,Set<Binding>> a1:finalResultOptional.entrySet())
		//		if(!a.getKey().equals(a1.getKey()))
		//			finalResultOptionalFinal.put(a1.getKey(), a1.getValue());
			
			Set<Binding> r2 = finalResult(finalResultOptional);
		//logger.info("This is now the final result:"+r2.size()+"--"+r1.size());
		r3.addAll(r1);
		r3.addAll(r2) ;
		//logger.info("This is now the final result12123:"+r2.size()+"--"+r1.size());
			
		}
		if(ParaEng.Optional.contains("OPTIONAL")==true) {
			finalResultOptional.putAll(finalResultRightOptional);
			finalResultOptional.putAll(finalResultLeftOptional);
			
			
				
	System.out.println("This this this this this this this this this:"+r1.size());
//System.out.println("222222222This this this this this this this this this:"+);
Set<Binding> iuo ;
Iterator<Set<Binding>> aa = finalResultOptional.values().iterator();
 aa = finalResultOptional.values().iterator();

 while(aa.hasNext()) {
Set<Binding> aaaa = aa.next();
//iuo=QueryUtil.join(aaaa,r1);
System.out.println("89898989898989This is the finalResultOptional:"+ aaaa.size()+"--"+r1.size());
//r1.removeAll(iuo);
System.out.println("89898989898989This is the finalResultOptional:"+ aaaa.size()+"--"+r1.size());

//if(iuo.size()>0) {
	//System.out.println("89898989898989This is the finalResultOptional:"+ iuo.size());

//	r3.addAll(iuo);
			//		r3.addAll(r1);
			//		if(aa.next().size()>0)
			//		r3.addAll(aa.next());
					if(r3.size()==0)
					   r3.addAll(r1);
					System.out.println("This is the finalResultOptional:"+r3.size()+"--"+r3.parallelStream().limit(1).collect(Collectors.toSet()));
								
					break;
				}
			}
//			for(BindingSet rr:r3)
////logger.info("This is now the final result rrr:"+rr);
//			r3.addAll(r1);r3.addAll(r2) ;
			//}
		

		
		if((ParaEng.Optional.contains("OPTIONAL")==true || ParaEng.Union.contains("UNION")==true)) {
			results = r3.iterator();
			finalResultSize =r3.size();

			System.out.println("This is the result of first Joining:"+r3.size()+"--"+r3.parallelStream().limit(1).collect(Collectors.toSet())+"--"+LocalTime.now());
			
			return;
			
			}
		
		
	//	if(!(ParaEng.Optional.contains("OPTIONAL")==true || ParaEng.Union.contains("UNION")==true)) {
		// r3=r1;
		//}
//}
*/		//logger.info("This is the result of first Joining11:"+r3.size()+"--"+LocalTime.now());
		r3 =r1;
		results = r3.iterator();
		System.out.println("This is finalization here--------------------------------------------------------------------------------------------------------------------");
		System.out.println("This is final result:"+"--"+LocalTime.now()+"--"+r3.size()+"--"+r3.parallelStream().limit(1).collect(Collectors.toSet()));
	//	System.exit(0);
		return;
		}	

//	}

	
	


	public void ExecuteJoinsDep(Optimiser opt,List<EdgeOperator> operators) {
	
		while (operators != null) {
			es = Executors.newWorkStealingPool(1);
			for (EdgeOperator eo : operators) {
				// consume the input BindingSets
				//logger.info("This is here in ExecuteJoinDep counting number of HashJoin:"+eo);
		
				Stream.of(eo.setInput(input)).sequential().forEachOrdered(
						e1->{		try {
							es.submit(eo);
							Thread.sleep(1000);
						} catch (RejectedExecutionException | InterruptedException e) {
							//if (Config.debug)
							//	//logger.info("Query execution is terminated.");
						}
						}
				);

		//logger.info("This is sequence of edgeOperators in actual"+eo.toString());
				// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
				// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());
				input = null;
				// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
				// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
				// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());

			}
			// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in BGPEval After
			// es.submit!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+getCurrentExeService());

			es.shutdownNow();
			while (!es.isTerminated()) {
				try {
					es.awaitTermination(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					//logger.info("Query execution interrupted.");
				} catch (IllegalMonitorStateException e) {
					//logger.info("IllegalMonitorStateException");
				}
			}

			// if(Config.debug)
			// //logger.info("********************");
			//logger.info(
			//		"***********************************************************************************************************************************************************************************************************");

			/*
			 * RelativeError.addEstimatedCardBGP(RelativeError.est_resultSize);
			 * RelativeError.print(); RelativeError.add_plan_joincards();
			 * RelativeError.est_resultSize.clear(); RelativeError.real_resultSize.clear();
			 */
			//logger.info("Now it is here1");
			for (EdgeOperator eo : operators) {
				if (eo.isFinished()) {
//					if (Config.debug) {
						//logger.info("Query is finished by " + eo.getEdge());
	//				}
				//	//logger.info("These are the values of result in BGPEval:"+ eo.getEdge());
					results = new Vector<Binding>().iterator();
			//		//logger.info("This is in BGPEval results:" + results);
					return;
				}
				//logger.info("Now it is here2:"+results);

			}
			
			operators = opt.nextStage();
		}
		i++;

		// find out vertices with BindingSets that have not been used (joined)

	//	Set<Binding> BindingSets = null;
		for (Vertex v : opt.getRemainVertices()) {
			if (Config.debug) {
				//logger.info("Remaining vertex: " + v);
			}
		//	//logger.info("This is in BGPEval results:" + BindingSets + "--" + v.getBindingSets());
	//		ForkJoinPool fjp = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
//		fjp.submit(()->Stream.of(BindingSets = QueryUtil.join(BindingSets, v.getBindingSets())).parallel().forEachOrdered(
		//		e->results = bindinfgs.iterator()))	;
	//	try {
	//		Thread.sleep(1000);
			//logger.info("THis is currently the entering part B6");
			//Stream.of(BindingSets = QueryUtil.join(BindingSets, v.getBindingSets())).parallel().forEachOrdered(e->{
			//	//logger.info("THis is after exiting QueryUtil B6"); 
			//results = BindingSets.iterator();
		//	});
	/*		Iterator<Binding> x = v.getBindingSets().iterator();
			Set<Binding> set = setFromIterator(x);
			//Set<Binding> foo =null;
			Iterable<List<Binding>> set_partition = Iterables.partition(set, 10);
			for(int i=0; i<v.getBindingSets().size();i++) {
		//	for (i=0;i<set.toArray().length/2;i++)
		int k = 0;
			for(List<Binding> sp:set_partition)	{
			Set<Binding> foo =	sp.stream().collect(Collectors.toSet()); //new HashSet<Binding>(set_partition);
			BindingSets = QueryUtil.join(BindingSets, foo);
			//logger.info("THis is currently the entering part B6 segment1:"+BindingSets);
			k++;
		}
	*///		k=0;
			
		//	}
		//	for(int i=v.getBindingSets().size()/2; i<v.getBindingSets().size();i++)	{	
			Set<Binding> results1 = new HashSet<Binding>(BindingSets);
		//	Vertex start1 = new Vertex();
		//	start1=start;

			//BindingSets = QueryUtil.join(BindingSets, v.getBindingSets());
			//logger.info("THis is currently the entering part B6 segment2");
			//logger.info("THis is currently the entering part B6 segment1"+BindingSets.size()+"--"+v.getBindingSets().size());

		}	
					//logger.info("THis is after exiting QueryUtil B6"); 
			results = BindingSets.iterator();
		
		
			//logger.info("THis is after BindingSets iterator QueryUtil B6");
			//logger.info("This is in BGPEval results1:" + BindingSets.size());
		}
//		//logger.info("This is the new value in BGPEval:" + BindingSets.size()+"--"+Iterators.size(results));

	//	ForkJoinPool fjp =new ForkJoinPool(Runtime.getRuntime().availableProcessors());

//fjp.submit(()->
////logger.info("This is the new value in BGPEval:" + BindingSets.size()+"--"+Iterators.size(results));//);
//fjp.shutdownNow();
	//	//logger.info("This is the new value in BGPEval:" + BindingSets + "--" + results);


	/*	public void ExecuteJoins(Optimiser opt,List<EdgeOperator> operators) {
		while (operators != null) {
			es = Executors.newCachedThreadPool();
			for (EdgeOperator eo : operators) {
				// consume the input BindingSets
				eo.setInput(input);

				// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
				// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
				// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());
				input = null;
				// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
				// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
				// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());

				try {
					es.submit(eo);
				} catch (RejectedExecutionException e) {
					if (Config.debug)
						//logger.info("Query execution is terminated.");
				}
			}
			// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in BGPEval After
			// es.submit!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+getCurrentExeService());

			es.shutdown();
			while (!es.isTerminated()) {
				try {
					es.awaitTermination(3600, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					//logger.info("Query execution interrupted.");
				} catch (IllegalMonitorStateException e) {
					//logger.info("IllegalMonitorStateException");
				}
			}

			// if(Config.debug)
			// //logger.info("********************");
			//logger.info(
					"***********************************************************************************************************************************************************************************************************");

			/*
			 * RelativeError.addEstimatedCardBGP(RelativeError.est_resultSize);
			 * RelativeError.print(); RelativeError.add_plan_joincards();
			 * RelativeError.est_resultSize.clear(); RelativeError.real_resultSize.clear();
			 
			//logger.info("Now it is here1");
			for (EdgeOperator eo : operators) {
				if (eo.isFinished()) {
					if (Config.debug) {
						//logger.info("Query is finished by " + eo.getEdge());
					}
					results = new Vector<Binding>().iterator();
					//logger.info("This is in BGPEval results:" + results);
					return;
				}
			}
			//logger.info("Now it is here2");

			operators = opt.nextStage();
		}
		i++;

		// find out vertices with BindingSets that have not been used (joined)

		Set<Binding> BindingSets = null;
		for (Vertex v : opt.getRemainVertices()) {
			if (Config.debug) {
				//logger.info("Remaining vertex: " + v);
			}
			//logger.info("This is in BGPEval results:" + BindingSets + "--" + v.getBindingSets());
			BindingSets = QueryUtil.join(BindingSets, v.getBindingSets());
			//logger.info("This is in BGPEval results1:" + BindingSets + "--" + v.getBindingSets());
		}
		//logger.info("This is the new value in BGPEval:" + BindingSets);

		results = BindingSets.iterator();
		//logger.info("This is the new value in BGPEval:" + BindingSets + "--" + results);

	}
*/
/*	public static int Transform(Edge edge) {
		HashJoin hj ;//= new HashJoin(edge);
//		hj.exec();
		Stream.of(  hj = new HashJoin(edge)).parallel().forEachOrdered(e->hj.exec());  
		hj.exec();
		//logger.info("This is every thread related to:"+edge+"--"+Thread.currentThread());
	//	//logger.info("Maximum Threads:"+ForkJoinPool.commonPool()+"--"+Runtime.getRuntime().availableProcessors());
	return 0;		
	}*/



	public static Set<EdgeOperator> setFromIterator(Iterator<EdgeOperator> it) {
	  final Set<EdgeOperator> s = new LinkedHashSet<EdgeOperator>();
	  while (it.hasNext()) 
		s.add(it.next()) ;
	  return s;
	}
public static ArrayList<Binding> setFromIteratorB(Iterator<Binding> it) {
	  final ArrayList<Binding> s = new ArrayList<Binding>();
	  while (it.hasNext()) 
		s.add(it.next()) ;
	  return s;
	}
public static LinkedHashSet<EdgeOperator> prepareBushyTree(Set<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator> set){
LinkedHashSet<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator> FinalOrdering = new LinkedHashSet<>();

for(EdgeOperator s:set)
	FinalOrdering.add(s);
/*int IsDr =0;
String	anm3 ;
String	bnm3[];
String	bnm4[];
String	anm ;
String	bnm[];
String	bnm2[];

int iteration=0;
int l=0;
int m=0;
EdgeOperator finalV = null;
for(EdgeOperator o:set) {
	FinalOrdering.add(o);

	
	if(iteration>0) {
	for(EdgeOperator fo:FinalOrdering)
	if(fo.toString().equals(o.toString()))
	{
		l=1;
		break;
	}
	if(l==1)
	{
		l=0;
		continue;
	}
}
FinalOrdering.add(o);

		for(EdgeOperator o2:set)
			{
			FinalOrderingTemp.add(o2);
			
			if(GetVertexName(o.getEdge().getV1()).toString().equals(GetVertexName(o2.getEdge().getV1()).toString()))			
			{
				//		//logger.info();
				FinalOrderingTemp.add(o2);
				continue;
			}	
			if(GetVertexName(o.getEdge().getV1()).toString().equals(GetVertexName(o2.getEdge().getV2()).toString()))			
			{	
		//		//logger.info();
				FinalOrderingTemp.add(o2);
				continue;
			}	
			if(GetVertexName(o.getEdge().getV2()).toString().equals(GetVertexName(o2.getEdge().getV1()).toString()))			
			{//	//logger.info();
				FinalOrderingTemp.add(o2);
				continue;
			}	
			if(GetVertexName(o.getEdge().getV2()).toString().equals(GetVertexName(o2.getEdge().getV2()).toString()))				
			{//	//logger.info();
				FinalOrderingTemp.add(o2);
				continue;
			}	
		if(GetVertexName(o.getEdge().getV1()).toString().length()>2 &&GetVertexName(o2.getEdge().getV1()).toString().length()>2) 
			for(int i=3;i>=2;i--)
			if(GetVertexName(o.getEdge().getV1()).toString().substring(0,i).equals(GetVertexName(o2.getEdge().getV1()).toString().substring(0,i)))	
					{	
			//	//logger.info();
anm3=			o.getEdge().getTriple().getPredicate().toString().substring(o.getEdge().getTriple().getPredicate().toString().indexOf(":")) ;
bnm3=			o2.getEdge().getTriple().getPredicate().toString().split("[/]");
bnm4=			bnm3[bnm3.length-1].split("_");
	for(String bnm10:bnm4) {
			if(anm3.contains(bnm10))
{//logger.info("1This is now the problem to be solved here1232131231"+anm3+"--"+bnm10);
IsDr++;
}
	}
		
				FinalOrderingTemp.add(o2);
								continue;
					}
					
			if(GetVertexName(o.getEdge().getV1()).toString().length()>2 &&GetVertexName(o2.getEdge().getV2()).toString().length()>2) 
				for(int i=3;i>=2;i--)
				if(GetVertexName(o.getEdge().getV1()).toString().substring(0,i).equals(GetVertexName(o2.getEdge().getV2()).toString().substring(0,i)))	
						{	
				//	//logger.info(); 

					
					anm=			o.getEdge().getTriple().getPredicate().toString() ;
					bnm=			o2.getEdge().getTriple().getPredicate().toString().split("[/]");
					bnm2=			bnm[bnm.length-1].split("_");
					
						for(String bnm9:bnm2)
						{
							String xkl = bnm9;
					if(xkl.toString().contains(anm.toString()))
					{//logger.info("2This is now the problem to be solved here1232131231"+anm+"--"+bnm9);
					IsDr++;
						}
					}
					FinalOrderingTemp.add(o2);
									continue;
						}
				if(GetVertexName(o.getEdge().getV2()).toString().length()>2 &&GetVertexName(o2.getEdge().getV1()).toString().length()>2) 
					for(int i=3;i>=2;i--)
					if(GetVertexName(o.getEdge().getV2()).toString().substring(0,i).equals(GetVertexName(o2.getEdge().getV1()).toString().substring(0,i)))	
						{	
					////logger.info();
					FinalOrderingTemp.add(o2);
									continue;
						}
					if(GetVertexName(o.getEdge().getV2()).toString().length()>2 &&GetVertexName(o2.getEdge().getV2()).toString().length()>2) 
						for(int i=3;i>=2;i--)
						if(GetVertexName(o.getEdge().getV2()).toString().substring(0,i).equals(GetVertexName(o2.getEdge().getV2()).toString().substring(0,i)))	
						{	
					////logger.info();
					FinalOrderingTemp.add(o2);
									continue;
						}
				
			}
}
/*		if(IsDr>0)
		{
			FinalOrdering.clear();
			for(EdgeOperator o:FinalOrderingTemp)
			for(Entry<EdgeOperator, List<EdgeOperator>> ord:orderingElements.entrySet())
			{
				{
					
					if(o.toString().equals(ord.getKey().toString()))
						orderingElementsTemp.put(ord.getKey(), ord.getValue());
			}
			}

			FinalOrdering=	ProcessOrdering(FinalOrderingTemp,orderingElementsTemp);
		}else 
			for(EdgeOperator fok:FinalOrderingTemp)
				FinalOrdering.add(fok);
		//	FinalOrdering=	ProcessOrdering(FinalOrdering,orderingElements);
		
//logger.info("This is the latest latest latest problem:"+IsDr);		
*/
	
return FinalOrdering;
}

/*
public static LinkedHashSet<EdgeOperator> orderBushyTree(LinkedHashSet<EdgeOperator> linkedHashSet){
Vector<String> order = new Vector<>();
order.add("Hash");
order.add("Bind");
LinkedHashSet<EdgeOperator> operatorNew = new LinkedHashSet<>();
for(String o:order)
for(EdgeOperator e:linkedHashSet)
		if(e.toString().contains(o)) {
			//	for(EdgeOperator e2:linkedHashSet) {
			//		if(e2.toString().equals(e.toString()))
			//logger.info("This is the new group of queriestwew:"+e);
			operatorNew.add(e);
		//break;	
				}
		
	
return operatorNew;
}
*/


public  static void PartitionedExecutions(LinkedHashMap<EdgeOperator, List<Binding>> finalResult2) {
	
//finalResult;
for (Entry<EdgeOperator, List<Binding>> eo : finalResult2.entrySet()) {

System.out.println("This is finalization:"+eo);
	//fjp_bind.submit(()->{
		Stream.of(eo.getKey().setInput(null)).parallel().forEach(
		e1->{		
			ForkJoinPool fjp_bind7 = new ForkJoinPool();

			try {
				fjp_bind7.submit(eo.getKey()).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//	Thread.sleep(300000);
	//		Thread.sleep(2000);
			
			fjp_bind7.shutdown();
} 
		
);
			//}).invoke();


}
}

/*
//logger.info("This is out of the looop:");
int aaa=0;
	for(Entry<Triple, Integer> tc:TripleExecution.TrackCompletion.entrySet())
		if(tc.getKey() == eo.getEdge().getTriple())
	//	{	while(tc.getValue()==0)
				{System.out.print("");
if(tc.getValue()==1) {
				aaa=1;
				break;
		//		}
				}	
	if(aaa==1) {
		aaa=0;
		break;
	}
		}
*/	

////logger.info("This is sequence of edgeOperators in actual"+eo.toString());
		// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
		// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());
	//	input = null;
		// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
		// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
		// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());

	



/*public  static void PartitionedExecutionsSingle(LinkedHashMap<EdgeOperator, Set<Binding>> finalResult2, ConcurrentHashMap<EdgeOperator,EdgeOperator>  exchangeOfElements2,List<EdgeOperator> operators) {
	
	LinkedList<EdgeOperator> newOperators = new LinkedList<>();
	LinkedList<EdgeOperator> newOperatorsSorted = new LinkedList<>();

	for(Entry<EdgeOperator, Map<EdgeOperator, Integer>> e2:BGPEval.exchangeOfElements.entrySet()) {
		for(Entry<EdgeOperator, Integer> e1:e2.getValue().entrySet())
		for(EdgeOperator o:operators) {
			if(e1.getValue()!=null)
		if(e1.getKey().getEdge().toString().equals(o.getEdge().toString())
		&& !(e1.getKey().getEdge().toString().equals(e1.getKey().getEdge().toString())))
			newOperators.add(new HashJoin(e1.getKey().getEdge()));
			if(e1.getKey().getEdge().toString().equals(o.getEdge().toString())
					&& (e1.getKey().getEdge().toString().equals(e1.getKey().getEdge().toString())))
						newOperators.add(e1.getKey());

			//	else
		//if(e1.getKey().getEdge().toString().equals(e1.getValue().getEdge().toString()))
	//newOperators.add(o);
		}
	}
	


//for(HashSet<List<EdgeOperator>> er:execOrder.keySet())
//	for(List<EdgeOperator> er1:er)
//		for(EdgeOperator er2:er1)
//	for(EdgeOperator no:newOperators)
//		if(er2.getEdge().toString().equals(no.getEdge().toString()))
//		newOperatorsSorted.add(no);

System.out.println("These are the replaced first non literal/URI characters"+newOperatorsSorted);


if(newOperatorsSorted.size()==1) {
	fjp_bind= new ForkJoinPool(6) ;
	
}
if(newOperatorsSorted.size()==2) {
 fjp_bind = new ForkJoinPool(3) ;
 fjp_bind1 = new ForkJoinPool(3) ;
}
if(newOperatorsSorted.size()==3) {
	 fjp_bind = new ForkJoinPool(2) ;

 fjp_bind1 = new ForkJoinPool(2) ;

 fjp_bind2 = new ForkJoinPool(2) ;
}
if(newOperatorsSorted.size()==4) {
	 fjp_bind = new ForkJoinPool(2) ;

 fjp_bind1 = new ForkJoinPool(2) ;

 fjp_bind2 = new ForkJoinPool(1) ;

 fjp_bind3 = new ForkJoinPool(1) ;
}
if(newOperatorsSorted.size()==5) {
	 fjp_bind = new ForkJoinPool(2) ;

	 fjp_bind1 = new ForkJoinPool(1) ;

	 fjp_bind2 = new ForkJoinPool(1) ;

	 fjp_bind3 = new ForkJoinPool(1) ;

 fjp_bind4 = new ForkJoinPool(1) ;
}
if(newOperatorsSorted.size()==6) {
fjp_bind = new ForkJoinPool(1) ;
fjp_bind1 = new ForkJoinPool(1) ;
fjp_bind2 = new ForkJoinPool(1) ;
fjp_bind3 = new ForkJoinPool(1) ;
fjp_bind4 = new ForkJoinPool(1) ;
fjp_bind5 = new ForkJoinPool(1) ;
}
for (EdgeOperator eo : (newOperatorsSorted.isEmpty())?operators:newOperatorsSorted) {
	/*	if(eo.toString().contains("Bind"))
	{

		//fjp_bind.submit(()->{
			Stream.of(eo.setInput(null)).parallel().forEach(
			e1->{		
				ForkJoinPool fjp_bind7 = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

				fjp_bind7.submit(eo).invoke();
			//	Thread.sleep(300000);
		//		Thread.sleep(2000);
				
				fjp_bind7.shutdown();

			} 
			
	);
				//}).invoke();


	}
	else
	{
	
	if(i000==0)
	Stream.of(eo.setInput(null)).parallel().forEach(
		e1->{	
	
			try {
				fjp_bind.submit(eo).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//	if(i000%2==1)
			
			
		}		
);
	

if(i000==1)
	Stream.of(eo.setInput(null)).parallel().forEach(
			e1->{	
		
			//	if(i000%2==0)
				try {
					fjp_bind1.submit(eo).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//	if(i000%2==1)
				
				
			}		
	);


if(i000==2)
	Stream.of(eo.setInput(null)).parallel().forEach(
			e1->{	
		
			//	if(i000%2==0)
				try {
					fjp_bind2.submit(eo).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//	if(i000%2==1)
				
				
			}		
	);


if(i000==3)
	Stream.of(eo.setInput(null)).parallel().forEach(
		e1->{	
	
		//	if(i000%2==0)
			try {
				fjp_bind3.submit(eo).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//	if(i000%2==1)
			
			
		}		
);
	

if(i000==4)
	Stream.of(eo.setInput(null)).parallel().forEach(
			e1->{	
		
			//	if(i000%2==0)
				try {
					fjp_bind4.submit(eo).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//	if(i000%2==1)	
					
			}		
	);


if(i000==5)
	Stream.of(eo.setInput(null)).parallel().forEach(
			e1->{	
		
			//	if(i000%2==0)
				try {
					fjp_bind5.submit(eo).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//	if(i000%2==1)
				
				
		}		
	);
i000++;
//}
	
}	

if(newOperatorsSorted.size()==1) {
try {
	fjp_bind.awaitTermination(270, TimeUnit.SECONDS);
	fjp_bind.shutdown();
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}			

	fjp_bind.shutdown();

}

if(newOperatorsSorted.size()==2) {

	try {
		fjp_bind.awaitTermination(180, TimeUnit.SECONDS);

	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}			

	
	try {
	fjp_bind1.awaitTermination(180, TimeUnit.SECONDS);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}



fjp_bind.shutdown();
fjp_bind1.shutdown();

}
if(newOperatorsSorted.size()==3) {
	try {
		fjp_bind.awaitTermination(270, TimeUnit.SECONDS);

	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}			

	
	try {
		fjp_bind1.awaitTermination(270, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

try {
	fjp_bind2.awaitTermination(270, TimeUnit.SECONDS);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
fjp_bind.shutdown();
fjp_bind1.shutdown();
fjp_bind2.shutdown();

}
if(newOperatorsSorted.size()==4) {
	try {
		fjp_bind.awaitTermination(90, TimeUnit.SECONDS);

	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}			


	try {
		fjp_bind1.awaitTermination(90, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}



try {
	fjp_bind2.awaitTermination(90, TimeUnit.SECONDS);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}


try {
	fjp_bind3.awaitTermination(90, TimeUnit.SECONDS);

} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}			
fjp_bind.shutdown();
fjp_bind1.shutdown();
fjp_bind2.shutdown();
fjp_bind3.shutdown();

}

if(newOperatorsSorted.size()==5) {
	try {
		fjp_bind.awaitTermination(90, TimeUnit.SECONDS);

	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}			


	try {
		fjp_bind1.awaitTermination(90, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}



try {
	fjp_bind2.awaitTermination(90, TimeUnit.SECONDS);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}


try {
	fjp_bind3.awaitTermination(90, TimeUnit.SECONDS);

} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}			

try {
	fjp_bind4.awaitTermination(90, TimeUnit.SECONDS);

} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
fjp_bind.shutdown();
fjp_bind1.shutdown();
fjp_bind2.shutdown();
fjp_bind3.shutdown();
fjp_bind4.shutdown();
}


if(newOperatorsSorted.size()==6) {

	try {
		fjp_bind.awaitTermination(270, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}			


	try {
		fjp_bind1.awaitTermination(270, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}



try {
	fjp_bind2.awaitTermination(270, TimeUnit.SECONDS);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}


try {
	fjp_bind3.awaitTermination(270, TimeUnit.SECONDS);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}			

try {
	fjp_bind4.awaitTermination(270, TimeUnit.SECONDS);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

try {
	fjp_bind5.awaitTermination(270, TimeUnit.SECONDS);

} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

fjp_bind.shutdown();
fjp_bind1.shutdown();
fjp_bind2.shutdown();
fjp_bind3.shutdown();
fjp_bind4.shutdown();
fjp_bind5.shutdown();

//}


}

i000=0;

}


*/
////logger.info("This is sequence of edgeOperators in actual"+eo.toString());
		// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
		// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());
	//	input = null;
		// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
		// BGPEval!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"+g+"--"+
		// opt.nextStage()+"--"+es+"--"+"--"+eo.getEdge()+"--"+eo.getStartVertex());

	


/*
public static int TransformDep(Vertex start, Edge edge) {
		////logger.info("This is beginning of TransformDep");
		
		//logger.info("This is every thread related to:"+start+"--"+edge+"--"+Thread.currentThread());
		BindJoin bj;//=new BindJoin(start,edge);
		HashJoin hj;
		//	//logger.info("These are the Start Dep. of Stream:"+start+"--"+edge+"--"+start.getBindingSets() );
		if(start==null)
			Stream.of(  hj = new HashJoin(edge)).parallel().forEachOrdered(e->hj.exec());  

			else
		Stream.of(  bj = new BindJoin(start,edge)).parallel().forEachOrdered(e->bj.exec());  
//Stream.of(  bj = new BindJoin(start,edge)).parallel().forEach(e->bj.exec());  
	//	//logger.info("These are the End Dep. of Stream" );
		
//		//logger.info("Maximum Threads:"+ForkJoinPool.commonPool()+"--"+Runtime.getRuntime().availableProcessors());
		return 0;	
	}*/
	public static HashMap<Integer,EdgeOperator> findFirstHash(List<EdgeOperator> EdgeO) {
		int i=0;
		HashMap<Integer,EdgeOperator>	NonLiteral = new HashMap<>();
		for(EdgeOperator e:EdgeO) {
		//	if( (e.getEdge().getTriple().getSubject().isLiteral()==false )&&(e.getEdge().getTriple().getObject().isLiteral()==false)
		//			&& (e.getEdge().getTriple().getSubject().isURI()==false )&&(e.getEdge().getTriple().getObject().isURI()==false))
		if(e.toString().contains("Hash"))
			{	NonLiteral.put(i,e);
			break;
			
			}
		i++;
		
		}
			
			return NonLiteral;
		}

	public static double CalculateTotalCost(List<EdgeOperator> EdgeO) {
		double TotalCost=0;
		Map<Double, Edge> edgesOrderTree =new TreeMap<>(StageGen.edges_order);
		for(Entry<Double, Edge> eot:edgesOrderTree.entrySet()){
			 for(EdgeOperator e:EdgeO)
			if(eot.getValue().toString().equals(e.getEdge().toString()))
				TotalCost+=eot.getKey();
		}
		
		return TotalCost;
	}
	public static String GetVertexName(Vertex input) {
		if(input.getNode().isURI())
			return input.getNode().toString();
			else if(input.getNode().isLiteral())
				return input.getNode().getLiteral().toString();
			else
				return	input.getNode().getName().toString();


	}
	public static EdgeOperator findFirstNonLiteral(List<EdgeOperator> EdgeO) {
	//	int i=0;
		 EdgeOperator	NonLiteral = null;
		for(EdgeOperator e:EdgeO) {
			if( (e.getEdge().getTriple().getSubject().isLiteral()==false )&&(e.getEdge().getTriple().getObject().isLiteral()==false)
					&& (e.getEdge().getTriple().getSubject().isURI()==false )&&(e.getEdge().getTriple().getObject().isURI()==false))
		//if(e.toString().contains("Hash"))
			{	NonLiteral=e;
		//	i++;
			break;
			}
		}
			if(NonLiteral==null)
			return	EdgeO.get(0);
			return NonLiteral;
		}
public static LinkedListMultimap<EdgeOperator, Integer> CreateBushyTreesLeft(List<EdgeOperator> operators_BushyTree, List<EdgeOperator> operators,List<EdgeOperator> operators_BushyTreeOrdered) {

	//for(Entry<Integer, List<EdgeOperator>> es:eSet.entrySet())
	//	//logger.info("This is now the new list444444:"+es);

	//logger.info("This is the source table in left :"+operators_BushyTree);

	List<EdgeOperator>	joinGroups2= new Vector<>();
	LinkedListMultimap<EdgeOperator, Integer> joinGroupsOrdered =  LinkedListMultimap.create(); 
	LinkedListMultimap<EdgeOperator, Integer> joinGroups3 =  LinkedListMultimap.create();
	int i5=0;
	for(EdgeOperator obt:operators_BushyTree) {
	for(EdgeOperator obt1:operators_BushyTree) {
		if((obt.getEdge().getV1().toString().equals(obt1.getEdge().getV1().toString())
				|| obt.getEdge().getV1().toString().equals(obt1.getEdge().getV2().toString())
				)&& !joinGroups2.contains(obt1) ) {
		
			if(i5==0)
				joinGroups2.add(obt1);
			if(i5>0)
			 if((!obt.equals(obt1)))
					joinGroups2.add(obt1);

					i5++;
		}
		
	}
	
}
		
String[] sic1;
//	Map<EdgeOperator,Integer> joinGroups3 = new ConcurrentHashMap<>();
	for(int i=0;i<Optimizer.triples.length;i++)
		if(Optimizer.triples[i][8]!=null) {
		sic1=Optimizer.triples[i][8].toString().replaceAll("[^0-9,]","").split(",");
		for(String si:sic1)	
			if(!si.equals(""))
		for(int i1=0;i1< joinGroups2.size();i1++) {
		//logger.info("This is the last resort:"+GetVertexName(joinGroups2.get(i1).getEdge().getV2())+"--"+(Optimizer.triples[i][1].toString().replaceAll("\"", "")));
			if(GetVertexName(joinGroups2.get(i1).getEdge().getV1()).equals(Optimizer.triples[i][0].toString().replaceAll("\"", "")) &&
					GetVertexName(joinGroups2.get(i1).getEdge().getV2()).equals(Optimizer.triples[i][1].toString().replaceAll("\"", "")))
					joinGroups3.put(joinGroups2.get(i1),Integer.parseInt(si));
			}
		}
for(EdgeOperator obt:operators_BushyTreeOrdered)
	for(Entry<EdgeOperator, Integer> jg2:joinGroups3.entries())
		if(obt.getEdge().toString().equals(jg2.getKey().getEdge().toString()))
			joinGroupsOrdered.put(jg2.getKey(),jg2.getValue());

//for(Entry<EdgeOperator, Integer> jg3:joinGroupsOrdered.entries())
	//logger.info("This is the created table in left :"+jg3.getValue()+"--"+jg3.getKey());


	return 	joinGroupsOrdered;
}

public static LinkedListMultimap<EdgeOperator, Integer> CreateBushyTreesRight(List<EdgeOperator> operators_BushyTree, List<EdgeOperator> operators,List<EdgeOperator> operators_BushyTreeOrdered) {
if(operators_BushyTree==null)
	return null;
//for(Entry<Integer, List<EdgeOperator>> es:eSet.entrySet())
//	//logger.info("This is now the new list444444:"+es);

//logger.info("This is the source table in left :"+operators_BushyTree);

List<EdgeOperator>	joinGroups2= new Vector<>();

	for(EdgeOperator obt:operators_BushyTree) {
for(EdgeOperator obt1:operators_BushyTree) {
	if((obt.getEdge().getV2().toString().equals(obt1.getEdge().getV2().toString())
			|| obt.getEdge().getV2().toString().equals(obt1.getEdge().getV1().toString())
			)&& !joinGroups2.contains(obt1)) {
		joinGroups2.add(obt1);
	}
	
}

}

//logger.info("This is the created table in left :"+joinGroups2);


String[] sic1;
LinkedListMultimap<EdgeOperator, Integer> joinGroupsOrdered =  LinkedListMultimap.create(); 
LinkedListMultimap<EdgeOperator, Integer> joinGroups3 =  LinkedListMultimap.create();

for(int i=0;i<Optimizer.triples.length;i++)
	if(Optimizer.triples[i][8]!=null) {
	sic1=Optimizer.triples[i][8].toString().replaceAll("[^0-9,]","").split(",");
	for(String si:sic1)	
		if(!si.equals(""))
	for(int i1=0;i1< joinGroups2.size();i1++)
		if(GetVertexName(joinGroups2.get(i1).getEdge().getV1()).equals(Optimizer.triples[i][0].toString().replaceAll("\"", "")) &&
				GetVertexName(joinGroups2.get(i1).getEdge().getV2()).equals(Optimizer.triples[i][1].toString().replaceAll("\"", "")))
			joinGroups3.put(joinGroups2.get(i1),Integer.parseInt(si));
	}
for(EdgeOperator obt:operators_BushyTreeOrdered)
	for(Entry<EdgeOperator, Integer> jg2:joinGroups3.entries())
		if(obt.getEdge().toString().equals(jg2.getKey().getEdge().toString()))
			joinGroupsOrdered.put(jg2.getKey(),jg2.getValue());

//for(Entry<EdgeOperator, Integer> jg3:joinGroupsOrdered.entries())
	//logger.info("This is the created table in left :"+jg3.getValue()+"--"+jg3.getKey());


	return 	joinGroupsOrdered;
}


public static Map<List<EdgeOperator>, Integer> CreateBushyTreesExclusive(List<EdgeOperator> operators_BushyTree, Map<Integer, List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator>> joinGroups,List<EdgeOperator> operators,List<EdgeOperator> operatorsOrder) {
	
LinkedHashMap<Integer,List<EdgeOperator>> eSet= new LinkedHashMap<>();
String[] sic;
for(int i=0;i<Optimizer.triples.length;i++)
	if(Optimizer.triples[i][8]!=null) {
	sic=Optimizer.triples[i][8].toString().replaceAll("[^0-9,]","").split(",");
	for(String si:sic)	
		if(!si.equals(""))
		eSet.put(Integer.parseInt(si.toString().replaceAll(",","")),new ArrayList<>());
	}
//for(Entry<Integer, List<EdgeOperator>> es:eSet.entrySet())
	//System.out.println("This is now the new list444444:"+es);

 
			for(EdgeOperator oiSub:operators_BushyTree) {
			//	if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) {
						
				//	//logger.info("#######################Sub Query to be seperated###################:"+oiSub);

String CurrentURL = null ;


for(int i=0;i<Optimizer.triples.length;i++) 
if(Optimizer.triples[i][8]!=null) {
//logger.info("This is tarzan:"+Optimizer.triples[i][1]+"--"+GetVertexName(oiSub.getEdge().getV2()));
	if(GetVertexName(oiSub.getEdge().getV1()).toString().equals(Optimizer.triples[i][0].toString()) && GetVertexName(oiSub.getEdge().getV2()).toString().equals(Optimizer.triples[i][1].toString().replaceAll("\"", "")))	{
//	for(Entry<Integer, HashSet<EdgeOperator>> es1:eSet.entrySet())
//		if(es1.getKey())
		////logger.info("1This is now checking the exclusive group00000:"+Optimizer.triples[i][8]);
			
		sic=Optimizer.triples[i][8].toString().replaceAll("[^0-9,]","").split(",");
		int kk=0;
		for(String si:sic)	{
			if(si.length()>1)
			if(!si.equals("") ) {
				List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator> nn = new ArrayList<>();	
				
				for(Entry<Integer, List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator>> es1:eSet.entrySet())
					if(es1.getKey()==Integer.parseInt(si.toString().replaceAll(",","")))
					{//logger.info("1This is now checking the exclusive group:"+es1.getKey()+"--"+es1.getValue());
					//logger.info("1This is now checking the exclusive group111:"+oiSub);
				if(!es1.getValue().toString().contains(oiSub.toString()))
					nn.add(oiSub);
					nn.addAll(es1.getValue());
					eSet.replace(Integer.parseInt(si.toString().replaceAll(",","")), nn);	
					kk++;
					}
					}
		}}
	}	
}

		
			Map<List<EdgeOperator>, Integer> joinGroups2 = new HashMap<>();
			for(Entry<Integer, List<EdgeOperator>> es1:eSet.entrySet())

			{		
		//logger.info("This is the triple444:"+es);
	List<EdgeOperator> mm = new ArrayList<>();
	
	//for(Entry<Integer, List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator>> es:eSet.entrySet())
		
	//{
		for(EdgeOperator obt:operators_BushyTree)		
				
			{
				for(EdgeOperator es2:es1.getValue())
					{
					if(obt.toString().equals(es2.toString())) {
					if(!mm.toString().contains(obt.toString()))
					mm.add(obt);
				//logger.info("This is the triple555:"+es1.getKey()+"--"+es2);
						}

					}
		
	}
		List<EdgeOperator> mm2= new ArrayList<>(mm);
			joinGroups2.put(mm2, es1.getKey());
		//logger.info("-----------------------------------------------------------------");
				mm.clear();

			}


//for(Entry<Integer, List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator>> es:joinGroups2.entrySet())
//	//logger.info("This is the triple444445555:"+es.getKey()+"--"+es.getValue());

	
return joinGroups2;
}

/*public static LinkedHashSet<EdgeOperator> ProcessOrdering(LinkedHashSet<EdgeOperator> FinalOrderingTemp,LinkedHashMap<EdgeOperator, List<EdgeOperator>> orderingElementsTemp){
	LinkedHashSet<EdgeOperator> FinalOrdering = new LinkedHashSet<>();
	EdgeOperator finalV = null;
	for(EdgeOperator o:FinalOrderingTemp) {
	FinalOrdering.add(o);	
break;
}

for(Entry<EdgeOperator, List<EdgeOperator>> ord:orderingElementsTemp.entrySet())
	{
			
			
		for(EdgeOperator o2:ord.getValue())
		{
			for(EdgeOperator o:FinalOrderingTemp) {
			
				if(GetVertexName(o.getEdge().getV1()).toString().equals(GetVertexName(o2.getEdge().getV2()).toString()))			
		{
			//		//logger.info();
					finalV = o; 
//			FinalOrdering.add(o2);
			//continue;
		}	
				if(GetVertexName(o.getEdge().getV1()).toString().equals(GetVertexName(o2.getEdge().getV2()).toString()))			
				{
					//		//logger.info();
							finalV = o; 
//					FinalOrdering.add(o2);
					//continue;
				}	
	/*	if(GetVertexName(o.getEdge().getV1()).toString().equals(GetVertexName(o2.getEdge().getV2()).toString()))			
		{	
			finalV = o; 
//			FinalOrdering.add(o2);
		//	continue;
		}
		if(GetVertexName(o.getEdge().getV2()).toString().equals(GetVertexName(o2.getEdge().getV2()).toString()))			
		{			finalV = o; 
//		FinalOrdering.add(o2);
	//continue;
		}	
		/*if(GetVertexName(o.getEdge().getV2()).toString().equals(GetVertexName(o2.getEdge().getV2()).toString()))				
		{			finalV = o; 
//		FinalOrdering.add(o2);
}
			}

	}
		FinalOrdering.add(finalV);
		}
return FinalOrdering;
}*/
/*static private Set<com.hp.hpl.jena.sparql.core.Var> getJoinVars(Collection<Binding> left, Collection<Binding> right) {
	if (left == null || right == null)
		return null;
	if (left.isEmpty() == true || right.isEmpty() == true)
		return null;
	if (left.size() == 0 || right.size() == 0)
		return null;

	//logger.info("This is in Join QueryUtil5:"+right.size()+"--"+left.size());

	Set<com.hp.hpl.jena.sparql.core.Var> joinVars = new HashSet<com.hp.hpl.jena.sparql.core.Var>();
	Iterator<com.hp.hpl.jena.sparql.core.Var> l = left.iterator().next().vars();
	BindingSet r = right.iterator().next();
	////logger.info("This is rule no. 1:"+r);
	while (l.hasNext()) {
		com.hp.hpl.jena.sparql.core.Var v = l.next();
	//	//logger.info("This is rule no. 2:"+v);
		
		if (r.contains(v)) {
			joinVars.add(v);
		}
	}
//	//logger.info("This is in Join QueryUtil6:"+joinVars);
	
	return joinVars;
}


*/

/*public static Set<Binding> ResultsSize8(ArrayList<Set<Binding>> finalResultQueryOrder){
Set<Binding> r5= new HashSet<>();	

int a = 0,b=0;
rTreeLowestLevel.clear();
for(ArrayList<Integer> e:Treelowestlevel)
{
//	if(TreelowestlevelProcessed.contains(e))
//		continue;
	ForkJoinPool fjp = new ForkJoinPool(4);
		fjp.submit(()->
			rTreeLowestLevel=	QueryUtil.join(finalResultQueryOrder.get(e.get(0)),finalResultQueryOrder.get(e.get(1))));
		//try {
		//	ProcessedSets.wait();
		//} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
	//		e1.printStackTrace();
	//	}
		if((rTreeLowestLevel).size()>0 && !ProcessedSets.contains(rTreeLowestLevel))
	{	
			synchronized(ProcessedSets) {

			//logger.info("This is happening yeay1:"+Treelowestlevel);

				//r5=	QueryUtil.join(finalResultQueryOrder.get(e.get(0)),finalResultQueryOrder.get(e.get(1)));
				a=e.get(0);
				b=e.get(1);
				ProcessedSets.add(rTreeLowestLevel);
				//logger.info("This is happening yeay:"+ProcessedSets.size()+"--"+e.get(0)+"--"+e.get(1));
				//logger.info("This is happening yeay:"+ProcessedSets.get(0));
				
				LowestLevelProcess(a,b);
				break;			

			
	}
}
		fjp.shutdownNow();		

}

//logger.info("This is happening yeay1:"+Treelowestlevel);



return r5;

}*/
//public static void ResultsSize14(HashMap<List<EdgeOperator>, Set<Binding>> processedSets2, HashSet<List<EdgeOperator>> evaluatedTriples, LinkedHashMap<List<EdgeOperator>, Set<Binding>> joinedTriples)
//{


	//if(evaluatedTriples.size()==0)
	//{
//	for(ArrayList<Integer> e:Treelowestlevel)
//	{
		//System.out.println("This is the current size of evaluated triples:"+HashJoin.EvaluatedTriples.size());
//		if(HashJoin.EvaluatedTriples.size()>0)
//			break;
//


	//}
//	}
//	else {
	/*
		for(Integer e:TreelowestlevelProcessed) {
			LinkedHashMap<List<EdgeOperator>, Set<Binding>> firstSet = new LinkedHashMap<>();
			int equity=0;
			for( Entry<List<EdgeOperator>, Set<Binding>> ps2:processedSets2.entrySet())
			{firstSet.clear();
				firstSet.put(ps2.getKey(),ps2.getValue());
			//	System.out.println("This is the numbering:"+e+"--"+equity);
				if(equity==e)
					break;
			equity++;
			}
			int s=0;
			for(List<EdgeOperator> et:evaluatedTriples) {
				//if(firstSet.toString().contains("id")) {
				//System.out.println("This is firstSet:"+firstSet);
				//System.out.println("This is SecondSet:"+et);
				//}
				if(firstSet.containsKey(et))
				{s=0 ;break;}
			if(s==1) {continue;}
			}
	//		for(List<EdgeOperator> et:evaluatedTriples)
	//	System.out.println("Second problem:"+et);
			
		//	System.out.println("First problem:"+firstSet.keySet());
		//	System.out.println("Second problem:"+joinedTriples.keySet());
				
			
			
				HashJoin.ProcessingTask(joinedTriples,firstSet);
				

		}
			*/
		
	//	}
	

//}
public void enableBindJoins(HashSet<List<EdgeOperator>> JoinGroupLists) {
	Set<EdgeOperator> BindEdges = new HashSet<>();
	Set<EdgeOperator> HashEdges = new HashSet<>();
	Set<Vertex> ProcessedVertex = new HashSet<>();
	for(List<EdgeOperator> e:JoinGroupLists) {
		for(EdgeOperator e1:e)
		{	if(e1.toString().contains("Bind"))
			BindEdges.add(e1);
				if(e1.toString().contains("Hash"))	
					HashEdges.add(e1);
		}
	;	
	}
	ConcurrentHashMap<Vertex, Edge> temp = new ConcurrentHashMap<>();

	for(EdgeOperator be:BindEdges)
		for(EdgeOperator he:HashEdges)
		if(be.getStartVertex().equals(he.getEdge().getV1()) || be.getStartVertex().equals(he.getEdge().getV2()))
		{	

			temp.put(be.getStartVertex(), he.getEdge());
			StartBindingFinal.put(temp,  new ArrayList<>());
			//Processed.add(he);
			ProcessedVertex.add(be.getStartVertex());
		}
	System.out.println("These are bindedges:"+BindEdges);
	System.out.println("These are pvertex:"+ProcessedVertex);
	
	for(EdgeOperator be:BindEdges)
		for(EdgeOperator he:BindEdges)
		if(be.getStartVertex().equals(he.getEdge().getV1()) &&  ProcessedVertex.contains(he.getStartVertex()) && !be.getStartVertex().equals(he.getStartVertex()))
		{	
			ProcessedVertex.add(be.getStartVertex());
temp.put(be.getEdge().getV1(), he.getEdge());
			StartBindingFinal.put(temp,  new ArrayList<>());
		}

	for(EdgeOperator be:BindEdges)
		for(EdgeOperator he:BindEdges)
		if(be.getStartVertex().equals(he.getEdge().getV2())&&   ProcessedVertex.contains(he.getStartVertex())&& !be.getStartVertex().equals(he.getStartVertex()))
		{	

		temp.put(be.getEdge().getV2(), he.getEdge());
			StartBindingFinal.put(temp, new ArrayList<>());
		}
	for(Entry<Vertex, Edge> obt:temp.entrySet())
		System.out.println("This is obt value:"+obt);
	
	for(Entry<ConcurrentHashMap<Vertex, Edge>, ArrayList<Binding>> obt:StartBindingFinal.entrySet())
		System.out.println("This is StartBindingFinal value:"+obt);
	
	//	StartBindingFinal.put(temp, null);
	
		for(EdgeOperator e1:HashEdges)
		{			if(ProcessedVertex.contains(e1.getEdge().getV1())){
						BindJoin a =new BindJoin(e1.getEdge().getV1(),e1.getEdge());
						if(JoinGroupsListLeft.contains(e1))
						{	JoinGroupsListLeft.remove(e1);
						JoinGroupsListLeft.add(a);
						}
						if(JoinGroupsListRight.contains(e1))
						{	JoinGroupsListRight.remove(e1);
						JoinGroupsListRight.add(a);
						}
						} 
					if(ProcessedVertex.contains(e1.getEdge().getV2())) {
						BindJoin a =new BindJoin(e1.getEdge().getV2(),e1.getEdge());
						if(JoinGroupsListLeft.contains(e1))
						{	JoinGroupsListLeft.remove(e1);
						JoinGroupsListLeft.add(a);
						}
						if(JoinGroupsListRight.contains(e1))
						{	JoinGroupsListRight.remove(e1);
						JoinGroupsListRight.add(a);
						}

					}
					}
		
//int l=0;

}

public HashMap<HashSet<List<EdgeOperator>>,Integer> MatchOperators(HashSet<List<EdgeOperator>> Temp,HashSet<List<EdgeOperator>> Temp1){
	HashSet<List<EdgeOperator>> xyz_current = new HashSet<>();
	HashMap<HashSet<List<EdgeOperator>>,Integer> 	cde = new HashMap<>();

	HashSet<List<EdgeOperator>>	xyz_processed = new HashSet<>();
	int i=0;
	int fee=0;

//	for(EdgeOperator bo:operators_BushyTreeOrder)
//	System.out.println("This is bushytreeorder:"+bo);
		
		for(List<EdgeOperator> e:Temp) {

		for(List<EdgeOperator> eS:Temp1) {
		//	try {
		//		Thread.sleep(500);
		//	} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
		//		e3.printStackTrace();
		//	}
			
		for(EdgeOperator e1:e)
			{	
			/*try {
				Thread.sleep(500);
			} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}*/
			for(EdgeOperator e2:eS)
			if(	!( xyz_processed.toString().contains(e1.toString())|| xyz_processed.toString().contains(e2.toString()))){
			{	
				xyz_current=new HashSet<>();
				
			//	try {
			//	Thread.sleep(500);
			//} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
		//		e3.printStackTrace();
		//	}
//				if(!e.equals(eS)) {
				if(!e1.equals(e2)) {
			
			{			
			//	try {
				//	Thread.sleep(500);
				//}/ catch (InterruptedException e3) {
					// TODO Auto-generated catch block
				//	e3.printStackTrace();
			//	}
				
				if(e1.getEdge().getV1().equals(e2.getEdge().getV2())) {
				//	xyz_current=new HashSet<>();
					
					xyz_current.add(eS);
		xyz_current.add(e);
		xyz_processed.add(e);
		xyz_processed.add(eS);
	
		
		
		if(!cde.containsKey(xyz_current))
		{
			HashSet<List<EdgeOperator>> JoinGroupsListExclusiveTemp1 =new HashSet<>();
			for(EdgeOperator obt1:operators_BushyTreeOrder)
				for(List<EdgeOperator> jg2:xyz_current)
							for(EdgeOperator jg4:jg2)
					if(jg4.getEdge().equals(obt1.getEdge()) )
						JoinGroupsListExclusiveTemp1.add(jg2);
			cde.put( JoinGroupsListExclusiveTemp1,i);
			}
	
		fee=1;
			}		
			
			if(e1.getEdge().getV1().equals(e2.getEdge().getV1()))
			{	//	System.out.println("This is now current work2:"+e1.getEdge().getV1()+"--"+e2.getEdge().getV1()+"--"+e+"--"+eS);
			xyz_current.add(eS);
			xyz_current.add(e);
if(!cde.containsKey(xyz_current))
{
	//try {
	//	Thread.sleep(500);
	//} catch (InterruptedException e3) {
		// TODO Auto-generated catch block
	//	e3.printStackTrace();
	//}
	HashSet<List<EdgeOperator>> JoinGroupsListExclusiveTemp1 =new HashSet<>();
	for(EdgeOperator obt1:operators_BushyTreeOrder)
		for(List<EdgeOperator> jg2:xyz_current)
					for(EdgeOperator jg4:jg2)
			if(jg4.getEdge().equals(obt1.getEdge()) )
				JoinGroupsListExclusiveTemp1.add(jg2);
	cde.put(JoinGroupsListExclusiveTemp1,i );
		
}
xyz_processed.add(e);
			xyz_processed.add(eS);
			
			//xyz_current=new HashSet<>();
			
			fee=1;
			}	
			if(e1.getEdge().getV2().equals(e2.getEdge().getV1()))
			{	
	//			System.out.println("This is now current work3:"+e1.getEdge().getV2()+"--"+e2.getEdge().getV1()+"--"+e+"--"+eS);
			xyz_current.add(eS);
			xyz_current.add(e);
			if(!cde.containsKey(xyz_current))
			{
				HashSet<List<EdgeOperator>> JoinGroupsListExclusiveTemp1 =new HashSet<>();
				for(EdgeOperator obt1:operators_BushyTreeOrder)
					for(List<EdgeOperator> jg2:xyz_current)
								for(EdgeOperator jg4:jg2)
						if(jg4.getEdge().equals(obt1.getEdge()) )
							JoinGroupsListExclusiveTemp1.add(jg2);
				cde.put( JoinGroupsListExclusiveTemp1,i);
				
			}
			xyz_processed.add(e);
			xyz_processed.add(eS);
	//				xyz_current=new HashSet<>();
			
			fee=1;
			}	
			
			
			if(e1.getEdge().getV2().equals(e2.getEdge().getV2()))
		   {	//	try {
		//		Thread.sleep(500);
		//	} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
			//	e3.printStackTrace();
		//	}
//				System.out.println("This is now current work4:"+e1.getEdge().getV2()+"--"+e2.getEdge().getV2()+"--"+e+"--"+eS);
			xyz_current.add(eS);
			xyz_current.add(e);
			if(!cde.containsKey(xyz_current))
			{
				HashSet<List<EdgeOperator>> JoinGroupsListExclusiveTemp1 =new HashSet<>();
				for(EdgeOperator obt1:operators_BushyTreeOrder)
					for(List<EdgeOperator> jg2:xyz_current)
								for(EdgeOperator jg4:jg2)
						if(jg4.getEdge().equals(obt1.getEdge()) )
							JoinGroupsListExclusiveTemp1.add(jg2);
				cde.put( JoinGroupsListExclusiveTemp1,i);
				
			}
			xyz_processed.add(e);
			xyz_processed.add(eS);
					
		//	xyz_current=new HashSet<>();
			fee=1;	
		   }	

} 		
			
			} 
			//
	//3		}
			}	//System.out.println("Remaning Numbers:"+cde_current.size());
			/*try {
				Thread.sleep(500);
			} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}*/
			}	//	System.out.println("Remaning Numbers:"+cde_current.size());
			
			}
		
//		System.out.println("Remaning Numbers:"+cde_current);
			if(fee==1) {
				fee=0;
//				break;
			}	}
			i++;
			}
	i=0;

return cde;
}

public HashMap<HashSet<List<EdgeOperator>>, Integer> ExecutionOrder(HashSet<List<EdgeOperator>> JoinGroupLists) {
//	LinkingTree
	HashMap<HashSet<List<EdgeOperator>>,Integer> 	cde = new HashMap<>();
	HashMap<HashSet<List<EdgeOperator>>,Integer> 	cdeDup = new HashMap<>();
	HashSet<List<EdgeOperator>>	Temp = new HashSet<>();
	HashSet<List<EdgeOperator>>	Temp1 = new HashSet<>();

	//	for(List<EdgeOperator> j:JoinGroupLists)
//	System.out.println("This is next:"+j);
//	for(EdgeOperator obt1:operators_BushyTreeOrder)
//		System.out.println("This is next222:"+obt1);
	
	for(EdgeOperator obt1:operators_BushyTreeOrder)
	for(List<EdgeOperator> ee:JoinGroupLists)
		if( !ee.isEmpty())
	for(EdgeOperator ee1:ee)
			if(ee1.toString().equals(obt1.toString()))
			Temp.add(ee);
	for(EdgeOperator obt1:operators_BushyTreeOrder)
		for(List<EdgeOperator> ee:JoinGroupLists)
			if( !ee.isEmpty())
		for(EdgeOperator ee1:ee)
				if(ee1.toString().equals(obt1.toString()))
				Temp1.add(ee);
		
	//for(List<EdgeOperator> t:Temp)
	//System.out.println("This is JoinGroupLists:"+t);
	
		cde = MatchOperators(Temp,Temp1);
	
	HashSet<List<EdgeOperator>> notPresent=new HashSet<>();
	
	for(List<EdgeOperator> e1:Temp)
	{
		//System.out.println("This is the old old new list:"+e1);
			
		notPresent.add(e1);
	}
//	System.out.println("This is the old old old list:"+notPresent);
//	System.out.println("This is the old old old cde list:"+cde);
	
	for(HashSet<List<EdgeOperator>> e1:cde.keySet())
			for(List<EdgeOperator> e2:e1)	
	//			for(EdgeOperator e3:e2)
		notPresent.remove(e2);

	
				
					
						//	for(HashSet<List<EdgeOperator>> cc1:cde.values())
	
	
	//System.out.println("This is in old list:"+notPresent);					
	//System.out.println("This is in old cde list:"+cde);
	

	if(o==1) {
	HashMap<HashSet<List<EdgeOperator>>, Integer> cde2 = ExecutionOrder(notPresent);
	int count=Collections.max(cde.values());
	HashSet<List<EdgeOperator>> du = new HashSet<>();
	for( HashSet<List<EdgeOperator>> c:cde2.keySet()) {
		du.addAll(c);
		count++;
		cde.put(du,count);
	}
	
	o++;
	}
//	for(Entry<HashSet<List<EdgeOperator>>, Integer> cde1:cde.entrySet()) {
//		System.out.println("Here is the main problem:"+cde1);
//	}
cde.remove(null);
HashSet<List<EdgeOperator>>	Done=new HashSet<> ();
int KeyNo=0;
cdeDup.putAll(cde);
Iterator<Entry<HashSet<List<EdgeOperator>>, Integer>> Outer = cde.entrySet().iterator();
 while(Outer.hasNext())
 {
	Entry<HashSet<List<EdgeOperator>>, Integer> Outer4 = Outer.next();
	HashSet<List<EdgeOperator>> Outer1Value = Outer4.getKey();//Outer.next().getKey();
//s	int Outer1Key= Outer.next().getValue();
Iterator<List<EdgeOperator>> Outer1 = Outer1Value.iterator();
	while(Outer1.hasNext())
	{
	List<EdgeOperator> Outer2Value = Outer1.next();
	Iterator<List<EdgeOperator>> InnerIterator = notPresent.iterator();
	while(InnerIterator.hasNext()) {
	List<EdgeOperator> Inner = InnerIterator.next();
	Iterator<EdgeOperator> Inner1Iterator = Inner.iterator();
	//if(!Done.equals(Outer1Value))
//	try {
//		Thread.sleep(500);
//	} catch (InterruptedException e3) {
		// TODO Auto-generated catch block
//		e3.printStackTrace();
//	}
	while(Inner1Iterator.hasNext()) {
	EdgeOperator Inner1 = Inner1Iterator.next();
	/*		try {
				Thread.sleep(500);
			} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}*/
			if(Outer2Value.toString().contains(Inner1.getEdge().getV1().toString()) || Outer2Value.toString().contains(Inner1.getEdge().getV2().toString())) {
			/*	try {
					Thread.sleep(500);
				} catch (InterruptedException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}*/
				HashSet<List<EdgeOperator>> Involvment = new HashSet<>();
				Involvment.add(Inner);
			//	Inner.remove(Inner1);
				if(!Done.equals(Involvment))
				cdeDup.put(Involvment,Outer4.getValue());
				Done.addAll(Involvment);
//	System.out.println("This is in final cde list:"+Outer2Value);
	//System.out.println("This is in final list list:"+Inner1);

	//KeyNo=Outer1Key;
			}
		}
			}
	}
	}
   /*Iterator<Entry<HashSet<List<EdgeOperator>>, Integer>> cde1 = cde.entrySet().iterator();
   while(cde1.hasNext()) {
	   HashSet<List<EdgeOperator>> cde1Value = cde1.next().getKey();
	 Iterator<Entry<HashSet<List<EdgeOperator>>, Integer>> cde21= cde.entrySet().iterator();
	   while(cde21.hasNext()) {
		   HashSet<List<EdgeOperator>> cde2Value = cde21.next().getKey();
			if(cde1Value.equals(cde2Value))
				cde.remove(cde1Value);
	  
	   }
   }*/
 
 HashSet<HashMap<HashSet<List<EdgeOperator>>,Integer>> 	cdehash = new HashSet<>();

 LinkedHashMap<HashSet<List<EdgeOperator>>,Integer> CdeUnOrder = new LinkedHashMap<>();
 
 
for(HashSet<List<EdgeOperator>> cde9:cdeDup.keySet())
	// for(List<EdgeOperator> cde10:cde9)
	//	 System.out.println("This is really bad:"+cde10);
 for(HashSet<List<EdgeOperator>> cde1:cdeDup.keySet()) {
	if(!cde1.isEmpty() || cde1!=null || !(cde1.size()==0))
	 CdeUnOrder.put(cde1, cde1.size());
	// System.out.println("This is HashSet Size:"+cde1.size()+"--"+cde1);
 }

 LinkedHashMap<HashSet<List<EdgeOperator>>,Integer> CdeOrder = new LinkedHashMap<>();
 //System.out.println("This is hash hash hash:"+CdeUnOrder.values().toArray()[0]);
 int size=0;
 if(CdeUnOrder.values().toArray()[0]!=null )
	 size=(int)CdeUnOrder.values().toArray()[0];
 else
 size=(int)CdeUnOrder.values().toArray()[1];
 if(size==1)
 CdeOrder=CdeUnOrder.entrySet()
 .stream()
 .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) 
 .collect( toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                 LinkedHashMap::new));
 else
	 CdeOrder.putAll(CdeUnOrder);
 
 //for(Entry<HashSet<List<EdgeOperator>>, Integer> cde1:CdeOrder.entrySet()) {
//	 CdeUnOrder.put(cde1.size(), cde1);
	 
//	 System.out.println("This is ordered HashSet Size:"+cde1);
 //}
 cdehash.add(CdeOrder);


 LinkedHashMap<HashSet<List<EdgeOperator>>,Integer> 	cdeSemiFinal = new LinkedHashMap<>();
 for( HashMap<HashSet<List<EdgeOperator>>, Integer> cdeh:cdehash)
 cdeSemiFinal.putAll(cdeh);

 LinkedHashMap<HashSet<List<EdgeOperator>>,Integer> 	cdeFinal = new LinkedHashMap<>();
 
 for(HashSet<List<EdgeOperator>>  co:CdeOrder.keySet()) {
	 for(Entry<HashSet<List<EdgeOperator>>, Integer> cdeh:cdeSemiFinal.entrySet()) {
		 if(co.equals(cdeh.getKey()))
		 {	 
			 
			 cdeFinal.put(cdeh.getKey(),cdeh.getValue());
		 }
		 }
 }
 
	//System.out.println("This is in final cdeFinal:"+cdeFinal);
	linkingTreeDup.putAll(cdeFinal);
return cdeFinal;
}

/*
public static Set<Binding> finalResult(LinkedHashMap<EdgeOperator, Set<Binding>>  finalRes) {
	ArrayList<Set<Binding>> finalResultQueryOrder = new ArrayList<>();
	
	////logger.info("aaaaaaaaaa111:"+UnProcessedSets.parallelStream().limit(1).collect(Collectors.toSet()));
	
		Set<EdgeOperator> fresults= finalRes.keySet();
	//for(com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator fr:fresults)
	////logger.info("These are now the ordered by similarity of words:"+fr);
	
	
	
	//for (Entry<EdgeOperator, Set<Binding>> eq:finalRes.entrySet())
	//if(eq.getValue()!=null)	
	//	if(eq.getKey().toString().equals("Bind join: (?place: 0 true)--(?place: 0 true) - (?longitude: 0 true)"))
	//for(BindingSet eq11:eq.getValue())
	//	{//logger.info("THis is currently the entering part B6 segment123123123123123:"+eq.getKey()+"--"+eq.getValue().size());
	
		
		//}
	for(EdgeOperator fr:fresults)
	for (Entry<EdgeOperator, Set<Binding>> eq2:finalRes.entrySet())
if(eq2.getValue()!=null)
		if(fr!=null )
		if(fr.toString().equals(eq2.getKey().toString()) && eq2.getValue().size()>0)
	{
	//		//logger.info("THis is one last thing left:"+fr+"--"+eq2.getValue().size());
	finalResultQueryOrder.add(eq2.getValue());
	}
System.out.println("This is the size:"+fresults.size()+"--"+finalRes.size()+"--"+LocalTime.now());
UnProcessedSets.addAll(finalResultQueryOrder);
	//for(Set<Binding> ek:finalResultQueryOrder)
	//	//logger.info("THis is currently the entering part B6 segment123123123123123:"+"--"+ek.size());
//	finalResultQueryOrder.notifyAll();
	
return	finalResultCalculation(finalResultQueryOrder);
}*/



public static ArrayList<Binding> GPUJoin(Collection<Binding> left, Collection<Binding> right,Collection<Binding> left2, Collection<Binding> right2,int type) {
	Iterator<Binding> r = right.iterator();
	Iterator<Binding> l = left.iterator();
	
	int a=0;
	
	ArrayList<String> headersAllLeft= new ArrayList<>();
	ArrayList<String> headersAllRight= new ArrayList<>();
//	ArrayList<String> headersAll= new ArrayList<>();
 int IsLeft=0;
// if(ParaEng.pConstant.contains("?p"))
	 System.out.println("This is::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::"+ParaEng.pConstant);
// for(BindingSet l:left)
//	 System.out.println("This is left side:"+l);

 //for(BindingSet l:right)
//	 System.out.println("This is right side:"+l);
 rightTable=null;
 leftTable=null;
 String length = "";

	String[] vv =null;
//	final String[][]	rTable;
	
System.out.println("These are all the headers:"+headersAll);

System.out.println("This is the size of left:"+left.size());

 System.out.println("This is the size of right:"+right.size());
	
	int incL=0;

// System.out.println("These are the optional Hedaers:"+OptionalHeaders);
// System.out.println("These are the optional Hedaers2:"+headersAllRight);
 //System.out.println("These are the optional Hedaers3:"+headersAllLeft);
 //System.out.println("These are the optional Hedaers4:"+OptionalHeadersRight);
 //System.out.println("These are the optional Hedaers5:"+OptionalHeadersLeft);

	
    //String[]
	  vv =null;
//String 

length = "";
Iterator<Binding> l2 = left.iterator();
while(l2.hasNext()) {
 length=l2.next().toString();
 vv=length.split(" ");
for(String v:vv)
if(v.startsWith("?")) {
headersAllLeft.add(v.substring(1));
//headersAll.add(v.substring(1));
//break;
}
break;
}
	//Iterator<Binding> l2 =left.iterator();

	//l2 = left.iterator();
	/*while(l2.hasNext()) {

		
	 Pattern regex = Pattern.compile(";(?=[a-zA-Z0-9])(?![$-:-?{-~!\"^_\\-`\\[\\]])");
	 String lll = l2.next().toString().replace("?", "");
	 System.out.println("This is left pattern:"+lll);
		  String[] regexMatcher = regex.split(lll);
	for(String ml:regexMatcher)
	{
				  headersAllLeft.add("?"+ml.substring(0,ml.indexOf('=')).replace("]","").replace("[",""));
	}


	break;
	 }
*/

for(String hla :headersAllLeft)
	System.out.println("This is headersAllLeft:"+hla);
for(String hla :headersAllRight)
	System.out.println("This is headersAllRight:"+hla);

incL=0;
	for(String hla :headersAllLeft)
	if(headersAll.contains(hla))
		incL++;
	if(incL==0)
	{for(String hla :headersAllLeft)
		if(headersAll.contains(hla))
			incL++;
		}



	
	      //String[]
	    		  vv =null;
	      //String 
	      
	      length = "";
	      Iterator<Binding> l21 = right.iterator();
	while(l21.hasNext()) {
	           length=l21.next().toString();
	           vv=length.split(" ");
	for(String v:vv)
	     if(v.startsWith("?")) {
	        headersAllRight.add(v.substring(1));
	//headersAll.add(v.substring(1));
	//break;
	}
	break;
		    }
	//ExecutorService fjp = Executors.newCachedThreadPool();
//	System.out.println("This is time1:"+LocalTime.now());      
/*for(Entry<String, String> a:ParaEng.pConstant.entrySet())
	if(headersAllRight.contains(a.getValue().substring(1)) && headersAllLeft.contains(a.getKey().substring(1))) {
	//	headersAllLeft.remove(a.getKey().substring(1));
	//	headersAllLeft.add(a.getValue().substring(1));
		int index = headersAllLeft.indexOf(a.getKey().substring(1));
		headersAllLeft.set(index, a.getValue().substring(1));
	}

for(Entry<String, String> a:ParaEng.pConstant.entrySet())
	if(headersAllLeft.contains(a.getValue().substring(1)) && headersAllRight.contains(a.getKey().substring(1))) {
	//	headersAllRight.remove(a.getKey().substring(1));
		int index = headersAllRight.indexOf(a.getKey().substring(1));
		headersAllRight.set(index, a.getValue().substring(1));
	}
*/
	
System.out.println("These are headersAllLeft:"+headersAllLeft);
System.out.println("These are headersAllRight:"+headersAllRight);

int hlsize=0;
	int hrsize=0;
	int hesize=0;
	
	if(headersAllRight.size()>headersAllLeft.size())
	{	for(String hl:headersAllLeft)
			if(headersAllRight.contains(hl))
				hlsize++;
	}
	if(headersAllRight.size()<headersAllLeft.size())
		{
		for(String hr:headersAllRight)
			if(headersAllLeft.contains(hr))
				hrsize++;
	}
	if(headersAllRight.size()==headersAllLeft.size())
	{
	for(String hr:headersAllRight)
		if(headersAllLeft.contains(hr))
			hesize++;
}
	if(hesize==1)
		hesize=0;
	if(hrsize==1)
		hrsize=0;
	if(hlsize==1)
		hlsize=0;
	
	if(hesize==headersAllRight.size() &&headersAllRight.size()==headersAllLeft.size())
		return null;
		
	if(hrsize==headersAllRight.size())
		return null;
	if(hlsize==headersAllLeft.size())
		return null;
		
		int incR=0;
	for(String hla :headersAllRight)
	if(headersAll.contains(hla))
		incR++;
	

	
int skip=0;
if(ParaEng.Optional.contains("OPTIONAL") ) {
	  incL=0;

	for(String hla :headersAllLeft)
 if(OptionalHeaders.contains(hla))
 	incL++;
 
	if(skip==0)
	if(OptionalHeaders!=null && OptionalHeaders.size()>0 )
 if(  OptionalHeaders.size()==incL)
 {
		System.out.println("These are right1:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
		
	 skip=1;
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	//	leftTable=TransformForGPU(right,right2);
		//ForkJoinPool fjp1 = new ForkJoinPool();
		//fjp1.submit(()->{ Stream.of(
				leftTable=TransformForGPU(right,null,headersAllRight);//).parallel();}).join();
	//	fjp1.shutdown();

		System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));

		System.out.println("These are left:"+left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
	//rightTable=TransformForGPU(left,left2);
	//ForkJoinPool fjp = new ForkJoinPool();
	//fjp.submit(()->{ Stream.of(
			rightTable=TransformForGPU(left,null,headersAllLeft);//).parallel();}).join();
//	fjp.shutdownNow();

	System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
	
	
 }

 
 incL=0;
for(String hla :headersAllRight)
if(OptionalHeaders.contains(hla))
	incL++;

if(skip==0)
if(OptionalHeaders!=null && OptionalHeaders.size()>0)
if( OptionalHeaders.size()==incL)
{
	System.out.println("These are right2:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	
		System.out.println("These are left:"+left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
	//leftTable=TransformForGPU(left,left2);
	//ForkJoinPool fjp = new ForkJoinPool();
//	fjp.submit(()->{ Stream.of(
			leftTable=TransformForGPU(left,null,headersAllLeft);//).parallel();}).join();
	//fjp.shutdownNow();

	System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
	 skip=1;
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	//rightTable=TransformForGPU(right,right2);
//		ForkJoinPool fjp1 = new ForkJoinPool();
	//	fjp1.submit(()->{ Stream.of(rightTable=
				TransformForGPU(right,null,headersAllRight);//).parallel();}).join();
	//	fjp1.shutdown();

		System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));

}

incL=0;
for(String hla :headersAllLeft)
if(OptionalHeadersRight.contains(hla))
	incL++;

if(skip==0)
if(OptionalHeadersRight!=null && OptionalHeadersRight.size()>0)
if( OptionalHeadersRight.size()==incL)
{
	System.out.println("These are right3:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
		//leftTable=TransformForGPU(right,right2);
		ForkJoinPool fjp1 = new ForkJoinPool();
		//fjp1.submit(()->{ Stream.of(
				leftTable=TransformForGPU(right,null,headersAllRight);//).parallel();}).join();
		//fjp1.shutdown();

		System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));
		 skip=1;
		System.out.println("These are left:"+left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
//	ForkJoinPool fjp = new ForkJoinPool();
	//fjp.submit(()->{ Stream.of(
			rightTable=TransformForGPU(left,null,headersAllLeft);//).parallel();}).join();
	//fjp.shutdownNow();
	System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
	
	
}


incL=0;
for(String hla :headersAllRight)
if(OptionalHeadersRight.contains(hla))
	incL++;

if(skip==0)

if(OptionalHeadersRight!=null && OptionalHeadersRight.size()>0)
if( OptionalHeadersRight.size()==incL)
{
	System.out.println("These are right4:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	
		System.out.println("These are left:"+left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
	//	ForkJoinPool fjp1 = new ForkJoinPool();
	//	fjp1.submit(()->{ Stream.of(
				leftTable=TransformForGPU(left,null,headersAllLeft);//).parallel();}).join();
	//	fjp1.shutdown();
			System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
	 skip=1;
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	//ForkJoinPool fjp = new ForkJoinPool();
	//fjp.submit(()->{ Stream.of(
			rightTable=TransformForGPU(right,null,headersAllRight);//).parallel();}).join();
	//fjp.shutdownNow();
	
	System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));

}

incL=0;
for(String hla :headersAllLeft)
if(OptionalHeadersLeft.contains(hla))
	incL++;

if(skip==0)

if(OptionalHeadersLeft!=null && OptionalHeadersLeft.size()>0)
if( OptionalHeadersLeft.size()==incL)
{
	System.out.println("These are right5:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
//		ForkJoinPool fjp1 = new ForkJoinPool();
	//	fjp1.submit(()->{ Stream.of(
				leftTable=TransformForGPU(right,null,headersAllRight);//).parallel();}).join();
		//fjp1.shutdown();
		System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));
		
		skip=1;
		System.out.println("These are left:"+left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
		//ForkJoinPool fjp = new ForkJoinPool();
		//fjp.submit(()->{ Stream.of(
				rightTable=TransformForGPU(left,null,headersAllLeft);//);//.parallel();}).join();
	//	fjp.shutdownNow();
	System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
	
	
}

incL=0;
for(String hla :headersAllRight)
if(OptionalHeadersLeft.contains(hla))
	incL++;

if(skip==0)
if(OptionalHeadersLeft!=null && OptionalHeadersLeft.size()>0 )
if( OptionalHeadersLeft.size()==incL)
{
	System.out.println("These are right6:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	//leftTable=TransformForGPU(left,left2);
//	ForkJoinPool fjp1 = new ForkJoinPool();
//	fjp1.submit(()->{ Stream.of(
			leftTable=TransformForGPU(left,null,headersAllLeft);//).parallel();}).join();
//	fjp1.shutdown();
	
	System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
	 skip=1;
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	//rightTable=TransformForGPU(right,right2);
//	ForkJoinPool fjp = new ForkJoinPool();
//	fjp.submit(()->{ Stream.of(
			rightTable=TransformForGPU(right,null,headersAllRight);//).parallel();}).join();
//	fjp.shutdownNow();
	
	System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));

}
}
System.out.println("THis is yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
if(skip==0) {
 if(headersAllRight.size()>headersAllLeft.size())
	{
		System.out.println("These are left:"+left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
//		for(BindingSet l:left)
//			System.out.println("These are left:"+l);//left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));

//	 ForkJoinPool fjp1 = new ForkJoinPool();
//		fjp1.submit(()->{
			leftTable=TransformForGPU(left,null,headersAllLeft);
			//}).join();
//		fjp1.shutdown();
	System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
	
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	//	ForkJoinPool fjp = new ForkJoinPool();
	//	fjp.submit(()->{
			rightTable=TransformForGPU(right,null,headersAllRight);//}).join();
	//	fjp.shutdownNow();
	System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));
	}
	else {
		System.out.println("These are right:"+right.size()+"--"+right.parallelStream().limit(2).collect(Collectors.toSet()));
	//ForkJoinPool fjp = new ForkJoinPool();
	//fjp.submit(()->{
	//	ArrayList<String> input2 =new ArrayList<>();
	//	boolean correct = str.matches("[-+]?[0-9]*\\.?[0-9]+");
	//	if(QueryTask.isBound==1)
/*			while(r.hasNext()) {
				String a1;
				//if(r.next().toString().matches("[-+]?[0-9]*\\.?[0-9]+"))
					a1=String.valueOf(r.next());// Float.toString(r.next());
				//	else
				//	a1=r.next().toString();
				
				System.out.println("This is in GPU Join:"+a1);
				input2.add(a1);
			//a1++;
			}*/
			System.out.println("This is going into function");
		rightTable=TransformForGPU(right,null,headersAllRight);//}).join();
	//fjp.shutdownNow();
		System.out.println("These are right result:"+rightTable.parallelStream().limit(2).collect(Collectors.toSet()));

		//for(BindingSet l:left)
		//	System.out.println("These are left:"+l);//left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
/*List<String >input1 = new ArrayList<>();
		if(QueryTask.isBound==1)
			while(l.hasNext()) {
				String a1=l.next().toString();
			//	System.out.println("This is in GPU Join:"+a1);
				input1.add(a1);
			//a1++;
			}*/

			System.out.println("These are left:"+left.size()+"--"+left.parallelStream().limit(2).collect(Collectors.toSet()));
		//ForkJoinPool fjp1 = new ForkJoinPool();
		//fjp1.submit(()->{ 
			leftTable=TransformForGPU(left,null,headersAllLeft);//}).join();
	//	fjp1.shutdown();
		System.out.println("These are left result:"+leftTable.parallelStream().limit(2).collect(Collectors.toSet()));
		
	
	}
}



int isAddition=0;
for(String s:HeaderReplacement.keySet())
if(headersAll.contains(s))
	isAddition=1;
if(headersAll.size()==incL && isAddition==0 )
	return null;

if(headersAll.size()==incR && isAddition==0)
	return null;


List<String[]> leftCsv = new ArrayList<String[]>();
/*for (int i = 0; i < leftTable.length; i++)
{
    String[] temp = new String[leftTable.get(0).size()];
    for (int n = 0; n < temp.length; n++)
    {
      System.out.print(" "+leftTable[i][n]);
    }
  System.out.println();  
//    leftCsv.add(temp);
}*/
if((leftTable.get(0).size()==1 && rightTable.get(0).size()>1) ||(rightTable.get(0).size()==1 && leftTable.get(0).size()>1) )
{	if(leftTable.get(0).size()==1 && leftTable.size()>5)
		type=4;
	if(rightTable.get(0).size()==1 && rightTable.size()>5)
		type=4;
}	
if((leftTable.get(0).size()==2 && rightTable.get(0).size()>2) ||(rightTable.get(0).size()==2 && leftTable.get(0).size()>2) )
{
if(leftTable.get(0).size()==2 && leftTable.size()>5)
		if(leftTable.get(0).get(1) ==null)
		type=4;
	if(rightTable.get(0).size()==2 && rightTable.size()>5)
		if(rightTable.get(0).get(1) ==null)
		type=4;
}
	leftCsv = new ArrayList<String[]>();
	for (int i = 0; i < leftTable.size(); i++)
	{
	    String[] temp = new String[leftTable.get(0).size()];
	    for (int n = 0; n < temp.length; n++)
	    {
	        temp[n] = leftTable.get(i).get(n);
	    }
	    
	    leftCsv.add(temp);
	}
	
	List<String[]> rightCsv = new ArrayList<String[]>();
	for (int i = 0; i < rightTable.size(); i++)
	{
	    String[] temp = new String[rightTable.get(0).size()];
	    for (int n = 0; n < temp.length; n++)
	    {
	        temp[n] = rightTable.get(i).get(n);
	    }
	    
	    rightCsv.add(temp);
	}
	//System.out.print("These are left var4");

	File file = new File("/mnt/hdd/hammad/hammad/leftTable.csv");
    
    if(file.delete())
    {
        System.out.println("File deleted successfully");
    }
    File file1 = new File("/mnt/hdd/hammad/hammad/rightTable.csv");
    
    if(file1.delete())
    {
        System.out.println("File deleted successfully1");
    }
	//System.out.print("These are left var4");
	   try (CSVWriter writer = new CSVWriter(new FileWriter("/mnt/hdd/hammad/hammad/leftTable.csv",true))) {
           writer.writeAll(leftCsv);
        //   writer.close();
	   } catch (IOException e4) {
		// TODO Auto-generated catch block
		e4.printStackTrace();
	}
	   try (CSVWriter writer1 = new CSVWriter(new FileWriter("/mnt/hdd/hammad/hammad/rightTable.csv",true))) {
           writer1.writeAll(rightCsv);
      //writer1.close();
	   } catch (IOException e3) {
		// TODO Auto-generated catch block
		e3.printStackTrace();
	}
	  
		//Engine ad =Engine.;
//	System.out.println("These are all the engines:"+Engine.getAllEngines());
//	NDManager manager = NDManager.newBaseManager();
	
//	PythonInterpreter pyInterp = new PythonInterpreter();
//	NDArray array = manager.ones(new Shape(1,3,2));

	System.out.println("Hello Python World blA BKA!");
	      GatewayServer.turnLoggingOff();
	        GatewayServer server = new GatewayServer();
	          server.start();
	      System.out.println("Hello Python World blA BKA!111:"+server.getListeningPort()+"--"+server.getPort()+"--"+server.getPythonPort()+"--"+server.getListeners().stream().collect(Collectors.toList())+"--"+server.getPythonAddress()+"--"+server.getAddress());
	      String join=null;	
	  		  
	      Field f = null;
		try {
			f = server.getClass().getDeclaredField("gateway");
		      System.out.println("Hello Python World blA BKA!555:"+f);
		  	
		} catch (NoSuchFieldException e2) {
// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SecurityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} //NoSuchFieldException
          f.setAccessible(true);
          Gateway gateway = new Gateway(null);
		try {
			gateway = (Gateway) f.get(server);
		    System.out.println("Hello Python World blA BKA!222:"+gateway.getBindings().values()+"--"+gateway.getEntryPoint());

		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
          String fakeCommandPart = "f" + Protocol.ENTRY_POINT_OBJECT_ID + ";" +"py4j.examples.IHello";
		  System.out.println("Hello Python World blA BKA!222:"+fakeCommandPart+"--"+gateway.getEntryPoint());

          //    System.out.println("Hello Python World blA BKA!22222");
		   RootClassLoadingStrategy rmmClassLoader = new RootClassLoadingStrategy();
		    ReflectionUtil.setClassLoadingStrategy(rmmClassLoader);
		  IHello hello = (IHello) Protocol.getPythonProxy(fakeCommandPart, gateway);
  		  System.out.println("Hello Python World blA BKA!9999:"+leftTable.size()+"--"+rightTable.size());
        try {
       	  System.out.println("Hello Python World blA BKA!33333");
       	  
       	if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION"))
       	{ try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if(type==4||QueryTask.IsLiteral==1 || QueryTask.IsURLProper==1)
       		//{
    		join=hello.sayHello(4);
       		//}
      // 		type=-1;}
       		if(type==0) 
       		{join=hello.sayHello(0);
       	 QueryTask.IsURLProper=0;
       		}
       	if(type==1)
       		join=hello.sayHello(1);
       	if(type==2)
       		join=hello.sayHello(2);
      
    
       	}else {
       	if(possibleOuter ==5)
            join=hello.sayHello(3);	
       	else	if(type==4||QueryTask.IsLiteral==1 || QueryTask.IsURLProper==1)
       		join=hello.sayHello(4);
       	else
       		join=hello.sayHello(0);
       	}
         } catch (Exception e) {
             e.printStackTrace();
         }
        server.shutdown();
 System.out.println("This is file initialization:"+LocalTime.now());
 /*
 List<List<String>> records = new ArrayList<List<String>>();
 try (CSVReader csvReader = new CSVReader(new FileReader("/mnt/hdd/hammad/hammad/output.csv"));) {
     String[] values = null;
     try {
			while ((values = csvReader.readNext()) != null) {
			    records.add(Arrays.asList(values));
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
 server.shutdown();       
 
  
*/
 
 File initialFile = new File("/mnt/hdd/hammad/hammad/output.csv");
 InputStream targetStream = null;
	try {
		targetStream = new FileInputStream(initialFile);
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
//TupleQueryResult v = null;

	   org.apache.jena.query.ResultSet v=     ResultSetFactory.load("/mnt/hdd/hammad/hammad/output.csv");

//while(v1.hasNext())
//	System.out.println("It has another element");

 //  System.out.println("This is v:"+v);
			ForkJoinPool fjp =new ForkJoinPool();
			fjp.submit(()->{
			resultoutput= postProcess(v);
					}).join();
	  return  resultoutput;		
        //}).join();
     //fjp.shutdownNow();
//for(BindingSet r:result)
//	System.out.println("This is verification:");
  

//return null;
}

public static  ArrayList<Binding> postProcess(ResultSet v) {
System.out.println("!!!!!!!!!!!!!!QueryTask7!!!!!!!!!!!!!!!!!!!!"+v);
	ArrayList<Binding> results = new ArrayList<Binding>();
	Binding a;
int l=0;
	while (v.hasNext()) {
	 a = v.nextBinding();

	 results.add(a);
		}
	

return results;
}

public static  List<ArrayList<String>> TransformForGPU(Collection<Binding> left, List<String> in,ArrayList<String> headersLeft ) {
	//List<String> headersLeft = new ArrayList<String>();
	List<String> headersAll = new ArrayList<String>();
	    String[] vv =null;
	      String length = "";
	      Iterator<Binding> l1 = left.iterator();
	      
	 //     Object String in1;
		//if(QueryTask.isBound==1)
		//		for(String in1:in) {
					//String a1=l1.next().toString();
		//		System.out.println("This is in GPU Join:"+a1);
					//input1.add(a1);
				//a1++;
		//		}
/*if(headersLeft==null) {	        
    Iterator<Binding> l2 = left.iterator();
while(l2.hasNext()) {
           length=l2.next().toString();
           vv=length.split(" ");
for(String v:vv)
     if(v.startsWith("?")) {
        headersLeft.add(v.substring(1));
headersAll.add(v.substring(1));
//break;
}
break;
	    }
}*/
for(String hla :headersLeft)
	System.out.println("This is headersAllLeft in GPU:"+hla);



//ExecutorService fjp = Executors.newCachedThreadPool();
System.out.println("This is time1:"+LocalTime.now());      
//}
//fjp.shutdownNow();
//executor.shutdown();
//ForkJoinPool fjp = new ForkJoinPool();
//fjp.submit(()-> {
//
Set<String> input =new HashSet<>();
int j=0;
//if(QueryTask.isBound==0)
while(l1.hasNext())
{
	
	
	String a=String.valueOf(l1.next());// l1.next().toString();
//	if(QueryTask.isBound==0)

	input.add(a);
//	if(QueryTask.isBound==1)
//	System.out.println("This is value:"+j+"--"+a);
j++;	
}
		rowsLeft=transformResult(input,in,headersLeft);
//	}).join();
//ForkJoinTask.join();
//fjp.shutdownNow();
;
		System.out.println("This is it::::::::::::::::::::::::::::::::::::::::::::::::");
       System.out.println("This is time2:"+LocalTime.now());      

//       for(String r:rowsLeft)
  //        System.out.println("This is correct:"+r);            
//Stream<String> stream = listToStream(rowsLeft);

//System.out.println("This is arraylist:"+Arrays.toString(stream.toArray()));
//stream.ma
//for(int l=0;l<=headersLeft.size();l++){
//	System.out.print(" "+table[0][l]);
//}
 //      ForkJoinPool fjp1 = new ForkJoinPool(39);
//fjp1.submit(()-> {finalTable = tableCreation(headersLeft,rowsLeft);}).join();
//fjp1.shutdownNow();
//}
//else 
//	for(int j=0;j<rowsLeft.size();j++)
//	{
	// System.out.println("This is the error:"+k+"--"+(rowsLeft.size()-2));

	
//	table[j][0]=rowsLeft.get(j);

	
//}
	
/*for(int l=0;l<=rowsLeft.size()/headersLeft.size();l++){
	{for(int m=0;m<headersLeft.size();m++)
	System.out.print(" "+table[l][m]);}
System.out.println();	
}*/
return rowsLeft;
}

public static List<ArrayList<String>> transformResult(Set<String> input2,List<String> in,ArrayList<String> headersLeft){
	 
	List<ArrayList<String>> rowsLeft = new ArrayList<>();



    System.out.println("This is time1.1:"+LocalTime.now());      

	ArrayList<String> temp = new ArrayList<>();
	rowsLeft.add(headersLeft);
	int size = headersLeft.size();
	//ArrayList<String> arr = new ArrayList<>();
//int j=0;
	//while(l1.hasNext()) {
//		if(QueryTask.isBound==1)
//		{
//		  try {
	//		l1.wait(500);
	//	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
	//	}	
	//Binding xz
	//	  =l1.next();
//if(QueryTask.isBound==0) {
for(String l:input2)
{//  String zy=  l1.toString();//.replace("\\", "").replace("',","").replace("'-","").replace(",","").replace("\'", "").replace("\\\"","").replace("^^xsd:int", "^^xsd:int\"").replace("^^xsd:date", "^^xsd:date\"").replace("@en", "@en\"").replace("@de", "@de\"").replace("_:", "\"_:").replace("^^xsd:float", "^^xsd:float\"").replace("^^xsd:double", "^^xsd:double\"").replace("NAN", "0");	
		//System.out.println("This is the original one:"+l1.size()+"--"+zy);
	 		int i=0;
	Pattern regex = Pattern.compile("[?=\\)][?= ][?=\\(]");
	String[] regexMatcher = regex.split(l);
	//synchronized(regexMatcher) {
	for(String ml:regexMatcher)
	{
		  String[] mlp= ml.split(" ");
	//	arr.clear();
	//	arr.addAll(Arrays.asList(mlp));
		  			 String str="";
						 
	for(int iii=3;iii<=mlp.length-1;iii++)
					//if(!arr.toString().contains("http"))
					  str+=mlp[iii].replace(")", "").replace("(", "").replace("->", "").replace("[Root]", "").replace("\"", "")+" ";
						//else
					//	str+=arr.get(iii).replace(")", "").replace("(", "").replace("->", "").replace("[Root]", "").replace("\"", "");
				//		System.out.println("This is subsubsplit:"+str);
						temp.add(str);
					
				//		if(QueryTask.isBound==1)
					//	 {
						//	for(String m:mlp)
					///		System.out.println("This is old record:"+m);
				//			for(String t:temp)
//					System.out.println("This is new record:"+str);
						//	}
			
						if(i==size-1) {
						rowsLeft.add(temp);
							temp = new ArrayList<>();
					    }	

			
i++;				 
	}
	
	
	//j++;

	}
/*}
else {
	for(String l:in)
	{//  String zy=  l1.toString();//.replace("\\", "").replace("',","").replace("'-","").replace(",","").replace("\'", "").replace("\\\"","").replace("^^xsd:int", "^^xsd:int\"").replace("^^xsd:date", "^^xsd:date\"").replace("@en", "@en\"").replace("@de", "@de\"").replace("_:", "\"_:").replace("^^xsd:float", "^^xsd:float\"").replace("^^xsd:double", "^^xsd:double\"").replace("NAN", "0");	
			//System.out.println("This is the original one:"+l1.size()+"--"+zy);
		 		int i=0;
		Pattern regex = Pattern.compile("[?=\\)][?= ][?=\\(]");
		String[] regexMatcher = regex.split(l);
		//synchronized(regexMatcher) {
		for(String ml:regexMatcher)
		{
			  String[] mlp= ml.split(" ");
		//	arr.clear();
		//	arr.addAll(Arrays.asList(mlp));
			  			 String str="";
							 
		for(int iii=3;iii<=mlp.length-1;iii++)
						//if(!arr.toString().contains("http"))
						  str+=mlp[iii].replace(")", "").replace("(", "").replace("->", "").replace("[Root]", "").replace("\"", "")+" ";
							//else
						//	str+=arr.get(iii).replace(")", "").replace("(", "").replace("->", "").replace("[Root]", "").replace("\"", "");
					//		System.out.println("This is subsubsplit:"+str);
							temp.add(str);
						
					//		if(QueryTask.isBound==1)
						//	 {
							//	for(String m:mlp)
						///		System.out.println("This is old record:"+m);
					//			for(String t:temp)
//						System.out.println("This is new record:"+str);
							//	}
				
							if(i==size-1) {
							rowsLeft.add(temp);
								temp = new ArrayList<>();
						    }	

				
	i++;				 
		}
		
		
		j++;

		}	
}*/
	 System.out.println("This is time1.2:"+LocalTime.now());      

	if(headersLeft.size()>1) {
		if(HeaderReplacement!=null ||HeaderReplacement.size()>0)
		for(int z=0;z<headersLeft.size();z++)
		{
			if(HeaderReplacement.containsKey(headersLeft.get(z).toString()))
			rowsLeft.get(0).set(z,HeaderReplacement.get(headersLeft.get(z).toString()));//).replace(rowsLeft.get(0).get(z), HeaderReplacement.get(headersLeft.get(z).toString())); 
			else
				rowsLeft.get(0).set(z,headersLeft.get(z));//).replace(rowsLeft.get(0).get(z), HeaderReplacement.get(headersLeft.get(z).toString())); 
				
				//rowsLeft.get(0).get(z).replace(rowsLeft.get(0).get(z), headersLeft.get(z)); 
			
			//	table[0][z]=headersLeft.get(z);
			
		}
		else
		for(int z=0;z<headersLeft.size();z++)
		{//table[0][z]=headersLeft.get(z);
		//rowsLeft.get(0).get(z).replace(rowsLeft.get(0).get(z), headersLeft.get(z)); 
			rowsLeft.get(0).set(z,headersLeft.get(z));//).replace(rowsLeft.get(0).get(z), HeaderReplacement.get(headersLeft.get(z).toString())); 
			
		
		}
	}
	//if(ParaEng.Union.contains("UNION"))
	if(headersLeft.size()==1)
	{
		HeaderReplacement.put(headersLeft.get(0).toString(),"?a");
		//rowsLeft.get(0).get(0).replace(rowsLeft.get(0).get(0), "?a"); 
		rowsLeft.get(0).set(0,"?a");//).replace(rowsLeft.get(0).get(z), HeaderReplacement.get(headersLeft.get(z).toString())); 
		
//		table[0][0]="?a";
		
	}
	for(int z=0;z<headersLeft.size();z++)
	{//table[z][0]=headersLeft.get(z);
	System.out.println("This is arraylist:"+z+"--"+ rowsLeft.get(0).get(z));

	}

//	for(List<String> r:rowsLeft) {
//	for(String l:r) {
//		System.out.print(" "+l);
//	}
//	System.out.println();
//}	
return rowsLeft;
}

//public static String replaceCharUsingCharArray(String str, char ch, int index) {
 //   char[] chars = str.toCharArray();
  //  chars[index] = ch;
  //  return String.valueOf(chars);
//}

public static String[][] tableCreation(List<String> headersLeft,List<String> rowsLeft){
	
	
	System.out.println("These are left var222222:"+	rowsLeft.size()+"--"+rowsLeft.parallelStream().limit(10).collect(Collectors.toSet()));
	
	System.out.println("These are left var111:"+headersLeft.size()+"--"+headersLeft);
	//for(String r:rowsLeft)
	//System.out.println("These are right var111:"+r);

	System.out.println("This is size of headers:"+headersLeft.size()+"--"+rowsLeft.size());
	String[][] table = new String[(rowsLeft.size()/headersLeft.size())+1][headersLeft.size()+1];
		System.out.println("Original");
	/*
	List<String[]> leftCsv1 = new ArrayList<String[]>();

		for (int i1 = 0; i1 < rowsLeft.size(); i1++)
		{
		    String[] temp = new String[1];
		    for (int n = 0; n < temp.length; n++)
		   {
		        temp[n] = rowsLeft.get(i1);
		    }
		    
		 leftCsv1.add(temp);
		}*/
	   
	//System.out.print("These are left var4");
	/*
	File file1 = new File("/mnt/hdd/hammad/hammad/RRR.csv");


	if(file1.delete())
	{
	    System.out.println("File deleted successfully1");
	}
	 file1 = new File("/mnt/hdd/hammad/hammad/RRR.csv");

	//System.out.print("These are left var4");
	   try (CSVWriter writer = new CSVWriter(new FileWriter("/mnt/hdd/hammad/hammad/RRR.csv",true))) {
	       writer.writeAll(leftCsv1);
		// TODO Auto-generated catch block
		e4.printStackTrace();
	}
	*/
	/*
	if(!ParaEng.Union.contains("UNION"))
	if(QueryTask.IsSingleStatement!=null ) {
		for(int j=0;j<rowsLeft.size();j++) {
	  if(QueryTask.IsSingleStatement.containsKey(rowsLeft.get(j)))		
		 if(QueryTask.IsSingleStatement.containsValue(rowsLeft.get(j-2)))
			 possibleOuter=5;
		}
	}
	*/
	System.out.println("This is time3:"+LocalTime.now()+"--"+rowsLeft.size());      
	int i=0;
int	hls=headersLeft.size();
int rss=rowsLeft.size();
FileWriter writer = null ;
try {
	 writer = new FileWriter("/mnt/hdd/hammad/hammad/rows.csv");
	
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
String objectsCommaSeparated = rowsLeft.stream().parallel().collect(Collectors.joining(","));

try {
	writer.write(objectsCommaSeparated);
	writer.close();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

	//if(rowsLeft.size()>1) {
		for(int j=1,k=0;j<rss;)
	{
	//if(k>1367590)
		if(k>=rss) 
			break;
//			if(!rowsLeft.get(k).equals("") || rowsLeft.get(k)!=null)
			//System.out.print(" "+j+"--"+i+"--"+k+"--"+rowsLeft.toArray()[k]);

			table[j][i]=(String) rowsLeft.toArray()[k];
	k++;

			
	if(i==hls-1)
	{ //column=0;
	i=0;
	j++;
	//System.out.println();
	}
	else
	i++;
//	} else break;
	}
	//}
/*	else {
		if(!ParaEng.Union.contains("UNION")) {
	    table[0][0]=headersLeft.get(0);
		table[1][0]=(String) rowsLeft.toArray()[0];
		possibleOuter=5;
		System.out.println("This is possibleOuter:"+possibleOuter);
		}
*/	//}
	System.out.println("This is time4:"+LocalTime.now());      
return table;
}
public static void finalResultCalculation(LinkedHashMap<List<EdgeOperator>, List<Binding>> joinedTriples, HashSet<List<EdgeOperator>> evaluatedTriples, LinkedHashMap<List<EdgeOperator>, List<Binding>> joinedTriples2){
	
	int k=0;
	Treelowestlevel.clear();
	//logger.info("THis is currently the entering part B6 size:"+"--"+ProcessedSets2.size());

	for(int i=0;i<joinedTriples.size()-1;i++)
	{TreelowestlevelProcessed.add(i);
		for(int j=i+1;j<joinedTriples.size();j++)
		{
		Treelowestlevel.add(new ArrayList<>());
			Treelowestlevel.get(k).add(i);//[[1,2]];
		Treelowestlevel.get(k).add(j);//[[1,2]];


		//xyz.get(k).add(j);//[[1,2]];

		k++;
		}
	}
//	Set<Binding> x = new HashSet<>();
	
	 ForkJoinPool fjp = new ForkJoinPool();

	fjp.submit(()->
		{		 
			ResultsSize14(joinedTriples,evaluatedTriples,joinedTriples2);
		}
	).join();
	
		//fjp.shutdownNow();
	////logger.info("13131313131313131313This this this this this this this this this this:"+LocalTime.now());

	return;//ProcessedSets.get(ProcessedSets.size()-1);	
	//}
}

public static void ResultsSize14(LinkedHashMap<List<EdgeOperator>, List<Binding>> joinedTriples, HashSet<List<EdgeOperator>> evaluatedTriples, LinkedHashMap<List<EdgeOperator>, List<Binding>> notJoinedTriples2)
{


	if(evaluatedTriples.size()==0)
	{
	for(ArrayList<Integer> e:Treelowestlevel)
	{
		//System.out.println("This is the current size of evaluated triples:"+HashJoin.EvaluatedTriples.size());
		if(HashJoin.EvaluatedTriples.size()>0)
			break;
//		
		LinkedHashMap<List<EdgeOperator>, List<Binding>> firstSet = new LinkedHashMap<>();
		LinkedHashMap<List<EdgeOperator>, List<Binding>> secondSet = new LinkedHashMap<>();

		Object firstKey = joinedTriples.keySet().toArray()[e.get(0)];

Object secondKey = joinedTriples.keySet().toArray()[e.get(1)];

for(Entry<List<EdgeOperator>, List<Binding>> ps2:joinedTriples.entrySet()) {
	//System.out.println("This is a firstKey:"+firstKey+"--"+ps2.getKey());

	if(ps2.getKey().toString().equals(firstKey.toString())) {
				firstSet.put(ps2.getKey(),ps2.getValue());
	}
}
for(Entry<List<EdgeOperator>, List<Binding>> ps2:joinedTriples.entrySet()) {
	//System.out.println("This is a secondKey:"+secondKey+"--"+ps2.getKey());
	if(ps2.getKey().toString().contains(secondKey.toString())) {
		
		secondSet.put(ps2.getKey(),ps2.getValue());
	}
}
if(!firstSet.equals(secondSet))
	firstSet.putAll(secondSet);
HashJoin.ProcessingTask(notJoinedTriples2,firstSet,0);
	}
	}
	else {
		for(Integer e:TreelowestlevelProcessed) {
			LinkedHashMap<List<EdgeOperator>, List<Binding>> firstSet = new LinkedHashMap<>();
			int equity=0;
			for( Entry<List<EdgeOperator>, List<Binding>> ps2:joinedTriples.entrySet())
			{firstSet.clear();
				firstSet.put(ps2.getKey(),ps2.getValue());
			//	System.out.println("This is the numbering:"+e+"--"+equity);
				if(equity==e)
					break;
			equity++;
			}
			int s=0;
			for(List<EdgeOperator> et:evaluatedTriples)
			if(firstSet.containsKey(et))
				{s=0 ;break;}
			if(s==1) {continue;}
			
	//		for(List<EdgeOperator> et:evaluatedTriples)
	//	System.out.println("Second problem:"+et);
			
		//	System.out.println("First problem:"+firstSet.keySet());
		//	System.out.println("Second problem:"+joinedTriples.keySet());
				
			
			
				HashJoin.ProcessingTask(notJoinedTriples2,firstSet,0);
				
		}
			
		
		}
	

}

//	if(m==1) {
	//		for(List<EdgeOperator> bl:bLM.keySet())
	//	HashJoin.JoinedTriples.remove(bLM.keySet());
	//		for(List<EdgeOperator> bl:aLM.keySet())
	//		HashJoin.JoinedTriples.remove(bl);
	//	}
/*				if(i==0)
					break;

				LinkedHashMap<List<EdgeOperator>, Set<Binding>> firstSet = new LinkedHashMap<>();
				LinkedHashMap<List<EdgeOperator>, Set<Binding>> secondSet = new LinkedHashMap<>();


			
		//if(firstKey.toString().contains("id"))
//			System.out.println("This is firstSet:"+firstSet);
		//if(secondKey.toString().contains("id"))
//			System.out.println("This is secondSet:"+secondSet);

		for(Entry<List<EdgeOperator>, Set<Binding>> ps2:ProcessedSets2.entrySet()) {
			//System.out.println("This is a firstKey:"+firstKey+"--"+ps2.getKey());

			if(ps2.getKey().toString().equals(firstKey.toString())) {
						firstSet.put(ps2.getKey(),ps2.getValue());
			}
		}
		for(Entry<List<EdgeOperator>, Set<Binding>> ps2:ProcessedSets2.entrySet()) {
			//System.out.println("This is a secondKey:"+secondKey+"--"+ps2.getKey());
			if(ps2.getKey().toString().contains(secondKey.toString())) {
				
				secondSet.put(ps2.getKey(),ps2.getValue());
			}
		}
		HashSet<String> aa = new HashSet<>();
		if(!firstSet.equals(secondSet))
			{firstSet.putAll(secondSet);
		}
		for(Entry<List<EdgeOperator>, Set<Binding>> f:firstSet.entrySet()) {
			for(EdgeOperator f1:f.getKey())
			{		aa.add(f1.getEdge().getTriple().getSubject().toString());
			aa.add(f1.getEdge().getTriple().getObject().toString());

			}
			}
		int l=0;
		for(Entry<List<EdgeOperator>, Set<Binding>> jt:joinedTriples.entrySet())
			for(String aaa:aa)
			if(jt.getKey().toString().contains(aaa))
				l++;
		System.out.println("THis is comparison for size:"+l+"--"+aa.size());
		if(l==aa.size())
			break;

*/
	//	HashJoin.ProcessingTask(joinedTriples,firstSet);	
	

/*public static void RemainingTriples(List<Set<Binding>> ProcessedSets2) {
	int bnc_ =0;
	int k=0;
	//Add remaining vertics and calucations from previous iterations		
	//	//logger.info("This is happening yeay2, Now being static:"+ProcessedSets.size());
	Set<Integer> remainings = new HashSet<>();
	ArrayList<Set<Binding>> ProcessedSetsRemaining = new ArrayList<>();
	for(ArrayList<Integer> e:Treelowestlevel)
			for(Integer e3:e)
				remainings.add(e3);

	for(Integer e4:remainings) 
	ProcessedSetsRemaining.addAll(UnProcessedSets);
	ProcessedSetsRemaining.addAll(ProcessedSets);
	
//	//logger.info("These are all the triples:"+ProcessedSets.size()+"--"+UnProcessedSets.size());
	
	
//	for(Set<Binding> e1:ProcessedSetsRemaining)
//		//logger.info("These are all the triples:"+e1.parallelStream().limit(1).collect(Collectors.toSet()));
	
//	for(Set<Binding> e:UnProcessedSets)
//		//logger.info("These are remaining ones:"+e.size()+"--"+e.parallelStream().limit(1).collect(Collectors.toSet()));
	


	k=0;
	Treelowestlevel.clear();
	//ProcessedSets.clear();

	for(int i=0;i<UnProcessedSets.size()-1;i++)
		for(int j=i+1;j<UnProcessedSets.size();j++)
		{
			Treelowestlevel.add(new ArrayList<>());
			Treelowestlevel.get(k).add(i);//[[1,2]];
		Treelowestlevel.get(k).add(j);//[[1,2]];

		//xyz.get(k).add(j);//[[1,2]];

		k++;
		}
	while(Treelowestlevel.size()>0) {
		bnc_ = ProcessedSets.size();
		Set<Binding> x = ResultsSize14(UnProcessedSets);
		
		for(Set<Binding> p:ProcessedSets)
		//logger.info("This is processedSet1:"+p.parallelStream().limit(1).collect(Collectors.toSet()));
			
			for(Set<Binding> up:UnProcessedSets)
			//logger.info("This is UnprocessedSet1:"+up.parallelStream().limit(1).collect(Collectors.toSet()));
			
	//	RemainingTriples(ProcessedSetsRemaining,k,bnc_);
		
		////logger.info("This is happening yeay98, Now being static0:"+UnProcessedSets.size()+"--"+ProcessedSets.get(0));

		if(bnc_==ProcessedSets.size())
		{	RemainingTriples(UnProcessedSets);
		//	//logger.info("This is happening yeay99, Now being static:"+UnProcessedSets+"--"+ProcessedSets.get(0));
		break;
		}
	}

}

	

*/	

public static void BindJoinCorrection(List<List<EdgeOperator>> joinGroupsListLeftTemp) {
	{
		HashMap<Vertex,Integer> bvb = new HashMap<>();
		
		for(List<EdgeOperator>  jgl1:JoinGroupsListExclusive)
			for(EdgeOperator jgl2:jgl1)
				if(jgl2.getStartVertex()!=null) {
			bvb.put( jgl2.getStartVertex(),0);
				}
		Integer kl=0,sum=0;
		
		for(List<EdgeOperator>  jgl1:joinGroupsListLeftTemp)
			for(EdgeOperator jgl2:jgl1)
				if(jgl2.getStartVertex()!=null) {
				
					for(List<EdgeOperator>  jgl1s:joinGroupsListLeftTemp)
					if(!jgl1.toString().equals(jgl1s.toString())) {
				if(jgl1s.toString().contains(jgl2.getStartVertex().toString())) {	
					for(Entry<Vertex,Integer> bvb1:bvb.entrySet())
						if(bvb1.getKey().toString().equals(jgl2.getStartVertex().toString()))
					{
							bvb.replace(bvb1.getKey(), bvb1.getValue(), kl);
							sum=sum+kl;
					}
					}
				kl++;
					}
				}

		//logger.info("This is the new sum now:"+sum);
		
		for(Map.Entry<Vertex,Integer> bvb11:bvb.entrySet()) {
			//logger.info("This is nwe waysssss:"+bvb11);
		}
		
			if(sum>0) {

		for(int i=0;i<joinGroupsListLeftTemp.size();i++)
			for(int j=0;j<joinGroupsListLeftTemp.get(i).size();j++)
			for(Map.Entry<Vertex,Integer> bvb11:bvb.entrySet()) {
		if(joinGroupsListLeftTemp.get(i).get(j).getStartVertex()!=null)		
		{
			if(joinGroupsListLeftTemp.get(i).get(j).getStartVertex().toString().contains(bvb11.getKey().toString())&& bvb11.getValue()==0 ) {
				//logger.info("Make this vertex opposite:"+JGroupsList.get(i).get(j).getStartVertex()+"--"+bvb11.getValue());
				if(joinGroupsListLeftTemp.get(i).get(j).getEdge().getV1().toString().contains(bvb11.getKey().toString()))
				joinGroupsListLeftTemp.get(i).set(j, new BindJoin(joinGroupsListLeftTemp.get(i).get(j).getEdge().getV2(),joinGroupsListLeftTemp.get(i).get(j).getEdge()));
				else
					joinGroupsListLeftTemp.get(i).set(j, new BindJoin(joinGroupsListLeftTemp.get(i).get(j).getEdge().getV1(),joinGroupsListLeftTemp.get(i).get(j).getEdge()));
				
			}
			}
			}
		
//		for(List<EdgeOperator>  jgl1:JGroupsList)
			//logger.info("This is the JoinGroupListAfter:"+jgl1);
		
		//for(Map.Entry<Vertex,Integer> bvb11:bvb.entrySet()) 
		//logger.info("This is nwe waysssss:"+bvb11);


		for(List<EdgeOperator>  jgl1:JoinGroupsListExclusive)
			for(EdgeOperator jgl2:jgl1)
				if(jgl2.getStartVertex()!=null) {
			bvb.put( jgl2.getStartVertex(),0);
				}
		kl=0;
		
		for(List<EdgeOperator>  jgl1:joinGroupsListLeftTemp)
			for(EdgeOperator jgl2:jgl1)
				if(jgl2.getStartVertex()!=null) {
				
					for(List<EdgeOperator>  jgl1s:joinGroupsListLeftTemp)
					if(!jgl1.toString().equals(jgl1s.toString())) {
				if(jgl1s.toString().contains(jgl2.getStartVertex().toString())) {	
					for(Entry<Vertex,Integer> bvb11:bvb.entrySet())
						if(bvb11.getKey().toString().equals(jgl2.getStartVertex().toString()))
					{
							bvb.replace(bvb11.getKey(), bvb11.getValue(), kl);		
					}
					}
				kl++;
					}
				}

		//for(Map.Entry<Vertex,Integer> bvb11:bvb.entrySet()) 
			//logger.info("This is nwe waysssss:"+bvb11);
		
				for(int i=0;i<joinGroupsListLeftTemp.size();i++)
			for(int j=0;j<joinGroupsListLeftTemp.get(i).size();j++)
			for(Map.Entry<Vertex,Integer> bvb11:bvb.entrySet()) {
		if(joinGroupsListLeftTemp.get(i).get(j).getStartVertex()!=null)		
		{
			if(joinGroupsListLeftTemp.get(i).get(j).getStartVertex().toString().contains(bvb11.getKey().toString())&& bvb11.getValue()==0 ) {
				//logger.info("Make this vertex opposite:"+JGroupsList.get(i).get(j).getStartVertex()+"--"+bvb11.getValue());
				joinGroupsListLeftTemp.get(i).set(j, new HashJoin(joinGroupsListLeftTemp.get(i).get(j).getEdge()));
				
			}
			}
			}
		
		//for(List<EdgeOperator>  jgl1:JGroupsList)
			//logger.info("This is the JoinGroupListAfter:"+jgl1);
		}

		int rep=0;
		
					for(List<EdgeOperator> jg2:JoinGroupsListExclusive) {
						for(EdgeOperator jg22:jg2)
							if(jg22.getStartVertex()!=null )
						{	for(EdgeOperator obt1:operators_BushyTreeOrder)
							if(!obt1.getEdge().equals(jg22.getEdge()))
						{
						//	//logger.info("This is si sis is is:"+jg22.getStartVertex()+"--"+obt1.getEdge().getV1() );
						if(jg22.getStartVertex().equals(obt1.getEdge().getV1() )
							|| jg22.getStartVertex().equals(obt1.getEdge().getV2()))
							{rep++;
							}
					}
						if(rep==0)
						{
							if(jg22.getStartVertex().getNode().equals(jg22.getEdge().getTriple().getSubject()))
							jg2.add(new BindJoin(jg22.getEdge().getV2(),jg22.getEdge()));
							else
								jg2.add(new BindJoin(jg22.getEdge().getV1(),jg22.getEdge()));
							jg2.remove(jg22);
					
							}
						}
						
				}


						}

		
	}
	

public static void BindJoinCorrectionLeft(List<EdgeOperator> JGroupsList,int type) {

	
	
	String FilterString=null;
	int countQmark=0;
	String extension;
	String[] Extension = null;
		for(EdgeOperator jgll:JGroupsList)
		{if(ParaEng.opq==" ")
			FilterString=ParaEng.Filter.toString();
		else if(ParaEng.opq.toString().contains("FILTER"))
			FilterString = ParaEng.opq.toString();
		else FilterString = " ";
		
if(FilterString!=" ") {
	 extension = FilterString.toString().substring(FilterString.toString().indexOf("FILTER"), FilterString.toString().indexOf(")")+1);

	 Extension = extension.toString().split(" ");
		
}
		String querySubject = jgll.getEdge().getTriple().getSubject().toString();
		String queryObject = jgll.getEdge().getTriple().getObject().toString();

		
		if(FilterString!=" ") 	
		if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) {
				if(jgll.toString().contains("Bind"))
				for(String ex:Extension )
			{
				if(queryObject.equals(ex.replaceAll("[()]","")) ||querySubject.equals(ex.replaceAll("[()]","")) ) {
			//logger.info("This is the subject and ex11:"+queryObject+"--"+querySubject+"--"+ex.replaceAll("[()]",""));
			for (Entry<EdgeOperator, Integer> jgr:joinGroupsLeftOptional.entries())
			if(jgr.getValue().equals(jgll)) {
			if(type==1) {
				joinGroupsLeftOptional.remove(jgr.getKey(),jgr.getValue());
				JoinGroupsListLeftOptional.remove(jgr.getValue());
				int a=jgr.getValue();
				EdgeOperator b= jgr.getKey();
				joinGroupsLeftOptional.put(new HashJoin(b.getEdge()), a);
				JoinGroupsListLeftOptional.add(new HashJoin(b.getEdge()));
			break;
			}
			}		
			}	
			}

	
			}
		}
int rep=0;		
		
		for(EdgeOperator jg22:JoinGroupsListLeft) {
		//	for(EdgeOperator jg22:jg2)
				if(jg22.getStartVertex()!=null )
			{	for(EdgeOperator obt1:operators_BushyTreeOrder)
				if(!obt1.getEdge().equals(jg22.getEdge()))
			{
		//		//logger.info("This is si sis is is:"+jg22.getStartVertex()+"--"+obt1.getEdge().getV1() );
			if(jg22.getStartVertex().equals(obt1.getEdge().getV1() )
				|| jg22.getStartVertex().equals(obt1.getEdge().getV2()))
				{rep++;
				}
		}
			if(type==0) {
				
		/*	if(rep==0)
			{
				//if(jg22.getStartVertex().getNode().equals(jg22.getEdge().getTriple().getSubject()))
	//				JoinGroupsListLeft.add(new BindJoin(jg22.getEdge().getV2(),jg22.getEdge()));
			//	else
		//			JoinGroupsListLeft.add(new BindJoin(jg22.getEdge().getV1(),jg22.getEdge()));
				JoinGroupsListLeft.remove(jg22);
		
				}
	*/		rep=0;
			}
			}
	}


		
}
/*
public static void BindJoinCorrectionRight(List<EdgeOperator> JGroupsList,int type) {

	
	
	
	String FilterString=null;
	int countQmark=0;
	String extension;
	String[] Extension = null;

	for(EdgeOperator jgll:JGroupsList)
		{if(ParaEng.opq==" ")
			FilterString=ParaEng.Filter.toString();
		else if(ParaEng.opq.toString().contains("FILTER"))
			FilterString = ParaEng.opq.toString();
		else FilterString = " ";
		

		if(FilterString!=" ") {
			 extension = FilterString.toString().substring(FilterString.toString().indexOf("FILTER"), FilterString.toString().indexOf(")")+1);

			 Extension = extension.toString().split(" ");
				
		}
		
		String querySubject = jgll.getEdge().getTriple().getSubject().toString();
		String queryObject = jgll.getEdge().getTriple().getObject().toString();

		
		if(FilterString!=" ") 		
		if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) {
			
		if(jgll.toString().contains("Bind"))
				for(String ex:Extension )
			{
				if(queryObject.equals(ex.replaceAll("[()]","")) ||querySubject.equals(ex.replaceAll("[()]","")) ) {
			//logger.info("This is the subject and ex11:"+queryObject+"--"+querySubject+"--"+ex.replaceAll("[()]",""));
			for (Entry<EdgeOperator, Integer> jgr:joinGroupsRightOptional.entries())
			if(jgr.getValue().equals(jgll)) {
				if(type==1) {
					
				joinGroupsRightOptional.remove(jgr.getKey(),jgr.getValue());
				JoinGroupsListRightOptional.remove(jgr.getValue());
				int a=jgr.getValue();
				EdgeOperator b= jgr.getKey();
				joinGroupsRightOptional.put(new HashJoin(b.getEdge()), a);
				JoinGroupsListRightOptional.add(new HashJoin(b.getEdge()));
			break;
			}
			}		
				}}		
				
		}
		}
		
	

	int rep=0;		
	
	for(EdgeOperator jg22:JoinGroupsListRight) {
	//	for(EdgeOperator jg22:jg2)
			if(jg22.getStartVertex()!=null )
		{	for(EdgeOperator obt1:operators_BushyTreeOrder)
			if(!obt1.getEdge().equals(jg22.getEdge()))
		{
			//logger.info("This is si sis is is:"+jg22.getStartVertex()+"--"+obt1.getEdge().getV1() );
		if(jg22.getStartVertex().equals(obt1.getEdge().getV1() )
			|| jg22.getStartVertex().equals(obt1.getEdge().getV2()))
			{rep++;
			}
	}
		if(rep==0)
		{	if(type==0) {
			
			if(jg22.getStartVertex().getNode().equals(jg22.getEdge().getTriple().getSubject()))
				JoinGroupsListRight.add(new BindJoin(jg22.getEdge().getV2(),jg22.getEdge()));
			else
				JoinGroupsListRight.add(new BindJoin(jg22.getEdge().getV1(),jg22.getEdge()));
			JoinGroupsListRight.remove(jg22);
	
			}
		rep=0;
		}
		}
}

	
}
*/
//JoinGroupsListOptional
//joinGroupsOptional1
public static void CompleteOptional(ConcurrentHashMap<EdgeOperator, HashSet<Integer>> newFormationM,List<EdgeOperator> operators,List<EdgeOperator> operators_BushyTreeOrder ) {
int ll=0;
ConcurrentHashMap<EdgeOperator, HashSet<Integer>>  newFormation = new ConcurrentHashMap<>();
JoinGroupsListOptional.clear();
List<EdgeOperator> operators_BushyTreeLeftOptional = new ArrayList<>();
List<EdgeOperator> operators_BushyTreeRightOptional = new ArrayList<>();
newFormationM.clear();

for(Entry<List<EdgeOperator>, Integer> e4: joinGroupsOptional1.entrySet()) {
	for(EdgeOperator e6:e4.getKey()) {
	
		if(newFormation.size()==0 || !newFormation.containsKey(e6)) {
			HashSet<Integer> bb = new HashSet<>();
			bb.add(e4.getValue());
			newFormation.put(e6,bb);
	
		}
		else {
			for(Entry<EdgeOperator, HashSet<Integer>> nf:newFormation.entrySet())
			{
				if(nf.getKey().equals(e6))
			{
				HashSet<Integer> bb = new HashSet<>();
			bb.addAll(nf.getValue());
			bb.add(e4.getValue());
		if(bb.size()>1)
			newFormation.put(e6,bb);
			}
				
			}
		}
	//	//logger.info("This is the new new new:"+e4.getKey()+"--"+e6);
		}
	
	JoinGroupsListOptional.add(e4.getKey());	
}

//for(Entry<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator, HashSet<Integer>> nf:newFormation.entrySet())
////logger.info("This is the new new new11:"+newFormation);
Set<EdgeOperator> URIbased = new HashSet<>();
Set<EdgeOperator> uris = new HashSet<>();

for(List<EdgeOperator> nf1:JoinGroupsListOptional)
for(EdgeOperator nf:nf1)
	if(nf.getEdge().getV1().getNode().isURI() || nf.getEdge().getV2().getNode().isURI())
{URIbased.add(nf);

}
for(EdgeOperator uri:URIbased)
	for(EdgeOperator uri1:URIbased) {

	if((uri.getEdge().getV1().equals(uri1.getEdge().getV2()) || uri.getEdge().getV2().equals(uri1.getEdge().getV1()))
			&&( uri.getEdge().getV1().getNode().isURI() || uri.getEdge().getV2().getNode().isURI())
			&& ( uri1.getEdge().getV1().getNode().isURI() || uri1.getEdge().getV2().getNode().isURI()))
		{uris.add(uri);uris.add(uri1);}
	}

for(Entry<EdgeOperator, HashSet<Integer>> nf:newFormation.entrySet())
{
	if(nf.getValue().size()>1)
	{		newFormationM.put(nf.getKey(),nf.getValue());
	}
	}
		

//System.out.println("This is Old joinGroup2:"+newFormationM);
//System.out.println("This is Old joinGroup333:"+JoinGroupsListExclusive);

//LinkedHashMap<Integer, List<EdgeOperator>> uuu;
//HashSet<EdgeOperator> ExistingEdges = new HashSet<>(); 

operators_BushyTreeLeftOptional.clear();

		operators_BushyTreeRightOptional.clear();
		
		/*		int isDoubled=0;
		int isTripled=0;
		int CompletlyNewFormation=0;
		List<EdgeOperator> leSafe= new ArrayList<>();
		List<EdgeOperator> leSafeNonConcrete= new ArrayList<>();
		
		Iterator<List<EdgeOperator>> jgle = JoinGroupsListExclusive.iterator()	;
		while(jgle.hasNext()) {
			List<com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator> jglen = jgle.next();
			//CompletlyNewFormation=0;
			for(Entry<EdgeOperator, HashSet<Integer>> nf:newFormationM.entrySet())
			{
				//Iterator<List<EdgeOperator>> le = JoinGroupsListExclusive.iterator()	;
				
			//	while(le.hasNext())
			//	{
			//		List<EdgeOperator> le2 = le.next();
			Iterator<EdgeOperator> le3 = jglen.iterator();
			EdgeOperator le1;
			while(le3.hasNext()) {
			le1=	le3.next();
			if(le1.getEdge().getTriple().getSubject().isURI() ||le1.getEdge().getTriple().getSubject().isLiteral())
			{ isTripled++;
		//	if(!ConcreteEdge.contains(le1.getEdge().getTriple().getObject()))
				
			//ConcreteEdge.add(le1.getEdge().getTriple().getObject());
				continue;
			}
			
			if( le1.getEdge().getTriple().getObject().isURI() ||le1.getEdge().getTriple().getObject().isLiteral())
			{ isTripled++;
			//if(!ConcreteEdge.contains(le1.getEdge().getTriple().getSubject()))
			//C/oncreteEdge.add(le1.getEdge().getTriple().getSubject());
				continue;
			}
			
			
		/*	if(!nf.getKey().getEdge().getTriple().getObject().isConcrete() && !nf.getKey().getEdge().getTriple().getSubject().isConcrete())
			{	for(Node ce: ConcreteEdge) {
				if(nf.getKey().getEdge().getTriple().getObject().equals(ce) )
					CompletlyNewFormation=1;
				//	//logger.info("");
			if( nf.getKey().getEdge().getTriple().getSubject().equals(ce))
					CompletlyNewFormation=1;	
					////logger.info("");
				}
			}	
			
			
				if(CompletlyNewFormation==0)
						{		isDoubled++;
						
						//	//log.info("This is total size of triple group:"+"--"+jglen.size());
							if(!leSafeNonConcrete.contains(nf.getKey()))
							leSafeNonConcrete.add(nf.getKey());
						}
				CompletlyNewFormation=1;
			
							if((nf.getKey().getEdge().getTriple().getSubject().toString().equals(le1.getEdge().getTriple().getObject().toString())
							||	nf.getKey().getEdge().getTriple().getObject().toString().equals(le1.getEdge().getTriple().getSubject().toString()))	
									&& !nf.getKey().getEdge().toString().equals(le1.getEdge().toString())) 
							{
			if ((le1.getEdge().getTriple().getSubject().isConcrete() && !le1.getEdge().getTriple().getObject().isConcrete()))
			//isDoubled++;
				isTripled++;
				if ((le1.getEdge().getTriple().getObject().isConcrete() && !le1.getEdge().getTriple().getSubject().isConcrete()))
				//isDoubled++;
					isTripled++;		
				if ((nf.getKey().getEdge().getTriple().getSubject().isConcrete() && !nf.getKey().getEdge().getTriple().getObject().isConcrete()))
					//isDoubled++;
						isTripled++;
						if ((nf.getKey().getEdge().getTriple().getObject().isConcrete() && !nf.getKey().getEdge().getTriple().getSubject().isConcrete()))
						//isDoubled++;
							isTripled++;		
							}
							leSafe.add(le1);
							
							}
			if(isTripled>1)
			{
				//isTripled=0;
				continue;
			}

				
				
				if(isTripled==0 ) {
					for(Entry<EdgeOperator, HashSet<Integer>> nf1:newFormationM.entrySet())
					{
					
					if(jglen.contains(nf1.getKey()))
			jglen.remove(nf1.getKey());
					if(!operators_BushyTreeLeft.contains(nf1.getKey()) && ((!nf1.getKey().getEdge().getTriple().getSubject().isURI() ||!nf1.getKey().getEdge().getTriple().getSubject().isLiteral()) || (!nf1.getKey().getEdge().getTriple().getObject().isURI() || !nf1.getKey().getEdge().getTriple().getObject().isLiteral())) )
			{operators_BushyTreeLeft.add(nf1.getKey());
			operators_BushyTreeRight.add(nf1.getKey());
			}
			
			if(jglen.size()==1) {
			if(!operators_BushyTreeLeft.toString().contains(jglen.toString()))
			{	operators_BushyTreeLeft.addAll(jglen);
			operators_BushyTreeRight.addAll(jglen);

			}
			
		//	jgle.remove();
			}
				}
			
				


			
				}
		
		
//		isDoubled=0;
		
				}
			}
//		}
		

		
		if(isDoubled>0) {
			//for(Entry<EdgeOperator, HashSet<Integer>> nf1:newFormationM.entrySet())
			//{
			for(EdgeOperator le11:leSafeNonConcrete)
			{
			EdgeOperator a11=	le11;
				for(int i=0;i<JoinGroupsListExclusive.size();i++)
			if(!JoinGroupsListExclusive.get(i).toString().contains("http")) {
					JoinGroupsListExclusive.get(i).remove(a11);
						if(!operators_BushyTreeLeft.contains(le11) )
						{
						operators_BushyTreeLeft.add(le11);
						operators_BushyTreeRight.add(le11);
						}
			}
				//	}	
								
			//		}
		//	}
		//}
			}
		
		
		}

	*/	
	
/*	for(int i=0 ;i<JoinGroupsListExclusive.size();i++)
		for(int j=0;j<JoinGroupsListExclusive.get(i).size();j++)
			for(Node ce:ConcreteEdge)
		if(!JoinGroupsListExclusive.get(i).get(j).getEdge().getTriple().getObject().equals(ce) && !JoinGroupsListExclusive.get(i).get(j).getEdge().getTriple().getSubject().equals(ce))		
			{
			operators_BushyTreeLeft.add(JoinGroupsListExclusive.get(i).get(j));
			operators_BushyTreeRight.add(JoinGroupsListExclusive.get(i).get(j));
			JoinGroupsListExclusive.get(i).remove(j);
			}
*/
List<EdgeOperator> inclusion = new ArrayList<>();		
Iterator<List<EdgeOperator>> jgIterator = JoinGroupsListOptional.iterator();//joinGroups2.keySet().iterator();
int ij1=0;
while(jgIterator.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator.next();
	for(Entry<EdgeOperator, HashSet<Integer>>  nfm:newFormationM.entrySet())
{
	
		if(aa.contains(nfm.getKey())) {
	inclusion.add(nfm.getKey());
//	jgIterator.remove();;
	if(!uris.contains(nfm.getKey())) {
	operators_BushyTreeLeftOptional.add(nfm.getKey());
	operators_BushyTreeRightOptional.add(nfm.getKey());
	}
	System.out.println("this is problem11111:"+aa);

//}
//}
//}
                  }
	}
	}

jgIterator = JoinGroupsListOptional.iterator();//joinGroups2.keySet().iterator();

while(jgIterator.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator.next();
	for(EdgeOperator e:inclusion)
 aa.remove(e);
}
List<EdgeOperator> namesList1 = uris.parallelStream().collect(Collectors.toList());

JoinGroupsListOptional.add(namesList1);	
//JoinGroupsListExclusive.remove(inclusion);
/*
Iterator<List<EdgeOperator>> jgIterator1 = JoinGroupsListExclusive.iterator();
int ij=0;

while(jgIterator1.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator1.next();
if(aa.size()==1  ) {
for(EdgeOperator aaa:aa) {
//	//log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
	for(List<EdgeOperator> jge:JoinGroupsListExclusive)
		if((aaa.getEdge().getTriple().getObject().isConcrete()  || aaa.getEdge().getTriple().getSubject().isConcrete())
				&& jge.size()>1)
			for(EdgeOperator jgle:jge) {
				if(jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getSubject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getSubject()))
				{	jge.add(aaa);
				aa.remove(aaa);
				}
			ij=1;	
			break;
			}
	if(ij==1)
		break;

	if(ij==1) {ij=0; break;}

}

}
}

Iterator<List<EdgeOperator>> jgIterator11 = joinGroups2.values().iterator();
int ij11=0;

while(jgIterator11.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator11.next();
if(aa.size()==1  ) {
for(EdgeOperator aaa:aa) {
//	//log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
	for(Entry<Integer, List<EdgeOperator>> jgel:joinGroups2.entrySet())
		if((aaa.getEdge().getTriple().getObject().isConcrete()  || aaa.getEdge().getTriple().getSubject().isConcrete())
				&& jgel.getValue().size()>1)
			for(EdgeOperator jgle:jgel.getValue()) {
				if(jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getSubject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getSubject().equals(aaa.getEdge().getTriple().getObject())
						|| jgle.getEdge().getTriple().getObject().equals(aaa.getEdge().getTriple().getSubject()))
				{	jgel.getValue().add(aaa);
				aa.remove(aaa);
				}
			ij11=1;	
			break;
			}
	if(ij11==1)
		break;

	if(ij11==1) {ij11=0; break;}

}

}
}

*//*
Iterator<List<EdgeOperator>> jgIterator111 = JoinGroupsListExclusive.iterator();
int ij111=0;

while(jgIterator111.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator111.next();
if(aa.size()<=newFormationM.size() && aa.size()>1  ) {
for(EdgeOperator aaa:aa) {
//	//log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
	for(Entry<EdgeOperator, HashSet<Integer>> nfm:newFormationM.entrySet())
		if(aaa.equals(nfm.getKey()) && !aaa.getEdge().getTriple().getSubject().isConcrete()&& !aaa.getEdge().getTriple().getObject().isConcrete())
			ij111++;
if(ij111==aa.size())	
	if(aa.size()>1 || aa.isEmpty()==false)
{

	//	System.out.println("1this is problem:"+aa);
	//if(!operators_BushyTreeRight.equals(aa)) {
	
//	operators_BushyTreeRight.addAll(aa);
//	operators_BushyTreeLeft.addAll(aa);
//	}
//	jgIterator111.remove();
	//System.out.println("1this is problem11:"+aa);

}
}
}
}
*/

/*
Iterator<List<EdgeOperator>> obtIterator99;
Iterator<List<EdgeOperator>> jg2Iterator99;
List<EdgeOperator> obt99;
boolean isString=false;
obtIterator99 = JoinGroupsListExclusive.iterator();
while(obtIterator99.hasNext()) {
   obt99 = obtIterator99.next();
   for(int i=0; i<obt99.size();i++) {
	   if(obt99.get(i).getEdge().getTriple().getObject().isConcrete() || obt99.get(i).getEdge().getTriple().getSubject().isConcrete())
		   {isString=true;
		   break;
		   }
   }
   if(isString==true) {
	   isString=false;
	 jg2Iterator99 = JoinGroupsListExclusive.iterator();
List<EdgeOperator> 	jg99;
while(jg2Iterator99.hasNext())	{	
		jg99=jg2Iterator99.next();
		if(!obt99.equals(jg99))
		
		for(EdgeOperator jg991:jg99)
		{	if(obt99.contains(jg991))
				jg99.remove(jg991);
		}
	}
   }

	}
*/


Iterator<List<EdgeOperator>> jgIterator3 = joinGroupsOptional1.keySet().iterator();

while(jgIterator3.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator3.next();
if(aa.size()==1)
{

	if(!operators_BushyTreeRightOptional.equals(aa) ) {
			
		
	operators_BushyTreeRightOptional.addAll(aa);
	operators_BushyTreeLeftOptional.addAll(aa);
	}
	jgIterator3.remove();
}
}
for(EdgeOperator uri:uris) {
	operators_BushyTreeRightOptional.remove(uri);
	operators_BushyTreeLeftOptional.remove(uri);

}
////logger.info("This is the task of eternity:"+joinGroups2);
Iterator<List<EdgeOperator>> jgIterator1111 = JoinGroupsListOptional.iterator();


while(jgIterator1111.hasNext())
{	List<EdgeOperator> aa = new ArrayList<>();
	aa=jgIterator1111.next();
if(aa.size()==1) {
	if(!operators_BushyTreeRightOptional.equals(aa)) {
	operators_BushyTreeRightOptional.addAll(aa);
	operators_BushyTreeLeftOptional.addAll(aa);
	}

	jgIterator1111.remove();
}
}	
	
/////////////////////////////Seperating SourceStatements from Exclusive Group and forming Left/Right Bushy Tree ////////////////////////

/////////////////////////////Ordering JoinGroupsListExclusive ////////////////////////



////////////////////////////////////////////////////////////////////////////
/*Iterator<List<EdgeOperator>> jgIterator6 = JoinGroupsListExclusive.iterator();

List<EdgeOperator> jg = new ArrayList<EdgeOperator>();
while(jgIterator6.hasNext())
{int ij6=0;

List<EdgeOperator> aa = new ArrayList<>();
aa=jgIterator6.next();
if(aa.size()>1  ) {
for(Entry<EdgeOperator, HashSet<Integer>> nfm:newFormationM.entrySet())
{
	int isConcrete=0;
	for(List<EdgeOperator> joinG:JoinGroupsListExclusive) {
		for(EdgeOperator joinG1:joinG) {
			if(joinG1.getEdge().getTriple().getObject().equals(nfm.getKey().getEdge().getTriple().getSubject()) && joinG1.getEdge().getTriple().getSubject().isConcrete())
				isConcrete=1;
		}
	}
	
	if(aa.contains(nfm.getKey()) && !nfm.getKey().getEdge().getTriple().getObject().isConcrete() && !nfm.getKey().getEdge().getTriple().getSubject().isConcrete() && isConcrete==0)
{ij6++;
jg.add(nfm.getKey());
}
}
if(aa.size()>=ij6 &&ij6>0)	
//for(EdgeOperator aaa:jg) {
////log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
for(EdgeOperator j:jg) {
if(!operators_BushyTreeRight.contains(j)) {
operators_BushyTreeRight.addAll(jg);
operators_BushyTreeLeft.addAll(jg);
}
aa.remove(j);

}
//}

}

}



Iterator<List<EdgeOperator>> jgIterator7 = joinGroups2.values().iterator();
List<EdgeOperator> jg1 = new ArrayList<EdgeOperator>();

while(jgIterator7.hasNext())
{int ij7=0;
List<EdgeOperator> aa = new ArrayList<>();
aa=jgIterator7.next();
if(aa.size()>1  ) {
for(Entry<EdgeOperator, HashSet<Integer>> nfm:newFormationM.entrySet())
{
	int isConcrete=0;
	for(List<EdgeOperator> joinG:JoinGroupsListExclusive) {
		for(EdgeOperator joinG1:joinG) {
			if(joinG1.getEdge().getTriple().getObject().equals(nfm.getKey().getEdge().getTriple().getSubject()) && joinG1.getEdge().getTriple().getSubject().isConcrete())
				isConcrete=1;
		}
	}
	
	if(aa.contains(nfm.getKey()) && !nfm.getKey().getEdge().getTriple().getObject().isConcrete() && !nfm.getKey().getEdge().getTriple().getSubject().isConcrete() && isConcrete==0)
{ij7++;
jg.add(nfm.getKey());

}
}
if(aa.size()>=ij7 &&ij7>0)	
//for(EdgeOperator aaa:jg) {
////log.info("aa.size()<newFormation.size():"+aa.size()+"--"+newFormationM.size());
for(EdgeOperator j:jg1) {
if(!operators_BushyTreeRight.contains(j)) {
operators_BushyTreeRight.addAll(jg1);
operators_BushyTreeLeft.addAll(jg1);
}
aa.remove(j);
}
//}

}
}*/

ForkJoinPool fjp98 = new ForkJoinPool();
fjp98.submit(()->
futileTripleOptional());
fjp98.shutdown();



ForkJoinPool fjp97 = new ForkJoinPool();
fjp97.submit(()->
refineTripleOptional(newFormationM,operators_BushyTreeLeftOptional,operators_BushyTreeRightOptional));
fjp97.shutdown();


for(int i=0;i<JoinGroupsListOptional.size();i++)
{
	if(JoinGroupsListOptional.get(i).size()==1)
	{
		operators_BushyTreeRightOptional.addAll(JoinGroupsListOptional.get(i));
		operators_BushyTreeLeftOptional.addAll(JoinGroupsListOptional.get(i));

		JoinGroupsListOptional.remove(i);
	}
}
/*
obtIterator=null;
jg2Iterator=null;
ax=null;
obt=null;
obtIterator = joinGroups2.values().iterator();
while(obtIterator.hasNext()) {
	int comparedSize=0;
	obt = obtIterator.next();
jg2Iterator = joinGroups2.values().iterator();
while(jg2Iterator.hasNext())	{
	List<EdgeOperator> 	jg2=jg2Iterator.next();
		size=jg2.size();
	if(obt.size()<size) {
		
		for(EdgeOperator jg22:jg2)
		{	if(obt.contains(jg22))
				comparedSize++;
		}
		ax.add(obt);
		//log.info("This is the size for whole1:"+obt.size()+"--"+size);;

		obtIterator.remove();
		
	}

	//log.info("THis is exclussss1:"+JoinGroupsListExclusive);

	
	}
}
*/////////////////////////////////////////////////////////////////////////////

////logger.info("This is the task of eternity222222222:"+JoinGroupsListExclusive);



////logger.info("This is now operators_BushyTreeRight:"+operators_BushyTreeRight);

////////////////////////////////////////////////////////////////////////////

//logger.info("This is the task of eternity222222222:"+JoinGroupsListExclusive);


List<EdgeOperator> JoinGroupsListExclusiveTemp1 =new Vector<>();
for(EdgeOperator obt1:operators_BushyTreeOrder)
	for(EdgeOperator jg2:operators_BushyTreeRightOptional)
		if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp1.contains(jg2) )
			JoinGroupsListExclusiveTemp1.add(jg2);

operators_BushyTreeRightOptional.clear();
operators_BushyTreeLeftOptional.clear();
Collections.reverse(JoinGroupsListExclusiveTemp1);
operators_BushyTreeRightOptional.addAll(JoinGroupsListExclusiveTemp1);
operators_BushyTreeLeftOptional.addAll(JoinGroupsListExclusiveTemp1);
//operators_BushyTreeRight.parallelStream();


/////////////////////////////Ordering JoinGroupsListExclusive ////////////////////////

//for(List<EdgeOperator> jgle1:JoinGroupsListExclusive)
////logger.info("This is now JoinGroupsListExclusive:"+jgle1);

//for(Entry<Integer, List<EdgeOperator>> jgle1:joinGroups2.entrySet())
////logger.info("This is now JoinGroups2:"+jgle1);

////logger.info("This is now the original order:"+operators_BushyTreeOrder);




////logger.info("This is JoinGroupsListExclusive:"+JoinGroupsListExclusive);
////logger.info("This is operators_BushyTreeLeft:"+operators_BushyTreeLeft);

Iterator<List<EdgeOperator>> jglll = JoinGroupsListOptional.iterator()	;
while(jglll.hasNext()) {
List<EdgeOperator> jglenl = jglll.next();
//if(jglenl.size()==1) {



Iterator<EdgeOperator> jgl111 = operators_BushyTreeLeftOptional.iterator();
while(jgl111.hasNext()) {
if(jglenl.contains(jgl111.next()))
{	jgl111.remove();  ;

}
}


//}
}


	if(operators_BushyTreeLeftOptional.size()>0)
{joinGroupsLeftOptional = CreateBushyTreesLeft(operators_BushyTreeLeftOptional,operators,operators_BushyTreeOrder);




for(Entry<EdgeOperator, Integer> e: joinGroupsLeftOptional.entries()) {
	//logger.info("This is the new group of queries123123 left:"+e.getKey()+"--"+e.getValue());
		if(!JoinGroupsListLeftOptional.contains(e.getKey()))
	JoinGroupsListLeftOptional.add(e.getKey());
			}	
	
 
Iterator<EdgeOperator> jgll = JoinGroupsListLeftOptional.iterator()	;
while(jgll.hasNext()) {
com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator jglenl = jgll.next();
//if(jglenl.size()==1) {
	if(operators_BushyTreeRightOptional.toString().contains(jglenl.toString()))
{	operators_BushyTreeRightOptional.remove(jglenl);

}
//}
}

Iterator<List<EdgeOperator>> jgrrr = JoinGroupsListOptional.iterator()	;
while(jgrrr.hasNext()) {
List<EdgeOperator> jglenr = jgrrr.next();
//if(jglenl.size()==1) {



Iterator<EdgeOperator> jgr111 = operators_BushyTreeRightOptional.iterator();
while(jgr111.hasNext()) {
if(jglenr.contains(jgr111.next()))
{	jgr111.remove();  ;

}
}
}
Iterator<EdgeOperator> jgrrl = JoinGroupsListLeftOptional.iterator()	;
while(jgrrl.hasNext()) {
EdgeOperator jglenrl = jgrrl.next();
//if(jglenl.size()==1) {



Iterator<EdgeOperator> jgr1112 = operators_BushyTreeRightOptional.iterator();
while(jgr1112.hasNext()) {
if(jglenrl.equals(jgr1112.next()))
{	jgr1112.remove();  ;

}
}
}
if(operators_BushyTreeRightOptional!=null)
joinGroupsRightOptional = CreateBushyTreesRight(operators_BushyTreeRightOptional,operators,operators_BushyTreeOrder);



if(joinGroupsRightOptional!=null)
for(Entry<EdgeOperator, Integer> e: joinGroupsRightOptional.entries()) {
	//logger.info("This is the new group of queries123123:"+e.getKey()+"--"+e.getValue());
	if(!JoinGroupsListRightOptional.contains(e.getKey()))
	JoinGroupsListRightOptional.add(e.getKey());
			}	
}
		/////logger.info("This is now right bushy tree:"+joinGroups1);

	

	
	//for(int i=0;i<JoinGroupsList.size();i++)
	/*for(List<EdgeOperator> jglr:JoinGroupsListExclusive)
			for(EdgeOperator jglr1:jglr)
	if(JoinGroupsList.toString().contains(jglr1.toString())) {			
		for(List<EdgeOperator> jgl:JoinGroupsList)
		

	}*/
		//logger.info("This is left group tree:"+JoinGroupsListLeft);

		//logger.info("This is right group tree:"+JoinGroupsListRight);
		Iterator<List<EdgeOperator>>  xyz=JoinGroupsListOptional.iterator();
		while(xyz.hasNext()) {
			List<EdgeOperator> xyz1 = xyz.next();
			if(xyz1.size()==0)
				xyz.remove();
		}
		
		
		////logger.info("This is the new group list of JoinGroupsListRight:"+operators_BushyTreeRight);

//		//logger.info("This is the new group list of JoinGroupsListLeft:"+operators_BushyTreeLeft);

			//ExecOrder
		//logger.info("This is now operators_BushyTreeRight:"+operators_BushyTreeRight);
		List<EdgeOperator> JoinGroupsListExclusiveTemp =new Vector<>();
	/*		
		for(EdgeOperator obt1:operators_BushyTreeOrder)
		{
			for(List<EdgeOperator> jg2:JoinGroupsListExclusive) {
				
				for(EdgeOperator jg22:jg2) {
				if(jg22.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp.contains(jg22) )
					JoinGroupsListExclusiveTemp.add(jg22);
				}
					
			}
				if(!JoinGroupsListExclusiveTempT.contains(JoinGroupsListExclusiveTemp))
					JoinGroupsListExclusiveTempT.add(JoinGroupsListExclusiveTemp);
				JoinGroupsListExclusiveTemp =new	Vector<>();
				
		}

		
	//	Map<List<EdgeOperator>, Integer> JoinGroupsListExclusiveTempTV =new ConcurrentHashMap<>();
			
	//	for(Entry<List<EdgeOperator>, Integer> jg2:joinGroups2.entrySet()) 
	//		for(EdgeOperator obt1:operators_BushyTreeOrder)
	//		if(jg2.getKey().get(0).getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTempTV.containsKey(jg2.getKey()) )
	//			JoinGroupsListExclusiveTempTV.put(jg2.getKey(),jg2.getValue());
		
				
			


		//joinGroups2.clear();

//		joinGroups2.putAll(JoinGroupsListExclusiveTempTV);

		for(EdgeOperator obt1:operators_BushyTreeOrder)
			for(Entry<List<EdgeOperator>, Integer> jg2:joinGroups2.entrySet())
				if(jg2.getKey().size()>0)	
				if(obt1.toString().equals(jg2.getKey().get(0).toString()))
					joinGroups2Temp1.put(jg2.getKey(),jg2.getValue());

		joinGroups2.clear();
		joinGroups2.putAll(joinGroups2Temp1);
*/if(JoinGroupsListRightOptional.size()>0 || !JoinGroupsListRightOptional.isEmpty()) {
		JoinGroupsListExclusiveTemp =new	Vector<>();
		for(EdgeOperator jg2:JoinGroupsListRightOptional) {
			for(EdgeOperator obt1:operators_BushyTreeOrder)
			{
			if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp.contains(jg2) )
				JoinGroupsListExclusiveTemp.add(jg2);
			
				
		}
			
	}


		JoinGroupsListRightOptional.clear();

		JoinGroupsListRightOptional.addAll(JoinGroupsListExclusiveTemp);


}
if(JoinGroupsListLeftOptional.size()>0 || !JoinGroupsListLeftOptional.isEmpty()) {

JoinGroupsListExclusiveTemp =new	Vector<>();
for(EdgeOperator obt1:operators_BushyTreeOrder)
{		
for(EdgeOperator jg2:JoinGroupsListLeftOptional) {
			if(jg2.getEdge().equals(obt1.getEdge())&&!JoinGroupsListExclusiveTemp.contains(jg2) )
				JoinGroupsListExclusiveTemp.add(jg2);
			
				
		}
			
	}


		JoinGroupsListLeftOptional.clear();

		JoinGroupsListLeftOptional.addAll(JoinGroupsListExclusiveTemp);
}

}




public void futileTriple() {
	//If 1 groups contain subset of elements of other group then remove 
	//group with less elements
	int size =0;
	Iterator<List<EdgeOperator>> obtIterator;
	Iterator<List<EdgeOperator>> jg2Iterator;
	List<EdgeOperator> obt = null;
	List<EdgeOperator> toBeDeleted=new ArrayList<>();
	for(int i=0;i<JoinGroupsListExclusive.size();i++)
		for(int j=0;j<JoinGroupsListExclusive.get(i).size();j++)
	if( (JoinGroupsListExclusive.get(i).get(j).getEdge().getTriple().getObject().isConcrete() ||JoinGroupsListExclusive.get(i).get(j).getEdge().getTriple().getSubject().isConcrete())) {
	obtIterator = JoinGroupsListExclusive.iterator();
	while(obtIterator.hasNext()) {
		int comparedSize=0;
		 obt = obtIterator.next();
	jg2Iterator = JoinGroupsListExclusive.iterator();
	List<EdgeOperator> 	jg2;
	while(jg2Iterator.hasNext())	{
			jg2=jg2Iterator.next();
			size=jg2.size();
			toBeDeleted.clear();
			comparedSize=0;
			
				if(obt.size()>size) {
			
			for(EdgeOperator jg22:jg2)
			{	if(obt.contains(jg22))
					{comparedSize++;
			toBeDeleted.add(jg22);
					}
			}
			
		//	ax.add(obt);
			////logger.info("This is the size for whole:"+obt.size()+"--"+size);;
			if(comparedSize>=size-1) {
				for(EdgeOperator jg22:toBeDeleted)
				{	jg2.remove(jg22);
						
				}
			}
		//	if(obtIterator.hasNext())
//				if(obtIterator.next().size()>1)
//					break;
			
		}

		
		
		////logger.info("THis is exclussss:"+JoinGroupsListExclusive);

		
		}
	}
	}

}

public static void futileTripleOptional() {
	//If 1 groups contain subset of elements of other group then remove 
	//group with less elements
	int size =0;
	Iterator<List<EdgeOperator>> obtIterator;
	Iterator<List<EdgeOperator>> jg2Iterator;
	List<EdgeOperator> obt = null;
	List<EdgeOperator> toBeDeleted=new ArrayList<>();
	for(int i=0;i<JoinGroupsListOptional.size();i++)
		for(int j=0;j<JoinGroupsListOptional.get(i).size();j++)
	if( (JoinGroupsListOptional.get(i).get(j).getEdge().getTriple().getObject().isConcrete() ||JoinGroupsListExclusive.get(i).get(j).getEdge().getTriple().getSubject().isConcrete())) {
	obtIterator = JoinGroupsListOptional.iterator();
	while(obtIterator.hasNext()) {
		int comparedSize=0;
		 obt = obtIterator.next();
	jg2Iterator = JoinGroupsListOptional.iterator();
	List<EdgeOperator> 	jg2;
	while(jg2Iterator.hasNext())	{
			jg2=jg2Iterator.next();
			size=jg2.size();
			toBeDeleted.clear();
			comparedSize=0;
			
				if(obt.size()>size) {
			
			for(EdgeOperator jg22:jg2)
			{	if(obt.contains(jg22))
					{comparedSize++;
			toBeDeleted.add(jg22);
					}
			}
			
		//	ax.add(obt);
			////logger.info("This is the size for whole:"+obt.size()+"--"+size);;
			if(comparedSize>=size-1) {
				for(EdgeOperator jg22:toBeDeleted)
				{	jg2.remove(jg22);
						
				}
			}
		//	if(obtIterator.hasNext())
//				if(obtIterator.next().size()>1)
//					break;
			
		}

		
		
		////logger.info("THis is exclussss:"+JoinGroupsListExclusive);

		
		}
	}
	}

}


void refineTriple(ConcurrentHashMap<EdgeOperator, HashSet<Integer>> newFormationM, List<EdgeOperator> operators_BushyTreeLeft, List<EdgeOperator> operators_BushyTreeRight) {
	///In the same set if triple pattern is not related to one with a URI
	//or has a mutliple source remove them
	//If a group does not have a URI with a single element then remove 
	Iterator<List<EdgeOperator>> obt1Iterator1;
	Iterator<List<EdgeOperator>> jg3Iterator1;
	Iterator<EdgeOperator> jg4Iterator;
	List<EdgeOperator> obt11;
	List<EdgeOperator> tobeRemoved = new ArrayList<>();
	int ab1=0;
	int size=0;
	int countConcrete=0;
	List<Node> obtnew1 = new ArrayList<>();
	obt1Iterator1 = joinGroupsOptional1.keySet().iterator();
	while(obt1Iterator1.hasNext()) {

		 obt11 = obt1Iterator1.next();
	jg3Iterator1 = JoinGroupsListExclusive.iterator();
	List<EdgeOperator> 	jg3;
	for(EdgeOperator bb:obt11) {
		if(bb.getEdge().getTriple().getObject().isConcrete() )
	{ab1++;
	////logger.info("This is the first:");
	obtnew1.add(bb.getEdge().getTriple().getSubject());
	}

		if(bb.getEdge().getTriple().getSubject().isConcrete() )
		{	ab1++;
		obtnew1.add(bb.getEdge().getTriple().getObject());
		}
	}

	if(ab1>0) {
		ab1=0;
	while(jg3Iterator1.hasNext())	{
			jg3=jg3Iterator1.next();
			size=jg3.size();
			jg4Iterator=jg3.iterator();
			
			if(obt11.equals(jg3)) {

			while(jg4Iterator.hasNext())
			//for(EdgeOperator jg22:jg3)
			{
			
				EdgeOperator jg22 = jg4Iterator.next();	
				if(!(obtnew1.contains(jg22.getEdge().getTriple().getSubject())
						|| obtnew1.contains(jg22.getEdge().getTriple().getObject()))) {
				if(
				!(jg22.getEdge().getTriple().getSubject().isURI() || jg22.getEdge().getTriple().getObject().isURI() 
						||	jg22.getEdge().getTriple().getSubject().isLiteral()|| jg22.getEdge().getTriple().getObject().isLiteral())	
			 )
		//		JoinGroupsListExclusive.get(ab).remove(jg22);
				{
		if(newFormationM.containsKey(jg22))
			{ tobeRemoved.add(jg22);
			//obt1.remove(jg22);
		//	operators_BushyTreeRight.addAll(jg3);
		//	operators_BushyTreeLeft.addAll(jg3);
			}
			
//				operators_BushyTreeRight
		if(jg3.size()==1) {
			if(!operators_BushyTreeRight.equals(jg3)) {
			operators_BushyTreeRight.addAll(jg3);
			operators_BushyTreeLeft.addAll(jg3);
			}
			jg3Iterator1.remove();
			break;	
		}
				}
				}
			}
			if(!tobeRemoved.isEmpty())
			for(EdgeOperator tbr:tobeRemoved) {
			if(!operators_BushyTreeRight.contains(tbr)) {
			operators_BushyTreeRight.add(tbr);
			operators_BushyTreeLeft.add(tbr);
			}
			jg3.remove(tbr);
			}
			tobeRemoved.clear();
			}
		//	ax.add(obt);
			////logger.info("This is the size for whole:"+obt.size()+"--"+size);;
			
		}

		
		
		////logger.info("THis is exclussss:"+JoinGroupsListExclusive);

		
		}


	if(ab1==0) {
		ab1=0;
	while(jg3Iterator1.hasNext())	{
			jg3=jg3Iterator1.next();
			size=jg3.size();
			jg4Iterator=jg3.iterator();
			
			if(obt11.equals(jg3)) {

			while(jg4Iterator.hasNext())
			//for(EdgeOperator jg22:jg3)
			{
			
				EdgeOperator jg22 = jg4Iterator.next();	
				if(
				(jg22.getEdge().getTriple().getSubject().isConcrete() || jg22.getEdge().getTriple().getObject().isConcrete() 
//						||	jg22.getEdge().getTriple().getSubject().isLiteral()|| jg22.getEdge().getTriple().getObject().isLiteral())	
			) )
		//		JoinGroupsListExclusive.get(ab).remove(jg22);
				{countConcrete++;}
				if(countConcrete>0)
					continue;
		if(newFormationM.containsKey(jg22))
			{ tobeRemoved.add(jg22);
			}
			
//				operators_BushyTreeRight
		if(jg3.size()==1) {
			if(!operators_BushyTreeRight.equals(jg3)) {
			operators_BushyTreeRight.addAll(jg3);
			operators_BushyTreeLeft.addAll(jg3);
			}
			jg3Iterator1.remove();
			break;	
		}
				}
				
			}
			if(!tobeRemoved.isEmpty())
			for(EdgeOperator tbr:tobeRemoved) {
			if(!operators_BushyTreeRight.contains(tbr)) {
			operators_BushyTreeRight.add(tbr);
			operators_BushyTreeLeft.add(tbr);
			}
			jg3.remove(tbr);
			}
			tobeRemoved.clear();
			}
		//	ax.add(obt);
			////logger.info("This is the size for whole:"+obt.size()+"--"+size);;
			
		

		
		
		////logger.info("THis is exclussss:"+JoinGroupsListExclusive);

		
		}
	countConcrete=0;
	}

}

static void refineTripleOptional(ConcurrentHashMap<EdgeOperator, HashSet<Integer>> newFormationM, List<EdgeOperator> operators_BushyTreeLeft, List<EdgeOperator> operators_BushyTreeRight) {
	///In the same set if triple pattern is not related to one with a URI
	//or has a mutliple source remove them
	//If a group does not have a URI with a single element then remove 
	Iterator<List<EdgeOperator>> obt1Iterator1;
	Iterator<List<EdgeOperator>> jg3Iterator1;
	Iterator<EdgeOperator> jg4Iterator;
	List<EdgeOperator> obt11;
	List<EdgeOperator> tobeRemoved = new ArrayList<>();
	int ab1=0;
	int size=0;
	int countConcrete=0;
	List<Node> obtnew1 = new ArrayList<>();
	obt1Iterator1 = joinGroupsOptional1.keySet().iterator();
	while(obt1Iterator1.hasNext()) {

		 obt11 = obt1Iterator1.next();
	jg3Iterator1 = JoinGroupsListOptional.iterator();
	List<EdgeOperator> 	jg3;
	for(EdgeOperator bb:obt11) {
		if(bb.getEdge().getTriple().getObject().isConcrete() )
	{ab1++;
	////logger.info("This is the first:");
	obtnew1.add(bb.getEdge().getTriple().getSubject());
	}

		if(bb.getEdge().getTriple().getSubject().isConcrete() )
		{	ab1++;
		obtnew1.add(bb.getEdge().getTriple().getObject());
		}
	}

	if(ab1>0) {
		ab1=0;
	while(jg3Iterator1.hasNext())	{
			jg3=jg3Iterator1.next();
			size=jg3.size();
			jg4Iterator=jg3.iterator();
			
			if(obt11.equals(jg3)) {

			while(jg4Iterator.hasNext())
			//for(EdgeOperator jg22:jg3)
			{
			
				EdgeOperator jg22 = jg4Iterator.next();	
				if(!(obtnew1.contains(jg22.getEdge().getTriple().getSubject())
						|| obtnew1.contains(jg22.getEdge().getTriple().getObject()))) {
				if(
				!(jg22.getEdge().getTriple().getSubject().isURI() || jg22.getEdge().getTriple().getObject().isURI() 
						||	jg22.getEdge().getTriple().getSubject().isLiteral()|| jg22.getEdge().getTriple().getObject().isLiteral())	
			 )
		//		JoinGroupsListExclusive.get(ab).remove(jg22);
				{
		if(newFormationM.containsKey(jg22))
			{ tobeRemoved.add(jg22);
			//obt1.remove(jg22);
		//	operators_BushyTreeRight.addAll(jg3);
		//	operators_BushyTreeLeft.addAll(jg3);
			}
			
//				operators_BushyTreeRight
		if(jg3.size()==1) {
			if(!operators_BushyTreeRight.equals(jg3)) {
			operators_BushyTreeRight.addAll(jg3);
			operators_BushyTreeLeft.addAll(jg3);
			}
			jg3Iterator1.remove();
			break;	
		}
				}
				}
			}
			if(!tobeRemoved.isEmpty())
			for(EdgeOperator tbr:tobeRemoved) {
			if(!operators_BushyTreeRight.contains(tbr)) {
			operators_BushyTreeRight.add(tbr);
			operators_BushyTreeLeft.add(tbr);
			}
			jg3.remove(tbr);
			}
			tobeRemoved.clear();
			}
		//	ax.add(obt);
			////logger.info("This is the size for whole:"+obt.size()+"--"+size);;
			
		}

		
		
		////logger.info("THis is exclussss:"+JoinGroupsListExclusive);

		
		}


	if(ab1==0) {
		ab1=0;
	while(jg3Iterator1.hasNext())	{
			jg3=jg3Iterator1.next();
			size=jg3.size();
			jg4Iterator=jg3.iterator();
			
			if(obt11.equals(jg3)) {

			while(jg4Iterator.hasNext())
			//for(EdgeOperator jg22:jg3)
			{
			
				EdgeOperator jg22 = jg4Iterator.next();	
				if(
				(jg22.getEdge().getTriple().getSubject().isConcrete() || jg22.getEdge().getTriple().getObject().isConcrete() 
//						||	jg22.getEdge().getTriple().getSubject().isLiteral()|| jg22.getEdge().getTriple().getObject().isLiteral())	
			) )
		//		JoinGroupsListExclusive.get(ab).remove(jg22);
				{countConcrete++;}
				if(countConcrete>0)
					continue;
		if(newFormationM.containsKey(jg22))
			{ tobeRemoved.add(jg22);
			}
			
//				operators_BushyTreeRight
		if(jg3.size()==1) {
			if(!operators_BushyTreeRight.equals(jg3)) {
			operators_BushyTreeRight.addAll(jg3);
			operators_BushyTreeLeft.addAll(jg3);
			}
			jg3Iterator1.remove();
			break;	
		}
				}
				
			}
			if(!tobeRemoved.isEmpty())
			for(EdgeOperator tbr:tobeRemoved) {
			if(!operators_BushyTreeRight.contains(tbr)) {
			operators_BushyTreeRight.add(tbr);
			operators_BushyTreeLeft.add(tbr);
			}
			jg3.remove(tbr);
			}
			tobeRemoved.clear();
			}
		//	ax.add(obt);
			////logger.info("This is the size for whole:"+obt.size()+"--"+size);;
			
		

		
		
		////logger.info("THis is exclussss:"+JoinGroupsListExclusive);

		
		}
	countConcrete=0;
	}

}










@Override
protected boolean hasNextBinding() {
	// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
	// execBGP11!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+results);

	if (results == null) {
		// //logger.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
		// execBGP12!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+results);
		//logger.info("This is after BGPEvalConstructor");
	//	if(completionE<1) {
		try {
			execBGP();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
//			StatementGroupOptimizer2 sgo = new StatementGroupOptimizer2(null);
//			sgo.meetNJoin(null);
	
		return results.hasNext();
		
		
//	}
//	return false;
}








@Override
public void output(IndentedWriter out, SerializationContext sCxt) {
	// TODO Auto-generated method stub
	
}







@Override
protected void requestCancel() {
	// TODO Auto-generated method stub
	
}					
			
								
			

}

