/*
 * $Id: CallingConventionTests.java,v 1.79 2013/01/22 15:13:22 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.server;

import java.io.*;
import java.net.*;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.xins.common.http.HTTPCallRequest;
import org.xins.common.http.HTTPCallResult;
import org.xins.common.http.HTTPServiceCaller;
import org.xins.common.service.TargetDescriptor;
import org.w3c.dom.Element;
import org.xins.common.xml.ElementFormatter;

import org.xins.tests.AllTests;

/**
 * Tests for calling conventions.
 *
 * @version $Revision: 1.79 $ $Date: 2013/01/22 15:13:22 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class CallingConventionTests extends TestCase {

   /**
    * The random number generator.
    */
   public final static Random RANDOM = new Random();

   /**
    * Constructs a new <code>CallingConventionTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public CallingConventionTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(CallingConventionTests.class);
   }

   /**
    * Tests with an unknown calling convention.
    */
   public void testInvalidCallingConvention() throws Throwable {
      TargetDescriptor descriptor = new TargetDescriptor(AllTests.url(), 2000);
      Map<String, String> params = new HashMap<String, String>();
      params.put("_function", "ResultCode");
      params.put("inputText", "blablabla");
      params.put("_convention", "_xins-bla");
      HTTPCallRequest request = new HTTPCallRequest(params);
      HTTPServiceCaller caller = new HTTPServiceCaller(descriptor);

      HTTPCallResult result = caller.call(request);
      assertEquals(400, result.getStatusCode());
   }

   /**
    * Test the custom calling convention.
    */
   public void testCustomCallingConvention() throws Exception {
      URL url = new URL(AllTests.url() + "?query=hello%20Custom&_convention=xins-tests");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.connect();
      assertEquals(200, connection.getResponseCode());
      URL url2 = new URL(AllTests.url() + "?query=hello%20Custom&_convention=xins-tests");
      HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
      connection2.connect();
      try {
         assertEquals(400, connection2.getResponseCode());
      } catch (IOException ioe) {
         assertTrue(ioe.getMessage(), ioe.getMessage().indexOf(" 400") != -1);
      }
   }

   /**
    * Call the ResultCode function with the specified calling convention.
    * Parameters are pass in as URL parameters.
    *
    * @param convention
    *    the name of the calling convention parameter, or <code>null</code>
    *    if no calling convention parameter should be sent.
    *
    * @param inputText
    *    the value of the parameter to send as input.
    *
    * @return
    *    the data returned by the API.
    *
    * @throw Throwable
    *    if anything goes wrong.
    */
   static String callResultCode(String convention, String inputText) throws Throwable {
      TargetDescriptor descriptor = new TargetDescriptor(AllTests.url() + "allinone/", 2000);
      Map<String, String> params = new HashMap<String, String>();
      params.put("_function", "ResultCode");
      params.put("useDefault", "false");
      params.put("inputText",  inputText);
      if (convention != null) {
         params.put("_convention", convention);
      }
      HTTPCallRequest request = new HTTPCallRequest(params);
      HTTPServiceCaller caller = new HTTPServiceCaller(descriptor);

      HTTPCallResult result = caller.call(request);
      if (result.getStatusCode() != 200) {
         throw new IOException("Received HTTP code " + result.getStatusCode());
      }
      return result.getString();
   }

   /**
    * Tests the HTTP OPTIONS method.
    */
   public void testOptionsMethod() throws Exception {

      String[] yes = new String[] { "GET", "HEAD", "POST", "OPTIONS" };
      String[] no  = new String[] { "CONNECT", "PUT", "DELETE" };
      doTestOptions("*",                        yes, no);
      doTestOptions("/?_convention=_xins-std",  yes, no);

      yes = new String[] { "POST", "OPTIONS" };
      no  = new String[] { "CONNECT", "PUT", "DELETE", "GET", "HEAD" };
      doTestOptions("/?_convention=_xins-soap", yes, no);
   }

   /**
    * Tests that calling DataSection with _xins-xmlrpc get access denied from the ACL.
    */
   public void testDeniedConvention() throws Exception {
      String destination = AllTests.url() + "allinone/?_convention=_xins-xmlrpc";

      // Send a correct request
      String data = "<?xml version=\"1.0\"?>" +
              "<methodCall>" +
              "  <methodName>DataSection</methodName>" +
              "  <params>" +
              "    <param><value><struct><member>" +
              "    <name>inputText</name>" +
              "    <value><string>hello</string></value>" +
              "    </member></struct></value></param>" +
              "  </params>" +
              "</methodCall>";
      postXML(destination, data, 403);
   }

   private void doTestOptions(String queryString, String[] yes, String[] no)
   throws Exception {

      String     host   = AllTests.host();
      int        port   = AllTests.port();
      String     method = "OPTIONS";
      Properties headers = new Properties();
      headers.put("Content-Length", "0");

      // Call the server
      HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);

      // Expect 200 OK
      assertEquals("Expected 200 OK in response to HTTP OPTIONS request to host \""
            + host + "\", port " + port + ", query string \"" + queryString + "\".",
            "200 OK", result.getStatus());

      // Expect an empty body
      String body = result.getBody();
      assertTrue("Expected no body in response to an HTTP OPTIONS request.", body == null || body.length() < 1);

      // Expect "Accept" field in the response (case-insensitive)
      List acceptHeaders = result.getHeaderValues("accept");
      assertTrue("Expected one \"Accept\" header in response to an HTTP OPTIONS request. Received " + acceptHeaders.size() + '.', acceptHeaders.size() == 1);

      // Make sure field is not empty
      String acceptHeader = (String) acceptHeaders.get(0);
      assertTrue("Expected \"Accept\" header in response to HTTP OPTIONS request to have a non-empty value.", acceptHeader.trim().length() > 0);

      // Split the list of acceptable HTTP methods
      List acceptValues = Arrays.asList(acceptHeader.split("[ ]*,[ ]*"));

      // Make sure all expected HTTP methods are in the list
      for (int i = 0; i < yes.length; i++) {
         assertTrue("Expected \"Accept\" header in response to HTTP OPTIONS request to indicate the \"" + yes[i] + "\" method is supported. Instead the response is \"" + acceptHeader + "\".", acceptValues.contains(yes[i]));
      }

      // Make sure all forbidden HTTP methods are not in the list
      for (int i = 0; i < no.length; i++) {
         assertFalse("Expected \"Accept\" header in response to HTTP OPTIONS request to not indicate the \"" + no[i] + "\" method is supported. Instead the response is \"" + acceptHeader + "\".", acceptValues.contains(no[i]));
      }
   }

   /**
    * Posts the XML data the the given destination.
    *
    * @param destination
    *    the destination where the XML has to be posted.
    * @param data
    *    the XML to post.
    *
    * @return
    *    the returned XML already parsed.
    *
    * @throw Exception
    *    if anything goes wrong.
    */
   static Element postXML(String destination, String data) throws Exception {
      return postXML(destination, data, 200);
   }

   /**
    * Posts the XML data the the given destination.
    *
    * @param destination
    *    the destination where the XML has to be posted.
    *
    * @param data
    *    the XML to post.
    *
    * @param expectedStatus
    *    the HTTP status code that is expected.
    *
    * @return
    *    the returned XML already parsed or <code>null</code> if the HTTP code is 403.
    *
    * @throw Exception
    *    if anything goes wrong.
    */
   static Element postXML(String destination, String data, int expectedStatus)
   throws Exception {

      String content = postData(destination, data, "text/xml; charset=UTF-8", expectedStatus);
      if (expectedStatus != 403) {
         Element result = ElementFormatter.parse(new StringReader(content));
         return result;
      } else {
         return null;
      }
   }

   /**
    * Posts the data the the given destination.
    *
    * @param destination
    *    the destination where the data has to be posted.
    *
    * @param data
    *    the data to post.
    *
    * @param contentType
    *    the content type of the data to post.
    *
    * @param expectedStatus
    *    the HTTP status code that is expected.
    *
    * @return
    *    the data returned by the API.
    *
    * @throw Exception
    *    if anything goes wrong.
    */
   static String postData(String destination, String data, String contentType, int expectedStatus)
   throws Exception {

      HttpPost post = new HttpPost(destination);
      if (contentType != null) {
         post.addHeader("Accept", contentType);
         post.addHeader("Content-Type", contentType);
      }
      post.addHeader("User-Agent", "PostMethod");
      post.setEntity(new StringEntity(data));
      HttpClient client = new DefaultHttpClient();
      //client.setConnectionTimeout(500000);
      //client.setTimeout(500000);
      try {
         HttpResponse response = client.execute(post);
         int code = response.getStatusLine().getStatusCode();
         assertEquals(expectedStatus, code);
         if (contentType != null && code != 403) {
            String returnedContentType = response.getFirstHeader("Content-Type").getValue();
            assertEquals("Content type received '" + returnedContentType  +
                  "' does not match the content type '" + contentType + "' sent.",
                  contentType, returnedContentType);
         }
         String result = EntityUtils.toString(response.getEntity());
         return result;
      } finally {

         // Release current connection to the connection pool once you are done
         post.abort();
      }
   }

   /**
    * Tests that when different parameter values are passed to the
    * specified calling convention, it must return a 400 status code
    * (invalid HTTP request).
    */
   static void doTestMultipleParamValues(String convention)
   throws Throwable {

      String destination = AllTests.url() + "allinone/";

      HttpClient client = new DefaultHttpClient();
      //client.setConnectionTimeout(5000);
      //client.setTimeout(5000);

      HttpPost post;
      String paramName, value1, value2, message;
      int actual, expected;

      // Different values for the same parameter
      post      = new HttpPost(destination);
      paramName = "_function";
      value1    = "_GetFunctionList";
      value2    = "_GetStatistics";
      List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
      params.add(new BasicNameValuePair("_convention", convention));
      params.add(new BasicNameValuePair(paramName, value1));
      params.add(new BasicNameValuePair(paramName, value2));
      try {
         post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
         HttpResponse response = client.execute(post);
         actual = response.getStatusLine().getStatusCode();
      } finally {
         post.abort();
      }
      expected = 400;
      message = "Expected the \""
              + convention
              + "\" calling convention to return HTTP response code "
              + expected
              + " instead of "
              + actual
              + " when two different values are given for the \""
              + paramName
              + "\" parameter.";
      assertEquals(message, expected, actual);

      // Equal values for the same parameter
      post      = new HttpPost(destination);
      paramName = "_function";
      value1    = "_GetVersion";
      value2    = value1;
      params = new ArrayList<BasicNameValuePair>();
      params.add(new BasicNameValuePair("_convention", convention));
      params.add(new BasicNameValuePair(paramName, value1));
      params.add(new BasicNameValuePair(paramName, value2));
      try {
         post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
         HttpResponse response = client.execute(post);
         actual = response.getStatusLine().getStatusCode();
      } finally {
         post.abort();
      }
      expected = 200;
      message = "Expected the \""
              + convention
              + "\" calling convention to return HTTP response code "
              + expected
              + " instead of "
              + actual
              + " when two equal values are given for the \""
              + paramName
              + "\" parameter.";
      assertEquals(message, expected, actual);
   }

   /**
    * Tests that unsupported HTTP methods return the appropriate HTTP error.
    */
   public void testUnsupportedHTTPMethods() throws Exception {

      String[] unsupported = new String[] { "GET", "HEAD", };

      String     queryString = "/?_convention=_xins-xml";
      String     host        = AllTests.host();
      int        port        = AllTests.port();
      Properties headers     = new Properties();
      headers.put("Content-Length", "0");

      for (int i=0; i<unsupported.length; i++) {
         String method = unsupported[i];

         // Call the server
         HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);

         // Expect "405 Method Not Allowed"
         assertEquals("Expected HTTP status code 405 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                      "405 Method Not Allowed", result.getStatus());
      }
   }

   /**
    * Tests that unknown HTTP methods return the appropriate HTTP error.
    */
   public void testUnknownHTTPMethods() throws Exception {

      String[] unknown = new String[] { "PUT", "DELETE", "POLL", "JO-JO", "get", "post" };

      String     queryString = "/?_convention=_xins-xml";
      String     host        = AllTests.host();
      int        port        = AllTests.port();
      Properties headers     = new Properties();
      headers.put("Content-Length", "0");

      for (int i=0; i<unknown.length; i++) {
         String method = unknown[i];

         // Call the server
         HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);

         // Expect "501 Not Implemented"
         assertEquals("Expected HTTP status code 501 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                      "501 Not Implemented", result.getStatus());
      }

      // Same, but now without the content length header
      headers = new Properties();
      for (int i=0; i<unknown.length; i++) {
         String method = unknown[i];

         // Call the server
         HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);

         // Expect "501 Not Implemented"
         assertEquals("Expected HTTP status code 501 in response to an HTTP " + method + " request for a calling convention that does not support that method.",
                      "501 Not Implemented", result.getStatus());
      }
   }
}
