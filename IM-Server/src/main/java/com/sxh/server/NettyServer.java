package com.sxh.server;

import com.sxh.codec.ProtobufDecoder;
import com.sxh.codec.ProtobufEncoder;
import com.sxh.handler.ClusterChatHandler;
import com.sxh.handler.HeartBeatHandler;
import com.sxh.handler.SingleChatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * netty服务类
 *
 * @author sxh
 * @date 2022/2/7
 */
@Component
@Slf4j
public class NettyServer {
    @Autowired
    private SingleChatHandler singleChatHandler;
    @Autowired
    private ClusterChatHandler clusterChatHandler;

    @Value("${server.port}")
    private int port;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    /**
     * 启动netty服务
     */
    @PostConstruct
    public void start() throws UnknownHostException {
        /*
         * 注: handler&childHandler中，前后两个的区别是：前者配置的是服务端初始化时加载的Handler，后者配置的是客户端连接时加载的Handler。option&childOption同样如此。
         */
        ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                // 服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        // 编解码
                        pipeline.addLast(new ProtobufDecoder());
                        pipeline.addLast(new ProtobufEncoder());
                        // 心跳处理Handler
                        pipeline.addLast(new IdleStateHandler(HeartBeatHandler.READ_IDLE_GAP, 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new HeartBeatHandler());
                        // 聊天消息处理Handler
                        pipeline.addLast(singleChatHandler);
                        pipeline.addLast(clusterChatHandler);
                    }
                });
        ChannelFuture channelFuture = bindPort(bootstrap);
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                log.info("Netty服务启动成功！{}:{}", InetAddress.getLocalHost().getHostAddress(), port);
            } else {
                log.error("Netty服务启动失败！{}:{}", InetAddress.getLocalHost().getHostAddress(), port);
            }
        });
    }

    /**
     * 关闭netty服务
     */
    @PreDestroy
    public void close() throws UnknownHostException {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
        log.info("Netty服务关闭成功！{}:{}", InetAddress.getLocalHost().getHostAddress(), port);
    }

    /**
     * 绑定端口号
     *
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
                log.info("端口号 [{}] 已被占用！", port);
                port++;
                log.info("尝试一个新的端口：" + port);
                // 重新绑定端口号
                bootstrap.localAddress(new InetSocketAddress(port));
            }
        }
    }
}
