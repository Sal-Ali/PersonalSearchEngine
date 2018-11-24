package pse;
import java.io.*;
import java.util.*;


/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class PersonalSearchEngine {

    /**
     * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
     * an array list of all occurrences of the keyword in documents. The array list is maintained in 
     * descending order of frequencies.
     */
    HashMap<String,ArrayList<Occurrence>> keywordsIndex;

    /**
     * The hash set of all noise words.
     */
    HashSet<String> noiseWords;

    /**
     * Creates the keyWordsIndex and noiseWords hash tables.
     */
    public PersonalSearchEngine() {
        keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
        noiseWords = new HashSet<String>(100,2.0f);
    }

    /**
     * Scans a document, and loads all keywords found into a hash table of keyword occurrences
     * in the document. Uses the getKeyWord method to separate keywords from other words.
     *
     * @param docFile Name of the document file to be scanned and loaded
     * @return Hash table of keywords in the given document, each associated with an pse.Occurrence object
     * @throws FileNotFoundException If the document file is not found on disk
     */
    public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile)
            throws FileNotFoundException {

        if (docFile == null ){
            throw new FileNotFoundException("File missing");
        }
        if (docFile.length() == 0){
            throw new FileNotFoundException("No docs declared");
        }

        HashMap<String, Occurrence> map = new HashMap<String, Occurrence>(500);

        // reads docFile
        Scanner sc = new Scanner(new FileReader(docFile));
        String wordCandidate;
        while(sc.hasNext()){
            wordCandidate = sc.next();
            String revision = getKeyword(wordCandidate);
            if (revision != null && revision.length() > 0) {
                if (map.containsKey(revision)) {
                    map.get(revision).frequency++;
                } else {
                    map.put(revision, new Occurrence(docFile, 1));
                }
            }
        }


        return map;
    }

    /**
     * Merges the keywords for a single document into the master keywordsIndex
     * hash table. For each keyword, its pse.Occurrence in the current document
     * must be inserted in the correct place (according to descending order of
     * frequency) in the same keyword's pse.Occurrence list in the master hash table.
     * This is done by calling the insertLastOccurrence method.
     *
     * @param kws Keywords hash table for a document
     */
    public void mergeKeywords(HashMap<String,Occurrence> kws) {

        if(!kws.isEmpty()) {


            for(String key : kws.keySet()){
                if (keywordsIndex.containsKey(key)) {
                    keywordsIndex.get(key).add(kws.get(key));
                    //System.out.println(key);
                    insertLastOccurrence(keywordsIndex.get(key));
                } else {

                    if(keywordsIndex.isEmpty()){
                        ArrayList<Occurrence> b1 = new ArrayList<Occurrence>();
                        b1.add(kws.get(key));
                        keywordsIndex.put(key, b1);
                    }
                    ArrayList<Occurrence> al = new ArrayList<Occurrence>();
                    al.add(kws.get(key));
                    keywordsIndex.put(key, al);

                }
            }
        }
    }

    /**
     * Given a word, returns it as a keyword if it passes the keyword test,
     * otherwise returns null. A keyword is any word that, after being stripped of any
     * trailing punctuation, consists only of alphabetic letters, and is not
     * a noise word. All words are treated in a case-INsensitive manner.
     *
     * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
     *
     * @param word Candidate word
     * @return Keyword (word without trailing punctuation, LOWER CASE)
     */

    private static boolean suffixMatch (String word){

        if (word.endsWith(".") ||
                word.endsWith(",") ||
                word.endsWith("?") ||
                word.endsWith(":") ||
                word.endsWith(";") ||
                word.endsWith("!")) { return  true;}
        return false;
    }

    public String getKeyword(String word) {


        word = word.toLowerCase();

        while(word.length() > 0 &&
                !(Character.isLetter(word.charAt(word.length()-1))) &&
                !(Character.isDigit(word.charAt(word.length()-1)))){
            if (suffixMatch(word)){
                word = word.substring(0, word.length()-1);
            }

            else {
                return null;
            }

        }
        // checks if noise word
        if (noiseWords.contains(word)){
            return null;
        }
        // checks remaining letters for other characters
        int n = 0;
        while ( n < word.length()){
            if (!(Character.isLetter(word.charAt(n)))) {
                return null;
            }
            n++;
        }

        if (noiseWords.contains(word)){
            return null;
        }

        return word;


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
    private static ArrayList<Integer> finalOccurrence(ArrayList<Occurrence> occs){
        ArrayList<Integer>index = new ArrayList<Integer>(occs.size());
        int high = 0;
        int low = occs.size()-2;
        int lastO = occs.get(occs.size()-1).frequency;
        int termination = 0;
// binary search
        while(low >= high && termination == 0){
            int mid = (low + high)/2;
            if(lastO == occs.get(mid).frequency){
                index.add(mid);
                termination = 1;
            }
            else if(lastO > occs.get(mid).frequency){
                low = mid-1;
                index.add(mid);
            }
            else{
                high = mid+1;
                index.add(mid);
            }
        }
        Occurrence last=occs.get(occs.size()-1);
        occs.add(index.get(index.size()-1),last);
        return index;
    }


    public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {


        return finalOccurrence(occs);


    }

    /**
     * This method indexes all keywords found in all the input documents. When this
     * method is done, the keywordsIndex hash table will be filled with all keywords,
     * each of which is associated with an array list of pse.Occurrence objects, arranged
     * in decreasing frequencies of occurrence.
     *
     * @param docsFile Name of file that has a list of all the document file names, one name per line
     * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
     * @throws FileNotFoundException If there is a problem locating any of the input files on disk
     */
    public void makeIndex(String docsFile, String noiseWordsFile) throws FileNotFoundException
    {
        // load noise words to hash table
        Scanner sc = new Scanner(new File(noiseWordsFile));
        OCRManager ocr = new OCRManager();

        while (sc.hasNext()) {
            String word = sc.next();
            noiseWords.add(word);
        }

        // index all keywords
        sc = new Scanner(new File(docsFile));
        while (sc.hasNext()) {
            String docFile = sc.next();
            String type = docFile.substring( docFile.length() - 4, docFile.length());
            if(!type.equals(".txt")) {
                String filename = docFile.substring(0, docFile.length() -4) + ".txt";
                if(type.equals(".pdf")){
//                    File gradeList = new File(docFile);
//                    if (!gradeList.exists()) {
//                        System.out.println(docFile);
//                        throw new FileNotFoundException("Failed to find file: " +
//                                gradeList.getAbsolutePath());
//                    } // testing code
                        ocr.manage(ocr.convertPDFtoImages(docFile), filename);
                }
                else if (type.equals(".png") || type.equals("jpg") || type.equals(".img") || type.equals(".bmp")) {
                    ocr.manage(docFile);
                }
                else {
                    throw new FileNotFoundException(type);
                }
                docFile = filename;
            }
            // ocr nd pdf functionality use load, then pdf handler
            HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
            mergeKeywords(kws);
        }
        sc.close();
    }

    /**
     * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
     * document. Result set is arranged in descending order of document frequencies. (Note that a
     * matching document will only appear once in the result.) Ties in frequency values are broken
     * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
     * also with the same frequency f1, then doc1 will take precedence over doc2 in the result. 
     * The result set is limited to 5 entries. If there are no matches at all, result is null.
     *
     * @param kw1 First keyword
     * @param kw1 Second keyword
     * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
     *         frequencies. The result size is limited to 5 documents. If there are no matches, returns null.
     */

    private ArrayList<String> topSearch (String kw1, String kw2){
        if(keywordsIndex.get(kw1) == null && keywordsIndex.get(kw2) == null){
            return null;
        }
        HashMap<String, Double> compareMap = new HashMap<String, Double>(500);
        if(keywordsIndex.get(kw1) != null){
            for(Occurrence occ : keywordsIndex.get(kw1)){
                if(!compareMap.containsKey(occ.document)){
                    compareMap.put(occ.document, (occ.frequency + 0.5) );
                }
                if(compareMap.containsKey(occ.document) && ((occ.frequency + 0.5) > compareMap.get(occ.document))){
                    compareMap.put(occ.document, (occ.frequency + 0.5) );
                }
            }
        }
        if(keywordsIndex.get(kw2) != null){
            for(Occurrence occ : keywordsIndex.get(kw2)){
                if(!compareMap.containsKey(occ.document)){
                    compareMap.put(occ.document, ( (double)occ.frequency ) );
                }
                if(compareMap.containsKey(occ.document) && (( (double )occ.frequency) > compareMap.get(occ.document))){
                    compareMap.put(occ.document, ((double )occ.frequency) );
                }
            }
        }


        ArrayList<String> result = new ArrayList<String>(10);
        TreeMap<Double, ArrayList<String>> compareTree = new TreeMap<Double, ArrayList<String>>();

        ArrayList<String> listOfKeys = new ArrayList<String>(compareMap.keySet());
        for(String key : listOfKeys){

            if(!compareTree.containsKey(compareMap.get(key))){
                ArrayList<String> list = new ArrayList<String>();
                list.add(key);
                compareTree.put(compareMap.get(key), list);


            }else{
                ArrayList<String> listOfFiles = compareTree.get(compareMap.get(key));
                listOfFiles.add(key);
            }
        }


@Deprecated
@SuppressWarnings("deprecation")
        Map<Double, ArrayList<String>> compareTree2 = compareTree.descendingMap();


        ArrayList<Double> listOfKeys2 = new ArrayList<Double>(compareTree2.keySet());
        int count = 0;
        outerloop:
        for(Double key: listOfKeys2){

            ArrayList<String> l = compareTree.get(key);
            for(String s: l){
                count++;
                // System.out.println(s + ": " + key);
                result.add(s);

                if(count == 5){
                    break outerloop;
                }
            }

        }



        return result;
    }

    public ArrayList<String> top5search(String kw1, String kw2) {


        return topSearch(kw1, kw2);

    }
}