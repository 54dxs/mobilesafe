package net.dxs.mobilesafe.activities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 缓存清理
 * 
 * @author lijian
 * @date 2016-5-24 下午11:20:36
 */
public class CleanCacheActivity extends BaseActivity implements OnClickListener {

	private static final int HANDLER_SCANING = 1;
	private static final int HANDLER_SCAN_FINISH = 2;

	private Button mBtn_cleanAll;
	private ProgressBar mPb_progress;
	private TextView mTv_status;
	private LinearLayout mLl_container;
	private List<CacheInfo> mList_cacheInfo;
	private PackageManager mPm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_cache);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mBtn_cleanAll = (Button) findViewById(R.id.btn_cleanAll);
		mPb_progress = (ProgressBar) findViewById(R.id.pb_progress);
		mTv_status = (TextView) findViewById(R.id.tv_status);
		mLl_container = (LinearLayout) findViewById(R.id.ll_container);
	}

	private void initData() {
		mBtn_cleanAll.setOnClickListener(this);
		scanCache();
	}

	/**
	 * 扫描缓存
	 */
	private void scanCache() {
		mList_cacheInfo = new ArrayList<CleanCacheActivity.CacheInfo>();
		new Thread() {

			public void run() {
				mPm = getPackageManager();
				// 得到所有的应用程序
				List<PackageInfo> infos = mPm.getInstalledPackages(0);
				mPb_progress.setMax(infos.size());
				int total = 0;
				for (PackageInfo info : infos) {
					String packname = info.packageName;
					getPackSize(packname, info.applicationInfo.loadLabel(mPm)
							.toString());
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendMsg(HANDLER_SCANING, info);
					total++;
					mPb_progress.setProgress(total);
				}
				mHandler.sendEmptyMessage(HANDLER_SCAN_FINISH);
			};
		}.start();
	}

	@Override
	protected void parserMessage(Message msg) {
		super.parserMessage(msg);
		switch (msg.what) {
		case HANDLER_SCANING:
			scaning(msg);
			break;

		case HANDLER_SCAN_FINISH:
			scanFinish();
			break;
		}
	}

	/**
	 * 扫描中...
	 * 
	 * @param msg
	 */
	private void scaning(Message msg) {
		PackageInfo info = (PackageInfo) msg.obj;
		String name = info.applicationInfo.loadLabel(mPm).toString();
		mTv_status.setText("正在扫描：" + name);
	}

	/**
	 * 扫描完成
	 */
	private void scanFinish() {
		if (mList_cacheInfo.size() > 0) {
			for (final CacheInfo cacheInfo : mList_cacheInfo) {
				TextView tv = new TextView(getApplicationContext());
				tv.setText(cacheInfo.appname
						+ "--缓存大小→"
						+ Formatter.formatFileSize(getApplicationContext(),
								cacheInfo.cache));
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// mPm.deleteApplicationCacheFiles(packageName,mClearCacheObserver);
						try {
							Method method = PackageManager.class.getMethod(
									"deleteApplicationCacheFiles",
									String.class, IPackageDataObserver.class);
							method.invoke(mPm, cacheInfo.packname,
									new MyDataObserver());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				mLl_container.addView(tv);
			}
		} else {
			AppToast.getInstance().show("扫描完毕,没有发现缓存...");
		}
	}

	/**
	 * 获得应用程序包大小
	 * 
	 * @param packname
	 *            包名
	 * @param appname
	 *            应用程序名称
	 */
	private void getPackSize(String packname, String appname) {
		try {
			Method method = PackageManager.class.getDeclaredMethod(
					"getPackageSizeInfo", String.class,
					IPackageStatsObserver.class);
			method.invoke(mPm, packname, new MyObserver(appname));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class CacheInfo {
		String packname;
		long cache;
		String appname;
	}

	private class MyObserver extends IPackageStatsObserver.Stub {
		private String appname;

		public MyObserver(String appname) {
			this.appname = appname;
		}

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			long cache = pStats.cacheSize;
			if (cache > 0) {
				CacheInfo cacheInfo = new CacheInfo();
				cacheInfo.packname = pStats.packageName;
				cacheInfo.cache = cache;
				cacheInfo.appname = appname;
				mList_cacheInfo.add(cacheInfo);
			}

			// if (cache > 0) {
			// runOnUiThread(new Runnable() {
			// @Override
			// public void run() {
			// try {
			// TextView tv = new TextView(getApplicationContext());
			// String packname = pStats.packageName;
			// String appname = pm.getApplicationInfo(packname, 0)
			// .loadLabel(pm).toString();
			// tv.setText(appname
			// + "--缓存--"
			// + Formatter.formatFileSize(
			// getApplicationContext(), cache));
			// ll_container.addView(tv);
			// } catch (NameNotFoundException e) {
			// e.printStackTrace();
			// }
			// }
			// });
			// }
		}
	}

	// IPackageDataObserver
	private class MyDataObserver extends IPackageDataObserver.Stub {

		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded)
				throws RemoteException {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cleanAll:// 一件清理
			cleanAll();
			break;
		}
	}

	/**
	 * 清理全部缓存
	 */
	private void cleanAll() {
		Method[] methods = PackageManager.class.getMethods();
		for (Method method : methods) {
			if ("freeStorageAndNotify".equals(method.getName())) {
				try {
					method.invoke(mPm, Integer.MAX_VALUE, new MyDataObserver());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
		}
	}
}
