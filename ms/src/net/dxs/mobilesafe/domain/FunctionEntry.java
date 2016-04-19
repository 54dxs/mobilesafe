package net.dxs.mobilesafe.domain;

/**
 * 功能入口对象实体
 * 
 * @author lijian-pc
 * @date 2016-4-19 下午4:05:46
 */
public class FunctionEntry {

	/** 名称 */
	private String name;
	/** 图标ID */
	private int icon;

	public FunctionEntry() {
	}

	public FunctionEntry(String name, int icon) {
		this.name = name;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

}
