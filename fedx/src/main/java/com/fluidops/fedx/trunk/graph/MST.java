package com.fluidops.fedx.trunk.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

@Deprecated
public class MST {

	private int type = 0;// default algorithm
	private Stack<Edge> treeEdge = new Stack<Edge>();
	private Stack<Edge> nonTreeEdge = new Stack<Edge>();
	private ArrayList<Edge> unVisited = new ArrayList<Edge>();
	private Set<Vertex> visitedVrt = new HashSet<Vertex>();
	private int vrtNum;

	public MST(Graph g) {
		vrtNum = g.getVertices().size();
		unVisited.addAll(g.getEdges());
		/*
		 * for(Vertex v:g.getVertices()) { Set<Vertex> group = new HashSet<Vertex>();
		 * group.add(v); v.setGroup(group); }
		 */
	}

	public MST(Graph g, int type) {
		vrtNum = g.getVertices().size();
		this.type = type;
		unVisited.addAll(g.getEdges());
		/*
		 * for(Vertex v:g.getVertices()) { Set<Vertex> group = new HashSet<Vertex>();
		 * group.add(v); v.setGroup(group); }
		 */
	}

	public Set<Vertex> getVisitedVrt() {
		return visitedVrt;
	}

	public boolean hasNext() {
		if (visitedVrt.size() == vrtNum)
			return false;

		return true;
	}

	/**
	 * Return newly visited vertices. Push the next tree edge into stack. Also push
	 * all edges with both end points visited into stack.
	 */
	public List<Vertex> next() {
		sort();
		Edge tem = unVisited.get(0);
		Vertex v1 = tem.getV1();
		Vertex v2 = tem.getV2();
		List<Vertex> vrts = new ArrayList<Vertex>();

		if (!visitedVrt.contains(v1))
			vrts.add(v1);
		if (!visitedVrt.contains(v2))
			vrts.add(v2);
		treeEdge.add(tem);
		unVisited.remove(tem);
		// union(v1,v2);
		visitedVrt.addAll(vrts);

		for (Edge e : unVisited) {
			if (visitedVrt.contains(e.getV1()) && visitedVrt.contains(e.getV2())) {
				nonTreeEdge.add(e);
			}
		}
		unVisited.removeAll(nonTreeEdge);

		return vrts;

		/*
		 * while(true) { Edge tem = unVisited.get(0); Vertex v1 = tem.getV1(); Vertex v2
		 * = tem.getV2(); List<Vertex> vrts = new ArrayList<Vertex>();
		 * 
		 * if(visitedVrt.contains(v1) && visitedVrt.contains(v2)) {
		 * nonTreeEdge.add(tem); unVisited.remove(tem); } else {
		 * if(!visitedVrt.contains(v1)) vrts.add(v1); if(!visitedVrt.contains(v2))
		 * vrts.add(v2); treeEdge.add(tem); unVisited.remove(tem); union(v1,v2);
		 * visitedVrt.addAll(v1.getGroup()); if(visitedVrt.size() == vrtNum) {
		 * nonTreeEdge.addAll(unVisited); } return vrts; } }
		 */
	}

	/*
	 * private void union(Vertex v1, Vertex v2) {
	 * v1.getGroup().addAll(v2.getGroup()); Object[] arr = v2.getGroup().toArray();
	 * for(Object v:arr) { ((Vertex)v).setGroup(v1.getGroup()); } }
	 */

	public Stack<Edge> getTree() {
		return treeEdge;
	}

	public Stack<Edge> getNonTree() {
		return nonTreeEdge;
	}

	public int getType() {
		return type;
	}

	private void sort() {
		Collections.sort(unVisited);// TODO only sort updated edges
	}

}
