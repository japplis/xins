/*
 * $Id: XMLRPCCallingConventionTests.java,v 1.8 2010/12/11 17:04:40 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.server;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import org.xins.common.xml.ElementList;

import org.xins.tests.AllTests;

/**
 * Tests for calling conventions.
 *
 * @version $Revision: 1.8 $ $Date: 2010/12/11 17:04:40 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class XMLRPCCallingConventionTests extends TestCase {

   /**
    * Constructs a new <code>XMLRPCCallingConventionTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public XMLRPCCallingConventionTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(XMLRPCCallingConventionTests.class);
   }

   /**
    * Tests the XML-RPC calling convention with an incomplete request.
    */
   public void testXMLRPCCallingConvention() throws Exception {
      String destination = AllTests.url() + "allinone/?_convention=_xins-xmlrpc";

      // Send an incorrect request
      String data = "<?xml version=\"1.0\"?>" +
              "<methodCall>" +
              "  <methodName>SimpleTypes</methodName>" +
              "  <params>" +
              "    <param><value><struct><member>" +
              "    <name>inputBoolean</name>" +
              "    <value><boolean>0</boolean></value>" +
              "    </member></struct></value></param>" +
              "  </params>" +
              "</methodCall>";
      Element result = CallingConventionTests.postXML(destination, data);
      assertEquals("methodResponse", result.getTagName());
      Element faultElem = new ElementList(result, "fault").getUniqueChildElement();
      Element valueElem = new ElementList(faultElem, "value").getUniqueChildElement();
      Element structElem = new ElementList(valueElem, "struct").getUniqueChildElement();
      Element member1 = new ElementList(structElem, "member").get(0);
      Element member1Name = new ElementList(member1, "name").getUniqueChildElement();
      assertEquals("faultCode", member1Name.getTextContent());
      Element member1Value = new ElementList(member1, "value").getUniqueChildElement();
      Element member1IntValue = new ElementList(member1Value , "int").getUniqueChildElement();
      assertEquals("3", member1IntValue.getTextContent());
      Element member2 = new ElementList(structElem, "member").get(1);
      Element member2Name = new ElementList(member2, "name").getUniqueChildElement();
      assertEquals("faultString", member2Name.getTextContent());
      Element member2Value = new ElementList(member2, "value").getUniqueChildElement();
      Element member2StringValue = new ElementList(member2Value, "string").getUniqueChildElement();
      assertEquals("_InvalidRequest", member2StringValue.getTextContent());
   }

   /**
    * Tests the XML-RPC calling convention for a successful result.
    */
   public void testXMLRPCCallingConvention2() throws Exception {
      String destination = AllTests.url() + "allinone/?_convention=_xins-xmlrpc";

      // Send a correct request
      String data = "<?xml version=\"1.0\"?>" +
              "<methodCall>" +
              "  <methodName>SimpleTypes</methodName>" +
              "  <params>" +
              "    <param><value><struct><member>" +
              "    <name>inputBoolean</name>" +
              "    <value><boolean>1</boolean></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>inputByte</name>" +
              "    <value><i4>0</i4></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>inputInt</name>" +
              "    <value><i4>50</i4></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>inputLong</name>" +
              "    <value><string>123456460</string></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>inputFloat</name>" +
              "    <value><double>3.14159265</double></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>inputText</name>" +
              "    <value><string>Hello World!</string></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>inputDate</name>" +
              "    <value><dateTime.iso8601>19980717T14:08:55</dateTime.iso8601></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>inputTimestamp</name>" +
              "    <value><dateTime.iso8601>19980817T15:08:55</dateTime.iso8601></value>" +
              "    </member></struct></value></param>" +
              "  </params>" +
              "</methodCall>";
      Element result = CallingConventionTests.postXML(destination, data);
      assertEquals("methodResponse", result.getTagName());
      Element paramsElem = new ElementList(result, "params").getUniqueChildElement();
      Element paramElem = new ElementList(paramsElem, "param").getUniqueChildElement();
      Element valueElem = new ElementList(paramElem, "value").getUniqueChildElement();
      Element structElem = new ElementList(valueElem, "struct").getUniqueChildElement();
   }

   /**
    * Tests the XML-RPC calling convention for a data section.
    */
   public void testXMLRPCCallingConvention3() throws Exception {
      String destination = AllTests.url() + "allinone/?_convention=_xins-xmlrpc";

      // Send a correct request
      String data = "<?xml version=\"1.0\"?>" +
              "<methodCall>" +
              "  <methodName>DataSection3</methodName>" +
              "  <params>" +
              "    <param><value><struct><member>" +
              "    <name>inputText</name>" +
              "    <value><string>hello</string></value>" +
              "    </member></struct></value></param>" +
              "    <param><value><struct><member>" +
              "    <name>data</name>" +
              "    <value><array><data>" +
              "    <value><struct><member>" +
              "    <name>address</name>" +
              "    <value><string></string></value>" +
              "    </member>" +
              "    <member>" +
              "    <name>company</name>" +
              "    <value><string>MyCompany</string></value>" +
              "    </member>" +
              "    <member>" +
              "    <name>postcode</name>" +
              "    <value><string>72650</string></value>" +
              "    </member></struct></value>" +
              "    </data></array></value>" +
              "    </member></struct></value></param>" +
              "  </params>" +
              "</methodCall>";
      Element result = CallingConventionTests.postXML(destination, data);
      assertEquals("methodResponse", result.getTagName());
      Element paramsElem = new ElementList(result, "params").getUniqueChildElement();
      Element paramElem = new ElementList(paramsElem, "param").getUniqueChildElement();
      Element valueElem = new ElementList(paramElem, "value").getUniqueChildElement();
      Element structElem = new ElementList(valueElem, "struct").getUniqueChildElement();
   }
}
