package org.xins.common.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xins.common.MandatoryArgumentChecker;

import org.znerd.xmlenc.XMLOutputter;

/**
 * Converter from Element to String and vice versa.
 *
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0
 */
public class ElementFormatter {

   private ElementFormatter() {
   }

   /**
    * Returns the String representation of the element as XML.
    * The XML declaration is not included.
    *
    * @param element
    *    the element to serialize, cannot be <code>null</code>.
    *
    * @return
    *    the XML representation of the XML or an empty element if an error occured.
    */
   public static String format(Element element) {
      MandatoryArgumentChecker.check("element", element);
      StringWriter output = new StringWriter();
      try {
         XMLOutputter xmlout = new XMLOutputter(output, "UTF-8");
         format(element, xmlout);
         return output.toString();
      } catch (IOException iex) {
         // TODO Log
         return "<" + element.getTagName() + "/>";
      }
   }

   /**
    * Serializes the DOM Element in the XMLOutputter.
    *
    * @param element
    *    the DOM element to serialize, cannot be <code>null</code>.
    *
    * @param xmlout
    *    the XMLOutputer to serialize the element, cannot be <code>null</code>.
    *
    * @throws IOException
    *    if the serialization failed.
    *
    * @since XINS 3.1
    */
   public static void format(Element element, XMLOutputter xmlout) throws IOException {
      MandatoryArgumentChecker.check("element", element, "xmlout", xmlout);

      xmlout.startTag(element.getTagName());

      NamedNodeMap attributes = element.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
         Node attribute = attributes.item(i);
         xmlout.attribute(attribute.getNodeName(), attribute.getNodeValue());
      }
      ElementList children = new ElementList(element);
      for (Element child : children) {
         format(child, xmlout);
      }
      String pcdata = element.getTextContent();
      if (children.isEmpty() && !pcdata.equals("")) {
         xmlout.pcdata(pcdata);
      }
      xmlout.endTag();
   }

   /**
    * Creates a DOM element by parsing the given XML.
    *
    * @param xml
    *    the XML text to parse, cannot be <code>null</code>.
    * @return
    *    the DOM element representing the parsed XML, never <code>null</code>.
    * @throws SAXException
    *    if for any reason the XML cannot be parsed correctly.
    */
   public static Element parse(String xml) throws SAXException {
      return parse(new StringReader(xml));
   }

   /**
    * Creates a DOM element by parsing the given XML location.
    *
    * @param uri
    *    the URI location of the XML resource to parse, cannot be <code>null</code>.
    * @return
    *    the DOM element representing the parsed XML, never <code>null</code>.
    * @throws SAXException
    *    if for any reason the XML cannot be parsed correctly.
    */
   public static Element parseURI(String uri) throws SAXException {
      try {
         DocumentBuilder builder = DocumentBuilderPool.getInstance().getBuilder();
         Document document = builder.parse(uri);
         Element parsedElement = document.getDocumentElement();
         DocumentBuilderPool.getInstance().releaseBuilder(builder);
         return parsedElement;
      } catch (IOException ex) {
         throw new SAXParseException("IO error occured while parsing " + uri + ": " + ex.getMessage(), null);
      }
   }

   /**
    * Creates a DOM element by parsing the XML included in the reader.
    *
    * @param reader
    *    the reader to use to read the XML source, cannot be <code>null</code>.
    * @return
    *    the DOM element representing the parsed XML, never <code>null</code>.
    * @throws SAXException
    *    if for any reason the XML cannot be parsed correctly.
    */
   public static Element parse(Reader reader) throws SAXException {
      try {
         DocumentBuilder builder = DocumentBuilderPool.getInstance().getBuilder();
         InputSource source = new InputSource();
         source.setCharacterStream(reader);
         Document document = builder.parse(source);
         Element parsedElement = document.getDocumentElement();
         DocumentBuilderPool.getInstance().releaseBuilder(builder);
         return parsedElement;
      } catch (IOException ex) {
         throw new SAXParseException("IO error occured while parsing: " + ex.getMessage(), null);
      }
   }


   /**
    * Creates a DOM element by parsing the XML included in the input stream.
    *
    * @param stream
    *    the input stream to use to read the XML source, cannot be <code>null</code>.
    * @return
    *    the DOM element representing the parsed XML, never <code>null</code>.
    * @throws SAXException
    *    if for any reason the XML cannot be parsed correctly.
    */
   public static Element parse(InputStream stream) throws SAXException {
      try {
         DocumentBuilder builder = DocumentBuilderPool.getInstance().getBuilder();
         Document document = builder.parse(stream);
         Element rootElement = document.getDocumentElement();
         DocumentBuilderPool.getInstance().releaseBuilder(builder);
         return rootElement;
      } catch (IOException ex) {
         throw new SAXParseException("IO error occured while parsing: " + ex.getMessage(), null);
      }
   }

   /**
    * Creates an empty DOM element with the specified name.
    *
    * @param name
    *    the local name of the XML element, cannot be <code>null</code>.
    * @return
    *    a new DOM element with the specified name, never <code>null</code>.
    */
   public static Element createMainElement(String name) {
      return createMainElementNS(null, name);
   }

   /**
    * Creates an empty DOM element with the specified name.
    *
    * @param namespaceURI
    *    the namespace URI of the XML element, can be <code>null</code>.
    * @param qualifiedName
    *    the qualified name of the XML element, cannot be <code>null</code>.
    * @return
    *    a new DOM element with the specified name, never <code>null</code>.
    */
   public static Element createMainElementNS(String namespaceURI, String qualifiedName) {
      DocumentBuilder documentBuilder = DocumentBuilderPool.getInstance().getBuilder();
      Document document = documentBuilder.newDocument();
      Element element = null;
      if (namespaceURI == null) {
         element = document.createElement(qualifiedName);
      } else {
         element = document.createElementNS(namespaceURI, qualifiedName);
      }
      document.appendChild(element);
      Element createdElement = document.getDocumentElement();
      DocumentBuilderPool.getInstance().releaseBuilder(documentBuilder);
      return createdElement;
   }
}
