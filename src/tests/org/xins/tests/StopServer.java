/*
 * $Id: StopServer.java,v 1.13 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Stops the web server.
 *
 * @version $Revision: 1.13 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class StopServer extends TestCase {

   /**
    * Constructs a new <code>StopServer</code> test suite with
    * the specified name. The name will be passed to the superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public StopServer(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(StopServer.class);
   }

   public void testStopServer() throws Exception {

      System.out.println("Stopping web server.");
      AllTests.HTTP_SERVER.close();
      System.out.println("Web server stopped.");
   }
}
