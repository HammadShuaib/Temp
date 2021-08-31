/*
 * Copyright (C) 2008-2013, fluid Operations AG
 *
 * FedX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fluidops.fedx.optimizer;

//import javax.xml.ws.Binding;
import com.fluidops.fedx.EndpointManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.StrictEvaluationStrategy;

import com.fluidops.fedx.EndpointManager;
import com.fluidops.fedx.FedX;
import com.fluidops.fedx.Summary;
import com.fluidops.fedx.Util;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryInfo;

public class Optimizer {
//private	static final org.apache.log4j.Logger logger = LogManager.getLogger(Optimizer.class.getName());
	public static LinkedHashMap<Integer, LinkedHashMap<Double, Double>> JoinValues = new LinkedHashMap<>();
	public static LinkedHashMap<String, HashMap<Var, Var>> JoinVars = new LinkedHashMap<>();
	public static TupleExpr query;
	public static HashMap<String, Set<Endpoint>> EndpointRef = new HashMap<>();
	private static List<String> StatementPatternArray = new ArrayList<>();
	static GenericInfoOptimizer info = null;

	private static Collection<String> TripleArray;
	static ArrayList<String> endpoints;
	static Set<String> EndpointArray = new HashSet<>();
	static Set<Endpoint> EndpointArrayE = new HashSet<>();
//	public static double[] objectCount1;
//	public static double[] subjectCount1;
//	public static double[] tripleCount1;
	public static ConcurrentHashMap<String, Double> objectCount1 = new ConcurrentHashMap<String, Double>();
	public static ConcurrentHashMap<String, Double> subjectCount1 = new ConcurrentHashMap<String, Double>();
	public static ConcurrentHashMap<String, Long> tripleCount1 = new ConcurrentHashMap<String, Long>();
	static List<StatementSource> sources;
	static StatementPattern stmt;
	static LinkedHashMap<Double, Double> xyz1 = new LinkedHashMap<>();;
	static LinkedHashMap<Integer, ArrayList<HashMap<Var, Var>>> xyz = new LinkedHashMap<>();;

	static int balance;
	static int balance1;

	static double objectCount;
	static double subjectCount;
	static long tripleCount;
	public static ConcurrentHashMap<String, Double> sCount;
	public static ConcurrentHashMap<String, Double> oCount;
	public static ConcurrentHashMap<String, Long> tCount;
	public static ConcurrentHashMap<StatementPattern, List<StatementSource>> sourceSelection1;
	static BindingSet binding;
	public static QueryInfo qInfo;
	public static int ExhOpt = 0;
	public static ConcurrentHashMap<String, String> subobj = new ConcurrentHashMap<String, String>();
	public static String obj;
	public static int i = 0;
	public static SourceSelection sourceSelection;
	static ArrayList<String> curQuery = new ArrayList<>();
	public static Object[][] triples = new Object[50][9];
	// private ArrayList<String> EndpointArray = new ArrayList<>();
	public static List<Endpoint> members;

	public static TupleExpr optimize(TupleExpr parsed, Dataset dataset, BindingSet bindings,
			StrictEvaluationStrategy strategy, QueryInfo queryInfo) {
//org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
//		Logger.getRootLogger().setLevel(Level.OFF);
//org.apache.log4j.Logger.getLogger("com.fluidops.fedx.optimizer.Optimizer").setLevel(Level.OFF);

//logger.trace(
//				"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&6Optimization&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

		FedX fed = queryInfo.getFederation();
		members = queryInfo.getFedXConnection().getEndpoints();
		// for(Endpoint m :members)
		// //logger.info("These are the members in Optimisers:"+m);
		// if the federation has a single member only, evaluate the entire query there
		// if (members.size() == 1 && queryInfo.getQuery() != null)
		// return new SingleSourceQuery(parsed, members.get(0), queryInfo);

		// Clone the tuple expression to allow for more aggressive optimizations
		query = new QueryRoot(parsed.clone());
		// TupleExpr query1 = new QueryRoot(parsed.clone());
		// logger.trace("88787878787878787878787878787878787878787878787 Before
		// Optimizer:" + queryInfo + "--"
		// + query + "--" + bindings);

		Cache cache = fed.getCache();

		// if (logger.isTraceEnabled())
		// logger.trace("Query before Optimization: " + query);

		/* original sesame optimizers */
		// new ConstantOptimizer(strategy).optimize(query, dataset, bindings); // maybe
		// remove this optimizer later

//		new DisjunctiveConstraintOptimizer().optimize(query, dataset, bindings);

		// new ConstantOptimizer(strategy).optimize(query, dataset, bindings); // maybe
		// remove this optimizer later

		// new DisjunctiveConstraintOptimizer().optimize(query, dataset, bindings);

		/*
		 * TODO add some generic optimizers: - FILTER ?s=1 && ?s=2 => EmptyResult -
		 * Remove variables that are not occurring in query stmts from filters
		 */

		/* custom optimizers, execute only when needed */
		ForkJoinPool fjp = new ForkJoinPool();
		fjp.submit(() -> info = new GenericInfoOptimizer(queryInfo));

		fjp.shutdown();
		// collect information and perform generic optimizations
		ForkJoinPool fjp1 = new ForkJoinPool();
		fjp1.submit(() -> info.optimize(query));
		fjp1.shutdown();
		// Source Selection: all nodes are annotated with their source
		long srcTime = System.currentTimeMillis();
		//// logger.info("These are the members in Optimisers1:"+members);
//	ForkJoinPool fjp2 = new ForkJoinPool(6);
//fjp2.submit(()->		
		Stream.of(sourceSelection = (SourceSelection) Util.instantiate(fed.getConfig().getSourceSelectionClass(),
				members, cache, queryInfo)).parallel();
//fjp2.shutdown();
		// Class.forName(Config.getSourceSelectionClass()).newInstance();
		// SourceSelection sourceSelection = new SourceSelection(members, cache,
		// queryInfo);
		// //logger.info("This is before optimization:" + info.getStatements());
		// for(List<StatementPattern> i: info.getStatements())
		// //logger.info("These are the members in Optimisers1:"+i);
		ForkJoinPool fjp3 = new ForkJoinPool();
		try {
			fjp3.submit(() -> sourceSelection.performSourceSelection(info.getStatements())).get();
		} catch (InterruptedException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (ExecutionException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		fjp3.shutdown();
		// //logger.info("This is before optimization:"+info.getStatements());
		// long srcSelTime = System.currentTimeMillis() - srcTime;
		// queryInfo.getSourceSelection().time = srcSelTime;
//		//logger.info("Source Selection Time " + (srcSelTime));

//	    try {
//			QueryEvaluation.bw.write(""+srcSelTime);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}

		/*
		 * int tpsrces = 0; for (Entry<StatementPattern, List<StatementSource>> es :
		 * sourceSelection.getStmtToSources().entrySet()) { tpsrces = tpsrces +
		 * es.getValue().size(); // //logger.info("-----------\n"+stmt); //
		 * //logger.info(SourceSelection.stmtToSources.get(stmt)); } logger.
		 * info("Total Triple pattern-wise selected sources after step 2 of FedSum source selection : "
		 * + tpsrces);
		 */
//		//logger.info("Source Selection Time "+ (System.currentTimeMillis()-srcTime) );	
//		
//		FedSumSourceSelection fsourceSelection = new FedSumSourceSelection(members,cache,queryInfo);
//		try {
//			long FedSumTime = System.currentTimeMillis();
//			  Map<StatementPattern, List<StatementSource>> stmtToSources=	fsourceSelection.performSourceSelection(QueryEvaluation.DNFgrps);
//			//logger.info("Source Selection Time "+ (System.currentTimeMillis()-FedSumTime) );	
//			  int tpsrces = 0; 
//				for (StatementPattern stmt : stmtToSources.keySet()) 
//		          {
//		        	tpsrces = tpsrces+ stmtToSources.get(stmt).size();
//					//logger.info("-----------\n"+stmt);
//					//logger.info(stmtToSources.get(stmt));
//		         }
//			      //logger.info("Total Triple pattern-wise selected sources after step 2 of FedSum source selection : "+ tpsrces);
//			
//		} catch (RepositoryException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MalformedQueryException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (QueryEvaluationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// if the query has a single relevant source (and if it is no a SERVICE query),
		// evaluate at this source only

		//// logger.info(
		// "90909090909090909090909090909090909090909090StatementGroupOptmization in
		//// Optimizer--------------------------------- ");

		List<Triple> StatementPatternArray = new ArrayList<>();
		List<Triple> TripleArray;
		// ArrayList<String> EndpointArray = new ArrayList<>();
		ConcurrentHashMap<StatementPattern, List<StatementSource>> stmtToSources = new ConcurrentHashMap<StatementPattern, List<StatementSource>>();
//	for (Endpoint epointname : sourceSelection.getRelevantSources()) {
//			EndpointArray.add(epointname.getEndpoint());
//			//logger.info(
//					"111536456456!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------FedXConnection"
//							+ "--" + epointname.getName() + "--" + epointname.getEndpoint());

//		}

		for (Endpoint epointname : sourceSelection.getRelevantSources()) {
			EndpointArrayE.add(epointname);
			// //logger.info(
			// "111536456456!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------FedXConnection"
			// + "--" + epointname.getName() + "--" + epointname.getEndpoint() + "--"
			// +
			// stmtToSources.entrySet()+"--"+stmtToSources.values()+"--"+stmtToSources.keySet());
		}
		ForkJoinPool fjp6 = new ForkJoinPool();
		sourceSelection1 = fjp6.submit(() -> SourceSelection.getStmtToSources()).join();
		fjp6.shutdown();
		// for( Entry<StatementPattern, List<StatementSource>>
		// e:SourceSelection.getStmtToSources().entrySet())
		// { for(StatementSource f:e.getValue())
		// //logger.info("This is now a new headache:" +
		// e.getKey()+"--"+f.getEndpointID());
		// }

		// ReverseListIterator itr = new
		// ReverseListIterator(SourceSelection.getStmtToSources().entrySet());
		// List<String> reversedList =
		// Lists.reverse(SourceSelection.getStmtToSources().entrySet());
		/*
		 * for (int i = keyList.size() - 1; i >= 0; i--) { //logger.info("Key :: " +
		 * keyList.getKey(i)); }
		 */
		// getting keySet() into Set
		// Set<StatementPattern> set = SourceSelection.getStmtToSources().keySet();

		// get Iterator from key set
		// Iterator<StatementPattern> itr = set.iterator();

		// List<StatementPattern> alKeys = new
		// ArrayList<StatementPattern>(SourceSelection.getStmtToSources().keySet());

		// reverse order of keys
		// Collections.reverse(alKeys);

		// iterate LHM using reverse order of keys
		// for(StatementPattern strKey : alKeys){
		// //logger.info("Key : " + strKey + "\t\t"
		// + "Value : " + SourceSelection.getStmtToSources().get(strKey));
		// stmt= stmts.getKey();
		// sources = stmts.getValue();
		// }
//ForkJoinPool fjp12 = new ForkJoinPool(6);
//fjp12.submit(()->

		new JoinOrderOptimizer2(queryInfo, sourceSelection1).meetOther(query);// );
		qInfo = queryInfo;

		for (Entry<LinkedHashMap<Double, Double>, LinkedHashMap<Integer, ArrayList<HashMap<Var, Var>>>> e : JoinOrderOptimizer2.JoinVar
				.entrySet()) {

			// for(Entry<LinkedHashMap<Double, Double>, TupleExpr>
			// e1:JoinOrderOptimizer2.Join.entrySet())
			// {
			// for(Entry<Double, Double> f:e.getKey().entrySet())
			// //logger.info("These are the optimizsers in
			// BGPEval:"+e1.getValue()+"--"+e1.getKey());
			// //logger.info("-------------------------------------------------------------------------------------------------------");

			// }
			//// logger.info("These are the optimizsers in
			// BGPEval:"+JoinOrderOptimizer2.JoinVar.entrySet());

			{
				// for(Entry<Double, Double> f:e.getKey().entrySet())
				// //logger.info("These are the optimizsers in
				// BGPEval:"+e.getValue()+"--"+e.getKey());
				// //logger.info("-------------------------------------------------------------------------------------------------------");
				// for(Entry<Double, Double> JoinType:e.getKey().entrySet())

				int balance = 0;

				xyz = e.getValue();
				xyz1 = e.getKey();

			} // Entry<Double, Double> FirstType = e.getKey().entrySet().iterator().next();
			// Entry<Integer, ArrayList<Map<Var, Var>>> FirstVars =
			// e.getValue().entrySet().iterator().next();

			// }
//		while(e.getKey().entrySet().iterator().hasNext()) {
			// while(e.getValue().values().iterator().hasNext()) {
			// }

			//// logger.info("These are the seconds in new Type of
			//// Boundaries:"+SecondType+"--"+SecondVars);
			break;
		}
		// //logger.info("These are the firsts in new Type of Boundaries2:"+xyz1);

		for (Entry<Double, Double> f1 : xyz1.entrySet()) {
			balance1++;
			LinkedHashMap<Double, Double> v = new LinkedHashMap<>();
			v.put(f1.getKey(), f1.getValue());
			JoinValues.put(balance1, v);
			// //logger.info("These are the firsts in new Type of
			// Boundaries:"+f1+"--"+balance1);
		}
		for (ArrayList<HashMap<Var, Var>> f : xyz.values()) {

			balance++;
			for (HashMap<Var, Var> f1 : f) {
				JoinVars.put(balance + "--" + f1, f1);

			}
		}
		//// logger.info("These are the firsts in new Type of Boundaries:"+JoinVars);
		//// logger.info("These are the firsts in new Type of Boundaries:"+JoinValues);
		for (Entry<StatementPattern, List<StatementSource>> stmts : SourceSelection.getStmtToSources().entrySet()) {
			System.out.println("These are the statement and sources:" + stmts);
		}

		for (Entry<StatementPattern, List<StatementSource>> stmts : SourceSelection.getStmtToSources().entrySet()) {
			// for(StatementPattern strKey : alKeys){
			stmt = stmts.getKey();
			// strKey;
			sources = stmts.getValue();
			// SourceSelection.getStmtToSources().get(strKey);
			// //logger.info("This is the stmt:"+stmt);
			int counter = 0;
			String equal = "";
			for (Entry<String, Set<String>> stmts1 : JoinOrderOptimizer2.joinType.entries()) {
				
					for (String st : stmts1.getValue()) {
					//	System.out.println("This is stmt in var:"+ stmt.getSubjectVar().getName()+"--"+st+"--"+ stmt.getObjectVar().getName());
						if ((stmt.getSubjectVar().getName().equals(st) || stmt.getObjectVar().getName().equals(st))
								&& !equal.equals(st)) {
							counter++;
							equal = st;
						}
						if (counter == 2) {
							triples[i][7]=stmts1.getKey();
							break;
						}
					}
				
				if (counter == 2) {
					break;
				}
			}

			subobj.put(stmt.getSubjectVar().getName(), stmt.getObjectVar().getName());
			subjectCount1.put(stmt.getSubjectVar().getName() + "--" + stmt.getObjectVar().getName(),
					((Summary) (queryInfo.getFedXConnection().getSummary())).getTriplePatternSubjectMVKoef(stmt,
							sources));
			objectCount1.put(stmt.getSubjectVar().getName() + "--" + stmt.getObjectVar().getName(),
					((Summary) (queryInfo.getFedXConnection().getSummary())).getTriplePatternObjectMVKoef(stmt,
							sources));
			tripleCount1.put(stmt.getSubjectVar().getName() + "--" + stmt.getObjectVar().getName(),
					((Summary) (queryInfo.getFedXConnection().getSummary())).getTriplePatternCardinality(stmt,
							sources));
			/*
			 * //logger.
			 * info("This is the blunder that is troubling me222222222222222222222222222222222222:"
			 * + ((Summary)
			 * (queryInfo.getFedXConnection().getSummary())).getTriplePatternSubjectMVKoef(
			 * stmt, sources) + "--" + ((Summary)
			 * (queryInfo.getFedXConnection().getSummary())).getTriplePatternObjectMVKoef(
			 * stmt, sources) + "--" + ((Summary)
			 * (queryInfo.getFedXConnection().getSummary())).getTriplePatternCardinality(
			 * stmt, sources) + "--" + stmt.getSubjectVar().getName() + "--" +
			 * stmt.getObjectVar().getName() + "--" +
			 * SourceSelection.getStmtToSources().entrySet());
			 */
			if (stmt.getSubjectVar().isAnonymous())
				triples[i][0] = stmt.getSubjectVar().getValue().toString();

			else
				triples[i][0] = (String) stmt.getSubjectVar().getName();// (new
			if (stmt.getPredicateVar().isAnonymous())
				triples[i][6] = stmt.getPredicateVar().getValue().toString();
			else
				triples[i][6] = (String) stmt.getPredicateVar().getName();// (new
			if (stmt.getObjectVar().isAnonymous())
				triples[i][1] = stmt.getObjectVar().getValue().toString();
			else
				triples[i][1] = stmt.getObjectVar().getName();// (new
			triples[i][8] = sources;
			Set<Endpoint> srcs = new HashSet<>();
			for (StatementSource s : sources) {
				srcs.add(EndpointManager.getEndpoint(s.getEndpointID()));
			}
			EndpointRef.put(triples[i][0] + "--" + triples[i][1], srcs);
	/*		for (Entry<String, HashMap<Var, Var>> e : JoinVars.entrySet())
				for (Entry<Var, Var> f : e.getValue().entrySet()) {
					System.out.println("This is the joined var with stmt:" + f.getKey() + "--" + f.getValue() + "--"
							+ stmt.getSubjectVar() + "--" + stmt.getObjectVar());

					if (f.getKey().equals(stmt.getSubjectVar()) && f.getValue().equals(stmt.getObjectVar())) {

						for (Entry<Integer, LinkedHashMap<Double, Double>> jv : JoinValues.entrySet()) { //// logger.info("This
																											//// is the
																											//// joined
																											//// value
																											//// with
																											//// stmt0000:"+e.getKey()+"--"+jv.getKey());

							if (Integer.parseInt(StringUtils.substringBefore(e.getKey().toString(), "-")) == jv
									.getKey()) {
								System.out.println("This is the joined value with stmt:" + f + "--" + jv.getValue());
								for (Entry<Double, Double> jt : jv.getValue().entrySet()) {
									// System.out.println("This is the size here:"+jt);
									if (jt.getKey() < jt.getValue()) {
										// logger.info("HashJoin");

										triples[i][7] = "HashJoin";// (new
									} else {
										// logger.info("BindJoin");

										triples[i][7] = "BindJoin";// (new

									}
								}
							}
						}

					}
				}*/
			// StringBuilder()).append("?").append(stmt.getObjectVar().getName());
			triples[i][2] = (double) ((Summary) (queryInfo.getFedXConnection().getSummary()))
					.getTriplePatternSubjectMVKoef(stmt, sources);
			triples[i][3] = (double) ((Summary) (queryInfo.getFedXConnection().getSummary()))
					.getTriplePatternObjectMVKoef(stmt, sources);
			triples[i][4] = (double) ((Summary) (queryInfo.getFedXConnection().getSummary()))
					.getTriplePatternCardinality(stmt, sources);
			System.out.println("These are the values of triples:" + triples[i][0] + "--" + triples[i][6] + "--"
					+ triples[i][1] + "--" + triples[i][7]);

			// sCount.put(triples[i][0]+"--"+triples[i][1],
			// ((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternSubjectMVKoef(stmt,
			// sources)-(int)triples[i][2]);
			// oCount.put(triples[i][0]+"--"+triples[i][1],
			// ((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternObjectMVKoef(stmt,
			// sources)-(int)triples[i][3]);
			// tCount.put(triples[i][0]+"--"+triples[i][1], (long)
			// (((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternCardinality(stmt,
			// sources)-(int)triples[i][4]));
			// //logger.info("This is the blunder that is troubling
			// me666666:"+m+"--"+(int)triples[i][2]+"--"+((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternSubjectMVKoef(stmt,
			// sources)+"--"
			// +n+"--"+(int)triples[i][3]+"--"+((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternObjectMVKoef(stmt,
			// sources)
			// +"--"+o+"--"+(int)triples[i][4]+"--"+((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternCardinality(stmt,
			// sources)+"--"+(m-(int)triples[i][2])+"--"+(n-(int)triples[i][2])+"--"+(o-(int)triples[i][4]));

			i++;
			// for(StatementSource s:sources) {
			// }

			// sources = stmts.getValue();
			// ids = getEndpoints();
			// ids1=((Summary)(queryInfo.getFedXConnection().getSummary())).lookupSources(stmt);
			// //logger.info("This is now in statementSources:"+sources1);
			/*
			 * if (ids != null && !ids.isEmpty()) { //synchronized (sources) { for (String
			 * id : ids) { sources.add(new StatementSource(id, StatementSourceType.REMOTE));
			 * //logger.info("These are the sources:"+sources);
			 * 
			 * } for(StatementSource source:sources) { es1 = new
			 * ExclusiveStatement(stmt,source,queryInfo); expr.add(es1);
			 * //logger.info("This is now where I am stuck:"+es1+"--"+sources+"--"+expr ); }
			 */
			// }

			// }
			/*
			 * if (ids != null && !ids.isEmpty()) { synchronized (sources) { for (String id
			 * : ids) { sources.add(new StatementSource(id, StatementSourceType.REMOTE));
			 * //logger.info("These are the sources:"+sources);
			 * 
			 * } ExclusiveStatement es1 = new ExclusiveStatement(stmt,source,queryInfo);
			 * expr.add(es1);
			 * ////logger.info("This is now where I am stuck:"+es1+"--"+source+"--"+ expr);
			 * 
			 * 
			 * } }
			 */
//	 				for(StatementSource source:stmts.getValue()) {
//	 				ExclusiveStatement es1 = new ExclusiveStatement(stmt,stmt.getStatementSource(),queryInfo);
//	 				expr1.add(es1);
//	 				//logger.info("This is now where I am stuck:"+expr1+"--"+source);
//	 		//		
//	 				}
			// //logger.info("LHD has started22---------------------------------
			// \n"+"$$$$"+queryInfo+"$$$$"+stmt+"$$$$"+sources+"$$$$$"+((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternSubjectMVKoef(stmt,
			// sources)+"--"+((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternObjectMVKoef(stmt,
			// sources)+"--"+((Summary)(queryInfo.getFedXConnection().getSummary())).getTriplePatternCardinality(stmt,
			// sources));

		}

		// }

		// for (int j = 0; j < i; j++)
		// for(int k=0;k<6;k++) {
		// System.out.print("This is the blunder that is troubling
		// me33333333333333333333:" + triples[j][0] + "--"
		// + triples[j][1] + "--" + triples[j][2] + "--" + triples[j][3] + "--" +
		// triples[j][4]);
		// logger.info("");

		// FedXOptimizer statementGroupOptimizer = (FedXOptimizer) Util
		// .instantiate(fed.getConfig().getStatementGroupOptimizerClass(), queryInfo);
		// NJoin node = new NJoin(expr,queryInfo);
		//
		// new StatementGroupOptimizer(queryInfo).meetOther(query);
		binding = bindings;
		// //logger.info(
		// "7878787878787878787878787878787878787878787878787878787878787878StatementGroupOptmization
		// in Optimizer2--------------------------------- "
		// + query + "--" + binding);

		// StatementGroupOptimizer a = new
//		//logger.info(
//				"7 This is the value of StatementToSources before: "
//						+ sourceSelection1);

		// System.out.println("88787878787878787878787878787878787878787878787 After
		// Optimizer:" + queryInfo + "--" + query);

		ForkJoinPool fjp13 = new ForkJoinPool();
		fjp13.submit(() ->

		new StatementGroupOptimizer2(queryInfo).meetOther(query));
		fjp13.shutdown();
		long k = 0;

		Set<Endpoint> relevantSources = sourceSelection.getRelevantSources();
//		if (relevantSources.size() == 1 && !info.hasService()) {
//			//logger.info("This is now in Optimizer1:");
//			return new SingleSourceQuery(query, relevantSources.iterator().next(), queryInfo);
//		}
		// if (info.hasService()) {
		// //logger.info("This is now in Optimizer2:");
		// new ServiceOptimizer(queryInfo).optimize(query);
		// }
		// optimize Filters, if available
		// if (info.hasFilter()) {
		// //logger.info("This is now in Optimizer3:");
		// new FilterOptimizer().optimize(query);
		// }
		// optimize unions, if available
		// if (info.hasUnion) {
		// //logger.info("This is now in Optimizer4:");
		// new UnionOptimizer(queryInfo).optimize(query);
		// } // optimize statement groups and join order

		// //logger.info(
		// "7878787878787878787878787878787878787878787878787878787878787878StatementGroupOptmization
		// in Optimizer1--------------------------------- "
		// + query);

//		FedXOptimizer statementGroupOptimizer = (FedXOptimizer)Util.instantiate(fed.getConfig().getStatementGroupOptimizerClass(), queryInfo);
//	new StatementGroupOptimizer(queryInfo).optimize(query);
		// //logger.info(
		// "7878787878787878787878787878787878787878787878787878787878787878StatementGroupOptmization
		// in Optimizer--------------------------------- "
		// + query);

		// sgo.optimize(query);
		// new StatementGroupOptimizer(queryInfo).optimize(query);
		/*
		 * try { Thread.sleep(450); } catch (InterruptedException e1) { // TODO
		 * Auto-generated catch block e1.printStackTrace(); }
		 */

		// new VariableScopeOptimizer(queryInfo).optimize(query);

		return query;
	}

	public static void setSources(List<List<StatementPattern>> query) {

		for (List<StatementPattern> stmts : query) {
			for (StatementPattern stmt : stmts) {
				StatementPatternArray.add(stmt.toString());
				// //logger.info("1414141414141414141414141414 These are now StatementPattern:"
				// + StatementPatternArray + "--" + stmt);

			}

		}
	}

	/*
	 * public static void setTriples(List<List<StatementPattern>> query, QueryInfo
	 * queryInfo, FedX fed) { Optimizer opt = new Optimizer(); QueryProvider qp =
	 * new QueryProvider("../queries/"); // SailRepository repo
	 * =repo.getConnection(). //
	 * FedXFactory.initializeSparqlFederation(fed.getConnection(), //
	 * opt.getEndpoints()); for (List<StatementPattern> stmts : query) { for
	 * (StatementPattern stmt : stmts) { List<String> qnames =
	 * Arrays.asList(QueryStringUtil.toString(stmt)); for (String curQueryName :
	 * qnames) { curQuery.add((curQueryName)); //logger.
	 * info("--------------------12312321423423423This is the query final war: " +
	 * queryInfo.getQuery() + "---" + QueryStringUtil.toString(stmt) + "--" +
	 * curQueryName); //
	 * 
	 * // repo = ; // TupleQuery query1 = //
	 * repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, curQuery); // /
	 * //logger.
	 * info("--------------------12312321423423423This is the query final war: "
	 * +query1+"---"+QueryStringUtil.toString(stmt )); // } // catch (Throwable e) {
	 * // e.printStackTrace(); // log.error("", e); // } }
	 * 
	 * // //logger.info(stmt. ); // NodeFactory. //
	 * //logger.info("1111111111111111111111111111111111111111111111Here are // the
	 * nodes now "+NodeFactory.parseNode(stmt.getSubjectVar().toString()) //
	 * +"--"+NodeFactory.parseNode(stmt.getPredicateVar().getValue().toString())+
	 * "--"+NodeFactory.parseNode(stmt.getObjectVar().getValue().toString())); //
	 * //logger.info(); // String a=stmt.getSubjectVar().toString(); /* Triple t =
	 * Triple.create( NodeFactory.create(stmt.getSubjectVar()),
	 * NodeFactory.create(stmt.getPredicateVar().toString()),
	 * NodeFactory.create(stmt.getObjectVar().getValue().toString()));
	 */

	// Triple list; //= new ArrayList<>();
	// Node subject =
	// NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein");
	// List<Binding> bindings;
	// bindings.add(stmt.getSubjectVar().toString());
	// Triple newtriple = new
	// Triple(stmt.getSubjectVar().toString(),stmt.getPredicateVar().toString(),stmt.getObjectVar().toString());
	/*
	 * list.add( Triple.create( NodeFactory.createURI("(triple ?president"),
	 * NodeFactory.createURI( "<http://dbpedia.org/ontology/director>"),
	 * NodeFactory.createURI("?director )")));
	 */ /*
		 * list.add(Triple.create(
		 * NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein"),
		 * NodeFactory.createURI("http://dbpedia.org/ontology/birthDate"),
		 * NodeFactory.createLiteral("1879-03-14", XSDDatatype.XSDdate)));
		 * 
		 * list.add(Triple.create(
		 * NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein"),
		 * RDF.type.asNode(),
		 * NodeFactory.createURI("http://dbpedia.org/ontology/Physican")));
		 * list.add(Triple.create(
		 * NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein"),
		 * RDF.type.asNode(),
		 * NodeFactory.createURI("http://dbpedia.org/ontology/Musican")));
		 * list.add(Triple.create(
		 * NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein"),
		 * NodeFactory.createURI("http://dbpedia.org/ontology/birthDate"),
		 * NodeFactory.createLiteral("1879-03-14", XSDDatatype.XSDdate)));
		 * 
		 * // list =Triple.create(Triple.create(("u:John"), // ("u:parentOf"), //
		 * ("u:Mary")));//"(project (?president ?party ?page)\r\n" + // " (bgp\r\n" + //
		 * " This is now doing the conversion: (triple ?president //
		 * <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> //
		 * <http://dbpedia.org/ontology/President>)\r\n" + // " (triple ?president
		 * <http://dbpedia.org/ontology/nationality> //
		 * <http://dbpedia.org/resource/United_States>)\r\n" + //
		 * " (triple ?president <http://dbpedia.org/ontology/party> ?party)\r\n" + //
		 * " (triple ?x <http://data.nytimes.com/elements/topicPage> ?page)\r\n" + //
		 * " (triple ?x <http://www.w3.org/2002/07/owl#sameAs> ?president)\r\n" + //
		 * " ))");
		 * 
		 * // StageGen gen = new StageGen();
		 * 
		 * // gen.make((Triple)list);//"[?president
		 * http://dbpedia.org/ontology/President, //
		 * ?president @http://dbpedia.org/ontology/nationality //
		 * http://dbpedia.org/resource/United_States, ?president
		 * // @http://dbpedia.org/ontology/party ?party, ?x
		 * // @http://data.nytimes.com/elements/topicPage ?page, ?x @owl:sameAs //
		 * ?president]");
		 * 
		 * }
		 * 
		 * } }
		 */

	public static List<String> getSources() {
		// logger.info(
		// "1313131313131313311313131313131These are now the converted sources: " +
		// StatementPatternArray);
		return StatementPatternArray;
	}

	// public static Collection<Triple> getTriple()
//	{
//		return TripleArray;
//	}

	public void setSubjectCount(double scount) {
		subjectCount = scount;
	}

	public void setObjectCount(double ocount) {
		objectCount = ocount;
	}

	public void setTripleCount(long tcount) {
		tripleCount = tcount;
	}

	public double getSubjectCount() {

		return subjectCount;
	}

	public double getObjectCount() {
		return objectCount;
	}

	public long getTripleCount() {
		return tripleCount;
	}

	public static Set<String> getEndpoints() {
		// //logger.info("1313131313131313311313131313131These are now the converted
		// endpoint: " + EndpointArray);
		return EndpointArray;
	}

	public Set<Endpoint> getEndpointE(Triple t) {
		// for(Entry<String, Set<Endpoint>> r:EndpointRef.entrySet()) {
		// System.out.println("These are the
		// conditionals:"+r+"--"+(t.getSubject().toString().substring(1)+"--"+t.getObject().toString().substring(1)));
		// }

		for (Entry<String, Set<Endpoint>> r : EndpointRef.entrySet()) {
			if ((t.getSubject().toString().substring(1) + "--" + t.getObject().toString().substring(1))
					.equals(r.getKey()))
				return r.getValue();
		}
		// for(Object ti:triples[i][8].)
		return null;

		// System.out.println("1313131313131313311313131313131These are now the
		// converted endpoint: " + EndpointArrayE);

	}

	public static void checkExclusiveGroup(List<ExclusiveStatement> exclusiveGroupStatements) {
		// by default do nothing
	}

	public BindingSet getBindings() {
		return binding;
	}
}

/*
 * public static ArrayList<String> getEndpoints(List<List<StatementPattern>>
 * query) { ConcurrentHashMap<StatementPattern, List<StatementSource>>
 * stmtToSources = new ConcurrentHashMap<StatementPattern,
 * List<StatementSource>>();
 * 
 * for (List <StatementPattern> stmts : info.getStatements()) { for
 * (StatementPattern stmt : stmts) { stmtToSources.put(stmt, new
 * ArrayList<StatementSource>());
 * 
 * } }
 * 
 * for (Map.Entry<StatementPattern, List<StatementSource>> stmts :
 * stmtToSources.entrySet()) { StatementPattern stmt = stmts.getKey();
 * List<StatementSource> sources = stmts.getValue();
 * 
 * //logger.info("LHD has started22---------------------------------  \n"+(
 * (Summary)(queryInfo.getFedXConnection().getSummary())).
 * getTriplePatternSubjectMVKoef(stmt,
 * sources)+"--"+((Summary)(queryInfo.getFedXConnection().getSummary())).
 * getTriplePatternObjectMVKoef(stmt,
 * sources)+"--"+((Summary)(queryInfo.getFedXConnection().getSummary())).
 * getTriplePatternCardinality(stmt, sources));
 * 
 * 
 * 
 * }
 * 
 * // return StatementPatternArray; }
 */
