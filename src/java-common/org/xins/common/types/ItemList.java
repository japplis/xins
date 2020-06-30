/*
 * $Id: ItemList.java,v 1.21 2012/03/03 10:41:19 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.xins.common.MandatoryArgumentChecker;

/**
 * Item in a list or a set type.
 *
 * @version $Revision: 1.21 $ $Date: 2012/03/03 10:41:19 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.0.0
 *
 * @see List
 */
public class ItemList {

   /**
    * The list that contains the items. Cannot <code>null</code>.
    */
   private final java.util.List _list;

   /**
    * Indicates whether this list accepts equal objects.
    */
   private final boolean _setType;


   /**
    * Creates a new <code>ItemList</code>.
    * The list will be able to contain several instances of an object.
    */
   public ItemList() {
      this(false);
   }

   /**
    * Creates a new <code>ItemList</code>.
    *
    * @param setType
    *    if <code>true</code> an object can be added only once in the list,
    *    if <code>false</code> an object can be added several times in the list.
    */
   public ItemList(boolean setType) {
      _list = new java.util.ArrayList(10);
      _setType = setType;
   }

   /**
    * Adds a list of items to the list or set. The items are added at the end of the list.
    *
    * @param items
    *    the collection of items to add in the list, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>items == null</code>.
    *
    * @since XINS 2.0.
    */
   public final void add(Collection items) throws IllegalArgumentException {

      MandatoryArgumentChecker.check("items", items);

      for (Object nextItem : items) {

         // A set can not have the same value twice
         if (!_setType || !_list.contains(nextItem)) {
            _list.add(nextItem);
         }
      }
   }

   /**
    * Gets the list of items as a collection.
    * A <code>java.util.Set</code> or a <code>java.util.List</code> is returned
    * depending on the type of this list.
    *
    * @return
    *    a List or a Set containing the items, never <code>null</code>.
    *    the collection returned cannot be modified.
    *
    * @since XINS 2.0.
    */
   public final Collection get() {

      if (_setType) {
         Set set = new HashSet();
         set.addAll(_list);
         return Collections.unmodifiableSet(set);
      } else {
         return Collections.unmodifiableList(_list);
      }
   }

   /**
    * Adds an item to the list. The item is added at the end of the list.
    *
    * @param value
    *    the value of the item to add in the list, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>value == null</code>.
    */
   protected final void addItem(Object value) throws IllegalArgumentException {

      MandatoryArgumentChecker.check("value", value);

      // A set can not have the same value twice
      if (_setType && _list.contains(value)) {
         return;
      }
      _list.add(value);
   }

   /**
    * Gets the item at the specified index as an <code>Object</code>.
    *
    * @param index
    *    the position of the required item, it should be >= 0 and < getSize().
    *
    * @return
    *    the item, not <code>null</code>.
    */
   protected final Object getItem(int index) {
      return _list.get(index);
   }

   /**
    * Gets the number of items included in the list.
    *
    * @return
    *    the size of the list.
    */
   public int getSize() {
      return _list.size();
   }
}
