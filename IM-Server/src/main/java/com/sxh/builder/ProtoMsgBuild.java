package com.sxh.builder;

import com.sxh.constant.ResultCodeEnum;
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
        ProtoMsg.MessageHeartBeat heartBeatMsg = ProtoMsg.MessageHeartBeat.newBuilder()
                .setUid(userUid).build();
        mb.setHeartBeat(heartBeatMsg);
        return mb.build();
    }

    /**
     * 退出消息响应构造
     * @param resultCodeEnum 响应信息
     * @param initiative 是否主动退出 true:是 false:否
     * @param platform 对应的平台
     * @return
     */
    public static ProtoMsg.Message logoutResponseMsg(ResultCodeEnum resultCodeEnum, Boolean initiative, Integer platform) {
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder().setType(ProtoMsg.HeadType.LOGOUT_RESPONSE);
        ProtoMsg.LogoutResponse logoutResponseMsg = ProtoMsg.LogoutResponse.newBuilder()
                .setCode(resultCodeEnum.getCode()).setInfo(resultCodeEnum.getDesc())
                .setInitiative(initiative).setPlatform(platform).build();
        mb.setLogoutResponse(logoutResponseMsg);
        return mb.build();
    }

    /**
     * 登录消息响应构造
     * @param resultCodeEnum 响应信息
     * @return
     */
    public static ProtoMsg.Message loginResponseMsg(ResultCodeEnum resultCodeEnum) {
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder().setType(ProtoMsg.HeadType.LOGIN_RESPONSE);
        ProtoMsg.LoginResponse loginResponseMsg = ProtoMsg.LoginResponse.newBuilder()
                .setCode(resultCodeEnum.getCode()).setInfo(resultCodeEnum.getDesc()).build();
        mb.setLoginResponse(loginResponseMsg);
        return mb.build();
    }

    /**
     * 聊天消息响应构造
     * @param resultCodeEnum 响应信息
     * @return
     */
    public static ProtoMsg.Message messageResponseMsg(ResultCodeEnum resultCodeEnum, String msgUid) {
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder().setType(ProtoMsg.HeadType.MESSAGE_RESPONSE);
        ProtoMsg.MessageResponse messageResponseMsg = ProtoMsg.MessageResponse.newBuilder()
                .setMsgUid(msgUid).setCode(resultCodeEnum.getCode()).setInfo(resultCodeEnum.getDesc()).build();
        mb.setMessageResponse(messageResponseMsg);
        return mb.build();
    }
}
