
package com.fluidops.fedx.trunk.parallel.engine.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.SimpleGraph;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.ParaConfig;
import com.fluidops.fedx.trunk.parallel.engine.error.RelativeError;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.BindJoin;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.HashJoin;
import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class ExhOptimiser extends Optimiser {
	
	private Set<EdgeOperator> operators_reorder = new LinkedHashSet<EdgeOperator>();
	public static Set<EdgeOperator> operators_reorder1 = new LinkedHashSet<EdgeOperator>();
	public static LinkedHashMap<String,LinkedHashMap<Double,Double>> LabeledSize= new LinkedHashMap<>();
		
	private Set<EdgeOperator> optimal;
	public int repeat = 0;
 int j=0;
	public ExhOptimiser(SimpleGraph g) {
		super(g);

		long start = System.nanoTime();
		//System.out.println("This is now a ExhOptimiser constructor:" + g);

		initialOptimal();
		long end = System.nanoTime();
		if (ParaConfig.debug) {
			Long inter = end - start;
			//System.out.println("Optimisation: " + inter.doubleValue() / 1000000000);
		}
	}

	@Override
	public List<EdgeOperator> nextStage() {
		List<EdgeOperator> operators = null;
		if (optimal != null) {
			operators = new ArrayList<EdgeOperator>(optimal);
			optimal = null;
		}
		return operators;
	}

	@Override
	public Set<Vertex> getRemainVertices() {
		Set<Vertex> remains = new HashSet<Vertex>();
		for (Vertex v : g.getVertices()) {
			if (!v.isConsumed()) {
				remains.add(v);
			}
		}
		return remains;
	}

	private void initialOptimal() {
		// optimal contains the sequence of the execution of edges.
		// each element (a List<EdgeOperator>) is the edges to be executed
		// in parallel, the index of the element indicates the order of the execution
		// of these edges.
		List<Edge> edges = g.getEdges();
		//Iterator<Edge> NewOrders;
		//	while(orders.hasNext())
		//			orders_counted.addAll(orders.next()); 
	
		optimal = getFirstStage(edges);
		if (!edges.isEmpty()) {
			Iterator<List<Edge>> orders = getPermutations(edges);
			
	//		Iterator<List<Edge>> orders_counted= orders;
		
	//		while(orders.hasNext())	
	//			//System.out.println("This is getOptimal orders_counted:"+orders.next());
			
			
/*			int i =0;
				while( orders_counted.hasNext()) {
					
					//if(i>=4) {
						////System.out.println("This is getOptimal orders_counted:"+orders_counted.next());
						//System.out.println("This is getOptimal orders_counted:"+i+"--"+orders_counted.next());
						orders_counted.next()=null;			
					//	orders_counted.next().remove(i) ;
						i++;
					//}
				}
	*/		
		
			ForkJoinTask<Stream<Set<EdgeOperator>>> candidates;
			 Set<Set<EdgeOperator>> candidates1 = null;
			//candidates = getOptimal(optimal, orders).parallelStream();
			ForkJoinPool fjp = new ForkJoinPool();
			 //while(orders.hasNext())
			//	 //System.out.println("This is the optimal Value in Initial Optimal:"+j+"--"+orders.next());

			candidates = fjp.submit(() -> getOptimal(optimal, orders).parallelStream());
		
			//	Set<Set<EdgeOperator>> candidates1 = null;
			// candidates1 = candidates.get().collect(Collectors.toSet());

			try {
				candidates1 = candidates.get().collect(Collectors.toSet());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fjp.shutdown();
		

			optimal = finalize(optimal, candidates1);
		}
	}


	/**
	 * Find out all edges having concrete node. All these edges will be executed
	 * first.
	 * 
	 * @param edges
	 * @return
	 */
	private Set<EdgeOperator> getFirstStage(List<Edge> edges) {
		Set<EdgeOperator> firststage = new HashSet<EdgeOperator>();
		List<Edge> toberemove = new ArrayList<Edge>();
		for (Edge e : edges) {
			Edge se = (Edge) e;
			if (se.getV1().getNode().isConcrete() || se.getV2().getNode().isConcrete()) {
				firststage.add(new HashJoin(se));
				toberemove.add(e);
				if (Config.relative_error) {
					RelativeError.est_resultSize.put(se.getV1(), se.estimatedCard());
					RelativeError.est_resultSize.put(se.getV2(), se.estimatedCard());
				}
			}

//            if(Config.relative_error){
//                RelativeError.est_resultSize.put(se.getV1(), (int)se.estimatedCard());
//                RelativeError.est_resultSize.put(se.getV2(), (int)se.estimatedCard());
//                if(se.getV1().getNode().isConcrete()) {
//                    RelativeError.est_resultSize.put(se.getV1(), 1);
//                    //RelativeError.est_resultSize.put(se.getV2(), 1);
//                }
//                else  {
//                    RelativeError.est_resultSize.put(se.getV1(), se.getTripleCount()/(se.getDistinctObject()!=0?se.getDistinctObject():1));
//                }
//                if (se.getV2().getNode().isConcrete()){
//                    RelativeError.est_resultSize.put(se.getV2(), 1);
//                    //RelativeError.est_resultSize.put(se.getV1(), 1);
//                }else {
//                    RelativeError.est_resultSize.put(se.getV2(), se.getTripleCount()/(se.getDistinctSubject()!=0?se.getDistinctSubject():1));
//                }
//            }
			//System.out.println(
					//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 0.1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
							//+ se.getV1() + "--" ////+ se.getV2()+"--"+se);

			if (!se.getV1().isConsumed()) {
			
				if(se.getV1()==null)
				firststage.add(new BindJoin(se.getV1(), se));
				//System.out.println(
						//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
								//+ se.getV1() + "--" //+ firststage.toString());

				toberemove.add(e);
				if (Config.relative_error) {
					RelativeError.est_resultSize.put(se.getV1(), se.estimatedCard());
					RelativeError.est_resultSize.put(se.getV2(), se.estimatedCard());
				}

			}else if (!se.getV2().isConsumed()) {
				firststage.add(new BindJoin(se.getV2(), se));
				//System.out.println(
						//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
								////+ se.getV2() + "--" //+ firststage.toString());

				toberemove.add(e);
				if (Config.relative_error) {
					RelativeError.est_resultSize.put(se.getV1(), se.estimatedCard());
					RelativeError.est_resultSize.put(se.getV2(), se.estimatedCard());
				}
			}
		}
		//System.out.println(//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 3!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
				//+ firststage.toString() + "--" + RelativeError.est_resultSize);

		edges.removeAll(toberemove);
		return firststage;
	}
	private Set<Set<EdgeOperator>> getOptimal(Set<EdgeOperator> firststage, Iterator<List<Edge>> orders) {
		Map<Vertex, Double> resultsize_ = new HashMap<Vertex, Double>();
//		Double size1;
//		Double size2;
		for (EdgeOperator eo : firststage) {
			Edge edge = eo.getEdge();
			Vertex v1 = edge.getV1();
			Vertex v2 = edge.getV2();
			if (v1.getNode().isConcrete()) {
				resultsize_.put(v1, 1.0);
				resultsize_.put(v2, (double) edge.getDistinctSubject());
//				resultsize_.put(v2, (double)edge.getTripleCount()/edge.getDistinctSubject());
			} else {
				resultsize_.put(v2, 1.0);
//				resultsize_.put(v1, (double)edge.getTripleCount()/edge.getDistinctObject());
				resultsize_.put(v1, (double) edge.getDistinctObject());

			}

			//System.out.println(//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
				//	+edge.getTriple().getSubject()+ "--" + resultsize_.get(v1)+"--"+edge.getTriple().getObject() + "--" + resultsize_.get(v2) + "--" + edge.getTripleCount() + "--"
				//	+ edge.getDistinctSubject() + "--" + edge.getDistinctObject());
		
			LinkedHashMap<Double,Double> a=new LinkedHashMap<>();
			a.put(resultsize_.get(v1), resultsize_.get(v2));
//		String ui = edge.getTriple().getObject().isURI()?edge.getTriple().getObject().toString():edge.getTriple().getObject().getName().toString();
		String ui ;//= x.getEdge().getTriple().getSubject().isURI()?x.getEdge().getTriple().getSubject().toString():x.getEdge().getTriple().getSubject().getName().toString();
		if(edge.getTriple().getObject().isURI())
		ui=edge.getTriple().getObject().toString();
		else if(edge.getTriple().getObject().isLiteral())
		ui=edge.getTriple().getObject().getLiteral().toString();
		else
		ui	=edge.getTriple().getObject().getName().toString();


		String ui1 ;//= x.getEdge().getTriple().getSubject().isURI()?x.getEdge().getTriple().getSubject().toString():x.getEdge().getTriple().getSubject().getName().toString();
		if(edge.getTriple().getSubject().isURI())
		ui1=edge.getTriple().getSubject().toString();
		else if(edge.getTriple().getSubject().isLiteral())
		ui1=edge.getTriple().getSubject().getLiteral().toString();
		else
		ui1	=edge.getTriple().getSubject().getName().toString();

		LabeledSize.put(ui1+"--"+ui,a);

		}

		if (Config.relative_error && resultsize_.size() > 0) {
			// RelativeError.est_resultSize.clear();
			RelativeError.est_resultSize.putAll(resultsize_);
		}
		double cost = Double.MAX_VALUE;
		Set<Set<EdgeOperator>> candidates = new HashSet<Set<EdgeOperator>>();
		while (orders.hasNext()) {
			List<Edge> order = orders.next();
			// for a list of edges generate the corresponding list of operator
			// that has the minimum cost. That is, for each edge, choose either
			// hash join or bind join that has lower cost
			Set<EdgeOperator> plan = new HashSet<EdgeOperator>();
			// record bound vertices and their number of bindings
			Map<Vertex, Double> resultsize = new HashMap<Vertex, Double>(resultsize_);
			double temp = 0;
			j++;
			if(j>6)
				break;
			//System.out.println("These are the numbers of orders in ExhOptimisers:"+j+"--"+order);
	
			for (Edge e : order) {
				Edge edge = (Edge) e;
				Vertex v1 = edge.getV1();
				Vertex v2 = edge.getV2();
				// add all concrete vertices and their binding number (1) to the record
				if (v1.getNode().isConcrete()) {
					resultsize.put(v1, 1.0);
				}
				if (v2.getNode().isConcrete()) {
					resultsize.put(v2, 1.0);
				}

		Double	size1 = resultsize.get(v1);
		Double	size2 = resultsize.get(v2);

				double nj = 1 * CostModel.CQ + edge.getTripleCount() * CostModel.CT;
				// double nj = 1*CostModel.CQ + CostModel.CT;
              //  double v1Value = 0,v2Value = 0;
				//System.out.println(
						//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 5!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
							//	+ v1.getNode() + "--" + v2.getNode() + "--" + resultsize.get(v1) + "--"
							//	//+ resultsize.get(v2) + "--" + edge.getDistinctSubject() + "--"
							//	+ edge.getDistinctObject() + "--" + edge.getTripleCount() + "--" + size1 + "--" + size2
							//	+ "--" + nj + "--" + CostModel.CQ + "--" + CostModel.CT);

				if (size1 == null && size2 == null) {// if this is the first edge, use HashJoin.
					temp += nj;

					//	if (edge.getDistinctSubject() == edge.getTripleCount())
					//if(edge.getDistinctSubject()>edge.getDistinctObject())
					//	plan.add(new HashJoin(edge));
					//else 
					if (edge.getTripleCount()== edge.getDistinctObject())
						//	plan.add(new HashJoin(edge));
							plan.add(new HashJoin(edge));
					else if (edge.getDistinctSubject() == edge.getTripleCount())
						if(edge.getDistinctObject()==1.0)
							plan.add(new HashJoin(edge));
						else plan.add(new HashJoin(edge));
		
					else if (edge.getDistinctSubject() == edge.getDistinctObject())
						if(edge.getTripleCount()==1.0)
							plan.add(new HashJoin(edge));
							else plan.add(new HashJoin(edge));
				
					else	if (edge.getTripleCount()!= edge.getDistinctObject()&& edge.getDistinctSubject()!= edge.getDistinctObject() && (edge.getDistinctSubject() + edge.getDistinctObject())<=edge.getTripleCount())
						//	plan.add(new HashJoin(edge));
							plan.add(new HashJoin(edge));
					else	if (edge.getTripleCount()!= edge.getDistinctSubject() && edge.getDistinctSubject()!= edge.getDistinctObject() && (edge.getDistinctSubject() + edge.getDistinctObject())>edge.getTripleCount())
						plan.add(new HashJoin(edge));
				
					else if ((edge.getDistinctSubject() ==0.71) &&(edge.getDistinctObject() ==0.71))
						plan.add(new HashJoin(edge));	
					else if (((edge.getDistinctSubject() + edge.getDistinctObject())<=edge.getTripleCount() && edge.getDistinctObject()!=edge.getDistinctSubject()&& edge.getTripleCount()!=edge.getDistinctSubject())  )
						plan.add(new BindJoin(v1,edge));
				//		else if ((edge.getDistinctSubject() == edge.getTripleCount()) && (edge.getTripleCount()>edge.getDistinctSubject()))
					//		plan.add(new HashJoin(edge));
					//	else if ((edge.getDistinctSubject() == edge.getTripleCount()) )
					//		plan.add(new BindJoin(v1,edge));
					
						
						/*else if ((edge.getDistinctSubject() + edge.getDistinctObject())>edge.getTripleCount() 
								&& !((edge.getDistinctSubject() ==0.71) &&(edge.getDistinctObject() ==0.71)))
						//	plan.add(new HashJoin(edge));
							plan.add(new BindJoin(v1,edge));
						*/
						else
						plan.add(new BindJoin(v1,edge));
				//	else
				//		plan.add(new HashJoin(edge));
					
	resultsize.put(v1, (double) edge.getTripleCount());
					resultsize.put(v2, (double) edge.getTripleCount());
					//System.out.println(//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
					//		+ edge.getDistinctSubject() + "--"
					//		+ edge.getDistinctObject() + "--" + edge.getTripleCount()+ "--" + plan.toString() + "--" + resultsize.get(v1) + "--" //+ resultsize.get(v2) + "--"
					//		+ edge.getTripleCount());
				
					LinkedHashMap<Double,Double> a=new LinkedHashMap<>();
					a.put(0.02, 0.02);
				int skp=0;
					for(Entry<String, LinkedHashMap<Double, Double>> gh:LabeledSize.entrySet())
						if(gh.getKey().toString().equals(v1.getNode().getName()+"--"+v2.getNode().getName()))
						{skp=1; break;}
						if(skp==1) {
							skp=0;
							continue;
							}
						else
						LabeledSize.put(v1.getNode().getName()+"--"+v2.getNode().getName(),a);
						
					//LabeledSize.put(v1.getNode().getName()+"--"+v2.getNode().getName(),a);
				
				} else {
					//System.out.println(
							//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 6.0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
						//			+ size1 + "--" + size2);
					size1 = size1 == null ? edge.getDistinctSubject() : size1;
//					size2 = size2 == null?edge.getDistinctObject()+edge.getTripleCount():size2;
					size2 = size2 == null ? edge.getDistinctObject() : size2;

					double ssel = 1d / edge.getDistinctSubject();
					double osel = 1d / edge.getDistinctObject();
					int bindingsize = (int) Math.max(size1 * ssel * edge.getTripleCount() * size2 * osel, 2);
				double	v1Value=resultsize.get(v1)==null?0:resultsize.get(v1);
				double	v2Value=resultsize.get(v2)==null?0:resultsize.get(v2);
				
					//System.out.println(
							//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 6!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
							//		+ v1.getNode() + "--" + v2.getNode() + "--bindingsize:" + bindingsize + "--size1:" + size1 + "--size2:"
							//		+ size2 + "--ssel:" + ssel + "--osel:" + osel + "--subject:" + edge.getDistinctSubject() + "--object:"
							//		+ edge.getDistinctObject() + "--triple:" + edge.getTripleCount() + "--resultsizeV1:"
								//	+ resultsize.get(v1) + "--resultsizeV2" //+ resultsize.get(v2)+"--temp:"+temp);
					int skp=0;
					LinkedHashMap<Double,Double> a=new LinkedHashMap<>();
					a.put(size1, size2);
					for(Entry<String, LinkedHashMap<Double, Double>> gh:LabeledSize.entrySet())
					if(gh.getKey().toString().equals(v1.getNode().getName()+"--"+v2.getNode().getName()))
					{skp=1; break;}
					if(skp==1) {
						LabeledSize.replace(v1.getNode().getName()+"--"+v2.getNode().getName(),a);
					skp=0;
					}
					else
					LabeledSize.put(v1.getNode().getName()+"--"+v2.getNode().getName(),a);
					
					if (size1 <= size2) {
						// if the minimum vertex is concrete (and a concrete vertex should always
						// be the minimum vertex since its binding size is 1), use hash join
						if (v1.getNode().isConcrete()) {
							temp += nj;
							plan.add(new HashJoin(edge));
							resultsize.put(v2, (double) bindingsize);
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

							continue;
						}

						double bj = size1 * CostModel.CQ + size1 * ssel * edge.getTripleCount();
						
						//System.out.println(
								//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 7!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
									//	+ v1.getNode().isConcrete() + "--" + v2.getNode().isConcrete() + "--" + nj
									//	+ "--" + bj+"--"+size1+"--"+size2);

						
							if ((((edge.getDistinctSubject() == edge.getDistinctObject()))
								&& (resultsize.get(v1) != resultsize.get(v2))
								&& (edge.getDistinctObject() != edge.getTripleCount())
								&& (edge.getDistinctSubject() != size1) && (edge.getDistinctObject() != size1)) || (size1==0.71 &&v1Value==0.71)) {
							temp += bj;
							plan.add(new BindJoin(v1, edge));
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

						} else if (edge.getDistinctSubject() == edge.getTripleCount()) {
							// //System.out.println(
							// //"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in equal triple subject
							// and minimum vertex is not concrete and
							// nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (bindingsize != size1) && (edge.getDistinctSubject()!=edge.getDistinctObject() ) && (size2!=edge.getDistinctObject()) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()) ))) {
								if(v1Value!=0.71 && size1!=0.71) {
								if(v2Value!=size2) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(v2Value==size2) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 resultsizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}
								
								else	if(v1Value==0.71 && size1==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1(0.71)=size1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value==0.71 && size2==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV2(0.71)=size2(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value!=0.71 && size2!=0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}


						}
							else	if ((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (bindingsize == size1))) {
								 if(size1==size2) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
	
								}
								 else	if ((edge.getDistinctSubject() == edge.getTripleCount()  && (bindingsize == size2)&& (bindingsize == edge.getDistinctSubject()))
											&& ((v1Value == size1) && (size1==edge.getDistinctObject())))  {
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject=binding=size2 v1Value=size1=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
											if (nj <= bj) {
												temp += bj;
												plan.add(new BindJoin(v1, edge));
												//System.out.println(
														//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
																//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
																//+ v2Value);

											} else {
												temp += nj;
												plan.add(new HashJoin(edge));
												//System.out.println(
														//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
																//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
																//+ resultsize.get(v2));

											}
			
										}

									else if(size2==edge.getDistinctObject()) {
									if(size2>temp) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize size2=object size2>temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
									}
									
									if(size2<=temp) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize size2=object size2<=temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
									}

								}

								 else {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v1, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ resultsize.get(v2));

										}
									}

							}
					//		else	if (((edge.getDistinctSubject() == edge.getTripleCount()
					//				&& (v1Value == size1) && (bindingsize == size1) && (size2 == size1)))) {
					//										}
							else if(size1==0.71 &&v1Value==0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1(0.71)=v1Value(0.71) bindingsize>(size1+ResultSizeV1) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}

							else if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1)  && (size2==edge.getDistinctObject()) ))) {
								
								if(v1Value!=0.71 && size1!=0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
								else	if(v1Value==0.71 && size1==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1(0.71)=size1(0.71) size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}

							}
							else	if ((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (edge.getDistinctSubject() == edge.getDistinctObject()))) {
								if(v2Value!=size2) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize object=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ resultsize.get(v2));

							}
							}
								if(v2Value==size2) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize object=subject value2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

								}
								}

							}
							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (edge.getTripleCount() == size1)) {
								if(size2 != v2Value) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  triple=subject=size1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								
								if(size2 == v2Value) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  triple=subject=size1 size2=resultSizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 != size2)
									&& (size1 != v1Value)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							} else if (((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size1 == v1Value) && (bindingsize != size1) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()))) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size=resultsizeV1  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}

							} else if ((edge.getDistinctSubject() == edge.getTripleCount()) && (size1 == bindingsize)
									&& (size2 != edge.getDistinctObject())&& (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()) ) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in resultsize equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 == size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size=object size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							} else if (((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == v2Value) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()) )) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else {
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
						}

						else if ((edge.getDistinctSubject() + edge.getDistinctObject()) == size2) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject+object equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == edge.getTripleCount()))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject=object=triple equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size1))) {
						if(edge.getDistinctSubject() != 0.71 && edge.getDistinctObject() != 0.71) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject=object=size1 equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						if(edge.getDistinctSubject() == 0.71 && edge.getDistinctObject() == 0.71) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject(0.71)=object(0.71)=size1 equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						}

						else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size2))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge subject+object+size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}

						else if ((size2 == edge.getDistinctObject())) {
							if(size1!=v1Value) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
							
							else	if(size1==v1Value) {
							if(bindingsize>(size1+v1Value)) {
								if(size1!=0.71 &&v1Value!=0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1=v1Value bindingsize>(size1+ResultSizeV1) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								
								if(size1==0.71 &&v1Value==0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1(0.71)=v1Value(0.71) bindingsize>(size1+ResultSizeV1) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}
							if(bindingsize<=(size1+v1Value)) {
								
								if(bindingsize!=size1) {
									if( v2Value!=size2) {
								if((bindingsize>(edge.getDistinctSubject()+size2)) ) {
										//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1=v1Value and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if((bindingsize<=(edge.getDistinctSubject()+size2)) ) {
									//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1=v1Value bindingsize<=(Subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
									}
									if( v2Value==size2) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object=size1=v1Value=v2Value and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
									}
								}
								if(bindingsize==size1) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object bindingsize=size1=v1Value and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}
								}

						} else if ((edge.getDistinctObject() + edge.getTripleCount() == size2)) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in object+subject+size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						else if(size2==v2Value){
							if(size1!=edge.getDistinctSubject()) {
							if(size1!=v1Value) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
							if(size1==v1Value) {
							if(size1!=0.71&&size2!=0.71&&v1Value!=0.71&&v2Value!=0.71) {
							if(edge.getDistinctObject()!=edge.getTripleCount()) {
								if(size2!=edge.getTripleCount()) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1==resultsizeV1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
								if(size2==edge.getTripleCount() ) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2=size1==resultsizeV1=triple and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
							}
							if(edge.getDistinctObject()==edge.getTripleCount()) {
								if((size1+size2+edge.getDistinctSubject())<=temp) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1==resultsizeV1 object=triple (size1+size2+subject)<=temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
								if((size1+size2+edge.getDistinctSubject())>temp) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1==resultsizeV1 object=triple (size1+size2+subject)>temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
							}
							}
							if(size1==0.71&&size2==0.71&&v1Value==0.71&&v2Value==0.71) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2(0.71)=resultsizeV2(0.71) size1(0.71)==resultsizeV1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
							}
						}
							if(size1==edge.getDistinctSubject()) {
							if(bindingsize>(edge.getDistinctSubject()+size2)) {
									if(size2 != edge.getTripleCount()) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1=subject  bindingsize>(subject+size2)and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and  nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
									}
									if(size2 == edge.getTripleCount()) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2=triple size1=subject  bindingsize>(subject+size2)and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and  nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
									}

							}
							if(edge.getDistinctObject()==edge.getTripleCount() && bindingsize==size2 && bindingsize==v2Value) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge size1=subject object=triple bindingsize=ResultSizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

							
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"				//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
									}

							if(bindingsize<=(edge.getDistinctSubject()+size2)) {
								if(edge.getDistinctObject()!=edge.getTripleCount()) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1=subject object!=triple bindingsize<=(subject+size2)and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										
									if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
							if(edge.getDistinctObject()==edge.getTripleCount()) {
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  object=triple and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
							}
						}
						/*	if(size1==v1Value) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in  size2=resultsizeV2 size1=resultsizeV1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}	*/	

						}

						else {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and sub!=obj and minimum vertex is concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						// either join should return the same number of results
						resultsize.put(v2, (double) bindingsize);
					}

					else {// size2<size1
						
						if (v2.getNode().isConcrete()) {
							temp += nj;
							plan.add(new HashJoin(edge));
							resultsize.put(v1, (double) bindingsize);
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

							continue;
						}

						double bj = (size2 * CostModel.CQ) + size2 * osel * (edge.getTripleCount());
						//System.out.println(
								//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 8!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
									//	+ v1.getNode().isConcrete() + "--" + v2.getNode().isConcrete() + "--" + nj
									//	+ "--" + bj);

						if ((((edge.getDistinctSubject() == edge.getDistinctObject()))
								&& (resultsize.get(v1) != resultsize.get(v2))
								&& (edge.getDistinctObject() != edge.getTripleCount())
								&& (edge.getDistinctSubject() != size1) && (edge.getDistinctObject() != size1))) {
							temp += bj;
							plan.add(new BindJoin(v2, edge));
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

						}

						else if (edge.getDistinctSubject() == edge.getTripleCount()) {
							v1Value=resultsize.get(v1)==null?0:resultsize.get(v1);
							v2Value=resultsize.get(v2)==null?0:resultsize.get(v2);
						
							// //System.out.println(
							// //"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 equal triple subject and
							// minimum vertex is not concrete and
							// nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (bindingsize != size1) && (edge.getDistinctSubject()!=edge.getDistinctObject() ) && (size2!=edge.getDistinctObject()) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject())))) {
								if(v1Value!=0.71 && size1!=0.71 && v2Value!=0.71 && size2!=0.71) {
								if(bindingsize<=(size1+edge.getTripleCount())) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 bindingsize<=(size1+triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <=bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
								if(bindingsize>(size1+edge.getTripleCount())) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 bindingsize>(size1+triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

								}
								else	if(v1Value==0.71 && size1==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1(0.71)=size1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value==0.71 && size2==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV2(0.71)=size2(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value!=0.71 && size2!=0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}


						}
							else	if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1)) && (bindingsize == size1) )) {
								if (size2 != size1) {
							if(size2!=edge.getDistinctObject()) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ resultsize.get(v2));

								}
							}
							if(size2==edge.getDistinctObject()) {
								if(bindingsize<=(edge.getDistinctSubject()+size2)) {
									
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize  size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ resultsize.get(v2));

							}
								}
								if(bindingsize>(edge.getDistinctSubject()+size2)) {
									
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize  size2==object bindingsize>(subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
								temp += bj;
								if(bindingsize==size1 &&size1==v1Value&&edge.getDistinctSubject() == edge.getTripleCount() )
								plan.add(new BindJoin(v1, edge));
								else
									plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ resultsize.get(v2));

							}
								}
						}
							}
								else	if  (size2 == size1) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
								}
								else if(size1==edge.getDistinctObject()) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize size1=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
								}
								else if(size2==edge.getDistinctObject()) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize size2=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
								}

							}
							
							else	if ((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (edge.getDistinctSubject() == edge.getDistinctObject()))) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize object=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if(v2Value!=size2) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize object=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

								}
								}
									if(v2Value==size2) {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize object=subject value2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v2, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ resultsize.get(v2));

									}
									}

								}
							else if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1)  && (size2==edge.getDistinctObject()) ))) {
								if(v1Value!=0.71 && size1!=0.71 && v2Value!=0.71 && size2!=0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
								if(v1Value==0.71 && size1==0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1(0.71)=size1(0.71) size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
							}
							
							 else	if ((edge.getDistinctSubject() == edge.getTripleCount()  && (bindingsize == size2)&& (bindingsize == edge.getDistinctSubject()))
										&& ((v1Value == size1) && (size1==edge.getDistinctObject())))  {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject=binding=size2 v1Value=size1=object v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v2, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ resultsize.get(v2));

										}
		
									}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 != size2)
									&& (size1 != v1Value)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 == size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size=object size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount()) && (size1 == bindingsize)
									&& (size2 != edge.getDistinctObject())) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in resultsize equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (edge.getTripleCount() == size1)) {
								if(size2!=0.71 && v2Value!=0.71) {
								if(size1!=edge.getDistinctObject()) {
								if(size2!=v2Value) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(size2==v2Value) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1 size2==ResultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								}
								if(size1==edge.getDistinctObject()) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								}
								else 	if(size2==0.71 && v2Value==0.71) {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v2, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										}
										}

							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == v2Value) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject())) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size1 == v1Value)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size=resultsizeV1  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}

							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == v2Value) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject())) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else {
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
						} else if ((edge.getDistinctSubject() + edge.getDistinctObject()) == size2) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  in subject+object equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}

						} else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == edge.getTripleCount()))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 in subject=object=triple equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((size2 == edge.getDistinctObject())) {
							if(size1!=v1Value)
							{
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  v2 size2=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
							else	if(size1==v1Value) {
								if(size2 > temp) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 > temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(size2 <= temp) {
									if(bindingsize>(size1+ edge.getTripleCount())) {
									if(size2>edge.getDistinctSubject()) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 <= temp  bindingsize>(size1+ triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
									}
									if(size2<=edge.getDistinctSubject()) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 <= temp size2<=subject bindingsize>(size1+ triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
									}

									}
									if(bindingsize<=(size1+ edge.getTripleCount())) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 <= temp  bindingsize<=(size1+ triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj >bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
									}
									}

								}

						}

						else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size2))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  subject+object+size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}

						else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size1))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 in subject=object=size1 equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((edge.getDistinctObject() + edge.getTripleCount() == size2)) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in object+triple=2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						else if(size2==v2Value){
							if(size1==edge.getDistinctSubject())
							{
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
							else	if(size1==edge.getDistinctSubject()) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in v2 size2=resultsizeV2 size1=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
							else	if(size1==v1Value) {
								if(bindingsize>(edge.getDistinctSubject()+size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in v2 size2=resultsizeV2 size1=resultsizeV1 bindingsize>(subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <=bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(bindingsize<=(edge.getDistinctSubject()+size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in v2 size2=resultsizeV2 size1=resultsizeV1 bindingsize<=(subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}		

					}
						else if(size1==0.71 &&v1Value==0.71) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1(0.71)=v1Value(0.71) bindingsize>(size1+ResultSizeV1) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}

						else {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in Obj!=Sub and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
											//	+ v2Value+"--"+size2+"--"+resultsize.get(v2));

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						resultsize.put(v1, (double) bindingsize);
					}
				}
			}
			if (temp < cost) {
				cost = temp;
				candidates.clear();
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp<cost:" + candidates);
				
				candidates.add(plan);
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp<cost 1:" + candidates);
				
				RelativeError.est_resultSize.putAll(resultsize);
			}
			if (temp == cost) {
				candidates.add(plan);
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp==cost:" + candidates);
					
				RelativeError.est_resultSize.putAll(resultsize);
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp==cost 1:" + candidates);
				
			}
			//System.out.println(
			//		"--------------------------------------------------------------------------------------------------------------------");

		}
		//System.out.println(//"!!!!!!!!!!!!!!!!!!This is binding size:" + candidates);
		StageGen sg = new StageGen(null);
		sg.repeat++;
		return candidates;
	}
/*	private Set<Set<EdgeOperator>> getOptimal(Set<EdgeOperator> firststage, Iterator<List<Edge>> orders) {
		Map<Vertex, Double> resultsize_ = new HashMap<Vertex, Double>();
//		Double size1;
//		Double size2;
		for (EdgeOperator eo : firststage) {
			Edge edge = eo.getEdge();
			Vertex v1 = edge.getV1();
			Vertex v2 = edge.getV2();
			if (v1.getNode().isConcrete()) {
				resultsize_.put(v1, 1.0);
				resultsize_.put(v2, (double) edge.getDistinctSubject());
//				resultsize_.put(v2, (double)edge.getTripleCount()/edge.getDistinctSubject());
			} else {
				resultsize_.put(v2, 1.0);
//				resultsize_.put(v1, (double)edge.getTripleCount()/edge.getDistinctObject());
				resultsize_.put(v1, (double) edge.getDistinctObject());

			}

			//System.out.println(//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 4!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
					+ "--" + resultsize_.get(v1) + "--" + resultsize_.get(v2) + "--" + edge.getTripleCount() + "--"
					+ edge.getDistinctSubject() + "--" + edge.getDistinctObject());

		}

		if (Config.relative_error && resultsize_.size() > 0) {
			// RelativeError.est_resultSize.clear();
			RelativeError.est_resultSize.putAll(resultsize_);
		}
		double cost = Double.MAX_VALUE;
		Set<Set<EdgeOperator>> candidates = new HashSet<Set<EdgeOperator>>();
		while (orders.hasNext()) {
			List<Edge> order = orders.next();
			// for a list of edges generate the corresponding list of operator
			// that has the minimum cost. That is, for each edge, choose either
			// hash join or bind join that has lower cost
			Set<EdgeOperator> plan = new HashSet<EdgeOperator>();
			// record bound vertices and their number of bindings
			Map<Vertex, Double> resultsize = new HashMap<Vertex, Double>(resultsize_);
			double temp = 0;
			j++;
			if(j>21)
				break;
			//System.out.println("These are the numbers of orders in ExhOptimisers:"+j+"--"+order);
	
			for (Edge e : order) {
				Edge edge = (Edge) e;
				Vertex v1 = edge.getV1();
				Vertex v2 = edge.getV2();
				// add all concrete vertices and their binding number (1) to the record
				if (v1.getNode().isConcrete()) {
					resultsize.put(v1, 1.0);
				}
				if (v2.getNode().isConcrete()) {
					resultsize.put(v2, 1.0);
				}

		Double	size1 = resultsize.get(v1);
		Double	size2 = resultsize.get(v2);

				double nj = 1 * CostModel.CQ + edge.getTripleCount() * CostModel.CT;
				// double nj = 1*CostModel.CQ + CostModel.CT;
              //  double v1Value = 0,v2Value = 0;
				//System.out.println(
						//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 5!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
								+ v1.getNode() + "--" + v2.getNode() + "--" + resultsize.get(v1) + "--"
								//+ resultsize.get(v2) + "--" + edge.getDistinctSubject() + "--"
								+ edge.getDistinctObject() + "--" + edge.getTripleCount() + "--" + size1 + "--" + size2
								+ "--" + nj + "--" + CostModel.CQ + "--" + CostModel.CT);

				if (size1 == null && size2 == null) {// if this is the first edge, use HashJoin.
					temp += nj;

					//	if (edge.getDistinctSubject() == edge.getTripleCount())
					//if(edge.getDistinctSubject()>edge.getDistinctObject())
					//	plan.add(new HashJoin(edge));
					//else 
					if (edge.getTripleCount()== edge.getDistinctObject())
						//	plan.add(new HashJoin(edge));
							plan.add(new HashJoin(edge));
					else if ((edge.getDistinctSubject() == edge.getTripleCount()) && ((edge.getDistinctObject()*2.1)>edge.getTripleCount()))
						plan.add(new BindJoin(v1,edge));
				
					else if ((edge.getDistinctSubject() == edge.getTripleCount()) && ((edge.getDistinctObject()*2.1)<=edge.getTripleCount()))
						plan.add(new HashJoin(edge));
				
					else	if (edge.getTripleCount()!= edge.getDistinctObject()&& edge.getDistinctSubject()!= edge.getDistinctObject() && (edge.getDistinctSubject() + edge.getDistinctObject())<edge.getTripleCount())
						//	plan.add(new HashJoin(edge));
							plan.add(new BindJoin(v1,edge));
					else	if (edge.getTripleCount()!= edge.getDistinctObject()&& edge.getDistinctSubject()!= edge.getDistinctObject() && (edge.getDistinctSubject() + edge.getDistinctObject())>=edge.getTripleCount())
						//	plan.add(new HashJoin(edge));
							plan.add(new HashJoin(edge));
					
					else if ((edge.getDistinctSubject() ==0.71) &&(edge.getDistinctObject() ==0.71))
						plan.add(new HashJoin(edge));	
					else if ((edge.getDistinctSubject() + edge.getDistinctObject())<=edge.getTripleCount())
						plan.add(new BindJoin(v1,edge));
						else if ((edge.getDistinctSubject() == edge.getTripleCount()) && (edge.getTripleCount()>edge.getDistinctSubject()))
							plan.add(new HashJoin(edge));
						else if ((edge.getDistinctSubject() == edge.getTripleCount()) )
							plan.add(new HashJoin(edge));
					
						else if (edge.getDistinctSubject() == edge.getTripleCount())
						if(edge.getDistinctObject()==1.0)
							plan.add(new HashJoin(edge));
					
						else if ((edge.getDistinctSubject() + edge.getDistinctObject())>edge.getTripleCount() 
								&& !((edge.getDistinctSubject() ==0.71) &&(edge.getDistinctObject() ==0.71)))
						//	plan.add(new HashJoin(edge));
							plan.add(new BindJoin(v1,edge));
						
						else
						plan.add(new BindJoin(v1,edge));
				//	else
				//		plan.add(new HashJoin(edge));
					
	resultsize.put(v1, (double) edge.getTripleCount());
					resultsize.put(v2, (double) edge.getTripleCount());
					//System.out.println(//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
							+ edge.getDistinctSubject() + "--"
							+ edge.getDistinctObject() + "--" + edge.getTripleCount()+ "--" + plan.toString() + "--" + resultsize.get(v1) + "--" //+ resultsize.get(v2) + "--"
							+ edge.getTripleCount());

				} else {
					//System.out.println(
							//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 6.0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
									+ size1 + "--" + size2);
					size1 = size1 == null ? edge.getDistinctSubject() : size1;
//					size2 = size2 == null?edge.getDistinctObject()+edge.getTripleCount():size2;
					size2 = size2 == null ? edge.getDistinctObject() : size2;

					double ssel = 1d / edge.getDistinctSubject();
					double osel = 1d / edge.getDistinctObject();
					int bindingsize = (int) Math.max(size1 * ssel * edge.getTripleCount() * size2 * osel, 2);
				double	v1Value=resultsize.get(v1)==null?0:resultsize.get(v1);
				double	v2Value=resultsize.get(v2)==null?0:resultsize.get(v2);
				
					//System.out.println(
							//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 6!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
									+ v1.getNode() + "--" + v2.getNode() + "--bindingsize:" + bindingsize + "--size1:" + size1 + "--size2:"
									+ size2 + "--ssel:" + ssel + "--osel:" + osel + "--subject:" + edge.getDistinctSubject() + "--object:"
									+ edge.getDistinctObject() + "--triple:" + edge.getTripleCount() + "--resultsizeV1:"
									+ resultsize.get(v1) + "--resultsizeV2" //+ resultsize.get(v2)+"--temp:"+temp);

					if (size1 <= size2) {
						// if the minimum vertex is concrete (and a concrete vertex should always
						// be the minimum vertex since its binding size is 1), use hash join
						if (v1.getNode().isConcrete()) {
							temp += nj;
							plan.add(new HashJoin(edge));
							resultsize.put(v2, (double) bindingsize);
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

							continue;
						}

						double bj = size1 * CostModel.CQ + size1 * ssel * edge.getTripleCount();
						
						//System.out.println(
								//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 7!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
										+ v1.getNode().isConcrete() + "--" + v2.getNode().isConcrete() + "--" + nj
										+ "--" + bj+"--"+size1+"--"+size2);

						if ((((edge.getDistinctSubject() == edge.getDistinctObject()))
								&& (resultsize.get(v1) != resultsize.get(v2))
								&& (edge.getDistinctObject() != edge.getTripleCount())
								&& (edge.getDistinctSubject() != size1) && (edge.getDistinctObject() != size1))) {
							temp += bj;
							plan.add(new BindJoin(v1, edge));
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

						} else if (edge.getDistinctSubject() == edge.getTripleCount()) {
							// //System.out.println(
							// //"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in equal triple subject
							// and minimum vertex is not concrete and
							// nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (bindingsize != size1) && (edge.getDistinctSubject()!=edge.getDistinctObject() ) && (size2!=edge.getDistinctObject()) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()) ))) {
								if(v1Value!=0.71 && size1!=0.71) {
								if(v2Value!=size2) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(v2Value==size2) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 resultsizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}
								
								else	if(v1Value==0.71 && size1==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1(0.71)=size1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value==0.71 && size2==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV2(0.71)=size2(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value!=0.71 && size2!=0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}


						}
							else	if ((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (bindingsize == size1))) {
								 if(size1==size2) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
	
								}
								 else	if ((edge.getDistinctSubject() == edge.getTripleCount()  && (bindingsize == size2)&& (bindingsize == edge.getDistinctSubject()))
											&& ((v1Value == size1) && (size1==edge.getDistinctObject())))  {
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject=binding=size2 v1Value=size1=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
											if (nj <= bj) {
												temp += bj;
												plan.add(new BindJoin(v1, edge));
												//System.out.println(
														//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
																//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
																//+ v2Value);

											} else {
												temp += nj;
												plan.add(new HashJoin(edge));
												//System.out.println(
														//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
																//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
																//+ resultsize.get(v2));

											}
			
										}

									else if(size2==edge.getDistinctObject()) {
									if(size2>temp) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize size2=object size2>temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
									}
									
									if(size2<=temp) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize size2=object size2<=temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
									}

								}

								 else {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v1, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ resultsize.get(v2));

										}
									}

							}
					//		else	if (((edge.getDistinctSubject() == edge.getTripleCount()
					//				&& (v1Value == size1) && (bindingsize == size1) && (size2 == size1)))) {
					//										}

							else if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1)  && (size2==edge.getDistinctObject()) ))) {
								
								if(v1Value!=0.71 && size1!=0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
								else	if(v1Value==0.71 && size1==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1(0.71)=size1(0.71) size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}

							}
							else	if ((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (edge.getDistinctSubject() == edge.getDistinctObject()))) {
								if(v2Value!=size2) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize object=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ resultsize.get(v2));

							}
							}
								if(v2Value==size2) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject resultsizeV1=size1 size1=bindingsize object=subject value2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

								}
								}

							}
							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (edge.getTripleCount() == size1)) {
								if(size2 != v2Value) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  triple=subject=size1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								
								if(size2 == v2Value) {
								if((size1+size2+edge.getDistinctSubject())>=temp) { 
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  triple=subject=size1 size2=resultSizeV2 (size1+size2+subject)>temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if((size1+size2+edge.getDistinctSubject())<temp) { 
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  triple=subject=size1 size2=resultSizeV2 (size1+size2+subject)<=temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}

								}
							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 != size2)
									&& (size1 != v1Value)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							} else if (((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size1 == v1Value) && (bindingsize != size1) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()))) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size=resultsizeV1  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}

							} else if ((edge.getDistinctSubject() == edge.getTripleCount()) && (size1 == bindingsize)
									&& (size2 != edge.getDistinctObject())&& (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()) ) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in resultsize equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 == size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size=object size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							} else if (((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == v2Value) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject()) )) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  subject=triple size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else {
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
						}

						else if ((edge.getDistinctSubject() + edge.getDistinctObject()) == size2) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject+object equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == edge.getTripleCount()))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject=object=triple equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size1))) {
						if(edge.getDistinctSubject() != 0.71 && edge.getDistinctObject() != 0.71) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject=object=size1 equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						if(edge.getDistinctSubject() == 0.71 && edge.getDistinctObject() == 0.71) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge in subject(0.71)=object(0.71)=size1 equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						}

						else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size2))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge subject+object+size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}

						else if ((size2 == edge.getDistinctObject())) {
							if(size1!=v1Value) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
							
							else	if(size1==v1Value) {
							if(bindingsize>(size1+v1Value)) {
								if(size1!=0.71 &&v1Value!=0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1=v1Value bindingsize>(size1+ResultSizeV1) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								
								if(size1==0.71 &&v1Value==0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1(0.71)=v1Value(0.71) bindingsize>(size1+ResultSizeV1) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}
							if(bindingsize<=(size1+v1Value)) {
								
								if(bindingsize!=size1) {
									if( v2Value!=size2) {
								if((bindingsize>(edge.getDistinctSubject()+size2)) ) {
										//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1=v1Value and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if((bindingsize<=(edge.getDistinctSubject()+size2)) ) {
									//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object size1=v1Value bindingsize<=(Subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
									}
									if( v2Value==size2) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object=size1=v1Value=v2Value and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
									}
								}
								if(bindingsize==size1) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge   size2=object bindingsize=size1=v1Value and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}
								}

						} else if ((edge.getDistinctObject() + edge.getTripleCount() == size2)) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in object+subject+size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						else if(size2==v2Value){
							if(size1!=edge.getDistinctSubject()) {
							if(size1!=v1Value) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
							if(size1==v1Value) {
							if(size1!=0.71&&size2!=0.71&&v1Value!=0.71&&v2Value!=0.71) {
							if(edge.getDistinctObject()!=edge.getTripleCount()) {
								if(size2!=edge.getTripleCount()) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1==resultsizeV1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
								if(size2==edge.getTripleCount() ) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2=size1==resultsizeV1=triple and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
							}
							if(edge.getDistinctObject()==edge.getTripleCount()) {
								if(bindingsize<=(edge.getDistinctSubject()+size2)) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1==resultsizeV1 object=triple (size1+size2+subject)<=temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
								if(bindingsize>(edge.getDistinctSubject()+size2)) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1==resultsizeV1 object=triple (size1+size2+subject)>temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
							}
							}
							if(size1==0.71&&size2==0.71&&v1Value==0.71&&v2Value==0.71) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2(0.71)=resultsizeV2(0.71) size1(0.71)==resultsizeV1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
							}
							}
						}
							if(size1==edge.getDistinctSubject()) {
							if(bindingsize>(edge.getDistinctSubject()+size2)) {
									if(size2 != edge.getTripleCount()) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1=subject  bindingsize>(subject+size2)and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and  nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
									}
									if(size2 == edge.getTripleCount()) {
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2=triple size1=subject  bindingsize>(subject+size2)and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and  nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj000000000!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
									}

							}
							if(edge.getDistinctObject()==edge.getTripleCount() && bindingsize==size2 && bindingsize==v2Value) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge size1=subject object=triple bindingsize=ResultSizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

							
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v1, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"				//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
									}

							if(bindingsize<=(edge.getDistinctSubject()+size2)) {
								if(edge.getDistinctObject()!=edge.getTripleCount()) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in size2=resultsizeV2 size1=subject object!=triple bindingsize<=(subject+size2)and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										
									if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}
						/*	if(edge.getDistinctObject()==edge.getTripleCount()) {
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  object=triple and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
								}--
							}
						}
							if(size1==v1Value) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in  size2=resultsizeV2 size1=resultsizeV1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}--
							}	

						}

						else {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and sub!=obj and minimum vertex is concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v1, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge and minimum vertex is concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						// either join should return the same number of results
						resultsize.put(v2, (double) bindingsize);
					}

					else {// size2<size1
						
						if (v2.getNode().isConcrete()) {
							temp += nj;
							plan.add(new HashJoin(edge));
							resultsize.put(v1, (double) bindingsize);
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

							continue;
						}

						double bj = (size2 * CostModel.CQ) + size2 * osel * (edge.getTripleCount());
						//System.out.println(
								//"!!!!!!!!!!!!!!!!!!!!!!!This is here in ExhOptimiser 8!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
										+ v1.getNode().isConcrete() + "--" + v2.getNode().isConcrete() + "--" + nj
										+ "--" + bj);

						if ((((edge.getDistinctSubject() == edge.getDistinctObject()))
								&& (resultsize.get(v1) != resultsize.get(v2))
								&& (edge.getDistinctObject() != edge.getTripleCount())
								&& (edge.getDistinctSubject() != size1) && (edge.getDistinctObject() != size1))) {
							temp += bj;
							plan.add(new BindJoin(v2, edge));
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
											//+ temp + "--" + plan.toString() + "--" + resultsize.get(v1) + "--"
											//+ resultsize.get(v2));

						}

						else if (edge.getDistinctSubject() == edge.getTripleCount()) {
							v1Value=resultsize.get(v1)==null?0:resultsize.get(v1);
							v2Value=resultsize.get(v2)==null?0:resultsize.get(v2);
						
							// //System.out.println(
							// //"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 equal triple subject and
							// minimum vertex is not concrete and
							// nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (bindingsize != size1) && (edge.getDistinctSubject()!=edge.getDistinctObject() ) && (size2!=edge.getDistinctObject()) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject())))) {
								if(v1Value!=0.71 && size1!=0.71 && v2Value!=0.71 && size2!=0.71) {
								if(bindingsize<=(size1+edge.getTripleCount())) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 bindingsize<=(size1+triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <=bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
								if(bindingsize>(size1+edge.getTripleCount())) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 bindingsize>(size1+triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

								}
								else	if(v1Value==0.71 && size1==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1(0.71)=size1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value==0.71 && size2==0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV2(0.71)=size2(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}
								else if(v2Value!=0.71 && size2!=0.71) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
								}


						}
							else	if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1)) && (bindingsize == size1) )) {
								if (size2 != size1) {
							if(size2!=edge.getDistinctObject()) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ resultsize.get(v2));

								}
							}
							if(size2==edge.getDistinctObject()) {
								if(bindingsize<=(edge.getDistinctSubject()+size2)) {
									
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize  size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ resultsize.get(v2));

							}
								}
								if(bindingsize>(edge.getDistinctSubject()+size2)) {
									
								//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize  size2==object bindingsize>(subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
								temp += bj;
								if(bindingsize==size1 &&size1==v1Value&&edge.getDistinctSubject() == edge.getTripleCount() )
								plan.add(new BindJoin(v1, edge));
								else
									plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ resultsize.get(v2));

							}
								}
						}
							}
								else	if  (size2 == size1) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
								}
								else if(size1==edge.getDistinctObject()) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize size1=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
								}
								else if(size2==edge.getDistinctObject()) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize size2=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first v2 edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

									}
								}

							}
							
							else	if ((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1) && (edge.getDistinctSubject() == edge.getDistinctObject()))) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize object=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if(v2Value!=size2) {
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize object=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ resultsize.get(v2));

								}
								}
									if(v2Value==size2) {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size1=bindingsize object=subject value2=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v2, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ resultsize.get(v2));

									}
									}

								}
							else if (((edge.getDistinctSubject() == edge.getTripleCount()
									&& (v1Value == size1)  && (size2==edge.getDistinctObject()) ))) {
								if(v1Value!=0.71 && size1!=0.71 && v2Value!=0.71 && size2!=0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1=size1 size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
								if(v1Value==0.71 && size1==0.71) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 triple=subject resultsizeV1(0.71)=size1(0.71) size2==object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
							}
							
							 else	if ((edge.getDistinctSubject() == edge.getTripleCount()  && (bindingsize == size2)&& (bindingsize == edge.getDistinctSubject()))
										&& ((v1Value == size1) && (size1==edge.getDistinctObject())))  {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge triple=subject=binding=size2 v1Value=size1=object v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v2, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ resultsize.get(v2));

										}
		
									}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 != size2)
									&& (size1 != v1Value)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == edge.getDistinctObject()) && (size1 == size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size=object size1=size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount()) && (size1 == bindingsize)
									&& (size2 != edge.getDistinctObject())) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in resultsize equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (edge.getTripleCount() == size1)) {
								if(size2!=0.71 && v2Value!=0.71) {
								if(size1!=edge.getDistinctObject()) {
								if(size2!=v2Value) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(size2==v2Value) {
								if((size1+size2+edge.getDistinctSubject())>temp) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1 size2==ResultsizeV2 size1+size2+subject>temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if((size1+size2+edge.getDistinctSubject())<=temp) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1 size2==ResultsizeV2 size1+size2+subject<=temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}

								}
								}
								if(size1==edge.getDistinctObject()) {
									//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1=object and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								}
								else 	if(size2==0.71 && v2Value==0.71) {
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 triple=subject=size1(0.71) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj <= bj) {
											temp += bj;
											plan.add(new BindJoin(v2, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										}
										}

							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == v2Value) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject())) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size1 == v1Value)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size=resultsizeV1  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}

							} else if ((edge.getDistinctSubject() == edge.getTripleCount())
									&& (size2 == v2Value) && (bindingsize != size2)&& (bindingsize != edge.getDistinctSubject())) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 subject=triple size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}

							else {
								if (nj > bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!else this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
						} else if ((edge.getDistinctSubject() + edge.getDistinctObject()) == size2) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  in subject+object equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}

						} else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == edge.getTripleCount()))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 in subject=object=triple equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((size2 == edge.getDistinctObject())) {
							if(size1!=v1Value)
							{
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge  v2 size2=object  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
							else	if(size1==v1Value) {
								if(size2 > temp) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 > temp and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(size2 <= temp) {
									if(bindingsize>(size1+ edge.getTripleCount())) {
									if(size2>edge.getDistinctSubject()) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 <= temp  bindingsize>(size1+ triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj > bj) {
										temp += bj;
										plan.add(new BindJoin(v2, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
									}
									if(size2<=edge.getDistinctSubject()) {
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 <= temp size2<=subject bindingsize>(size1+ triple) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
									}

									}
									if(bindingsize<=(size1+ edge.getTripleCount())) {
										if(bindingsize<=(edge.getDistinctSubject()+size2)){
										//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 <= temp  bindingsize<=(size1+ triple)  bindingsize<=(size2+ subject) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
									if (nj <= bj) {
										temp += bj;
										plan.add(new BindJoin(v1, edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									} else {
										temp += nj;
										plan.add(new HashJoin(edge));
										//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
														//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
														//+ v2Value);

									}
										}
										
										if(bindingsize>(edge.getDistinctSubject()+size2)){
											//System.out.println(
												//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 size2=object size1=v1Value  size2 <= temp  bindingsize>(size1+ triple)  bindingsize<=(size2+ subject) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
										if (nj > bj) {
											temp += bj;
											plan.add(new BindJoin(v1, edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										} else {
											temp += nj;
											plan.add(new HashJoin(edge));
											//System.out.println(
													//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
															//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
															//+ v2Value);

										}
											}

									}
									}

								}

						}

						else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size2))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  subject+object+size2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}

						else if ((edge.getDistinctSubject() == edge.getDistinctObject())
								&& ((edge.getDistinctObject() == size1))) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2 in subject=object=size1 equal and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is not the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						} else if ((edge.getDistinctObject() + edge.getTripleCount() == size2)) {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in object+triple=2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2  and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						else if(size2==v2Value){
							if(size1==edge.getDistinctSubject())
							{
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in size2=resultsizeV2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj <= bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
							else	if(size1==edge.getDistinctSubject()) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in v2 size2=resultsizeV2 size1=subject and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
							}
							else	if(size1==v1Value) {
								if(bindingsize>(edge.getDistinctSubject()+size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in v2 size2=resultsizeV2 size1=resultsizeV1 bindingsize>(subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <=bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
								if(bindingsize<=(edge.getDistinctSubject()+size2)) {
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge in v2 size2=resultsizeV2 size1=resultsizeV1 bindingsize<=(subject+size2) and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

								if (nj <= bj) {
									temp += bj;
									plan.add(new BindJoin(v2, edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								} else {
									temp += nj;
									plan.add(new HashJoin(edge));
									//System.out.println(
											//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
													//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
													//+ v2Value);

								}
								}
							}		

					}
						else {
							//System.out.println(
									//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 in Obj!=Sub and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");

							if (nj > bj) {
								temp += bj;
								plan.add(new BindJoin(v2, edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												+ v2Value+"--"+size2+"--"+resultsize.get(v2));

							} else {
								temp += nj;
								plan.add(new HashJoin(edge));
								//System.out.println(
										//"!!!!!!!!!!!!!!!!!!!!!!!this is the first edge v2 and minimum vertex is not concrete and not nj>bj!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:"
												//+ temp + "--" + plan.toString() + "--" + v1Value + "--"
												//+ v2Value);

							}
						}
						resultsize.put(v1, (double) bindingsize);
					}
				}
			}
			if (temp < cost) {
				cost = temp;
				candidates.clear();
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp<cost:" + candidates);
				
				candidates.add(plan);
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp<cost 1:" + candidates);
				
				RelativeError.est_resultSize.putAll(resultsize);
			}
			if (temp == cost) {
				candidates.add(plan);
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp==cost:" + candidates);
					
				RelativeError.est_resultSize.putAll(resultsize);
				//System.out.println(//"!!!!!!!!!!!!!!!!!!This is candidate temp==cost 1:" + candidates);
				
			}
			//System.out.println(
					"--------------------------------------------------------------------------------------------------------------------");

		}
		//System.out.println(//"!!!!!!!!!!!!!!!!!!This is binding size:" + candidates);
		StageGen sg = new StageGen(null);
		sg.repeat++;
		return candidates;
	}
*/
	private Set<EdgeOperator> finalize(Set<EdgeOperator> firststage, Set<Set<EdgeOperator>> plans) {
		Set<EdgeOperator> optimal = null;
		int count = 0;
		for (Set<EdgeOperator> plan : plans) {
			//System.out.println("This is final value of optimal in ExhOptimiser plan:" + plan);

			/*
			 * List<List<EdgeOperator>> temp; temp = parallelise(firststage,plan); //keep
			 * the plan with minimum depth, which means minimum //steps are needed to
			 * execute a query if(optimal == null) { optimal = temp; } else
			 * if(temp.size()<optimal.size()) { optimal = temp; }
			 */
			int _count = 0;
			for (EdgeOperator eo : plan) {
				if (eo instanceof HashJoin) {
					_count++;
				}
			}
			if (_count > count) {
				count = _count;
				plan.addAll(firststage);
				optimal = plan;
			}
		}
		if (optimal == null) {
			optimal = plans.iterator().next();
			optimal.addAll(firststage);
		}
//System.out.println("This is final value of optimal in ExhOptimiser:" + optimal);

		//System.out.println("This is final value of optimal in ExhOptimiser:" + StageGen.edges_order.toString());
Map<Double, Edge> edgesOrderTree =new TreeMap<>(StageGen.edges_order);
//System.out.println("This is hashmap for ordering in ExhOptimiser1221:" +edgesOrderTree);
//System.out.println("This is hashmap for ordering in ExhOptimiser1221221:" +optimal);

EdgeOperator[] a =new EdgeOperator[300];

		//for (Entry<Integer, Edge> e : StageGen.edges_order.entrySet()) {
int q=-1;
for (HashMap.Entry<Double, Edge> e : edgesOrderTree.entrySet()) {
			// is is hashmap for ordering in ExhOptimi
		
	for (EdgeOperator o2 : optimal) {
		
		//for(int i=0;i<100;i++) {
		
	//	//System.out.println("This is the 1st step in new array to be processed:"+o2.getEdge()+"--"+o2.getStartVertex()+"--"+e.getValue().getDistinctObject()+"--"+e.getValue().getDistinctSubject()+"--"+e.getValue().getTriple()+"--"+e.getValue().getV1()+"--"+e.getValue().getV2());
		//System.out.println("This is the 1st step in new array to be processed:"+q+"--"+o2.getEdge());
Triple	ab	=o2.getEdge().getTriple();
Triple cd=e.getValue().getTriple();

Vertex	ab1	=o2.getEdge().getV1();
Vertex cd1=e.getValue().getV1();

Vertex	ab2	=o2.getEdge().getV2();
Vertex cd2=e.getValue().getV2();
//		if (o2.getEdge() == e.getValue()) {
if (ab.equals(cd) && ab1.equals(cd1) && ab2.equals(cd2)) {
	
			q++;		
						//	for(int n=0;n<i;n++) {

								a[q] = o2;
							//System.out.println("This is the new array to be processed:"+q+"--"+a[q]);
							//}
						
					//
					//System.out.println(
					//	"This is hashmap for ordering in ExhOptimiser11:" + o2.getEdge() + "--" +e.getKey() +"--"+(Vertex)e.getValue().getV1()+"("+(Node)e.getValue().getV1().getNode()+")/"+(Vertex)e.getValue().getV2()+"("+(Node)e.getValue().getV2().getNode()+")");
						
					operators_reorder.add(o2);
					// i++ ;
				}
				
		//}		
		
			}
		}
//Arrange the triple pattern for arranging them by minimum cardinality
for(int i=0;i<=q;i++) {
	//System.out.println("This is the final array-1:"+a[i]);
	}
int k=0,l=0;
EdgeOperator[] new_e= new EdgeOperator[300];
Integer[] new_i = new Integer[300];
for(int i=0;i<=q;i++) {
	//System.out.println("This is the final array0:"+a[i]);
	k=0;
	for(int j=0;j<=q;j++)	{
		////System.out.println("This is the final array:"+a[j]+"--"+k);
		
		if((a[i].getEdge().getV1()==a[j].getEdge().getV2()) || (a[i].getEdge().getV1()==a[j].getEdge().getV1()) )
		{
			//if(k>0)
			//System.out.println("This is the final array:"+a[j]+"--"+k);
			new_e[l]=a[j];
			new_i[l]=k;
		k++;l++;
		}	//if((a[i].getV1()!=a[j].getV2()) || (a[i].getV1()!=a[j].getV1()) )
			//	//System.out.println("This is the final array:"+a[i]+"--"+a[j]);
			
				//	//System.out.println("This is the final array1:"+a[i]+"--"+a[j]);
			
			// Iterator<EdgeOperator> ij=optimal.iterator();
	}
	//if(k>0)
		
}
//Removing triple pattern with no connection with other triple pattern
for(int i=0;i<=l;i++) {
	//System.out.println("This is getting closer to array:"+new_e[i]+"--"+new_i[i]);
if(new_i[i]==new_i[i+1])
{	//new_e[i]=null;
	//new_i[i]=null;
	new_e=ArrayUtils.remove(new_e, i);	
	new_i=ArrayUtils.remove(new_i, i);	
}
}
for(int i=0;i<=l;i++) {
	//System.out.println("This is getting more closer to array:"+new_e[i]+"--"+new_i[i]);

}
EdgeOperator[] final_edge = new EdgeOperator[300];
Set<Edge> set_edge;
final_edge[0]=new_e[0];
//Remove repeating triples
//set_edge = Set.of(final_edge);
for(int i=0;i<=l;i++) {
	////System.out.println("This is more getting closer to array1:"+final_edge[i]+"--"+Arrays.asList(final_edge));
	
	if(Arrays.asList(final_edge).contains(new_e[i]))
		continue;
		else {
		//	//System.out.println("This is more getting closer to array2:"+final_edge[i]+"--"+Arrays.asList(final_edge));
		
			final_edge[i]=new_e[i];
		}

	//set_edge=Set.of(final_edge);


	/* if(i==0)
  {   
 final_edge[i]=new_e[i];
  //System.out.println("This is more getting closer to array2:"+final_edge[i]);
  
  }*/
//	  if(i>0) {
		//  //System.out.println("This is more getting closer to array3:"+final_edge[i]);
/*		  if(!ArrayUtils.isNotEmpty(final_edge))
		  {
			  final_edge[i]=new_e[i];
				//System.out.println("This is more getting closer to array2:"+final_edge[i]+"--"+new_i[i]);

		  }
			  else {
//System.out.println("This is more getting closer to array3:"+new_e[i]+"--"+new_i[i]);
set_edge=Set.of(final_edge);
if(set_edge.contains(new_e[i]))
	continue;
	else
		final_edge[i]=new_e[i];
  }*/
  }
//final_edge. .removeIf(Objects::isNull);
//operators_reorder1 = Array.as
//Remove null triples
operators_reorder1.addAll(Arrays.asList(final_edge));
operators_reorder1.remove(null);
//for(int i=0;i<final_edge.length;i++)
	//System.out.println("This is more getting closer to array5:"+operators_reorder1);

	// while(ij.hasNext())
		// //System.out.println("This is hashmap for ordering in
		// ExhOptimiser:"+operators_reorder+"--"+ij.next());

/*for(int i=0;i<edgesOrderTree.size();i++) {
	for(int j=0;j<=i;j++)
		//System.out.println("This is the new array to be processed:"+a[i][j]);
}*/		

		//System.out.println("This is operator_reorder in ExhOptimiser:" + operators_reorder);
		//System.out.println("THis is towards solution of problem in ExhOptimiser");
		
		return operators_reorder;

//		return optimal;

	}

	/*
	 * private List<List<EdgeOperator>> parallelise(List<List<EdgeOperator>>
	 * firststage, List<EdgeOperator> plan) { List<List<EdgeOperator>> prllplan =
	 * new ArrayList<List<EdgeOperator>>(); prllplan.add(new
	 * ArrayList<EdgeOperator>(firststage.get(0))); List<Set<Vertex>> dependency =
	 * new ArrayList<Set<Vertex>>(); //initial dependency dependency.add(new
	 * HashSet<Vertex>()); for(EdgeOperator eo:prllplan.get(0)) {
	 * dependency.get(0).add(eo.getEdge().getV1());
	 * dependency.get(0).add(eo.getEdge().getV2()); }
	 * 
	 * 
	 * for(EdgeOperator eo:plan) { Vertex start = eo.getStartVertex(); if(start ==
	 * null) {//no dependency needed prllplan.get(0).add(eo);
	 * dependency.get(0).add(eo.getEdge().getV1());
	 * dependency.get(0).add(eo.getEdge().getV2()); } else {//check dependency
	 * for(int i=0;i<dependency.size();i++) {
	 * 
	 * if(dependency.get(i).contains(start)) {//if the start vertex has been bound
	 * try { prllplan.get(i+1).add(eo);
	 * dependency.get(i+1).add(eo.getEdge().getV1());
	 * dependency.get(i+1).add(eo.getEdge().getV2()); } catch (Exception e) {
	 * prllplan.add(new ArrayList<EdgeOperator>()); dependency.add(new
	 * HashSet<Vertex>()); prllplan.get(i+1).add(eo);
	 * dependency.get(i+1).add(eo.getEdge().getV1());
	 * dependency.get(i+1).add(eo.getEdge().getV2()); } break; }//try the next
	 * plan_num } } } return prllplan; }
	 */

	/**
	 * Generate the permutations of the given set of objects
	 * 
	 * @param items the set of objects
	 * @return a list of all permutations
	 */
	private <T> Iterator<List<T>> getPermutations(Collection<T> items) {

		/*
		 * //Recursive approach List<List<T>> results = new ArrayList<List<T>>();
		 * if(items.size() == 1) { List<T> temp = new ArrayList<T>();
		 * temp.add(items.iterator().next()); results.add(temp); return results; } for(T
		 * current:items) { Set<T> next = new HashSet<T>(); next.addAll(items);
		 * next.remove(current); List<List<T>> subResults = getPermutations(next);
		 * for(List<T> solution:subResults) { solution.add(0, current);
		 * results.add(solution); } } return results;
		 */
		return new Permutor<T>(items);
	}

	/**
	 * An iterative approach
	 * 
	 * @param plans
	 * @param items
	 * @return
	 *//*
		 * private <T> List<List<T>> getPermutations(List<List<T>> plans, Collection<T>
		 * items) { if(plans.get(0).size() == items.size()) return plans; List<List<T>>
		 * partial = new ArrayList<List<T>>(); for(List<T> plan:plans) { for(T
		 * item:items) { if(plan.contains(item)) continue; List<T> temp = new
		 * ArrayList<T>(); temp.addAll(plan); temp.add(item); partial.add(temp); } }
		 * 
		 * return getPermutations(partial,items); }
		 */
}


class Permutor<T> implements Iterator<List<T>> {

	private Iterator<List<T>> iter = null;
	private Iterator<T> self = null;
	private Collection<T> items;
	private T current = null;
	List<T> next;
	int s=0;
//	private int j;

	public Permutor(Collection<T> items) {
//		int i =0;
//	if(i>20000 && i<=30000) {
		this.items = items;
	//else
	//	this.items = null;
	//	i++;
	//}
	//		}
		self = items.iterator();
//while(self.hasNext())
///		//System.out.println("This is list of iterators in Permutor during Permutation:"+ExhOptimiser.j);
	
		
		}

	@Override
	public boolean hasNext() {
		//if(ExhOptimiser.j<=1001)
		//	return false;
	//	if	(ExhOptimiser.j>55) {
	//		s=1;
//		return false;
//		}
		if (iter == null) {
////System.out.println("This is first condiction in hasNext():"+"--"+ExhOptimiser.j);
	s=0;
	
			return self.hasNext();
		}
		if (iter.hasNext() || self.hasNext()) {
//			//System.out.println("This is second condiction in hasNext():"+next+"--"+ExhOptimiser.j);
s=0;
			return true;
		}
//		//System.out.println("This is third condiction in hasNext():"+next+"--"+ExhOptimiser.j);
		s=0;
		return false;
	}

	@Override
	public List<T> next() {
	//	while(orders.hasNext())
		
	//	ExhOptimiser.j++;
		
	//	if(hasNext()==false && s==1)
	//		return next;
		while (true ) {
			////System.out.println("This is getOptimal orders_counted:"+next+"--"+ExhOptimiser.j);

			//if(ExhOptimiser.j<=1001)
			//	return null;
				
		if (items.size() == 1) {
			next = new ArrayList<T>();
			next.add(self.next());
//			//System.out.println("This is first condiction in next():"+next+"--"+ExhOptimiser.j);

			return next;
		}
			
			if (current == null) {
				current = self.next();
				List<T> temp = new ArrayList<T>(items);
				temp.remove(current);
				iter = new Permutor<T>(temp);

				////System.out.println("This is second condiction in next():"+temp+"--"+ExhOptimiser.j);
				
			}
			if (!iter.hasNext()) {
				current = null;
			//	//System.out.println("This is third condiction in next():"+next+"--"+ExhOptimiser.j);

				continue;
			}
			
			next = iter.next();
			next.add(0, current);
		//	//System.out.println("This is fourth condiction in next():"+next+"--"+ExhOptimiser.j);
			return next;
			
}	
}
	//	return next;
			//return next;
		//}
		

	@Override
	public void remove() {

	}
}
