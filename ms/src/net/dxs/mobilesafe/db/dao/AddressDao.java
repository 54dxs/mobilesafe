package net.dxs.mobilesafe.db.dao;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 电话号码归属地数据库的查询工具类
 * 
 * @author lijian-pc
 * @date 2016-5-19 下午7:04:20
 */
public class AddressDao {
	@SuppressLint("SdCardPath")
	private static final String path = "/data/data/net.dxs.mobilesafe/files/address.db";

	/**
	 * 查询电话号码的归属地
	 * 
	 * @param number
	 *            电话号码
	 * @return 归属地位置信息
	 */
	public static String find(String number) {
		String location = null;
		// 在使用这个asset数据库文件之前，把文件拷贝到手机的内部存储系统里面/data/data/包名/files目录。

		// path 数据库的路径
		// 直接采用file:///android_asset路径写法 只是对网页资源有效，如果是数据库 不能这样写路径
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		// 手机号码比较简单。 11位 1开头 数字 13x 14x 15x 18x
		// 正则表达式
		// 手机号码的匹配正则表达式 ^1[3458]\d{9}$
		if (number.matches("^1[3458]\\d{9}$")) {// 正则表达式 手机号码
			Cursor cursor = db
					.rawQuery(
							"select location from data2 where id = (select outkey from data1 where id=?)",
							new String[] { number.substring(0, 7) });
			if (cursor.moveToNext()) {
				location = cursor.getString(0);
			}
			cursor.close();
		} else {// 其他号码 110 119 999
			switch (number.length()) {
			case 3:
				if ("110".equals(number)) {
					location = "匪警";
				} else {
					location = "暂无收录";
				}
				break;
			case 4:
				location = "模拟器";
				break;
			case 5:
				location = "客服电话";
				break;
			case 7:
				location = "本地电话";
				break;
			case 8:
				location = "本地电话";
				break;
			// 0101234567
			// 02112343559
			// 075532432449
			// 0086
			// 00353
			default:
				if (number.length() >= 10 && number.startsWith("0")) {
					Cursor cursor = db.rawQuery(
							"select location from data2 where area =?",
							new String[] { number.substring(1, 3) });
					if (cursor.moveToNext()) {
						String temp = cursor.getString(0);
						location = temp.substring(0, temp.length() - 2);
					}
					cursor.close();
					cursor = db.rawQuery(
							"select location from data2 where area =?",
							new String[] { number.substring(1, 4) });
					if (cursor.moveToNext()) {
						String temp = cursor.getString(0);
						location = temp.substring(0, temp.length() - 2);
					}
					cursor.close();
				}
				break;
			}
		}
		db.close();
		return location;
	}
}
