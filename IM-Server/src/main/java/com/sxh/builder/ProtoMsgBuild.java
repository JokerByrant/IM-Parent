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
        ProtoMsg.MessageHeartBeat heartBeatMsg = ProtoMsg.MessageHeartBeat.newBuilder().setUid(userUid).build();
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
        ProtoMsg.LogoutResponse logoutResponseMsg = ProtoMsg.LogoutResponse.newBuilder().setResult(true)
                .setCode(resultCodeEnum.getCode()).setInfo(resultCodeEnum.getDesc())
                .setInitiative(initiative).setPlatform(platform).build();
        mb.setLogoutResponse(logoutResponseMsg);
        return mb.build();
    }

    /**
     * 登录消息响应构造
     * @param resultCodeEnum 响应信息
     * @param apiAuth 用户登录成功后的token信息
     * @return
     */
    public static ProtoMsg.Message loginResponseMsg(ResultCodeEnum resultCodeEnum, String apiAuth) {
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder().setType(ProtoMsg.HeadType.LOGIN_RESPONSE);
        ProtoMsg.LoginResponse loginResponseMsg = ProtoMsg.LoginResponse.newBuilder().setResult(true)
                .setCode(resultCodeEnum.getCode()).setInfo(resultCodeEnum.getDesc())
                .setExpose(1).setApiAuth(apiAuth).build();
        mb.setLoginResponse(loginResponseMsg);
        return mb.build();
    }
}
