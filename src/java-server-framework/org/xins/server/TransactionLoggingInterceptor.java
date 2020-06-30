/*
 * $Id: TransactionLoggingInterceptor.java,v 1.2 2013/01/14 11:14:39 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xins.common.FormattedParameters;
import org.xins.common.text.DateConverter;

/**
 * Logs the transaction.
 *
 * @since xins 3.0
 *
 * @version $Revision: 1.2 $ $Date: 2013/01/14 11:14:39 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class TransactionLoggingInterceptor extends Interceptor {

   /**
    * Contants to indicate that not modified was returned.
    */
   private static final String NOT_MODIFIED = "-- Not Modified --";

   /**
    * Class used to convert dates to String.
    */
   private static final DateConverter DATE_CONVERTER = new DateConverter(true);

   @Override
   public HttpServletRequest beginRequest(HttpServletRequest request) {

      // Log that we have received an HTTP request
      String remoteIP    = request.getRemoteAddr();
      String method      = request.getMethod();
      String path        = request.getRequestURI();
      String queryString = request.getQueryString();
      Log.log_3521(remoteIP, method, path, queryString);
      return request;
   }

   @Override
   public FunctionResult afterFunctionCall(FunctionRequest functionRequest, FunctionResult xinsResult, HttpServletResponse httpResponse) {
      logTransaction(functionRequest, xinsResult);
      return xinsResult;
   }

   /**
    * Logs the specified transaction.
    *
    * @param request
    *    the {@link FunctionRequest}, should not be <code>null</code>.
    *
    * @param result
    *    the {@link FunctionResult}, should not be <code>null</code>.
    *
    * @throws NullPointerException
    *    if <code>request == null || result  == null</code>.
    */
   protected void logTransaction(FunctionRequest request, FunctionResult  result) throws NullPointerException {

      // Determine the name of the function that was requested to be invoked
      String functionName = request.getFunctionName();

      // Determine error code, fallback is a zero character
      String code = result.getErrorCode();
      if (code == null || code.length() < 1) {
         code = "0";
      }

      // Determine start and duration of the call
      long start = (Long) request.getBackpack().get(BackpackConstants.START);
      long duration = System.currentTimeMillis() - start;

      // Prepare for transaction logging
      String serStart = DATE_CONVERTER.format(start);
      Object inParams  = new FormattedParameters(request.getParameters(), request.getDataElement());
      Object outParams = null;
      if (result instanceof NotModifiedResult) {
         outParams = NOT_MODIFIED;
      } else {
         outParams = new FormattedParameters(result.getParameters(), result.getDataElement());
      }
      String ip = (String) request.getBackpack().get(BackpackConstants.IP);

      // Log transaction before returning the result
      Log.log_3540(serStart, ip, functionName, duration, code, inParams, outParams);
      Log.log_3541(serStart, ip, functionName, duration, code);
   }
}
