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

package com.fluidops.fedx.algebra;

import java.io.File;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.fluidops.fedx.evaluation.TripleSource;
import com.fluidops.fedx.evaluation.iterator.SingleBindingSetIteration;
import com.fluidops.fedx.exception.IllegalQueryException;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.Pair;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.trunk.description.Statistics;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.fluidops.fedx.util.QueryStringUtil;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.Symbol;

/**
 * Represents a StatementPattern that can only produce results at a single
 * endpoint, the owner.
 * 
 * @author Andreas Schwarte
 */
public class ExclusiveStatement extends FedXStatementPattern {
	private static final long serialVersionUID = 3290145471772314112L;
	Query query1 = null;

	public ExclusiveStatement(StatementPattern node, StatementSource owner, QueryInfo queryInfo) {
		super(node, queryInfo);
		addStatementSource(owner);
		queryInfo.numSources.incrementAndGet();
		queryInfo.totalSources.incrementAndGet();
	//	System.out.println("This is in ExclusiveStatement:" + node + "--" + owner + "--" + queryInfo);

	}

	public StatementSource getOwner() {
		return getStatementSources().get(0);
	}

	public List<StatementSource> getStatements() {
		// XXX make a copy? (or copyOnWrite list?)
		return getStatementSources();
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BindingSet bindings) {
		if (bindings == null)
			return new EmptyIteration<BindingSet, QueryEvaluationException>();

		Endpoint ownedEndpoint = getQueryInfo().getFedXConnection().getEndpointManager()
				.getEndpoint(getOwner().getEndpointID());
		RepositoryConnection ownedConnection = ownedEndpoint.getConn();
		TripleSource t = ownedEndpoint.getTripleSource();

		/*
		 * Implementation note: for some endpoint types it is much more efficient to use
		 * prepared queries as there might be some overhead (obsolete optimization) in
		 * the native implementation. This is for instance the case for SPARQL
		 * connections. In contrast for NativeRepositories it is much more efficient to
		 * use getStatements(subj, pred, obj) instead of evaluating a prepared query.
		 */

		if (t.usePreparedQuery()) {
			Pair<String, Boolean> preparedQuery;
			try {
	System.out.println("This is preparedquery in Exclusive Statement");
				preparedQuery = QueryStringUtil.selectQueryString(this, bindings, filterExpr);
	//		asd
			
				List<String> qnames = Arrays.asList(preparedQuery.getFirst());
				for (String curQueryName : qnames) {
					query1 = QueryFactory.create(curQueryName);
				}
				List<String> resultvar = query1.getResultVars();
				//log.info("this is in sparqlfederation:" + query + "--" + query1 + "--" + resultvar);

				Statistics config = new Statistics(new File("summaries/complete-largeRDFBench-summaries.n3"));
				Symbol property = Symbol.create("config");
				ForkJoinPool fjp = new ForkJoinPool(6);
		fjp.submit(()->		{ARQ.getContext().set(property, config);
				ParaEng.register();});
		fjp.shutdown();

		ForkJoinPool fjp1 = new ForkJoinPool(6);
				Model model = fjp1.submit(()->ModelFactory.createDefaultModel()).join();
				fjp1.shutdown();
				ForkJoinPool fjp2 = new ForkJoinPool(6);
				Dataset dataset = fjp2.submit(()->DatasetFactory.create(model)).join();
				fjp2.shutdown();
				// initiate timer and results count for each query
				ForkJoinPool fjp3 = new ForkJoinPool(6);
				QueryExecution qe = fjp3.submit(()->QueryExecutionFactory.create(query1, dataset)).join();
				fjp3.shutdown();
				ResultSet results = qe.execSelect();
			//int ResultSize=	Iterators.size(results);
				//System.out.println("Query is already running!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + results.toString() + "--"
				//		+ qe.toString());
				QuerySolution querySolution = null;
				long count = 0;
				while (results.hasNext()) {
					querySolution = results.next();
				//	System.out.println("This is the continous solution in LHD Count:" + count + " Solution:" + querySolution);

					count++;

					
					if(BGPEval.finalResultSize==count) 
			System.out.println("This is the last value in LHD: Count:" +LocalTime.now()+"--"+ count + " Solution:" + querySolution);
			


				}
				System.out.println("This is the final solution in LHD: Count:" +LocalTime.now()+"--"+ count + " Solution:" + querySolution);
			
			} catch (IllegalQueryException e1) {
				// TODO there might be an issue with filters being evaluated => investigate
				/*
				 * all vars are bound, this must be handled as a check query, can occur in joins
				 */
				if (t.hasStatements(this, ownedConnection, bindings))
					return new SingleBindingSetIteration(bindings);
				return new EmptyIteration<BindingSet, QueryEvaluationException>();
			}

			return t.getStatements(preparedQuery.getFirst(), ownedConnection, bindings,
					(preparedQuery.getSecond() ? null : filterExpr));
		} else {
			return t.getStatements(this, ownedConnection, bindings, filterExpr);
		}
	}

	@Override
	public void visit(FedXExprVisitor v) {
		v.meet(this);
	}

	@Override
	protected CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BindingSet bindings,
			List<StatementSource> sources) {
		return evaluate(bindings);
	}
}
