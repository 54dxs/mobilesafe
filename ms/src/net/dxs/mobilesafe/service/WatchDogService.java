package net.dxs.mobilesafe.service;

import java.lang.reflect.Field;
import java.util.List;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.activities.EnterPwdActivity;
import net.dxs.mobilesafe.db.dao.ApplockDao;
import net.dxs.mobilesafe.utils.L;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

/**
 * 看门狗服务
 * 
 * @author lijian-pc
 * @date 2016-5-10 下午1:42:50
 */
public class WatchDogService extends Service {
	private static final String TAG = "WatchDogService";

	private ActivityManager am;
	private boolean flag;
	private ApplockDao mDao_appLock;
	private InnerReceiver receiver;
	private ScreenlockReceiver lockReceiver;
	/**
	 * 临时停止保护的包名
	 */
	private String tempStopProtectPackname;

	/**
	 * 存放所有的被锁定应用程序的包名
	 */
	private List<String> mList_lockedPackNames;

	private Intent intent;

	/**
	 * 数据库内容的观察者
	 */
	private MyDataObserver observer;

	private class InnerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			tempStopProtectPackname = intent.getStringExtra("packname");
			L.i(TAG, "接收到了停止保护的广播事件" + tempStopProtectPackname);
		}
	}

	private class ScreenlockReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			L.i(TAG, "屏幕锁屏了");
			tempStopProtectPackname = null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mDao_appLock = new ApplockDao(this);
		mList_lockedPackNames = mDao_appLock.getLockedPackNames();

		// 注册一个内容观察者
		Uri uri = Uri.parse("content://net.dxs.mobilesafe.applock");
		observer = new MyDataObserver(new Handler());
		getContentResolver().registerContentObserver(uri, false, observer);

		// 注册一个广播
		receiver = new InnerReceiver();
		IntentFilter filter = new IntentFilter(
				Constants.INTENT_ACTION_STOPPROTECT);// 指定要接收的广播
		filter.setPriority(100);// 这是优先级
		registerReceiver(receiver, filter);

		// 注册一个锁屏监听广播
		lockReceiver = new ScreenlockReceiver();
		registerReceiver(lockReceiver, new IntentFilter(
				Intent.ACTION_SCREEN_OFF));

		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		intent = new Intent(WatchDogService.this, EnterPwdActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		new Thread() {
			public void run() {
				flag = true;
				while (flag) {
					String packname = getRunningTaskPackageName();
					L.i(TAG, "packname:" + packname);
					// 判断当前应用程序的包名是否需要保护。
					// 查询数据库操作 1.打开数据库 2查询遍历 3返回结果
					// if (dao.find(packname)) {//消耗几十毫秒的时间 把查询数据库的逻辑换成查询内存
					if (mList_lockedPackNames.contains(packname)) {
						// 判断当前应用程序是否需要临时的停止保护。
						if (packname.equals(tempStopProtectPackname)) {
							// 需要临时停止保护
						} else {
							// 弹出输入密码的界面
							intent.putExtra(
									Constants.INTENT_DATA_LOCKED_PACKNAMES,
									packname);
							startActivity(intent);
						}
					}
					SystemClock.sleep(3000);
				}
			}
		}.start();

		super.onCreate();
	}

	private String getRunningTaskPackageName() {
		String packname;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
			packname = getCurrentPkgName(this);
		} else {
			//Android5.0之后不起作用
			RunningTaskInfo taskInfo = am.getRunningTasks(1).get(0);
			packname = taskInfo.topActivity.getPackageName();
		}
		return packname;
	}

	private String getCurrentPkgName(Context context) {
		ActivityManager.RunningAppProcessInfo currentInfo = null;
		Field field = null;
		int START_TASK_TO_FRONT = 2;
		String pkgName = null;
		try {
			field = ActivityManager.RunningAppProcessInfo.class
					.getDeclaredField("processState");
		} catch (Exception e) {
			e.printStackTrace();
		}
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		//小米Note（型号：MI NOTE PRO）（Android版本 5.1.1）方法获取不到数据，继续寻找解决方案中
		List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo app : appList) {
			if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				Integer state = null;
				try {
					state = field.getInt(app);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (state != null && state == START_TASK_TO_FRONT) {
					currentInfo = app;
					break;
				}
			}
		}
		if (currentInfo != null) {
			pkgName = currentInfo.processName;
		}
		return pkgName;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		flag = false;
		unregisterReceiver(receiver);
		receiver = null;
		unregisterReceiver(lockReceiver);
		lockReceiver = null;
		getContentResolver().unregisterContentObserver(observer);
		observer = null;
	}

	private class MyDataObserver extends ContentObserver {

		public MyDataObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			L.i(TAG, "观察者接受到了消息，数据库的内容变化了。");
			mList_lockedPackNames = mDao_appLock.getLockedPackNames();
			super.onChange(selfChange);
		}
	}
}
