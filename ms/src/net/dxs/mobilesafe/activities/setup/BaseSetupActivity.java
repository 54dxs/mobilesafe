package net.dxs.mobilesafe.activities.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import net.dxs.mobilesafe.activities.BaseActivity;
import net.dxs.mobilesafe.utils.AppUtil.AppToast;

/**
 * 设置向导基类
 * 
 * @author lijian-pc
 * @date 2016-4-26 上午11:04:48
 */
public abstract class BaseSetupActivity extends BaseActivity {

	/**
	 * 父类里面声明一个手势识别器
	 */
	protected GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 2.初始化手势识别器 第二个参数就是我们注册的手势识别器的监听器
		mGestureDetector = new GestureDetector(this, new MyGestureListener());
	}

	/**
	 * 由子类实现的显示下一个界面
	 */
	protected abstract void showNext();

	/**
	 * 由子类实现的显示上一个界面
	 */
	protected abstract void showPre();

	private class MyGestureListener extends SimpleOnGestureListener {

		// 当用户在屏幕上手指乱动(滑动)的时候调用的方法
		// e1手指接触到屏幕对应的动作
		// e2手指离开屏幕的一瞬间对应的动作
		// velocityX 水平方向的速度 px/m
		// velocityY 竖直方向的速度
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(e1.getRawY() - e2.getRawY()) > 100) {
				AppToast.getInstance().show("不带这么滑的!");
				return true;
			}

			if (e1.getRawX() - e2.getRawX() > 200) {
				// 显示下一个界面
				// 父类不知道显示下一个界面的代码怎么写
				showNext();
				return true;
			}
			if (e2.getRawX() - e1.getRawX() > 200) {
				// 显示上一个界面
				// 父类不知道显示上一个界面的代码怎么写
				showPre();
				return true;
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}

	/**
	 * 加载显示的一个Activity
	 * 
	 * @param cls
	 *            要显示的activity的字节码
	 */
	public void loadActivity(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 3.用我们的手势识别器处理事件
		mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
}
