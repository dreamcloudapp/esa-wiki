package com.dreamcloud.esa_wiki.cli;

import com.dreamcloud.esa_wiki.annoatation.AnnotationOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class AnnotationOptionsReader {
    private static String MIN_TERMS = "min-terms";
    private static String MAX_TERMS = "max-terms";
    private static String MIN_INLINKS = "min-in-links";
    private static String MIN_OUTLINKS = "min-out-links";
    private static String TITLE_EXCLUSION = "title-exclusion";

    public void addOptions(Options options) {
        Option minimumTermCountOption = new Option(null, MIN_TERMS, true, "int / (indexing)\tThe minimum number of terms allowed for a document.");
        minimumTermCountOption.setRequired(false);
        options.addOption(minimumTermCountOption);

        Option maximumTermCountOption = new Option(null, MAX_TERMS, true, "int / (indexing)\tThe maximum number of terms allowed for a document.");
        maximumTermCountOption.setRequired(false);
        options.addOption(maximumTermCountOption);

        Option minimumIncomingLinksOption = new Option(null, MIN_INLINKS, true, "int / (indexing:wiki)\tThe minimum number of incoming links.");
        minimumIncomingLinksOption.setRequired(false);
        options.addOption(minimumIncomingLinksOption);

        Option minimumOutgoingLinksOption = new Option(null, MIN_OUTLINKS, true, "int / (indexing:wiki)\tThe minimum number of outgoing links.");
        minimumOutgoingLinksOption.setRequired(false);
        options.addOption(minimumOutgoingLinksOption);

        Option titleExclusionRegExListOption = new Option(null, TITLE_EXCLUSION, true, "string [string2 ...] / (indexing:wiki)\tA list of regexes used to exclude Wiki article titles.");
        titleExclusionRegExListOption.setArgs(Option.UNLIMITED_VALUES);
        titleExclusionRegExListOption.setRequired(false);
        options.addOption(titleExclusionRegExListOption);
    }

    public AnnotationOptions getOptions(CommandLine cli) {
        AnnotationOptions options = new AnnotationOptions();
        if (cli.hasOption(MIN_TERMS)) {
            options.setMinimumTermCount(Integer.parseInt(cli.getOptionValue(MIN_TERMS)));
        }
        if (cli.hasOption(MAX_TERMS)) {
            options.setMaximumTermCount(Integer.parseInt(cli.getOptionValue(MAX_TERMS)));
        }
        if (cli.hasOption(MIN_INLINKS)) {
            options.setMinimumIncomingLinks(Integer.parseInt(cli.getOptionValue(MIN_INLINKS)));
        }
        if (cli.hasOption(MIN_OUTLINKS)) {
            options.setMinimumOutgoingLinks(Integer.parseInt(cli.getOptionValue(MIN_OUTLINKS)));
        }
        if (cli.hasOption(TITLE_EXCLUSION)) {
            String[] exclusions = cli.getOptionValues(TITLE_EXCLUSION);
            for (String exclusion: exclusions) {
                options.addTitleExclusion(exclusion);
            }
        }
        return options;
    }
}
