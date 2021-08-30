package com.fluidops.fedx.trunk.parallel.engine.exec;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fluidops.fedx.trunk.parallel.engine.main.StageGen;
import com.fluidops.fedx.trunk.parallel.engine.opt.ExhOptimiser;
import com.fluidops.fedx.trunk.description.RemoteService;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.BindJoin;
import com.fluidops.fedx.trunk.parallel.engine.exec.operator.EdgeOperator;
import com.fluidops.fedx.trunk.parallel.engine.main.BGPEval;
import com.fluidops.fedx.trunk.stream.engine.util.QueryUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fluidops.fedx.structures.Endpoint;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import java.util.*;  
import java.io.*;  
public class BoundQueryTask extends QueryTask {


//static int i11 = 0;
//static  int i=0;
//ArrayList<Binding>  = new ArrayList<Binding>();
  Binding bindings ;
  static String task;
//HashMap<Var, String> otherBinding =new HashMap<>() ;
//static Map<Map<Map<Node,Integer>,Node>,Node> bin = Collections.synchronizedMap(new HashMap<>()) ;
	public static int ja=0;	
	int index;
	public BoundQueryTask(TripleExecution te, Endpoint service, Binding bindings, String string) {
		super(te, service);
		this.bindings = bindings;
		this.task  =string;
	//for(Binding b:bindings)
	//	System.out.println("This is secure here:"+b);
		// //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!BoundQueryTask!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

	}

	
public synchronized Binding getBindings(){
	return bindings;
}	
	@Override
	protected synchronized Query buildQuery() {
		// //System.out.println("BoundQueryTask is being used");
	//bin.clear();
//		TripleExecution.i++;
		//synchronized(bindings) {
		List<Binding> bs= new ArrayList<Binding>();
		 Node subject,object,predicate;
//		 if(BindJoin.MultipleBinding!=null)
//for(Entry<Binding, Binding> i:BindJoin.MultipleBinding.entries())
//	if(i.getValue().equals(this.bindings))
//		bs.addAll(BindJoin.MultipleBinding.get(i.getKey()));
//	 System.out.println("This is doubly query being processed::"+bs+"--"+this.bindings+"--"+bs.size());
		 
		Query query = new Query();
		ElementGroup elg = new ElementGroup();
				Binding binding = this.bindings;
				// BGPEval.JoinGroupsList.stream().
		//if(bs.isEmpty()|| bs==null) {
				if (getBusyTreeTriple() != null ) {
			// int ced=0;

			for (EdgeOperator btt : getBusyTreeTriple()) {
//if(ced<ghi) {
				//// System.out.println("This is the BushyTreeTrple in QueryTask:"+btt);
		
				subject= QueryUtil.replacewithBinding(btt.getEdge().getTriple().getSubject(), binding);
				predicate = QueryUtil.replacewithBinding(btt.getEdge().getTriple().getPredicate(), binding);
				object = QueryUtil.replacewithBinding(btt.getEdge().getTriple().getObject(), binding);
		///		Stream.of(ReplaceLabelInQueries(subject,object,predicate,binding));
				//Node;
				//Node 
				if (subject.isVariable()) {
					subject = Var.alloc(subject.getName() );
				}

				if (predicate.isVariable()) {
					predicate = Var.alloc(predicate.getName()  );
				}

				if (object.isVariable()) {
					object = Var.alloc(object.getName() );
				}

				Triple newtriple = new Triple(subject, predicate, object);
		//				ElementGroup elg = new ElementGroup();

				elg.addTriplePattern(newtriple);
		//		System.out.println("This is processing:"+elg+"--"+newtriple);
//		ced++;
//}		
			}
		}
				else {
					subject= QueryUtil.replacewithBinding(triple.getSubject(), binding);
					predicate = QueryUtil.replacewithBinding(triple.getPredicate(), binding);
					object = QueryUtil.replacewithBinding(triple.getObject(), binding);
			///		Stream.of(ReplaceLabelInQueries(subject,object,predicate,binding));
					//Node;
					//Node 
					if (subject.isVariable()) {
						subject = Var.alloc(subject.getName() );
					}

					if (predicate.isVariable()) {
						predicate = Var.alloc(predicate.getName()  );
					}

					if (object.isVariable()) {
						object = Var.alloc(object.getName() );
					}

					Triple newtriple = new Triple(subject, predicate, object);
			//		ElementGroup elg = new ElementGroup();
					
					elg.addTriplePattern(newtriple);
				}
		
	//	}
/*		else if(bs!=null) {
		ElementUnion eu = new ElementUnion();
			for(int i=0;i<bs.size();i++) {
				Binding binding1 = bs.get(i);
				Node subject1 = QueryUtil.replacewithBinding(triple.getSubject(), binding1);
				Node predicate1 = QueryUtil.replacewithBinding(triple.getPredicate(), binding1);
				Node object1 = QueryUtil.replacewithBinding(triple.getObject(), binding1);
				if(subject1.isVariable()) {
					subject1 = Var.alloc(subject1.getName());
				}
				
				if(predicate1.isVariable()) {
					predicate1 = Var.alloc(predicate1.getName());
				}
				
				if(object1.isVariable()) {
					object1 = Var.alloc(object1.getName());
				}
				
				Triple newtriple = new Triple(subject1,predicate1,object1);
				ElementTriplesBlock tb = new ElementTriplesBlock();
				tb.addTriple(newtriple);
				eu.addElement(tb);
			}
			
			List<Element> elements = eu.getElements();
			
			if(elements.size() > 1) {
				elg.addElement(eu);
			}
			else {
				elg.addTriplePattern(((ElementTriplesBlock)elements.get(0)).getPattern().get(0));
			}
			
		System.out.println("This is the query:"+elg );
		}*/
				query.setQueryResultStar(true);
		query.setQueryPattern(elg);
		query.setQuerySelectType();
		
		//		////System.out.println("---------------QueryTask buildQUery---------------: "+query+"--"+elg.getElements()+"--"+query.getQueryPattern()+"--"+query.getBindingValues()+"--"+query.getResultURIs());
	//	isBound = 0;
//		return query;
	QueryTask.isBound=1;
			
//	System.out.println("This is the query:"+query);
		return query;
		//}
	}

	@Override
	public 	synchronized Set<Binding> postProcess(ResultSet rs) {
		Set<Binding> resultsBq =
				 Collections.synchronizedSet(new HashSet<Binding>());
	
		
		//	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!BoundQueryTask222222222222222222222!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		//	int i=0;
			
		//	Set<Binding> a = new HashSet<>();		
	/*		for(Binding b:this.bindings)
			{	while(b.vars().hasNext()) {
					Var x = b.vars().next();
					
					System.out.println("These are the bindings:"+Var.alloc(x+"_"+i)+"--"+ b.get(x));
					a.add(BindingFactory.binding(Var.alloc(x.toString().substring(i)+"_"+i), b.get(x)));
				//	System.out.println("This is resultset:"+x+""+i+"--"+b.get(x));
				}
			i++;
			//System.out.println("This");
			}*/	
//for(Binding a1:a)
//System.out.println("This is a new error:"+a1);
//		ForkJoinPool fjp1 = new ForkJoinPool();
	//				fjp1.submit(
				//()->
	//	if(ParaEng.pConstant.toString().contains("?p=")) {		
	//	
	//			Res=Non_Predicate(result1);
	//	}
			//	).join();
			//	fjp1.shutdown();
				
			//	if(Res!=null);
			//	for(Entry<Integer, String> r:Res.entrySet())
			//		System.out.println("This is result values:"+r);
		
		
	//	Multimap<Integer ,String> pInc = ArrayListMultimap.create();
		//HashMap<String,Integer>  = new HashMap<>();
		//Map<Object, List<Integer>> FinalMap =new HashMap<>();
//		ForkJoinPool fjp = new ForkJoinPool();
/*		if(ParaEng.pConstant.toString().contains("?p="))
		while (rs.hasNext()) {
			Binding binding = rs.nextBinding();
			HashMap<Var, String> otherBinding = ExtractBinding(binding);
			for(Entry<Var,String> ob:otherBinding.entrySet())
			{
						
				
int	num=Integer.parseInt(ob.getKey().getName().substring(ob.getKey().getName().indexOf("_")+1));

for(Entry<Integer, String> fm:Res.entrySet())
if(num==fm.getKey())
	extend.add(Var.alloc((String)ParaEng.pConstant.values().toArray()[0]), StageGen.StringConversion(fm.getValue()));

								
extend.add(Var.alloc(ob.getValue()), binding.get(ob.getKey()));


			}
//System.out.println("This is now extend:"+extend);
results.add(extend);
//fjp.shutdownNow();
//results.notifyAll();
//}

//i++;
		}
		else
	*/		//String b1=
	//	String b2="";
		String key=(String) pro.keySet().toArray()[0];
		//System.out.println("This is the respective keySet"+key);
		while(rs.hasNext())
		proExt.put(key, rs.nextBinding());
		for(Entry<String, ResultSet> p:pro.entries())
			while(p.getValue().hasNext())
				proExt.put(key, p.getValue().nextBinding());
		//for(Entry<String, Binding> pe:proExt.entries())
		//	System.out.println("This is pe:"+pe);
		synchronized(proExt) {
			proExt.entries().parallelStream().forEach(entry->{

	//	ab++;
//	System.out.println("These are the number of bidings:"+ab);

	//BindingHashMap extend = new BindingHashMap();// TODO do not use a new binding map
	
	
	
  // String p="";
	//	while(entry.getValue().hasNext())
		//{
			Set<String> finall =new HashSet<>() ;
			String s1 = null;
			BindingHashMap extend = new BindingHashMap();// TODO do not use a new binding map

			String ar=entry.getValue().toString();
				//System.out.println("This is extend1:"+ar+"--"+entry.getKey());	
			
				Pattern regex = Pattern.compile("[?=\\)][?= ][?=\\(]");
				 String[] regexMatcher = regex.split(ar);
				//synchronized(regexMatcher) {
				
				 Stream<String> stream1 = Arrays.stream(regexMatcher);
				// stringStream.toArray(String[]::new);
				 
					
				 stream1.parallel().forEach(st->{
					 String p1="";
						String str = null;
						String[] mlp=st.split(" ");	 
						for(int iii=3;iii<=mlp.length-1;iii++)
						{ str+=mlp[iii].replace(")", "").replace("(", "").replace("\"", "")+" ";
							
						}String mll=st;
						  p1=mll.substring(mll.toString().indexOf("?"),mll.toString().indexOf("=")-1);
						  if(!str.contains("http"))
							extend.add(Var.alloc(p1.substring(1)),StageGen.StringConversion("\""+str.substring(0,str.length()-1).replace("null", "").replace(")", "")+"\""));  
						  else
								extend.add(Var.alloc(p1.substring(1)),StageGen.StringConversion(str.replace("null", "").replace("<", "").replace(">", "").replace(" ", "")));  
			//	 System.out.println("THis is extend000:"+extend);
				 });
		
					
				 String rr=entry.getKey().toString();
					
				// System.out.println("------------------------------------------------------------------");
				//	System.out.println("This is query:"+rr);
					
		//	System.out.println("This is index value within:"+rr+"--"+extend);
				// if(extend==null || extend.isEmpty())
				//	 continue;
			/*	 for(String ml:regexMatcher)
				{	String str = null;
				 String p1="";
					 String[] mlp= ml.split(" ");
					for(int iii=3;iii<=mlp.length-1;iii++)
					{ str+=mlp[iii].replace(")", "").replace("(", "").replace("\"", "")+" ";
						
					}String mll=ml;
					  p1=mll.substring(mll.toString().indexOf("?"),mll.toString().indexOf("=")-1);
					  if(!str.contains("http"))
						extend.add(Var.alloc(p1.substring(1,p1.indexOf("_"))),StageGen.StringConversion("\""+str.substring(0,str.length()-1).replace("null", "").replace(")", "")+"\""));  
					  else
							extend.add(Var.alloc(p1.substring(1,p1.indexOf("_"))),StageGen.StringConversion(str.replace("null", "").replace("<", "").replace(">", "").replace(" ", "")));  
				}*/			
				// System.out.println("This is now extend:"+extend);
				//	 System.out.println("This is now value:"+rr);
						
				 	//String[] rrr=rr.split("UNION");
//				 	int inc=0;
				 	//for(String r:rrr)
		//		 	System.out.println("This is problem here:"+rrr[index]);
				 	//for(String r3:rrr) {
						//if(inc==index)
						//{
				// String[]	rr1=rrr[index].split(" ");
					
					//	System.out.println("This is extend2:"+finall);	
														
							//}
	//						if(!finall.isEmpty())
						//	System.out.println("This is the problem here010101:"+finall);
					//inc++;
				//	}
					
				//	System.out.println("This is the problem here010101:"+finall);
				//	 System.out.println("------------------------------------------------------------------");
						
					//					System.out.println("This is the extend here:"+extend);
								//System.out.println("This is the problem here:"+rr);
				//		 System.out.println("This is extend:"+extend);
													
				
				//	 System.out.println("This is extend3:"+e);
int a=0;
String s3 = null;
//for(String rrr1:rr.split(" ")) {
//System.out.println("This is extend2:"+a+"--"+rrr1);
//}
				 for(String rrr1:rr.split(" ")) {
							
					 
					      if((rrr1.endsWith("\n")) && rrr1.startsWith("<"))
					      {		s1=rrr1.replace("\n", "");
						   	 extend.add(Var.alloc(triple.getSubject().toString().substring(1)), StageGen.StringConversion(s1.replace("<", "").replace(">", "")));
						 //    System.out.println("This is extend3.1:"+triple.getSubject()+"--"+extend);		
					      }
					    
								 
				 }
					    	 
				 
				 if(s1==null)
				 {	 for(String rrr1:rr.split(" ")) {
			    	  if(rrr1.startsWith("<") && rrr1.endsWith(">"))
						 {//	 System.out.println("This is extend3:"+a+"--"+rrr1);
						if(a==9)
						{	s1=rrr1;
						 extend.add(Var.alloc(triple.getObject().toString().substring(1)), StageGen.StringConversion(s1.replace("<", "").replace(">", "")));
					//	 System.out.println("This is extend3.2:"+triple.getObject()+"--"+extend);
						   	
						} 
				//		System.out.println("This is extend3.3:"+a+"--"+rrr1);
						 }
			    		a++;
				 }}
					 //		finall.add(s1);
							
					
					//for(int i=0; i<rr.length(); i++)
				//	{
				//	  int asciiValue = rr.charAt(i);
				//	  System.out.println("This is the ascii value:"+rr.charAt(i) + "=" + asciiValue);
				//	}
				//	System.out.println("This is extend:"+rr+"--"+extend+"--"+s1);	
					//			if(s1t==1)
					//			else
						//			extend.add(Var.alloc(triple.getObject().toString().substring(1)), StageGen.StringConversion(s1.replace("<", "").replace(">", "")));
								
					//				System.out.println("This is extend1:"+extend+"--"+triple.getObject());	

						 // if(!resultsBq.contains(extend))
						 resultsBq.add(extend);  
				//	for(Binding rb:resultsBq)
			//		 System.out.println("This is extend:"+rb);
							
										 
	/*				 for(Entry<String, String> f:finall.entrySet())	
					if(e.equals(f.getValue())) {
		
extend.add(Var.alloc(triple.getSubject().toString().substring(1)), StageGen.StringConversion(f.getKey().replace("<", "").replace(">", "")));
	
resultsBq.add(extend); */
				//	}
				
	/*			 for(String ar1:ar.split(" "))
					//	if(ar1.startsWith("?"))
					//		{	
									for(Entry<String, String> f:finall.entrySet())	
							if(ar1.equals(f.getValue())) {
				
	extend.add(Var.alloc(triple.getSubject().toString().substring(1)), StageGen.StringConversion(f.getKey().replace("<", "").replace(">", "")));
			
	resultsBq.add(extend);//});
	*/
				//			}
//}
					//}
//		}
}
);
//for(Entry<String, ResultSet> r:pro.entrySet())
//{
//synchronized(r) {	
		//								if(resultsBq.toString().contains("TCGA-21"))
		//							System.out.println("This is the problem here44444:"+resultsBq);

	//						}
		//				}		//
							//}	
	//	}
	//	l=r2;
	//System.out.println("This is the problem here111:"+l);	
//}}
	

	
//}
//}
//for(Entry<String, ResultSet> r:pro.entrySet())
	
//{
/*	System.out.println("This is need:"+r);
	String s0=""; String s1="";
	HashMap<String,String> s=new HashMap<>();
	
	s.put(s0, s1);
	while(r.getValue().hasNext())
	{System.out.println("This is the problem here:"+s+"--"+r.getValue().nextBinding().toString());

	}
	for(String st:r.getKey().split(" "))
	{
		if(st.contains("http"))
			s1=st.replace("}", "").replace("{", "");

		//System.out.println("These are strings:"+st);
		if(st.startsWith("?"))
			s0=st;
	//}
	 * 
	 * 
}*/
/*synchronized(rs) {
while (rs.hasNext()) {
	
	Binding binding = rs.nextBinding();
	System.out.println("This is binding:"+binding);
	int lock=0;

	//	extend=new BindingHashMap();
	ArrayList<Binding> resultsSub = new ArrayList<Binding>();
		
String ch=	binding.toString().substring(binding.toString().indexOf("?")+1,binding.toString().indexOf("=")-1);
String ch1=	binding.toString().substring(binding.toString().indexOf("_")+1,binding.toString().indexOf("=")-1);
int x=0;
for(Entry<Map<Map<Node, Integer>, Node>, Node> b:BoundQueryTask.bin.entrySet())
				{
	if(lock==1)
		continue;
			for(Entry<Map<Node, Integer>, Node> b1:b.getKey().entrySet())
								for(Entry<Node,Integer> b2:b1.getKey().entrySet())
				{	
									
									String b2K = b2.getKey().getName();
									String chK=ch.toString();
									String b2V=b2.getValue().toString();
									String ch1K=ch1;
//									System.out.println("This is binding123:"+b2K+"--"+chK);
//									System.out.println("This is binding1234:"+b2V+"--"+ch1K);
//									System.out.println("This is b:"+b+"--"+ch1);
				
			//		System.out.println("This is bin:"+b2.getKey().getName()+"--"+ch.toString());
					//	
					//	System.out.println("This is bin:"+b1.getValue()+""+ch1+"--"+b.getValue());
					//	System.out.println("This is bin2:"+BindingFactory.binding(Var.alloc(b1.getValue().toString().substring(1)+""+ch1), b.getValue()));
				//	HashMap<Node, Node> b3=new HashMap<>();
				//	b3.put(b1.getKey(),b1.getValue());;
					//for(Entry<Node, Node> bb:b3.entrySet())
					//b2=bb.ke.getName().substring(b2.indexOf("_"),b2.indexOf("=")-1);
											
						//otherBinding =ExtractBinding(BindingFactory.binding(Var.alloc(b1.getValue().toString().substring(1)+""+ch1), b.getValue()));
			//otherBinding.entrySet().parallelStream().forEach(e->{extend.add(Var.alloc(e.getValue()), binding.get(e.getKey()));results.add(extend);});
//			String index=b1.getKey().toString().substring(binding.toString().indexOf("_"),binding.toString().indexOf("=")-1);
						//System.out.println("This is now extend00:"+b1);
						
					//	System.out.println("This is now extend:"+binding+"--"+ch1+"--"+b2.getValue());
						
						if(b2K.equals(chK)) {
							
			if(b2V.equals(ch1K))
			{	lock=1;
			BindingHashMap extend = new BindingHashMap();// TODO do not use a new binding map
			BindingHashMap extend1 = new BindingHashMap();// TODO do not use a new binding map
			

			
			 	
		//	 System.out.println("This is now extend1:"+extend1);
		
	//			System.out.println("This is now extend999:"+extend+"--"+b1+"--"+BindingFactory.binding(Var.alloc(b1.getValue().toString().substring(1)+""+ch1), b.getValue()));
				
				//for(Entry<Node, Node> bb:b3.entrySet())
			//b1.put(bb.getKey(),bb.getValue());;
				//resultsBq.add(extend1);
				

				otherBinding	= ExtractBinding(binding);

				//otherBinding.entrySet().parallelStream().forEach(e->{
					
					//Object e;
					for(Entry<Var,String> e:otherBinding.entrySet())
					extend1.add(Var.alloc(e.getValue()), binding.get(e.getKey()));  
					extend1.addAll(BindingFactory.binding(Var.alloc(b1.getValue().toString().substring(1)), b.getValue()));
					//			System.out.println("This is now extend1:"+extend1);/	
					resultsBq.add(extend1);//});

		//				System.out.println("This is now extend000:"+resultsBq);
				//break;
				////System.out.println("This is now result:"+results);
			//	extend=null;
			}
					}
				}	}}
*/
//System.out.println("-----------------------------------------END oF BIND JOIN-------------------------------------");
		
//		System.out.println("This is now result11111:"+results);

	
//		for(Entry<Var,String> ob:otherBinding.entrySet())
	//		{				
//extend.add(Var.alloc(ob.getValue()), binding.get(ob.getKey()));

//}

//results.notifyAll();
//}

//i++;
	//	}
	//	fjp.shutdownNow();

		//	for (Entry<Integer, String> entry : pInc.entrySet()) {
	//		  multiMap.put( entry.getKey(),entry.getValue());
	//		}			
	//	FinalMap=pInc.keySet().stream().collect(Collectors.groupingBy(k -> pInc.get(k)));
	//	for(Entry<Integer, String> fm:FinalMap.entrySet())
	//	System.out.println("This is final value:"+fm);
	
}
//	ja=i;

//System.out.println("-------------------------------------------------------------------------");
//for(Binding r:resultsBq)
//System.out.println("These are resultsBq:"+r);
//System.out.println("These are the extends:"+resultsBq.size());
return resultsBq;
	}
	
	public int ReplaceLabelInQueries(Node subject, Node Object, Node Predicate,Binding binding) {
		subject= QueryUtil.replacewithBinding(triple.getSubject(), binding);
		Predicate = QueryUtil.replacewithBinding(triple.getPredicate(), binding);
		Object = QueryUtil.replacewithBinding(triple.getObject(), binding);
	return 0;
	}
public static HashMap<Var,String> ExtractBinding(Binding binding) {
	HashMap<Var,String> Combination =new HashMap<>();
	Arrays.stream(binding.toString().split(" ")).parallel().forEach(b->{
		if(b.contains("?") && !b.startsWith("\""))
		{
		//	Var var = );
			//String vName = null ;
			if(b.contains("_"))
			//vName =;
			Combination.put(Var.alloc(b.substring(1)),b.substring(1,b.indexOf("_")));
//			//System.out.println("This is correction of bindJoin results1:"+binding+"--"+b1+"--"+var1+"--"+var2+"--"+binding.get(var1));
		}		
	});
	
//	String[] b = binding.toString().split(" ");
//	for(String b1:b) {


	//	}
	return Combination;

}
@Override
public  List<EdgeOperator> BushyTreeTripleCreation(Triple triples)
{	
	List<EdgeOperator> BushyTreeTriple2 = new Vector<EdgeOperator>();
	List<EdgeOperator> AlreadyProcessed = new Vector<EdgeOperator>();
	List<List<EdgeOperator>> JoinGroups = new Vector<>();
//if(ParaEng.Optional.contains("OPTIONAL") || ParaEng.Union.contains("UNION")) 
	JoinGroups.addAll(BGPEval.JoinGroupsListOptional);
	JoinGroups.addAll(BGPEval.JoinGroupsListExclusive);
	if (BGPEval.JoinGroupsListRight.size() > 0)
		JoinGroups.add(BGPEval.JoinGroupsListRight);
	if (BGPEval.JoinGroupsListLeft.size() > 0)
		JoinGroups.add(BGPEval.JoinGroupsListLeft);
	if (BGPEval.JoinGroupsListRightOptional.size() > 0)
		JoinGroups.add(BGPEval.JoinGroupsListRightOptional);
	if (BGPEval.JoinGroupsListLeftOptional.size() > 0)
		JoinGroups.add(BGPEval.JoinGroupsListLeftOptional);
//logger.info("This is the processing order:"+JoinGroups);
	int br = 0;
//if(BGPEval.IsLeftQuery==false&&BGPEval.IsRightQuery==false)
//{	
	for (int i = 0; i < JoinGroups.size(); i++)
		if (JoinGroups.get(i).size() > 0)
			for (Entry<EdgeOperator, Map<EdgeOperator, Integer>> e2 : BGPEval.exchangeOfElements.entrySet()) {
				for (Entry<EdgeOperator, Integer> e1 : e2.getValue().entrySet())
					if (e2.getKey().equals(JoinGroups.get(i).get(0)) && e1.getValue() == 1)
						if (e1.getKey().getEdge().getTriple().equals(triple)) {
							{
								BushyTreeTriple2.addAll(JoinGroups.get(i));
								br = 1;
								break;
							}
						}
				if (br == 1)
					return BushyTreeTriple2;
			}

//}
//}
//else {
	BushyTreeTriple2 = null;

	return BushyTreeTriple2;


	
}


static String shortestWord(ArrayList<String> words) {
	String shortest = words.stream().parallel()
	        .sorted((e2, e1) -> e1.length() > e2.length() ? -1 : 1)
	        .findFirst().get();
return shortest;
}
/*
protected Object clone() throws CloneNotSupportedException{
    BGPEval student = (BGPEval) super.clone();
    student.results = results ;
    return student;
 }*/

/*private static Object deepCopy(Object object) {
	   try {
	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	     ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
	     outputStrm.writeObject(object);
	     ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
	     ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
	     return objInputStream.readObject();
	   }
	   catch (Exception e) {
	     e.printStackTrace();
	     return null;
	   }
	 }
*/}
