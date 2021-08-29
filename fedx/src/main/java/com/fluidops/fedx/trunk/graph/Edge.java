package com.fluidops.fedx.trunk.graph;

import org.apache.jena.graph.Triple;

import com.fluidops.fedx.trunk.parallel.engine.opt.CostModel;
//import com.fluidops.fedx.trunk.stream.engine.util.HypTriple;

public class Edge implements Comparable<Edge> {

	private Vertex v1;
	private Vertex v2;
	private double weight;

	private final int IMPO = -1;
	private Triple triple;
	private double distinctSubject;
	private double distinctObject;
	private double tripleCount;
	private double cost = IMPO;

	public Edge(Vertex v1, Vertex v2, Triple t) {
		this.v1 = v1;
		this.v2 = v2;
		this.triple = t;
	}

	public Edge(Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	@Deprecated
	public Edge(Vertex v1, Vertex v2, double weight) {
		this.v1 = v1;
		this.v2 = v2;
		this.weight = weight;
	}

	public Vertex getV1() {
		return v1;
	}

	public Vertex getV2() {
		return v2;
	}

	public double getWeight() {
		return weight;
	}

	/**
	 * set vertices, use null to leave a vertex unchanged
	 * 
	 * @param v1
	 * @param v2
	 */
	public void setVertices(Vertex v1, Vertex v2) {
		if (v1 != null)
			this.v1 = v1;
		if (v2 != null)
			this.v2 = v2;
	}

	@Deprecated
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void setDistinctSubject(double s) {
		distinctSubject = s;
	}

	public double getDistinctSubject() {
		return distinctSubject;
	}

	public void setDistinctObject(double o) {
		distinctObject = o;
	}

	public double getDistinctObject() {
		return distinctObject;
	}

	public void setTripleCount(double t) {
		tripleCount = t;
	}

	public double getTripleCount() {
		return tripleCount;
	}

	@Override
	public String toString() {
		return getV1().toString() + " - " + getV2().toString();
	}

	public Triple getTriple() {
		return triple;
	}

	public void setTriple(Triple t) {
		this.triple = t;
	}

	public double estimatedCard() {
		cost = CostModel.resultSize(this);
		return cost;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Edge otherEdge = (Edge) obj;
			boolean result = (v1.equals(otherEdge.getV1()) && v2.equals(otherEdge.getV2()));
			result = result || (v1.equals(otherEdge.getV2()) && v2.equals(otherEdge.getV1()));
			return result;
		} catch (Exception e) {
			return super.equals(obj);
		}
	}

	@Override
	/*
	 * public int compareTo(Edge e) {
	 * 
	 * if(weight > e.getWeight()) return 1; if(weight < e.getWeight()) return -1;
	 * return 0; }
	 */
	public int compareTo(Edge e) {
		if (estimatedCard() > e.estimatedCard())
			return 1;
		if (estimatedCard() < e.estimatedCard())
			return -1;
		return 0;
	}

}
