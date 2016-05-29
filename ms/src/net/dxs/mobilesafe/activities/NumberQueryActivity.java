package net.dxs.mobilesafe.activities;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.db.dao.AddressDao;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 号码归属地查询
 * 
 * @author lijian
 * @date 2016-5-29 上午9:05:12
 */
public class NumberQueryActivity extends BaseActivity implements
		OnClickListener, TextWatcher {

	private EditText mEt_phone_number;
	private Button mBtn_requery;
	private TextView mTv_address;
	private Vibrator mVibrator;
	private String mStr_number;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_query);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mEt_phone_number = (EditText) findViewById(R.id.et_phone_number);
		mTv_address = (TextView) findViewById(R.id.tv_address);
		mBtn_requery = (Button) findViewById(R.id.btn_numberQuery_requery);
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	}

	private void initData() {
		mEt_phone_number.addTextChangedListener(this);
		mBtn_requery.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_numberQuery_requery:// 查询
			requery();
			break;
		}
	}

	/**
	 * 查询归属地
	 */
	private void requery() {
		mStr_number = mEt_phone_number.getText().toString().trim();
		if (TextUtils.isEmpty(mStr_number)) {
			AppToast.getInstance().show("请输入要查询的号码");
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			mEt_phone_number.startAnimation(shake);
			mVibrator.vibrate(1000);
		} else {
			String address = AddressDao.find(mStr_number);
			if (address != null) {
				mTv_address.setText("号码归属地:" + address);
			} else {
				AppToast.getInstance().show("暂无收录该手机号");
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mStr_number = mEt_phone_number.getText().toString().trim();
		if (!TextUtils.isEmpty(mStr_number)) {
			String address = AddressDao.find(mStr_number);
			if (address != null) {
				mTv_address.setText("号码归属地:" + address);
			} else {
				mTv_address.setText("暂无收录该手机号");
			}
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
