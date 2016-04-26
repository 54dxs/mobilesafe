package net.dxs.mobilesafe.activities.setup;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

/**
 * 设置向导-界面二
 * 
 * @author lijian-pc
 * @date 2016-4-26 上午11:40:56
 */
public class Setup2Activity extends BaseSetupActivity {

	/** sim卡是否获取的状态图标 */
	private ImageView mIv_status;
	/** 电话管理器 */
	private TelephonyManager mTelephonyManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup2);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mIv_status = (ImageView) findViewById(R.id.iv_setup2_status);
	}

	private void initData() {
		mTelephonyManager = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		// 判断sim卡的绑定状态，如果sim卡绑定了就修改图片
		checkSIM();
	}

	/**
	 * 判断sim卡的绑定状态，如果sim卡绑定了就修改图片
	 */
	private void checkSIM() {
		String savedSim = SpUtil.getInstance().getString(
				Constants.LOSTFIND_SETUP2_SIM, "");
		if (!TextUtils.isEmpty(savedSim)) {
			mIv_status.setImageResource(R.drawable.lock);
		}
	}

	public void next(View v) {
		// 判断是否绑定了sim卡，如果没有绑定就提示用户绑定sim卡
		String savedSim = SpUtil.getInstance().getString(
				Constants.LOSTFIND_SETUP2_SIM, "");
		if (TextUtils.isEmpty(savedSim)) {
			AppToast.getInstance().show("请先绑定sim卡");
			return;
		}
		loadActivity(Setup3Activity.class);
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}

	public void pre(View v) {
		loadActivity(Setup1Activity.class);
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
	 * 绑定sim卡对应的点击事件
	 * 
	 * @param v
	 */
	public void bindSim(View v) {
		// 判断是否已经绑定了sim卡
		String savedSim = SpUtil.getInstance().getString(
				Constants.LOSTFIND_SETUP2_SIM, "");
		if (TextUtils.isEmpty(savedSim)) {
			// TODO 注意这里要真机带SIM卡，否则会崩溃
			String sim = mTelephonyManager.getSimSerialNumber();// 获取sim卡的唯一串号
			SpUtil.getInstance().saveString(Constants.LOSTFIND_SETUP2_SIM, sim);
			mIv_status.setImageResource(R.drawable.lock);
		} else {// 原来绑定过sim卡
			SpUtil.getInstance()
					.saveString(Constants.LOSTFIND_SETUP2_SIM, null);
			mIv_status.setImageResource(R.drawable.unlock);
		}
	}
}
