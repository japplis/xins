/*
 * $Id: UnacceptableRequestException.java,v 1.30 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;

/**
 * Exception that indicates that a request for an API call is considered
 * unacceptable on the application-level. For example, a mandatory input
 * parameter may be missing.
 *
 * @version $Revision: 1.30 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 1.2.0
 */
public final class UnacceptableRequestException extends UnacceptableMessageException {

   /**
    * Constructs a new <code>UnacceptableRequestException</code> using the
    * specified <code>AbstractCAPICallRequest</code>.
    *
    * @param request
    *    the {@link AbstractCAPICallRequest} that is considered unacceptable,
    *    cannot be <code>null</code>.
    */
   public UnacceptableRequestException(AbstractCAPICallRequest request) {
      super(request.xinsCallRequest());
   }
}
