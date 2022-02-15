package com.sxh.constant;

/**
 * 返回消息枚举类
 * @author sxh
 * @date 2022/2/15
 */
public enum ResultCodeEnum {
    SUCCESS(0, "Success"),
    AUTH_FAILED(1, "登录失败"),
    NO_TOKEN(2, "没有授权码"),
    UNKNOWN_ERROR(3, "未知错误"),
    CLUSTER_SEND_ERROR(4, "群聊信息发送失败");

    private Integer code;
    private String desc;

    ResultCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

