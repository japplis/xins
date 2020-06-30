/*
 * $Id: StandardErrorCodeException.java,v 1.10 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;

import org.xins.common.service.TargetDescriptor;

/**
 * Abstract base class for exceptions that indicate that a standard error code
 * was returned from the server-side. Standard error codes all start with an
 * underscore, e.g. <em>_InternalError</em>.
 *
 * @version $Revision: 1.10 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.2.0
 */
public abstract class StandardErrorCodeException
extends UnsuccessfulXINSCallException {

   /**
    * Constructs a new <code>StandardErrorCodeException</code>.
    *
    * @param request
    *    the original request, cannot be <code>null</code>.
    *
    * @param target
    *    descriptor for the target that was attempted to be called, cannot be
    *    <code>null</code>.
    *
    * @param duration
    *    the call duration in milliseconds, must be &gt;= 0.
    *
    * @param resultData
    *    the result data, cannot be <code>null</code>.
    *
    * @param detail
    *    detail message, or <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>request     == null
    *          || target      == null
    *          || duration  &lt; 0
    *          || resultData  == null
    *          || resultData.{@link XINSCallResult#getErrorCode()
    *             getErrorCode()} == null</code>.
    */
   StandardErrorCodeException(XINSCallRequest    request,
                              TargetDescriptor   target,
                              long               duration,
                              XINSCallResultData resultData,
                              String             detail)
   throws IllegalArgumentException {
      super(request, target, duration, resultData, detail);
   }
}
