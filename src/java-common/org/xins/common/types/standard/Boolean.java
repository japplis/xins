/*
 * $Id: Boolean.java,v 1.23 2010/11/18 20:35:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types.standard;

import org.xins.common.types.Type;
import org.xins.common.types.TypeValueException;
import org.xins.common.MandatoryArgumentChecker;

/**
 * Standard type <em>_boolean</em>.
 *
 * @version $Revision: 1.23 $ $Date: 2010/11/18 20:35:05 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class Boolean extends Type {

   /**
    * The only instance of this class. This field is never <code>null</code>.
    */
   public static final Boolean SINGLETON = new org.xins.common.types.standard.Boolean();

   /**
    * Constructs a new <code>Boolean</code>.
    * This constructor is private, the field {@link #SINGLETON} should be
    * used.
    */
   private Boolean() {
      super("_boolean", java.lang.Boolean.class);
   }

   /**
    * Converts the specified non-<code>null</code> string value to a
    * <code>boolean</code>.
    *
    * @param string
    *    the string to convert, cannot be <code>null</code>.
    *
    * @return
    *    the <code>boolean</code> value.
    *
    * @throws IllegalArgumentException
    *    if <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.
    */
   public static boolean fromStringForRequired(String string)
   throws IllegalArgumentException, TypeValueException {
      if ("true".equals(string)) {
         return true;
      } else if ("false".equals(string)) {
         return false;
      } else if (string == null) {
         throw new IllegalArgumentException("string == null");
      } else {
         throw new TypeValueException(SINGLETON, string);
      }
   }

   /**
    * Converts the specified string value to a <code>java.lang.Boolean</code>
    * value.
    *
    * @param string
    *    the string to convert, can be <code>null</code>.
    *
    * @return
    *    the {@link java.lang.Boolean}, or <code>null</code> if
    *    <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.
    */
   public static java.lang.Boolean fromStringForOptional(String string)
   throws TypeValueException {
      if ("true".equals(string)) {
         return java.lang.Boolean.TRUE;
      } else if ("false".equals(string)) {
         return java.lang.Boolean.FALSE;
      } else if (string == null || string.length() == 0) {
         return null;
      } else {
         throw new TypeValueException(SINGLETON, string);
      }
   }

   /**
    * Converts the specified <code>Boolean</code> to a string.
    *
    * @param value
    *    the value to convert, can be <code>null</code>.
    *
    * @return
    *    the textual representation of the value, or <code>null</code> if and
    *    only if <code>value == null</code>.
    */
   public static String toString(java.lang.Boolean value) {
      if (value == null) {
         return null;
      } else {
         return toString(value.booleanValue());
      }
   }

   /**
    * Converts the specified <code>boolean</code> to a string.
    *
    * @param value
    *    the value to convert.
    *
    * @return
    *    the textual representation of the value, never <code>null</code>.
    */
   public static String toString(boolean value) {
      return value ? "true" : "false";
   }

   /**
    * Determines if the specified <code>String</code> value is considered
    * valid for this type (implementation method).
    *
    * <p>This method is called from {@link #isValidValue(String)}. When
    * called from that method, it is guaranteed that the argument is not
    * <code>null</code>.
    *
    * @param string
    *    the <code>String</code> value that should be checked for validity,
    *    never <code>null</code>.
    *
    * @return
    *    <code>true</code> if and only if the specified <code>String</code>
    *    value is valid, <code>false</code> otherwise.
    */
   protected boolean isValidValueImpl(String string) {
      return "true".equals(string) || "false".equals(string);
   }

   /**
    * Converts from a <code>String</code> to an instance of the value class
    * for this type (implementation method).
    *
    * <p>This method is not required to check the validity of the specified
    * value (since {@link #isValidValueImpl(String)} should have been called
    * before) but if it does, then it may throw a {@link TypeValueException}.
    *
    * @param string
    *    the string to convert to an instance of the value class, guaranteed
    *    to be not <code>null</code> and guaranteed to have been passed to
    *    {@link #isValidValueImpl(String)} without getting an exception.
    *
    * @return
    *    an instance of the value class, cannot be <code>null</code>.
    *
    * @throws TypeValueException
    *    if <code>string</code> is considered to be an invalid value for this
    *    type.
    */
   protected Object fromStringImpl(String string) throws TypeValueException {
      if (!isValidValue(string)) {
          throw new TypeValueException(this, string);
      }
      return "true".equals(string) ? java.lang.Boolean.TRUE
                                   : java.lang.Boolean.FALSE;
   }

   /**
    * Generates a string representation of the specified value for this type.
    * The specified value must be an instance of the value class for this type
    * (see {@link #getValueClass()}). Also, it may have to fall within a
    * certain range of valid values, depending on the type.
    *
    * @param value
    *    the value, cannot be <code>null</code>.
    *
    * @return
    *    the string representation of the specified value for this type,
    *    cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>value == null</code>.
    *
    * @throws ClassCastException
    *    if <code>getValueClass().isInstance(value) == false</code>.
    *
    * @throws TypeValueException
    *    if the specified value is not in the allowed range.
    */
   public String toString(Object value)
   throws IllegalArgumentException, ClassCastException, TypeValueException {
      MandatoryArgumentChecker.check("value", value);
      java.lang.Boolean b = (java.lang.Boolean) value;
      return b.toString();
   }
   
   public String getDescription() {
      return "A boolean, either 'true' or 'false'.";
   }
}
