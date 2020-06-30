package org.xins.common.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xins.common.MandatoryArgumentChecker;

/**
 * Class used to create a XINS data section.
 *
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0
 */
public class DataElementBuilder {

   /**
    * The DOM document containing the data element.
    */
   private Document document;

   /**
    * The data element.
    */
   private Element dataElement;

   /**
    * Creates a new data element.
    */
   public DataElementBuilder() {
      dataElement = ElementFormatter.createMainElement("data");
      document = dataElement.getOwnerDocument();
   }

   /**
    * Creates a new element intented to be included in the data section.
    *
    * @param elementName
    *    the name of the element, cannot be <code>null</code>.
    * @return
    *    the created element, never <code>null</code>.
    */
   public Element createElement(String elementName) {
      MandatoryArgumentChecker.check("elementName", elementName);
      return document.createElement(elementName);
   }

   /**
    * Adds an element to the data section. The added element is a direct child
    * of the data element.
    * 
    * @param subElement
    *    the element to add, cannot be <code>null</code>.
    */
   public void addToDataElement(Element subElement) {
      MandatoryArgumentChecker.check("subElement", subElement);
      if (subElement.getOwnerDocument() == document) {
         dataElement.appendChild(subElement);
      } else {
         Element element = (Element) document.importNode(subElement, true);
         dataElement.appendChild(element);
      }
   }

   /**
    * Adds an element to the data section.
    * The empty element based on the element name is created before being added.
    * The added element is a direct child of the data element.
    *
    * @param elementName
    *    the name of the element to add, cannot be <code>null</code>.
    * @return
    *    the created element, never <code>null</code>
    */
   public Element addToDataElement(String elementName) {
      Element element = document.createElement(elementName);
      dataElement.appendChild(element);
      return element;
   }

   /**
    * Gets the document used to create the data section.
    *
    * @return
    *    the DOM document, never <code>null</code>.
    */
   public Document getDocument() {
      return document;
   }

   /**
    * Gets the data element.
    *
    * @return
    *    the data element, never <code>null</code>.
    */
   public Element getDataElement() {
      return dataElement;
   }

   /**
    * Creates the String representation of the data section.
    *
    * @return
    *    the XML as String without XML declaration, never <code>null</code>.
    */
   @Override
   public String toString() {
      return ElementFormatter.format(dataElement);
   }
}
