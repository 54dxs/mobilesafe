package net.dxs.mobilesafe.activities;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.engine.SmsTools;
import net.dxs.mobilesafe.engine.SmsTools.BackupSmsCallback;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 高级工具
 * 
 * @author lijian
 * @date 2016-5-29 上午8:49:05
 */
public class AtoolsActivity extends BaseActivity implements OnClickListener {

	private Button mBtn_numberAddressQuery;
	private Button mBtn_smsBackUp;
	private Button mBtn_smsRestore;
	private ProgressDialog mPd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atools);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mBtn_numberAddressQuery = (Button) findViewById(R.id.btn_atools_numberAddressQuery);
		mBtn_smsBackUp = (Button) findViewById(R.id.btn_atools_smsBackUp);
		mBtn_smsRestore = (Button) findViewById(R.id.btn_atools_smsRestore);
	}

	private void initData() {
		mBtn_numberAddressQuery.setOnClickListener(this);
		mBtn_smsBackUp.setOnClickListener(this);
		mBtn_smsRestore.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_atools_numberAddressQuery:// 查询号码归属地
			numberAddressQuery();
			break;
		case R.id.btn_atools_smsBackUp:// 短息备份
			smsBackUp();
			break;
		case R.id.btn_atools_smsRestore:// 短信还原
			smsRestore();
			break;
		}
	}

	/**
	 * 查询号码归属地
	 */
	private void numberAddressQuery() {
		Intent intent = new Intent(this, NumberQueryActivity.class);
		startActivity(intent);
	}

	/**
	 * 短息备份
	 */
	private void smsBackUp() {
		mPd = new ProgressDialog(this);
		mPd.setTitle("正在备份...");
		mPd.setCancelable(false);
		mPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mPd.show();

		new Thread() {
			public void run() {
				try {
					SmsTools.backupSms(AtoolsActivity.this,
							new BackupSmsCallback() {

								@Override
								public void onSmsBackup(int progress) {
									mPd.setProgress(progress);
								}

								@Override
								public void beforeSmsBackup(int max) {
									mPd.setMax(max);
								}
							});
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AppToast.getInstance().show("恭喜你,短信备份成功");
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AppToast.getInstance().show("短信备份失败");
						}
					});
				} finally {
					mPd.dismiss();
				}
			};
		}.start();
	}

	/**
	 * 短信还原
	 */
	private void smsRestore() {
		mPd = new ProgressDialog(this);
		mPd.setTitle("正在还原...");
		mPd.setCancelable(false);
		mPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mPd.show();

		new Thread() {
			public void run() {
				try {
					SmsTools.RestoreSms(AtoolsActivity.this,
							new BackupSmsCallback() {

								@Override
								public void onSmsBackup(int progress) {
									mPd.setProgress(progress);
								}

								@Override
								public void beforeSmsBackup(int max) {
									mPd.setMax(max);
								}
							});
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AppToast.getInstance().show("恭喜你,短信还原成功");
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AppToast.getInstance().show("短信还原失败");
						}
					});
				} finally {
					mPd.dismiss();
				}
			};
		}.start();
	}
}
