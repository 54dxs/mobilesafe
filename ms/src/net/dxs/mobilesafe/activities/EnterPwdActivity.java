package net.dxs.mobilesafe.activities;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 看门狗-密码输入
 * 
 * @author lijian-pc
 * @date 2016-5-10 下午1:59:06
 */
public class EnterPwdActivity extends BaseActivity implements OnClickListener {

	private TextView mTv_name;
	private ImageView mIv_icon;
	private EditText mEt_password;
	private Button mBtn_ok;

	private String packname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enter_pwd);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mTv_name = (TextView) findViewById(R.id.tv_enterPwd_name);
		mIv_icon = (ImageView) findViewById(R.id.iv_enterPwd_icon);
		mEt_password = (EditText) findViewById(R.id.et_enterPwd_password);
		mBtn_ok = (Button) findViewById(R.id.btn_enterPwd_ok);
	}

	private void initData() {
		packname = getIntent().getStringExtra(
				Constants.INTENT_DATA_LOCKED_PACKNAMES);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packInfo = pm.getPackageInfo(packname, 0);
			mTv_name.setText(packInfo.applicationInfo.loadLabel(pm));
			mIv_icon.setImageDrawable(packInfo.applicationInfo.loadIcon(pm));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mBtn_ok.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		// 返回桌面
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addCategory("android.intent.category.MONKEY");// 做自动化测试的时候用到
		startActivity(intent);
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_enterPwd_ok:

			String password = mEt_password.getText().toString().trim();
			if (TextUtils.isEmpty(password)) {
				AppToast.getInstance().show("密码不能为空");
				return;
			}
			// 假设正确的密码是123，后期设置为用户自定义
			if ("123".equals(password)) {
				// 告诉看门狗 不要再去保护当前的应用程序了。
				// 一个组件 想给另外一个组件发个信号
				// 自定义的广播消息
				Intent intent = new Intent();
				intent.setAction(Constants.INTENT_ACTION_STOPPROTECT);
				intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 包含从未启动过的应用(3.1之后默认不包含)
				intent.putExtra(Constants.INTENT_DATA_LOCKED_PACKNAMES,
						packname);
				sendBroadcast(intent);// 发送自定义的广播要求停止保护packname的应用程序
				finish();// 关闭掉输入密码的界面。
			}
			break;

		default:
			break;
		}
	}
}
