/*
 * $Id: Function.java,v 1.161 2012/02/27 22:26:04 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.util.concurrent.atomic.AtomicInteger;
import org.xins.common.FormattedParameters;
import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.manageable.Manageable;

/**
 * Base class for function implementation classes.
 *
 * <p>A function can be enabled or disabled using the
 * {@link #setEnabled(boolean)} method. A function that is enabled can be
 * invoked, while a function that is disabled cannot. By default a function
 * is enabled.
 *
 * @version $Revision: 1.161 $ $Date: 2012/02/27 22:26:04 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public abstract class Function extends Manageable {

   /**
    * Call result to be returned when a function is currently disabled. See
    * {@link #isEnabled()}.
    */
   private static final FunctionResult DISABLED_FUNCTION_RESULT = new FunctionResult("_DisabledFunction");

   /**
    * The API implementation this function is part of. This field cannot be
    * <code>null</code>.
    */
   private final API _api;

   /**
    * The name of this function. This field cannot be <code>null</code>.
    */
   private final String _name;

   /**
    * The version of the specification this function implements. This field
    * cannot be <code>null</code>.
    */
   private final String _version;

   /**
    * Flag that indicates if this function is currently accessible.
    */
   private boolean _enabled;

   /**
    * The total number of calls executed up until now.
    */
   private AtomicInteger _callCount;

   /**
    * Constructs a new <code>Function</code>.
    *
    * @param api
    *    the API to which this function belongs, not <code>null</code>.
    *
    * @param name
    *    the name, not <code>null</code>.
    *
    * @param version
    *    the version of the specification this function implements, not
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>api == null || name == null || version == null</code>.
    */
   protected Function(API api, String name, String version)
   throws IllegalArgumentException {

      // Check arguments
      MandatoryArgumentChecker.check("api",     api,
                                     "name",    name,
                                     "version", version);

      // Initialize fields
      _api       = api;
      _name      = name;
      _version   = version;
      _enabled   = true;
      _callCount = new AtomicInteger();

      // Notify the API that a Function has been added
      _api.functionAdded(this);
   }

   /**
    * Returns the API that contains this function.
    *
    * @return
    *    the {@link API}, not <code>null</code>.
    */
   public final API getAPI() {
      return _api;
   }

   /**
    * Returns the name of this function.
    *
    * @return
    *    the name, not <code>null</code>.
    *
    * @since XINS 1.5.0.
    */
   public final String getName() {
      return _name;
   }

   /**
    * Returns the specification version for this function.
    *
    * @return
    *    the version, not <code>null</code>.
    */
   final String getVersion() {
      return _version;
   }

   /**
    * Checks if this function is currently accessible.
    *
    * @return
    *    <code>true</code> if this function is currently accessible,
    *    <code>false</code> otherwise.
    *
    * @see #setEnabled(boolean)
    */
   public final boolean isEnabled() {
      return _enabled;
   }

   /**
    * Sets if this function is currently accessible.
    *
    * @param enabled
    *    <code>true</code> if this function should be accessible,
    *    <code>false</code> if not.
    *
    * @see #isEnabled()
    */
   public final void setEnabled(boolean enabled) {
      _enabled = enabled;
   }

   /**
    * Handles a call to this function (wrapper method). This method will call
    * {@link #handleCall(CallContext context)}.
    *
    * @param functionRequest
    *    the request, never <code>null</code>.
    *
    * @return
    *    the call result, never <code>null</code>.
    *
    * @throws IllegalStateException
    *    if this object is currently not initialized.
    */
   FunctionResult handleCall(FunctionRequest functionRequest) throws IllegalStateException {

      // Check state first
      assertUsable();

      // Assign a call ID
      int callID = _callCount.incrementAndGet();

      // Check if this function is enabled
      if (! _enabled) {
         return DISABLED_FUNCTION_RESULT;
      }

      // Skipped the function call if asked to
      if (functionRequest.shouldSkipFunctionCall()) {
         Object inParams  = new FormattedParameters(functionRequest.getParameters(), functionRequest.getDataElement());
         Log.log_3516(functionRequest.getFunctionName(), inParams);
         return API.SUCCESSFUL_RESULT;
      }

      // Construct a CallContext object
      CallContext context = new CallContext(functionRequest, this, callID);

      FunctionResult result;
      try {

         // Handle the call
         result = handleCall(context);

         // Make sure the result is valid
         InvalidResponseResult invalidResponse = result.checkOutputParameters();
         if (invalidResponse != null) {
            result = invalidResponse;
            String details = invalidResponse.toString();
            Log.log_3501(functionRequest.getFunctionName(), callID, details);
         }

      } catch (Throwable exception) {
         result = _api.handleFunctionException(functionRequest, callID, exception);
      }

      return result;
   }

   /**
    * Handles a call to this function.
    *
    * @param context
    *    the context for this call, never <code>null</code>.
    *
    * @return
    *    the result of the call, never <code>null</code>.
    *
    * @throws Throwable
    *    if anything goes wrong.
    */
   protected abstract FunctionResult handleCall(CallContext context)
   throws Throwable;
}
