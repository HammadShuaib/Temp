package com.fluidops.fedx.trunk.parallel.engine.exec.operator;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
import org.apache.solr.client.solrj.io.stream.ParallelStream;
import org.eclipse.rdf4j.query.BindingSet;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;

import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.graph.Edge;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.error.RelativeError;
import com.fluidops.fedx.trunk.parallel.engine.error.TripleCard;
import com.fluidops.fedx.trunk.parallel.engine.exec.QueryTask;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.fluidops.fedx.trunk.stream.engine.util.QueryUtil;

public class HashJoin extends EdgeOperator {

	//private	static final com.hp.hpl.log4j.Logger log = LogManager.getLogger(BindJoin.class.getName());

	public HashJoin(Edge e) {
		super(null, e);
	}
 static int total=0;
static int abnormal=0;
 static int remaning=0;
	static Var joinVars = null;
	
	int skp=0;
String finalization="Y";
static List<EdgeOperator> AllEdges = new ArrayList<>();
	List<BindingSet> BindJoinWhenEnd;
	EdgeOperator CurrentEdgeOperator;
static ArrayList<Binding>	sBindingSet;
public static String isComplete="N";
static HashSet<List<EdgeOperator>> ProcessedEdgeOperators=new HashSet<>();

 static LinkedHashMap<List<EdgeOperator>,List<Binding>> ProcessedTriples = new LinkedHashMap<>();
 static LinkedHashMap<List<EdgeOperator>,List<Binding>> ProcessedTriplesUnion = new LinkedHashMap<>();
 public static HashSet<List<EdgeOperator>> EvaluatedTriples=new HashSet<>();
static	HashSet<List<EdgeOperator>> Removal= new HashSet<>();
	
public static LinkedHashMap<List<EdgeOperator>,List<Binding>> JoinedTriples = new LinkedHashMap<>();
public static LinkedHashMap<List<EdgeOperator>,List<Binding>> NotJoinedTriples = new LinkedHashMap<>();
public static LinkedHashMap<List<EdgeOperator>,List<Binding>> NotJoinedTriplesUnion = new LinkedHashMap<>();
static List<Binding> results = new ArrayList<>();

public static LinkedHashMap<List<EdgeOperator>,List<Binding>> JoinedTriplesUnion = new LinkedHashMap<>();
@Override
	public  void exec() {
	results = new ArrayList<>();
	//	com.hp.hpl.log4j.Logger.getRootLogger().setLevel(Level.OFF);
//		Logger.getRootLogger().setLevel(Level.OFF);
//com.hp.hpl.log4j.Logger.getLogger("com.fluidops.fedx.trunk.parallel.engine.exec.operator.HashJoin").setLevel(Level.OFF);
//////System.out.printlnln("44444444444444444444444444444444Subsubsubsubsubs:");

	//	synchronized(BindJoin.StartBindingSet123) {
		
		// it's possible that both vertices are not visited, but
		// it should not occur. To simplify the code we assume
		// at least one vertex is bound.
		//log.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in HashJoin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
		//		+ edge.getV1() + "--" + edge.getV2());
		
;
 
//	results = null;

		Vertex end;
		
//ForkJoinPool fjp = new ForkJoinPool();
	// fjp.submit(()->
	//{Stream.of(
		System.out.println("This is out of TripleExectuion in HashJoin2:");
			results =te.exec(null,null);//).parallel();}).join();
			for(List<EdgeOperator> e:BGPEval.JoinGroupsListExclusive) {
				for(EdgeOperator e1:e)
					if(e1.getEdge().equals(edge)) {
						AllEdges.addAll(e);
					}
			}

			for(EdgeOperator e:BGPEval.JoinGroupsListLeft) {
			
					if(e.getEdge().equals(edge)) {
						AllEdges.add(e);
					}
			}
			
					for(EdgeOperator e1:BGPEval.JoinGroupsListRight) {
						
						if(e1.getEdge().equals(edge)) {
							AllEdges.add(e1);
						}
					}

					//System.out.println("this is after hashjoin:"+te.getTriple()+"--"+results.size());
			if (edge.getV1().isBound() && edge.getV2().isBound()) {
				//System.out.println("This is out of TripleExectuion in HashJoin2:");

				start = edge.getV1();
				end = edge.getV2();
				results = QueryUtil.join(results, start.getBindings());
				results = QueryUtil.join(results, end.getBindings());
				//results = QueryUtil.join(results, input);
				// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!This is here in
				// HashJoin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
				// "+start.getBindings()+"--"+end.getBindings());

				if (Config.debug) {
					
					//System.out.println(edge + ": " + results.size());

				}
				if (Config.relative_error) {
					//System.out.println("This is out of TripleExectuion in HashJoin4:");

					RelativeError.real_resultSize.put(end, (double) results.size());
					RelativeError.real_resultSize.put(start, (double) results.size());
					// RelativeError.addJoinCard(end,(double)results.size(),edge.getTriple(),tripleCard);
					// RelativeError.addJoinCard(start,(double)results.size(),edge.getTriple(),tripleCard);

				}
				if (results.size() == 0) {
					//System.out.println("This is out of TripleExectuion in HashJoin5:");

					finished = true;
					// //System.out.println("SE shutdown");
					BGPEval.getCurrentExeService().shutdownNow();
				}
				if (start.getNode().isConcrete()) {
					//System.out.println("This is out of TripleExectuion in HashJoin6:");

					end.setBindings(results);
					synchronized (end) {
						end.notifyAll();
					}
				} else {
					//System.out.println("This is out of TripleExectuion in HashJoin7:");

					start.setBindings(results);
					synchronized (start) {
						start.notifyAll();
					}
				}
				start.removeEdge(edge);
				end.removeEdge(edge);
				return;
			}
			
			String fq;
			if(edge.getV1().toString().contains("http:"))
			fq=edge.getV1().getNode().getURI().toString();
			else
				fq=edge.getV1().getNode().getName().toString();
			
				if(results.toString().contains(fq))
				{	//System.out.println("Theser are the vars of result in hashJoin edge.getV1:"+edge.getV1().getNode().getName().toString());
				start = edge.getV1();
				end = edge.getV2();
		
				}
				else
				{	//System.out.println("Theser are the vars of result in hashJoin edge.getV2:"+edge.getV1());
				start = edge.getV2();
				end = edge.getV1();
		
				}
				
	//	System.out.println("This is the edge:"+edge);		
		if(results.size()==0)
			return;
		else {
	
		//	synchronized(BindJoin.StartBinding123) {
			//	ForkJoinPool fjp = new ForkJoinPool();
			//for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, ArrayList<Binding>> e:BGPEval.StartBindingSetBJ.entrySet())
								//BGPEval.HashJoinCompletion++;

		//BindJoin.StartBinding123.notifyAll();
			//		}
			
			int xz=0;
	//	for(Entry<Triple, Integer> ab:QueryTask.CompletionValue.entrySet())
		//	if()
			
		//	while(QueryTask.CompletionValue.containsKey(te.getTriple())&& (QueryTask.CompletionValue.values().contains(0)))
		//	{	
		//		if(results.size()!=0)
		//		{	
		//			break;
		//		}
				//else
		//	}
		
		//	else break;		
		//	QueryTask.CompletionValue
	//	if(	BGPEval.ExclusivelyExclusive==1)
			/*if(QueryTask.CompletionValue.containsKey(te.getTriple())&& (QueryTask.CompletionValue.values().contains(0)))
			{	if(results.size()!=0)
				{	
			//		break;
				}
				//else
				//	////System.out.printlnln("");
			}*/
				//	log.debug("111111111111111This is out of TripleExectuion");
		//	log.debug("");
		//	log.debug("1232131231231231232112321312312312321312323213123123213");
		ForkJoinPool fjp = new ForkJoinPool();
			try {
				fjp.submit(()->IntermediateProcedure(HashJoin.results,HashJoin.AllEdges)).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fjp.shutdown();
		
		


		//log.info("This is out of TripleExectuion in HashJoin Initil:"+results.size());
			
//synchronized(BGPEval.finalResult) {
	Edge	CurrentEdgeOperator=  new Edge(edge.getV1(),edge.getV2());//edge.getV1()+"--"+edge.getV2();
		
	 Iterator<Entry<EdgeOperator, List<Binding>>> frIterator = BGPEval.finalResult.entrySet().iterator();
		while(frIterator.hasNext()) {
			total++;
			Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
		
			if((ab.getKey().getEdge().getV1()+"--"+ab.getKey().getEdge().getV2()).toString().equals((edge.getV1()+"--"+edge.getV2()).toString()))
			{
	//			System.out.println("This is finalResult:"+ab.getKey()+"--"+edge+"--"+results.size());
				BGPEval.finalResult.replace(ab.getKey(), results); 
	//			CurrentEdgeOperator=ab.getKey();
			}
				//	}
	 
	 //for(Entry<EdgeOperator, ArrayList<Binding>> ab:BGPEval.finalResult.entrySet())
	
}
//synchronized(BGPEval.finalResultOptional) {
		for(Entry<EdgeOperator, List<Binding>> ab:BGPEval.finalResultRightOptional.entrySet())
		{	total++;	
			if((ab.getKey().getEdge().getV1()+"--"+ab.getKey().getEdge().getV2()).toString().equals((edge.getV1()+"--"+edge.getV2()).toString()))
			{	BGPEval.finalResultRightOptional.replace(ab.getKey(), results);
			}
		
			//	CurrentEdgeOperator=ab.getKey();
			
			}
		for(Entry<EdgeOperator, List<Binding>> ab:BGPEval.finalResultLeftOptional.entrySet())
		{total++;
			if((ab.getKey().getEdge().getV1()+"--"+ab.getKey().getEdge().getV2()).toString().equals((edge.getV1()+"--"+edge.getV2()).toString()))
			{	BGPEval.finalResultLeftOptional.replace(ab.getKey(), results);
		//	CurrentEdgeOperator=ab.getKey();
			
			}
		}
for(Entry<EdgeOperator, List<Binding>> ab:BGPEval.finalResultOptional.entrySet())
{	total++;
	if((ab.getKey().getEdge().getV1()+"--"+ab.getKey().getEdge().getV2()).toString().equals((edge.getV1()+"--"+edge.getV2()).toString()))
			{	BGPEval.finalResultOptional.replace(ab.getKey(), results);
		//	CurrentEdgeOperator=ab.getKey();
			
			}
}
//}}

//synchronized(BGPEval.finalResultRight) {

for(Entry<EdgeOperator, List<Binding>> ab:BGPEval.finalResultRight.entrySet())
{total++;

if((ab.getKey().getEdge().getV1()+"--"+ab.getKey().getEdge().getV2()).toString().equals((edge.getV1()+"--"+edge.getV2()).toString()))
				{
//	System.out.println("This is finalResultRight:"+ab.getKey()+"--"+edge+"--"+results.size());
//	for(Entry<EdgeOperator, List<Binding>> fr:BGPEval.finalResult.entrySet())
//		System.out.println("This is finalResult:"+fr.getKey()+"--"+edge+"--"+fr.getValue().size());

	BGPEval.finalResultRight.replace(ab.getKey(), results);
		//		CurrentEdgeOperator=ab.getKey();
				
				}
}
for(Entry<EdgeOperator, List<Binding>> ab:BGPEval.finalResultLeft.entrySet())
{
	total++;

	if((ab.getKey().getEdge().getV1()+"--"+ab.getKey().getEdge().getV2()).toString().equals((edge.getV1()+"--"+edge.getV2()).toString()))
		{BGPEval.finalResultLeft.replace(ab.getKey(), results);
//		CurrentEdgeOperator=ab.getKey();
		
		}
}
//}	

//synchronized(BGPEval.finalResultLeft) {
//for(Entry<EdgeOperator, List<Binding>> fr:BGPEval.finalResult.entrySet())
//System.out.println("This is fr:"+fr.getKey()+"--"+fr.getValue().size());

//if(BGPEval.finalResultRight.size()>0 ||BGPEval.finalResultRight!=null)
//for(Entry<EdgeOperator, List<Binding>> frr:BGPEval.finalResultRight.entrySet())
//System.out.println("This is frr:"+frr.getKey()+"--"+frr.getValue().size());

for(HashSet<List<EdgeOperator>>  e1:BGPEval.linkingTreeDup.keySet()) {
				for(List<EdgeOperator> e2:e1)
				for(EdgeOperator e3:e2)
					if(e3.getEdge().equals(CurrentEdgeOperator))
				{
					ProcessedEdgeOperators.addAll(e1);
				}
			}
			////System.out.printlnln("This is now the new tree right:"+ProcessedEdgeOperators+"--"+CurrentEdgeOperator+"--"+total);
			int count=0;
			frIterator = BGPEval.finalResult.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{		//////System.out.printlnln("THis is coming to final algo:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2:"+ee);
							ProcessedTriples.put(CompleteEdgeOperator(ab.getKey()),ab.getValue());
							
					count++;
					}
								}
					}
			////System.out.printlnln("This is now the new tree right count:"+count);
			count=0;
			frIterator = BGPEval.finalResultLeft.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{		//////System.out.printlnln("THis is coming to final algo Rights:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2 Right:"+ee);
				//count++;
							List<EdgeOperator> l = new ArrayList<>();
				l.add(ab.getKey());
							ProcessedTriples.put(l,ab.getValue());
				
					}
								}
					}
			//////System.out.printlnln("This is now the new tree right count:"+count);
			frIterator = BGPEval.finalResultRight.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{		//////System.out.printlnln("THis is coming to final algo Rights:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2 Right:"+ee);
				//count++;
							List<EdgeOperator> l = new ArrayList<>();
				l.add(ab.getKey());
							ProcessedTriples.put(l,ab.getValue());
				
					}
								}
					}
			if(ParaEng.Optional.contains("OPTIONAL") ) 
			{
			frIterator = BGPEval.finalResultOptional.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{		//////System.out.printlnln("THis is coming to final algo:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2:"+ee);
							ProcessedTriples.put(CompleteEdgeOperator(ab.getKey()),ab.getValue());
							
					count++;
					}
					}
					}
					}
			////System.out.printlnln("This is now the new tree right count:"+count);
			if(ParaEng.Optional.contains("OPTIONAL")) 
			{
			frIterator = BGPEval.finalResultLeftOptional.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{	//	////System.out.printlnln("THis is coming to final algo Left:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2 Left:"+ee);
							List<EdgeOperator> l = new ArrayList<>();
							l.add(ab.getKey());
										ProcessedTriples.put(l,ab.getValue());
							//count++;
					
					}
								}
					}
		}
			//////System.out.printlnln("This is now the new tree right count:"+count);
		
			if(ParaEng.Optional.contains("OPTIONAL") ) 
			{		
			frIterator = BGPEval.finalResultRightOptional.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{		//////System.out.printlnln("THis is coming to final algo Rights:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2 Right:"+ee);
				//count++;
							List<EdgeOperator> l = new ArrayList<>();
				l.add(ab.getKey());
							ProcessedTriples.put(l,ab.getValue());
					}	
					}
								}
					}
			int IsUnion=0;
			
		
			if(ParaEng.Union.contains("UNION") ) 
			{
			
			frIterator = BGPEval.finalResultOptional.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				//System.out.println("This is finalReusltOptional:"+ab);
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
				{//	System.out.println("This is finalReusltOptional1111:"+ee);
				
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{		//////System.out.printlnln("THis is coming to final algo:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2:"+ee);
							ProcessedTriplesUnion.put(CompleteEdgeOperator(ab.getKey()),ab.getValue());
							IsUnion=1;
					count++;
					}
					}
					}
					}
					}
			////System.out.printlnln("This is now the new tree right count:"+count);
			if( ParaEng.Union.contains("UNION")) 
			{
			frIterator = BGPEval.finalResultLeftOptional.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{	//	////System.out.printlnln("THis is coming to final algo Left:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2 Left:"+ee);
							List<EdgeOperator> l = new ArrayList<>();
							l.add(ab.getKey());
										ProcessedTriplesUnion.put(l,ab.getValue());
							//count++;
					IsUnion=1;
					}
								}
					}
		}
			//////System.out.printlnln("This is now the new tree right count:"+count);
		
			if(ParaEng.Union.contains("UNION") ) 
			{		
			frIterator = BGPEval.finalResultRightOptional.entrySet().iterator();
			while(frIterator.hasNext()) {
				Entry<EdgeOperator, List<Binding>> ab = frIterator.next();
				for(List<EdgeOperator> ee:ProcessedEdgeOperators)
					for(EdgeOperator ee1:ee) {
						if(ab.getValue()!=null)
						if(ee1.equals(ab.getKey()) )
					{		//////System.out.printlnln("THis is coming to final algo Rights:"+ab.getKey()+"--"+ab.getValue().size());
				//////System.out.printlnln("THis is coming to final algo2 Right:"+ee);
				//count++;
							List<EdgeOperator> l = new ArrayList<>();
				l.add(ab.getKey());
					
				ProcessedTriplesUnion.put(l,ab.getValue());
				IsUnion=1;
					}	
					}
								}
					}

			//////System.out.printlnln("This is now the new tree right count:"+count);
			//count=0;

		for(List<EdgeOperator> et:EvaluatedTriples)
		{ProcessedTriples.remove(et);
		}
		
				ProcessingTask(JoinedTriples,ProcessedTriples,0);
			ProcessingTask(NotJoinedTriples,ProcessedTriples,0);

	//	}else
	//	{
		//  if(IsUnion==1)		{
				
		//  }
		
		}
		
		
		
		for(List<EdgeOperator> et:EvaluatedTriples)
		{
			NotJoinedTriples.remove(et);
		}


for(int i=0;i<9;i++){	
	ForkJoinPool fjp1 = new ForkJoinPool();
	fjp1.submit(()->{
		BGPEval.finalResultCalculation(NotJoinedTriples,EvaluatedTriples,JoinedTriples);}).join();
	fjp1.shutdown();
	ForkJoinPool fjp2 = new ForkJoinPool();
	
fjp2.submit(()->{BGPEval.finalResultCalculation(JoinedTriples,EvaluatedTriples,JoinedTriples);}).join();
	fjp2.shutdown();

	ForkJoinPool fjp21 = new ForkJoinPool();
	
fjp21.submit(()->{BGPEval.finalResultCalculation(NotJoinedTriples,EvaluatedTriples,NotJoinedTriples);}).join();
	fjp21.shutdown();
}


		 
ProcessUnion();
//	}
	//	for(Entry<EdgeOperator, ArrayList<Binding>> fr: BGPEval.finalResult.entrySet())
	//		if(fr.getValue().size()>0)
//		log.info("This is out of TripleExectuion in HashJoin:"+results.size());
		TripleCard tripleCard = new TripleCard();
	/*	if (Config.relative_error) {
			log.info("This is out of TripleExectuion in HashJoin1:");
			
			tripleCard.real_Card = results.size();
			tripleCard.estimated_card = edge.estimatedCard();
		}*/

		// if both vertices are bound
		if (edge.getV1().isBound() && edge.getV2().isBound()) {
	//		log.info("This is out of TripleExectuion in HashJoin2:");

			start = edge.getV1();
			end = edge.getV2();
			//results = QueryUtil.join(results, start.getBindingSets(),results, start.getBindingSets());
			//results = QueryUtil.join(results, end.getBindingSets(),results, end.getBindingSets());
			//results = QueryUtil.join(results, input,results, input);
			// log.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
			// HashJoin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
			// "+start.getBindingSets()+"--"+end.getBindingSets());

		/*	if (Config.debug) {
				
				log.info(edge + ": " + results.size());

			}
			if (Config.relative_error) {
				log.info("This is out of TripleExectuion in HashJoin4:");

				RelativeError.real_resultSize.put(end, (double) results.size());
				RelativeError.real_resultSize.put(start, (double) results.size());
				// RelativeError.addJoinCard(end,(double)results.size(),edge.getTriple(),tripleCard);
				// RelativeError.addJoinCard(start,(double)results.size(),edge.getTriple(),tripleCard);

			}*/
			if (results.size() == 0) {
		//		log.info("This is out of TripleExectuion in HashJoin5:");

				finished = true;
				// log.info("SE shutdown");
			//	BGPEval.getCurrentExeService().shutdownNow();
			}
			if (start.getNode().isConcrete()) {
			//	log.info("This is out of TripleExectuion in HashJoin6:");

				end.setBindings(results);
				synchronized (end) {
					end.notifyAll();
				}
			} else {
				//log.info("This is out of TripleExectuion in HashJoin7:");

				start.setBindings(results);
	//			synchronized (start) {
	//				start.notifyAll();
	//			}
			}
			start.removeEdge(edge);
			end.removeEdge(edge);
			return;
		}

	
			if(results.toString().contains(edge.getV1().getNode().toString()))
			{//	log.info("Theser are the vars of result in hashJoin edge.getV1:"+edge.getV1());
			start = edge.getV1();
			end = edge.getV2();
	
			}
			else
			{	//log.info("Theser are the vars of result in hashJoin edge.getV2:"+edge.getV1());
			start = edge.getV2();
			end = edge.getV1();
	
			}
		// only one vertex is bound
	/*	if (edge.getV1().isBound()) {
			log.info("This is out of TripleExectuion in HashJoin8:");

			start = edge.getV1();
			end = edge.getV2();
		} else {
			log.info("This is out of TripleExectuion in HashJoin9:");

			start = edge.getV1();
			end = edge.getV2();
		}*/
/*			synchronized(BindJoin.StartBinding123) {
//	for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, ArrayList<Binding>> e:BGPEval.StartBindingSetBJ.entrySet())
//		log.info("This is end 23 set BindingSet in HashJoin:"+e.getKey()+"--"+edge+"--"+edge);
	Vertex start1 = new Vertex();
	start1=start;
//	////System.out.printlnln("This is here now before error"+results.size()+"--"+start.getBindingSets()+"--"+results1.size()+"--"+start1.getBindingSets());
//		results = QueryUtil.join(results, start.getBindingSets(),results1, start1.getBindingSets());
		for(Entry<ConcurrentHashMap<Set<Vertex>, Set<Edge>>, ArrayList<Binding>> e:BGPEval.StartBindingSetBJ.entrySet())
		{	for(Entry<Set<Vertex>, Set<Edge>> f:e.getKey().entrySet())
			{
//			log.info("This is en  d 24 set BindingSet in HashJoin11111:"+BGPEval.StartBindingSetBJ);
			for(Vertex fCondition:f.getKey()) {
				if((fCondition.toString().equals(start.toString()))) {
//					if((fCondition.toString().equals(end)) ||(fCondition.toString().equals(edge.getV2().toString()))) {

					for(Edge f1:f.getValue())
				if(f1.toString().equals(edge.toString()))
			{
	//				log.info("This is end 24 set BindingSet in HashJoin1.1111.1.1111:"+f1+"--"+f );
					
			ArrayList<Binding> x = new ArrayList<Binding>();		
				x.addAll(results);
				ConcurrentHashMap<Set<Vertex>,Set<Edge>> b = new ConcurrentHashMap<>();
			b.put(f.getKey(),f.getValue());
		//	BGPEval.StartBindingSet.put(b,new ArrayList<Binding>(results));
				
			BindJoin.StartBinding123.put(b,x);
		//		log.info("This is end 24 set BindingSet in HashJoin0000:"+results.size());
skp=1;
				break;
		//				BGPEval.StartBindingSet.remove(e);
			//	BGPEval.StartBindingSet.put(e.getKey(),a);
			}
				if(skp==1)
					break;
				}
				if(skp==1)
				break;
			}
			if(skp==1)
				break;
			}
		if(skp==1) {
			skp=0;
			break;
		}
		}
 
		
	*/		
		//////System.out.printlnln("This is now the new tree:"+BGPEval.finalResult);
		//////System.out.printlnln("This is now the new tree left:"+BGPEval.finalResultLeft);
		//////System.out.printlnln("This is now the new tree right:"+BGPEval.finalResultRight);

				
	//		finalResultCalculation()
			
		//	log.info("This is end 23 set BindingSet in HashJoin:"+results );

//		BGPEval.StartBindingSet.put(BGPEval.b,BGPEval.a);
	//	log.info("This is end 24 set BindingSet in HashJoin0000:"+BGPEval.StartBindingSet);
		//log.info("This is end 25 set BindingSet in HashJoin:"+results );
		// log.info("!!!!!!!!!!!!!!!!!!!!!!!This is here in
		// HashJoin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: "+start.getBindingSets());

		if(ParaEng.Union.contains("UNION")==false) {
		//	ArrayList<Binding> input1 = new ArrayList<Binding>();
		//	if(input!=null)
		//		input1.addAll(input);
			//Vertex start1 = new Vertex();
			//start1=start;
//////System.out.printlnln("This is great solution:");
//	ArrayList<Binding> results1 = new ArrayList<Binding>();
//if(results !=null)
//	for(BindingSet r:results1)
//results1.add(r);
		//		if(results!=null || input!=null)
			//results = QueryUtil.join(results, input);
		}
		
		

		if (Config.relative_error) {
//            RelativeError.writeQueryPlan("\n"+"Hash Join: "+ edge +":"+results.size());
		}
		if (Config.relative_error) {
			RelativeError.real_resultSize.put(end, (double) results.size());
			RelativeError.real_resultSize.put(start, (double) results.size());
			// RelativeError.addJoinCard(start,(double)results.size(),edge.getTriple(),tripleCard);

		}
		if (results.size() == 0) {
			finished = true;
			// log.info("SE shutdown");
		//	BGPEval.getCurrentExeService().shutdownNow();
		}
		
		//log.info("This is end 24 set BindingSet in HashJoin0000000000111:"+ BGPEval.StartBindingSet);
	//	if(ParaEng.Union.contains("UNION")==false) 
	//	end.setBindings(results);
	//	sBindingSet = start.getBindingSets();
		//for(BindingSet r:results)
	//	log.info("This is end 25 set BindingSet in HashJoin:"+results.size());
		//	for(BindingSet r:results)
		synchronized (end) {
			end.notifyAll();
		}		
			//	log.info("End statement in HashJoin notified1:"+start.getBindingSets());
		//	log.info("End statement in HashJoin notified2:"+results);
		//	log.info("End statement in HashJoin notified3:"+end.getBindingSets());
			
		//	{	BindJoinWhenEnd=new ArrayList<BindingSet>(end.getBindingSets());
//
//			te.exec(BindJoinWhenEnd);
//			}
		//}
	//	log.info("This is end 26 set BindingSet in HashJoin:"+start.getBindingSets() );
	//	log.info("This is end 27 set BindingSet in HashJoin:"+end.getBindingSets() );
//	setInput(null);
	//	if(start.getBindingSets()==null)
	//	if(end.getBindingSets() != null) {
	//	BindJoinWhenEnd=new ArrayList<BindingSet>(end.getBindingSets());
	//	te.exec(BindJoinWhenEnd);
	//}
		start.removeEdge(edge);
		end.removeEdge(edge);
	
//		BGPEval.HashJoinCompletion++;
	//	if(BGPEval.HashJoinCompletion>3)
		//	BindJoin.StartBindingSet123.notifyAll();
	
		}
		//BGPEval.HashJoinCompletion++;

		//BindJoin.StartBindingSet123.notifyAll();
		//}
//	}
   public List<EdgeOperator> CompleteEdgeOperator(EdgeOperator ct) {
	 //  Object jgl;
	   for(List<EdgeOperator>  jgl: BGPEval.JoinGroupsListAll) {
			if(jgl.contains(ct))
				return jgl;
	   }
	   return null;
   }
	@Override
	public String toString() {
		return "Hash join: " + edge;
	}

public static void ProcessingTask(LinkedHashMap<List<EdgeOperator>, List<Binding>>  joinedTriplesUnion2,LinkedHashMap<List<EdgeOperator>, List<Binding>> processedTriplesUnion2,int IsUnionClause) {
	
	int index=0;
	ArrayList<Binding> First= new ArrayList<Binding>();
	ArrayList<Binding> Second= new ArrayList<Binding>();

	ArrayList<Binding> Union = new ArrayList<>();
	//JoinedTriples	
	List<EdgeOperator> ListEdges = new ArrayList<>();
	List<EdgeOperator> ListEdges2 = new ArrayList<>();
  int IsLiteral=0;
//  Set<String> Equity1 = new HashSet<>();
//	Set<String> Equity2 = new HashSet<>();
for(Entry<List<EdgeOperator>, List<Binding>> ew1: processedTriplesUnion2.entrySet()) {
	
if(processedTriplesUnion2.size()==2) {

	try {
		Thread.sleep(500);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if(index==0) {
	//System.out.println("This is here in index value first:"+ew1.getValue());
	//	ForkJoinPool fjp = new ForkJoinPool();

		//fjp.submit(	()->{	
		First.addAll(ew1.getValue());
		ListEdges.addAll( ew1.getKey());
		ListEdges2.addAll( ew1.getKey());//}).join();
//		fjp.shutdown();
	//	////System.out.printlnln("This is here in index0:"+ew1.getKey());
		//(ew1.getKey());
	
	
	}
	if(index==1) {
	/*	
		
			  for(EdgeOperator le1:ListEdges)
			  {
				  Equity1.add(le1.te.getTriple().getSubject().toString());//////System.out.printlnln("This is ListEdges to work"+le1.te.getTriple().getSubject()+"--"+le1.te.getTriple().getObject());
				  Equity1.add(le1.te.getTriple().getObject().toString());
			  }
					for(EdgeOperator le1:ew1.getKey())
					{
						  Equity2.add(le1.te.getTriple().getSubject().toString());//////System.out.printlnln("This is ListEdges to work"+le1.te.getTriple().getSubject()+"--"+le1.te.getTriple().getObject());
						  Equity2.add(le1.te.getTriple().getObject().toString());

					}
					
//			////System.out.printlnln("This is ListEdges to work:::::::"+Equity1);
//			////System.out.printlnln("This is ListEdges to work1::::::"+Equity2);

			int isAvailable=0;
				
				for(String eq2:Equity2)
					if(Equity1.contains(eq2))
						isAvailable++;
				
	//			////System.out.printlnln("There are common nodes::::::::::::::::::::::::::::::::::::::"+isAvailable);
					//for(Node eq1:Equity1)
						if(isAvailable==0 && Equity2.size()>1 && Equity1.size()>1)
							return;
*/
		
		int isExecuted=0;
		for(EdgeOperator ew2:ew1.getKey()) {
		//	if(ListEdges2.contains(ew2))
		//		break;
			//	try {
			//		Thread.sleep(500);
				//} catch (InterruptedException e) {
			//		// TODO Auto-generated catch block
				//	e.printStackTrace();
			//	}
				//////System.out.printlnln("This is here in index1:"+ew2);
				
				if(ListEdges.toString().contains(ew2.getEdge().getV1().toString()) || ListEdges.toString().contains(ew2.getEdge().getV2().toString()))
		{
				//	////System.out.printlnln("This is here in index1.1:"+ew2);
					
				if(EvaluatedTriples.contains(ListEdges2) || EvaluatedTriples.contains(ew1.getKey()))
					break;
				if(ListEdges2.equals(ew1.getKey()))
					break;
			//	////System.out.printlnln("This is here in index1.2:"+ew2);
				
				
			//////System.out.printlnln("This is here in index1.3:"+ew2);
			

		isExecuted=1;
//				try {
	//		Thread.sleep(500);
//		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
				
				////System.out.printlnln("This is pseudo-iteration First Main:"+First.size()+"--"+First.parallelStream().limit(3).collect(Collectors.toSet()));
				////System.out.printlnln("This is pseudo-iteration First Main:"+ListEdges);
			
				////System.out.printlnln("This is pseudo-iteration Secod Main:"+ew1.getValue().size()+"--" +ew1.getValue().parallelStream().limit(3).collect(Collectors.toSet()));
		////System.out.printlnln("This is pseudo-iteration Second Main:"+ew1.getKey());
   		
   		int Which=0;
   	//	ForkJoinPool fjp = new ForkJoinPool();
//fjp.submit(()->{ 
	Second.addAll(ew1.getValue());
		ListEdges.addAll(ew1.getKey());
//}).join();
//fjp.shutdown();
     	if(ParaEng.Optional.contains("OPTIONAL") )
	     	{
	     	for(List<EdgeOperator> jg:BGPEval.JoinGroupsListOptional)
				for(EdgeOperator as:ew1.getKey())
					if(jg.contains(as))
				    Which=2;
     	for(List<EdgeOperator> jg:BGPEval.JoinGroupsListOptional)
				for(EdgeOperator as:ListEdges)
					if(jg.contains(as))
				    Which=1;
	     	for(EdgeOperator jg:BGPEval.JoinGroupsListRightOptional)
				for(EdgeOperator as:ew1.getKey())
					if(jg.equals(as))
				    Which=2;
     	for(EdgeOperator jg:BGPEval.JoinGroupsListRightOptional)
				for(EdgeOperator as:ListEdges)
					if(jg.equals(as))
				    Which=1;
	     	for(EdgeOperator jg:BGPEval.JoinGroupsListLeftOptional)
				for(EdgeOperator as:ew1.getKey())
					if(jg.equals(as))
				    Which=2;
     	for(EdgeOperator jg:BGPEval.JoinGroupsListLeftOptional)
				for(EdgeOperator as:ListEdges)
					if(jg.equals(as))
				    Which=1;

	     
	     	if(Which==2)	
   {Union=	BGPEval.GPUJoin(First,Second,First,Second,2);
   isExecuted=1;
   }	else if (Which==1)
   {  Union=	BGPEval.GPUJoin(Second,First,Second,First,1);
   isExecuted=1;
   }
   else
   {		 Union=	BGPEval.GPUJoin(Second,First,Second,First,0);
   isExecuted=1;
   }     	}
	     	else {
	  //   		try {
		//			Thread.sleep(500);
		//		} catch (InterruptedException e) {
					// TODO Auto-generated catch block
			//		e.printStackTrace();
			//	}
	     		////System.out.printlnln("This is here in Union 0:");
	     			 Union=	BGPEval.GPUJoin(First,Second,First,Second,0);
	     			  isExecuted=1;
	  				EvaluatedTriples.add(ListEdges2);
	  			   EvaluatedTriples.add(ew1.getKey());
	  		
	     	}

//		Union=	BGPEval.GPUJoin(First,Second);
//		   JoinedTriples.remove( jt.getKey());
    	if(Union==null)
     		return;
		   joinedTriplesUnion2.put(ListEdges,Union);
			EvaluatedTriples.add(ListEdges2);
			   EvaluatedTriples.add(ew1.getKey());
		
		   isExecuted=1;
		   //	////System.out.printlnln("This is joinedTriples:"+JoinedTriples.values().parallelStream().limit(3).collect(Collectors.toSet()));
		//	////System.out.printlnln("This is joinedTriples evaluatedTriples:"+Union.parallelStream().limit(3).collect(Collectors.toSet()));
		  	
	}
		
	}
		if(isExecuted==0) {
			if(IsUnionClause==1)
				
				NotJoinedTriplesUnion.put(ListEdges,First);
			else
					NotJoinedTriples.put(ListEdges,First);
		}
		}
	index++;
		
}
else if(processedTriplesUnion2.size()==1) {
	int out=0;
	int isProcessed=0;
	int e=0;
	for(List<EdgeOperator> et:EvaluatedTriples)
	if(ew1.getKey().equals(et))
	{e=1; break;}
	if(e==1)
		break;
	//System.out.println("This is size1:"+processedTriplesUnion2.size());
	for(Entry<List<EdgeOperator>, List<Binding>>  jt:joinedTriplesUnion2.entrySet())
	{
		
		for(List<EdgeOperator> et:EvaluatedTriples)
			if(jt.getKey().equals(et))
			{e=1; break;}
			if(e==1)
				break;
  			for(EdgeOperator ew1K:ew1.getKey()) {
  			//	for(List<EdgeOperator> et:EvaluatedTriples)
  		//		if(et.equals(ew1))
		if(jt.getKey().toString().contains(ew1K.getEdge().getV1().toString()) || jt.getKey().toString().contains(ew1K.getEdge().getV2().toString()))
			{
			isProcessed=1;
		
		/*	  for(EdgeOperator le:ew1.getKey())
				{	
			  
				  Equity1.add(le.te.getTriple().getSubject().toString());//////System.out.printlnln("This is ListEdges to work"+le1.te.getTriple().getSubject()+"--"+le1.te.getTriple().getObject());
				  Equity1.add(le.te.getTriple().getObject().toString());
			 
				}
				for(EdgeOperator le:jt.getKey())
				{	
						  Equity2.add(le.te.getTriple().getSubject().toString());//////System.out.printlnln("This is ListEdges to work"+le1.te.getTriple().getSubject()+"--"+le1.te.getTriple().getObject());
						  Equity2.add(le.te.getTriple().getObject().toString());

				}
				int isAvailable=0;
				
				for(String eq2:Equity2)
					if(Equity1.contains(eq2))
						isAvailable++;
		//		////System.out.printlnln("There are common nodes 0000:"+Equity1);
		//		////System.out.printlnln("There are common nodes 1111:"+Equity2);
						
		//		////System.out.printlnln("There are common nodes Single::::::::::::::::::::::::::::::::::::::"+isAvailable);
					//for(Node eq1:Equity1)
						if(isAvailable==0 && Equity1.size()>1 && Equity2.size()>1)
							return;
*/
			
			if(ew1.getKey().toString().equals(jt.getKey().toString())) 
				break;
						
			////System.out.printlnln("This is 1 pseudo-iteration:"+ew1.getKey()+"--"+ew1.getValue().parallelStream().limit(3).collect(Collectors.toSet()));
			First.addAll(ew1.getValue());
			////System.out.printlnln("This is 1 pseudo-iteration Second Main:"+jt.getKey()+"--"+jt.getValue().parallelStream().limit(3).collect(Collectors.toSet()));
			//for( com.hp.hpl.jena.sparql.engine.BindingSet.BindingSet jt2:jt1)
				//////System.out.printlnln("This is pseudo-iteration Second Sub:"+jt1.parallelStream().limit(3).collect(Collectors.toSet()));
   			int Which=0;
			
//ForkJoinPool fjp = new ForkJoinPool();

//fjp.submit(	()->{		
	Second.addAll(jt.getValue());
				ListEdges.addAll(ew1.getKey());
				ListEdges.addAll(jt.getKey());
//}).join();
//fjp.shutdown();
		     	if(ParaEng.Optional.contains("OPTIONAL"))
		     	{
		     	for(List<EdgeOperator> jg:BGPEval.JoinGroupsListOptional)
   				for(EdgeOperator as:ew1.getKey())
   					if(jg.contains(as))
   				    Which=1;
		     	for(EdgeOperator jg:BGPEval.JoinGroupsListRightOptional)
	   				for(EdgeOperator as:ew1.getKey())
	   					if(jg.equals(as))
	   				    Which=1;

		     	for(EdgeOperator jg:BGPEval.JoinGroupsListLeftOptional)
	   				for(EdgeOperator as:ew1.getKey())
	   					if(jg.equals(as))
	   				    Which=1;

		     	//ew1.getKey().size();
		     		
		     	if(Which==1)	
	   {Union=	BGPEval.GPUJoin(Second,First,Second,First,1);
	   isProcessed=1;
	   }     	
		     		 
			else
			{			  Union=	BGPEval.GPUJoin(First,Second,First,Second,0);
			isProcessed=1;
			}
		     	}
		     	else {
		     		int c=0;
		     	for(EdgeOperator ew2: jt.getKey())
		     		if(ew2.getEdge().toString().contains("http"))
		     	c++;
		     	
		     	if(c==jt.getKey().size())
		     	{		     		 Union=	BGPEval.GPUJoin(Second,First,Second,First,4);
		     	////System.out.printlnln("THis is number 4:"+Union.size()+"--"+Union.parallelStream().limit(4).collect(Collectors.toSet()));
		     	isProcessed=1;
		     	}
		     	else
		     	{		 Union=	BGPEval.GPUJoin(First,Second,First,Second,0);
		     	isProcessed=1;
		     	}}
		     	if(Union==null)
		     		return;
		     	
		     	joinedTriplesUnion2.remove( ew1.getKey());
		 	joinedTriplesUnion2.remove( jt.getKey());
		   joinedTriplesUnion2.put(ListEdges,Union);
		   isProcessed=1;
		   EvaluatedTriples.add(ew1.getKey());
			EvaluatedTriples.add(jt.getKey());
			
		//   ////System.out.printlnln("This is the result of joinTriple:"+Union.parallelStream().limit(3).collect(Collectors.toSet()));

out=1;		
		   break;
			}		
if(out==1) 
	break;

  			}
  			if(out==1) {
  				out=0;
  				break;
  			}
		
	}
//	System.out.println("This is size1=====1:"+processedTriplesUnion2.size());

	if(isProcessed==0)
		if(IsUnionClause==1)
			NotJoinedTriplesUnion.putAll(processedTriplesUnion2);
		else
		NotJoinedTriples.putAll(processedTriplesUnion2);
}
else {
	if(IsUnionClause==1)
	NotJoinedTriplesUnion.putAll(processedTriplesUnion2);
	else
	NotJoinedTriples.putAll(processedTriplesUnion2);}
}

}

public static void ProcessUnion() {
	
	System.out.println("------------------------This is processing Union---------------------------");
	for(List<EdgeOperator> et:EvaluatedTriples)
	{
		ProcessedTriplesUnion.remove(et);
	}
	
	for(List<EdgeOperator> e:EvaluatedTriples)
	System.out.println("These are evaulatedTriple:"+e);
	for(Entry<List<EdgeOperator>, List<Binding>> e:JoinedTriplesUnion.entrySet())
		System.out.println("These are JoinTripleUnion:"+e.getKey());
	for(Entry<List<EdgeOperator>, List<Binding>> e:NotJoinedTriplesUnion.entrySet())
		System.out.println("These are NotJoinTripleUnion:"+e.getKey());
	for(Entry<List<EdgeOperator>, List<Binding>> e:ProcessedTriplesUnion.entrySet())
		System.out.println("These are ProcessedTriplesUnion:"+e.getKey());
	
	ProcessingTask(JoinedTriplesUnion,ProcessedTriplesUnion,1);
		ProcessingTask(NotJoinedTriplesUnion,ProcessedTriplesUnion,1);

//	}else
//	{
	//  if(IsUnion==1)		{
			
	//  }
	
	
	
	
	
	for(List<EdgeOperator> et:EvaluatedTriples)
	{
		NotJoinedTriplesUnion.remove(et);
	}


for(int i=0;i<9;i++){	
ForkJoinPool fjp1 = new ForkJoinPool();
fjp1.submit(()->{
	BGPEval.finalResultCalculation(NotJoinedTriplesUnion,EvaluatedTriples,JoinedTriplesUnion);}).join();
fjp1.shutdown();
ForkJoinPool fjp2 = new ForkJoinPool();

fjp2.submit(()->{BGPEval.finalResultCalculation(JoinedTriplesUnion,EvaluatedTriples,JoinedTriplesUnion);}).join();
fjp2.shutdown();

ForkJoinPool fjp21 = new ForkJoinPool();

fjp21.submit(()->{BGPEval.finalResultCalculation(NotJoinedTriplesUnion,EvaluatedTriples,NotJoinedTriplesUnion);}).join();
fjp21.shutdown();
}

	if(ParaEng.Union.contains("UNION") && (!JoinedTriplesUnion.isEmpty() || JoinedTriplesUnion!=null || JoinedTriplesUnion.size()>0))
	{	ArrayList<Binding> First= new ArrayList<Binding>();
		ArrayList<Binding> Second= new ArrayList<Binding>();
		List<EdgeOperator> ListEdges = new ArrayList<>();
		
		for(Entry<List<EdgeOperator>, List<Binding>> jgu:JoinedTriplesUnion.entrySet())
		{First.addAll(jgu.getValue());
		ListEdges.addAll(jgu.getKey());
		}
		for(Entry<List<EdgeOperator>, List<Binding>> jg:JoinedTriples.entrySet())
		{
			Second.addAll(jg.getValue());
		ListEdges.addAll(jg.getKey());
		}
		if(First!=null && Second!=null) {
		ArrayList<Binding> Union = new ArrayList<>();
		BGPEval.GPUJoin(First,Second,First,Second,2);
		 JoinedTriples.put(ListEdges,Union);}
}
}
static void IntermediateProcedure(List<Binding> results,List<EdgeOperator> AllEdges) {
	Set<Binding>	temp1 = new HashSet<>();
	
	///	results = QueryUtil.join(results, start.getBindings());
		for(Entry<ConcurrentHashMap<Vertex, Edge>, ArrayList<Binding>> e:BGPEval.StartBindingFinal.entrySet())
		{	System.out.println("This is end 23 set Binding in HashJoin:"+e.getKey());
		
		
			//System.out.println("This is end 24 set Binding in HashJoin11111:"+BGPEval.StartBindingSetBJ);
			for(EdgeOperator ae:AllEdges) {
				System.out.println("This is end 23 set Alledges in HashJoin:"+ae);
					for(Entry<Vertex, Edge> ev:e.getKey().entrySet())
					{joinVars=null;
			//		System.out.println("This is euqality of AllEdges Vertex:"+ae.getEdge()+"--"+ev.getValue());
						
					if(ev.getValue().toString().equals(ae.getEdge().toString())) {
						//for(Entry<Vertex, Set<Binding>> e1:BGPEval.StartBinding123.entrySet())
					//				if(e1.getKey().toString().equals(ev.getKey().toString()))
					//					if(e1.getValue()!=null)
					//					return;
					//	System.out.println("This is end 25 set Alledges equal in HashJoin:"+ae.getEdge()+"--"+ev.getValue());
									
							Iterator<Var> l = results.iterator().next().vars();
							Var r = Var.alloc(ev.getKey().getNode());
					//	System.out.println("This is rule no. 1:"+r+"--"+ev.getKey().getNode());
							while (l.hasNext()) {
								Var v = l.next();
								
								if (r.equals(v)) {
				//					System.out.println("This is rule no.3:"+r+"--"+v);
									joinVars=v;
								}
							}
							
							if(joinVars==null)
								return;
			//				System.out.println("This is rule no. 2:"+joinVars);
							
							for(Binding e1:results){
					//		BindingMap join = BindingFactory.create();
					//		join.add(joinVars, e1.get(joinVars));
							
							temp1.add(BindingFactory.binding(joinVars, e1.get(joinVars)));
							//for(int k=0;k<4;k++)
							//	System.out.println("This is join:"+temp1);
								
								
						};
		//				System.out.println("This is temp1:"+temp1.size());
							BGPEval.StartBinding123.put(ev.getKey(), temp1);
		//					for(Entry<Vertex, Set<Binding>> sj:BGPEval.StartBinding123.entrySet())
	//							System.out.println("This is bindJoin with value:"+sj.getKey()+"--"+sj.getValue().size());
							
					}
						
					}
					
			
			}
		}
		//for(Entry<Vertex, Set<Binding>> sj:BGPEval.StartBinding123.entrySet())
		//	System.out.println("This is bindJoin with value111111:"+sj.getKey()+"--"+sj.getValue().size());
}

}
