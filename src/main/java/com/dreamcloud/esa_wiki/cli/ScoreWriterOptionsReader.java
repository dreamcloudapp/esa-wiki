package com.dreamcloud.esa_wiki.cli;

import com.dreamcloud.esa_wiki.fs.ScoreWriterOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ScoreWriterOptionsReader {
    protected static String THREAD_COUNT = "threads";
    protected static String BATCH_SIZE = "batch-size";

    public void addOptions(Options options) {

        Option threadCountOption = new Option(null, THREAD_COUNT, true, "int / (indexing)\tThe number of threads to use.");
        threadCountOption.setRequired(false);
        options.addOption(threadCountOption);

        Option batchSizeOption = new Option(null, BATCH_SIZE, true, "int / (indexing)\tThe number of documents to process at once, distributed across the threads.");
        batchSizeOption.setRequired(false);
        options.addOption(batchSizeOption);
    }

    public ScoreWriterOptions getOptions(CommandLine cli) {
        ScoreWriterOptions options = new ScoreWriterOptions();

        if (cli.hasOption(THREAD_COUNT)) {
            options.setThreadCount(Integer.parseInt(cli.getOptionValue(THREAD_COUNT)));
        }

        if (cli.hasOption(BATCH_SIZE)) {
            options.setBatchSize(Integer.parseInt(cli.getOptionValue(BATCH_SIZE)));
        }

       return options;
    }
}
