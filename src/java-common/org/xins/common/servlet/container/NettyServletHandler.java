/*
 * $Id: NettyServletHandler.java,v 1.2 2012/04/27 07:40:28 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.servlet.container;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import org.jboss.netty.bootstrap.ServerBootstrap;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

/**
 * Netty handler to invoke servlets.
 *
 * This class is partly based on the examples of the Netty project which are
 * released under the Apache License, version 2.0.
 *
 * @version $Revision: 1.2 $ $Date: 2012/04/27 07:40:28 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class NettyServletHandler extends SimpleChannelUpstreamHandler {

   private LocalServletHandler localServletHandler;

   /**
    * Creates a Netty handler that allow to invoke a Servlet without starting a
    * HTTP server.
    *
    * @param warFile the location of the war file containing the Servlet, cannot
    * be
    * <code>null</code>.
    *
    * @throws ServletException if the Servlet cannot be created.
    */
   public NettyServletHandler(File warFile) throws ServletException {
      localServletHandler = new LocalServletHandler(warFile);
   }

   /**
    * Creates a Servlet handler that allow to invoke a Servlet without starting
    * a HTTP server.
    *
    * @param servletClassName The name of the servlet's class to load, cannot be
    * <code>null</code>.
    *
    * @throws ServletException if the Servlet cannot be created.
    */
   public NettyServletHandler(String servletClassName) throws ServletException {
      localServletHandler = new LocalServletHandler(servletClassName);
   }

   @Override
   public void messageReceived(ChannelHandlerContext context, MessageEvent event) throws Exception {
      HttpRequest request = (HttpRequest) event.getMessage();
      String method = request.getMethod().getName();
      String url = request.getUri();
      String data = request.getContent().toString();
      
      if (url.equals("/favicon.ico")) {
         sendError(context, HttpResponseStatus.NOT_FOUND);
         return;
      }
      Map headers = new LinkedHashMap();
      for (Map.Entry<String, String> header : request.getHeaders()) {
         headers.put(header.getKey(), header.getValue());
      }
      XINSServletResponse response = localServletHandler.query(method, url, data, headers);

      int statusCode = response.getStatus();
      HttpResponse nettyResponse = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(statusCode));
      setContentLength(request, response.getContentLength());

      Map<String, String> responseHeaders = response.getHeaders();
      for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
         nettyResponse.addHeader(header.getKey(), header.getValue());
      }
      String responseString = response.getResult();
      if (responseString != null) {
         Charset encoding = Charset.forName(response.getCharacterEncoding());
         nettyResponse.setContent(ChannelBuffers.copiedBuffer(responseString, encoding));
         context.getChannel().write(nettyResponse);
      }
      context.getChannel().close();
   }

   private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
      HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
      response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
      response.setContent(ChannelBuffers.copiedBuffer(
              "Failure: " + status.toString() + "\r\n",
              CharsetUtil.UTF_8));
      ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
   }

   public void startServer(int port, String pipelineFactory) throws Exception {
      ServerBootstrap server = new ServerBootstrap(
              new NioServerSocketChannelFactory(
              Executors.newCachedThreadPool(),
              Executors.newCachedThreadPool()));

      if (pipelineFactory == null) {
         pipelineFactory = "org.xins.common.servlet.container.DefaultNettyServletPipelineFactory";
      }
      DefaultNettyServletPipelineFactory pipelineFactoryClass = (DefaultNettyServletPipelineFactory) Class.forName(pipelineFactory).newInstance();
      pipelineFactoryClass.setServletHandler(this);

      server.setPipelineFactory(pipelineFactoryClass);

      server.bind(new InetSocketAddress(port));
   }
}