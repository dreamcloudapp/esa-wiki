package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_wiki.annoatation.handler.XmlWritingHandler;
import com.dreamcloud.esa_wiki.fs.BZipFileTools;
import com.dreamcloud.esa_wiki.utility.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//#REDIRECT [[Cat]]
/**
 * Takes a Wikimedia dump file, and generates a mapping of normalized titles.
 * This mapping will be written to an XML file in the following format:
 * <docs>
 *     <doc>
 *         <title>United States of America</title>
 *         <redirect>United States</redirect>
 *     </doc>
 * </docs>
 *
 * Also creates a text file with all article IDs mapped to their titles:
 * int string newline
 * int string newline
 * etc.
 */
class WikiTitleMapper extends XmlWritingHandler {
    ArrayList<Pattern> titleExclusionPatterns;
    protected Pattern redirectPattern = Pattern.compile("^.*#REDIRECT[^\\[]*\\[\\[([^#]+)(#.+)?]]", Pattern.CASE_INSENSITIVE);
    protected final SAXParserFactory saxFactory;
    protected int numRedirects = 0;
    protected Map<String, String> titleMap = new ConcurrentHashMap<>();

    protected WikiTitleMapper(ArrayList<Pattern> titleExclusionPatterns) {
        this.setDocumentTag("page");
        this.titleExclusionPatterns = titleExclusionPatterns;
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public void mapTitles(File inputFile) throws ParserConfigurationException, IOException, SAXException {
        this.parse(inputFile);

        //Resolve all redirects
        Map<String, String> resolvedTitleMap = new HashMap<>();
        for (String title: titleMap.keySet()) {
            String originalTitle = title;
            while (titleMap.containsKey(title)) {
                String resolvedTitle = titleMap.get(title);
                if (title.equals(resolvedTitle)) {
                    break;
                } else {
                    title = resolvedTitle;
                }
            }
            resolvedTitleMap.put(originalTitle, title);
        }
        titleMap = resolvedTitleMap;
    }

    public void writeTitles(File outputFile) throws IOException, XMLStreamException {
        this.open(outputFile);
        this.writeDocumentBegin("docs");
        for (String title: titleMap.keySet()) {
            String redirect = titleMap.get(title);
            this.writeDocument(title, redirect);
        }
        this.writeDocumentEnd();

        System.out.println("----------------------------------------");
        System.out.println("Articles Read:\t" + getDocsRead());
        System.out.println("Articles Redirected:\t" + numRedirects);
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(1);
        System.out.println("Redirection Rate:\t" + format.format(((double) numRedirects) / ((double) getDocsRead())));
        System.out.println("----------------------------------------");
    }

    protected void parse(File inputFile) throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(inputFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, this);
        reader.close();
    }

    @Override
    protected void handleDocument(Map<String, String> xmlFields) {
        String title = StringUtils.normalizeWikiTitle(xmlFields.get("title"));
        //Exclude titles by regex
        for (Pattern pattern: this.titleExclusionPatterns) {
            Matcher matcher = pattern.matcher(title.toLowerCase());
            if (matcher.find()) {
                return;
            }
        }
        String text = xmlFields.get("text");
        String redirect = title;
        Matcher matcher = redirectPattern.matcher(text);
        if (matcher.matches()) {
            numRedirects++;
            redirect = StringUtils.normalizeWikiTitle(matcher.group(1));
            for (Pattern pattern: this.titleExclusionPatterns) {
                Matcher redirectMatcher = pattern.matcher(redirect.toLowerCase());
                if (redirectMatcher.find()) {
                    return;
                }
            }
        }
        titleMap.put(title, redirect);
    }

    private void writeDocument(String title, String redirect) throws XMLStreamException, IOException {
        this.writeStartElement("doc");
        this.writeElement("title", title);
        this.writeElement("redirect", redirect);
        this.writeEndElement();
    }
}
