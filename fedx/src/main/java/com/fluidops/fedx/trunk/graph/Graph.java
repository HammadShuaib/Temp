package com.fluidops.fedx.trunk.graph;

import java.util.Collection;

public abstract class Graph {

	abstract public void addEdge(Edge e);

	abstract public Collection<Vertex> getVertices();

	abstract public Collection<Edge> getEdges();

}
