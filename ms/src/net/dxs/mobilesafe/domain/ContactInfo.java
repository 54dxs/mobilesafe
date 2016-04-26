package net.dxs.mobilesafe.domain;

/**
 * 联系人信息
 * 
 * @author lijian-pc
 * @date 2016-4-26 下午5:27:49
 */
public class ContactInfo {
	/** 姓名 */
	private String name;
	/** 手机号 */
	private String phone;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String toString() {
		return "ContactInfo [name=" + name + ", phone=" + phone + "]";
	}
}
