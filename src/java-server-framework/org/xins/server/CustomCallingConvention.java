/*
 * $Id: CustomCallingConvention.java,v 1.29 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for calling convention implementations that are not part of the
 * core XINS framework.
 *
 * <p>Extend this class to create your own calling conventions. Make sure you
 * override {@link #matches(HttpServletRequest)}.
 *
 * <p>If your custom calling convention takes XML as input, you are advised to
 * use {@link #parseXMLRequest(HttpServletRequest)} to parse the request.
 *
 * @version $Revision: 1.29 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 */
public abstract class CustomCallingConvention extends CallingConvention {

   /**
    * Constructs a new <code>CustomCallingConvention</code>.
    */
   public CustomCallingConvention() {
      // empty
   }

   /**
    * Checks if the specified request can possibly be handled by this calling
    * convention as a function invocation.
    *
    * <p>Implementations of this method should be optimized for performance,
    * as this method may be called for each incoming request. Also, this
    * method should not have any side-effects except possibly some caching in
    * case there is a match.
    *
    * <p>The default implementation of this method always returns
    * <code>true</code>.
    *
    * <p>If this method throws any exception, the exception is logged as an
    * ignorable exception and <code>false</code> is assumed.
    *
    * <p>This method should only be called by the XINS/Java Server Framework.
    *
    * @param httpRequest
    *    the HTTP request to investigate, never <code>null</code>.
    *
    * @return
    *    <code>true</code> if this calling convention is <em>possibly</em>
    *    able to handle this request, or <code>false</code> if it is
    *    <em>definitely</em> not able to handle this request.
    *
    * @throws Exception
    *    if analysis of the request causes an exception; in this case
    *    <code>false</code> will be assumed by the framework.
    *
    * @since XINS 1.4.0
    */
   protected boolean matches(HttpServletRequest httpRequest)
   throws Exception {
      return true;
   }

}
