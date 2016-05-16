package net.dxs.mobilesafe.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Build;

import com.jaredrummler.android.processes.AndroidProcesses;

/**
 * 系统信息的工具类
 * 
 * @author lijian-pc
 * @date 2016-5-16 上午9:58:26
 */
public class SystemInfoUtils {

	/**
	 * 获取手机里面正在运行的进程的个数
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static int getRunningProcessCount(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 获取手机里面所有的正在运行的进程信息
		List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();

		// Android5.0 21 LOLLIPOP
		if (Build.VERSION.SDK_INT >= 21) {
			infos = AndroidProcesses.getRunningAppProcessInfo(context);
		} else {
			infos = am.getRunningAppProcesses();
		}
		return infos.size();
	}

	/**
	 * 获取可用的手机内存
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static long getAvailRAM(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);
		return outInfo.availMem;
	}

	/**
	 * 获取全部的手机内存
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static long getTotalRAM(Context context) {
		// ActivityManager am = (ActivityManager)
		// context.getSystemService(Context.ACTIVITY_SERVICE);
		// MemoryInfo outInfo = new MemoryInfo();
		// am.getMemoryInfo(outInfo);
		// return outInfo.totalMem;
		BufferedReader br = null;
		try {
			File file = new File("/proc/meminfo");
			FileInputStream fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis));
			// MemTotal: 513000 kB 字符 串
			String line = br.readLine();
			char[] chars = line.toCharArray();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] >= '0' && chars[i] <= '9') {
					sb.append(chars[i]);
				}
			}
			long memsize = Integer.parseInt(sb.toString());
			return memsize * 1024;// byte 单位
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
