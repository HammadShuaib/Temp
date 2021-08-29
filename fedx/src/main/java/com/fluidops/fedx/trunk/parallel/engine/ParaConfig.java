package com.fluidops.fedx.trunk.parallel.engine;

import com.fluidops.fedx.trunk.config.Config;

public class ParaConfig extends Config {

	// the block size of union-bind-join i.e. the number of bindings sent out per
	// request,at least 2
	public static int blocksize = 40;
	// the timeout of each sub-queries in milliseconds. may have no effect depending
	// on triple store
	public static int timeout = 1000000000;
	// the max number of requests permitted per service
	public static int THREADPERSERVICE = 20;
}
