/*
 * $Id: SOAPMapCallingConventionTests.java,v 1.6 2010/12/11 17:04:40 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.server;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xins.common.text.HexConverter;
import org.w3c.dom.Element;
import org.xins.common.xml.ElementList;

import org.xins.tests.AllTests;

/**
 * Tests for calling conventions.
 *
 * @version $Revision: 1.6 $ $Date: 2010/12/11 17:04:40 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class SOAPMapCallingConventionTests extends TestCase {

   /**
    * Constructs a new <code>SOAPMapCallingConventionTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public SOAPMapCallingConventionTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(SOAPMapCallingConventionTests.class);
   }

   /**
    * Tests the SOAP calling convention.
    */
   public void testSOAPMapCallingConvention() throws Throwable {
      String randomLong = HexConverter.toHexString(CallingConventionTests.RANDOM.nextLong());
      String randomFive = randomLong.substring(0, 5);

      // Successful call
      postSOAPRequest(randomFive, true);

      // Unsuccessful call
      //postSOAPRequest(randomFive, false);
   }

   /**
    * Posts SOAP request.
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
   private void postSOAPRequest(String randomFive, boolean success) throws Exception {
      String destination = AllTests.url() + "allinone/?_convention=_xins-soap-map";
      String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
              "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" soap:encodingStyle=\"http://www.w3.org/2001/12/soap-encoding\">" +
              "  <soap:Body xmlns:m=\"http://www.example.org/stock\">" +
              "    <m:ResultCodeRequest>" +
              "      <m:useDefault>false</m:useDefault>" +
              "      <m:inputText>" + randomFive + "</m:inputText>" +
              "    </m:ResultCodeRequest>" +
              "  </soap:Body>" +
              "</soap:Envelope>";
      int expectedStatus = success ? 200 : 500;
      Element result = CallingConventionTests.postXML(destination, data, expectedStatus);
      assertEquals("Envelope", result.getLocalName());
      assertEquals("soap", result.getPrefix());
      assertEquals("http://schemas.xmlsoap.org/soap/envelope/", result.getNamespaceURI());
      //Element.QualifiedName encodingStyle = new Element.QualifiedName("soap", "http://schemas.xmlsoap.org/soap/envelope/", "encodingStyle");
      assertEquals("http://www.w3.org/2001/12/soap-encoding", result.getAttributeNS("http://schemas.xmlsoap.org/soap/envelope/", "encodingStyle"));
      assertEquals("Incorrect number of \"Fault\" elements.", 0, new ElementList(result, "Fault").size());
      assertEquals("Incorrect number of \"Body\" elements.", 1, new ElementList(result, "Body").size());
      Element bodyElem = new ElementList(result, "Body").getUniqueChildElement();
      if (success) {
         assertEquals("Incorrect number of response elements.", 1, new ElementList(bodyElem, "ResultCodeResponse").size());
         assertEquals("Incorrect namespace prefix of the response:" + bodyElem.getPrefix(), "soap", bodyElem.getPrefix());
         Element responseElem = (Element) new ElementList(bodyElem, "ResultCodeResponse").get(0);
         assertEquals("Incorrect number of \"outputText\" elements.", 1, new ElementList(responseElem, "outputText").size());
         Element outputTextElem = (Element) new ElementList(responseElem, "outputText").get(0);
         assertEquals("Incorrect returned text", randomFive + " added.", outputTextElem.getTextContent());
         assertNull("Incorrect namespace prefix of the outputText.", outputTextElem.getPrefix());
      } else {
         assertEquals("Incorrect number of \"Fault\" elements.", 1, new ElementList(bodyElem, "Fault").size());
         Element faultElem = (Element) new ElementList(bodyElem, "Fault").get(0);
         assertEquals("Incorrect number of \"faultcode\" elements.", 1, new ElementList(faultElem, "faultcode").size());
         Element faultCodeElem = (Element) new ElementList(faultElem, "faultcode").get(0);
         assertEquals("Incorrect faultcode text", "soap:Server", faultCodeElem.getTextContent());
         assertEquals("Incorrect number of \"faultstring\" elements.", 1, new ElementList(faultElem, "faultstring").size());
         Element faultStringElem = (Element) new ElementList(faultElem, "faultstring").get(0);
         assertEquals("Incorrect faultstring text", "AlreadySet", faultStringElem.getTextContent());
      }
   }

   /**
    * Tests the SOAP calling convention for the type convertion.
    */
   public void testSOAPMapCallingConvention2() throws Throwable {
      String destination = AllTests.url() + "allinone/?_convention=_xins-soap-map";
      String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
              "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
              "  <SOAP-ENV:Body>" +
              "    <gs:SimpleTypes xmlns:gs=\"urn:WhatEver\">" +
              "      <inputBoolean>0</inputBoolean>" +
              "      <inputByte>0</inputByte>" +
              "      <inputInt>0</inputInt>" +
              "      <inputLong>0</inputLong>" +
              "      <inputFloat>1.0</inputFloat>" +
              "      <inputText>0</inputText>" +
              "    </gs:SimpleTypes>" +
              "  </SOAP-ENV:Body>" +
              "</SOAP-ENV:Envelope>";
      Element result = CallingConventionTests.postXML(destination, data);
      assertEquals("Envelope", result.getLocalName());
      assertEquals("SOAP-ENV", result.getPrefix());
      assertEquals("Incorrect number of \"Fault\" elements.", 0, new ElementList(result, "Fault").size());
      assertEquals("Incorrect number of \"Body\" elements.", 1, new ElementList(result, "Body").size());
      Element bodyElem = new ElementList(result, "Body").getUniqueChildElement();
      Element responseElem = new ElementList(bodyElem, "SimpleTypesResponse").getUniqueChildElement();
      assertEquals("Incorrect response namespace prefix.", "gs", responseElem.getPrefix());
   }

   /**
    * Tests the SOAP calling convention with a data section.
    */
   public void testSOAPMapCallingConvention3() throws Throwable {
      String destination = AllTests.url() + "allinone/?_convention=_xins-soap-map";
      String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
              "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns0=\"urn:allinone\">" +
              "  <soap:Body>" +
              "    <ns0:DataSection3Request>" +
              "      <address><company>McDo</company><postcode>1234</postcode></address>" +
              "      <address><company>Drill</company><postcode>4567</postcode></address>" +
              "    </ns0:DataSection3Request>" +
              "  </soap:Body>" +
              "</soap:Envelope>";
      Element result = CallingConventionTests.postXML(destination, data);
      assertEquals("Envelope", result.getLocalName());
      assertEquals("Incorrect number of \"Fault\" elements.", 0, new ElementList(result, "Fault").size());
      assertEquals("Incorrect number of \"Body\" elements.", 1, new ElementList(result, "Body").size());
      Element bodyElem = (Element) new ElementList(result, "Body").get(0);
      assertEquals("Incorrect number of response elements.", 1, new ElementList(bodyElem, "DataSection3Response").size());
   }
}
