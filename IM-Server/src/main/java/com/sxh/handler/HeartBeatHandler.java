package com.sxh.handler;

import com.sxh.protobuf.ProtoMsg;
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
    //心跳检测时间,读空闲
    public static final int READ_IDLE_GAP = 10;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoMsg.Message msg) throws Exception {
        if (msg.getType().equals(ProtoMsg.HeadType.HEART_BEAT)) {
            log.info("处理心跳包...");
            // TODO 这里之后可以开辟一个线程进行处理
            ctx.channel().writeAndFlush(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * 当 `IdleStateHandler` 发现 [读超时]、[写超时] 后，会调用 `fireUserEventTriggered()` 寻找 `userEventTriggered()` 方法并执行。
     * 服务端只处理读超时的情况，即若在指定时间内未读取到客户端发送的消息，将连接关闭。
     *
     * @param ctx 连接信息
     * @param evt 事件
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                log.error("{} 秒内未读到数据，关闭连接", READ_IDLE_GAP);
                ctx.channel().close();
            }
        }
    }
}
