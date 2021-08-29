package com.fluidops.fedx;

import java.io.File;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.util.EndpointFactory;

public class EndpointListFileProvider implements EndpointListProvider {
	final File dataFile;

	EndpointListFileProvider(File dataFile) {
		this.dataFile = dataFile;
	}

	@Override
	public List<Endpoint> getEndpoints(FedX federation) {
		ForkJoinPool fjp = new ForkJoinPool(6);
		return fjp.submit(()->EndpointFactory.loadFederationMembers(federation.getConfig(), federation.getHttpClient(), dataFile)).join();
	}

	@Override
	public void close() {

	}
}
