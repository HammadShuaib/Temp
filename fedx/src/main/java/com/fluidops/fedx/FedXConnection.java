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

package com.fluidops.fedx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;



import org.eclipse.rdf4j.common.iteration.AbstractCloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.DistinctIteration;
import org.eclipse.rdf4j.common.iteration.ExceptionConvertingIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSailConnection;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import com.fluidops.fedx.algebra.NJoin;
import com.fluidops.fedx.evaluation.EvaluationStrategyFactory;
import com.fluidops.fedx.evaluation.FederationEvalStrategy;
import com.fluidops.fedx.evaluation.SailFederationEvalStrategy;
import com.fluidops.fedx.evaluation.SparqlFederationEvalStrategy;
import com.fluidops.fedx.evaluation.iterator.RepositoryExceptionConvertingIteration;
import com.fluidops.fedx.evaluation.union.ControlledWorkerUnion;
import com.fluidops.fedx.evaluation.union.SynchronousWorkerUnion;
import com.fluidops.fedx.evaluation.union.WorkerUnionBase;
import com.fluidops.fedx.exception.FedXRuntimeException;
import com.fluidops.fedx.optimizer.Optimizer;
import com.fluidops.fedx.optimizer.StatementGroupOptimizer2;
import com.fluidops.fedx.optimizer.EvalVisitor;
import com.fluidops.fedx.sail.FedXSailRepositoryConnection;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.Endpoint.EndpointClassification;
import com.fluidops.fedx.structures.Endpoint.EndpointType;
import com.fluidops.fedx.trunk.parallel.engine.exec.TripleExecution;
import com.hp.hpl.jena.sparql.engine.QueryEngineBase;
//import org.eclipse.rdf4j.query.algebra.helpers;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.structures.QueryType;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.GetTriple;

/**
 * An implementation of RepositoryConnection that uses
 * {@link FederationEvalStrategy} to evaluate provided queries. Prior to
 * evaluation various optimizations are performed, see {@link Optimizer} for
 * further details.
 * 
 * Implementation notes: - the federation connection currently is read only -
 * not all methods are implemented as of now
 * 
 * @author Andreas Schwarte
 *
 */
public class FedXConnection extends AbstractSailConnection {

//	private	static final org.apache.log4j.Logger log = LogManager.getLogger(FedXConnection.class.getName());

	/**
	 * The Federation type definition: Local, Remote, Hybrid
	 * 
	 * @author Andreas Schwarte
	 */
	public static enum FederationType {
		LOCAL, REMOTE, HYBRID;
	}

	protected FedX federation;
	List<Endpoint> endpoints;
	EndpointManager endpointManager;
	Summary summary;
	List<String> StatementPatternArray = new ArrayList<String>();
	Triple TripleArray;
	ArrayList<String> EndpointArray = new ArrayList<>();
	ArrayList<String> endpoints2 = new ArrayList<>();
	List<Endpoint> ep;
	QueryManager queryManager;
	TupleQuery query1;
	BindingSet bindings;
	protected FederationType type;
	public FederationEvalStrategy strategy;

	public FedXConnection(FedX federation, List<Endpoint> endpoints, Summary summary) throws SailException {
		super(new SailBaseDefaultImpl());
		this.federation = federation;
		this.endpoints = endpoints;
		this.summary = summary;
//		log.info("111!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------FedXConnection"
//				+ "--" + this.getFederation() + "--" + this.getEndpoints() + "--" + this.getSummary() + "--"
//				+ this.federation + "--" + this.getFederation() + "--" + this.endpointManager);
//ForkJoinPool fjp = new ForkJoinPool();

//	fjp.submit(()->{
			updateStrategy();
	//});
 
//fjp.shutdown();
	//	log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%Before the end:" + endpoints.get(0));

		for (Endpoint epointname : endpoints) {
			EndpointArray.add(epointname.getEndpoint());
			// log.info("111536456456!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------FedXConnection"+"--"+endpoint.getName()+"--"+endpoint.getEndpoint());

		}
		for (Endpoint endpoint : endpoints) {

			try {
			//	 log.info("111536456456!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------FedXConnection"+"--"+endpoint.getName()+"--"+endpoint.getEndpoint()+"--"+EndpointArray.toString());
				 //log.info("111536456456!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------FedXConnectionHammad"+"--"+EndpointArray+"--"+EndpointArray.toString());

				endpoint.initialize(strategy);
			} catch (RepositoryException e) {
			//	log.error("Initialization of endpoint " + endpoint.getId() + " failed: " + e.getMessage());
				throw new SailException(e);
			}
		}
//		ForkJoinPool fjp1 = new ForkJoinPool(6);
//		try {
			endpointManager =  //fjp1.submit(()->(
					new EndpointManager(endpoints);//).get();
//		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//log.info("111!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------FedXConnection"
		//		+ "--" + EndpointArray + "--" + this.getFederation() + "--" + this.getEndpoints() + "--"
		//		+ this.getSummary() + "--" + this.federation + "--" + this.getFederation() + "--"
		//		+ this.endpointManager);

	}

	public FedX getFederation() {
		return federation;
	}

	public FederationEvalStrategy getStrategy() {
		return strategy;
	}

	public FederationType getFederationType() {
		return type;
	}

	public BindingSet getBindings() {
		return bindings;
	}

	public List<Endpoint> getEndpoints() {
		//log.info("This is the getEndpoints method in FedxConnetion:"+endpoints);
		return endpoints;
	}

	public EndpointManager getEndpointManager() {
		return endpointManager;
	}

	public Summary getSummary() {
		return summary;
	}

	public QueryManager getQueryManager() {
		return queryManager;
	}

	public void setQqueryManager(QueryManager queryManager) {
		this.queryManager = queryManager;
	}

	/**
	 * return the number of triples in the federation as string. Retrieving the size
	 * is only supported {@link EndpointType#NativeStore} and
	 * {@link EndpointType#RemoteRepository}.
	 * 
	 * If the federation contains other types of endpoints, the size is indicated as
	 * a lower bound, i.e. the string starts with a larger sign.
	 * 
	 * @return
	 */
	public String getFederationSize() {
/*		long size = 0;
		boolean isLowerBound = false;
		for (Endpoint e : endpoints)
			try {
				size += e.size();
			} catch (RepositoryException e1) {
				isLowerBound = true;
			}*/
		return null;//isLowerBound ? ">" + size : Long.toString(size);
	}

	/**
	 * Add the specified endpoint to the federation. The endpoint must be
	 * initialized and the federation must not contain a member with the same
	 * endpoint location.
	 * 
	 * @param e              the initialized endpoint
	 * @param updateStrategy optional parameter, to determine if strategy is to be
	 *                       updated, default=true
	 * 
	 * @throws FedXRuntimeException if the endpoint is not initialized, or if the
	 *                              federation has already a member with the same
	 *                              location
	 */
	public void addEndpoint(Endpoint e, boolean... updateStrategy) throws FedXRuntimeException {
	//	log.info("Adding endpoint " + e.getId() + " to federation ...");

		/* check if endpoint is initialized */
		if (!e.isInitialized()) {
			try {
				e.initialize(strategy);
			} catch (RepositoryException e1) {
				throw new FedXRuntimeException(
						"Provided endpoint was not initialized and could not be initialized: " + e1.getMessage(), e1);
			}
		}

		/* check for duplicate before adding: heuristic => same location */
	//ForkJoinPool fjp = new ForkJoinPool(6);
//	fjp.submit(()->{	
		for (Endpoint member : endpoints)
			if (member.getEndpoint().equals(e.getEndpoint()))
				throw new FedXRuntimeException("Adding failed: there exists already an endpoint with location "
						+ e.getEndpoint() + " (eid=" + member.getId() + ")");

		endpoints.add(e);
		endpointManager.addEndpoint(e);
	//	log.info("222!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-EndponitManager------------------------"
	//			+ e.getEndpoint());
	//});
	//fjp.shutdown();
	//	if (updateStrategy == null || updateStrategy.length == 0
	//			|| (updateStrategy.length == 1 && updateStrategy[0] == true)) {
	//		updateStrategy();
	//	}
	}

/*	public List<Endpoint> addEndpoint1(Endpoint e, boolean... updateStrategy) throws FedXRuntimeException {
		log.info("Adding endpoint " + e.getId() + " to federation ...");

			if (!e.isInitialized()) {
			try {
				e.initialize(strategy);
			} catch (RepositoryException e1) {
				throw new FedXRuntimeException(
						"Provided endpoint was not initialized and could not be initialized: " + e1.getMessage(), e1);
			}
		}

		for (Endpoint member : endpoints)
			if (member.getEndpoint().equals(e.getEndpoint()))
				throw new FedXRuntimeException("Adding failed: there exists already an endpoint with location "
						+ e.getEndpoint() + " (eid=" + member.getId() + ")");

		endpoints.add(e);
		endpointManager.addEndpoint(e);
		log.info("222!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-EndponitManager------------------------"
				+ e.getEndpoint());

		if (updateStrategy == null || updateStrategy.length == 0
				|| (updateStrategy.length == 1 && updateStrategy[0] == true)) {
			updateStrategy();

		}
		return endpoints;
	}
*/
	/**
	 * Add the specified endpoints to the federation and take care for updating all
	 * structures.
	 * 
	 * @param endpoints a list of initialized endpoints to add
	 */
	public void addAll(List<Endpoint> endpoints) {
	//	log.info("Adding " + endpoints.size() + " endpoints to the federation.");

		for (Endpoint e : endpoints) {
			addEndpoint(e, false);
		}
		//log.info("333!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------");

		//updateStrategy();
	}

	/**
	 * Remove the specified endpoint from the federation.
	 * 
	 * @param e              the endpoint
	 * @param updateStrategy optional parameter, to determine if strategy is to be
	 *                       updated, default=true
	 */
	public void removeEndpoint(Endpoint e, boolean... updateStrategy) throws RepositoryException {
		//log.info("Removing endpoint " + e.getId() + " from federation ...");

		/* check if e is a federation member */
		if (!endpoints.contains(e))
			throw new FedXRuntimeException("Endpoint " + e.getId() + " is not a member of the current federation.");

		endpoints.remove(e);
		endpointManager.removeEndpoint(e);
		e.shutDown();
//		log.info("444!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------");

//		if (updateStrategy == null || updateStrategy.length == 0
//				|| (updateStrategy.length == 1 && updateStrategy[0] == true)) {
//			updateStrategy();
//		}
	}

	/**
	 * Remove all endpoints from the federation, e.g. to load a new preset.
	 * Repositories of the endpoints are shutDown, and the EndpointManager is added
	 * accordingly.
	 * 
	 * @throws RepositoryException
	 */
	public void removeAll() throws RepositoryException {
		//log.info("Removing all endpoints from federation.");

		for (Endpoint e : new ArrayList<Endpoint>(endpoints)) {
			removeEndpoint(e, false);
		}
	//	log.info("555!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------");

		updateStrategy();
	}

	/**
	 * Update the federation evaluation strategy using the classification of
	 * endpoints as provided by {@link Endpoint#getEndpointClassification()}:
	 * <p>
	 * 
	 * Which strategy is applied depends on {@link EvaluationStrategyFactory}.
	 * 
	 * Default strategies:
	 * <ul>
	 * <li>local federation: {@link SailFederationEvalStrategy}</li>
	 * <li>endpoint federation: {@link SparqlFederationEvalStrategy}</li>
	 * <li>hybrid federation: {@link SparqlFederationEvalStrategy}</li>
	 * </ul>
	 * 
	 */
	protected void updateStrategy() {

		int localCount = 0, remoteCount = 0;
		for (Endpoint e : endpoints) {
			if (e.getEndpointClassification() == EndpointClassification.Remote)
				remoteCount++;
			else
				localCount++;
		}
	//	log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!UPDATESTRATEGY-------------------------");
		boolean updated = false;
		if (remoteCount == 0) {
			if (type != FederationType.LOCAL) {
				type = FederationType.LOCAL;
				updated = true;
			}
		} else if (localCount == 0) {
			if (type != FederationType.REMOTE) {
				type = FederationType.REMOTE;
				updated = true;
			}
		} else {
			if (type != FederationType.HYBRID) {
				type = FederationType.HYBRID;
				updated = true;
			}
		}

		if (updated) {
strategy = EvaluationStrategyFactory.getEvaluationStrategy(this, type);//.parallel();
//			log.info("Federation updated. Type: " + type + ", evaluation strategy is "
//					+ strategy.getClass().getSimpleName());
		}

	}

	@Override
	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(TupleExpr query,
			Dataset dataset, BindingSet bindings, boolean includeInferred) {
		FederationEvalStrategy strategy = getStrategy();

		long start = 0;
		if (true) {
		//	if (log.isDebugEnabled()) {
				System.out.println("Optimization start");
				start = System.currentTimeMillis();
		//	}
			try {
				String queryString = getOriginalQueryString(bindings);
	//			if (queryString == null) {
	//				logger.warn("Query string is null. Please check your FedX setup.");
	//			}
			//	ForkJoinPool fjp = new ForkJoinPool(6);
				
				QueryInfo queryInfo =new QueryInfo(this, queryString, getOriginalQueryType(bindings), summary);
			//	fjp.shutdown();
				//federation.getMonitoring().monitorQuery(queryInfo);
				query = Optimizer.optimize(query, dataset, bindings, strategy, queryInfo);
				// EvalVisitor.evaluate(query, dataset, bindings, strategy, queryInfo);
			//	if (Optimizer.query instanceof NJoin) {
				System.out.println("That that that that that that :"+Optimizer.query.getSignature()+"--"+Optimizer.members.size());
			//	}else
			//		System.out.println("S6 Thit this this this this this this:");
				
					//	if (StatementGroupOptimizer2.ss>0) {
				try {
					Thread.sleep(999999999);;
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					Thread.sleep(999999999);;
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				long i=0;
				while(i<=999999999)
				{	i++;
			//	log.info("This is inorder to stop threads:"+i);
				}
				i=0;

				i=0;
				while(i<=999999999)
				{	i++;
			//	log.info("This is inorder to stop threads:"+i);
				}
			
		//		log.info("This is not anything new:" + query);
				// log.info("blablablablablablablablablablablabla"+Optimizer.getSources());
				 //query1 = queryInfo.getQueryManager().prepareTupleQuery(query);
			//	 EvalVisitor.evaluate(query, dataset, bindings, strategy, queryInfo);

			} catch (Exception e) {
			System.out.printf("This is the Exception occured during optimization.", e);
				throw new SailException(e);
			}
		//	if (log.isDebugEnabled()) {
		//		log.debug(("Optimization duration: " + ((System.currentTimeMillis() - start))));
		//	}
		}

		// TripleArray=Optimizer.getTriple();
		// StatementPatternArray=Optimizer.getSources();

		// endpoints.clear();
		// endpoints.add(Optimizer.getEndpointss());
		// (Endpoint endpoint : Optimizer.getEndpointss())
		// ep.initialize(strategy);

		// log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>These are
		// endpoints in fedxConnetion before:"+ep.getEndpoint());

		// EndpointArray2=Optimizer.getEndpoint();
		// log the optimized query plan, if Config#isLogQueryPlan(), otherwise void
		// operation
		federation.getMonitoring().logQueryPlan(query);
		// GetTriple.getTriple(query);
		//log.info(
		//		"565656565656565655656565FedxConnection--------------------------------------: Late at evaluateInternal"
		//				+ query);
		
		
	//	if (federation.getConfig().isDebugQueryPlan()) {
	//		log.info("Optimized query execution plan: \n" + query);
	//		log.debug("Optimized query execution plan: \n" + query);
	//	}

		try {
		//	log.info(
		//			"565656565656565655656565FedxConnection--------------------------------------Going for strategy.evaluaate");

			return strategy.evaluate(query, EmptyBindingSet.getInstance());
		} catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	// public BindingSet getBinding() {
	// return bindings;
	// }

	@Override
	protected void clearInternal(Resource... contexts) throws SailException {
	//	log.warn("Operation is not yet supported. (clearInternal)");
		// throw new UnsupportedOperationException("Operation is not yet supported.");
	}

	@Override
	protected void clearNamespacesInternal() throws SailException {
	//	log.warn("Operation is not yet supported. (clearNamespacesInternal)");
		// throw new UnsupportedOperationException("Operation is not yet supported.");
	}

	@Override
	protected void closeInternal() throws SailException {
		if (summary != null) {
			summary.close();
		}
		// endpointManager.shutDown();
		/*
		 * think about it: the federation connection should remain open until the
		 * federation is shutdown. we use a singleton connection!!
		 */
	}

	@Override
	protected void commitInternal() throws SailException {
		throw new UnsupportedOperationException("Writing not supported to a federation: the federation is readonly.");
	}

	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal() {
		//log.info(
		//		"121212989898989898989898989898989898989 FedxConnection WorkerUnion--------------:::::::::::::::::::::.:");

		final WorkerUnionBase<Resource> union = new ControlledWorkerUnion<Resource>(federation.getScheduler(),
				new QueryInfo(this, "getContextIDsInternal", QueryType.UNKNOWN, summary));

		for (final Endpoint e : endpoints) {
			union.addTask(new Callable<CloseableIteration<Resource, QueryEvaluationException>>() {
				@Override
				public CloseableIteration<Resource, QueryEvaluationException> call() {
					return new RepositoryExceptionConvertingIteration<Resource>(e.getConn().getContextIDs());
				}
			});
		}

		return new DistinctIteration<Resource, SailException>(
				new ExceptionConvertingIteration<Resource, SailException>(union) {
					@Override
					protected SailException convert(Exception e) {
						return new SailException(e);
					}
				});
	}

	@Override
	protected String getNamespaceInternal(String prefix) throws SailException {
		//log.warn("Operation is not yet supported. (getNamespaceInternal)");
		return null;
		// throw new UnsupportedOperationException("Operation is not yet supported.");
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal() throws SailException {
	//	log.warn("Operation is not yet supported. (getNamespacesInternal)");
		return new AbstractCloseableIteration<Namespace, SailException>() {
			@Override
			public boolean hasNext() throws SailException {
				return false;
			}

			@Override
			public Namespace next() throws SailException {
				return null;
			}

			@Override
			public void remove() throws SailException {
			}
		};
		// throw new UnsupportedOperationException("Operation is not yet supported.");
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj, IRI pred,

			Value obj, boolean includeInferred, Resource... contexts) throws SailException {
		try {
			QueryInfo queryInfo = new QueryInfo(this, subj, pred, obj, summary);
			federation.getMonitoring().monitorQuery(queryInfo);
			CloseableIteration<Statement, QueryEvaluationException> res = getStrategy().getStatements(queryInfo, subj,
					pred, obj, contexts);
			return new ExceptionConvertingIteration<Statement, SailException>(res) {
				@Override
				protected SailException convert(Exception e) {
					return new SailException(e);
				}
			};
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new SailException(e);
		}
	}

	@Override
	protected void addStatementInternal(Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
		throw new UnsupportedOperationException("Not supported. the federation is readonly.");
	}

	@Override
	protected void removeNamespaceInternal(String prefix) throws SailException {
		throw new UnsupportedOperationException("Not supported. the federation is readonly.");
	}

	@Override
	protected void removeStatementsInternal(Resource subj, IRI pred, Value obj, Resource... contexts)
			throws SailException {
		throw new UnsupportedOperationException("Not supported. the federation is readonly.");
	}

	@Override
	protected void rollbackInternal() throws SailException {
		throw new UnsupportedOperationException("Not supported. the federation is readonly.");
	}

	@Override
	protected void setNamespaceInternal(String prefix, String name) throws SailException {
		throw new UnsupportedOperationException("Not supported. the federation is readonly.");
	}

	@Override
	protected long sizeInternal(Resource... contexts) throws SailException {
		if (contexts != null && contexts.length > 0)
			throw new UnsupportedOperationException("Context handling for size() not supported");
		long size = 0;
		List<String> errorEndpoints = new ArrayList<String>();
		for (Endpoint e : endpoints) {
			try {
				size += e.size();
			} catch (RepositoryException e1) {
				errorEndpoints.add(e.getId());
			}
		}
		if (errorEndpoints.size() > 0)
			throw new SailException("Could not determine size for members " + errorEndpoints.toString()
					+ "(Supported for NativeStore and RemoteRepository only). Computed size: " + size);
		return size;
	}

	@Override
	protected void startTransactionInternal() throws SailException {
		throw new UnsupportedOperationException("Not supported. the federation is readonly.");
	}

	private static String getOriginalQueryString(BindingSet b) {
		if (b == null)
			return null;
		Value q = b.getValue(FedXSailRepositoryConnection.BINDING_ORIGINAL_QUERY);
		if (q != null)
			return q.stringValue();
		return null;
	}

	private static QueryType getOriginalQueryType(BindingSet b) {
		if (b == null)
			return null;
		Value q = b.getValue(FedXSailRepositoryConnection.BINDING_ORIGINAL_QUERY_TYPE);
		if (q != null)
			return QueryType.valueOf(q.stringValue());
		return null;
	}

	/**
	 * Create an appropriate worker union for this federation, i.e. a synchronous
	 * worker union for local federations and a multithreaded worker union for
	 * remote & hybrid federations.
	 * 
	 * @return
	 * 
	 * @see ControlledWorkerUnion
	 * @see SynchronousWorkerUnion
	 */
	public WorkerUnionBase<BindingSet> createWorkerUnion(QueryInfo queryInfo) {
	//	log.info("989898989898989898989898989898989 FedxConnection WorkerUnion--------------:" + queryInfo);
		if (type == FederationType.LOCAL)
			return new SynchronousWorkerUnion<BindingSet>(queryInfo);
	//	log.info("222989898989898989898989898989898989 FedxConnection WorkerUnion--------------:" + queryInfo);

		return new ControlledWorkerUnion<BindingSet>(federation.getScheduler(), queryInfo);
	}

	/**
	 * A default implementation for SailBase. This implementation has no further
	 * use, however it is needed for the constructor call.
	 * 
	 * @author as
	 *
	 */
	protected static class SailBaseDefaultImpl extends AbstractSail {

		@Override
		protected SailConnection getConnectionInternal() throws SailException {
			return null;
		}

		@Override
		protected void shutDownInternal() throws SailException {
		}

		@Override
		public ValueFactory getValueFactory() {
			return null;
		}

		@Override
		public boolean isWritable() throws SailException {
			return false;
		}

		@Override
		protected void connectionClosed(SailConnection connection) {
			// we do not need this in FedX
		}
	}

	@Override
	public boolean pendingRemovals() {
		// TODO Auto-generated method stub
		return false;
	}
}
