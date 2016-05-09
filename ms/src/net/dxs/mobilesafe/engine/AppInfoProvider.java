package net.dxs.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.domain.AppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * 手机里面应用程序信息的提供类
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午4:54:41
 */
public class AppInfoProvider {

	/**
	 * 获取手机里面所有的安装的应用程序的信息
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context) {
		PackageManager pm = context.getPackageManager();
		// 获取手机里面所有的apk包的信息，PackageInfo代表的就是每个应用程序的manifest.xml文件
		List<PackageInfo> packInfos = pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();

		for (PackageInfo packInfo : packInfos) {
			String packName = packInfo.packageName;
			String version = packInfo.versionName;
			String appName = packInfo.applicationInfo.loadLabel(pm).toString();
			Drawable appIcon = packInfo.applicationInfo.loadIcon(pm);

			AppInfo appInfo = new AppInfo();
			appInfo.setPackName(packName);
			appInfo.setVersion(version);
			appInfo.setAppName(appName + packInfo.applicationInfo.uid);
			appInfo.setAppIcon(appIcon);
			int flags = packInfo.applicationInfo.flags; // 相当于学生提交的答题卡
			if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				// 用户程序
				appInfo.setUserApp(true);
			} else {
				// 系统程序
				appInfo.setUserApp(false);
			}
			if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
				// 手机内存
				appInfo.setInRom(true);
			} else {
				// sd卡
				appInfo.setInRom(false);
			}
			appInfos.add(appInfo);
		}
		return appInfos;
	}
}
