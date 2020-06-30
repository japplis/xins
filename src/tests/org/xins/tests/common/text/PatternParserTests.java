/*
 * $Id: PatternParserTests.java,v 1.12 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.common.text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for parsing of the regular expression in <code>PatternType</code>.
 *
 * @version $Revision: 1.12 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class PatternParserTests extends TestCase {

   /**
    * Constructs a new <code>SimplePatternParserTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public PatternParserTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(PatternParserTests.class);
   }

   public void testMatchPattern() throws Throwable {
      doTestMatchPattern(".*", "hello world", true);
      doTestMatchPattern("^([a-zA-Z0-9._\\-]*@((wanadoo)|(euronet))\\.nl)$", "freeclosed.seven@wanadoo.nl", true);
   }

   /**
    * Tests if an example matches a regular expression.
    *
    * @param re
    *    The Perl 5 regular expression.
    * @param example
    *    The example that should match or not the regular expression.
    * @param shouldMatch
    *    true if the example should match the regex, false if the example
    *    should not match the regex.
    */
   private void doTestMatchPattern(String re, String example, boolean shouldMatch)
   throws Throwable {

      Pattern pattern = null;
      try {
         pattern = Pattern.compile(re);
      } catch (PatternSyntaxException pse) {
         fail("Failed to compile the regular expression: "+re);
      }
      boolean match = pattern.matcher(example).matches();
      if (shouldMatch) {
         assertTrue("The example \"" + example + "\" does not match the pattern \""+re+"\"", match);
      } else {
         assertTrue("The example \"" + example + "\" does not match the pattern \""+re+"\"", !match);
      }
   }
}
