/*
 * $Id: InvalidResponseException.java,v 1.17 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

/**
 * Exception that indicates that a response is considered invalid.
 *
 * @version $Revision: 1.17 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class InvalidResponseException
extends RuntimeException {

   /**
    * Constructs a new <code>InvalidResponseException</code> with the
    * specified detail message.
    *
    * @param message
    *    the detail message, can be <code>null</code>.
    */
   InvalidResponseException(String message) {
      super(message);
   }
}
