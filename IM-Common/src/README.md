### 生成ProtoBuf
`ProtoBuf.proto` 生成 `Java` 文件，命令如下：
```
protoc --java_out=${项目所在目录}\IM-Parent\IM-Common\src\main\java\ ProtoMsg.proto -I ${项目所在目录}\IM-Parent\IM-Common\src\main\java\com\sxh\protobuf\
```
