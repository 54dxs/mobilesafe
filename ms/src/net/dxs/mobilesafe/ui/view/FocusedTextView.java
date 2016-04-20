package net.dxs.mobilesafe.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

/**
 * 自定义跑马灯的TextView控件
 * 
 * @author lijian
 * @date 2016-4-20 上午11:32:24
 */
public class FocusedTextView extends TextView {

	public FocusedTextView(Context context) {
		super(context);
	}

	public FocusedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FocusedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * “欺骗”系统，让控件永远获得焦点
	 */
	@Override
	@ExportedProperty(category = "focus")
	public boolean isFocused() {
		return true;
	}

}
