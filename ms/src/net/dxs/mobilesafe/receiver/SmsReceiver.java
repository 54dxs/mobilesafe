package net.dxs.mobilesafe.receiver;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SpUtil;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

/**
 * 短信广播接收者
 * 
 * @author lijian-pc
 * @date 2016-4-27 下午4:10:25
 */
public class SmsReceiver extends BroadcastReceiver {

	private static final String TAG = "SmsReceiver";
	/** 设备策略管理器 */
	private DevicePolicyManager mDpm;
	/** 位置管理器 */
	private LocationManager mLm;
	/** 我的位置监听器 */
	private MyLocationListener mListener_location;

	@Override
	public void onReceive(Context context, Intent intent) {
		L.i(TAG, "有短信到来了");
		// 判断用户是否开启了手机防盗功能
		boolean protectingstatus = SpUtil.getInstance().getBoolean(
				Constants.LOSTFIND_PROTECTING_STATUS, false);
		if (protectingstatus) {
			L.i(TAG, "手机防盗状态是开启的，解析特殊的短信指定");
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			mDpm = (DevicePolicyManager) context
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			for (Object object : objs) {
				SmsMessage smsMessage = SmsMessage
						.createFromPdu((byte[]) object);
				// 判断是否是安全号码发来的短信
				String sender = smsMessage.getOriginatingAddress();
				if (!sender.equals("")
						&& !sender.equals(SpUtil.getInstance().getString(
								Constants.LOSTFIND_SAFE_NUMBER))) {
					continue;
				}
				String body = smsMessage.getMessageBody();
				if (Constants.SMS_ACTION_LOCATION.equals(body)) {
					abortBroadcast();// 终止广播，不让小偷看到短信
					// 获取手机的位置，并且返回
					String safenumber = SpUtil.getInstance().getString(
							Constants.LOSTFIND_SAFE_NUMBER, "");
					mLm = (LocationManager) context
							.getSystemService(Context.LOCATION_SERVICE);
					mListener_location = new MyLocationListener(safenumber);
					mLm.requestLocationUpdates("gps", 0, 0, mListener_location);
					L.i(TAG, "获取手机的位置，并且返回给安全号码");
				} else if (Constants.SMS_ACTION_ALARM.equals(body)) {
					abortBroadcast();
					MediaPlayer player = MediaPlayer
							.create(context, R.raw.ylzs);
					player.setLooping(false);
					player.setVolume(1.0f, 1.0f);
					player.start();
					L.i(TAG, "播放报警音乐");
				} else if (Constants.SMS_ACTION_WIPEDATA.equals(body)) {
					abortBroadcast();
					mDpm.wipeData(0);
					L.i(TAG, "远程清除数据");
				} else if (Constants.SMS_ACTION_LOCKSCREEN.equals(body)) {
					abortBroadcast();
					// 重置密码为123456，后期优化这个密码由用户设置
					mDpm.resetPassword("123456", 0);
					mDpm.lockNow();
					L.i(TAG, "远程锁屏");
				}
			}
		}
	}

	/**
	 * 我的位置监听器
	 * 
	 * @author lijian-pc
	 * @date 2016-4-27 下午4:25:35
	 */
	private class MyLocationListener implements LocationListener {
		private String safenumber;

		public MyLocationListener(String safenumber) {
			this.safenumber = safenumber;
		}

		// 当位置变化的时候调用的方法
		@Override
		public void onLocationChanged(Location location) {
			String latitude = "latitude:" + location.getLatitude();// 纬度
			String longitude = "longitude:" + location.getLongitude();// 经度
			String accuarcy = "accuarcy:" + location.getAccuracy();// 精确度

			// 获得一个短信管理器
			SmsManager smsManager = SmsManager.getDefault();
			// 发送短信
			// 参数说明：<br>
			// -- destinationAddress：目标电话号码
			// -- scAddress：短信中心号码，测试可以不填
			// -- text: 短信内容
			// -- sentIntent：发送 -->中国移动 --> 中国移动发送失败 --> 返回发送成功或失败信号 --> 后续处理
			// 即，这个意图包装了短信发送状态的信息
			// -- deliveryIntent： 发送 -->中国移动 --> 中国移动发送成功 --> 返回对方是否收到这个信息 -->
			// 后续处理 即：这个意图包装了短信是否被对方收到的状态信息（供应商已经发送成功，但是对方没有收到）。
			smsManager.sendTextMessage(safenumber, null, latitude + "-"
					+ longitude + "-" + accuarcy, null, null);
			mLm.removeUpdates(mListener_location);
			mListener_location = null;
		}

		// 当状态变化的时候调用的方法。可用--》不可以 不可以--》可用
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		// 当一个位置提供者可用的时候
		@Override
		public void onProviderEnabled(String provider) {

		}

		// 当一个位置提供者不可用的时候
		@Override
		public void onProviderDisabled(String provider) {

		}

	}

}
