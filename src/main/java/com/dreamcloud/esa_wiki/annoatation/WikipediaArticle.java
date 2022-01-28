package com.dreamcloud.esa_wiki.annoatation;

public class WikipediaArticle {
    public int id;
    public String title;
    public String text;
    public Integer incomingLinks;
    public Integer outgoingLinks;
    public Integer terms;


    public boolean canIndex(AnnotationOptions options) {
        return !(
                (options.minimumIncomingLinks > 0 && incomingLinks != null && incomingLinks < options.minimumIncomingLinks)
                || (options.minimumOutgoingLinks > 0 && outgoingLinks != null && outgoingLinks < options.minimumOutgoingLinks)
                || (options.minimumTermCount > 0 && terms != null && terms < options.minimumTermCount)
                || (options.maximumTermCount > 0 && terms != null && terms > options.maximumTermCount)
        );
    }
}
