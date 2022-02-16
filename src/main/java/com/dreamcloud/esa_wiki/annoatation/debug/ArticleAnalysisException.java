package com.dreamcloud.esa_wiki.annoatation.debug;

import org.xml.sax.SAXException;

public class ArticleAnalysisException extends SAXException {
    public ArticleAnalysis article;

    public ArticleAnalysisException(String message, ArticleAnalysis article) {
        super(message);
        this.article = article;
    }
}
