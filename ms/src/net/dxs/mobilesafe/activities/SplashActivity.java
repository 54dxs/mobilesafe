package net.dxs.mobilesafe.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.app.App;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.StreamUtil;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.SystemClock;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 欢迎界面
 * 
 * @author lijian
 * @date 2016-4-7 下午6:00:04
 */
public class SplashActivity extends BaseActivity {

	private static final String TAG = "SplashActivity";
	private static final int HANDLER_SERVER_CODE_ERROR = 1000;
	private static final int HANDLER_SHOW_UPDATE_DIALOG = 1001;
	private static final int HANDLER_URL_MALFORMED = 1002;
	private static final int HANDLER_URL_ERROR = 1003;
	private static final int HANDLER_NETWORK_ERROR = 1004;
	private static final int HANDLER_JSON_ERROR = 1005;

	private RelativeLayout mRl_root;
	private TextView mTv_version;
	private TextView mTv_progress;
	private String description;
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mRl_root = (RelativeLayout) findViewById(R.id.rl_splash_root);
		mTv_version = (TextView) findViewById(R.id.tv_splash_version);
		mTv_progress = (TextView) findViewById(R.id.tv_splash_progress);
	}

	private void initData() {
		getSharedPreferences("config", Context.MODE_PRIVATE);
		mTv_version.setText("版本号:" + App.getVersionName());
		//播放一个动画效果
		playAnimation();
		autoUpdate();

		// 在应用程序打开的splash界面里面 完成 数据库文件的初始化。
		//		copyDB("address.db");
		//		copyDB("antivirus.db");

		//		//启动来电归属地服务 
		//		Intent intent = new Intent(this, CallAddressService.class);
		//		startService(intent);

		createShortCut();
	}

	/**
	 * 创建一个桌面快捷图标
	 */
	private void createShortCut() {
		//判断是否已经创建了快捷图标
		boolean shortcut = SpUtil.getInstance().getBoolean("shortcut", false);
		if(shortcut){
			return;
		}
		
		//在桌面launcher应用创建一个快捷图标,即给其发送一个广播
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		
		//1,告诉桌面创建的快捷图标的名称
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机卫士");
		//2,设置快捷图标的图标
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_show_logo);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
		
		//3,指定快捷方式的动作
		Intent i = new Intent();
		i.setAction("net.dxs.mobilesafe.home");//这里只能用隐式意图
		i.addCategory("android.intent.category.DEFAULT");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);//3，设置快捷方式的意图
		//发送一个创建快捷方式的广播
		sendBroadcast(intent);
		SpUtil.getInstance().saveBoolean("shortcut", true);
	}

	/**
	 * 软件更新
	 */
	private void autoUpdate() {
		//判断是否开启了自动更新检查
		boolean isAutoUpdate = SpUtil.getInstance().getBoolean("autoupdate", false);
		if (isAutoUpdate) {
			//连接服务器检查更新信息
			chackVersion();
		} else {//自动更新是关闭的
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					loadMainUI();
				}
			}, 2000);
		}
	}

	/**
	 * 连接服务器检查是否有新版本
	 */
	private void chackVersion() {
		new Thread() {
			@Override
			public void run() {
				long startTime = System.currentTimeMillis();
				Message msg = mHandler.obtainMessage();
				try {
					String serviceUrl = getResources().getString(R.string.serviceUrl);
					URL url = new URL(serviceUrl);//http: https:// ftp:// svn://
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");//设置请求方式，区分大小写
					conn.setConnectTimeout(5000);//设置连接超时时间
					//conn.setReadTimeout(5000);//读取超时时间
					int code = conn.getResponseCode();//获取服务器状态码

					if (code == 200) {//请求成功
						InputStream is = conn.getInputStream();//json字符串
						String result = StreamUtil.readStream(is);
						JSONObject jsonObj = new JSONObject(result);
						String version = jsonObj.getString("version");
						description = jsonObj.getString("description");
						path = jsonObj.getString("path");
						L.i(TAG, "version: " + version + ",\ndescription: " + description + ",\npath: " + path);
						//判断 服务器的版本号和客户端的版本号是否一致
						if (App.getVersionName().equals(version)) {
							L.i(TAG, "版本号相同,进入主界面");
							SystemClock.sleep(2000);
							loadMainUI();
						} else {
							L.i(TAG, "版本号不相同,弹出更新提醒对话框");
							msg.what = HANDLER_SHOW_UPDATE_DIALOG;
						}
					} else {
						//状态码不正确
						msg.what = HANDLER_SERVER_CODE_ERROR;
					}

				} catch (MalformedURLException e) {//路径错误(协议错误了)
					e.printStackTrace();
					msg.what = HANDLER_URL_MALFORMED;
				} catch (NotFoundException e) {//域名或者路径找不到
					e.printStackTrace();
					msg.what = HANDLER_URL_ERROR;
				} catch (IOException e) {//访问网络错误
					e.printStackTrace();
					msg.what = HANDLER_NETWORK_ERROR;
				} catch (JSONException e) {//解析json文件出错了
					e.printStackTrace();
					msg.what = HANDLER_JSON_ERROR;
				} finally {
					long endTime = System.currentTimeMillis();
					long dTime = endTime - startTime;
					if (dTime < 2000) {
						SystemClock.sleep(2000 - dTime);
					}
					mHandler.sendMessage(msg);
				}
			}
		}.start();
	}

	@Override
	protected void parserMessage(Message msg) {
		super.parserMessage(msg);
		switch (msg.what) {
		case HANDLER_SERVER_CODE_ERROR:
			AppToast.getInstance().show("获取更新信息失败,错误码:" + HANDLER_SERVER_CODE_ERROR);
			loadMainUI();
			break;
		case HANDLER_SHOW_UPDATE_DIALOG:
			showUpdateDialog();
			break;
		case HANDLER_URL_MALFORMED:
			AppToast.getInstance().show("URL地址错误,错误码:" + HANDLER_URL_MALFORMED);
			loadMainUI();
			break;
		case HANDLER_URL_ERROR:
			AppToast.getInstance().show("URL地址错误,错误码:" + HANDLER_URL_ERROR);
			loadMainUI();
			break;
		case HANDLER_NETWORK_ERROR:
			AppToast.getInstance().show("您的网络不给力啊,再试下下哈~~~");
			loadMainUI();
			break;
		case HANDLER_JSON_ERROR:
			AppToast.getInstance().show("JSON解析错误,错误码:" + HANDLER_SERVER_CODE_ERROR);
			loadMainUI();
			break;

		default:
			break;
		}
	}

	/**
	 * 显示更新提醒对话框
	 */
	private void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(this);

		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				loadMainUI();
			}
		});

		builder.setTitle("更新提醒");
		builder.setMessage(description);
		builder.setPositiveButton("立即更新", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				L.i(TAG, "dialog:" + dialog + ";which:" + which);
				//下载apk，替换安装
				FinalHttp fh = new FinalHttp();
				//调用download方法开始下载

				String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path.substring(path.lastIndexOf("/") + 1);
				fh.download(path, targetPath, false, new AjaxCallBack<File>() {

					private ProgressDialog pd;

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
						int progress = (int) ((current * 100) / count);
						pd = new ProgressDialog(SplashActivity.this);
						pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						pd.setTitle("正在下载，请稍后...");
						pd.setMax((int) count);
						pd.setProgress((int) current);
						pd.show();
						mTv_progress.setText("下载进度：" + progress + "%");
					}

					@Override
					public void onSuccess(File t) {
						super.onSuccess(t);
						if (pd != null && pd.isShowing()) {
							pd.dismiss();
						}

						AppToast.getInstance().show("下载成功,开始替换安装");
						//						<activity android:name=".PackageInstallerActivity"
						//				        	android:configChanges="orientation|keyboardHidden"
						//				        	android:theme="@style/TallTitleBarTheme">
						//						    <intent-filter>
						//						        <action android:name="android.intent.action.VIEW" />
						//						        <category android:name="android.intent.category.DEFAULT" />
						//						        <data android:scheme="content" />
						//						        <data android:scheme="file" />
						//						        <data android:mimeType="application/vnd.android.package-archive" />
						//						    </intent-filter>
						//						</activity>
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");
						intent.addCategory("android.intent.category.DEFAULT");
						//	intent.setData(Uri.fromFile(t));
						//	intent.setType("application/vnd.android.package-archive");
						intent.setDataAndType(Uri.fromFile(t), "application/vnd.android.package-archive");//上面两行的合并,因为上面两行单独设置会报错
						startActivity(intent);
					}

					@Override
					public void onFailure(Throwable t, int errorNo, String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						AppToast.getInstance().show("下载失败");
						loadMainUI();
					}
				});
			}
		});

		builder.setNegativeButton("下次再说", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				loadMainUI();
			}
		});

		builder.setCancelable(false);//流氓做法
		builder.show();
	}

	/**
	 * 进入主界面
	 */
	protected void loadMainUI() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();//将自身关闭
	}

	/**
	 * 播放一个动画
	 */
	private void playAnimation() {
		Animation animation = new AlphaAnimation(0.5f, 1.0f);
		animation.setDuration(2000);//动画播放时间
		mRl_root.startAnimation(animation);
	}
}
