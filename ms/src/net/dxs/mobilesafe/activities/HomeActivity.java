package net.dxs.mobilesafe.activities;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.FunctionEntry;
import net.dxs.mobilesafe.ui.adapter.HomeAdapter;
import net.dxs.mobilesafe.utils.Md5Utils;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

/**
 * 程序主界面
 * 
 * @author lijian
 * @date 2016-4-8 下午5:51:47
 */
public class HomeActivity extends BaseActivity implements OnItemClickListener {

	private static final String TAG = "HomeActivity";

	private static final String[] names = { "手机防盗", "通讯卫士", "软件管理", "进程管理",
			"流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心" };
	private static final int[] icons = { R.drawable.safe,
			R.drawable.callmsgsafe_selector, R.drawable.app,
			R.drawable.taskmanager, R.drawable.netmanager, R.drawable.trojan,
			R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings };

	private GridView mGr_functionEntry;

	private EditText mEt_pwd;
	private EditText mEt_pwd_confirm;
	private Button mBtn_ok;
	private Button mBtn_cancle;
	private AlertDialog mAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mGr_functionEntry = (GridView) findViewById(R.id.gv_home_functionentry);
	}

	private void initData() {
		List<FunctionEntry> list = new ArrayList<FunctionEntry>();

		for (int i = 0; i < names.length; i++) {
			FunctionEntry entry = new FunctionEntry(names[i], icons[i]);
			list.add(entry);
		}

		mGr_functionEntry.setAdapter(new HomeAdapter(this, list));
		mGr_functionEntry.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0:// 手机防盗
			checkPwd();
			break;

		case 1:// 通讯卫士
			break;

		case 2:// 软件管理
			break;

		case 3:// 进程管理
			break;

		case 4:// 流量统计
			break;

		case 5:// 手机杀毒
			break;

		case 6:// 缓存清理
			break;

		case 7:// 高级工具
			break;

		case 8:// 设置中心
			break;
		}
	}

	/**
	 * 检测用户是否设置过密码
	 */
	private void checkPwd() {
		String strPwd = SpUtil.getInstance().getString("password");
		if (TextUtils.isEmpty(strPwd)) {
			// 没有设置过密码
			Log.i(TAG, "没有设置过密码,弹出设置密码对话框");
			showSetupPasswordDialog();
		} else {
			// 设置过密码
			Log.i(TAG, "设置过密码,弹出输入密码对话框");
			showEnterPasswordDialog();
		}
	}

	/**
	 * 设置密码对话框
	 */
	private void showSetupPasswordDialog() {
		// 自定义布局的对话框
		AlertDialog.Builder builder = new Builder(this);

		View view = View.inflate(this, R.layout.dialog_setup_pwd, null);
		mEt_pwd = (EditText) view.findViewById(R.id.et_setup_password);
		mEt_pwd_confirm = (EditText) view
				.findViewById(R.id.et_setup_password_confirm);
		mBtn_ok = (Button) view.findViewById(R.id.btn_setup_ok);
		mBtn_cancle = (Button) view.findViewById(R.id.btn_setup_cancle);
		mBtn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String strPwd = mEt_pwd.getText().toString().trim();
				String strPwdConfirm = mEt_pwd_confirm.getText().toString()
						.trim();
				if (TextUtils.isEmpty(strPwd)
						|| TextUtils.isEmpty(strPwdConfirm)) {
					AppToast.getInstance().show("对不起,密码输入不能为空!");
					return;
				}
				if (!strPwd.equals(strPwdConfirm)) {
					AppToast.getInstance().show("两次密码输入不一致");
					return;
				}
				SpUtil.getInstance().saveString(Constants.LOSTFIND_PSSWORD,
						Md5Utils.encode(strPwd));
				mAlertDialog.dismiss();
			}
		});
		mBtn_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAlertDialog.dismiss();
			}
		});

		builder.setView(view);
		mAlertDialog = builder.create();
		mAlertDialog.show();
	}

	/**
	 * 输入密码对话框
	 */
	private void showEnterPasswordDialog() {
		// 自定义布局的对话框
		AlertDialog.Builder builder = new Builder(this);

		View view = View.inflate(this, R.layout.dialog_enter_pwd, null);
		mEt_pwd = (EditText) view.findViewById(R.id.et_enter_password);
		mBtn_ok = (Button) view.findViewById(R.id.btn_enter_ok);
		mBtn_cancle = (Button) view.findViewById(R.id.btn_enter_cancle);
		mBtn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 用户输入的密码
				String strPwd = mEt_pwd.getText().toString().trim();
				if (TextUtils.isEmpty(strPwd)) {
					AppToast.getInstance().show("亲,密码不能为空哦!");
					return;
				}
				// 得到本地保存的密码加密后的密文
				String strLocalPWD = SpUtil.getInstance().getString(
						Constants.LOSTFIND_PSSWORD);
				if (Md5Utils.encode(strPwd).equals(strLocalPWD)) {
					// 密码正确进入手机防盗界面
					mAlertDialog.dismiss();
					Intent intent = new Intent(HomeActivity.this,
							LostFindActivity.class);
					startActivity(intent);
				} else {
					AppToast.getInstance().show("密码错误!");
				}
			}
		});
		mBtn_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAlertDialog.dismiss();
			}
		});

		builder.setView(view);
		mAlertDialog = builder.create();
		mAlertDialog.show();
	}
}
