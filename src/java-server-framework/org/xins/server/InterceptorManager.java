package org.xins.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.manageable.BootstrapException;
import org.xins.common.manageable.DeinitializationException;
import org.xins.common.manageable.InitializationException;
import org.xins.common.manageable.Manageable;

/**
 * This class manages interceptors.
 *
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @since XINS 3.0.
 */
class InterceptorManager extends Manageable {

   public final static String DEFAULT_INTERCEPTORS = "org.xins.server.ContextIDInterceptor, " + 
           "org.xins.server.StatisticsInterceptor, " +
           "org.xins.server.TransactionLoggingInterceptor";

   public final static String INTERCEPTORS_PROPERTY_NAME = "org.xins.server.interceptors";
   
   private List<Interceptor> interceptors = new ArrayList<Interceptor>();
   
   private Map<String, String> bootstrapProperties;
   
   private API _api;
   
   @Override
   protected void bootstrapImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {
      bootstrapProperties = properties;
   }

   @Override
   protected void initImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException {
      String interceptorsClasses = properties.get(INTERCEPTORS_PROPERTY_NAME);
      if (interceptorsClasses == null || interceptorsClasses.trim().equals("")) {
         interceptorsClasses = DEFAULT_INTERCEPTORS;
      } else if (interceptorsClasses.startsWith(",")) {
         interceptorsClasses = DEFAULT_INTERCEPTORS + interceptorsClasses;
      }
      
      String[] classNames = interceptorsClasses.split(",");
      List<Interceptor> newInterceptors = new ArrayList<Interceptor>();
      for (String className : classNames) {
         className = className.trim();
         if (!className.equals("")) {
            Interceptor interceptor = getInterceptor(className);
            newInterceptors.add(interceptor);
         }
      }
      interceptors = newInterceptors;
      
      for (Interceptor interceptor : interceptors) {
         interceptor.init(properties);
      }
   }
   
   @Override
   protected void deinitImpl() throws DeinitializationException {
      for (Interceptor interceptor : interceptors) {
         interceptor.deinit();
      }
      interceptors.clear();
   }
   
   private Interceptor getInterceptor(String className) throws InitializationException {
      for (Interceptor interceptor : interceptors) {
         if (interceptor.getClass().getName().equals(className)) {
            return interceptor;
         }
      }
      Interceptor interceptor = createIntercepor(className);
      return interceptor;
   }
   
   private Interceptor createIntercepor(String className) throws InitializationException {
      try {
         Class interceptorClass = Class.forName(className);
         Object interceptorObject = interceptorClass.newInstance();
         if (!Interceptor.class.isInstance(interceptorObject)) {
            throw new InitializationException(className + " is not an interceptor class");
         }
         Interceptor interceptor = (Interceptor) interceptorObject;
         interceptor.setApi(_api);
         interceptor.bootstrap(bootstrapProperties);
         return interceptor;
      } catch (Exception ex) {
         throw new InitializationException("Cannot create interceptor " + className 
                 + ". Reason: " + ex.getMessage());
      }
   }
   
   public HttpServletRequest beginRequest(HttpServletRequest httpRequest) {
      for (Interceptor interceptor : interceptors) {
         httpRequest = interceptor.beginRequest(httpRequest);
      }
      return httpRequest;
   }
   
   public void beforeCallingConvention(HttpServletRequest httpRequest) {
      for (Interceptor interceptor : interceptors) {
         interceptor.beforeCallingConvention(httpRequest);
      }
   }

   public FunctionRequest beforeFunctionCall(HttpServletRequest httpRequest, FunctionRequest functionRequest) {
      FunctionRequest interceptedRequest = functionRequest;
      for (Interceptor interceptor : interceptors) {
         interceptedRequest = interceptor.beforeFunctionCall(httpRequest, interceptedRequest);
      }
      return interceptedRequest;
   }

   public FunctionResult afterFunctionCall(FunctionRequest functionRequest, FunctionResult xinsResult, HttpServletResponse httpResponse) {
      FunctionResult interceptedResult = xinsResult;
      for (Interceptor interceptor : interceptors) {
         interceptedResult = interceptor.afterFunctionCall(functionRequest, interceptedResult, httpResponse);
      }
      return interceptedResult;
   }

   public void afterCallingConvention(FunctionRequest functionRequest, FunctionResult xinsResult, HttpServletResponse httpResponse) {
      for (Interceptor interceptor : interceptors) {
         interceptor.afterCallingConvention(functionRequest, xinsResult, httpResponse);
      }
   }

   public void endRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      for (int i = interceptors.size() - 1; i >= 0; i--) {
         Interceptor interceptor = interceptors.get(i);
         interceptor.endRequest(httpRequest, httpResponse);
      }
   }

   public API getApi() {
      return _api;
   }

   public void setApi(API api) {
      this._api = api;
   }

   public List<Interceptor> getInterceptors() {
      return interceptors;
   }
}
