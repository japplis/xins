/*
 * $Id: SOAPMapCallingConvention.java,v 1.19 2013/01/12 16:36:29 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xins.common.spec.DataSectionElementSpec;
import org.xins.common.spec.EntityNotFoundException;
import org.xins.common.spec.FunctionSpec;
import org.xins.common.spec.InvalidSpecificationException;
import org.xins.common.spec.ParameterSpec;
import org.xins.common.text.ParseException;
import org.xins.common.types.Type;
import org.xins.common.xml.DataElementBuilder;
import org.xins.common.xml.ElementFormatter;
import org.xins.common.xml.ElementList;


/**
 * The SOAP calling convention that tries to map the SOAP request to the
 * parameters of the function. The rules applied for the mapping are the same
 * as for the command wsdl-to-api.
 * <p/>
 * Note that by default any SOAP message will be handled by the _xins_soap
 * calling convention. If you want to use this calling convention you will
 * need to explicitly have _convention=_xins_soap_map in the URL parameters.
 * <p/>
 * This calling convention is easily extendable in order to adapt to the
 * specificity of your SOAP requests.
 * <p/>
 * Here is the mapping for the input:
 * <ul>
 * <li>If the element in the Body ends with 'Request', the function name is
 * considered to be what is specified before</li>
 * <li>Otherwise the name of the element is used for the name of the function</li>
 * <li>Elements in the request are mapped to input parameters if available.</li>
 * <li>Elements with sub-elements are mapped to input parameters element1.sub-element1... if available.</li>
 * <li>If no parameter is found, try to find an input data element with the name.</li>
 * <li>If not found, go to the sub-elements and try to find an input data element with the name.</li>
 * <li>If not found, skip it. Here it's up to you to override this convention and provide a mapping.</li>
 * </ul>
 * <p/>
 * Here is the mapping for the output:
 * <ul>
 * <li>Response name = function name + "Response"</li>
 * <li>Output parameters with dots are transformed to XML.
 * e.g. element1.element2 -&gt; &lt;element1&gt;&lt;element2&gt;value&lt;/element2&gt;&lt;/element1&gt;</li>
 * <li>The data section is not put in the returned XML, only the elements it contains.</li>
 * <li>Data section element attributes are changed to sub-elements with the
 * same rule as for output parameters.</li>
 * </ul>
 *
 * @version $Revision: 1.19 $ $Date: 2013/01/12 16:36:29 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.1.
 */
public class SOAPMapCallingConvention extends SOAPCallingConvention {

   /**
    * The key used to store the Envelope element of the request.
    */
   protected static final String REQUEST_ENVELOPE = "_envelope";

   /**
    * The key used to store the Body element of the request.
    */
   protected static final String REQUEST_BODY = "_body";

   /**
    * The key used to store the function element of the request.
    */
   protected static final String REQUEST_FUNCTION = "_function_request";

   /**
    * Creates a new <code>SOAPCallingConvention</code> instance.
    *
    * @param api
    *    the API, needed for the SOAP messages, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>api == null</code>.
    */
   public SOAPMapCallingConvention(API api) throws IllegalArgumentException {
      super(api);
   }

   protected boolean matches(HttpServletRequest httpRequest)
   throws Exception {

      return false;
   }

   protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
   throws InvalidRequestException,
          FunctionNotSpecifiedException {
      Map<String, Object> backpack = new HashMap<String, Object>();

      Element envelopeElem = parseXMLRequest(httpRequest);
      String envelopeName = envelopeElem.getLocalName();

      if (! envelopeName.equals("Envelope")) {
         throw new InvalidRequestException("Root element is not a SOAP envelope but \"" +
               envelopeName + "\".");
      }
      backpack.put(REQUEST_ENVELOPE, cloneElement(envelopeElem));

      String functionName;
      Element functionElem;
      try {
         Element bodyElem = new ElementList(envelopeElem, "Body").getUniqueChildElement();
         backpack.put(REQUEST_BODY, cloneElement(bodyElem));
         functionElem = new ElementList(bodyElem).getUniqueChildElement();
         backpack.put(REQUEST_FUNCTION, cloneElement(functionElem));
      } catch (ParseException pex) {
         throw new InvalidRequestException("Incorrect SOAP message.", pex);
      }
      String requestName = functionElem.getLocalName();
      if (!requestName.endsWith("Request")) {
         functionName = requestName;
      } else {
         functionName = requestName.substring(0, requestName.lastIndexOf("Request"));
      }

      // Parse the input parameters
      FunctionRequest functionRequest = readInput(functionElem, functionName, backpack);

      // If there is information in the SOAP Header that you want to store in
      // the HTTP request or for input parameters or input data section,
      // parse the SOAP Header here and fill the functionRequest or httpRequest
      // with the wanted data.

      return functionRequest;
   }

   /**
    * Generates the function request based the the SOAP request.
    * This function will get the XML element in the SOAP request and associate
    * the values with the input parameter or data section element of the function.
    *
    * @param functionElem
    *    the SOAP element of the function request, cannot be <code>null</code>.
    * @param functionName
    *    the name of the function, cannot be <code>null</code>.
    * @param backpack
    *    the backpack of this request, cannot be <code>null</code>.
    *
    * @return
    *    the function request that will be passed to the XINS function, cannot be <code>null</code>.
    */
   protected FunctionRequest readInput(Element functionElem, String functionName, Map<String, Object> backpack) {
      Map<String, String> inputParams = new HashMap<String, String>();
      DataElementBuilder dataSectionBuilder = new DataElementBuilder();
      for (Element parameterElem : new ElementList(functionElem)) {
         try {
            Element dataElement = readInputElem(parameterElem, functionName, null, null, inputParams);
            if (dataElement != null) {
               dataSectionBuilder.addToDataElement(dataElement);
            }
         } catch (Exception ex) {
            Log.log_3571(ex, parameterElem.getTagName(), functionName);
         }
      }
      return new FunctionRequest(functionName, inputParams, dataSectionBuilder.getDataElement(), backpack);
   }

   /**
    * Parses the SOAP request element according to the rules specified in this
    * <a href="_top">class description</a>.
    *
    * @param inputElem
    *    the SOAP request element, cannot be <code>null</code>.
    *
    * @param functionName
    *    the name of the function, cannot be <code>null</code>.
    *
    * @param parent
    *    the name of the super element, can be <code>null</code>.
    *
    * @param parentElement
    *    the input data element that is being created, can be <code>null</code>.
    *
    * @param inputParams
    *    the Map where the input parameters should be stored, cannot be <code>null</code>.
    *
    * @return
    *    the input data element for the FunctionRequest or <code>null</code> if the SOAP
    *    request does not need to create a input data element.
    *
    * @throws Exception
    *    if anything goes wrong such specifications not available or incorrect SOAP request.
    */
   protected Element readInputElem(Element inputElem, String functionName, String parent,
         Element parentElement, Map<String, String> inputParams) throws Exception {
      FunctionSpec functionSpec = getAPI().getAPISpecification().getFunction(functionName);
      Map inputParamsSpec = functionSpec.getInputParameters();
      Map inputDataSectionSpec = functionSpec.getInputDataSectionElements();
      String parameterName = inputElem.getLocalName();
      String fullName = parent == null ? parameterName : parent + "." + parameterName;
      boolean hasInputChild = !new ElementList(inputElem).isEmpty();

      // Fill the attribute of the input data section with the SOAP sub-elements
      if (parentElement != null) {
         String parentElementName = parentElement.getLocalName();
         if (parentElementName == null) {
            parentElementName = parentElement.getTagName();
         }
         DataSectionElementSpec elementSpec =
               (DataSectionElementSpec) inputDataSectionSpec.get(parentElementName);
         if (elementSpec != null && elementSpec.getAttributes().containsKey(parameterName) &&
               !hasInputChild) {
            String parameterValue = inputElem.getTextContent();
            Type parameterType = elementSpec.getAttribute(parameterName).getType();
            parameterValue = soapInputValueTransformation(parameterType, parameterValue);
            parentElement.setAttribute(parameterName, parameterValue);
         } else if (elementSpec != null && hasInputChild) {
            //Add a sub-element
            Element middleElement = parentElement.getOwnerDocument().createElement(inputElem.getLocalName());
            parentElement.appendChild(middleElement);
            for (Element parameterElem : new ElementList(inputElem)) {
               // read sub-sub-elements
               readInputElem(parameterElem, functionName, parameterName, middleElement, inputParams);
            }
         }
      // Simple input parameter that maps
      } else if (inputParamsSpec.containsKey(fullName) && !hasInputChild) {
         String parameterValue = inputElem.getTextContent();
         Type parameterType = ((ParameterSpec) inputParamsSpec.get(fullName)).getType();
         parameterValue = soapInputValueTransformation(parameterType, parameterValue);
         inputParams.put(fullName, parameterValue);

      // Element with sub-elements
      } else if (hasInputChild) {

         // It can be in the parameters or in the data section
         Iterator itParamNames = inputParamsSpec.keySet().iterator();
         boolean found = false;
         while (itParamNames.hasNext() && !found) {
            String nextParamName = (String) itParamNames.next();
            if (nextParamName.startsWith(fullName + ".")) {
               found = true;
            }
         }

         // The sub element match a input parameter
         if (found) {
            for (Element parameterElem : new ElementList(inputElem)) {
               readInputElem(parameterElem, functionName, fullName, null, inputParams);
            }

         // The sub element match a input data element
         } else if (inputDataSectionSpec.containsKey(parameterName)) {
            Element dataElement = inputElem.getOwnerDocument().createElement(parameterName);
            for (Element parameterElem : new ElementList(inputElem)) {
               readInputElem(parameterElem, functionName, null, dataElement, inputParams);
            }
            return dataElement;

         // Ignore this element and go throw the sub-elements
         } else {
            for (Element parameterElem : new ElementList(inputElem)) {
               readInputElem(parameterElem, functionName, parent, null, inputParams);
            }
         }
      } else {
         Log.log_3570(inputElem.getLocalName(), functionName);
      }
      return null;
   }

   protected void convertResultImpl(FunctionResult      xinsResult,
                                    HttpServletResponse httpResponse,
                                    Map<String, Object> backpack)
   throws IOException {

      // Send the XML output to the stream and flush
      httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
      PrintWriter out = httpResponse.getWriter();
      if (xinsResult.getErrorCode() != null) {
         httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } else {
         httpResponse.setStatus(HttpServletResponse.SC_OK);
      }

      Element envelope = writeResponse(xinsResult, backpack);

      // Write the result to the servlet response
      String envelopeString = ElementFormatter.format(envelope);
      out.write(envelopeString);

      out.close();
   }

   protected Element writeResponse(FunctionResult xinsResult, Map<String, Object> backpack)
   throws IOException {

      Element requestEnvelope = (Element) backpack.get(REQUEST_ENVELOPE);
      String envelopeQualifiedName = requestEnvelope.getPrefix() == null ? "Envelope"
              : requestEnvelope.getPrefix() + ":Envelope";
      Element envelope = ElementFormatter.createMainElementNS(requestEnvelope.getNamespaceURI(), envelopeQualifiedName);
      copyAttributes(requestEnvelope, envelope);

      // If you want to write the SOAP Header to the response, do it here

      Element requestBody = (Element) backpack.get(REQUEST_BODY);
      String bodyQualifiedName = requestBody.getPrefix() == null ? "Body"
              : requestBody.getPrefix() + ":Body";
      Element body = envelope.getOwnerDocument().createElementNS(requestBody.getNamespaceURI(), bodyQualifiedName);
      copyAttributes(requestBody, body);
      envelope.appendChild(body);

      String functionName = (String) backpack.get(BackpackConstants.FUNCTION_NAME);

      if (xinsResult.getErrorCode() != null) {
         //writeFaultSection(functionName, namespaceURI, xinsResult, xmlout);
      } else {

         // Write the response start tag
         Element requestFunction = (Element) backpack.get(REQUEST_FUNCTION);
         String functionQualifiedName = requestFunction.getPrefix() == null ? functionName + "Response"
                 : requestFunction.getPrefix() + ":" + functionName + "Response";
         Element response = envelope.getOwnerDocument().createElementNS(requestFunction.getNamespaceURI(), functionQualifiedName);
         copyAttributes(requestFunction, response);

         writeOutputParameters(functionName, xinsResult, response);
         writeOutputDataSection(functionName, xinsResult, response);
         body.appendChild(response);
      }
      return envelope;
   }

   /**
    * Writes the output parameters to the SOAP XML.
    *
    * @param functionName
    *    the name of the function called, cannot be <code>null</code>.
    *
    * @param xinsResult
    *    the result of the call to the function, cannot be <code>null</code>.
    *
    * @param response
    *    the SOAP response element, cannot be <code>null</code>.
    */
   protected void writeOutputParameters(String functionName, FunctionResult xinsResult, Element response) {
      Iterator outputParameterNames = xinsResult.getParameters().keySet().iterator();
      while (outputParameterNames.hasNext()) {
         String parameterName = (String) outputParameterNames.next();
         String parameterValue = xinsResult.getParameter(parameterName);
         try {
            FunctionSpec functionSpec = getAPI().getAPISpecification().getFunction(functionName);
            Type parameterType = functionSpec.getOutputParameter(parameterName).getType();
            parameterValue = soapOutputValueTransformation(parameterType, parameterValue);
         } catch (InvalidSpecificationException ise) {

            // keep the old value
         } catch (EntityNotFoundException enfe) {

            // keep the old value
         }
         writeOutputParameter(parameterName, parameterValue, response);
      }
   }

   /**
    * Write an output parameter to the SOAP response.
    *
    * @param parameterName
    *    the name of the output parameter, cannot be <code>null</code>.
    *
    * @param parameterValue
    *    the value of the output parameter, cannot be <code>null</code>.
    *
    * @param parent
    *    the parent element to put the created element in, cannot be <code>null</code>.
    */
   protected void writeOutputParameter(String parameterName, String parameterValue, Element parent) {
      String paramPrefix = parent.getNamespaceURI() == null ? parent.getPrefix() + ":" : "";
      if (parameterName.indexOf('.') == -1) {
         Element paramElem = parent.getOwnerDocument().createElementNS(parent.getNamespaceURI(), paramPrefix + parameterName);
         paramElem.setTextContent(parameterValue);
         parent.appendChild(paramElem);
      } else {
         String elementName = parameterName.substring(0, parameterName.indexOf('.'));
         String rest = parameterName.substring(parameterName.indexOf('.') + 1);
         Element paramElem = null;
         ElementList paramElemChildren = new ElementList(parent, elementName);
         if (!paramElemChildren.isEmpty()) {
            paramElem = paramElemChildren.get(0);
            writeOutputParameter(rest, parameterValue, paramElem);
         } else {
            paramElem = parent.getOwnerDocument().createElementNS(parent.getNamespaceURI(), paramPrefix + elementName);
            writeOutputParameter(rest, parameterValue, paramElem);
            parent.appendChild(paramElem);
         }
      }
   }

   /**
    * Writes the output data section to the SOAP XML.
    *
    * @param functionName
    *    the name of the function called.
    *
    * @param xinsResult
    *    the result of the call to the function.
    *
    * @param response
    *    the SOAP response element, cannot be <code>null</code>.
    */
   protected void writeOutputDataSection(String functionName, FunctionResult xinsResult, Element response) {
      Map dataSectionSpec = null;
      try {
         FunctionSpec functionSpec = getAPI().getAPISpecification().getFunction(functionName);
         dataSectionSpec = functionSpec.getOutputDataSectionElements();
      } catch (InvalidSpecificationException ise) {
      } catch (EntityNotFoundException enfe) {
      }
      Element dataElement = xinsResult.getDataElement();
      if (dataElement != null) {
         Element importedDataElement = (Element) response.getOwnerDocument().importNode(dataElement, true);
         for (Element nextDataElement : new ElementList(importedDataElement)) {
            writeOutputDataElement(dataSectionSpec, nextDataElement, response);
         }
      }
   }

   /**
    * Write the given output data element in the SOAP response.
    *
    * @param dataSectionSpec
    *    the specification of the output data elements for the function, cannot be <code>null</code>.
    *
    * @param dataElement
    *    the data element to tranform as SOAP element, cannot be <code>null</code>.
    *
    * @param parent
    *    the parent element to add the created element, cannot be <code>null</code>.
    */
   protected void writeOutputDataElement(Map dataSectionSpec, Element dataElement, Element parent) {

      // Set a prefix to the data element in order to be copied to the created SOAP element
      if (parent.getNamespaceURI() == null) {
         dataElement.setPrefix(parent.getPrefix());
      }

      Element transformedDataElement = soapElementTransformation(dataSectionSpec, false, dataElement, false);
      parent.appendChild(transformedDataElement);
   }

   @Override
   protected void setDataElementAttribute(Element builder, String attributeName,
         String attributeValue, String elementNameSpacePrefix) {
      if (attributeName.indexOf(".") == -1) {
         Element dataElement = builder.getOwnerDocument().createElementNS(builder.getNamespaceURI(), elementNameSpacePrefix + attributeName);
         dataElement.setTextContent(attributeValue);
         builder.appendChild(dataElement);
      } else {
         String elementName = attributeName.substring(0, attributeName.indexOf("."));
         String rest = attributeName.substring(attributeName.indexOf(".") + 1);
         Element paramElem = builder.getOwnerDocument().createElementNS(builder.getNamespaceURI(), elementNameSpacePrefix + elementName);
         writeOutputParameter(rest, attributeValue, paramElem);
         builder.appendChild(paramElem);
      }
   }

   /**
    * Utility method that clones an Element without the children.
    *
    * @param element
    *   the element to be cloned, cannot be <code>null</code>.
    *
    * @return
    *   an element which is identical to the given element but with no sub-elements, never <code>null</code>.
    */
   private Element cloneElement(Element element) {
      Element result = (Element) element.cloneNode(true);
      return result;
   }

   /**
    * Utility method that copies the attributes of an element to another element.
    * Note that the name space URI is not copied.
    *
    * @param source
    *   the source element to get the attributes from, cannot be <code>null</code>.
    *
    * @param target
    *   the target element to copy the attributes to, cannot be <code>null</code>.
    */
   private void copyAttributes(Element source, Element target) {
      NamedNodeMap attributes = source.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
         Attr nextAttribute = (Attr) attributes.item(i);
         String attrName = nextAttribute.getName();
         String attrValue = nextAttribute.getValue();
         target.setAttributeNS(nextAttribute.getNamespaceURI(), attrName, attrValue);
      }
   }
}
