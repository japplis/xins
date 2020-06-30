/*
 * $Id: BasicPropertyReader.java,v 1.15 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

import java.util.HashMap;
import org.xins.common.MandatoryArgumentChecker;

/**
 * Modifiable implementation of a property reader.
 *
 * @version $Revision: 1.15 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class BasicPropertyReader
extends AbstractPropertyReader {

   /**
    * Constructs a new <code>BasicPropertyReader</code>.
    */
   public BasicPropertyReader() {
      super(new HashMap(89));
   }

   /**
    * Sets the specified property.
    *
    * <p>If <code>value == null</code>, then the property is removed.
    *
    * @param name
    *    the name of the property to set or reset, cannot be
    *    <code>null</code>.
    *
    * @param value
    *    the value for the property, can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null</code>.
    */
   public void set(String name, String value)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("name", name);

      // Remove the current value
      if (value == null) {
         getPropertiesMap().remove(name);

      // Store a new value
      } else {
         getPropertiesMap().put(name, value);
      }
   }

   /**
    * Removes the specified property. If the property is not found, then
    * nothing happens.
    *
    * @param name
    *    the name of the property to remove, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null</code>.
    */
   public void remove(String name) throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("name", name);

      // Remove the property
      getPropertiesMap().remove(name);
   }
}
