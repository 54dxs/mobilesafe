package net.dxs.mobilesafe.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.domain.Smss;
import net.dxs.mobilesafe.utils.L;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Xml;

/**
 * 备份用户的短信
 * 
 * @author lijian
 * @date 2016-5-29 上午11:13:36
 */
public class SmsTools {
	private static final String TAG = "SmsTools";

	/** SD卡错误报告文件路径 */
	@SuppressLint("SdCardPath")
	private static String SDCARD_PATH = "/mnt/sdcard/mobilesafe/sms/";
	private static String SMS_BACKUP_FILENAME = "SMSBackup.xml";

	/**
	 * 备份短信的回调接口
	 * 
	 * @author lijian
	 * @date 2016-5-29 上午11:14:40
	 */
	public interface BackupSmsCallback {
		/**
		 * 短信备份前调用的方法
		 * 
		 * @param max
		 *            一共有多少条短信需要备份
		 */
		public void beforeSmsBackup(int max);

		/**
		 * 短信备份中需要调用的方法
		 * 
		 * @param progress
		 *            当前备份的进度
		 */
		public void onSmsBackup(int progress);
	}

	private static void initAddress() {
		SDCARD_PATH = Environment.getExternalStorageDirectory()
				+ "/mobilesafe/sms/";
		File file = new File(SDCARD_PATH);
		// 如果目录不存在则创建之
		if (!file.exists() && !file.isDirectory()) {
			file.mkdirs();
			L.i(TAG, "Create SDCARD_PATH--->" + SDCARD_PATH);
		} else {
			L.e(TAG, "Create SDCARD_PATH fail>>" + SDCARD_PATH);
		}
	}

	/**
	 * 备份用户的短信
	 * 
	 * @param context
	 *            上下文
	 * @param BackupSmsCallback
	 *            callback
	 */
	public static void backupSms(Context context, BackupSmsCallback callback)
			throws Exception {
		Uri uri = Uri.parse("content://sms/");
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(uri, new String[] { "address", "date",
				"type", "body" }, null, null, null);
		// 设置进度条的总长度
		// 在备份之前需要知道有多少条短信需要备份
		callback.beforeSmsBackup(cursor.getCount());

		// 创建一个xml生成器
		XmlSerializer serializer = Xml.newSerializer();

		// 判断sdcard是否可用
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			initAddress();
			File file = new File(SDCARD_PATH, SMS_BACKUP_FILENAME);
			OutputStream os = new FileOutputStream(file);
			// 初始化xml文件生成器
			serializer.setOutput(os, "utf-8");
			// xml文档的声明
			serializer.startDocument("utf-8", true);
			serializer.startTag(null, "smss");

			int totle = 0;
			while (cursor.moveToNext()) {
				serializer.startTag(null, "sms");

				serializer.startTag(null, "address");
				serializer.text(cursor.getString(0));
				serializer.endTag(null, "address");

				serializer.startTag(null, "date");
				serializer.text(cursor.getString(1));
				serializer.endTag(null, "date");

				serializer.startTag(null, "type");
				serializer.text(cursor.getString(2));
				serializer.endTag(null, "type");

				serializer.startTag(null, "body");
				serializer.text(cursor.getString(3));
				serializer.endTag(null, "body");

				serializer.endTag(null, "sms");
				Thread.sleep(10);
				totle++;
				// 设置进度条当前进度
				// 在备份的过程中，需要更新ui界面
				callback.onSmsBackup(totle);
			}
			SystemClock.sleep(500);

			cursor.close();
			serializer.endTag(null, "smss");
			serializer.endDocument();
			os.close();
		}
	}

	public static void RestoreSms(Context context, BackupSmsCallback callback)
			throws Exception {
		List<Smss> list = new ArrayList<Smss>();
		Smss sms = new Smss();

		// 创建一个pull解析器
		XmlPullParser parser = Xml.newPullParser();
		// 判断sdcard卡是否可用
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(SDCARD_PATH, SMS_BACKUP_FILENAME);
			InputStream fis = new FileInputStream(file);

			// 初始化pull解析器
			parser.setInput(fis, "utf-8");

			// 获得解析器的第一个事件类型
			int eventType = parser.getEventType();
			int totle = 0;
			while (eventType != XmlPullParser.END_DOCUMENT) {// 如果事件类型不等于结束的类型,继续循环
				// 获得当前节点的名称
				String name = parser.getName();
				// 解析数据
				switch (eventType) {
				case XmlPullParser.START_TAG:// 代表开始结点
					if ("address".equals(name)) {
						sms.setAddress(parser.nextText());
					} else if ("date".equals(name)) {
						sms.setDate(parser.nextText());
					} else if ("type".equals(name)) {
						sms.setType(parser.nextText());
					} else if ("body".equals(name)) {
						sms.setBody(parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:// 代表结束节点
					if ("sms".equals(name)) {
						Uri url = Uri.parse("content://sms/");
						// 获得内容解析器对象
						ContentResolver resolver = context.getContentResolver();
						ContentValues values = new ContentValues();
						values.put("address", sms.getAddress());
						values.put("date", sms.getDate());
						values.put("type", sms.getType());
						values.put("body", sms.getBody());
						resolver.insert(url, values);
						list.add(sms);
						totle++;
						callback.onSmsBackup(totle);
						System.out.println(totle);
					}
					break;
				}
				L.i(TAG, "onSmsBackup:" + totle);
				eventType = parser.next();// 赋值下一个事件
				Thread.sleep(10);
			}
			L.i(TAG, "listsize:" + list.size());
			callback.beforeSmsBackup(list.size());
			fis.close();
		}
		Thread.sleep(1000);
	}
}
