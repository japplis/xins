/*
 * The code of this file is in public domain.
 */
package org.xins.common.xml;

import java.util.LinkedList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xins.common.text.ParseException;

/**
 * An ElementList is an NodeList with the following improvements:
 * <ul><li>Implements List which make it iterable with for each loop
 * <li>Only includes the direct child of the element
 * <li>Only includes the elements
 * <li>By default includes all direct child elements
 * <li>Preserves the order of the child elements
 * <li>Includes a method to get the sub element when unique
 * </ul>
 *
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0
 */
public class ElementList extends LinkedList<Element> {

   /**
    * The local name of the parent element.
    */
   private String parentName;

   /**
    * The local name of the child elements or * for all elements
    */
   private String childName;

   /**
    * Creates a list with all direct child element of the given element.
    *
    * @param element
    *    the parent element, cannot be <code>null</code>.
    */
   public ElementList(Element element) {
      this(element, "*");
   }

   /**
    * Creates a list with all direct child element with a specific local name of the given element.
    *
    * @param element
    *    the parent element, cannot be <code>null</code>.
    * @param childName
    *    the local name of the direct child elements that should be added to the list, cannot be <code>null</code>.
    */
   public ElementList(Element element, String childName) {
      parentName = element.getTagName();
      this.childName = childName;
      Node child = element.getFirstChild();
      while (child != null) {
         String newChildName = child.getLocalName();
         if (newChildName == null) {
            newChildName = child.getNodeName();
         }
         if (child.getNodeType() == Node.ELEMENT_NODE &&
                 (childName.endsWith("*") || childName.equals(newChildName))) {
            add((Element) child);
         }
         child = child.getNextSibling();
      }
   }

   /**
    * Gets the unique child of this list.
    *
    * @return
    *    the sub-element of this element list, never <code>null</code>.
    *
    * @throws ParseException
    *    if no child with the specified name was found,
    *    or if more than one child with the specified name was found.
    */
   public Element getUniqueChildElement() throws ParseException {
      if (isEmpty()) {
         throw new ParseException("No \"" + childName + "\" child found in the \"" + parentName + "\" element.");
      } else if (size() > 1) {
         throw new ParseException("More than one \"" + childName + "\" children found in the \"" + parentName + "\" element.");
      }
      return get(0);
   }

   /**
    * Gets the first child of this element.
    *
    * @return
    *    the sub-element of this element, or <code>null</code> if no element is found.
    */
   public Element getFirstChildElement() {
      if (isEmpty()) {
         return null;
      }
      return get(0);
   }
}
