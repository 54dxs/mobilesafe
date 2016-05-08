package net.dxs.mobilesafe.service;

import java.lang.reflect.Method;
import java.util.Date;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.db.dao.BlacknumberDao;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.txt.PhoneNumberUtils;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.ITelephony;

/**
 * 通讯卫士-黑名单的监听服务
 * 
 * @author lijian-pc
 * @date 2016-5-6 下午2:09:47
 */
public class CallSmsSafeService extends Service {
	private static final String TAG = "CallSmsSafeService";

	/** 手机短信内容提供者的Uri */
	private static final Uri URI_SMS = Uri.parse("content://sms");
	/** 通话记录内容提供者的Uri */
	private static final Uri URI_CALL_LOG = Uri
			.parse("content://call_log/calls/");

	/** 黑名单数据库操作 */
	private BlacknumberDao mDao_blackNumber;
	/** 电话管理器 */
	private TelephonyManager mTm;

	/** 短信的广播接收者 */
	private InnerSmsReceiver mReceiver_innerSms;

	/** 记录时间 */
	private Date mDate_fistOne;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDao_blackNumber = new BlacknumberDao(this);
		// 黑名单电话拦截
		mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);// 获得电话管理器
		mTm.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);// 监听来电状态

		// // 黑名单短信拦截
		// mReceiver_innerSms = new InnerSmsReceiver();
		// IntentFilter filter = new IntentFilter(
		// "android.provider.Telephony.SMS_RECEIVED");// 监听短信广播
		// filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);// 将优先级设置为最高
		// registerReceiver(mReceiver_innerSms, filter);// 注册广播

		// 黑名单短信拦截
		SMSObserver();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver_innerSms);// 注销广播
		mReceiver_innerSms = null;
		super.onDestroy();
	}

	/**
	 * 电话状态监听器
	 * 
	 * @author lijian-pc
	 * @date 2016-5-6 下午2:32:17
	 */
	private class MyPhoneListener extends PhoneStateListener {

		// 监听电话状态改变
		@Override
		public void onCallStateChanged(int state, final String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// 响铃状态
				callStateRinging(incomingNumber);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 电话响铃时的处理
	 * 
	 * @param incomingNumber
	 */
	private void callStateRinging(final String incomingNumber) {
		String mode = mDao_blackNumber.findMode(incomingNumber);
		if (!TextUtils.isEmpty(mode)
				&& (mode.equals(Constants.SAFE_MODE_ALL) || mode
						.equals(Constants.SAFE_MODE_PHONE))) {
			L.i(TAG, incomingNumber + "黑名单电话,挂断电话...");
			// 挂断电话
			endCall();
			ContentResolver resolver = getContentResolver();
			resolver.registerContentObserver(URI_CALL_LOG, false,
					new ContentObserver(new Handler()) {
						@Override
						public void onChange(boolean selfChange) {
							super.onChange(selfChange);
							L.i(TAG, "观察者观察到通话记录数据变化了,删除...");
							deleteCallLog(incomingNumber);// 删除呼叫记录
							// 监测到内容发生改变,并执行删除操作后,内容观察者没必要继续观察,删除内容观察者
							getContentResolver()
									.unregisterContentObserver(this);
						}
					});
		}
	}

	/**
	 * 挂断电话
	 */
	public void endCall() {
		try {
			Class<?> clazz = CallSmsSafeService.class.getClassLoader()
					.loadClass("android.os.ServiceManager");
			Method method = clazz.getMethod("getService", String.class);
			ITelephony iTelephony = ITelephony.Stub
					.asInterface((IBinder) method.invoke(null,
							Context.TELEPHONY_SERVICE));
			iTelephony.endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除通讯记录
	 * 
	 * @param incomingNumber
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		resolver.delete(URI_CALL_LOG, "number=?",
				new String[] { incomingNumber });
	}

	/**
	 * 内部短信接收者<br>
	 * 在服务的内部创建了一个广播接收者，希望广播接收者的存活周期跟服务一致。
	 * 
	 * @author lijian-pc
	 * @date 2016-5-6 下午2:12:07
	 */
	private class InnerSmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// pdu协议数据单元protecol data unit
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for (Object obj : objs) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);// 将数据格式化为短信数据
				String sendNumber = smsMessage.getOriginatingAddress();// 获得发件人
				String body = smsMessage.getMessageBody();// 获得短信内容
				smsMessage.getTimestampMillis();
				String mode = mDao_blackNumber.findMode(sendNumber);
				if (!TextUtils.isEmpty(mode)
						&& (Constants.SAFE_MODE_ALL.equals(mode) || Constants.SAFE_MODE_SMS
								.equals(mode))) {
					L.i(TAG, sendNumber + "发现黑名单短信,拦截...");
					abortBroadcast();
					// 真实开发这里应该把拦截下来的短信存储起来,在通知栏有所提示,并且被拦截下来的短信可以恢复
				}
				// 智能拦截
				if (body.contains("fapiao")) {// 真是开发这里可能要使用到分词技术lucence//黑名单库见金山卫士firewall_sys_rules.db
					L.i(TAG, sendNumber + "发现发票短信,拦截...");
					abortBroadcast();
				}
			}
		}
	}

	/**
	 * 短信观察者
	 */
	private void SMSObserver() {
		ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(URI_SMS, false, new ContentObserver(
				new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				L.i(TAG, "观察者观察到短信记录数据变化了,删除...");
				// 每当有一条新短信到来时，我们接收并处理这条短信
				getSmsLatestOne();
			}
		});
	}

	/**
	 * 获取最后一条短信
	 */
	private void getSmsLatestOne() {
		Cursor cursor = getContentResolver().query(URI_SMS,
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
				handleSMS(PhoneNumberUtils.formatPhoneNumber(address), body,
						cursor.getLong(1));
			}
		}
	}

	/**
	 * 处理接收到的短信
	 * 
	 * @param phone
	 * @param body
	 * @param data
	 */
	private void handleSMS(String phone, String body, long data) {
		L.i(TAG, "handleSMS()-有短信到来了");
		String mode = mDao_blackNumber.findMode(phone);
		if (!TextUtils.isEmpty(mode)
				&& (Constants.SAFE_MODE_ALL.equals(mode) || Constants.SAFE_MODE_SMS
						.equals(mode))) {
			L.i(TAG, phone + "发现黑名单短信,拦截...");
			// 真实开发这里应该把拦截下来的短信存储起来,在通知栏有所提示,并且被拦截下来的短信可以恢复
			deleteSMS(phone, data);
		}
		// 智能拦截
		if (body.contains("fapiao")) {// 真是开发这里可能要使用到分词技术lucence//黑名单库见金山卫士firewall_sys_rules.db
			L.i(TAG, phone + "发现发票短信,拦截...");
		}
	}

	/**
	 * 删除短信
	 * 
	 * @param phone
	 * @param data
	 */
	private void deleteSMS(String phone, long data) {
		ContentResolver resolver = getContentResolver();
		resolver.delete(URI_SMS, "address=? and date=?", new String[] { phone,
				String.valueOf(data) });
	}
}
