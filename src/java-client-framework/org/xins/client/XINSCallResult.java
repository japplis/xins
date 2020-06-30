/*
 * $Id: XINSCallResult.java,v 1.34 2013/01/22 15:13:22 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;

import java.util.List;
import java.util.Map;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.service.CallResult;
import org.xins.common.service.TargetDescriptor;
import org.xins.common.service.CallException;

import org.w3c.dom.Element;

/**
 * Successful result of a call to a XINS service. It may be that some targets
 * failed before a target returned a successful result. All the failures are
 * also stored in this object.
 *
 * <p>When a <code>XINSCallResult</code> instance is created, information must
 * be passed both about the successful call (which target successfully
 * returned a result, how long did it take, what was the result) and about the
 * unsuccessful calls (to which targets were they, what was the error, etc.)
 *
 * <p>While a {@link XINSCallResultData} object describes the result of a call
 * to an single target, a <code>XINSCallResultData</code> also describes all
 * failed calls that happened before.
 *
 * @version $Revision: 1.34 $ $Date: 2013/01/22 15:13:22 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class XINSCallResult extends CallResult implements XINSCallResultData {

   /**
    * The <code>XINSCallResultData</code> object that contains all the
    * information returned from the call. This field can be
    * <code>null</code> for not modified result.
    */
   private final XINSCallResultData _data;

   /**
    * Constructs a new <code>XINSCallResult</code> object.
    *
    * @param request
    *    the original {@link XINSCallRequest} that was used to perform the
    *    call, cannot be <code>null</code>.
    *
    * @param succeededTarget
    *    the {@link TargetDescriptor} that was used to successfully get the
    *    result, cannot be <code>null</code>.
    *
    * @param duration
    *    the call duration, should be &gt;= 0.
    *
    * @param exceptions
    *    the list of {@link org.xins.common.service.CallException}s, collected
    *    in a {@link List} object, or <code>null</code> if the
    *    first call attempt succeeded.
    *
    * @param data
    *    the {@link XINSCallResultData} returned from the call, can be
    *    <code>null</code> for not modified result.
    *
    * @throws IllegalArgumentException
    *    if <code>request         ==   null
    *          || succeededTarget ==   null
    *          || data            ==   null
    *          || duration        &lt; 0</code>.
    */
   XINSCallResult(XINSCallRequest      request,
                  TargetDescriptor     succeededTarget,
                  long                 duration,
                  List<CallException>  exceptions,
                  XINSCallResultData   data)

   throws IllegalArgumentException {

      super(request, succeededTarget, duration, exceptions);
      _data = data;
   }

   /**
    * Returns the error code. If <code>null</code> is returned the call was
    * successful and thus no error code was returned. Otherwise the call was
    * unsuccessful.
    *
    * <p>This method will never return an empty string, so if the result is
    * not <code>null</code>, then it is safe to assume the length of the
    * string is at least 1 character.
    *
    * @return
    *    the returned error code, or <code>null</code> if the call was successful.
    */
   public String getErrorCode() {
      if (_data == null) {
         throw new IllegalStateException("As not modified was returned from the API, no data information is available.");
      }
      return _data.getErrorCode();
   }

   /**
    * Gets all parameters.
    *
    * @return
    *    a {@link Map} with all parameters, or <code>null</code> if there are none.
    */
   public Map<String, String> getParameters() {
      if (_data == null) {
         throw new IllegalStateException("As not modified was returned from the API, no data information is available.");
      }
      return _data.getParameters();
   }

   /**
    * Gets the value of the specified parameter.
    *
    * @param name
    *    the parameter element name, not <code>null</code>.
    *
    * @return
    *    string containing the value of the parameter element,
    *    or <code>null</code> if the parameter has no value.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null</code>.
    */
   public String getParameter(String name)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("name", name);

      Map<String, String> params = getParameters();

      // Short-circuit if there are no parameters at all
      if (params == null) {
         return null;
      }

      // Otherwise return the parameter value
      return params.get(name);
   }

   /**
    * Returns the optional extra data. The data is an XML {@link Element},
    * or <code>null</code>.
    *
    * @return
    *    the extra data as an XML {@link Element}, can be <code>null</code>.
    */
   public Element getDataElement() {
      if (_data == null) {
         throw new IllegalStateException("As not modified was returned from the API, no data information is available.");
      }
      return _data.getDataElement();
   }

   /**
    * Returns whether the result was a not modified result.
    *
    * @return
    *    <code>true</code> if not modified was returned from the server,
    *    <code>false</code> if a result with data was returned from the server.
    * @since XINS 3.1.
    */
   public boolean isNotModified() {
      return _data == null;
   }
}
