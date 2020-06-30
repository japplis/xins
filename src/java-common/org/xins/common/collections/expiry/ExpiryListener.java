/*
 * $Id: ExpiryListener.java,v 1.12 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections.expiry;

import java.util.Map;

/**
 * Interface for objects that can receive expiry events from an
 * <code>ExpiryFolder</code>.
 *
 * @version $Revision: 1.12 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public interface ExpiryListener {

   /**
    * Notification of the expiry of the specified set of objects.
    *
    * @param folder
    *    the folder that has expired the entries , never <code>null</code>.
    *
    * @param expired
    *    the map containing the objects that have expired, indexed by key;
    *    never <code>null</code>.
    */
   void expired(ExpiryFolder folder, Map expired);
}
