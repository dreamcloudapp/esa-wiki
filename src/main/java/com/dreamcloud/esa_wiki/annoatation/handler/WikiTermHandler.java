package com.dreamcloud.esa_wiki.annoatation.handler;

import com.dreamcloud.esa_wiki.annoatation.WikiAnnotation;
import com.dreamcloud.esa_wiki.annoatation.WikiLinkAndTermAnnotatorOptions;
import com.dreamcloud.esa_wiki.annoatation.handler.ConcurrentXmlReadingHandler;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

public class WikiTermHandler extends ConcurrentXmlReadingHandler {
    WikiLinkAndTermAnnotatorOptions options;
    protected Map<String, String> titleMap;
    protected Map<Integer, WikiAnnotation> annotations;

    public WikiTermHandler(int threadCount, int batchSize, WikiLinkAndTermAnnotatorOptions options, Map<String, String> titleMap, Map<Integer, WikiAnnotation> annotations) {
        super(threadCount, batchSize);
        this.options = options;
        this.titleMap = titleMap;
        this.annotations = annotations;
    }

    @Override
    protected Integer handleDocuments(Vector<Map<String, String>> documents) {
       try {
           boolean logged = false;
           for (Map<String, String> document: documents) {
               String title = document.get("title");
               String text = document.get("text");
               int titleId = Integer.parseInt(document.get("id"));

               if (!logged) {
                   System.out.println("term-annotated article\t[" + this.getDocsRead() + "]\t\"" + title + "\"");
                   logged = true;
               }

               //Analyze the text!
               int termCount = 0;
               try {
                   TokenStream tokens = options.analyzer.tokenStream("text", text);
                   tokens.addAttribute(CharTermAttribute.class);
                   tokens.reset();
                   while(tokens.incrementToken()) {
                       termCount++;
                   }
                   tokens.close();
               } catch (IOException e) {
                   e.printStackTrace();
                   System.exit(1);
               }
               if (termCount >= options.minimumTerms && (options.maximumTermCount == 0 || termCount <= options.maximumTermCount)) {
                   synchronized (annotations) {
                       WikiAnnotation annotation = annotations.getOrDefault(titleId, new WikiAnnotation(0, 0, 0));
                       annotation.terms = termCount;
                       annotations.put(titleId, annotation);
                   }
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
           System.exit(1);
       }
        return documents.size();
    }
}
