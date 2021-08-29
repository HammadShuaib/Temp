package com.fluidops.fedx.trunk.parallel.engine.error;

import com.hp.hpl.jena.graph.Node;
import com.fluidops.fedx.trunk.config.Config;
import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.stream.engine.util.HypTriple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RelativeError {

	public static String q_name = "q";
	public static String aksw_schema = "http://aksw.org/schema/";
	public static String plan = "<http://aksw.org/res/lhd-" + q_name + "-plan>";
	public static int join_number = 1;
	public static String plan_join = "<http://aksw.org/res/lhd-" + q_name + "-plan-join#" + join_number + ">";
	public static String plan_join_tp = "<http://aksw.org/res/lhd-" + q_name + "-plan-join#" + join_number + "-tp";
	public static String queryTxt = "<http://aksw.org/res/query#" + q_name + ">  <" + aksw_schema + "queryPlan> ";
	public static String literal_type = "^^<http://www.w3.org/2001/XMLSchema#double>";
	public static Map<Vertex, Double> est_resultSize = new HashMap<Vertex, Double>();
	public static Map<Map<Vertex, Double>, Integer> estimated_cardinality = new HashMap<Map<Vertex, Double>, Integer>();
	public static int plan_num = 0;
	public static Map<Vertex, Double> real_resultSize = new HashMap<Vertex, Double>();
	public static Map<Map<Vertex, Double>, Integer> real_cardinality = new HashMap<Map<Vertex, Double>, Integer>();
	public static Config config = null;

	public static void setQueryTxt(String qrytext) {
		queryTxt.replace("b1", qrytext);
	}

	// for calculating the Similarity constant
	static HashMap<HashMap<String, Double>, Integer> estimated_values = new HashMap<HashMap<String, Double>, Integer>();
	static HashMap<HashMap<String, Double>, Integer> real_values = new HashMap<HashMap<String, Double>, Integer>();
	public static ArrayList<JoinCard> joinCards;

	public static void initialize() {
		/*joinCards = new ArrayList<JoinCard>();
		est_resultSize = new HashMap<Vertex, Double>();
		estimated_cardinality = new HashMap<Map<Vertex, Double>, Integer>();
		plan_num = 0;
		real_resultSize = new HashMap<Vertex, Double>();
		real_cardinality = new HashMap<Map<Vertex, Double>, Integer>();
		Config config = null;
		estimated_values = new HashMap<HashMap<String, Double>, Integer>();
		real_values = new HashMap<HashMap<String, Double>, Integer>();
*/	}

	public static ArrayList<ArrayList> plan_joinCards = new ArrayList<ArrayList>();

	public static void add_plan_joincards() {
		plan_joinCards.add(new ArrayList(joinCards));
		joinCards.clear();
	}

	public static void addJoinCard(Vertex vertex, Double real_Card, HypTriple hypTriple, TripleCard tripleCard) {

/*		for (int i = 0; i < joinCards.size(); i++) {
			if (vertex.equals(joinCards.get(i).getVertex())) {
				joinCards.get(i).setJoin_number(joinCards.size());
				joinCards.get(i).setEstimated_Card(est_resultSize.get(vertex));
				joinCards.get(i).setReal_Card(real_Card);
				joinCards.get(i).addTripleWithCard(hypTriple, tripleCard);
				return;
			}
		}*/
		// if(vertex)
	//	JoinCard joinCard = new JoinCard();
	//	joinCard.setVertex(vertex);
	//	joinCard.setJoin_number(joinCards.size());
	//	joinCard.setEstimated_Card(est_resultSize.get(vertex));
//		joinCard.setReal_Card(real_Card);
//		joinCard.addTripleWithCard(hypTriple, tripleCard);
//		joinCards.add(joinCard);
	}

	public static float getRelativeError(float estimatedCord, float realCord) {
		return (estimatedCord - realCord) / realCord;
	}

	/*
	 * For each BGP save the estimated cardinality, and read cardinalities
	 */
	public static void addEstimatedCardBGP(Map<Vertex, Double> v) {

	//	Map<Vertex, Double> vertics = new HashMap<>(real_resultSize);
	//	estimated_cardinality.put(new HashMap<>(v), plan_num);
	//	real_cardinality.put(vertics, plan_num);
	//	plan_num++;
	}

	/*
	 * Print and save the real and estimated cardinality in arrays for calculating
	 * the cosine similarities and relative errors
	 */
	public static void print() {
		/*int cnt = 0;
		Iterator real_iter = (((HashMap) RelativeError.real_cardinality).keySet()).iterator();

		if (Config.debug) {
			System.out.println("Real Cardinality");
		}
		while (real_iter.hasNext()) {
			HashMap iterator = (HashMap) real_iter.next();
			HashMap<String, Double> v2 = new HashMap<String, Double>();
			for (Object v : iterator.keySet()) {
				Node key = ((Vertex) v).getNode();
//                if(!key.isConcrete()) {
				String value = iterator.get(v).toString();
				if (Config.debug) {
					System.out.println(key + ":" + value);
				}
				if (key.isConcrete()) {
					v2.put(key.toString(), Double.parseDouble(value));
				} else
					v2.put((key.isVariable()) ? key.getName() : key.getURI(), Double.parseDouble(value));
//                }
			}
			real_values.put(v2, cnt);
			cnt++;

		}
		cnt = 0;
		Iterator est_iter = (((HashMap) RelativeError.estimated_cardinality).keySet()).iterator();
		if (Config.debug) {
			System.out.println("Estimated Cardinality");
		}

		while (est_iter.hasNext()) {
			HashMap iterator = (HashMap) est_iter.next();
			HashMap<String, Double> v1 = new HashMap<String, Double>();
			for (Object v : iterator.keySet()) {
				Node key = ((Vertex) v).getNode();
//                if(!key.isConcrete()) {
				String value = iterator.get(v).toString();
				if (Config.debug) {
					System.out.println(key + ":" + value);
				}
				if (key.isConcrete()) {
					v1.put(key.toString(), Double.parseDouble(value));
				} else
					v1.put((key.isVariable()) ? key.getName() : key.getURI(), Double.parseDouble(value));
//              }
			}
			estimated_values.put(v1, cnt);
			cnt++;
		}*/

	}

	/*
	 * This function is about writing the query plan and cardinalities to file
	 */
	public static void writeplan(String type) {
	/*	RelativeError.q_name = type;

//        queryTxt =  "<http://aksw.org/res/query#"+ q_name +">  <"+aksw_schema+"queryPlan> ";
		WriteErrorData.writeQueryPlan(type);

		String write_str = "";
		int count = 0;

		for (int k = 0; k < plan_joinCards.size(); k++) {
			joinCards.clear();
			joinCards.addAll(plan_joinCards.get(k));
//            plan = "<http://aksw.org/res/lhd-"+ q_name +"-plan"+(k+1)+">";
			write_str += queryTxt + plan + " .\n";
			for (int i = 0; i < joinCards.size(); i++) {
				WriteErrorData.writeJoinInfoToFile(joinCards.get(i).getEstimated_Card(),
						joinCards.get(i).getReal_Card());
				HashMap<HypTriple, TripleCard> tripleCards = joinCards.get(i).getTripleCards();
				Iterator iterator = tripleCards.values().iterator();
				int j = 1;
//            if(iterator!=null)
				while (iterator.hasNext()) {
					String tp = plan_join_tp + j + ">";
					Object object = iterator.next();
					if (object != null) {
						WriteErrorData.writeJoinTPInfoToFile(((TripleCard) object).estimated_card,
								((TripleCard) object).real_Card, j);
					}
					j++;

				}
				join_number++;
			}
		}
		plan_joinCards.clear();
		// writeQueryPlan(write_str);
	*/}

	/*
	 * Calculating the similarity constant between the real and estimated values
	 */
	public static void calculateSimilarity() {
		/*Iterator estv_iter = estimated_values.keySet().iterator();
		Iterator realv_iter = real_values.keySet().iterator();
		HashMap<String, Double> current_real = new HashMap<>();
		HashMap<String, Double> current_est = new HashMap<>();
		for (int i = 0; i < plan_num; i++) {
			HashMap<String, Double> est = new HashMap<String, Double>();
			/// needs to be fixed what if more than 2 BGPs?
			if (estv_iter.hasNext()) {
				current_est.clear();
				current_est.putAll((HashMap) estv_iter.next());
				est.putAll(current_est);
			} else {
				est.putAll(current_est);
			}
			HashMap<String, Double> real = new HashMap<String, Double>();
			if (realv_iter.hasNext()) {
				current_real.clear();
				current_real.putAll((HashMap) realv_iter.next());
				real.putAll(current_real);
			} else {
				real.putAll(current_real);
			}

			ArrayList<Double> est_array = new ArrayList<Double>();
			ArrayList<Double> real_array = new ArrayList<Double>();
			Iterator real_iter = real.keySet().iterator();
			if (real.size() <= est.size())
				for (int j = 0; j < real.size(); j++) {
					String key1 = real_iter.next().toString();
					Iterator est_iter = est.keySet().iterator();
					for (int k = 0; k < est.size(); k++) {
						String key2 = est_iter.next().toString();
						if (key1.equals(key2)) {
							est_array.add(est.get(key2));
							real_array.add(real.get(key1));
						}

					}
				}
			SimilarityConstants.estimatedValues.addAll(est_array);
			SimilarityConstants.realValues.addAll(real_array);

		}
		double similarity = SimilarityConstants.calculateAngleBetweenVector();
		System.out.println("Similarity Angle" + ":" + similarity);
		WriteErrorData.calculateSimilarity(similarity);
		SimilarityConstants.realValues.clear();
		SimilarityConstants.estimatedValues.clear();
*/	}

	public static void pre_initfileRelativeError(String rel) {
		WriteErrorData.pre_initfileRelativeError(rel);
	}

	public static int count = 0;

	public static void initializeFile(String query, String type) {

		WriteErrorData.initializeFile(query, type);
	}
//    public static void relativeErrorJoinCard(String joinType, List<CardinalityVisitor.JoinCard> estimatedCord, int real_cord){
//        String errText2="";
//        for (int i=0;i<estimatedCord.size();i++) {
//
//            errText2 = "\n-------------------\n"+joinType+" Real Cardinality: " + real_cord +
//                    "\nJoin Estimated: " + estimatedCord.get(i).nd.card +
//                    "\nJoin Relative Error: " + RelativeError.getRelativeError(estimatedCord.get(i).nd.card, real_cord) +
//                    "\n-------------------\n";
//            SimilarityConstants.realValues.add(real_cord);
//            SimilarityConstants.estimatedValues.add(estimatedCord.get(i).nd.card);
//        }
//        try {
//            writeToFile(errText2);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    private static void writeToFile(String result) throws IOException {
//        BufferedWriter writer = new BufferedWriter(new FileWriter("RelativeError.txt", true));
//        writer.append(result);
//        writer.close();
//    }

	// Not used yet
//    public static void initializeWriter(String query){
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter("RelativeError.txt"));
//            writer.append("\tRelative Errors File \n====================================\n\t\nQuery Plan\n\n====================================\n");
//            writer.append("\n" + query + "\n");
//            writer.append("\n====================================\nData\n");
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public static void writeQueryPlan(String queryPlan){
//        String planText="";
//        if(count==0)
//            planText =queryTxt +"\""+ plan;
//        count++;
//
//        planText = queryPlan;
//        try {
//            writeToFile(planText);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
