package net.dxs.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建软件锁管理数据库
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午4:22:09
 */
public class ApplockDBOpenHelper extends SQLiteOpenHelper {

	public ApplockDBOpenHelper(Context context) {
		super(context, "applock.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table applock (_id integer primary key autoincrement,packname varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
