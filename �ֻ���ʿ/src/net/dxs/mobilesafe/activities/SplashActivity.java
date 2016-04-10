package net.dxs.mobilesafe.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.app.App;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.StreamUtil;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
	}

	/**
	 * 软件更新
	 */
	private void autoUpdate() {
		//判断是否开启了自动更新检查
		boolean autoUpdate = SpUtil.getInstance().getBoolean("autoupdate", false);
		if (autoUpdate) {
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
			AppToast.getToast().show("获取更新信息失败,错误码:" + HANDLER_SERVER_CODE_ERROR);
			loadMainUI();
			break;
		case HANDLER_SHOW_UPDATE_DIALOG:
			showUpdateDialog();
			break;
		case HANDLER_URL_MALFORMED:
			AppToast.getToast().show("URL地址错误,错误码:" + HANDLER_URL_MALFORMED);
			loadMainUI();
			break;
		case HANDLER_URL_ERROR:
			AppToast.getToast().show("URL地址错误,错误码:" + HANDLER_URL_ERROR);
			loadMainUI();
			break;
		case HANDLER_NETWORK_ERROR:
			AppToast.getToast().show("您的网络不给力啊,再试下下哈~~~");
			loadMainUI();
			break;
		case HANDLER_JSON_ERROR:
			AppToast.getToast().show("JSON解析错误,错误码:" + HANDLER_SERVER_CODE_ERROR);
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
