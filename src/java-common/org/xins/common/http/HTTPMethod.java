/*
 * $Id: HTTPMethod.java,v 1.16 2011/02/21 21:50:52 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.http;

import java.io.Serializable;

/**
 * HTTP method. Possible values for variable of this class:
 *
 * <ul>
 *    <li>{@link #GET}
 *    <li>{@link #POST}
 * </ul>
 *
 * @version $Revision: 1.16 $ $Date: 2011/02/21 21:50:52 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class HTTPMethod implements Serializable {

   /**
    * The GET method.
    */
   public static final HTTPMethod GET = new HTTPMethod("GET");

   /**
    * The POST method.
    */
   public static final HTTPMethod POST = new HTTPMethod("POST");

   /**
    * The PUT method.
    */
   public static final HTTPMethod PUT = new HTTPMethod("PUT");

   /**
    * The DELETE method.
    */
   public static final HTTPMethod DELETE = new HTTPMethod("DELETE");

   /**
    * The HEAD method.
    */
   public static final HTTPMethod HEAD = new HTTPMethod("HEAD");

   /**
    * The OPTIONS method.
    */
   public static final HTTPMethod OPTIONS = new HTTPMethod("OPTIONS");

   /**
    * The TRACE method.
    */
   public static final HTTPMethod TRACE = new HTTPMethod("TRACE");

   /**
    * The name of this method. For example <code>"GET"</code> or
    * <code>"POST"</code>. This field should never be <code>null</code>.
    */
   private final String _name;

   /**
    * Constructs a new <code>HTTPMethod</code> object with the specified name.
    *
    * @param name
    *    the name of the method, for example <code>"GET"</code> or
    *    <code>"POST"</code>; should not be <code>null</code>.
    */
   private HTTPMethod(String name) {
      _name = name;
   }

   /**
    * Returns a textual representation of this object. The implementation
    * of this method returns the name of this HTTP method, like
    * <code>"GET"</code> or <code>"POST"</code>.
    *
    * @return
    *    the name of this method, e.g. <code>"GET"</code> or
    *    <code>"POST"</code>; never <code>null</code>.
    */
   public String toString() {
      return _name;
   }
}
