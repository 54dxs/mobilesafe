package net.dxs.mobilesafe.receiver;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SpUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

/**
 * 监听开机广播（手机一开机将调用onReceive()方法）
 * 
 * @author lijian-pc
 * @date 2016-4-27 下午3:56:28
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	private static final String TAG = "BootCompleteReceiver";
	/** 电话管理器 */
	private TelephonyManager mTm;

	@Override
	public void onReceive(Context context, Intent intent) {
		L.i(TAG, "哈哈，手机启动了...");
		// 判断用户是否开启了手机防盗功能
		boolean protectingstatus = SpUtil.getInstance().getBoolean(
				Constants.LOSTFIND_PROTECTING_STATUS, false);

		if (protectingstatus) {
			L.i(TAG, "手机防盗是开启状态");
			// 判断当前手机里面的sim卡与我原来绑定的sim卡是否一致
			mTm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String currentSim = mTm.getSimSerialNumber();// 当前手机里面的sim卡串号
			String bindSim = SpUtil.getInstance().getString(
					Constants.LOSTFIND_SIM, "");
			if (bindSim.equals(currentSim)) {
				// sim没有变化就是你的卡
			} else {
				// sim卡变化了，有可能手机被盗了，偷偷在后台发送报警短信
				SmsManager smsManager = SmsManager.getDefault();
				String safenumber = SpUtil.getInstance().getString(
						Constants.LOSTFIND_SAFE_NUMBER, "");
				smsManager.sendTextMessage(safenumber, null, "sim change",
						null, null);
			}
		} else {
			L.i(TAG, "手机防盗是没有开启状态");
		}
	}
}
