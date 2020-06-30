/*
 * $Id: TimeOutException.java,v 1.12 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common;

/**
 * Exception that indicates the total time-out for a service call was reached.
 *
 * @version $Revision: 1.12 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class TimeOutException extends Exception {

   /**
    * Constructs a new <code>TimeOutException</code>.
    */
   public TimeOutException() {
      // empty
   }
}
