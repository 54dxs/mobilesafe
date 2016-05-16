package net.dxs.mobilesafe.ui.adapter;

import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.TaskInfo;
import android.content.Context;
import android.graphics.Color;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 适配器-进程信息
 * 
 * @author lijian-pc
 * @date 2016-5-16 上午10:36:20
 */
public class TaskInfoAdapter extends BaseAdapter {

	private Context mContext;
	private List<TaskInfo> mList_userTaskInfo;
	private List<TaskInfo> mList_systemTaskInfo;

	public TaskInfoAdapter(Context context, List<TaskInfo> list_userTaskInfo,
			List<TaskInfo> list_systemTaskInfo) {
		this.mContext = context;
		this.mList_userTaskInfo = list_userTaskInfo;
		this.mList_systemTaskInfo = list_systemTaskInfo;
	}

	@Override
	public int getCount() {// 返回手机里有多少应用
		if (mList_userTaskInfo != null && mList_systemTaskInfo != null) {
			// 读取sp里面的配置 ，如果显示系统进程
			// 多了一个用户应用的textView和一个系统应用的textView
			return mList_userTaskInfo.size() + 1 + mList_systemTaskInfo.size()
					+ 1;
			// 如果不显示系统进程
			// return mList_userTaskInfo.size() + 1;
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
		TaskInfo info;
		if (position == 0) {// 显示用户程序标签
			TextView view = new TextView(mContext);
			view.setText("用户进程：" + mList_userTaskInfo.size());
			view.setTextColor(Color.WHITE);
			view.setBackgroundColor(Color.GRAY);
			return view;
		} else if (position == (mList_userTaskInfo.size() + 1)) {
			TextView view = new TextView(mContext);
			view.setText("系统进程：" + mList_systemTaskInfo.size());
			view.setTextColor(Color.WHITE);
			view.setBackgroundColor(Color.GRAY);
			return view;
		} else if (position <= mList_userTaskInfo.size()) {// 用户进程条目
			int newposition = position - 1;// 用户程序的标签占用了一个位置
			info = mList_userTaskInfo.get(newposition);
		} else {// 系统进程条目
			int newposition = position - mList_userTaskInfo.size() - 1 - 1;// 减去用户程序的个数,再减去两个TextView
			info = mList_systemTaskInfo.get(newposition);
		}

		ViewHolder viewHolder = null;
		// 1.使用历史缓存的view对象 减少 布局创建的次数
		if (convertView != null && convertView instanceof RelativeLayout) {// 注意这里对TextView的过滤
			viewHolder = (ViewHolder) convertView.getTag();// 从口袋里面取出记事本
		} else {
			convertView = View.inflate(mContext, R.layout.item_taskinfo, null);
			// 2.减少子孩子查询的次数，只是在创建子孩子的时候 获取孩子对象的引用
			viewHolder = getViewHolder(convertView);
			convertView.setTag(viewHolder);// 把记事本放在父亲的口袋里
		}
		// 寻找子孩子的引用比较消耗资源
		setData(viewHolder, info, position);
		return convertView;
	}

	private void setData(ViewHolder v, TaskInfo info, int position) {
		v.iv_appIcon.setImageDrawable(info.getIcon());
		v.tv_appName.setText(info.getName());
		v.tv_memsize.setText("内存占用："
				+ Formatter.formatFileSize(mContext, info.getMemsize()));
		v.cb_status.setChecked(info.isChecked());
		if (info.getPackname() != null
				&& info.getPackname().equals(mContext.getPackageName())) {// 条目的包名和当前应用程序包名一致了。
			v.cb_status.setVisibility(View.INVISIBLE);
		} else {
			v.cb_status.setVisibility(View.VISIBLE);
		}
	}

	private ViewHolder getViewHolder(View v) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.iv_appIcon = (ImageView) v.findViewById(R.id.iv_icon);
		viewHolder.tv_appName = (TextView) v.findViewById(R.id.tv_name);
		viewHolder.tv_memsize = (TextView) v.findViewById(R.id.tv_memsize);
		viewHolder.cb_status = (CheckBox) v.findViewById(R.id.cb_status);
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
		/** 内存占用 */
		public TextView tv_memsize;
		/** 是否选中 */
		public CheckBox cb_status;
	}
}
