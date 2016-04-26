package net.dxs.mobilesafe.activities.setup;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.activities.SelectContactActivity;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

/**
 * 设置向导-界面三
 * 
 * @author lijian-pc
 * @date 2016-4-26 下午4:36:17
 */
public class Setup3Activity extends BaseSetupActivity {

	/** 安全号码输入框 */
	private EditText mEt_phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup3);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mEt_phone = (EditText) findViewById(R.id.et_setup3_phone);
	}

	private void initData() {
		mEt_phone.setText(SpUtil.getInstance().getString(
				Constants.LOSTFIND_SETUP3_SAFENUMBER, ""));
	}

	public void next(View v) {
		String safenumber = mEt_phone.getText().toString().trim();
		if (TextUtils.isEmpty(safenumber)) {
			AppToast.getInstance().show("请设置安全号码");
			return;
		}
		SpUtil.getInstance().saveString(Constants.LOSTFIND_SETUP3_SAFENUMBER,
				safenumber);
		loadActivity(Setup4Activity.class);
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}

	public void pre(View v) {
		loadActivity(Setup2Activity.class);
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
	}

	@Override
	protected void showNext() {
		next(null);
	}

	@Override
	protected void showPre() {
		pre(null);
	}

	/**
	 * 选择联系人
	 * 
	 * @param v
	 */
	public void selectContact(View v) {
		Intent intent = new Intent(this, SelectContactActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Constants.ACTIVITYRESULT_RESULTCODE_SELECTCONTACTACTIVITY) {
			if (data != null) {
				String phone = data
						.getStringExtra(Constants.INTENT_SETUP3_CONTACTINFO_PHONE);
				mEt_phone.setText(phone);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
