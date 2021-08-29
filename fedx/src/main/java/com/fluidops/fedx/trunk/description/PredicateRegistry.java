package com.fluidops.fedx.trunk.description;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Triple;

public class PredicateRegistry {

	private Map<String, PPartition> predicates;

	public PredicateRegistry() {
		//predicates = new HashMap<String, PPartition>();
	}

	public Collection<PPartition> getAvailablePredicates() {
		return predicates.values();
	}

	public void add(PPartition partition) {
	//	predicates.put(partition.getPredicate(), partition);
	}

	public PPartition getMatchingPredicate(Triple t) {
		String predicate = t.getPredicate().getURI();
		return predicates.get(predicate);
	}

}