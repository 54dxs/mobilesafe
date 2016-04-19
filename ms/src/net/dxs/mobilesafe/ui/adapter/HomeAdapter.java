package net.dxs.mobilesafe.ui.adapter;

import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.FunctionEntry;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 主界面功能图标适配器
 * 
 * @author lijian-pc
 * @date 2016-4-19 下午3:56:15
 */
public class HomeAdapter extends BaseAdapter {

	private Context mContext;
	private List<FunctionEntry> mList;

	public HomeAdapter(Context context, List<FunctionEntry> list) {
		this.mContext = context;
		this.mList = list;
	}

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mList != null) {
			return mList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mList != null) {
			return position;
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FunctionEntry entry = mList.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_home, null);
			viewHolder = getViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		setData(viewHolder, entry);
		return convertView;
	}

	private void setData(ViewHolder v, FunctionEntry entry) {
		v.mIv_icon.setImageResource(entry.getIcon());
		v.mTv_name.setText(entry.getName());
	}

	private ViewHolder getViewHolder(View v) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.mIv_icon = (ImageView) v.findViewById(R.id.iv_home_icon);
		viewHolder.mTv_name = (TextView) v.findViewById(R.id.tv_home_name);
		return viewHolder;
	}

	static class ViewHolder {
		/** 图标 */
		ImageView mIv_icon;
		/** 名称 */
		TextView mTv_name;
	}
}
