package net.dxs.mobilesafe.ui.view;

import net.dxs.mobilesafe.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 自定义-设置中心的条目
 * 
 * @author lijian-pc
 * @date 2016-5-9 上午10:28:40
 */
public class SettingView extends RelativeLayout {

	private Context mContext;
	private TextView mTv_title;
	private TextView mTv_desc;
	private CheckBox mCb_state;
	private String mStr_title;
	private String mStr_desc_on;
	private String mStr_desc_off;

	public SettingView(Context context) {
		super(context);
		this.mContext = context;
	}

	public SettingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;

		/***方式一***********************************************************************/
//		mStr_title = attrs.getAttributeValue(
//				"http://schemas.android.com/apk/res/net.dxs.mobilesafe",
//				"title");
//		mStr_desc_on = attrs.getAttributeValue(
//				"http://schemas.android.com/apk/res/net.dxs.mobilesafe",
//				"desc_on");
//		mStr_desc_off = attrs.getAttributeValue(
//				"http://schemas.android.com/apk/res/net.dxs.mobilesafe",
//				"desc_off");

		/***方式二***********************************************************************/
		TypedArray _typeArray = context.obtainStyledAttributes(attrs,
				R.styleable.SettingView);
		mStr_title = _typeArray.getString(R.styleable.SettingView_title);
		mStr_desc_on = _typeArray.getString(R.styleable.SettingView_desc_on);
		mStr_desc_off = _typeArray.getString(R.styleable.SettingView_desc_off);
		_typeArray.recycle();
		
		/**************************************************************************/

		init();
	}

	public SettingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	private void init() {
		View.inflate(mContext, R.layout.ui_setting_view, this);
		mTv_title = (TextView) this.findViewById(R.id.tv_title);
		mTv_desc = (TextView) this.findViewById(R.id.tv_desc);
		mCb_state = (CheckBox) this.findViewById(R.id.cb_state);
		mTv_title.setText(mStr_title);
		mTv_desc.setText(mStr_desc_off);
	}

	/**
	 * 判断组合控件是否被点击
	 * 
	 * @return
	 */
	public boolean isChecked() {
		return mCb_state.isChecked();
	}

	/**
	 * 设置组合控件的勾选状态
	 * 
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		mCb_state.setChecked(checked);
		if (checked) {
			mTv_desc.setText(mStr_desc_on);
			mTv_desc.setTextColor(0x66000000);
		} else {
			mTv_desc.setText(mStr_desc_off);
			mTv_desc.setTextColor(Color.RED);
		}
	}
}
