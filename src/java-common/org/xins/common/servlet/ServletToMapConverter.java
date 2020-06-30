/*
 * $Id: ServletToMapConverter.java,v 1.1 2010/10/25 20:36:52 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.xins.common.text.ParseException;
import org.xins.common.text.TextUtils;
import org.xins.common.text.URLEncoding;

/**
 * Class used to convert Servlet properties to Map&lt;String, String&gt;.
 *
 * @version $Revision: 1.1 $ $Date: 2010/10/25 20:36:52 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0.0
 */
public class ServletToMapConverter {

   /**
    * Returns a Map that contains the initialization properties from a <code>ServletConfig</code> object.
    */
   public static Map<String, String> servletConfigToMap(ServletConfig configuration) {
      Map<String, String> settings = new HashMap<String, String>();
      Enumeration enuParameterNames = configuration.getInitParameterNames();
      while (enuParameterNames.hasMoreElements()) {
         String name = (String) enuParameterNames.nextElement();
         String value = configuration.getInitParameter(name);
         settings.put(name, value);
      }
      return settings;
   }

   /**
    * Returns a Map that contains the parameters from a <code>ServletRequest</code> object.
    */
   public static Map<String, String> servletRequestToMap(HttpServletRequest request)
         throws ParseException {
      Map<String, String> settings = new HashMap<String, String>();

      // Get the HTTP query string
      String query = request.getQueryString();

      // Short-circuit if the query string is empty
      if (TextUtils.isEmpty(query)) {
         return settings;
      }

      // Parse the parameters in the HTTP query string
      try {
         StringTokenizer st = new StringTokenizer(query, "&");
         while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int equalsPos = token.indexOf('=');
            if (equalsPos != -1) {
               String parameterKey = URLEncoding.decode(token.substring(0, equalsPos));
               String parameterValue = URLEncoding.decode(token.substring(equalsPos + 1));
               settings.put(parameterKey, parameterValue);
            } else {
               settings.put(token, "");
            }
         }

      // URLEncoder.decode(String url, String enc) may throw an UnsupportedEncodingException
      // or an IllegalArgumentException
      } catch (Exception cause) {
         throw new ParseException("Failed to parse HTTP query string.",
                                  cause,
                                  "URL decoding failed.");
      }
      return settings;
   }
}
