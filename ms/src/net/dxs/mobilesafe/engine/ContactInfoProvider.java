package net.dxs.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.domain.ContactInfo;
import net.dxs.mobilesafe.utils.L;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;

/**
 * 联系人信息提供者
 * 
 * @author lijian-pc
 * @date 2016-4-26 下午5:19:38
 */
public class ContactInfoProvider {

	private static final String TAG = "ContactInfoProvider";

	/**
	 * 获取系统里面所有的联系人信息
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public static List<ContactInfo> getContactInfo(Context context) {
		long in_time = SystemClock.uptimeMillis();

		List<ContactInfo> infos = new ArrayList<ContactInfo>();
		ContentResolver resolver = context.getContentResolver();
		Uri rawcontactsuri = Uri
				.parse("content://com.android.contacts/raw_contacts");
		Uri datauri = Uri.parse("content://com.android.contacts/data");

		// 1.查询raw_contacts表 id给查询出来
		Cursor cursor = resolver.query(rawcontactsuri,
				new String[] { "contact_id" }, null, null, null);
		while (cursor.moveToNext()) {
			String id = cursor.getString(0);
			L.i(TAG, "联系人的id:" + id);
			if (id != null) {
				ContactInfo info = new ContactInfo();
				Cursor dataCursor = resolver.query(datauri, new String[] {
						"mimetype", "data1" }, "raw_contact_id=?",
						new String[] { id }, null);
				while (dataCursor.moveToNext()) {
					String mimetype = dataCursor.getString(0);
					String data1 = dataCursor.getString(1);
					L.i(TAG, "mimetype:" + mimetype);
					L.i(TAG, "data1:" + data1);
					if ("vnd.android.cursor.item/name".equals(mimetype)) {
						info.setName(data1);
					} else if ("vnd.android.cursor.item/phone_v2"
							.equals(mimetype)) {
						info.setPhone(data1);
					}
				}
				if (!TextUtils.isEmpty(info.getName())
						&& !TextUtils.isEmpty(info.getPhone())) {
					infos.add(info);
					L.i(TAG, "--------------华丽丽的分割线--------------");
				}
				dataCursor.close();
			}
		}
		cursor.close();

		// 这里故意设置一个3s的加载时间
		long timeInterval = SystemClock.uptimeMillis() - in_time;
		if (timeInterval < 3000) {
			SystemClock.sleep(3000 - timeInterval);
		}

		return infos;
	}

}
