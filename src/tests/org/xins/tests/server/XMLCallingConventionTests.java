/*
 * $Id: XMLCallingConventionTests.java,v 1.8 2010/12/11 17:04:40 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xins.common.text.HexConverter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xins.common.xml.ElementList;

import org.xins.tests.AllTests;

/**
 * Tests for calling conventions.
 *
 * @version $Revision: 1.8 $ $Date: 2010/12/11 17:04:40 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class XMLCallingConventionTests extends TestCase {

   /**
    * Constructs a new <code>XMLCallingConventionTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public XMLCallingConventionTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(XMLCallingConventionTests.class);
   }

   /**
    * Test the XML calling convention.
    */
   public void testXMLCallingConvention() throws Throwable {
      String randomLong = HexConverter.toHexString(CallingConventionTests.RANDOM.nextLong());
      String randomFive = randomLong.substring(0, 5);

      // Successful call
      postXMLRequest(randomFive, true);

      // Unsuccessful call
      postXMLRequest(randomFive, false);
   }

   /**
    * Posts XML request.
    *
    * @param randomFive
    *    A randomly generated String.
    * @param success
    *    <code>true</code> if the expected result should be successfal,
    *    <code>false</code> otherwise.
    *
    * @throws Exception
    *    If anything goes wrong.
    */
   private void postXMLRequest(String randomFive, boolean success) throws Exception {
      String destination = AllTests.url() + "allinone/?_convention=_xins-xml";
      String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
              "<request function=\"ResultCode\">" +
              "  <param name=\"useDefault\">false</param>" +
              "  <param name=\"inputText\">" + randomFive + "</param>" +
              "</request>";
      Element result = CallingConventionTests.postXML(destination, data);
      assertEquals("result", result.getTagName());
      if (success) {
         assertEquals("The method returned an error code: " + result.getAttribute("errorcode"), "", result.getAttribute("errorcode"));
      } else {
         assertNotNull("The method did not return an error code for the second call: " + result.getAttribute("errorcode"), result.getAttribute("errorcode"));
         assertEquals("AlreadySet", result.getAttribute("errorcode"));
      }
      assertEquals("The method returned a code attribute: " + result.getAttribute("code"), "", result.getAttribute("code"));
      assertEquals("The method returned a success attribute.", "", result.getAttribute("success"));
      ElementList child = new ElementList(result);
      assertEquals(1, child.size());
      Element param = (Element) child.get(0);
      assertEquals("param", param.getTagName());
      if (success) {
         assertEquals("outputText", param.getAttribute("name"));
         assertEquals(randomFive + " added.", param.getTextContent());
      } else {
         assertEquals("count", param.getAttribute("name"));
         assertEquals("1", param.getTextContent());
      }
   }
}
