package net.dxs.mobilesafe.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.db.dao.ApplockDao;
import net.dxs.mobilesafe.domain.AppInfo;
import net.dxs.mobilesafe.engine.AppInfoProvider;
import net.dxs.mobilesafe.ui.adapter.AppManagerAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 软件管理
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午3:46:52
 */
public class AppManagerActivity extends BaseActivity implements
		OnScrollListener, OnItemClickListener, OnItemLongClickListener {

	private static final int HANDLER_DATA = 1000;

	private ApplockDao mDao_appLock;
	private LinearLayout mLl_loading;
	private ListView mLv_appmanager;
	private TextView mTv_status;
	private TextView mTv_availRom;
	private TextView mTv_availSD;

	private List<AppInfo> appInfos;
	private ArrayList<AppInfo> userAppInfos;
	private ArrayList<AppInfo> systemAppInfos;

	private AppManagerAdapter mAdp_appManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mDao_appLock = new ApplockDao(this);
		mLl_loading = (LinearLayout) findViewById(R.id.ll_appManager_loading);
		mLv_appmanager = (ListView) findViewById(R.id.lv_appManager_appmanager);
		mTv_status = (TextView) findViewById(R.id.tv_appManager_status);
		mTv_availRom = (TextView) findViewById(R.id.tv_appManager_availRom);
		mTv_availSD = (TextView) findViewById(R.id.tv_appManager_availSD);
	}

	private void initData() {
		mLv_appmanager.setOnScrollListener(this);
		mLv_appmanager.setOnItemClickListener(this);
		mLv_appmanager.setOnItemLongClickListener(this);
		mTv_availRom.setText("内存可用:" + getAvailRom());
		mTv_availSD.setText("SD卡可用:" + getAvailSD());
		fillData();
	}

	/**
	 * 刷新界面
	 */
	private void fillData() {
		mLl_loading.setVisibility(View.VISIBLE);
		new Thread() {

			public void run() {
				// 所有的应用程序信息
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo appInfo : appInfos) {
					if (appInfo.isUserApp()) {
						userAppInfos.add(appInfo);
					} else {
						systemAppInfos.add(appInfo);
					}
				}
				mHandler.sendEmptyMessage(HANDLER_DATA);
			};
		}.start();
	}

	@Override
	protected void parserMessage(Message msg) {
		super.parserMessage(msg);
		switch (msg.what) {
		case HANDLER_DATA:
			setAdpData();
			break;
		}
	}

	/**
	 * 給适配器设置数据
	 */
	private void setAdpData() {
		mLl_loading.setVisibility(View.INVISIBLE);
		if (mAdp_appManager == null) {
			mAdp_appManager = new AppManagerAdapter(this, mDao_appLock, userAppInfos, systemAppInfos);
			mLv_appmanager.setAdapter(mAdp_appManager);
		} else {
			mAdp_appManager.notifyDataSetChanged();
		}
	}

	/**
	 * 获取手机内部存储的可用空间
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getAvailRom() {
		File path = Environment.getDataDirectory();// 手机内部数据存储的目录
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();// 获取每个数据区块的大小
		long availableBlocks = stat.getAvailableBlocks();// 获取还剩多少块的可用空间
		long totalspace = blockSize * availableBlocks;
		return Formatter.formatFileSize(this, totalspace);
	}

	/**
	 * 获取sd卡的可用空间
	 * 
	 * @return
	 */
	private String getAvailSD() {
		File path = Environment.getExternalStorageDirectory(); // 获取了sd卡的目录
		StatFs stat = new StatFs(path.getPath());// 获得当前linux文件系统的状态
		long blockSize = stat.getBlockSize();// 获取每个数据区块的大小
		long availableBlocks = stat.getAvailableBlocks();// 获取还剩多少块的可用空间
		long totalspace = blockSize * availableBlocks; // int 类型数据最大空间2G
		return Formatter.formatFileSize(this, totalspace);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		return false;
	}
}
