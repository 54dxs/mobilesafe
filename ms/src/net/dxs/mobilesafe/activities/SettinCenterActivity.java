package net.dxs.mobilesafe.activities;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.service.CallSmsSafeService;
import net.dxs.mobilesafe.ui.view.SettingView;
import net.dxs.mobilesafe.utils.ServiceStatusUtils;
import net.dxs.mobilesafe.utils.SpUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 设置中心
 * 
 * @author lijian-pc
 * @date 2016-5-9 上午10:20:30
 */
public class SettinCenterActivity extends BaseActivity implements
		OnClickListener {

	// 声明归属地显示的控件
	private SettingView mSv_show_address;
	private Intent showAddressIntent;

	// 声明软件自动更新的控件
	private SettingView mSv_auto_update;

	// 声明黑名单拦截的控件
	private SettingView mSv_callsms_safe;
	private Intent callSmsSafeIntent;

	// 声明程序锁控件
	private SettingView mSv_app_lock;
	private Intent watchDogIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_center);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mSv_auto_update = (SettingView) findViewById(R.id.sv_auto_update);
		mSv_show_address = (SettingView) findViewById(R.id.sv_show_address);
		mSv_callsms_safe = (SettingView) findViewById(R.id.sv_callsms_safe);
		mSv_app_lock = (SettingView) findViewById(R.id.sv_app_lock);
	}

	private void initData() {
		mSv_auto_update.setOnClickListener(this);
		mSv_show_address.setOnClickListener(this);
		mSv_callsms_safe.setOnClickListener(this);
		mSv_app_lock.setOnClickListener(this);

		// 归属地显示的初始化操作
		// showAddressIntent = new Intent(this, CallAddressService.class);

		// 黑名单拦截的初始化操作
		callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);

		// 程序锁初始化
		// watchDogIntent = new Intent(this, WatchDogService.class);
	}

	/**
	 * 当用户看到activity页面的时候调用的方法
	 */
	@Override
	protected void onStart() {
		super.onStart();

		// 检查软件是否自动更新
		boolean autoupdate = SpUtil.getInstance().getBoolean(
				Constants.AUTO_UPDATE, false);
		mSv_auto_update.setChecked(autoupdate);

		// //动态的检查服务的状态
		// boolean showaddress = ServiceStatusUtils.isServiceRunning(this,
		// "net.dxs.mobilesafe.service.CallAddressService");
		// sv_show_address.setChecked(showaddress);

		// 动态的检查黑名单拦截服务的状态
//		boolean callSmsSafe = ServiceStatusUtils.isServiceRunning(this,
//				"net.dxs.mobilesafe.service.CallSmsSafeService");
		boolean callSmsSafe = ServiceStatusUtils
				.isServiceRunning(CallSmsSafeService.class);
		mSv_callsms_safe.setChecked(callSmsSafe);

		// //动态的检查程序锁服务的状态
		// boolean applock = ServiceStatusUtils.isServiceRunning(this,
		// "net.dxs.mobilesafe.service.WatchDogService");
		// sv_applock.setChecked(applock);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sv_auto_update:// 自动更新设置
			if (mSv_auto_update.isChecked()) {
				mSv_auto_update.setChecked(false);
				SpUtil.getInstance().saveBoolean(Constants.AUTO_UPDATE, false);
			} else {
				mSv_auto_update.setChecked(true);
				SpUtil.getInstance().saveBoolean(Constants.AUTO_UPDATE, true);
			}
			break;
		case R.id.sv_show_address:// 归属地显示设置
			if (mSv_show_address.isChecked()) {
				mSv_show_address.setChecked(false);
				stopService(showAddressIntent);
			} else {
				mSv_show_address.setChecked(true);
				startService(showAddressIntent);
			}
			break;
		case R.id.sv_callsms_safe:// 黑名单拦截设置
			if (mSv_callsms_safe.isChecked()) {
				mSv_callsms_safe.setChecked(false);
				stopService(callSmsSafeIntent);
			} else {
				mSv_callsms_safe.setChecked(true);
				startService(callSmsSafeIntent);
			}
			break;
		case R.id.sv_app_lock:// 程序锁设置
			if (mSv_app_lock.isChecked()) {
				mSv_app_lock.setChecked(false);
				stopService(watchDogIntent);
			} else {
				mSv_app_lock.setChecked(true);
				startService(watchDogIntent);
			}
			break;
		}
	}

}
