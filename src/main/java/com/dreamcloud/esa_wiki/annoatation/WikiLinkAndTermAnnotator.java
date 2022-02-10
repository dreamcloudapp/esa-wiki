package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_wiki.annoatation.handler.WikiLinkHandler;
import com.dreamcloud.esa_wiki.annoatation.handler.WikiTermHandler;
import com.dreamcloud.esa_wiki.annoatation.handler.XmlReadingHandler;
import com.dreamcloud.esa_wiki.annoatation.handler.XmlWritingHandler;
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
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.*;

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
public class WikiLinkAndTermAnnotator extends XmlWritingHandler {
    private final WikiLinkAndTermAnnotatorOptions options;
    private final Map<String, String> titleMap = new ConcurrentHashMap<>();
    private final MultiValuedMap<Integer, Integer> incomingLinkMap = new HashSetValuedHashMap<>();
    private final MultiValuedMap<Integer, Integer> outgoingLinkMap = new HashSetValuedHashMap<>();
    private final Map<Integer, WikiAnnotation> annotations = new ConcurrentHashMap<>();

    private final SAXParserFactory saxFactory;
    private int numStripped = 0;

    public WikiLinkAndTermAnnotator(WikiLinkAndTermAnnotatorOptions options) {
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

    public void annotate(File inputFile, File titleMapFile, File outputFile) throws Exception {
        reset();
        buildTitleMap(titleMapFile);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CompletionService<Integer> completionService =
                new ExecutorCompletionService<>(executorService);

        Vector<Future<Integer>> tasks = new Vector<>();

        tasks.add(completionService.submit(() -> {
            analyzeTerms(inputFile);
            return 0;
        }));

        tasks.add(completionService.submit(() -> {
            analyzeLinks(inputFile);
            return 0;
        }));

        //Wait for futures to finish
        int completed = 0;
        boolean errors = false;
        while (completed < 2 && !errors) {
            Future<Integer> resultFuture = completionService.take();
            try {
                Integer result = resultFuture.get();
                System.out.println("task completed: " + result);
                if (result != 0) {
                    errors = true;
                }
                completed++;
            }
            catch(Exception e) {
                errors = true;
            }
        }

        if (errors) {
            tasks.forEach((Future<Integer> future) -> future.cancel(true));
            System.out.println("errors in threads!");
            System.exit(1);
        }
        tasks.clear();
        executorService.shutdown();

        System.out.println("(before pruning)");
        System.out.println("---------------------------------------");
        System.out.println("Annotations: " + annotations.size());
        System.out.println("Outgoing Links: " + outgoingLinkMap.keySet().size());
        System.out.println("Incoming Links: " + incomingLinkMap.keySet().size());
        System.out.println("---------------------------------------");
        LinkPruner pruner = new LinkPruner(incomingLinkMap, outgoingLinkMap, options.minimumIncomingLinks);
        pruner.prune();
        annotations.keySet().retainAll(outgoingLinkMap.keySet());
        for (Integer articleId: annotations.keySet()) {
            WikiAnnotation annotation = annotations.get(articleId);
            annotation.outgoingLinks = outgoingLinkMap.get(articleId).size();
            annotation.incomingLinks = incomingLinkMap.get(articleId).size();
        }

        System.out.println("(after pruning)");
        System.out.println("---------------------------------------");
        System.out.println("Annotations: " + annotations.size());
        System.out.println("Outgoing Links: " + outgoingLinkMap.keySet().size());
        System.out.println("Incoming Links: " + incomingLinkMap.keySet().size());
        System.out.println("---------------------------------------");

        //Free up some memory
        outgoingLinkMap.clear();
        incomingLinkMap.clear();

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
    }

    protected void analyzeTerms(File strippedFile) throws Exception {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(strippedFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        XmlReadingHandler handler = new WikiTermHandler(12, 1000, options, titleMap, annotations);
        saxParser.parse(is, handler);
        handler.close();
        reader.close();
    }

    protected void analyzeLinks(File strippedFile) throws Exception {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(strippedFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        WikiLinkHandler handler = new WikiLinkHandler(12, 1000, options, titleMap, annotations, incomingLinkMap, outgoingLinkMap);
        saxParser.parse(is, handler);
        handler.close();
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
    protected void handleDocument(Map<String, String> document) throws SAXException {
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

        this.logMessage("annotated article\t[" + numStripped + " | " + this.getDocsRead() + "]");
    }

    public void writeDocument(int id, String title, String text, WikiAnnotation annotation) throws XMLStreamException, IOException {
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
