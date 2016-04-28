package net.dxs.mobilesafe.observer;

import java.util.Date;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.txt.PhoneNumberUtils;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;

/**
 * 短信观察者（监听最新收到的一条短信，处理其内容）
 * 
 * @author lijian-pc
 * @date 2016-4-28 下午1:51:43
 */
public class SmsObserver extends ContentObserver {
	private static final String TAG = "SmsObserver";

	/** 手机短信内容提供者的Uri */
	private static final Uri uri_SMS = Uri.parse("content://sms");

	/** 上下文 */
	private Context mContext;

	/** 记录时间 */
	private Date mDate_fistOne;

	/** 设备策略管理器 */
	private DevicePolicyManager mDpm;
	/** 位置管理器 */
	private LocationManager mLm;
	/** 我的位置监听器 */
	private MyLocationListener mListener_location;

	public SmsObserver(Context context, Handler handler) {
		super(handler);
		this.mContext = context;
	}

	@Override
	public void onChange(boolean selfChange) {// 收发短信时查询出最后一条数据
		super.onChange(selfChange);
		L.i(TAG, "onChange");

		// 每当有一条新短信到来时，我们接收并处理这条短信
		getSmsLatestOne();

		// 每当有新短信到来时，使用我们获取短消息的方法
		// getSmsFromPhone();
	}

	/**
	 * 获取最后一条短信
	 */
	private void getSmsLatestOne() {
		Cursor cursor = mContext.getContentResolver().query(uri_SMS,
				new String[] { "address", "date", "type", "body" }, null, null,
				"_id DESC LIMIT 1");
		if (cursor == null) {
			L.i(TAG, "getSmsLatestOne()-游标为空");
			return;
		}

		if (cursor.moveToNext()) {
			String address = cursor.getString(0);// 手机号
			Date newDate = new Date(cursor.getLong(1));
			if (newDate.equals(mDate_fistOne)) {
				// 因为收到一条短信会在数据库中记录两次，所以这里要做一次排重
				L.i(TAG, "getSmsLatestOne()-时间重复，排重");
				return;
			}
			mDate_fistOne = newDate;
			int type = cursor.getInt(2);// 短信状态（1：收；0：发）
			// 只处理收到的一条短信
			if (type == 1) {
				String body = cursor.getString(3);
				handleSMS(PhoneNumberUtils.formatPhoneNumber(address), body);
			}
		}
	}

	/**
	 * 处理接收到的短信
	 * 
	 * @param phone
	 * @param body
	 */
	private void handleSMS(String phone, String body) {
		L.i(TAG, "handleSMS()-有短信到来了");
		// 判断用户是否开启了手机防盗功能
		boolean protectingStatus = SpUtil.getInstance().getBoolean(
				Constants.LOSTFIND_PROTECTING_STATUS, false);
		if (protectingStatus) {
			L.i(TAG, "手机防盗状态是开启的，解析特殊的短信指定");
			mDpm = (DevicePolicyManager) mContext
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			// 判断是否是安全号码发来的短信
			if (!phone.equals("")
					&& !phone.equals(SpUtil.getInstance().getString(
							Constants.LOSTFIND_SAFE_NUMBER))) {
				L.i(TAG, "来信号码与本地安全号码不匹配");
				return;
			}
			if (Constants.SMS_ACTION_LOCATION.equals(body)) {
				// 获取手机的位置，并且返回
				String safenumber = SpUtil.getInstance().getString(
						Constants.LOSTFIND_SAFE_NUMBER, "");
				mLm = (LocationManager) mContext
						.getSystemService(Context.LOCATION_SERVICE);
				mListener_location = new MyLocationListener(safenumber);
				mLm.requestLocationUpdates("gps", 0, 0, mListener_location);
				L.i(TAG, "获取手机的位置，并且返回给安全号码");
			} else if (Constants.SMS_ACTION_ALARM.equals(body)) {
				MediaPlayer player = MediaPlayer.create(mContext, R.raw.ylzs);
				player.setLooping(false);
				player.setVolume(1.0f, 1.0f);
				player.start();
				L.i(TAG, "播放报警音乐");
			} else if (Constants.SMS_ACTION_WIPEDATA.equals(body)) {
				mDpm.wipeData(0);
				L.i(TAG, "远程清除数据");
			} else if (Constants.SMS_ACTION_LOCKSCREEN.equals(body)) {
				// 重置密码为123456，后期优化这个密码由用户设置
				mDpm.resetPassword("123456", 0);
				mDpm.lockNow();
				L.i(TAG, "远程锁屏");
			}
		} else {
			L.i(TAG, "手机防盗状态是未开启，解析特殊的短信指定");
		}
	}

	// public void getSmsFromPhone() {
	// ContentResolver cr = mContext.getContentResolver();
	// String[] projection = new String[] { "body" };//"_id", "address",
	// "person",, "date", "type
	// String where = " address = '1066321332' AND date >  "
	// + (System.currentTimeMillis() - 10 * 60 * 1000);
	// Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
	// if (null == cur)
	// return;
	// if (cur.moveToNext()) {
	// String number = cur.getString(cur.getColumnIndex("address"));//手机号
	// String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
	// String body = cur.getString(cur.getColumnIndex("body"));
	// //这里我是要获取自己短信服务号码中的验证码~~
	// Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
	// Matcher matcher = pattern.matcher(body);
	// if (matcher.find()) {
	// String res = matcher.group().substring(1, 11);
	// mobileText.setText(res);
	// }
	// }

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
			L.i(TAG, latitude + "-" + longitude + "-" + accuarcy);
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
