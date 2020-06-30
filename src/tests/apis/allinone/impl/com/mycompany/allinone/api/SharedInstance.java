/*
 * $Id: SharedInstance.java,v 1.7 2010/10/25 20:36:52 agoubard Exp $
 */
package com.mycompany.allinone.api;

import java.util.Map;
import java.util.Properties;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.manageable.InitializationException;
import org.xins.common.manageable.Manageable;

/**
 * Common object used by the API to shared properties.
 *
 * @version $Revision: 1.7 $ $Date: 2010/10/25 20:36:52 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class SharedInstance extends Manageable {

   /**
    * Constructs a new <code>SharedInstance</code> instance.
    *
    * @param api
    *    the API to which this function belongs, guaranteed to be not
    *    <code>null</code>.
    */
   public SharedInstance(APIImpl api) {
   }

   /**
    * The collection used to store the properties.
    */
   private Properties _sharedProperties;

   @Override
   protected void initImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException {
      _sharedProperties = new Properties();
   }

   /**
    * Stores a property for the API.
    *
    * @param key
    *    the key of the property, cannot be <code>null</code>.
    * @param value
    *    the value of the property, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if (<code>key == null</code> || <code>value == null</code>).
    */
   public final void put(String key, String value) {

      // Check preconditions
      MandatoryArgumentChecker.check("key", key, "value", value);

      _sharedProperties.put(key, value);
   }

   /**
    * Gets a property from the API.
    *
    * @return
    *    returns the value of the property or <code>null</code> if the value was
    *    not stored.
    */
   public final String get(String key) {
      return _sharedProperties.getProperty(key);
   }
}
