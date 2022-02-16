package com.dreamcloud.esa_wiki.utility;

import com.dreamcloud.esa_core.documentPreprocessor.DocumentPreprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans up crap in wikipedia articles.
 *
 * Things to do to improve Wikipedia parsing (2005):
 * 1. remove references (should be done already)
 * 2. remove languages (+)
 * 3. remove &amp; type references (+)
 * 4. always parse templates to remove things we don't want
 * 5. parse links to keep only link target and anchor
 * 6. strip out all file and image links
 *      For (5) and (6) we really just want to take all links and:
 *      keep the anchor text for files/links
 *      and keep the target and anchor otherwise
 *      remove namespace from links (gets rid of "category" and the like)
 * 7. remove more styling/tables (2021)
 * 8. strip HTML comments (+)
 */
public class WikiCleanupPreprocessor implements DocumentPreprocessor {
    private final Pattern languageCodePattern = Pattern.compile("\\[\\[[a-z_-]+:[^]]+]]");
    private final Pattern htmlCommentPattern = Pattern.compile("<!--+.*?-->", Pattern.DOTALL);
    private final Pattern htmlEscapePattern = Pattern.compile("&[a-zA-Z_-]+;");
    private final Pattern fileImageLinkPattern = Pattern.compile("\\[\\[(?:File|Image):.+\\|([^]]+)]]");

    @Override
    public String process(String document) throws Exception {
        if (document == null) {
            return document;
        }

        //Strip out &amp; HTML symbols
        Matcher matcher = htmlEscapePattern.matcher(document);
        document = matcher.replaceAll("");

        //Replace image and file links with their anchors
        matcher = fileImageLinkPattern.matcher(document);
        document = matcher.replaceAll("$1");

        //Strip out language code links
        matcher = languageCodePattern.matcher(document);
        document= matcher.replaceAll("");

        //Strip HTML comments
        matcher = htmlCommentPattern.matcher(document);
        document = matcher.replaceAll("");

        return document;
    }

    @Override
    public String getInfo() {
        return this.getClass().getSimpleName();
    }
}
