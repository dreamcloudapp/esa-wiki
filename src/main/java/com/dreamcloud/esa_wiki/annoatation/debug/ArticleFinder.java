package com.dreamcloud.esa_wiki.annoatation.debug;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dreamcloud.esa_core.xml.BZipFileTools;
import com.dreamcloud.esa_core.xml.XmlReadingHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Finds an article from a Wikimedia dump file.
 */
public class ArticleFinder extends XmlReadingHandler {
    File wikimediaDump;
    String searchTitle;
    int searchIndex = -1;

    private final SAXParserFactory saxFactory;

    public ArticleFinder(File wikimediaDump) {
        this.wikimediaDump = wikimediaDump;
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public DebugArticle find(String articleTitle) {
        this.searchTitle = articleTitle.toLowerCase();
        try {
            reset();
            SAXParser saxParser = saxFactory.newSAXParser();
            Reader reader = BZipFileTools.getFileReader(wikimediaDump);
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");
            saxParser.parse(is, this);
        }
        catch (ArticleFoundException e) {
            return e.article;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void handleDocument(Map<String, String> xmlFields) throws ArticleFoundException {
        String title = xmlFields.get("title");
        if (title.toLowerCase().equals(searchTitle) || this.getDocsRead() == searchIndex) {
            DebugArticle article = new DebugArticle();
            article.index = this.getDocsRead();
            article.title = title;
            article.text = xmlFields.get("text");
            throw new ArticleFoundException("Article found.", article);
        }
    }
}
