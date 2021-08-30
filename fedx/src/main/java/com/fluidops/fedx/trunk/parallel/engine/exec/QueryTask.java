package com.fluidops.fedx.trunk.parallel.engine.exec;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Bindings;

//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.apache.jena.sparql.core.Var;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.joda.time.LocalTime;

import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.description.RemoteService;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.BindJoin;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.HashJoin;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import com.fluidops.fedx.trunk.stream.engine.util.Logging;
import com.fluidops.fedx.trunk.stream.engine.util.QueryUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fluidops.fedx.Summary;
import com.fluidops.fedx.optimizer.Optimizer;
import com.fluidops.fedx.structures.Endpoint;

import org.apache.commons.collections.MultiMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.pfunction.library.alt;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.sparql.syntax.ElementGroup;
import com.fluidops.fedx.structures.QueryInfo;

public class QueryTask {
//	private	static final org.apache.log4j.Logger logger = LogManager.getLogger(QueryTask.class.getName());
	Multimap<String, ResultSet> pro = ArrayListMultimap.create();;
	Multimap<String, Binding> proExt = ArrayListMultimap.create();;

	//	boolean finished = false;
	AtomicBoolean finished = new AtomicBoolean(false);
	static List<EdgeOperator> ae = new Vector<>();
	static int isExecuted = 0;
	static volatile ConcurrentHashMap<Triple, Integer> singleVariable = new ConcurrentHashMap<>();
	static int singleVariableURL = 0;
	static int ab = 0;
//private final Object lock = new Object();
	public static HashMap<String, String> IsSingleStatement = new HashMap<>();
	protected Triple triple;
	static private TripleExecution issuer;
	protected static Endpoint service;
	private int timeout = 0;
	public static int IsLiteral = 0;
	public static int IsURLProper = 0;
	static int m = 0;
	static int num = 0;
	static int ghi;
	List<EdgeOperator> BushyTreeTriple = Collections.synchronizedList(new ArrayList<>());

	public static int ja = 0;
	// static HashMap<String,Integer> GroupEdgeOperatorGroupProcessed=new
	// HashMap<>();
	public static int isBound = 0;
	private int MAXTRIES = 3;
//public static Map<Triple,Integer> CompletionValue=Collections.synchronizedMap(new HashMap<Triple,Integer>()) ;;
//Map<Integer, String> Res = new HashMap<>();

	List count = new Vector<>();

//RepositoryConnection getSummaryConnection() {
	// return
	// ((Summary)(Optimizer.qInfo.getFedXConnection().getSummary())).getConnection();
//}	
	public QueryTask(TripleExecution te, Endpoint service) {
		this.triple = te.getTriple();
		this.service = service;
		this.issuer = te;
		// ////System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+this.triple+"--"+this.service+"--"+this.issuer);

	}

	public synchronized void setPro(String st, ResultSetMem rsm) {
		pro.put(st, rsm);
	}

	public synchronized Multimap<String, ResultSet> getPro() {
		return pro;
	}

	public QueryTask(Triple triple, Endpoint service, int timeout) {
		//// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		this.triple = triple;
		this.service = service;
		this.timeout = timeout;
	}

	public synchronized List<EdgeOperator> getBusyTreeTriple() {
		return BushyTreeTriple;
	}

	public void runTask() throws InterruptedException {
		
		/*
		 * org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
		 * org.apache.log4j.Logger.getLogger(
		 * "com.fluidops.fedx.trunk.parallel.engine.exec.QueryTask").setLevel(Level.OFF)
		 * ; org.apache.log4j.Logger.getLogger("httpclient.wire.level").setLevel(Level.
		 * FATAL);
		 * org.apache.log4j.Logger.getLogger("httpclient.wire").setLevel(Level.FATAL);
		 * 
		 * org.apache.log4j.Logger.getLogger("org.apache.http.wire").setLevel(Level.
		 * FATAL);
		 * org.apache.log4j.Logger.getLogger("org.apache.http.impl.client").setLevel(
		 * Level.FATAL);
		 * org.apache.log4j.Logger.getLogger("org.apache.http.impl.conn").setLevel(Level
		 * .FATAL);
		 * org.apache.log4j.Logger.getLogger("org.apache.http.client").setLevel(Level.
		 * FATAL);
		 */

		// System.out.println("This is in start of QueryTask");
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.SEVERE);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.SEVERE);
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
		ab++;

		// ////System.out.println("Normal QueryTask is being used:"+triple);
		// ForkJoinPool fjp = new ForkJoinPool(3);
		// fjp.invoke(fjp.submit(()->{

//	if(BGPEval.IsLeftQuery==false&&BGPEval.IsRightQuery==false)
		// else BushyTreeTriple=null;
		////// System.out.println("These are the created new Triples:"+BushyTreeTriple);

//ForkJoinPool fjp1 = new ForkJoinPool();	

		// fjp.shutdownNow();
//synchronized(lock) {
//	ForkJoinPool fjp1 = new ForkJoinPool();	

//	fjp1.submit(()->
//	{

		BushyTreeTriple = BushyTreeTripleCreation(triple);
	//	System.out.println("These are the triple final:"+BushyTreeTriple);
		Query q = buildQuery();
		
		
			Set<Binding> bindings = Collections.synchronizedSet(new HashSet<>());

			ResultSet rs = execQuery(q);

			bindings = postProcess(rs);
			synchronized (issuer) {
			issuer.addBindings(bindings);

			if (IsURLProper == 1)
				if (bindings.size() < 5)
					IsURLProper = 0;
			if (IsLiteral == 1)
				if (bindings.size() < 5)
					IsLiteral = 0;

		}

		

	}

	public boolean isFinished() {
		//// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		return finished.get();
	}

	protected Query buildQuery() {
		////// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask5!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		Query query = new Query();
		ElementGroup elg = new ElementGroup();
		// BGPEval.JoinGroupsList.stream().
		if (getBusyTreeTriple() != null) {
			// int ced=0;

			for (EdgeOperator btt : getBusyTreeTriple()) {
//if(ced<ghi) {
				//// System.out.println("This is the BushyTreeTrple in QueryTask:"+btt);
				elg.addTriplePattern(btt.getEdge().getTriple());
//		ced++;
//}		
			}
		} else
			elg.addTriplePattern(triple);
		query.setQueryResultStar(true);
		query.setQueryPattern(elg);
		query.setQuerySelectType();
//		////System.out.println("---------------QueryTask buildQUery---------------: "+query+"--"+elg.getElements()+"--"+query.getQueryPattern()+"--"+query.getBindingValues()+"--"+query.getResultURIs());
		isBound = 0;
		return query;
	}

	protected ResultSet execQuery(Query query) {
		int unionCount = 0;

		try {
			HashSet<String> numOfNodes = new HashSet<>();

			int count = 0;
			num = 0;
			String[] bbb = query.toString().split(" ");

			for (String bb : bbb)
				if (bb.startsWith("?"))
					numOfNodes.add(bb.replaceAll(" ", "").replaceAll("\n", ""));
			////// System.out.printlnln("This is numOfNodes:"+numOfNodes);
			num = numOfNodes.size();
			for (int i = 0; i < query.toString().length(); i++) {
				if (query.toString().charAt(i) == '?') {
					count++;
					//// System.out.printlnln("This is good one:"+query.toString().charAt(i));
				}
			}
			// If 2 URI present in 2 triple pattern in query then ignore 2nd URI
			singleVariable.put(triple, 0);
			if (count == 1)
				if (singleVariable.containsKey(triple))
					if (singleVariable.containsValue(0))
						if (query.toString().contains("\"")) {
							IsLiteral = 1;
							singleVariable.replace(triple, 1);
						}

			if (count == 1 && singleVariableURL == 0)
				if (query.toString().contains("<")) {
					IsURLProper = 1;
					singleVariableURL++;
				}
			//// System.out.printlnln("This is occurenece of
			//// ?:"+count+"--"+IsSingleStatement);

			boolean error = false;
			String url = service.getEndpoint();
			String queryString;
			//// System.out.println("This is the filter out of OP:"+ParaEng.opq);
			//// System.out.println("This is the Query:"+query);
			/*
			 * for(int j=0;j<xyz2.length;j++) {
			 * //System.out.println("These are the character sequence:"+xyz1[i]+"--"+xyz2[j]
			 * ); if(xyz1[i].equals(xyz2[j])) { if(xyz1[i].startsWith("?"))
			 * //System.out.println("These are the character sequence in Clause:"+xyz1[i]);
			 * } }
			 */

			// //System.out.println("ThiOP12:"+QueryExtension(query));
			QueryEngineHTTP qexec;
			// if(ParaEng.opq!=null) {

			String qString = QueryExtension(query);
			// qString = qString.replace("}", "} LIMIT 100");
			qexec = new QueryEngineHTTP(url, qString);
			// }
			// else {
//	//System.out.println("This is now creating extended query:"+qString+"--"+url);

//		qexec = new QueryEngineHTTP(url,query);
			// }
			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask62!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
			// ");

			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask
			// execQuery 2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+url+"--"+qexec);
//				//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask execQuery 2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: ");
			// qexec.toString().contains("UNION");
			if (Config.log)
				Logging.out();// record the number of outgoing requests
			// qexec.addParam("timeout", String.valueOf(timeout));
			int n = 0;
			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask
			// execQuery 3!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+qexec.toString() );
			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask6!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
			// "+qexec);

			// while (n < MAXTRIES) {

			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask61!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
			// "+MAXTRIES);
			m++;

//	System.out.println("THis is object:"+query);

//	ReadWriteLock lock = new ReentrantReadWriteLock();
			if (QueryTask.isBound == 0)
				System.out.println("This is now creating extended query:" + qString + "--" + url+"--"+service);

			//
//					ForkJoinPool fjp = new ForkJoinPool();
			// fjp.submit(()->{

			// lock.writeLock().lock();
			// try {
			// try {
			// Thread.sleep(1);
			// } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			ResultSetMem result = new ResultSetMem(qexec.execSelect());// }).join();

			// } finally {
			// lock.writeLock().unlock();
			// }
			// if(BoundQueryTask.isBound==1)

			// if(BoundQueryTask.isBound==1)
			// while(result.hasNext())
			// System.out.println("This is now the
			// bindings:"+result.nextBinding()+"--"+BoundQueryTask.task);

			// if(isBound==1)
			// for(int i1=0;i1<BoundQueryTask.bindings.size();i1++)
			// System.out.println("This is now the
			// bindings:"+BoundQueryTask.bindings.get(i1));
			// result1=new ResultSetMem(result);
			// pro.computeIfAbsent(qString,k->new ResultSetMem(qexec.execSelect()));
			// //put(qString,result);
			if (BoundQueryTask.isBound == 1)
				setPro(qString, result);
			// pro.put(qString, result); //put(qString,result);
			// for(Entry<String, ResultSet> pr:pro.entrySet())
			// {String ar=pr.getValue().nextBinding().toString();
			// System.out.println("This is the problem here44444:"+pr.getKey()+"--"+ar);
			// }

//					fjp.shutdownNow();
			// result1=result;
			// ResultSet result1 = result;

			// TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL,
			// query.toString());
			//// System.out.println(queryString);
			// TupleQueryResult result1 = tupleQuery.evaluate();

			// System.out.println(pro);

			/*
			 * try { while(result1.hasNext()) {
			 * //System.out.println("This is the second type query for result:"+result1.next
			 * ()); } } finally { result1.close(); }
			 */
			// if (Config.log)
			// Logging.in(((ResultSetMem) result).size());
			// while(result.hasNext()) {
			// a=result.next();
			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask4
			// execQuery 4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
			// "+result.next().varNames()+"--"+result.next());
			// }
			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask4
			// execQuery 4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
			// "+qexec.execDescribe()+"--"+qexec);
			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!QueryTask4 execQuery
			// 4!!!!!!!!!!!!!: "+m);
			// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask4
			// execQuery 4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+qexec.getContext()
			// +"--"+qexec);

			// m=0;
			if (error) {
				result = new ResultSetMem();
				System.err.println("Error:\nremote service: " + url + "\nquery:\n" + query);
			}
			//// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask
			//// execQuery 5!!!!!!!!!!!!:"+result);

			// if(QueryTask.isBound==1) {
			// for(Entry<Map<Map<Node, Integer>, Node>, Node>
			// b1:BoundQueryTask.bin.entrySet())
			// for(Var q:query.getValuesVariables())
			// System.out.println("This is experiment1:"+b1);
			// System.out.println("This is experiment1:"+b1);

			// postProcess(result);
			// for(Binding q1:query.getValuesData())
			// System.out.println("This is experiment2:"+q1);
			// for(String q2:query.getResultVars())
			// System.out.println("This is experiment3:"+q2);
			// }
			qexec.close();

			// ArrayList<ResultSet> rs = new ArrayList<>();
			// rs.add(result);
			return result;

		} catch (NullPointerException e) {
			// System.out.println(
			// "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!QueryTask execQuery
			// 4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: ");
		} catch (ResultSetException e) {
		}
		return null;
	}

	public synchronized Set<Binding> postProcess(ResultSet rs) {
//System.out.println("!!!!!!!!!!!!!!QueryTask7!!!!!!!!!!!!!!!!!!!!"+num);
		Set<Binding> results = new HashSet<Binding>();
		int i = 0;
		Binding a;
		ResultSet b;
//	if(BoundQueryTask.isBound==0)

		while (rs.hasNext()) {
			a = rs.nextBinding();
			Pattern regex = Pattern.compile("[?=\\)][?= ][?=\\(]");
			int numA = regex.split(a.toString()).length;
//		 System.out.println("These are the equations:"+num+"--"+numA+"--"+a);
			if (numA == num) {
//	System.out.println("This is working set:"+a);
				results.add(a);
			}
		}
		return results;
	}
	/*
	 * public static boolean isPrime(int n) { for(int i = 2;2*i<n;i++) { if(n%i==0)
	 * { return false; } } return true; }
	 */
	/*
	 * static int divsum(int n) { int sum = 0; for (int i = 1; i <= (Math.sqrt(n));
	 * i++) { if (n % i == 0) {
	 * 
	 * if (n / i == i) { sum = sum + i; } else { sum = sum + i; sum = sum + (n / i);
	 * } } } return sum; }
	 */

	public static String QueryExtension(Query q) {
		String FilterString = null;
		int countQmark = 0;
		// if(ParaEng.opq==null)
		// return q.toString();
		// FilterString=ParaEng.Filter.toString();
		if (ParaEng.Filter.contains("FILTER"))
			FilterString = ParaEng.Filter;
		else
			FilterString = " ";
		if (FilterString == " ")
			return q.toString();

		String extension = null;
		if (FilterString != " ")
			extension = FilterString.toString().substring(FilterString.toString().indexOf("FILTER"),
					FilterString.toString().indexOf(")") + 1);
//			////System.out.printlnln("These are the character sequence in Clause0:"+extension);

		String[] Extension = extension.toString().split(" ");
//			////System.out.printlnln("These are the character sequence in Clause0:"+q);
//			////System.out.printlnln("These are the character sequence in Clause0.2:"+extension);

		String[] query = q.toString().split(" ");

//			for(int i =0 ; i<query.length;i++)
		for (String ext : Extension)
			if (ext.contains("?"))
				countQmark++;

		if (countQmark == 2)
			for (String qa : query)
				for (String ex : Extension) {
					String qq = qa.replaceAll("[(),]", "");
					String e = ex.replaceAll("[(),]", "");
					if (qq.equals(e) && (!qq.isEmpty())) {
						isExecuted++;
						// //System.out.printlnln("These are the character sequence in
						// Clause0.1111:"+qa.replaceAll("[(),]","")+"--"+ex.replaceAll("[(),]","")+"--"+Extension);

					}
				}
//			//System.out.printlnln("These are the character sequence in Clause0.1:"+countQmark+"--"+isExecuted);

		String queryString = null;
		for (int i = 0; i < Extension.length; i++) {
			// ////System.out.printlnln("These are the character sequence in
			// Clause:"+Extension[i]);
			int breaking = 0;
			if (Extension[i].startsWith("(?") || Extension[i].startsWith("?") || Extension[i].startsWith("str")
					|| Extension[i].startsWith("regex")) {
				{

					for (int j = 0; j < query.length - 1; j++) {

						////// System.out.printlnln("These are the character sequence in
						////// Clause35:"+Extension[i]);

						if (query[j].startsWith("(?") || query[j].startsWith("?") || query[j].startsWith("str")
								|| Extension[i].startsWith("regex")) {
							////// System.out.printlnln("These are the character sequence in
							////// Clause36:"+query[j]);
							////// System.out.printlnln("These are the character sequence in
							////// Clause37:"+Extension[i]+"--"+Extension[i].substring(4,
							////// Extension[i].length()-1));

							String NewQuery = " " + extension + "}";
							// If both filter elements are Nodes
							if (countQmark == 2) {
								if ((Extension[i].startsWith("str") || Extension[i].startsWith("regex"))
										&& (isExecuted > 1)) {
									if (query[j].equals(Extension[i].substring(4, Extension[i].length() - 1))) {
										queryString = q.toString().substring(0, q.toString().length() - 2)
												.concat(NewQuery);
									}
								}
								if ((Extension[i].startsWith("?") || Extension[i].startsWith("(?"))
										&& (isExecuted > 1)) {
									if (query[j].equals(Extension[i].replaceAll("[(),]", ""))) {
										queryString = q.toString().substring(0, q.toString().length() - 2)
												.concat(NewQuery);
									}
								}
							}
							// If one filter element is node
							else {
								if (Extension[i].startsWith("str")) {
									if (query[j].equals(Extension[i].substring(4, Extension[i].length() - 1))) {

										queryString = q.toString().substring(0, q.toString().length() - 2)
												.concat(NewQuery);
									}
								}
								if ((Extension[i].startsWith("regex"))) {
									String ext = Extension[i].substring(4, Extension[i].length() - 1);
									//// System.out.printlnln("Thesis problematic:"+ext);
									HashMap<String, String> nMap = new HashMap<>();
									for (int l = 0; l < Extension.length; l++)
										if (query[j].equals(Extension[l])) {
											String[] nQuery = NewQuery.split(" ");
											int index = 0;
											for (String nq : nQuery) {
												index++;
												if (nq.startsWith("?"))
													break;
											}
											for (int i1 = index - 1; i1 < nQuery.length - 1; i1++) {
												nMap.put(nQuery[i1], nQuery[i1] + ",");
											}
											// NewQuery=Arrays.toString(nQuery).replace("[]", "");//.toString();
											//// System.out.printlnln("These are the seperators:"+NewQuery);
											for (Entry<String, String> n : nMap.entrySet())
												NewQuery = NewQuery.replace(n.getKey(), n.getValue());
											queryString = q.toString().substring(0, q.toString().length() - 2)
													.concat(NewQuery);
											breaking = 1;
											break;
										}
								}
								if (Extension[i].startsWith("?") || Extension[i].startsWith("(?")) {
									String e = Extension[i].replaceAll(",", "").replaceAll("\n", "").replaceAll("[)(]",
											"");
									String ji = query[j].replaceAll(",", "").replaceAll("\n", "").replaceAll("[)(]",
											"");
									if (ji.equals(e)) {
										queryString = q.toString().substring(0, q.toString().length() - 2)
												.concat(NewQuery);
									}
								}
							}
							if (breaking == 1) {
								break;
							}
						}
					}
					if (breaking == 1) {
						breaking = 0;
						break;
					}
				}
			}
			// return queryString;
		}
//			////System.out.printlnln("This is the new query:"+queryString);
		if (queryString == null)
			return q.toString();
		else
			return queryString;

	}

	public synchronized List<EdgeOperator> BushyTreeTripleCreation(Triple triples) {
		List<EdgeOperator> BushyTreeTriple2 = new Vector<EdgeOperator>();
		List<EdgeOperator> AlreadyProcessed = new Vector<EdgeOperator>();
		List<List<EdgeOperator>> JoinGroups = new Vector<>();
//if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
		JoinGroups.addAll(BGPEval.JoinGroupsListOptional);
		JoinGroups.addAll(BGPEval.JoinGroupsListExclusive);
		if (BGPEval.JoinGroupsListRight.size() > 0)
			JoinGroups.add(BGPEval.JoinGroupsListRight);
		if (BGPEval.JoinGroupsListLeft.size() > 0)
			JoinGroups.add(BGPEval.JoinGroupsListLeft);
		if (BGPEval.JoinGroupsListRightOptional.size() > 0)
			JoinGroups.add(BGPEval.JoinGroupsListRightOptional);
		if (BGPEval.JoinGroupsListLeftOptional.size() > 0)
			JoinGroups.add(BGPEval.JoinGroupsListLeftOptional);
//logger.info("This is the processing order:"+JoinGroups);
		int br = 0;
//if(BGPEval.IsLeftQuery==false&&BGPEval.IsRightQuery==false)
//{	
		for (int i = 0; i < JoinGroups.size(); i++)
			if (JoinGroups.get(i).size() > 0)
				for (Entry<EdgeOperator, Map<EdgeOperator, Integer>> e2 : BGPEval.exchangeOfElements.entrySet()) {
					for (Entry<EdgeOperator, Integer> e1 : e2.getValue().entrySet())
						if (e2.getKey().equals(JoinGroups.get(i).get(0)) && e1.getValue() == 1)
							if (e1.getKey().getEdge().getTriple().equals(triple)) {
								{
									BushyTreeTriple2.addAll(JoinGroups.get(i));
									br = 1;
									break;
								}
							}
					if (br == 1)
						return BushyTreeTriple2;
				}

//	}
//}
//else {
		BushyTreeTriple2 = null;

		return BushyTreeTriple2;

	}

	public static Map<Integer, String> Non_Predicate(ResultSet rs) {

		BindingHashMap extend = new BindingHashMap();// TODO do not use a new binding map
		Map<Integer, String> FinalMap = new HashMap<>();// pInc.values().stream().collect(Collectors.groupingBy(pInc.entrySet()::getKey,
														// TreeMap::new, Collectors.toList()));
//	HashMap<Integer,ArrayList<String>> pInc = new HashMap<>();
		Multimap<Integer, String> pInc = ArrayListMultimap.create();
		int num = 0;
		String str;
		// Iterator<ResultSet> rs2 = rs.iterator();
		while (rs.hasNext()) {
			Binding binding = rs.nextBinding();

			if (ParaEng.pConstant.toString().contains("?p=")) {
//			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!BoundQueryTask33333333333333!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		while(binding1.hasNext()) {
				// Binding binding = binding1.nextBinding();
				HashMap<Var, String> otherBinding = BoundQueryTask.ExtractBinding(binding);
				for (Entry<Var, String> ob : otherBinding.entrySet()) {
					if (!ob.getValue().equals("p") && binding.get(ob.getKey()).toString().startsWith("http:")
							&& binding.get(ob.getKey()).toString().contains("TCGA")) {
//	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!BoundQueryTask4444444444444!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

						num = Integer.parseInt(ob.getKey().getName().substring(ob.getKey().getName().indexOf("_") + 1));
						str = binding.get(ob.getKey()).toString();
//	pInc.put(num,new ArrayList<>());
						// pInc.get(num).add(str);
						pInc.put(num, str);

//	System.out.println("This is the number and its value:"+pInc.keySet()+"--"+pInc.values());
					}

					for (Entry<Integer, String> entry : pInc.entries()) {

						// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!BoundQueryTask555555555555!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						FinalMap.put(entry.getKey(), pInc.get(entry.getKey()).stream().parallel()
								.max(Comparator.comparing(String::length)).get());
					}

//extend.add(Var.alloc(ob.getValue()), binding.get(ob.getKey()));
				}
			}
			// }
			else
				return null;
		}
//	for (Entry<Integer, String> entry : FinalMap.entrySet()) {
//System.out.println("This is good good godd whatever go to  hell:"+entry);

//	}
		return FinalMap;
	}
}