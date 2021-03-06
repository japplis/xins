/*
 * $Id: ElementBuilder.java,v 1.30 2010/09/29 20:04:51 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.xml;

import java.io.IOException;
import java.io.StringReader;
import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.Utils;
import org.xins.common.text.ParseException;

/**
 * Builder for <code>Element</code> instances.
 *
 * <p>This class is not thread-safe; it should not be used from different
 * threads at the same time.
 *
 * @version $Revision: 1.30 $ $Date: 2010/09/29 20:04:51 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.1.0
 * @deprecated since XINS 3.0. Use {@link org.w3c.dom.Element}
 */
@Deprecated
public class ElementBuilder {

   // TODO: Document the states. A user of this class should be aware of the
   //       fact that addChild(...) cannot be called if startElement(...) was
   //       just called, for example.

   // TODO: Add one or more examples in the class comment.
   /**
    * Initial state for the builder.
    */
   private static final State INITIAL = new State("INITIAL");

   /**
    * State that indicates that the builder has started to build the element.
    */
   private static final State STARTED = new State("STARTED");

   /**
    * The current state. Never <code>null</code>.
    */
   private State _state;

   /**
    * The current <code>Element</code> that this builder is building.
    * Initially <code>null</code>, but set to a value by the
    * <code>startElement</code> methods.
    */
   private Element _element;

   /**
    * Creates a new <code>ElementBuilder</code>.
    */
   public ElementBuilder() {

      _state = INITIAL;
   }

   /**
    * Creates a new <code>ElementBuilder</code>.
    *
    * @param localName
    *    the local name of the element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    */
   public ElementBuilder(String localName)
   throws IllegalArgumentException {
      this(null, null, localName);
   }

   /**
    * Creates a new <code>ElementBuilder</code>.
    *
    * @param namespaceURI
    *    the namespace URI for the element, can be <code>null</code>; an empty
    *    string is equivalent to <code>null</code>.
    *
    * @param localName
    *    the local name of the element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    */
   public ElementBuilder(String namespaceURI, String localName)
   throws IllegalArgumentException {
      this(null, namespaceURI, localName);
   }

   /**
    * Creates a new <code>ElementBuilder</code>.
    *
    * @param namespacePrefix
    *    the namespace prefix for the element, can be <code>null</code>; an empty
    *    string is equivalent to <code>null</code>.
    *
    * @param namespaceURI
    *    the namespace URI for the element, can be <code>null</code>; an empty
    *    string is equivalent to <code>null</code>.
    *
    * @param localName
    *    the local name of the element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    *
    * @since XINS 2.1.
    */
   public ElementBuilder(String namespacePrefix, String namespaceURI, String localName)
   throws IllegalArgumentException {

      _state = INITIAL;
      startElement(namespacePrefix, namespaceURI, localName);
   }

   /**
    * Sets the specified attribute. If the value for the specified
    * attribute is already set, then the previous value is replaced.
    *
    * @param localName
    *    the local name for the attribute, cannot be <code>null</code>.
    *
    * @param value
    *    the value for the attribute, can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    */
   public void setAttribute(String localName, String value)
   throws IllegalArgumentException {
      setAttribute(null, localName, value);
   }

   /**
    * Sets the specified attribute. If the value for the specified
    * attribute is already set, then the previous value is replaced.
    *
    * @param namespaceURI
    *    the namespace URI for the attribute, can be <code>null</code>; an
    *    empty string is equivalent to <code>null</code>.
    *
    * @param localName
    *    the local name for the attribute, cannot be <code>null</code>.
    *
    * @param value
    *    the value for the attribute, can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    */
   public void setAttribute(String namespaceURI,
                            String localName,
                            String value)
   throws IllegalArgumentException {
      setAttribute(null, namespaceURI, localName, value);
   }

   /**
    * Sets the specified attribute. If the value for the specified
    * attribute is already set, then the previous value is replaced.
    *
    * @param namespacePrefix
    *    the namespace prefix for the attribute, can be <code>null</code>; an
    *    empty string is equivalent to <code>null</code>.
    *
    * @param namespaceURI
    *    the namespace URI for the attribute, can be <code>null</code>; an
    *    empty string is equivalent to <code>null</code>.
    *
    * @param localName
    *    the local name for the attribute, cannot be <code>null</code>.
    *
    * @param value
    *    the value for the attribute, can be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    *
    * @since XINS 2.1.
    */
   public void setAttribute(String namespacePrefix,
                            String namespaceURI,
                            String localName,
                            String value)
   throws IllegalArgumentException {

      // Check state
      if (_state == INITIAL) {
         throw Utils.logProgrammingError("Unexpected state " + _state);
      }

      // Really set the attribute
      _element.setAttribute(namespacePrefix, namespaceURI, localName, value);
   }

   /**
    * Adds a new child element.
    *
    * @param child
    *    the new child to add to this element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>child == null || child == <em>this</em></code>.
    */
   public void addChild(Element child)
   throws IllegalArgumentException {

      // Check state
      if (_state == INITIAL) {
         throw Utils.logProgrammingError("Unexpected state " + _state);
      }

      // Really add the child element
      _element.addChild(child);
   }

   /**
    * Adds a new child element.
    *
    * @param xmlChild
    *    the character string that contains the XML that defines the new
    *    child to add to this element, cannot be <code>null</code>.
    *
    * @throws ParseException
    *    if the <code>String</code> passed as argument cannot be parsed.
    *
    * @throws IllegalArgumentException
    *    if <code>child == null</code>.
    *
    * @since XINS 1.3.0
    */
   public void addXMLChild(String xmlChild)
   throws ParseException, IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("xmlChild", xmlChild);

      // Check state
      if (_state == INITIAL) {
         throw Utils.logProgrammingError("Unexpected state " + _state);
      }

      ElementParser parser = new ElementParser();

      try {
         Element parsedElement = parser.parse(new StringReader(xmlChild));
         _element.addChild(parsedElement);
      } catch (IOException ioe) {

         // Never happens.
      }
   }

   /**
    * Sets the character content. The existing character content, if any, is
    * replaced
    *
    * @param text
    *    the character content for this element, or <code>null</code>.
    */
   public void setText(String text) {

      // Check state
      if (_state == INITIAL) {
         throw Utils.logProgrammingError("Unexpected state " + _state);
      }

      // Really set the character content
      _element.setText(text);
   }

   /**
    * Starts to create a new {@link Element} with the given local name.
    *
    * @param localName
    *    the local name of the element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    */
   public void startElement(String localName)
   throws IllegalArgumentException {
      startElement(null, localName);
   }

   /**
    * Starts to create a new <code>Element</code>.
    *
    * @param namespaceURI
    *    the namespace URI for the element, can be <code>null</code>; an empty
    *    string is equivalent to <code>null</code>.
    *
    * @param localName
    *    the local name of the element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    */
   public void startElement(String namespaceURI, String localName)
   throws IllegalArgumentException {

      // Check state
      if (_state != INITIAL) {
         throw Utils.logProgrammingError("Unexpected state " + _state);
      }

      // Really start the element
      _element = new Element(namespaceURI, localName);
      _state   = STARTED;
   }

   /**
    * Starts to create a new <code>Element</code>.
    *
    * @param namespacePrefix
    *    the namespace prefix for the element, can be <code>null</code>; an empty
    *    string is equivalent to <code>null</code>.
    *
    * @param namespaceURI
    *    the namespace URI for the element, can be <code>null</code>; an empty
    *    string is equivalent to <code>null</code>.
    *
    * @param localName
    *    the local name of the element, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>localName == null</code>.
    *
    * @since XINS 2.1.
    */
   public void startElement(String namespacePrefix, String namespaceURI, String localName)
   throws IllegalArgumentException {

      // Check state
      if (_state != INITIAL) {
         throw Utils.logProgrammingError("Unexpected state " + _state);
      }

      // Really start the element
      _element = new Element(namespacePrefix, namespaceURI, localName);
      _state   = STARTED;
   }

   /**
    * Creates the <code>Element</code>.
    * If you want to reuse this <code>ElementBuilder</code> you will need
    * to call the {@link #startElement(String)} or
    * {@link #startElement(String,String)} method.
    *
    * @return
    *    the constructed {@link Element}, never <code>null</code>.
    */
   public Element createElement() {

      // Check state
      if (_state != STARTED) {
         throw Utils.logProgrammingError("Unexpected state " + _state);
      }

      return _element;
   }

   /**
    * State of the builder.
    *
    * @version $Revision: 1.30 $ $Date: 2010/09/29 20:04:51 $
    * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
    */
   private static final class State {

      /**
       * The name of this state. Cannot be <code>null</code>.
       */
      private final String _name;

      /**
       * Constructs a new <code>State</code> object.
       *
       * @param name
       *    the name of this state, cannot be <code>null</code>.
       *
       * @throws IllegalArgumentException
       *    if <code>name == null</code>.
       */
      private State(String name) throws IllegalArgumentException {

         // Check preconditions
         MandatoryArgumentChecker.check("name", name);

         _name = name;
      }

      /**
       * Returns the name of this state.
       *
       * @return
       *    the name of this state, cannot be <code>null</code>.
       */
      public String getName() {
         return _name;
      }

      /**
       * Returns a textual representation of this object.
       *
       * @return
       *    the name of this state, never <code>null</code>.
       */
      public String toString() {
         return _name;
      }
   }
}
