/*
 * $Id: WarnDoubleProperties.java,v 1.11 2010/10/25 20:36:52 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

import java.util.Properties;

import org.xins.common.Log;

/**
 * Class that logs a warning message in the log system if a property value
 * is overwritten.
 *
 * @version $Revision: 1.11 $ $Date: 2010/10/25 20:36:52 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.4.0
 */
public class WarnDoubleProperties extends Properties {

   @Override
   public Object put(Object key, Object value) {
       Object oldValue = super.put(key, value);
       if (oldValue != null &&
             key instanceof String && value instanceof String && oldValue instanceof String) {
           Log.log_1350((String) key, (String) oldValue, (String) value);
       }
       return oldValue;
   }
}
