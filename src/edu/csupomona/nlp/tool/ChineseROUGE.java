/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool;

import edu.csupomona.nlp.util.ChineseSeg;
import edu.csupomona.nlp.util.NGram;
import edu.csupomona.nlp.util.Stopword;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Xing
 */
public class ChineseROUGE extends EnglishROUGE {
    
    ChineseSeg cs;
    
    public ChineseROUGE() {
        cs = new ChineseSeg();
        
        try {
            Stopword.init("stopwords_c.txt");
        } catch (IOException ex) {
            Logger.getLogger(ChineseROUGE.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected HashMap<String, Integer> createNGram(Integer N, String path) 
            throws FileNotFoundException, IOException {
        // construct a n-gram processor
        NGram ngram = new NGram(N); 
        
        // read model file and create model n-gram maps
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        HashMap<String, Integer> map = new HashMap<>();
        String text;
        List<String> words;
        while ((text = br.readLine()) != null) {           
            // tokenize input text
            words = cs.toMMsegWords(text, "C");
            
            // remove stopwords
            words = Stopword.rmStopword(words);
            
            // update n-gram
            ngram.updateNGram(map, words);
        }
        
        return map;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChineseROUGE rouge = new ChineseROUGE();
        try {
            String peerPath = "data/chinese/peer/";
            String modelPath = "data/chinese/model/";
            File[] files = new File(peerPath).listFiles();
            for (File file : files) {
                Result score = rouge.computeNGramScore(
                                peerPath + file.getName(),
                                modelPath, "A", 1, 0.8);
                System.out.println(file.getName() + " : " + 
                        score.getGramScoreP() + ", " +
                        score.getGramScoreF());
            }
        } catch (IOException ex) {
            Logger.getLogger(EnglishROUGE.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}
