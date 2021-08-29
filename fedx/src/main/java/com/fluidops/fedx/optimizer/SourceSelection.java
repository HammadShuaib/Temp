/*
 * Copyright (C) 2008-2013, fluid Operations AG
 *
 * FedX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fluidops.fedx.optimizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import org.eclipse.rdf4j.query.algebra.StatementPattern;

import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.Config;

/**
 * Perform source selection during optimization
 * 
 * @author Andreas Schwarte
 *
 */
public abstract class SourceSelection {

	protected final List<Endpoint> endpoints;
	protected final Cache cache;
	protected final QueryInfo queryInfo;
	public long time = 0;
	/**
	 * Map statements to their sources.
	 */
	protected static ConcurrentHashMap<StatementPattern, List<StatementSource>> stmtToSources;

	public SourceSelection(List<Endpoint> endpoints, Cache cache, QueryInfo queryInfo) {
		this.endpoints = endpoints;
//	System.out.println("This is constructor in endpoints:"+this.endpoints);
		this.cache = cache;
		this.queryInfo = queryInfo;
		this.queryInfo.setSourceSelection(this);
		// System.out.println("§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§This is
		// getStmtToSources:"+this.queryInfo.setSourceSelection(this));
	//	System.out.println(
	//			"TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT Performing Source Selection TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");

	}

	public abstract void performSourceSelection(List<List<StatementPattern>> bgpGroups);

	/**
	 * Retrieve a source selection for the provided statement patterns.
	 * 
	 * @return
	 */
	public static ConcurrentHashMap<StatementPattern, List<StatementSource>> getStmtToSources() {
		return stmtToSources;
	}

	/**
	 * Retrieve a set of relevant sources for this query.
	 * \
	 * @return
	 */
	public Set<Endpoint> getRelevantSources() {
	//	System.out.println(
	//			"TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§ in SourceSeleciton Performing Source Selection TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
	//	System.out.println("This is the sourceSelection in SourceSelection out of loop:"+getStmtToSources().values());
		
		Set<Endpoint> endpoints = new HashSet<Endpoint>();
		for (List<StatementSource> sourceList : getStmtToSources().values())
		{//System.out.println("This is the sourceSelection in SourceSelection:"+sourceList);
		//	ForkJoinPool fjp = new ForkJoinPool(6);
	//		fjp.submit(()->{	
			for (StatementSource source : sourceList)
				if(queryInfo.getFedXConnection().getEndpointManager().getEndpoint(source.getEndpointID())!=null)
				endpoints.add(queryInfo.getFedXConnection().getEndpointManager().getEndpoint(source.getEndpointID()));
			//});
		}	return endpoints;
	}

	/**
	 * Add a source to the given statement in the map (synchronized through map)
	 * 
	 * @param stmt
	 * @param source
	 */
	protected void addSource(StatementPattern stmt, StatementSource source) {
		// The list for the stmt mapping is already initialized
	//	System.out.println("This is the addSource in SourceSelection out of loop:"+stmtToSources.get(stmt));
		
		List<StatementSource> sources = stmtToSources.get(stmt);
		//synchronized (sources) {
			sources.add(source);
		//}
	}
}
