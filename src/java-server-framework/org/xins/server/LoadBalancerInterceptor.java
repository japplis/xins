package org.xins.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xins.client.UnsuccessfulXINSCallException;
import org.xins.client.XINSCallRequest;
import org.xins.client.XINSCallResult;
import org.xins.client.XINSServiceCaller;

import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.manageable.InitializationException;
import org.xins.common.service.CallException;
import org.xins.common.service.Descriptor;
import org.xins.common.service.DescriptorBuilder;
import org.xins.common.service.TargetDescriptor;

/**
 * Interceptor that redirect requests to other servers.
 *
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class LoadBalancerInterceptor extends Interceptor {

   public final static String FROM_LOAD_BALANCER_PARAMETER = "_fromLoadBalancer";

   public final static String RESULT_FORWARD_PROPERTY = "_forwardResult";

   public final static String LB_PROPERTY_NAME = "org.xins.lb.";

   public final static String LB_FORWARD_STATEGY = "forward";

   public final static String LB_DISPATCH_STATEGY = "dispatch";
   
   protected Map<Pattern, Descriptor> descriptors = new LinkedHashMap<Pattern, Descriptor>();

   protected Map<Pattern, String> strategies = new LinkedHashMap<Pattern, String>();
   
   @Override
   protected void initImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException {
      for (Map.Entry<String, String> property : properties.entrySet()) {
         String key = property.getKey();
         if (key.startsWith(LB_PROPERTY_NAME) && key.endsWith(".target")) {
            Pattern methodNamePattern = Pattern.compile(key);
            Descriptor targetDescriptor = DescriptorBuilder.build(properties, key);
            descriptors.put(methodNamePattern, targetDescriptor);
         } else if (key.startsWith(LB_PROPERTY_NAME) && key.endsWith(".strategy")) {
            Pattern methodNamePattern = Pattern.compile(key);
            String strategy = property.getValue();
            strategies.put(methodNamePattern, strategy);
         }
      }
   }

   @Override
   protected void deinitImpl() {
      descriptors.clear();
      strategies.clear();
   }
   
   @Override
   public FunctionRequest beforeFunctionCall(HttpServletRequest httpRequest, FunctionRequest functionRequest) {
      String methodName = functionRequest.getFunctionName();
      Descriptor descriptor = findDescriptor(methodName);
      String strategy = findStrategy(methodName);
      if (descriptor != null) {
         if (LB_FORWARD_STATEGY.equals(strategy)) {
            try {
               XINSCallResult callResult = forwardRequest(functionRequest, descriptor);
               functionRequest.getBackpack().put(BackpackConstants.SKIP_FUNCTION_CALL, true);
               functionRequest.getBackpack().put(RESULT_FORWARD_PROPERTY, callResult);
            } catch (CallException ex) {
               functionRequest.getBackpack().put(RESULT_FORWARD_PROPERTY, ex);
               // TODO log?
            }
         } else {
            dispatchRequest(functionRequest, descriptor);
         }
      }
      return functionRequest;
   }
   
   private XINSCallResult forwardRequest(FunctionRequest functionRequest, Descriptor descriptor) throws CallException {
      XINSServiceCaller caller = new XINSServiceCaller(descriptor);
      XINSCallRequest request = new XINSCallRequest(functionRequest.getFunctionName());
      Map<String, String> parameters = functionRequest.getParameters();
      parameters.put(FROM_LOAD_BALANCER_PARAMETER, LB_FORWARD_STATEGY);
      request.setParameters(parameters);
      request.setDataSection(request.getDataSection());
      try {
         return caller.call(request);
      } catch (CallException ex) {
         throw ex;
      }
   }
   
   private void dispatchRequest(final FunctionRequest functionRequest, Descriptor descriptor) {
      for (final TargetDescriptor target : descriptor) {
         Thread callThread = new Thread() {
            @Override
            public void run() {
               try {
                  forwardRequest(functionRequest, target);
               } catch (CallException ex) {
                  // TODO log
               }
            }
         };
         callThread.start();
      }
   }
   
   private Descriptor findDescriptor(String functionName) {
      for (Map.Entry<Pattern, Descriptor> descriptorEntry : descriptors.entrySet()) {
         Pattern methodPattern = descriptorEntry.getKey();
         if (methodPattern.matcher(functionName).matches()) {
            return descriptorEntry.getValue();
         }
      }
      return null;
   }
   
   private String findStrategy(String functionName) {
      for (Map.Entry<Pattern, String> stategyEntry : strategies.entrySet()) {
         Pattern methodPattern = stategyEntry.getKey();
         if (methodPattern.matcher(functionName).matches()) {
            return stategyEntry.getValue();
         }
      }
      return null;
   }

   @Override
   public FunctionResult afterFunctionCall(FunctionRequest functionRequest, FunctionResult xinsResult, HttpServletResponse httpResponse) {
      Object forwardResult = functionRequest.getBackpack().get(RESULT_FORWARD_PROPERTY);
      if (forwardResult instanceof XINSCallResult) {
         XINSCallResult callResult  = (XINSCallResult) forwardResult;
         xinsResult.getParameters().putAll(callResult.getParameters());
         xinsResult.getDataElementBuilder().addToDataElement(callResult.getDataElement());
         return xinsResult;
      } else if (forwardResult instanceof UnsuccessfulXINSCallException) {
         UnsuccessfulXINSCallException errorResult  = (UnsuccessfulXINSCallException) forwardResult;
         FunctionResult xinsErrorResult = new FunctionResult(errorResult.getErrorCode(), errorResult.getParameters());
         xinsErrorResult.getDataElementBuilder().addToDataElement(errorResult.getDataElement());
         return xinsErrorResult;
      } else if (forwardResult instanceof Exception) {
         FunctionResult xinsErrorResult = new FunctionResult(DefaultResultCodes._INTERNAL_ERROR.getName());
         return xinsErrorResult;
      }
      return xinsResult;
   }
}
