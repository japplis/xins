/*
 * $Id: JSONRPC2CallingConventionTests.java,v 1.1 2013/01/23 09:59:40 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.json.JSONObject;

import org.xins.common.text.HexConverter;

import org.xins.tests.AllTests;

/**
 * Tests for calling conventions.
 *
 * @version $Revision: 1.1 $ $Date: 2013/01/23 09:59:40 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class JSONRPC2CallingConventionTests extends TestCase {

   /**
    * Constructs a new <code>XMLCallingConventionTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public JSONRPC2CallingConventionTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(JSONRPC2CallingConventionTests.class);
   }

   /**
    * Test the JSON calling convention using the 1.0 specifications.
    */
   public void testJSONRPC2CallingConventionEcho() throws Throwable {
      String destination = AllTests.url() + "allinone/?_convention=_xins-jsonrpc2";
      String input = "{ \"jsonrpc\" : \"2.0\", \"method\"  : \"Echo\", \"params\"  : { \"in\" : \"test-jsonrpc2\" }, \"id\": 4 }";
      String jsonResult = CallingConventionTests.postData(destination, input, "application/json", 200);
      // System.err.println("json rpc 2.0: " + jsonResult);
      JSONObject jsonObject = new JSONObject(jsonResult);
      String version = jsonObject.getString("jsonrpc");
      assertEquals("2.0", version);
      JSONObject result = jsonObject.optJSONObject("result");
      String out = result.getString("out");
      assertEquals("test-jsonrpc2", out);
   }

   /**
    * Test the JSON calling convention using the 2.0 specifications.
    */
   public void testJSONRPCCallingConvention2() throws Throwable {
      String randomLong = HexConverter.toHexString(CallingConventionTests.RANDOM.nextLong());
      String randomFive = randomLong.substring(0, 5);

      // Successful call
      postJSONRPCRequest2(randomFive, true);

      // Unsuccessful call
      postJSONRPCRequest2(randomFive, false);
   }

   /**
    * Posts JSON-RPC 2.0 request.
    *
    * @param randomFive
    *    A randomly generated String.
    * @param success
    *    <code>true</code> if the expected result should be successful,
    *    <code>false</code> otherwise.
    *
    * @throws Throwable
    *    If anything goes wrong.
    */
   private void postJSONRPCRequest2(String randomFive, boolean success) throws Throwable {
      String destination = AllTests.url() + "allinone/?_convention=_xins-jsonrpc2";
      String input = "{ \"jsonrpc\" : \"2.0\", \"method\"  : \"ResultCode\", \"params\"  : { \"useDefault\" : false, \"inputText\" : \"" + randomFive + "\" }, \"id\": 4 }";
      String jsonResult = CallingConventionTests.postData(destination, input, "application/json", success ? 200 : 500);
      // System.err.println("1_1: " + jsonResult);
      JSONObject jsonObject = new JSONObject(jsonResult);
      String version = jsonObject.getString("jsonrpc");
      assertEquals("2.0", version);
      JSONObject error = jsonObject.optJSONObject("error");
      if (success) {
         JSONObject result = jsonObject.getJSONObject("result");
         String outputText = result.getString("outputText");
         assertEquals("Incorrect result received: " + outputText, randomFive + " added.", outputText);
         assertNull(error);
      } else {
         assertNotNull(error);
         int errorCode = error.getInt("code");
         assertTrue(errorCode < 1000);
         String errorMessage = error.getString("message");
         assertEquals("Unexpected error message: " + errorMessage, "The parameter has already been given.", errorMessage);
      }
   }
}
