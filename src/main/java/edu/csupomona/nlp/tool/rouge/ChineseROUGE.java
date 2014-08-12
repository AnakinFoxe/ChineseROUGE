/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool.rouge;

import edu.csupomona.nlp.util.ChineseSeg;
import edu.csupomona.nlp.util.Stopword;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java implemented ROUGE for Chinese
 * @author Xing
 */
public class ChineseROUGE extends EnglishROUGE {
    // for segmentation
    private String segMode; // segmentation mode
    private final ChineseSeg cs;
    
    /**
     * Construct an ChineseROUGE class with default values
     */
    public ChineseROUGE() {
        super();
        this.segMode = "C";
        this.cs = new ChineseSeg();
    }

    public String getSegMode() {
        return segMode;
    }

    /**
     * Set the segmentation mode 
     * @param segMode   Segmentation mode.
     *                  "C": complex mode (default),
     *                  "S": simple mode,
     *                  "M": max word mode
    */
    public void setSegMode(String segMode) {
        this.segMode = segMode;
    }
    

    @Override
    public void setRmStopword(boolean rmStopword) {
        this.rmStopword = rmStopword;
        
        if (rmStopword)
            sw = new Stopword("zh_CN");
    }
    
    
    /**
     * Preprocessing seems not necessary for Chinese
     * @param text      Input string text
     * @return          The same as input
     */
    @Override
    protected String preprocess(String text) {
        return text;
    }
    
    /**
     * Tokenize the input text into Chinese words
     * @param text      Input string text
     * @return          List of words
     */
    @Override
    protected List<String> tokenize(String text) {
        try {
            return cs.toMMsegWords(text, this.segMode);
        } catch (IOException ex) {
            Logger.getLogger(ChineseROUGE.class.getName())
                    .log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }
    
    /**
     * Stemming seems not necessary for Chinese
     * @param words     List of words
     * @return          The same as input
     */
    @Override
    protected List<String> stemming(List<String> words) {
        return words;
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChineseROUGE rouge = new ChineseROUGE();
        rouge.setRmStopword(false);
        rouge.setAlpha(0.5);
        
        try {
            String peerPath = "./data/evaluation/chinese/SubSum/d132d/";
            String modelPath = "./data/evaluation/chinese/model.M.100/d132d/";
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
