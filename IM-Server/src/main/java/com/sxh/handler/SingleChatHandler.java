package com.sxh.handler;

import com.sxh.builder.ProtoMsgBuild;
import com.sxh.constant.ResultCodeEnum;
import com.sxh.constant.UserSessions;
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
            ProtoMsg.SingleChatting singleChatting = msg.getSingleChatting();
            // 检查发送人的channel
            ChannelHandlerContext sendChannel = UserSessions.onlineUsers.get(singleChatting.getUserUid());
            if (sendChannel == null) {
                log.info("发送人 {} 离线, 重新绑定channel...", singleChatting.getUserUid());
                UserSessions.onlineUsers.put(singleChatting.getUserUid(), ctx);
            }
            // 检查接收人的channel
            ChannelHandlerContext recvChannel = UserSessions.onlineUsers.get(singleChatting.getRecvUid());
            if (recvChannel == null) {
                log.info("接收人 {} 离线, 消息发送失败...", singleChatting.getRecvUid());
                log.info("缓存消息到离线消息列表, msgUid: {}", singleChatting.getMsgUid());
            } else {
                recvChannel.channel().writeAndFlush(msg);
            }
            // 给发送人返回消息发送成功ack
            ctx.channel().writeAndFlush(ProtoMsgBuild.messageResponseMsg(ResultCodeEnum.MSG_SEND_SUCCESS, singleChatting.getMsgUid()));
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
