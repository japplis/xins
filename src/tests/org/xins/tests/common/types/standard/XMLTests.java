package org.xins.tests.common.types.standard;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xins.common.types.TypeValueException;
import org.xins.common.types.XMLType;

/**
 *
 * @author Anthony Goubard
 */
public class XMLTests extends TestCase {

   /**
    * Constructs a new <code>Int16Tests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public XMLTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(XMLTests.class);
   }

   public void testValidValue() throws Throwable {
      XHTMLType htmlType = new XHTMLType();

      assertFalse("fred should be invalid html", htmlType.isValidValue("fred"));

      assertFalse("Not all xml is valid html.", htmlType.isValidValue("<test />"));

      assertTrue("Valid html.", htmlType.isValidValue("<?xml version=\"1.0\"?><xhtml><body></body></xhtml>"));
   }

   class XHTMLType extends XMLType {

      // constructor
      public XHTMLType() throws Exception {
         super("SOAPType", "http://www.w3.org/2002/08/xhtml/xhtml1-strict.xsd");
      }

  }
}
