/*
 * $Id: AllTests.java,v 1.150 2013/01/23 09:59:40 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xins.common.servlet.container.HTTPServletHandler;

/**
 * Combination of all XINS/Java tests.
 *
 * @version $Revision: 1.150 $ $Date: 2013/01/23 09:59:40 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public class AllTests extends TestSuite {

   private static final int DEFAULT_PORT = 9123;

   private static final boolean RUN_SERVER = true;

   public static HTTPServletHandler HTTP_SERVER;

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
    * Returns the host to use to connect to the server.
    *
    * @return
    *    the host, for example <code>"127.0.0.1"</code>.
    */
   public static final String host() {
      return "127.0.0.1";
   }

   /**
    * Returns the port to use to connect to the server.
    *
    * @return
    *    the port, for example <code>80</code>.
    */
   public static final int port() {
      return DEFAULT_PORT;
   }

   /**
    * Returns the URL to use to connect to the server.
    *
    * @return
    *    the URL to connect to, for example
    *    <code>"http://127.0.0.1:8080/"</code>, never <code>null</code>.
    */
   public static final String url() {
      return "http://" + host() + ":" + port() + "/";
   }

   /**
    * Returns a test suite with all test cases.
    *
    * @return
    *    the test suite, never <code>null</code>.
    */
   public static Test suite() {
      TestSuite suite = new TestSuite();

      // Start the server
      if (RUN_SERVER) {
         suite.addTestSuite(StartServer.class);
      }

      addAllTests(suite);

      // XXX: Perform just a single test
      //suite.addTestSuite(org.xins.tests.server.frontend.PortalAPITests.class);
      //suite.addTest(new org.xins.tests.client.AllInOneAPITests("testAttributeCombo1"));

      // Stop the server
      if (RUN_SERVER) {
         suite.addTestSuite(StopServer.class);
      }

      return suite;
   }

   /**
    * Add all tests.
    *
    * @param suite
    *    the suite to add the test to, cannot be <code>null</code>.
    */
   protected static void addAllTests(TestSuite suite) {
      suite.addTestSuite(org.xins.tests.common.BeanUtilsTests.class);
      suite.addTestSuite(org.xins.tests.common.ExceptionUtilsTests.class);
      suite.addTestSuite(org.xins.tests.common.MandatoryArgumentCheckerTests.class);
      suite.addTestSuite(org.xins.tests.common.UtilsTests.class);

      suite.addTestSuite(org.xins.tests.common.ant.CallXINSTaskTests.class);
      suite.addTestSuite(org.xins.tests.common.ant.HostnameTaskTests.class);
      suite.addTestSuite(org.xins.tests.common.ant.HungarianMapperTests.class);
      suite.addTestSuite(org.xins.tests.common.ant.UppercaseTaskTests.class);

      suite.addTestSuite(org.xins.tests.common.collections.BasicPropertyReaderTests.class);
      suite.addTestSuite(org.xins.tests.common.collections.ChainedMapTests.class);
      suite.addTestSuite(org.xins.tests.common.collections.PropertyReaderUtilsTests.class);
      suite.addTestSuite(org.xins.tests.common.collections.ProtectedListTests.class);
      suite.addTestSuite(org.xins.tests.common.collections.ProtectedPropertyReaderTests.class);
      suite.addTestSuite(org.xins.tests.common.collections.StatsPropertyReaderTests.class);
      suite.addTestSuite(org.xins.tests.common.collections.UniquePropertiesTests.class);

      suite.addTestSuite(org.xins.tests.common.collections.expiry.ExpiryFolderTests.class);
      suite.addTestSuite(org.xins.tests.common.collections.expiry.ExpiryStrategyTests.class);

      suite.addTestSuite(org.xins.tests.common.http.HTTPCallConfigTests.class);
      suite.addTestSuite(org.xins.tests.common.http.HTTPServiceCallerTests.class);

      suite.addTestSuite(org.xins.tests.common.manageable.InitializationExceptionTests.class);
      suite.addTestSuite(org.xins.tests.common.manageable.ManageableTests.class);

      suite.addTestSuite(org.xins.tests.common.net.IPAddressUtilsTests.class);

      suite.addTestSuite(org.xins.tests.common.spec.AttributeComboTests.class);
      suite.addTestSuite(org.xins.tests.common.spec.APITests.class);
      suite.addTestSuite(org.xins.tests.common.spec.DataSectionElementTests.class);
      suite.addTestSuite(org.xins.tests.common.spec.ErrorCodeTests.class);
      suite.addTestSuite(org.xins.tests.common.spec.FunctionTests.class);
      suite.addTestSuite(org.xins.tests.common.spec.ParamComboTests.class);
      suite.addTestSuite(org.xins.tests.common.spec.ParameterTests.class);

      suite.addTestSuite(org.xins.tests.common.service.DescriptorBuilderTests.class);
      suite.addTestSuite(org.xins.tests.common.service.TargetDescriptorTests.class);
      suite.addTestSuite(org.xins.tests.common.service.GroupDescriptorTests.class);
      suite.addTestSuite(org.xins.tests.common.service.UnsupportedProtocolExceptionTests.class);

      suite.addTestSuite(org.xins.tests.common.servlet.ServletRequestPropertyReaderTests.class);
      suite.addTestSuite(org.xins.tests.common.servlet.container.HTTPServletHandlerTests.class);
      suite.addTestSuite(org.xins.tests.common.servlet.container.XINSServletRequestTests.class);

      suite.addTestSuite(org.xins.tests.common.text.DateConverterTests.class);
      suite.addTestSuite(org.xins.tests.common.text.FastStringBufferTest.class);
      suite.addTestSuite(org.xins.tests.common.text.FormatExceptionTests.class);
      suite.addTestSuite(org.xins.tests.common.text.HexConverterTests.class);
      suite.addTestSuite(org.xins.tests.common.text.PatternParserTests.class);
      suite.addTestSuite(org.xins.tests.common.text.ParseExceptionTests.class);
      suite.addTestSuite(org.xins.tests.common.text.SimplePatternParserTests.class);
      suite.addTestSuite(org.xins.tests.common.text.TextUtilsTests.class);
      suite.addTestSuite(org.xins.tests.common.text.URLEncodingTests.class);

      suite.addTestSuite(org.xins.tests.common.types.standard.BooleanTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.DateTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.DescriptorTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.Int8Tests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.Int16Tests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.Int32Tests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.Int64Tests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.Float32Tests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.Float64Tests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.Base64Tests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.HexTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.PropertiesTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.TimestampTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.URLTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.ListTests.class);
      suite.addTestSuite(org.xins.tests.common.types.standard.SetTests.class);

      suite.addTestSuite(org.xins.tests.common.xml.DataElementBuilderTests.class);
      suite.addTestSuite(org.xins.tests.common.xml.ElementTests.class);
      suite.addTestSuite(org.xins.tests.common.xml.ElementBuilderTests.class);
      suite.addTestSuite(org.xins.tests.common.xml.ElementFormatterTests.class);
      suite.addTestSuite(org.xins.tests.common.xml.ElementListTests.class);
      suite.addTestSuite(org.xins.tests.common.xml.ElementParserTests.class);
      suite.addTestSuite(org.xins.tests.common.xml.ElementSerializerTests.class);
      suite.addTestSuite(org.xins.tests.common.xml.SAXParserProviderTests.class);

      suite.addTestSuite(org.xins.tests.client.AllInOneAPITests.class);
      suite.addTestSuite(org.xins.tests.client.CAPIRequestTests.class);
      suite.addTestSuite(org.xins.tests.client.CAPITests.class);
      suite.addTestSuite(org.xins.tests.client.InvalidRequestTests.class);
      suite.addTestSuite(org.xins.tests.client.InvalidResponseTests.class);
      suite.addTestSuite(org.xins.tests.client.UnacceptableResultXINSCallExceptionTests.class);
      suite.addTestSuite(org.xins.tests.client.XINSCallConfigTests.class);
      suite.addTestSuite(org.xins.tests.client.XINSCallRequestTests.class);
      suite.addTestSuite(org.xins.tests.client.XINSCallResultParserTests.class);
      suite.addTestSuite(org.xins.tests.client.XINSServiceCallerTests.class);

      suite.addTestSuite(org.xins.tests.client.async.CallCAPIThreadTests.class);
      suite.addTestSuite(org.xins.tests.client.async.AsynchronousCallTests.class);

      suite.addTestSuite(org.xins.tests.server.AccessRuleListTests.class);
      suite.addTestSuite(org.xins.tests.server.AccessRuleTests.class);
      suite.addTestSuite(org.xins.tests.server.APITests.class);
      suite.addTestSuite(org.xins.tests.server.APIServletTests.class);
      suite.addTestSuite(org.xins.tests.server.CallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.FunctionResultTests.class);
      suite.addTestSuite(org.xins.tests.server.IPFilterTests.class);
      suite.addTestSuite(org.xins.tests.server.JSONCallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.JSONRPCCallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.JSONRPC2CallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.MetaFunctionsTests.class);
      suite.addTestSuite(org.xins.tests.server.StandardCallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.SOAPCallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.SOAPMapCallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.XMLCallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.XMLRPCCallingConventionTests.class);
      suite.addTestSuite(org.xins.tests.server.XSLTCallingConventionTests.class);

      suite.addTestSuite(org.xins.tests.server.frontend.PortalAPITests.class);

      suite.addTestSuite(org.xins.tests.xslt.FirstlineXSLTTestCase.class);
      suite.addTestSuite(org.xins.tests.xslt.JavaXSLTTestCase.class);
      suite.addTestSuite(org.xins.tests.xslt.RcsXSLTTestCase.class);
      suite.addTestSuite(org.xins.tests.xslt.WarningXSLTTestCase.class);
   }
}
