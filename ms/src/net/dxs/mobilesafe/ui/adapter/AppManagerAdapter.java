package net.dxs.mobilesafe.ui.adapter;

import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.db.dao.ApplockDao;
import net.dxs.mobilesafe.domain.AppInfo;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 适配器-软件管理
 * 
 * @author lijian-pc
 * @date 2016-5-9 下午5:16:02
 */
public class AppManagerAdapter extends BaseAdapter {

	private Context mContext;
	private ApplockDao mDao_applock;
	private List<AppInfo> mList_userAppInfo;
	private List<AppInfo> mList_systemAppInfo;

	public AppManagerAdapter(Context context, ApplockDao dao,
			List<AppInfo> list_userAppInfo, List<AppInfo> list_systemAppInfo) {
		this.mContext = context;
		this.mDao_applock = dao;
		this.mList_userAppInfo = list_userAppInfo;
		this.mList_systemAppInfo = list_systemAppInfo;
	}

	@Override
	public int getCount() {// 返回手机里有多少应用
		if (mList_userAppInfo != null && mList_systemAppInfo != null) {
			// 多了一个用户应用的textView和一个系统应用的textView
			return mList_userAppInfo.size() + 1 + mList_systemAppInfo.size()
					+ 1;
		}
		return 2;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AppInfo info;
		if (position == 0) {// 显示用户程序标签
			TextView view = new TextView(mContext);
			view.setText("用户程序:" + mList_userAppInfo.size());
			view.setTextColor(Color.WHITE);
			view.setBackgroundColor(Color.GRAY);
			return view;
		} else if (position == (mList_userAppInfo.size() + 1)) {
			TextView view = new TextView(mContext);
			view.setText("系统程序:" + mList_systemAppInfo.size());
			view.setTextColor(Color.WHITE);
			view.setBackgroundColor(Color.GRAY);
			return view;
		} else if (position <= mList_userAppInfo.size()) {// 用户程序
			int newposition = position - 1;// 用户程序的标签占用了一个位置
			info = mList_userAppInfo.get(newposition);
		} else {// 系统程序
			int newposition = position - mList_userAppInfo.size() - 1 - 1;// 减去用户程序的个数,再减去两个TextView
			info = mList_systemAppInfo.get(newposition);
		}

		ViewHolder viewHolder = null;
		// 1.使用历史缓存的view对象 减少 布局创建的次数
		if (convertView != null && convertView instanceof RelativeLayout) {// 注意这里对TextView的过滤
			viewHolder = (ViewHolder) convertView.getTag();// 从口袋里面取出记事本
		} else {
			convertView = View.inflate(mContext, R.layout.item_appinfo, null);
			// 2.减少子孩子查询的次数，只是在创建子孩子的时候 获取孩子对象的引用
			viewHolder = getViewHolder(convertView);
			convertView.setTag(viewHolder);// 把记事本放在父亲的口袋里
		}
		// 寻找子孩子的引用比较消耗资源
		setData(viewHolder, info, position);
		return convertView;
	}

	private void setData(ViewHolder v, AppInfo info, int position) {
		v.iv_appIcon.setImageDrawable(info.getAppIcon());
		v.tv_appName.setText(info.getAppName());
		v.tv_version.setText(info.getVersion());
		if (info.isInRom()) {
			v.tv_inRom.setText("手机内存");
		} else {
			v.tv_inRom.setText("SD卡");
		}

		// 根据数据库的状态，更新条目的图片
		if (mDao_applock.find(info.getPackName())) {
			v.iv_status.setImageResource(R.drawable.lock);
		} else {
			v.iv_status.setImageResource(R.drawable.unlock);
		}
	}

	private ViewHolder getViewHolder(View v) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.iv_appIcon = (ImageView) v.findViewById(R.id.iv_appIcon);
		viewHolder.tv_appName = (TextView) v.findViewById(R.id.tv_appName);
		viewHolder.tv_inRom = (TextView) v.findViewById(R.id.tv_inRom);
		viewHolder.tv_version = (TextView) v.findViewById(R.id.tv_version);
		viewHolder.iv_status = (ImageView) v.findViewById(R.id.iv_status);
		return viewHolder;
	}

	/**
	 * 定义view对象的容器，记事本用来保存孩子控件的引用。
	 * 
	 * @author lijian-pc
	 * @date 2016-5-9 下午5:53:06
	 */
	public static class ViewHolder {
		/** app图标 */
		public ImageView iv_appIcon;
		/** app名称 */
		public TextView tv_appName;
		/** 是否为系统程序 */
		public TextView tv_inRom;
		/** 版本 */
		public TextView tv_version;
		/** 是否开启程序锁 */
		public ImageView iv_status;
	}
}
