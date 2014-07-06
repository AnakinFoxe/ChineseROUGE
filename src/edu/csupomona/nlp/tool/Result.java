/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool;

/**
 *
 * @author Xing
 */
public class Result {
    
    private int totalGramCount;     // total number of n-grams in models
    private int totalGramHit;
    private double gramScore;
    private int totalGramCountP;    // total number of n-grams in peers
    private double gramScoreP;      // precision score
    private double gramScoreF;      // f1-measure score

    public Result(int totalGramCount, int totalGramHit, double gramScore, 
            int totalGramCountP, double gramScoreP, double gramScoreF) {
        this.totalGramCount = totalGramCount;
        this.totalGramHit = totalGramHit;
        this.gramScore = gramScore;
        this.totalGramCountP = totalGramCountP;
        this.gramScoreP = gramScoreP;
        this.gramScoreF = gramScoreF;
    }

    public int getTotalGramCount() {
        return totalGramCount;
    }

    public void setTotalGramCount(int totalGramCount) {
        this.totalGramCount = totalGramCount;
    }

    public int getTotalGramHit() {
        return totalGramHit;
    }

    public void setTotalGramHit(int totalGramHit) {
        this.totalGramHit = totalGramHit;
    }

    public double getGramScore() {
        return gramScore;
    }

    public void setGramScore(double gramScore) {
        this.gramScore = gramScore;
    }

    public int getTotalGramCountP() {
        return totalGramCountP;
    }

    public void setTotalGramCountP(int totalGramCountP) {
        this.totalGramCountP = totalGramCountP;
    }

    public double getGramScoreP() {
        return gramScoreP;
    }

    public void setGramScoreP(double gramScoreP) {
        this.gramScoreP = gramScoreP;
    }

    public double getGramScoreF() {
        return gramScoreF;
    }

    public void setGramScoreF(double gramScoreF) {
        this.gramScoreF = gramScoreF;
    }
    
    
    
}
