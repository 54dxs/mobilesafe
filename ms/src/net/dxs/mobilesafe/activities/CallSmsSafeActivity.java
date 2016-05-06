package net.dxs.mobilesafe.activities;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.db.dao.BlacknumberDao;
import net.dxs.mobilesafe.domain.BlackNumber;
import net.dxs.mobilesafe.ui.adapter.BlackNumberAdapter;
import net.dxs.mobilesafe.ui.adapter.BlackNumberAdapter.IDeleteListener;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import net.dxs.mobilesafe.utils.validate.RegexUtils;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 通讯卫士
 * 
 * @author lijian-pc
 * @date 2016-5-5 下午3:43:11
 */
public class CallSmsSafeActivity extends BaseActivity implements
		OnClickListener, OnItemLongClickListener, IDeleteListener {
	protected static final String TAG = "CallSmsSafeActivity";

	protected static final int HANDLER_DATA_FULL = 1000;
	protected static final int HANDLER_DATA_EMPTY = 1001;
	/** 添加黑名单 */
	private Button mBtn_addBlackNumber;
	/** 黑名单列表 */
	private ListView mLv_blackNumber;
	/** 加载中 */
	private LinearLayout mLl_loading;

	/** 黑名单数据库操作 */
	private BlacknumberDao mDao_blackNumber;

	/** 数据加载中标记 */
	private boolean isloading;

	/** 单次查询数据库要返回的数据量 */
	private int maxNumber = 20;
	/** 查询数据库的起始位置 */
	private int startIndex = 0;

	/** 黑名单数据集合 */
	private List<BlackNumber> mList_blackNumber;

	/** 黑名单适配器 */
	BlackNumberAdapter mAdp_blackNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_callsms_safe);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mBtn_addBlackNumber = (Button) findViewById(R.id.btn_callSmsSafe_addBlackNumber);
		mLv_blackNumber = (ListView) findViewById(R.id.lv_callSmsSafe_blackNumber);
		mLl_loading = (LinearLayout) findViewById(R.id.ll_callSmsSafe_loading);
	}

	private void initData() {
		mBtn_addBlackNumber.setOnClickListener(this);
		mLv_blackNumber.setOnScrollListener(mScrollListener);
		mLv_blackNumber.setOnItemLongClickListener(this);
		mDao_blackNumber = new BlacknumberDao(this);
		mList_blackNumber = new ArrayList<BlackNumber>();
		queryBlackNumber();
	}

	/**
	 * 查询数据库获取黑名单列表
	 */
	private void queryBlackNumber() {
		isloading = true;
		mLl_loading.setVisibility(View.VISIBLE);
		new Thread() {

			public void run() {
				List<BlackNumber> list = mDao_blackNumber.findPart(maxNumber,
						startIndex);
				if (list.size() != 0) {
					mList_blackNumber.addAll(list);
					mHandler.sendEmptyMessage(HANDLER_DATA_FULL);
				} else {
					mHandler.sendEmptyMessage(HANDLER_DATA_EMPTY);
				}
			};
		}.start();
	}

	@Override
	protected void parserMessage(Message msg) {
		super.parserMessage(msg);
		switch (msg.what) {
		case HANDLER_DATA_EMPTY:
			queryBlackNumberDataEmpty();
			break;

		case HANDLER_DATA_FULL:
			queryBlackNumberDataFull();
			break;

		default:
			break;
		}
	}

	private void queryBlackNumberDataFull() {
		mLl_loading.setVisibility(View.INVISIBLE);
		if (mAdp_blackNumber == null) {
			mAdp_blackNumber = new BlackNumberAdapter(this,
					mList_blackNumber, this);
			mLv_blackNumber.setAdapter(mAdp_blackNumber);
		} else {
			mAdp_blackNumber.notifyDataSetChanged();// 数据适配器已经存在，刷新界面
		}
		isloading = false;
	}

	private void queryBlackNumberDataEmpty() {
		mLl_loading.setVisibility(View.INVISIBLE);
		AppToast.getInstance().show("哦买噶,所有数据都被你取完啦...");
		isloading = false;
	}

	OnScrollListener mScrollListener = new OnScrollListener() {

		// 当滚动状态发生变化的时候调用的方法。
		// 静止--》拖动滚动
		// 拖动--》惯性滑动
		// 滑动--》静止
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:// 静止状态
				int position = mLv_blackNumber.getLastVisiblePosition();// 获取最后一个可见条目在listview集合里面位置。
				L.i(TAG, "最后一个可见条目的位置:" + position);
				if (position + 1 == mList_blackNumber.size()) {
					if (isloading) {
						AppToast.getInstance().show("正在加载,请稍后...");
						return;
					}
					startIndex += maxNumber;
					queryBlackNumber();
				}
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸滚动状态
				L.i(TAG, "触摸滚动状态");
				break;
			case OnScrollListener.SCROLL_STATE_FLING:// 惯性滚动状态
				L.i(TAG, "惯性滚动状态");
				break;
			}
		}

		// 当listview滚动的时候调用的方法。
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_callSmsSafe_addBlackNumber:
			addBlackNumber();
			break;

		default:
			break;
		}
	}

	EditText et_blacknumber;
	CheckBox cb_phone;
	CheckBox cb_sms;
	Button btn_ok;
	Button btn_cancle;

	/**
	 * 添加一条黑名单号码
	 */
	private void addBlackNumber() {
		AlertDialog.Builder builder = new Builder(this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(this, R.layout.dialog_add_blacknumber, null);
		dialog.setView(view);
		dialog.show();
		et_blacknumber = (EditText) view
				.findViewById(R.id.et_addBlackNumber_blacknumber);
		cb_phone = (CheckBox) view.findViewById(R.id.cb_addBlackNumber_phone);
		cb_sms = (CheckBox) view.findViewById(R.id.cb_addBlackNumber_sms);
		btn_ok = (Button) view.findViewById(R.id.btn_addBlackNumber_ok);
		btn_cancle = (Button) view.findViewById(R.id.btn_addBlackNumber_cancle);
		btn_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String number = et_blacknumber.getText().toString().trim();
				if (TextUtils.isEmpty(number)) {
					AppToast.getInstance().show("你忘记填写号码了吧...");
					return;
				}
				if (!RegexUtils.isMobileNO(number)) {
					AppToast.getInstance().show("您输入的手机号格式不正确...");
					return;
				}
				String mode = null;
				if (cb_phone.isChecked() && cb_sms.isChecked()) {
					mode = Constants.SAFE_MODE_ALL;
				} else if (cb_phone.isChecked()) {
					mode = Constants.SAFE_MODE_PHONE;
				} else if (cb_sms.isChecked()) {
					mode = Constants.SAFE_MODE_SMS;
				}
				if (TextUtils.isEmpty(mode)) {
					AppToast.getInstance().show("你忘记选择拦截模式了吧...");
					return;
				}
				// 将数据更新到数据库
				mDao_blackNumber.add(number, mode);
				// 更新界面
				BlackNumber blackNumber = new BlackNumber();
				blackNumber.setNumber(number);
				blackNumber.setMode(mode);
				mList_blackNumber.add(0, blackNumber);
				mAdp_blackNumber.notifyDataSetChanged();
				dialog.dismiss();
			}
		});
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		L.i(TAG, "哈哈，我被长点击了。。。position:" + position + "-id:" + id);
		updateBlackNumber(position);
		return false;
	}

	/**
	 * 修改一条黑名单号码
	 * 
	 * @param v
	 */
	public void updateBlackNumber(int position) {
		AlertDialog.Builder builder = new Builder(this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(this, R.layout.dialog_add_blacknumber, null);
		dialog.setView(view);
		dialog.show();
		TextView title = (TextView) view
				.findViewById(R.id.tv_addBlackNumber_title);
		title.setText("修改黑名单");
		et_blacknumber = (EditText) view
				.findViewById(R.id.et_addBlackNumber_blacknumber);
		cb_phone = (CheckBox) view.findViewById(R.id.cb_addBlackNumber_phone);
		cb_sms = (CheckBox) view.findViewById(R.id.cb_addBlackNumber_sms);
		btn_ok = (Button) view.findViewById(R.id.btn_addBlackNumber_ok);
		btn_cancle = (Button) view.findViewById(R.id.btn_addBlackNumber_cancle);

		// 对数据进行回显
		final BlackNumber blackNumber = mList_blackNumber.get(position);
		final String number = blackNumber.getNumber();
		String mode = blackNumber.getMode();
		et_blacknumber.setText(number);
		et_blacknumber.setEnabled(false);

		if (mode.equals(Constants.SAFE_MODE_ALL)) {
			L.i(TAG, "mode:" + mode);
			cb_phone.setChecked(true);
			cb_sms.setChecked(true);
		} else if (mode.equals(Constants.SAFE_MODE_PHONE)) {
			L.i(TAG, "mode:" + mode);
			cb_phone.setChecked(true);
		} else if (mode.equals(Constants.SAFE_MODE_SMS)) {
			L.i(TAG, "mode:" + mode);
			cb_sms.setChecked(true);
		}

		btn_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String newmode = null;
				if (cb_phone.isChecked() && cb_sms.isChecked()) {
					newmode = "0";
				} else if (cb_phone.isChecked()) {
					newmode = "1";
				} else if (cb_sms.isChecked()) {
					newmode = "2";
				}
				if (TextUtils.isEmpty(newmode)) {
					AppToast.getInstance().show("你忘记选择拦截模式了吧...");
					return;
				}
				// 将数据更新到数据库
				mDao_blackNumber.update(number, newmode);
				// 更新界面
				blackNumber.setNumber(number);
				blackNumber.setMode(newmode);
				mAdp_blackNumber.notifyDataSetChanged();
				dialog.dismiss();
			}
		});
	}

	@Override
	public void onDelete(View v) {
		final int position = (Integer) v.getTag();
		AlertDialog.Builder builder = new Builder(CallSmsSafeActivity.this);
		builder.setTitle("警告");
		builder.setMessage("你确定要将\""
				+ mList_blackNumber.get(position).getNumber() + "\"移除黑名单吗?");
		builder.setNegativeButton("取消", null);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDao_blackNumber.delete(mList_blackNumber.get(position)
						.getNumber());// 从数据库把条目删除
				mList_blackNumber.remove(mList_blackNumber.get(position));
				mAdp_blackNumber.notifyDataSetChanged();
			}
		});
		builder.show();
	}
}
