package com.fluidops.fedx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fluidops.fedx.Summary;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.fluidops.fedx.algebra.ExclusiveGroup;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.optimizer.OptimizerUtil;
import com.fluidops.fedx.structures.QueryInfo;

public class CardinalityVisitor extends AbstractQueryModelVisitor<RuntimeException>
{
	static HashMap<Var,Var> Varse = new HashMap<>();
	int	IsVar1=0;

	LinkedHashMap<Integer,HashMap<Var,Var>> VarseTemp = new LinkedHashMap<>(); 
	int slstSize=0;
	int IsSPFirst=0;
	protected static int ESTIMATION_TYPE = 0;
	final QueryInfo queryInfo;
	static	List<StatementSource>  stmtSrces;
	public ConcurrentHashMap<StatementPattern, List<StatementSource>>  statmentToSources;
   public static LinkedHashMap<Integer, ArrayList<HashMap<Var, Var>>> VarJoinType = new LinkedHashMap<>();
	public static  int VarIndex=0;
	RepositoryConnection getSummaryConnection() {
	    return ((Summary)(queryInfo.getFedXConnection().getSummary())).getConnection();
	}
	   
	public CardinalityVisitor(QueryInfo queryInfo2,ConcurrentHashMap<StatementPattern, List<StatementSource>>  stmtToSources) {
		this.queryInfo = queryInfo2;
		this.statmentToSources = stmtToSources;
	}

	public static class NodeDescriptor {
		public long card = Long.MAX_VALUE;
		public double sel = 0;
		public double mvobjkoef = 1.0;
		public double mvsbjkoef = 1.0;
	}
	
	public static class CardPair {
		public TupleExpr expr;
		public NodeDescriptor nd;
		
		public CardPair(TupleExpr te, NodeDescriptor nd) {
			this.expr = te;
			this.nd = nd;
		}

		@Override
		public String toString() {
			return String.format("CardPair [expr=%s, card=%s, mvs=%s, mvo=%s", expr, nd.card, nd.mvsbjkoef, nd.mvobjkoef);
		}
	}
	
	public static NodeDescriptor getJoinCardinality(Collection<String> commonvars, CardPair left, CardPair right)
	{
		NodeDescriptor result = new NodeDescriptor();
		if (CardinalityVisitor.ESTIMATION_TYPE == 0) {
			if (commonvars != null && !commonvars.isEmpty()) {
				result.card = (long)Math.ceil((Math.min(left.nd.card, right.nd.card)));
				
				// multivalue fixes
				if (left.expr instanceof StatementPattern) {
					StatementPattern leftsp = (StatementPattern)left.expr;
					if (commonvars.contains(leftsp.getSubjectVar().getName())) {
						result.card *= left.nd.mvsbjkoef;
					}
					if (commonvars.contains(leftsp.getObjectVar().getName())) {
						result.card *= left.nd.mvobjkoef;
					}
				}
				if (right.expr instanceof StatementPattern) {
					StatementPattern rightsp = (StatementPattern)right.expr;
					if (commonvars.contains(rightsp.getSubjectVar().getName())) {
						result.card *= right.nd.mvsbjkoef;
					}
					if (commonvars.contains(rightsp.getObjectVar().getName())) {
						result.card *= right.nd.mvobjkoef;
					}
				}
			} else {
				result.card = (long)Math.ceil(left.nd.card * right.nd.card);
			}
		} else {
			result.sel = 1;
			if (commonvars != null && !commonvars.isEmpty()) {
				result.sel *= Math.min(left.nd.sel, right.nd.sel);
			}
			result.card = (long)Math.ceil(left.nd.card * right.nd.card * result.sel);
		}
		return result;
	}
	
	protected NodeDescriptor current = new NodeDescriptor();
	
	public void reset() {
		current = new NodeDescriptor();
	}
	
	public NodeDescriptor getDescriptor() {
		return current;
	}
	
	public void setDescriptor(NodeDescriptor d) {
		current = d;
	}
	
	public long getCardinality() {
		return current.card;
	}
	
	public static Collection<String> getCommonVars(Collection<String> vars, TupleExpr tupleExpr) {
		Collection<String> commonvars = null;
		Collection<String> exprVars = OptimizerUtil.getFreeVars(tupleExpr);
		for (String argvar : exprVars) {
			if (vars.contains(argvar)) {
				if (commonvars == null) {
					commonvars = new HashSet<String>();
				}
				commonvars.add(argvar);
			}
		}
		return commonvars;
	}
	
	@Override
	public void meet(StatementPattern stmt) {
		IsSPFirst++;
		stmtSrces = statmentToSources.get(stmt);
		int clear=0;
		if(VarIndex!=1)
		IsVar1=0;
		//for(StatementPattern e:statmentToSources.keySet())
	//	{	//VarJoinType.put(e.getSubjectVar(),e.getObjectVar());
		if(Varse!=null)
			for( Entry<Var, Var> f:Varse.entrySet()) {
			if(stmt.getSubjectVar() ==f.getKey() && stmt.getObjectVar()==f.getValue()) {
				//clear=1;
					//clear=1;
					return;
			}
			
		}
		/*if(clear==1) {
			clear=0;
			continue;
		}*/
		//System.out.println("These are the StatementPattern in Cardinality Visitor:"+VarIndex+"--"+stmt.getSubjectVar()+"--"+stmt.getObjectVar());
		int kl=0;
		for(Entry<Integer, ArrayList<HashMap<Var, Var>>> l:VarJoinType.entrySet())
		{for(HashMap<Var, Var> m:l.getValue()) {
			for( Entry<Var, Var> n:m.entrySet())
			{//System.out.println("These are the children StatementPattern in Cardinality Visitor:"+VarIndex+"--"+n.getKey()+"--"+n.getValue());
			if(n.getKey().equals(stmt.getSubjectVar()) && n.getValue().equals(stmt.getObjectVar()))
			kl++;}	
	}
		}if(kl<1)
		if(VarIndex==1)
		{
			HashMap<Var,Var> a = new HashMap<>();
			int j=VarseTemp.size()+1;
			a.put(stmt.getSubjectVar(),stmt.getObjectVar());
			VarseTemp.put(j,a);
			//VarseTemp.put(stmt.getSubjectVar(),stmt.getObjectVar());
			VarJoinType.put(VarIndex-1,new ArrayList<>(VarseTemp.values()));
//			VarJoinType.put(VarIndex-1,new ArrayList<>(Arrays.asList(VarseTemp.values())));
			VarIndex++;
IsVar1=1;
		}if(IsVar1==0)
		if((kl==0 ||VarJoinType.isEmpty())) {
		IsVar1=1;
			Varse=new LinkedHashMap<>();
		Varse.put(stmt.getSubjectVar(),stmt.getObjectVar());
		//System.out.println("This is the size of VarJoinType:"+VarJoinType.size()+"--"+VarJoinType.isEmpty());
		if(VarJoinType.size()==0&&IsSPFirst>0)
		{	HashMap<Var,Var> a = new HashMap<>();
		a.put(stmt.getSubjectVar(),stmt.getObjectVar());
		
			VarseTemp.put(VarIndex,a);		
		slstSize++;
		}
		VarJoinType.put(VarIndex,new ArrayList<>(Arrays.asList(Varse)));
		VarIndex++;

	}	
			//}
			current.card = Cardinality.getTriplePatternCardinality(queryInfo, stmt, stmtSrces);
		assert(current.card != 0);
		current.sel = current.card/(double)Cardinality.getTotalTripleCount(getSummaryConnection(), stmtSrces);
//		current.sel = current.card/(double)Cardinality.getTriplePatternCardinality(queryInfo, stmt, stmtSrces);
		
		current.mvsbjkoef = Cardinality.getTriplePatternSubjectMVKoef(queryInfo, stmt, stmtSrces);
		current.mvobjkoef = Cardinality.getTriplePatternObjectMVKoef(queryInfo, stmt, stmtSrces);
	}
	
	@Override
	public void meet(Filter filter)  {
		
		filter.getArg().visit(this);
		
	}
	
	public void meet(ExclusiveGroup eg)  {
		int j=0;
		LinkedHashMap<Integer,LinkedHashMap<Var, Var>> VarsTemp2 = new LinkedHashMap<>();
		
		List<ExclusiveStatement> slst = new LinkedList<ExclusiveStatement>(eg.getStatements()); // make copy
		List<StatementSource> stmtSrces = slst.get(0).getStatementSources();
		slstSize+=slst.size();
		int clear=0;
		assert (stmtSrces.size() == 1);
		for(StatementPattern e:slst)
		{	//VarJoinType.put(e.getSubjectVar(),e.getObjectVar());
			//System.out.println("These are the ExclusiveGroup in Cardinality Visitor:"+slst+"--"+Varse);
			
			if(Varse!=null)
			for( Entry<Var, Var> f:Varse.entrySet()) {
		if(e.getSubjectVar() ==f.getKey() && e.getObjectVar()==f.getValue()) {
				//clear=1;
					clear=1;
					continue;
			}
		}
		
		if(clear==1) {
			clear=0;
			continue;
		}
		//System.out.println("These are the ExclusiveGroup children in Cardinality Visitor:"+VarIndex+"--"+e.getSubjectVar()+"--"+e.getObjectVar());
		slst.size();
		if(VarIndex==1)
		{
			//if(j==0)	
			//	VarseTemp.put(j,Varse);
//		if(VarseTemp.isEmpty()) {
//			VarseTemp.put(j,Varse);
			
//			j=Varse.size()+1;
//		}
//
			
		j=VarseTemp.size()+1;
		HashMap<Var,Var> a = new HashMap<>();
		a.put(e.getSubjectVar(),e.getObjectVar());
		
			VarseTemp.put(j,a);
	//VarseTemp.put(j,VarsTemp2.values().toArray()[VarsTemp2.s]);
int jkl=0;
		//	if(IsSPFirst==0)
		//jkl=slstSize+1;
		//	if(IsSPFirst>0)
		//jkl=slstSize+2;
			if(j==(slstSize))
	//for(Entry<Integer, Map<Var, Var>> g:VarseTemp.entrySet())
	{VarJoinType.put(VarIndex-1,new ArrayList<>(VarseTemp.values()));
	VarIndex++;
	}}	else {
/*		Varse=new LinkedHashMap<>();
		
		Varse.put(e.getSubjectVar(),e.getObjectVar());
		VarJoinType.put(VarIndex ,new ArrayList<>(Arrays.asList(Varse)));
*/
			//if(j==0)	
			//	VarseTemp.put(j,Varse);
		j++;
		HashMap<Var,Var> a = new HashMap<>();
		a.put(e.getSubjectVar(),e.getObjectVar());
		
		VarseTemp.put(j,a);
						
	//VarseTemp.put(j,VarsTemp2.values().toArray()[VarsTemp2.s]);
	if(j==(slst.size())) 
	{
		{for(Entry<Integer, HashMap<Var, Var>> g:VarseTemp.entrySet())
	VarJoinType.put(VarIndex,new ArrayList<>(VarseTemp.values()));
	}
	VarIndex++;
	
	}
	}
		
		}
		j=0;
		
		//	VarseTemp=null;
	
		
		
		
		
	//	for(ExclusiveStatement e:slst)
	//	VarJoinType.put(e.getSubjectVar(),e.getObjectVar());

		//long total = Cardinality.getTotalTripleCount(stmtSrces);
		
		ExclusiveStatement leftArg = slst.get(0);
		slst.remove(0);
		
		Set<String> joinVars = new HashSet<String>();
		joinVars.addAll(OptimizerUtil.getFreeVars(leftArg));
		
		//long leftCard = Cardinality.getTriplePatternCardinality(leftArg, stmtSrces);
		leftArg.visit(this);
		CardPair left  = new CardPair(leftArg, getDescriptor());
		reset();
		//double leftSelectivity = leftCard/(double)total;
		
		// find possible join order
		while (!slst.isEmpty()) {
			ExclusiveStatement rightArg = null;
			Collection<String> commonvars = null;
			for (int i = 0, n = slst.size(); i < n; ++i) {
				ExclusiveStatement arg = slst.get(i);
				commonvars = getCommonVars(joinVars, arg);
				if (commonvars == null || commonvars.isEmpty()) continue;
				rightArg = arg;
				slst.remove(i);
				break;
			}
			if (rightArg == null) {
				rightArg = slst.get(0);
				slst.remove(0);
			}
			rightArg.visit(this);
			CardPair right = new CardPair(rightArg, getDescriptor());
			reset();
			//long rightCard = Cardinality.getTriplePatternCardinality(rightArg, stmtSrces);
			
			joinVars.addAll(OptimizerUtil.getFreeVars(rightArg));
			
			//double sel = 1;
			
			left.nd = getJoinCardinality(commonvars, left, right);

			//if (ESTIMATION_TYPE == 0) {
				//if (commonvars != null && !commonvars.isEmpty()) {
				//	leftCard = (long)Math.ceil((Math.min(leftCard, rightCard))/2);
				//} else {
				//	leftCard = (long)Math.ceil(leftCard * rightCard);
				//}
			//} else {
			//	double rightSelectivity = rightCard/(double)total;
			//	if (commonvars != null && !commonvars.isEmpty()) {
			//		sel *= Math.min(leftSelectivity, rightSelectivity);
			//	}
			//	leftCard = (long)Math.ceil(leftCard * rightCard * sel);
			//	leftSelectivity = sel;
			//}
		}
		current = left.nd;
		//current.sel = leftSelectivity;
	}
}
