package com.dreamcloud.esa_wiki;

import com.dreamcloud.esa_wiki.annoatation.linkParser.Link;
import com.dreamcloud.esa_wiki.annoatation.linkParser.LinkParser;
import org.junit.*;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static org.junit.Assert.*;

public class LinkParserUnitTest {
    public LinkParserUnitTest() {

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

    public PushbackReader getReader(String text) {
        return new PushbackReader(new StringReader(text));
    }

    @Test
    public void testInstantiation() {
        new LinkParser(getReader(""));
    }

    public void assertLink(Link link, String target, String anchor, String section) {
        assertNotNull(link);
        assertEquals(target, link.getTargetArticle());
        assertEquals(anchor, link.getAnchor());
        assertEquals(section, link.getTargetSection());
    }

    @Test
    public void testSimpleLink() throws IOException {
        LinkParser parser = new LinkParser(getReader("[[Cat]]"));
        Link link = parser.parse();
        assertLink(link, "Cat", "Cat", null);
    }

    @Test
    public void testSimpleLinkWithAnchor() throws IOException {
        LinkParser parser = new LinkParser(getReader("[[Cat|cat]]"));
        Link link = parser.parse();
        assertLink(link, "Cat", "cat", null);
    }

    @Test
    public void testSimpleLinkWithAnchorAndSection() throws IOException {
        LinkParser parser = new LinkParser(getReader("[[Cat#History|cat]]"));
        Link link = parser.parse();
        assertLink(link, "Cat", "cat", "History");
    }

    @Test
    public void testMultipleLinks() throws IOException {
        LinkParser parser = new LinkParser(getReader("[[Cat|cat]][[Cat#History|cat]]"));
        Link link1 = parser.parse();
        assertLink(link1, "Cat", "cat", null);

        Link link2 = parser.parse();
        assertLink(link2, "Cat", "cat", "History");
    }

    @Test
    public void testLinkWithMultipleParameters() throws IOException {
        LinkParser parser = new LinkParser(getReader("[[Image:Cat|thumb|275px|A picture of a cat.]]"));
        Link link = parser.parse();
        assertLink(link, "Image:Cat", "A picture of a cat.", null);
    }

    @Test
    public void testLinkText() throws IOException {
        LinkParser parser = new LinkParser(getReader("''This article deals with the history and development of the different sports around the world known as \"football\". For links to articles on each of these codes of football, please see the list in the [[Football#Football today|Football today]] section of this article."));
        Link link = parser.parse();
        assertEquals("[[Football#Football today|Football today]]", link.getText());
    }

    @Test
    public void testRealisticLink() throws IOException {
        LinkParser parser = new LinkParser(getReader("''This article deals with the history and development of the different sports around the world known as \"football\". For links to articles on each of these codes of football, please see the list in the [[Football#Football today|Football today]] section of this article."));
        Link link = parser.parse();
        assertLink(link, "Football", "Football today", "Football today");
    }

    @Test
    public void testNestedLink() throws IOException {
        LinkParser parser = new LinkParser(getReader("[[Image:Australianfootball1866.jpg|right|thumb|275px|An Australian rules football Australian rules football match at the [[Yarra Park|Richmond Paddock]], Melbourne Melbourne, in 1866 1866. (A [[wood engraving]] by Robert Bruce.)]]"));
        Link link = parser.parse();
        assertEquals("[[Image:Australianfootball1866.jpg|right|thumb|275px|An Australian rules football Australian rules football match at the [[Yarra Park|Richmond Paddock]], Melbourne Melbourne, in 1866 1866. (A [[wood engraving]] by Robert Bruce.)]]", link.getText());

        assertEquals("An Australian rules football Australian rules football match at the [[Yarra Park|Richmond Paddock]], Melbourne Melbourne, in 1866 1866. (A [[wood engraving]] by Robert Bruce.)", link.getAnchor());
    }
}
