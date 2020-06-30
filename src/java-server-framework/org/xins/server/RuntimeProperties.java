/*
 * $Id: RuntimeProperties.java,v 1.20 2012/02/28 18:10:54 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xins.common.collections.InvalidPropertyValueException;
import org.xins.common.collections.MissingRequiredPropertyException;
import org.xins.common.service.Descriptor;

/**
 * Base class to get the runtime properties.
 *
 * @version $Revision: 1.20 $ $Date: 2012/02/28 18:10:54 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * @author <a href="mailto:tauseef.rehman@orange-ftgroup.com">Tauseef Rehman</a>
 *
 * @since XINS 1.2.0
 */
public class RuntimeProperties {

   /**
    * The stored runtime settings. This variable is initially
    * <code>null</code> and then initialized by
    * {@link #init(Map)}.
    */
   private Map<String, String> _runtimeSettings;

   /**
    * Initialize the runtime properties. This method should be overwritten
    * by a generated class if any runtime properties is declared in the
    * <code>impl.xml</code> file.
    *
    * @param runtimeSettings
    *    the runtime properties, not <code>null</code>.
    *
    * @throws MissingRequiredPropertyException
    *    if a required property is not given.
    *
    * @throws InvalidPropertyValueException
    *    if the value of a certain property is invalid.
    */
   protected void init(Map<String, String> runtimeSettings)
   throws MissingRequiredPropertyException, InvalidPropertyValueException {
      _runtimeSettings = runtimeSettings;
   }

   /**
    * Gets the descriptor list. The list is created by getting all the
    * properties which are marked as <i>_descriptor</i> in the run time
    * properties file.
    *
    * <p>Since XINS 3.0, the returned collection is type-safe.
    *
    * @return
    *    the list of all descriptors, may not be <code>null</code>.
    *
    * @since XINS 1.3.0
    */
   protected List<Descriptor> descriptors() {
      return new ArrayList<Descriptor>();
   }

   /**
    * Gets all the runtime properties.
    *
    * @return
    *    the runtime properties, can be <code>null</code> before the initialization.
    *
    * @since XINS 2.1
    */
   public Map<String, String> properties() {
      return _runtimeSettings;
   }
}
