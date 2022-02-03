package com.dreamcloud.esa_wiki.annoatation.category;

import com.dreamcloud.esa_wiki.utility.StringUtils;

/**
 * Representation of a Wikipedia category
 */
public class Category {
    private String name;
    private boolean isExpandable;

    public Category(String name, boolean isExpandable) {
        this.name = name;
        this.isExpandable = isExpandable;
    }

    public Category(String name) {
        this(name, true);
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public static Category createNormalizedCategory(String name, boolean isExpandable) {
        return new Category(StringUtils.normalizeWikiTitle(name), isExpandable);
    }

    public static Category createNormalizedCategory(String name) {
        return new Category(StringUtils.normalizeWikiTitle(name), true);
    }

    public String getName() {
        return name;
    }
}
