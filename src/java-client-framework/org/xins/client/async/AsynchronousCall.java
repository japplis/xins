/*
 * $Id: AsynchronousCall.java,v 1.16 2011/04/16 15:48:02 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xins.client.AbstractCAPI;
import org.xins.client.AbstractCAPICallRequest;

/**
 * Class used to register the {@link CallListener}s and to call the API
 * asynchronously.
 *
 * @version $Revision: 1.16 $ $Date: 2011/04/16 15:48:02 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.4.0
 */
public class AsynchronousCall {

   /**
    * List containing the registered {@link CallListener}.
    */
   private List<CallListener> _listeners = new ArrayList<CallListener>();

   /**
    * Adds a new listener for the call.
    *
    * @param listener
    *    the listener that will be notified of the result of the call.
    */
   public void addCallListener(CallListener listener) {
      _listeners.add(listener);
   }

   /**
    * Removes a listener for the call. If the listener was not previously added
    * nothing happens.
    *
    * @param listener
    *    the listener that will be notified of the result of the call.
    */
   public void removeCallListener(CallListener listener) {
      _listeners.remove(listener);
   }

   /**
    * Calls a function asynchronously. This function does not return anything as
    * the result and exception will be received by the registered {@link CallListener}.
    *
    * @param capi
    *    the CAPI to use to call the function.
    *
    * @param request
    *    the input parameters for this call.
    */
   public void call(AbstractCAPI capi, AbstractCAPICallRequest request) {
      CallNotifyThread thread = new CallNotifyThread(capi, request, _listeners);
      thread.start();
   }

   /**
    * Thread that executes the call to the function.
    *
    * @version $Revision: 1.16 $ $Date: 2011/04/16 15:48:02 $
    * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
    */
   private static class CallNotifyThread extends CallCAPIThread {

      /**
       * The listeners to notify.
       */
      private final List<CallListener> _listeners;

      /**
       * Calls a CAPI function on a separate thread and notifies the listeners
       * of the result.
       *
       * @param capi
       *    the CAPI to use to call the function.
       *
       * @param request
       *    the input parameters for this call.
       *
       * @param listeners
       *    the listeners to notify of the result of the call.
       */
      CallNotifyThread(AbstractCAPI capi, AbstractCAPICallRequest request, List<CallListener> listeners) {
         super(capi, request);

         // Notify the listeners registered at the moment of the call and not
         // when the result is received.
         _listeners = Collections.unmodifiableList(listeners);
      }

      public void run() {

         // Call the API
         super.run();

         // Get the result and notify the listeners
         if (getException() == null) {
            CallSucceededEvent event = new CallSucceededEvent(getCAPI(),
                  getRequest(), getDuration(), getResult());
            for (CallListener listener : _listeners) {
               listener.callSucceeded(event);
            }
         } else {
            CallFailedEvent event = new CallFailedEvent(getCAPI(),
                  getRequest(), getDuration(), getException());
            for (CallListener listener : _listeners) {
               listener.callFailed(event);
            }
         }
      }
   }
}
