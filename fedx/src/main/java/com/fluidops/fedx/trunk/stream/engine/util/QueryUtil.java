package com.fluidops.fedx.trunk.stream.engine.util;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.fluidops.fedx.trunk.description.RemoteService;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import org.apache.jena.graph.Node;
import  org.apache.jena.graph.Triple;
import  org.apache.jena.query.Query;
import  org.apache.jena.query.QueryExecException;
import  org.apache.jena.query.ResultSet;
import  org.apache.jena.sparql.core.Var;
import  org.apache.jena.sparql.engine.binding.Binding;
import  org.apache.jena.sparql.engine.binding.BindingFactory;
import  org.apache.jena.sparql.engine.binding.BindingMap;
import  org.apache.jena.sparql.expr.E_Equals;
import  org.apache.jena.sparql.expr.E_LogicalAnd;
import  org.apache.jena.sparql.expr.E_LogicalOr;
import  org.apache.jena.sparql.expr.Expr;
import  org.apache.jena.sparql.expr.ExprVar;
import  org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import  org.apache.jena.sparql.syntax.ElementFilter;
import  org.apache.jena.sparql.syntax.ElementGroup;

public class QueryUtil {

	static int count_r;
	static List<Binding> joinBinding;
	
	public static Query buildQuery(Triple t, Binding pre) {
		Node s = replacewithBinding(t.getSubject(), pre);
		Node p = replacewithBinding(t.getPredicate(), pre);
		Node o = replacewithBinding(t.getObject(), pre);
		/*
		 * if(s.isConcrete() && o.isConcrete()) ////System.out.println("WTF");
		 */
		Triple tri = new Triple(s, p, o);
		Query query = new Query();
		ElementGroup elg = new ElementGroup();
		elg.addTriplePattern(tri);
		query.setQueryResultStar(true);
		query.setQueryPattern(elg);
		return query;
	}

	public static Node replacewithBinding(org.apache.jena.graph.Node node, Binding b) {
		if (b == null)
			return node;

		if (node.isVariable() && b.contains(Var.alloc(node))) {
			Node n = b.get(Var.alloc(node));
			if (n == null)
				return node; // should not happen!
			if (n.isBlank()) {
				// we do not support blank nodes
				throw new QueryExecException("Cannot handle Blank Nodes over different graphs.");
			} else {
				return n;
			}
		}
		return node;
	}
/*
	public static ElementFilter buildFilter(ElementGroup elg, List<Binding> bindings) {
		List<Var> vars = new ArrayList<Var>();
		vars.addAll(elg.varsMentioned());

		Expr expr = null;
		for (Binding b : bindings) {
			Expr and = null;
			for (Var v : vars) {
				if (b.contains(v)) {
					ExprVar eVar = new ExprVar(v);
					NodeValueNode eVal = new NodeValueNode(b.get(v));
					E_Equals eq = new E_Equals(eVar, eVal);
					if (and == null)// is this the first result filter? can use true as the initial value of and to
									// avoid the if clause
						and = eq;
					else
						and = new E_LogicalAnd(and, eq);// do the logical and with previous result filter
				}
			}

			if (and != null) {
				if (expr == null)
					expr = and;
				else
					expr = new E_LogicalOr(expr, and);
			}

		}

		if (expr != null)
			return new ElementFilter(expr);

		return null;
	}*/
/*	public static List<Binding> concatenate(List<Binding> left, List<Binding> right) {
		List<Binding> results = new HashList<Binding>();
		results.addAll(left);
		for (Binding l : left) {
			for (Binding r : right) {
				Binding temp =null;// new BindingMap();
				temp.addAll(l);
				temp.addAll(r);
				results.add(temp);
			}
		}
		return results;

	}
*/
	/**
	 * Used for pruning only
	 * 
	 * @param left  The original bindings
	 * @param right Pruned bindings
	 * @return
	 */
	public static List<Binding> join_prune(List<Binding> left, List<Binding> right) {
		List<Binding> results = new ArrayList<Binding>();
		if (left.size() == 0 || right.size() == 0)
			return results;

		/*
		 * Binding lb = left.iterator().next(); Binding rb = right.iterator().next();
		 * Binding temL = lb; Binding temR = rb; temL. ////System.out.print(temL.size()
		 * +":"+temR.size()+"\n"); if(temL.size() == temR.size()) {
		 * results.addAll(right); return results; }
		 */
		for (Binding l : left) {
			Iterator<Var> lvar = l.vars();
			List<Var> lvars = new ArrayList<Var>();
			while (lvar.hasNext())
				lvars.add(lvar.next());

			for (Binding r : right) {
				Iterator<Var> rvar = r.vars();
				List<Var> rvars = new ArrayList<Var>();
				while (rvar.hasNext())
					rvars.add(rvar.next());
				if (lvars.size() == rvars.size()) {// if left contains the same variables as right, then right is the
													// result
					return right;
				}

				Binding b = join_prune(l, r);
				if (b != null)
					results.add(b);
			}
		}
		return results;
	}

	/**
	 * {@code r} contains only one variable
	 * 
	 * @param l
	 * @param r
	 * @return
	 */
	private static Binding join_prune(Binding l, Binding r) {
		Iterator<Var> vars = r.vars();
		Var v = vars.next();
		if (!l.get(v).equals(r.get(v))) {
			return null;
		}
		return l;
	}
static int i=0;
	public static List<Binding> join(List<Binding> left, List<Binding> right) {
	
		/*	try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	*/
		if (left == null ) {
		//	////System.out.println("This is in Join QueryUtil1:"+right.size());

			return right;
		}

		if (right == null) {
			////System.out.println("This is in Join QueryUtil2:"+left.size());

			return left;
		}
		
		if (left.isEmpty() && !right.isEmpty()) {
			
i=1;
//ForkJoinPool fjp =new ForkJoinPool(Runtime.getRuntime().availableProcessors());

//fjp.submit(()->////System.out.println("This is in Join QueryUtil11:"+right));
//fjp.shutdownNow();
////System.out.println("This is in Join QueryUtil11:"+right.size());
			return right;
		}

		if (!left.isEmpty() && right.isEmpty()) {
//			ForkJoinPool fjp =new ForkJoinPool(Runtime.getRuntime().availableProcessors());

//			fjp.submit(()->////System.out.println("This is in Join QueryUtil12:"+left));
	///		fjp.shutdownNow();
			i=1;
			////System.out.println("This is in Join QueryUtil12:"+left.size());
			return left;
		}

		


		/*		if (left.isEmpty() && !right.isEmpty()) {
//			ForkJoinPool fjp =new ForkJoinPool(Runtime.getRuntime().availableProcessors());

//			fjp.submit(()->////System.out.println("This is in Join QueryUtil12:"+left));
	///		fjp.shutdownNow();
			i=1;
			////System.out.println("This is in Join QueryUtil12:"+left.size());
			return right;
		}
*/
		List<Binding> results = new ArrayList<Binding>();
		if (left.size() == 0 || right.size() == 0)
		{
			////System.out.println("This is in Join QueryUtil3:"+right.size()+"--"+left.size());

			return results;
		}
		if (left.equals(right)) {
			return left;
		}
		////System.out.println("This is in Join QueryUtil4:"+right.size()+"--"+left.size());

	//	ForkJoinPool fjp = new ForkJoinPool(6);
	
		Set<Var> joinVars = //fjp.submit(()->
		getJoinVars(left, right).parallelStream().collect(Collectors.toSet());//).join();
//fjp.shutdown();
//ForkJoinPool fjp1=new ForkJoinPool(6);
		return null;// fjp1.submit(()->hashJoin(left, right, joinVars).parallelStream().collect(Collectors.toSet())).join();
	}

	static private Set<Var> getJoinVars(Collection<Binding> left, Collection<Binding> right) {
		if (left == null || right == null)
			return null;
  
			
		Set<Var> joinVars = new HashSet<Var>();
		Iterator<Var> l = left.iterator().next().vars();
		Binding r = right.iterator().next();
	//	////System.out.println("This is rule no. 1:"+r);
		while (l.hasNext()) {
			Var v = l.next();
		//	////System.out.println("This is rule no. 2:"+v);
			
			if (r.contains(v)) {
				joinVars.add(v);
			}
		}
		//System.out.println("This is in Join QueryUtil6:"+joinVars);
		
		return joinVars;
	}



	public static List<Binding> hashJoin(Collection<Binding> left, Collection<Binding> right, Set<Var> vars) {
		int xyz=0;
int ced=0;
int ced1=0;
		//Collections.copy(left2, left);
	//	Iterator<Binding> l1 = left1.iterator();
		//System.out.println("Heading towards final solution:"+left2);

//	System.out.println("This is the final final final final final final final:"+right.size()+"--"+left.size());
	
	//	System.out.println("This is the final final final final final final final2222:"+right.parallelStream().limit(10).collect(Collectors.toSet()));

//		System.out.println("This is the final final final final final final final3333:"+left.parallelStream().limit(10).collect(Collectors.toSet()));

		List<Binding> results = new ArrayList<Binding>();
		List<Binding> results2 = new ArrayList<Binding>();
		
		if (vars.isEmpty()) {
			// create cross product
			for (Binding l : left) {
				for (Binding r : right) {
					BindingMap joinBinding = null ;
					joinBinding.addAll(l);
					joinBinding.addAll(r);
					results.forEach(e->results.add(joinBinding));
				}

			}
		//System.out.println("This is in Join QueryUtil7:"+results);
			
			return results;
		}

		if (left.size() > right.size()) {
			Collection<Binding> temp = left;
			left = right;
			right = temp;
//System.out.println("this that there done:"+left.size()+"--"+right.size());	
		}
		
		Map<Binding, List<Binding>> hashBindingMap = new HashMap<Binding, List<Binding>>();
		List<Var> joinVars = new ArrayList<Var>(vars);
		for (Binding l : left) {
			BindingMap joinBinding = null;
			for (Var var : joinVars) {
				joinBinding.add(var, l.get(var));
			}
			if (hashBindingMap.get(joinBinding) == null) {
				hashBindingMap.put(joinBinding, new ArrayList<Binding>());
			}
			hashBindingMap.get(joinBinding).add(l);
	
		}
			for(Entry<Binding, List<Binding>> hbm:hashBindingMap.entrySet())
		{//System.out.println("This is in Join QueryUtil.018:"+xyz+"--"+hbm.getValue().size());
		
		}
		for (Binding r : right) {
			BindingMap joinBinding = null;
			for (Var var : joinVars) {
				if(xyz<50)
		//		System.out.println("This is in Join QueryUtil11111:"+ced1+"--"+ced+"--"+r);
				joinBinding.add(var, r.get(var));
			}
//			if(xyz<50)
				
			//System.out.println("This is in Join QueryUtil000:"+joinBinding+"--"+xyz);
			if (hashBindingMap.get(joinBinding) != null) {
				for (Binding l : hashBindingMap.get(joinBinding)) {
	//				if(xyz<50)
						
				//	System.out.println("This is in Join QueryUtil444:"+l+"--"+xyz);
							
					BindingMap join = BindingFactory.create();
					join.addAll(l);
					//if(ParaEng.Optional.contains("OPTIONAL")==true) {
						
				//	}
//					OptionalB.add(l);
//if(i>=50)
		xyz++;			
					Iterator<Var> iter = r.vars();
					while (iter.hasNext()) {
						Var v = iter.next();
						if (!joinBinding.contains(v)) {
							if(r.contains(v))
							//System.out.println("This is in Join QueryUtil2222222:"+r.get(v));
					
							join.add(v, r.get(v));
						}
					}
					results.add(join);


					
					if(ParaEng.Optional.contains("OPTIONAL")==true) {
						BindingMap join1 = BindingFactory.create();
						
						join1.addAll(l);
										
		//			ForkJoinPool fjp = new ForkJoinPool(6);
	//	fjp.submit(()->{			
			Iterator<Var> iter2 = r.vars();
			
			while (iter2.hasNext()) {
						Var v = iter2.next();
						if (!joinBinding.contains(v)) {
							if(!r.contains(v))
							//	System.out.println("This is in Join QueryUtil2222222:"+r.get(v));
					
							join1.add(v, r.get(v));
						}
					}
					results2.add(join1);
				
		//		});
		//fjp.shutdown();
			}
				}	
				}
		}
	//	results.addAll(OptionalB);

		
		
		//System.out.println("zxc zxc zxc zxc zxc zxc zxc zxc:"+results.size());

		if(results.size()==9)
			for(Binding r11:results)
				System.out.println("xcv xcv xcv xcv xcv:"+r11);

		
		
		if(ParaEng.Optional.contains("OPTIONAL")==true) {
		int a1=left.size();	
		left.removeAll(results2);
int a2=left.size();
		List<Binding> results1=new ArrayList<>();
		results1.addAll(left);
		results1.addAll(results);
		System.out.println("090909090909090909:"+results1.size()+"--"+results2.size());
	//	if(results==results1)
	//		return results2;
	//	else
		return results1;}
		else
		return results;
	}

	


}
