package net.dxs.mobilesafe.activities;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.TaskInfo;
import net.dxs.mobilesafe.engine.TaskInfoProvider;
import net.dxs.mobilesafe.ui.adapter.TaskInfoAdapter;
import net.dxs.mobilesafe.ui.adapter.TaskInfoAdapter.ViewHolder;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.SystemInfoUtils;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 进程管理
 * 
 * @author lijian-pc
 * @date 2016-5-16 上午9:53:35
 */
public class TaskManagerActivity extends BaseActivity implements
		OnScrollListener, OnItemClickListener, OnClickListener {

	private static final String TAG = "TaskManagerActivity";

	private static final int HANDLER_DATA = 1000;

	private TextView mTv_running_process_count;
	private TextView mTv_memory_info;
	private LinearLayout mLl_loading;
	private ListView mLv_taskmanager;
	private TextView mTv_status;
	private Button mBtn_selectAll;
	private Button mBtn_unSelectAll;
	private Button mBtn_killAll;
	private Button mBtn_enterSetting;

	/** 正运行的进程数 */
	private int mInt_runningProcessCount;
	/** 剩余内存 */
	private long mLong_availRam;
	/** 总内存 */
	private long mLong_totalRam;

	private List<TaskInfo> taskInfos;
	private ArrayList<TaskInfo> userTaskInfos;
	private ArrayList<TaskInfo> systemTaskInfos;

	private TaskInfoAdapter mAdp_taskInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_manager);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mTv_running_process_count = (TextView) findViewById(R.id.tv_running_process_count);
		mTv_memory_info = (TextView) findViewById(R.id.tv_memory_info);
		mLl_loading = (LinearLayout) findViewById(R.id.ll_loading);
		mLv_taskmanager = (ListView) findViewById(R.id.lv_taskmanager);
		mTv_status = (TextView) findViewById(R.id.tv_status);
		mBtn_selectAll = (Button) findViewById(R.id.btn_taskManager_selectAll);
		mBtn_unSelectAll = (Button) findViewById(R.id.btn_taskManager_unSelectAll);
		mBtn_killAll = (Button) findViewById(R.id.btn_taskManager_killAll);
		mBtn_enterSetting = (Button) findViewById(R.id.btn_taskManager_enterSetting);
	}

	private void initData() {
		mLv_taskmanager.setOnScrollListener(this);
		mLv_taskmanager.setOnItemClickListener(this);
		mBtn_selectAll.setOnClickListener(this);
		mBtn_unSelectAll.setOnClickListener(this);
		mBtn_killAll.setOnClickListener(this);
		mBtn_enterSetting.setOnClickListener(this);

		mInt_runningProcessCount = SystemInfoUtils.getRunningProcessCount(this);
		mLong_availRam = SystemInfoUtils.getAvailRAM(this);
		mLong_totalRam = SystemInfoUtils.getTotalRAM(this);
		mTv_running_process_count.setText("运行中进程：" + mInt_runningProcessCount);
		mTv_memory_info.setText("剩余/总内存："
				+ Formatter.formatFileSize(this, mLong_availRam) + "/"
				+ Formatter.formatFileSize(this, mLong_totalRam));
		fillData();
	}

	private void fillData() {
		mLl_loading.setVisibility(View.VISIBLE);
		new Thread() {

			public void run() {
				taskInfos = TaskInfoProvider
						.getTaskInfos(TaskManagerActivity.this);// 获取手机进程是一个耗时操作
				userTaskInfos = new ArrayList<TaskInfo>();
				systemTaskInfos = new ArrayList<TaskInfo>();
				for (TaskInfo taskInfo : taskInfos) {
					if (taskInfo.isUserTask()) {
						userTaskInfos.add(taskInfo);
					} else {
						systemTaskInfos.add(taskInfo);
					}
				}
				mHandler.sendEmptyMessage(HANDLER_DATA);
			}
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
		if (mAdp_taskInfo == null) {
			mAdp_taskInfo = new TaskInfoAdapter(this, userTaskInfos,
					systemTaskInfos);
			mLv_taskmanager.setAdapter(mAdp_taskInfo);
		} else {
			mAdp_taskInfo.notifyDataSetChanged();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	// 当ListView滚动的时候调用的方法
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (userTaskInfos != null && systemTaskInfos != null) {
			if (firstVisibleItem > userTaskInfos.size()) {
				// 当系统程序标签被拖上来的时候
				mTv_status.setText("系统进程：" + systemTaskInfos.size());
			} else {
				mTv_status.setText("用户进程：" + userTaskInfos.size());
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		TaskInfo taskInfo = null;
		ViewHolder holder = (ViewHolder) view.getTag();
		if (position == 0) {
			// 用户进程数量
			return;
		} else if (position == (userTaskInfos.size() + 1)) {
			// 系统进程数量标签
			return;
		} else if (position <= userTaskInfos.size()) {// 用户进程条目
			taskInfo = userTaskInfos.get(position - 1);
		} else {// 系统进程条目
			taskInfo = systemTaskInfos.get(position - 1 - userTaskInfos.size()
					- 1);
		}
		L.i(TAG, "当前被点击的条目：" + taskInfo.toString());
		if (taskInfo.getPackname().equals(getPackageName())) {// 条目的包名和当前应用程序包名一致了。
			return;
		}
		if (taskInfo.isChecked()) {
			taskInfo.setChecked(false);
			holder.cb_status.setChecked(false);
		} else {
			taskInfo.setChecked(true);
			holder.cb_status.setChecked(true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_taskManager_selectAll:// 全选
			selectAll();
			break;
		case R.id.btn_taskManager_unSelectAll:// 取消
			unSelectAll();
			break;
		case R.id.btn_taskManager_killAll:// 清理
			killAll();
			break;
		case R.id.btn_taskManager_enterSetting:// 设置
			enterSetting();
			break;
		}
	}

	/**
	 * 选择全部条目
	 */
	private void selectAll() {
		for (TaskInfo info : userTaskInfos) {
			if (info.getPackname().equals(getPackageName())) {// 条目的包名和当前应用程序包名一致了。
				continue;
			}
			info.setChecked(true);
		}
		for (TaskInfo info : systemTaskInfos) {
			info.setChecked(true);
		}
		mAdp_taskInfo.notifyDataSetChanged();
	}

	/**
	 * 取消选择全部条目
	 */
	private void unSelectAll() {
		for (TaskInfo info : userTaskInfos) {
			info.setChecked(false);
		}
		for (TaskInfo info : systemTaskInfos) {
			info.setChecked(false);
		}
		mAdp_taskInfo.notifyDataSetChanged();
	}

	/**
	 * 杀死全部选中的进程
	 */
	private void killAll() {
		long savedMem = 0;// 释放的内存空间
		int count = 0;// 杀死的进程数量
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// 存放那些被杀死的进程信息
		List<TaskInfo> killedTaskInfos = new ArrayList<TaskInfo>();
		for (TaskInfo info : userTaskInfos) {
			if (info.isChecked()) {
				am.killBackgroundProcesses(info.getPackname());// 杀死进程
				count++;
				savedMem += info.getMemsize();
				killedTaskInfos.add(info);
			}
		}
		for (TaskInfo info : systemTaskInfos) {// 遍历集合的时候 是不可以修改集合的大小的。
			if (info.isChecked()) {
				am.killBackgroundProcesses(info.getPackname());
				count++;
				savedMem += info.getMemsize();
				killedTaskInfos.add(info);
			}
		}
		if (killedTaskInfos.size() == 0) {
			AppToast.getInstance().show("请先勾选要清理的应用");
			return;
		}
		for (TaskInfo info : killedTaskInfos) {
			if (info.isUserTask()) {
				userTaskInfos.remove(info);
			} else {
				systemTaskInfos.remove(info);
			}
		}
		// fillData();//
		// 更新ui(不能直接更新ui,因为有些系统应用是没法杀死的,但是我们又要在界面上显示其被杀死了,那么这个时候我们就要在视觉上欺骗用户,将其应用信息在界面上进行移除)
		AppToast.getInstance().show(
				"清理了" + count + "个进程，释放了"
						+ Formatter.formatFileSize(this, savedMem) + "的空间");
		mAdp_taskInfo.notifyDataSetChanged();
		mLong_availRam += savedMem;
		mInt_runningProcessCount -= count;
		mTv_running_process_count.setText("运行中进程：" + mInt_runningProcessCount);
		mTv_memory_info.setText("可用/总内存："
				+ Formatter.formatFileSize(this, mLong_availRam) + "/"
				+ Formatter.formatFileSize(this, mLong_totalRam));
	}

	/**
	 * 进入程序管理器的设置界面
	 */
	private void enterSetting() {
		// TODO Auto-generated method stub

	}
}
