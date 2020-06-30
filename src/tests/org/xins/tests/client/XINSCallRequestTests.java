/*
 * $Id: XINSCallRequestTests.java,v 1.14 2010/10/25 20:36:51 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.client;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.NDC;

import org.xins.client.XINSCallRequest;

import org.xins.common.http.HTTPMethod;

/**
 * Tests for class <code>XINSCallRequest</code>.
 *
 * @version $Revision: 1.14 $ $Date: 2010/10/25 20:36:51 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class XINSCallRequestTests extends TestCase {

   /**
    * Constructs a new <code>XINSCallRequestTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public XINSCallRequestTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(XINSCallRequestTests.class);
   }

   /**
    * Tests the behaviour of the <code>XINSCallRequestTests</code>
    * class.
    *
    * @throws Exception
    *    if an unexpected exception is thrown.
    */
   public void testXINSCallRequest() throws Exception {

      // Test first constructor
      try {
         new XINSCallRequest(null, null);
         fail("XINSCallRequest(null, null) should throw an IllegalArgumentException.");
      } catch (IllegalArgumentException e) {
         // as expected
      }
      try {
         new XINSCallRequest(null, new HashMap<String, String>());
         fail("XINSCallRequest(null, new HashMap<String, String>()) should throw an IllegalArgumentException.");
      } catch (IllegalArgumentException e) {
         // as expected
      }

      final int constructorCount = 3;
      XINSCallRequest[] r = new XINSCallRequest[constructorCount];

      String functionName = "SomeFunction";
      r[0] = new XINSCallRequest(functionName, null);
      r[1] = new XINSCallRequest(functionName, null, false);
      r[2] = new XINSCallRequest(functionName, null, false, null);

      for (int i = 0; i < constructorCount; i++) {
         assertEquals(functionName, r[i].getFunctionName());
         if (r[i].describe().indexOf(functionName) < 0) {
            fail("XINSCallRequest.describe() should return a string that contains the function name. Function name is: \"" + functionName + "\". Description is: \"" + r[i].describe() + "\".");
         }
      }

      String contextID = "f54b715f249bd02c";
      NDC.push("f54b715f249bd02c");
      Map<String, String> p = new HashMap<String, String>();
      p.put("channel",     "USR_REG_WEB_W");
      p.put("lineType",    "PSTN");
      p.put("postCode",    "1011PZ");
      p.put("houseNumber", "1");
      XINSCallRequest req = new XINSCallRequest("GetUpgradePlanList", p, false, HTTPMethod.POST);
      NDC.pop();
      NDC.remove();

      if (req.describe().indexOf(contextID) < 0) {
         fail("XINSCallRequest.describe() should return a string that contains the diagnostic context ID. Context ID is: \"" + contextID + "\". Description is: \"" + req.describe() + "\".");
      }
   }
}
