package com.sxh.codec;

import com.sxh.constant.ProtoInstant;
import com.sxh.protobuf.ProtoMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Protobuf解码器
 *
 * @author sxh
 * @date 2022/2/8
 */
public class ProtobufDecoder extends ByteToMessageDecoder {

    private final static Logger log = LoggerFactory.getLogger(ProtobufDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 标记一下当前的readIndex的位置
        in.markReaderIndex();
        // 判断包头长度
        if (in.readableBytes() < 8) {
            return;
        }
        // 判断魔数
        short magic = in.readShort();
        if (magic != ProtoInstant.MAGIC_CODE) {
            log.error("客户端口令有误: {}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }
        // 读取版本
        short version = in.readShort();
        if (version != ProtoInstant.VERSION_CODE) {
            log.error("客户端版本号有误: {}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }
        // 读取消息的长度
        int length = in.readInt();
        if (length < 0) {
            log.error("消息长度数据有误: {}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }
        // 读到的消息体长度如果小于传送过来的消息长度
        if (in.readableBytes() < length) {
            // 重置读取位置
            in.resetReaderIndex();
            return;
        }

        // 获取消息内容
        byte[] array;
        if (in.hasArray()) {
            ByteBuf slice = in.slice();
            array = slice.array();
        } else {
            array = new byte[length];
            in.readBytes(array, 0, length);
        }

        // 字节转成对象
        ProtoMsg.Message outMsg = ProtoMsg.Message.parseFrom(array);
        log.info("ProtobufDecoder解码，{}", outMsg);
        if (outMsg != null) {
            out.add(outMsg);
        }
    }
}
