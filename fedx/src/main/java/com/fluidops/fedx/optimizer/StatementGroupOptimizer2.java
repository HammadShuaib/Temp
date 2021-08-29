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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.eclipse.rdf4j.query.BindingSet;
//import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.fluidops.fedx.algebra.ExclusiveGroup;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.NJoin;
import com.fluidops.fedx.exception.OptimizationException;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.trunk.description.Statistics;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.google.common.collect.Iterators;
import com.opencsv.CSVWriter;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Op;
//import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Symbol;


/**
 * Optimizer with the following tasks:
 * 
 * 1. Group {@link ExclusiveStatement} into {@link ExclusiveGroup} 2. Adjust the
 * join order using {@link JoinOrderOptimizer}
 * 
 * 
 * @author as
 */
public class StatementGroupOptimizer2 extends AbstractQueryModelVisitor<OptimizationException>
		implements FedXOptimizer {
	Query query1 = null;
	String[][] print=null;
	
	//public static Logger log = LoggerFactory.getLogger(StatementGroupOptimizer.class);
	ArrayList<Binding> resultoutput =new ArrayList<>();
	
	protected final QueryInfo queryInfo;
	public BindingSet bindings1 = null;
static int Completion=0;
	public StatementGroupOptimizer2(QueryInfo queryInfo) {
		super();
		this.queryInfo = queryInfo;
	}

	@Override
	public void optimize(TupleExpr tupleExpr) {
	//	log.info("This is statementGroupOptimizer1");
		tupleExpr.visit(this);

	}

	@Override
	public void meet(Service tupleExpr) {
		// stop traversal
	}

	@Override
	public void meetOther(QueryModelNode node) {
	
		if (node instanceof NJoin) {
		//	System.out.println("This is statementGroupOptimizer2123123123123");

			super.meetOther(node); // depth first
			meetNJoin((NJoin) node);
		} else {
			super.meetOther(node);
			}
}	

	public void meetNJoin(NJoin node) {
		Completion++;
		
		//log.info("This is statementGroupOptimizer6");

//		log.info("this is now in meetNJoin:" + node);
		/*
		 * while (!argsCopy.isEmpty()) {
		 * 
		 * t = argsCopy.element();//removeFirst();
		 * log.info("this is MeetN in StatementGroupOptimizer1:"+t); /* If one
		 * of the join arguments cannot produce results, the whole join expression does
		 * not produce results. => replace with empty join and return
		 * 
		 * if (t instanceof EmptyResult) { node.replaceWith(new EmptyNJoin(node,
		 * queryInfo));
		 * log.info("this is MeetN in StatementGroupOptimizer0:"+node);
		 * return; }
		 * 
		 * 
		 * /* for exclusive statements find those belonging to the same source (if any)
		 * and form exclusive group
		 * 
		 * else if (t instanceof ExclusiveStatement) { current = (ExclusiveStatement)t;
		 * t1=t;
		 * 
		 * l= null; for (TupleExpr te : argsCopy) { /* in the remaining join args find
		 * exclusive statements having the same source, and add to a list which is later
		 * used to form an exclusive group
		 * 
		 * log.info("this is MeetN in StatementGroupOptimizer2:"+te);
		 * 
		 * // if (te instanceof ExclusiveStatement) { ExclusiveStatement check =
		 * (ExclusiveStatement)te;
		 * 
		 * log.info("this is MeetN in StatementGroupOptimizer33:"+check); //
		 * if (check.getOwner().equals(current.getOwner())) { if (l == null) { l = new
		 * ArrayList<ExclusiveStatement>(); // l.add(current);
		 * log.info("this is MeetN in StatementGroupOptimizer31:"+l);
		 * 
		 * } l.add(check);
		 * log.info("this is MeetN in StatementGroupOptimizer32:"+l);
		 * 
		 * // } //} }
		 * log.info("this is MeetN in StatementGroupOptimizer3:"+l);
		 * 
		 * 
		 * // check if we can construct a group, otherwise add directly if (l != null) {
		 * 
		 * argsCopy.add(current); // will be removed in one row if pass checking
		 * //if(eg==null) { // eg= new ExclusiveGroup(l, current.getOwner(), queryInfo);
		 * // continue; // }
		 * 
		 * checkExclusiveGroup(l); argsCopy.removeAll(l); newArgs.add(new
		 * ExclusiveGroup(l, current.getOwner(), queryInfo));
		 * 
		 * System.out.
		 * println("THis is now going into ExclusiveGruop in StatementGroupOptimizer:"+l
		 * +"--"+current.getOwner()+"--"+queryInfo+"--"+t1); //for(ExclusiveStatement
		 * l1:l) { // eg.addExclusiveStatements(l1);
		 * //log.info("This is during addition:"+eg); // } } else {
		 * //eg.addExclusiveStatements(current); newArgs.add(current);
		 * log.info("THis is now the difficult part in Optimizer:"+newArgs+
		 * "--"+current+"--"+(ExclusiveStatement)t);
		 * 
		 * 
		 * // egList.add(eg.addExclusiveStatements(current)); //
		 * eg.addExclusiveStatements(new ExclusiveGroup(c, current.getOwner(),
		 * queryInfo));
		 * 
		 * } egList.add(new ExclusiveGroup(l, current.getOwner(), queryInfo));
		 * 
		 * log.info("this is MeetN in StatementGroupOptimizer4:"+newArgs+"--"+
		 * eg); // egList.add(eg); }
		 * 
		 * 
		 * /* statement yields true in any case, not needed for join
		 * 
		 * else if (t instanceof TrueStatementPattern) { if (log.isDebugEnabled())
		 * log.debug("Statement " + QueryStringUtil.toString((StatementPattern)t) +
		 * " yields results for at least one provided source, prune it."); }
		 * 
		 * else { newArgs.add(t); eg.addExclusiveStatements((ExclusiveStatement)t);
		 * log.info("this iss MeetN in StatementGroupOptimizer5:"+t+"--"+
		 * newArgs+"--"+eg); //egList.add((ExclusiveStatement)t); }
		 * 
		 * 
		 * }
		 * log.info("this is MeetN in StatementGroupOptimizer5:"+newArgs+"--"+
		 * eg); // if the join args could be reduced to just one, e.g. OwnedGroup // we
		 * can safely replace the join node
		 * 
		 * 
		 * //return eg; // List<TupleExpr> optimized = newArgs;
		 * 
		 * // optimize the join order // optimizeJoinOrder(node, optimized);
		 * 
		 * /*if (newArgs.size() == 1) { log.
		 * debug("Join arguments could be reduced to a single argument, replacing join node."
		 * ); node.replaceWith( newArgs.get(0) ); return; }
		 */

		// in rare cases the join args can be reduced to 0, e.g. if all statements are
		// TrueStatementPatterns. We can safely replace the join node in such case
		/*
		 * if (newArgs.isEmpty()) { log.
		 * debug("Join could be pruned as all join statements evaluate to true, replacing join with true node."
		 * ); node.replaceWith( new TrueStatementPattern( new StatementPattern()));
		 * return; }
		 */

		// for(ExclusiveGroup e:egList) {
if(node!=null)
//		log.info("This is now in StatementGroupOptimizer2:" + node.getQueryInfo().getQuery());
	//if(Completion<2)
		def(node.getQueryInfo().getQuery());
	//else return;
		// } List<TupleExpr> optimized = newArgs;

		// optimize the join order
//		optimizeJoinOrder(node, optimized);
	}

	public void checkExclusiveGroup(String query1) {
		// by default do nothing
	}

	public void optimizeJoinOrder(NJoin node, List<TupleExpr> joinArgs) {
	//	log.info("This is statementGroupOptimizer4 before:" + joinArgs);

		// List<TupleExpr> optimized = JoinOrderOptimizer.optimizeJoinOrder(joinArgs);
		// exchange the node
		// NJoin newNode = new NJoin(optimized, queryInfo);
		// node.replaceWith(newNode);
	}

	
	public void def(String query) {

		Optimizer opt = new Optimizer();

		// Pair<String, Boolean> preparedQuery = QueryStringUtil.selectQueryString(eg,
		// opt.getBindings(), eg.getFilterExpr());
		//log.info("this is in sparqlfederation1:" + query);
		// String abs = query;
		List<String> qnames = Arrays.asList(query);
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
		//log.info("Query is already running!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + results.toString() + "--"
		//		+ qe.toString());
		QuerySolution querySolution = null;
		long count = 0;
		while (results.hasNext()) {
		if(Completion>1)
			System.exit(0);
			querySolution = results.next();
		//	System.out.println("This is the continous solution in LHD Count:" + count + " Solution:" + querySolution);

			count++;

			
			if(BGPEval.finalResultSize==count) 
	System.out.println("This is the last value in LHD: Count:" +LocalTime.now()+"--"+ count + " Solution:" + querySolution);
	


		}
		//System.out.println("This is the query:"+ParaEng.arg);
		System.out.println("This is the final solution in LHD: Count:" +LocalTime.now()+"--"+ count + " Solution:" + querySolution);
		System.exit(0);
		
		//results=null;
		/*
		 * Element querypattern = query1.getQueryPattern(); Op op =
		 * Algebra.compile(querypattern) ; // ElementTriplesBlock element = new
		 * ElementTriplesBlock(); //BaseGraphMaker bgm = new BaseGraphMaker(); // String
		 * gelement=querypattern.toString(); OpBGP opbgp= getBGP(op); BasicPattern bgp
		 * =opbgp.getPattern(); List<Triple> t2 = new ArrayList<>(); for(Triple abc:bgp)
		 * { t2.add(abc); } // Triple t = bgp.get(0) ; Triple t1 = bgp.get(1) ; //Graph
		 * def = BaseGraphMaker.createGraph(gelement); //Element e = Element; //
		 * QuerySolutionMap qsm = new QuerySolutionMap(); //RDFNode rdfnode=
		 * qsm._get(gelement); //OpBGP opbgp = TransformFilterPlacement.getBGP(op);
		 * 
		 * 
		 * //Element bp = element.getPattern(); Optimizer opt1=new Optimizer();
		 * 
		 * StageGen stagegen = new StageGen(); opt1.getSubjectCount();
		 * opt1.getObjectCount(); opt1.getTripleCount();
		 * log.info("This is in SparqlFederation:"+opt1.getSubjectCount()+"--"
		 * +preparedQuery.getFirst()+"--"
		 * +"--"+querypattern+"--"+op+"--"+opbgp+"--"+bgp);
		 * 
		 * log.info("1$$2$31231231231231----------This is the new task:"+"--"+
		 * preparedQuery.getFirst()+"--"
		 * +"--"+querypattern+"--"+opt1.getEndpointE()+"--"+op+"--"+opbgp+"--"+bgp );
		 * 
		 * // EndpointArray=opt.getEndpoints(); // OpFilter opf =new OpFilter(); //
		 * OpFilter opfilter;
		 * log.info("1$$2$31231231231231----------This is the new task1:"+"--"
		 * +opt1.getSubjectCount()+"--"+preparedQuery.getFirst()+"--"
		 * +"--"+querypattern+"--"+opt1.getEndpointE()+"--"+op+"--"+opbgp+"--"+bgp );
		 * 
		 * //opfilter=OpFilter.filter(op); stagegen.make(t2);
		 */

	}

}
