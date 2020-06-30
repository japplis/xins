/*
 * $Id: XMLCallingConvention.java,v 1.58 2013/01/18 14:21:24 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xins.common.text.TextUtils;
import org.xins.common.xml.ElementList;

/**
 * XML calling convention.
 *
 * @version $Revision: 1.58 $ $Date: 2013/01/18 14:21:24 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class XMLCallingConvention extends CallingConvention {

   /**
    * The response encoding format.
    */
   protected static final String RESPONSE_ENCODING = "UTF-8";

   /**
    * The content type of the HTTP response.
    */
   protected static final String RESPONSE_CONTENT_TYPE = "text/xml; charset=" + RESPONSE_ENCODING;

   @Override
   protected String[] getSupportedMethods() {
      return new String[] { "POST" };
   }

   /**
    * Checks if the specified request can be handled by this calling
    * convention.
    *
    * <p>This method will not throw any exception.
    *
    * @param httpRequest
    *    the HTTP request to investigate, cannot be <code>null</code>.
    *
    * @return
    *    <code>true</code> if this calling convention is <em>possibly</em>
    *    able to handle this request, or <code>false</code> if it
    *    <em>definitely</em> not able to handle this request.
    *
    * @throws Exception
    *    if analysis of the request causes an exception;
    *    <code>false</code> will be assumed.
    */
   protected boolean matches(HttpServletRequest httpRequest)
   throws Exception {

      // Parse the XML in the request (if any)
      Element element = parseXMLRequest(httpRequest);

      return element.getTagName().equals("request") &&
            !TextUtils.isEmpty(element.getAttribute("function"));
   }

   /**
    * Converts an HTTP request to a XINS request (implementation method). This
    * method should only be called from class CallingConvention. Only
    * then it is guaranteed that the <code>httpRequest</code> argument is not
    * <code>null</code>.
    *
    * @param httpRequest
    *    the HTTP request, will not be <code>null</code>.
    *
    * @return
    *    the XINS request object, never <code>null</code>.
    *
    * @throws InvalidRequestException
    *    if the request is considerd to be invalid.
    *
    * @throws FunctionNotSpecifiedException
    *    if the request does not indicate the name of the function to execute.
    */
   protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
   throws InvalidRequestException,
          FunctionNotSpecifiedException {

      Element requestElem = parseXMLRequest(httpRequest);

      String functionName = requestElem.getAttribute("function");

      // Determine function parameters
      Map<String, String> functionParams = new HashMap<String, String>();
      ElementList parameters = new ElementList(requestElem, "param");
      for (int i = 0; i < parameters.size(); i++) {
         Element nextParam = (Element) parameters.get(i);
         String name  = nextParam.getAttribute("name");
         String value = nextParam.getTextContent();
         functionParams.put(name, value);
      }

      // Check if function is specified
      if (TextUtils.isEmpty(functionName)) {
         throw new FunctionNotSpecifiedException();
      }

      // Remove all invalid parameters
      cleanUpParameters(functionParams);

      // Get data section
      Element dataElement = null;
      ElementList dataElementList = new ElementList(requestElem, "data");
      if (dataElementList.size() == 1) {
         dataElement = (Element) dataElementList.get(0);
      } else if (dataElementList.size() > 1) {
         throw new InvalidRequestException("Found multiple data sections.");
      }

      return new FunctionRequest(functionName, functionParams, dataElement);
   }

   /**
    * Converts a XINS result to an HTTP response (implementation method).
    *
    * @param xinsResult
    *    the XINS result object that should be converted to an HTTP response,
    *    will not be <code>null</code>.
    *
    * @param httpResponse
    *    the HTTP response object to configure, will not be <code>null</code>.
    *
    * @param backpack
    *    the request backpack, will not be <code>null</code>.
    *
    * @throws IOException
    *    if calling any of the methods in <code>httpResponse</code> causes an
    *    I/O error.
    */
   protected void convertResultImpl(FunctionResult      xinsResult,
                                    HttpServletResponse httpResponse,
                                    Map<String, Object> backpack)
   throws IOException {

      // Send the XML output to the stream and flush
      httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
      PrintWriter out = httpResponse.getWriter();
      Integer backpackStatusCode = (Integer) backpack.get(BackpackConstants.STATUS_CODE);
      if (backpackStatusCode == null) {
         httpResponse.setStatus(HttpServletResponse.SC_OK);
      } else {
         httpResponse.setStatus(backpackStatusCode);
      }
      CallResultOutputter.output(out, xinsResult);
      out.close();
   }
}
