package com.dreamcloud.esa_wiki.annoatation;

import com.dreamcloud.esa_wiki.annoatation.handler.XmlReadingHandler;
import com.dreamcloud.esa_wiki.fs.BZipFileTools;
import com.dreamcloud.esa_wiki.utility.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.Map;

/**
 * Takes an annotated Wikipedia XML file and writes a text file mapping the article IDs
 * to their normalized titles.
 * The format for each line is as follows:
 * int ID bytes|normalized title bytes|\n
 * where the | is not actually stored in the file
 *
 * This is useful for finding out which articles correspond to their IDs later on
 * as only the IDs are written to save disk space.
 */
public class IdTitleWriter extends XmlReadingHandler {
    protected final SAXParserFactory saxFactory;
    protected DataOutputStream idTitleOutputStream;
    File inputFile;

    public IdTitleWriter(File inputFile) {
        this.inputFile = inputFile;
        saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(false);
        saxFactory.setXIncludeAware(true);
    }

    public void writeTitles(File outputFile) throws IOException, ParserConfigurationException, SAXException {
        OutputStream outputStream = new FileOutputStream(outputFile);
        this.idTitleOutputStream = new DataOutputStream(outputStream);
        this.parse();

        System.out.println("----------------------------------------");
        System.out.println("ID-titles written:\t" + getDocsRead());
        System.out.println("----------------------------------------");
    }

    protected void parse() throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = saxFactory.newSAXParser();
        Reader reader = BZipFileTools.getFileReader(inputFile);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is, this);
        reader.close();
    }

    @Override
    protected void handleDocument(Map<String, String> xmlFields) {
        try {
            String title = StringUtils.normalizeWikiTitle(xmlFields.get("title"));
            int id = Integer.parseInt(xmlFields.get("id"));
            this.idTitleOutputStream.writeInt(id);
            this.idTitleOutputStream.write(title.getBytes());
            this.idTitleOutputStream.write('\n');
            this.logMessage("id-title mapped\t[" + getDocsRead() + "]");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void close() throws Exception {
        this.idTitleOutputStream.close();
        super.close();
    }
}
