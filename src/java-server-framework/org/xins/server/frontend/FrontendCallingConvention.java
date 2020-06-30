/*
 * $Id: FrontendCallingConvention.java,v 1.79 2013/01/18 14:21:24 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server.frontend;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.io.IOReader;
import org.xins.common.manageable.BootstrapException;
import org.xins.common.manageable.InitializationException;
import org.xins.common.spec.FunctionSpec;
import org.xins.common.text.TextUtils;
import org.xins.common.types.EnumItem;
import org.xins.common.types.standard.Date;
import org.xins.common.types.standard.Timestamp;
import org.xins.common.xml.ElementList;
import org.xins.common.xml.ElementFormatter;

import org.xins.server.API;
import org.xins.server.BackpackConstants;
import org.xins.server.CustomCallingConvention;
import org.xins.server.DefaultResultCodes;
import org.xins.server.Function;
import org.xins.server.FunctionNotSpecifiedException;
import org.xins.server.FunctionRequest;
import org.xins.server.FunctionResult;
import org.xins.server.InvalidRequestException;
import org.xins.server.Log;

/**
 * XINS Front-end Framework calling convention.
 *
 * @version $Revision: 1.79 $ $Date: 2013/01/18 14:21:24 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.5.0.
 */
public class FrontendCallingConvention extends CustomCallingConvention {

   /**
    * The response encoding format.
    */
   private static final String RESPONSE_ENCODING = "ISO-8859-1";

   /**
    * The content type of the HTTP response.
    */
   private static final String XML_CONTENT_TYPE = "text/xml;charset=" + RESPONSE_ENCODING;

   /**
    * The content type of the HTTP response.
    */
   private static final String HTML_CONTENT_TYPE = "text/html;charset=" + RESPONSE_ENCODING;

   /**
    * The name of the runtime property that defines if the templates should be
    * cached. Should be either <code>"true"</code> or <code>"false"</code>.
    * By default the cache is enabled.
    */
   private static final String TEMPLATES_CACHE_PROPERTY = "templates.cache";

   /**
    * Argument used when calling function with no parameters using the reflection API.
    */
   private static final Object[] NO_ARGS = {};

   /**
    * Argument used when finding a function with no parameters using the reflection API.
    */
   private static final Class[] NO_ARGS_CLASS = {};

   /**
    * The API. Never <code>null</code>.
    */
   private final API _api;

   /**
    * Session manager.
    */
   private SessionManager _session;

   /**
    * Location of the XSLT transformation Style Sheet.
    */
   private String _baseXSLTDir;

   /**
    * The XSLT transformer.
    */
   private TransformerFactory _factory;

   /**
    * The default page, cannot be <code>null</code>.
    */
   private String _defaultCommand;

   /**
    * The login page or <code>null</code> if the framework does no have any login page.
    */
   private String _loginPage;

   /**
    * The error page or <code>null</code> if the framework does no have any special error page.
    */
   private String _errorPage;

   /**
    * Redirection map. The key is the command and the value is the redirection
    * command.
    */
   private Map<String, String> _redirectionMap = new LinkedHashMap<String, String>();

   /**
    * Conditional redirection map. The key is the command and the value is the
    * {@link Templates} that will return the name of the redirection command.
    */
   private Map<String, Templates> _conditionalRedirectionMap = new HashMap<String, Templates>();

   /**
    * Flag that indicates whether the templates should be cached. This field
    * is set during initialization.
    */
   private boolean _cacheTemplates;

   /**
    * Cache for the XSLT templates. Never <code>null</code>.
    */
   private Map<String, Templates> _templateCache = new HashMap<String, Templates>();

   /**
    * The template used for the Control command.
    */
   private Templates _templateControl;

   /**
    * The template used for the error page.
    */
   private Templates _templateError;

   /**
    * The list of the real function names for this API.
    */
   private List<String> _functionList = new ArrayList<String>();

   /**
    * Creates a new <code>FrontendCallingConvention</code> instance.
    *
    * @param api
    *    the API, needed for the SOAP messages, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>api == null</code>.
    */
   public FrontendCallingConvention(API api)
   throws IllegalArgumentException {

      // Check arguments
      MandatoryArgumentChecker.check("api", api);

      // Store the API
      _api = api;

      // Get the session manager manageable from the API
      try {
         _session = (SessionManager) api.getClass().getMethod("getSessionManager", NO_ARGS_CLASS).invoke(api, NO_ARGS);
      } catch (Exception ex) {
         Log.log_3700(ex);
      }
   }

   @Override
   protected void bootstrapImpl(Map<String, String> bootstrapProperties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          BootstrapException {
      _loginPage = bootstrapProperties.get("xinsff.login.page");
      _errorPage = bootstrapProperties.get("xinsff.error.page");
      _defaultCommand = bootstrapProperties.get("xinsff.default.command");
      if (_defaultCommand == null) {
         _defaultCommand = "DefaultCommand";
      }

      // Creates the transformer factory
      _factory = TransformerFactory.newInstance();

      initRedirections(bootstrapProperties);
   }

   @Override
   protected void initImpl(Map<String, String> runtimeProperties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException {

      // Get the base directory of the Style Sheet
      String templatesProperty = "templates." + _api.getName() + ".xinsff.source";
      _baseXSLTDir = runtimeProperties.get(templatesProperty);
      if (_baseXSLTDir == null) {
         throw new MissingRequiredPropertyException(templatesProperty);
      }
      Properties systemProps = System.getProperties();
      _baseXSLTDir = TextUtils.replace(_baseXSLTDir, systemProps, "${", "}");
      _baseXSLTDir = _baseXSLTDir.replace('\\', '/');

      // Determine if the template cache should be enabled
      String cacheEnabled = runtimeProperties.get(TEMPLATES_CACHE_PROPERTY);
      initCacheEnabled(cacheEnabled);

      // Gets the functions of the API
      for (Function function : _api.getFunctionList()) {
         _functionList.add(function.getName());
      }
   }

   /**
    * Determines if the template cache should be enabled. If no value is
    * passed, then by default the cache is enabled. An invalid value, however,
    * will trigger an {@link InvalidPropertyValueException}.
    *
    * @param cacheEnabled
    *    the value of the runtime property that specifies whether the cache
    *    should be enabled, can be <code>null</code>.
    *
    * @throws InvalidPropertyValueException
    *    if the value is incorrect.
    */
   private void initCacheEnabled(String cacheEnabled)
   throws InvalidPropertyValueException {

      // By default, the template cache is enabled
      if (TextUtils.isEmpty(cacheEnabled)) {
         _cacheTemplates = true;

      // Trim before comparing with 'true' and 'false'
      } else {
         cacheEnabled = cacheEnabled.trim();
         if ("true".equals(cacheEnabled)) {
            _cacheTemplates = true;
         } else if ("false".equals(cacheEnabled)) {
            _cacheTemplates = false;
         } else {
            throw new InvalidPropertyValueException(TEMPLATES_CACHE_PROPERTY,
               cacheEnabled, "Expected either \"true\" or \"false\".");
         }
      }
   }

   protected boolean matches(HttpServletRequest httpRequest) throws Exception {

      return (httpRequest.getMethod().equalsIgnoreCase("GET") && httpRequest.getParameterMap().isEmpty()) ||
            !TextUtils.isEmpty(httpRequest.getParameter("command"));
   }

   @Override
   protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
   throws InvalidRequestException,
          FunctionNotSpecifiedException {
      Map<String, Object> backpack = new HashMap<String, Object>();
      backpack.put("_httpRequest", httpRequest);

      // Determine function name
      String functionName = httpRequest.getParameter("command");
      if (functionName == null || functionName.equals("")) {
         functionName = _defaultCommand;
      }

      _session.request(httpRequest);

      // Control command has a special behaviour
      if ("Control".equals(functionName)) {
         String action = httpRequest.getParameter("action");
         if ("ReadConfigFile".equals(action)) {
            functionName = "_ReloadProperties";
         }
         backpack.put(BackpackConstants.SKIP_FUNCTION_CALL, true);
         return new FunctionRequest(functionName, null, null, backpack);
      }

      // Append the action to the function name
      String actionName = httpRequest.getParameter("action");
      if (actionName != null && !actionName.equals("") && !actionName.toLowerCase().equals("show")) {
         functionName += TextUtils.firstCharUpper(actionName);
      }

      // Redirect to the login page if not logged in or the function is not implemented
      if (_session.shouldLogIn() ||
            (_redirectionMap.get(functionName) != null && !_functionList.contains(functionName))) {
         backpack.put(BackpackConstants.SKIP_FUNCTION_CALL, true);
         return new FunctionRequest(functionName, null, null, backpack);
      }

      // Determine function parameters
      Map<String, String> functionParams = new HashMap<String, String>();
      Enumeration params = httpRequest.getParameterNames();
      while (params.hasMoreElements()) {
         String name = (String) params.nextElement();

         // TODO remove the next line when no longer needed.
         String realName = getRealParameter(name, functionName);
         String value = httpRequest.getParameter(name);
         functionParams.put(realName, value);
      }

      // Get data section
      String dataSectionValue = httpRequest.getParameter("_data");
      Element dataElement;
      if (dataSectionValue != null && dataSectionValue.length() > 0) {

         // Parse the data section
         try {
            dataElement = ElementFormatter.parse(dataSectionValue);

         // Parsing error
         } catch (SAXException ex) {
            throw new InvalidRequestException("Cannot parse the data section.", ex);
         }
      } else {
         dataElement = null;
      }

      // Construct and return the request object
      return new FunctionRequest(functionName, functionParams, dataElement, backpack);
   }

   @Override
   protected void convertResultImpl(FunctionResult      xinsResult,
                                    HttpServletResponse httpResponse,
                                    Map<String, Object> backpack)
   throws IOException {
      HttpServletRequest httpRequest = (HttpServletRequest) backpack.get("_httpRequest");

      addSessionCookie(httpRequest, httpResponse);

      String mode = httpRequest.getParameter("mode");
      String command = httpRequest.getParameter("command");
      if (command == null || command.equals("")) {
         command = _defaultCommand;
      }
      String action = httpRequest.getParameter("action");
      if (action == null || action.equals("show")) {
         action = "";
      }
      String functionName = command + action;

      _session.result(xinsResult.getErrorCode() == null);

      // Display the XSLT
      if ("template".equalsIgnoreCase(mode)) {
         String xsltSource = getCommandXSLT(command);
         httpResponse.setContentType(XML_CONTENT_TYPE);
         httpResponse.setStatus(HttpServletResponse.SC_OK);
         Writer output = httpResponse.getWriter();
         output.write(xsltSource);
         output.close();
         return;
      }

      // Control command
      if ("Control".equals(command)) {
         xinsResult = control(action);
      }

      Element commandResult = null;
      String commandResultXML = null;
      if (_conditionalRedirectionMap.get(functionName) != null) {
         commandResult = createXMLResult(httpRequest, xinsResult);
         commandResultXML = serializeResult(commandResult);
      }

      // Redirection
      String redirection = getRedirection(xinsResult, command, functionName, commandResultXML);
      if (redirection != null) {
         if ("source".equals(mode)) {
            redirection += "&mode=source";
         }
         httpResponse.sendRedirect(redirection);
         return;
      }

      if (commandResult == null) {
         commandResult = createXMLResult(httpRequest, xinsResult);
         commandResultXML = serializeResult(commandResult);
      }

      if ("source".equalsIgnoreCase(mode)) {
         PrintWriter out = httpResponse.getWriter();
         httpResponse.setContentType(XML_CONTENT_TYPE);
         httpResponse.setStatus(HttpServletResponse.SC_OK);
         out.print(commandResultXML);
         out.close();
      } else if (command != null) {
         /*if (command.endsWith("Show") || command.endsWith("Okay")) {
            command = command.substring(0, command.length() - 4);
         }*/
         String xsltLocation = _baseXSLTDir + command + ".xslt";
         try {
            Templates template = null;
            if ("Control".equals(command) && _templateControl == null) {
               try {
                  StringReader controlXSLT = new StringReader(ControlResult.getControlTemplate());
                  _templateControl = _factory.newTemplates(new StreamSource(controlXSLT));
                  template = _templateControl;
               } catch (TransformerConfigurationException tcex) {
                  Log.log_3701(tcex, "control");
               }
            } else if ("Control".equals(command)) {
               template = _templateControl;
            } else {
               template = getTemplate(xsltLocation);
            }
            Log.log_3704(command);
            String resultHTML = translate(commandResultXML, template);
            String contentType = getContentType(template.getOutputProperties());
            PrintWriter out = httpResponse.getWriter();
            httpResponse.setContentType(contentType);
            Integer backpackStatusCode = (Integer) backpack.get(BackpackConstants.STATUS_CODE);
            if (backpackStatusCode == null) {
               httpResponse.setStatus(HttpServletResponse.SC_OK);
            } else {
               httpResponse.setStatus(backpackStatusCode);
            }
            out.print(resultHTML);
            out.close();
         } catch (TransformerConfigurationException tcex) {
            showError(tcex, httpResponse, httpRequest);
         } catch (TransformerException tex) {
            showError(tex, httpResponse, httpRequest);
         } catch (Exception cause) {
            IOException e = new IOException(cause.getMessage());
            e.initCause(cause);
            throw e;
         }
      }
   }

   /**
    * Adds the session ID to the cookies.
    *
    * @param httpRequest
    *    the HTTP request, cannot be <code>null</code>.
    *
    * @param httpResponse
    *    the HTTP response, cannot be <code>null</code>.
    */
   private void addSessionCookie(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
      Cookie cookie = new Cookie("SessionID", _session.getSessionId());

      // Determine domain for the session cookie
      String host = httpRequest.getHeader("host");
      if (TextUtils.isEmpty(host)) {
         host = httpRequest.getRemoteHost();
      }

      String domain = host;

      // Strip subdomain from the host
      int firstDot = host.indexOf(".");
      if (firstDot != -1 && (firstDot < host.length() - 6 ||
              (firstDot < host.length() - 5 && !host.endsWith(".uk")))) {
         domain = host.substring(firstDot);
      }

      // Strip port if any
      if (domain.indexOf(":") != -1) {
         domain = domain.substring(0, domain.indexOf(":"));
      }

      // Only domains starting with . and containing another . are accepted (RFC 2109)
      if (domain.startsWith(".") && domain.indexOf(".", 1) != -1) {
         cookie.setDomain(domain);
      }
      cookie.setPath("/");

      httpResponse.addCookie(cookie);
   }

   /**
    * Creates the GPF XML from the result returned by the function
    * and in the session.
    *
    * @param httpRequest
    *    the HTTP request, cannot be <code>null</code>.
    *
    * @param xinsResult
    *    the result returned by the function, cannot be <code>null</code>.
    *
    * @return
    *    The XML Element containing the GPF XML result.
    */
   private Element createXMLResult(HttpServletRequest httpRequest, FunctionResult xinsResult) {

      // Create the source element
      Element commandResultElem = ElementFormatter.createMainElement("commandresult");
      Document commandResultDoc = commandResultElem.getOwnerDocument();
      commandResultElem.setAttribute("command", httpRequest.getParameter("command"));
      //builder.setAttribute("description", "Description of " + httpRequest.getParameter("command") + '.');
      Element dataSection = commandResultDoc.createElement("data");

      // Put all the sessions in the XML
      Map sessionProperties = _session.getProperties();
      if (sessionProperties != null) {
         Iterator itSessionProperties = sessionProperties.entrySet().iterator();
         while (itSessionProperties.hasNext()) {
            Map.Entry nextEntry = (Map.Entry) itSessionProperties.next();
            String nextProperty = (String) nextEntry.getKey();
            Object propValue = nextEntry.getValue();
            if (propValue instanceof Element && ((Element)propValue).getTagName().equals("data")) {
               propValue = new ElementList((Element) propValue);
            }
            if (nextProperty.startsWith("_") || propValue == null) {
               // continue
            } else if (propValue instanceof String || propValue instanceof Number || propValue instanceof Boolean ||
                  propValue instanceof EnumItem || propValue instanceof Date.Value || propValue instanceof Timestamp.Value) {
               Element builderParam = commandResultDoc.createElement("parameter");
               builderParam.setAttribute("name", "session." + nextProperty);
               builderParam.setTextContent(propValue.toString());
               commandResultElem.appendChild(builderParam);
            } else if ("org.jdom.Element".equals(propValue.getClass().getName())) {
               //org.jdom.Element propElem = (org.jdom.Element) propValue;
               // TODO dataSection.addChild(Utils.convertFromJDOM(propValue));
            } else if (propValue instanceof Element) {
               dataSection.appendChild(commandResultDoc.importNode((Element) propValue, true));
            } else if (propValue instanceof List) {
               Iterator itPropValue = ((List) propValue).iterator();
               while (itPropValue.hasNext()) {
                  Object nextPropertyInList = itPropValue.next();
                  if (nextPropertyInList == null) {
                     // continue
                  } else if ("org.jdom.Element".equals(nextPropertyInList.getClass().getName())) {
                     //org.jdom.Element propElem = (org.jdom.Element) nextPropertyInList;
                     // TODO dataSection.addChild(Utils.convertFromJDOM(nextPropertyInList));
                  } else if (nextPropertyInList instanceof Element) {
                     dataSection.appendChild((Element) nextPropertyInList);
                  }
               }
            }
         }
      }

      // Store all the input parameters also in the XML
      Enumeration inputParameterNames = httpRequest.getParameterNames();
      while (inputParameterNames.hasMoreElements()) {
         String nextParameter = (String) inputParameterNames.nextElement();
         Element builderParam = commandResultDoc.createElement("parameter");
         builderParam.setAttribute("name", "input." + nextParameter);
         builderParam.setTextContent(httpRequest.getParameter(nextParameter));
         commandResultElem.appendChild(builderParam);
      }

      // Store all the returned parameters also in the XML
      Map<String, String> parameters = xinsResult.getParameters();
      if (parameters != null) {
         Iterator parameterNames = parameters.keySet().iterator();
         while (parameterNames.hasNext()) {
            String nextParameter = (String) parameterNames.next();
            if (!"redirect".equals(nextParameter)) {
               Element builderParam = commandResultDoc.createElement("parameter");
               builderParam.setAttribute("name", nextParameter);
               builderParam.setTextContent(parameters.get(nextParameter));
               commandResultElem.appendChild(builderParam);
            }
         }
      }

      // Store the error code
      if (xinsResult.getErrorCode() != null) {
         if (xinsResult.getErrorCode().equals(DefaultResultCodes._INVALID_REQUEST.getName()) ||
               xinsResult.getErrorCode().equals("InvalidRequest")) {
            addParameter(commandResultElem, "error.type", "FieldError");
            Element errorSection = commandResultDoc.createElement("errorlist");
            for (Element incorrectParamElement : new ElementList(xinsResult.getDataElement())) {
               String elementName = incorrectParamElement.getTagName();

               // param-combo not supported for xins ff
               if (elementName.equals("param-combo")) {
                  ElementList incorrectParamCombo = new ElementList(incorrectParamElement, "param");
                  for (int j = 0; j < incorrectParamCombo.size(); j++) {
                     Element incorrectParamComboElement = (Element) incorrectParamCombo.get(j);
                     String paramName = incorrectParamComboElement.getAttribute("name");
                     Element fieldError = createFieldError(errorSection, elementName, paramName);
                     errorSection.appendChild(fieldError);
                  }
               } else {
                  String paramName = incorrectParamElement.getAttribute("param");
                  Element fieldError = createFieldError(errorSection, elementName, paramName);
                  errorSection.appendChild(fieldError);
               }
            }
            dataSection.appendChild(errorSection);
            commandResultElem.appendChild(dataSection);
            return commandResultElem;
         } else {
            addParameter(commandResultElem, "error.type", "FunctionError");
            addParameter(commandResultElem, "error.code", xinsResult.getErrorCode());
         }
      }

      // Store the data section as it is
      Element resultElement = xinsResult.getDataElement();
      if (resultElement != null) {
         for (Element resultChild : new ElementList(resultElement)) {
            dataSection.appendChild(commandResultDoc.importNode(resultChild, true));
         }
      }
      commandResultElem.appendChild(dataSection);
      return commandResultElem;
   }

   /**
    * Creates a field error based on the error based on the error returned
    * by the function.
    *
    * @param elementName
    *    the name of the error element, cannot be <code>null</code>.
    *
    * @param paramName
    *    the name of the incorrect parameter, cannot be <code>null</code>.
    *
    * @return
    *    the field error element, never <code>null</code>.
    */
   private Element createFieldError(Element parent, String elementName, String paramName) {
      paramName = getOriginalParameter(paramName);
      Element fieldError = parent.getOwnerDocument().createElement("fielderror");
      fieldError.setAttribute("field", paramName);
      if (elementName.equals("missing-param")) {
         fieldError.setAttribute("type", "mand");
      } else if (elementName.equals("invalid-value-for-type")) {
         fieldError.setAttribute("type", "format");
      } else {
         fieldError.setAttribute("type", elementName);
      }
      return fieldError;
   }

   /**
    * Adds a parameter element to the XML result.
    *
    * @param parent
    *    the ElementBuilder where the parameter should be added.
    *
    * @param name
    *    the name of the parameter, cannot be <code>null</code>.
    *
    * @param value
    *    the value of the parameter, cannot be <code>null</code>.
    */
   private void addParameter(Element parent, String name, String value) {
         Element builderParam = parent.getOwnerDocument().createElement("parameter");
         builderParam.setAttribute("name", name);
         builderParam.setTextContent(value);
         parent.appendChild(builderParam);
   }

   /**
    * Returns the String representation of the result.
    *
    * @param commandResult
    *    the Element object containing the result.
    *
    * @return
    *    the String representation of the Element.
    */
   private String serializeResult(Element commandResult) {

      String result = ElementFormatter.format(commandResult);
      return result;
   }

   /**
    * Translates the input using the specified XSLT.
    *
    * @param xmlInput
    *    the XML input that should be transformed, never <code>null</code>.
    *
    * @param template
    *    the template that should be used to transform the input XML, never <code>null</code>.
    *
    * @return
    *    the transformed XML, never <code>null</code>.
    *
    * @throws Exception
    *    if the transformation fails.
    */
   private String translate(String xmlInput, Templates template) throws Exception {
      try {

         // Use the template to create a transformer
         Transformer xformer = template.newTransformer();

         // Prepare the input and output files
         Source source = new StreamSource(new StringReader(xmlInput));

         // Store the result in a StringWriter before sending it.
         Writer buffer = new StringWriter(8192);

         Result result = new StreamResult(buffer);

         // Apply the xsl file to the source file and write the result to the output file
         xformer.transform(source, result);

         return buffer.toString();
      } catch (TransformerConfigurationException tcex) {

         // An error occurred in the XSL file
         Log.log_3701(tcex, "<unknown>");
         throw tcex;
      } catch (TransformerException tex) {

         // An error occurred while applying the XSL file
         // Get location of error in input file
         SourceLocator locator = tex.getLocator();
         if (locator != null) {
            int line = locator.getLineNumber();
            int col = locator.getColumnNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
            Log.log_3703(tex, String.valueOf(line), String.valueOf(col), publicId, systemId);
         } else {
            Log.log_3703(tex, "<unknown>", "<unknown>", "<unknown>", "<unknown>");
         }
         throw tex;
      }
   }

   /**
    * Gets the template to use to transform the XML.
    *
    * @param xsltUrl
    *    the URL of the XSLT file that should be used to transform the input XML,
    *    never <code>null</code>.
    *
    * @return
    *    the template, never <code>null</code>.
    *
    * @throws Exception
    *    if the URL is not found or the XSLT cannot be read correctly.
    */
   private Templates getTemplate(String xsltUrl) throws Exception {

      // Use the factory to create a template containing the xsl file
      // Load the template or get it from the cache.
      Templates template;
      if (_cacheTemplates && _templateCache.containsKey(xsltUrl)) {
         template = _templateCache.get(xsltUrl);
      } else {
         try {
            template = _factory.newTemplates(new StreamSource(xsltUrl));
            if (_cacheTemplates) {
               _templateCache.put(xsltUrl, template);
            }
         } catch (TransformerConfigurationException tcex) {
            Log.log_3701(tcex, xsltUrl);
            throw tcex;
         }
      }
      return template;
   }

   /**
    * Gets the XSLT of the specified command.
    *
    * @param command
    *    the command of which we want the XSLT, never <code>null</code>.
    *
    * @return
    *    the XSLT for the command, never <code>null</code>.
    *
    * @throws IOException
    *    if the XSLT cannot be found.
    */
   private String getCommandXSLT(String command) throws IOException {
      String xsltLocation = _baseXSLTDir + command + ".xslt";
      //httpResponse.sendRedirect(xsltLocation);
      InputStream inputXSLT = new URL(xsltLocation).openStream();
      return IOReader.readFully(inputXSLT);
   }

   /**
    * Gets the MIME type and the character encoding to return for the HTTP response.
    *
    * @param outputProperties
    *    the output properties defined in the XSLT, never <code>null</code>.
    *
    * @return
    *    the content type, never <code>null</code>.
    */
   private String getContentType(Properties outputProperties) {
      String mimeType = outputProperties.getProperty("media-type");
      if (TextUtils.isEmpty(mimeType)) {
         String method = outputProperties.getProperty("method");
         if ("xml".equals(method)) {



            mimeType = "text/xml";
         } else if ("html".equals(method)) {
            mimeType = "text/html";
         } else if ("text".equals(method)) {
            mimeType = "text/plain";
         }
      }
      String encoding = outputProperties.getProperty("encoding");
      if (!TextUtils.isEmpty(mimeType) && !TextUtils.isEmpty(encoding)) {
         mimeType += ";charset=" + encoding;
      }
      if (!TextUtils.isEmpty(mimeType)) {
         return mimeType;
      } else {
         return HTML_CONTENT_TYPE;
      }
   }

   /**
    * Executes the Control command.
    *
    * @param action
    *    the action associated with the Control command, can be <code>null</code>.
    *
    * @return
    *    the function result of the execution of the command
    */
   private FunctionResult control(String action) {
      if ("RemoveSessionProperties".equals(action)) {
         _session.removeProperties();
      } else if ("FlushCommandTemplateCache".equals(action)) {
         _templateCache.clear();
      } else if ("RefreshCommandTemplateCache".equals(action)) {
         _templateCache.clear();
         String xsltLocation;
         Iterator itRealFunctions = _api.getFunctionList().iterator();
         while (itRealFunctions.hasNext()) {
            Function nextFunction = (Function) itRealFunctions.next();
            String nextCommand = nextFunction.getName();
            xsltLocation = _baseXSLTDir + nextCommand + ".xslt";

            try {
               Templates template = _factory.newTemplates(new StreamSource(xsltLocation));
               _templateCache.put(xsltLocation, template);
            } catch (TransformerConfigurationException tcex) {
               // Ignore as if the functionName include the action, it won't match a XSLT file
            }
         }
         Iterator itVirtualFunctions = _redirectionMap.entrySet().iterator();
         while (itVirtualFunctions.hasNext()) {
            Map.Entry nextFunction = (Map.Entry) itVirtualFunctions.next();
            xsltLocation = _baseXSLTDir + nextFunction.getKey() + ".xslt";
            if (nextFunction.getValue().equals("-")) {
               try {
                  Templates template = _factory.newTemplates(new StreamSource(xsltLocation));
                  _templateCache.put(xsltLocation, template);
               } catch (TransformerConfigurationException tcex) {
                  // Ignore as if the functionName include the action, it won't match a XSLT file
               }
            }
         }
      }
      return new ControlResult(_api, _session, _redirectionMap);
   }

   /**
    * Gets the redirection URL.
    *
    * @param xinsResult
    *    the XINS result object that should be converted to an HTTP response,
    *    cannot be <code>null</code>.
    *
    * @param command
    *    the name of the command, cannot be <code>null</code>.
    *
    * @param functionName
    *    the name of the function, cannot be <code>null</code>.
    *
    * @param xmlResult
    *    the result of the call in case of a conditional redirection, can be <code>null</code>.
    *
    * @return
    *    the location where the command should be redirected, or <code>null</code>
    *    if the command should not be redirected.
    */
   private String getRedirection(FunctionResult xinsResult, String command,
         String functionName, String xmlResult) {
      String redirection = xinsResult.getParameter("redirect");
      if (_session.shouldLogIn() || (redirection == null && "NotLoggedIn".equals(xinsResult.getErrorCode()))) {
         redirection = _loginPage + "&targetcommand=" + command;
      }

      if (redirection == null && xinsResult.getErrorCode() == null && _conditionalRedirectionMap.get(functionName) != null) {
         Templates conditionTemplate = _conditionalRedirectionMap.get(functionName);
         try {
            redirection = translate(xmlResult, conditionTemplate);
         } catch (Exception ex) {

            // continue;
         }
      }

      if (redirection == null && xinsResult.getErrorCode() == null) {
         redirection = _redirectionMap.get(functionName);
      }

      // No redirection for this function
      if (redirection == null || redirection.equals("-") ||
            (xinsResult.getErrorCode() != null && "NotLoggedIn".equals(xinsResult.getErrorCode()))) {
         return null;
      }

      // Return the location of the redirection
      if (redirection.equals("/")) {
         redirection = _defaultCommand;
      } else if (!redirection.startsWith("http://") && !redirection.startsWith("https://")) {
         redirection = "?command=" + redirection;
         Map<String, String> parameters = xinsResult.getParameters();
         if (parameters != null) {
            Iterator parameterNames = parameters.keySet().iterator();
            while (parameterNames.hasNext()) {
               String nextParameter = (String) parameterNames.next();
               if (!"redirect".equals(nextParameter)) {
                  redirection += "&" + nextParameter + '=' + parameters.get(nextParameter);
               }
            }
         }
      }
      return redirection;
   }

   /**
    * Initializes the redirections of the commands.
    *
    * @param bootstrapProperties
    *    the bootstrap properties, cannot be <code>null</code>.
    */
   private void initRedirections(Map<String, String> bootstrapProperties) {

      TreeMap conditionalRedirectionProperties = new TreeMap();

      // Get the commands automatically redirected to another one
      Iterator itProperties = bootstrapProperties.keySet().iterator();
      while (itProperties.hasNext()) {
         String nextProp = (String) itProperties.next();
         if (nextProp.startsWith("xinsff.redirect.")) {
            String command = nextProp.substring(16);
            String redirectionPage = bootstrapProperties.get(nextProp);
            // TODO the condition should have the same order as in the XML?
            int conditionalPos = command.indexOf('[');
            if (conditionalPos != -1) {
               conditionalRedirectionProperties.put(command, redirectionPage);
            } else {
               _redirectionMap.put(command, redirectionPage);
            }
         }
      }

      // Create the conditional map
      String startXSLT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
            "<xsl:output omit-xml-declaration=\"yes\" />\n" +
            "<xsl:template match=\"commandresult\">\n" +
            "<xsl:choose>\n";
      Iterator itConditions = conditionalRedirectionProperties.entrySet().iterator();
      String currentCommand = null;
      String xsltText = null;
      while (itConditions.hasNext()) {

         // Parse the line
         Map.Entry nextCondition = (Map.Entry) itConditions.next();
         String nextKey = (String) nextCondition.getKey();
         int conditionPos = nextKey.indexOf('[');
         String command = nextKey.substring(0, conditionPos);
         String condition = nextKey.substring(conditionPos + 1, nextKey.length() - 1);
         String redirectionPage = (String) nextCondition.getValue();

         // Create the template object and store it
         if (currentCommand != null && !currentCommand.equals(command)) {
            finishConditionalTemplate(command, xsltText);
            currentCommand = null;
         }

         // Start a new template as it is a new command
         if (currentCommand == null) {
            xsltText = startXSLT;
            currentCommand = command;
         }

         // Add the condition in the XSL choose
         xsltText += "<xsl:when test=\"" + condition + "\"><xsl:text>" + redirectionPage + "</xsl:text></xsl:when>\n";

         // Close the last condition
         if (!itConditions.hasNext()) {
            finishConditionalTemplate(command, xsltText);
         }
      }
   }

   /**
    * Finishes the creation of the XSLT, creates the {@link Templates} object
    * and stores it in the map.
    *
    * @param command
    *    the command to store, cannot be <code>null</code>.
    *
    * @param currentXSLT
    *    the XSLT created before, cannot be <code>null</code>.
    */
   private void finishConditionalTemplate(String command, String currentXSLT) {
      String defaultRedirection = _redirectionMap.get(command);
      if (defaultRedirection == null) {
         defaultRedirection = "-";
      }
      String xsltText = currentXSLT;
      xsltText += "<xsl:when test=\"not(param[@name='error.type'])\"><xsl:text>" + defaultRedirection + "</xsl:text></xsl:when>\n";
      xsltText += "<xsl:otherwise><xsl:text>-</xsl:text></xsl:otherwise>\n";
      xsltText += "</xsl:choose></xsl:template></xsl:stylesheet>";
      try {
         StringReader conditionXSLT = new StringReader(xsltText);
         Templates conditionTemplate = _factory.newTemplates(new StreamSource(conditionXSLT));
         _conditionalRedirectionMap.put(command, conditionTemplate);
      } catch (TransformerConfigurationException tcex) {
         Log.log_3701(tcex, "conditional redirection for " + command + " command");
      }
   }

   /**
    * Displays the transformation error.
    *
    * @param transformException
    *    The exception that occured during the transformation, cannot be <code>null</code>.
    *
    * @param httpResponse
    *    where to send the response, cannot be <code>null</code>.
    *
    * @param httpRequest
    *    the request of the user, cannot be <code>null</code>.
    *
    * @throws IOException
    *    if this transformation also fails for any reason.
    */
   private void showError(Exception transformException, HttpServletResponse httpResponse,
         HttpServletRequest httpRequest) throws IOException {
      try {
         FunctionResult errorResult = new ErrorResult(transformException, httpRequest);
         if (_templateError == null) {
            if (_errorPage == null) {
               try {
                  StringReader errorXSLT = new StringReader(ErrorResult.getDefaultErrorTemplate());
                  _templateError = _factory.newTemplates(new StreamSource(errorXSLT));
               } catch (TransformerConfigurationException tcex) {
                  Log.log_3701(tcex, "error");
               }
            } else {
               _templateError = getTemplate(_baseXSLTDir + _errorPage + ".xslt");
            }
         }
         Element commandResult = createXMLResult(httpRequest, errorResult);
         String commandResultXML = serializeResult(commandResult);
         String resultHTML = translate(commandResultXML, _templateError);
         String contentType = getContentType(_templateError.getOutputProperties());
         PrintWriter out = httpResponse.getWriter();
         httpResponse.setContentType(contentType);
         httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         out.print(resultHTML);
         out.close();
      } catch (Exception ex) {
         if (ex instanceof IOException) {
            throw (IOException)ex;
         } else if (ex instanceof RuntimeException) {
            throw (RuntimeException)ex;
         } else {
            throw new IOException(ex.getMessage());
         }
      }
   }

   /**
    * Gets the real parameter name.
    *
    * @param receivedParameter
    *    the name of the parameter as received.
    *
    * @param functionName
    *    the name of the function.
    *
    * @return
    *    the name of the parameter as specified in the function.
    *
    * @deprecated
    *    no mapping should be needed and the forms should send directly the correct parameters.
    */
   private String getRealParameter(String receivedParameter, String functionName) {
      String flatParameter = receivedParameter;
      if (receivedParameter.indexOf("_") != -1) {
         flatParameter = TextUtils.removeCharacter('_', receivedParameter);
      }
      try {
         FunctionSpec function = _api.getAPISpecification().getFunction(functionName);
         Set parametersSet = function.getInputParameters().keySet();
         if (parametersSet.contains(receivedParameter)) {
            return receivedParameter;
         }
         Iterator itParameters = parametersSet.iterator();
         while (itParameters.hasNext()) {
            String nextParameterName = (String) itParameters.next();
            if (nextParameterName.equalsIgnoreCase(flatParameter)) {
               return nextParameterName;
            }
         }
      } catch (Exception ex) {

         // No function defined for this call, continue
      }
      return receivedParameter;
   }

   /**
    * Gets the original passed parameter name.
    *
    * @param parameter
    *    the name of the parameter as specified in the function, cannot be <code>null</code>.
    *
    * @return
    *    the name of the parameter as received.
    *
    * @deprecated
    *    no mapping should be needed and the forms should send directly the correct parameters.
    */
   private String getOriginalParameter(String parameter) {
      Map inputs = (Map) _session.getProperty("_inputs");
      Iterator itParameterNames = inputs.keySet().iterator();
      while (itParameterNames.hasNext()) {
         String nextParam = (String) itParameterNames.next();
         String flatParam = TextUtils.removeCharacter('_', nextParam);
         if (parameter.equalsIgnoreCase(flatParam)) {
            return nextParam;
         }
      }
      return parameter;
   }
}
