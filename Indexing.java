// Author : Archit Jain

package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class Indexing {
	static HashMap<Integer, HashMap> docMap = new HashMap<Integer, HashMap>();
	HashMap<String, Integer> termMap;
	static HashMap<Integer, Double> DiMap = new HashMap<Integer, Double>();

	
	//creates the hashmap of hashmaps, where every document is linked with all its terms and their frequency
	//docmap has keys as document id and values as termMap, which has terms and their frequency
	public void fwdIndex() {
		IndexReader r;
		try {

			r = IndexReader.open(FSDirectory.open(new File("index")));
			TermEnum t = r.terms();
			while (t.next()) {
				if (t.term().field().equals("contents")) {
					Term term = t.term();
					TermDocs td = r.termDocs(term);
					while (td.next()) {

						if (docMap.containsKey(td.doc()))
							termMap = docMap.get(td.doc());
						else
							termMap = new HashMap<String, Integer>();
						termMap.put(term.text(), td.freq());

						docMap.put(td.doc(), termMap);

					}
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

	
	
	// calculates Norm di as per the weight type
	// idf values are included if wieght type is tf-idf
	public void calcDi(String weightType) {
		long startTime=System.nanoTime();
		startTime=startTime/1000000;
		System.out.println("Start calculating Document norm:" + startTime);
        Iterator it1 = docMap.entrySet().iterator();

		while (it1.hasNext()) {

			Map.Entry pair1 = (Map.Entry) it1.next();

			HashMap wordMap = docMap.get(pair1.getKey());
			Iterator it = wordMap.entrySet().iterator();
			double di = 0.0;
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();

				if (weightType.equals("tf-idf")) {
					//idf of the term is multiplied in case of if-idf weight type
					double tempdi = Double.parseDouble(pair.getValue()
							.toString())
							* CosineSimilarity.idfMap.get(pair.getKey());
					di += tempdi * tempdi;
				} else {
					di += Math.pow(
							Double.parseDouble(pair.getValue().toString()), 2);

				}
				it.remove();
			}

			di = Math.sqrt(di);
			//DiMap contains docid and its calculated normdi
			DiMap.put((int) pair1.getKey(), di);

		}
		 long endTime=System.nanoTime();
			endTime=endTime/1000000;
			System.out.println("End time :" + endTime);
			long totTime=endTime-startTime;
			System.out.println("Norm Calculation Time : "  + totTime);
	}

	
	
}
