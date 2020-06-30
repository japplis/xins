/*
 * $Id: Float64.java,v 1.20 2010/11/18 20:35:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types.standard;

import org.xins.common.types.Type;
import org.xins.common.types.TypeValueException;
import org.xins.common.MandatoryArgumentChecker;

/**
 * Standard type <em>_float64</em>.
 *
 * @version $Revision: 1.20 $ $Date: 2010/11/18 20:35:05 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.1.0
 */
public class Float64 extends Type {

   /**
    * The only instance of this class. This field is never <code>null</code>.
    */
   public static final Float64 SINGLETON = new Float64();

   /**
    * The minimum value that this Float64 can have.
    */
   private final double _minimum;

   /**
    * The maximum value that this Float64 can have.
    */
   private final double _maximum;

   /**
    * Constructs a new <code>Float64</code>.
    * This constructor is private, the field {@link #SINGLETON} should be
    * used.
    */
   private Float64() {
      this("_float64", -Double.MAX_VALUE, Double.MAX_VALUE);
   }

   /**
    * Constructs a new <code>Float64</code> object (constructor for
    * subclasses).
    *
    * @param name
    *    the name of this type, cannot be <code>null</code>.
    *
    * @param minimum
    *    the minimum for the value.
    *
    * @param maximum
    *    the maximum for the value.
    */
   protected Float64(String name, double minimum, double maximum) {
      super(name, Double.class);

      _minimum = minimum;
      _maximum = maximum;
   }

   /**
    * Converts the specified non-<code>null</code> string value to an
    * <code>double</code>.
    *
    * @param string
    *    the string to convert, cannot be <code>null</code>.
    *
    * @return
    *    the <code>double</code> value.
    *
    * @throws IllegalArgumentException
    *    if <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.
    */
   public static double fromStringForRequired(String string)
   throws IllegalArgumentException, TypeValueException {
      if (string == null) {
         throw new IllegalArgumentException("string == null");
      } else {
         try {
            return Double.parseDouble(string);
         } catch (NumberFormatException nfe) {
            throw new TypeValueException(SINGLETON, string);
         }
      }
   }

   /**
    * Converts the specified string value to an <code>Double</code> value.
    *
    * @param string
    *    the string to convert, can be <code>null</code>.
    *
    * @return
    *    the {@link Double}, or <code>null</code> if
    *    <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.
    */
   public static Double fromStringForOptional(String string)
   throws TypeValueException {

      if (string == null || string.length() == 0) {
         return null;
      }

      try {
         return Double.valueOf(string);
      } catch (NumberFormatException nfe) {
         throw new TypeValueException(SINGLETON, string);
      }
   }

   /**
    * Converts the specified <code>Double</code> to a string.
    *
    * @param value
    *    the value to convert, can be <code>null</code>.
    *
    * @return
    *    the textual representation of the value, or <code>null</code> if and
    *    only if <code>value == null</code>.
    */
   public static String toString(Double value) {
      if (value == null) {
         return null;
      } else {
         return toString(value.doubleValue());
      }
   }

   /**
    * Converts the specified <code>double</code> to a string.
    *
    * @param value
    *    the value to convert.
    *
    * @return
    *    the textual representation of the value, never <code>null</code>.
    */
   public static String toString(double value) {
      return String.valueOf(value);
   }

   protected boolean isValidValueImpl(String value) {
      try {
         double number = Double.parseDouble(value);
         if (number < _minimum || number > _maximum) {
            return false;
         }
         return true;
      } catch (NumberFormatException nfe) {
         return false;
      }
   }

   protected Object fromStringImpl(String string) throws TypeValueException {
      if (!isValidValue(string)) {
          throw new TypeValueException(this, string);
      }
      return Double.valueOf(string);
   }

   public final String toString(Object value)
   throws IllegalArgumentException, ClassCastException, TypeValueException {
      MandatoryArgumentChecker.check("value", value);
      Double d = (Double) value;
      return d.toString();
   }

   public String getDescription() {
      return "A 64 bits precision float number.";
   }
}
