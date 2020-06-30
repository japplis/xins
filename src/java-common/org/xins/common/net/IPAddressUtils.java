/*
 * $Id: IPAddressUtils.java,v 1.42 2010/10/02 19:18:29 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.xins.common.MandatoryArgumentChecker;
import org.xins.common.text.ParseException;

/**
 * IP address-related utility functions.
 *
 * @version $Revision: 1.42 $ $Date: 2010/10/02 19:18:29 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class IPAddressUtils {

   /**
    * Constructs a new <code>IPAddressUtils</code> object.
    */
   private IPAddressUtils() {
      // empty
   }

   /**
    * Converts an IP address in the form <em>a.b.c.d</em> to an
    * <code>int</code>.
    *
    * @param ip
    *    the IP address, must be in the form:
    *    <em>a.a.a.a.</em>, where <em>a</em> is a number between 0 and 255,
    *    with no leading zeroes; cannot be <code>null</code>.
    *
    * @return
    *    the IP address as an <code>int</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>ip == null</code>.
    *
    * @throws ParseException
    *    if <code>ip</code> cannot be parsed as an IP address.
    */
   public static int ipToInt(String ip)
           throws IllegalArgumentException, ParseException {
      MandatoryArgumentChecker.check("ip", ip);

      int value;

      // Tokenize the string
      StringTokenizer tokenizer = new StringTokenizer(ip, ".", false);

      try {

         // Token 1 must be an IP address part
         value = ipPartToInt(ip, tokenizer.nextToken());

         // Token 3 must be an IP address part
         value <<= 8;
         value += ipPartToInt(ip, tokenizer.nextToken());

         // Token 5 must be an IP address part
         value <<= 8;
         value += ipPartToInt(ip, tokenizer.nextToken());

         // Token 7 must be an IP address part
         value <<= 8;
         value += ipPartToInt(ip, tokenizer.nextToken());

      } catch (NoSuchElementException nsee) {
         throw newParseException(ip);
      }
      if (tokenizer.hasMoreTokens()) {
         throw newParseException(ip);
      }

      return value;
   }

   /**
    * Converts the specified component of an IP address to a number between 0
    * and 255.
    *
    * @param ip
    *    the complete IP address, needed when throwing a
    *    {@link ParseException}, should not be <code>null</code>; if it is,
    *    then the behaviour is undefined.
    *
    * @param part
    *    the part to convert to an <code>int</code> number, should not be
    *    <code>null</code>; if it is, then the behaviour is undefined.
    *
    * @return
    *    the <code>int</code> value of the part, between 0 and 255
    *    (inclusive).
    *
    * @throws ParseException
    *    if the part cannot be parsed.
    */
   private static int ipPartToInt(String ip, String part)
           throws ParseException {

      char[] partString = part.toCharArray();
      int length = partString.length;

      if (length == 1) {
         char c0 = partString[0];
         if (c0 >= '0' && c0 <= '9') {
            return c0 - '0';
         }

      } else if (length == 2) {
         char c0 = partString[0];
         char c1 = partString[1];

         if (c0 >= '1' && c0 <= '9' && c1 >= '0' && c1 <= '9') {
            return ((c0 - '0') * 10) + (c1 - '0');
         }

      } else if (length == 3) {
         char c0 = partString[0];
         char c1 = partString[1];
         char c2 = partString[2];

         if (c0 >= '1' && c0 <= '2'
                 && c1 >= '0' && c1 <= '9'
                 && c2 >= '0' && c2 <= '9') {

            int value = ((c0 - '0') * 100) + ((c1 - '0') * 10) + (c2 - '0');
            if (value <= 255) {
               return value;
            }
         }
      }

      throw newParseException(ip);
   }

   /**
    * Retrieves the localhost IP address.
    *
    * @return
    *    if possible the IP address for localhost, otherwise
    *    the string <code>"127.0.0.1"</code>.
    *
    * @since XINS 1.3.0
    */
   public static String getLocalHostIPAddress() {
      try {
         return InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException exception) {
         return "127.0.0.1";

         // Google App Engine
      } catch (NoClassDefFoundError error) {
         return "127.0.0.1";
      }
   }

   /**
    * Retrieves the localhost host name. This method applies several
    * techniques to attempt to retrieve the localhost host name.
    *
    * @return
    *    if possible the fully qualified host name for localhost, otherwise if
    *    possible the non-qualified host name for the localhost, otherwise
    *    the string <code>"localhost"</code>.
    */
   public static String getLocalHost() {

      String hostname = "localhost";

      try {
         hostname = InetAddress.getLocalHost().getCanonicalHostName();
      } catch (UnknownHostException unknownHostException) {
         String unknownMessage = unknownHostException.getMessage();
         int twoDotPos = unknownMessage.indexOf(':');
         if (twoDotPos != -1) {
            hostname = unknownMessage.substring(0, twoDotPos);
         }
      } catch (SecurityException securityException) {
         // fall through
      } catch (NoClassDefFoundError error) {
         // Google App Engine, fall through
      }

      return hostname;
   }

   /**
    * Constructs a new <code>ParseException</code> for the specified malformed
    * IP address.
    *
    * @param ip
    *    the malformed IP address, not <code>null</code>.
    *
    * @return
    *    the {@link ParseException} to throw.
    */
   private static ParseException newParseException(String ip) {

      // Construct the message for the exception
      String detail = "The string \"" + ip + "\" is not a valid IP address.";

      // Return the exception
      return new ParseException(detail);
   }
}
