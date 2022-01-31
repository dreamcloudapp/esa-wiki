package com.dreamcloud.esa_wiki;

import com.dreamcloud.esa_core.analyzer.AnalyzerOptions;
import com.dreamcloud.esa_core.analyzer.EsaAnalyzer;
import com.dreamcloud.esa_core.analyzer.TokenizerFactory;
import com.dreamcloud.esa_core.cli.AnalyzerOptionsReader;
import com.dreamcloud.esa_core.cli.VectorizationOptionsReader;
import com.dreamcloud.esa_core.vectorizer.VectorizationOptions;
import com.dreamcloud.esa_score.analysis.CollectionInfo;
import com.dreamcloud.esa_score.analysis.TfIdfAnalyzer;
import com.dreamcloud.esa_score.analysis.TfIdfOptions;
import com.dreamcloud.esa_score.analysis.TfIdfStrategyFactory;
import com.dreamcloud.esa_score.analysis.strategy.TfIdfStrategy;
import com.dreamcloud.esa_score.cli.FileSystemScoringReader;
import com.dreamcloud.esa_score.cli.TfIdfOptionsReader;
import com.dreamcloud.esa_score.fs.CollectionWriter;
import com.dreamcloud.esa_wiki.annoatation.*;
import com.dreamcloud.esa_wiki.annoatation.debug.ArticleFinder;
import com.dreamcloud.esa_wiki.annoatation.debug.DebugArticle;
import com.dreamcloud.esa_wiki.cli.AnnotationOptionsReader;
import com.dreamcloud.esa_wiki.cli.ScoreWriterOptionsReader;
import com.dreamcloud.esa_wiki.fs.ScoreWriter;
import com.dreamcloud.esa_wiki.fs.ScoreWriterOptions;
import com.dreamcloud.esa_wiki.utility.ArrayUtils;
import com.dreamcloud.esa_wiki.utility.StringUtils;

import org.apache.commons.cli.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();

        AnalyzerOptionsReader analyzerOptionsReader = new AnalyzerOptionsReader();
        VectorizationOptionsReader vectorizationOptionsReader = new VectorizationOptionsReader();
        AnnotationOptionsReader annotationOptionsReader = new AnnotationOptionsReader();
        TfIdfOptionsReader tfIdfOptionsReader = new TfIdfOptionsReader();
        ScoreWriterOptionsReader scoreWriterOptionsReader = new ScoreWriterOptionsReader();
        FileSystemScoringReader fileSystemScoringReader = new FileSystemScoringReader();

        analyzerOptionsReader.addOptions(options);
        vectorizationOptionsReader.addOptions(options);
        annotationOptionsReader.addOptions(options);
        tfIdfOptionsReader.addOptions(options);
        scoreWriterOptionsReader.addOptions(options);
        fileSystemScoringReader.addOptions(options);

        //Debugging
        Option findArticleOption = new Option(null, "find-article", true, "inputFile articleTitle|index / Displays info about an article searched for via title or numeric index");
        findArticleOption.setRequired(false);
        findArticleOption.setArgs(2);
        options.addOption(findArticleOption);

        Option debugOption = new Option(null, "debug", true, "input.txt / Shows the tokens for a text.");
        debugOption.setRequired(false);
        options.addOption(debugOption);

        //Annotating
        Option wikiPreprocessorOption = new Option(null, "preprocess", true, "inputFile outputFile titleMapOutputFile / Wiki preprocessing: template resolution, title normalization, article stripping");
        wikiPreprocessorOption.setRequired(false);
        wikiPreprocessorOption.setArgs(3);
        options.addOption(wikiPreprocessorOption);

        Option linkCountOption = new Option(null, "count-links-and-terms", true, "wikiInputFile titleMapFile outputFile / Creates an annotated XML file with link counts.");
        linkCountOption.setRequired(false);
        linkCountOption.setArgs(3);
        options.addOption(linkCountOption);

        Option repeatContentOption = new Option(null, "repeat-content", true, "inputFile outputFile / Repeats titles and links to weight them more highly.");
        repeatContentOption.setRequired(false);
        repeatContentOption.setArgs(2);
        options.addOption(repeatContentOption);

        Option categoryInfoOption = new Option(null, "category-info", true, "inputFile / Displays category information about the processed dump file.");
        categoryInfoOption.setRequired(false);
        options.addOption(categoryInfoOption);

        Option repeatTitleOption = new Option(null, "repeat-title", true, "int | The number of times to repeat titles.");
        repeatTitleOption.setRequired(false);
        options.addOption(repeatTitleOption);

        Option repeatLinkOption = new Option(null, "repeat-link", true, "int | The number of times to repeat links.");
        repeatLinkOption.setRequired(false);
        options.addOption(repeatLinkOption);

        Option writeRareWordsOption = new Option(null, "write-rare-words", true, "inputFile outputFile rareWordCount / Creates a text file of rare words to later be used for stopwords.");
        writeRareWordsOption.setRequired(false);
        writeRareWordsOption.setArgs(3);
        options.addOption(writeRareWordsOption);

        Option articleStatsOption = new Option(null, "article-stats", true, "inputFile outputFile / Gets stats about the annotated articles.");
        articleStatsOption.setRequired(false);
        options.addOption(articleStatsOption);

        Option indexOption = new Option(null, "index", true, "input file / Indexes a corpus of documents.");
        indexOption.setRequired(false);
        options.addOption(indexOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cli = parser.parse(options, args);
            VectorizationOptions vectorOptions = vectorizationOptionsReader.getOptions(cli);
            AnalyzerOptions analyzerOptions = analyzerOptionsReader.getOptions(cli);
            TfIdfOptions tfIdfOptions = tfIdfOptionsReader.getOptions(cli);
            fileSystemScoringReader.parseOptions(cli);

            //Just for Wikipedia
            Set<String> stopTokenTypes = new HashSet<>();
            stopTokenTypes.add(WikipediaTokenizer.EXTERNAL_LINK_URL);
            stopTokenTypes.add(WikipediaTokenizer.EXTERNAL_LINK);
            stopTokenTypes.add(WikipediaTokenizer.CITATION);
            analyzerOptions.setStopTokenTypes(stopTokenTypes);
            analyzerOptions.setTokenizerFactory(new TokenizerFactory() {
                public Tokenizer getTokenizer() {
                    return new WikipediaTokenizer();
                }
            });

            AnnotationOptions annotationOptions = annotationOptionsReader.getOptions(cli);
            ScoreWriterOptions scoreWriterOptions = scoreWriterOptionsReader.getOptions(cli);
            scoreWriterOptions.setVectorizationOptions(vectorOptions);
            scoreWriterOptions.setAnnotationOptions(annotationOptions);


            String[] wikiPreprocessorArgs = cli.getOptionValues("preprocess");
            String[] findArticleArgs = cli.getOptionValues("find-article");
            String[] countLinkArgs = cli.getOptionValues("count-links-and-terms");
            String[] repeatContentArgs = cli.getOptionValues("repeat-content");
            String[] writeRareWordArgs = cli.getOptionValues("write-rare-words");

            String titleRepeat = cli.getOptionValue("repeat-title");
            String linkRepeat = cli.getOptionValue("repeat-link");
            String categoryInfo = cli.getOptionValue("category-info");

            //Display article text
            if(!ArrayUtils.tooShort(findArticleArgs, 2)) {
                ArticleFinder af = new ArticleFinder(new File(findArticleArgs[0]));
                System.out.println("Article");
                System.out.println("----------------------------------------");
                DebugArticle article = af.find(findArticleArgs[1]);
                if (article != null) {
                    System.out.println("index:\t" + article.index);
                    System.out.println("title:\t" + article.title);
                    System.out.println("string(" + article.text.length() + ")");
                    System.out.println(article.text);
                } else {
                    System.out.println("article not found");
                }
                System.out.println("----------------------------------------");
            }

            else if(!ArrayUtils.tooShort(wikiPreprocessorArgs, 3)) {
                File inputFile = new File(wikiPreprocessorArgs[0]);
                File outputFile = new File(wikiPreprocessorArgs[1]);
                File titleMapOutputFile = new File(wikiPreprocessorArgs[2]);
                WikiPreprocessorOptions wikiPreprocessorOptions = new WikiPreprocessorOptions();
                wikiPreprocessorOptions.titleExclusionRegExList = annotationOptions.getTitleExclusionRegExList();
                try (WikiPreprocessor wikiPreprocessor = new WikiPreprocessor(wikiPreprocessorOptions);) { wikiPreprocessor.preprocess(inputFile, outputFile, titleMapOutputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else if (!ArrayUtils.tooShort(countLinkArgs, 3)) {
                File strippedFile = new File(countLinkArgs[0]);
                File titleMapFile = new File(countLinkArgs[1]);
                File outputFile = new File(countLinkArgs[2]);
                WikiLinkAndTermAnnotatorOptions wikiLinkAnnotatorOptions = new WikiLinkAndTermAnnotatorOptions();
                wikiLinkAnnotatorOptions.minimumIncomingLinks = annotationOptions.getMinimumIncomingLinks();
                wikiLinkAnnotatorOptions.minimumOutgoingLinks = annotationOptions.getMinimumOutgoingLinks();
                wikiLinkAnnotatorOptions.minimumTerms = annotationOptions.getMinimumTermCount();
                wikiLinkAnnotatorOptions.maximumTermCount = annotationOptions.getMaximumTermCount();
                wikiLinkAnnotatorOptions.analyzer = new EsaAnalyzer(analyzerOptions);
                try(WikiLinkAndTermAnnotator annotator = new WikiLinkAndTermAnnotator(wikiLinkAnnotatorOptions)) {
                    annotator.annotate(strippedFile, titleMapFile, outputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            else if (!ArrayUtils.tooShort(repeatContentArgs, 2)) {
                File inputFile = new File(repeatContentArgs[0]);
                File outputFile = new File(repeatContentArgs[1]);
                WikiContentRepeatOptions repeatOptions = new WikiContentRepeatOptions();
                if (!StringUtils.empty(titleRepeat)) {
                    repeatOptions.titleRepeat = Integer.parseInt(titleRepeat);
                }
                if (!StringUtils.empty(linkRepeat)) {
                    repeatOptions.linkRepeat = Integer.parseInt(linkRepeat);
                }
                try(WikiContentRepeater repeater = new WikiContentRepeater(repeatOptions)) {
                    repeater.repeatContent(inputFile, outputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            else if (!ArrayUtils.tooShort(writeRareWordArgs, 3)) {
                File inputFile = new File(writeRareWordArgs[0]);
                File outputFile = new File(writeRareWordArgs[1]);
                int rareWordThreshold = Integer.parseInt(writeRareWordArgs[2]);
                try(RareWordDictionary termCountMapper = new RareWordDictionary(new EsaAnalyzer(analyzerOptions), rareWordThreshold)) {
                    termCountMapper.mapToXml(inputFile, outputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            else if (cli.hasOption("article-stats")) {
                File inputFile = new File(cli.getOptionValue("article-stats"));
                int rareTerms = 0; //need to read this from the rare words file, which isn't really possible atm because it's incorporated into the stopwords file
                try(ArticleStatsReader articleStatsReader = new ArticleStatsReader(new EsaAnalyzer(analyzerOptions), rareTerms)) {
                    articleStatsReader.readArticles(inputFile);
                } catch (ParserConfigurationException | SAXException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            else if(!StringUtils.empty(categoryInfo)) {
                File inputFile = new File(categoryInfo);
                try(CategoryAnalyzer categoryAnalyzer = new CategoryAnalyzer()) {
                    categoryAnalyzer.analyze(inputFile, null);
                }
            }

            //Indexing
            else if(cli.hasOption("index")) {
                System.out.println("Indexing...");
                File wikipediaFile = new File(cli.getOptionValue("index"));
                RareWordDictionary rareWordDictionary = new RareWordDictionary(new EsaAnalyzer(analyzerOptions), 0);
                rareWordDictionary.parse(wikipediaFile);
                CollectionInfo collectionInfo = new CollectionInfo(rareWordDictionary.getDocsRead(), rareWordDictionary.getAverageDocumentLength(), rareWordDictionary.getDocumentFrequencies());

                TfIdfStrategyFactory tfIdfFactory = new TfIdfStrategyFactory();
                TfIdfStrategy tfIdfStrategy = tfIdfFactory.getStrategy(tfIdfOptions);
                TfIdfAnalyzer tfIdfAnalyzer = new TfIdfAnalyzer(tfIdfStrategy, new EsaAnalyzer(analyzerOptions), collectionInfo);
                scoreWriterOptions.setAnalyzer(tfIdfAnalyzer);

                ScoreWriter writer = new ScoreWriter(wikipediaFile, fileSystemScoringReader.getCollectionWriter(), collectionInfo, scoreWriterOptions);
                writer.index();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
