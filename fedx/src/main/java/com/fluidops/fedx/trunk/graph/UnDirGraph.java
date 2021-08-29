package com.fluidops.fedx.trunk.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

@Deprecated
public class UnDirGraph extends Graph {

	private Map<Vertex, List<Edge>> adjList = new HashMap<Vertex, List<Edge>>();
	private List<Edge> edges = new ArrayList<Edge>();
	// private Set<Vertex> vertices = null;

	private Stack<Edge> treeEdge = new Stack<Edge>();
	private Stack<Edge> nonTreeEdge = new Stack<Edge>();
	private ArrayList<Edge> unVisited = new ArrayList<Edge>();
	private Set<Vertex> unVisitedVrt = new HashSet<Vertex>();

	public UnDirGraph(List<Edge> edges) {
		addEdge(edges);
	}

	public UnDirGraph() {
	}

	public UnDirGraph(Edge[] edges) {
		addEdge(edges);
	}

	public void addEdge(Edge e) {

		Vertex v1 = e.getV1();
		Vertex v2 = e.getV2();

		unVisitedVrt.add(v1);
		unVisitedVrt.add(v2);

		if (adjList.get(v1) == null)
			adjList.put(v1, new ArrayList<Edge>());
		adjList.get(v1).add(e);

		if (adjList.get(v2) == null)
			adjList.put(v2, new ArrayList<Edge>());
		adjList.get(v2).add(e);

		edges.add(e);
		if (v1.isBound() && v2.isBound())
			nonTreeEdge.add(e);
		else
			unVisited.add(e);

	}

	public void addEdge(List<Edge> edges) {
		for (Edge e : edges)
			addEdge(e);
	}

	public void addEdge(Edge[] edges) {
		for (int i = 0; i < edges.length; i++) {
			addEdge(edges[i]);
		}
	}

	public void removeEdge(Edge e) {
		edges.remove(e);
	}

	public void removeEdge(List<Edge> edges) {
		for (Edge e : edges)
			removeEdge(e);
	}

	public int getEdgeNum() {
		return edges.size();
	}

	public void sort() {
		Collections.sort(edges);
	}

	@Override
	public List<Edge> getEdges() {
		return edges;
	}

	@Override
	public Set<Vertex> getVertices() {
		return adjList.keySet();
	}

	public Map<Vertex, List<Edge>> asAdjList() {
		return adjList;
	}

	public boolean hasNext() {
		return !unVisited.isEmpty();
		/*
		 * Set<Vertex> temp = new HashSet<Vertex>(); for(Vertex v:unVisitedVrt) {
		 * if(v.isBound()) { temp.add(v); } } unVisitedVrt.removeAll(temp);
		 * if(unVisitedVrt.size() == 0) return false;
		 * 
		 * return true;
		 */
	}

	/**
	 * Return newly visited vertices. Push the next tree edge into stack. Also push
	 * all edges with both end points visited into stack.
	 */
	public List<Vertex> next() {

		Collections.sort(unVisited);
		Edge tem = unVisited.get(0);
		Vertex v1 = tem.getV1();
		Vertex v2 = tem.getV2();
		List<Vertex> vrts = new ArrayList<Vertex>();

		if (!v1.isBound())
			vrts.add(v1);
		if (!v2.isBound())
			vrts.add(v2);
		treeEdge.add(tem);
		unVisited.remove(tem);
		return vrts;
	}

	public Stack<Edge> getTree() {
		return treeEdge;
	}

	public Stack<Edge> getNonTree() {
		return nonTreeEdge;
	}
}
