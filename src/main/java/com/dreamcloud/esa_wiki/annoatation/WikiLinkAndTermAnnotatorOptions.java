package com.dreamcloud.esa_wiki.annoatation;

import org.apache.lucene.analysis.Analyzer;

public class WikiLinkAndTermAnnotatorOptions {
    public int minimumIncomingLinks = 0;
    public int minimumOutgoingLinks = 0;
    public int minimumTerms = 0;
    public Analyzer analyzer;
    public int maximumTermCount;
}
