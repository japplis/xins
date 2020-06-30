/*
 * $Id: InvalidMessageResult.java,v 1.10 2012/03/03 10:41:19 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Result code that indicates that a request or a response parameter is either
 * missing or invalid.
 *
 * @version $Revision: 1.10 $ $Date: 2012/03/03 10:41:19 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.0
 */
class InvalidMessageResult extends FunctionResult {

   /**
    * Constructs a new <code>InvalidMessageResult</code> object.
    *
    * @param errorCode
    *    the error code to return to the client, never <code>null</code>.
    */
   InvalidMessageResult(String errorCode) {
      super(errorCode);
   }

   /**
    * Adds to the response that a paramater that is missing.
    *
    * @param parameter
    *    the missing parameter.
    */
   public void addMissingParameter(String parameter) {
      Element missingParam = getDataElementBuilder().addToDataElement("missing-param");
      missingParam.setAttribute("param", parameter);
   }

   /**
    * Adds to the response a parameter that is missing in an element.
    *
    * @param parameter
    *    the missing parameter.
    *
    * @param element
    *    the element in which the parameter is missing.
    */
   public void addMissingParameter(String parameter, String element) {
      Element missingParam = getDataElementBuilder().addToDataElement("missing-param");
      missingParam.setAttribute("param", parameter);
      missingParam.setAttribute("element", element);
   }

   /**
    * Adds an invalid value for a specified type.
    *
    * @param parameter
    *    the parameter passed by the user.
    *
    * @param type
    *    the type which this parameter should be compliant with.
    *
    * @deprecated since XINS 2.0, use {@link #addInvalidValueForType(String, String, String)}.
    */
   public void addInvalidValueForType(String parameter, String type) {
      Element invalidValue = getDataElementBuilder().addToDataElement("invalid-value-for-type");
      invalidValue.setAttribute("param", parameter);
      invalidValue.setAttribute("type", type);
   }

   /**
    * Adds an invalid value for a specified type.
    *
    * @param parameter
    *    the parameter passed by the user.
    *
    * @param value
    *    the value of the parameter passed by the user.
    *
    * @param type
    *    the type which this parameter should be compliant with.
    */
   public void addInvalidValueForType(String parameter, String value, String type) {
      Element invalidValue = getDataElementBuilder().addToDataElement("invalid-value-for-type");
      invalidValue.setAttribute("param", parameter);
      invalidValue.setAttribute("value", value);
      invalidValue.setAttribute("type", type);
   }

   /**
    * Adds an invalid value for a specified type.
    *
    * @param parameter
    *    the parameter passed by the user.
    *
    * @param value
    *    the value of the parameter passed by the user.
    *
    * @param type
    *    the type which this parameter should be compliant with.
    *
    * @param element
    *    the element in which the parameter is missing.
    */
   public void addInvalidValueForType(String parameter, String value, String type, String element) {
      Element invalidValue = getDataElementBuilder().addToDataElement("invalid-value-for-type");
      invalidValue.setAttribute("param", parameter);
      invalidValue.setAttribute("value", value);
      invalidValue.setAttribute("type", type);
      invalidValue.setAttribute("element", element);
   }

   /**
    * Adds an invalid combination of parameters.
    *
    * @param type
    *    the type of the combination.
    *
    * @param parameters
    *    list of the parameters in the combination passed as a list of
    *    {@link String} objects.
    */
   public void addParamCombo(String type, List<String> parameters) {

      Element paramCombo = getDataElementBuilder().addToDataElement("param-combo");
      paramCombo.setAttribute("type", type);

      // Iterate over all parameters
      for (String parameter : parameters) {
         Element param = getDataElementBuilder().createElement("param");
         param.setAttribute("name", parameter);
         paramCombo.appendChild(param);
      }
   }

   /**
    * Adds an invalid combination of attributes.
    *
    * @param type
    *    the type of the combination.
    *
    * @param attributes
    *    list of the attributes in the combination passed as a list of
    *    {@link String} objects.
    *
    * @param elementName
    *    the name of the element to which these attributes belong.
    *
    * @since XINS 1.4.0
    */
   public void addAttributeCombo(String type, List<String> attributes, String elementName) {

      Element attributeCombo = getDataElementBuilder().addToDataElement("attribute-combo");
      attributeCombo.setAttribute("type", type);

      for (String attr : attributes) {
         Element attribute = getDataElementBuilder().createElement("attribute");
         attribute.setAttribute("name", attr);
         attributeCombo.appendChild(attribute);
      }
   }
}
