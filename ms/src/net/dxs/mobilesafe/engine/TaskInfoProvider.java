package net.dxs.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.TaskInfo;
import net.dxs.mobilesafe.utils.process.ProcessManager;
import net.dxs.mobilesafe.utils.process.ProcessManager.Process;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug.MemoryInfo;

import com.jaredrummler.android.processes.AndroidProcesses;

/**
 * 进程信息提供者
 * 
 * @author lijian-pc
 * @date 2016-5-16 上午10:15:58
 */
public class TaskInfoProvider {

	/**
	 * 获取所有的正在运行的进程信息。
	 * 
	 * @return
	 */
	public static List<TaskInfo> getTaskInfos(Context context) {
		// Android5.0 21 LOLLIPOP
		if (Build.VERSION.SDK_INT >= 21) {
			return getTaskInfos_after50(context);
		} else {
			return getTaskInfos_pre50(context);
		}
	}

	/**
	 * android5.0之后的写法<br>
	 * 使用了第三方类库（地址：https://github.com/jaredrummler/AndroidProcesses）
	 * 
	 * @param context
	 * @return
	 */
	private static List<TaskInfo> getTaskInfos_after50(Context context) {
		List<TaskInfo> taskInfos = new ArrayList<TaskInfo>();
		PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 获取当前运行的进程列表
		// List<RunningAppProcessInfo> runningAppPorcessInfos = am
		// .getRunningAppProcesses();
		List<ActivityManager.RunningAppProcessInfo> runningAppPorcessInfos = AndroidProcesses
				.getRunningAppProcessInfo(context);
		for (RunningAppProcessInfo runningAppPorcessInfo : runningAppPorcessInfos) {
			TaskInfo taskInfo = new TaskInfo();
			// 得到进程的包名
			String packname = runningAppPorcessInfo.processName;
			taskInfo.setPackname(packname);
			// 获取某个pid进程的内存信息。
			MemoryInfo[] memoryinfos = am
					.getProcessMemoryInfo(new int[] { runningAppPorcessInfo.pid });
			long memsize = memoryinfos[0].getTotalPrivateDirty() * 1024;
			taskInfo.setMemsize(memsize);
			try {
				ApplicationInfo applicationInfo = pm.getApplicationInfo(
						packname, 0);
				Drawable icon = applicationInfo.loadIcon(pm);
				taskInfo.setIcon(icon);
				String name = applicationInfo.loadLabel(pm).toString();
				taskInfo.setName(name);
				if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					// 用户进程
					taskInfo.setUserTask(true);
				} else {
					// 系统进程
					taskInfo.setUserTask(false);
				}
			} catch (NameNotFoundException e) {
				taskInfo.setName(packname);
				taskInfo.setIcon(context.getResources().getDrawable(
						R.drawable.ic_launcher));
			}
			taskInfos.add(taskInfo);
		}
		return taskInfos;
	}

	/**
	 * android5.0之后的写法<br>
	 * 废弃该方法，因为获取到的重复进程（1.一个应用程序可能开启了多个后台服务，导致被识别为多个进程；2.系统进程有些没有获取到程序相关信息，
	 * 如包名内存占用）<br>
	 * 使用了第三方类库（地址：https://github.com/Chainfire/libsuperuser）
	 * 
	 * @param context
	 * @return
	 */
	@Deprecated
	private static List<TaskInfo> getTaskInfos_after50_2(Context context) {
		List<TaskInfo> taskInfos = new ArrayList<TaskInfo>();
		PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 获取当前运行的进程列表
		List<Process> listInfo = ProcessManager.getRunningProcesses();
		if (listInfo.isEmpty() || listInfo.size() == 0) {
			return null;
		}
		for (Process info : listInfo) {
			TaskInfo taskInfo = new TaskInfo();
			// 得到进程的包名
			String packname = info.getPackageName();
			taskInfo.setPackname(packname);
			// 获取某个pid进程的内存信息。
			MemoryInfo[] memoryinfos = am
					.getProcessMemoryInfo(new int[] { info.pid });
			long memsize = memoryinfos[0].getTotalPrivateDirty() * 1024;
			taskInfo.setMemsize(memsize);
			try {
				ApplicationInfo applicationInfo = pm.getApplicationInfo(
						packname, 0);
				Drawable icon = applicationInfo.loadIcon(pm);
				taskInfo.setIcon(icon);
				String name = applicationInfo.loadLabel(pm).toString();
				taskInfo.setName(name);
				if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					// 用户进程
					taskInfo.setUserTask(true);
				} else {
					// 系统进程
					taskInfo.setUserTask(false);
				}
			} catch (NameNotFoundException e) {
				taskInfo.setName(packname);
				taskInfo.setIcon(context.getResources().getDrawable(
						R.drawable.ic_launcher));
			}
			taskInfos.add(taskInfo);
		}
		return taskInfos;
	}

	/**
	 * android5.0之前的写法
	 * 
	 * @param context
	 * @return
	 */
	private static List<TaskInfo> getTaskInfos_pre50(Context context) {
		List<TaskInfo> taskInfos = new ArrayList<TaskInfo>();
		PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 获取当前运行的进程列表
		List<RunningAppProcessInfo> runningAppPorcessInfos = am
				.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppPorcessInfo : runningAppPorcessInfos) {
			TaskInfo taskInfo = new TaskInfo();
			// 得到进程的包名
			String packname = runningAppPorcessInfo.processName;
			taskInfo.setPackname(packname);
			// 获取某个pid进程的内存信息。
			MemoryInfo[] memoryinfos = am
					.getProcessMemoryInfo(new int[] { runningAppPorcessInfo.pid });
			long memsize = memoryinfos[0].getTotalPrivateDirty() * 1024;
			taskInfo.setMemsize(memsize);
			try {
				ApplicationInfo applicationInfo = pm.getApplicationInfo(
						packname, 0);
				Drawable icon = applicationInfo.loadIcon(pm);
				taskInfo.setIcon(icon);
				String name = applicationInfo.loadLabel(pm).toString();
				taskInfo.setName(name);
				if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					// 用户进程
					taskInfo.setUserTask(true);
				} else {
					// 系统进程
					taskInfo.setUserTask(false);
				}
			} catch (NameNotFoundException e) {
				taskInfo.setName(packname);
				taskInfo.setIcon(context.getResources().getDrawable(
						R.drawable.ic_launcher));
			}
			taskInfos.add(taskInfo);
		}
		return taskInfos;
	}
}
