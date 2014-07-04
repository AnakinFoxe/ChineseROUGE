/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csupomona.nlp.tool;

import edu.csupomona.nlp.util.NGram;
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
        // TODO code application logic here
        NGram ngram = new NGram(5);
        
        HashMap<String, Integer> map = new HashMap<>();
        
        ngram.updateNGram(map, "this is good, and  (*Huh32 so it is.");
        
        for (String key : map.keySet()) {
            System.out.println(key);
        }
    }
    
    
    
}
