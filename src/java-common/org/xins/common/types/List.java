/*
 * $Id: List.java,v 1.30 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types;

import java.util.StringTokenizer;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.Utils;
import org.xins.common.text.FormatException;
import org.xins.common.text.URLEncoding;
import org.xins.common.types.standard.Text;

/**
 * Abstract base class for list types.
 *
 * @version $Revision: 1.30 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.0.0
 */
public abstract class List extends Type {

   /**
    * The type for the values. Cannot be <code>null</code>.
    */
   private final Type _itemType;

   /**
    * Constructs a new <code>List</code> object (constructor for
    * subclasses).
    *
    * @param name
    *    the name of this type, cannot be <code>null</code>.
    *
    * @param itemType
    *    the type for the values, or <code>null</code> if {@link Text}
    *    should be assumed.
    */
   protected List(String name, Type itemType) {
      super(name, ItemList.class);

      _itemType = itemType == null ? Text.SINGLETON : itemType;
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
   protected final boolean isValidValueImpl(String string) {

      if (string == null) {
         return false;
      }

      // Separate the string by ampersands
      StringTokenizer tokenizer = new StringTokenizer(string, "&");
      while (tokenizer.hasMoreTokens()) {
         String token = tokenizer.nextToken();
         try {
            String item = URLEncoding.decode(token);
            if (!_itemType.isValidValue(item)) {
               return false;
            }
         } catch (FormatException fe) {
            return false;
         } catch (IllegalArgumentException iae) {
            return false;
         }
      }
      return true;
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
   protected final Object fromStringImpl(String string)
   throws TypeValueException {

      // Construct a ItemList to store the values in
      ItemList list = createList();

      // Separate the string by ampersands
      StringTokenizer tokenizer = new StringTokenizer(string, "&");
      while (tokenizer.hasMoreTokens()) {
         String token = tokenizer.nextToken();
         try {
            String itemString = URLEncoding.decode(token);
            Object item = _itemType.fromString(itemString);
            list.addItem(item);
         } catch (FormatException fe) {
            throw new TypeValueException(this, string, fe.getReason());
         } catch (IllegalArgumentException iae) {
            throw Utils.logProgrammingError(iae);
         }
      }

      return list;
   }

   /**
    * Creates a new <code>ItemList</code>.
    *
    * @return
    *    the new list created, never <code>null</code>.
    */
   public abstract ItemList createList();

   /**
    * Converts the specified <code>ItemList</code> to a string.
    *
    * @param value
    *    the value to convert, can be <code>null</code>.
    *
    * @return
    *    the textual representation of the value, or <code>null</code> if and
    *    only if <code>value == null</code>.
    */
   public String toString(ItemList value) {

      // Short-circuit if the argument is null
      if (value == null) {
         return null;
      }

      // Use a buffer to create the string
      StringBuffer buffer = new StringBuffer(255);

      // Iterate over the list
      int listSize = value.getSize();
      for (int i=0; i < listSize; i++) {
         if (i != 0) {
            buffer.append('&');
         }

         Object nextItem = value.getItem(i);
         String stringItem;
         try {
            stringItem = _itemType.toString(nextItem);
         } catch (Exception ex) {

            // Should never happens as only add() is able to add items in the list.
            throw new IllegalArgumentException("Incorrect value for type: " + nextItem);
         }
         buffer.append(URLEncoding.encode(stringItem));
      }

      return buffer.toString();
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
   public final String toString(Object value)
   throws IllegalArgumentException, ClassCastException, TypeValueException {

      // Check preconditions
      MandatoryArgumentChecker.check("value", value);

      // The argument must be a ItemList
      return toString((ItemList) value);
   }
}
