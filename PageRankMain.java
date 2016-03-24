// Author : Archit Jain

package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;

public class PageRankMain {

	static HashMap<String, Integer> queryMap = new HashMap<String, Integer>();
	static HashMap<Integer, Double> docDotProduct = new HashMap<Integer, Double>();

	public static void calcDotProd(String weightType) {
		try {

			System.out.println("Weight type - " + weightType);
			IndexReader r = IndexReader.open(FSDirectory
					.open(new File("index")));
			Iterator it = queryMap.entrySet().iterator();

			// Iterate through queryMap to get the documents where terms of the
			// query are present
			while (it.hasNext()) {
				Map.Entry pair = (Entry) it.next();
				Term term = new Term("contents", pair.getKey().toString());
				TermDocs tdocs = r.termDocs(term);
				// System.out.println("no of docs" + r.docFreq(term));
				// for all the documents found with each term
				while (tdocs.next()) {

					// get the count of term in query and assign to product
					double product = queryMap.get(term.text());

					// for tf-tdf
					if (weightType.equals("tf-idf")) {
						// multiply with tdf
						product = product * tdocs.freq()
								* CosineSimilarity.idfMap.get(term.text());

					}

					else {
						product = product * tdocs.freq();
					}

					if (docDotProduct.containsKey(tdocs.doc())) {
						double newProduct = product
								+ docDotProduct.get(tdocs.doc());
						docDotProduct.put(tdocs.doc(), newProduct);

					} else {
						docDotProduct.put(tdocs.doc(), product);
					}
					// System.out.println("*****");
				}

			}
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out
				.println("Type 1 for auth/hub and 2 for pagerank, Default is 1");
		System.out.println("1. auth hub");
		System.out.println("2. pagerank");
		int ch = 1;
		ch = sc.nextInt();
		sc.nextLine();
		System.out
				.println("wait!! doing initial calculations---idf map calculations");
		CosineSimilarity obj = new CosineSimilarity();
		obj.calcIdf();
		String lowestIdfTerm = "";
		Iterator it = CosineSimilarity.idfMap.entrySet().iterator();
		Indexing index = new Indexing();
		index.fwdIndex();
		index.calcDi("tf-idf");
		Calc_PageRank pr = new Calc_PageRank();
		if (ch == 2) {

			System.out.println("calclating page ranks ");
			pr.calcPageRank();
			System.out.println("calculation done");
		}

		// index.calcDi("tf"); //uncomment if tf calculation is required
		// comment if tf calculation is required

		// type the query

		String query = null;

		while (true) {
			System.out.print("Query (type exit to discontinue)--> ");
			query = sc.nextLine();
			if (query.equals("exit"))
				break;
			// System.out.print("Type Query > ");
			String[] queryArray = query.split("\\s");

			double weight = 0.4;
			if (ch == 2) {
				System.out
						.println("Enter weight for page rank(should be between 0 to 1, default is 0.4)--> ");
				weight = sc.nextDouble();
				sc.nextLine();
			}
			for (int i = 0; i < queryArray.length; i++) {
				// put into map the words in query and its count within query
				if (queryMap.containsKey(queryArray[i])) {
					int newValue = queryMap.get(queryArray[i]) + 1;
					queryMap.put(queryArray[i], newValue);
				} else {
					queryMap.put(queryArray[i], 1);
				}
			}
			long startTime = System.nanoTime();
			startTime = startTime / 1000000;

			// calcDotProd("tf"); //uncomment if tf calculation is required
			calcDotProd("tf-idf"); // comment if tf calculation is required

			obj.calCosSim();

			CalcAuthHub auth = new CalcAuthHub();
			if (ch == 1)
				auth.createLinks();

			if (ch == 2)
				pr.retrieveTopPages(weight);

			queryMap.clear();
			docDotProduct.clear();
			CosineSimilarity.SimilarityMap.clear();
			Calc_PageRank.rankMap.clear();

		}
	}

}
