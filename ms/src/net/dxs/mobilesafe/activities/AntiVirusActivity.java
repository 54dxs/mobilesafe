package net.dxs.mobilesafe.activities;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.db.dao.AntivirusDao;
import net.dxs.mobilesafe.utils.Md5Utils;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 手机杀毒
 * 
 * @author lijian
 * @date 2016-5-28 上午9:45:56
 */
public class AntiVirusActivity extends BaseActivity {

	/** 扫描状态 */
	private static final int HANDLER_SCAN_STATUS = 1000;
	/** 发现病毒 */
	private static final int HANDLER_FIND_VIRUS = 1001;
	/** 扫描完成 */
	private static final int HANDLER_SCAN_COMPLETE = 1002;

	private ProgressBar mPb_progress;
	private TextView mTv_scan_status;
	private LinearLayout mLl_container;
	private ImageView mIv_scan;

	/** 病毒包名集合 */
	private ArrayList<String> virusPacknames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_virus);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mPb_progress = (ProgressBar) findViewById(R.id.pb_progress);
		mTv_scan_status = (TextView) findViewById(R.id.tv_scan_status);
		mLl_container = (LinearLayout) findViewById(R.id.ll_container);
		mIv_scan = (ImageView) findViewById(R.id.iv_scan);
	}

	private void initData() {
		scanAnimation();
		scanVirus();
	}

	/**
	 * 扫描动画
	 */
	private void scanAnimation() {
		RotateAnimation ra = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		ra.setDuration(1000);
		ra.setRepeatCount(Animation.INFINITE);
		mIv_scan.startAnimation(ra);
	}

	@Override
	protected void parserMessage(Message msg) {
		super.parserMessage(msg);
		switch (msg.what) {
		case HANDLER_SCAN_STATUS:// 扫描病毒
			mTv_scan_status.setText(msg.obj.toString());
			break;

		case HANDLER_FIND_VIRUS:// 发现病毒
			findVirus(msg);
			break;

		case HANDLER_SCAN_COMPLETE:// 扫描完成
			scanComplete();
			break;

		default:
			break;
		}
	}

	/**
	 * 扫描完成
	 */
	private void scanComplete() {
		mTv_scan_status.setText("扫描完毕");
		mIv_scan.clearAnimation();
		mIv_scan.setVisibility(View.INVISIBLE);
		AlertDialog.Builder builder = new Builder(AntiVirusActivity.this);
		if (virusPacknames.size() > 0) {
			builder.setTitle("警告!");
			builder.setMessage("在您的手机里面发现了" + virusPacknames.size()
					+ "个病毒！！,不查杀手机就会爆炸！");
			builder.setPositiveButton("立刻处理", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					for (String packname : virusPacknames) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_DELETE);
						intent.setData(Uri.parse("package:" + packname));
						startActivity(intent);
					}
				}
			});
			builder.setNegativeButton("我不怕", null);
			builder.show();
		} else {
			builder.setTitle("提示!");
			builder.setMessage("你的手机非常安全,请放心使用...");
			builder.setNegativeButton("确定", null);
			builder.show();
			AppToast.getInstance().show("你的手机非常安全,请放心使用...");
		}
	}

	/**
	 * 发现病毒
	 * 
	 * @param msg
	 */
	private void findVirus(Message msg) {
		TextView tv = new TextView(getApplicationContext());
		tv.setTextColor(Color.BLACK);
		tv.setTextSize(16);
		tv.setText(msg.obj.toString());
		mLl_container.addView(tv, 0);
	}

	/**
	 * 扫描病毒
	 */
	private void scanVirus() {
		mTv_scan_status.setText("正在初始化8核杀毒引擎。");
		virusPacknames = new ArrayList<String>();
		new Thread() {
			public void run() {
				SystemClock.sleep(2000);
				final PackageManager pm = getPackageManager();
				List<PackageInfo> infos = pm
						.getInstalledPackages(PackageManager.GET_SIGNATURES);
				mPb_progress.setMax(infos.size());
				int total = 0;
				for (final PackageInfo info : infos) {
					sendMsg(HANDLER_SCAN_STATUS,
							"正在扫描：" + info.applicationInfo.loadLabel(pm));
					String md5 = Md5Utils.encode(info.signatures[0]
							.toCharsString());
					// 查询md5信息是否在病毒数据库里面。
					String result = AntivirusDao.find(md5);
					final String showinfo;
					if (result != null) {
						showinfo = "发现病毒→☆"
								+ info.applicationInfo.loadLabel(pm)
								+ "☆\n病毒类型：" + result;
						virusPacknames.add(info.packageName);
					} else {
						showinfo = "扫描安全→" + info.applicationInfo.loadLabel(pm);
					}
					sendMsg(HANDLER_FIND_VIRUS, showinfo);

					total++;
					mPb_progress.setProgress(total);
					// 由于程序执行的太快，这里故意睡眠100ms，让用户感觉程序在努力的扫描病毒
					SystemClock.sleep(100);
				}
				mHandler.sendEmptyMessage(HANDLER_SCAN_COMPLETE);
			};
		}.start();
	}

}
