/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool;

import edu.csupomona.nlp.util.NGram;
import edu.csupomona.nlp.util.Preprocessor;
import edu.csupomona.nlp.util.Stopword;
import java.util.HashMap;

/**
 *
 * @author Xing
 */
public class ChineseROUGE {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HashMap<String, Integer> map = new HashMap<>();
         
        // init stopword
        Stopword.init("stopwords.txt");
        
        // input text
        String text = "this is good, and  (*Huh32 so it is.";
        
        // preprocess input text
        String procText = Preprocessor.Simple(text);
        
        // tokenize input text
        String[] words = procText.split(" ");
        
        // remove stopword
//        words = Stopword.rmStopword(words);
        
        // get N-gram
        NGram ngram = new NGram(2);
        ngram.updateNGram(map, words);
        
        for (String key : map.keySet()) {
            System.out.println(key);
        }
    }
    
    
    
}
