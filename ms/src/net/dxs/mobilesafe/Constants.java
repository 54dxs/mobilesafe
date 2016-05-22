package net.dxs.mobilesafe;

/**
 * Static constants for this package.<br>
 * 静态常量
 * 
 * @author lijian
 * @date 2016-04-08 16:45:40
 */
public interface Constants {

	/** SharedPreferences 文件名 **/
	String SHARED_PREFERENCE_NAME = "mobilesafe_client_preferences";

	/********************************** 用户登陆管理 ***************************************************************************************************/

	/********************************** 偏好设置 ***************************************************************************************************/

	/********************************** 通知 ***************************************************************************************************/

	/********************************** 短信指令 ***************************************************************************************************/
	/** 短信指令-位置追踪 **/
	String SMS_ACTION_LOCATION = "#*location*#";
	/** 短信指令-播放报警音乐 **/
	String SMS_ACTION_ALARM = "#*alarm*#";
	/** 短信指令-清除数据 **/
	String SMS_ACTION_WIPEDATA = "#*wipedata*#";
	/** 短信指令-锁屏 **/
	String SMS_ACTION_LOCKSCREEN = "#*lockscreen*#";

	/********************************** intent数据 ***************************************************************************************************/
	/** 手机防盗-设置向导3-联系人信息-号码 **/
	String INTENT_DATA_CONTACTINFO_PHONE = "Intent_data_contactInfo_phone";
	/** 看门狗-受保护的程序包名 **/
	String INTENT_DATA_LOCKED_PACKNAMES = "Intent_data_locked_PackNames";

	/********************************** intent动作 ***************************************************************************************************/
	/** 看门狗-停止程序保护的动作指令 **/
	String INTENT_ACTION_STOPPROTECT = "net.dxs.mobilesafe.stopprotect";

	/********************************** ActivityResult ***************************************************************************************************/
	/** 选择联系人-返回结果码 **/
	int ACTIVITYRESULT_CODE_SELECTCONTACTACTIVITY = 1000;

	/********************************** 外设 ***************************************************************************************************/
	/** 连接设备 **/
	int CONNECT_DEVICE = 1;
	/** 启用蓝牙 **/
	int ENABLE_BT = 2;

	/** JS 回调映射名 **/
	String PPL_WEBVIEW_JS_CALLBACK = "PPLWebViewJSCallback";

	/********************************** 欢迎界面 ***************************************************************************************************/
	/** 软件自动更新标记 **/
	String AUTO_UPDATE = "auto_update";
	/** 创建桌面快捷图标 **/
	String SHORTCUT = "shortcut";

	/********************************** 手机防盗 ***************************************************************************************************/
	/** 进入手机防盗的密钥 **/
	String LOSTFIND_PSSWORD = "LostFind_pssword";
	/** 手机防盗-是否已完成设置向导 **/
	String LOSTFIND_FINISH_SETUP = "LostFind_finish_setup";
	/** 手机防盗-安全号码 **/
	String LOSTFIND_SAFE_NUMBER = "LostFind_safe_number";
	/** 手机防盗-防盗保护是否已开启标记 **/
	String LOSTFIND_PROTECTING_STATUS = "LostFind_protecting_status";
	/** 手机防盗-设置向导2-sim **/
	String LOSTFIND_SIM = "LostFind_SIM";

	/********************************** 通讯卫士 ***************************************************************************************************/
	/** 拦截模式-全部拦截 **/
	String SAFE_MODE_ALL = "0";
	/** 拦截模式-电话拦截 **/
	String SAFE_MODE_PHONE = "1";
	/** 拦截模式-短信拦截 **/
	String SAFE_MODE_SMS = "2";

	/********************************** 归属地 ***************************************************************************************************/
	/** 归属地显示窗体-x坐标 **/
	String CALLADDRESS_WINDOWMANAGER_PARAMS_X = "CallAddress_windowManager_params_x";
	/** 归属地显示窗体-y坐标 **/
	String CALLADDRESS_WINDOWMANAGER_PARAMS_Y = "CallAddress_windowManager_params_y";

}
