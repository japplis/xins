/*
 * $Id: FunctionRequest.java,v 1.28 2012/02/28 18:10:54 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xins.common.MandatoryArgumentChecker;
import org.w3c.dom.Element;

/**
 * Function request. Consists of a function name, a set of parameters and a
 * data section. The function name is mandatory, while there may not be any
 * parameters nor data section.
 *
 * @version $Revision: 1.28 $ $Date: 2012/02/28 18:10:54 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.2.0
 */
public class FunctionRequest {

   /**
    * The name of the function. This field is never <code>null</code>.
    */
   private final String _functionName;

   /**
    * The parameters of the function. This field is never <code>null</code>
    */
   private final Map<String, String> _parameters;

   /**
    * The data section of the function. If there is none, then this field is
    * <code>null</code>.
    */
   private final Element _dataElement;

   /**
    * The backpack is a container object that allow to pass specific information
    * (not parameters) between the calling convention and the function.
    */
   private final Map _backpack;

   /**
    * Creates a new <code>FunctionRequest</code>. The function name must be
    * specified.
    *
    * @param functionName
    *    the name of the function, cannot be <code>null</code>.
    *
    * @param parameters
    *    the parameters of the function requested, cannot be
    *    <code>null</code>.
    *
    * @param dataElement
    *    the data section of the input request, can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>functionName == null</code>.
    */
   public FunctionRequest(String         functionName,
                          Map<String, String> parameters,
                          Element        dataElement)
   throws IllegalArgumentException {
       this(functionName, parameters, dataElement, new HashMap<String, Object>());
   }

   /**
    * Creates a new <code>FunctionRequest</code>. The function name must be
    * specified.
    *
    * @param functionName
    *    the name of the function, cannot be <code>null</code>.
    *
    * @param parameters
    *    the parameters of the function requested, cannot be
    *    <code>null</code>.
    *
    * @param dataElement
    *    the data section of the input request, can be <code>null</code>.
    *
    * @param backpack
    *    the container containing information for the XINS engine or the function.
    *
    * @throws IllegalArgumentException
    *    if <code>functionName == null</code>.
    *
    * @since XINS 2.0
    */
   public FunctionRequest(String         functionName,
                          Map<String, String> parameters,
                          Element        dataElement,
                          Map<String, Object> backpack)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("functionName", functionName);

      // Store the function name (never null)
      _functionName = functionName;

      // Store the parameters, make sure this is never null
      if (parameters == null) {
         _parameters = Collections.EMPTY_MAP;
      } else {
         _parameters = parameters;
      }

      // Store the data section, or null if there is none
      _dataElement = dataElement;

      _backpack = backpack;
   }

   /**
    * Gets the name of the function.
    *
    * @return
    *    the name of the function, never <code>null</code>.
    *
    * @since XINS 2.0
    */
   public String getFunctionName() {
      return _functionName;
   }

   /**
    * Gets the parameters of the function. The returned
    * {@link Map} instance is unmodifiable.
    *
    * @return
    *    the parameters of the function, never <code>null</code>.
    *
    * @since XINS 2.0
    */
   public Map<String, String> getParameters() {
      return _parameters;
   }

   /**
    * Gets the data section of the request.
    *
    * @return
    *    the data section, or <code>null</code> if there is none.
    *
    * @since XINS 2.0
    */
   public Element getDataElement() {
      return _dataElement;
   }

   /**
    * Gets whether the function should be executed or not.
    *
    * @return
    *    <code>true</code> if the function shouldn't be executed, <code>false</code> otherwise.
    *
    * @since XINS 2.0
    */
   public boolean shouldSkipFunctionCall() {
      Boolean shouldSkipFunctionCall = (Boolean) _backpack.get(BackpackConstants.SKIP_FUNCTION_CALL);
      return shouldSkipFunctionCall == null ? false : shouldSkipFunctionCall.booleanValue();
   }

   /**
    * Gets the backpack.
    *
    * @return
    *    the backpack.
    *
    * @since XINS 3.0
    */
   public Map<String, Object> getBackpack() {
      return _backpack;
   }
}
