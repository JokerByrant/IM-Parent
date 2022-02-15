package com.sxh.constant;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户连接信息
 * @author sxh
 * @date 2022/2/15
 */
public class UserSessions {
    // 在线的客户端连接上下文 {userUid, ctx}
    public static Map<String, ChannelHandlerContext> onlineUsers = new ConcurrentHashMap<>();
}
