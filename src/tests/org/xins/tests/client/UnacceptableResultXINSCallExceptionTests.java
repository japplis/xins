/*
 * $Id: UnacceptableResultXINSCallExceptionTests.java,v 1.11 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xins.client.AbstractCAPICallResult;
import org.xins.client.UnacceptableResultXINSCallException;
import org.xins.client.XINSCallResult;

/**
 * Tests for class <code>UnacceptableResultXINSCallException</code>.
 *
 * @version $Revision: 1.11 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class UnacceptableResultXINSCallExceptionTests extends TestCase {

   /**
    * Constructs a new <code>UnacceptableResultXINSCallExceptionTests</code>
    * test suite with the specified name. The name will be passed to the
    * superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public UnacceptableResultXINSCallExceptionTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(UnacceptableResultXINSCallExceptionTests.class);
   }

   /**
    * Tests the behaviour of the
    * <code>UnacceptableResultXINSCallException</code> class.
    *
    * @throws Exception
    *    if an unexpected exception is thrown.
    */
   public void testUnacceptableResultXINSCallException() throws Exception {
      try {
         new UnacceptableResultXINSCallException((XINSCallResult) null, null, null);
         fail("Expected UnacceptableResultXINSCallException constructor to throw an IllegalArgumentException if result == null.");
      } catch (IllegalArgumentException ex) {
         // as expected
      }

      try {
         new UnacceptableResultXINSCallException((AbstractCAPICallResult) null, null, null);
         fail("Expected UnacceptableResultXINSCallException constructor to throw an IllegalArgumentException if result == null.");
      } catch (IllegalArgumentException ex) {
         // as expected
      }
   }
}
