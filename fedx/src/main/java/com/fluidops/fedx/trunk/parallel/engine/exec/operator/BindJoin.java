package com.fluidops.fedx.trunk.parallel.engine.exec.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
//import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;

import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.error.RelativeError;
import com.fluidops.fedx.trunk.parallel.engine.error.TripleCard;
import com.fluidops.fedx.trunk.parallel.engine.exec.QueryTask;
import com.fluidops.fedx.trunk.parallel.engine.exec.TripleExecution;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.fluidops.fedx.trunk.stream.engine.util.QueryUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.sun.org.slf4j.internal.Logger;

import org.apache.commons.collections4.ListUtils;

public class BindJoin extends EdgeOperator {
	int remaining = 0;
	static List<EdgeOperator> AllEdges1 = new ArrayList<>();
	private int total;
	Var joinVars = null;
	 public static Multimap<Binding,Binding> MultipleBinding =  ArrayListMultimap.create();
	
	// public Stream<Binding> results1;
	public static List<org.apache.jena.sparql.engine.binding.Binding> intermediate = new ArrayList<>();
	static List<Binding> results = new ArrayList<Binding>();

	public BindJoin(Vertex s, Edge e) {
		super(s, e);
	}

	@Override
	public void exec() {
		results = new ArrayList<>();
		intermediate = new ArrayList<>();
		AllEdges1 =new ArrayList<>();
		System.out.println("These are intermediate0000:");

		Vertex end;
		if (start.equals(edge.getV1())) {
			end = edge.getV2();
		} else {
			end = edge.getV1();
		}
		// System.out.println("These are intermediate11111:"+StartBinding123);
		String x = start.getNode().toString().replaceAll("[^A-Za-z]+", "");
		int i = 0;
		for (Entry<Vertex, Set<Binding>> e : BGPEval.StartBinding123.entrySet()) {
			int y = x.compareTo(e.getKey().getNode().toString().replaceAll("[^A-Za-z]+", ""));
			System.out.println("This is comparison value:" + y);
			if (y == 0) {

				for (Binding e1 : e.getValue()) {
					intermediate.add((org.apache.jena.sparql.engine.binding.Binding) e1);

				}
				// System.out.println("These are
				// intermediate:"+intermediate.parallelStream().limit(2).collect(Collectors.toList()));
			}
			System.out.println("This is end 23 set Binding:" + x + "--"
					+ e.getKey().getNode().toString().replaceAll("[^A-Za-z]+", "") + "--"
					+ e.getValue().stream().limit(3).collect(Collectors.toList()) + "--" + edge + "--" + edge);
		}
		// List<List<Binding>> lists = ListUtils.partition(intermediate,
		// Math.round(intermediate.size() / 2) + 1);
		// twoThreads(lists);
		/*
		 * ExecutorService executor = Executors.newWorkStealingPool(); Future<?> result
		 * = executor.submit(() ->twoThreads(lists)); // future.get() Waits for the task
		 * to complete, and then retrieves its result. try { result.get(); } catch
		 * (InterruptedException e4) { // TODO Auto-generated catch block
		 * e4.printStackTrace(); } catch (ExecutionException e4) { // TODO
		 * Auto-generated catch block e4.printStackTrace(); } executor.shutdown();
		 */
	//	for (ConcurrentHashMap<Vertex, Edge> i1 : BGPEval.StartBindingFinal.keySet())
	//		System.out.println("These are intermediate size:" + i1);
	//	for (Binding i1 : intermediate)
	//		System.out.println("These are intermediate size:" + i1);
		results = te.exec(intermediate, null);
	//	for (Binding r : results)
	//		System.out.println("These are size of result in BindJoin:" + r);
		if (results == null || results.size() == 0) {
			results = te.exec(null, null);
			// for(Binding i1:results)
			// System.out.println("These are null result in bindjoin:"+i1);
		}
		for (List<EdgeOperator> e : BGPEval.JoinGroupsListExclusive) {
			for (EdgeOperator e1 : e)
				if (e1.getEdge().toString().equals(edge.toString())) {
					AllEdges1.addAll(e);
				}
		}

		for (EdgeOperator e : BGPEval.JoinGroupsListLeft) {
			System.out.println("This is equality in JoinGroupsListLeft:"+e.getEdge().toString()+"--"+edge.toString());
			if (e.getEdge().toString().equals(edge.toString())) {
				AllEdges1.add(e);
			}
		}

		for (EdgeOperator e1 : BGPEval.JoinGroupsListRight) {

			if (e1.getEdge().toString().equals(edge.toString())) {
				AllEdges1.add(e1);
			}
		}
//		Iterator<Var> vff = BindJoin.results.iterator().next().vars();
//		Var vf = null;
//			while (vff.hasNext()) {
//				 vf = vff.next();

//			}
			
	//	System.out.println("THis is the vertex of this query:"+vf);	
				// Multimap<String, String> cars = ArrayListMultimap.create();
	//	if (!BGPEval.StartBinding123.containsKey(start)) {
			System.out.println("This key not present:"+BindJoin.AllEdges1);
			ForkJoinPool fjp = new ForkJoinPool();
			try {
				fjp.submit(() -> HashJoin.IntermediateProcedure(BindJoin.results,BindJoin.AllEdges1)).get();
			} catch (InterruptedException e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
			} catch (ExecutionException e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
		//	}
		}
			fjp.shutdown();
		
		//	Iterator<Var> l = BindJoin.results.iterator().next().vars();
		//	Set<Var> v9=new HashSet<>();
		//		while (l.hasNext()) {
		//			Var v2 = l.next();
		//			v9.add(v2);
					
							
		//	}
			
				
				
			//	for(Var v1:v9)
			//		if(BGPEval.ProcessedVertexT.toString().contains(v1.toString()))
			//		{
				//		if(!Var.alloc(start.getNode()).toString().equals(v1.toString())) {
				//			Var va = Var.alloc(edge.getV1().getNode());
							
				//			Var va1 = Var.alloc(edge.getV2().getNode());
									
					//		for(Binding e1:results) {
								
					//		MultipleBinding.put(BindingFactory.binding(va1, e1.get(va1)),BindingFactory.binding(va, e1.get(va)));
							
						//	}
						
							

							System.out.println("----------------------------------------------------------------------------");
						
						//	for(Entry<Binding, Binding> mb:MultipleBinding.entries())
						//		System.out.println("This is child1:"+mb);
//			}
						
//					}
				
	//			if(v9.contains(Var.alloc(start.getNode())))
		//			System.out.println("These are now being processed here in here");
			if (BGPEval.StartBinding123.containsKey(start)) {
		
							
			//	for(Binding e2:results)
			//	{	System.out.println("This is parent:"+e2);
			//		for(Binding i1:inter1)
			//		if(e2.toString().contains(i1.toString()))
				//		System.out.println("This is child2:"+inter1);
				//}
				System.out.println("----------------------------------------------------------------------------");
				
				Set<Binding> temp1 = new HashSet<>();
		//	System.out.println("This key is present:"+start);
			Set<Vertex> v = new HashSet<>();
//for(Entry<Vertex, Set<Binding>>	e:BGPEval.StartBinding123.entrySet()) {
			v.addAll(BGPEval.StartBinding123.keySet());
//}
			int br = 0;
			Iterator<Var> l1 = BindJoin.results.iterator().next().vars();
			for (Vertex v1 : v) {
				Var r = Var.alloc(v1.getNode());
			//	System.out.println("This is rule no. 1 in BindJoin:" + r);
				while (l1.hasNext()) {
					Var v2 = l1.next();

					if (r.equals(v2)) {
	//					System.out.println("This is rule no.3 in BindJoin:" + r + "--" + v);
						joinVars = v2;
						br = 1;
						break;
					}

				}
				if (br == 1) {
					br = 0;
					break;
				}
			}
			if (joinVars != null) {
//				System.out.println("This is rule no. 2 in BindJoin:" + joinVars);

				for (Binding e1 : results) {
//		BindingMap join = BindingFactory.create();
//		join.add(joinVars, e1.get(joinVars));

//		System.out.println("This is join in temp:"+temp1);
					temp1.add(BindingFactory.binding(joinVars, e1.get(joinVars)));
					// for(int k=0;k<4;k++)

				}
//	for(Binding t:temp1)
//System.out.println("This the replacement:"+t+"--"+temp1.size()+"--"+BGPEval.StartBinding123.get(start).size());
//	BGPEval.StartBinding123.remove(joinVars);//.replace(start, BGPEval.StartBinding123.get(start), temp1);
				BGPEval.StartBinding123.put(start, temp1);
//	for(Entry<Vertex, Set<Binding>> t:BGPEval.StartBinding123.entrySet())
			//	System.out.println("This the replacement:" + "--" + temp1.size() + "--"
			//			+ BGPEval.StartBinding123.get(start).size());
			}
		}
		/// results = QueryUtil.join(results, start.getBindings());

		// for(Binding r:results)
		// System.out.println("These are value of bind results:"+r);
		int kl = 0;

		Edge CurrentEdgeOperator = new Edge(edge.getV1(), edge.getV2());// edge.getV1()+"--"+edge.getV2();
		Iterator<Entry<EdgeOperator, List<Binding>>> frIterator = BGPEval.finalResult.entrySet().iterator();
		while (frIterator.hasNext()) {
			total++;
			Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
			// System.out.println("This is
			// finalResult:"+ab.getKey()+"--"+ab.getValue().size());

			if ((ab.getKey().getEdge().getV1() + "--" + ab.getKey().getEdge().getV2()).toString()
					.equals((edge.getV1() + "--" + edge.getV2()).toString())) {
				BGPEval.finalResult.replace(ab.getKey(), results);
				// CurrentEdgeOperator=ab.getKey();
			}

		}

		for (Entry<EdgeOperator, List<Binding>> ab : BGPEval.finalResultRight.entrySet()) {
			total++;
			// System.out.println("This is
			// finalResultRight:"+ab.getKey()+"--"+ab.getValue().size());

			if ((ab.getKey().getEdge().getV1() + "--" + ab.getKey().getEdge().getV2()).toString()
					.equals((edge.getV1() + "--" + edge.getV2()).toString())) {
				BGPEval.finalResultRight.replace(ab.getKey(), results);
			}

			// CurrentEdgeOperator=ab.getKey();

		}
		for (Entry<EdgeOperator, List<Binding>> ab : BGPEval.finalResultLeft.entrySet()) {
			total++;

			if ((ab.getKey().getEdge().getV1() + "--" + ab.getKey().getEdge().getV2()).toString()
					.equals((edge.getV1() + "--" + edge.getV2()).toString())) {
				// System.out.println("This is
				// finalResultLeft:"+ab.getKey()+"--"+results.size());

				BGPEval.finalResultLeft.replace(ab.getKey(), results);
				// CurrentEdgeOperator=ab.getKey();

			}
		}
		System.out.println("This is out of TripleExectuion in BindJoin000000000000:");

		// synchronized(BGPEval.finalResultOptional) {
		for (Entry<EdgeOperator, List<Binding>> ab : BGPEval.finalResultRightOptional.entrySet()) {
			total++;
			if ((ab.getKey().getEdge().getV1() + "--" + ab.getKey().getEdge().getV2()).toString()
					.equals((edge.getV1() + "--" + edge.getV2()).toString())) {
				BGPEval.finalResultRightOptional.replace(ab.getKey(), results);
			}

			// CurrentEdgeOperator=ab.getKey();

		}
		for (Entry<EdgeOperator, List<Binding>> ab : BGPEval.finalResultLeftOptional.entrySet()) {
			total++;
			if ((ab.getKey().getEdge().getV1() + "--" + ab.getKey().getEdge().getV2()).toString()
					.equals((edge.getV1() + "--" + edge.getV2()).toString())) {
				BGPEval.finalResultLeftOptional.replace(ab.getKey(), results);
				// CurrentEdgeOperator=ab.getKey();

			}
		}
		for (Entry<EdgeOperator, List<Binding>> ab : BGPEval.finalResultOptional.entrySet()) {
			total++;
			if ((ab.getKey().getEdge().getV1() + "--" + ab.getKey().getEdge().getV2()).toString()
					.equals((edge.getV1() + "--" + edge.getV2()).toString())) {
				BGPEval.finalResultOptional.replace(ab.getKey(), results);
				// CurrentEdgeOperator=ab.getKey();

			}
		}
		// for(Binding i:start.getBindings())
		System.out.println("This is out of TripleExectuion in BindJoin:");

		for (HashSet<List<EdgeOperator>> e1 : BGPEval.linkingTreeDup.keySet()) {
			for (List<EdgeOperator> e2 : e1)
				for (EdgeOperator e3 : e2)
					if (e3.getEdge().equals(CurrentEdgeOperator)) {
						HashJoin.ProcessedEdgeOperators.addAll(e1);
					}
		}
		//// System.out.printlnln("This is now the new tree
		//// right:"+HashJoin.ProcessedEdgeOperators+"--"+CurrentEdgeOperator+"--"+total);
		int count = 0;
		frIterator = BGPEval.finalResult.entrySet().iterator();
		while (frIterator.hasNext()) {
			Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
			for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
				for (EdgeOperator ee1 : ee) {
					if (ab.getValue() != null)
						if (ee1.equals(ab.getKey())) { ////// System.out.printlnln("THis is coming to final
														////// algo:"+ab.getKey()+"--"+ab.getValue().size());
							////// System.out.printlnln("THis is coming to final algo2:"+ee);
							HashJoin.ProcessedTriples.put(CompleteEdgeOperator(ab.getKey()), ab.getValue());

							count++;
						}
				}
		}
		//// System.out.printlnln("This is now the new tree right count:"+count);
		count = 0;
		frIterator = BGPEval.finalResultLeft.entrySet().iterator();
		while (frIterator.hasNext()) {
			Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
			for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
				for (EdgeOperator ee1 : ee) {
					if (ab.getValue() != null)
						if (ee1.equals(ab.getKey())) { ////// System.out.printlnln("THis is coming to final algo
														////// Rights:"+ab.getKey()+"--"+ab.getValue().size());
							////// System.out.printlnln("THis is coming to final algo2 Right:"+ee);
							// count++;
							List<EdgeOperator> l1 = new ArrayList<>();
							l1.add(ab.getKey());
							HashJoin.ProcessedTriples.put(l1, ab.getValue());

						}
				}
		}
		////// System.out.printlnln("This is now the new tree right count:"+count);
		frIterator = BGPEval.finalResultRight.entrySet().iterator();
		while (frIterator.hasNext()) {
			Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
			for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
				for (EdgeOperator ee1 : ee) {
					if (ab.getValue() != null)
						if (ee1.equals(ab.getKey())) { ////// System.out.printlnln("THis is coming to final algo
														////// Rights:"+ab.getKey()+"--"+ab.getValue().size());
							////// System.out.printlnln("THis is coming to final algo2 Right:"+ee);
							// count++;
							List<EdgeOperator> l1 = new ArrayList<>();
							l1.add(ab.getKey());
							HashJoin.ProcessedTriples.put(l1, ab.getValue());

						}
				}
		}
		if (ParaEng.Optional.contains("OPTIONAL")) {
			frIterator = BGPEval.finalResultOptional.entrySet().iterator();
			while (frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
					for (EdgeOperator ee1 : ee) {
						if (ab.getValue() != null)
							if (ee1.equals(ab.getKey())) { ////// System.out.printlnln("THis is coming to final
															////// algo:"+ab.getKey()+"--"+ab.getValue().size());
								////// System.out.printlnln("THis is coming to final algo2:"+ee);
								HashJoin.ProcessedTriples.put(CompleteEdgeOperator(ab.getKey()), ab.getValue());

								count++;
							}
					}
			}
		}
		//// System.out.printlnln("This is now the new tree right count:"+count);
		if (ParaEng.Optional.contains("OPTIONAL")) {
			frIterator = BGPEval.finalResultLeftOptional.entrySet().iterator();
			while (frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
					for (EdgeOperator ee1 : ee) {
						if (ab.getValue() != null)
							if (ee1.equals(ab.getKey())) { // ////System.out.printlnln("THis is coming to final algo
															// Left:"+ab.getKey()+"--"+ab.getValue().size());
								////// System.out.printlnln("THis is coming to final algo2 Left:"+ee);
								List<EdgeOperator> l1 = new ArrayList<>();
								l1.add(ab.getKey());
								HashJoin.ProcessedTriples.put(l1, ab.getValue());
								// count++;

							}
					}
			}
		}
		////// System.out.printlnln("This is now the new tree right count:"+count);

		if (ParaEng.Optional.contains("OPTIONAL")) {
			frIterator = BGPEval.finalResultRightOptional.entrySet().iterator();
			while (frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
					for (EdgeOperator ee1 : ee) {
						if (ab.getValue() != null)
							if (ee1.equals(ab.getKey())) { ////// System.out.printlnln("THis is coming to final algo
															////// Rights:"+ab.getKey()+"--"+ab.getValue().size());
								////// System.out.printlnln("THis is coming to final algo2 Right:"+ee);
								// count++;
								List<EdgeOperator> l1 = new ArrayList<>();
								l1.add(ab.getKey());
								HashJoin.ProcessedTriples.put(l1, ab.getValue());
							}
					}
			}
		}
		int IsUnion = 0;
		if (ParaEng.Union.contains("UNION")) {

			frIterator = BGPEval.finalResultOptional.entrySet().iterator();
			while (frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
					for (EdgeOperator ee1 : ee) {
						if (ab.getValue() != null)
							if (ee1.equals(ab.getKey())) { ////// System.out.printlnln("THis is coming to final
															////// algo:"+ab.getKey()+"--"+ab.getValue().size());
								////// System.out.printlnln("THis is coming to final algo2:"+ee);
								HashJoin.ProcessedTriplesUnion.put(CompleteEdgeOperator(ab.getKey()), ab.getValue());
								IsUnion = 1;
								count++;
							}
					}
			}
		}
		//// System.out.printlnln("This is now the new tree right count:"+count);
		if (ParaEng.Union.contains("UNION")) {
			frIterator = BGPEval.finalResultLeftOptional.entrySet().iterator();
			while (frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
					for (EdgeOperator ee1 : ee) {
						if (ab.getValue() != null)
							if (ee1.equals(ab.getKey())) { // ////System.out.printlnln("THis is coming to final algo
															// Left:"+ab.getKey()+"--"+ab.getValue().size());
								////// System.out.printlnln("THis is coming to final algo2 Left:"+ee);
								List<EdgeOperator> l1 = new ArrayList<>();
								l1.add(ab.getKey());
								HashJoin.ProcessedTriplesUnion.put(l1, ab.getValue());
								// count++;
								IsUnion = 1;
							}
					}
			}
		}
		////// System.out.printlnln("This is now the new tree right count:"+count);

		if (ParaEng.Union.contains("UNION")) {
			frIterator = BGPEval.finalResultRightOptional.entrySet().iterator();
			while (frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for (List<EdgeOperator> ee : HashJoin.ProcessedEdgeOperators)
					for (EdgeOperator ee1 : ee) {
						if (ab.getValue() != null)
							if (ee1.equals(ab.getKey())) { ////// System.out.printlnln("THis is coming to final algo
															////// Rights:"+ab.getKey()+"--"+ab.getValue().size());
								////// System.out.printlnln("THis is coming to final algo2 Right:"+ee);
								// count++;
								List<EdgeOperator> l1 = new ArrayList<>();
								l1.add(ab.getKey());

								HashJoin.ProcessedTriplesUnion.put(l1, ab.getValue());
								IsUnion = 1;
							}
					}
			}
		}

		////// System.out.printlnln("This is now the new tree right count:"+count);
		// count=0;

		for (List<EdgeOperator> et : HashJoin.EvaluatedTriples) {
			HashJoin.ProcessedTriples.remove(et);
		}
		// for(Entry<List<EdgeOperator>, List<Binding>>
		// s:HashJoin.ProcessedTriples.entrySet())
		// System.out.println("This is
		// linkingTreeDup:"+s.getKey()+"--"+s.getValue().size());
		// for(Entry<List<EdgeOperator>, List<Binding>>
		// s:HashJoin.JoinedTriples.entrySet())
		// System.out.println("This is linkingTreeDup
		// JoinedTriples:"+s.getKey()+"--"+s.getValue().size());
		// for(Entry<List<EdgeOperator>, List<Binding>>
		// s:HashJoin.NotJoinedTriples.entrySet())
		// System.out.println("This is linkingTreeDup
		// NotJoinedTriples:"+s.getKey()+"--"+s.getValue().size());

		// System.out.println("These are the processed
		// triples:"+HashJoin.ProcessedTriples);
		// if (!ParaEng.Union.contains("UNION")) {
		HashJoin.ProcessingTask(HashJoin.JoinedTriples, HashJoin.ProcessedTriples, 0);
		HashJoin.ProcessingTask(HashJoin.NotJoinedTriples, HashJoin.ProcessedTriples, 0);

		// } else {
		// if (IsUnion == 1) {

		// HashJoin.ProcessingTask(HashJoin.JoinedTriples, HashJoin.ProcessedTriples,0);
		// HashJoin.ProcessingTask(HashJoin.NotJoinedTriples,
		// HashJoin.ProcessedTriples,0);
		// remaining++;
		// System.out.println("This is second
		// Union"+HashJoin.JoinedTriples.size()+"--"+HashJoin.ProcessedTriples.size());

		// }
		// }

		for (List<EdgeOperator> et : HashJoin.EvaluatedTriples) {
			HashJoin.JoinedTriples.remove(et);
		}

		for (int i1 = 0; i1 < 9; i1++) {
			ForkJoinPool fjp1 = new ForkJoinPool();
			fjp1.submit(() -> {
				BGPEval.finalResultCalculation(HashJoin.NotJoinedTriples, HashJoin.EvaluatedTriples,
						HashJoin.JoinedTriples);
			}).join();
			fjp1.shutdown();
			ForkJoinPool fjp2 = new ForkJoinPool();

			fjp2.submit(() -> {
				BGPEval.finalResultCalculation(HashJoin.JoinedTriples, HashJoin.EvaluatedTriples,
						HashJoin.JoinedTriples);
			}).join();
			fjp2.shutdown();

			ForkJoinPool fjp21 = new ForkJoinPool();

			fjp21.submit(() -> {
				BGPEval.finalResultCalculation(HashJoin.NotJoinedTriples, HashJoin.EvaluatedTriples,
						HashJoin.NotJoinedTriples);
			}).join();
			fjp21.shutdown();
		}

		HashJoin.ProcessUnion();
		// for(Binding i:start.getBindings())
		//// System.out.println("This is out of TripleExectuion in BindJoin:");
		// }

		synchronized (end) {

			end.notifyAll();
		}
		// setInput(null);

		// start.removeEdge(edge);
		// end.removeEdge(edge);
		// }
	}

	@Override
	public String toString() {
		return "Bind join: " + start + "--" + edge;
	}

	public List<EdgeOperator> CompleteEdgeOperator(EdgeOperator ct) {
		// Object jgl;
		for (List<EdgeOperator> jgl : BGPEval.JoinGroupsListAll) {
			if (jgl.contains(ct))
				return jgl;
		}
		return null;
	}

}
