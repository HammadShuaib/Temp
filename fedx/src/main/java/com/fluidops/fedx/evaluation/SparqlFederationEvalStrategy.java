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

package com.fluidops.fedx.evaluation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.fluidops.fedx.FedXConnection;
import com.fluidops.fedx.algebra.CheckStatementPattern;
import com.fluidops.fedx.algebra.ExclusiveGroup;
import com.fluidops.fedx.algebra.FilterTuple;
import com.fluidops.fedx.algebra.FilterValueExpr;
import com.fluidops.fedx.algebra.IndependentJoinGroup;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.algebra.StatementTupleExpr;
import com.fluidops.fedx.evaluation.concurrent.ControlledWorkerScheduler;
import com.fluidops.fedx.evaluation.iterator.BoundJoinConversionIteration;
import com.fluidops.fedx.evaluation.iterator.BufferedCloseableIterator;
import com.fluidops.fedx.evaluation.iterator.FilteringIteration;
import com.fluidops.fedx.evaluation.iterator.GroupedCheckConversionIteration;
import com.fluidops.fedx.evaluation.iterator.IndependentJoingroupBindingsIteration;
import com.fluidops.fedx.evaluation.iterator.IndependentJoingroupBindingsIteration3;
import com.fluidops.fedx.evaluation.iterator.QueueIteration;
import com.fluidops.fedx.evaluation.iterator.SingleBindingSetIteration;
import com.fluidops.fedx.evaluation.join.ControlledWorkerBoundJoin;
import com.fluidops.fedx.exception.IllegalQueryException;
import com.fluidops.fedx.structures.Pair;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.util.QueryStringUtil;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;

import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.fluidops.fedx.optimizer.Optimizer;
//import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
//import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
//import com.hp.hpl.jena.sparql.syntax.Element1;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.graph.Triple;
//import org.apache.jena.graph.impl.BaseGraphMaker ;
//import org.apache.jena.graph.* ;
//import com.hp.hpl.jena.query.QuerySolutionMap;
//import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.fluidops.fedx.trunk.description.Statistics;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;

/**
 * Implementation of a federation evaluation strategy which provides some
 * special optimizations for SPARQL (remote) endpoints. The most important
 * optimization is to used prepared SPARQL Queries that are already created
 * using Strings.
 * 
 * Joins are executed using {@link ControlledWorkerBoundJoin}.
 * 
 * @author Andreas Schwarte
 *
 */
public class SparqlFederationEvalStrategy extends FederationEvalStrategy {

	public SparqlFederationEvalStrategy(FedXConnection conn) {
		super(conn);
	}

	FedXConnection conn1 = conn;
	Query query1 = null;

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluateBoundJoinStatementPattern(
			StatementTupleExpr stmt, List<BindingSet> bindings) {
		System.out.println(
				"1-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111222:");

		// we can omit the bound join handling
		if (bindings.size() == 1)
			return evaluate(stmt, bindings.get(0));

		FilterValueExpr filterExpr = null;
		if (stmt instanceof FilterTuple)
			filterExpr = ((FilterTuple) stmt).getFilterExpr();

		Boolean isEvaluated = false;
		String preparedQuery = QueryStringUtil.selectQueryStringBoundUnion((StatementPattern) stmt, bindings,
				filterExpr, isEvaluated);
		
		
		

		CloseableIteration<BindingSet, QueryEvaluationException> result = evaluateAtStatementSources(preparedQuery,
				stmt.getStatementSources(), stmt.getQueryInfo());

		// apply filter and/or convert to original bindings
		if (filterExpr != null && !isEvaluated) {
			result = new BoundJoinConversionIteration(result, bindings); // apply conversion
			result = new FilteringIteration(this, filterExpr, result); // apply filter
			if (!result.hasNext()) {
				result.close();
				return new EmptyIteration<BindingSet, QueryEvaluationException>();
			}
		} else {
			result = new BoundJoinConversionIteration(result, bindings);
		}

		// in order to avoid leakage of http route during the iteration
		return new BufferedCloseableIterator<BindingSet, QueryEvaluationException>(result);
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluateGroupedCheck(CheckStatementPattern stmt,
			List<BindingSet> bindings) {
		System.out
				.println("2-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111");

		if (bindings.size() == 1)
			return stmt.evaluate(bindings.get(0));

		String preparedQuery = QueryStringUtil.selectQueryStringBoundCheck(stmt.getStatementPattern(), bindings);

		CloseableIteration<BindingSet, QueryEvaluationException> result = evaluateAtStatementSources(preparedQuery,
				stmt.getStatementSources(), stmt.getQueryInfo());

		// in order to avoid licking http route while iteration
		return new BufferedCloseableIterator<BindingSet, QueryEvaluationException>(
				new GroupedCheckConversionIteration(result, bindings));
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluateIndependentJoinGroup(
			IndependentJoinGroup joinGroup, BindingSet bindings) {
		System.out
				.println("3-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111");

		String preparedQuery = QueryStringUtil.selectQueryStringIndependentJoinGroup(joinGroup, bindings);

		try {
			List<StatementSource> statementSources = joinGroup.getMembers().get(0).getStatementSources(); // TODO this
																											// is only
																											// correct
																											// for the
																											// prototype
																											// (=>
																											// different
																											// endpoints)
			CloseableIteration<BindingSet, QueryEvaluationException> result = evaluateAtStatementSources(preparedQuery,
					statementSources, joinGroup.getQueryInfo());

			// return only those elements which evaluated positively at the endpoint
			result = new IndependentJoingroupBindingsIteration(result, bindings);

			// in order to avoid licking http route while iteration
			return new BufferedCloseableIterator<BindingSet, QueryEvaluationException>(result);
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}

	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluateIndependentJoinGroup(
			IndependentJoinGroup joinGroup, List<BindingSet> bindings) {
		System.out
				.println("4-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111");

		String preparedQuery = QueryStringUtil.selectQueryStringIndependentJoinGroup(joinGroup, bindings);

		try {
			List<StatementSource> statementSources = joinGroup.getMembers().get(0).getStatementSources(); // TODO this
																											// is only
																											// correct
																											// for the
																											// prototype
																											// (=>
																											// different
																											// endpoints)
			CloseableIteration<BindingSet, QueryEvaluationException> result = evaluateAtStatementSources(preparedQuery,
					statementSources, joinGroup.getQueryInfo());

			// return only those elements which evaluated positively at the endpoint
//			result = new IndependentJoingroupBindingsIteration2(result, bindings);
			result = new IndependentJoingroupBindingsIteration3(result, bindings);

			// in order to avoid licking http route while iteration
			return new BufferedCloseableIterator<BindingSet, QueryEvaluationException>(result);
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> executeJoin(ControlledWorkerScheduler joinScheduler,
			CloseableIteration<BindingSet, QueryEvaluationException> leftIter, TupleExpr rightArg, BindingSet bindings,
			QueryInfo queryInfo) {
		System.out
				.println("5-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111");

		ControlledWorkerBoundJoin join = new ControlledWorkerBoundJoin(joinScheduler, this, leftIter, rightArg,
				bindings, queryInfo);
		return join;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluateExclusiveGroup(ExclusiveGroup group,
			RepositoryConnection conn, TripleSource tripleSource, BindingSet bindings) {
		Query query = null;
		System.out.println(
				"61-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111: "
						+ tripleSource);

		try {

			Pair<String, Boolean> preparedQuery = QueryStringUtil.selectQueryString(group, bindings,
					group.getFilterExpr());
			System.out.println(
					"62-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111: "
							+ preparedQuery);

			
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
				System.out.println("This is the continous solution in LHD Count:" + count + " Solution:" + querySolution);

				count++;

				
				if(BGPEval.finalResultSize==count) 
		System.out.println("This is the last value in LHD: Count:" +LocalTime.now()+"--"+ count + " Solution:" + querySolution);
		


			}
			System.out.println("This is the final solution in LHD: Count:" +LocalTime.now()+"--"+ count + " Solution:" + querySolution);
			System.out
			.println("1-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111: "
					+ preparedQuery);

			System.exit(0);

			/*
			 * String abs = preparedQuery.getFirst();
			 * 
			 * List<String> qnames = Arrays.asList(abs); for (String curQueryName : qnames)
			 * { query=QueryFactory.create(curQueryName);
			 * 
			 * } List<String> resultvar = query.getResultVars(); Element querypattern =
			 * query.getQueryPattern(); Op op = Algebra.compile(querypattern) ; //
			 * ElementTriplesBlock element = new ElementTriplesBlock(); //BaseGraphMaker bgm
			 * = new BaseGraphMaker(); // String gelement=querypattern.toString(); OpBGP
			 * opbgp= getBGP(op); BasicPattern bgp =opbgp.getPattern(); List<Triple> t = new
			 * ArrayList<>(); for(Triple abc:bgp) { t.add(abc); } // Triple t = bgp.get(0) ;
			 * Triple t1 = bgp.get(1) ; //Graph def = BaseGraphMaker.createGraph(gelement);
			 * //Element e = Element; // QuerySolutionMap qsm = new QuerySolutionMap();
			 * //RDFNode rdfnode= qsm._get(gelement); // OpBGP opbgp =
			 * TransformFilterPlacement.getBGP(op);
			 * 
			 * 
			 * //Element bp = element.getPattern(); Optimizer opt=new Optimizer();
			 * System.out.println("1$$2$31231231231231----------This is the new task:"+query
			 * +"--"+preparedQuery.getFirst()+"--"
			 * +"--"+querypattern+"--"+opt.getEndpoints()+"--"+op+"--"+opbgp+"--"+bgp+"--"+t
			 * );
			 * 
			 * StageGen stagegen = new StageGen(); opt.getSubjectCount();
			 * opt.getObjectCount(); opt.getTripleCount(); //
			 * EndpointArray=opt.getEndpoints(); // OpFilter opf =new OpFilter(); //
			 * OpFilter opfilter;
			 * 
			 * //opfilter=OpFilter.filter(op); stagegen.make(t); //
			 * DatasetFactory.create(op); //OpBGP opbgp = (OpBGP)op ;
			 * //System.out.println("12312312312312123Is the expression Op BGP:"+OpBGP.isBGP
			 * (opfilter)+"--"+op.getPattern().getList());
			 * 
			 * // in order to avoid leaking http route while iteration
			 * 
			 * /* Query query1=null; String abs = preparedQuery.getFirst();
			 * System.out.println("this is in sparqlfederation:"+abs+"--"+preparedQuery);
			 * List<String> qnames = Arrays.asList(abs); for (String curQueryName : qnames)
			 * { query1=QueryFactory.create(curQueryName); } List<String> resultvar =
			 * query1.getResultVars(); Element querypattern = query1.getQueryPattern(); Op
			 * op = Algebra.compile(querypattern) ; // ElementTriplesBlock element = new
			 * ElementTriplesBlock(); //BaseGraphMaker bgm = new BaseGraphMaker(); // String
			 * gelement=querypattern.toString(); OpBGP opbgp= getBGP(op); BasicPattern bgp
			 * =opbgp.getPattern(); List<Triple> t = new ArrayList<>(); for(Triple abc:bgp)
			 * { t.add(abc); } // Triple t = bgp.get(0) ; Triple t1 = bgp.get(1) ; //Graph
			 * def = BaseGraphMaker.createGraph(gelement); //Element e = Element; //
			 * QuerySolutionMap qsm = new QuerySolutionMap(); //RDFNode rdfnode=
			 * qsm._get(gelement); // OpBGP opbgp = TransformFilterPlacement.getBGP(op);
			 * 
			 * 
			 * //Element bp = element.getPattern(); Optimizer opt=new Optimizer();
			 * 
			 * StageGen stagegen = new StageGen(); opt.getSubjectCount();
			 * opt.getObjectCount(); opt.getTripleCount();
			 * System.out.println("This is in SparqlFederation:"+opt.getSubjectCount()+"--"+
			 * query+"--"+preparedQuery.getFirst()+"--"
			 * +"--"+querypattern+"--"+opt.getEndpointE()+"--"+op+"--"+opbgp+"--"+bgp+"--"+t
			 * );
			 * 
			 * System.out.println("1$$2$31231231231231----------This is the new task:"+opt.
			 * getSubjectCount()+"--"+query+"--"+preparedQuery.getFirst()+"--"
			 * +"--"+querypattern+"--"+opt.getEndpointE()+"--"+op+"--"+opbgp+"--"+bgp+"--"+t
			 * );
			 * 
			 * // EndpointArray=opt.getEndpoints(); // OpFilter opf =new OpFilter(); //
			 * OpFilter opfilter;
			 * System.out.println("1$$2$31231231231231----------This is the new task1:"+opt.
			 * getSubjectCount()+"--"+query+"--"+preparedQuery.getFirst()+"--"
			 * +"--"+querypattern+"--"+opt.getEndpointE()+"--"+op+"--"+opbgp+"--"+bgp+"--"+t
			 * );
			 * 
			 * //opfilter=OpFilter.filter(op); stagegen.make(t);
			 */
			return new BufferedCloseableIterator<BindingSet, QueryEvaluationException>(
					tripleSource.getStatements(preparedQuery.getFirst(), conn, bindings,
							(preparedQuery.getSecond() ? null : group.getFilterExpr())));
		} catch (IllegalQueryException e) {
			// System.err.println("Here is an error:"+e);
			/* no projection vars, e.g. local vars only, can occur in joins */
			if (tripleSource.hasStatements(group, conn, bindings))
				return new SingleBindingSetIteration(bindings);
			return new EmptyIteration<BindingSet, QueryEvaluationException>();
		}

	}

	@Override
	public void evaluate(QueueIteration<BindingSet> qit, TupleExpr expr, List<BindingSet> bindings) {
		System.out
				.println("7-----------------------SparqlFederationEvalStrategy111111111111111111111111111111111111111");

		throw new NotImplementedException("evaluate");
	}

	private static OpBGP getBGP(Op op) {
		if (op instanceof OpBGP)
			return (OpBGP) op;

		if (op instanceof OpSequence) {
			// Is last in OpSequence an BGP?
			OpSequence opSeq = (OpSequence) op;
			List<Op> x = opSeq.getElements();
			if (x.size() > 0) {
				Op opTop = x.get(x.size() - 1);
				if (opTop instanceof OpBGP)
					return (OpBGP) opTop;
				// Drop through
			}
		}
		// Can't find.
		return null;
	}
}
