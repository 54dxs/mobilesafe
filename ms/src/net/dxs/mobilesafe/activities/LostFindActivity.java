package net.dxs.mobilesafe.activities;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.receiver.MyAdmin;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 手机防盗
 * 
 * @author lijian-pc
 * @date 2016-4-19 下午6:03:17
 */
public class LostFindActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "LostFindActivity";

	/** 安全号码 */
	private TextView mTv_number;
	/** 重新进入设置向导 */
	private TextView mTv_reentrySetup;
	/** 防盗保护是否已开启图标锁 */
	private ImageView mIv_status;
	/** 激活短信指令功能 */
	private Button mBtn_active;

	/** 设备管理器 */
	private DevicePolicyManager mDpm;
	/** 组件-设备管理员 */
	private ComponentName mComponent_deviceAdmin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 判断用户是否进行过设置向导
		boolean finishSetup = SpUtil.getInstance().getBoolean(
				Constants.LOSTFIND_FINISHSETUP, true);
		if (finishSetup) {// 如果配置过，就显示正常的ui界面
			// 已经完成过设置向导,加载正常的ui界面
			init();
		} else {
			// 定向页面到设置向导页面
			// Intent intent = new Intent(this, Setup1Activity.class);
			// startActivity(intent);
			// this.finish();
		}
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		setContentView(R.layout.activity_lostfind);
		mTv_number = (TextView) findViewById(R.id.tv_lostfind_number);
		mIv_status = (ImageView) findViewById(R.id.iv_lostfind_status);
		mTv_reentrySetup = (TextView) findViewById(R.id.tv_lostfind_reentrySetup);
		mBtn_active = (Button) findViewById(R.id.btn_lostfind_active);
	}

	private void initData() {
		mTv_number.setText(SpUtil.getInstance().getString(
				Constants.LOSTFIND_SAFENUMBER, ""));
		mTv_reentrySetup.setOnClickListener(this);
		mBtn_active.setOnClickListener(this);
		lockStatus();

		// 获得设备管理器
		mDpm = (DevicePolicyManager) this
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		mComponent_deviceAdmin = new ComponentName(this, MyAdmin.class);
	}

	/**
	 * 防盗保护是否开启-图标锁状态控制
	 */
	private void lockStatus() {
		if (SpUtil.getInstance().getBoolean(
				Constants.LOSTFIND_PROTECTINGSTATUS, false)) {
			mIv_status.setImageResource(R.drawable.lock);
		} else {
			mIv_status.setImageResource(R.drawable.unlock);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_lostfind_active:
			setAdminActive();
			break;
		case R.id.tv_lostfind_reentrySetup:
			reentrySetup();
			break;
		}
	}

	/**
	 * 重新进入设置向导页面
	 */
	private void reentrySetup() {
		// 定向页面到设置向导页面
		// Intent intent = new Intent(this, Setup1Activity.class);
		// startActivity(intent);
		// this.finish();
	}

	/**
	 * 设置-设备管理器是否启用
	 */
	private void setAdminActive() {
		boolean isAdminActive = mDpm.isAdminActive(mComponent_deviceAdmin);
		L.i(TAG, "isAdminActive--->" + isAdminActive);
		if (isAdminActive) {
			cancel_deviceAdmin();
		} else {
			activation_deviceAdmin();
		}
	}

	/**
	 * 激活-设备管理器
	 */
	private void activation_deviceAdmin() {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		// 意图里面携带的数据
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
				mComponent_deviceAdmin);
		intent.putExtra(
				DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				"开启后可以通过绑定的“安全号码”远程\n发送短信指令#*location*#对手机进行远程GPS追踪,\n发送短信指令#*alarm*#对手机进行播放报警音乐,\n发送短信指令#*wipedata*#对手机进行远程擦除数据,\n发送短信指令#*lockscreen*#对手机进行远程锁屏.");
		startActivity(intent);
	}

	/**
	 * 关闭-设备管理器
	 */
	private void cancel_deviceAdmin() {
		// 移除激活的组件
		mDpm.removeActiveAdmin(mComponent_deviceAdmin);
		AppToast.getInstance().show("哦买噶,短信指令被你取消了");
		setDeviceAdminStatus();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setDeviceAdminStatus();
	}

	/**
	 * 设置设备管理按钮的显示状态
	 */
	private void setDeviceAdminStatus() {
		boolean isAdminActive = mDpm.isAdminActive(mComponent_deviceAdmin);
		L.i(TAG, "isAdminActive--->" + isAdminActive);
		if (isAdminActive) {
			mBtn_active.setText("激活短信指令功能");
			mBtn_active.setTextColor(Color.RED);
		} else {
			mBtn_active.setText("取消短信指令功能");
			mBtn_active.setTextColor(Color.BLACK);
		}
	}
}
