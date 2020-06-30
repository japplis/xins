/*
 * $Id: XML.java,v 1.5 2012/03/03 21:23:44 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types.standard;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.types.Type;
import org.xins.common.types.TypeValueException;
import org.w3c.dom.Element;
import org.xins.common.xml.ElementFormatter;
import org.xml.sax.SAXException;

/**
 * Standard type <em>_xml</em>. A value of this type represents an XML
 * fragment.
 *
 * @version $Revision: 1.5 $ $Date: 2012/03/03 21:23:44 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 2.2
 */
public class XML extends Type {

   /**
    * The only instance of this class. This field is never <code>null</code>.
    */
   public static final XML SINGLETON = new XML();

   /**
    * Constructs a new <code>XML</code> instance.
    * This constructor is private, the field {@link #SINGLETON} should be
    * used.
    */
   private XML() {
      super("_xml", Element.class);
   }

   /**
    * Constructs a new <code>XML</code> instance.
    * This constructor is protected to be used by {@link XMLType}.
    */
   protected XML(String name) {
      super(name, Element.class);
   }

   /**
    * Constructs an <code>Element</code> from the specified
    * non-<code>null</code> string.
    *
    * @param string
    *    the string to convert, cannot be <code>null</code>.
    *
    * @return
    *    the {@link Element} object, never <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.
    */
   public static Element fromStringForRequired(String string)
   throws IllegalArgumentException, TypeValueException {

      // Check preconditions
      MandatoryArgumentChecker.check("string", string);

      return (Element) SINGLETON.fromString(string);
   }

   /**
    * Constructs an <code>Element</code> from the specified string.
    *
    * @param string
    *    the string to convert, can be <code>null</code>.
    *
    * @return
    *    the {@link Element}, or <code>null</code> if
    *    <code>string == null</code>.
    *
    * @throws TypeValueException
    *    if the specified string does not represent a valid value for this
    *    type.
    */
   public static Element fromStringForOptional(String string)
   throws TypeValueException {
      return (string == null || string.length() == 0) ? null : fromStringForRequired(string);
   }

   @Override
   protected boolean isValidValueImpl(String value) {

      Element element;
      try {
         element = ElementFormatter.parse(value);
      } catch (Throwable exception) {
         element = null;
      }

      return element != null;
   }

   protected final Object fromStringImpl(String string)
   throws TypeValueException {

      try {
         Element parsedElement = ElementFormatter.parse(string);
         return parsedElement;
      } catch (SAXException cause) {
         TypeValueException exception = new TypeValueException(this, string, "Not a valid XML fragment.");
         exception.initCause(cause);
         throw exception;
      }
   }

   public final String toString(Object value)
   throws IllegalArgumentException, ClassCastException, TypeValueException {

      // Check preconditions
      MandatoryArgumentChecker.check("value", value);

      // Convert the object to a String
      return toString((Element) value);
   }

   /**
    * Converts the specified <code>Element</code> to a string.
    *
    * @param element
    *    the XML fragment to convert, can be <code>null</code>.
    *
    * @return
    *    the textual representation of the value;
    *    or <code>null</code> if and only if <code>value == null</code>.
    */
   public static String toString(Element element) {

      // Short-circuit if the argument is null
      if (element == null) {
         return null;
      }

      String elementAsString = ElementFormatter.format(element);
      return elementAsString;
   }

   public String getDescription() {
      return "XML fragment.";
   }
}
