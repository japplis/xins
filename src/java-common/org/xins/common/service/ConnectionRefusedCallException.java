/*
 * $Id: ConnectionRefusedCallException.java,v 1.19 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.service;

/**
 * Exception that indicates that a connection to a service could not be
 * established since it was refused.
 *
 * @version $Revision: 1.19 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class ConnectionRefusedCallException
extends ConnectionCallException {

   /**
    * Serial version UID. Used for serialization. The assigned value is for
    * compatibility with XINS 1.2.5.
    */
   private static final long serialVersionUID = -7764503252933271908L;

   /**
    * Constructs a new <code>ConnectionRefusedCallException</code>.
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
   public ConnectionRefusedCallException(CallRequest      request,
                                         TargetDescriptor target,
                                         long             duration)
   throws IllegalArgumentException {

      // Trace and then call constructor of superclass
      super("Connection refused",
            request, target, duration, null, null);
   }
}
