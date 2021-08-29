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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

import com.fluidops.fedx.algebra.EmptyNJoin;
import com.fluidops.fedx.algebra.EmptyResult;
import com.fluidops.fedx.algebra.ExclusiveGroup;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.NJoin;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.algebra.TrueStatementPattern;
import com.fluidops.fedx.exception.OptimizationException;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.util.QueryStringUtil;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * Optimizer with the following tasks:
 * 
 * 1. Group {@link ExclusiveStatement} into {@link ExclusiveGroup} 2. Adjust the
 * join order using {@link JoinOrderOptimizer}
 * 
 * 
 * @author as
 */
public class StatementGroupOptimizer3 extends AbstractQueryModelVisitor<OptimizationException> implements FedXOptimizer {

	public static Logger log = LoggerFactory.getLogger(StatementGroupOptimizer3.class);
	
	protected final QueryInfo queryInfo;
	protected final ConcurrentHashMap<StatementPattern, List<StatementSource>>  stmtToSources;
	public StatementGroupOptimizer3(QueryInfo queryInfo,ConcurrentHashMap<StatementPattern, List<StatementSource>>  sourceSelection1) {
		super();
		this.queryInfo = queryInfo;
		this.stmtToSources = sourceSelection1;
	}

	@Override
	public void optimize(TupleExpr tupleExpr) {
		//System.out.println("This is statementGroupOptimizer1");
		tupleExpr.visit(this);
	}

	@Override
	public void meet(Service tupleExpr) {
		// stop traversal
	}

	@Override
	public void meetOther(QueryModelNode node) {
		//System.out.println("This is statementGroupOptimizer2");

		if (node instanceof NJoin) {
			super.meetOther(node); // depth first
			meetNJoin((NJoin) node);
		} else {
			super.meetOther(node);
		}
	}

	protected void meetNJoin(NJoin node) {
		//System.out.println("This is statementGroupOptimizer3");

		//System.out.println("this is now in meetNJoin:" + node);
		LinkedList<TupleExpr> newArgs = new LinkedList<TupleExpr>();

		LinkedList<TupleExpr> argsCopy = new LinkedList<TupleExpr>(node.getArgs());
		while (!argsCopy.isEmpty()) {

			TupleExpr t = argsCopy.removeFirst();
			//System.out.println("this is MeetN in StatementGroupOptimizer1:" + t);
			/*
			 * If one of the join arguments cannot produce results, the whole join
			 * expression does not produce results. => replace with empty join and return
			 */
			if (t instanceof EmptyResult) {
				node.replaceWith(new EmptyNJoin(node, queryInfo));
				//System.out.println("this is MeetN in StatementGroupOptimizer0:" + node);

				return;
			}

			/*
			 * for exclusive statements find those belonging to the same source (if any) and
			 * form exclusive group
			 */
			else if (t instanceof ExclusiveStatement) {
				ExclusiveStatement current = (ExclusiveStatement) t;
				//System.out.println("this is MeetN in StatementGroupOptimizer2:" + current);

				List<ExclusiveStatement> l = null;
				for (TupleExpr te : argsCopy) {
					/*
					 * in the remaining join args find exclusive statements having the same source,
					 * and add to a list which is later used to form an exclusive group
					 */
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
				//System.out.println("this is MeetN in StatementGroupOptimizer3:" + l);

				// check if we can construct a group, otherwise add directly
				if (l != null) {
					argsCopy.add(current); // will be removed in one row if pass checking
					checkExclusiveGroup(l);
					argsCopy.removeAll(l);
					newArgs.add(new ExclusiveGroup(l, current.getOwner(), queryInfo));
					////System.out.println("THis is now going into ExclusiveGruop in StatementGroupOptimizer:" + l + "--"
					//		+ current.getOwner() + "--" + queryInfo);

				} else {
					newArgs.add(current);

				}
				//System.out.println("this is MeetN in StatementGroupOptimizer4:" + newArgs);

			}

			/*
			 * statement yields true in any case, not needed for join
			 */
			else if (t instanceof TrueStatementPattern) {
				if (log.isDebugEnabled())
					log.debug("Statement " + QueryStringUtil.toString((StatementPattern) t)
							+ " yields results for at least one provided source, prune it.");
			}

			else
				newArgs.add(t);
		}
		//System.out.println("this is MeetN in StatementGroupOptimizer5:" + newArgs);

		// if the join args could be reduced to just one, e.g. OwnedGroup
		// we can safely replace the join node
		if (newArgs.size() == 1) {
			log.debug("Join arguments could be reduced to a single argument, replacing join node.");
			node.replaceWith(newArgs.get(0));
			return;
		}

		// in rare cases the join args can be reduced to 0, e.g. if all statements are
		// TrueStatementPatterns. We can safely replace the join node in such case
		if (newArgs.isEmpty()) {
			log.debug("Join could be pruned as all join statements evaluate to true, replacing join with true node.");
			node.replaceWith(new TrueStatementPattern(new StatementPattern()));
			return;
		}

		List<TupleExpr> optimized = newArgs;

		// optimize the join order
		optimizeJoinOrder(node, optimized);
	}

	public void checkExclusiveGroup(List<ExclusiveStatement> exclusiveGroupStatements) {
		// by default do nothing
	}

	public void optimizeJoinOrder(NJoin node, List<TupleExpr> joinArgs) {
		//System.out.println("This is statementGroupOptimizer4 before:" + joinArgs);

		List<TupleExpr> optimized = JoinOrderOptimizer.optimizeJoinOrder(joinArgs);
		// exchange the node
		//System.out.println("This is statementGroupOptimizer4 after:" + optimized);
		NJoin newNode = new NJoin(optimized, queryInfo);
		node.replaceWith(newNode);
	}

	public ExclusiveGroup ReturnExclusiveGroup(NJoin node) {
		//System.out.println("This is statementGroupOptimizer6");

		//System.out.println("this is now in meetNJoin:" + node);
		LinkedList<TupleExpr> newArgs = new LinkedList<TupleExpr>();
		TupleExpr t = null;
		LinkedList<TupleExpr> argsCopy = new LinkedList<TupleExpr>(node.getArgs());
		ExclusiveStatement current = null;
		List<ExclusiveStatement> l = null;
		ExclusiveGroup eg = null;
		while (!argsCopy.isEmpty()) {

			t = argsCopy.removeFirst();
			//System.out.println("this is MeetN in StatementGroupOptimizer1:" + t);
			/*
			 * If one of the join arguments cannot produce results, the whole join
			 * expression does not produce results. => replace with empty join and return
			 */
			if (t instanceof EmptyResult) {
				node.replaceWith(new EmptyNJoin(node, queryInfo));
				//System.out.println("this is MeetN in StatementGroupOptimizer0:" + node);
			}

			/*
			 * for exclusive statements find those belonging to the same source (if any) and
			 * form exclusive group
			 */
			else if (t instanceof ExclusiveStatement) {
				current = (ExclusiveStatement) t;
				//System.out.println("this is MeetN in StatementGroupOptimizer2:" + current);

				l = null;
				for (TupleExpr te : argsCopy) {
					/*
					 * in the remaining join args find exclusive statements having the same source,
					 * and add to a list which is later used to form an exclusive group
					 */
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
				//System.out.println("this is MeetN in StatementGroupOptimizer3:" + l);

				// check if we can construct a group, otherwise add directly
				if (l != null) {
					argsCopy.add(current); // will be removed in one row if pass checking
					checkExclusiveGroup(l);
					argsCopy.removeAll(l);
					newArgs.add(new ExclusiveGroup(l, current.getOwner(), queryInfo));
					if (eg == null) {
						eg = new ExclusiveGroup(l, current.getOwner(), queryInfo);
						continue;
					}

					//System.out.println("THis is now going into ExclusiveGruop in StatementGroupOptimizer:" + l + "--"
						//	+ current.getOwner() + "--" + queryInfo);
					for (ExclusiveStatement l1 : l) {
						eg.addExclusiveStatements(l1);
						//System.out.println("This is during addition:" + eg);
					}
				} else {
					newArgs.add(current);
					//System.out.println("THis is now the difficult part in Optimizer:" + newArgs);
					// eg.addExclusiveStatements(current);
					// for (StatementSource c:current.getStatements())
					// eg= new ExclusiveGroup(l, current.getOwner(), queryInfo);

				}
				//System.out.println("this is MeetN in StatementGroupOptimizer4:" + newArgs);

			}

			/*
			 * statement yields true in any case, not needed for join
			 */
			else if (t instanceof TrueStatementPattern) {
				if (log.isDebugEnabled())
					log.debug("Statement " + QueryStringUtil.toString((StatementPattern) t)
							+ " yields results for at least one provided source, prune it.");
			}

			else {
				newArgs.add(t);
				eg.addExclusiveStatements((ExclusiveStatement) t);
				//System.out.println("this iss MeetN in StatementGroupOptimizer5:" + t + "--" + newArgs + "--" + eg);
			}

		}
		//System.out.println("this is MeetN in StatementGroupOptimizer5:" + newArgs);

		// if the join args could be reduced to just one, e.g. OwnedGroup
		// we can safely replace the join node

		return eg;
		// List<TupleExpr> optimized = newArgs;

		// optimize the join order
		// optimizeJoinOrder(node, optimized);
	}
}
