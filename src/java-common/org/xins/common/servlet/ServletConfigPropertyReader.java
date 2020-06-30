/*
 * $Id: ServletConfigPropertyReader.java,v 1.22 2010/10/25 20:36:52 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.servlet;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletConfig;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.collections.EnumerationIterator;
import org.xins.common.collections.PropertyReader;
import org.xins.common.collections.PropertyReaderUtils;

/**
 * Implementation of a <code>PropertyReader</code> that returns the
 * initialization properties from a <code>ServletConfig</code> object.
 *
 * @version $Revision: 1.22 $ $Date: 2010/10/25 20:36:52 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
@Deprecated
public final class ServletConfigPropertyReader implements PropertyReader {

   /**
    * The servlet configuration object.
    */
   private final ServletConfig _servletConfig;

   /**
    * The number of properties. This field is lazily initialized by
    * {@link #size()}.
    */
   private int _size;

   /**
    * Constructs a new <code>ServletConfigPropertyReader</code>.
    *
    * @param servletConfig
    *    the {@link ServletConfig} object, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>servletConfig == null</code>.
    */
   public ServletConfigPropertyReader(ServletConfig servletConfig)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("servletConfig", servletConfig);

      _servletConfig = servletConfig;
   }

   /**
    * Retrieves the value of the property with the specified name.
    *
    * @param name
    *    the name of the property, cannot be <code>null</code>.
    *
    * @return
    *    the value of the property, possibly <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null</code>.
    */
   public String get(String name) throws IllegalArgumentException {
      MandatoryArgumentChecker.check("name", name);
      return _servletConfig.getInitParameter(name);
   }

   /**
    * Returns an <code>Iterator</code> that returns all property names.
    *
    * @return
    *    an {@link Iterator} for all property names, never <code>null</code>.
    */
   public Iterator getNames() {
      return new EnumerationIterator(_servletConfig.getInitParameterNames());
   }

   /**
    * Determines the number of properties.
    *
    * @return
    *    the size, always &gt;= 0.
    */
   public int size() {
      if (_size < 0) {
         int size = 0;
         Enumeration e = _servletConfig.getInitParameterNames();
         while (e.hasMoreElements()) {
            e.nextElement();
            size++;
         }
         _size = size;
      }

      return _size;
   }

   /**
    * Compares this object with the specified argument for equality.
    *
    * @param obj
    *    the object to compare with, can be <code>null</code>.
    *
    * @return
    *    <code>true</code> if the objects <code>a</code> and <code>b</code>
    *    are considered to be equal, <code>false</code> if they are considered
    *    different.
    */
   public boolean equals(Object obj) {
      return PropertyReaderUtils.equals(this, obj);
   }

   /**
    * Returns a hash code value for this object.
    *
    * @return
    *    a hash code value for this object.
    */
   public int hashCode() {
      // XXX: This is compute-intensive.
      //      Possible optimization is to store the hash code in a field.
      return PropertyReaderUtils.hashCode(this);
   }
}
