package net.dxs.mobilesafe.utils.txt;

/**
 * 电话号码处理
 * 
 * @author lijian-pc
 * @date 2016-4-28 下午2:33:38
 */
public class PhoneNumberUtils {

	/**
	 * 获得一个格式化的手机号
	 * 
	 * @param phone
	 * @return
	 */
	public static String formatPhoneNumber(String phone) {
		int len = phone.length();
		if (len > 11) {
			if (phone.contains("+86")) {
				phone = phone.substring(3);
			}
		} else {
			return phone;
		}
		return phone;
	}
}
