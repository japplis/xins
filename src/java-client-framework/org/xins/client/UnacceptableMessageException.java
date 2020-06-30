/*
 * $Id: UnacceptableMessageException.java,v 1.9 2011/04/16 15:48:02 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;

import java.util.List;

import org.w3c.dom.Element;
import org.xins.common.xml.DataElementBuilder;

/**
 * Exception that indicates that a request for an API call is considered
 * unacceptable on the application-level. For example, a mandatory input
 * parameter may be missing.
 *
 * @version $Revision: 1.9 $ $Date: 2011/04/16 15:48:02 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.0
 */
public class UnacceptableMessageException extends XINSCallException {

   // TODO: Support XINSCallRequest objects?
   // TODO: Is the name UnacceptableRequestException okay?

   /**
    * The DataElement containing the errors.
    */
   private DataElementBuilder _errors = new DataElementBuilder();

   /**
    * The error message.
    */
   private String _message;

   /**
    * Constructs a new <code>UnacceptableMessageException</code> using the
    * specified <code>AbstractCAPICallRequest</code>.
    *
    * @param request
    *    the {@link AbstractCAPICallRequest} that is considered unacceptable,
    *    cannot be <code>null</code>.
    */
   UnacceptableMessageException(XINSCallRequest request) {
      super("Invalid request", request, null, 0L, null, null);
   }

   /**
    * Constructs a new <code>UnacceptableMessageException</code> using the
    * specified <code>XINSCallResult</code>.
    * This constructor is used by the generated CAPI.
    *
    * @param result
    *    the {@link XINSCallResult} that is considered unacceptable,
    *    cannot be <code>null</code>.
    */
   public UnacceptableMessageException(XINSCallResult result) {
      super("Invalid result", result, null, null);
   }

   /**
    * Returns the message for this exception.
    *
    * @return
    *    the exception message, can be <code>null</code>.
    */
   public String getMessage() {
      if (_message == null) {
         _message = InvalidRequestException.createMessage(_errors.getDataElement());
      }
      return _message;
   }

   /**
    * Adds to the response that a paramater that is missing.
    *
    * @param parameter
    *    the missing parameter.
    */
   public void addMissingParameter(String parameter) {
      Element missingParam = _errors.addToDataElement("missing-param");
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
      Element missingParam = _errors.addToDataElement("missing-param");
      missingParam.setAttribute("param", parameter);
      missingParam.setAttribute("element", element);
   }

   /**
    * Adds an invalid value for a specified type.
    *
    * @param parameter
    *    the name of the parameter passed by the user.
    *
    * @param type
    *    the type which this parameter should be compliant with.
    *
    * @deprecated since XINS 2.0, use {@link #addInvalidValueForType(String, String, String)}.
    */
   public void addInvalidValueForType(String parameter, String type) {
      Element invalidValue = _errors.addToDataElement("invalid-value-for-type");
      invalidValue.setAttribute("param", parameter);
      invalidValue.setAttribute("type", type);
   }

   /**
    * Adds an invalid value for a specified type.
    *
    * @param parameter
    *    the name of the parameter passed by the user.
    *
    * @param value
    *    the value of the parameter passed by the user.
    *
    * @param type
    *    the type which this parameter should be compliant with.
    *
    * @since XINS 2.0
    */
   public void addInvalidValueForType(String parameter, String value, String type) {
      Element invalidValue = _errors.addToDataElement("invalid-value-for-type");
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
    *
    * @since XINS 2.0
    */
   public void addInvalidValueForType(String parameter, String value, String type, String element) {
      Element invalidValue = _errors.addToDataElement("invalid-value-for-type");
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

      Element paramCombo = _errors.addToDataElement("param-combo");
      paramCombo.setAttribute("type", type);

      for (String parameter : parameters) {
         Element param = _errors.createElement("param");
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

      Element attributeCombo = _errors.addToDataElement("attribute-combo");
      attributeCombo.setAttribute("type", type);

      for (String attribute : attributes) {
         Element attributeElement = _errors.createElement("attribute");
         attributeElement.setAttribute("name", attribute);
         attributeCombo.appendChild(attributeElement);
      }
   }
}
