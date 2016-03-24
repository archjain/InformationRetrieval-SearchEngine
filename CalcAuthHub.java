package edu.asu.irs13;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

// this class mainly calculates authority and hub count of top k docs retrieved
//using tf-idf cosine similarity
//to see the results please run the code from PageRankMain.java
public class CalcAuthHub {
	// rootSet gets values from CosineSimilarity.java
	static List<Integer> rootSet = new ArrayList<Integer>();
	static List<Integer> baseSet = new ArrayList<Integer>();
	static HashMap<Integer, Double> authMap = new HashMap<Integer, Double>();
	static HashMap<Integer, Double> hubMap = new HashMap<Integer, Double>();

	public void createLinks() {
		long startTime = System.nanoTime();
		startTime = startTime / 1000000;
		// System.out.println(startTime);

		int size = rootSet.size();
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link = new LinkAnalysis();

		// building baseSet
		baseSet.addAll(rootSet);
		for (int i = 0; i < rootSet.size(); i++) {
			int[] linkDocs = link.getLinks(rootSet.get(i));
			int[] citDocs = link.getCitations(rootSet.get(i));
			for (int l = 0; l < linkDocs.length; l++) {
				if (!baseSet.contains(linkDocs[l]))
					baseSet.add(linkDocs[l]);
			}
			for (int c = 0; c < citDocs.length; c++) {
				if (!baseSet.contains(citDocs[c]))
					baseSet.add(citDocs[c]);
			}
		}
		//
		long endTime6 = System.nanoTime();
		endTime6 = endTime6 / 1000000;
		long totTime6 = endTime6 - startTime;
	//	System.out.println("preparing baseSet time: " + totTime6);

		int baseSize = baseSet.size();
		int[][] adjacency = new int[baseSize][baseSize];
		int[][] adjacencyTranspose = new int[baseSize][baseSize];

		long startTime4 = System.nanoTime();
		startTime4 = startTime4 / 1000000;
		// calculating adjecency matrix
		for (int i = 0; i < baseSize; i++) {
			int[] docLinks = link.getLinks(baseSet.get(i));
			for (int j = 0; j < baseSize; j++) {
				for (int k = 0; k < docLinks.length; k++) {
					if (docLinks[k] == baseSet.get(j))
						adjacency[i][j] = 1;
				}

			}

		}

		// calculating transpose
		for (int i = 0; i < baseSize; i++) {
			for (int j = 0; j < baseSize; j++) {
				adjacencyTranspose[j][i] = adjacency[i][j];
			}

		}

		long endTime4 = System.nanoTime();
		endTime4 = endTime4 / 1000000;
		long totTime4 = endTime4 - startTime4;
	//	System.out.println("matrix calculation time: " + totTime4);
		double[][] authInit = new double[baseSize][1];
		double[][] hubInit = new double[baseSize][1];
		double[][] authNext = new double[baseSize][1];
		double[][] hubNext = new double[baseSize][1];

		// initializing hub and auth values to 1.0
		for (int i = 0; i < baseSize; i++) {
			authInit[i][0] = 1.0;
			hubInit[i][0] = 1.0;
		}
		int iterations = 1;

		// operations to calculate auth and hub values start here
		while (true) {
			long startTime2 = System.nanoTime();
			startTime2 = startTime2 / 1000000;
			double authNorm = 0;
			double hubNorm = 0;
			for (int i = 0; i < baseSize; i++) {
				int sum = 0;
				int sum2 = 0;
				for (int j = 0; j < 1; j++) {

					// calculating auth and hub values in each iteration
					for (int k = 0; k < baseSize; k++) {
						authNext[i][j] += adjacencyTranspose[i][k]
								* hubInit[k][j];
						hubNext[i][j] += adjacency[i][k] * authInit[k][j];
						authNorm += authNext[i][j] * authNext[i][j];
						hubNorm += hubNext[i][j] * hubNext[i][j];

					}

				}
			}

			// Normalizing auth values
			authNorm = Math.sqrt(authNorm);
			for (int i = 0; i < baseSize; i++) {
				authNext[i][0] = authNext[i][0] / authNorm;
				// System.out.println("autNext"+authNext[i][0]);
			}

			// Normalizing hub values
			hubNorm = Math.sqrt(hubNorm);
			for (int i = 0; i < baseSize; i++) {
				hubNext[i][0] = hubNext[i][0] / hubNorm;
			}

			double hubConverge = 0.0;
			double authConverge = 0;

			// calculating convergence value
			for (int i = 0; i < baseSize; i++) {

				for (int j = 0; j < 1; j++) {

					hubConverge += (hubNext[i][j] - hubInit[i][j])
							* (hubNext[i][j] - hubInit[i][j]);
					authConverge += (authNext[i][j] - authInit[i][j])
							* (authNext[i][j] - authInit[i][j]);
				}

			}

			// checking if convergence is done
			if (hubConverge < 0.0000001 && authConverge < 0.0000001) {
				System.out
						.println("No of iterations to converge " + iterations);
				long endTime2 = System.nanoTime();
				endTime2 = endTime2 / 1000000;
				long totTime2 = endTime2 - startTime2;
				// System.out.println("convergence time: " +totTime2);
				break;

			}
			// to count the total iterations before convergence
			iterations++;

			// assigning new values to old values
			for (int i = 0; i < baseSize; i++) {
				authInit[i][0] = authNext[i][0];
				hubInit[i][0] = hubNext[i][0];
			}
			long endTime3 = System.nanoTime();
			endTime3 = endTime3 / 1000000;
			long totTime3 = endTime3 - startTime2;
			// System.out.println("each iteration time: " +totTime3);
		}

		// Making map for auth values with docid
		long startTime5 = System.nanoTime();
		startTime5 = startTime5 / 1000000;

		for (int i = 0; i < baseSize; i++) {
			authMap.put(baseSet.get(i), authNext[i][0]);
		}
		// Making map for hub values with docid
		for (int i = 0; i < baseSize; i++) {
			hubMap.put(baseSet.get(i), hubNext[i][0]);
		}
		long endTime5 = System.nanoTime();
		endTime5 = endTime5 / 1000000;
		long totTime5 = endTime5 - startTime5;
		// System.out.println("preapring map :" + totTime5);
		// sorting and displaying the values
		System.out.println("**********");
		System.out.println("hub docs");
		sortAndDisplay(hubMap);
		System.out.println("**********");
		System.out.println("authorities docs");
		sortAndDisplay(authMap);

		// clearing the auth and hub maps
		baseSet.clear();
		rootSet.clear();
		authMap.clear();
		hubMap.clear();

		long endTime = System.nanoTime();
		endTime = endTime / 1000000;
		long totTime = endTime - startTime;

		// System.out.println("total algorithm time : " + totTime);
	}

	// method to sort and display docid on the basis of their auth or hub values
	public void sortAndDisplay(HashMap map) {
		long startTime = System.nanoTime();
		startTime = startTime / 1000000;
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return -((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it2 = list.iterator(); it2.hasNext();) {
			Map.Entry entry = (Map.Entry) it2.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		Iterator sorted = sortedHashMap.entrySet().iterator();
		int i = 0;

		while (sorted.hasNext() && i < 10) {
			i++;
			Map.Entry pair = (Entry) sorted.next();
			// uncomment this to see the hub values also with doc ids
			System.out.println(pair.getKey() + " ");// +pair.getValue());
		}
		long endTime = System.nanoTime();
		endTime = endTime / 1000000;
		long totTime = endTime - startTime;
		// System.out.println("sort and display time : "+totTime);
	}
}
