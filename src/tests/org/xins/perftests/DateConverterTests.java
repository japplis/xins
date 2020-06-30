/*
 * $Id: DateConverterTests.java,v 1.15 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.perftests;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xins.common.text.DateConverter;
import org.xins.common.text.FastStringBuffer;

/**
 * Performance tests for class <code>DateConverter</code>.
 *
 * @version $Revision: 1.15 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class DateConverterTests extends TestCase {

   private static final int ROUNDS = 1000000;

   private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

   /**
    * Constructs a new <code>DateConverterTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public DateConverterTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(DateConverterTests.class);
   }

   public void testDateConverterFormatString() throws Exception {

      long millis = System.currentTimeMillis();
      DateConverter dc = new DateConverter(true);

      for (int i = 0; i < ROUNDS; i++) {
         millis += i & 0xff;
         dc.format(millis);
         dc.format(millis);
         millis++;
         dc.format(millis);
      }
   }

   public void testDateConverterFormatCharBuffer() throws Exception {

      long millis = System.currentTimeMillis();
      DateConverter dc = new DateConverter(true);

      char[] buffer = new char[30];
      for (int i = 0; i < ROUNDS; i++) {
         millis += i & 0xff;
         dc.format(millis, buffer, 0);
         dc.format(millis, buffer, 0);
         millis++;
         dc.format(millis, buffer, 0);
      }
   }

   public void testDateConverterFormatWithThreadLocal() throws Exception {

      long millis = System.currentTimeMillis();
      DateConverter dc = new DateConverter(true);

      char[] buffer = new char[30];
      THREAD_LOCAL.set(buffer);

      for (int i = 0; i < ROUNDS; i++) {
         buffer = (char[]) THREAD_LOCAL.get();
         millis += i & 0xff;
         dc.format(millis, buffer, 0);
         buffer = (char[]) THREAD_LOCAL.get();
         dc.format(millis, buffer, 0);
         millis++;
         buffer = (char[]) THREAD_LOCAL.get();
         dc.format(millis, buffer, 0);
      }
   }
}
