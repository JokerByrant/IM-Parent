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
 * 用户登录处理
 * @author sxh
 * @date 2022/2/15
 */
@Component
@Slf4j
public class LoginHandler extends SimpleChannelInboundHandler<ProtoMsg.Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoMsg.Message msg) throws Exception {
        if (msg.getType().equals(ProtoMsg.HeadType.LOGIN_REQUEST)) {
            log.info("处理登录请求...");
            ProtoMsg.LoginRequest loginRequest = msg.getLoginRequest();
            ChannelHandlerContext existChannel = UserSessions.onlineUsers.get(loginRequest.getUserUid());
            if (existChannel != null) {
                // 已登录的用户强制下线
                existChannel.channel().writeAndFlush(ProtoMsgBuild.logoutResponseMsg(ResultCodeEnum.NO_TOKEN, false, loginRequest.getPlatform()));
            }
            UserSessions.onlineUsers.put(loginRequest.getUserUid(), ctx);
            // todo 获取用户登录成功后的token信息
            String apiAuth = null;
            ctx.channel().writeAndFlush(ProtoMsgBuild.loginResponseMsg(ResultCodeEnum.SUCCESS, apiAuth));
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
