/*
 * $Id: DeinitializationException.java,v 1.25 2011/03/19 09:11:18 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.manageable;

import org.xins.common.MandatoryArgumentChecker;

/**
 * Exception thrown when the deinitialization of a <code>Manageable</code>
 * object caused an exception to be thrown.
 *
 * @version $Revision: 1.25 $ $Date: 2011/03/19 09:11:18 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class DeinitializationException extends Exception {

   /**
    * Constructs a new <code>DeinitializationException</code> with the
    * specified cause exception.
    *
    * @param cause
    *    the cause exception, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>cause == null</code>.
    */
   DeinitializationException(Throwable cause)
   throws IllegalArgumentException {
      super(createMessage(cause), cause);
   }

   /**
    * Creates a message based on the specified constructor argument.
    *
    * @param cause
    *    the cause exception, cannot be <code>null</code>.
    *
    * @return
    *    the message, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>cause == null</code>.
    */
   private static String createMessage(Throwable cause)
   throws IllegalArgumentException {
      MandatoryArgumentChecker.check("cause", cause);

      String exceptionMessage = cause.getMessage();

      String message = "Caught " + cause.getClass().getName();
      if (exceptionMessage != null && exceptionMessage.length() > 0) {
         message += ". Message: \"" + exceptionMessage + "\".";
      } else {
         message += '.';
      }

      return message;
   }
}
