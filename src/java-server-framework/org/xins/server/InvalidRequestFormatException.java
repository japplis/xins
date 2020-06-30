/*
 * $Id: InvalidRequestFormatException.java,v 1.1 2013/01/18 10:41:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

/**
 * Exception that indicates that an incoming request is considered invalid
 * due to the invalid input format.
 *
 * @version $Revision: 1.1 $ $Date: 2013/01/18 10:41:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class InvalidRequestFormatException extends InvalidRequestException {

   /**
    * Constructs a new <code>InvalidRequestFormatException</code> with the specified
    * detail message and cause exception.
    *
    * @param message
    *    the message, can be <code>null</code>.
    *
    * @param cause
    *    the cause exception, can be <code>null</code>.
    */
   public InvalidRequestFormatException(String message, Throwable cause) {
      super(message, cause);
   }

   /**
    * Constructs a new <code>InvalidRequestFormatException</code> with the specified
    * detail message.
    *
    * @param message
    *    the message, can be <code>null</code>.
    */
   public InvalidRequestFormatException(String message) {
      this(message, null);
   }
}
