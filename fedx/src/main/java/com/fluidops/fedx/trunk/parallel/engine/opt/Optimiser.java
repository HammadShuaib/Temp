package com.fluidops.fedx.trunk.parallel.engine.opt;

import java.util.List;
import java.util.Set;

import com.fluidops.fedx.trunk.graph.SimpleGraph;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;

public abstract class Optimiser implements Cloneable {
	SimpleGraph g;
	 protected BGPEval optimiser;
	Optimiser(SimpleGraph g) {
		this.g = g;
	}
	   public Object clone() throws CloneNotSupportedException{
		      return super.clone();
		   }
	public abstract List<EdgeOperator> nextStage();

	public abstract Set<Vertex> getRemainVertices();
}
