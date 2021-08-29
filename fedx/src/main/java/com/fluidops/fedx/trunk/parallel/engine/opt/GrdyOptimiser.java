package com.fluidops.fedx.trunk.parallel.engine.opt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.SimpleGraph;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.BindJoin;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.HashJoin;

public class GrdyOptimiser extends Optimiser {

	private Set<Vertex> remainVertices;
	private int edgepertime = 4;

	public GrdyOptimiser(SimpleGraph g) {
		super(g);
		remainVertices = new HashSet<Vertex>();
		remainVertices.addAll(g.getVertices());
	}

	@Override
	public List<EdgeOperator> nextStage() {
		if (g.getEdges().isEmpty())
			return null;
		List<EdgeOperator> operators = new ArrayList<EdgeOperator>();
		int size = Integer.MAX_VALUE;
		Vertex mini = null;
		for (Vertex v : g.getVertices()) {
			if (v.getNumber() < size) {
				mini = v;
				size = v.getNumber();
			}
		}

		remainVertices.remove(mini);
		Set<Edge> edges = new HashSet<Edge>(mini.getEdges());
		for (Edge e : edges) {
			g.removeEdge(e);
			if (e.getV1().getNode().isConcrete() || e.getV2().getNode().isConcrete()) {
				operators.add(new HashJoin((Edge) e));
			} else {
				operators.add(new BindJoin(mini, (Edge) e));
			}
		}
		return operators;
	}

	@Override
	public Set<Vertex> getRemainVertices() {
		return remainVertices;
	}

	public Edge nextEdge() {
		List<Edge> edges = g.getEdges();
		Edge e = null;
		double min = Double.MAX_VALUE;
		for (Edge edge : edges) {
			if (((Edge) edge).estimatedCard() < min) {
				min = ((Edge) edge).estimatedCard();
				e = (Edge) edge;
			}
		}

		return e;
	}

	public Map<Edge, Vertex> nextEdges() {
		return null;
/*		Map<Edge, Vertex> next = new HashMap<Edge, Vertex>();

		Set<Vertex> starts = new HashSet<Vertex>();
		Set<Vertex> ends = new HashSet<Vertex>();
		for (int i = 0; i < edgepertime && remainVertices.size() > 1 && !g.getEdges().isEmpty(); i++) {
			Edge e = nextEdge();
			Vertex start = e.getV1();
			Vertex end = e.getV2();
			if (start.getNumber() > end.getNumber()) {
				Vertex temp = start;
				start = end;
				end = temp;
			}

			if (starts.contains(end) || ends.contains(start)) {
				Vertex temp = start;
				start = end;
				end = temp;
				if (starts.contains(end) || ends.contains(start)) {
					start = null;
					end = null;
				}
			}

			if (start == null)
				break;
			starts.add(start);
			Set<Edge> adj = start.getEdges();
			for (Edge edge : adj) {
				if (edge.getV1().equals(start)) {
					ends.add(edge.getV2());
				} else {
					ends.add(edge.getV1());
				}
			}
			remainVertices.remove(start);
			remainVertices.add(end);
			g.removeEdge(e);
			next.put(e, start);
		}

		/*
		 * for(int i = 0;i < edgepertime && remainVertices.size() > 1;) {
		 * 
		 * //select the minimum vertex that is not connected with any vertex that has
		 * been selected //this is to make sure that a vertex won't be the start point
		 * in the evaluation of one edge, //and at the same time the end point of
		 * another. For example, {a p1 ?o, ?o p2 b} should be evaluated //sequentially
		 * rather than in parallel. List<CostObject> costs = new
		 * ArrayList<CostObject>(); for(Vertex v:g.getVertices()) costs.add(new
		 * CostObject(g,v)); Collections.sort(costs);
		 * 
		 * Vertex v = null; Iterator<Edge> iter = next.keySet().iterator(); int k = 0;
		 * do { v = costs.get(k).getV(); //check whether v is connected by vertices that
		 * have been selected while(iter.hasNext()) { Edge edg = iter.next();
		 * if(edg.getV1().equals(v) || edg.getV2().equals(v)) { v = null; break; } }
		 * k++; } while (k<costs.size() && v == null);
		 * 
		 * if(v == null) { break;//break when no vertex is available }
		 * remainVertices.remove(v); List<Edge> edges = g.asList().get(v);
		 * Collections.sort(edges); for(int j = 0;j < edgepertime && j <
		 * edges.size();j++) { next.put((Edge) edges.get(j), v);
		 * g.removeEdge(edges.get(j)); i++; } if(g.getEdges().isEmpty()) { break; } }
		 *
		return next;
	*/
	}
	
}
