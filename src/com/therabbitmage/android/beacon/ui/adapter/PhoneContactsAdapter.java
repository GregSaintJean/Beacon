package com.therabbitmage.android.beacon.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.provider.BeaconManager;
import com.therabbitmage.android.beacon.utils.AndroidUtils;

public class PhoneContactsAdapter extends CursorAdapter {
	
	private LayoutInflater mInflater;
	private ArrayMap<Integer, Boolean> mSelectedNumbers;
	private BeaconManager mManager;
	private boolean isCursorSet;
	
	public PhoneContactsAdapter(Context context) {
		super(context, null, 0);
		mInflater = LayoutInflater.from(context);
		mSelectedNumbers = new ArrayMap<Integer, Boolean>();
		mManager = new BeaconManager(context);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		isCursorSet = newCursor != null;
		return super.swapCursor(newCursor);
	}
	
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		isCursorSet = cursor != null;
	}

	public boolean isCursorSet(){
		return isCursorSet;
	}
	
	public void setSelectedNumbers(ArrayMap<Integer, Boolean> numbers){
		if(numbers != null){
			mSelectedNumbers = numbers;
		} else {
			mSelectedNumbers = new ArrayMap<Integer, Boolean>();
		}
		notifyDataSetChanged();
		
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder)view.getTag();
		
		final int phoneContactId = cursor.getInt(cursor.getColumnIndex(Data._ID));
		final String name = cursor.getString(
			    cursor.getColumnIndex(
				AndroidUtils.honeycombOrBetter() ? Data.DISPLAY_NAME_PRIMARY : Data.DISPLAY_NAME));
		final String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		
		holder.name.setText(name);
		holder.number.setText(number);
		
		//holder.choiceBox.setChecked(mSelectedNumbers.containsKey(phoneContactId));
		
		holder.choiceBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				
				if(isChecked){
					new Thread(new Runnable(){

						@Override
						public void run() {
							mManager.addPhoneContact(phoneContactId, name, number);
							mSelectedNumbers.put(phoneContactId, true);
						}
						
					}).start();
					/*if(!mSelectedNumbers.containsKey(id)){
						
						
						new Thread(new Runnable(){

							@Override
							public void run() {
								mManager.addPhoneContact(name, id, number);
							}
							
						}).start();
						
						mSelectedNumbers.put(id, true);
						
						
					} */
				} else {
					new Thread(new Runnable(){

						@Override
						public void run() {
							mManager.removePhoneContactByContactId(phoneContactId);
							mSelectedNumbers.remove(phoneContactId);
						}
						
					}).start();
					/*new Thread(new Runnable(){

						@Override
						public void run() {
							mManager.removePhoneContact(id);
						}
						
					}).start();*/
					
				}
				
			}
			
		});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		final View itemLayout = mInflater.inflate(R.layout.phone_contact_list_adapter, viewGroup, false);
		
		final ViewHolder holder = new ViewHolder();
		holder.choiceBox = (CheckBox)itemLayout.findViewById(R.id.choice_box);
		holder.name = (TextView)itemLayout.findViewById(R.id.name);
		holder.number = (TextView)itemLayout.findViewById(R.id.number);
		itemLayout.setTag(holder);
		return itemLayout;
	}
	
	class ViewHolder{
		CheckBox choiceBox;
		TextView name;
		TextView number;
	}

}
