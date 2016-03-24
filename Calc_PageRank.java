package edu.asu.irs13;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Calc_PageRank {
	int n = 25054;
	double[] oldRank = new double[n];
	double[] newRank = new double[n];
	static HashMap<Integer, Double> rankMap = new HashMap<Integer, Double>();

	public void calcPageRank() {
		long startTime = System.nanoTime();
		startTime = startTime / 1000000;
		int count = 0;
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis link = new LinkAnalysis();
		double c = 0.8;
		// setting initial ranks to 1/n
		for (int i = 0; i < n; i++) {
			oldRank[i] = 1.0 / n;
			newRank[i] = 1.0 / n;
		}

		while (true) {
			count++;
			double sink = 0.0;
			double[] tempRank = new double[n];
			for (int i = 0; i < n; i++) {
				// get referred docs by each document in the corpus
				int[] docLinks = link.getLinks(i);
				if (docLinks.length != 0) {
					// if referred docs count !=0, divide oldrank by referred
					// docs count
					for (int j = 0; j < docLinks.length; j++) {
						tempRank[docLinks[j]] += oldRank[i] / docLinks.length;
					}
				} else {
					// otherwise, add oldrank/n to sink total
					sink += oldRank[i] / n;
				}
			}
			double max = 0.0;
			for (int i = 0; i < n; i++) {
				// calculate new rank
				newRank[i] = ((sink + tempRank[i]) * c) + (1 - c) / n;
				if (newRank[i] - oldRank[i] > max)
					max = newRank[i] - oldRank[i];
			}
			// checking the threshold
			if (max < 0.0000001)
				break;
			else {
				// if not converged
				for (int i = 0; i < n; i++) {
					oldRank[i] = newRank[i];
				}
			}

		}
		System.out.println("Total iterations: " + count);
		double m = 0.0;
		int l = -1;
		// normalizing the ranks by dividing with maximum rank value
		for (int i = 0; i < n; i++) {
			if (newRank[i] > m) {
				m = newRank[i];
				l = i;
			}
		}
		System.out.println("Highest ranked document: " + l);
		for (int i = 0; i < n; i++) {
			newRank[i] = newRank[i] / m;
		}
		// int[] links=link.getLinks(9048);
		// int[] links2=link.getCitations(9048);
		// System.out.println("links length"+links.length);
		// System.out.println("cite length"+ links2.length);
		long endTime = System.nanoTime();
		endTime = endTime / 1000000;
		long totaltime = endTime - startTime;

	//	System.out.println("Time taken to calculate page ranks : " + totaltime);
	}

	// function which combines cosine similarity and pagerank to retrive
	// relevant docs
	public void retrieveTopPages(double w) {

		// taking values from similarity map and combining it with pageranks
		Iterator it = CosineSimilarity.SimilarityMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Entry) it.next();
			int docId = Integer.parseInt(pair.getKey().toString());
			double cosSim = Double.parseDouble(pair.getValue().toString());
			double relevance = w * newRank[docId] + (1 - w) * cosSim;

			// putting the new values in rankmap
			rankMap.put(docId, relevance);
		}
		// sorting
		List list = new LinkedList(rankMap.entrySet());
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
			System.out.println(pair.getKey());
		}

	}

}
