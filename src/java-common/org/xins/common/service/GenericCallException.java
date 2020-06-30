/*
 * $Id: GenericCallException.java,v 1.19 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.service;

/**
 * Generic call exception, not related to any particular kind of service.
 *
 * @version $Revision: 1.19 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public abstract class GenericCallException extends CallException {

   /**
    * Constructs a new <code>GenericCallException</code> based on a short
    * reason, the original request, target called, call duration, detail
    * message and cause exception.
    *
    * @param shortReason
    *    the short reason, cannot be <code>null</code>.
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
    * @param detail
    *    a detailed description of the problem, can be <code>null</code> if
    *    there is no more detail.
    *
    * @param cause
    *    the cause exception, can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>shortReason == null
    *          || request == null
    *          || target == null
    *          || duration &lt; 0</code>.
    */
   GenericCallException(String           shortReason,
                        CallRequest      request,
                        TargetDescriptor target,
                        long             duration,
                        String           detail,
                        Throwable        cause)
   throws IllegalArgumentException {

      // Trace and then call constructor of superclass
      super(shortReason, request, target, duration, detail, cause);
   }
}
