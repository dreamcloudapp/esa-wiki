package com.dreamcloud.esa_wiki.annoatation.debug;

import com.dreamcloud.esa_score.analysis.CollectionInfo;
import com.dreamcloud.esa_score.score.TfIdfScore;

import java.text.DecimalFormat;
import java.util.Vector;

public class ArticleAnalysis {
    public int id;
    public String title;
    public int linkCount;
    public int termCount;
    public String sourceText;
    public String cleanText;
    public String analyzedText;
    public Vector<TfIdfScore> tfIdfScores = new Vector<>();

    public void display(CollectionInfo info) {
        DecimalFormat format = new DecimalFormat("#0.##");

        System.out.println("Article Analysis");
        System.out.println("----------------------------------------");
        System.out.println("id:\t" + id);
        System.out.println("title:\t" + title);
        System.out.println("links:\t" + linkCount);
        System.out.println("terms:\t" + termCount);
        System.out.println("------");
        System.out.println("<source text>");
        System.out.println(sourceText);
        System.out.println("</source text>");
        System.out.println("------");
        System.out.println("<clean text>");
        System.out.println(cleanText);
        System.out.println("</clean text>");
        System.out.println("------");
        System.out.println("<analyzed terms>");
        System.out.println(analyzedText);
        System.out.println("</analyzed terms>");
        System.out.println("------");
        System.out.println("<scores>");
        for(TfIdfScore score: tfIdfScores) {
            System.out.println(score.getTerm() + ":\t" + format.format(score.getScore()) + "\t(" +  (analyzedText.split(score.getTerm(), -1).length - 1) + " / "  + info.getDocumentFrequency(score.getTerm()) + ")");
        }
        System.out.println("</scores>");
        System.out.println("------");
        System.out.println("----------------------------------------");
    }
}
