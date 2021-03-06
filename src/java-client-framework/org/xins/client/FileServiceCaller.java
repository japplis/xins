/*
 * $Id: FileServiceCaller.java,v 1.26 2013/01/23 11:36:37 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import org.xins.common.FormattedParameters;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.Utils;
import org.xins.common.http.HTTPCallConfig;
import org.xins.common.http.HTTPCallException;
import org.xins.common.http.HTTPCallRequest;
import org.xins.common.http.HTTPCallResult;
import org.xins.common.http.HTTPCallResultData;
import org.xins.common.http.HTTPStatusCodeVerifier;
import org.xins.common.http.StatusCodeHTTPCallException;
import org.xins.common.service.CallConfig;
import org.xins.common.service.CallException;
import org.xins.common.service.CallRequest;
import org.xins.common.service.CallResult;
import org.xins.common.service.Descriptor;
import org.xins.common.service.GenericCallException;
import org.xins.common.service.IOCallException;
import org.xins.common.service.ServiceCaller;
import org.xins.common.service.TargetDescriptor;
import org.xins.common.service.UnsupportedProtocolException;
import org.xins.common.servlet.container.LocalServletHandler;
import org.xins.common.servlet.container.XINSServletResponse;
import org.xins.common.text.URLEncoding;

/**
 * Call a XINS API using the internal Servlet container. This service caller
 * doesn't send data over the network but directly invoke the Servlet method.
 *
 * @version $Revision: 1.26 $ $Date: 2013/01/23 11:36:37 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.5.0
 */
class FileServiceCaller extends ServiceCaller {

   /**
    * Charset for the error messages.
    */
   private static final Charset UTF_CHARSET = Charset.forName("UTF-8");

   /**
    * The pool of the loaded XINS APIs. The key is the location of the WAR
    * file, as a {@link TargetDescriptor}, the value is the {@link LocalServletHandler}.
    */
   private static HashMap<TargetDescriptor, LocalServletHandler> SERVLETS = new HashMap<TargetDescriptor, LocalServletHandler>();

   /**
    * Constructs a new <code>HTTPServiceCaller</code> object with the
    * specified descriptor and call configuration.
    *
    * @param descriptor
    *    the descriptor of the service, cannot be <code>null</code>.
    *
    * @param callConfig
    *    the call configuration, or <code>null</code> if a default one should
    *    be used.
    *
    * @throws IllegalArgumentException
    *    if <code>descriptor == null</code>.
    *
    * @throws UnsupportedProtocolException
    *    if <code>descriptor</code> is or contains a {@link TargetDescriptor}
    *    with an unsupported protocol.
    */
   public FileServiceCaller(Descriptor     descriptor,
                            HTTPCallConfig callConfig)
   throws IllegalArgumentException, UnsupportedProtocolException {

      // Call superclass constructor
      super(descriptor, callConfig);
   }

   /**
    * Constructs a new <code>FileServiceCaller</code> object with the
    * specified descriptor and call configuration.
    *
    * @param descriptor
    *    the descriptor of the service, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>descriptor == null</code>.
    *
    * @throws UnsupportedProtocolException
    *    if <code>descriptor</code> is or contains a {@link TargetDescriptor}
    *    with an unsupported protocol.
    */
   public FileServiceCaller(Descriptor descriptor)
   throws IllegalArgumentException, UnsupportedProtocolException {

      this(descriptor, (HTTPCallConfig) null);
   }

   /**
    * Returns a default <code>CallConfig</code> object. This method is called
    * by the <code>ServiceCaller</code> constructor if no
    * <code>CallConfig</code> object was given.
    *
    * <p>The implementation of this method in class {@link FileServiceCaller}
    * returns a standard {@link HTTPCallConfig}.
    *
    * @return
    *    a new {@link HTTPCallConfig} instance, never <code>null</code>.
    */
   protected CallConfig getDefaultCallConfig() {
      return new HTTPCallConfig();
   }

   protected CallResult createCallResult(CallRequest request, TargetDescriptor succeededTarget,
         long duration, List<CallException> exceptions, Object result) throws ClassCastException {

      return new HTTPCallResult((HTTPCallRequest) request,
                                succeededTarget,
                                duration,
                                exceptions,
                                (HTTPCallResultData) result);
   }

   protected boolean isProtocolSupportedImpl(String protocol) {
      return "file".equalsIgnoreCase(protocol);
   }

   /**
    * Executes a request towards the specified target. If the call succeeds,
    * then a {@link HTTPCallResult} object is returned, otherwise a
    * {@link CallException} is thrown.
    *
    * <p>The implementation of this method in class
    * <code>HTTPServiceCaller</code> delegates to
    * {@link #call(HTTPCallRequest,HTTPCallConfig)}.
    *
    * @param request
    *    the call request to be executed, must be an instance of class
    *    {@link HTTPCallRequest}, cannot be <code>null</code>.
    *
    * @param callConfig
    *    the call configuration, never <code>null</code> and should always be
    *    an instance of class {@link HTTPCallConfig}.
    *
    * @param target
    *    the target to call, cannot be <code>null</code>.
    *
    * @return
    *    the result, if and only if the call succeeded, always an instance of
    *    class {@link HTTPCallResult}, never <code>null</code>.
    *
    * @throws ClassCastException
    *    if the specified <code>request</code> object is not <code>null</code>
    *    and not an instance of class {@link HTTPCallRequest}.
    *
    * @throws IllegalArgumentException
    *    if <code>target == null || request == null</code>.
    *
    * @throws CallException
    *    if the call to the specified target failed.
    */
   public Object doCallImpl(CallRequest      request,
                            CallConfig       callConfig,
                            TargetDescriptor target)
   throws ClassCastException, IllegalArgumentException, CallException {

      long start = System.currentTimeMillis();
      long duration;
      LocalServletHandler servletHandler = SERVLETS.get(target);
      if (servletHandler == null) {
         String fileLocation = target.getURL();
         try {
            File warFile = new File(new URI(fileLocation));
            servletHandler = new LocalServletHandler(warFile);
            SERVLETS.put(target, servletHandler);
         } catch (URISyntaxException usex) {
            Log.log_2117(usex);
         } catch (ServletException sex) {
            Log.log_2117(sex);
         }
      }

      Map<String, String> parameters = ((HTTPCallRequest) request).getParameters();

      // Get the parameters for logging
      FormattedParameters params = new FormattedParameters(parameters, null, "", "?", 160);

      // Get URL value
      String url = target.getURL();

      // Loop through the parameters
      StringBuffer query = new StringBuffer(255);
      query.append("/?");
      for (Map.Entry<String, String> parameter : parameters.entrySet()) {

         String key = parameter.getKey();
         String value = parameter.getValue();
         if (value == null) {
            value = "";
         }

         // Add this parameter key/value combination.
         if (key != null) {

            if (query.length() > 2) {
               query.append("&");
            }
            query.append(URLEncoding.encode(key));
            query.append("=");
            query.append(URLEncoding.encode(value));
         }
      }

      XINSServletResponse response;
      try {
         response = servletHandler.query(query.toString());
      } catch (IOException exception) {
         duration = System.currentTimeMillis() - start;
         org.xins.common.Log.log_1109(exception, url, params, duration);
         throw new IOCallException(request, target, duration, exception);

      }

      // Retrieve the data returned from the call
      HTTPCallResultData data;
      try {
         String result = response.getResult();
         byte[] resultData = null;
         if (result != null) {
            resultData = result.getBytes(response.getCharacterEncoding());
         }
         data = new HTTPCallResultDataHandler(response.getStatus(), resultData);
      } catch (UnsupportedEncodingException ueex) {
         throw Utils.logProgrammingError(ueex);
      }

      // Determine the HTTP status code
      int code = data.getStatusCode();

      duration = System.currentTimeMillis() - start;

      HTTPStatusCodeVerifier verifier = ((HTTPCallRequest)request).getStatusCodeVerifier();

      // Status code is considered acceptable
      if (verifier == null || verifier.isAcceptable(code)) {
         org.xins.common.Log.log_1107(url, params, duration, code);

      // Status code is considered unacceptable
      } else {
         org.xins.common.Log.log_1108(url, params, duration, code);

         String details = data.getData() == null ? null : new String(data.getData(), UTF_CHARSET);
         throw new StatusCodeHTTPCallException((HTTPCallRequest) request, target, duration, code, details);
      }

      return new HTTPCallResult((HTTPCallRequest) request, target, duration, null, data);
   }

   /**
    * Performs the specified request towards the HTTP service. If the call
    * succeeds with one of the targets, then a {@link HTTPCallResult} object
    * is returned, that combines the HTTP status code and the data returned.
    * Otherwise, if none of the targets could successfully be called, a
    * {@link CallException} is thrown.
    *
    * @param request
    *    the call request, not <code>null</code>.
    *
    * @param callConfig
    *    the call configuration to use, or <code>null</code>.
    *
    * @return
    *    the result of the call, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>request == null</code>.
    *
    * @throws GenericCallException
    *    if the first call attempt failed due to a generic reason and all the
    *    other call attempts failed as well.
    *
    * @throws HTTPCallException
    *    if the first call attempt failed due to an HTTP-related reason and
    *    all the other call attempts failed as well.
    */
   public HTTPCallResult call(HTTPCallRequest request,
                              HTTPCallConfig  callConfig)
   throws IllegalArgumentException,
          GenericCallException,
          HTTPCallException {

      // Check preconditions
      MandatoryArgumentChecker.check("request", request);

      // Perform the call
      CallResult callResult;
      try {
         callResult = doCall(request, callConfig);

      // Allow GenericCallException, HTTPCallException and Error to proceed,
      // but block other kinds of exceptions and throw an Error instead.
      } catch (GenericCallException exception) {
         throw exception;
      } catch (HTTPCallException exception) {
         throw exception;
      } catch (Exception exception) {
         throw Utils.logProgrammingError(exception);
      }

      return (HTTPCallResult) callResult;
   }

   /**
    * Performs the specified request towards the HTTP service. If the call
    * succeeds with one of the targets, then a {@link HTTPCallResult} object
    * is returned, that combines the HTTP status code and the data returned.
    * Otherwise, if none of the targets could successfully be called, a
    * {@link CallException} is thrown.
    *
    * @param request
    *    the call request, not <code>null</code>.
    *
    * @return
    *    the result of the call, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>request == null</code>.
    *
    * @throws GenericCallException
    *    if the first call attempt failed due to a generic reason and all the
    *    other call attempts failed as well.
    *
    * @throws HTTPCallException
    *    if the first call attempt failed due to an HTTP-related reason and
    *    all the other call attempts failed as well.
    */
   public HTTPCallResult call(HTTPCallRequest request)
   throws IllegalArgumentException,
          GenericCallException,
          HTTPCallException {
      return call(request, (HTTPCallConfig) null);
   }

   /**
    * Container of the data part of an HTTP call result.
    *
    * @version $Revision: 1.26 $ $Date: 2013/01/23 11:36:37 $
    * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
    *
    * @since XINS 1.5.0
    */
   private static final class HTTPCallResultDataHandler
   implements HTTPCallResultData {

      /**
       * The HTTP status code.
       */
      private final int _code;

      /**
       * The data returned.
       */
      private final byte[] _data;

      /**
       * Constructs a new <code>HTTPCallResultDataHandler</code> object.
       *
       * @param code
       *    the HTTP status code.
       *
       * @param data
       *    the data returned from the call, as a set of bytes.
       */
      HTTPCallResultDataHandler(int code, byte[] data) {
         _code = code;
         _data = data;
      }

      /**
       * Returns the HTTP status code.
       *
       * @return
       *    the HTTP status code.
       */
      public int getStatusCode() {
         return _code;
      }

      /**
       * Returns the result data as a byte array. Note that this is not a copy or
       * clone of the internal data structure, but it is a link to the actual
       * data structure itself.
       *
       * @return
       *    a byte array of the result data, never <code>null</code>.
       */
      public byte[] getData() {
         return _data;
      }
   }
}
