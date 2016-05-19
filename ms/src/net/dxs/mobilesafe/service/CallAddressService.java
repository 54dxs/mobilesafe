package net.dxs.mobilesafe.service;

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
 * 
 * 如果要弹出来 可以相应触摸点击事件的窗体，需要做3件事情。<br>
 * 1.params.type 设置为可以相应触摸事件<br>
 * 2.修改窗体类型 电话优先级窗体类型TYPE_PRIORITY_PHONE<br>
 * 3.设置权限 android.permission.SYSTEM_ALERT_WINDOW<br>
 * 
 * @author lijian-pc
 * @date 2016-5-19 下午6:47:20
 */
public class CallAddressService extends Service {
	private static final String TAG = "CallAddressService";

	/** 系统窗体的管理器 */
	private WindowManager mWm;
	/** 定义一个电话状态的管理器 */
	private TelephonyManager mTm;

	/** 定义一个呼出电话接收者 */
	private OutCallInnerReceiver mReceiver_outCallInner;

	/** 电话状态监听器 */
	private MyPhoneStateListener listener;

	/** 类的成员变量 显示出来的土司的view对象 */
	private View view;

	private WindowManager.LayoutParams params;

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
				// Toast.makeText(getApplicationContext(), address, 1).show();
				L.i(TAG, "启动归属地显示窗体");
				showMyToast(address);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化窗体管理器
		mWm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		mReceiver_outCallInner = new OutCallInnerReceiver();
		// 过滤电话打出去的动作
		IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(mReceiver_outCallInner, filter);

		mTm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneStateListener();
		// 让监听器监听电话呼叫状态的变化
		mTm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
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
					// Toast.makeText(getApplicationContext(), address,
					// Toast.LENGTH_LONG).show();
					showMyToast(address);
				}
				break;

			case TelephonyManager.CALL_STATE_IDLE:// 空闲
				if (view != null) {
					mWm.removeView(view);
					mWm = null;
				}
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:// 接听状态
				break;
			}
		}
	}

	// 服务停止的时候调用的方法
	@Override
	public void onDestroy() {
		// 取消电话状态的监听
		mTm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		// 服务停止取消注册广播接受者
		unregisterReceiver(mReceiver_outCallInner);
		mReceiver_outCallInner = null;

		super.onDestroy();
	}

	long[] mHits = new long[2];

	/**
	 * 显示自定义吐司
	 * 
	 * @param address
	 *            电话号码的归属地
	 */
	public void showMyToast(String address) {
		view = View.inflate(this, R.layout.toast_address, null);
		// 给view对象注册一个双击事件
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);// 数组向左移位操作
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();// uptimeMillis()手机的开机时间
				if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
					params.x = (mWm.getDefaultDisplay().getWidth() - view
							.getWidth()) / 2;
					params.y = (mWm.getDefaultDisplay().getHeight() - view
							.getHeight()) / 2;
					mWm.updateViewLayout(view, params);
					SpUtil.getInstance().saveInt("paramsx", params.x);
					SpUtil.getInstance().saveInt("paramsy", params.y);
				}
			}
		});

		// 给view对象组成触摸的监听器
		view.setOnTouchListener(new OnTouchListener() {

			int startX;
			int startY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 手指按下
					L.i(TAG, "手指按下");
					event.getRawX();
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					L.i(TAG, "oldx:" + startX);
					L.i(TAG, "oldy:" + startY);

					break;
				case MotionEvent.ACTION_MOVE:// 手指移动
					L.i(TAG, "手指移动");
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					L.i(TAG, "newX ： " + newX);
					L.i(TAG, "newY ： " + newY);

					int dx = newX - startX;
					int dy = newY - startY;
					L.i(TAG, "手指水平方向偏移量dx ： " + dx);
					L.i(TAG, "手指竖直方向偏移量dy ： " + dy);

					// 立刻让控件也跟随着手指移动 dx dy。
					params.x += dx;
					params.y += dy;

					// 超出边界修正
					if (params.x < 0) {
						params.x = 0;
					}
					if (params.y < 0) {
						params.y = 0;
					}
					if (params.x > (mWm.getDefaultDisplay().getWidth() - view
							.getWidth())) {
						params.x = mWm.getDefaultDisplay().getWidth()
								- view.getWidth();
					}
					if (params.y > (mWm.getDefaultDisplay().getHeight() - view
							.getHeight())) {
						params.y = mWm.getDefaultDisplay().getHeight()
								- view.getHeight();
					}

					mWm.updateViewLayout(view, params);

					// 重复第一步的操作 ，重新初始化手指的开始位置。
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();

					break;
				case MotionEvent.ACTION_UP:// 手指抬起
					L.i(TAG, "手指抬起");
					SpUtil.getInstance().saveInt("paramsx", params.x);
					SpUtil.getInstance().saveInt("paramsy", params.y);
					break;

				}

				return false;// True if the listener has consumed the event,
								// false otherwise.
								// true 代表监听器 处理掉了这个事件，false监听器没有处理这个事件。
			}
		});

		TextView tv_toast_address = (TextView) view
				.findViewById(R.id.tv_toast_address);
		tv_toast_address.setText(address);

		// 土司显示的参数
		params = new WindowManager.LayoutParams();

		// 对齐方式
		params.gravity = Gravity.LEFT + Gravity.TOP;

		// 指定距离屏幕左边的距离 必须与 Gravity.LEFT同时使用
		params.x = SpUtil.getInstance().getInt("paramsx", 0);
		// 指定距离屏幕上边的距离 必须与 Gravity.TOP同时使用
		params.y = SpUtil.getInstance().getInt("paramsy", 0);

		// 土司的宽高
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;

		// 土司的参数 不可获取焦点 不可以别点击 保存屏幕常亮
		params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		// | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		// 半透明窗体
		params.format = PixelFormat.TRANSLUCENT;
		// 吐司显示动画
		// params.windowAnimations = R.anim.toast_show;

		// 改用电话优先级的窗体类型，这种类型可以响应触摸事件。
		params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
		mWm.addView(view, params);

	}

}
