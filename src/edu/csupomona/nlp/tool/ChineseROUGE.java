/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool;

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
    private ChineseSeg cs;
    
    /**
     * Construct an ChineseROUGE class with default values
     */
    public ChineseROUGE() {
        this.segMode = "C";
        this.cs = new ChineseSeg();
        
        this.metric = "N";
        this.scoreMode = "A";
        this.alpha = 0.8;
        
        this.rmStopword = false;
        this.useStemmer = false;
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
    public boolean isRmStopword() {
        return rmStopword;
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
     * @return          Processed string text
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
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChineseROUGE rouge = new ChineseROUGE();
        rouge.setRmStopword(true);
        
        try {
            String peerPath = "data/chinese/peer/";
            String modelPath = "data/chinese/model/";
            File[] files = new File(peerPath).listFiles();
            for (File file : files) {
                Result score = rouge.computeNGramScore(1, 0, 0,
                                peerPath + file.getName(),
                                modelPath);
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
