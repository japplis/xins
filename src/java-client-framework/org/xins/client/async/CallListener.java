/*
 * $Id: CallListener.java,v 1.9 2010/09/29 17:21:47 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client.async;

import java.util.EventListener;

/**
 * Listener notified when the call to an API is finished whether it has
 * succeeded or failed.
 *
 * @version $Revision: 1.9 $ $Date: 2010/09/29 17:21:47 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.4.0
 */
public interface CallListener extends EventListener {

   /**
    * Invoked when a successful result has been returned by the function.
    *
    * @param event
    *    the call event that has the result of the call.
    */
   void callSucceeded(CallSucceededEvent event);

   /**
    * Invoked when the call to the function failed.
    *
    * @param event
    *    the call event that has the details of the failure.
    */
   void callFailed(CallFailedEvent event);
}
