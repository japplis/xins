/*
 * $Id: FastDataImpl.java,v 1.1 2013/01/22 10:42:30 agoubard Exp $
 */
package com.mycompany.allinone.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.manageable.InitializationException;

/**
 * Implementation of the <code>FastData</code> function.
 *
 * @version $Revision: 1.1 $ $Date: 2013/01/22 10:42:30 $
 * @author John Doe (<a href="mailto:john.doe@mycompany.com">john.doe@mycompany.com</a>)
 */
public final class FastDataImpl extends FastData {

    private long descriptionsTimestamp;

    private Map<Long, String> productDescriptions = new HashMap<Long, String>();

   /**
    * Constructs a new <code>FastDataImpl</code> instance.
    *
    * @param api
    *    the API to which this function belongs, guaranteed to be not
    *    <code>null</code>.
    */
   public FastDataImpl(APIImpl api) {
      super(api);
   }
   protected void initImpl(Map<String, String> properties)
   throws MissingRequiredPropertyException,
          InvalidPropertyValueException,
          InitializationException {
      productDescriptions.put(123456789L, "This is a table");
      productDescriptions.put(987654321L, "This is a chair");
      descriptionsTimestamp = System.currentTimeMillis();
   }

   /**
    * Calls this function. If the function fails, it may throw any kind of
    * exception. All exceptions will be handled by the caller.
    *
    * @param request
    *    the request, never <code>null</code>.
    *
    * @return
    *    the result of the function call, should never be <code>null</code>.
    *
    * @throws Throwable
    *    if anything went wrong.
    */
   public Result call(Request request) throws Throwable {

      // If the client sends us the same timestamp as the data timestamp,
      // there no need to get the information and to return/send it.
      if (request.isSetProductDataClientTimestamp()) {
         long clientTimestamp = request.getProductDataClientTimestamp();
         if (clientTimestamp == descriptionsTimestamp) {
            return new NotModifiedResult();
         }
      }
      long productId = request.getProductId();
      String description = productDescriptions.get(productId);
      if (description == null) {
         return new InvalidNumberResult();
      } else {
         SuccessfulResult result = new SuccessfulResult();
         result.setProductDescription(description);
         result.setProductDataTimestamp(descriptionsTimestamp);
         return result;
      }
   }
}
