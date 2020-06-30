/*
 * $Id: Hex.java,v 1.15 2010/11/18 20:35:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types.standard;

import org.xins.common.types.Type;
import org.xins.common.types.TypeValueException;
import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.text.HexConverter;

/**
 * Standard type <em>_hex</em>.
 *
 * @version $Revision: 1.15 $ $Date: 2010/11/18 20:35:05 $
 * @author gveiog
 *
 * @since XINS 1.5.0.
 */
public class Hex extends Type {

   /**
    * The only instance of this class. This field is never <code>null</code>.
    */
   public static final Hex SINGLETON = new Hex();

   /**
    * The minimum number of bytes this Hex can have.
    */
   private final int _minimum;

   /**
    * The maximum number of bytes this Hex can have.
    */
   private final int _maximum;

   /**
    * Constructs a new <code>Hex</code>.
    * This constructor is private, the field {@link #SINGLETON} should be
    * used.
    */
   private Hex() {
      this("_hex", 0, Integer.MAX_VALUE);
   }

   /**
    * Constructs a new <code>Hex</code> object (constructor for
    * subclasses).
    *
    * @param name
    *    the name of this type, cannot be <code>null</code>.
    *
    * @param minimum
    *    the minimum for the value.# minimum number of bytes this Hex can have
    *
    * @param maximum
    *    the maximum for the value.# maximum number of bytes this Hex can have
    */
   protected Hex(String name, int minimum, int maximum) {
      super(name, byte[].class);

      _minimum = minimum;
      _maximum = maximum;
   }

   /**
    * Converts the specified non-<code>null</code> string value to a
    * <code>byte[]</code> value.
    *
    * @param string
    *    the hexadecimal string to convert, cannot be <code>null</code>.
    *
    * @return
    *    the <code>byte[]</code> value.
    *
    * @throws IllegalArgumentException
    *    if <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.If the string does not have a hexadecimal value or have a character
    *    that is not hexadecimal digit.
    */
   public static byte[] fromStringForRequired(String string)
   throws IllegalArgumentException, TypeValueException {
      int index = 0;
      if (string == null) {
         throw new IllegalArgumentException("string == null");
      } else {
         try {

            // this method converts the string to byte and also checks if the string has hex digits
            return HexConverter.parseHexBytes(string,index,string.length());

         } catch (Exception ex){
            throw new TypeValueException(SINGLETON, string);
         }
      }
   }

   /**
    * Converts the specified string value to a <code>byte[]</code> value.
    *
    * @param string
    *    the hexadecimal string to convert, can be <code>null</code>.
    *
    * @return
    *    the byte[], or <code>null</code> if
    *    <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.If the string does not have a hexadecimal value or
    *    have a character that is not hexadecimal digit.
    */
   public static byte[] fromStringForOptional(String string)
   throws TypeValueException {
      int index = 0;
      if (string == null || string.length() == 0) {
         return null;
      }
      try {
         return HexConverter.parseHexBytes(string,index,string.length());

      } catch (Exception e){
         throw new TypeValueException(SINGLETON, string);
      }
   }


   /**
    * Converts the specified <code>byte[]</code> to a hexadecimal string.
    *
    * @param value
    *    the value to convert, can be <code>null</code>.
    *
    * @return
    *    the textual representation of the value, or <code>null</code> if and
    *    only if <code>value == null</code>.
    */
   public static String toString(byte[] value) {
      if (value == null) {
         return null;
      } else {
         return HexConverter.toHexString(value);
      }
   }

   /**
    * Determines if the specified <code>String</code> value is considered
    * valid for this type (implementation method).
    *
    * <p>This method is called from {@link #isValidValue(String)}. When
    * called from that method, it is guaranteed that the argument is not
    * <code>null</code>.
    * # 1.check if the string has hex digits
    * # 2. if the byte[] created from that string has minimum < byte[].length < maximum
    * @param string
    *    the <code>String</code> value that should be checked for validity,
    *    never <code>null</code>.
    *
    * @return
    *    <code>true</code> if and only if the specified <code>String</code>
    *    value is valid, <code>false</code> otherwise.
    */
   protected boolean isValidValueImpl(String string) {
      try {
         for (int i = 0; i < string.length(); i++) {
            if (!HexConverter.isHexDigit(string.charAt(i))) {
               return false;
            }
         }
         byte[] number = HexConverter.parseHexBytes(string,0,string.length());
         if (number.length < _minimum || number.length > _maximum) {
            return false;
         }
         return true;
      } catch (Exception ex) {
         // TODO: Log
         return false;
      }
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
      try {
         return HexConverter.parseHexBytes(string,0,string.length());
      } catch (Exception ex) {
         throw new TypeValueException(SINGLETON, string, ex.getMessage());
      }
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
    *    #Maybe this is redundant because i dont think that there is any possibility to be out of range
    *    but since it exists also to other standard types i left it.
    *
    */
   public final String toString(Object value)
   throws IllegalArgumentException, ClassCastException, TypeValueException {

      // Check preconditions
      MandatoryArgumentChecker.check("value", value);

      // Convert the argument to a byte array (may throw ClassCastException)
      byte[] b = (byte[]) value;

      // Try converting the byte array as a Hex string
      try {
         return HexConverter.toHexString(b);
      } catch (Exception e) {

         throw new TypeValueException(SINGLETON, new String(b), e.getMessage());
      }
   }

   public String getDescription() {
      return "Binary data where each byte is represented by its hexadecimal value.";
   }
}
