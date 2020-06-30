/*
 * $Id: AbstractCAPIErrorCodeException.java,v 1.14 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;

import org.xins.common.service.TargetDescriptor;

/**
 * Abstract base class for generated CAPI exceptions that map to an
 * API-specific error code.
 *
 * <p>This class should not be derived from directly. Only generated CAPI
 * classes should derive from this class.
 *
 * @version $Revision: 1.14 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public abstract class AbstractCAPIErrorCodeException
extends UnsuccessfulXINSCallException {

   /**
    * Constructs a new <code>AbstractCAPIErrorCodeException</code>.
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
    * @throws IllegalArgumentException
    *    if <code>request     == null
    *          || target      == null
    *          || duration  &lt; 0
    *          || resultData  == null
    *          || resultData.{@link XINSCallResult#getErrorCode() getErrorCode()} == null</code>.
    */
   protected AbstractCAPIErrorCodeException(XINSCallRequest    request,
                                            TargetDescriptor   target,
                                            long               duration,
                                            XINSCallResultData resultData)
   throws IllegalArgumentException {
      super(request, target, duration, resultData, null);
   }
}
