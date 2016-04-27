package net.dxs.mobilesafe.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import net.dxs.mobilesafe.Constants;
import net.dxs.mobilesafe.R;
import net.dxs.mobilesafe.domain.ContactInfo;
import net.dxs.mobilesafe.engine.ContactInfoProvider;
import net.dxs.mobilesafe.ui.adapter.ContactInfoAdapter;

/**
 * 选择联系人
 * 
 * @author lijian-pc
 * @date 2016-4-26 下午4:39:57
 */
public class SelectContactActivity extends BaseActivity implements
		OnItemClickListener {

	/** 联系人数据 */
	private static final int HANDLER_CONTACT_INFO = 1000;

	/** 数据显示列表 */
	private ListView mLv_contacts;
	/** 加载中... */
	private LinearLayout mLl_loading;
	/** 联系人数据集合 */
	private List<ContactInfo> mList_contactInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_contact);
		init();
	}

	private void init() {
		initView();
		initData();
	}

	private void initView() {
		mLv_contacts = (ListView) findViewById(R.id.lv_contacts);
		mLl_loading = (LinearLayout) findViewById(R.id.ll_loading);
	}

	private void initData() {
		mLl_loading.setVisibility(View.VISIBLE);
		mLv_contacts.setOnItemClickListener(this);
		getContactInfoList();
	}

	/**
	 * 获得联系人信息集合
	 */
	private void getContactInfoList() {
		new Thread() {
			public void run() {
				// 获取所有的联系人信息
				mList_contactInfo = ContactInfoProvider
						.getContactInfo(SelectContactActivity.this);
				mHandler.sendEmptyMessage(HANDLER_CONTACT_INFO);
			};
		}.start();
	}

	@Override
	protected void parserMessage(Message msg) {
		super.parserMessage(msg);
		switch (msg.what) {
		case HANDLER_CONTACT_INFO:// 联系人数据
			showContactInfoList();
			break;
		}
	}

	/**
	 * 显示联系人列表
	 */
	private void showContactInfoList() {
		mLl_loading.setVisibility(View.INVISIBLE);
		mLv_contacts
				.setAdapter(new ContactInfoAdapter(this, mList_contactInfo));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String phone = mList_contactInfo.get(position).getPhone();
		Intent data = new Intent();
		data.putExtra(Constants.INTENT_DATA_CONTACTINFO_PHONE, phone);
		setResult(Constants.ACTIVITYRESULT_CODE_SELECTCONTACTACTIVITY,
				data);// 设置一个结果数据
		finish();// 关闭掉当前的activity，把数据返回给调用者activity
	}
}
