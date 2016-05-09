package net.dxs.mobilesafe.domain;

import android.graphics.drawable.Drawable;

/**
 * 应用程序信息的业务bean
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午4:55:41
 */
public class AppInfo {
	private String appName;// 应用程序名
	private Drawable appIcon; // 应用程序图标
	private String packName; // 应用程序包名
	private String version; // 应用程序版本号
	private boolean inRom; // 是否安装在手机内存
	private boolean userApp; // 是否是用户自己安装的程序

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Drawable getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isInRom() {
		return inRom;
	}

	public void setInRom(boolean inRom) {
		this.inRom = inRom;
	}

	public boolean isUserApp() {
		return userApp;
	}

	public void setUserApp(boolean userApp) {
		this.userApp = userApp;
	}

	@Override
	public String toString() {
		return "AppInfo [appName=" + appName + ", packName=" + packName
				+ ", version=" + version + ", inRom=" + inRom + ", userApp="
				+ userApp + "]";
	}

}
