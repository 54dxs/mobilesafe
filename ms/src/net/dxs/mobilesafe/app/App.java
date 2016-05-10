package net.dxs.mobilesafe.app;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;

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
		//获取一个系统的包管理器
		PackageManager packageManager = instance.getPackageManager();
		try {
			//清单文件manifest.xml文件的所有信息
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
		//获取一个系统的包管理器
		PackageManager packageManager = instance.getPackageManager();
		try {
			//清单文件manifest.xml文件的所有信息
			PackageInfo packageInfo = packageManager.getPackageInfo(instance.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			//can't reach	不可能发生的异常
			return 0;
		}
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 * 
	 * @param dpValue
	 * @return
	 */
	public static float dip2px(float dpValue) {
		final float scale = App.getContext().getResources().getDisplayMetrics().density;
		return (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 * 
	 * @param pxValue
	 * @return
	 */
	public static float px2dip(float pxValue) {
		final float scale = App.getContext().getResources().getDisplayMetrics().density;
		return (pxValue / scale + 0.5f);
	}

	/**
	 * 获取屏幕的宽度
	 * 
	 * @return
	 */
	public static int getWidth() {
		DisplayMetrics dm = App.getContext().getApplicationContext()
				.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	/**
	 * 获取屏幕的高度
	 * 
	 * @return
	 */
	public static int getHeight() {
		DisplayMetrics dm = App.getContext().getApplicationContext()
				.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}
}
