package net.dxs.mobilesafe.domain;

/**
 * 黑名单号码的业务bean 数据实体
 * 
 * @author lijian-pc
 * @date 2016-5-5 下午4:06:18
 */
public class BlackNumber {

	/** 手机号 */
	private String number;
	/** 拦截模式: 0全部拦截,1拦截电话,2连接短信 */
	private String mode;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		if ("0".equals(mode) || "1".equals(mode) || "2".equals(mode)) {
			this.mode = mode;
		} else {
			this.mode = "0";
		}
	}

	@Override
	public String toString() {
		return "BlackNumber [number=" + number + ", mode=" + mode + "]";
	}
}
