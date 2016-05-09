package net.dxs.mobilesafe.utils;

import java.util.List;

import net.dxs.mobilesafe.app.App;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

/**
 * 服务状态工具类
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午2:59:51
 */
public class ServiceStatusUtils {

	/**
	 * 判断一个服务是否处于开启状态
	 * 
	 * @param context
	 *            上下文
	 * @param serviceClassname
	 *            服务的完整的类名称
	 * @return
	 */
	public static boolean isServiceRunning(Context context,
			String serviceClassname) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> serviceInfos = am.getRunningServices(100);
		for (RunningServiceInfo serviceInfo : serviceInfos) {
			String servicename = serviceInfo.service.getClassName();
			if (serviceClassname.equals(servicename)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断一个服务是否处于开启状态
	 * 
	 * @param clazz
	 *            服务类
	 * @return
	 */
	public static boolean isServiceRunning(Class<?> clazz) {
		ActivityManager am = (ActivityManager) App.getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> serviceInfos = am.getRunningServices(100);
		for (RunningServiceInfo serviceInfo : serviceInfos) {
			String servicename = serviceInfo.service.getClassName();
			if (clazz.getName().equals(servicename)) {
				return true;
			}
		}
		return false;
	}
}
