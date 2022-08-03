package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {

		HashMap<String,Occurrence> potato = new HashMap<String, Occurrence>(1000,2.0f);
		
		Scanner sc = new Scanner(new File(docFile));
		//find a word
		while(sc.hasNext())
		{
			
			String word=sc.next();
			word=getKeyword(word); //gets wurd
			
			if(word==null) //if the word is null then continues
			{
				continue;
			}
			
			if(potato.containsKey(word)==false) //if that key exists in the table check if the doc file matches
			{
				Occurrence occur = new Occurrence(docFile,1);//new occur
				potato.put(word, occur);//inserts wurd
			}
			
			else
			{
				Occurrence occur=potato.get(word);
				occur.frequency++;//increments wurd occur
				potato.put(word,occur);
			}
			
		}
		

		return potato;//returns map
		
	}

	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		for(String i: kws.keySet())
		{
			ArrayList<Occurrence> test;
			if(keywordsIndex.containsKey(i)==false) //if its empty
			{
				test = new ArrayList<Occurrence>();		
			}			
			else{
				test = keywordsIndex.get(i);
			}
				
			Occurrence recent_table_occurrence=kws.get(i);
			test.add(recent_table_occurrence);
			insertLastOccurrence(test);
			keywordsIndex.put(i, test);
	}
}


	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		String temp=word;
		int len=word.length();
		boolean isValid=true;
		int firstFoundPunctuation=0;

		if (len==0 || (len > 0 && Character.isLetter(word.charAt(0))==false))
			return null;

		for(int i=1;i<len;i++) { 
			if(!Character.isLetter(word.charAt(i))) {
				if (firstFoundPunctuation == 0) {
					firstFoundPunctuation = i;
				}
			}
			else {
				if (firstFoundPunctuation > 0 && i > firstFoundPunctuation) {
					isValid = false;
					break;
				}
			}
		}

		temp = temp.toLowerCase();
		
		if (isValid) {
			if (firstFoundPunctuation > 0) {
				temp = temp.substring(0,firstFoundPunctuation);

			}

			if(noiseWords.contains(temp)) {
				temp = null;
			}
		}
		else
			temp = null; 
		

		return temp;
	}

	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		/** COMPLETE THIS METHOD **/
		ArrayList<Integer> middle = new ArrayList<Integer>(); 

		if(occs.size()==1){
			return null;
		}
		Occurrence temp = occs.get(occs.size()-1);
		int hi = 0; //fist index contains highest number
		int low = occs.size()-1;
		int mid=0;
		int midFrequency;

		while(hi <= low){
			mid = (hi + low)/2;
			midFrequency = occs.get(mid).frequency;

			if(midFrequency == temp.frequency){
				middle.add(mid);
				break;
			}
			if(midFrequency < temp.frequency){
				low = mid-1;
				middle.add(mid);
			}
			if(midFrequency > temp.frequency){
				hi = mid + 1;
				middle.add(mid);
				mid++;
			}
		}
		occs.add(mid, temp);
		return middle;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> topFive= new ArrayList<String>(5);
		
		//if both words exists
		int i=0;
		int j =0;
		ArrayList<Occurrence> kw1List = keywordsIndex.get(kw1);
		ArrayList<Occurrence> kw2List = keywordsIndex.get(kw2);
		
		
		while( i< kw1List.size() && j < kw2List.size() && topFive.size() < 5) {
			//System.out.println("***?????  i = " + i + " j = " + j);

			if (kw1List.get(i).document == kw2List.get(j).document) {
				if (kw1List.get(i).frequency >= kw2List.get(j).frequency) {
					if(!topFive.contains(kw1List.get(i).document))
						topFive.add(kw1List.get(i).document);
				}
				else {
					if(!topFive.contains(kw2List.get(j).document))
						topFive.add(kw2List.get(j).document);
				}
				i++;
				j++;
			} 
			else {
				if (kw1List.get(i).frequency >= kw2List.get(j).frequency) {
					if(!topFive.contains(kw1List.get(i).document)) {
						topFive.add(kw1List.get(i).document);
					}
					i++;
				}
				else {
					if(!topFive.contains(kw2List.get(j).document)) {
						topFive.add(kw2List.get(j).document);
					}
					j++;
				}
			}
		}

		//System.out.println("***both  i = " + i + " j = " + j);

		while( i< kw1List.size() && topFive.size() < 5 ) {
			if(!topFive.contains(kw1List.get(i).document)) {
				topFive.add(kw1List.get(i).document);
			}
			i++;
		}

		while( j < kw2List.size() && topFive.size() < 5 ) {
			if(!topFive.contains(kw2List.get(j).document)) {
				topFive.add(kw2List.get(j).document);
			}
			j++;
		}

		return topFive;
	}
}