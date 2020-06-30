/*
 * $Id: XINSServletResponse.java,v 1.25 2010/10/25 20:36:52 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.servlet.container;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.xins.common.MandatoryArgumentChecker;

/**
 * This class is an implementation of the HttpServletResponse that can be
 * invoked locally.
 *
 * @version $Revision: 1.25 $ $Date: 2010/10/25 20:36:52 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class XINSServletResponse implements HttpServletResponse {

   /**
    * The content type of the result. Initially <code>null</code>.
    */
   private String _contentType;

   /**
    * The non-negative content length, or a negative number if unset.
    * Initially a negative number.
    */
   private int _contentLength = -99;

   /**
    * The status of the result.
    */
   private int _status;

   /**
    * The encoding of the result. Must default to ISO-8859-1, according to the
    * Java Servlet 2.4 Specification.
    */
   private String _encoding = "ISO-8859-1";

   /**
    * The writer where to write the result.
    */
   private StringWriter _writer;

   /**
    * The headers.
    */
   private Map<String, String> _headers = new HashMap<String, String>();

   /**
    * Creates a new instance of <code>XINSServletResponse</code>.
    */
   public XINSServletResponse() {
   }

   public void addDateHeader(String str, long param) {
      throw new UnsupportedOperationException();
   }

   public void setDateHeader(String str, long param) {
      throw new UnsupportedOperationException();
   }

   public String encodeUrl(String url) {
      return url;
   }

   public String encodeURL(String url) {
      return url;
   }

   public String encodeRedirectUrl(String str) {
      throw new UnsupportedOperationException();
   }

   public String encodeRedirectURL(String str) {
      throw new UnsupportedOperationException();
   }

   public boolean containsHeader(String str) {
      return _headers.get(str) != null;
   }

   public void sendRedirect(String location) {
      setStatus(302);
      setHeader("Location", location);
   }

   /**
    * Sets the content type.
    *
    * @param type
    *    the content type, cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>type == null</code>.
    */
   public void setContentType(String type)
   throws IllegalArgumentException {

      MandatoryArgumentChecker.check("type", type);

      setHeader("Content-Type", type);

      String search = "charset=";
      int i = type.indexOf(search);
      if (i >= 0) {
         _encoding = type.substring(i + search.length());
      }

      _contentType = type;
   }

   public void setStatus(int sc) {
      _status = sc;
   }

   public void sendError(int sc) {
      sendError(sc, null);
   }

   public void setBufferSize(int param) {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns the content length.
    *
    * @return
    *    the (non-negative) content length if set, or a negative value if
    *    unset.
    */
   int getContentLength() {
      return _contentLength;
   }

   public void setContentLength(int param) {
      _contentLength = param;
      setIntHeader("Content-Length", param);
   }

   public void addCookie(Cookie cookie) {
      String cookieHeader = cookie.getName() + "=" + cookie.getValue();
      if (cookie.getPath() != null) {
         cookieHeader += "; path=" + cookie.getPath();
      }
      if (cookie.getDomain() != null) {
         cookieHeader += "; domain=" + cookie.getDomain();
      }
      setHeader("Set-Cookie", cookieHeader);
   }

   public void setLocale(Locale locale) {
      throw new UnsupportedOperationException();
   }

   public void setStatus(int param, String str) {
      throw new UnsupportedOperationException();
   }

   public void setIntHeader(String str, int param) {
      setHeader(str, "" + param);
   }

   public void addIntHeader(String str, int param) {
      setHeader(str, "" + param);
   }

   public void sendError(int sc, String msg) {
      _status = sc;
   }

   public void setHeader(String str, String str1) {
      _headers.put(str, str1);
   }

   public Locale getLocale() {
      throw new UnsupportedOperationException();
   }

   public String getCharacterEncoding() {
      return _encoding;
   }

   public int getBufferSize() {
      throw new UnsupportedOperationException();
   }

   public void flushBuffer() {
      throw new UnsupportedOperationException();
   }

   public void addHeader(String str, String str1) {
      _headers.put(str, str1);
   }

   public ServletOutputStream getOutputStream() {
      throw new UnsupportedOperationException();
   }

   public PrintWriter getWriter() {
      _writer = new StringWriter();
      return new PrintWriter(_writer);
   }

   public boolean isCommitted() {
      throw new UnsupportedOperationException();
   }

   public void reset() {
      throw new UnsupportedOperationException();
   }

   public void resetBuffer() {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the returned message from the servlet.
    *
    * @return
    *    the returned message or <code>null</code> if no message is returned.
    */
   public String getResult() {
      if (_writer == null) {
         return null;
      }
      return _writer.toString();
   }

   /**
    * Gets the status of the returned message.
    *
    * @return
    *    the HTTP status returned.
    */
   public int getStatus() {
      return _status;
   }

   /**
    * Gets the type of the returned content.
    *
    * @return
    *    the content type, can be <code>null</code>.
    *
    * @see #setContentType(String)
    */
   public String getContentType() {
      return _contentType;
   }

   /**
    * Gets the headers to return to the client.
    *
    * @return
    *    the headers, cannot be <code>null</code>.
    *
    * @since XINS 1.3.0
    */
   public Map<String, String> getHeaders() {
      return _headers;
   }
}
