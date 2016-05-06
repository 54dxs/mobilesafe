package net.dxs.mobilesafe.ui.adapter;

import java.util.List;

import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.BlackNumber;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 黑名单列表适配器
 * 
 * @author lijian-pc
 * @date 2016-5-5 下午4:49:26
 */
public class BlackNumberAdapter extends BaseAdapter implements OnClickListener {

	private Context mContext;
	private List<BlackNumber> mList;
	private IDeleteListener mCallback;

	public BlackNumberAdapter(Context context, List<BlackNumber> list,
			IDeleteListener callback) {
		this.mContext = context;
		this.mList = list;
		this.mCallback = callback;
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

	// 异常的原因 是单位时间内 getview方法创建对象的速度 > 回收对象的速度
	// converview 历史缓存的view对象，可以使用这个缓存的view对象 （检查是否为空 检查类型是否合适）
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BlackNumber info = mList.get(position);

		ViewHolder viewHolder = null;
		// 1.使用历史缓存的view对象 减少 布局创建的次数
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_black_number,
					null);
			// 2.减少子孩子查询的次数，只是在创建子孩子的时候 获取孩子对象的引用
			viewHolder = getViewHolder(convertView);
			convertView.setTag(viewHolder);// 把记事本放在父亲的口袋里
		} else {
			viewHolder = (ViewHolder) convertView.getTag();// 从口袋里面取出记事本
		}
		// 寻找子孩子的引用比较消耗资源
		setData(viewHolder, info, position);
		return convertView;
	}

	private void setData(ViewHolder v, BlackNumber info, int position) {
		v.mTv_number.setText(info.getNumber());
		v.mTv_mode.setText(info.getMode());
		String mode = info.getMode();
		if (mode.equals(Constants.SAFE_MODE_ALL)) {
			v.mTv_mode.setText("全部拦截");
		} else if (mode.equals(Constants.SAFE_MODE_PHONE)) {
			v.mTv_mode.setText("电话拦截");
		} else if (mode.equals(Constants.SAFE_MODE_SMS)) {
			v.mTv_mode.setText("短信拦截");
		}
		v.mIv_delete.setOnClickListener(this);
		v.mIv_delete.setTag(position);
	}

	private ViewHolder getViewHolder(View v) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.mTv_number = (TextView) v
				.findViewById(R.id.tv_itemBlackNumber_number);
		viewHolder.mTv_mode = (TextView) v
				.findViewById(R.id.tv_itemBlackNumber_mode);
		viewHolder.mIv_delete = (ImageView) v
				.findViewById(R.id.iv_itemBlackNumber_delete);
		return viewHolder;
	}

	/**
	 * 定义view对象的容器，记事本用来保存孩子控件的引用。
	 * 
	 * @author lijian-pc
	 * @date 2016-5-5 下午5:16:07
	 */
	static class ViewHolder {
		/** 名称 */
		TextView mTv_number;
		/** 号码 */
		TextView mTv_mode;
		/** 头像 */
		ImageView mIv_delete;
	}

	@Override
	public void onClick(View v) {
		if (mCallback != null && mCallback instanceof IDeleteListener) {
			((IDeleteListener) mCallback).onDelete(v);
		}
	}

	/**
	 * 对外暴露一个接口
	 * 
	 * @author lijian-pc
	 * @date 2016-5-6 上午11:44:39
	 */
	public interface IDeleteListener {
		public void onDelete(View v);
	}
}
