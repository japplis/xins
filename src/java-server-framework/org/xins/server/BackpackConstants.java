package org.xins.server;

/*
 * Properties names used in the backpack.
 *
 * @version $Revision: 1.4 $ $Date: 2013/01/18 10:41:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 3.0
 */
public interface BackpackConstants {

   /**
    * The key used to store the name of the function in the backpack.
    */
   String FUNCTION_NAME = "_functionName";

   /**
    * The key used to store the request IP address in the backpack.
    * The value is added by XINS Engine, so it's always available in the function.
    */
   String IP = "_ip";

   /**
    * The key used to indicating whether the function should be skipped or not.
    * Default is function not skipped.
    * If set, the value should be a Boolean.
    */
   String SKIP_FUNCTION_CALL = "_skipFunctionCall";

   /**
    * The key used to store the starting time of the call to the function.
    * The value is the number of milliseconds since the UNIX Epoch.
    */
   String START = "_start";

   /**
    * The key used to specify a specific caching time for this call.
    * The value is the number of seconds.
    */
   String CACHE = "_cache";

   /**
    * The key used to specify a specific HTTP status code to return.
    */
   String STATUS_CODE = "_statusCode";
}
