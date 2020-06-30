/*
 * $Id: XINSCallResultData.java,v 1.15 2010/11/18 20:35:05 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.client;

import java.util.Map;
import org.w3c.dom.Element;

/**
 * Data part of a XINS call result.
 *
 * @version $Revision: 1.15 $ $Date: 2010/11/18 20:35:05 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public interface XINSCallResultData {

   /**
    * Returns the error code. If <code>null</code> is returned the call was
    * successful and thus no error code was returned. Otherwise the call was
    * unsuccessful.
    *
    * <p>This method will never return an empty string, so if the result is
    * not <code>null</code>, then it is safe to assume the length of the
    * string is at least 1 character.
    *
    * @return
    *    the returned error code, or <code>null</code> if the call was
    *    successful.
    */
   String getErrorCode();

   /**
    * Gets all parameters.
    *
    * @return
    *    a {@link Map} with all parameters, or <code>null</code> if
    *    there are none.
    */
   Map<String, String> getParameters();

   /**
    * Returns the optional extra data. The data is an XML {@link Element},
    * or <code>null</code>.
    *
    * @return
    *    the extra data as an XML {@link Element}, can be
    *    <code>null</code>;
    */
   Element getDataElement();
}
