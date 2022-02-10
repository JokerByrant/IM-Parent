package com.sxh.netty;

import com.sxh.protobuf.ProtoMsg;

/**
 * 消息构造类
 * @author sxh
 * @date 2022/2/10
 */
public class ProtoMsgBuild {
    /**
     * 心跳包消息构造
     * @param userUid 用户uid
     * @return
     */
    public static ProtoMsg.Message heartBeatMsg(String userUid) {
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder().setType(ProtoMsg.HeadType.HEART_BEAT);
        ProtoMsg.MessageHeartBeat heartBeatMsg = ProtoMsg.MessageHeartBeat.newBuilder().setUid(userUid).build();
        mb.setHeartBeat(heartBeatMsg);
        return mb.build();
    }
}
