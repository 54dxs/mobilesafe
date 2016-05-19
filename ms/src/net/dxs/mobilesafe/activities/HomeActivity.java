package net.dxs.mobilesafe.activities;

import java.util.ArrayList;
import java.util.List;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.app.App;
import net.dxs.mobilesafe.domain.FunctionEntry;
import net.dxs.mobilesafe.observer.SmsObserver;
import net.dxs.mobilesafe.ui.adapter.HomeAdapter;
import net.dxs.mobilesafe.utils.L;
import net.dxs.mobilesafe.utils.Md5Utils;
import net.dxs.mobilesafe.utils.SpUtil;
import net.dxs.mobilesafe.utils.WindowUtils;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

/**
 * 程序主界面
 * 
 * @author lijian
 * @date 2016-4-8 下午5:51:47
 */
public class HomeActivity extends BaseActivity implements OnItemClickListener {

	private static final String TAG = "HomeActivity";
	/** 手机短信内容提供者的Uri */
	private static final Uri URI_SMS = Uri.parse("content://sms");

	private static final String[] names = { "手机防盗", "通讯卫士", "软件管理", "进程管理",
			"流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心" };
	private static final int[] icons = { R.drawable.safe,
			R.drawable.callmsgsafe_selector, R.drawable.app,
			R.drawable.taskmanager, R.drawable.netmanager, R.drawable.trojan,
			R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings };

	private GridView mGr_functionEntry;

	private EditText mEt_pwd;
	private EditText mEt_pwd_confirm;
	private Button mBtn_ok;
	private Button mBtn_cancle;
	private AlertDialog mAlertDialog;
	private View view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mGr_functionEntry = (GridView) findViewById(R.id.gv_home_functionentry);
	}

	private void initData() {
		List<FunctionEntry> list = new ArrayList<FunctionEntry>();

		for (int i = 0; i < names.length; i++) {
			FunctionEntry entry = new FunctionEntry(names[i], icons[i]);
			list.add(entry);
		}

		mGr_functionEntry.setAdapter(new HomeAdapter(this, list));
		mGr_functionEntry.setOnItemClickListener(this);

		// 注册一个监听短信的ContentObserver
		getContentResolver().registerContentObserver(URI_SMS, true,
				new SmsObserver(this, mHandler));
		// 初始化窗体管理器
		mWm = (WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0:// 手机防盗
			checkPwd();
			break;

		case 1:// 通讯卫士
			toActivity(CallSmsSafeActivity.class);
			break;

		case 2:// 软件管理
			toActivity(AppManagerActivity.class);
			break;

		case 3:// 进程管理
			toActivity(TaskManagerActivity.class);
			break;

		case 4:// 流量统计
			break;

		case 5:// 手机杀毒
			break;

		case 6:// 缓存清理
			break;

		case 7:// 高级工具
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					showMyToast("高级工具");
//					WindowUtils.showPopupWindow(HomeActivity.this);
				}
			}, 1000 * 3);
			break;

		case 8:// 设置中心
			toActivity(SettinCenterActivity.class);
			break;
		}
	}

	/**
	 * 进入到指定页面
	 * 
	 * @param clazz
	 */
	private void toActivity(Class<?> clazz) {
		Intent intent;
		intent = new Intent(HomeActivity.this, clazz);
		startActivity(intent);
	}

	/**
	 * 检测用户是否设置过密码
	 */
	private void checkPwd() {
		String strPwd = SpUtil.getInstance().getString(
				Constants.LOSTFIND_PSSWORD);
		if (TextUtils.isEmpty(strPwd)) {
			// 没有设置过密码
			L.i(TAG, "没有设置过密码,弹出设置密码对话框");
			showSetupPasswordDialog();
		} else {
			// 设置过密码
			L.i(TAG, "设置过密码,弹出输入密码对话框");
			showEnterPasswordDialog();
		}
	}

	/**
	 * 设置密码对话框
	 */
	private void showSetupPasswordDialog() {
		// 自定义布局的对话框
		AlertDialog.Builder builder = new Builder(this);

		View view = View.inflate(this, R.layout.dialog_setup_pwd, null);
		mEt_pwd = (EditText) view.findViewById(R.id.et_setup_password);
		mEt_pwd_confirm = (EditText) view
				.findViewById(R.id.et_setup_password_confirm);
		mBtn_ok = (Button) view.findViewById(R.id.btn_setup_ok);
		mBtn_cancle = (Button) view.findViewById(R.id.btn_setup_cancle);
		mBtn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String strPwd = mEt_pwd.getText().toString().trim();
				String strPwdConfirm = mEt_pwd_confirm.getText().toString()
						.trim();
				if (TextUtils.isEmpty(strPwd)
						|| TextUtils.isEmpty(strPwdConfirm)) {
					AppToast.getInstance().show("对不起,密码输入不能为空!");
					return;
				}
				if (!strPwd.equals(strPwdConfirm)) {
					AppToast.getInstance().show("两次密码输入不一致");
					return;
				}
				SpUtil.getInstance().saveString(Constants.LOSTFIND_PSSWORD,
						Md5Utils.encode(strPwd));
				mAlertDialog.dismiss();
			}
		});
		mBtn_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAlertDialog.dismiss();
			}
		});

		builder.setView(view);
		mAlertDialog = builder.create();
		mAlertDialog.show();
	}

	/**
	 * 输入密码对话框
	 */
	private void showEnterPasswordDialog() {
		// 自定义布局的对话框
		AlertDialog.Builder builder = new Builder(this);

		View view = View.inflate(this, R.layout.dialog_enter_pwd, null);
		mEt_pwd = (EditText) view.findViewById(R.id.et_enter_password);
		mBtn_ok = (Button) view.findViewById(R.id.btn_enter_ok);
		mBtn_cancle = (Button) view.findViewById(R.id.btn_enter_cancle);
		mBtn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 用户输入的密码
				String strPwd = mEt_pwd.getText().toString().trim();
				if (TextUtils.isEmpty(strPwd)) {
					AppToast.getInstance().show("亲,密码不能为空哦!");
					return;
				}
				// 得到本地保存的密码加密后的密文
				String strLocalPWD = SpUtil.getInstance().getString(
						Constants.LOSTFIND_PSSWORD);
				if (Md5Utils.encode(strPwd).equals(strLocalPWD)) {
					// 密码正确进入手机防盗界面
					mAlertDialog.dismiss();
					Intent intent = new Intent(HomeActivity.this,
							LostFindActivity.class);
					startActivity(intent);
				} else {
					AppToast.getInstance().show("密码错误!");
				}
			}
		});
		mBtn_cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAlertDialog.dismiss();
			}
		});

		builder.setView(view);
		mAlertDialog = builder.create();
		mAlertDialog.show();
	}

	long[] mHits = new long[2];
	private WindowManager.LayoutParams params;
	/** 系统窗体的管理器 */
	private WindowManager mWm;
	/**
	 * 显示自定义吐司
	 * 
	 * @param address
	 *            电话号码的归属地
	 */
	public void showMyToast(String address) {
		view = View.inflate(this, R.layout.toast_address, null);
		// 给view对象注册一个双击事件
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);// 数组向左移位操作
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();// uptimeMillis()手机的开机时间
				if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
					params.x = (mWm.getDefaultDisplay().getWidth() - view
							.getWidth()) / 2;
					params.y = (mWm.getDefaultDisplay().getHeight() - view
							.getHeight()) / 2;
					mWm.updateViewLayout(view, params);
					SpUtil.getInstance().saveInt("paramsx", params.x);
					SpUtil.getInstance().saveInt("paramsy", params.y);
				}
			}
		});

		// 给view对象组成触摸的监听器
		view.setOnTouchListener(new OnTouchListener() {

			int startX;
			int startY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 手指按下
					L.i(TAG, "手指按下");
					event.getRawX();
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					L.i(TAG, "oldx:" + startX);
					L.i(TAG, "oldy:" + startY);

					break;
				case MotionEvent.ACTION_MOVE:// 手指移动
					L.i(TAG, "手指移动");
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					L.i(TAG, "newX ： " + newX);
					L.i(TAG, "newY ： " + newY);

					int dx = newX - startX;
					int dy = newY - startY;
					L.i(TAG, "手指水平方向偏移量dx ： " + dx);
					L.i(TAG, "手指竖直方向偏移量dy ： " + dy);

					// 立刻让控件也跟随着手指移动 dx dy。
					params.x += dx;
					params.y += dy;

					// 超出边界修正
					if (params.x < 0) {
						params.x = 0;
					}
					if (params.y < 0) {
						params.y = 0;
					}
					if (params.x > (mWm.getDefaultDisplay().getWidth() - view
							.getWidth())) {
						params.x = mWm.getDefaultDisplay().getWidth()
								- view.getWidth();
					}
					if (params.y > (mWm.getDefaultDisplay().getHeight() - view
							.getHeight())) {
						params.y = mWm.getDefaultDisplay().getHeight()
								- view.getHeight();
					}

					mWm.updateViewLayout(view, params);

					// 重复第一步的操作 ，重新初始化手指的开始位置。
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();

					break;
				case MotionEvent.ACTION_UP:// 手指抬起
					L.i(TAG, "手指抬起");
					SpUtil.getInstance().saveInt("paramsx", params.x);
					SpUtil.getInstance().saveInt("paramsy", params.y);
					break;

				}

				return false;// True if the listener has consumed the event,
								// false otherwise.
								// true 代表监听器 处理掉了这个事件，false监听器没有处理这个事件。
			}
		});

		TextView tv_toast_address = (TextView) view
				.findViewById(R.id.tv_toast_address);
		tv_toast_address.setText(address);

		// 土司显示的参数
		params = new WindowManager.LayoutParams();

		// 对齐方式
		params.gravity = Gravity.LEFT + Gravity.TOP;

		// 指定距离屏幕左边的距离 必须与 Gravity.LEFT同时使用
		params.x = SpUtil.getInstance().getInt("paramsx", 0);
		// 指定距离屏幕上边的距离 必须与 Gravity.TOP同时使用
		params.y = SpUtil.getInstance().getInt("paramsy", 0);

		// 土司的宽高
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;

		// 土司的参数 不可获取焦点 不可以别点击 保存屏幕常亮
		params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		// | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		// 半透明窗体
		params.format = PixelFormat.TRANSLUCENT;
		// 吐司显示动画
		// params.windowAnimations = R.anim.toast_show;

		// 改用电话优先级的窗体类型，这种类型可以响应触摸事件。
		params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
		mWm.addView(view, params);
	}
}
