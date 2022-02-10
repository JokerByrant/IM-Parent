package com.sxh.netty;

import com.sxh.protobuf.ProtoMsg;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳处理
 * @author sxh
 * @date 2022/2/10
 */
@Slf4j
public class HeartBeatHandler extends SimpleChannelInboundHandler<ProtoMsg.Message> {
    //心跳检测时间，写空闲
    public static final int WRITE_IDLE_GAP = 5;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoMsg.Message msg) throws Exception {
        if (msg.getType().equals(ProtoMsg.HeadType.HEART_BEAT)) {
            log.info("接收到服务端发送的心跳包");
        }
    }

    /**
     * 当 `IdleStateHandler` 发现 [读超时]、[写超时] 后，会调用 `fireUserEventTriggered()` 寻找 `userEventTriggered()` 方法并执行。
     * 客户端若在指定时间内未发消息给服务端，则发送一个心跳包过去。
     *
     * @param ctx 连接信息
     * @param evt 事件
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.WRITER_IDLE)) {
                log.info("已经 {} 秒未给服务端发消息了，发送一个心跳包...", WRITE_IDLE_GAP);
                ProtoMsg.Message heartBeatMsg = ProtoMsgBuild.heartBeatMsg("heartBeat");
                // 向服务端送心跳包，并在发送失败时关闭该连接
                ctx.writeAndFlush(heartBeatMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }
    }
}
