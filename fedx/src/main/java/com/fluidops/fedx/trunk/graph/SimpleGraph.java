package com.fluidops.fedx.trunk.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleGraph extends Graph {

	private List<Edge> edges = new ArrayList<Edge>();
	private Set<Vertex> vertices = new HashSet<Vertex>();

	public SimpleGraph(Collection<Edge> edges) {
		for (Edge e : edges) {
			addEdge(e);
		}
	}

	public SimpleGraph(Edge e) {
		addEdge(e);
	}

	@Override
	public void addEdge(Edge e) {
		edges.add(e);
		vertices.add(e.getV1());
		vertices.add(e.getV2());
	}

	public void removeEdge(Collection<Edge> edges_) {
		for (Edge e : edges_) {
			removeEdge(e);
		}
	}

	public void removeEdge(Edge e) {
		edges.remove(e);
		Vertex v1 = e.getV1();
		Vertex v2 = e.getV2();
		v1.getEdges().remove(e);
		v2.getEdges().remove(e);
		if (v1.getEdges().isEmpty())
			vertices.remove(v1);
		if (v2.getEdges().isEmpty())
			vertices.remove(v2);
	}

	public List<Edge> getEdges() {
		return edges;
	}

	@Override
	public Set<Vertex> getVertices() {
		return vertices;
	}

	@Override
	public String toString() {
		return edges.toString();

	}
}
