package net.dxs.mobilesafe;

import android.app.Activity;

/**
 * 全局变量
 * 
 * @author lijian
 * @date 2016-4-8 下午4:45:58
 */
public class GlobalParams {

	/** 全局的activity **/
	public static Activity activity = null;

	/**
	 * 代理的ip
	 */
	public static String PROXY_IP = "";
	/**
	 * 代理的端口
	 */
	public static int PROXY_PORT = 0;
	/**
	 * 屏幕宽度
	 */
	public static int WIN_WIDTH = 0;
	/**
	 * 登录的状态
	 */
	public static boolean ISLOGIN = false;
	/**
	 * 用户的余额
	 */
	public static Float MONEY = null;
	/**
	 * 用户名
	 */
	public static String USERNAME = "";
}
