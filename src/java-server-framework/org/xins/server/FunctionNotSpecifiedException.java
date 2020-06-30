/*
 * $Id: FunctionNotSpecifiedException.java,v 1.12 2013/01/18 09:06:04 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

/**
 * Exception that indicates that an incoming request does not specify the
 * function to execute.
 *
 * @version $Revision: 1.12 $ $Date: 2013/01/18 09:06:04 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public final class FunctionNotSpecifiedException extends InvalidRequestException {

   /**
    * Constructs a new <code>FunctionNotSpecifiedException</code>.
    */
   public FunctionNotSpecifiedException() {
      super("Function not specified in incoming request.");
   }
}
