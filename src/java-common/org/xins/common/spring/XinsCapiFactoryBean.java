/*
 * $Id: XinsCapiFactoryBean.java,v 1.5 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.spring;

import org.springframework.beans.factory.FactoryBean;

import org.xins.client.AbstractCAPI;

/**
 * FactoryBean for locally defined CAPI references.
 * This class requires the Spring library.
 *
 * @version $Revision: 1.5 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.0
 */
public class XinsCapiFactoryBean extends XinsClientInterceptor implements FactoryBean {

   public Object getObject() throws Exception {
      return capi;
   }

   public Class getObjectType() {
      if (capi == null) {
         return AbstractCAPI.class;
      } else {
         return capi.getClass();
      }
   }

   public boolean isSingleton() {
      return true;
   }

}
