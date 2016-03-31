
import com.netty.initializer.DataClientInitializer;
import com.netty.pojo.Configuration;
import com.netty.proto.Message;
import com.netty.proto.Message.Packet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class Client {
	static final String HOST = "10.0.0.4";
    static final int PORT = Integer.parseInt(String.valueOf(8080));

    private static void initialize(Configuration configuration) {
        configuration.setHost("localhost"); 
        configuration.setPort(8088);
        Message.Packet.Builder packet = Message.Packet.newBuilder();
        packet.setHost("localhost");
        packet.setPort(8088);
        packet.setMessageType(Packet.Type.CLIENTREQUEST);
        packet.setTermId(0);
        configuration.setPacket(packet);
    }

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        Configuration configuration = new Configuration();
        initialize(configuration);


        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new DataClientInitializer(sslCtx, configuration));

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();
            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            Message.Packet.Builder packet = Message.Packet.newBuilder(configuration.getPacket().build());

           //addUserRequest(packet);
            //addUserCourseRequest(packet);
            addCourseRequest(packet);
//                getCourse("CmpE275", packet);
             //   getUser(1, packet);
                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(packet);
                lastWriteFuture.channel().closeFuture().sync();


            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }
    }

    private static void getUser(int userId, Packet.Builder packet) {
        packet.setFunctionalities(Packet.Functionalities.GETUSER);
        Packet.Details.Builder details=Packet.Details.newBuilder();
        details.setUserId(userId);
        packet.setDetails(details);        
    }

    private static void getCourse(String courseId, Packet.Builder packet) {
        packet.setFunctionalities(Packet.Functionalities.GETCOURSEDESCRIPTION);
        Packet.Details.Builder details=Packet.Details.newBuilder();
        details.setCourseId(courseId);
        packet.setDetails(details);
    }

    private static void addUserCourseRequest(Packet.Builder packet) {
        Packet.Details.Builder details=Packet.Details.newBuilder();
        details.setCourseDescription("English 294 course");
        details.setCourseId("2");
        details.setCourseName("English");
        details.setUserId(2);
        details.setUsername("Akshay");
        packet.setDetails(details);
        packet.setFunctionalities(Packet.Functionalities.ADDCOURSETOUSER);

    }
    private static void addUserRequest(Packet.Builder packet) {
        Packet.Details.Builder details=Packet.Details.newBuilder();
        details.setUserId(1);
        details.setUsername("Akshay");
        packet.setDetails(details);
        packet.setFunctionalities(Packet.Functionalities.ADDUSER);

    }
    private static void addCourseRequest(Packet.Builder packet) {
        Packet.Details.Builder details=Packet.Details.newBuilder();
        details.setCourseDescription("Distributed Application Development");
        details.setCourseId("CmpE275");
        details.setCourseName("Cmpe 273 gash");
        packet.setDetails(details);
        packet.setFunctionalities(Packet.Functionalities.ADDCOURSE);
    }
}
