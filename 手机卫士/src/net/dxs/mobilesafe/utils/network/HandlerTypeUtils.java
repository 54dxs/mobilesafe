package net.dxs.mobilesafe.utils.network;

/**
 * 网络请求结果码
 * 
 * @author lijian
 * @date 2016-4-12 下午4:09:16
 */
public interface HandlerTypeUtils {
	/**
	 * 服务器返回状态码不正确
	 */
	int MS_HANDLER_SERVER_CODE_ERROR = 1000;
	/**
	 * URL地址错误
	 */
	int MS_HANDLER_SHOW_UPDATE_DIALOG = 1001;
	/**
	 * URL地址错误
	 */
	int MS_HANDLER_URL_MALFORMED = 1002;

}
