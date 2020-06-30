/*
 * $Id: XMLType.java,v 1.3 2012/05/12 08:03:00 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.types;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Element;

import org.xins.common.types.standard.XML;
import org.xins.common.xml.ElementFormatter;

/**
 * Abstract base class for XML types with XSD validation. 
 *
 * @version $Revision: 1.3 $ $Date: 2012/05/12 08:03:00 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0
 */
public abstract class XMLType extends XML {

   /**
    * The location of the XSD file. This is the uncompiled version of {@link #_xml}.
    * This field cannot be <code>null</code>.
    */
   private final String _xsdLocation;

   /**
    * Validator. This field cannot be <code>null</code>.
    */
   private final Validator _validator;

   /**
    * Creates a new <code>XMLType</code> instance. The name of the type
    * needs to be specified. The value class (see
    * {@link Type#getValueClass()}) is set to {@link Element org.w3c.dom.Element.class}.
    *
    * @param name
    *    the name of the type, not <code>null</code>.
    *
    * @param xsdLocation
    *    the location of the schema, can be absolute or relative URI, not <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null || xsdLocation == null</code>.
    *
    * @throws PatternCompileException
    *    if the specified pattern is considered invalid.
    */
   protected XMLType(String name, String xsdLocation)
   throws IllegalArgumentException, Exception {
      super(name);

      if (xsdLocation == null) {
         throw new IllegalArgumentException("xsdLocation == null");
      }

      // Compile the regular expression to a Pattern object
      try {
         SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         Schema schema = factory.newSchema(getSource(xsdLocation));
         _validator = schema.newValidator();

      // Handle pattern compilation error
      } catch (Exception ex) {
         throw ex;
      }

      // Store the original pattern string
      _xsdLocation = xsdLocation;
   }
   
   protected Source getSource(String xsdLocation) {
      if (xsdLocation.startsWith("http") && xsdLocation.contains("://")) {
         return new StreamSource(xsdLocation);
      } else {
         InputStream xsdStream = getClass().getResourceAsStream("/specs/" + xsdLocation);
         if (xsdStream == null) {
            xsdStream = getClass().getResourceAsStream("/WEB-INF/specs/" + xsdLocation);
         }
         if (xsdStream == null) {
            xsdStream = getClass().getResourceAsStream(xsdLocation);
         }
         if (xsdStream != null) {
            return new StreamSource(xsdStream);
         } else {
            return new StreamSource(xsdLocation);
         }
      }
   }

   @Override
   protected final boolean isValidValueImpl(String value) {

      Element element;
      try {
         element = ElementFormatter.parse(value);
         _validator.validate(new DOMSource(element));
      } catch (Exception exception) {
         element = null;
      }

      return element != null;
   }

   @Override
   public String getDescription() {
      return "A XML Element that matched the schema located at " + _xsdLocation + ".";
   }
}
