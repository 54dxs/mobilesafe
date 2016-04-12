package net.dxs.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 数据流工具类
 * 
 * @author lijian
 * @date 2016-4-8 下午6:32:48
 */
public class StreamUtil {

	/**
	 * 把流里面的数据转换为字符串
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String readStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		is.close();
		String result = baos.toString();
		baos.close();
		return result;
	}

}
