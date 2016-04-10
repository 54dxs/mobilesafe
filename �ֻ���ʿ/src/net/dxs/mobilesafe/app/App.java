package net.dxs.mobilesafe.app;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 全局变量
 * 
 * @author lijian
 * @date 2016-4-8 下午4:30:23
 */
public class App extends Application {
	private static App instance;

	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();

		// 异常处理，不需要处理时注释掉这两句即可！
		CrashHandler crashHandler = CrashHandler.getInstance();
		// 注册crashHandler
		crashHandler.init(getApplicationContext());
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	/**
	 * Global context
	 * 
	 * @return
	 */
	public static App getContext() {
		return instance;
	}

	/**
	 * 获取版本name
	 * 
	 * @return
	 */
	public static String getVersionName() {
		PackageManager packageManager = instance.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(instance.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			//can't reach	不可能发生的异常
			return "";
		}
	}

	/**
	 * 获取版本code
	 * 
	 * @return
	 */
	public static int getVersionCode() {
		PackageManager packageManager = instance.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(instance.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			//can't reach	不可能发生的异常
			return 0;
		}
	}
}
