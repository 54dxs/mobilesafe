package net.dxs.mobilesafe.activities.setup;

import net.dxs.mobilesafe.R;
import android.os.Bundle;
import android.view.View;

/**
 * 设置向导-界面一
 * 
 * @author lijian-pc
 * @date 2016-4-26 上午11:05:35
 */
public class Setup1Activity extends BaseSetupActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup1);
	}

	public void next(View v) {
		loadActivity(Setup2Activity.class);
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}

	@Override
	protected void showNext() {
		loadActivity(Setup2Activity.class);
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}

	@Override
	protected void showPre() {

	}
}
