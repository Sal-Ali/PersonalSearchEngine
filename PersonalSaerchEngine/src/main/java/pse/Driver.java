package pse;

import java.io.*;
import java.util.*;


/**
 * Main driver for a personal search engine (PSE) throughout a directory, multiple directories can be processed in
 * parallel through the use of multiple instances of PSE.
 *
 * Serialization not yet implemented, but will be introduced as a future improvement as an exercise
 */


public class Driver {
    public static void main(String args[]) throws IOException {
        String docsFile = "docs.txt"; // directory indicator, may source files with data mining in the future
        String noiseWords = "noisewords.txt"; // common words to de-noise sample



        PersonalSearchEngine lse = new PersonalSearchEngine();
        lse.makeIndex(docsFile, noiseWords);
        String kw1 = "die";
        String kw2 = "world";

        ArrayList<String> results = lse.top5search(kw1, kw2);
        if(results == null){
            System.out.println("null");
        }

        for(int i = 0; i < results.size(); i++){
            System.out.println(results.get(i));
        } // lse actual code


//        OCRManager c = new OCRManager();
//        c.convertPDFtoImages("multipage.pdf"); // testing code


//        try{
//        File file = new File(".");
//        for(String fileNames : file.list()) System.out.println(fileNames);
//    } catch (NullPointerException e){
//            System.out.println("lol");
//        } // this is the file checking code testing code

}
}
