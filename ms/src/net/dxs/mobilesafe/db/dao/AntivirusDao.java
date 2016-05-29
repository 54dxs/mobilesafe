package net.dxs.mobilesafe.db.dao;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 病毒数据库查询工具类
 * 
 * @author lijian
 * @date 2016-5-28 上午10:00:16
 */
public class AntivirusDao {
	@SuppressLint("SdCardPath")
	private static final String path = "/data/data/net.dxs.mobilesafe/files/antivirus.db";

	/**
	 * 查询一条记录是否是病毒
	 * 
	 * @param md5
	 *            程序签名的md5信息
	 * @return 病毒的描述信息 null代表扫描安全
	 */
	public static String find(String md5) {
		String desc = null;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select desc from datable where md5=?",
				new String[] { md5 });
		if (cursor.moveToNext()) {
			desc = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return desc;
	}

}
