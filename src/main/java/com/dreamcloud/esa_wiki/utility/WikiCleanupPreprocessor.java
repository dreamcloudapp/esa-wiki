package com.dreamcloud.esa_wiki.utility;

import com.dreamcloud.esa_core.documentPreprocessor.DocumentPreprocessor;
import com.dreamcloud.esa_wiki.annoatation.linkParser.Link;
import com.dreamcloud.esa_wiki.annoatation.linkParser.LinkParser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
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
    private final Pattern referenceSectionPattern = Pattern.compile("==References==.+?(==|\\n\\n)", Pattern.DOTALL);
    private final Pattern sectionTitlePattern = Pattern.compile("^={2,4}.+={2,4}$", Pattern.MULTILINE);

    @Override
    public String process(String document) throws Exception {
        if (document == null) {
            return null;
        }

        //Strip out sections (===See also===) etc.
        Matcher matcher = referenceSectionPattern.matcher(document);
        document = matcher.replaceAll("");

        matcher = sectionTitlePattern.matcher(document);
        document = matcher.replaceAll("");

        //Strip out language code links
        matcher = languageCodePattern.matcher(document);
        document = matcher.replaceAll("");

        //Cleanup links
        for (int i=0; i<3; i++) {
            document = cleanTextLinks(document);
        }

        //Strip out &amp; HTML symbols
        matcher = htmlEscapePattern.matcher(document);
        document = matcher.replaceAll("");

        //Strip HTML comments
        matcher = htmlCommentPattern.matcher(document);
        document = matcher.replaceAll("");

        return document;
    }

    private String cleanTextLinks(String text) throws IOException {
        PushbackReader reader = new PushbackReader(new StringReader(text));
        LinkParser linkParser = new LinkParser(reader);
        Link link;
        while ((link = linkParser.parse()) != null) {
            String replacement;
            //good numbers for Pearson!
            /*if (link.isResource() || link.isNamespaced()) {
                replacement = link.getAnchor();
            } else if(link.isSelfLink()) {
                replacement = "";
            } else {
                replacement = link.getTargetArticle() + " " + link.getAnchor();
            }*/

            //better Spearman numbers
            if (link.isResource() || link.isNamespaced()) {
                replacement = "";
            } else {
                replacement =  link.getAnchor();
            }
            text = text.replaceAll(Pattern.quote(link.getText()), Matcher.quoteReplacement(replacement));
        }
        return text;
    }

    @Override
    public String getInfo() {
        return this.getClass().getSimpleName();
    }
}
