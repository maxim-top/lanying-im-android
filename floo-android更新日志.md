# floo-android更新日志

## v2.3.0 - 2020/04/25

变更:
1. 新的floo包
   修改获取ChatManager UserManager RosterManager GroupManager的方式
   
## v2.2.1 - 2020/04/07

变更：

1. 网络请求超时时间更改为20s  

## v2.2.0 - 2020/03/27

变更：

1. 将同步接口更换成异步方式，可以直接在回调做相关业务处理
2. 增加BMXDataCallBack<T> BMXCallBack两种回调低缓BMXErrorCode返回
    
    ```
    /**
     * 获取所有会话
     **/
    - public void getAllConversations(BMXDataCallBack<BMXConversationList> callBack) {
        mService.getAllConversations(callBack);
    }
	```
	
	```
	 /**
     * 设置昵称
     **/
    - public void setNickname(String nickname, BMXCallBack callBack) {
        mService.setNickname(nickname, callBack);
    }

	```


## v2.1.0 - 2020/03/17

新增:

1. 会话：新增更新会话中消息的扩展字段接口

	```
	/// 更新一条数据库存储消息的扩展字段信息
	/// @param message 需要更改扩展信息的消息此时msg部分已经更新扩展字椴信息
	- updateMessageExtension:(BMXMessage msg);
	```

2. 命令消息：新增创建命令消息的接口
	
	```
	/// 创建发送命令消息(命令消息通过content字段或者extension字段存放命令信息)
	/// @param type 单群类型
	/// @param from 消息发送者
	/// @param to 消息接收者
	/// @param mtype 消息类型
	/// @param content 消息内容
	- public void sendCommandMessage(BMXMessage.MessageType type, long from, long to,
            String content)
	```

3.  新增收到命令消息通知接口

	```
	/**
	 * 收到命令消息
	 **/
	- public void onReceiveCommandMessages(BMXMessageList list) {}
	```
	


