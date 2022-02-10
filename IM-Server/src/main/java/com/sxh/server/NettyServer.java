package com.sxh.server;

import com.sxh.codec.ProtobufDecoder;
import com.sxh.codec.ProtobufEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * netty服务类
 *
 * @author sxh
 * @date 2022/2/7
 */
@Component
public class NettyServer {
    private static int port = 7700;

    private final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 启动netty服务
     */
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        /*
         * 注: handler&childHandler中，前后两个的区别是：前者配置的是服务端初始化时加载的Handler，后者配置的是客户端连接时加载的Handler。option&childOption同样如此。
         */
        ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                // 服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝
                .option(ChannelOption.SO_BACKLOG, 1024).childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("MessageDecoder", new ProtobufDecoder());
                        pipeline.addLast("MessageEncoder", new ProtobufEncoder());
                    }
                });
        ChannelFuture channelFuture = bindPort(bootstrap);
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                // TODO Netty服务启动成功后，一些后续操作在这处理
                logger.info("启动成功！{}:{}", InetAddress.getLocalHost().getHostAddress(), port);
            } else {
                logger.error("启动失败！{}:{}", InetAddress.getLocalHost().getHostAddress(), port);
            }
        });

        // try {
        //     // 应用程序会一直等待，直到channel关闭
        //     channelFuture.channel().closeFuture().sync();
        // } catch (Exception e) {
        //     logger.error("关闭Netty服务时出现异常！", e);
        // } finally {
        //     // 优雅关闭EventLoopGroup，
        //     bossGroup.shutdownGracefully();
        //     workGroup.shutdownGracefully();
        // }
    }

    /**
     * 绑定端口号
     * @param bootstrap
     * @return
     */
    private ChannelFuture bindPort(ServerBootstrap bootstrap) {
        // 启动失败时，多次启动，直到启动成功为止
        while (true) {
            try {
                // sync()作用，bind()是异步操作，sync()用于等待bind()执行完成
                return bootstrap.bind(port).sync();
            } catch (Exception e) {
                logger.info("端口号 [{}] 已被占用！", port);
                port++;
                logger.info("尝试一个新的端口：" + port);
                // 重新绑定端口号
                bootstrap.localAddress(new InetSocketAddress(port));
            }
        }
    }
}
