/*
 * $Id: ManageableTests.java,v 1.8 2010/10/25 20:36:52 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests.common.manageable;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xins.common.manageable.*;

/**
 * Tests for class <code>Manageable</code>.
 *
 * @version $Revision: 1.8 $ $Date: 2010/10/25 20:36:52 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class ManageableTests extends TestCase {

   /**
    * Constructs a new <code>ManageableTests</code>
    * test suite with the specified name. The name will be passed to the
    * superconstructor.
    *
    * @param name
    *    the name for this test suite.
    */
   public ManageableTests(String name) {
      super(name);
   }

   /**
    * Returns a test suite with all test cases defined by this class.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      return new TestSuite(ManageableTests.class);
   }

   /**
    * Tests the <code>Manageable</code> class.
    */
   public void testManageable() throws Exception {
      TestManageable m = new TestManageable();

      // Test initial state
      assertEquals("Initial Manageable state must be UNUSABLE.", m.UNUSABLE, m.getState());

      // Test state after bootstrapping
      m.bootstrap(null);
      assertEquals("After bootstrapping Manageable state must be BOOTSTRAPPED.", m.BOOTSTRAPPED, m.getState());

      // Test state after initialization
      m.init(null);
      assertEquals("After initialization Manageable state must be USABLE.", m.USABLE, m.getState());

      // Test state after deinitialization
      m.deinit();
      assertEquals("After deinitialization Manageable state must be UNUSABLE.", m.UNUSABLE, m.getState());

      // Test state after 2nd-time bootstrapping
      m.bootstrap(null);
      assertEquals("After bootstrapping Manageable state must be BOOTSTRAPPED.", m.BOOTSTRAPPED, m.getState());

      // Test state after 2nd-time initialization
      m.init(null);
      assertEquals("After initialization Manageable state must be USABLE.", m.USABLE, m.getState());

      // Test state after 2nd-time deinitialization
      m.deinit();
      assertEquals("After deinitialization Manageable state must be UNUSABLE.", m.UNUSABLE, m.getState());

      // Test that bootstrap cannot be called twice in a row
      m.bootstrap(null);
      try {
         m.bootstrap(null);
         fail("Expected Manageable.bootstrap to throw an IllegalStateException when called while in state BOOTSTRAPPED.");
      } catch (IllegalStateException exception) {
         // as expected
      }
      assertEquals("Expected Manageable state to be BOOTSTRAPPED.", m.BOOTSTRAPPED, m.getState());

      // Test state after init was called before bootstrap
      m.deinit();
      try {
         m.init(null);
         fail("Expected Manageable.init to throw IllegalStateException if state is UNUSABLE.");
      } catch (IllegalStateException exception) {
         // as expected
      }
      assertEquals("After Manageable.init failed, state must remain UNUSABLE.", m.UNUSABLE, m.getState());

      // Test state after init failed
      m.bootstrap(null);
      m._failInit = true;
      try {
         m.init(null);
         fail("Expected Manageable.init to throw InitializationException if initImpl throws an Error.");
      } catch (InitializationException exception) {
         // as expected
      }
      assertEquals("After Manageable.init failed, state must remain BOOTSTRAPPED.", m.BOOTSTRAPPED, m.getState());

      // Test state after bootstrap failed
      m.deinit();
      m._failBootstrap = true;
      try {
         m.bootstrap(null);
         fail("Expected Manageable.bootstrap to throw BootstrapException if bootstrapImpl throws an Error.");
      } catch (BootstrapException exception) {
         // as expected
      }
   }

   private static final class TestManageable extends Manageable {

      private boolean _failBootstrap;
      private boolean _failInit;

      protected void bootstrapImpl(Map<String, String> properties) {
         if (_failBootstrap) {
            _failBootstrap = false;
            throw new Error();
         }
      }

      protected void initImpl(Map<String, String> properties) {
         if (_failInit) {
            _failInit = false;
            throw new Error();
         }
      }
   }
}
