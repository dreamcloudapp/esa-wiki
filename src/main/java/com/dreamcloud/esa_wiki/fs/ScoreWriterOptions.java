package com.dreamcloud.esa_wiki.fs;

import com.dreamcloud.esa_core.analyzer.AnalyzerOptions;
import com.dreamcloud.esa_core.documentPreprocessor.DocumentPreprocessor;
import com.dreamcloud.esa_core.vectorizer.VectorizationOptions;
import com.dreamcloud.esa_score.analysis.TfIdfAnalyzer;
import com.dreamcloud.esa_wiki.annoatation.AnnotationOptions;

public class ScoreWriterOptions {
    protected TfIdfAnalyzer analyzer;
    protected int threadCount = 0;
    protected int batchSize = 0;
    protected DocumentPreprocessor preprocessor;
    protected VectorizationOptions vectorizationOptions;
    protected AnnotationOptions annotationOptions;

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public DocumentPreprocessor getPreprocessor() {
        return preprocessor;
    }

    public void setPreprocessor(DocumentPreprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }

    public AnnotationOptions getAnnotationOptions() {
        return annotationOptions;
    }

    public void setAnnotationOptions(AnnotationOptions annotationOptions) {
        this.annotationOptions = annotationOptions;
    }

    public VectorizationOptions getVectorizationOptions() {
        return vectorizationOptions;
    }

    public void setVectorizationOptions(VectorizationOptions vectorizationOptions) {
        this.vectorizationOptions = vectorizationOptions;
    }

    public TfIdfAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(TfIdfAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
}
