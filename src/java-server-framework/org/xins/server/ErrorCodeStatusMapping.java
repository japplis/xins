/*
 * $Id: ErrorCodeStatusMapping.java,v 1.2 2013/01/18 10:41:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import static org.xins.server.DefaultResultCodes.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Maps standard error code to HTTP status code.
 *
 * @version $Revision: 1.2 $ $Date: 2013/01/18 10:41:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.1
 */
public class ErrorCodeStatusMapping {

   /**
    * Get the HTTP status code for the given error code
    *
    * @param errorCode
    *    the error code, can be <code>null</code>.
    */
   public static int getStatusCodeForError(String errorCode) {
      if (errorCode == null || errorCode.equals("")) {
         return HttpServletResponse.SC_OK;
      } else if (errorCode.equals(_INVALID_REQUEST.getName())) {
         return HttpServletResponse.SC_BAD_REQUEST;
      } else if (errorCode.equals(_DISABLED_FUNCTION.getName())) {
         return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
      } else if (errorCode.equals(_INVALID_RESPONSE.getName()) || errorCode.equals(_INTERNAL_ERROR.getName())) {
         return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      } else {
         return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      }
   }
}
