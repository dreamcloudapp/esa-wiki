package com.dreamcloud.esa_wiki.annoatation.handler;

import com.dreamcloud.esa_score.score.DocumentNameResolver;
import com.dreamcloud.esa_wiki.annoatation.WikiAnnotation;
import com.dreamcloud.esa_wiki.annoatation.WikiLinkAndTermAnnotatorOptions;
import com.dreamcloud.esa_wiki.utility.StringUtils;
import org.apache.commons.collections4.MultiValuedMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiLinkHandler extends ConcurrentXmlReadingHandler {
    WikiLinkAndTermAnnotatorOptions options;
    static Pattern linkRegexPattern = Pattern.compile("\\[\\[(?!File:|Image:)([^|#\\]]+)[^]]*]]");
    protected Map<String, String> titleMap;
    private final MultiValuedMap<Integer, Integer> incomingLinkMap;
    private final MultiValuedMap<Integer, Integer> outgoingLinkMap;
    protected final Map<Integer, WikiAnnotation> annotations;

    public WikiLinkHandler(int threadCount, int batchSize, WikiLinkAndTermAnnotatorOptions options, Map<String, String> titleMap, Map<Integer, WikiAnnotation> annotations, MultiValuedMap<Integer, Integer> incomingLinks, MultiValuedMap<Integer, Integer> outgoingLinks) {
        super(threadCount, batchSize);
        this.options = options;
        this.titleMap = titleMap;
        this.annotations = annotations;
        this.incomingLinkMap = incomingLinks;
        this.outgoingLinkMap = outgoingLinks;
    }

    @Override
    protected Integer handleDocuments(Vector<Map<String, String>> documents) {
        try {
            System.out.println("documents: " + documents.size());
            boolean logged = false;
            for (Map<String, String> document: documents) {
                String title = document.get("title");
                String text = document.get("text");
                int titleId = Integer.parseInt(document.get("id"));

                if (!logged) {
                    System.out.println("link-annotated article\t[" + this.getDocsRead() + "]\t\"" + title + "\"");
                    logged = true;
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
                    String link = titleMap.get(StringUtils.normalizeWikiTitle(matcher.group(1)));
                    if (link != null) {
                        int linkId = DocumentNameResolver.getId(link);
                        if (linkId != -1) {
                            outgoingLinks.add(linkId);
                        }
                    }
                }

                //Add to our outgoing links
                synchronized (annotations) {
                    if (options.minimumOutgoingLinks > 0 && outgoingLinks.size() < options.minimumOutgoingLinks) {
                        //This doesn't count toward anyone's links and can get removed now to free up memory
                        annotations.remove(titleId);
                        continue;
                    }

                    WikiAnnotation annotation = annotations.getOrDefault(titleId, new WikiAnnotation(0, 0, 0));
                    annotation.outgoingLinks = outgoingLinks.size();
                    annotations.put(titleId, annotation);

                    for (Integer outgoingLink: outgoingLinks) {
                        WikiAnnotation outgoingAnnotation = annotations.getOrDefault(outgoingLink, new WikiAnnotation(0, 0, 0));
                        outgoingAnnotation.incomingLinks++;
                        annotations.put(outgoingLink, outgoingAnnotation);
                        synchronized (outgoingLinkMap) {
                            outgoingLinkMap.put(titleId, outgoingLink);
                        }

                        synchronized (incomingLinkMap) {
                            incomingLinkMap.put(outgoingLink, titleId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return documents.size();
    }
}
