/*
 * $Id: JSONRPCCallingConvention.java,v 1.33 2013/01/18 10:41:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import static org.xins.server.DefaultResultCodes.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.collections.MapStringUtils;
import org.xins.common.spec.APISpec;
import org.xins.common.spec.EntityNotFoundException;
import org.xins.common.spec.ErrorCodeSpec;
import org.xins.common.spec.FunctionSpec;
import org.xins.common.spec.InvalidSpecificationException;
import org.xins.common.spec.ParameterSpec;
import org.xins.common.types.Type;
import org.w3c.dom.Element;
import org.xins.common.xml.ElementFormatter;
import org.xml.sax.SAXException;

/**
 * The JSON-RPC calling convention.
 * Version <a href='http://json-rpc.org/wiki/specification'>1.0</a>
 * and <a href='http://json-rpc.org/wd/JSON-RPC-1-1-WD-20060807.html'>1.1</a> are supported.
 * The service description is also returned on request when calling the
 * <em>system.describe</em> function.
 * The returned object is a JSON Object with a similar structure as the input
 * parameters when HTTP POST is used.
 *
 * @since XINS 2.0.
 * @version $Revision: 1.33 $ $Date: 2013/01/18 10:41:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class JSONRPCCallingConvention extends CallingConvention {

   /**
    * The content type of the HTTP response.
    */
   protected static final String RESPONSE_CONTENT_TYPE = "application/json";

   /**
    * The API. Never <code>null</code>.
    */
   private final API _api;

   /**
    * Creates a new <code>JSONRPCCallingConvention</code> instance.
    *
    * @param api
    *    the API, needed for the JSON-RPC messages, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>api == null</code>.
    */
   public JSONRPCCallingConvention(API api) throws IllegalArgumentException {
      MandatoryArgumentChecker.check("api", api);
      _api = api;
   }

   /**
    * Returns the XML-RPC equivalent for the XINS type.
    *
    * @param parameterType
    *    the XINS type, cannot be <code>null</code>.
    *
    * @return
    *    the XML-RPC type, never <code>null</code>.
    */
   private static String convertType(Type parameterType) {
      if (parameterType instanceof org.xins.common.types.standard.Boolean) {
         return "bit";
      } else if (parameterType instanceof org.xins.common.types.standard.Int8
            || parameterType instanceof org.xins.common.types.standard.Int16
            || parameterType instanceof org.xins.common.types.standard.Int32
            || parameterType instanceof org.xins.common.types.standard.Int64
            || parameterType instanceof org.xins.common.types.standard.Float32
            || parameterType instanceof org.xins.common.types.standard.Float64) {
         return "num";
      } else {
         return "str";
      }
   }

   protected String[] getSupportedMethods() {
      return new String[] { "GET", "POST" };
   }

   protected boolean matches(HttpServletRequest httpRequest)
   throws Exception {

      // Note that matches will only accept calls that matches the 1.1 specification of JSON-RPC.
      if (httpRequest.getHeader("User-Agent") == null) {
         return false;
      }
      if (!RESPONSE_CONTENT_TYPE.equals(httpRequest.getHeader("Accept"))) {
         return false;
      }
      if ("post".equalsIgnoreCase(httpRequest.getMethod())) {
         return RESPONSE_CONTENT_TYPE.equals(httpRequest.getContentType());
      }
      return true;
   }

   protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
   throws InvalidRequestException, FunctionNotSpecifiedException {

      if ("post".equalsIgnoreCase(httpRequest.getMethod())) {
         return parsePostRequest(httpRequest);
      } else if ("get".equalsIgnoreCase(httpRequest.getMethod())) {
         return parseGetRequest(httpRequest);
      } else {
         throw new InvalidRequestException("Incorrect HTTP method: " + httpRequest.getMethod());
      }
   }

   protected void convertResultImpl(FunctionResult      xinsResult,
                                    HttpServletResponse httpResponse,
                                    Map<String, Object> backpack)
   throws IOException {

      // Send the XML output to the stream and flush
      httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
      PrintWriter out = httpResponse.getWriter();
      httpResponse.setStatus(HttpServletResponse.SC_OK);

      // Return the service description when asked.
      String functionName = (String) backpack.get(BackpackConstants.FUNCTION_NAME);
      if ("system.describe".equals(functionName)) {
         String uri = (String) backpack.get("_requestURI");
         if (uri.indexOf("system.describe") != -1) {
            uri = uri.substring(0, uri.indexOf("system.describe"));
         }
         try {
            JSONObject serviceDescriptionObject = createServiceDescriptionObject(uri);
            out.print(serviceDescriptionObject.toString());
            out.close();
            return;
         } catch (JSONException jsonex) {
            throw new IOException(jsonex.getMessage());
         }
      }

      // Transform the XINS result to a JSON object
      JSONObject returnObject = new JSONObject();
      try {
         String version = (String) backpack.get("_version");
         if (version != null) {
            returnObject.put("version", version);
         }
         if (xinsResult.getErrorCode() != null) {
            if (version == null) {
               returnObject.put("result", JSONObject.NULL);
               returnObject.put("error", xinsResult.getErrorCode());
            } else {
               JSONObject errorObject = new JSONObject();
               String errorCode = xinsResult.getErrorCode();
               errorObject.put("name", errorCode);
               errorObject.put("code", new Integer(123));
               errorObject.put("message", getErrorDescription(functionName, errorCode));
               JSONObject paramsObject = createResultObject(xinsResult);
               errorObject.put("error", paramsObject);
               returnObject.put("error", errorObject);
            }
         } else {
            JSONObject paramsObject = createResultObject(xinsResult);
            returnObject.put("result", paramsObject);
            if (version == null) {
               returnObject.put("error", JSONObject.NULL);
            }
         }
         Object requestId = backpack.get("_id");
         if (requestId != null) {
            returnObject.put("id", requestId);
         }

         // Write the result to the servlet response
         String returnString = returnObject.toString();
         out.print(returnString);
      } catch (JSONException jsonex) {
         throw new IOException(jsonex.getMessage());
      }

      out.close();
   }

   /**
    * Parses the JSON-RPC HTTP GET request according to the specs.
    *
    * @param httpRequest
    *    the HTTP request.
    *
    * @return
    *    the XINS request object, should not be <code>null</code>.
    *
    * @throws InvalidRequestException
    *    if the request is considerd to be invalid.
    *
    * @throws FunctionNotSpecifiedException
    *    if the request does not indicate the name of the function to execute.
    */
   protected FunctionRequest parseGetRequest(HttpServletRequest httpRequest)
   throws InvalidRequestException, FunctionNotSpecifiedException {
      String functionName;
      Map<String, String> functionParams;
      Element dataElement = null;
      Map<String, Object> backpack = new HashMap<String, Object>();

      String pathInfo = httpRequest.getPathInfo();
      if (pathInfo == null || pathInfo.lastIndexOf("/") == pathInfo.length() - 1) {
         throw new FunctionNotSpecifiedException();
      } else {
         functionName = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
      }
      if (functionName.equals("system.describe")) {
         backpack.put(BackpackConstants.SKIP_FUNCTION_CALL, true);
         backpack.put("_requestURI", httpRequest.getRequestURI());
         return new FunctionRequest(functionName, null, null, backpack);
      }
      backpack.put("_version", "1.1");
      functionParams = gatherParams(httpRequest);

      // Get data section
      String dataSectionValue = httpRequest.getParameter("_data");
      if (dataSectionValue != null && dataSectionValue.length() > 0) {
         try {
            dataElement = ElementFormatter.parse(dataSectionValue);

         // Parsing error
         } catch (SAXException exception) {
            String detail = "Cannot parse the data section.";
            throw new InvalidRequestException(detail, exception);
         }
      }
      return new FunctionRequest(functionName, functionParams, dataElement, backpack);
   }

   /**
    * Parses the JSON-RPC HTTP POST request according to the specs.
    * http://json-rpc.org/wd/JSON-RPC-1-1-WD-20060807.html
    *
    * @param httpRequest
    *    the HTTP request.
    *
    * @return
    *    the XINS request object, should not be <code>null</code>.
    *
    * @throws InvalidRequestException
    *    if the request is considerd to be invalid.
    *
    * @throws FunctionNotSpecifiedException
    *    if the request does not indicate the name of the function to execute.
    */
   protected FunctionRequest parsePostRequest(HttpServletRequest httpRequest)
   throws InvalidRequestException, FunctionNotSpecifiedException {
      String functionName;
      Map<String, String> functionParams = new HashMap<String, String>();
      Element dataElement = null;
      Map<String, Object> backpack = new HashMap<String, Object>();

      // Read the message
      // TODO replace with IOReader.readFully()
      StringBuffer requestBuffer = new StringBuffer(2048);
      try {
         Reader reader = httpRequest.getReader();
         char[] buffer = new char[2048];
         int length;
         while ((length = reader.read(buffer)) != -1) {
            requestBuffer.append(buffer, 0, length);
         }
      } catch (IOException ioe) {
         throw new InvalidRequestException("I/O Error while reading the request: " + ioe.getMessage());
      }
      String requestString = requestBuffer.toString();

      // Extract the request from the message
      try {
         JSONObject requestObject = new JSONObject(requestString);

         Object version = requestObject.opt("version");
         if (version != null) {
            backpack.put("_version", version);
         }

         Object jsonrpc = requestObject.opt("jsonrpc");
         if (jsonrpc != null) {
            backpack.put("_jsonrpc", jsonrpc);
         }

         functionName = requestObject.getString("method");
         if (functionName == null) {
            throw new FunctionNotSpecifiedException();
         }
         if (functionName.equals("system.describe")) {
            backpack.put(BackpackConstants.SKIP_FUNCTION_CALL, true);
            backpack.put("_requestURI", httpRequest.getRequestURI());
            return new FunctionRequest(functionName, null, null, backpack);
         }

         Object paramsParam = requestObject.get("params");
         if (paramsParam instanceof JSONArray) {
            JSONArray paramsArray = (JSONArray) paramsParam;
            Iterator itInputParams = _api.getAPISpecification().getFunction(functionName).getInputParameters().keySet().iterator();
            int paramPos = 0;
            while (itInputParams.hasNext() && paramPos < paramsArray.length()) {
               String nextParamName = (String) itInputParams.next();
               Object nextParamValue = paramsArray.get(paramPos);
               functionParams.put(nextParamName, String.valueOf(nextParamValue));
               paramPos++;
            }
         } else if (paramsParam instanceof JSONObject) {
            JSONObject paramsObject = (JSONObject) paramsParam;
            JSONArray paramNames = paramsObject.names();
            for (int i = 0; i < paramNames.length(); i++) {
               String nextName = paramNames.getString(i);
               if (nextName.equals("_data")) {
                  JSONObject dataSectionObject = paramsObject.getJSONObject("_data");
                  String dataSectionString = XML.toString(dataSectionObject);
                  dataElement = ElementFormatter.parse(dataSectionString);
               } else {
                  String value = paramsObject.get(nextName).toString();
                  functionParams.put(nextName, value);
               }
            }
         }
         Object id = requestObject.opt("id");
         if (id != null) {
            backpack.put("_id", id);
         }
      } catch (SAXException parseEx) {
         throw new InvalidRequestException(parseEx.getMessage());
      } catch (JSONException jsonex) {
         throw new InvalidRequestException(jsonex.getMessage());
      } catch (InvalidSpecificationException isex) {
         RuntimeException exception = new RuntimeException(isex);
         throw exception;
      } catch (EntityNotFoundException enfex) {
         throw new InvalidRequestException(enfex.getMessage());
      }
      return new FunctionRequest(functionName, functionParams, dataElement, backpack);
   }

   /**
    * Creates the JSON object from the result returned by the function.
    *
    * @param xinsResult
    *    the result returned by the function, cannot be <code>null</code>.
    *
    * @return
    *    the JSON object created from the result of the function, never <code>null</code>.
    *
    * @throws JSONException
    *    if the object cannot be created for any reason.
    */
   static JSONObject createResultObject(FunctionResult xinsResult) throws JSONException {
      Properties params = MapStringUtils.toProperties(xinsResult.getParameters());
      JSONObject paramsObject = new JSONObject(params);
      if (xinsResult.getDataElement() != null) {
         String dataSection = ElementFormatter.format(xinsResult.getDataElement());
         JSONObject dataSectionObject = XML.toJSONObject(dataSection);
         paramsObject.accumulate("data", dataSectionObject);
      }
      return paramsObject;
   }

   /**
    * Creates the JSON object containing the description of the API.
    * Specifications are available at http://json-rpc.org/wd/JSON-RPC-1-1-WD-20060807.html
    *
    * @param address
    *    the URL address of the service, cannot be <code>null</code>.
    *
    * @return
    *    the JSON object containing the description of the API, or <code>null</code>
    *    if an error occured.
    *
    * @throws JSONException
    *    if the object cannot be created for any reason.
    */
   private JSONObject createServiceDescriptionObject(String address) throws JSONException {
      JSONObject serviceObject = new JSONObject();
      serviceObject.put("sdversion", "1.0");
      serviceObject.put("name", _api.getName());
      String apiClassName = _api.getClass().getName();
      serviceObject.put("id", "xins:" + apiClassName.substring(0, apiClassName.indexOf(".api.API")));
      serviceObject.put("version", _api.getBootstrapProperties().get(API.API_VERSION_PROPERTY));
      try {
         APISpec apiSpec = _api.getAPISpecification();
         String description = apiSpec.getDescription();
         serviceObject.put("summary", description);
         serviceObject.put("address", address);

         // Add the functions
         JSONArray procs = new JSONArray();
         Iterator itFunctions = apiSpec.getFunctions().entrySet().iterator();
         while (itFunctions.hasNext()) {
            Map.Entry nextFunction = (Map.Entry) itFunctions.next();
            JSONObject functionObject = new JSONObject();
            functionObject.put("name", (String) nextFunction.getKey());
            FunctionSpec functionSpec = (FunctionSpec) nextFunction.getValue();
            functionObject.put("summary", functionSpec.getDescription());
            JSONArray params = getParamsDescription(functionSpec.getInputParameters(), functionSpec.getInputDataSectionElements());
            functionObject.put("params", params);
            JSONArray result = getParamsDescription(functionSpec.getOutputParameters(), functionSpec.getOutputDataSectionElements());
            functionObject.put("return", result);
         }
         serviceObject.put("procs", procs);
      } catch (InvalidSpecificationException ex) {
         return serviceObject;
      }
      return serviceObject;
   }

   /**
    * Returns the description of the input or output parameters.
    *
    * @param paramsSpecs
    *    the specification of the input of output parameters, cannot be <code>null</code>.
    *
    * @param dataSectionSpecs
    *    the specification of the input of output data section, cannot be <code>null</code>.
    *
    * @return
    *    the JSON array containing the description of the input or output parameters, never <code>null</code>.
    *
    * @throws JSONException
    *    if the JSON object cannot be created.
    */
   static JSONArray getParamsDescription(Map paramsSpecs, Map dataSectionSpecs) throws JSONException {
      JSONArray params = new JSONArray();
      Iterator itParams = paramsSpecs.entrySet().iterator();
      while (itParams.hasNext()) {
         Map.Entry nextParam = (Map.Entry) itParams.next();
         JSONObject paramObject = new JSONObject();
         paramObject.put("name", (String) nextParam.getKey());
         String jsonType = convertType(((ParameterSpec) nextParam.getValue()).getType());
         paramObject.put("type", jsonType);
         params.put(paramObject);
         // TODO data section
      }
      return params;
   }

   /**
    * Gets a description of the error.
    *
    * @param functionName
    *   the name of the function called, cannot be <code>null</code>.
    * @param errorCode
    *   the error code returned by the function, cannot be <code>null</code>.
    *
    * @return
    *    a single sentence containing the description of the error.
    */
   protected String getErrorDescription(String functionName, String errorCode) {
      if (errorCode.equals(_INVALID_REQUEST.getName())) {
         return "The request is invalid.";
      } else if (errorCode.equals(_INVALID_RESPONSE.getName())) {
         return "The response is invalid.";
      } else if (errorCode.equals(_DISABLED_FUNCTION.getName())) {
         return "The \"" + functionName + "\" function is disabled.";
      } else if (errorCode.equals(_INTERNAL_ERROR.getName())) {
         return "There was an internal error.";
      }
      try {
         ErrorCodeSpec errorSpec = _api.getAPISpecification().getFunction(functionName).getErrorCode(errorCode);
         String errorDescription = errorSpec.getDescription();
         if (errorDescription.indexOf(". ") != -1) {
            errorDescription = errorDescription.substring(0, errorDescription.indexOf(". "));
         } else if (errorDescription.indexOf(".\n") != -1) {
            errorDescription = errorDescription.substring(0, errorDescription.indexOf(".\n"));
         }
         return errorDescription;
      } catch (Exception ex) {
         return "Unknown error: \"" + errorCode + "\".";
      }
   }
}
