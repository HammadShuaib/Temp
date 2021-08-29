package com.fluidops.fedx.trunk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.Symbol;

import com.fluidops.fedx.trunk.config.Config;
//import trunk.description.Statistics;
import com.fluidops.fedx.trunk.parallel.engine.ParaEng;
import com.fluidops.fedx.trunk.parallel.engine.error.RelativeError;

public class lhd {

	static String queries_path = null;
	static String stats_path = null;
	static String rel_err_path = null;
	static File queriesFile = null;
	static boolean rest = false;
	static Integer[] ignor = {};
	public static long tpSources;
	public static long sourceSelectionTime;
	public static boolean relative_err = false;
	public static String query_name = "S2";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// argsParse(args);
//		if(args.length<4){
		// System.err.println("Arguments must be specified \n1) path of queries file
		// \n2) path of the stats folder \n3) Relative error file path");
		// System.exit(0);
		// }
		stats_path = "C:\\University\\Thesis\\CostBased-FedEval\\source code\\LHD\\stats";
		queries_path = "C:\\University\\Thesis\\CostBased-FedEval\\source code\\LHD\\";
		rel_err_path = "C:\\University\\Thesis\\CostBased-FedEval\\source code\\LHD\\";
		relative_err = Boolean.parseBoolean("C:\\University\\Thesis\\CostBased-FedEval\\source code\\LHD\\");
		query_name = "S4";

		lhd test = new lhd();
		// test.run();
		System.exit(0);
	}

	private static void argsParse(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: lhd -q \"path_to_query_file\" Execute queries in the file");
			System.err.println("-r Have breaks between queries");
			System.err.println("-i x;y;z Ignor queries of number x, y and z");
			System.exit(1);
		}

		// List<String> arglst = new ArrayList<String>();
		// arglst.addAll(Arrays.asList(args));

//		while(!arglst.isEmpty()) {
//			String cmd = arglst.remove(0);
//			if(!cmd.startsWith("-")) {
//				System.err.println("Should be one of \"-q\", \"-r\", \"-i\"...");
//				System.exit(1);
//			}

//			switch (cmd) {
//				case "-q":
//					queriesFile = new File(parseP(arglst));
//					if(!queriesFile.exists()) {
//						System.err.println(queriesFile.getName()+" does not exist");
//						System.exit(1);
//					}
//					break;
//				case "-i":
//					String str = parseP(arglst);
//					String[] _ignor = str.split(";");
//					List<Integer> lst = new ArrayList<Integer>();
//					for(String s:_ignor){
//						lst.add(Integer.getInteger(s));
//					}
//					lst.addAll(Arrays.asList(ignor));
//					ignor = (Integer[]) lst.toArray();
//				case "-v":
//					Config.debug = true;
//					break;
//				case "-r":
//					rest = true;
//					break;
//				default:
//					System.out.println("Unregonized cmmand");
//					break;
//			}
	}

	private static String parseP(List<String> arglst) {
		if (arglst.isEmpty()) {
			System.err.println("One or more parameters expected.");
			System.exit(1);
		}
		return arglst.remove(0);
	}

	static public List<List<String>> readQueries(File query) {
		System.out.println("Reading queries...");
		Map<Integer, List<String>> queries = new HashMap<Integer, List<String>>();

		StringBuffer queryString = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(query));
			String currentline = null;
			while ((currentline = reader.readLine()) != null) {
				// skip comments
				if (currentline.startsWith("#") || currentline.isEmpty())
					continue;
				// store a finished query string
				if (currentline.toLowerCase().startsWith("query")) {
					String[] parts = currentline.split(" ");
					String querynum = parts[1].substring(0, parts[1].length() - 1);
					int num = Integer.parseInt(querynum);
					int idx = num - 1;
					if (queries.get(idx) == null)
						queries.put(idx, new ArrayList<String>());
					queries.get(idx).add(queryString.toString());
					queryString = new StringBuffer();
					continue;
				}
				// build up the query string
				queryString.append(currentline.trim() + "\n");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.print("Query file not found.\n");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<Integer> qnum = queries.keySet();
		List<Integer> sort_qnum = new ArrayList<Integer>();
		sort_qnum.addAll(qnum);
		Collections.sort(sort_qnum);
		List<List<String>> q_lst = new ArrayList<List<String>>();
		for (int n : sort_qnum) {
			q_lst.add(queries.get(n));
		}
		return q_lst;
	}

	/**
	 * 
	 */
	// public void run(String stats_path, String queries_path,String
	// rel_err_path,Boolean relative_err,Config config) {
//	public void run(String stats_path, String queries_path,String rel_err_path,Boolean relative_err) {
	/*
	 * public void run() SimpleDateFormat df = new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); List<Long> querytime = new
	 * ArrayList<Long>(); List<Long> firstresulttime = new ArrayList<Long>();
	 * List<Long> resultcount = new ArrayList<Long>(); List<Integer> sacount = new
	 * ArrayList<Integer>(); //Config.relative_error = relative_err;
	 * //if(Config.relative_error) //
	 * RelativeError.pre_initfileRelativeError(rel_err_path+"\\"); //Statistics
	 * config = new Statistics(new File(stats_path)); Symbol property =
	 * Symbol.create("config"); ARQ.getContext().set(property, config);
	 * System.out.println(
	 * "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$111111111111111111$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"
	 * ); ParaEng.register();
	 * 
	 * if(rest) { //leave some time for starting recording try { Thread.sleep(5000);
	 * } catch (InterruptedException e1) { e1.printStackTrace(); } }
	 * 
	 * Model model = ModelFactory.createDefaultModel(); Dataset dataset =
	 * DatasetFactory.create(model); //queriesFile = new
	 * File(queries_path+"/queries.txt"); // List<List<String>> queries =
	 * readQueries(queries_path); //initiate timer and results count for each query
	 * // for(int i = 0;i<queries.size();i++) { querytime.add(0L);
	 * firstresulttime.add(0L); resultcount.add(0L); sacount.add(0); // }
	 * 
	 * 
	 * 
	 * List<Integer> _ignor = Arrays.asList(ignor);
	 * //System.out.println("Starting at: "+df.format(new Date()));
	 * //System.out.println("Query " + qnum +" is under testing."); //get the number
	 * of each queries
	 * 
	 * long maxResult = 0; String maxQuery = null;
	 * 
	 * //List<String> qs = queries.get(i);//get all instances of query i+1 //for(int
	 * j = 0;j<qs.size();j++) {// // String q = qs.get(j);
	 * 
	 * // if(Config.relative_error){ // initializer(queries_path,"q"+queries_path);
	 * // } //System.out.println(q);//print each query, could be annoying //long
	 * begin = Sy // stem.nanoTime(); // System.out.println(q); long startTime =
	 * System.currentTimeMillis();
	 * 
	 * QueryExecution qe = QueryExecutionFactory.create(queries_path,dataset);
	 * ResultSet results = qe.execSelect(); long count = 0; while(results.hasNext())
	 * { QuerySolution querySolution = results.next(); //System.out.println(q);
	 * count++; } System.out.println(" Query Execution time (ms): "
	 * +(System.currentTimeMillis()-startTime));
	 * 
	 * System.out.println(" Total Triple-Pattern-wise selected sources: "
	 * +tpSources); System.out.println(" Source selection time (ms): "
	 * +sourceSelectionTime);
	 * 
	 * //RelativeError.writeplan("q"+(i+1)); RelativeError.calculateSimilarity();
	 * //long end = System.nanoTime(); if(count != 0) { // if(count>maxResult) { //
	 * maxResult = count; // maxQuesry = q; // }
	 * System.out.println("Results# "+count); }
	 * 
	 * System.out.println("Results# "+count); qe.close(); //record query time and
	 * results count // firstresulttime.set(i,
	 * firstresulttime.get(i)+(firsttime-begin)); // querytime.set(i,
	 * querytime.get(i)+(end-begin)); // resultcount.set(i,
	 * resultcount.get(i)+count);
	 * 
	 * //10 seconds break between two runs of the same query if(rest) { //let the
	 * end-points rest for 10sec try { // wait(); Thread.sleep(5000); } catch
	 * (InterruptedException e) { e.printStackTrace(); } } }
	 * 
	 * // System.out.println("Query "+qnum); //
	 * System.out.println("finishes at: "+df.format(new Date())); // int rounds =
	 * queries.get(i).size();
	 * 
	 * //20 minutes break between two different queries // if(rest) { // try { //
	 * Thread.sleep(600000); // } catch (InterruptedException e) { //
	 * e.printStackTrace(); // } // System.gc(); // try { // Thread.sleep(600000);
	 * // } catch (InterruptedException e) { // e.printStackTrace(); // } // } // }
	 * // System.out.println("Test is finished."); // for(int
	 * i=0;i<querytime.size();i++) { // int rounds = queries.get(i).size(); //
	 * if(rounds == 0) // continue; //
	 * System.out.println("Query "+(i+1)+": QT: "+querytime.get(i).doubleValue()/
	 * 1000000000/rounds +"s; " + "Average results count " +
	 * resultcount.get(i).doubleValue()/rounds); // } //}
	 * 
	 * // System.out.println("Query "+qnum); //
	 * System.out.println("finishes at: "+df.format(new Date())); // int rounds =
	 * queries.get(i).size();
	 * 
	 * //20 minutes break between two different queries // if(rest) { // try { //
	 * Thread.sleep(600000); // } catch (InterruptedException e) { //
	 * e.printStackTrace(); // } // System.gc(); // try { // Thread.sleep(600000);
	 * // } catch (InterruptedException e) { // e.printStackTrace(); // } // }
	 * 
	 * // System.out.println("Test is finished."); // for(int
	 * i=0;i<querytime.size();i++) { // int rounds = queries.get(i).size(); //
	 * if(rounds == 0) // continue; //
	 * System.out.println("Query "+(i+1)+": QT: "+querytime.get(i).doubleValue()/
	 * 1000000000/rounds +"s; " + "Average results count " +
	 * resultcount.get(i).doubleValue()/rounds); // }
	 * 
	 * /*private void initializer(String query, String type){
	 * RelativeError.real_resultSize.clear(); RelativeError.est_resultSize.clear();
	 * RelativeError.real_cardinality.clear();
	 * RelativeError.estimated_cardinality.clear(); RelativeError.count=0;
	 * RelativeError.plan_num =0; RelativeError.initializeFile(query,type);
	 * RelativeError.initialize(); }
	 */

	/*
	 * public void run(String stats_path, String queries_path,QuetzalConfig config)
	 * { System.out.
	 * println("-------------------------------------run has started---------------------------------------------------"
	 * ); SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 * List<Long> querytime = new ArrayList<Long>(); List<Long> firstresulttime =
	 * new ArrayList<Long>(); List<Long> resultcount = new ArrayList<Long>();
	 * List<Integer> sacount = new ArrayList<Integer>(); // Config.relative_error =
	 * relative_err; //if(Config.relative_error) //
	 * RelativeError.pre_initfileRelativeError(rel_err_path+"\\"); // Statistics
	 * config = new Statistics(new File(stats_path)); Symbol property =
	 * Symbol.create("config"); ARQ.getContext().set(property, config);
	 * ParaEng.register(); if(rest) { //leave some time for starting recording try {
	 * Thread.sleep(5000); } catch (InterruptedException e1) { e1.printStackTrace();
	 * } }
	 * 
	 * Model model = ModelFactory.createDefaultModel(); Dataset dataset =
	 * DatasetFactory.create(model); // queriesFile = new
	 * File(queries_path+"/queries.txt"); List<List<String>> queries =
	 * readQueries(queriesFile); //initiate timer and results count for each query
	 * for(int i = 0;i<queries.size();i++) { querytime.add(0L);
	 * firstresulttime.add(0L); resultcount.add(0L); sacount.add(0); }
	 * 
	 * List<Integer> _ignor = Arrays.asList(ignor); for(int i = 0;i <
	 * queries.size();i++) { int qnum = i+1; if(_ignor.contains(qnum)) continue;
	 * //System.out.println("Starting at: "+df.format(new Date()));
	 * //System.out.println("Query " + qnum +" is under testing."); //get the number
	 * of each queries
	 * 
	 * long maxResult = 0; String maxQuery = null;
	 * 
	 * List<String> qs = queries.get(i);//get all instances of query i+1 for(int j =
	 * 0;j<qs.size();j++) {// String q = qs.get(j);
	 * 
	 * // if(Config.relative_error){ // initializer(q,"q"+(i+1)); // }
	 * //System.out.println(q);//print each query, could be annoying //long begin =
	 * Sy // stem.nanoTime(); // System.out.println(q); long startTime =
	 * System.currentTimeMillis();
	 * 
	 * QueryExecution qe = QueryExecutionFactory.create(q,dataset); ResultSet
	 * results = qe.execSelect(); long count = 0; while(results.hasNext()) {
	 * QuerySolution querySolution = results.next(); //System.out.println(q);
	 * count++; } System.out.println(" Query Execution time (ms): "
	 * +(System.currentTimeMillis()-startTime));
	 * 
	 * System.out.println(" Total Triple-Pattern-wise selected sources: "
	 * +tpSources); System.out.println(" Source selection time (ms): "
	 * +sourceSelectionTime);
	 * 
	 * RelativeError.writeplan("q"+(i+1)); RelativeError.calculateSimilarity();
	 * //long end = System.nanoTime(); if(count != 0) { // if(count>maxResult) { //
	 * maxResult = count; // maxQuery = q; // }
	 * System.out.println("Results# "+count); }
	 * 
	 * System.out.println("Results# "+count); qe.close(); //record query time and
	 * results count // firstresulttime.set(i,
	 * firstresulttime.get(i)+(firsttime-begin)); // querytime.set(i,
	 * querytime.get(i)+(end-begin)); // resultcount.set(i,
	 * resultcount.get(i)+count);
	 * 
	 * //10 seconds break between two runs of the same query if(rest) { //let the
	 * end-points rest for 10sec try { // wait(); Thread.sleep(5000); } catch
	 * (InterruptedException e) { e.printStackTrace(); } } }
	 * 
	 * // System.out.println("Query "+qnum); //
	 * System.out.println("finishes at: "+df.format(new Date())); // int rounds =
	 * queries.get(i).size();
	 * 
	 * //20 minutes break between two different queries // if(rest) { // try { //
	 * Thread.sleep(600000); // } catch (InterruptedException e) { //
	 * e.printStackTrace(); // } // System.gc(); // try { // Thread.sleep(600000);
	 * // } catch (InterruptedException e) { // e.printStackTrace(); // } // } } //
	 * System.out.println("Test is finished."); // for(int
	 * i=0;i<querytime.size();i++) { // int rounds = queries.get(i).size(); //
	 * if(rounds == 0) // continue; //
	 * System.out.println("Query "+(i+1)+": QT: "+querytime.get(i).doubleValue()/
	 * 1000000000/rounds +"s; " + "Average results count " +
	 * resultcount.get(i).doubleValue()/rounds); // } } private void
	 * initializer(String query, String type){
	 * RelativeError.real_resultSize.clear(); RelativeError.est_resultSize.clear();
	 * RelativeError.real_cardinality.clear();
	 * RelativeError.estimated_cardinality.clear(); RelativeError.count=0;
	 * RelativeError.plan_num =0; RelativeError.initializeFile(query,type);
	 * RelativeError.initialize(); }
	 */
}
