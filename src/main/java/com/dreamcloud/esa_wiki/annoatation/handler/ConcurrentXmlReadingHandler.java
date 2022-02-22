package com.dreamcloud.esa_wiki.annoatation.handler;

import org.xml.sax.SAXException;

import com.dreamcloud.esa_core.xml.XmlReadingHandler;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.*;

abstract public class ConcurrentXmlReadingHandler extends XmlReadingHandler {
    protected final int threadCount;
    protected final int batchSize;
    private final ExecutorService executorService;
    private final Vector<Map<String, String>> documentQueue = new Vector<>();

    public void reset() {
        super.reset();
        documentQueue.clear();
    }

    public ConcurrentXmlReadingHandler(int threadCount, int batchSize) {
        this.threadCount = threadCount;
        this.batchSize = batchSize;
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    abstract protected Integer handleDocuments(Vector<Map<String, String>> documents);

    @Override
    protected void handleDocument(Map<String, String> xmlFields) throws SAXException {
        documentQueue.add(xmlFields);

        if (documentQueue.size() == batchSize * threadCount) {
           this.processQueue();
        }
    }

    private void processQueue() {
        ArrayList<Callable<Integer>> processors = new ArrayList<>();
        for (int threadIdx = 0; threadIdx < threadCount && (threadIdx * batchSize) < documentQueue.size(); threadIdx++) {
            Vector<Map<String, String>> documents = new Vector<>(batchSize);
            for (int batchIdx = threadIdx * batchSize; batchIdx < (((threadIdx + 1) * batchSize)) && batchIdx < documentQueue.size(); batchIdx++) {
                documents.add(documentQueue.get(batchIdx));
            }
            processors.add(() -> this.handleDocuments(documents));
        }

        //Wait on all threads and then processes the results
        try{
            executorService.invokeAll(processors);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        documentQueue.clear();
    }

    @Override
    public void close() throws Exception {
        if (this.documentQueue.size() > 0) {
            this.processQueue();
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        super.close();
    }
}
