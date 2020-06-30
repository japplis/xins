/*
 * $Id: PortalAPITests.java,v 1.31 2013/01/23 11:36:37 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.server.frontend;

import java.io.StringReader;
import java.util.Iterator;

import com.mycompany.portal.capi.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xins.client.InternalErrorException;
import org.xins.client.UnsuccessfulXINSCallException;
import org.xins.client.XINSCallRequest;
import org.xins.client.XINSServiceCaller;
import org.xins.common.http.HTTPCallRequest;
import org.xins.common.http.HTTPCallResult;
import org.xins.common.http.HTTPServiceCaller;
import org.xins.common.http.StatusCodeHTTPCallException;
import org.xins.common.service.TargetDescriptor;
import org.w3c.dom.Element;
import org.xins.common.xml.ElementList;
import org.xins.common.xml.ElementFormatter;

import org.xins.tests.AllTests;
import org.xins.tests.server.HTTPCaller;
import org.xins.tests.server.HTTPCallerResult;

/**
 * Tests the functions in the <em>allinone</em> API using the generated CAPI
 * classes.
 *
 * @version $Revision: 1.31 $ $Date: 2013/01/23 11:36:37 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class PortalAPITests extends TestCase {

   private TargetDescriptor _target;
   private CAPI _capi;

   /**
    * Constructs a new <code>PortalAPITests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public PortalAPITests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(PortalAPITests.class);
   }

   public void setUp() throws Exception {
      int port = AllTests.port() + 1;
      _target = new TargetDescriptor("http://localhost:" + port + "/portal/");
      _capi   = new CAPI(_target);

      // Clean the session before executing any tests
      Map<String, String> params = new HashMap<String, String>();
      params.put("command", "Control");
      params.put("action", "RemoveSessionProperties");
      callCommand(params);

   }

   public void testControlCommand() throws Exception {
      Map<String, String> params = new HashMap<String, String>();
      params.put("command", "Control");
      String htmlResult = callCommand(params);
      assertTrue("Incorrect content.", htmlResult.indexOf("API start-up time") != -1);
   }

   public void testFlushControlCommand() throws Exception {
      Map<String, String> params = new HashMap<String, String>();
      params.put("command", "Control");
      params.put("action", "FlushCommandTemplateCache");
      callCommand(params);
   }

   public void testRefreshControlCommand() throws Exception {
      Map<String, String> params = new HashMap<String, String>();
      params.put("command", "Control");
      params.put("action", "RefreshCommandTemplateCache");
      callCommand(params);
   }

   public void testRemoveSessionPropertiesControlCommand1() throws Exception {
      Map<String, String> params = new HashMap<String, String>();
      params.put("command", "Control");
      params.put("action", "RemoveSessionProperties");
      callCommand(params);
   }

   public void testRemoveSessionPropertiesControlCommand2() throws Exception {

      // Check that testProp is not in the session
      Map<String, String> paramsControl = new HashMap<String, String>();
      paramsControl.put("command", "Control");
      String controlResult1 = callCommand(paramsControl);
      assertTrue("username property already in the session.", controlResult1.indexOf("username") == -1);

      // Add testProp to the session
      Map<String, String> paramsLogin = createLoginParams();
      callCommand(paramsLogin);

      // Check that it's in the session
      String controlResult2 = callCommand(paramsControl);
      assertTrue("username property not found in the session.", controlResult2.indexOf("test1") != -1);

      // Clear the session
      Map<String, String> paramsRemove = new HashMap<String, String>();
      paramsRemove.put("command", "Control");
      paramsRemove.put("action", "RemoveSessionProperties");
      callCommand(paramsRemove);

      // Check that it's no more in the session
      String controlResult3 = callCommand(paramsControl);
      assertTrue("username has not been cleared of the session.", controlResult3.indexOf("test1") == -1);
   }

   public void testRedirection() throws Exception {
      Map<String, String> params = createLoginParams();
      String result = callCommand(params);
      assertEquals("", result);
   }

   public void testRedirectionWithUnderscore() throws Exception {
      Map<String, String> params = createLoginParams();
      params.put("user_name", "test1");
      String result = callCommand(params);
      assertEquals("", result);
   }

   public void testSourceMode() throws Exception {
      Map<String, String> params = createLoginParams();
      callCommand(params);
      Map<String, String> params2 = new HashMap<String, String>();
      params2.put("command", "Login");
      params2.put("mode", "source");
      String xmlResult = callCommand(params2);
      Element result = ElementFormatter.parse(new StringReader(xmlResult));
      assertEquals("commandresult", result.getTagName());
      assertEquals(4, new ElementList(result, "parameter").size());
      assertEquals(1, result.getAttributes().getLength());
   }

   public void testTemplateMode() throws Exception {
      Map<String, String> params = createLoginParams();
      params.put("mode", "template");
      String xmlResult = callCommand(params);
      Element result = ElementFormatter.parse(new StringReader(xmlResult));
      String resultElementName = result.getTagName();
      assertEquals("xsl:stylesheet", resultElementName);
   }

   public void testInvalidRequest() throws Exception {
      Map<String, String> params = createLoginParams();
      params.put("username", "bla@bla");
      params.remove("password");
      params.put("mode", "source");
      String xmlResult = callCommand(params);
      assertTrue("Not XML result: " + xmlResult, xmlResult.startsWith("<"));
      Element result = ElementFormatter.parse(new StringReader(xmlResult));
      assertEquals("commandresult", result.getTagName());
      assertTrue("No FieldError found", xmlResult.indexOf("error.type\">FieldError") != -1);
      assertTrue("No mantatory field found",
            xmlResult.indexOf("type=\"mand\" field=\"password\"") != -1 ||
            xmlResult.indexOf("field=\"password\" type=\"mand\"") != -1);
      assertTrue("Invalid field found",
            xmlResult.indexOf("type=\"format\" field=\"username\"") != -1 ||
            xmlResult.indexOf("field=\"username\" type=\"format\"") != -1);
   }

   public void testInvalidRequest2() throws Exception {
      Map<String, String> params = createLoginParams();
      params.remove("password");
      params.put("mode", "source");
      String xmlResult = callCommand(params);
      Element result = ElementFormatter.parse(new StringReader(xmlResult));
      assertEquals("commandresult", result.getTagName());
      assertTrue(xmlResult.indexOf("<parameter name=\"error.type\">FieldError</parameter>") != -1);
      Element data = new ElementList(result, "data").getUniqueChildElement();
      Element errorlist = new ElementList(data, "errorlist").getUniqueChildElement();
      Element fielderror = new ElementList(errorlist, "fielderror").getUniqueChildElement();
      assertEquals("mand", fielderror.getAttribute("type"));
      assertEquals("password", fielderror.getAttribute("field"));
   }

   public void testSimpleRedirection() throws Exception {
      Map<String, String> paramsLogin = createLoginParams();
      String xmlResult = callCommand(paramsLogin);

      Map<String, String> params = new HashMap<String, String>();
      String redirection = callRedirection(params);
      assertTrue("Incorrect returned redirection: " + redirection, redirection.endsWith("?command=MainPage"));
   }

   public void testInternalError() throws Exception {
      Map<String, String> params = createLoginParams();
      params.put("username", "superman");
      String result = callCommand(params);
      assertTrue(result.indexOf("<html ") != -1);
      assertTrue(result.indexOf("An unknown error has occurred") != -1);
   }

   public void testXSLTError() throws Exception {
      Properties headers = new Properties();
      headers.setProperty("Cookie", "SessionID=1234567");
      HTTPCallerResult resultLogin = HTTPCaller.call("1.1", AllTests.host(), AllTests.port() + 1, "GET", "/portal/?command=Login&action=Okay&username=superhuman&password=passW1", headers);

      HTTPCallerResult resultMainPage = HTTPCaller.call("1.1", AllTests.host(), AllTests.port() + 1, "GET", "/portal/?command=MainPage", headers);
      assertTrue("Incorrect status code returned: " + resultMainPage.getStatus(),
            resultMainPage.getStatus().trim().startsWith("500"));
      String htmlResult = resultMainPage.getBody();
      assertTrue(htmlResult.indexOf("<html ") != -1);
      assertTrue(htmlResult.indexOf("A technical error occured") != -1);
   }

   public void testConditionalRedirection() throws Exception {
      Map<String, String> params = createLoginParams();
      String redirection = callRedirection(params);
      assertTrue("Incorrect returned redirection: " + redirection, redirection.endsWith("?command=MainPage"));

      params.put("username", "superuser");
      String redirection2 = callRedirection(params);
      assertTrue("Incorrect returned redirection: " + redirection2, redirection2.endsWith("?command=Admin"));
   }

   private String callRedirection(Map<String, String> params) throws Exception {
      String     host   = AllTests.host();
      int        port   = AllTests.port() + 1;
      String     method = "GET";
      Properties headers = new Properties();
      headers.put("Content-Length", "0");
      String queryString = "/portal/";
      boolean first = true;
      Iterator itParams = params.keySet().iterator();
      while (itParams.hasNext()) {
         if (first) {
            queryString += '?';
            first = false;
         } else {
            queryString += '&';
         }
         String nextParam = (String) itParams.next();
         queryString += nextParam + "=" + params.get(nextParam);
      }

      // TODO Call the server
      HTTPCallerResult result = HTTPCaller.call("1.1", host, port, method, queryString, headers);
      int statusCode = Integer.parseInt(result.getStatus().substring(0, 3));
      assertEquals(302, statusCode);
      assertNotNull(result.getHeaderValues("Location"));
      assertEquals(1, result.getHeaderValues("Location").size());
      return (String) result.getHeaderValues("Location").get(0);
   }

   private String callCommand(Map<String, String> params) throws Exception {
      HTTPServiceCaller callControl = new HTTPServiceCaller(_target);
      HTTPCallRequest callRequest = new HTTPCallRequest(params);
      HTTPCallResult callResult = callControl.call(callRequest);
      assertTrue("Incorrect status code returned: " + callResult.getStatusCode(),
            callResult.getStatusCode() < 400);
      String htmlResult = callResult.getString();
      return htmlResult;
   }

   private Map<String, String> createLoginParams() {
      Map<String, String> paramsLogin = new HashMap<String, String>();
      paramsLogin.put("command", "Login");
      paramsLogin.put("action", "Okay");
      paramsLogin.put("username", "test1");
      paramsLogin.put("password", "passW1");
      return paramsLogin;
   }

   /**
    * Tests invalid responses from the server.
    */
   public void testInvalidResponse() throws Exception {
      XINSCallRequest request = new XINSCallRequest("InvalidResponse");
      XINSServiceCaller caller = new XINSServiceCaller(_target);
      try {
         caller.call(request);
         fail("No invalid response received as expected.");
      } catch (UnsuccessfulXINSCallException exception) {
         assertEquals("_InvalidResponse", exception.getErrorCode());
         assertEquals(_target, exception.getTarget());
         assertNull(exception.getParameters());
         Element dataSection = exception.getDataElement();
         assertNotNull(dataSection);
         Element missingParam = (Element) new ElementList(dataSection, "*").get(0);
         assertEquals("missing-param", missingParam.getTagName());
         assertEquals("outputText1", missingParam.getAttribute("param"));
         assertEquals(0, new ElementList(missingParam, "*").size());
         assertEquals("", missingParam.getTextContent());
         Element invalidParam = (Element) new ElementList(dataSection, "*").get(1);
         assertEquals("invalid-value-for-type", invalidParam.getTagName());
         assertEquals("pattern", invalidParam.getAttribute("param"));
         assertEquals(0, new ElementList(invalidParam, "*").size());
         assertEquals("", invalidParam.getTextContent());
      }
   }

   /**
    * Tests invalid responses from the server using the new XINS 1.2 call
    * method.
    */
   public void testInvalidResponse2() throws Exception {

      InvalidResponseRequest request = new InvalidResponseRequest();
      try {
         _capi.callInvalidResponse(request);
         fail("Expected InternalErrorException.");
      } catch (InternalErrorException exception) {
         assertEquals("_InvalidResponse", exception.getErrorCode());
         assertEquals(_target, exception.getTarget());
         assertNull(exception.getParameters());
         Element dataSection = exception.getDataElement();
         assertNotNull(dataSection);
         Element missingParam = (Element) new ElementList(dataSection, "*").get(0);
         assertEquals("missing-param", missingParam.getTagName());
         assertEquals("outputText1", missingParam.getAttribute("param"));
         assertEquals(0, new ElementList(missingParam, "*").size());
         assertEquals("", missingParam.getTextContent());
         Element invalidParam = (Element) new ElementList(dataSection, "*").get(1);
         assertEquals("invalid-value-for-type", invalidParam.getTagName());
         assertEquals("pattern", invalidParam.getAttribute("param"));
         assertEquals(0, new ElementList(invalidParam, "*").size());
         assertEquals("", invalidParam.getTextContent());
      }

      request = new InvalidResponseRequest();
      request.setErrorCode("ErrorCodeNotKnownWhatsoever");
      try {
         _capi.callInvalidResponse(request);
         fail("Expected InternalErrorException.");
      } catch (InternalErrorException exception) {
         assertEquals("_InternalError", exception.getErrorCode());
         assertEquals(_target,          exception.getTarget());
      }

      request = new InvalidResponseRequest();
      request.setErrorCode("InvalidNumber");
      try {
         _capi.callInvalidResponse(request);
         fail("Expected InternalErrorException.");
      } catch (InternalErrorException exception) {
         assertEquals("_InternalError", exception.getErrorCode());
         assertEquals(_target,          exception.getTarget());
      }
   }

   public void testGetSettingsFunction() throws Exception {
      XINSCallRequest request = new XINSCallRequest("_GetSettings");
      XINSServiceCaller caller = new XINSServiceCaller(_target);
      try {
         caller.call(request);
         fail("The call to _GetSettings should have failed with ACL denied.");
      } catch (StatusCodeHTTPCallException schcex) {

         // As expected.
         assertEquals(403, schcex.getStatusCode());
         assertTrue(schcex.getDetail().contains("_NotAllowed"));
      }
   }

   boolean threadSucceeded = true;

   /**
    * Test concurent calls to the _xins-soap calling convention
    */
   /*public void testFrontendConcurentCalls() throws Throwable {
      for (int i = 0; i < 5; i++) {
         LoginSessionThread t1 = new LoginSessionThread("test1");
         LoginSessionThread t2 = new LoginSessionThread("test2");
         LoginSessionThread t3 = new LoginSessionThread("test3");
         LoginSessionThread t4 = new LoginSessionThread("test4");
         LoginSessionThread t5 = new LoginSessionThread("test5");
         t1.start();
         t2.start();
         t3.start();
         t4.start();
         t5.start();
         t1.join();
         t2.join();
         t3.join();
         t4.join();
         t5.join();
         assertTrue("Incorrect result while 'test1' was sent.", t1.hasSucceeded());
         assertTrue("Incorrect result while 'test2' was sent.", t2.hasSucceeded());
         assertTrue("Incorrect result while 'test3' was sent.", t3.hasSucceeded());
         assertTrue("Incorrect result while 'test4' was sent.", t4.hasSucceeded());
         assertTrue("Incorrect result while 'test5' was sent.", t5.hasSucceeded());
      }
      assertTrue("An error occured in one of the threads.", threadSucceeded);
   }*/

   class LoginSessionThread extends Thread {

      private String userName;
      private boolean succeeded = false;

      LoginSessionThread(String userName) {
         this.userName = userName;
      }

      public void run() {
         try {
            testRemoveSessionPropertiesControlCommand2();
            succeeded = true;
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }

      boolean hasSucceeded() {
         return succeeded;
      }
   }
}
