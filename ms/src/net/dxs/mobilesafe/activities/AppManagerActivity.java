package net.dxs.mobilesafe.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.app.App;
import net.dxs.mobilesafe.db.dao.ApplockDao;
import net.dxs.mobilesafe.domain.AppInfo;
import net.dxs.mobilesafe.engine.AppInfoProvider;
import net.dxs.mobilesafe.ui.adapter.AppManagerAdapter;
import net.dxs.mobilesafe.ui.adapter.AppManagerAdapter.ViewHolder;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 软件管理
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午3:46:52
 */
public class AppManagerActivity extends BaseActivity implements
		OnScrollListener, OnItemClickListener, OnItemLongClickListener,
		OnClickListener {
	private static final String TAG = "AppManagerActivity";

	private static final int HANDLER_DATA = 1000;
	private static final int ACTIVITY_REQUEST_CODE_UNINSTALL_APPLICATION = 1000;

	private ApplockDao mDao_appLock;
	private LinearLayout mLl_loading;
	private ListView mLv_appmanager;
	private TextView mTv_status;
	private TextView mTv_availRom;
	private TextView mTv_availSD;

	private List<AppInfo> appInfos;
	private ArrayList<AppInfo> userAppInfos;
	private ArrayList<AppInfo> systemAppInfos;

	/** 适配器 */
	private AppManagerAdapter mAdp_appManager;
	/** 当前条目 */
	private AppInfo appInfo;

	private PopupWindow mPopupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mDao_appLock = new ApplockDao(this);
		mLl_loading = (LinearLayout) findViewById(R.id.ll_appManager_loading);
		mLv_appmanager = (ListView) findViewById(R.id.lv_appManager_appmanager);
		mTv_status = (TextView) findViewById(R.id.tv_appManager_status);
		mTv_availRom = (TextView) findViewById(R.id.tv_appManager_availRom);
		mTv_availSD = (TextView) findViewById(R.id.tv_appManager_availSD);
	}

	private void initData() {
		mLv_appmanager.setOnScrollListener(this);
		mLv_appmanager.setOnItemClickListener(this);
		mLv_appmanager.setOnItemLongClickListener(this);
		mTv_availRom.setText("内存可用:" + getAvailRom());
		mTv_availSD.setText("SD卡可用:" + getAvailSD());
		fillData();
	}

	/**
	 * 刷新界面
	 */
	private void fillData() {
		mLl_loading.setVisibility(View.VISIBLE);
		new Thread() {

			public void run() {
				// 所有的应用程序信息
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo appInfo : appInfos) {
					if (appInfo.isUserApp()) {
						userAppInfos.add(appInfo);
					} else {
						systemAppInfos.add(appInfo);
					}
				}
				mHandler.sendEmptyMessage(HANDLER_DATA);
			};
		}.start();
	}

	@Override
	protected void parserMessage(Message msg) {
		super.parserMessage(msg);
		switch (msg.what) {
		case HANDLER_DATA:
			setAdpData();
			break;
		}
	}

	/**
	 * 給适配器设置数据
	 */
	private void setAdpData() {
		mLl_loading.setVisibility(View.INVISIBLE);
		if (mAdp_appManager == null) {
			mAdp_appManager = new AppManagerAdapter(this, mDao_appLock,
					userAppInfos, systemAppInfos);
			mLv_appmanager.setAdapter(mAdp_appManager);
		} else {
			mAdp_appManager.notifyDataSetChanged();
		}
	}

	/**
	 * 获取手机内部存储的可用空间
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getAvailRom() {
		File path = Environment.getDataDirectory();// 手机内部数据存储的目录
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();// 获取每个数据区块的大小
		long availableBlocks = stat.getAvailableBlocks();// 获取还剩多少块的可用空间
		long totalspace = blockSize * availableBlocks;
		return Formatter.formatFileSize(this, totalspace);
	}

	/**
	 * 获取sd卡的可用空间
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getAvailSD() {
		File path = Environment.getExternalStorageDirectory(); // 获取了sd卡的目录
		StatFs stat = new StatFs(path.getPath());// 获得当前linux文件系统的状态
		long blockSize = stat.getBlockSize();// 获取每个数据区块的大小
		long availableBlocks = stat.getAvailableBlocks();// 获取还剩多少块的可用空间
		long totalspace = blockSize * availableBlocks; // int 类型数据最大空间2G
		return Formatter.formatFileSize(this, totalspace);
	}

	/**
	 * 显示一个PopupWindow
	 * 
	 * @param parent
	 * @param view
	 */
	private void showPopupWindow(AdapterView<?> parent, View view) {
		View contentView = View.inflate(getApplicationContext(),
				R.layout.popup_app_item, null);
		LinearLayout ll_uninstall = (LinearLayout) contentView
				.findViewById(R.id.ll_uninstall);
		LinearLayout ll_start = (LinearLayout) contentView
				.findViewById(R.id.ll_start);
		LinearLayout ll_share = (LinearLayout) contentView
				.findViewById(R.id.ll_share);
		LinearLayout ll_detail = (LinearLayout) contentView
				.findViewById(R.id.ll_detail);

		ll_uninstall.setOnClickListener(AppManagerActivity.this);
		ll_start.setOnClickListener(AppManagerActivity.this);
		ll_share.setOnClickListener(AppManagerActivity.this);
		ll_detail.setOnClickListener(AppManagerActivity.this);

		mPopupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
				250);// 设置浮窗及其大小
		mPopupWindow
				.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		int[] location = new int[2];
		view.getLocationInWindow(location);// 获得所点击的view的距屏幕x,y的坐标

		int px = (int) App.dip2px(60.0f);// 60dip
		// java代码里指定的宽高都是px
		mPopupWindow.showAtLocation(parent, Gravity.LEFT + Gravity.TOP,
				location[0] + px, location[1]);// 设置相对于父窗体位置

		AlphaAnimation aa = new AlphaAnimation(0.4f, 1.0f);// 定义渐变动画
		aa.setDuration(200);
		// 定义缩放动画
		ScaleAnimation sa = new ScaleAnimation(0.5f, 1.0f, 1.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
		sa.setDuration(200);
		AnimationSet set = new AnimationSet(false);// 定义动画数组
		set.addAnimation(aa);
		set.addAnimation(sa);
		contentView.startAnimation(set);// 播放动画
	}

	/**
	 * 关闭PopupWindow
	 */
	private void dismissPopupWindow() {
		if (mPopupWindow != null) {
			if (mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
				mPopupWindow = null;
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
	}

	// 当ListView滚动的时候调用的方法
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		L.i(TAG, "firstVisibleItem:" + firstVisibleItem);
		L.i(TAG, "visibleItemCount:" + visibleItemCount);
		L.i(TAG, "totalItemCount:" + totalItemCount);
		if (userAppInfos != null && systemAppInfos != null) {
			if (firstVisibleItem > userAppInfos.size()) {
				// 当系统程序标签被拖上来的时候
				mTv_status.setText("系统程序:" + systemAppInfos.size());
			} else {
				mTv_status.setText("用户程序:" + userAppInfos.size());
			}
			dismissPopupWindow();// 关闭popupwindow窗体
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == 0) {// 显示用户程序标签
			return;
		} else if (position == (userAppInfos.size() + 1)) {
			return;
		} else if (position <= userAppInfos.size()) {// 用户程序
			int newposition = position - 1;// 用户程序的标签占用了一个位置
			appInfo = userAppInfos.get(newposition);
		} else {// 系统程序
			int newposition = position - userAppInfos.size() - 1 - 1;// 减去用户程序的个数,再减去两个TextView
			appInfo = systemAppInfos.get(newposition);
		}

		L.i(TAG, "当前被点击的条目：" + appInfo.toString());
		// 弹出一个悬浮窗体
		// contentView窗体里面显示的内容
		dismissPopupWindow();// 关闭popupwindow窗体
		showPopupWindow(parent, view);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (position == 0) {// 显示用户程序标签
			return false;
		} else if (position == (userAppInfos.size() + 1)) {
			return false;
		} else if (position <= userAppInfos.size()) {// 用户程序
			int newposition = position - 1;// textview占用了一个位置
			appInfo = userAppInfos.get(newposition);
		} else {// 系统程序
			int newposition = position - 1 - userAppInfos.size() - 1;// 减去用户程序个数
			appInfo = systemAppInfos.get(newposition);
		}
		String packname = appInfo.getPackName();
		ViewHolder holder = (ViewHolder) view.getTag();

		if (mDao_appLock.find(packname)) {// 锁定的应用程序，解除锁定，修改图片为打开的锁
			mDao_appLock.delete(packname);
			holder.iv_status.setImageResource(R.drawable.unlock);
		} else {// 锁定应用，修改图片为关闭的锁
			mDao_appLock.add(packname);
			holder.iv_status.setImageResource(R.drawable.lock);
		}
		return true;// true消费了点击事件 false 没有消费事件
	}

	@Override
	public void onClick(View v) {
		dismissPopupWindow();
		switch (v.getId()) {
		case R.id.ll_uninstall:
			L.i(TAG, "卸载" + appInfo.getAppName());
			uninstallApplication();
			break;
		case R.id.ll_share:
			L.i(TAG, "分享" + appInfo.getAppName());
			shareApplication();
			break;
		case R.id.ll_start:
			L.i(TAG, "启动" + appInfo.getAppName());
			startApplication();
			break;
		case R.id.ll_detail:
			L.i(TAG, "详细" + appInfo.getAppName());
			startDetailPage();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ACTIVITY_REQUEST_CODE_UNINSTALL_APPLICATION:
			// 刷新界面
			fillData();
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissPopupWindow();
	}

	/**
	 * 卸载一个应用程序
	 */
	private void uninstallApplication() {
		// <action android:name="android.intent.action.DELETE" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:scheme="package" />
		if (appInfo.isUserApp()) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.DELETE");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setData(Uri.parse("package:" + appInfo.getPackName()));
			startActivityForResult(intent,
					ACTIVITY_REQUEST_CODE_UNINSTALL_APPLICATION);
		} else {
			AppToast.getInstance().show("系统应用需要有root权限才能卸载");
		}
	}

	/**
	 * 分享一个应用程序
	 */
	private void shareApplication() {
		// <action android:name="android.intent.action.SEND" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:mimeType="text/plain" />
		Intent intent = new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT,
				"推荐您使用一款软件，软件的名称叫：" + appInfo.getAppName() + "应用程序包名："
						+ appInfo.getPackName());
		startActivity(intent);
	}

	/**
	 * 启动一个应用程序
	 */
	private void startApplication() {
		PackageManager manager = getPackageManager();
		Intent intent = manager
				.getLaunchIntentForPackage(appInfo.getPackName());// 获得有启动activity的意图
		if (intent != null) {
			startActivity(intent);
		} else {
			AppToast.getInstance().show("无法启动该应用");
		}
	}

	/**
	 * 进入应用程序的详细信息
	 */
	private void startDetailPage() {
		Intent intent = new Intent();
		intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(Uri.parse("package:" + appInfo.getPackName()));
		startActivity(intent);
	}
}
