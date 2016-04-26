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
	String SHARED_PREFERENCE_NAME = "client_preferences";

	/********************************** 用户登陆管理 ***************************************************************************************************/

	/********************************** 偏好设置 ***************************************************************************************************/

	/********************************** 通知 ***************************************************************************************************/

	/********************************** intent动作 ***************************************************************************************************/

	/********************************** 外设 ***************************************************************************************************/
	/** 连接设备 **/
	int CONNECT_DEVICE = 1;
	/** 启用蓝牙 **/
	int ENABLE_BT = 2;

	/** JS 回调映射名 **/
	String PPL_WEBVIEW_JS_CALLBACK = "PPLWebViewJSCallback";

	/********************************** 密码锁 ***************************************************************************************************/
	/** 进入手机防盗的密钥 **/
	String LOSTFIND_PSSWORD = "LostFind_Pssword";
	/** 手机防盗-是否已完成设置向导 **/
	String LOSTFIND_FINISHSETUP = "LostFind_finishSetup";
	/** 手机防盗-安全号码 **/
	String LOSTFIND_SAFENUMBER = "LostFind_safeNumber";
	/** 手机防盗-防盗保护是否已开启标记 **/
	String LOSTFIND_PROTECTINGSTATUS = "LostFind_protectingStatus";

	/** 手机防盗-设置向导2-sim **/
	String LOSTFIND_SETUP2_SIM = "LostFind_setup2_sim";
	/** 手机防盗-设置向导3-安全号码 **/
	String LOSTFIND_SETUP3_SAFENUMBER = "LostFind_setup3_safenumber";

	/** 手机防盗-设置向导3-联系人信息-号码 **/
	String INTENT_SETUP3_CONTACTINFO_PHONE = "Intent_setup3_contactInfo_phone";

	/** 选择联系人-返回结果码 **/
	int ACTIVITYRESULT_RESULTCODE_SELECTCONTACTACTIVITY = 1000;

}
