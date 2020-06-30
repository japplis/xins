/*
 * $Id: UnknownHostCallException.java,v 1.17 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.service;

/**
 * Exception that indicates that a connection to a service could not be
 * established since the indicated host is unknown.
 *
 * @version $Revision: 1.17 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class UnknownHostCallException
extends ConnectionCallException {

   /**
    * Serial version UID. Used for serialization. The assigned value is for
    * compatibility with XINS 1.2.5.
    */
   private static final long serialVersionUID = 1266820641762046595L;

   /**
    * Constructs a new <code>UnknownHostCallException</code>.
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
    * @throws IllegalArgumentException
    *    if <code>request     == null
    *          || target      == null
    *          || duration  &lt; 0</code>.
    *
    */
   public UnknownHostCallException(CallRequest      request,
                                   TargetDescriptor target,
                                   long             duration)
   throws IllegalArgumentException {

      // Call constructor of superclass
      super("Unknown host",
            request, target, duration, null, null);
   }
}
