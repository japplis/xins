/*
 * $Id: APIServletSingleThreaded.java,v 1.3 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

import javax.servlet.SingleThreadModel;

/**
 * This class is similar to APIServlet except that it implements the javax.servlet.SingleThreadModel
 * to indique that only 1 thread can handle only 1 request at a time.
 *
 * @version $Revision: 1.3 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @since XINS 2.2
 */
public class APIServletSingleThreaded extends APIServlet implements SingleThreadModel {
}
