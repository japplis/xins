/*
 * $Id: PropertyException.java,v 1.1 2011/02/12 08:22:46 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

import org.xins.common.MandatoryArgumentChecker;

/**
 * Exception thrown to indicate a problem with a property.
 *
 * @version $Revision: 1.1 $ $Date: 2011/02/12 08:22:46 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 3.0
 */
public abstract class PropertyException extends Exception {

   /**
    * The name of the property. Cannot be <code>null</code>.
    */
   private final String _propertyName;

   /**
    * Constructs a new <code>PropertyException</code> with the specified
    * property name.
    *
    * @param propertyName
    *    the property name, cannot be <code>null</code>.
    *
    * @param message
    *    the detail message to be returned by {@link #getMessage()},
    *    can be <code>null</code>.
    *
    * @param cause
    *    the cause exception, to be returned by {@link #getCause()},
    *    can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null</code>.
    */
   PropertyException(String propertyName, String message, Throwable cause) {
      super(message);
      MandatoryArgumentChecker.check("propertyName", propertyName);
      _propertyName = propertyName;

      if (cause != null) {
         initCause(cause);
      }
   }

   /**
    * Returns the name of the property.
    *
    * @return
    *    the name of the property, never <code>null</code>.
    */
   public final String getPropertyName() {
      return _propertyName;
   }
}
