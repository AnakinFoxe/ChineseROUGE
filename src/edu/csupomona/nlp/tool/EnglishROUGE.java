/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool;

import edu.csupomona.nlp.util.MapUtil;
import edu.csupomona.nlp.util.NGram;
import edu.csupomona.nlp.util.Preprocessor;
import edu.csupomona.nlp.util.Stopword;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Xing
 */
public class EnglishROUGE {
    
    public EnglishROUGE() {
        try {
            Stopword.init("stopwords_e.txt");
        } catch (IOException ex) {
            Logger.getLogger(EnglishROUGE.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    protected class HitScore {
        int hit = 0;
        double score = 0;
    }
    
    protected HashMap<String, Integer> createNGram(Integer N, String path) 
            throws FileNotFoundException, IOException {
        // construct a n-gram processor
        NGram ngram = new NGram(N); 
        
        // read model file and create model n-gram maps
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        HashMap<String, Integer> map = new HashMap<>();
        String text;
        while ((text = br.readLine()) != null) {
            // preprocess text
            text = Preprocessor.Simple(text);
            
            // tokenize input text
            String[] words = text.split(" ");
            
            // remove stopwords
            words = Stopword.rmStopword(words);
            
            // update n-gram
            ngram.updateNGram(map, words);
        }
        
        return map;
    }
    
    protected HitScore ngramScore(HashMap<String, Integer> model_grams,
            HashMap<String, Integer> peer_grams){
        int hit = 0;    // overall hits
        int h;      // hit for a n-gram
        for (String key : model_grams.keySet()) {
            h = 0;
            if (peer_grams.containsKey(key))
                h = model_grams.get(key) >= peer_grams.get(key)?
                        peer_grams.get(key) : model_grams.get(key);
            hit += h;
        }
        
         HitScore hit_score = new HitScore();
        int sum = MapUtil.sumHashMap(model_grams);
        if (sum != 0)
            hit_score.score = (double)hit / sum;
        hit_score.hit = hit;
       
        return hit_score;
    }
    
    protected Result generateResult(HitScore hit_score, String scoreMode,
            double alpha, int model_count, int peer_count) {
        
        int totalGramHit = 0;
        int totalGramCount = 0;
        int totalGramCountP = 0;
        double gramScoreBest = -1;
        switch (scoreMode) {
            case "A":
                // average mode
                totalGramHit += hit_score.hit;
                totalGramCount += model_count;
                totalGramCountP += peer_count;
                break;
            case "B":
                // best match mode
                if (hit_score.score > gramScoreBest) {
                    gramScoreBest = hit_score.score;
                    totalGramHit = hit_score.hit;
                    totalGramCount = model_count;
                    totalGramCountP = peer_count;
                }   break;
            default:
                // default is average mode
                totalGramHit += hit_score.hit;
                totalGramCount += model_count;
                totalGramCountP += peer_count;
                break;
        }
        
        // prepare score result for return
        double gramScore = 0;
        double gramScoreP = 0;  // precision score
        double gramScoreF = 0;  // f-measure
        if (totalGramCount != 0)
            gramScore = (double)totalGramHit / totalGramCount;
        if (totalGramCountP != 0)
            gramScoreP = (double)totalGramHit / totalGramCountP;
        if ((1-alpha)*gramScoreP+alpha*gramScore > 0)
            gramScoreF = (gramScoreP*gramScore) / 
                    ((1-alpha)*gramScoreP+alpha*gramScore);
        
        return new Result(totalGramCount, totalGramHit, gramScore, 
                totalGramCountP, gramScoreP, gramScoreF);
    }
    
    public Result computeNGramScore(String modelPath, String peerPath,
            String scoreMode, Integer N, double alpha) 
            throws FileNotFoundException, IOException{

        // TODO: model could be multiple files
        // read model file and create model n-gram maps
        HashMap<String, Integer> model_grams = createNGram(N, modelPath);
        
        // read peer file and create model n-gram maps
        HashMap<String, Integer> peer_grams = createNGram(N, peerPath);
        
        HitScore hit_score = ngramScore(model_grams, peer_grams);
        
        return generateResult(hit_score, scoreMode, alpha, 
                MapUtil.sumHashMap(model_grams), 
                MapUtil.sumHashMap(peer_grams));
    }
    

    public static void main(String[] args) {
        EnglishROUGE rouge = new EnglishROUGE();
        try {
            Result score = rouge.computeNGramScore("data/english/D132.M.100.D.A",
                                "data/english/D132.M.100.D.C", "A", 1, 0.8);
            System.out.println(score.getGramScoreP() + ", " +
                               score.getGramScoreF());
        } catch (IOException ex) {
            Logger.getLogger(EnglishROUGE.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
}
