package com.dreamcloud.esa_wiki.annoatation.linkParser;

import com.dreamcloud.esa_wiki.utility.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Link {
    private String text;
    private String target;
    private String anchor;
    private List<LinkParameter> parameters;

    public Link(String target, String anchor, List<LinkParameter> parameters) {
        this.target = target;
        if (anchor == null) {
            anchor = StringUtils.stripNamespace(target);
        }
        this.anchor = anchor;
        this.parameters = parameters;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Link(String target, List<LinkParameter> parameters) {
        this(target, null, parameters);
    }

    public Link(String target, String anchor) {
        this(target, anchor, new ArrayList<>());
    }

    public Link(String target) {
        this(target, (String) null);
    }

    public String getTarget() {
        return target;
    }

    public String getAnchor() {
        return anchor;
    }

    public List<LinkParameter> getParameters() {
        return parameters;
    }

    public void addParameter(LinkParameter parameter) {
        parameters.add(parameter);
    }
}
