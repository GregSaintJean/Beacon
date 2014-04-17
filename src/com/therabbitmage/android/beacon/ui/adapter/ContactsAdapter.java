package com.therabbitmage.android.beacon.ui.adapter;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.entities.beacon.BeaconSMSContact;
import com.therabbitmage.android.beacon.provider.BeaconManager;

public class ContactsAdapter extends ArrayAdapter<BeaconSMSContact> {
	private LayoutInflater mInflater;
	private List<BeaconSMSContact> mContacts;
	private BeaconManager mBeaconMgr;
	
	public ContactsAdapter(Context ctx){
		this(ctx, 0);
	}

	public ContactsAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = LayoutInflater.from(context);
		mBeaconMgr = new BeaconManager(context);
	}
	
	@Override
	public void add(BeaconSMSContact c) {
		mContacts.add(c);
	}

	@Override
	public void addAll(Collection<? extends BeaconSMSContact> collection) {
		mContacts.addAll(collection);
	}

	@Override
	public void addAll(BeaconSMSContact... items) {
		
		for(BeaconSMSContact c : items){
			mContacts.add(c);
		}
		
	}
	
	public void setData(List<BeaconSMSContact> c){
		mContacts = c;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(mContacts == null){
			return 0;
		}
		
		return mContacts.size();
	}

	@Override
	public BeaconSMSContact getItem(int position) {
		return mContacts.get(position);
	}

	@Override
	public boolean isEmpty() {
		return mContacts == null || mContacts.size() <= 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = null;
		ViewHolder vh = null;
		if(convertView != null){
			v = convertView;
			vh = (ViewHolder)convertView.getTag();
		} else {
			vh = new ViewHolder();
			v = mInflater.inflate(R.layout.phone_contact_item, null, false);
			vh.title = (TextView)v.findViewById(R.id.title);
			vh.subtitle = (TextView)v.findViewById(R.id.subtitle);
			vh.check_item = (Button)v.findViewById(R.id.delete_btn);
			v.setTag(vh);
		}
		
		vh.title.setText(mContacts.get(position).getDisplayName());
		vh.subtitle.setText(mContacts.get(position).getNumber());
		final int currentPosition = position; 
		vh.check_item.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				new AsyncTask<Void, Void, Void>(){

					@Override
					protected Void doInBackground(Void... params) {
						mBeaconMgr.removePhoneContactByBeaconId(mContacts.get(currentPosition).getContactId());
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						super.onPostExecute(result);
						mContacts.remove(currentPosition);
						ContactsAdapter.this.notifyDataSetChanged();
					}
					
				}.execute();
				
			}
			
		});
		
		return v;
	}
	
	class ViewHolder{
		TextView title;
		TextView subtitle;
		Button check_item;
		int position;
	}

}
