/*
 * $Id: InvalidSpecificationException.java,v 1.15 2011/03/19 09:11:18 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.spec;

/**
 * Thrown when the specification of the API is incorrect or cannot be found.
 *
 * @version $Revision: 1.15 $ $Date: 2011/03/19 09:11:18 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.3.0
 */
public class InvalidSpecificationException extends Exception {

   /**
    * Creates a new <code>InvalidSpecificationException</code> with the reason
    * of the problem.
    *
    * @param message
    *    the reason why this exception has been thrown, can be <code>null</code>.
    */
   InvalidSpecificationException(String message) {
      this(message, null);
   }

   /**
    * Creates a new <code>InvalidSpecificationException</code> with the reason
    * of the problem.
    *
    * @param message
    *    the reason why this exception has been thrown, can be <code>null</code>.
    *
    * @param cause
    *    the cause of the exception, can be <code>null</code>.
    */
   InvalidSpecificationException(String message, Throwable cause) {
      super(message, cause);
   }

}
