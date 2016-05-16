package net.dxs.mobilesafe.domain;

import android.graphics.drawable.Drawable;

/**
 * 进程信息的业务bean
 * 
 * @author lijian-pc
 * @date 2016-5-16 上午10:18:01
 */
public class TaskInfo {
	private Drawable icon;// 进程图标
	private String name;// 进程名称
	private long memsize;// 内存大小
	private boolean userTask;// 用户进程
	private String packname;// 包名
	private boolean checked;// 是否被选中

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMemsize() {
		return memsize;
	}

	public void setMemsize(long memsize) {
		this.memsize = memsize;
	}

	public boolean isUserTask() {
		return userTask;
	}

	public void setUserTask(boolean userTask) {
		this.userTask = userTask;
	}

	public String getPackname() {
		return packname;
	}

	public void setPackname(String packname) {
		this.packname = packname;
	}

	@Override
	public String toString() {
		return "TaskInfo [name=" + name + ", memsize=" + memsize
				+ ", userTask=" + userTask + ", packname=" + packname + "]";
	}
}
