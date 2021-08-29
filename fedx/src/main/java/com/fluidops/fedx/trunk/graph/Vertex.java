package com.fluidops.fedx.trunk.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fluidops.fedx.trunk.stream.engine.util.QueryUtil;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class Vertex implements Comparable<Vertex> {

	private int number = Integer.MAX_VALUE;
	private Node node;
	private boolean bound = false;
	private boolean consumed = true;
	private List<Binding> bindings = new ArrayList<Binding>();// bindings == null represents a concrete node
	private Set<Edge> adjEdges = new HashSet<Edge>();

	static private VertexFactory vf = new VertexFactory();// see the comments of VertexFactory

	public static Vertex create(Node value) {
		return vf.create(value);
	}

	/**
	 * Clear cached vertices to avoid reusing vertices having the same name but from
	 * different queries.
	 */
	public static void resetVertexPool() {
		vf.reset();
	}

	public Vertex() {
	}

	Vertex(Node value) {
		node = value;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Vertex otherV = (Vertex) obj;
			boolean result = (node.equals(otherV.getNode()));
			return result;
		} catch (Exception e) {
			return super.equals(obj);
		}
	}

	@Override
	public String toString() {
		return "(" + getNode().toString() + ": " + number + " " + bound + ")";
	}

	@Override
	public int hashCode() {

		return node.hashCode() * 17;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		bound = true;
		this.number = number;
	}

	public List<Binding> getBindings() {
		synchronized (this) {
			consumed = true;
			return bindings;
		}
	}

	public void setBindings(List<Binding> bindings) {
		synchronized (this) {
			// set the bindings
			if (!consumed && this.bindings != null && !this.bindings.isEmpty()) {
				this.bindings = QueryUtil.join(this.bindings, bindings);
			} else {
				this.bindings = bindings;
			}
			consumed = false;
			// set the number of bindings
			if (node.isConcrete()) {
				setNumber(1);
				return;
			}

			if (bindings == null)
				setNumber(1);// should not reach here
			else {
				// count only the number of distinct bindings
				Set<Node> distinctValue = new HashSet<Node>();
				Iterator<Binding> iter = bindings.iterator();
				while (iter.hasNext()) {
					Binding b = iter.next();
					distinctValue.add(b.get(Var.alloc((Node) node)));
				}

				setNumber(distinctValue.size());
				// setNumber(bindings.size());
			}
		}
	}

	public boolean isBound() {
		synchronized (this) {
			return bound;
		}
	}

	public void addEdge(Edge e) {
		adjEdges.add(e);
	}

	public boolean isConsumed() {
		return consumed;
	}

	public void removeEdge(Edge e) {
		synchronized (this) {
			adjEdges.remove(e);
			if (adjEdges.isEmpty() && consumed) {
				bindings = null;
			}
		}
	}

	public Set<Edge> getEdges() {
		return adjEdges;
	}

	@Override
	public int compareTo(Vertex arg0) {
		if (number > arg0.number)
			return 1;
		if (number < arg0.number)
			return -1;
		return 0;
	}
}

/**
 * In order to guarantee that vertices initiated from the same value refer to
 * the same {@code Vertex} object, a cache is maintained in this
 * {@VertexFactory}. If a value has been used, this class will reuse the vertex
 * initiated from this value.
 * 
 * @author xw4g08
 *
 */
class VertexFactory {

	private Map<Object, Vertex> vertexCache = new HashMap<Object, Vertex>();

	Vertex create(Node value) {
		Vertex v = vertexCache.get(value);
		if (v == null) {
			v = new Vertex(value);
			vertexCache.put(value, v);
		}

		return v;

	}

	void reset() {
		vertexCache.clear();
	}
}
