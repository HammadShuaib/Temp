package com.fluidops.fedx.trunk.parallel.engine.exec.operator;

import java.util.Set;
import java.util.Map.Entry;

import  org.apache.jena.sparql.engine.binding.Binding;

import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.exec.TripleExecution;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;

/**
 * Warp an edge, a vertex providing intermediate results and the join to execute
 * this edge
 * 
 * @author xgfd
 *
 */
public abstract class EdgeOperator implements Runnable {
	Vertex start;
	 Edge edge;
	TripleExecution te;
	Set<Binding> input = null;
	Set<Binding> input1 = null;
	boolean finished = false;

	public EdgeOperator(Vertex s, Edge e) {
		this.start = s;
		this.edge = e;
		te = new TripleExecution(edge.getTriple());
			
	//	 System.out.println("!!!!!!!!!!!!!!!!!!!!!!!This is here in Edge Operator Cons!!!!!!!!!!!:"+s+"--"+e);
		
	
	}

	@Override
	public void run() {
		if (Config.debug) {
			System.out.println(this);
		}
	//	 System.out.println("!!!!!!!!!!!!!!!!!!!!!!!This is here in Edge Operator1!!!!!!!!!!!:"+te);

		exec();
	}

	abstract protected void exec();

	public int setInput(Set<Binding> bindings) {
		this.input = bindings;
		this.input1 = bindings;
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!This is here in Edge Operator2!!!!!!!!!!!:"+te);

		return 0;
	}

	public boolean isFinished() {
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!This is here in Edge Operator2!!!!!!!!!!!:"+te);

		return finished;
	}

	public Vertex getStartVertex() {
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!This is here in Edge Operator3!!!!!!!!!!!:"+te);

		return start;
	}

	public Edge getEdge() {
	//	 System.out.println("!!!!!!!!!!!!!!!!!!!!!!!This is here in Edge Operator4!!!!!!!!!!!:"+te);

		return edge;
	}

}
