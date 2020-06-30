/*
 * $Id: API.java,v 1.388 2013/01/18 14:26:45 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletResponse;

import org.xins.common.FormattedParameters;
import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.Utils;
import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.manageable.BootstrapException;
import org.xins.common.manageable.DeinitializationException;
import org.xins.common.manageable.InitializationException;
import org.xins.common.manageable.Manageable;
import org.xins.common.net.IPAddressUtils;
import org.xins.common.spec.APISpec;
import org.xins.common.spec.InvalidSpecificationException;
import org.xins.common.text.ParseException;
import org.w3c.dom.Element;

/**
 * Base class for API implementation classes.
 *
 * @version $Revision: 1.388 $ $Date: 2013/01/18 14:26:45 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:tauseef.rehman@orange-ftgroup.com">Tauseef Rehman</a>
 *
 * @since XINS 1.0.0
 */
public abstract class API extends Manageable {

   /**
    * Successful empty call result.
    */
   static final FunctionResult SUCCESSFUL_RESULT = new FunctionResult();

   /**
    * The runtime (initialization) property that defines the ACL (access
    * control list) rules.
    */
   private static final String ACL_PROPERTY = "org.xins.server.acl";

   /**
    * The name of the bootstrap property that specifies the version of the API.
    */
   static final String API_VERSION_PROPERTY = "org.xins.api.version";

   /**
    * The name of the bootstrap property that specifies the hostname of the
    * machine the package was built on.
    */
   private static final String BUILD_HOST_PROPERTY = "org.xins.api.build.host";

   /**
    * The name of the bootstrap property that specifies the time the package was
    * built.
    */
   private static final String BUILD_TIME_PROPERTY = "org.xins.api.build.time";

   /**
    * The name of the bootstrap property that specifies which version of XINS was
    * used to build the package.
    */
   private static final String BUILD_XINS_VERSION_PROPERTY = "org.xins.api.build.version";

   /**
    * The engine that owns this <code>API</code> object.
    */
   private Engine _engine;

   /**
    * The name of this API. Cannot be <code>null</code> and cannot be an empty
    * string.
    */
   private String _name;

   /**
    * List of registered manageable objects. See {@link #add(Manageable)}.
    *
    * <p />This field is initialized to a non-<code>null</code> value by the
    * constructor.
    */
   private List<Manageable> _manageableObjects;

   /**
    * Map that maps function names to <code>Function</code> instances.
    * Contains all functions associated with this API.
    *
    * <p />This field is initialized to a non-<code>null</code> value by the
    * constructor.
    */
   private Map<String, Function> _functionsByName;

   /**
    * List of all functions. This field cannot be <code>null</code>.
    */
   private List<Function> _functionList;

   /**
    * The build-time settings. This field is initialized exactly once by
    * {@link #bootstrap(Map)}. It can be <code>null</code> before
    * that.
    */
   private Map<String, String> _buildSettings;

   /**
    * The {@link RuntimeProperties} containing the method to verify and access
    * the defined runtime properties.
    */
   private RuntimeProperties _emptyProperties;

   /**
    * The runtime-time settings. This field is initialized by
    * {@link #init(Map)}. It can be <code>null</code> before that.
    */
   private Map<String, String> _runtimeSettings;

   /**
    * Timestamp indicating when this API instance was created.
    */
   private long _startupTimestamp;

   /**
    * Host name for the machine that was used for this build.
    */
   private String _buildHost;

   /**
    * Time stamp that indicates when this build was done.
    */
   private String _buildTime;

   /**
    * XINS version used to build the web application package.
    */
   private String _buildVersion;

   /**
    * The time zone used when generating dates for output.
    */
   private TimeZone _timeZone;

   /**
    * Version of the API.
    */
   private String _apiVersion;

   /**
    * The API specific access rule list.
    */
   private AccessRuleList _apiAccessRuleList;

   /**
    * The general access rule list.
    */
   private AccessRuleList _accessRuleList;

   /**
    * The API specification.
    */
   private APISpec _apiSpecification;

   /**
    * The local IP address.
    */
   private String _localIPAddress;

   /**
    * Mapping from function name to the call ID for all meta-functions. This
    * field is never <code>null</code>.
    */
   private HashMap<String, AtomicInteger> _metaFunctionCallIDs;

   /**
    * Flag indicating that the API is down for maintenance.
    */
   private boolean _apiDisabled;

   /**
    * Constructs a new <code>API</code> object.
    *
    * @param name
    *    the name of the API, cannot be <code>null</code> nor can it be an
    *    empty string.
    *
    * @throws IllegalArgumentException
    *    if <code>name == null
    *          || name.{@link String#length() length()} &lt; 1</code>.
    */
   protected API(String name)
   throws IllegalArgumentException {

      // Check preconditions
      MandatoryArgumentChecker.check("name", name);
      if (name.length() < 1) {
         String message = "name.length() == "
                        + name.length();
         throw new IllegalArgumentException(message);
      }

      // Initialize fields
      _name                = name;
      _startupTimestamp    = System.currentTimeMillis();
      _manageableObjects   = new ArrayList<Manageable>(20);
      _functionsByName     = new HashMap<String, Function>(89);
      _functionList        = new ArrayList<Function>(80);
      _emptyProperties     = new RuntimeProperties();
      _timeZone            = TimeZone.getDefault();
      _localIPAddress      = IPAddressUtils.getLocalHostIPAddress();
      _apiDisabled         = false;

      // Initialize mapping from meta-function to call ID
      _metaFunctionCallIDs = new HashMap<String, AtomicInteger>(89);
      _metaFunctionCallIDs.put("_NoOp",             new AtomicInteger());
      _metaFunctionCallIDs.put("_GetFunctionList",  new AtomicInteger());
      _metaFunctionCallIDs.put("_GetStatistics",    new AtomicInteger());
      _metaFunctionCallIDs.put("_GetVersion",       new AtomicInteger());
      _metaFunctionCallIDs.put("_CheckLinks",       new AtomicInteger());
      _metaFunctionCallIDs.put("_GetSettings",      new AtomicInteger());
      _metaFunctionCallIDs.put("_DisableFunction",  new AtomicInteger());
      _metaFunctionCallIDs.put("_EnableFunction",   new AtomicInteger());
      _metaFunctionCallIDs.put("_ResetStatistics",  new AtomicInteger());
      _metaFunctionCallIDs.put("_ReloadProperties", new AtomicInteger());
      _metaFunctionCallIDs.put("_WSDL",             new AtomicInteger());
      _metaFunctionCallIDs.put("_SMD",              new AtomicInteger());
      _metaFunctionCallIDs.put("_DisableAPI",       new AtomicInteger());
      _metaFunctionCallIDs.put("_EnableAPI",        new AtomicInteger());
   }

   /**
    * Gets the name of this API.
    *
    * @return
    *    the name of this API, never <code>null</code> and never an empty
    *    string.
    */
   public final String getName() {
      return _name;
   }

   /**
    * Gets the list of the functions of this API.
    *
    * @return
    *    the functions of this API as a {@link List} of {@link Function} objects, never <code>null</code>.
    *
    * @since XINS 1.5.0.
    */
   public final List<Function> getFunctionList() {
      return _functionList;
   }

   /**
    * Gets the bootstrap properties specified for the API.
    *
    * @return
    *   the bootstrap properties, cannot be <code>null</code>.
    *
    * @since XINS 1.5.0.
    */
   public Map<String, String> getBootstrapProperties() {
      return _buildSettings;
   }

   /**
    * Gets the API runtime properties.
    *
    * @return
    *   the runtime properties, cannot be <code>null</code>.
    */
   Map<String, String> getRuntimeProperties() {
      return _runtimeSettings;
   }

   /**
    * Gets the runtime properties specified in the implementation.
    *
    * @return
    *    the runtime properties for the API, cannot be <code>null</code>.
    */
   public RuntimeProperties getProperties() {

      // This method is overridden by the APIImpl to return the generated
      // RuntimeProperties class which contains the runtime properties.
      return _emptyProperties;
   }

   /**
    * Gets the timestamp that indicates when this <code>API</code> instance
    * was created.
    *
    * @return
    *    the time this instance was constructed, as a number of milliseconds
    *    since the
    *    <a href="http://en.wikipedia.org/wiki/Unix_Epoch">UNIX Epoch</a>.
    */
   public final long getStartupTimestamp() {
      return _startupTimestamp;
   }

   /**
    * Returns the applicable time zone.
    *
    * @return
    *    the time zone, never <code>null</code>.
    */
   public final TimeZone getTimeZone() {
      return _timeZone;
   }

   /**
    * Gets the resource in the WAR file.
    *
    * @param path
    *    the path for the resource, cannot be <code>null</code> and should start with /.
    *
    * @return
    *    the InputStream to use to read this resource or <code>null</code> if
    *    the resource cannot be found.
    *
    * @throws IllegalArgumentException
    *    if <code>path == null</code> or if the path doesn't start with /.
    *
    * @since XINS 2.0.
    */
   public final InputStream getResourceAsStream(String path) throws IllegalArgumentException {
      return _engine.getResourceAsStream(path);
   }

   /**
    * Bootstraps this API (wrapper method). This method calls
    * {@link #bootstrapImpl2(Map)}.
    *
    * @param buildSettings
    *    the build-time configuration properties, not <code>null</code>.
    *
    * @throws IllegalStateException
    *    if this API is currently not bootstraping.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is not given.
    *
    * @throws InvalidPropertyValueException
    *    if a property has an invalid value.
    *
    * @throws BootstrapException
    *    if the bootstrap fails.
    */
   @Override
   protected final void bootstrapImpl(Map<String, String> buildSettings)
   throws IllegalStateException,
          MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {

      // Check state
      Manageable.State state = getState();
      if (state != BOOTSTRAPPING) {
         String message = "State is " + state.getName() + " instead of BOOTSTRAPPING.";
         Utils.logProgrammingError(message);
         throw new IllegalStateException(message);
      }

      // Log the time zone
      String tzShortName = _timeZone.getDisplayName(false, TimeZone.SHORT);
      String tzLongName  = _timeZone.getDisplayName(false, TimeZone.LONG);
      Log.log_3404(tzShortName, tzLongName);

      // Store the build-time settings
      _buildSettings = buildSettings;

      // Get build-time properties
      _apiVersion   = buildSettings.get(API_VERSION_PROPERTY       );
      _buildHost    = buildSettings.get(BUILD_HOST_PROPERTY        );
      _buildTime    = buildSettings.get(BUILD_TIME_PROPERTY        );
      _buildVersion = buildSettings.get(BUILD_XINS_VERSION_PROPERTY);
      Log.log_3212(_buildHost, _buildTime, _buildVersion, _name, _apiVersion);

      // Skip check if build version is not set
      if (_buildVersion == null) {
         // fall through

      // Check if build version identifies a production release of XINS
      } else if (! Library.isProductionRelease(_buildVersion)) {
         Log.log_3228(_buildVersion);
      }

      // Let the subclass perform initialization
      // TODO: What if bootstrapImpl2 throws an unexpected exception?
      bootstrapImpl2(buildSettings);

      // Bootstrap all instances
      int count = _manageableObjects.size();
      for (int i = 0; i < count; i++) {
         Manageable m = _manageableObjects.get(i);
         String className = m.getClass().getName();
         Log.log_3213(_name, className);
         try {
            m.bootstrap(buildSettings);

         // Missing property
         } catch (MissingRequiredPropertyException exception) {
            Log.log_3215(_name, className, exception.getPropertyName(),
                         exception.getDetail());
            throw exception;

         // Invalid property
         } catch (InvalidPropertyValueException exception) {
            Log.log_3216(_name,
                         className,
                         exception.getPropertyName(),
                         exception.getPropertyValue(),
                         exception.getReason());
            throw exception;

         // Catch BootstrapException and any other exceptions not caught
         // by previous catch statements
         } catch (Throwable exception) {

            // Log event
            Log.log_3217(exception, _name, className);

            // Throw a BootstrapException. If necessary, wrap around the
            // caught exception
            if (exception instanceof BootstrapException) {
               throw (BootstrapException) exception;
            } else {
               throw new BootstrapException(exception);
            }
         }
      }

      // Bootstrap all functions
      count = _functionList.size();
      for (int i = 0; i < count; i++) {
         Function f = _functionList.get(i);
         String functionName = f.getName();
         Log.log_3220(_name, functionName);
         try {
            f.bootstrap(buildSettings);

         // Missing required property
         } catch (MissingRequiredPropertyException exception) {
            Log.log_3222(_name, functionName, exception.getPropertyName(),
                         exception.getDetail());
            throw exception;

         // Invalid property value
         } catch (InvalidPropertyValueException exception) {
            Log.log_3223(_name,
                         functionName,
                         exception.getPropertyName(),
                         exception.getPropertyValue(),
                         exception.getReason());
            throw exception;

         // Catch BootstrapException and any other exceptions not caught
         // by previous catch statements
         } catch (Throwable exception) {

            // Log this event
            Log.log_3224(exception, _name, functionName);

            // Throw a BootstrapException. If necessary, wrap around the
            // caught exception
            if (exception instanceof BootstrapException) {
               throw (BootstrapException) exception;
            } else {
               throw new BootstrapException(exception);
            }
         }
      }
   }

   /**
    * Bootstraps this API (implementation method).
    *
    * <p />The implementation of this method in class {@link API} is empty.
    * Custom subclasses can perform any necessary bootstrapping in this
    * class.
    *
    * <p />Note that bootstrapping and initialization are different. Bootstrap
    * includes only the one-time configuration of the API based on the
    * build-time settings, while the initialization
    *
    * <p />The {@link #add(Manageable)} may be called from this method,
    * and from this method <em>only</em>.
    *
    * @param buildSettings
    *    the build-time properties, guaranteed not to be <code>null</code>.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is not given.
    *
    * @throws InvalidPropertyValueException
    *    if a property has an invalid value.
    *
    * @throws BootstrapException
    *    if the bootstrap fails.
    */
   protected void bootstrapImpl2(Map<String, String> buildSettings)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {
      // empty
   }

   /**
    * Stores a reference to the <code>Engine</code> that owns this
    * <code>API</code> object.
    *
    * @param engine
    *    the {@link Engine} instance, should not be <code>null</code>.
    */
   void setEngine(Engine engine) {
      _engine = engine;
   }

   /**
    * Triggers re-initialization of this API. This method is meant to be
    * called by API function implementations when it is anticipated that the
    * API should be re-initialized.
    */
   protected final void reinitializeImpl() {
      _engine.initAPI();
   }

   /**
    * Initializes this API.
    *
    * @param runtimeSettings
    *    the runtime configuration settings, cannot be <code>null</code>.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is missing.
    *
    * @throws InvalidPropertyValueException
    *    if a property has an invalid value.
    *
    * @throws InitializationException
    *    if the initialization failed for some other reason.
    *
    * @throws IllegalStateException
    *    if this API is currently not initializing.
    */
   protected final void initImpl(Map<String, String> runtimeSettings)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException,
          IllegalStateException {

      Log.log_3405(_name);

      // Store runtime settings
      _runtimeSettings = runtimeSettings;

      String propName  = ConfigManager.CONFIG_RELOAD_INTERVAL_PROPERTY;
      String propValue = runtimeSettings.get(propName);
      int interval = ConfigManager.DEFAULT_CONFIG_RELOAD_INTERVAL;
      if (propValue != null && propValue.trim().length() > 0) {
         try {
            interval = Integer.parseInt(propValue);
         } catch (NumberFormatException e) {
            String detail = "Invalid interval. Must be a non-negative integer"
                          + " number (32-bit signed).";
            throw new InvalidPropertyValueException(propName, propValue,
                                                    detail);
         }

         if (interval < 0) {
            throw new InvalidPropertyValueException(propName, propValue,
               "Negative interval not allowed. Use 0 to disable reloading.");
         }
      }

      // Initialize ACL subsystem

      // First with the API specific access rule list
      if (_apiAccessRuleList != null) {
         _apiAccessRuleList.dispose();
      }
      _apiAccessRuleList = createAccessRuleList(runtimeSettings, ACL_PROPERTY + '.' + _name, interval);

      // Then read the generic access rule list
      if (_accessRuleList != null) {
         _accessRuleList.dispose();
      }
      _accessRuleList = createAccessRuleList(runtimeSettings, ACL_PROPERTY, interval);

      // Initialize the RuntimeProperties object.
      getProperties().init(runtimeSettings);

      // Initialize all instances
      int count = _manageableObjects.size();
      for (int i = 0; i < count; i++) {
         Manageable m = _manageableObjects.get(i);
         String className = m.getClass().getName();
         Log.log_3416(_name, className);
         try {
            m.init(runtimeSettings);

         // Missing required property
         } catch (MissingRequiredPropertyException exception) {
            Log.log_3418(_name, className, exception.getPropertyName(),
                         exception.getDetail());
            throw exception;

         // Invalid property value
         } catch (InvalidPropertyValueException exception) {
            Log.log_3419(_name,
                         className,
                         exception.getPropertyName(),
                         exception.getPropertyValue(),
                         exception.getReason());
            throw exception;

         // Catch InitializationException and any other exceptions not caught
         // by previous catch statements
         } catch (Throwable exception) {

            // Log this event
            Log.log_3420(exception, _name, className);
            if (exception instanceof InitializationException) {
               throw (InitializationException) exception;
            } else {
               throw new InitializationException(exception);
            }
         }
      }

      // Initialize all functions
      count = _functionList.size();
      for (int i = 0; i < count; i++) {
         Function f = _functionList.get(i);
         String functionName = f.getName();
         Log.log_3421(_name, functionName);
         try {
            f.init(runtimeSettings);

         // Missing required property
         } catch (MissingRequiredPropertyException exception) {
            Log.log_3423(_name, functionName, exception.getPropertyName(),
                         exception.getDetail());
            throw exception;

         // Invalid property value
         } catch (InvalidPropertyValueException exception) {
            Log.log_3424(_name,
                         functionName,
                         exception.getPropertyName(),
                         exception.getPropertyValue(),
                         exception.getReason());
            throw exception;

         // Catch InitializationException and any other exceptions not caught
         // by previous catch statements
         } catch (Throwable exception) {

            // Log this event
            Log.log_3425(exception, _name, functionName);

            // Throw an InitializationException. If necessary, wrap around the
            // caught exception
            if (exception instanceof InitializationException) {
               throw (InitializationException) exception;
            } else {
               throw new InitializationException(exception);
            }
         }
      }

      Log.log_3406(_name);
   }

   /**
    * Creates the access rule list for the given property.
    *
    * @param runtimeSettings
    *    the runtime properties, never <code>null</code>.
    *
    * @param aclProperty
    *    the ACL property, never <code>null</code>
    *
    * @param interval
    *    the interval in seconds to chack if the ACL file has changed and
    *    should be reloaded.
    *
    * @return
    *    the access rule list created from the property value, never <code>null</code>.
    *
    * @throws InvalidPropertyValueException
    *    if the value for the property is invalid.
    */
   private AccessRuleList createAccessRuleList(Map<String, String> runtimeSettings,
         String aclProperty, int interval)
   throws InvalidPropertyValueException {
      String acl = runtimeSettings.get(aclProperty);

      // New access control list is empty
      if (acl == null || acl.trim().length() < 1) {
         if (aclProperty.equals(ACL_PROPERTY)) {
            Log.log_3426(aclProperty);
         }
         return AccessRuleList.EMPTY;

      // New access control list is non-empty
      } else {

         // Parse the new ACL
         try {
            AccessRuleList accessRuleList =
               AccessRuleList.parseAccessRuleList(acl, interval);
            int ruleCount = accessRuleList.getRuleCount();
            Log.log_3427(ruleCount);
            return accessRuleList;

         // Parsing failed
         } catch (ParseException exception) {
            String exceptionMessage = exception.getMessage();
            Log.log_3428(aclProperty, acl, exceptionMessage);
            throw new InvalidPropertyValueException(aclProperty,
                                                    acl,
                                                    exceptionMessage);
         }
      }
   }

   /**
    * Adds the specified manageable object. It will not immediately be
    * bootstrapped and initialized.
    *
    * @param m
    *    the manageable object to add, not <code>null</code>.
    *
    * @throws IllegalStateException
    *    if this API is currently not bootstrapping.
    *
    * @throws IllegalArgumentException
    *    if <code>instance == null</code>.
    */
   protected final void add(Manageable m)
   throws IllegalStateException,
          IllegalArgumentException {

      // Check state
      Manageable.State state = getState();
      if (state != BOOTSTRAPPING) {
         String message = "State is "
                        + state
                        + " instead of "
                        + BOOTSTRAPPING
                        + '.';
         Utils.logProgrammingError(message);
         throw new IllegalStateException(message);
      }

      // Check preconditions
      MandatoryArgumentChecker.check("m", m);
      String className = m.getClass().getName();

      Log.log_3218(_name, className);

      // Store the manageable object in the list
      _manageableObjects.add(m);
   }

   /**
    * Performs shutdown of this XINS API. This method will never throw any
    * exception.
    */
   protected final void deinitImpl() {

      // Deinitialize instances
      int count = _manageableObjects.size();
      for (int i = 0; i < count; i++) {
         Manageable m = _manageableObjects.get(i);

         String className = m.getClass().getName();

         Log.log_3603(_name, className);
         try {
            m.deinit();
         } catch (DeinitializationException exception) {
            Log.log_3605(_name, className, exception.getMessage());
         } catch (Throwable exception) {
            Log.log_3606(exception, _name, className);
         }
      }
      _manageableObjects.clear();

      // Deinitialize functions
      count = _functionList.size();
      for (int i = 0; i < count; i++) {
         Function f = _functionList.get(i);

         String functionName = f.getName();

         Log.log_3607(_name, functionName);
         try {
            f.deinit();
         } catch (DeinitializationException exception) {
            Log.log_3609(_name, functionName, exception.getMessage());
         } catch (Throwable exception) {
            Log.log_3610(exception, _name, functionName);
         }
      }
   }

   /**
    * Callback method invoked when a function is constructed.
    *
    * @param function
    *    the function that is added, not <code>null</code>.
    *
    * @throws NullPointerException
    *    if <code>function == null</code>.
    *
    * @throws IllegalStateException
    *    if this API state is incorrect.
    */
   final void functionAdded(Function function)
   throws NullPointerException, IllegalStateException {

      // Check state
      Manageable.State state = getState();
      if (state != UNUSABLE) {
         String message = "State is "
                        + state
                        + " instead of "
                        + UNUSABLE
                        + '.';
         Utils.logProgrammingError(message);
         throw new IllegalStateException(message);
      }

      _functionsByName.put(function.getName(), function);
      _functionList.add(function);
   }

   /**
    * Returns the function with the specified name.
    *
    * @param name
    *    the name of the function, will not be checked if it is
    *    <code>null</code>.
    *
    * @return
    *    the function with the specified name, or <code>null</code> if there
    *    is no match.
    */
   final Function getFunction(String name) {
      return _functionsByName.get(name);
   }

   /**
    * Get the specification of the API.
    *
    * @return
    *    the {@link APISpec} specification object, never <code>null</code>.
    *
    * @throws InvalidSpecificationException
    *    if the specification cannot be found or is invalid.
    *
    * @since XINS 1.3.0
    */
   public final APISpec getAPISpecification()
   throws InvalidSpecificationException {

      if (_apiSpecification == null) {
         String baseURL = _engine.getFileLocation("/WEB-INF/specs/");
         _apiSpecification = new APISpec(getClass(), baseURL);
      }
      return _apiSpecification;
   }

   /**
    * Determines if the specified IP address is allowed to access the
    * specified function, returning a <code>boolean</code> value.
    *
    * <p>This method finds the first matching rule and then returns the
    * <em>allow</em> property of that rule (see
    * {@link AccessRule#isAllowRule()}). If there is no matching rule, then
    * <code>false</code> is returned.
    *
    * @param ip
    *    the IP address, cannot be <code>null</code>.
    *
    * @param functionName
    *    the name of the function, cannot be <code>null</code>.
    *
    * @param conventionName
    *    the name of the calling convention, can be <code>null</code>.
    *
    * @return
    *    <code>true</code> if the request is allowed, <code>false</code> if
    *    the request is denied.
    *
    * @throws IllegalArgumentException
    *    if <code>ip == null || functionName == null</code>.
    *
    * @since XINS 2.1.
    */
   public boolean allow(String ip, String functionName, String conventionName)
   throws IllegalArgumentException {

      // If no property is defined only localhost is allowed
      if (_apiAccessRuleList == AccessRuleList.EMPTY &&
          _accessRuleList == AccessRuleList.EMPTY &&
          (ip.equals("127.0.0.1") || ip.equals("::1") ||
           ip.startsWith("0:0:0:0:0:0:0:1%") || ip.equals(_localIPAddress))) {
         return true;
      }

      // Match an access rule
      Boolean allowed;
      try {

         // First check with the API specific one, then use the generic one.
         allowed = _apiAccessRuleList.isAllowed(ip, functionName, conventionName);
         if (allowed == null) {
            allowed = _accessRuleList.isAllowed(ip, functionName, conventionName);
         }

      // If the IP address cannot be parsed there is a programming error
      // somewhere
      } catch (ParseException exception) {
         String detail = "Malformed IP address: \"" + ip + "\".";
         throw Utils.logProgrammingError(detail, exception);
      }

      // If there is a match, return the allow-indication
      if (allowed != null) {
         return allowed.booleanValue();
      }

      // No matching access rule match, do not allow
      Log.log_3553(ip, functionName, conventionName);
      return false;
   }

   /**
    * Forwards a call to a function. The call will actually be handled by
    * {@link Function#handleCall(FunctionRequest)}.
    *
    * @param functionRequest
    *    the function request, never <code>null</code>.
    *
    * @param cc
    *    the calling convention to use to handle the call, never <code>null</code>.
    *
    * @return
    *    the result of the call, never <code>null</code>.
    *
    * @throws IllegalStateException
    *    if this object is currently not initialized.
    *
    * @throws NullPointerException
    *    if <code>functionRequest == null</code> or <code>cc == null</code>.
    *
    * @throws NoSuchFunctionException
    *    if there is no matching function for the specified request.
    *
    * @throws AccessDeniedException
    *    if access is denied for the specified combination of IP address and
    *    function name.
    */
   final FunctionResult handleCall(FunctionRequest    functionRequest,
                                   CallingConvention  cc)
   throws IllegalStateException,
          NullPointerException,
          NoSuchFunctionException,
          AccessDeniedException {

      // Check state first
      assertUsable();

      // Determine the function name
      String functionName = functionRequest.getFunctionName();

      // Do not handle the call if the API is disabled
      if (_apiDisabled && !"_EnableAPI".equals(functionName)) {
         functionRequest.getBackpack().put(BackpackConstants.STATUS_CODE, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
         return new FunctionResult(DefaultResultCodes._DISABLED_API.getName());
      }

      // Check the access rule list
      String ip = (String) functionRequest.getBackpack().get(BackpackConstants.IP);
      boolean allow = allow(ip, functionName, cc.getConventionName());
      if (! allow) {
         throw new AccessDeniedException(ip, functionName, cc.getConventionName());
      }

      // Handle meta-functions
      FunctionResult result;
      if (functionName.length() > 0 && functionName.charAt(0) == '_') {

         // Determine the call ID
         int callID;
         AtomicInteger counter = _metaFunctionCallIDs.get(functionName);
         if (counter == null) {
            throw new NoSuchFunctionException(functionName);
         } else {
            callID = counter.incrementAndGet();
         }

         // Call the meta-function
         try {
            result = callMetaFunction(functionName, functionRequest);
         } catch (Throwable exception) {
            result = handleFunctionException(functionRequest, callID, exception);
         }

      // Handle normal functions
      } else {
         Function function = getFunction(functionName);
         if (function == null && !functionRequest.shouldSkipFunctionCall())  {
            throw new NoSuchFunctionException(functionName);
         }
         if (function == null) {
            Object inParams  = new FormattedParameters(functionRequest.getParameters(), functionRequest.getDataElement());
            Log.log_3516(functionRequest.getFunctionName(), inParams);
            result = SUCCESSFUL_RESULT;
         } else {
            result = function.handleCall(functionRequest);
         }
      }
      return result;
   }

   /**
    * Handles a call to a meta-function.
    *
    * @param functionName
    *    the name of the meta-function, cannot be <code>null</code> and must
    *    start with the underscore character <code>'_'</code>.
    *
    * @param functionRequest
    *    the function request, never <code>null</code>.
    *
    * @return
    *    the result of the function call, never <code>null</code>.
    *
    * @throws NoSuchFunctionException
    *    if there is no meta-function by the specified name.
    */
   private FunctionResult callMetaFunction(String          functionName,
                                           FunctionRequest functionRequest)
   throws NoSuchFunctionException {

      FunctionResult result;

      // No Operation
      if ("_NoOp".equals(functionName)) {
         result = SUCCESSFUL_RESULT;

      // Retrieve function list
      } else if ("_GetFunctionList".equals(functionName)) {
         result = doGetFunctionList();

      // Get function call quantity and performance statistics
      } else if ("_GetStatistics".equals(functionName)) {

         // Determine value of 'detailed' argument
         String detailedArg = functionRequest.getParameters().get("detailed");
         boolean detailed = !"false".equals(detailedArg);

         // Determine the name of the specific function, if any
         String targetFunction = functionRequest.getParameters().get("targetFunction");

         // Get the statistics
         result = doGetStatistics(detailed, targetFunction);

         // Determine value of 'reset' argument
         String resetArg = functionRequest.getParameters().get("reset");
         boolean reset = "true".equals(resetArg);
         if (reset) {
            doResetStatistics();
         }

      // Get version information
      } else if ("_GetVersion".equals(functionName)) {
         result = doGetVersion();

      // Check links to underlying systems
      } else if ("_CheckLinks".equals(functionName)) {
         result = doCheckLinks();

      // Retrieve configuration settings
      } else if ("_GetSettings".equals(functionName)) {
         result = doGetSettings();

      // Disable a function
      } else if ("_DisableFunction".equals(functionName)) {
         String disabledFunction = functionRequest.getParameters().get("functionName");
         result = doDisableFunction(disabledFunction);

      // Enable a function
      } else if ("_EnableFunction".equals(functionName)) {
         String enabledFunction = functionRequest.getParameters().get("functionName");
         result = doEnableFunction(enabledFunction);

      // Reset the statistics
      } else if ("_ResetStatistics".equals(functionName)) {
         result = doResetStatistics();

      // Reload the runtime properties
      } else if ("_ReloadProperties".equals(functionName)) {
         _engine.reloadPropertiesIfChanged();
         result = SUCCESSFUL_RESULT;

      // Retrieve eggs
      } else if ("_IWantTheEasterEggs".equals(functionName)) {
         result = SUCCESSFUL_RESULT;

      // Return the WSDL description of the API
      } else if ("_WSDL".equals(functionName)) {
         result = SUCCESSFUL_RESULT;

      // Return the SMD (Simple Method Description) description of the API
      } else if ("_SMD".equals(functionName)) {
         result = SUCCESSFUL_RESULT;

      // Disable the API
      } else if ("_DisableAPI".equals(functionName)) {
         _apiDisabled = true;
         result = SUCCESSFUL_RESULT;

      // Enable the API
      } else if ("_EnableAPI".equals(functionName)) {
         _apiDisabled = false;
         result = SUCCESSFUL_RESULT;

      // Meta-function does not exist
      } else {
         throw new NoSuchFunctionException(functionName);
      }

      return result;
   }

   /**
    * Handles an exception caught while a function was executed.
    *
    * @param functionRequest
    *    the request, never <code>null</code>.
    *
    * @param callID
    *    the call identifier, never <code>null</code>.
    *
    * @param exception
    *    the exception caught, never <code>null</code>.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   FunctionResult handleFunctionException(FunctionRequest functionRequest,
                                          int             callID,
                                          Throwable       exception) {

      Log.log_3500(exception, _name, callID);

      // Create a set of parameters for the result
      Map<String, String> resultParams = new HashMap<String, String>();

      // Add the exception class
      String exceptionClass = exception.getClass().getName();
      resultParams.put("_exception.class", exceptionClass);

      // Add the exception message, if any
      String exceptionMessage = exception.getMessage();
      if (exceptionMessage != null) {
         exceptionMessage = exceptionMessage.trim();
         if (exceptionMessage.length() > 0) {
            resultParams.put("_exception.message", exceptionMessage);
         }
      }

      // Add the stack trace, if any
      StringWriter stWriter = new StringWriter(360);
      PrintWriter printWriter = new PrintWriter(stWriter);
      exception.printStackTrace(printWriter);
      String stackTrace = stWriter.toString();
      stackTrace = stackTrace.trim();
      if (stackTrace.length() > 0) {
         resultParams.put("_exception.stacktrace", stackTrace);
      }

      return new FunctionResult("_InternalError", resultParams);
   }

   /**
    * Returns a list of all functions in this API. Per function the name and
    * the version are returned.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doGetFunctionList() {

      FunctionResult builder = new FunctionResult();

      // Loop over all functions
      int count = _functionList.size();
      for (int i = 0; i < count; i++) {

         // Get some details about the function
         Function function = _functionList.get(i);
         String name    = function.getName();
         String version = function.getVersion();
         String enabled = function.isEnabled()
                        ? "true"
                        : "false";

         // Add an element describing the function
         Element functionElem = builder.getDataElementBuilder().createElement("function");
         functionElem.setAttribute("name",    name   );
         functionElem.setAttribute("version", version);
         functionElem.setAttribute("enabled", enabled);
         builder.getDataElement().appendChild(functionElem);
      }

      return builder;
   }

   /**
    * Returns the call statistics for all functions in this API.
    *
    * @param detailed
    *    If <code>true</code>, the unsuccessful result will be returned sorted
    *    per error code. Otherwise the unsuccessful result won't be displayed
    *    by error code.
    *
    * @param functionName
    *    the name of the specific function to return the statistics for,
    *    if <code>null</code>, then the stats for all functions are returned.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doGetStatistics(boolean detailed, String functionName) {

      StatisticsInterceptor statInterceptor = getStatisticInterceptor();
      FunctionResult result = statInterceptor.getStatistics(detailed, functionName);
      return result;
   }

   /**
    * Returns the XINS version.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doGetVersion() {

      FunctionResult builder = new FunctionResult();

      builder.param("java.version",   System.getProperty("java.version"));
      builder.param("xmlenc.version", org.znerd.xmlenc.Library.getVersion());
      builder.param("xins.version",   Library.getVersion());
      builder.param("api.version",    _apiVersion);

      return builder;
   }

   /**
    * Returns the links in linked system components. It uses the
    * {@link CheckLinks} to connect to each link and builds a
    * {@link FunctionResult} which will have the total link count and total
    * link failures.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doCheckLinks() {
      return CheckLinks.checkLinks(getProperties().descriptors());
   }

   /**
    * Returns the settings.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doGetSettings() {

      FunctionResult builder = new FunctionResult();

      // Build settings
      Element build = builder.getDataElementBuilder().createElement("build");
      for (Map.Entry<String, String> names : _buildSettings.entrySet()) {
         String key   = names.getKey();
         String value = names.getValue();

         Element property = builder.getDataElementBuilder().createElement("property");
         property.setAttribute("name", key);
         property.setTextContent(value);
         build.appendChild(property);
      }
      builder.getDataElement().appendChild(build);

      // Runtime settings
      Element runtime = builder.getDataElementBuilder().createElement("runtime");
      for (Map.Entry<String, String> names : _runtimeSettings.entrySet()) {
         String key   = names.getKey();
         String value = names.getValue();

         Element property = builder.getDataElementBuilder().createElement("property");
         property.setAttribute("name", key);
         property.setTextContent(value);
         runtime.appendChild(property);
      }
      builder.getDataElement().appendChild(runtime);

      // System properties
      Properties sysProps;
      try {
         sysProps = System.getProperties();
      } catch (SecurityException ex) {
         Utils.logProgrammingError(ex);
         sysProps = new Properties();
      }

      Enumeration e = sysProps.propertyNames();
      Element system = builder.getDataElementBuilder().createElement("system");
      while (e.hasMoreElements()) {
         String key   = (String) e.nextElement();
         String value = sysProps.getProperty(key);

         if (  key != null &&   key.trim().length() > 0
          && value != null && value.trim().length() > 0) {
            Element property = builder.getDataElementBuilder().createElement("property");
            property.setAttribute("name", key);
            property.setTextContent(value);
            system.appendChild(property);
         }
      }
      builder.getDataElement().appendChild(system);

      return builder;
   }

   /**
    * Enables a function.
    *
    * @param functionName
    *    the name of the function to disable, can be <code>null</code>.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doEnableFunction(String functionName) {

      // Get the name of the function to enable
      if (functionName == null || functionName.length() < 1) {
         InvalidRequestResult invalidRequest = new InvalidRequestResult();
         invalidRequest.addMissingParameter("functionName");
         return invalidRequest;
      }

      // Get the Function object
      Function function = getFunction(functionName);
      if (function == null) {
         return new InvalidRequestResult();
      }

      // Enable or disable the function
      function.setEnabled(true);

      return SUCCESSFUL_RESULT;
   }

   /**
    * Disables a function.
    *
    * @param functionName
    *    the name of the function to disable, can be <code>null</code>.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doDisableFunction(String functionName) {

      // Get the name of the function to disable
      if (functionName == null || functionName.length() < 1) {
         InvalidRequestResult invalidRequest = new InvalidRequestResult();
         invalidRequest.addMissingParameter("functionName");
         return invalidRequest;
      }

      // Get the Function object
      Function function = getFunction(functionName);
      if (function == null) {
         return new InvalidRequestResult();
      }

      // Enable or disable the function
      function.setEnabled(false);

      return SUCCESSFUL_RESULT;
   }

   /**
    * Resets the statistics.
    *
    * @return
    *    the call result, never <code>null</code>.
    */
   private FunctionResult doResetStatistics() {

      StatisticsInterceptor statInterceptor = getStatisticInterceptor();
      FunctionResult result = statInterceptor.resetStatistics();
      return result;
   }

   StatisticsInterceptor getStatisticInterceptor() {
      List<Interceptor> interceptors = _engine.getInterceptorManager().getInterceptors();
      for (Interceptor interceptor : interceptors) {
         if (interceptor instanceof StatisticsInterceptor) {
            return (StatisticsInterceptor) interceptor;
         }
      }
      return null;
   }
}
