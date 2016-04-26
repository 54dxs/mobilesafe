package net.dxs.mobilesafe.ui.adapter;

import java.util.List;

import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.ContactInfo;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 联系人信息适配器
 * 
 * @author lijian-pc
 * @date 2016-4-26 下午6:21:58
 */
public class ContactInfoAdapter extends BaseAdapter {

	private Context mContext;
	private List<ContactInfo> mList;

	public ContactInfoAdapter(Context context, List<ContactInfo> list) {
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
		ContactInfo info = mList.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_contact, null);
			viewHolder = getViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		setData(viewHolder, info);
		return convertView;
	}

	private void setData(ViewHolder v, ContactInfo info) {
		v.mIv_picture.setImageResource(android.R.drawable.sym_def_app_icon);
		v.mTv_name.setText(info.getName());
		v.mTv_phone.setText(info.getPhone());
	}

	private ViewHolder getViewHolder(View v) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.mIv_picture = (ImageView) v
				.findViewById(R.id.iv_contact_picture);
		viewHolder.mTv_name = (TextView) v.findViewById(R.id.tv_contact_name);
		viewHolder.mTv_phone = (TextView) v.findViewById(R.id.tv_contact_phone);
		return viewHolder;
	}

	static class ViewHolder {
		/** 头像 */
		ImageView mIv_picture;
		/** 名称 */
		TextView mTv_name;
		/** 号码 */
		TextView mTv_phone;
	}
}
