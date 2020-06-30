/*
 * $Id: ElementListTests.java,v 1.3 2012/05/12 08:08:27 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.common.xml;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;

import org.w3c.dom.Element;

import org.xins.common.text.ParseException;
import org.xins.common.xml.DataElementBuilder;
import org.xins.common.xml.DocumentBuilderPool;
import org.xins.common.xml.ElementList;
import org.xml.sax.InputSource;

/**
 * Tests for class <code>ElementList</code>.
 *
 * @version $Revision: 1.3 $ $Date: 2012/05/12 08:08:27 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class ElementListTests extends TestCase {

   /**
    * Constructs a new <code>ElementListTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public ElementListTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(ElementListTests.class);
   }

   /**
    * Tests the <code>ElementList</code> class.
    */
   public void testElementListFromDataElementBuilder() throws Exception {
      DataElementBuilder dataBuilder = new DataElementBuilder();
      Element dataElement = dataBuilder.getDataElement();
      Element test1 = dataBuilder.addToDataElement("test1");
      Element test2 = dataBuilder.addToDataElement("test2");
      assertEquals("test1", dataElement.getFirstChild().getNodeName());
      assertEquals("test2", dataElement.getLastChild().getNodeName());
      Element test3 = dataBuilder.createElement("test3");
      test2.appendChild(test3);
      //assertEquals(3, dataElement.getChildNodes().getLength());

      ElementList testChildren = new ElementList(dataElement);
      assertEquals(2, testChildren.size());
      assertNotNull(testChildren.getFirstChildElement());

      ElementList test2Children = new ElementList(test2);
      assertEquals(1, test2Children.size());
      assertEquals(test3, test2Children.getUniqueChildElement());
      assertEquals(test3, test2Children.getFirstChildElement());

      ElementList test1Children = new ElementList(test1);
      assertTrue(test1Children.isEmpty());
      assertNull(test1Children.getFirstChildElement());
      try {
         test1Children.getUniqueChildElement();
         fail("The list is empty so no child should be returned");
      } catch (ParseException pex) {
         // as expected
      }

      ElementList test2bChildren = new ElementList(test2, "test");
      assertTrue(test2bChildren.isEmpty());
      assertNull(test2bChildren.getFirstChildElement());
      try {
         test2bChildren.getUniqueChildElement();
         fail("The list is empty so no child should be returned");
      } catch (ParseException pex) {
         // as expected
      }
   }
   
   public void testElementListFromXML() throws Exception {
      String xml1 = "<xml><element>one</element><element>two<element>three</element></element></xml>";
      DocumentBuilder xmlBuilder = DocumentBuilderPool.getInstance().getBuilder();
      Document rootDoc = xmlBuilder.parse(new InputSource(new StringReader(xml1)));
      Element rootElement = rootDoc.getDocumentElement();

      ElementList list1 = new ElementList(rootElement);
      Assert.assertEquals(2, list1.size());
      Assert.assertEquals("one", list1.get(0).getTextContent());
      Assert.assertEquals("one", list1.getFirstChildElement().getTextContent());
      Assert.assertEquals("twothree", list1.get(1).getTextContent());
      try {
         list1.getUniqueChildElement();
         fail("Should have thrown exception");
      } catch (ParseException ex) {
      }

      ElementList list2 = new ElementList(list1.get(1));
      Assert.assertEquals(1, list2.size());
      Assert.assertEquals("three", list2.getUniqueChildElement().getTextContent());
   }

   public void testElementListFromXML2() throws Exception {
      DocumentBuilder xmlBuilder = DocumentBuilderPool.getInstance().getBuilder();
      String xml2 = "<xml><element>one</element><element>two<element>three</element><other>otro</other></element></xml>";
      Document rootDoc2 = xmlBuilder.parse(new InputSource(new StringReader(xml2)));
      Element rootElem2 = rootDoc2.getDocumentElement();
      Element secondElem = new ElementList(rootElem2).get(1);
      ElementList list3 = new ElementList(secondElem, "other");
      Assert.assertEquals(1, list3.size());
      Assert.assertEquals("otro", list3.getUniqueChildElement().getTextContent());

      ElementList list4 = new ElementList(secondElem, "*");
      Assert.assertEquals(2, list4.size());
      Assert.assertEquals("three", list4.get(0).getTextContent());
   }
}
