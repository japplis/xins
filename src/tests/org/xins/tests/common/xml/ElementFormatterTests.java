/*
 * $Id: ElementFormatterTests.java,v 1.4 2013/01/29 11:03:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.common.xml;

import java.io.StringReader;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Element;

import org.xins.common.xml.ElementFormatter;
import org.xins.common.xml.ElementList;
import org.xml.sax.SAXException;

/**
 * Tests for class <code>ElementFormatterTests</code>.
 *
 * @version $Revision: 1.4 $ $Date: 2013/01/29 11:03:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class ElementFormatterTests extends TestCase {

   /**
    * Constructs a new <code>ElementFormatterTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public ElementFormatterTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(ElementFormatterTests.class);
   }

   /**
    * Tests the <code>ElementFormatter</code> class.
    */
   public void testElementFormatter() throws Exception {
      String xml = "<test/>";
      Element test = ElementFormatter.parse(xml);

      assertEquals(0, test.getChildNodes().getLength());

      String backToXML = ElementFormatter.format(test);
      assertTrue("Incorrect result: " + backToXML, backToXML.equals("<test/>") || backToXML.equals("<test></test>"));
   }

   /**
    * Tests the <code>ElementParser</code> class.
    */
   public void testElementParser() throws Exception {

      // Parse an XML string with namespaces
      String s = "<ns:a xmlns:ns=\"b\" c='2'><ns:e><g xmlns='f'/><h></h></ns:e></ns:a>";
      Element element = ElementFormatter.parse(new StringReader(s));
      assertNotNull(element);
      String parsedElementAsString = ElementFormatter.format(element);

      // Parse root 'a' element
      assertEquals(parsedElementAsString, "a",  element.getLocalName());
      assertEquals(parsedElementAsString, "b",  element.getNamespaceURI());
      assertEquals(parsedElementAsString, 2,    element.getAttributes().getLength());
      assertEquals(parsedElementAsString, "2",  element.getAttribute("c"));
      assertEquals(parsedElementAsString, 0,    element.getElementsByTagName("d").getLength());
      assertEquals(parsedElementAsString, 0,    element.getElementsByTagName("d:e").getLength());
      assertEquals(parsedElementAsString, "",   element.getTextContent());

      // Parse contained 'e' element
      ElementList aChildren = new ElementList(element);
      assertEquals(parsedElementAsString, 1, aChildren.size());
      Element eChild = (Element) aChildren.get(0);
      assertEquals(parsedElementAsString, "e",  eChild.getLocalName());
      assertEquals(parsedElementAsString, "b",  eChild.getNamespaceURI());
      assertEquals(parsedElementAsString, 2,    eChild.getElementsByTagName("*").getLength());
      assertEquals(parsedElementAsString, 0,    eChild.getElementsByTagName("d:g").getLength());
      assertEquals(parsedElementAsString, 1,    eChild.getElementsByTagName("h").getLength());
      assertEquals(parsedElementAsString, "",   eChild.getTextContent());

      // Parse contained 'g' element
      ElementList eChildren = new ElementList(eChild);
      assertEquals(parsedElementAsString, 2, eChildren.size());
      Element gChild = (Element) eChildren.get(0);
      Element hChild = (Element) eChildren.get(1);
      assertEquals(parsedElementAsString, "g",  gChild.getLocalName());
      assertEquals(parsedElementAsString, "f",  gChild.getNamespaceURI());
      assertEquals(parsedElementAsString, 0,    gChild.getElementsByTagName("*").getLength());
      assertEquals(parsedElementAsString, "",   gChild.getTextContent());
      assertEquals(parsedElementAsString, "h",  hChild.getLocalName());
      assertEquals(parsedElementAsString, null, hChild.getNamespaceURI());
      assertEquals(parsedElementAsString, 0,    hChild.getElementsByTagName("*").getLength());
      assertEquals(parsedElementAsString, "",   hChild.getTextContent());

      // Test the getUniqueChildElement
      assertTrue(aChildren.getUniqueChildElement() == eChild);
   }
}
