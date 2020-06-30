/*
 * $Id: DefaultNettyServletPipelineFactory.java,v 1.1 2012/03/03 16:35:17 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.common.servlet.container;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Netty handler to invoke servlet.
 *
 * @version $Revision: 1.1 $ $Date: 2012/03/03 16:35:17 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class DefaultNettyServletPipelineFactory implements ChannelPipelineFactory {

   private NettyServletHandler servletHandler;
   
   public ChannelPipeline getPipeline() throws Exception {
      ChannelPipeline pipeline = pipeline();

      pipeline.addLast("decoder", new HttpRequestDecoder());
      pipeline.addLast("encoder", new HttpResponseEncoder());
      pipeline.addLast("deflater", new HttpContentCompressor());
      pipeline.addLast("handler", servletHandler);
      return pipeline;
   }

   public NettyServletHandler getServletHandler() {
      return servletHandler;
   }

   public void setServletHandler(NettyServletHandler servletHandler) {
      this.servletHandler = servletHandler;
   }
}