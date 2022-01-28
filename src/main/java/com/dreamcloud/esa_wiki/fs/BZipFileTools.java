package com.dreamcloud.esa_wiki.fs;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BZipFileTools {
    public static Reader getFileReader(File file) throws IOException {
        InputStream wikiInputStream = new FileInputStream(file);
        InputStream bufferedInputStream = new BufferedInputStream(wikiInputStream);
        InputStream bzipInputStream = new BZip2CompressorInputStream(bufferedInputStream, true);
        return new InputStreamReader(bzipInputStream, StandardCharsets.UTF_8);
    }

    public static XMLStreamWriter getXmlWriter(File file) throws IOException, XMLStreamException {
        OutputStream outputStream = new FileOutputStream(file);
        outputStream = new BufferedOutputStream(outputStream);
        outputStream = new BZip2CompressorOutputStream(outputStream);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8");
        return new WikiXmlStreamWriter(writer, outputStream);
    }
}
