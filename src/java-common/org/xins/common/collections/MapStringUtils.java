/*
 * $Id: MapStringUtils.java,v 1.3 2012/03/15 21:07:39 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

import java.io.InputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.xins.common.MandatoryArgumentChecker;

import org.xins.common.text.TextUtils;
import org.xins.common.text.URLEncoding;

/**
 * Utility functions for dealing with <code>Map&lt;String, String&gt;</code> objects.
 *
 * @version $Revision: 1.3 $ $Date: 2012/03/15 21:07:39 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0.0
 */
public final class MapStringUtils {

   /**
    * Constructs a new <code>MapStringUtils</code> object. This
    * constructor is marked as <code>private</code>, since no objects of this
    * class should be constructed.
    */
   private MapStringUtils() {
      // empty
   }

   /**
    * Gets the property with the specified name and converts it to a
    * <code>boolean</code>.
    *
    * @param properties
    *    the set of properties to read from, cannot be <code>null</code>.
    *
    * @param propertyName
    *    the name of the property to read, cannot be <code>null</code>.
    *
    * @param fallbackDefault
    *    the fallback default value, returned if the value of the property is
    *    either <code>null</code> or <code>""</code> (an empty string).
    *
    * @return
    *    the value of the property.
    *
    * @throws IllegalArgumentException
    *    if <code>properties == null || propertyName == null</code>.
    *
    * @throws InvalidPropertyValueException
    *    if the value of the property is neither <code>null</code> nor
    *    <code>""</code> (an empty string), nor <code>"true"</code> nor
    *    <code>"false"</code>.
    */
   public static boolean getBooleanProperty(Map<String, String> properties, String propertyName, boolean fallbackDefault)
   throws IllegalArgumentException, InvalidPropertyValueException {

      // Check preconditions
      MandatoryArgumentChecker.check("properties",   properties, "propertyName", propertyName);

      // Query the Map<String, String>
      String value = properties.get(propertyName);

      // Fallback to the default, if necessary
      if (TextUtils.isEmpty(value)) {
         return fallbackDefault;
      }

      // Parse the string
      if ("true".equals(value)) {
         return true;
      } else if ("false".equals(value)) {
         return false;
      } else {
         throw new InvalidPropertyValueException(propertyName, value);
      }
   }

   /**
    * Gets the property with the specified name and converts it to an
    * <code>int</code>.
    *
    * @param properties
    *    the set of properties to read from, cannot be <code>null</code>.
    *
    * @param propertyName
    *    the name of the property to read, cannot be <code>null</code>.
    *
    * @return
    *    the value of the property, as an <code>int</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>properties == null || propertyName == null</code>.
    *
    * @throws MissingRequiredPropertyException
    *    if the specified property is not set, or if it is set to an empty
    *    string.
    *
    * @throws InvalidPropertyValueException
    *    if the conversion to an <code>int</code> failed.
    */
   public static int getIntProperty(Map<String, String> properties, String propertyName)
   throws IllegalArgumentException, MissingRequiredPropertyException, InvalidPropertyValueException {

      // Check preconditions
      MandatoryArgumentChecker.check("properties", properties, "propertyName", propertyName);

      // Query the Map<String, String>
      String value = properties.get(propertyName);

      // Make sure the value is set
      if (value == null || value.length() == 0) {
         throw new MissingRequiredPropertyException(propertyName);
      }

      // Parse the string
      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException exception) {
         throw new InvalidPropertyValueException(propertyName, value);
      }
   }

   /**
    * Retrieves the specified property and throws a
    * <code>MissingRequiredPropertyException</code> if it is not set.
    *
    * @param properties
    *    the set of properties to retrieve a specific proeprty from, cannot be
    *    <code>null</code>.
    *
    * @param name
    *    the name of the property, cannot be <code>null</code>.
    *
    * @return
    *    the value of the property, guaranteed not to be <code>null</code> and
    *    guaranteed to contain at least one character.
    *
    * @throws IllegalArgumentException
    *    if <code>properties == null || name == null</code>.
    *
    * @throws MissingRequiredPropertyException
    *    if the value of the property is either <code>null</code> or an empty
    *    string.
    */
   public static String getRequiredProperty(Map<String, String> properties, String name)
   throws IllegalArgumentException, MissingRequiredPropertyException {

      // Check preconditions
      MandatoryArgumentChecker.check("properties", properties, "name", name);

      // Retrieve the value
      String value = properties.get(name);

      // The property is required
      if (value == null || value.length() < 1) {
         throw new MissingRequiredPropertyException(name);
      }

      return value;
   }
 
   /**
    * Retrieves a property with the specified name, falling back to a default 
    * value if the property is not set.
    *
    * @param properties
    *    the set of properties to retrieve a property from,
    *    cannot be <code>null</code>.
    *
    * @param key
    *    the property key, 
    *    cannot be <code>null</code>.
    *
    * @param fallbackValue
    *    the fallback default value, returned in case the property is not set 
    *    in <code>properties</code>, cannot be <code>null</code>.
    *
    * @return
    *    the value of the property or the fallback value.
    *
    * @throws IllegalArgumentException
    *    if <code>properties == null || key == null || fallbackValue == null</code>.
    */
   public String getWithDefault(Map<String, String> properties, String key, String fallbackValue)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("properties",    properties,
                                     "key",           key,
                                     "fallbackValue", fallbackValue);

      // Get value
      String value = properties.get(key);
      if (value != null) {
         return value;

      // Fallback if necessary
      } else {
         return fallbackValue;
      }
   }

   /**
    * Constructs a <code>Map&lt;String, String&gt;</code> from the specified input
    * stream.
    *
    * <p>The parsing done is similar to the parsing done by the
    * {@link Properties#load(InputStream)} method. Empty values will be
    * ignored.
    *
    * @param in
    *    the input stream to read from, cannot be <code>null</code>.
    *
    * @return
    *    a {@link Map} instance that contains all the properties
    *    defined in the specified input stream.
    *
    * @throws IllegalArgumentException
    *    if <code>in == null</code>.
    *
    * @throws IOException
    *    if there was an I/O error while reading from the stream.
    */
   public static Map<String, String> createMapString(InputStream in)
   throws IllegalArgumentException, IOException {

      // Check preconditions
      MandatoryArgumentChecker.check("in", in);

      // Parse the input stream using java.util.Properties
      Properties properties = new Properties();
      properties.load(in);

      // Convert from java.util.Properties to Map<String, String>
      Map<String, String> r = new HashMap<String, String>();
      Enumeration names = properties.propertyNames();
      while (names.hasMoreElements()) {
         String key   = (String) names.nextElement();
         String value = properties.getProperty(key);

         if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            r.put(key, value);
         }
      }

      return r;
   }

   /**
    * Returns the String representation of the specified <code>Map&lt;String, String&gt;</code>.
    * For each entry, both the key and the value are encoded using the URL
    * encoding (see {@link URLEncoding}).
    * The key and value are separated by a literal equals sign
    * (<code>'='</code>). The entries are separated using an ampersand
    * (<code>'&amp;'</code>).
    *
    * <p>If the value for an entry is either <code>null</code> or an empty
    * string (<code>""</code>), then nothing is added to the String for that
    * entry.
    *
    * @param properties
    *    the {@link Map} to serialize, cannot be <code>null</code>.
    *
    * @return
    *    the String representation of the specified <code>Map&lt;String, String&gt;</code>.
    */
   public static String toString(Map<String, String> properties) {
      return toString(properties, null, null, null, -1);
   }

   /**
    * Serializes the specified <code>MapMap&lt;String, String&gt;</code> to a
    * <code>String</code>. For each entry, both the key and the
    * value are encoded using the URL encoding (see {@link URLEncoding}).
    * The key and value are separated by a literal equals sign
    * (<code>'='</code>). The entries are separated using
    * an ampersand (<code>'&amp;'</code>).
    *
    * <p>If the value for an entry is either <code>null</code> or an empty
    * string (<code>""</code>), then nothing is added to the String for that
    * entry.
    *
    * @param properties
    *    the {@link Map} to serialize, can be <code>null</code>.
    *
    * @param valueIfEmpty
    *    the string to append to the buffer in case
    *    <code>properties == null || properties.isEmpty()</code>.
    *
    * @return
    *    the String representation of the Map<String, String> or the valueIfEmpty, never <code>null</code>.
    *    If all parameters are <code>null</code> then an empty String is returned.
    */
   public static String toString(Map<String, String> properties,
                                 String         valueIfEmpty) {
      return toString(properties, valueIfEmpty, null, null, -1);
   }


   /**
    * Returns the <code>String</code> representation for the specified
    * <code>Map&lt;String, String&gt;</code>.
    *
    * @param properties
    *    the {@link Map} to construct a String for, or <code>null</code>.
    *
    * @param valueIfEmpty
    *    the value to return if the specified set of properties is either
    *    <code>null</code> or empty, can be <code>null</code>.
    *
    * @param prefixIfNotEmpty
    *    the prefix to add to the value if the <code>Map&lt;String, String&gt;</code>
    *    is not empty, can be <code>null</code>.
    *
    * @param suffix
    *    the suffix to add to the value, can be <code>null</code>. The suffix
    *    will be added even if the Map&lt;String, String&gt; is empty.
    *
    * @return
    *    the String representation of the Map&lt;String, String&gt; with the different artifacts, never <code>null</code>.
    *    If all parameters are <code>null</code> then an empty String is returned.
    */
   public static String toString(Map<String, String> properties, String valueIfEmpty,
         String prefixIfNotEmpty, String suffix) {
      return toString(properties, valueIfEmpty, prefixIfNotEmpty, suffix, -1);
   }

   /**
    * Returns the <code>String</code> representation for the specified
    * <code>Map&lt;String, String&gt;</code>.
    *
    * @param properties
    *    the {@link Map} to construct a String for, or <code>null</code>.
    *
    * @param valueIfEmpty
    *    the value to return if the specified set of properties is either
    *    <code>null</code> or empty, can be <code>null</code>.
    *
    * @param prefixIfNotEmpty
    *    the prefix to add to the value if the <code>Map&lt;String, String&gt;</code>
    *    is not empty, can be <code>null</code>.
    *
    * @param suffix
    *    the suffix to add to the value, can be <code>null</code>. The suffix
    *    will be added even if the Map<String, String>is empty.
    *
    * @param maxValueLength
    *    the maximum of characters to set for the value, if the value is longer
    *    than this limit '...' will be added after the limit.
    *    If the value is -1, no limit will be set.
    *
    * @return
    *    the String representation of the Map<String, String> with the different artifacts, never <code>null</code>.
    *    If all parameters are <code>null</code> then an empty String is returned.
    */
   public static String toString(Map<String, String> properties, String valueIfEmpty,
         String prefixIfNotEmpty, String suffix, int maxValueLength) {

      // If the property set if null, return the fallback
      if (properties == null || properties.isEmpty()) {
         if (suffix != null) {
            return suffix;
         } else {
            return valueIfEmpty;
         }
      }

      StringBuffer buffer = new StringBuffer(299);

      boolean first = true;
      for (Entry<String, String> property : properties.entrySet()) {

         // Get the name and value
         String name  = property.getKey();
         String value = property.getValue();

         // If the value is null or an empty string, then output nothing
         if (value == null || value.length() == 0) {
            continue;
         }

         // Append an ampersand, except for the first entry
         if (!first) {
            buffer.append('&');
         } else {
            first = false;
            if (prefixIfNotEmpty != null) {
               buffer.append(prefixIfNotEmpty);
            }
         }

         // Append the key and the value, separated by an equals sign
         buffer.append(URLEncoding.encode(name));
         buffer.append('=');
         String encodedValue;
         if (maxValueLength == -1 || value.length() <= maxValueLength) {
            encodedValue = URLEncoding.encode(value);
         } else {
            encodedValue = URLEncoding.encode(value.substring(0, maxValueLength)) + "...";
         }
         buffer.append(encodedValue);
      }

      if (suffix != null) {
         buffer.append('&');
         buffer.append(suffix);
      }

      return buffer.toString();
   }

   /**
    * Converts the specified <code>Properties</code> object to a new
    * <code>Map&lt;String, String&gt;</code> object.
    *
    * @param properties
    *    the {@link Properties} object, cannot be <code>null</code>.
    *
    * @return
    *    a new {@link Map} object, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>properties == null</code>.
    */
   public static Map<String, String> fromProperties(Properties properties)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("properties", properties);

      Map<String, String> prop = new HashMap<String, String>();
      for (Entry<Object, Object> property : properties.entrySet()) {
         String name  = (String) property.getKey();
         String value = (String) property.getValue();

         prop.put(name, value);
      }
      return prop;
   }

   /**
    * Converts the specified <code>Map&lt;String, String&gt;</code> object to a new
    * <code>Properties</code> object.
    *
    * @param properties
    *    the {@link Map} object, cannot be <code>null</code>.
    *
    * @return
    *    a new {@link Properties} object, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>properties == null</code>.
    */
   public static Properties toProperties(Map<String, String> properties)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("properties", properties);

      Properties prop = new Properties();
      for (Entry<String, String> property : properties.entrySet()) {
         String name  = property.getKey();
         String value = property.getValue();

         prop.setProperty(name, value);
      }
      return prop;
   }
}