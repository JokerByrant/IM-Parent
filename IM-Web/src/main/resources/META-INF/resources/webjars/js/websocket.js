/**
 * WebSocket 连接类
 */

import './chat.js'
import './ByteBuffer.js'
import {initUuid} from '../util'
import auth from '@/utils/auth'

class WsSocket {
  constructor(wconfig, events, that) {
    this.socket = null;
    this.that = that

    this.events = Object.assign({
      onError: function(evt) {},
      onOpen: function(evt) {},
      onMessage: function(evt) {},
      onClose: function(evt) {}
    }, events);

    this.wconfig = wconfig;
    //避免重复连接
    this.lockReconnect = false;
    //重连标识
    this.reconnectFlag = null;
    //重连时间,4S
    this.reconnectTime = 4000;
    //心跳检测时间50s
    this.heartTime = 50000;
    //websocket默认是传输字符串的，需要改为arraybuffer二进制传输类型
    this.binaryType = wconfig.binaryType!=undefined&&wconfig.binaryType!=''?wconfig.binaryType:"arraybuffer";;
    //连接参数
    this.wsUrl = "ws://"+wconfig.serverPort+"/websocket";
    //接收到的二位数组，第一个是消息类型，第二个是消息体
    this.resData = null;
    this.reconnectNum = 3; // 重连次数
    this.count = 0;
  }

  //创建连接
  createWebSocket() {
    try {
			if(typeof(WebSocket) == "undefined") {  
				console.log("您的浏览器不支持WebSocket");  
			}else{
				console.log("您的浏览器支持WebSocket");  
			}
			this.socket = new WebSocket(this.wsUrl);
			//websocket默认是传输字符串的，需要改为arraybuffer二进制传输类型	      	
			this.socket.binaryType = this.binaryType;
			//初始化
			this.init();
		} catch(e) {
			console.log('catch');
			this.reconnect();
		}
  }

  init() {
    var _self = this
    
		this.socket.onclose = function () {
			console.log('链接关闭');
			_self.reconnect();
    };
    
		this.socket.onerror = function() {
			console.log('发生异常了');
			_self.reconnect();
    };
    
		this.socket.onopen = function () {
      _self.count = 0 // 重置重连次数
      if(_self.wconfig.userUid != null) {
        _self.sendLoginRequest()
      } else if (_self.wconfig.binaryUid != null) {
        _self.sendLoginWebRequest()
      }
    };
    
		this.socket.onmessage = function (event) {
			//解包操作
			var length = event.data.byteLength;
      var arr = new ByteBuffer(event.data).short().short().int32().byteArray(null, length-8).unpack();
      var message = new proto.com.cloudfish.bean.msg.Message.deserializeBinary(arr[3]);
      
			//登录响应
			if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.LOGIN_RESPONSE){
				console.log(_self.getNowTime() + "登录响应:" + message.getSessionId());
				//登录成功后，获取sessionId以供后续使用
				if(message.getLoginresponse().getInfo() == "Success"){	
          _self.wconfig.sessionId = message.getSessionId();
				}
        _self.resData = message.getLoginresponse();
      } 
      
      else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.LOGOUT_RESPONSE) {
				console.log(_self.getNowTime() + "退出响应");
        _self.resData= message.getLoginresponse();//TODO
      }
      
      else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.HEART_BEAT){
				console.log(_self.getNowTime() + "心跳响应");
				_self.resData = message.getHeartbeat();
      }
      
      else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.MESSAGE_NOTIFICATION){
				console.log(_self.getNowTime() + "通知");
				_self.resData = message.getNotification();
      }
      
      else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.SINGLE_REQUEST) {
				console.log(_self.getNowTime() + "聊天信息");
				_self.resData =  message.getSinglechatting();
      }
      
      else if(message.getType() === proto.com.cloudfish.bean.msg.HeadType.LOGIN_REQUEST_WEB) {
        console.log(_self.getNowTime() + "二维码扫描登录信息");
        _self.resData =  message.getLoginweb();
        //来自app端，状态等于1，代表app已经同意登录，这里可以进行登录操作及跳转了
        if(_self.resData.getFromuid() == 0 && _self.resData.getStatus() == 1){
          _self.wconfig.userUid = _self.resData.getUseruid();		    		
          _self.sendLoginRequest();
        }
      }
      
			_self.heartCheck(_self.socket, _self.wconfig)
      _self.events.onMessage(_self.resData, _self)
    }
  }

  /**
   * 发送Login_Web请求
   */
  sendLoginWebRequest() {
    //发送Web端初始请求
    var loginWeb = new proto.com.cloudfish.bean.msg.LoginWeb();
    console.log('发出uid建立连接：' + this.wconfig.binaryUid);
    loginWeb.setWebuid(this.wconfig.binaryUid); // 二维码uid
    loginWeb.setStatus(0); // 登录状态 ===> 0:未登录 1:确认登录 2:退出登录
    loginWeb.setFromuid(1); // 消息来自哪里 ===> 0:app 1:web
    
    var message = new proto.com.cloudfish.bean.msg.Message();
    message.setSequence(0);
    message.setType(proto.com.cloudfish.bean.msg.HeadType.LOGIN_REQUEST_WEB);
    //message.setSessionId(loginData.sessionId);
    message.setLoginweb(loginWeb);
    var bytes = message.serializeBinary();
    var sbuf = new ByteBuffer();
    var buffer = sbuf.short(this.wconfig.imMagicCode).short(this.wconfig.imVersionCode).int32(bytes.byteLength)
          .byteArray(bytes, bytes.byteLength)
          .pack();
    // 发送登录信息
    this.socket.send(buffer);
  }

  /**
   * 发送登录请求
   */
  sendLoginRequest() {
    //发送登录请求
    var loginRequest = new proto.com.cloudfish.bean.msg.LoginRequest();
    loginRequest.setUseruid(this.wconfig.userUid);
    loginRequest.setDevid(this.wconfig.token);
    loginRequest.setPlatform(this.wconfig.platform);
    loginRequest.setToken(this.wconfig.token);
    var message = new proto.com.cloudfish.bean.msg.Message();
    message.setType(proto.com.cloudfish.bean.msg.HeadType.LOGIN_REQUEST);
    message.setSequence(0);
    //message.setSessionId(wconfig.sessionId);
    message.setLoginrequest(loginRequest);
        var bytes = message.serializeBinary();
        var sbuf = new ByteBuffer();
    var buffer = sbuf.short(this.wconfig.imMagicCode).short(this.wconfig.imVersionCode).int32(bytes.byteLength)
                  .byteArray(bytes, bytes.byteLength)
          .pack();
    console.log("登录信息如下：")
    console.log(this.wconfig)
    this.socket.send(buffer);
  }

  /**
	 * 消息的发送
	 * msgData 消息结构体，包括以下几个属性
	 * @param toUser 消息接收人
	 * @param textContent 消息内容
	 * @param contentType 消息类型
	 */
  send(msgData) {
    //组装消息
		var messageRequest = new proto.com.cloudfish.bean.msg.SingleChatting();
		messageRequest.setChattingUid(initUuid());
		messageRequest.setUserUid(this.wconfig.userUid);
		messageRequest.setRecvUid(msgData.toUser);
		messageRequest.setContent(msgData.textContent);
    messageRequest.setContentType(msgData.contentType);
    console.log(messageRequest, '即将发送的信息')
		var message = new proto.com.cloudfish.bean.msg.Message();
		message.setType(proto.com.cloudfish.bean.msg.HeadType.SINGLE_REQUEST);
		message.setSequence(0);
		message.setSessionId(this.wconfig.sessionId);
		message.setSinglechatting(messageRequest);
		var bytes = message.serializeBinary();
		var sbuf = new ByteBuffer();
		var buffer = sbuf.short(this.wconfig.imMagicCode).short(this.wconfig.imVersionCode).int32(bytes.byteLength)
					.byteArray(bytes, bytes.byteLength)
					.pack();
		this.socket.send(buffer);
  }

  //重新连接
  reconnect() {
    var _self = this
		if(this.lockReconnect) {
			return;
		};
		this.lockReconnect = true;
		//没连接上会一直重连，设置延迟避免请求过多
		this.reconnectFlag && clearTimeout(this.reconnectFlag);
		this.reconnectFlag = setTimeout(function () {
      if (_self.count < _self.reconnectNum) { // 限制重连次数
        console.log("第" + _self.count + '次重连')
        _self.count++
        console.log("reconnecting...")
        _self.createWebSocket();
      } else { // 重连失败，重置链接到login页面
        console.log('重连失败！重置到login页面，清除登录信息！')
        _self.that.$store.dispatch('logout')
        if(_self.that.$route.name != 'login') {
          location.reload()
        } else {
          _self.that.$message('无法连接到服务器...')
          _self.that.state = 6
        }
      }
			_self.lockReconnect = false;
		}, _self.reconnectTime);//重连时间
	}

  //WebSocket心跳检测
  heartCheck(socket, wconfig) {
    var _self = this
    var timeout = 50000 //心跳检测时间
    var timeoutObj = null
    var serverTimeoutObj = null

    timeoutObj && clearTimeout(timeoutObj);
    serverTimeoutObj && clearTimeout(serverTimeoutObj);
    timeoutObj = setTimeout(function(){
      //这里发送一个心跳，后端收到后，返回一个心跳消息，
      //onmessage拿到返回的心跳就说明连接正常
      console.log(_self.getNowTime() +' socket心跳检测');
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

      console.log(message, 'message')

      var bytes = message.serializeBinary();
      var sbuf = new ByteBuffer();
      var buffer = sbuf.short(wconfig.imMagicCode).short(wconfig.imVersionCode).int32(bytes.byteLength)
                        .byteArray(bytes, bytes.byteLength)
                        .pack();
      console.log(socket, "ready for send heartcheck")
      console.log(wconfig, "wconfig配置")
      socket.send(buffer);
      //发送心跳 end

      // serverTimeoutObj = setTimeout(function() {
      //   socket.close();
      // }, timeout);

    }, timeout)
  }

  // 关闭WebSocket连接
  close() {
    this.socket.close()
    console.log(this.socket.readyState, "Socket连接断开！")
  }

  p(s) {
    return s < 10 ? '0' + s : s;
  }
  getNowTime() {
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
    return year + '-' + this.p(month) + "-" + this.p(date) + " " + this.p(h) + ':' + this.p(m) + ":" + this.p(s);
  }
}

export default WsSocket;
