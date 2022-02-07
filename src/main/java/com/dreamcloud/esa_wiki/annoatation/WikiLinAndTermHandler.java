package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_score.score.DocumentNameResolver;
import com.dreamcloud.esa_wiki.annoatation.handler.XmlReadingHandler;
import com.dreamcloud.esa_wiki.utility.StringUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiLinAndTermHandler extends XmlReadingHandler {
    public static int ANALYSIS_TERMS = 1;
    public static int ANALYSIS_LINKS = 2;

    WikiLinkAndTermAnnotatorOptions options;
    static Pattern linkRegexPattern = Pattern.compile("\\[\\[(?!File:|Image:)([^|#\\]]+)[^]]*]]");
    protected Map<String, String> titleMap;
    protected MultiValuedMap<Integer, Integer> incomingLinkMap;
    protected MultiValuedMap<Integer, Integer> outgoingLinkMap;
    protected Map<Integer, WikiAnnotation> annotations;
    protected int analysisMode;

    public WikiLinAndTermHandler(WikiLinkAndTermAnnotatorOptions options, Map<String, String> titleMap, Map<Integer, WikiAnnotation> annotations, int analysisMode) {
        this.options = options;
        this.titleMap = titleMap;
        this.annotations = annotations;
        this.analysisMode = analysisMode;
    }

    public WikiLinAndTermHandler(WikiLinkAndTermAnnotatorOptions options, Map<String, String> titleMap, Map<Integer, WikiAnnotation> annotations, int analysisMode, MultiValuedMap<Integer, Integer> incomingLinks, MultiValuedMap<Integer, Integer> outgoingLinks) {
        this.options = options;
        this.titleMap = titleMap;
        this.annotations = annotations;
        this.analysisMode = analysisMode;
        this.incomingLinkMap = incomingLinks;
        this.outgoingLinkMap = outgoingLinks;
    }

    protected void handleDocument(Map<String, String> xmlFields) {
        String title = xmlFields.get("title");
        String text = xmlFields.get("text");
        int titleId = Integer.parseInt(xmlFields.get("id"));

        //Analyze the text!
        if (analysisMode == ANALYSIS_TERMS) {
            int termCount = 0;
            try {
                TokenStream tokens = options.analyzer.tokenStream("text", text);
                tokens.addAttribute(CharTermAttribute.class);
                tokens.reset();
                while(tokens.incrementToken()) {
                    termCount++;
                }
                tokens.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            if (termCount >= options.minimumTerms && (options.maximumTermCount == 0 || termCount <= options.maximumTermCount)) {
                annotations.put(titleId, new WikiAnnotation(0, 0, termCount));
            }
        } else {
            WikiAnnotation annotation = annotations.get(titleId);
            if (annotation == null) {
                return;
            }

            /*
                Create a set of outgoing links to valid annotations.
                We will add to our outgoing link count for each valid link in the set.
                Valid annotations were added to the map if they had enough terms.
                After that, we go to each annotation for an outgoing link and up it's incoming link count.
             */
            Matcher matcher = linkRegexPattern.matcher(text);
            Set<Integer> outgoingLinks = new HashSet<>();
            while (matcher.find()) {
                String link = titleMap.get(titleMap.get(StringUtils.normalizeWikiTitle(matcher.group(1))));
                int linkId = DocumentNameResolver.getId(link);
                if (linkId != -1 && annotations.containsKey(linkId)) {
                    outgoingLinks.add(linkId);
                }
            }

            if (options.minimumOutgoingLinks > 0 && outgoingLinks.size() < options.minimumOutgoingLinks) {
                //This doesn't count toward anyone's links and can get removed now to free up memory
                annotations.remove(titleId);
                return;
            }

            //Add to our outgoing links
            annotation.outgoingLinks = outgoingLinks.size();

            //Add to others incoming links
            for (Integer outgoingLink: outgoingLinks) {
                if (annotations.containsKey(outgoingLink)) {
                    WikiAnnotation outgoingAnnotation = annotations.get(outgoingLink);
                    outgoingAnnotation.incomingLinks++;
                    outgoingLinkMap.put(titleId, outgoingLink);
                    incomingLinkMap.put(outgoingLink, titleId);
                }
            }
        }

        if (this.getDocsRead() % 1000 == 0) {
            System.out.println((analysisMode == ANALYSIS_LINKS ? "link" : "term") + "-annotated article\t[" + this.getDocsRead() + "]\t\"" + title + "\"");
        }
    }
}
