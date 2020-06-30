/*
 * $Id: CallContext.java,v 1.132 2012/02/28 18:10:54 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.util.Map;
import org.xins.common.MandatoryArgumentChecker;
import org.w3c.dom.Element;

/**
 * Context for a function call. Objects of this kind are passed with a
 * function call.
 *
 * @version $Revision: 1.132 $ $Date: 2012/02/28 18:10:54 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class CallContext {

   /**
    * The parameters of the request.
    */
   private final Map<String, String> _parameters;

   /**
    * The data section of the request.
    */
   private final Element _dataElement;

   /**
    * The call result builder. Cannot be <code>null</code>.
    */
   private final FunctionResult _builder;

   /**
    * The call ID, unique in the context of the pertaining function.
    */
   private final int _callID;

   /**
    * The backpack.
    */
   private final Map<String, Object> _backpack;

   /**
    * Constructs a new <code>CallContext</code> and configures it for the
    * specified request.
    *
    * @param functionRequest
    *    the request, never <code>null</code>.
    *
    * @param function
    *    the concerning function, cannot be <code>null</code>.
    *
    * @param callID
    *    the assigned call ID.
    *
    * @throws IllegalArgumentException
    *    if <code>functionRequest == null || function == null</code>.
    */
   CallContext(FunctionRequest functionRequest,
               Function        function,
               int             callID)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("functionRequest",  functionRequest,
                                     "function",         function);

      // Initialize fields
      _parameters   = functionRequest.getParameters();
      _dataElement  = functionRequest.getDataElement();
      _callID       = callID;
      _backpack     = functionRequest.getBackpack();
      _builder      = new FunctionResult();
   }

   /**
    * Returns the stored return code.
    *
    * @return
    *    the return code, can be <code>null</code>.
    */
   final String getErrorCode() {
      return _builder.getErrorCode();
   }

   /**
    * Returns the value of a parameter with the specificied name. Note that
    * reserved parameters, i.e. those starting with an underscore
    * (<code>'_'</code>) cannot be retrieved.
    *
    * @param name
    *    the name of the parameter, not <code>null</code>.
    *
    * @return
    *    the value of the parameter, or <code>null</code> if the parameter is
    *    not set, never an empty string (<code>""</code>) because it will be
    *    returned as being <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null</code>.
    */
   public String getParameter(String name)
   throws IllegalArgumentException {

      // Check arguments
      if (name == null) {
         throw new IllegalArgumentException("name == null");
      }

      // XXX: In a later version, support a parameter named 'function'

      if (_parameters != null && name.length() > 0 && !"function".equals(name) && name.charAt(0) != '_') {
         String value = _parameters.get(name);
         return "".equals(value) ? null : value;
      }
      return null;
   }

   /**
    * Returns the data section of the request, if any.
    *
    * @return
    *    the element representing the data section or <code>null</code> if the
    *    function does not define a data section or if the data section sent is
    *    empty.
    */
   public Element getDataElement() {
      return _dataElement;
   }

   /**
    * Returns the assigned call ID. This ID is unique within the context of
    * the pertaining function. If no call ID is assigned, then <code>-1</code>
    * is returned.
    *
    * @return
    *    the assigned call ID for the function, or <code>-1</code> if none is
    *    assigned.
    */
   public int getCallID() {
      return _callID;
   }

   /**
    * Returns the backpack of this request.
    *
    * @return
    *    the backpack.
    */
   public Map<String, Object> getBackpack() {
      return _backpack;
   }
}
