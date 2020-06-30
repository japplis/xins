/*
 * $Id: IncorrectSecretKeyException.java,v 1.9 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.collections;

/**
 * Exception that indicates a secret key argument did not match the actual
 * secret key.
 *
 * @version $Revision: 1.9 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.2.0
 */
public final class IncorrectSecretKeyException
extends IllegalArgumentException {

   /**
    * Constructs a new <code>IncorrectSecretKeyException</code>.
    */
   IncorrectSecretKeyException() {
      super("Incorrect secret key.");
   }
}
