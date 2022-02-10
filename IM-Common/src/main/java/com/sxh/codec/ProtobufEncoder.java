package com.sxh.codec;

import com.sxh.constant.ProtoInstant;
import com.sxh.protobuf.ProtoMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protobuf编码器
 *
 * @author sxh
 * @date 2022/2/8
 */
public class ProtobufEncoder extends MessageToByteEncoder<ProtoMsg.Message> {

    private final static Logger log = LoggerFactory.getLogger(ProtobufEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtoMsg.Message msg, ByteBuf out) {
        log.info("ProtobufEncoder编码：{}", msg);
        out.writeShort(ProtoInstant.MAGIC_CODE);
        out.writeShort(ProtoInstant.VERSION_CODE);
        // 写入消息长度
        out.writeInt(msg.toByteArray().length);
        // 写入消息体
        out.writeBytes(msg.toByteArray());
    }
}
