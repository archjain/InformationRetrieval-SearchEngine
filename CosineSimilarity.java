// Author : Archit Jain

package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class CosineSimilarity {
	static HashMap<Integer, Double> SimilarityMap = new HashMap<Integer, Double>();
	static HashMap<String, Double> idfMap = new HashMap<String, Double>();
	// function to calculate idf of each term and store it in tdfMap
	public void calcIdf() {
		IndexReader r;
		try {
			r = IndexReader.open(FSDirectory.open(new File("index")));

			TermEnum t = r.terms();
			
			//for all the terms in all docs
			while (t.next()) {
				double n = 0;
				Term te = new Term("contents", t.term().text());
				n = r.docFreq(te);
				double totalDocs = r.maxDoc();
				double tdf = totalDocs / n;
				//taking log
				double logTdf = Math.log(tdf);

				idfMap.put(t.term().text(), logTdf);
			}

		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// function to calculate similarity using tf weights only
	public void calCosSim() {
		double qnorm = 0.0;
		double dnorm = 0.0;
		double cos = 0.0;
		double dotprod = 0.0;

		Iterator qit = PageRankMain.queryMap.entrySet().iterator();
		
		//calculating query norm
		while (qit.hasNext()) {
			Map.Entry pair = (Entry) qit.next();
			qnorm += Double.parseDouble(pair.getValue().toString())
					* Double.parseDouble(pair.getValue().toString());
		}
		qnorm = Math.sqrt(qnorm);
		Iterator it = PageRankMain.docDotProduct.entrySet().iterator();
		
		//calculating similarity 
		while (it.hasNext()) {
			Map.Entry pair = (Entry) it.next();
			//retrieves the dot prodiuct from map
			dotprod = Double.parseDouble(pair.getValue().toString());
			dnorm = Indexing.DiMap.get(pair.getKey());
			double sim = dotprod / (qnorm * dnorm);
			//storing in similarity map
			SimilarityMap.put((Integer) pair.getKey(), sim);

		}
	//	System.out.println("Total Docs found : " + SimilarityMap.size());

		//sorting
		
		long startTime = System.nanoTime();
		startTime = startTime / 1000000;
	//	System.out.println("Start time :" + startTime);
		List list = new LinkedList(SimilarityMap.entrySet());
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
		long endTime = System.nanoTime();
		endTime = endTime / 1000000;
		//System.out.println("End time :" + endTime);
		long totTime = endTime - startTime;
		//System.out.println("Sorting time: " + totTime);
		Iterator sorted = sortedHashMap.entrySet().iterator();
		int i = 0;

		while (sorted.hasNext() && i < 10) {
			i++;
			Map.Entry pair = (Entry) sorted.next();
			//System.out.println(pair.getKey());// + pair.getValue());
			
			//adding top 10 docs to rootset
            CalcAuthHub.rootSet.add(Integer.parseInt(pair.getKey().toString())); 
		}
		System.out.println("******");

	}
}
