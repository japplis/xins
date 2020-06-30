/*
 * $Id: PatternCompileException.java,v 1.14 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types;

/**
 * Exception thrown to indicate a pattern string could not be compiled.
 *
 * @version $Revision: 1.14 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public class PatternCompileException extends RuntimeException {

   /**
    * Creates a new <code>PatternCompileException</code>.
    *
    * @param message
    *    the detail message, or <code>null</code>.
    */
   protected PatternCompileException(String message) {
      super(message);
   }
}
