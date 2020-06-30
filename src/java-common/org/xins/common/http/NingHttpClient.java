/*
 * $Id: NingHttpClient.java,v 1.2 2013/01/28 15:34:53 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.http;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

/**
 * This class is a HttpClient that will use the async-http-client with Netty to call the URL.
 * Only HTTP GET is supported, so if you use this class with XINS, use
 * <code>callConfig.setHTTPMethod(HTTPMethod.GET);</code>
 *
 * @version $Revision: 1.2 $ $Date: 2013/01/28 15:34:53 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 *
 * @see https://github.com/AsyncHttpClient/async-http-client
 * @since XINS 3.1
 */
public class NingHttpClient extends DefaultHttpClient {

   /**
    * The Ning client to call the URLs.
    */
   private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

   protected RequestDirector createClientRequestDirector(
           HttpRequestExecutor requestExec,
           ClientConnectionManager conman,
           ConnectionReuseStrategy reustrat,
           ConnectionKeepAliveStrategy kastrat,
           HttpRoutePlanner rouplan,
           HttpProcessor httpProcessor,
           HttpRequestRetryHandler retryHandler,
           RedirectStrategy redirectStrategy,
           AuthenticationStrategy targetAuthStrategy,
           AuthenticationStrategy proxyAuthStrategy,
           UserTokenHandler userTokenHandler,
           HttpParams params) {
      return new NingRequestDirector(
              LogFactory.getLog(NingRequestDirector.class),
              requestExec,
              conman,
              reustrat,
              kastrat,
              rouplan,
              httpProcessor,
              retryHandler,
              redirectStrategy,
              targetAuthStrategy,
              proxyAuthStrategy,
              userTokenHandler,
              params);
   }

   class NingRequestDirector extends DefaultRequestDirector {

      public NingRequestDirector(
              Log log,
              HttpRequestExecutor requestExec,
              ClientConnectionManager conman,
              ConnectionReuseStrategy reustrat,
              ConnectionKeepAliveStrategy kastrat,
              HttpRoutePlanner rouplan,
              HttpProcessor httpProcessor,
              HttpRequestRetryHandler retryHandler,
              RedirectStrategy redirectStrategy,
              AuthenticationStrategy targetAuthStrategy,
              AuthenticationStrategy proxyAuthStrategy,
              UserTokenHandler userTokenHandler,
              HttpParams params) {
         super(log, requestExec, conman, reustrat, kastrat, rouplan, httpProcessor,
                 retryHandler, redirectStrategy, targetAuthStrategy, proxyAuthStrategy, userTokenHandler, params);
      }

      public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException, IOException {
         try {
            // Call the URL
            String uri = request.getRequestLine().getUri();
            Future<Response> reponseFuture = asyncHttpClient.prepareGet(uri).execute();

            // Get the response
            Response response = reponseFuture.get();
            int statusCode = response.getStatusCode();
            String statusText = response.getStatusText();
            String contentType = response.getContentType();
            String content = response.getResponseBody();
            HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, statusText);
            HttpEntity entity = new StringEntity(content, ContentType.parse(contentType));
            httpResponse.setEntity(entity);
            return httpResponse;
         } catch (InterruptedException ex) {
            throw new IOException(ex);
         } catch (ExecutionException ex) {
            ex.printStackTrace();
            throw new IOException(ex);
         }
      }
   }
}
