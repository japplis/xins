/*
 * $Id: XinsFunctionResultValidator.java,v 1.6 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.spring;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.xins.server.FunctionResult;
import org.xins.server.InvalidResponseResult;

/**
 * Validator for the result return by the function implementation on the server side.
 * This class requires the Spring library.
 *
 * @version $Revision: 1.6 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.0
 */
public class XinsFunctionResultValidator implements Validator {

   /**
    * Creates a new instance of XinsFunctionResultValidator.
    */
   public XinsFunctionResultValidator() {
   }

   public boolean supports(Class beanClass) {

      // Only support XINS function implementation result generated beans
      return beanClass.isInstance(FunctionResult.class);
   }

   public void validate(Object bean, Errors errors) {
      FunctionResult result = (FunctionResult) bean;
      InvalidResponseResult validationError = result.checkOutputParameters();
      if (validationError != null) {
         errors.reject(validationError.getDataElement().toString());
      }
   }
}
