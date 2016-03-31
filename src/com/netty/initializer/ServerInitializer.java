package com.netty.initializer;
  import com.netty.handler.ServerHandler;
import com.netty.pojo.Configuration;
import com.netty.proto.Message;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
  
  /**
   * Creates a newly configured {@link ChannelPipeline} for a new channel.
   */
  public class ServerInitializer extends ChannelInitializer<SocketChannel> {
  
      private final SslContext sslCtx;
      Configuration configuration;
      public ServerInitializer(SslContext sslCtx,Configuration configuration) {
          this.sslCtx = sslCtx;
          this.configuration=configuration;
      }
  
      @Override
      public void initChannel(SocketChannel ch) throws Exception {
          ChannelPipeline pipeline = ch.pipeline();  
          pipeline.addLast(sslCtx.newHandler(ch.alloc()));
          pipeline.addLast(new ProtobufVarint32FrameDecoder());
          pipeline.addLast(new ProtobufDecoder(Message.Packet.getDefaultInstance()));
          pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
          pipeline.addLast(new ProtobufEncoder());
          // and then business logic.
          pipeline.addLast(new ServerHandler(this.configuration));
      }
  }
