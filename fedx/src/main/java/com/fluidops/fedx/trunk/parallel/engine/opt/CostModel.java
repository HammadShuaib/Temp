package com.fluidops.fedx.trunk.parallel.engine.opt;

import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.Vertex;

public class CostModel {

	/**
	 * cost per incoming triple
	 */
	final static public int CT = 1;
	/**
	 * cost per outgoing query
	 */
	final static public int CQ = 1;

	static public int TotalEntities = 0;
	static public long TotalTriples = 0;
	static public int TotalProperties = 0;
	public static double TotalSubjects;
	public static double TotalObjects;

	/**
	 * To execute a triple pattern using bind join we need to send out the input
	 * bindings and receive the final results.
	 * 
	 * @param e
	 * @return
	 */
	static public double costBJ(Edge e) {
		return 0;
		/*
		 * double cost; Vertex v1 = e.getV1(); Vertex v2 = e.getV2(); //if both vertices
		 * are variables, return a big value to make sure nested loop join //will be
		 * used if(!v1.isVisited() && !v2.isVisited()) { return Double.MAX_VALUE; }
		 * 
		 * int in; if(v1.getNumber()<v2.getNumber()) { in = v1.getNumber(); } else { in
		 * = v2.getNumber(); }
		 * 
		 * cost = in*CQ+resultSize(e)*CT;
		 * 
		 * return cost;
		 */
	}

	static public long costNJ(Edge e) {
		return 0;
		/*
		 * int t = e.getTripleCount(); if(t == 0) { t = TotalTriples/TotalProperties; }
		 * return t*CT+CQ;
		 */}

	static public double resultSize(Edge edge) {
		return 0;
	}
	/*
	 * double size = 0; int t = edge.getTripleCount(); if(t == 0) { t =
	 * TotalTriplesTotalProperties; } Vertex v1 = edge.getV1(); Vertex v2 =
	 * edge.getV2(); if(!v1.isBound() && !v2.isBound()) { return t; } //if both
	 * vertices are bound, we use the minimum number of input bindings //to
	 * calculate the expected result size int o; int in;
	 * if(v1.getNumber()<v2.getNumber()) { in = v1.getNumber(); o =
	 * edge.getDistinctSubject(); //in case the statistics provides no property
	 * partitions //assume entities are uniformly distributed for properties if(o ==
	 * 0) o = TotalSubjects/TotalProperties; } else { in = v2.getNumber(); o =
	 * edge.getDistinctObject(); if(o == 0) o = TotalObjects/TotalProperties; } int
	 * to = TotalEntities; size = calculateResultSize(in,t,o,to); //the expected
	 * number of results won't exceed the total number of triples //with the
	 * predicate of this edge size = Math.min(size, t); return size; }
	 */

	/**
	 * 
	 * @param in the number of input bindings (should be distinct).
	 * @param t  the number of triples of this property partition.
	 * @param o  the number of distinct object/subject of the property partition.
	 * @param to the total number of distinct objects/subjects, which is the sum of
	 *           o of all property partitions.
	 * @return the expected number of results of this bind join
	 * 
	 * 
	 *         static private double calculateResultSize(int in,int t,int o,int to)
	 *         { in = Math.min(in, to); int m = Math.min(in, o); double size = 0;
	 *         for(int i=1;i<=m;i++) { size +=
	 *         C(i,o)*C(in-i,to-o)*i*t/(double)o/C(in,to); } return size; }
	 * 
	 *         //select a objects out of b objects static private double C(int a,int
	 *         b) { if(b<=0) { System.out.println("Error: b should be positive.");
	 *         return -1; }
	 * 
	 *         if(a == 0 || a == b) return 1;
	 * 
	 *         if(a>b) return 0; double c = 1; for(int i=b-a+1;i<=b;i++) { if(c >
	 *         Double.MAX_VALUE) { c = Double.MAX_VALUE; break; } c*=i; }
	 * 
	 *         return c; }
	 */
}
