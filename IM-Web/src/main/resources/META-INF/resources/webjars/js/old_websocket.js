/**
 * websocket的构造方法
 * @param wconfig 连接websocket的基础参数
 */

import './chat.js'
import './ByteBuffer.js'
import {initUuid} from '../util'

proto.com.cloudfish.WebSocketHtml5 = function(wconfig, isLogin){
	//<!-- ws客户端	 -->
	var socket;
	var wconfig = wconfig;
	//把本身引用负值到一变量上
	var _self = this;
	//避免重复连接
	var lockReconnect = false;
	//重连标识
	var reconnectFlag;
	//重连时间,4S
	var reconnectTime = 4000;
	//心跳检测时间50s
	var heartTime = 50000;
	//websocket默认是传输字符串的，需要改为arraybuffer二进制传输类型
	var binaryType = wconfig.binaryType!=undefined&&wconfig.binaryType!=''?wconfig.binaryType:"arraybuffer";;
	//连接参数
  var wsUrl = "ws://"+wconfig.serverPort+"/websocket";
	//接收到的二位数组，第一个是消息类型，第二个是消息体
  var resData;
  var reconnectNum = 3; // 重连次数
  var count = 0;

	//创建连接
	this.createWebSocket = function () {
		try {
			if(typeof(WebSocket) == "undefined") {  
				console.log("您的浏览器不支持WebSocket");  
			}else{
				console.log("您的浏览器支持WebSocket");  
			}
			socket = new WebSocket(wsUrl);
			//websocket默认是传输字符串的，需要改为arraybuffer二进制传输类型	      	
			socket.binaryType = binaryType;
			//初始化
			init();
		} catch(e) {
			console.log('catch');
			reconnect();
		}
	}	
	//初始化
	function init() {
		socket.onclose = function () {
			console.log('链接关闭');
			reconnect();
		};
		socket.onerror = function() {
			console.log('发生异常了');
			reconnect();
		};
		socket.onopen = function () {
      count = 0 // 重置重连次数
      if (!isLogin) { // 如果尚未登录
        sendLoginWebRequest()
      } else {
        sendLoginRequest()
      }
		};
		socket.onmessage = function (event) {
			//解包操作
			var length = event.data.byteLength;
			console.log(length);
			var arr = new ByteBuffer(event.data).short().short().int32().byteArray(null, length-8).unpack();
			var message = new proto.com.cloudfish.bean.msg.Message.deserializeBinary(arr[3]);
			console.log(message.getType());
			//登录响应
			if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.LOGIN_RESPONSE){
				console.log(getNowTime() + "登录响应:" + message.getSessionId());
				//登录成功后，获取sessionId以供后续使用
				if(message.getLoginresponse().getInfo() == "Success"){	
					wconfig.sessionId = message.getSessionId();
				}
				resData = message.getLoginresponse();
			//退出响应
			}else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.LOGOUT_RESPONSE){
				console.log(getNowTime() + "退出响应");
				resData= message.getLoginresponse();//TODO
			}else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.HEART_BEAT){
				console.log(getNowTime() + "心跳响应");
				resData = message.getHeartbeat();
			}else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.MESSAGE_NOTIFICATION){
				console.log(getNowTime() + "通知");
				resData = message.getNotification();
			}else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.SINGLE_REQUEST) {
				console.log(getNowTime() + "聊天信息");
				resData =  message.getSinglechatting();
			}else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.LOGIN_REQUEST_WEB) {
		    	console.log(getNowTime() + "二维码扫描登录信息");
		    	resData =  message.getLoginweb();
		    	//来自app端，状态等于1，代表app已经同意登录，这里可以进行登录操作及跳转了
		    	if(resData.getFromuid() == 0 && resData.getStatus() == 1){
            wconfig.userUid = resData.getUseruid();		    		
            sendLoginRequest();
				}
			}
			heartCheck.start(socket, wconfig);
			// 接收消息并渲染到主页上
			receiveResData(resData, _self);
		}
  }
  
  /**
   * 发送Login_Web请求
   */
  function sendLoginWebRequest() {
    //发送Web端初始请求
    var loginWeb = new proto.com.cloudfish.bean.msg.LoginWeb();
    console.log('发出uid建立连接：' + wconfig.binaryUid);
    loginWeb.setWebuid(wconfig.binaryUid); // 二维码uid
    loginWeb.setStatus(0); // 登录状态 ===> 0:未登录 1:确认登录 2:退出登录
    loginWeb.setFromuid(1); // 消息来自哪里 ===> 0:app 1:web
    
    var message = new proto.com.cloudfish.bean.msg.Message();
    message.setSequence(0);
    message.setType(proto.com.cloudfish.bean.msg.HeadType.LOGIN_REQUEST_WEB);
    //message.setSessionId(loginData.sessionId);
    message.setLoginweb(loginWeb);
    var bytes = message.serializeBinary();
    var sbuf = new ByteBuffer();
    var buffer = sbuf.short(wconfig.imMagicCode).short(wconfig.imVersionCode).int32(bytes.byteLength)
          .byteArray(bytes, bytes.byteLength)
          .pack();
    // 发送登录信息
    socket.send(buffer);
  }

  /**
   * 发送登录请求
   */
  function sendLoginRequest() {
    //发送登录请求
    var loginRequest = new proto.com.cloudfish.bean.msg.LoginRequest();
    loginRequest.setUseruid(wconfig.userUid);
    loginRequest.setDevid(wconfig.token);
    loginRequest.setPlatform(wconfig.platform);
    loginRequest.setToken(wconfig.token);
    var message = new proto.com.cloudfish.bean.msg.Message();
    message.setType(proto.com.cloudfish.bean.msg.HeadType.LOGIN_REQUEST);
    message.setSequence(0);
    //message.setSessionId(wconfig.sessionId);
    message.setLoginrequest(loginRequest);
        var bytes = message.serializeBinary();
        var sbuf = new ByteBuffer();
    var buffer = sbuf.short(wconfig.imMagicCode).short(wconfig.imVersionCode).int32(bytes.byteLength)
                  .byteArray(bytes, bytes.byteLength)
          .pack();
    console.log("登录信息如下：")
    console.log(wconfig)
    socket.send(buffer);
  }
	/**
	 * 消息的发送
	 * msgData 消息结构体，包括以下几个属性
	 * @param toUser 消息接收人
	 * @param textContent 消息内容
	 * @param contentType 消息类型
	 */
	this.send = function (msgData){
		//组装消息
		var messageRequest = new proto.com.cloudfish.bean.msg.SingleChatting();
		messageRequest.setChattingUid(initUuid());
		messageRequest.setUserUid(wconfig.userUid);
		messageRequest.setRecvUid(msgData.toUser);
		messageRequest.setContent(msgData.textContent);
		messageRequest.setContentType(msgData.contentType);
		var message = new proto.com.cloudfish.bean.msg.Message();
		message.setType(proto.com.cloudfish.bean.msg.HeadType.SINGLE_REQUEST);
		message.setSequence(0);
		message.setSessionId(wconfig.sessionId);
		message.setSinglechatting(messageRequest);
		var bytes = message.serializeBinary();
		var sbuf = new ByteBuffer();
		var buffer = sbuf.short(wconfig.imMagicCode).short(wconfig.imVersionCode).int32(bytes.byteLength)
					.byteArray(bytes, bytes.byteLength)
					.pack();
		socket.send(buffer);
	}

	//重新连接
	function reconnect() {
		if(lockReconnect) {
			return;
		};
		lockReconnect = true;
		//没连接上会一直重连，设置延迟避免请求过多
		reconnectFlag && clearTimeout(reconnectFlag);
		reconnectFlag = setTimeout(function () {
      if (count < reconnectNum) { // 限制重连次数
        count++
        console.log("reconnecting...")
        _self.createWebSocket();
      } else { // 重新获取服务器Ip
        initWebSocket();
      }
			lockReconnect = false;
		}, reconnectTime);//重连时间
	}	
}
//心跳检测
var heartCheck =  {
    timeout: 50000,//心跳检测时间
    timeoutObj: null,
    serverTimeoutObj: null,
    start: function(socket, wconfig){
        var self = this;
        this.timeoutObj && clearTimeout(this.timeoutObj);
        this.serverTimeoutObj && clearTimeout(this.serverTimeoutObj);
        this.timeoutObj = setTimeout(function(){
            //这里发送一个心跳，后端收到后，返回一个心跳消息，
            //onmessage拿到返回的心跳就说明连接正常
            console.log(getNowTime() +' socket心跳检测');
            //发送心跳 start
			var heartBeat = new proto.com.cloudfish.bean.msg.MessageHeartBeat();
			heartBeat.setUid(wconfig.userUid);
			var data={"from":"WEB_CLIENT"};
			heartBeat.setJson(JSON.stringify(data));
			heartBeat.setSeq(0);
			var message = new proto.com.cloudfish.bean.msg.Message();
			message.setSequence(0);
			message.setType(proto.com.cloudfish.bean.msg.HeadType.HEART_BEAT);
			message.setSessionId(wconfig.sessionId);
			message.setHeartbeat(heartBeat);
            var bytes = message.serializeBinary();
			var sbuf = new ByteBuffer();
			var buffer = sbuf.short(wconfig.imMagicCode).short(wconfig.imVersionCode).int32(bytes.byteLength)
                       .byteArray(bytes, bytes.byteLength)
                       .pack();
			socket.send(buffer);
			//发送心跳 end
            self.serverTimeoutObj = setTimeout(function() {
                socket.close();
            }, self.timeout);
        }, this.timeout)
    }
};
/**
 * 获取系统当前时间
 * @returns
 */
function p(s) {
    return s < 10 ? '0' + s : s;
}
function getNowTime() {
    var myDate = new Date();
    //获取当前年
    var year = myDate.getFullYear();
    //获取当前月
    var month = myDate.getMonth() + 1;
    //获取当前日
    var date = myDate.getDate();
    var h = myDate.getHours();       //获取当前小时数(0-23)
    var m = myDate.getMinutes();     //获取当前分钟数(0-59)
    var s = myDate.getSeconds();
    return year + '-' + p(month) + "-" + p(date) + " " + p(h) + ':' + p(m) + ":" + p(s);
}
