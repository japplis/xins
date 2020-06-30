/*
 * $Id: SimplePatternParserTests.java,v 1.14 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.common.text;

import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xins.common.text.ParseException;
import org.xins.common.text.SimplePatternParser;

/**
 * Tests for class <code>SimplePatternParser</code>.
 *
 * @version $Revision: 1.14 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class SimplePatternParserTests extends TestCase {

   private SimplePatternParser _parser;

   /**
    * Constructs a new <code>SimplePatternParserTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public SimplePatternParserTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(SimplePatternParserTests.class);
   }

   /**
    * Performs setup for the tests.
    */
   protected void setUp() {
      _parser = new SimplePatternParser();
   }

   public void testParseSimplePattern() throws Throwable {

      try {
         _parser.parseSimplePattern(null);
         fail("SimplePatternParser.parseSimplePattern(null) should throw an IllegalArgumentException.");
         return;
      } catch (IllegalArgumentException exception) {
         // as expected
      }

      doTestParseSimplePattern("**");
      doTestParseSimplePattern("??");
      doTestParseSimplePattern("*?");
      doTestParseSimplePattern("?*");
      doTestParseSimplePattern("aa?a?*a");
      doTestParseSimplePattern("aa*a*?a");
      doTestParseSimplePattern("aa?a??a");

      doTestParseSimplePattern("",         "");
      doTestParseSimplePattern("*",        ".*");
      doTestParseSimplePattern("?",        ".");
      doTestParseSimplePattern("_Get*",    "_Get.*");
      doTestParseSimplePattern("_Get*i?n", "_Get.*i.n");
      doTestParseSimplePattern("_Get*,_Dis*", "_Get.*|_Dis.*");
      doTestParseSimplePattern("*on",      ".*on");
      doTestParseSimplePattern("...",      "\\.\\.\\.");

      doTestParseSimplePattern("abcdefghijklmnopqrstuvwxyz1234567890-_.", "abcdefghijklmnopqrstuvwxyz1234567890-_\\.");

      final String invalidChars = "~`!@#$%^&()=+[]{}|\\:;\"'<>/";
      for (int i = 0; i < invalidChars.length(); i++) {
         char invalidChar = invalidChars.charAt(i);
         doTestParseSimplePattern(""   + invalidChar + "");
         doTestParseSimplePattern("aa" + invalidChar + "");
         doTestParseSimplePattern(""   + invalidChar + "aa");
         doTestParseSimplePattern("aa" + invalidChar + "aa");
      }
   }

   private void doTestParseSimplePattern(String simple)
   throws Throwable {
      try {
         _parser.parseSimplePattern(simple);
         fail("SimplePatternParser.parseSimplePattern(\"" + simple + "\") should throw a ParseException.");
         return;
      } catch (ParseException exception) {
         // as expected
      }
   }

   private void doTestParseSimplePattern(String simple, String re)
   throws Throwable {
      Pattern pattern = _parser.parseSimplePattern(simple);
      assertEquals(re, pattern.pattern());
   }
}
