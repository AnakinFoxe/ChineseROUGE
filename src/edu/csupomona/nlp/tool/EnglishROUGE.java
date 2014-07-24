/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool;

import edu.csupomona.nlp.util.MapUtil;
import edu.csupomona.nlp.util.NGram;
import edu.csupomona.nlp.util.Preprocessor;
import edu.csupomona.nlp.util.Stemmer;
import edu.csupomona.nlp.util.Stopword;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    protected Stemmer stem;
    
    
    /**
    * Construct an EnglishROUGE class with default values
    */
    public EnglishROUGE() {
        this.metric = "N";
        this.scoreMode = "A";
        this.alpha = 0.8;
        
        this.rmStopword = false;
        this.useStemmer = false;
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
            sw = new Stopword("en");
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
        
        if (this.useStemmer == true)
            stem = new Stemmer("en");
    }
    
    /**
    * Nested class for saving hit counts and score
    */
    private class HitScore {
        int hit = 0;
        double score = 0;
    }
    
    
    /**
     * Preprocess the input text to remove/replace undesired symbols
     * @param text      Input string text
     * @return          Processed string text
     */
    protected String preprocess(String text) {
        return Preprocessor.simple(text).toLowerCase(new Locale("en"));
    }
    
    /**
     * Tokenize the input text into words
     * @param text      Input string text
     * @return          List of words
     */
    protected List<String> tokenize(String text) {
        return new ArrayList<>(Arrays.asList(text.split(" ")));
    }
    
    /**
     * Stem input words using Snowball stemmer
     * @param words     List of words
     * @return          Stemmed list of words
     */
    protected List<String> stemming(List<String> words) {
        return stem.stemWords(words);
    }
     
    /**
     * Read file and extract words from the file restricted by the 
     * length limit or byte limit.
     * @param lengthLimit   Limit in number of words to be extracted
     * @param byteLimit     Limit in number of bytes to be extracted
     * @param path          Path to the file
     * @return              List of words extracted from the file
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private List<String> readText(Integer lengthLimit, Integer byteLimit, 
            String path) 
            throws FileNotFoundException, IOException {
        List<String> tokenizedText = new ArrayList<>();
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String text;
        Integer usedLengthLimit = 0;
        Integer usedByteLimit = 0;
        while ((text = br.readLine()) != null) {
            // preprocess text
            text = preprocess(text);
            
            // tokenize input text
            List<String> words = tokenize(text);
            
            // remove stopwords
            if (this.rmStopword)
                words = sw.rmStopword(words);
            
            // TODO: stemmer
            if (this.useStemmer)
                words = stemming(words);
            
            // length or byte limit
            if ((lengthLimit == 0) && (byteLimit == 0)) {
                // no limit control, add everything
                tokenizedText.addAll(words);
            }
            else if (lengthLimit != 0) {
                // priority goes length limit control
                if ((usedLengthLimit + words.size()) <= lengthLimit) 
                    tokenizedText.addAll(words);
                else {
                    // reached limit
                    tokenizedText.addAll(words.subList(0, 
                            lengthLimit-usedLengthLimit));
                    
                    break;
                }
                    
                usedLengthLimit += words.size();
                
            } else if (byteLimit != 0) {
                // byte limit control
                for (String word : words) {
                    if ((usedByteLimit + word.length()) <= byteLimit) 
                        tokenizedText.add(word);
                    else {
                        // reached limit
                        // NOTE: current implementation may result in 
                        // word truncation
                        tokenizedText.add(word.substring(0, 
                                byteLimit-usedByteLimit));
                        break;
                    }
                    
                    usedByteLimit += word.length();
                }
            }
        }
        
        return tokenizedText;
    }

    /**
     * Create a HashMap to record n-gram information of a file
     * @param N         N of n-gram
     * @param words     List of words from a file
     * @return          HashMap stores n-gram information
     */
    private HashMap<String, Integer> createNGram(Integer N, 
            List<String> words) {
        HashMap<String, Integer> map = new HashMap<>();
        
        // update n-gram info to the hashmap
        NGram.updateNGram(N, map, words);
        
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
     * @param N             N of n-gram
     * @param lengthLimit   Length limit for ROUGE (in words)
     * @param byteLimit     Byte limit for ROUGE (in bytes)
     * @param peerPath      The path to the peer file (include the file name) 
     * @param modelPath     The path contains model files (exclude the file names)
     * @return              Result class that contains precision, F1 scores.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Result computeNGramScore(Integer N, 
            Integer lengthLimit, Integer byteLimit,
            String peerPath, String modelPath) 
            throws FileNotFoundException, IOException{
        // init variables
        int totalGramHit = 0;
        int totalGramCount = 0;
        int totalGramCountP = 0;
        double gramScoreBest = -1;
        int model_count;
        int peer_count;
        
        // read peer file and create n-gram maps
        List<String> words = readText(lengthLimit, byteLimit, peerPath);
        HashMap<String, Integer> peer_grams = createNGram(N, words);
        peer_count = MapUtil.sumHashMap(peer_grams);
        
        File[] files = new File(modelPath).listFiles(); // multiple model files
        for (File file : files) {
            // read model file and create n-gram maps
            words = readText(lengthLimit, byteLimit,modelPath + file.getName());
            HashMap<String, Integer> model_grams = createNGram(N, words);
            model_count = MapUtil.sumHashMap(model_grams);

            HitScore hit_score = ngramScore(peer_grams, model_grams);
            
            switch (this.scoreMode) {
                case "B":
                    // best match mode
                    if (hit_score.score > gramScoreBest) {
                        gramScoreBest = hit_score.score;
                        totalGramHit = hit_score.hit;
                        totalGramCount = model_count;
                        totalGramCountP = peer_count;
                    }   break;
                case "A":
                    // average mode
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
        rouge.setRmStopword(false);
        rouge.setUseStemmer(false);
        rouge.setAlpha(0.5);
        rouge.setScoreMode("A");
        
        
        try {
            String peerPath = "./data/evaluation/english/SubSum/d132d/";
            String modelPath = "./data/evaluation/english/model.M.100/d132d/";
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
