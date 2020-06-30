/*
 * $Id: EntityNotFoundException.java,v 1.13 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.spec;

/**
 * Thrown when the required entity cannot be found in the API.
 *
 * @version $Revision: 1.13 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.3.0
 */
public class EntityNotFoundException extends Exception {

   /**
    * Creates a new <code>InvalidSpecificationException</code> with the reason
    * of the problem.
    *
    * @param message
    *    the reason why this exception has been thrown, can be <code>null</code>.
    */
   EntityNotFoundException(String message) {
      super(message);
   }
}
