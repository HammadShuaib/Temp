package com.fluidops.fedx.trunk.stream.engine.util;

import java.util.ArrayList;
import java.util.List;

import com.fluidops.fedx.trunk.description.RemoteService;

import com.hp.hpl.jena.graph.Triple;

public class HypTriple extends Triple {

	private List<RemoteService> services;

	public HypTriple(Triple t, List<RemoteService> srvcs) {
		super(t.getSubject(), t.getPredicate(), t.getObject());
		setServices(srvcs);
	}

	public List<RemoteService> getServices() {
		return services;
	}

	public void setServices(List<RemoteService> services) {
		this.services = services;
	}

}
