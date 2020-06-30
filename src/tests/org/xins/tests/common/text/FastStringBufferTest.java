/*
 * $Id: FastStringBufferTest.java,v 1.18 2010/11/18 20:35:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.common.text;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xins.common.text.FastStringBuffer;

/**
 * FastStringBuffer TestCase.
 *
 * @version $Revision: 1.18 $ $Date: 2010/11/18 20:35:05 $
 * @author <a href="mailto:mees.witteman@orange-ftgroup.com">Mees Witteman</a>
 */
public class FastStringBufferTest extends TestCase {

   /**
    * Constructs a new <code>FastStringBufferTest</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public FastStringBufferTest(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(FastStringBufferTest.class);
   }

   /*
    * test for void FastStringBuffer(int)
    */
   public void testFastStringBufferint() {
      int capacity = 3;
      FastStringBuffer fsb = new FastStringBuffer(capacity);
      assertEquals(capacity, fsb.getCapacity());

      capacity = -1;
      try {
         fsb = new FastStringBuffer(capacity);
         fail("Expected IllegalArgumentException");
      } catch (IllegalArgumentException iae) {/* as expected */}

      try {
         fsb = new FastStringBuffer(null);
         fail("Expected IllegalArgumentException");
      } catch (IllegalArgumentException iae) {/* as expected */}
   }

   /*
    * test for void FastStringBuffer(String)
    */
   public void testFastStringBufferString() {
      String testString = "5 pos";
      FastStringBuffer fsb = new FastStringBuffer(testString);
      assertEquals(testString.length(), fsb.getLength());
      assertEquals(testString, fsb.toString());
   }

   /*
    * test for void FastStringBuffer(int, String)
    */
   public void testFastStringBufferintString() {
      int capacity = 5;
      String testString = "5 pos";
      FastStringBuffer fsb = new FastStringBuffer(capacity, testString);
      assertEquals(testString.length(), fsb.getLength());
      assertEquals(testString, fsb.toString());

      try {
         fsb = new FastStringBuffer(3, "more than 3 positions");
         fail("Expected IllegalArgumentException ");
      } catch (IllegalArgumentException iae) {/* as expected */}
      try {
         fsb = new FastStringBuffer(-1, "more than 3 positions");
         fail("Expected IllegalArgumentException ");
      } catch (IllegalArgumentException iae) {/* as expected */}
      try {
         fsb = new FastStringBuffer(1, null);
         fail("Expected IllegalArgumentException ");
      } catch (IllegalArgumentException iae) {/* as expected */}
   }

   /*
    * test for void append(char)
    */
   public void testAppendchar() {
      String testString = "12345";
      FastStringBuffer fsb = new FastStringBuffer(testString);
      assertEquals(5, fsb.getLength());
      fsb.append('a');
      assertEquals(testString + "a", fsb.toString());
      assertEquals((testString+ "a").length(), fsb.getLength());
      fsb.clear();
      fsb.append('s');
      assertEquals("s", fsb.toString());
      assertEquals(1, fsb.getLength());
   }

   /*
    * test for void append(char[])
    */
   public void testAppendcharArray() {
      String testString = "12345";
      FastStringBuffer fsb = new FastStringBuffer("");
      fsb.append(testString.toCharArray());
      assertEquals(5, fsb.getLength());
      assertEquals(testString, fsb.toString());
   }

   /*
    * test for void append(char[], int, int)
    */
   public void testAppendcharArrayintint() {
      String testString = "12345";
      FastStringBuffer fsb = new FastStringBuffer("");
      fsb.append(testString.toCharArray(), 0, 3);
      assertEquals(testString.substring(0,3), fsb.toString());
      assertEquals(3, fsb.getLength());
      fsb.append(testString.toCharArray(), 3, 2);
      assertEquals(testString, fsb.toString());
      assertEquals(5, fsb.getLength());
      fsb.clear();
      try {
         fsb.append(null, 0, 3);
      } catch (IllegalArgumentException iae) {/* as expected */ }
      try {
         fsb.append(testString.toCharArray(), -1, 1);
      } catch (IllegalArgumentException iae) {/* as expected */ }
      try {
         fsb.append(testString.toCharArray(), testString.length(), 1);
      } catch (IllegalArgumentException iae) {/* as expected */ }
      try {
         fsb.append(testString.toCharArray(), 1, -1);
      } catch (IllegalArgumentException iae) {/* as expected */ }
      try {
         fsb.append(testString.toCharArray(), testString.length()-1, testString.length()-1);
      } catch (IllegalArgumentException iae) {/* as expected */ }
      assertEquals(fsb.toString(), "");
      assertEquals(0, fsb.getLength());
   }

   /*
    * test for void append(String)
    */
   public void testAppendString() {
      String testString = "12345";
      FastStringBuffer fsb = new FastStringBuffer(6);
      fsb.append(testString);
      assertEquals(testString, fsb.toString());
      assertEquals(testString.length(), fsb.getLength());
   }

   /*
    * test for void append(byte)
    */
   public void testAppendbyte() {
      byte b = Byte.parseByte("1");
      FastStringBuffer fsb = new FastStringBuffer("abc");
      fsb.append(b);
      assertEquals("abc1", fsb.toString());
      assertEquals(4, fsb.getLength());
   }

   /*
    * test for void append(short)
    */
   public void testAppendshort() {
      short s = 1;
      FastStringBuffer fsb = new FastStringBuffer("abc");
      fsb.append(s);
      assertEquals("abc1", fsb.toString());
      s = 345;
      fsb.append(s);
      assertEquals("abc1345", fsb.toString());
      assertEquals(7, fsb.getLength());
   }

   /*
    * test for void append(int)
    */
   public void testAppendint() {
      int i = 1;
      FastStringBuffer fsb = new FastStringBuffer("abc");
      fsb.append(i);
      assertEquals("abc1", fsb.toString());
      assertEquals(4, fsb.getLength());
      i = 345;
      fsb.append(i);
      assertEquals("abc1345", fsb.toString());
      assertEquals(7, fsb.getLength());
      fsb.clear();
      fsb.append(i);
      assertEquals("345", fsb.toString());
      assertEquals(3, fsb.getLength());
   }

   /*
    * test for void append(long)
    */
   public void testAppendlong() {
      long l = 1;
      FastStringBuffer fsb = new FastStringBuffer("abc");
      fsb.append(l);
      assertEquals("abc1", fsb.toString());
      assertEquals(4, fsb.getLength());
      l = 345;
      fsb.append(l);
      assertEquals("abc1345", fsb.toString());
      assertEquals(7, fsb.getLength());
   }

   /*
    * test for void append(float)
    */
   public void testAppendfloat() {
      float f = 1.1234f;
      FastStringBuffer fsb = new FastStringBuffer("abc");
      fsb.append(f);
      assertEquals("abc" + Float.toString(f), fsb.toString());
      assertEquals(9, fsb.getLength());
   }

   /*
    * test for void append(double)
    */
   public void testAppenddouble() {
      double d = 12345567866666666666661.12348484848493839483498d;
      FastStringBuffer fsb = new FastStringBuffer("abc");
      fsb.append(d);
      assertEquals("abc" + Double.toString(d), fsb.toString());
      assertEquals(("abc" + Double.toString(d)).length(), fsb.getLength());
   }


   public void testGetCapacity() {
      // inital capacity is what we say it must be
      FastStringBuffer fsb = new FastStringBuffer(3);
      assertEquals(3, fsb.getCapacity());
      // capacity must be greater or equal than length
      assertTrue(fsb.getCapacity() > fsb.getLength());
      fsb.append("...en van je hela hola houdt er de moed maar in");
      assertTrue(fsb.getCapacity() > fsb.getLength());
   }

   public void testSetChar() {
      FastStringBuffer fsb = new FastStringBuffer("a c");
      fsb.setChar(1,'b');
      assertEquals("abc", fsb.toString());
      try {
         fsb.setChar(4,'d');
         fail("Expected to get an IndexOutOfBoundsException");
      } catch (IndexOutOfBoundsException ioobe) {/* as expected */}
   }

   public void testFastStringBufferCrop() {
      String original = "Hello there";
      FastStringBuffer buffer = new FastStringBuffer(original);
      assertEquals(original, buffer.toString());
      try {
         buffer.crop(-1);
         fail("Expected IllegalArgumentException.");
         return;
      } catch (IllegalArgumentException exception) {
         // as expected
      }
      assertEquals(original, buffer.toString());

      try {
         buffer.crop(original.length() + 1);
         fail("Expected IllegalArgumentException.");
         return;
      } catch (IllegalArgumentException exception) {
         // as expected
      }
      assertEquals(original, buffer.toString());

      buffer.crop(original.length());
      assertEquals(original, buffer.toString());

      buffer.crop(original.length() - 1);
      assertEquals("Hello ther", buffer.toString());
      buffer.crop(original.length() - 1);
      assertEquals("Hello ther", buffer.toString());

      buffer.crop(0);
      assertEquals("", buffer.toString());
      buffer.crop(0);
      assertEquals("", buffer.toString());

      try {
         buffer.crop(1);
         fail("Expected IllegalArgumentException.");
         return;
      } catch (IllegalArgumentException exception) {
         // as expected
      }
      assertEquals("", buffer.toString());
   }
}
