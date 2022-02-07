package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_wiki.annoatation.handler.ConcurrentXmlWritingHandler;
import com.dreamcloud.esa_wiki.fs.BZipFileTools;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Takes a stripped dump file and a mapping of redirect titles
 * and adds the following information:
 * <docs>
 *     <doc>
 *         <title>cat</title>
 *         <text>Cats are small, furry, and cute mammals.</text>
 *         <incomingLinks>24</incomingLinks>
 *         <outgoingLinks>24</incomingLinks>
 *         <terms>24</terms>
 *     </doc>
 * </docs>
 *
 * This will be saved as an XML file,
 * with the option to exclude things that don't meed the minimum criteria.
 * This results in a smaller file size,
 * but makes the dump less versatile.
 */
public class WikiLinkAndTermAnnotator extends ConcurrentXmlWritingHandler {
    private final WikiLinkAndTermAnnotatorOptions options;
    private Map<String, String> titleMap = new HashMap<>();
    private final MultiValuedMap<Integer, Integer> incomingLinkMap = new HashSetValuedHashMap<>();
    private final MultiValuedMap<Integer, Integer> outgoingLinkMap = new HashSetValuedHashMap<>();
    private final Map<Integer, WikiAnnotation> annotations = new ConcurrentHashMap<>();

    private final SAXParserFactory saxFactory;
    private int numStripped = 0;

    public WikiLinkAndTermAnnotator(int threadCount, int batchSize, WikiLinkAndTermAnnotatorOptions options) {
        super(threadCount, batchSize);
        this.options = options;
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public void reset() {
        super.reset();
        annotations.clear();
        titleMap.clear();
        outgoingLinkMap.clear();
        incomingLinkMap.clear();
    }

    public void annotate(File inputFile, File titleMapFile, File outputFile) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        reset();
        buildTitleMap(titleMapFile);
        analyzeTerms(inputFile);
        analyzeLinks(inputFile);

        LinkPruner pruner = new LinkPruner(incomingLinkMap, outgoingLinkMap, options.minimumIncomingLinks);
        System.out.println("Outgoing Links: " + outgoingLinkMap.keySet().size());
        System.out.println("Incoming Links: " + incomingLinkMap.keySet().size());
        pruner.prune();
        annotations.keySet().retainAll(outgoingLinkMap.keySet());
        for (Integer articleId: annotations.keySet()) {
            WikiAnnotation annotation = annotations.get(articleId);
            annotation.outgoingLinks = outgoingLinkMap.get(articleId).size();
            annotation.incomingLinks = incomingLinkMap.get(articleId).size();
        }

        //Free up some memory
        outgoingLinkMap.clear();
        incomingLinkMap.clear();

        System.out.println("Annotations: " + annotations.size());
        float totalIncomingLinks = 0;
        float totalOutgoingLinks = 0;
        float totalTerms = 0;
        for (WikiAnnotation annotation: annotations.values()) {
            totalIncomingLinks += annotation.incomingLinks;
            totalOutgoingLinks += annotation.outgoingLinks;
            totalTerms += annotation.terms;
        }
        System.out.println("Link Stats: " + titleMap.size());
        System.out.println("---------------------------------------");
        System.out.println("Average Incoming Links: " + (totalIncomingLinks / annotations.size()));
        System.out.println("Average Outgoing Links: " + (totalOutgoingLinks / annotations.size()));
        System.out.println("Average Terms per Doc: " + (totalTerms / annotations.size()));
        System.out.println("---------------------------------------");
        writeAnnotatedXml(inputFile, outputFile);
    }

    protected void buildTitleMap(File titleMapFile) throws IOException, ParserConfigurationException, SAXException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(titleMapFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, new WikiTitleMapHandler(titleMap));
        reader.close();

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

    protected void analyzeTerms(File strippedFile) throws IOException, SAXException, ParserConfigurationException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(strippedFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, new WikiLinAndTermHandler(threadCount, batchSize, options, titleMap, annotations, WikiLinAndTermHandler.ANALYSIS_TERMS));
        reader.close();
    }

    protected void analyzeLinks(File strippedFile) throws IOException, SAXException, ParserConfigurationException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(strippedFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, new WikiLinAndTermHandler(threadCount, batchSize, options, titleMap, annotations, WikiLinAndTermHandler.ANALYSIS_LINKS, incomingLinkMap, outgoingLinkMap));
        reader.close();
    }

    protected void writeAnnotatedXml(File strippedFile, File outputFile) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(strippedFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        this.open(outputFile);
        this.writeDocumentBegin("docs");
        saxParser.parse(is, this);
        reader.close();
        this.writeDocumentEnd();

        System.out.println("Link Annotation Stats:");
        System.out.println("---------------------------------------");
        System.out.println("Articles Read:\t" + this.getDocsRead());
        System.out.println("Articles Skipped:\t" + this.numStripped);
        System.out.println("Articles Written:\t" + (this.getDocsRead() - this.numStripped));
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(1);
        System.out.println("Skip Rate:\t" + format.format((double) this.numStripped / (double) this.getDocsRead()));
        System.out.println("---------------------------------------");
    }

    @Override
    protected Integer handleDocuments(Vector<Map<String, String>> documents) {
        for (Map<String, String> document: documents) {
            String title = document.get("title");
            String text = document.get("text");
            int id = Integer.parseInt(document.get("id"));
            WikiAnnotation annotation = annotations.getOrDefault(id, null);
            if (annotation != null) {
                if (annotation.incomingLinks < options.minimumIncomingLinks || annotation.outgoingLinks < options.minimumOutgoingLinks || annotation.terms < options.minimumTerms) {
                    numStripped++;
                } else {
                    try {
                        writeDocument(id, title, text, annotation);
                    } catch (XMLStreamException | IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                //Free up some memory
                annotations.remove(id);
            } else {
                numStripped++;
            }

            if (this.getDocsRead() % 1000 == 0) {
                System.out.println("annotated article\t[" + numStripped + " | " + this.getDocsRead() + "]");
            }
        }
        return documents.size();
    }

    public void writeDocument(int id, String title, String text, WikiAnnotation annotation) throws XMLStreamException, IOException {
        synchronized (xmlWriter) {
            this.writeStartElement("doc");
            this.writeElement("id", String.valueOf(id));
            this.writeElement("title", title);
            this.writeElement("text", text);
            this.writeElement("incomingLinks", String.valueOf(annotation.incomingLinks));
            this.writeElement("outgoingLinks", String.valueOf(annotation.outgoingLinks));
            this.writeElement("terms", String.valueOf(annotation.terms));
            this.writeEndElement();
        }
    }
}
