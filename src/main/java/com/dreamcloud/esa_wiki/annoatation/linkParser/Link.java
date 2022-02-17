package com.dreamcloud.esa_wiki.annoatation.linkParser;

import com.dreamcloud.esa_wiki.utility.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Link {
    private static Pattern namespacedPattern = Pattern.compile("^[^ :]+:[^ ]");

    private String text;
    private String target;
    private String anchor;
    private List<LinkParameter> parameters;
    private boolean isNamespaced;

    public Link(String target, String anchor, List<LinkParameter> parameters) {
        if (target == null) {
            throw new NullPointerException("Target cannot be null.");
        }

        Matcher matcher = namespacedPattern.matcher(target);
        isNamespaced = matcher.find();

        this.target = target;
        if (anchor == null) {
            anchor = StringUtils.stripNamespace(target);
            anchor = normalizeTarget(anchor);
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

    public String getTargetArticle() {
        return normalizeTarget(target);
    }

    public String getTargetSection() {
        if (target.contains("#")) {
            return target.split("#")[1];
        } else {
            return null;
        }
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

    private static String normalizeTarget(String target) {
        if (target.contains("#")) {
            return target.split("#", 2)[0];
        } else {
            return target;
        }
    }

    public boolean isResource() {
        return this.target.startsWith("File:") || this.target.startsWith("Image:");
    }

    public boolean isSelfLink() {
        return this.target.startsWith("#");
    }

    public boolean isNamespaced() {
        return !isResource() && isNamespaced;
    }
}
