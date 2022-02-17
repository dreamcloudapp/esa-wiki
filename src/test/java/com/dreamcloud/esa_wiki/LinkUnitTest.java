package com.dreamcloud.esa_wiki;

import com.dreamcloud.esa_wiki.annoatation.linkParser.Link;
import com.dreamcloud.esa_wiki.annoatation.linkParser.LinkParameter;
import org.junit.*;
import static org.junit.Assert.*;

public class LinkUnitTest {
    public LinkUnitTest() {

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

    @Test
    public void testInstantiation() {
        new Link("target");
    }

    @Test
    public void testTarget() {
        Link link = new Link("Cat");
        assertEquals("Cat", link.getTarget());

        link = new Link("Cats and dogs");
        assertEquals("Cats and dogs", link.getTarget());

        link = new Link("Cats and dogs#Comparison");
        assertEquals("Cats and dogs#Comparison", link.getTarget());
        assertEquals("Cats and dogs", link.getTargetArticle());
        assertEquals("Comparison", link.getTargetSection());
    }

    @Test
    public void testAnchor() {
        Link link = new Link("Cat", "Feline");
        assertEquals("Feline", link.getAnchor());

        link = new Link("Cats and dogs");
        assertEquals("Cats and dogs", link.getAnchor());

        link = new Link("Cats and dogs#Comparison");
        assertEquals("Cats and dogs", link.getAnchor());
    }

    @Test
    public void testText() {
        Link link = new Link("Cat", "Feline");
        link.setText("test");
        assertEquals("test", link.getText());
    }

    @Test
    public void testParameters() {
        Link link = new Link("Image:Cat", "A picture of a cute cat.");
        link.addParameter(new LinkParameter("width", "250px"));
        assertEquals(1, link.getParameters().size());
    }

    @Test
    public void testResource() {
        Link link = new Link("File:Cat", "A picture of a cute cat.");
        assertTrue(link.isResource());

        link = new Link("Image:Cat", "A picture of a cute cat.");
        assertTrue(link.isResource());

        link = new Link("ImageCat", "A picture of a cute cat.");
        assertFalse(link.isResource());
    }

    @Test
    public void testSelfLink() {
        Link link = new Link("#History");
        assertTrue(link.isSelfLink());

        link = new Link("Image:Cat", "A picture of a cute cat.");
        assertFalse(link.isSelfLink());
    }

    @Test
    public void testIsNamespaced() {
        Link link = new Link("Category:Cat");
        assertTrue(link.isNamespaced());

        link = new Link("Image:Cat", "A picture of a cute cat.");
        assertFalse(link.isNamespaced());
    }

    @Test
    public void testSelfLinkSplit() {
        Link link = new Link("#");
        assertTrue(link.isSelfLink());
    }
}
