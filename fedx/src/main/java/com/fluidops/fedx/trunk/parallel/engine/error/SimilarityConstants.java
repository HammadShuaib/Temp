package com.fluidops.fedx.trunk.parallel.engine.error;

import java.util.ArrayList;

public class SimilarityConstants {
	public static ArrayList<Double> estimatedValues = new ArrayList<>();
	public static ArrayList<Double> realValues = new ArrayList<>();

	public static double calculateAngleBetweenVector() {
double result = 1;
		/*		double result = 0;
		long ab = 0;
		double a_norm = 0;
		double b_norm = 0;
		for (int i = 0; i < estimatedValues.size(); i++) {
			ab += estimatedValues.get(i) * realValues.get(i);
			a_norm += Math.pow(realValues.get(i), 2);
			b_norm += Math.pow(estimatedValues.get(i), 2);
		}
//        System.out.println(realValues);
//        System.out.println(estimatedValues);
//        System.out.println(ab);
		a_norm = Math.sqrt(a_norm);
		b_norm = Math.sqrt(b_norm);
		if (a_norm * b_norm == 0) {
			return 0.0;
		}
		result = ab / (a_norm * b_norm);
//        System.out.println(a_norm);
//        System.out.println(b_norm);
*/
		return Math.round(result * 10000000000D) / 10000000000D;
	}
}
