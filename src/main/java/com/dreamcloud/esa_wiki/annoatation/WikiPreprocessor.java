package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_wiki.annoatation.category.CategoryAnalyzer;
import com.dreamcloud.esa_wiki.annoatation.handler.XmlWritingHandler;
import com.dreamcloud.esa_wiki.fs.BZipFileTools;
import com.dreamcloud.esa_wiki.utility.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Combines previous annotators into one for efficiency.
 * 1. Template resolution (no parameters, hard-coded for 5 template depth and 30 word threshold)
 * 2. Title mapping (normalizing Wiki titles and handling redirects)
 * 3. Article stripping (Removing articles via regex)
 *  set via --title-exclusion-regex "regex1" "regex2"
 */
public class WikiPreprocessor extends XmlWritingHandler {
    Map<String, String> templateMap;
    protected TemplateProcessor templateProcessor;
    protected CategoryAnalyzer categoryAnalyzer;
    Set<String> excludedCategories;
    protected Pattern redirectPattern = Pattern.compile("^.*#REDIRECT[^\\[]*\\[\\[([^#]+)(#.+)?]]", Pattern.CASE_INSENSITIVE);
    protected Pattern htmlAttributePattern = Pattern.compile("\\|\\s*[a-zA-Z_-]+\\s*=\\s*[^|]+\\|", Pattern.CASE_INSENSITIVE);
    protected Pattern stubPattern = Pattern.compile("\\{\\{[^}]*([Ss]tub|[Aa]sbox|[Mm]issing [Ii]nformation)[^}]*}}");
    ArrayList<Pattern> titleExclusionPatterns;
    protected final SAXParserFactory saxFactory;

    protected int docsStripped = 0;
    protected int numRedirects = 0;
    private int docsStrippedByCategory = 0;
    private int numCategories = 0;
    private Map<String, Set<String>> docsStrippedByRegex = new ConcurrentHashMap<>();
    protected int numStubs = 0;

    public WikiPreprocessor(WikiPreprocessorOptions options) {
        this.setDocumentTag("page");
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
        this.titleExclusionPatterns = new ArrayList<>();
        if (options.titleExclusionRegExList != null) {
            for(String titleExclusionRegEx: options.titleExclusionRegExList) {
                this.titleExclusionPatterns.add(Pattern.compile(titleExclusionRegEx));
            }
        }
        categoryAnalyzer = new CategoryAnalyzer();
    }

    public void preprocess(File inputFile, File outputFile, File titleOutputFile) throws Exception {
        //Create a map of normalized titles
        try(WikiTitleMapper titleMapper = new WikiTitleMapper(titleExclusionPatterns, inputFile)) {
            titleMapper.writeTitles(titleOutputFile);
        }


        //Generate a normalized template map
        try(TemplateMapper mapper = new TemplateMapper(new TemplateResolutionOptions())) {
            templateMap = mapper.map(inputFile);
        }


        //Perform the template substitution
        reset();
        TemplateResolutionOptions options = new TemplateResolutionOptions();
        options.recursionDepth = 1;
        templateProcessor = new TemplateProcessor(templateMap, options);
        Reader reader = BZipFileTools.getFileReader(inputFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        SAXParser saxParser = saxFactory.newSAXParser();

        //Build category hierarchies
        categoryAnalyzer.analyze(inputFile, templateProcessor);
        excludedCategories = categoryAnalyzer.getExcludedCategoryNames();

        this.open(outputFile);
        this.writeDocumentBegin("docs");

        saxParser.parse(is, this);
        reader.close();

        //End document
        this.writeDocumentEnd();

        //Show logs
        System.out.println("----------------------------------------");
        System.out.println("Articles Read:\t" + this.getDocsRead());
        System.out.println("Articles Stripped:\t" + docsStripped);
        System.out.println("Articles Stripped by Category:\t" + docsStrippedByCategory);
        System.out.println("Redirects Stripped:\t" + numRedirects);
        System.out.println("Stubs Stripped:\t" + numStubs);
        System.out.println("Categories Stripped:\t" + numCategories);

        int docsStrippedByRegexCount = 0;
        for (Set<String> stripped: docsStrippedByRegex.values()) {
            docsStrippedByRegexCount += stripped.size();
        }
        System.out.println("Stripped By Title:\t" + docsStrippedByRegexCount);
        System.out.println("----------------------------------------");
        for (String regex: docsStrippedByRegex.keySet()) {
            System.out.println(regex + ":\t" + docsStrippedByRegex.get(regex).size());
        }
        System.out.println("----------------------------------------");
        for (String regex: docsStrippedByRegex.keySet()) {
            docsStrippedByRegex.get(regex).forEach((String title) -> {
                System.out.println(regex + "\t->\t" + title);
            });
        }
        System.out.println("----------------------------------------");
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMinimumFractionDigits(1);
        System.out.println("Strip Rate:\t" + format.format(((double) docsStripped) / ((double) this.getDocsRead())));
        templateProcessor.displayInfo();
        System.out.println("----------------------------------------");
    }

    @Override
    protected void handleDocument(Map<String, String> xmlFields) throws SAXException {
        String title = xmlFields.get("title");
        String normalizedTitle = StringUtils.normalizeWikiTitle(title);
        String text = xmlFields.get("text");

        if (StringUtils.empty(title) || StringUtils.empty(text)) {
            this.docsStripped++;
            return;
        }

        //Exclude category articles
        if (normalizedTitle.startsWith("category:")) {
            this.docsStripped++;
            this.numCategories++;
            return;
        }

        //Exclude titles by regex
        for (Pattern pattern: this.titleExclusionPatterns) {
            Matcher matcher = pattern.matcher(title.toLowerCase());
            if (matcher.find()) {
                if (!docsStrippedByRegex.containsKey(pattern.pattern())) {
                    docsStrippedByRegex.put(pattern.pattern(), new HashSet<>());
                }
                docsStrippedByRegex.get(pattern.pattern()).add(title.toLowerCase());
                this.docsStripped++;
                return;
            }
        }

        //Exclude redirects
        Matcher matcher = redirectPattern.matcher(text);
        if (matcher.find()) {
            this.docsStripped++;
            this.numRedirects++;
            return;
        }

        /**matcher = stubPattern.matcher(text);
        if (matcher.find() || text.contains("stub}}")) {
            this.numStubs++;
            this.docsStripped++;
            return;
        }*/

        try {
            text = templateProcessor.substitute(text, title); //todo: why not use normalized title here?

            //Exclude articles in excluded categories
            System.out.println("Excluded articles:");
            System.out.println("---------------------------------------");
            for (String articleCategory: categoryAnalyzer.getArticleCategories(text)) {
                if (excludedCategories.contains(articleCategory)) {
                    System.out.println(articleCategory + "\t->\t" + normalizedTitle);
                    this.docsStripped++;
                    this.docsStrippedByCategory++;
                    return;
                }
            }
            System.out.println("---------------------------------------");

            //We've handled templates, so let's strip out HTML tags and CSS stuff
            //text = Jsoup.clean(text, "", Safelist.none());
            text = htmlAttributePattern.matcher(text).replaceAll(" ");
            this.writeDocument(normalizedTitle, text);
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int docsRead = this.getDocsRead();
        if (docsRead % 1000 == 0) {
            System.out.println("preprocessed article\t[" + docsStripped + " | " + docsRead + "]");
        }
    }

    public void writeDocument(String title, String text) throws XMLStreamException, IOException {
        this.writeStartElement("doc");
        this.writeElement("id", String.valueOf(this.getDocsRead()));
        this.writeElement("title", title);
        this.writeElement("text", text);
        this.writeEndElement();
    }
}
