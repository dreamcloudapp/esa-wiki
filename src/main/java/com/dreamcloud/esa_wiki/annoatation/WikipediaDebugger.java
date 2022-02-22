package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_core.xml.BZipFileTools;
import com.dreamcloud.esa_core.xml.XmlReadingHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikipediaDebugger extends XmlReadingHandler {
    protected final SAXParserFactory saxFactory;
    private Pattern disambiguationPattern = Pattern.compile("\\{\\{[^}]*([Dd]isambiguat)[^}]*}}");
    private int numDisambiguationPages = 0;
    private File inputFile;

    public WikipediaDebugger(File inputFile) {
        this.inputFile = inputFile;
        this.setDocumentTag("page");
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public void analyze() throws IOException, ParserConfigurationException, SAXException {
        Reader reader = BZipFileTools.getFileReader(inputFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        SAXParser saxParser = saxFactory.newSAXParser();
        saxParser.parse(is, this);
        reader.close();

        System.out.println("----------------------------------------");
        System.out.println("Articles Read:\t" + this.getDocsRead());
        System.out.println("Disambiguations:\t" + numDisambiguationPages);
        System.out.println("----------------------------------------");
    }

    @Override
    protected void handleDocument(Map<String, String> xmlFields) throws SAXException {
        String text = xmlFields.get("text");
        String title = xmlFields.get("title");
        Matcher matcher = disambiguationPattern.matcher(text);
        if (matcher.find()) {
            numDisambiguationPages++;
            if (numDisambiguationPages % 6000 == 0) {
                System.out.println("debugged article [" + numDisambiguationPages + " | " + getDocsRead() + "]" + " (" + title + ")");
            }
        }
    }
}
