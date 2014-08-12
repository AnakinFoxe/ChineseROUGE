/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.rouge;

import edu.csupomona.nlp.util.Stemmer;
import edu.csupomona.nlp.util.Stopword;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.tokenize.SimpleTokenizer;

/**
 * Java implemented ROUGE for Spanish
 * @author Xing
 */
public class SpanishROUGE extends EnglishROUGE {
    
    private final SimpleTokenizer stk;    
    
     /**
    * Construct an SpanishROUGE class with default values
    */
    public SpanishROUGE() {
        super();
        
        stk = SimpleTokenizer.INSTANCE;
    }
    

    @Override
    public void setRmStopword(boolean rmStopword) {
        this.rmStopword = rmStopword;
        
        if (rmStopword)
            sw = new Stopword("es");
    }
    
    
    @Override
    public void setUseStemmer(boolean useStemmer) {
        this.useStemmer = useStemmer;
        
        if (this.useStemmer == true)
            stem = new Stemmer("es");
    }
    
    /**
     * Preprocessing seems not necessary for Spanish
     * @param text      Input string text
     * @return          The same as input
     */
    @Override
    protected String preprocess(String text) {
        return text.toLowerCase(new Locale("es"));
    }
    
    /**
     * Tokenize the input text into Spanish words
     * @param text      Input string text
     * @return          List of words
     */
    @Override
    protected List<String> tokenize(String text) {
        return new ArrayList<>(Arrays.asList(stk.tokenize(text)));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpanishROUGE rouge = new SpanishROUGE();
        rouge.setRmStopword(false);
        rouge.setUseStemmer(false);
        rouge.setAlpha(0.5);
        rouge.setScoreMode("A");
        
        try {
            String peerPath = "./data/evaluation/spanish/SubSum/d133c/";
            String modelPath = "./data/evaluation/spanish/model.M.100/d133c/";
            File[] files = new File(peerPath).listFiles();
            for (File file : files) {
                Result score = rouge.computeNGramScore(1, 100, 0,
                                peerPath + file.getName(),
                                modelPath);
                System.out.println(file.getName() + " : " + 
                        score.getGramScore() + ", " +
                        score.getGramScoreP() + ", " +
                        score.getGramScoreF());
                
                System.out.println(score.getTotalGramCount() + ", " +
                        score.getTotalGramCountP() + ", " +
                        score.getTotalGramHit());
            }
        } catch (IOException ex) {
            Logger.getLogger(EnglishROUGE.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    
}
