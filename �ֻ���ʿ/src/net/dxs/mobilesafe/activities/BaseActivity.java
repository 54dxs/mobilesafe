package net.dxs.mobilesafe.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Activity基类
 * 
 * @author lijian
 * @date 2016-4-8 下午5:26:44
 */
public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			parserMessage(msg);
		};
	};

	/**
	 * 处理Handler Message
	 * 
	 * @param msg 消息包
	 */
	protected void parserMessage(Message msg) {
		if (msg == null) {
			return;
		}
	}

	/**
	 * 发送handler消息
	 * 
	 * @param what
	 * @param obj
	 */
	public void sendMsg(int what, Object obj) {
		Message _msg = mHandler.obtainMessage();
		_msg.what = what;
		_msg.obj = obj;
		mHandler.sendMessage(_msg);
	}

}
