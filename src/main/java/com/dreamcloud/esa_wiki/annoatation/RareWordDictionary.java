package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_wiki.annoatation.handler.XmlReadingHandler;
import com.dreamcloud.esa_wiki.fs.BZipFileTools;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RareWordDictionary extends XmlReadingHandler {
    protected final SAXParserFactory saxFactory;
    protected int rareWordThreshold = 0;
    protected int termsRead = 0;
    protected int rareTerms = 0;
    protected Map<String, Integer> uniqueTerms = new HashMap<>();
    protected long totalDocumentLength;
    protected Analyzer analyzer;

    public RareWordDictionary(Analyzer analyzer, int rareWordThreshold) {
        this.analyzer = analyzer;
        this.rareWordThreshold = rareWordThreshold;
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public Map<String, Integer> getDocumentFrequencies() {
        return uniqueTerms;
    }

    public void parse(File inputFile) throws IOException, SAXException, ParserConfigurationException {
        //Build the map
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(inputFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, this);
        reader.close();
    }

    public void mapToXml(File inputFile, File outputFile) throws IOException, ParserConfigurationException, SAXException {
        this.parse(inputFile);

        //Write the map
        OutputStream outputStream = new FileOutputStream(outputFile);
        outputStream = new BufferedOutputStream(outputStream, 4096 * 4);
        for (String term: uniqueTerms.keySet()) {
            int count = uniqueTerms.get(term);
            if (count < rareWordThreshold) {
                rareTerms++;
                outputStream.write(term.concat("\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        outputStream.close();

        System.out.println("Word Statistics:");
        System.out.println("----------------------------------------");
        System.out.println("Words Read:\t" + termsRead);
        System.out.println("Unique Words:\t" + uniqueTerms.size());
        System.out.println("Rare Words:\t" + rareTerms);
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(1);
        System.out.println("Rare Word %:\t" + format.format((double) rareTerms / (double) uniqueTerms.size()));
        System.out.println("----------------------------------------");
    }

    @Override
    protected void handleDocument(Map<String, String> xmlFields) throws SAXException {
        String text = xmlFields.get("text");
        TokenStream tokens = analyzer.tokenStream("text", text);
        CharTermAttribute termAttribute = tokens.addAttribute(CharTermAttribute.class);
        Set<String> uniqueTerms = new HashSet<>();
        try {
            tokens.reset();
            while(tokens.incrementToken()) {
                termsRead++;
                uniqueTerms.add(termAttribute.toString());
                totalDocumentLength++;
            }
            tokens.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        for (String term: uniqueTerms) {
            this.uniqueTerms.put(term, this.uniqueTerms.getOrDefault(term, 0) + 1);
        }
    }

    public double getAverageDocumentLength() {
        return this.totalDocumentLength / (double) this.getDocsRead();
    }
}
