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
		CosineSimilarity obj = new CosineSimilarity();
		obj.calcIdf();
		String lowestIdfTerm = "";
		Iterator it = CosineSimilarity.idfMap.entrySet().iterator();

		// This commented part of the code is just to find the term with lowest
		// idf
		
		// double min=1.0;
		// while (it.hasNext()) {
		// Map.Entry pair = (Entry) it.next();
		// if(min>Double.parseDouble(pair.getValue().toString())){
		// lowestIdfTerm = pair.getKey().toString();
		// / min=Double.parseDouble(pair.getValue().toString());
		// }

		// }
		// System.out.println(lowestIdfTerm +" : : " + min);

		
		
		Indexing index = new Indexing();
		index.fwdIndex();

		index.calcDi("tf"); //uncomment if tf calculation is required
	//	index.calcDi("tf-idf"); // comment if tf calculation is required

		// type the query
		Scanner sc = new Scanner(System.in);
		String query = null;
		while (!(query = sc.nextLine()).equals("exit")) {
			System.out.print("Type Query > ");
			String[] queryArray = query.split("\\s");

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
			System.out.println("Start time :" + startTime);

			 calcDotProd("tf"); //uncomment if tf calculation is required
		//	calcDotProd("tf-idf"); // comment if tf calculation is required

			obj.calCosSim();

			long endTime = System.nanoTime();
			endTime = endTime / 1000000;
			System.out.println("End time :" + endTime);
			long totTime = endTime - startTime;
			System.out.println("Time Taken=" + totTime);
			queryMap.clear();
			docDotProduct.clear();
			CosineSimilarity.SimilarityMap.clear();

		}
	}

}
