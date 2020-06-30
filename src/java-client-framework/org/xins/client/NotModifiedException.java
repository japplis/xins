/*
 * $Id: NotModifiedException.java,v 1.1 2013/01/15 11:19:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;

import org.xins.common.service.TargetDescriptor;

/**
 *
 * @author Gebruiker
 */
public class NotModifiedException extends XINSCallException {

   public NotModifiedException(XINSCallRequest request, TargetDescriptor target, long duration)
           throws IllegalArgumentException {
      super("Not modified", request, target, duration, null, null);
   }
}
