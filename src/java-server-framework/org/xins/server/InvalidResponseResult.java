/*
 * $Id: InvalidResponseResult.java,v 1.28 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

/**
 * Result code that indicates that an output parameter is either missing or
 * invalid.
 *
 * @version $Revision: 1.28 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.0.0
 */
public class InvalidResponseResult extends InvalidMessageResult {

   /**
    * Constructs a new <code>InvalidResponseResult</code> object.
    */
   public InvalidResponseResult() {
      super(DefaultResultCodes._INVALID_RESPONSE.getName());
   }
}
