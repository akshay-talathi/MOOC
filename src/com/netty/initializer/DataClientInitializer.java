package com.netty.initializer;
import com.netty.handler.DataClientHandler;
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

public class DataClientInitializer extends ChannelInitializer<SocketChannel> {  
      private final SslContext sslCtx;
      Configuration configuration;
      public DataClientInitializer(SslContext sslCtx,Configuration configuration) {
          this.sslCtx = sslCtx;
          this.configuration=configuration;
      }
      @Override
      public void initChannel(SocketChannel ch) throws Exception {
          ChannelPipeline pipeline = ch.pipeline();
          pipeline.addLast(sslCtx.newHandler(ch.alloc(), configuration.getHost(), configuration.getPort()));
          pipeline.addLast(new ProtobufVarint32FrameDecoder());
          pipeline.addLast(new ProtobufDecoder(Message.Packet.getDefaultInstance()));
          pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
          pipeline.addLast(new ProtobufEncoder());
          pipeline.addLast(new DataClientHandler(configuration));
      }
  }

