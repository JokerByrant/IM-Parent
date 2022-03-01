package com.sxh.client;

import com.sxh.codec.ProtobufDecoder;
import com.sxh.codec.ProtobufEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author sxh
 * @date 2022/2/10
 */
@Component
@Slf4j
public class NettyClient {
    @Value("${netty.port}")
    private int port;
    @Value("${netty.host}")
    private String host;

    private final EventLoopGroup group = new NioEventLoopGroup();

    @PostConstruct
    public void start()  {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        // 编解码
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new ProtobufDecoder());
                        // 心跳处理Handler
                        pipeline.addLast(new IdleStateHandler(0, HeartBeatHandler.WRITE_IDLE_GAP, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new HeartBeatHandler());
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("连接Netty服务端成功！");
            } else {
                log.info("连接失败，进行断线重连...");
                future.channel().eventLoop().schedule(this::start, 20, TimeUnit.SECONDS);
            }
        });
    }
}
