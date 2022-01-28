package com.dreamcloud.esa_wiki.annoatation;

import java.util.ArrayList;

public class AnnotationOptions {
    protected int minimumTermCount = 0;
    protected int maximumTermCount = 0;
    protected int minimumIncomingLinks = 0;
    protected int minimumOutgoingLinks = 0;
    protected ArrayList<String> titleExclusionRegExList = new ArrayList<>();

    public AnnotationOptions() {

    }

    public int getMinimumTermCount() {
        return minimumTermCount;
    }

    public int getMaximumTermCount() {
        return maximumTermCount;
    }

    public int getMinimumIncomingLinks() {
        return minimumIncomingLinks;
    }

    public int getMinimumOutgoingLinks() {
        return minimumOutgoingLinks;
    }

    public ArrayList<String> getTitleExclusionRegExList() {
        return titleExclusionRegExList;
    }

    public void setMinimumTermCount(int minimumTermCount) {
        this.minimumTermCount = minimumTermCount;
    }

    public void setMaximumTermCount(int maximumTermCount) {
        this.maximumTermCount = maximumTermCount;
    }

    public void setMinimumIncomingLinks(int minimumIncomingLinks) {
        this.minimumIncomingLinks = minimumIncomingLinks;
    }

    public void setMinimumOutgoingLinks(int minimumOutgoingLinks) {
        this.minimumOutgoingLinks = minimumOutgoingLinks;
    }

    public void setTitleExclusionRegExList(ArrayList<String> titleExclusionRegExList) {
        this.titleExclusionRegExList = titleExclusionRegExList;
    }

    public void addTitleExclusion(String regex) {
        this.titleExclusionRegExList.add(regex);
    }
}
