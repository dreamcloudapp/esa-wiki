package com.dreamcloud.esa_wiki.annoatation.debug;

import org.xml.sax.SAXException;

public class ArticleFoundException extends SAXException {
    public DebugArticle article;

    public ArticleFoundException(String message, DebugArticle article) {
        super(message);
        this.article = article;
    }
}
