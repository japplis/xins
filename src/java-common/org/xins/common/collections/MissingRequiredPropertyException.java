/*
 * $Id: MissingRequiredPropertyException.java,v 1.23 2011/02/12 08:22:46 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

import org.xins.common.text.TextUtils;

/**
 * Exception thrown to indicate a required property has no value set for it.
 *
 * @version $Revision: 1.23 $ $Date: 2011/02/12 08:22:46 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 *
 * @see InvalidPropertyValueException
 */
public final class MissingRequiredPropertyException extends PropertyException {

   /**
    * Detailed description of why this property is required in the current
    * context. Can be <code>null</code>.
    */
   private final String _detail;

   /**
    * Constructs a new <code>MissingRequiredPropertyException</code>, with the
    * specified detail message.
    *
    * @param propertyName
    *    the name of the required property, not <code>null</code>.
    *
    * @param detail
    *    a more detailed description of why this property is required in this
    *    context, can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>propertyName == null</code>.
    *
    * @since XINS 1.3.0
    */
   public MissingRequiredPropertyException(String propertyName, String detail)
   throws IllegalArgumentException {

      // Construct message and call superclass constructor
      super(propertyName, createMessage(propertyName, detail), null);

      // Store data
      _detail       = TextUtils.trim(detail, null);
   }

   /**
    * Constructs a new <code>MissingRequiredPropertyException</code>.
    *
    * @param propertyName
    *    the name of the required property, not <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>propertyName == null</code>.
    */
   public MissingRequiredPropertyException(String propertyName)
   throws IllegalArgumentException {
      this(propertyName, null);
   }

   /**
    * Creates message based on the specified constructor argument.
    *
    * @param propertyName
    *    the name of the property, cannot be <code>null</code>.
    *
    * @param detail
    *    a more detailed description of why this property is required in this
    *    context, can be <code>null</code>.
    *
    * @return
    *    the message, never <code>null</code>.
    */
   private static String createMessage(String propertyName, String detail) {

      // Construct the message
      String message = "No value is set for the required property \"" + propertyName;

      // Append the detail message, if any
      detail = TextUtils.trim(detail, null);
      if (detail != null) {
         message += "\": " + detail;
      } else {
         message += "\".";
      }

      return message;
   }

   /**
    * Returns the detail message.
    *
    * @return
    *    the trimmed detail message, can be <code>null</code>.
    *
    * @since XINS 1.3.0
    */
   public String getDetail() {
      return _detail;
   }
}
