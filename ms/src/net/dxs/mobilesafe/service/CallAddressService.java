package net.dxs.mobilesafe.service;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.db.dao.AddressDao;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SpUtil;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * 如果要弹出可以相应触摸点击事件的窗体，需要做3件事情。<br>
 * 1.params.type 设置为可以相应触摸事件<br>
 * 2.修改窗体类型 电话优先级窗体类型TYPE_PRIORITY_PHONE<br>
 * 3.设置权限 android.permission.SYSTEM_ALERT_WINDOW<br>
 * 
 * @author lijian-pc
 * @date 2016-5-19 下午6:47:20
 */
public class CallAddressService extends Service implements OnClickListener,
		OnTouchListener {
	private static final String TAG = "CallAddressService";

	/** 系统窗体的管理器 */
	private WindowManager mWm;
	/** 定义一个电话状态的管理器 */
	private TelephonyManager mTm;

	/** 定义一个呼出电话接收者 */
	private OutCallInnerReceiver mReceiver_outCallInner;

	/** 电话状态监听器 */
	private MyPhoneStateListener mListener_phoneState;

	/** 类的成员变量 显示出来的土司的view对象 */
	private View mV_toastAddress;

	/** 窗体对象参数设置器 */
	private WindowManager.LayoutParams mParams_windowManager;
	/** 当前手指触摸点坐标X */
	private int mStartX;
	/** 当前手指触摸点坐标Y */
	private int mStartY;

	/** 点击事件-点击次数的设置-这里设置长度为2即表示为双击事件 */
	long[] mHits = new long[2];

	/**
	 * 在服务的内部创建了一个广播接收者，希望广播接收者的存活周期跟服务一致。
	 * 
	 * @author lijian-pc
	 * @date 2016-5-19 下午7:13:23
	 */
	private class OutCallInnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String number = getResultData();
			L.i(TAG, "有新的电话打出去了，号码是：" + number);
			String address = AddressDao.find(number);
			if (!TextUtils.isEmpty(address)) {
				L.i(TAG, "启动归属地显示窗体");
				showMyToast(address);
				return;
			}

			/*** 可以根据电话状态做相应业务逻辑处理 **************************************************************/
			// // Action is "android.intent.action.PHONE_STATE"
			// // or "android.intent.action.NEW_OUTGOING_CALL"
			// String action = intent.getAction();
			// L.i("Seven", "action is " + action);
			//
			// if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
			// // "android.intent.extra.PHONE_NUMBER"
			// String outgoingNum = intent
			// .getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			// L.i("Seven", "It's outgoing call. Number is:" + outgoingNum);
			// // return;
			// }
			//
			// // State is RINGING/OFFHOOK/IDLE
			// String state = intent.getStringExtra("state");
			//
			// // Only state is Ringing can get the incoming_number
			// String incomingNum = intent.getStringExtra("incoming_number");
			//
			// // MTK add for dual SIM support
			// String simId = intent.getStringExtra("simId");
			//
			// L.i("Seven", "state is " + state);
			// L.i("Seven", "incomingNum is " + incomingNum);
			// L.i("Seven", "simId is " + simId);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 1.初始化窗体管理器
		mWm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

		// 说明：这里设计（1.注册广播监听电话的呼出；2.注册电话监听，监听电话呼入及电话挂断）
		// 其实光注册一个广播监听，然后根据广播数据中的电话状态字段，去处理对应业务逻辑也可以实现，写法相对复杂些而已，后期可以研究

		// 2.过滤电话打出去的动作(电话呼出)
		// Dynamic register the broadcast
		IntentFilter filter = new IntentFilter();
		// "android.intent.action.PHONE_STATE"
		// filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		// "android.intent.action.NEW_OUTGOING_CALL"
		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		mReceiver_outCallInner = new OutCallInnerReceiver();
		registerReceiver(mReceiver_outCallInner, filter);

		// 3.电话呼入及电话挂断
		mTm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mListener_phoneState = new MyPhoneStateListener();
		// 让监听器监听电话呼叫状态的变化
		mTm.listen(mListener_phoneState, PhoneStateListener.LISTEN_CALL_STATE);
	}

	/**
	 * 电话状态监听
	 * 
	 * @author lijian-pc
	 * @date 2016-5-19 下午7:09:07
	 */
	private class MyPhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// 响铃状态
				String address = AddressDao.find(incomingNumber);
				if (!TextUtils.isEmpty(address)) {
					showMyToast(address);
				}
				L.i(TAG, "响铃状态");
				break;

			case TelephonyManager.CALL_STATE_IDLE:// 挂机状态
				if (mV_toastAddress != null) {
					mWm.removeView(mV_toastAddress);
				}
				L.i(TAG, "挂机状态");
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:// 接听状态
				L.i(TAG, "接听状态");
				break;
			default:
				L.i(TAG, "default");
				break;
			}
		}
	}

	// 服务停止的时候调用的方法
	@Override
	public void onDestroy() {
		// 取消电话状态的监听
		if (mTm != null) {
			mTm.listen(mListener_phoneState, PhoneStateListener.LISTEN_NONE);
			mListener_phoneState = null;
		}
		// 服务停止取消注册广播接受者
		if (mReceiver_outCallInner != null) {
			unregisterReceiver(mReceiver_outCallInner);
			mReceiver_outCallInner = null;
		}
		super.onDestroy();
	}

	/**
	 * 显示自定义吐司
	 * 
	 * @param address
	 *            电话号码的归属地
	 */
	public void showMyToast(String address) {
		mV_toastAddress = View.inflate(this, R.layout.toast_address, null);
		// 给view对象注册一个双击事件
		mV_toastAddress.setOnClickListener(this);

		// 给view对象组成触摸的监听器
		mV_toastAddress.setOnTouchListener(this);

		TextView tv_toast_address = (TextView) mV_toastAddress
				.findViewById(R.id.tv_toast_address);
		tv_toast_address.setText(address);

		// 土司显示的参数
		mParams_windowManager = new WindowManager.LayoutParams();

		// 对齐方式
		mParams_windowManager.gravity = Gravity.LEFT + Gravity.TOP;

		// 指定距离屏幕左边的距离 必须与 Gravity.LEFT同时使用
		mParams_windowManager.x = SpUtil.getInstance().getInt(
				Constants.CALLADDRESS_WINDOWMANAGER_PARAMS_X, 0);
		// 指定距离屏幕上边的距离 必须与 Gravity.TOP同时使用
		mParams_windowManager.y = SpUtil.getInstance().getInt(
				Constants.CALLADDRESS_WINDOWMANAGER_PARAMS_Y, 0);

		// 土司的宽高
		mParams_windowManager.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams_windowManager.width = WindowManager.LayoutParams.WRAP_CONTENT;

		// 土司的参数 不可获取焦点 不可以别点击 保存屏幕常亮
		mParams_windowManager.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		// | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		// 半透明窗体
		mParams_windowManager.format = PixelFormat.TRANSLUCENT;
		// 吐司显示动画
		// params.windowAnimations = R.anim.toast_show;

		// 改用电话优先级的窗体类型，这种类型可以响应触摸事件。
		mParams_windowManager.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
		mWm.addView(mV_toastAddress, mParams_windowManager);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);// 数组向左移位操作
		mHits[mHits.length - 1] = SystemClock.uptimeMillis();// uptimeMillis()手机的开机时间
		if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
			L.i(TAG, "触发双击事件");
			mParams_windowManager.x = (mWm.getDefaultDisplay().getWidth() - mV_toastAddress
					.getWidth()) / 2;
			mParams_windowManager.y = (mWm.getDefaultDisplay().getHeight() - mV_toastAddress
					.getHeight()) / 2;
			mWm.updateViewLayout(mV_toastAddress, mParams_windowManager);
			SpUtil.getInstance().saveInt(
					Constants.CALLADDRESS_WINDOWMANAGER_PARAMS_X,
					mParams_windowManager.x);
			SpUtil.getInstance().saveInt(
					Constants.CALLADDRESS_WINDOWMANAGER_PARAMS_Y,
					mParams_windowManager.y);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 手指按下
			touchAction_down(event);
			break;

		case MotionEvent.ACTION_MOVE:// 手指移动
			touchAction_move(event);
			break;

		case MotionEvent.ACTION_UP:// 手指抬起
			touchAction_up();
			break;
		}
		// True if the listener has consumed the event,
		// false otherwise.
		// true 代表监听器 处理掉了这个事件，false监听器没有处理这个事件。
		return false;
	}

	private void touchAction_down(MotionEvent event) {
		L.i(TAG, "手指按下");
		event.getRawX();
		mStartX = (int) event.getRawX();
		mStartY = (int) event.getRawY();
		L.i(TAG, "oldx:" + mStartX);
		L.i(TAG, "oldy:" + mStartY);
	}

	@SuppressWarnings("deprecation")
	private void touchAction_move(MotionEvent event) {
		L.i(TAG, "手指移动");
		int newX = (int) event.getRawX();
		int newY = (int) event.getRawY();
		L.i(TAG, "newX ： " + newX);
		L.i(TAG, "newY ： " + newY);

		int dx = newX - mStartX;
		int dy = newY - mStartY;
		L.i(TAG, "手指水平方向偏移量dx ： " + dx);
		L.i(TAG, "手指竖直方向偏移量dy ： " + dy);

		// 立刻让控件也跟随着手指移动 dx dy。
		mParams_windowManager.x += dx;
		mParams_windowManager.y += dy;

		// 超出边界修正
		if (mParams_windowManager.x < 0) {
			mParams_windowManager.x = 0;
		}
		if (mParams_windowManager.y < 0) {
			mParams_windowManager.y = 0;
		}
		if (mParams_windowManager.x > (mWm.getDefaultDisplay().getWidth() - mV_toastAddress
				.getWidth())) {
			mParams_windowManager.x = mWm.getDefaultDisplay().getWidth()
					- mV_toastAddress.getWidth();
		}
		if (mParams_windowManager.y > (mWm.getDefaultDisplay().getHeight() - mV_toastAddress
				.getHeight())) {
			mParams_windowManager.y = mWm.getDefaultDisplay().getHeight()
					- mV_toastAddress.getHeight();
		}

		mWm.updateViewLayout(mV_toastAddress, mParams_windowManager);

		// 重复第一步的操作 ，重新初始化手指的开始位置。
		mStartX = (int) event.getRawX();
		mStartY = (int) event.getRawY();
	}

	private void touchAction_up() {
		L.i(TAG, "手指抬起");
		SpUtil.getInstance().saveInt(
				Constants.CALLADDRESS_WINDOWMANAGER_PARAMS_X,
				mParams_windowManager.x);
		SpUtil.getInstance().saveInt(
				Constants.CALLADDRESS_WINDOWMANAGER_PARAMS_Y,
				mParams_windowManager.y);
	}
}
