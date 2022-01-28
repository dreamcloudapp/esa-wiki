package com.dreamcloud.esa_wiki;

import com.dreamcloud.esa_wiki.annoatation.TemplateProcessor;
import com.dreamcloud.esa_wiki.annoatation.TemplateResolutionOptions;
import org.junit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;



public class TemplateProcessorTest {
    public TemplateProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    private TemplateProcessor getDefaultProcessor() {
        TemplateResolutionOptions options = new TemplateResolutionOptions();
        Map<String, String> templateMap = new HashMap<>();
        return new TemplateProcessor(templateMap, options);
    }

    @Test
    public void testInstantiation() {
        TemplateProcessor processor = getDefaultProcessor();
        assertEquals(processor, processor);
    }

    @Test
    public void testSimpleTemplate() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("foo", "bar");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{foo}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("bar", processedArticle);
    }

    @Test
    public void testTemplateWithParameter() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("foo", "hello {{{1}}}");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{foo|world}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("hello world", processedArticle);
    }

    @Test
    public void testTemplateWithNamedParameter() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("foo", "hello {{{bar}}}");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{foo|bar=world}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("hello world", processedArticle);
    }

    @Test
    public void testTemplateWithAllParameters() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("foo", "{{{1}}} {{{bar}}}{{{3}}}");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{foo|hello|bar=world|!}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("hello world!", processedArticle);
    }

    @Test
    public void testNestedTemplates() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("foo", "hello {{bar}}");
        templateMap.put("bar", "world");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{foo}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("hello world", processedArticle);
    }

    @Test
    public void testNestedTemplatesWithParameters() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("greeting", "{{user|{{{from}}}}} greets {{{to}}}{{{3}}}");
        templateMap.put("user", "{{{1}}}");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{greeting|from=Bob|to=the world|!}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("Bob greets the world!", processedArticle);
    }

    @Test
    public void testMissingVariable() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("greeting", "hello {{{to}}}");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{greeting}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("hello {{{to}}}", processedArticle);
    }

    @Test
    public void testDefaultVariable() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("greeting", "hello {{{to|world}}}");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{greeting}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("hello world", processedArticle);
    }

    @Test
    public void testTemplateDepth() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("a", "{{b}}");
        templateMap.put("b", "{{c}}");
        templateMap.put("c", "{{d}}");
        templateMap.put("d", "{{e}}");
        templateMap.put("e", "{{f}}");
        templateMap.put("f", "I'm in too deep!");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{a}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("{{f}}", processedArticle);
    }

    @Test
    public void testRecursiveTemplate() throws IOException {
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("a", "{{b|letter={{{letter}}}}}");
        templateMap.put("b", "{{a|letter={{{letter}}}}}");

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{a|letter=b}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("b ", processedArticle);
    }

    @Test
    public void testMissingTemplate() throws IOException {
        Map<String, String> templateMap = new HashMap<>();

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{missing|cats=better|than=dogs}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("better dogs ", processedArticle);
    }

    @Test
    public void testTemplateWithFormatting() throws IOException {
        Map<String, String> templateMap = new HashMap<>();

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "I like {{lc:cats|and|also=dogs}}.";
        String processedArticle = processor.substitute(article, "");
        assertEquals("I like cats|and|also=dogs.", processedArticle);
    }

    @Test
    public void testTemplateWithMagic() throws IOException {
        Map<String, String> templateMap = new HashMap<>();

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{#magic: you know|never believe it's not so}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("", processedArticle);
    }

    @Test
    public void testTemplateWithTitleVariable() throws IOException {
        Map<String, String> templateMap = new HashMap<>();

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "I like {{FULLPAGENAME}}.";
        String processedArticle = processor.substitute(article, "cats");
        assertEquals("I like cats.", processedArticle);
    }

    @Test
    public void testTemplateWithTag() throws IOException {
        Map<String, String> templateMap = new HashMap<>();

        TemplateResolutionOptions options = new TemplateResolutionOptions();
        TemplateProcessor processor = new TemplateProcessor(templateMap, options);

        String article = "{{#tag:span|some|spanned text}}";
        String processedArticle = processor.substitute(article, "");
        assertEquals("some|spanned text", processedArticle);
    }
}
