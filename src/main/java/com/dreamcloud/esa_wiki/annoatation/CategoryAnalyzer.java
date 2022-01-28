package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_wiki.annoatation.handler.XmlReadingHandler;
import com.dreamcloud.esa_wiki.fs.BZipFileTools;
import com.dreamcloud.esa_wiki.utility.StringUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryAnalyzer extends XmlReadingHandler {
    protected final SAXParserFactory saxFactory;
    static Pattern categoryRegexPattern = Pattern.compile("\\[\\[\\s?([cC]ategory:[^|#\\]]+)[^]]*]]");
    protected MultiValuedMap<String, String> categoryHierarchy = new HashSetValuedHashMap<>();
    protected MutableObjectIntMap<String> categoryInfo = ObjectIntMaps.mutable.empty();
    Set<String> excludedCategories = new HashSet<>();
    TemplateProcessor templateProcessor;

    public Set<String> getGabrilovichExclusionCategories() {
        return excludedCategories;
    }

    public CategoryAnalyzer() {
        setDocumentTag("page");
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Star name disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:America"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Disambiguation"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Georgia"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Lists of political parties by generic name"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Galaxy name disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Lists of two-letter combinations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Disambiguation categories"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Towns in Italy (StringUtils.normalizeWikiTitle(disambiguation)"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Redirects to disambiguation pages"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Birmingham"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Mathematical disambiguation"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Public schools in Montgomery County"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Structured lists"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Identical titles for unrelated songs"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Signpost articles"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Township disambiguation"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:County disambiguation"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Disambiguation pages in need of cleanup"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Human name disambiguation"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Number disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Letter and number combinations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:4-letter acronyms"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Acronyms that may need to be disambiguated"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Lists of roads sharing the same title"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:List disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:3-digit Interstate disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Geographical locations sharing the same title"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Tropical cyclone disambiguation"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Repeat-word disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Song disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Disambiguated phrases"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Subway station disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Lists of identical but unrelated album titles"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:5-letter acronyms"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Three-letter acronym disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Miscellaneous disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Two-letter acronym disambiguations"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Days"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Eastern Orthodox liturgical days"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Stubs"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Pages for deletion"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Articles that need to be wikified"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:All stub articles"));
       excludedCategories.add(StringUtils.normalizeWikiTitle("Category:Articles to be expanded"));
    }

    public void analyze(File inputFile, TemplateProcessor templateProcessor) throws ParserConfigurationException, SAXException, IOException {
        this.templateProcessor = templateProcessor;
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(inputFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, this);
        reader.close();

        /*System.out.println("Category Stats");
        System.out.println("---------------------------------------");
        System.out.println("Total categories " + categoryInfo.size());
        Iterator<String> orderedCategories = categoryInfo.keySet().stream().
                sorted((String k1, String k2) -> (int) Math.signum(categoryInfo.get(k2) - categoryInfo.get(k1))).
                iterator();
        int i = 0;
        while (orderedCategories.hasNext()) {
            String category = orderedCategories.next();
            int count = categoryInfo.get(category);
            System.out.println(category + ":\t\t" + count);
            if (++i == 100) {
                break;
            }
        }

        int excludedCount = 0;

        for (String category: categoryInfo.keySet()) {
            for (String excludedCategory : getGabrilovichExclusionCategories()) {
                if (areCategoriesRelated(excludedCategory, category)) {
                    excludedCount += categoryInfo.get(excludedCategory);
                    break;
                }
            }
        }
        System.out.println("Excluded articles: " + excludedCount);
        System.out.println("---------------------------------------");
         */

        for (String excludedCategory: excludedCategories.toArray(String[]::new)) {
            expandExcludedCategories(excludedCategory);
        }
    }

    public void expandExcludedCategories(String category) {
        Collection<String> childCategories = categoryHierarchy.get(category);
        if (childCategories != null) {
            for (String childCategory: childCategories) {
                if (excludedCategories.contains(childCategory)) {
                    continue;
                }
                excludedCategories.add(childCategory);
                expandExcludedCategories(childCategory);
            }
        }
    }

    protected void handleDocument(Map<String, String> xmlFields) {
        String title = StringUtils.normalizeWikiTitle(xmlFields.get("title"));
        String text = xmlFields.get("text");
        boolean isCategoryArticle = title.startsWith("category:");

        try {
            if (isCategoryArticle && this.templateProcessor != null) {
                text = templateProcessor.substitute(text, title);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        for (String category: getArticleCategories(text)) {
            categoryInfo.addToValue(category, 1);
            if (isCategoryArticle) {
                categoryHierarchy.put(category, title);
            }
        }
        this.logMessage("Categorized article\t" + this.getDocsRead());
    }

    public Collection<String> getArticleCategories(String articleText) {
        Matcher matcher = categoryRegexPattern.matcher(articleText);
        Set<String> categories = new HashSet<>();
        while (matcher.find()) {
            String category = StringUtils.normalizeWikiTitle(matcher.group(1));
            categories.add(category);
        }
        return categories;
    }

    public boolean articleHasCategory(String articleText, String category) {
        return getArticleCategories(articleText).contains(category);
        /*for (String articleCategory: getArticleCategories(articleText)) {
            if (areCategoriesRelated(category, articleCategory)) {
                return true;
            }
        }
        return false;*/
    }

    protected boolean areCategoriesRelated(String parent, String orphan) {
        Set<String> categoriesSeen = new HashSet<>();
        return areCategoriesRelated(parent, orphan, categoriesSeen);
    }

    protected boolean areCategoriesRelated(String parent, String orphan, Set<String> categoriesSeen) {
        if (categoriesSeen.contains(parent)) {
            //prevent recursion
            return false;
        }
        categoriesSeen.add(parent);

        if (parent.equals(orphan)) {
            //as related as you can be
            return true;
        }

        Collection<String> children = categoryHierarchy.get(parent);
        for (String child: children) {
            if (areCategoriesRelated(child, orphan, categoriesSeen)) {
                return true;
            }
        }
        return false;
    }
}
