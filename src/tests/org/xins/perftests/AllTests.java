/*
 * $Id: AllTests.java,v 1.6 2010/12/11 17:04:40 agoubard Exp $
 */
package org.xins.perftests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Combination of all performance tests.
 *
 * @version $Revision: 1.6 $ $Date: 2010/12/11 17:04:40 $
 * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
 */
public class AllTests extends TestSuite {

   /**
    * Constructs a new <code>AllTests</code> object with the specified name.
    * The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test case.
    */
   public AllTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      // long start = System.currentTimeMillis();
      // long time = System.currentTimeMillis() - start;
      // System.err.println("Time specs " + time);
      TestSuite suite = new TestSuite();
      suite.addTestSuite(AllInOneTests.class);
      suite.addTestSuite(DateConverterTests.class);
      suite.addTestSuite(MandatoryArgumentCheckerTests.class);
      return suite;
   }
}
