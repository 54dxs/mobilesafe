package net.dxs.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.db.ApplockDBOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * 程序锁增删改查业务类
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午4:20:49
 */
public class ApplockDao {
	private ApplockDBOpenHelper helper;
	private Context context;

	public ApplockDao(Context context) {
		helper = new ApplockDBOpenHelper(context);
		this.context = context;
	}

	/**
	 * 添加锁定包名 希望当数据库的内容发生变化的时候，服务能够知道数据库的内容发生了变化
	 * 
	 * @param packname
	 *            包名
	 */
	public void add(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("packname", packname);
		db.insert("applock", null, values);
		db.close();
		// 向外大吼一声数据库的内容变化了。
		Uri uri = Uri.parse("content://net.dxs.mobilesafe.applock");
		context.getContentResolver().notifyChange(uri, null);
	}

	/**
	 * 删除锁定包名 希望当数据库的内容发生变化的时候，服务能够知道数据库的内容发生了变化
	 * 
	 * @param packname
	 *            包名
	 */
	public void delete(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("applock", "packname=?", new String[] { packname });
		db.close();
		// 向外大吼一声数据库的内容变化了。
		Uri uri = Uri.parse("content://net.dxs.mobilesafe.applock");
		context.getContentResolver().notifyChange(uri, null);
	}

	/**
	 * 查询某个包名是否需要锁定
	 * 
	 * @param packname
	 *            包名
	 * @return 是否需要锁定
	 */
	public boolean find(String packname) {
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();// 获取只读的数据库
		Cursor cursor = db.query("applock", null, "packname=?",
				new String[] { packname }, null, null, null);
		if (cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 获取所有的被锁定应用程序的包名。
	 * 
	 * @return
	 */
	public List<String> getLockedPackNames() {
		// 所有被锁定的包名集合
		List<String> lockedPacknames = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();// 获取只读的数据库
		Cursor cursor = db.query("applock", new String[] { "packname" }, null,
				null, null, null, null);
		while (cursor.moveToNext()) {
			String packname = cursor.getString(0);
			lockedPacknames.add(packname);
		}
		cursor.close();
		db.close();
		return lockedPacknames;
	}
}
