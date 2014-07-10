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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java implemented ROUGE
 * http://www.berouge.com/Pages/default.aspx
 * @author Xing
 */
public class EnglishROUGE {
    protected String metric;        
    protected String scoreMode; 
    protected double alpha;         
    
    // preprocessing options
    protected boolean rmStopword;
    protected boolean useStemmer;
    protected Stopword sw;
    
    // for n-gram ROUGE
    protected Integer N;
    
    /**
    * Construct an EnglishROUGE class with default values
    */
    public EnglishROUGE() {
        this.metric = "N";
        this.scoreMode = "A";
        this.alpha = 0.8;
        
        this.rmStopword = false;
        this.useStemmer = false;
        
        this.N = 1;
    }

    public String getMetric() {
        return metric;
    }

    /**
    * Which ROUGE metric to compute
    * @param metric    ROUGE metric.
    *                   "N": n-gram (supported),
    *                   "L": longest common subsequence,
    *                   "W": weighted longest common subsequence,
    *                   "S": skip-bigram co-occurrence statistics
    * 
    */
    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getScoreMode() {
        return scoreMode;
    }

    /**
     * Set scoring mode 
     * @param scoreMode "A": average mode (default), "B" best match mode
    */
    public void setScoreMode(String scoreMode) {
        this.scoreMode = scoreMode;
    }

    public double getAlpha() {
        return alpha;
    }

    /**
     * Relative importance between recall and precision
     * @param alpha    Close to 1: recall is more important,
     *                  Close to 0: precision is more important
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public boolean isRmStopword() {
        return rmStopword;
    }

    /**
     * Remove stopwords or not
     * @param rmStopword    True: remove stopwords, False: not to remove
     */
    public void setRmStopword(boolean rmStopword) {
        this.rmStopword = rmStopword;
        
        if (rmStopword)
            sw = new Stopword("E");
    }

    public boolean isUseStemmer() {
        return useStemmer;
    }

    /**
     * Use stemmer or not
     * @param useStemmer    True: use stemmer, False: not to use
     */
    public void setUseStemmer(boolean useStemmer) {
        this.useStemmer = useStemmer;
    }

    public Integer getN() {
        return N;
    }

    /**
     * N of n-gram. 
     * @param N     1 for unigram, 2 for bigram, etc.
     */
    public void setN(Integer N) {
        this.N = N;
    }
    
    /**
    * Nested class for saving hit counts and score
    */
    private class HitScore {
        int hit = 0;
        double score = 0;
    }

    /**
     * Create a HashMap to record n-gram information of a file
     * @param path      The path of the input file
     * @return          HashMap stores n-gram information
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected HashMap<String, Integer> createNGram(String path) 
            throws FileNotFoundException, IOException {
        // construct a n-gram processor
        NGram ngram = new NGram(this.N); 
        
        // read model file and create model n-gram maps
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        HashMap<String, Integer> map = new HashMap<>();
        String text;
        while ((text = br.readLine()) != null) {
            // preprocess text
            text = Preprocessor.simple(text);
            
            // tokenize input text
            String[] words = text.split(" ");
            
            // remove stopwords
            if (this.rmStopword)
                words = sw.rmStopword(words);
            
            // TODO: stemmer
            
            // update n-gram
            ngram.updateNGram(map, words);
        }
        
        return map;
    }

    /**
     * Calculate the score for n-gram ROUGE
     * Please refer to 
     *      "Rouge: A package for automatic evaluation of summaries"
     * @param peer_grams        n-gram for model file (reference)
     * @param model_grams       n-gram for peer file (target)
     * @return                  Hit count and score
     */
    private HitScore ngramScore(HashMap<String, Integer> peer_grams,
            HashMap<String, Integer> model_grams){
        int hit = 0;    // overall hits
        int h;      // hit for a n-gram
        
        // get the overall hits
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
            hit_score.score = (double)hit / sum;    // score
        hit_score.hit = hit;
       
        return hit_score;
    }


    /**
     * Compute the n-gram ROUGE score
     * @param peerPath      The path to the peer file (include the file name) 
     * @param modelPath     The path contains model files (exclude the file names)
     * @return              Result class that contains precision, F1 scores.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Result computeNGramScore(String peerPath, String modelPath) 
            throws FileNotFoundException, IOException{
        // init variables
        int totalGramHit = 0;
        int totalGramCount = 0;
        int totalGramCountP = 0;
        double gramScoreBest = -1;
        int model_count;
        int peer_count;
        
        // read peer file and create n-gram maps
        HashMap<String, Integer> peer_grams = createNGram(peerPath);
        peer_count = MapUtil.sumHashMap(peer_grams);
        
        File[] files = new File(modelPath).listFiles(); // multiple model files
        for (File file : files) {
            // read model file and create n-gram maps
            HashMap<String, Integer> model_grams = 
                    createNGram(modelPath + file.getName());
            model_count = MapUtil.sumHashMap(model_grams);

            HitScore hit_score = ngramScore(peer_grams, model_grams);
            
            switch (this.scoreMode) {
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
    

    public static void main(String[] args) {
        EnglishROUGE rouge = new EnglishROUGE();
        rouge.setRmStopword(true);
        rouge.setN(1);
        
        
        try {
            String peerPath = "data/english/peer/";
            String modelPath = "data/english/model/";
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
