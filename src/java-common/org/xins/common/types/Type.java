/*
 * $Id: Type.java,v 1.35 2010/11/18 20:35:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.Utils;

/**
 * Value type. This is an abstract base class for type classes. Each type
 * defines a name and it defines what values are considered valid and what
 * values are considered invalid.
 *
 * @version $Revision: 1.35 $ $Date: 2010/11/18 20:35:05 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public abstract class Type {

   /**
    * The name of this type. Never <code>null</code>.
    */
   private final String _name;

   /**
    * The class for all values. Never <code>null</code>.
    */
   private final Class _valueClass;

   /**
    * Creates a new <code>Type</code> instance. Both the name of the type and
    * the value class must be specified. The value class in the class (or
    * interface) that values for this type should be instances of. If
    * <code>null</code> is specified as the value class, then that is the same
    * as specifying <code>Object.class</code> as the value class.
    *
    * @param name
    *    the name of the type, not <code>null</code>.
    *
    * @param valueClass
    *    the class or interface that values should be instances of, or
    *    <code>null</code> if any class is valid.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null</code>.
    */
   protected Type(String name, Class valueClass)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("name", name);

      // Store the arguments
      _name       = name;
      _valueClass = valueClass == null ? Object.class : valueClass;
   }

   /**
    * Retrieves the name of this type.
    *
    * @return
    *    the name of this type, never <code>null</code>.
    */
   public final String getName() {
      return _name;
   }

   /**
    * Retrieves the description of this type.
    *
    * @return
    *    the description of this type, never <code>null</code>.
    * 
    * @since XINS 2.1.
    */
   public String getDescription() {
      return _name + " type.";
   }

   /**
    * Retrieves the value class. All values for this type are instances of
    * this class.
    *
    * @return
    *    the class values should be instances of, never <code>null</code>.
    */
   public final Class getValueClass() {
      return _valueClass;
   }

   /**
    * Checks if the specified value is valid for this type and throws an
    * exception if not.
    *
    * <p />Note that <code>null</code> values are <em>always</em> considered
    * to be valid.
    *
    * @param value
    *    the value that should be checked for validity, can be
    *    <code>null</code>.
    *
    * @throws TypeValueException
    *    if the specified value is invalid for this type.
    */
   public final void checkValue(String value)
   throws TypeValueException {
      if (value == null) {
         return;
      } else if (! isValidValueImpl(value)) {
         throw new TypeValueException(this, value);
      }
   }

   /**
    * Determines if the specified <code>String</code> value is considered
    * valid for this type (wrapper method).
    *
    * <p />This method first checks if <code>string == null</code> and if it
    * is not, then it returns the result of a call to
    * {@link #isValidValueImpl(String)}. Note that <code>null</code> values
    * are <em>always</em> considered to be valid.
    *
    * @param string
    *    the value that should be checked for validity, can be
    *    <code>null</code>.
    *
    * @return
    *    <code>true</code> if and only if the specified value is valid,
    *    <code>false</code> otherwise.
    */
   public final boolean isValidValue(String string) {
      if (string == null || string.length() == 0) {
         return true;
      } else {
         return isValidValueImpl(string);
      }
   }

   /**
    * Determines if the specified <code>String</code> value is considered
    * valid for this type (implementation method).
    *
    * <p>This method is called from {@link #isValidValue(String)}. When
    * called from that method, it is guaranteed that the argument is not
    * <code>null</code>.
    *
    * <p />The implementation of this method in class {@link Type} returns
    * <code>true</code>.
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
      return true;
   }

   /**
    * Converts from a <code>String</code> to an instance of the value class
    * for this type (wrapper method).
    *
    * <p />This method returns <code>null</code> if <code>string ==
    * null</code>. Otherwise it first calls {@link #isValidValueImpl(String)}
    * to check if the value is in principle valid. If it is, then
    * {@link #fromStringImpl(String)} is called. If the result of that call is
    * <em>not</em> an instance of the value class, then an
    * {@link Error} is thrown. Notice that this error is also thrown
    * if {@link #fromStringImpl(String)} returns <code>null</code>.
    *
    * @param string
    *    the string to convert to an instance of the value class, can be
    *    <code>null</code>.
    *
    * @return
    *    an instance of the value class, will be <code>null</code> if and only
    *    if <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.
    */
   public final Object fromString(String string)
   throws TypeValueException {

      if (string == null) {
         return null;
      }

      Object value = fromStringImpl(string);

      // TODO: Create a unit test to check that a null returned from
      //       fromStringImpl(String) is actually causing a
      //       ProgrammingException to be thrown

      if (!_valueClass.isInstance(value)) {
         String detail = "The value returned is an instance of class " + value.getClass().getName()
               + " instead of an instance of " + _valueClass.getName() + '.';
         throw Utils.logProgrammingError(detail);
      }

      return value;
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
   protected abstract Object fromStringImpl(String string)
   throws TypeValueException;

   /**
    * Generates a string representation of the specified value for this type.
    * The specified value must be an instance of the value class for this type
    * (see {@link #getValueClass()}). Also, it may have to fall within a
    * certain range of valid values, depending on the type.
    *
    * <p>The default implementation of this method in class {@link Type} does
    * the following:
    *
    * <ul>
    *    <li>if <code>value == null</code> then it throws an
    *        {@link IllegalArgumentException};
    *    <li>if <code>getValueClass().isInstance(value) == false</code> then
    *        it throws a {@link ClassCastException};
    *    <li>otherwise it returns
    *        <code>value.</code>{@link Object#toString()}.
    * </ul>
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

      // Check preconditions
      MandatoryArgumentChecker.check("value", value);

      if (!getValueClass().isInstance(value)) {
         throw new ClassCastException();
      }
      return value.toString();
   }

   /**
    * Returns a textual presentation of this object. The implementation of
    * this method just returns the name of this type.
    *
    * @return
    *    the textual presentation, never <code>null</code>.
    */
   public final String toString() {
      return _name;
   }
}
