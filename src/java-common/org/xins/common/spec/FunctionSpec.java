/*
 * $Id: FunctionSpec.java,v 1.34 2013/01/10 21:17:15 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.spec;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.xml.ElementList;
import org.xins.common.xml.ElementFormatter;
import org.xml.sax.SAXException;

/**
 * Specification of a function.
 *
 * @version $Revision: 1.34 $ $Date: 2013/01/10 21:17:15 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.3.0
 */
public final class FunctionSpec {

   /**
    * Name of the function, cannot be <code>null</code>.
    */
   private final String _functionName;

   /**
    * The number of seconds that the result of this function could be cached.
    */
   private int _cache = 0;

   /**
    * Description of the function, cannot be <code>null</code>.
    */
   private String _description;

   /**
    * The input parameters of the function.
    */
   private Map<String,ParameterSpec> _inputParameters = new LinkedHashMap<String,ParameterSpec>();

   /**
    * The input param combos of the function.
    */
   private List<ParamComboSpec> _inputParamCombos = new ArrayList<ParamComboSpec>();

   /**
    * The input data section elements of the function.
    */
   private Map<String,DataSectionElementSpec> _inputDataSectionElements = new LinkedHashMap<String,DataSectionElementSpec>();

   /**
    * The defined error code that the function can return.
    */
   private Map<String,ErrorCodeSpec> _errorCodes = new LinkedHashMap<String,ErrorCodeSpec>();

   /**
    * The output parameters of the function.
    */
   private Map<String,ParameterSpec> _outputParameters = new LinkedHashMap<String,ParameterSpec>();

   /**
    * The output param combos of the function.
    */
   private List<ParamComboSpec> _outputParamCombos = new ArrayList<ParamComboSpec>();

   /**
    * The output data section elements of the function.
    */
   private Map<String,DataSectionElementSpec> _outputDataSectionElements = new LinkedHashMap<String,DataSectionElementSpec>();

   /**
    * Creates a new <code>Function</code> by parsing the function specification file.
    *
    * @param functionName
    *    the name of the function, cannot be <code>null</code>.
    *
    * @param reference
    *    the reference class used to get the defined type class, cannot be <code>null</code>.
    *
    * @param baseURL
    *    the base URL path where are located the specifications, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>functionName == null || reference == null || baseURL == null</code>.
    *
    * @throws InvalidSpecificationException
    *    if the specification is incorrect or cannot be found.
    */
   FunctionSpec(String functionName, Class  reference, String baseURL)
   throws IllegalArgumentException, InvalidSpecificationException {
      MandatoryArgumentChecker.check("functionName", functionName, "reference", reference, "baseURL", baseURL);
      _functionName = functionName;
      try {
         Reader reader = APISpec.getReader(baseURL, functionName + ".fnc");
         parseFunction(reader, reference, baseURL);
      } catch (IOException ioe) {
         throw new InvalidSpecificationException("[Function: " + functionName + "] Cannot read function.", ioe);
      }
   }

   /**
    * Gets the name of the function.
    *
    * @return
    *    the name of the function, never <code>null</code>.
    */
   public String getName() {
      return _functionName;
   }

   /**
    * Gets the number of seconds the result of this method could be cached.
    *
    * @return
    *    the number of seconds the result of this method could be cached,
    *    or <code>0</code> if the result should not be cached.
    *
    * @since XINS 3.1
    */
   public int getCache() {
      return _cache;
   }


   /**
    * Sets the number of seconds the result of this method could be cached.
    *
    * @param cache
    *    the number of seconds the result of this method could be cached,
    *    or <code>0</code> if the result should not be cached.
    *
    * @since XINS 3.1
    */
   public void setCache(int cache) {
      _cache = cache;
   }

   /**
    * Gets the description of the function.
    *
    * @return
    *    the description of the function, never <code>null</code>.
    */
   public String getDescription() {
      return _description;
   }

   /**
    * Gets the input parameter for the specified name.
    *
    * @param parameterName
    *    the name of the parameter, cannot be <code>null</code>.
    *
    * @return
    *    the parameter, never <code>null</code>.
    *
    * @throws EntityNotFoundException
    *    if the function does not contain any input parameter with the specified name.
    *
    * @throws IllegalArgumentException
    *    if <code>parameterName == null</code>.
    */
   public ParameterSpec getInputParameter(String parameterName)
   throws EntityNotFoundException, IllegalArgumentException {

      MandatoryArgumentChecker.check("parameterName", parameterName);

      ParameterSpec parameter = (ParameterSpec) _inputParameters.get(parameterName);
      if (parameter == null) {
         throw new EntityNotFoundException("Input parameter \"" + parameterName + "\" not found.");
      }

      return parameter;
   }

   /**
    * Gets the input parameter specifications defined in the function.
    * The key is the name of the parameter, the value is the {@link ParameterSpec} object.
    *
    * @return
    *    the input parameters, never <code>null</code>.
    */
   public Map<String,ParameterSpec> getInputParameters() {
      return Collections.unmodifiableMap(_inputParameters);
   }

   /**
    * Gets the output parameter of the specified name.
    *
    * @param parameterName
    *    the name of the parameter, cannot be <code>null</code>.
    *
    * @return
    *    the parameter, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>parameterName == null</code>.
    *
    * @throws EntityNotFoundException
    *    if the function does not contain any output parameter with the specified name.
    */
   public ParameterSpec getOutputParameter(String parameterName)
   throws IllegalArgumentException, EntityNotFoundException {

      MandatoryArgumentChecker.check("parameterName", parameterName);

      ParameterSpec parameter = (ParameterSpec) _outputParameters.get(parameterName);
      if (parameter == null) {
         throw new EntityNotFoundException("Output parameter \"" + parameterName + "\" not found.");
      }

      return parameter;
   }

   /**
    * Gets the output parameter specifications defined in the function.
    * The key is the name of the parameter, the value is the {@link ParameterSpec} object.
    *
    * @return
    *    the output parameters, never <code>null</code>.
    */
   public Map<String,ParameterSpec> getOutputParameters() {
      return Collections.unmodifiableMap(_outputParameters);
   }

   /**
    * Gets the error code specification for the specified error code.
    *
    * @param errorCodeName
    *    the name of the error code, cannot be <code>null</code>.
    *
    * @return
    *    the error code specifications, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>errorCodeName == null</code>.
    *
    * @throws EntityNotFoundException
    *    if the function does not define any error code with the specified name.
    */
   public ErrorCodeSpec getErrorCode(String errorCodeName)
   throws IllegalArgumentException, EntityNotFoundException {

      MandatoryArgumentChecker.check("errorCodeName", errorCodeName);
      ErrorCodeSpec errorCode = _errorCodes.get(errorCodeName);
      if (errorCode == null) {
         throw new EntityNotFoundException("Error code \"" + errorCodeName + "\" not found.");
      }

      return errorCode;
   }

   /**
    * Gets the error code specifications defined in the function.
    * The standard error codes are not included.
    * The key is the name of the error code, the value is the {@link ErrorCodeSpec} object.
    *
    * @return
    *    the error code specifications, never <code>null</code>.
    */
   public Map<String,ErrorCodeSpec> getErrorCodes() {
      return Collections.unmodifiableMap(_errorCodes);
   }

   /**
    * Gets the specification of the element of the input data section with the
    * specified name.
    *
    * @param elementName
    *    the name of the element, cannot be <code>null</code>.
    *
    * @return
    *    the specification of the input data section element, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>elementName == null</code>.
    *
    * @throws EntityNotFoundException
    *    if the function does not define any input data element with the specified name.
    */
   public DataSectionElementSpec getInputDataSectionElement(String elementName)
   throws IllegalArgumentException, EntityNotFoundException {

      MandatoryArgumentChecker.check("elementName", elementName);

      DataSectionElementSpec element = _inputDataSectionElements.get(elementName);
      if (element == null) {
         throw new EntityNotFoundException("Input data section element \"" + elementName + "\" not found.");
      }

      return element;
   }

   /**
    * Gets the specification of the elements of the input data section.
    * The key is the name of the element, the value is the {@link DataSectionElementSpec} object.
    *
    * @return
    *    the input data section elements, never <code>null</code>.
    */
   public Map<String,DataSectionElementSpec> getInputDataSectionElements() {
      return _inputDataSectionElements;
   }

   /**
    * Gets the specification of the element of the output data section with the
    * specified name.
    *
    * @param elementName
    *    the name of the element, cannot be <code>null</code>.
    *
    * @return
    *   The specification of the output data section element, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>elementName == null</code>.
    *
    * @throws EntityNotFoundException
    *    if the function does not define any output data element with the specified name.
    */
   public DataSectionElementSpec getOutputDataSectionElement(String elementName)
   throws IllegalArgumentException, EntityNotFoundException {

      MandatoryArgumentChecker.check("elementName", elementName);

      DataSectionElementSpec element = (DataSectionElementSpec) _outputDataSectionElements.get(elementName);
      if (element == null) {
         throw new EntityNotFoundException("Output data section element \"" + elementName + "\" not found.");
      }

      return element;
   }

   /**
    * Gets the specification of the elements of the output data section.
    * The key is the name of the element, the value is the {@link DataSectionElementSpec} object.
    *
    * @return
    *    the output data section elements, never <code>null</code>.
    */
   public Map<String,DataSectionElementSpec> getOutputDataSectionElements() {
      return _outputDataSectionElements;
   }

   /**
    * Gets the input param combo specifications.
    *
    * @return
    *    the list of the input param combos specification
    *    ({@link ParamComboSpec}), never <code>null</code>.
    */
   public List<ParamComboSpec> getInputParamCombos() {
      return Collections.unmodifiableList(_inputParamCombos);
   }

   /**
    * Gets the output param combo specifications.
    *
    * @return
    *    the list of the output param combos specification
    *    ({@link ParamComboSpec}), never <code>null</code>.
    */
   public List<ParamComboSpec> getOutputParamCombos() {
      return Collections.unmodifiableList(_outputParamCombos);
   }

   /**
    * Parses the function specification file.
    *
    * @param reader
    *    the reader that contains the content of the result code file, cannot be <code>null</code>.
    *
    * @param reference
    *    the reference class used to get the defined type class, cannot be <code>null</code>.
    *
    * @param baseURL
    *    the base URL path where are located the specifications, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>reader == null || reference == null || baseURL == null</code>.
    *
    * @throws IOException
    *    if the parser cannot read the content.
    *
    * @throws InvalidSpecificationException
    *    if the result code file is incorrect.
    */
   private void parseFunction(Reader reader, Class reference, String baseURL)
   throws IllegalArgumentException, IOException, InvalidSpecificationException {

      MandatoryArgumentChecker.check("reader", reader, "reference", reference, "baseURL", baseURL);

      Element function;
      try {
         function = ElementFormatter.parse(reader);
      } catch (SAXException pe) {
         throw new InvalidSpecificationException("[Function: " + _functionName + "] Cannot parse function.", pe);
      }
      String cacheValue = function.getAttribute("cache");
      if (!cacheValue.equals("")) {
         _cache = Integer.parseInt(cacheValue);
      }
      ElementList descriptionElementList = new ElementList(function, "description");
      if (descriptionElementList.isEmpty()) {
         throw new InvalidSpecificationException("[Function: " + _functionName + "] No definition specified.");
      }
      Element descriptionElement = descriptionElementList.get(0);
      _description = descriptionElement.getTextContent();
      ElementList input = new ElementList(function, "input");
      if (!input.isEmpty()) {

         // Input parameters
         Element inputElement = input.get(0);
         _inputParameters = parseParameters(reference, inputElement);

         // Param combos
         _inputParamCombos = parseCombos(inputElement, _inputParameters, true);

         // Data section
         ElementList dataSections = new ElementList(inputElement, "data");
         if (!dataSections.isEmpty()) {
            Element dataSection = dataSections.get(0);
            _inputDataSectionElements = parseDataSectionElements(reference, dataSection, dataSection);
         }
      }

      ElementList output = new ElementList(function, "output");
      if (!output.isEmpty()) {
         Element outputElement = output.get(0);

         // Error codes
         ElementList errorCodesList = new ElementList(outputElement, "resultcode-ref");
         for (Element nextErrorCode : errorCodesList) {
            String errorCodeName = nextErrorCode.getAttribute("name");
            if (errorCodeName.equals("")) {
               throw new InvalidSpecificationException("[Function: " + _functionName + "] Missing name attribute for a error code.");
            }
            if (errorCodeName.indexOf('/') != -1) {
               errorCodeName = errorCodeName.substring(errorCodeName.indexOf('/') + 1);
            }
            ErrorCodeSpec errorCodeSpec = new ErrorCodeSpec(errorCodeName, reference, baseURL);
            _errorCodes.put(errorCodeName, errorCodeSpec);
         }

         // Output parameters
         _outputParameters = parseParameters(reference, outputElement);

         // Param combos
         _outputParamCombos = parseCombos(outputElement, _outputParameters, true);

         // Data section
         ElementList dataSections = new ElementList(outputElement, "data");
         if (!dataSections.isEmpty()) {
            Element dataSection = dataSections.get(0);
            _outputDataSectionElements = parseDataSectionElements(reference, dataSection, dataSection);
         }
      }
   }

   /**
    * Parse an element in the data section.
    *
    * @param reference
    *    the reference class used to locate the files, cannot be <code>null</code>.
    *
    * @param topElement
    *    the element to parse, cannot be <code>null</code>.
    *
    * @param dataSection
    *    the data section, cannot be <code>null</code>.
    *
    * @return
    *    the top elements of the data section, or an empty array there is no
    *    data section.
    *
    * @throws IllegalArgumentException
    *    if <code>reference == null || topElement == null || dataSection == null</code>.
    *
    * @throws InvalidSpecificationException
    *    if the specification is incorrect.
    */
   static Map<String,DataSectionElementSpec> parseDataSectionElements(Class reference, Element topElement, Element dataSection)
   throws IllegalArgumentException, InvalidSpecificationException {

      MandatoryArgumentChecker.check("reference", reference, "topElement", topElement, "dataSection", dataSection);

      Map<String,DataSectionElementSpec> dataSectionElements = new LinkedHashMap<String,DataSectionElementSpec>();

      // The <data> may have a "contains" attribute.
      String dataContainsAttr = topElement.getAttribute("contains");
      if (!dataContainsAttr.equals("")) {
         DataSectionElementSpec dataSectionElement = getDataSectionElement(reference, dataContainsAttr, dataSection);
         dataSectionElements.put(dataContainsAttr, dataSectionElement);
      }

      // Gets the sub elements of this element
      ElementList dataSectionContains = new ElementList(topElement, "contains");
      if (!dataSectionContains.isEmpty()) {
         Element containsElement = dataSectionContains.get(0);
         ElementList contained = new ElementList(containsElement, "contained");
         for (Element containedElement : contained) {
            String name = containedElement.getAttribute("element");
            DataSectionElementSpec dataSectionElement = getDataSectionElement(reference, name, dataSection);
            dataSectionElements.put(name, dataSectionElement);
         }
      }
      return dataSectionElements;
   }

   /**
    * Gets the specified element in the data section.
    *
    * @param reference
    *    the reference class used to locate the files, cannot be <code>null</code>.
    *
    * @param name
    *    the name of the element to retreive, cannot be <code>null</code>.
    *
    * @param dataSection
    *    the data section, cannot be <code>null</code>.
    *
    * @return
    *    the data section element or <code>null</code> if there is no element
    *    with the specified name.
    *
    * @throws IllegalArgumentException
    *    if <code>reference == null || name == null || dataSection == null</code>.
    *
    * @throws InvalidSpecificationException
    *    if the specification is incorrect.
    */
   static DataSectionElementSpec getDataSectionElement(Class reference, String name, Element dataSection)
   throws IllegalArgumentException, InvalidSpecificationException {
      MandatoryArgumentChecker.check("reference", reference, "name", name, "dataSection", dataSection);
      ElementList elements = new ElementList(dataSection, "element");
      for (Element nextElement : elements) {
         String nextName = nextElement.getAttribute("name");
         if (name.equals(nextName)) {

            String description = new ElementList(nextElement, "description").get(0).getTextContent();

            Map subElements = parseDataSectionElements(reference, nextElement, dataSection);

            boolean isPcdataEnable = false;
            ElementList dataSectionContains = new ElementList(nextElement, "contains");
            if (!dataSectionContains.isEmpty()) {
               Element containsElement = dataSectionContains.get(0);
               List pcdata = new ElementList(containsElement, "pcdata");
               if (!pcdata.isEmpty()) {
                  isPcdataEnable = true;
               }
            }

            ElementList attributesList = new ElementList(nextElement, "attribute");
            Map<String, ParameterSpec> attributes = new LinkedHashMap();
            for (Element nextAttribute : attributesList) {
               ParameterSpec attribute = parseParameter(reference, nextAttribute);
               attributes.put(attribute.getName(), attribute);
            }

            List attributeCombos = parseCombos(nextElement, attributes, false);
            DataSectionElementSpec result = new DataSectionElementSpec(nextName,
                  description, isPcdataEnable, subElements, attributes, attributeCombos);
            return result;
         }
      }
      return null;
   }

   /**
    * Parses a function parameter or an attribute of a data section element.
    *
    * @param reference
    *    the reference class used to locate the files, cannot be <code>null</code>.
    *
    * @param paramElement
    *    the element that contains the specification of the parameter, cannot be <code>null</code>.
    *
    * @return
    *    the parameter, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>reference == null || paramElement == null</code>.
    *
    * @throws InvalidSpecificationException
    *    if the specification is incorrect.
    */
   static ParameterSpec parseParameter(Class reference, Element paramElement)
   throws IllegalArgumentException, InvalidSpecificationException {
      MandatoryArgumentChecker.check("reference", reference, "paramElement", paramElement);
      String parameterName = paramElement.getAttribute("name");
      if (parameterName.equals("")) {
         throw new InvalidSpecificationException("Missing name for a parameter.");
      }
      String parameterTypeName = paramElement.getAttribute("type");
      boolean requiredParameter = "true".equals(paramElement.getAttribute("required"));
      ElementList descriptionElementList = new ElementList(paramElement, "description");
      String parameterDefaultValue = paramElement.getAttribute("default");
      if (descriptionElementList.isEmpty()) {
         throw new InvalidSpecificationException("No definition specified for a parameter.");
      }
      String parameterDescription = descriptionElementList.get(0).getTextContent();
      ParameterSpec parameter = new ParameterSpec(reference ,parameterName,
            parameterTypeName, requiredParameter, parameterDescription, parameterDefaultValue);
      return parameter;
   }

   /**
    * Parses the input or output parameters.
    *
    * @param reference
    *    the reference class used to locate the files, cannot be <code>null</code>.
    *
    * @param topElement
    *    the input or output element, cannot be <code>null</code>.
    *
    * @return
    *    a map containing the parameter names as keys, and the
    *    <code>Parameter</code> objects as value, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>reference == null || topElement == null</code>.
    *
    * @throws InvalidSpecificationException
    *    if the specification is incorrect.
    */
   static Map<String, ParameterSpec> parseParameters(Class reference, Element topElement)
   throws IllegalArgumentException, InvalidSpecificationException {
      MandatoryArgumentChecker.check("reference", reference, "topElement", topElement);
      ElementList parametersList = new ElementList(topElement, "param");
      Map<String, ParameterSpec> parameters = new LinkedHashMap<String, ParameterSpec>();
      for (Element nextParameter : parametersList) {
         ParameterSpec parameter = parseParameter(reference, nextParameter);
         parameters.put(parameter.getName(), parameter);
      }
      return parameters;
   }

   /**
    * Parses the param-combo element.
    *
    * @param topElement
    *    the input or output element, cannot be <code>null</code>.
    *
    * @param parameters
    *    the list of the input or output parameters or attributes, cannot be <code>null</code>.
    *
    * @param paramCombo
    *    <code>true</code> if a param-combo should be parsed, <code>false</code>
    *    if an attribute-combo should be parsed.
    *
    * @return
    *    the list of the param-combo elements or an empty array if no
    *    param-combo is defined, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>topElement == null || parameters == null</code>.
    *
    * @throws InvalidSpecificationException
    *    if the format of the param-combo is incorrect.
    */
   static List parseCombos(Element topElement, Map parameters, boolean paramCombo)
   throws IllegalArgumentException, InvalidSpecificationException {
      MandatoryArgumentChecker.check("topElement", topElement, "parameters", parameters);
      String comboTag = paramCombo ? "param-combo" : "attribute-combo";
      String referenceTag = paramCombo ? "param-ref" : "attribute-ref";
      ElementList paramCombosList = new ElementList(topElement, comboTag);
      List paramCombos = new ArrayList(paramCombosList.size());
      for (Element nextParamCombo : paramCombosList) {
         String type = nextParamCombo.getAttribute("type");
         if (type.equals("")) {
            throw new InvalidSpecificationException("No type defined for " + comboTag + ".");
         }
         ElementList paramDefs = new ElementList(nextParamCombo, referenceTag);
         Map paramComboParameters = new LinkedHashMap();
         for (Element paramDef : paramDefs) {
            String parameterName = paramDef.getAttribute("name");
            if (parameterName.equals("")) {
               throw new InvalidSpecificationException("Missing name for a parameter in " + comboTag + ".");
            }
            ParameterSpec parameter = (ParameterSpec) parameters.get(parameterName);
            if (parameter == null) {
               throw new InvalidSpecificationException("Incorrect parameter name \"" +
                     parameterName + "\" in " + comboTag + ".");
            }
            paramComboParameters.put(parameterName, parameter);
         }
         if (paramCombo) {
            ParamComboSpec paramComboSpec = new ParamComboSpec(type, paramComboParameters);
            paramCombos.add(paramComboSpec);
         } else {
            AttributeComboSpec paramComboSpec = new AttributeComboSpec(type, paramComboParameters);
            paramCombos.add(paramComboSpec);
         }
      }
      return paramCombos;
   }
}
