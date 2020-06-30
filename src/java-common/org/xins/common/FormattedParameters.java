/*
 * $Id: FormattedParameters.java,v 1.7 2010/11/18 20:35:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common;

import java.util.Iterator;
import java.util.Map;

import org.xins.common.text.URLEncoding;
import org.w3c.dom.Element;

/**
 * Convert parameters to (semi) human readable text.
 *
 * @version $Revision: 1.7 $ $Date: 2010/11/18 20:35:05 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.0
 */
public class FormattedParameters {

   /**
    * The parameters to serialize. This field can be <code>null</code>.
    */
   private final Map<String, String> _parameters;

   /**
    * The data section.
    */
   private final Element _dataSection;

   /**
    * The value if there is no parameters and data section.
    */
   private final String _valueIfEmpty;

   /**
    * The prefix if there is at least a parameter or a data section.
    */
   private final String _prefixIfNotEmpty;

   /**
    * The maximum limit of serialized value characters.
    */
   private final int _maxValueLength;

   /**
    * The String representation of the parameters.
    */
   private String _asString;

   /**
    * Constructs a new <code>FormattedParameters</code> object.
    *
    * @param parameters
    *    the parameters, can be <code>null</code>.
    */
   public FormattedParameters(Map<String, String> parameters) {
      this(parameters, null);
   }

   /**
    * Constructs a new <code>FormattedParameters</code> object.
    *
    * @param parameters
    *    the parameters, can be <code>null</code>.
    *
    * @param dataSection
    *    the data section, can be <code>null</code>.
    */
   public FormattedParameters(Map<String, String> parameters, Element dataSection) {
      this(parameters, dataSection, "-", null, -1);
   }

   /**
    * Constructs a new <code>FormattedParameters</code> object.
    *
    * @param parameters
    *    the parameters, can be <code>null</code>.
    *
    * @param dataSection
    *    the data section, can be <code>null</code>.
    *
    * @param valueIfEmpty
    *    the value to return if there is no parameter and no data section, can be <code>null</code>.
    *
    * @param prefixIfNotEmpty
    *    the prefix to add if there is a parameter or a data section, can be <code>null</code>.
    *
    * @param maxValueLength
    *    the maximum of characters to set for the value, if the value is longer
    *    than this limit '...' will be added after the limit.
    *    If the value is -1, no limit will be set.
    */
   public FormattedParameters(Map<String, String> parameters, Element dataSection, String valueIfEmpty,
         String prefixIfNotEmpty, int maxValueLength) {
      _parameters = parameters;
      _dataSection = dataSection;
      _valueIfEmpty = valueIfEmpty;
      _prefixIfNotEmpty = prefixIfNotEmpty;
      _maxValueLength = maxValueLength;
   }

   /**
    * String representation of the parameters including the data section.
    *
    * @return
    *    the String representation of the request.
    */
   public String toString() {

      // The String representation has already been created.
      if (_asString != null) {
         return _asString;
      }

      Iterator names = (_parameters == null) ? null : _parameters.keySet().iterator();

      // If there are no parameters, then just return a hyphen
      if ((names == null || ! names.hasNext()) && _dataSection == null) {
         _asString = _valueIfEmpty;
         return _asString;
      }

      StringBuffer buffer = new StringBuffer(160);
      if (_prefixIfNotEmpty != null) {
         buffer.append(_prefixIfNotEmpty);
      }

      boolean first = true;
      while (names != null && names.hasNext()) {

         // Get the name and value
         String name  = (String) names.next();
         String value = _parameters.get(name);

         // If the value is null or an empty string, then output nothing
         if (value == null || value.length() == 0) {
            continue;
         }

         // Append an ampersand, except for the first entry
         if (!first) {
            buffer.append('&');
         } else {
            first = false;
         }

         // Append the key and the value, separated by an equals sign
         buffer.append(URLEncoding.encode(name));
         buffer.append('=');
         String encodedValue;
         if (_maxValueLength == -1 || value.length() <= _maxValueLength) {
            encodedValue = URLEncoding.encode(value);
         } else {
            encodedValue = URLEncoding.encode(value.substring(0, _maxValueLength)) + "...";
         }
         buffer.append(encodedValue);
      }

      if (_dataSection != null) {
         if (!first) {
            buffer.append('&');
         }
         buffer.append("_data=");
         buffer.append(URLEncoding.encode(_dataSection.toString()));
      }

      _asString = buffer.toString();
      return _asString;
   }
}
