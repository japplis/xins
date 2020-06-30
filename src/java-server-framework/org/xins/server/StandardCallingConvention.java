/*
 * $Id: StandardCallingConvention.java,v 1.74 2013/01/18 10:41:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xins.common.text.TextUtils;
import org.xins.common.xml.ElementFormatter;

/**
 * Standard calling convention. The technical name for this calling convention
 * is <em>_xins-std</em>.
 *
 * @version $Revision: 1.74 $ $Date: 2013/01/18 10:41:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class StandardCallingConvention extends CallingConvention {

   /**
    * The response encoding format.
    */
   protected static final String RESPONSE_ENCODING = "UTF-8";

   /**
    * The content type of the HTTP response.
    */
   protected static final String RESPONSE_CONTENT_TYPE = "text/xml; charset=" + RESPONSE_ENCODING;

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
    */
   protected boolean matches(HttpServletRequest httpRequest) {

      // If no _function parameter is specified, then there is no match
      return ! TextUtils.isEmpty(httpRequest.getParameter("_function"));
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

      // Parse the parameters in the HTTP request
      Map<String, String> params = gatherParams(httpRequest);

      // Remove all invalid parameters
      cleanUpParameters(params);

      // Determine function name
      String functionName = httpRequest.getParameter("_function");
      if (TextUtils.isEmpty(functionName)) {
         throw new FunctionNotSpecifiedException();
      }

      // Get data section
      String dataSectionValue = httpRequest.getParameter("_data");
      Element dataElement = null;
      if (dataSectionValue != null && dataSectionValue.length() > 0) {

         // Parse the data section
         try {
            dataElement = ElementFormatter.parse(dataSectionValue);

         // Parsing error
         } catch (SAXException exception) {
            String detail = "Cannot parse the data section.";
            throw new InvalidRequestException(detail, exception);
         }
      }

      // Define the HTTP method in the backpack
      Map<String, Object> backpack = new HashMap<String, Object>();
      backpack.put("_httpMethod", httpRequest.getMethod());

      // Construct and return the request object
      return new FunctionRequest(functionName, params, dataElement, backpack);
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

      // Set the status code and the content type
      Integer backpackStatusCode = (Integer) backpack.get(BackpackConstants.STATUS_CODE);
      if (backpackStatusCode == null) {
         httpResponse.setStatus(HttpServletResponse.SC_OK);
      } else {
         httpResponse.setStatus(backpackStatusCode);
      }
      httpResponse.setContentType(RESPONSE_CONTENT_TYPE);

      // Determine the method
      String method = (String) backpack.get("_httpMethod");

      // Handle HEAD requests
      if ("HEAD".equals(method)) {
         StringWriter out = new StringWriter();
         CallResultOutputter.output(out, xinsResult);
         httpResponse.setContentLength(out.getBuffer().length());

      // Handle non-HEAD requests
      } else {
         Writer out = httpResponse.getWriter();
         CallResultOutputter.output(out, xinsResult);
         out.close();
      }
   }
}
