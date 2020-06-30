/*
 * $Id: JSONRPC2CallingConvention.java,v 1.2 2013/01/18 10:41:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Element;
import org.xins.common.spec.EntityNotFoundException;
import org.xins.common.spec.InvalidSpecificationException;
import org.xins.common.xml.ElementFormatter;
import org.xml.sax.SAXException;

/**
 * The JSON-RPC calling convention.
 * Version  <a href='http://www.jsonrpc.org/specification'>2.0</a> is supported.
 *
 *
 * @See {@link http://www.simple-is-better.org/json-rpc/jsonrpc20-over-http.html}
 * @since XINS 3.1.
 * @version $Revision: 1.2 $ $Date: 2013/01/18 10:41:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class JSONRPC2CallingConvention extends JSONRPCCallingConvention {

   /**
    * The json content type.
    */
   protected static final String RESPONSE_CONTENT_TYPE = "application/json";

   /**
    * The json-rpc content type.
    */
   protected static final String JSON_RPC_CONTENT_TYPE = "application/json-rpc";

   /**
    * The jsonrequest content type.
    */
   protected static final String JSON_REQUEST_CONTENT_TYPE = "application/jsonrequest";

   /**
    * Creates a new <code>JSONRPCCallingConvention</code> instance.
    *
    * @param api
    *    the API, needed for the JSON-RPC messages, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>api == null</code>.
    */
   public JSONRPC2CallingConvention(API api) throws IllegalArgumentException {
      super(api);
   }

   protected boolean matches(HttpServletRequest httpRequest)
   throws Exception {

      String acceptHeader = httpRequest.getHeader("Accept");
      if (!RESPONSE_CONTENT_TYPE.equals(acceptHeader) &&
              !JSON_RPC_CONTENT_TYPE.equals(acceptHeader) &&
              !JSON_REQUEST_CONTENT_TYPE.equals(acceptHeader)) {
         return false;
      }
      String requestContentType = httpRequest.getContentType();
      if ("post".equalsIgnoreCase(httpRequest.getMethod())) {
         return RESPONSE_CONTENT_TYPE.equals(requestContentType) ||
              !JSON_RPC_CONTENT_TYPE.equals(requestContentType) ||
              !JSON_REQUEST_CONTENT_TYPE.equals(requestContentType);
      }
      return true;
   }
   @Override
   protected FunctionRequest parseGetRequest(HttpServletRequest httpRequest)
   throws InvalidRequestException, FunctionNotSpecifiedException {
      String functionName = httpRequest.getParameter("method");
      if (functionName == null) {
         throw new FunctionNotSpecifiedException();
      }
      String id = httpRequest.getParameter("id");
      Map<String, Object> backpack = new HashMap<String, Object>();
      if (id != null) {
         backpack.put("_id", id);
      }

      Map<String, String> functionParams = new HashMap<String, String>();
      Element dataElement = null;
      String base64Params = httpRequest.getParameter("params");
      String params = new String(Base64.decodeBase64(base64Params), Charset.forName("UTF-8"));
      try {
         if (params.startsWith("[")) {
            JSONArray paramsArray = new JSONArray(params);
            Iterator itInputParams = getAPI().getAPISpecification().getFunction(functionName).getInputParameters().keySet().iterator();
            int paramPos = 0;
            while (itInputParams.hasNext() && paramPos < paramsArray.length()) {
               String nextParamName = (String) itInputParams.next();
               Object nextParamValue = paramsArray.get(paramPos);
               functionParams.put(nextParamName, String.valueOf(nextParamValue));
               paramPos++;
            }
         } else {
            JSONObject requestObject = new JSONObject(params);
            JSONArray paramNames = requestObject.names();
            for (int i = 0; i < paramNames.length(); i++) {
               String nextName = paramNames.getString(i);
               if (nextName.equals("_data")) {
                  JSONObject dataSectionObject = requestObject.getJSONObject("_data");
                  String dataSectionString = XML.toString(dataSectionObject);
                  dataElement = ElementFormatter.parse(dataSectionString);
               } else {
                  String value = requestObject.get(nextName).toString();
                  functionParams.put(nextName, value);
               }
            }
         }
      } catch (EntityNotFoundException ex) {
         throw new InvalidRequestException("Function " + functionName + " not found: " + ex.getMessage());
      } catch (InvalidSpecificationException ex) {
         throw new InvalidRequestException("Invalid specifications for the function " + functionName + ": " + ex.getMessage());
      } catch (JSONException ex) {
         throw new InvalidRequestException("Cannot parse params: " + ex.getMessage());
      } catch (SAXException ex) {
         throw new InvalidRequestException("Cannot parse data element: " + ex.getMessage());
      }
      return new FunctionRequest(functionName, functionParams, dataElement, backpack);
   }

   @Override
   protected FunctionRequest parsePostRequest(HttpServletRequest httpRequest)
   throws InvalidRequestException, FunctionNotSpecifiedException {
      FunctionRequest functionRequest = super.parsePostRequest(httpRequest);
      if (!"2.0".equals(functionRequest.getBackpack().get("_jsonrpc"))) {
         throw new InvalidRequestException("jsonrpc input is mandatory and must be 2.0.");
      }
      functionRequest.getBackpack().put("_accept", httpRequest.getHeader("Accept"));
      return functionRequest;
   }

   @Override
   protected void convertResultImpl(FunctionResult      xinsResult,
                                    HttpServletResponse httpResponse,
                                    Map<String, Object> backpack)
   throws IOException {

      // Send the XML output to the stream and flush
      String contentType = (String) backpack.get("_accept");
      httpResponse.setContentType(contentType);
      Object requestId = backpack.get("_id");
      if (requestId == null) {
         httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
         return;
      }
      PrintWriter out = httpResponse.getWriter();
      String errorCode = xinsResult.getErrorCode();
      int statusCode = getStatusCodeForError(errorCode);
      httpResponse.setStatus(statusCode);

      // Return the service description when asked.
      String functionName = (String) backpack.get(BackpackConstants.FUNCTION_NAME);

      // Transform the XINS result to a JSON object
      JSONObject returnObject = new JSONObject();
      try {
         String version = (String) backpack.get("_jsonrpc");
         returnObject.put("jsonrpc", version);
         if (errorCode != null) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("code", getXmlRpcErrorCode(errorCode));
            errorObject.put("message", getErrorDescription(functionName, errorCode));
            JSONObject paramsObject = createResultObject(xinsResult);
            errorObject.put("data", paramsObject);
            returnObject.put("error", errorObject);
         } else {
            JSONObject paramsObject = createResultObject(xinsResult);
            returnObject.put("result", paramsObject);
         }
         returnObject.put("id", requestId);

         // Write the result to the servlet response
         String returnString = returnObject.toString();
         out.print(returnString);
      } catch (JSONException jsonex) {
         throw new IOException(jsonex.getMessage());
      }

      out.close();
   }

   private int getStatusCodeForError(String errorCode) {
      if (errorCode == null) {
         return HttpServletResponse.SC_OK;
      } else if (errorCode.equals(DefaultResultCodes._INVALID_REQUEST.getName())) {
         return HttpServletResponse.SC_BAD_REQUEST;
      } else {
         return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      }
   }

   private int getXmlRpcErrorCode(String xinsErrorCode) {
      if (xinsErrorCode.equals(DefaultResultCodes._INVALID_REQUEST.getName())) {
         return -32600;
      } else if (xinsErrorCode.startsWith("_")) {
         return -32603;
      } else {
         return -32000;
      }
   }
}
