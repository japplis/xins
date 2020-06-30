package org.xins.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xins.common.manageable.Manageable;

/**
 * Intercepter are similar in concept to Servlet filters except for the following points:
 * <ul>
 * <li> More anchor points: before the calling convention, before the function request, 
 *      after the function request and after the calling convention.
 * <li> They are manageable, bootstrap properties, runtime properties, runtime changes.
 * <li> They could be added and removed at runtime
 * </ul>
 * 
 * Example of uses: Statistics, transaction logging, load balancing and fail over.
 *
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class Interceptor extends Manageable {
   
   private API _api;

   public HttpServletRequest beginRequest(HttpServletRequest httpRequest) {
      return httpRequest;
   }

   public void beforeCallingConvention(HttpServletRequest httpRequest) {
   }

   public FunctionRequest beforeFunctionCall(HttpServletRequest httpRequest, FunctionRequest functionRequest) {
      return functionRequest;
   }

   public FunctionResult afterFunctionCall(FunctionRequest functionRequest, FunctionResult xinsResult, HttpServletResponse httpResponse) {
      return xinsResult;
   }

   public void afterCallingConvention(FunctionRequest functionRequest, FunctionResult xinsResult, HttpServletResponse httpResponse) {
   }

   public void endRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
   }

   protected final API getApi() {
      return _api;
   }

   void setApi(API api) {
      this._api = api;
   }
}
