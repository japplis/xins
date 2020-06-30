/*
 * $Id: FunctionResultTests.java,v 1.4 2010/10/25 20:36:51 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.server;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xins.server.FunctionResult;

/**
 * Tests for the <code>FunctionResult</code> class.
 *
 * @version $Revision: 1.4 $ $Date: 2010/10/25 20:36:51 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class FunctionResultTests extends TestCase {

   //-------------------------------------------------------------------------
   // Class functions
   //-------------------------------------------------------------------------

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(FunctionResultTests.class);
   }


   //-------------------------------------------------------------------------
   // Class fields
   //-------------------------------------------------------------------------

   //-------------------------------------------------------------------------
   // Constructor
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>FunctionResultTests</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public FunctionResultTests(String name) {
      super(name);
   }


   //-------------------------------------------------------------------------
   // Methods
   //-------------------------------------------------------------------------

   /**
    * Tests the <code>FunctionResult</code> constructor that takes no
    * arguments.
    */
   public void testFunctionResultConstructor1() throws Throwable {
      FunctionResult fr = new FunctionResult();
      assertEquals(null, fr.getErrorCode());
      assertEquals(0,    fr.getParameters().size());
      assertEquals(null, fr.getParameter(""));
      assertEquals(null, fr.getParameter("a"));
   }

   /**
    * Tests the <code>FunctionResult</code> constructor that takes one
    * argument, the error code.
    */
   public void testFunctionResultConstructor2() throws Throwable {

      String         errorCode;
      FunctionResult fr;

      // Argument is null
      errorCode = null;
      fr = new FunctionResult(errorCode);
      assertEquals(errorCode, fr.getErrorCode());
      assertEquals(0,         fr.getParameters().size());
      assertEquals(null,      fr.getParameter(""));
      assertEquals(null,      fr.getParameter("a"));

      // Argument is non-null
      errorCode = "FailedMiserably";
      fr = new FunctionResult(errorCode);
      assertEquals(errorCode, fr.getErrorCode());
      assertEquals(0,         fr.getParameters().size());
      assertEquals(null,      fr.getParameter(""));
      assertEquals(null,      fr.getParameter("a"));
   }

   /**
    * Tests the <code>FunctionResult</code> constructor that takes two
    * arguments, the error code and the parameter set.
    */
   public void testFunctionResultConstructor3() throws Throwable {

      String              errorCode;
      Map<String, String> pr;
      FunctionResult fr;

      // Both arguments null
      errorCode = null;
      pr        = null;
      fr        = new FunctionResult(errorCode, pr);
      assertEquals(errorCode, fr.getErrorCode());
      assertEquals(0,         fr.getParameters().size());
      assertEquals(null,      fr.getParameter(""));
      assertEquals(null,      fr.getParameter("a"));

      // First argument null, second is empty
      errorCode = null;
      pr        = new HashMap<String, String>();
      fr        = new FunctionResult(errorCode, pr);
      assertEquals(errorCode, fr.getErrorCode());
      assertEquals(0,         fr.getParameters().size());
      assertEquals(null,      fr.getParameter(""));
      assertEquals(null,      fr.getParameter("a"));

      // First argument null, second is non-empty
      errorCode = null;
      pr        = new HashMap<String, String>();
      pr.put("x", "1");
      pr.put("y", "2");
      fr        = new FunctionResult(errorCode, pr);
      assertEquals(errorCode, fr.getErrorCode());
      assertEquals(2,         fr.getParameters().size());
      assertEquals(null,      fr.getParameter(""));
      assertEquals(null,      fr.getParameter("a"));
      assertEquals("1",       fr.getParameter("x"));
      assertEquals("2",       fr.getParameter("y"));
      assertEquals(2,         fr.getParameters().size());

      // Second argument null
      errorCode = "FailedMiserably";
      pr        = null;
      fr        = new FunctionResult(errorCode, pr);
      assertEquals(errorCode, fr.getErrorCode());
      assertEquals(0,         fr.getParameters().size());
      assertEquals(null,      fr.getParameter(""));
      assertEquals(null,      fr.getParameter("a"));
   }
}
