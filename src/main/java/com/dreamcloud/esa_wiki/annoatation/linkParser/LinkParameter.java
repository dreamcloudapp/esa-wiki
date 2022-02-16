package com.dreamcloud.esa_wiki.annoatation.linkParser;

public class LinkParameter {
    private String parameter;
    private String value;

    public LinkParameter(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
    }

    public LinkParameter(String value) {
        this(null, value);
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
