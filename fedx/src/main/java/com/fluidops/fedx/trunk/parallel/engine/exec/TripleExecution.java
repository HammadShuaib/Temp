package com.fluidops.fedx.trunk.parallel.engine.exec;

import java.util.Vector;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fluidops.fedx.structures.QueryInfo;

import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
import org.eclipse.rdf4j.query.algebra.StatementPattern;

//import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.description.RemoteService;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.parallel.engine.ParaConfig;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.BindJoin;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import com.fluidops.fedx.trunk.parallel.engine.opt.ExhOptimiser;
import com.fluidops.fedx.trunk.stream.engine.util.QueryUtil;
//import com.fluidops.fedx.trunk.stream.engine.util.HypTriple;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.graph.Triple;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.optimizer.Optimizer;
import com.fluidops.fedx.optimizer.SourceSelection;
import com.fluidops.fedx.structures.Endpoint;

public class TripleExecution {

	// private static final org.apache.log4j.Logger log =
	// LogManager.getLogger(TripleExecution.class.getName());
	static int i11 = 0;
	static int i = 0;

	QueryTask current1;

	private Triple triple;
	static public List<Binding> results = Collections.synchronizedList(new ArrayList<Binding>());
	public static Integer taskcounter = 0;
	public int blocksize = 40;
	ReentrantLock lock = new ReentrantLock();

//	Stream<BoundQueryTask> x ;
	BoundQueryTask current3;
	static int IterationNumber = -1;
	static ConcurrentHashMap<List<Endpoint>, TripleExecution> processedEndPoint = new ConcurrentHashMap<>();

	public TripleExecution(Triple triple) {
		// log.info("This is now in TripleExecution1");

		this.triple = triple;
	}

	public synchronized void addBindings(Collection<Binding> bindings) {
		// synchronized (results) {
		results.addAll(bindings);
	}

	public boolean isFinished() {
		// log.info("This is now in TripleExecution3:" + taskcounter);

		// synchronized (taskcounter) {
		if (taskcounter == 0) {
			// log.info("TE333333 Task Counter:" + true);

			return true;
		} else {
			// log.info("TE333333 Task Counter:" + false);
			return false;
		}
		// }

	}

	public int taskMinus() {
//	synchronized (taskcounter) {
		taskcounter--;
		// }

		return 0;
	}

	public Triple getTriple() {
//		log.info("This is now in TripleExecution5");

		return triple;
	}

	public List<Binding> exec(List<Binding> intermediate, String string) {
		results  = new ArrayList<>();
		//// System.out.println("33333333333Subsubsubsubsubs:");
		//// System.out.println("this is exclusiely exclusive:"+
		//// BGPEval.ExclusivelyExclusive);
		// task=string;
//		org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
//		Logger.getRootLogger().setLevel(Level.OFF);
//org.apache.log4j.Logger.getLogger("com.fluidops.fedx.trunk.parallel.engine.exec.TripleExecution").setLevel(Level.OFF);

		// log.info("This is now in TripleExecution6");
		// static Vector<String> EndpointArray = new Vector<>();
		Optimizer opt = new Optimizer();
		// log.info("TE6666666666:"+opt.getEndpointE());

		// opt.getEndpoints();
		// List<RemoteService> services = triple.getServices();
//		if(intermediate!=null)
		//// System.out.println("This is value of intermediate:"+triple);
		if (intermediate == null) {
//			//System.out.println("This is value of intermediate2222:"+triple);

			Set<Endpoint> relevantSources;
			relevantSources = opt.getEndpointE(triple);

			for (Endpoint endpoints : relevantSources) {
				taskcounter++;
			}
			// IterationNumber++;
			// log.info("This is in Triple Execution service size:"
			// +relevantSources.size()+"--"+triple+"--"+taskcounter+"--"+IterationNumber);
			for (Endpoint endpoints : relevantSources) {

				int k = 0;

				if (intermediate == null) {
					// System.out.println("This is value of intermediate4444444:"+triple +"--"+
					// BGPEval.IsRightQuery+"--"+BGPEval.IsLeftQuery);

					if (BGPEval.OptionalQuery == false && BGPEval.IsLeftQuery == false
							&& BGPEval.IsRightQuery == false) {
						// System.out.println("This is here in
						// intermediateeeeeeeeeeee33333333333333333333:"+triple +"--"+
						// BGPEval.IsRightQuery+"--"+BGPEval.IsLeftQuery);
						ForkJoinPool ex = new ForkJoinPool();
						ex.submit(() -> {
//								//System.out.println("This is in performance for:"+this+"--"+endpoints);

							current1 = new QueryTask(this, endpoints);
							try {

								current1.runTask();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}).join();
	//					ForkJoinTask.invokeAll();
						// ForkJoinTask.invokeAll();

						/*
						 * try { Thread.sleep(500); } catch (InterruptedException e) { // TODO
						 * Auto-generated catch block e.printStackTrace(); }
						 */

						ex.shutdown();

					} else {
//					log.info("This is here in intermediateeeeeeeeeeee2222222222222222222222:"+triple);					

						ForkJoinPool ex = new ForkJoinPool();
						ex.submit(() -> {
						System.out.println("This is in performance for:"+this+"--"+endpoints+"--"+LocalTime.now());
							current1 = new QueryTask(this, endpoints);
							System.out.println("This is in performance end for:"+this+"--"+endpoints+"--"+LocalTime.now());
							
							try {
								current1.runTask();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						});
//						ForkJoinTask.invokeAll();

						ex.shutdown();	

					}
				}
			}
		} else {
			if (intermediate.isEmpty()) {

				return new ArrayList<Binding>();
			}
			Set<Endpoint> relevantSources1 = opt.getEndpointE(triple);
			long start = System.currentTimeMillis();

			List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
			TripleExecution temp = this;
			ForkJoinPool exec = new ForkJoinPool();

			for (Endpoint endpoints : relevantSources1) {
				System.out.println("These are the endpoints:"+endpoints+"--"+temp);
				
				for (Binding binding : intermediate) {
					Callable<Integer> c = new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							 System.out.println("This is the binding in intermediate within call:"+temp+"--"+endpoints);

							BoundQueryTask current3 = new BoundQueryTask(temp, endpoints, binding, string);
							try {
								current3.runTask();
								return 1;

							} catch (InterruptedException e) {
								e.printStackTrace();
								return 0;
							}
						}
					};
					tasks.add(c);
				}
			
				//try {
					List<Future<Integer>> results = exec.invokeAll(tasks);
					long elapsed = System.currentTimeMillis() - start;
					System.out.println(String.format("Total time elapsed:%d ms", elapsed));
			//	} finally {
			//	}
			}
			
			exec.shutdown();
			

		}

		System.out.println("Results.size()" + results.size());
		return results;
	}

	@Override
	public String toString() {
		return triple.toString();
	}

	public boolean EndPointMatch(Triple triples, Endpoint service) {
		List<EdgeOperator> BushyTreeTriple2 = new Vector<EdgeOperator>();
		List<List<EdgeOperator>> JoinGroups = new Vector<>();
		Set<Integer> CurrentEndPoints = new HashSet<>();
		// if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION"))
		JoinGroups.addAll(BGPEval.JoinGroupsListOptional);
		JoinGroups.addAll(BGPEval.JoinGroupsListExclusive);
		JoinGroups.add(BGPEval.JoinGroupsListRight);
		JoinGroups.add(BGPEval.JoinGroupsListLeft);
		JoinGroups.add(BGPEval.JoinGroupsListRightOptional);
		JoinGroups.add(BGPEval.JoinGroupsListLeftOptional);

		// JoinGroups.remove(ae);

		Map<List<EdgeOperator>, Integer> JoinGroupsUlt = new LinkedHashMap<>();
		ArrayListMultimap<EdgeOperator, Integer> JoinGroupsUlt1 = ArrayListMultimap.create();

		if (BGPEval.OptionalQuery == false && BGPEval.IsLeftQuery == false && BGPEval.IsRightQuery == false)
			JoinGroupsUlt.putAll(BGPEval.joinGroups2);
		else if (BGPEval.OptionalQuery == false && BGPEval.IsLeftQuery == true && BGPEval.IsRightQuery == false)
			for (Entry<EdgeOperator, Integer> jgu1 : BGPEval.joinGroupsLeft.entries())
				JoinGroupsUlt1.put(jgu1.getKey(), jgu1.getValue());
		else if (BGPEval.OptionalQuery == false && BGPEval.IsLeftQuery == false && BGPEval.IsRightQuery == true)
			for (Entry<EdgeOperator, Integer> jgu1 : BGPEval.joinGroupsRight.entries())
				JoinGroupsUlt1.put(jgu1.getKey(), jgu1.getValue());
		else if (BGPEval.OptionalQuery == true && BGPEval.IsLeftQuery == true && BGPEval.IsRightQuery == false)
			for (Entry<EdgeOperator, Integer> jgu1 : BGPEval.joinGroupsLeftOptional.entries())
				JoinGroupsUlt1.put(jgu1.getKey(), jgu1.getValue());
		else if (BGPEval.OptionalQuery == true && BGPEval.IsLeftQuery == false && BGPEval.IsRightQuery == true)
			for (Entry<EdgeOperator, Integer> jgu1 : BGPEval.joinGroupsRightOptional.entries())
				JoinGroupsUlt1.put(jgu1.getKey(), jgu1.getValue());

		else if (BGPEval.OptionalQuery == true && BGPEval.IsLeftQuery == false && BGPEval.IsRightQuery == false)
			JoinGroupsUlt.putAll(BGPEval.joinGroupsOptional1);

		Integer ghi = Integer.parseInt(service.getEndpoint().toString().replaceAll("[^0-9]", ""));
		// log.info("This is triple within endpoint:"+ghi);

		for (int i = 0; i < JoinGroups.size(); i++)
			if (JoinGroups.get(i).size() > 0)
				if (JoinGroups.get(i).get(0).getEdge().getTriple().toString().equals(triples.toString())) {
					{
						BushyTreeTriple2.addAll(JoinGroups.get(i));
						if (JoinGroupsUlt1.size() > 0) {

							for (Entry<EdgeOperator, Integer> jgu : JoinGroupsUlt1.entries()) {

								for (EdgeOperator btt : BushyTreeTriple2) {
									if (jgu.getKey().toString().equals(btt.toString()))
										CurrentEndPoints.add(jgu.getValue());

								}
							}
							if (!CurrentEndPoints.contains(ghi))
								return false;
						}

						if (JoinGroupsUlt.size() > 0) {
							for (Entry<List<EdgeOperator>, Integer> jgu : JoinGroupsUlt.entrySet())
								if (jgu.getKey().equals(BushyTreeTriple2)) {

									if (!jgu.getValue().toString().equals(ghi.toString())) {
										return false;
									}
								}

						}
						break;
					}
				}

		// public static Map< Integer,List<EdgeOperator>> joinGroups2= new
		// ConcurrentHashMap<>();
		// public static Map< Integer,EdgeOperator> joinGroupsLeft = new
		// ConcurrentHashMap<>();
		// public static Map< Integer,EdgeOperator> joinGroupsRight = new
		// ConcurrentHashMap<>();
		// public static Map< Integer,List<EdgeOperator>> joinGroupsOptional1 = new
		// HashMap<>();

		return true;

	}
}
