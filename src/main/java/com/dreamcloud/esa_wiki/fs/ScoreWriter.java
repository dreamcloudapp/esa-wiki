package com.dreamcloud.esa_wiki.fs;

import com.dreamcloud.esa_score.analysis.CollectionInfo;
import com.dreamcloud.esa_score.fs.CollectionWriter;
import com.dreamcloud.esa_wiki.annoatation.WikipediaArticle;
import com.dreamcloud.esa_wiki.annoatation.handler.XmlReadingHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.*;

public class ScoreWriter extends XmlReadingHandler {
    protected final SAXParserFactory saxFactory;
    private final CollectionWriter collectionWriter;
    private ExecutorService executorService;
    WikipediaArticle[] fixedQueue;
    WikipediaArticle article;
    int queueSize = 0;
    private int numIndexed = 0;
    ScoreWriterOptions options;
    File inputFile;
    CollectionInfo collectionInfo;

    public ScoreWriter(File inputFile, CollectionWriter collectionWriter, CollectionInfo collectionInfo, ScoreWriterOptions options) {
        this.inputFile = inputFile;
        this.collectionInfo = collectionInfo;
        this.options = options;
        this.collectionWriter = collectionWriter;

        this.fixedQueue = new WikipediaArticle[options.threadCount * options.batchSize];
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public void reset() {
        super.reset();
        numIndexed = 0;
        queueSize = 0;
        article = null;
        this.fixedQueue = new WikipediaArticle[options.threadCount * options.batchSize];
    }

    public void index() throws IOException, ParserConfigurationException, SAXException {
        reset();
        executorService = Executors.newFixedThreadPool(options.threadCount);
        parseXmlDump(inputFile);
        //There may be queue items left over
        if (queueSize > 0) {
            this.processQueue();
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(12, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Write additional information about the collection
        collectionWriter.writeCollectionInfo(collectionInfo);
        collectionWriter.close();

        //Show logs
        System.out.println("----------------------------------------");
        System.out.println("Articles Indexed:\t" + numIndexed);
        System.out.println("Articles Skipped:\t" + (this.getDocsRead() - numIndexed));
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(1);
        System.out.println("Acceptance Rate:\t" + format.format(((double) numIndexed) / ((double) getDocsRead())));
        System.out.println("----------------------------------------");
    }

    public void parseXmlDump(File file) throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(file);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, this);
        reader.close();
    }

    void processQueue() {
        //Spawn up THREAD_COUNT threads and give each BATCH_SIZE articles
        ArrayList<Callable<Integer>> processors = new ArrayList<>();
        for (int i=0; i<options.threadCount && (i * options.batchSize) < queueSize; i++) {
            Vector<WikipediaArticle> articles = new Vector<>(options.batchSize);
            for (int j = i * options.batchSize; j<(((i+1) * options.batchSize)) && j<queueSize; j++) {
                articles.add(fixedQueue[j]);
            }
            processors.add(() -> this.indexArticles(articles));
        }

        //Wait on all threads and then processes the results
        try{
            List<Future<Integer>> futures = executorService.invokeAll(processors);
            for(Future<Integer> future: futures){
                if (future.isDone()) {
                    Integer indexed = future.get();
                    numIndexed += indexed;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }

        for (int i=0; i<options.threadCount * options.batchSize; i++) {
            fixedQueue[i] = null;
        }
        queueSize = 0;
        this.logMessage("Indexed articles\t[" + numIndexed + " | " + getDocsRead() + "]");
    }

    Integer indexArticles (Vector<WikipediaArticle> articles) throws Exception {
        for (WikipediaArticle article: articles) {
            indexDocument(article);
        }
        return articles.size();
    }

    void indexDocument(WikipediaArticle article) throws Exception {
        String wikiText = article.text;
        if (options.preprocessor != null) {
            wikiText = options.preprocessor.process(wikiText);
        }
        collectionWriter.writeDocumentScores(article.id, options.analyzer.getTfIdfScores(wikiText));
    }

    public void close() throws IOException {
    }

    protected void handleDocument(Map<String, String> xmlFields) throws SAXException {
        WikipediaArticle article = new WikipediaArticle();
        article.id = this.getDocsRead();
        article.title = xmlFields.get("title");
        article.text = xmlFields.get("text");
        article.incomingLinks = Integer.parseInt(xmlFields.get("incomingLinks"));
        article.outgoingLinks = Integer.parseInt(xmlFields.get("outgoingLinks"));
        article.terms = Integer.parseInt(xmlFields.get("terms"));

        if (article.canIndex(options.annotationOptions)) {
            fixedQueue[queueSize++] = article;
        }

        if (queueSize == options.batchSize * options.threadCount) {
            this.processQueue();
            queueSize = 0;
        }
    }
}
