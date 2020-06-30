/*
 * $Id: DataElementBuilderTests.java,v 1.1 2011/01/19 18:13:39 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.common.xml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xins.common.xml.DataElementBuilder;
import org.xins.common.xml.ElementFormatter;

/**
 * Tests for class <code>DataElementBuilder</code>.
 *
 * @version $Revision: 1.1 $ $Date: 2011/01/19 18:13:39 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class DataElementBuilderTests extends TestCase {

   /**
    * Constructs a new <code>DataElementBuilderTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public DataElementBuilderTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(DataElementBuilderTests.class);
   }

   /**
    * Tests the <code>DataElementBuilder</code> class.
    */
   public void testDataElementBuilder() throws Exception {
      DataElementBuilder dataBuilder = new DataElementBuilder();
      Element dataElement = dataBuilder.getDataElement();
      Document dataDocument = dataBuilder.getDocument();

      assertNotNull(dataElement);
      assertNotNull(dataDocument);
      assertEquals(dataDocument.getDocumentElement(), dataElement);
      assertEquals(dataDocument, dataElement.getOwnerDocument());

      dataBuilder.addToDataElement("test1");
      dataBuilder.addToDataElement("test2");
      assertEquals(2, dataElement.getChildNodes().getLength());
      assertEquals("test1", dataElement.getFirstChild().getNodeName());
      assertEquals("test2", dataElement.getLastChild().getNodeName());

      Element test3 = dataBuilder.createElement("test3");
      assertEquals(2, dataElement.getChildNodes().getLength());
      dataBuilder.addToDataElement(test3);
      assertEquals(3, dataElement.getChildNodes().getLength());

      String xml  = dataBuilder.toString();
      Element parsed = ElementFormatter.parse(xml);
      assertEquals(3, parsed.getChildNodes().getLength());
   }
}
