package com.fluidops.fedx.optimizer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//import javax.xml.ws.Binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.ConstantOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.StrictEvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.FedXFactory;
import com.fluidops.fedx.trunk.stream.engine.util.QueryProvider;
import com.fluidops.fedx.Config;
//import org.eclipse.rdf4j.query.Query;

import com.fluidops.fedx.FedX;
import com.fluidops.fedx.Util;
import com.fluidops.fedx.algebra.SingleSourceQuery;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.util.QueryStringUtil;
import com.fluidops.fedx.Summary;
import com.fluidops.fedx.algebra.StatementSource;
import com.hp.hpl.jena.graph.Triple;
//import org.apache.jena.graph.NodeFactory;
//import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import com.fluidops.fedx.trunk.lhd;
import com.fluidops.fedx.trunk.description.RemoteService;
import org.apache.jena.graph.NodeFactory;

import com.hp.hpl.jena.query.Query;
import java.util.ArrayList;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.query.QueryFactory;

import com.fluidops.fedx.optimizer.Optimizer;
//import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
//import com.hp.hpl.jena.sparql.syntax.Element1;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.graph.Triple;
//import org.apache.jena.graph.impl.BaseGraphMaker ;
//import org.apache.jena.graph.* ;
//import com.hp.hpl.jena.query.QuerySolutionMap;
//import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.fluidops.fedx.algebra.SingleSourceQuery;
import com.fluidops.fedx.algebra.FedXExpr;
import com.fluidops.fedx.algebra.FedXExprVisitor;
import com.fluidops.fedx.algebra.ExclusiveGroup;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import com.fluidops.fedx.algebra.FilterExpr;
import com.fluidops.fedx.algebra.IndependentJoinGroup;
import com.fluidops.fedx.algebra.NJoin;
import com.fluidops.fedx.algebra.NUnion;
import com.fluidops.fedx.algebra.ProjectionWithBindings;
import com.fluidops.fedx.algebra.StatementTupleExpr;
import com.fluidops.fedx.algebra.EmptyResult;
import com.fluidops.fedx.algebra.FedXService;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import java.util.LinkedList;
import com.fluidops.fedx.algebra.NJoin;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.EmptyNJoin;

public class EvalVisitor implements FedXExprVisitor {
	CloseableIteration<BindingSet, QueryEvaluationException> result = null;
	BindingSet bindings;

	public EvalVisitor(BindingSet bindings) {
	//	System.out.println(
	//			"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!FederationEvaluation44444444444444444444-----------------------------");

		this.bindings = bindings;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> get() {
	//	System.out.println(
	//			"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!FederationEvaluation55555555555555555555555555------------------------------");

		return result;
	}

	public static void checkExclusiveGroup(List<ExclusiveStatement> exclusiveGroupStatements) {
		// by default do nothing
	}

	@Override
	public void meet(StatementTupleExpr es) {
		// result = es.evaluate(bindings);
	}

	@Override
	public void meet(ExclusiveGroup eg) {
//		System.out.println("THis is also now in meet group first:" + bindings);
		result = eg.evaluate(bindings);
	}

	@Override
	public void meet(NJoin nj) {
		// result = evaluateNJoin(nj, bindings);
	}

	@Override
	public void meet(NUnion nu) {
		// result = evaluateNaryUnion(nu, bindings);
	}

	@Override
	public void meet(EmptyResult er) {
		// result = new EmptyIteration<BindingSet, QueryEvaluationException>();
	}

	@Override
	public void meet(SingleSourceQuery ssq) {
		// result = evaluateSingleSourceQuery(ssq, bindings);
	}

	@Override
	public void meet(FedXService fs) {
		// result = evaluateService(fs, bindings);
	}

	@Override
	public void meet(ProjectionWithBindings pwb) {
		// result = evaluateProjectionWithBindings(pwb, bindings);
	}

	@Override
	public void meet(IndependentJoinGroup ijg) {
		// result = evaluateIndependentJoinGroup(ijg, bindings); // XXX
	}

	public static void evaluate(TupleExpr parsed, Dataset dataset, BindingSet bindings,
			StrictEvaluationStrategy strategy, QueryInfo queryInfo) {
/*
		// List<TupleExpr> expr1=new ArrayList<TupleExpr>();
		List<TupleExpr> expr1 = new ArrayList<TupleExpr>();
		TupleExpr expr = Optimizer.optimize(parsed, dataset, bindings, strategy, queryInfo);
		expr1.add(expr);

		LinkedList<TupleExpr> newArgs = new LinkedList<TupleExpr>();

		NJoin node = new NJoin(expr1, queryInfo);
		LinkedList<TupleExpr> argsCopy = new LinkedList<TupleExpr>(node.getArgs());
		while (!argsCopy.isEmpty()) {

			TupleExpr t = argsCopy.removeFirst();
			//System.out.println("this is MeetN in StatementGroupOptimizer1:" + t);
			/*
			 * If one of the join arguments cannot produce results, the whole join
			 * expression does not produce results. => replace with empty join and return
			 *
			if (t instanceof EmptyResult) {
				node.replaceWith(new EmptyNJoin(node, queryInfo));
				//System.out.println("this is MeetN in StatementGroupOptimizer0:" + node);

				return;
			}

			/*
			 * for exclusive statements find those belonging to the same source (if any) and
			 * form exclusive group
			 *
			else if (t instanceof ExclusiveStatement) {
				ExclusiveStatement current = (ExclusiveStatement) t;
			//	System.out.println("this is MeetN in StatementGroupOptimizer2:" + current);

				List<ExclusiveStatement> l = null;
				for (TupleExpr te : argsCopy) {
					/*
					 * in the remaining join args find exclusive statements having the same source,
					 * and add to a list which is later used to form an exclusive group
					 *
					if (te instanceof ExclusiveStatement) {
						ExclusiveStatement check = (ExclusiveStatement) te;
						if (check.getOwner().equals(current.getOwner())) {
							if (l == null) {
								l = new ArrayList<ExclusiveStatement>();
								l.add(current);
							}
							l.add(check);
						}
					}
				}
			//	System.out.println("this is MeetN in StatementGroupOptimizer3:" + l);

				// check if we can construct a group, otherwise add directly
				if (l != null) {
					argsCopy.add(current); // will be removed in one row if pass checking
					checkExclusiveGroup(l);
					argsCopy.removeAll(l);
					newArgs.add(new ExclusiveGroup(l, current.getOwner(), queryInfo));
				}
			//	System.out.println("this is MeetN in StatementGroupOptimizer4:" + newArgs);

			}

		}

	//	System.out.println("!!!!This is fedeval eval---------------------" + expr + "--" + bindings);
		// ExclusiveGroup eg = new ExclusiveGroup(expr,null,queryInfo);

		// ExclusiveGroup.evaluate(bindings);
		// if (expr instanceof FedXExpr) {
		// FedXExpr fexpr = (FedXExpr)expr;
		// EvalVisitor visitor = new EvalVisitor(bindings);
		// fexpr.visit(visitor);
		// System.out.println("!!!!This is fedeval
		// eval---------------------"+expr+"--"+bindings+"--"+visitor.get());

		// visitor.get();
		// }

		// return null;
	*/}
}