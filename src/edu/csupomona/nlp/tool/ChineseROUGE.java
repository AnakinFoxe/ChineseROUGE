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
        
        this.N = 1;
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
     * Create a HashMap to record n-gram information of a file
     * @param path       The path of the input file
     * @return           HashMap stores n-gram information
     * @throws FileNotFoundException
     * @throws IOException 
    */
    @Override
    protected HashMap<String, Integer> createNGram(String path) 
            throws FileNotFoundException, IOException {
        // construct a n-gram processor
        NGram ngram = new NGram(this.N); 
        
        // read model file and create model n-gram maps
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        HashMap<String, Integer> map = new HashMap<>();
        String text;
        List<String> words;
        while ((text = br.readLine()) != null) {           
            // tokenize input text
            words = cs.toMMsegWords(text, this.segMode);
            
            // remove stopwords
            if (this.rmStopword)
                words = sw.rmStopword(words);
            
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
        rouge.setRmStopword(true);
        
        try {
            String peerPath = "data/chinese/peer/";
            String modelPath = "data/chinese/model/";
            File[] files = new File(peerPath).listFiles();
            for (File file : files) {
                Result score = rouge.computeNGramScore(
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
