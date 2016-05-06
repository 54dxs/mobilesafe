package net.dxs.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.db.BlacknumberDBOpenHelper;
import net.dxs.mobilesafe.domain.BlackNumber;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

/**
 * 黑名单号码的增删改查业务类
 * 
 * @author lijian-pc
 * @date 2016-5-5 下午4:01:36
 */
public class BlacknumberDao {
	private BlacknumberDBOpenHelper helper;

	public BlacknumberDao(Context context) {
		helper = new BlacknumberDBOpenHelper(context);
	}

	/**
	 * 添加黑名单号码
	 * 
	 * @param number
	 *            号码
	 * @param mode
	 *            拦截模式
	 */
	public void add(String number, String mode) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("mode", mode);
		db.insert("info", null, values);
		db.close();
	}

	/**
	 * 删除黑名单号码
	 * 
	 * @param number
	 *            要删除的号码
	 */
	public void delete(String number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("info", "number=?", new String[] { number });
		db.close();
	}

	/**
	 * 修改黑名单号码的拦截模式
	 * 
	 * @param number
	 *            要修改的黑名单号码
	 * @param newmode
	 *            新的拦截模式
	 */
	public void update(String number, String newmode) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newmode);
		db.update("info", values, "number=?", new String[] { number });
		db.close();
	}

	/**
	 * 查询黑名单号码的拦截模式
	 * 
	 * @param number
	 *            要查询的黑名单号码
	 * @return mode 拦截模式 返回null代表的是黑名单号码不存在
	 */
	public String findMode(String number) {
		String mode = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("info", new String[] { "mode" }, "number=?",
				new String[] { number }, null, null, null);
		if (cursor.moveToNext()) {
			mode = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return mode;
	}

	/**
	 * 查询返回全部的黑名单号码
	 * 
	 * @return
	 */
	public List<BlackNumber> findAll() {
		SystemClock.sleep(3000);
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("info", new String[] { "number", "mode" },
				null, null, null, null, "_id desc");
		List<BlackNumber> list = new ArrayList<BlackNumber>();
		while (cursor.moveToNext()) {
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			BlackNumber blackNumber = new BlackNumber();
			blackNumber.setNumber(number);
			blackNumber.setMode(mode);
			list.add(blackNumber);
		}
		cursor.close();
		db.close();
		return list;
	}

	/**
	 * 查询部分的黑名单号码
	 * 
	 * @param maxNumber
	 *            最多返回多少条数据
	 * @param startIndex
	 *            从哪个位置开始获取数据
	 * @return
	 */
	public List<BlackNumber> findPart(int maxNumber, int startIndex) {
		SystemClock.sleep(1000);
		SQLiteDatabase db = helper.getReadableDatabase();
		// select _id,location,areacode from mob_location limit 20,10
		Cursor cursor = db
				.rawQuery(
						"select number,mode from info order by _id desc limit ? offset ?",
						new String[] { String.valueOf(maxNumber),
								String.valueOf(startIndex) });
		List<BlackNumber> list = new ArrayList<BlackNumber>();
		while (cursor.moveToNext()) {
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			BlackNumber blackNumber = new BlackNumber();
			blackNumber.setNumber(number);
			blackNumber.setMode(mode);
			list.add(blackNumber);
		}
		cursor.close();
		db.close();
		return list;
	}

}
