package com.dreamcloud.esa_wiki.annoatation;

import org.apache.commons.collections4.MultiValuedMap;

import java.util.Collection;
import java.util.Iterator;

public class LinkPruner {
    protected MultiValuedMap<Integer, Integer> incomingLinkMap;
    protected MultiValuedMap<Integer, Integer> outgoingLinkMap;
    protected int minimumLinks;

    public LinkPruner(MultiValuedMap<Integer, Integer> incomingLinkMap, MultiValuedMap<Integer, Integer> outgoingLinkMap, int minimumLinks) {
        this.incomingLinkMap = incomingLinkMap;
        this.outgoingLinkMap = outgoingLinkMap;
        this.minimumLinks = minimumLinks;
    }

    public void prune() {
        int pruned;
        do {
            int prunedOut = pruneOutgoingLinks();
            int prunedIn = pruneIncomingLinks();
            pruned = prunedOut + prunedIn;
            System.out.println("Pruned " + pruned + ": out=" + prunedOut + "; in=" + prunedIn);
        } while (pruned > 0);
        System.out.println("Pruned Outgoing: " + outgoingLinkMap.keySet().size());
        System.out.println("Pruned Incoming: " + incomingLinkMap.keySet().size());
        outgoingLinkMap.keySet().retainAll(incomingLinkMap.keySet());
        System.out.println("Pruned Intersection: " + outgoingLinkMap.keySet().size());
    }

    protected int pruneOutgoingLinks() {
        int pruned = 0;
        for (Iterator<Integer> it = outgoingLinkMap.keySet().iterator(); it.hasNext();) {
            Integer articleId = it.next();
            Collection<Integer> linkedArticles = outgoingLinkMap.get(articleId);
            if (linkedArticles.size() < minimumLinks) {
                for (Integer linkedArticleId: linkedArticles) {
                    incomingLinkMap.removeMapping(linkedArticleId, articleId);
                }
                it.remove();
                pruned++;
            }
        }
        return pruned;
    }

    protected int pruneIncomingLinks() {
        int pruned = 0;
        for (Iterator<Integer> it = incomingLinkMap.keySet().iterator(); it.hasNext();) {
            Integer linkedArticleId = it.next();
            Collection<Integer> articles = incomingLinkMap.get(linkedArticleId);
            if (articles.size() < minimumLinks) {
                for (Integer articleId: articles) {
                    outgoingLinkMap.removeMapping(articleId, linkedArticleId);
                }
                it.remove();
                pruned++;
            }
        }
        return pruned;
    }
}
