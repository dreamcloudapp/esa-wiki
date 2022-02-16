package com.dreamcloud.esa_wiki.annoatation.debug;

import com.dreamcloud.esa_score.analysis.TfIdfAnalyzer;
import com.dreamcloud.esa_score.score.TfIdfScore;
import com.dreamcloud.esa_wiki.annoatation.handler.XmlReadingHandler;
import com.dreamcloud.esa_wiki.fs.BZipFileTools;
import com.dreamcloud.esa_wiki.utility.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;

public class ArticleAnalyzer extends XmlReadingHandler {
    private final SAXParserFactory saxFactory;
    private final Analyzer analyzer;
    private final TfIdfAnalyzer scoreAnalyzer;
    private ArticleMatcher articleMatcher;

    public ArticleAnalyzer(Analyzer analyzer, TfIdfAnalyzer scoreAnalyzer) {
        this.analyzer = analyzer;
        this.scoreAnalyzer = scoreAnalyzer;
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public ArticleAnalysis analyze(File inputFile, int searchIndex) throws ParserConfigurationException, IOException, SAXException {
        articleMatcher = (id, text) -> id == searchIndex;
        return this.parse(inputFile);
    }

    public ArticleAnalysis analyze(File inputFile, String title) throws ParserConfigurationException, IOException, SAXException {
        final String normalizedTitle = StringUtils.normalizeWikiTitle(title);
        articleMatcher = (id, text) -> text.equals(normalizedTitle);
        return this.parse(inputFile);
    }

    private ArticleAnalysis parse(File inputFile) throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(inputFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        try {
            saxParser.parse(is, this);
        }
        catch (ArticleAnalysisException e) {
            return e.article;
        }
        catch (SAXException e) {
            e.printStackTrace();
            System.exit(1);
        }
        reader.close();
        return null;
    }

    protected void handleDocument(Map<String, String> xmlFields) throws SAXException {
        int id = Integer.parseInt(xmlFields.get("id"));
        String title = xmlFields.get("title");
        String text = xmlFields.get("text");
        if (articleMatcher.matches(id, title)) {
            //do analysis
            ArticleAnalysis analysis = new ArticleAnalysis();
            analysis.id = id;
            analysis.title = title;
            analysis.sourceText = text;
            analysis.linkCount = Integer.parseInt(xmlFields.get("outgoingLinks"));
            analysis.termCount = Integer.parseInt(xmlFields.get("terms"));
            try {
                TfIdfScore[] scores = scoreAnalyzer.getTfIdfScores(text);
                Arrays.sort(scores, (TfIdfScore s1, TfIdfScore s2) -> Double.compare(s2.getScore(), s1.getScore()));
                analysis.tfIdfScores.addAll(Arrays.asList(scores));

                StringBuilder analyzedText = new StringBuilder();
                TokenStream tokens = analyzer.tokenStream("text", text);
                CharTermAttribute termAttribute = tokens.addAttribute(CharTermAttribute.class);
                tokens.reset();
                while(tokens.incrementToken()) {
                   analyzedText.append(termAttribute.toString()).append(' ');
                }
                analyzedText.deleteCharAt(analyzedText.length() - 1);
                tokens.close();
                analysis.analyzedText = analyzedText.toString();
            } catch (Exception e) {
                throw new SAXException(e.getMessage());
            }

            throw new ArticleAnalysisException("found article", analysis);
        }
    }
}
