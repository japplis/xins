/*
 * $Id: PropertiesPropertyReader.java,v 1.14 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

import java.util.Properties;
import org.xins.common.MandatoryArgumentChecker;

/**
 * Property reader based on a <code>Properties</code> object.
 *
 * @version $Revision: 1.14 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class PropertiesPropertyReader
extends AbstractPropertyReader {

   /**
    * Constructs a new <code>PropertiesPropertyReader</code>.
    *
    * @param properties
    *    the {@link Properties} object to read from, not <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>properties == null</code>.
    */
   public PropertiesPropertyReader(Properties properties)
   throws IllegalArgumentException {
      super(properties);
      MandatoryArgumentChecker.check("properties", properties);
   }
}
