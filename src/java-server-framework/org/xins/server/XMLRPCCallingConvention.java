/*
 * $Id: XMLRPCCallingConvention.java,v 1.58 2013/01/18 14:21:24 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import static org.xins.server.DefaultResultCodes.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Attr;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.spec.DataSectionElementSpec;
import org.xins.common.spec.EntityNotFoundException;
import org.xins.common.spec.FunctionSpec;
import org.xins.common.spec.InvalidSpecificationException;
import org.xins.common.text.ParseException;
import org.xins.common.text.TextUtils;
import org.xins.common.types.Type;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xins.common.xml.DataElementBuilder;
import org.xins.common.xml.ElementList;

import org.znerd.xmlenc.XMLOutputter;

/**
 * The XML-RPC calling convention.
 *
 * @version $Revision: 1.58 $ $Date: 2013/01/18 14:21:24 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class XMLRPCCallingConvention extends CallingConvention {

   /**
    * The formatter for XINS Date type.
    */
   private static final DateFormat XINS_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

   /**
    * The formatter for XINS Timestamp type.
    */
   private static final DateFormat XINS_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");

   /**
    * The formatter for XML-RPC dateTime.iso8601 type.
    */
   private static final DateFormat XML_RPC_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");

   /**
    * The key used to store the parsing fault in the request attributes.
    */
   private static final String FAULT_KEY = "org.xins.server.xml-rpc.fault";

   /**
    * The key used to store the name of the function in the request attributes.
    */
   private static final String FUNCTION_NAME = "org.xins.server.xml-rpc.function";

   /**
    * The response encoding format.
    */
   protected static final String RESPONSE_ENCODING = "UTF-8";

   /**
    * The content type of the HTTP response.
    */
   protected static final String RESPONSE_CONTENT_TYPE = "text/xml; charset=" + RESPONSE_ENCODING;

   /**
    * The API. Never <code>null</code>.
    */
   private final API _api;

   /**
    * Creates a new <code>XMLRPCCallingConvention</code> instance.
    *
    * @param api
    *    the API, needed for the XML-RPC messages, cannot be
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>api == null</code>.
    */
   public XMLRPCCallingConvention(API api)
   throws IllegalArgumentException {

      // Check arguments
      MandatoryArgumentChecker.check("api", api);

      // Store the API reference (can be null!)
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
         return "boolean";
      } else if (parameterType instanceof org.xins.common.types.standard.Int8
            || parameterType instanceof org.xins.common.types.standard.Int16
            || parameterType instanceof org.xins.common.types.standard.Int32) {
         return "int";
      } else if (parameterType instanceof org.xins.common.types.standard.Float32
            || parameterType instanceof org.xins.common.types.standard.Float64) {
         return "double";
      } else if (parameterType instanceof org.xins.common.types.standard.Date
            || parameterType instanceof org.xins.common.types.standard.Timestamp) {
         return "dateTime.iso8601";
      } else if (parameterType instanceof org.xins.common.types.standard.Base64) {
         return "base64";
      } else {
         return "string";
      }
   }

   /**
    * Attribute a number for the error code.
    *
    * @param errorCode
    *    the error code, cannot be <code>null</code>.
    *
    * @return
    *    the error code number, always > 0;
    */
   private static int getErrorCodeNumber(String errorCode) {
      if (errorCode.equals(_DISABLED_FUNCTION.getName())) {
         return 1;
      } else if (errorCode.equals(_INTERNAL_ERROR.getName())) {
         return 2;
      } else if (errorCode.equals(_INVALID_REQUEST.getName())) {
         return 3;
      } else if (errorCode.equals(_INVALID_RESPONSE.getName())) {
         return 4;
      } else {

         // Defined error code returned. For more information, see the
         // faultString element.
         return 99;
      }
   }

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

      // The root element must be <methodCall/>
      if (element.getTagName().equals("methodCall")) {

         // The text within the <methodName/> element is the function name
         String function = new ElementList(element, "methodName").get(0).getTextContent();

         // There is a match only if the function name is non-empty
         if (! TextUtils.isEmpty(function)) {
            return true;
         }
      }

      return false;
   }

   protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
   throws InvalidRequestException,
          FunctionNotSpecifiedException {
      Map<String, Object> backpack = new HashMap<String, Object>();
      backpack.put(BackpackConstants.SKIP_FUNCTION_CALL, true);

      Element xmlRequest = parseXMLRequest(httpRequest);
      if (xmlRequest.getNamespaceURI() != null) {
         backpack.put(FAULT_KEY, "Namespace not allowed in XML-RPC requests");
         return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
      }

      if (!xmlRequest.getTagName().equals("methodCall")) {
         String faultMessage = "Root element is not \"methodCall\" but \"" + xmlRequest.getTagName() + "\".";
         backpack.put(FAULT_KEY, faultMessage);
         return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
      }

      Element methodNameElem;
      try {
         methodNameElem = new ElementList(xmlRequest, "methodName").getUniqueChildElement();
      } catch (ParseException pex) {
         backpack.put(FAULT_KEY, "No unique methodName found");
         return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
      }
      if (methodNameElem.getNamespaceURI() != null) {
         backpack.put(FAULT_KEY, "Namespace not allowed in XML-RPC requests");
         return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
      }
      String functionName = methodNameElem.getTextContent();

      // Determine function parameters and the data section
      Map<String, String> functionParams = new HashMap<String, String>();
      Element dataSection = null;

      ElementList params = new ElementList(xmlRequest, "params");
      if (params.isEmpty()) {
         return new FunctionRequest(functionName, functionParams, null);
      } else if (params.size() > 1) {
         backpack.put(FAULT_KEY, "More than one params specified in the XML-RPC request.");
         return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
      }
      Element paramsElem = (Element) params.get(0);
      ElementList paramsNodes = new ElementList(paramsElem, "param");
      for (int i = 0; i < paramsNodes.size(); i++) {
         Element nextParam = (Element) paramsNodes.get(i);
         Element structElem;
         Element valueElem;
         try {
            valueElem = new ElementList(nextParam, "value").getUniqueChildElement();
            structElem = new ElementList(valueElem).getUniqueChildElement();
         } catch (ParseException pex) {
            backpack.put(FAULT_KEY, "Invalid XML-RPC request.");
            return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
         }
         if (structElem.getTagName().equals("struct")) {

            // Parse the input parameter
            String parameterName = null;
            String parameterValue = null;
            try {
               Element memberElem = new ElementList(structElem, "member").getUniqueChildElement();
               Element memberNameElem = new ElementList(memberElem, "name").getUniqueChildElement();
               Element memberValueElem = new ElementList(memberElem, "value").getUniqueChildElement();
               Element typeElem = new ElementList(memberValueElem).getUniqueChildElement();
               parameterName = memberNameElem.getTextContent();
               parameterValue = typeElem.getTextContent();
               FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
               Type parameterType = functionSpec.getInputParameter(parameterName).getType();
               parameterValue = convertInput(parameterType, typeElem);
            } catch (InvalidSpecificationException ise) {

               // keep the old value
            } catch (EntityNotFoundException enfe) {

               // keep the old value
            } catch (java.text.ParseException pex) {

               backpack.put(FAULT_KEY,  "Invalid value for parameter \"" + parameterName + "\".");
               return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
            } catch (ParseException pex) {

               backpack.put(FAULT_KEY, "Invalid XML-RPC request: " + pex.getMessage());
               return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
            }
            functionParams.put(parameterName, parameterValue);
         } else if (structElem.getTagName().equals("array")) {

            // Parse the input data section
            Element dataElem;
            try {
               Element arrayElem = new ElementList(valueElem, "array").getUniqueChildElement();
               dataElem = new ElementList(arrayElem, "data").getUniqueChildElement();
            } catch (ParseException pex) {
               backpack.put(FAULT_KEY, "Incorrect specification of the input data section: " + pex.getMessage());
               return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
            }
            if (dataSection != null) {
               backpack.put(FAULT_KEY, "Only one data section is allowed per request.");
               return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
            }
            Map dataSectionSpec = null;
            try {
               FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
               dataSectionSpec = functionSpec.getInputDataSectionElements();
            } catch (InvalidSpecificationException ise) {

               // keep the old value
            } catch (EntityNotFoundException enfe) {

               // keep the old value
            }
            DataElementBuilder builder = new DataElementBuilder();
            for (Element childValueElem : new ElementList(dataElem, "value")) {
               try {
                  Element childElem = parseElement(childValueElem, dataSectionSpec);
                  builder.getDataElement().appendChild(childElem);
               } catch (ParseException pex) {
                  backpack.put(FAULT_KEY, "Incorrect format for data element in XML-RPC request: " + pex.getMessage());
                  return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
               }
            }
            dataSection = builder.getDataElement();
         } else {
            backpack.put(FAULT_KEY, "Only \"struct\" and \"array\" are valid as parameter type.");
            return new FunctionRequest("InvalidRequest", new HashMap<String, String>(), null, backpack);
         }
      }

      backpack.put(BackpackConstants.SKIP_FUNCTION_CALL, false);
      return new FunctionRequest(functionName, functionParams, null, backpack);
   }

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

      // Store the result in a StringWriter before sending it.
      Writer buffer = new StringWriter(1024);

      // Create an XMLOutputter
      XMLOutputter xmlout = new XMLOutputter(buffer, RESPONSE_ENCODING);

      // Output the declaration
      xmlout.declaration();

      xmlout.startTag("methodResponse");

      String errorCode = xinsResult.getErrorCode();
      String faultRequest = (String) backpack.get(FAULT_KEY);
      if (errorCode != null || faultRequest != null) {
         xmlout.startTag("fault");
         xmlout.startTag("value");
         xmlout.startTag("struct");

         xmlout.startTag("member");
         xmlout.startTag("name");
         xmlout.pcdata("faultCode");
         xmlout.endTag(); // name
         xmlout.startTag("value");
         xmlout.startTag("int");
         if (errorCode != null) {
            xmlout.pcdata(String.valueOf(getErrorCodeNumber(errorCode)));
         } else {
            xmlout.pcdata("10");
         }
         xmlout.endTag(); // int
         xmlout.endTag(); // value
         xmlout.endTag(); // member

         xmlout.startTag("member");
         xmlout.startTag("name");
         xmlout.pcdata("faultString");
         xmlout.endTag(); // name
         xmlout.startTag("value");
         xmlout.startTag("string");
         if (errorCode != null) {
            xmlout.pcdata(errorCode);
         } else {
            xmlout.pcdata(faultRequest);
         }
         xmlout.endTag(); // string
         xmlout.endTag(); // value
         xmlout.endTag(); // member

         xmlout.endTag(); // struct
         xmlout.endTag(); // value
         xmlout.endTag(); // fault
      } else {

         String functionName = (String) backpack.get(BackpackConstants.FUNCTION_NAME);

         xmlout.startTag("params");
         xmlout.startTag("param");
         xmlout.startTag("value");
         xmlout.startTag("struct");

         // Write the output parameters
         Map<String, String> outputParameters = xinsResult.getParameters();
         for (Map.Entry<String, String> outputParameter : outputParameters.entrySet()) {
            String parameterName = outputParameter.getKey();
            String parameterValue = outputParameter.getValue();
            String parameterTag = "string";
            try {
               FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
               Type parameterType = functionSpec.getOutputParameter(parameterName).getType();
               parameterValue = convertOutput(parameterType, parameterValue);
               parameterTag = convertType(parameterType);
            } catch (InvalidSpecificationException ise) {

               // keep the old value
            } catch (EntityNotFoundException enfe) {

               // keep the old value
            } catch (java.text.ParseException pex) {

               throw new IOException("Invalid value for parameter \"" + parameterName + "\".");
            }

            // Write the member element
            xmlout.startTag("member");
            xmlout.startTag("name");
            xmlout.pcdata(parameterName);
            xmlout.endTag();
            xmlout.startTag("value");
            xmlout.startTag(parameterTag);
            xmlout.pcdata(parameterValue);
            xmlout.endTag(); // type tag
            xmlout.endTag(); // value
            xmlout.endTag(); // member
         }

         // Write the data section if needed
         Element dataSection = xinsResult.getDataElement();
         if (dataSection != null) {

            Map dataSectionSpec = null;
            try {
               FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
               dataSectionSpec = functionSpec.getOutputDataSectionElements();
            } catch (InvalidSpecificationException ise) {

               // keep the old value
            } catch (EntityNotFoundException enfe) {

               // keep the old value
            }

            xmlout.startTag("member");
            xmlout.startTag("name");
            xmlout.pcdata("data");
            xmlout.endTag();
            xmlout.startTag("value");
            xmlout.startTag("array");
            xmlout.startTag("data");
            for (Element child : new ElementList(dataSection)) {
               writeElement(child, xmlout, dataSectionSpec);
            }
            xmlout.endTag(); // data
            xmlout.endTag(); // array
            xmlout.endTag(); // value
            xmlout.endTag(); // member
         }

         xmlout.endTag(); // struct
         xmlout.endTag(); // value
         xmlout.endTag(); // param
         xmlout.endTag(); // params
      }

      xmlout.endTag(); // methodResponse

      // Write the result to the servlet response
      out.write(buffer.toString());

      out.close();
   }

   /**
    * Parses the data section element.
    *
    * @param valueElem
    *    the value element, cannot be <code>null</code>.
    *
    * @param dataSection
    *    the specification of the elements, cannot be <code>null</code>.
    *
    * @return
    *    the data section element, never <code>null</code>.
    *
    * @throws ParseException
    *    if the XML request is incorrect.
    */
   private Element parseElement(Element valueElem, Map dataSection) throws ParseException {
      Element structElem = new ElementList(valueElem, "struct").getUniqueChildElement();
      DataSectionElementSpec elementSpec;
      Iterator itMemberElems = new ElementList(structElem, "member").iterator();
      Element builder;
      if (itMemberElems.hasNext()) {
         Element memberElem = (Element) itMemberElems.next();
         Element memberNameElem = new ElementList(memberElem, "name").getUniqueChildElement();
         Element memberValueElem = new ElementList(memberElem, "value").getUniqueChildElement();
         Element typeElem = new ElementList(memberValueElem).getUniqueChildElement();
         String parameterName = memberNameElem.getTextContent();
         elementSpec = (DataSectionElementSpec) dataSection.get(parameterName);
         builder = valueElem.getOwnerDocument().createElement(parameterName);
         if (typeElem.getTagName().equals("string")) {
            builder.setTextContent(typeElem.getTextContent());
         } else if (typeElem.getTagName().equals("array")) {
            Map childrenSpec = elementSpec.getSubElements();
            Element dataElem = new ElementList(typeElem, "data").getUniqueChildElement();
            for (Element childValueElem : new ElementList(dataElem, "value")) {
               Element childElem = parseElement(childValueElem, childrenSpec);
               builder.appendChild(childElem);
            }
         } else {
            throw new ParseException("Only \"string\" and \"array\" are valid as member value type.");
         }
      } else {
         throw new ParseException("The \"struct\" element should at least have one member.");
      }

      // Fill in the attributes
      while (itMemberElems.hasNext()) {
         Element memberElem = (Element) itMemberElems.next();
         Element memberNameElem = new ElementList(memberElem, "name").getUniqueChildElement();
         Element memberValueElem = new ElementList(memberElem, "value").getUniqueChildElement();
         Element typeElem = new ElementList(memberValueElem).getUniqueChildElement();
         String parameterName = memberNameElem.getTextContent();
         String parameterValue = typeElem.getTextContent();

         try {
            Type xinsElemType = elementSpec.getAttribute(parameterName).getType();
            parameterValue = convertInput(xinsElemType, memberValueElem);
         } catch (EntityNotFoundException enfe) {

            // keep the old value
         } catch (java.text.ParseException pex) {
            throw new ParseException("Invalid value for parameter \"" + parameterName + "\".");
         }

         builder.setAttribute(parameterName, parameterValue);
      }
      return builder;
   }

   /**
    * Write the given data section element to the output.
    *
    * @param dataElement
    *    the data section element, cannot be <code>null</code>.
    *
    * @param xmlout
    *    the output where the data section element should be serialised, cannot be <code>null</code>.
    *
    * @param dataSectionSpec
    *    the specification of the data element to be written, cannot be <code>null</code>.
    *
    * @throws IOException
    *    if an IO error occurs while writing on the output.
    */
   private void writeElement(Element dataElement, XMLOutputter xmlout, Map dataSectionSpec) throws IOException {
      xmlout.startTag("value");
      xmlout.startTag("struct");
      xmlout.startTag("member");
      xmlout.startTag("name");
      xmlout.pcdata(dataElement.getTagName());
      xmlout.endTag(); // name
      xmlout.startTag("value");
      DataSectionElementSpec elementSpec = (DataSectionElementSpec) dataSectionSpec.get(dataElement.getTagName());
      ElementList children = new ElementList(dataElement);;
      if (!children.isEmpty()) {
         Map childrenSpec = elementSpec.getSubElements();
         xmlout.startTag("array");
         xmlout.startTag("data");
         for (Element nextChild : children) {
            writeElement(nextChild, xmlout, childrenSpec);
         }
         xmlout.endTag(); // data
         xmlout.endTag(); // array
      } else {
         xmlout.startTag("string");
         if (dataElement.getTextContent() != null) {
            xmlout.pcdata(dataElement.getTextContent());
         }
         xmlout.endTag(); // string
      }
      xmlout.endTag(); // value
      xmlout.endTag(); // member

      // Write the attributes
      NamedNodeMap attributesMap = dataElement.getAttributes();
      for (int i = 0; i < attributesMap.getLength(); i++) {
         Attr attribute = (Attr) attributesMap.item(i);
         String attributeName = attribute.getName();
         String attributeValue = attribute.getValue();

         String attributeTag;
         try {
            Type attributeType = elementSpec.getAttribute(attributeName).getType();
            attributeValue = convertOutput(attributeType, attributeValue);
            attributeTag = convertType(attributeType);
         } catch (EntityNotFoundException enfe) {
            attributeTag = "string";
         } catch (java.text.ParseException pex) {
            throw new IOException("Invalid value for parameter \"" + attributeName + "\".");
         }

         xmlout.startTag("member");
         xmlout.startTag("name");
         xmlout.pcdata(attributeName);
         xmlout.endTag(); // name
         xmlout.startTag("value");
         xmlout.startTag(attributeTag);
         xmlout.pcdata(attributeValue);
         xmlout.endTag(); // tag
         xmlout.endTag(); // value
         xmlout.endTag(); // member
      }
      xmlout.endTag(); // struct
      xmlout.endTag(); // value
   }

   /**
    * Converts the XML-RPC input values to XINS input values.
    *
    * @param parameterType
    *    the type of the XINS parameter, cannot be <code>null</code>.
    *
    * @param typeElem
    *    the content of the XML-RPC value, cannot be <code>null</code>.
    *
    * @return
    *    the XINS value, never <code>null</code>.
    *
    * @throws java.text.ParseException
    *    if the parameterValue is incorrect for the type.
    */
   private String convertInput(Type parameterType, Element typeElem) throws java.text.ParseException {
      String xmlRpcType = typeElem.getTagName();
      String parameterValue = typeElem.getTextContent();
      if (parameterType instanceof org.xins.common.types.standard.Boolean) {
         if (parameterValue.equals("1")) {
            return "true";
         } else if (parameterValue.equals("0")) {
            return "false";
         } else {
            throw new java.text.ParseException("Incorrect value for boolean: " + parameterValue, 0);
         }
      }
      //System.err.println("type: " + xmlRpcType + " ; value: " + parameterValue);
      if (xmlRpcType.equals("dateTime.iso8601")) {
         Date date = XML_RPC_TIMESTAMP_FORMATTER.parse(parameterValue);
         if (parameterType instanceof org.xins.common.types.standard.Date) {
            synchronized (XINS_DATE_FORMATTER) {
               return XINS_DATE_FORMATTER.format(date);
            }
         } else if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
            synchronized (XINS_TIMESTAMP_FORMATTER) {
               return XINS_TIMESTAMP_FORMATTER.format(date);
            }
         }
      }
      return parameterValue;
   }

   /**
    * Converts the XINS output values to XML-RPC output values.
    *
    * @param parameterType
    *    the type of the XINS parameter, cannot be <code>null</code>.
    *
    * @param parameterValue
    *    the XINS parameter value to convert, cannot be <code>null</code>.
    *
    * @return
    *    the XML-RPC value, never <code>null</code>.
    *
    * @throws java.text.ParseException
    *    if the parameterValue is incorrect for the type.
    */
   private String convertOutput(Type parameterType, String parameterValue) throws java.text.ParseException {
      if (parameterType instanceof org.xins.common.types.standard.Boolean) {
         if (parameterValue.equals("true")) {
            return "1";
         } else if (parameterValue.equals("false")) {
            return "0";
         } else {
            throw new java.text.ParseException("Incorrect value for boolean: " + parameterValue, 0);
         }
      } else if (parameterType instanceof org.xins.common.types.standard.Date) {
         Date date = null;
         synchronized (XINS_DATE_FORMATTER) {
            date = XINS_DATE_FORMATTER.parse(parameterValue);
         }
         synchronized (XML_RPC_TIMESTAMP_FORMATTER) {
            return XML_RPC_TIMESTAMP_FORMATTER.format(date);
         }
      } else if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
         Date date = null;
         synchronized (XINS_TIMESTAMP_FORMATTER) {
            date = XINS_TIMESTAMP_FORMATTER.parse(parameterValue);
         }
         synchronized (XML_RPC_TIMESTAMP_FORMATTER) {
            return XML_RPC_TIMESTAMP_FORMATTER.format(date);
         }
      }
      return parameterValue;
   }
}
