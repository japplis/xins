/*
 * $Id: InvalidPropertyValueException.java,v 1.26 2011/02/12 08:22:46 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

/**
 * Exception thrown to indicate the property of a value is invalid.
 *
 * @version $Revision: 1.26 $ $Date: 2011/02/12 08:22:46 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 *
 * @see MissingRequiredPropertyException
 */
public final class InvalidPropertyValueException extends PropertyException {

   /**
    * The (invalid) value of the property. Cannot be <code>null</code>.
    */
   private final String _propertyValue;

   /**
    * The detailed reason. Can be <code>null</code>.
    */
   private final String _reason;

   /**
    * Constructs a new <code>InvalidPropertyValueException</code>.
    *
    * @param propertyName
    *    the name of the property, cannot be <code>null</code>.
    *
    * @param propertyValue
    *    the (invalid) value set for the property, cannot be
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>propertyName == null || propertyValue == null</code>.
    */
   public InvalidPropertyValueException(String propertyName,
                                        String propertyValue)
   throws IllegalArgumentException {

      this(propertyName, propertyValue, null);
   }

   /**
    * Constructs a new <code>InvalidPropertyValueException</code> with the
    * specified reason.
    *
    * @param propertyName
    *    the name of the property, cannot be <code>null</code>.
    *
    * @param propertyValue
    *    the (invalid) value set for the property, cannot be
    *    <code>null</code>.
    *
    * @param reason
    *    additional description of the problem, or <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>propertyName == null || propertyValue == null</code>.
    */
   public InvalidPropertyValueException(String propertyName,
                                        String propertyValue,
                                        String reason)
   throws IllegalArgumentException {

      // Construct message and call superclass constructor
      super(propertyName, createMessage(propertyName, propertyValue, reason), null);

      // Store data
      _propertyValue = propertyValue;
      _reason        = reason;
   }

   /**
    * Creates message based on the specified constructor arguments.
    *
    * @param propertyName
    *    the name of the property, cannot be <code>null</code>.
    *
    * @param propertyValue
    *    the (invalid) value set for the property, cannot be
    *    <code>null</code>.
    *
    * @param reason
    *    additional description of the problem, or <code>null</code>.
    *
    * @return
    *    the message, never <code>null</code>.
    */
   private static String createMessage(String propertyName, String propertyValue, String reason) {

      // Construct the message
      String message = "The value \"" + propertyValue +
            "\" is invalid for property \"" + propertyName;
      if (reason == null) {
         message += "\".";
      } else {
         message += "\": " + reason;
      }

      return message;
   }

   /**
    * Returns the (invalid) value of the property.
    *
    * @return
    *    the value of the property, never <code>null</code>.
    */
   public String getPropertyValue() {
      return _propertyValue;
   }

   /**
    * Returns the description of the reason.
    *
    * @return
    *    the reason, or <code>null</code>.
    */
   public String getReason() {
      return _reason;
   }
}
