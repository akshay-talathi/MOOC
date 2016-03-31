package com.netty.intercluster;

import com.netty.handler.DataClientHandler;
import com.netty.pojo.Configuration;
import com.netty.proto.App;
import com.netty.proto.Message;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;

public class InterClusterInitializer extends ChannelInitializer<SocketChannel> {
      private final SslContext sslCtx;
      Configuration configuration;
      public InterClusterInitializer(SslContext sslCtx, Configuration configuration) {
          this.sslCtx = sslCtx;
          this.configuration=configuration;
      }
      @Override
      public void initChannel(SocketChannel ch) throws Exception {
          ChannelPipeline pipeline = ch.pipeline();
//          pipeline.addLast(sslCtx.newHandler(ch.alloc(), configuration.getHost(), configuration.getPort()));
//          pipeline.addLast(new ProtobufVarint32FrameDecoder());
//          pipeline.addLast(new ProtobufDecoder(App.Request.getDefaultInstance()));
//          pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
//          pipeline.addLast(new ProtobufEncoder());
          pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(369296132, 0, 4, 0, 4));

          // pipeline.addLast("frameDecoder", new
          // DebugFrameDecoder(67108864, 0, 4, 0, 4));

          // decoder must be first
          pipeline.addLast("protobufDecoder", new ProtobufDecoder(App.Request.getDefaultInstance()));
          pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
          pipeline.addLast("protobufEncoder", new ProtobufEncoder());
          pipeline.addLast("handler", new InterClusterHandler(configuration));
      }
  }

