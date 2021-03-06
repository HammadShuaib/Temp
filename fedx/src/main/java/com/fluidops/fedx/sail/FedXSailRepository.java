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

package com.fluidops.fedx.sail;

import java.util.concurrent.ForkJoinPool;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.SailException;

import com.fluidops.fedx.FedX;
import com.fluidops.fedx.FedXConnection;

/**
 * A special {@link SailRepository} which performs the actions as defined in
 * {@link FedXSailRepositoryConnection}.
 * 
 * @author as
 */
public class FedXSailRepository extends SailRepository {
	public FedXSailRepository(FedX sail) {
		super(sail);
		ForkJoinPool fjp = new ForkJoinPool(6);
				
		fjp.submit(()->this.setHttpClient(sail.getHttpClient()));
fjp.shutdown();
	}

	@Override
	public SailRepositoryConnection getConnection() throws RepositoryException {
		try {
			ForkJoinPool fjp = new ForkJoinPool(6);
			return fjp.submit(()->new FedXSailRepositoryConnection(this, (FedXConnection) getSail().getConnection())).join();
		} catch (SailException e) {
			throw new RepositoryException(e);
		}
	}
}
