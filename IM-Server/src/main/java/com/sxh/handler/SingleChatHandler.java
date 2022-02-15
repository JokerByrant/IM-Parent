package com.sxh.handler;

import com.sxh.protobuf.ProtoMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 单聊消息转发处理
 * @author sxh
 * @date 2022/2/10
 */
@Component
@Slf4j
public class SingleChatHandler extends SimpleChannelInboundHandler<ProtoMsg.Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoMsg.Message msg) throws Exception {
        if (msg.getType().equals(ProtoMsg.HeadType.SINGLE_REQUEST)) {
            log.info("处理单聊消息...");
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
