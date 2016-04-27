package net.dxs.mobilesafe.activities.setup;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.activities.LostFindActivity;
import net.dxs.mobilesafe.utils.SpUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 设置向导-界面四
 * 
 * @author lijian-pc
 * @date 2016-4-27 下午2:54:34
 */
public class Setup4Activity extends BaseSetupActivity implements
		OnCheckedChangeListener {

	/** 防盗保护是否勾选 */
	private CheckBox mCb_status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup4);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mCb_status = (CheckBox) findViewById(R.id.cb_setup4_status);
	}

	private void initData() {
		mCb_status.setOnCheckedChangeListener(this);
		setProtectingStatus();
	}

	/**
	 * 设置当前保护状态
	 */
	private void setProtectingStatus() {
		boolean protectingstatus = SpUtil.getInstance().getBoolean(
				Constants.LOSTFIND_PROTECTING_STATUS, false);
		if (protectingstatus) {
			mCb_status.setChecked(true);
			mCb_status.setText("防盗保护已经开启");
		} else {
			mCb_status.setChecked(false);
			mCb_status.setText("防盗保护没有开启");
		}
	}

	public void next(View v) {
		// 在sp里面存放一个finishsetup->true即设置向导完成
		SpUtil.getInstance().saveBoolean(Constants.LOSTFIND_FINISH_SETUP, true);

		loadActivity(LostFindActivity.class);
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}

	public void pre(View v) {
		loadActivity(Setup3Activity.class);
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			mCb_status.setText("防盗保护已经开启");
		} else {
			mCb_status.setText("防盗保护没有开启");
		}
		SpUtil.getInstance().saveBoolean(
				Constants.LOSTFIND_PROTECTING_STATUS, isChecked);
	}

}
