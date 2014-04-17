package com.therabbitmage.android.beacon.ui.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.entities.beacon.BeaconSMSContact;
import com.therabbitmage.android.beacon.entities.beacon.PhoneContact;
import com.therabbitmage.android.beacon.provider.BeaconManager;

public class SMSContactAdapter extends ArrayAdapter<PhoneContact> {
	
	private static final int ADD_CONTACT = 0;
	private static final int REMOVE_CONTACT = 1;
	private LayoutInflater mInflater;
	private BeaconManager mManager;
	private ArrayMap<Integer, Boolean> mSelectedNumbers;
	private ArrayList<PhoneContact> mPhoneContacts;
	private ArrayList<BeaconSMSContact> mBeaconSmsContacts;
	private Queue<Command> mQ;
	private boolean commandInProgress;
	
	public SMSContactAdapter(Context ctx){
		this(ctx, 0);
	}
	
	public SMSContactAdapter(Context ctx, int res){
		super(ctx, res);
		mInflater = LayoutInflater.from(ctx);
		mManager = new BeaconManager(ctx);
		mSelectedNumbers = new ArrayMap<Integer, Boolean>();
		commandInProgress = false;
		mQ  = new LinkedList<Command>();
	}
	
	public void setPhoneContacts(ArrayList<PhoneContact> phoneContacts){
		mPhoneContacts = phoneContacts;
		
		if(mBeaconSmsContacts != null && mPhoneContacts != null){
			processMatchingContacts();
		}
		
		notifyDataSetChanged();
	}
	
	public void setBeaconContacts(ArrayList<BeaconSMSContact> beaconContacts){
		mBeaconSmsContacts = beaconContacts;
		
		if(mBeaconSmsContacts != null && mPhoneContacts != null){
			processMatchingContacts();
		}
		
		notifyDataSetChanged();
	}
	
	private void processMatchingContacts(){
		mSelectedNumbers = new ArrayMap<Integer,Boolean>();
		
		for(int i = 0; i < mBeaconSmsContacts.size(); i++){
			
			for(int j = 0; j < mPhoneContacts.size(); j++){
				
				if(mBeaconSmsContacts.get(i).getContactId() == mPhoneContacts.get(j).getId()
						&& !mSelectedNumbers.containsKey(j)){
					mSelectedNumbers.put(j, true);
				}
				
			}
			
		}
		
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
			v = mInflater.inflate(R.layout.phone_contact_item_select, null, false);
			vh.title = (TextView)v.findViewById(R.id.title);
			vh.subtitle = (TextView)v.findViewById(R.id.subtitle);
			vh.checkbox = (CheckBox)v.findViewById(R.id.check_item);
			v.setTag(vh);
		}
		
		vh.title.setText(mPhoneContacts.get(position).getDisplayName());
		vh.subtitle.setText(mPhoneContacts.get(position).getNumber());
		vh.checkbox.setChecked(mSelectedNumbers.containsKey(position));
		final CheckBox cb = vh.checkbox;
		final boolean isChecked = vh.checkbox.isChecked();
		final int currentPosition = position;
		v.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isChecked){
					addToQueue(new Command(REMOVE_CONTACT, currentPosition));
				} else {
					addToQueue(new Command(ADD_CONTACT, currentPosition));
				}
				
			}
			
		});
		
		return v;
	}
	
	private void addToQueue(Command command){
		mQ.add(command);
		invoke();
	}
	
	private void invoke(){
		if(!commandInProgress && !mQ.isEmpty()){
			new AsyncTask<Void, Void, Void>(){

				@Override
				protected Void doInBackground(Void... params) {
					runCommand(mQ.poll());
					return null;
				}
				
			}.execute();
		}
	}
	
	private void runCommand(Command command){
		commandInProgress = true;
		if(command.getCommand() ==  ADD_CONTACT){
			mManager.addPhoneContact(
					mPhoneContacts.get(command.getPosition()).getId(),
					mPhoneContacts.get(command.getPosition()).getDisplayName(),
					mPhoneContacts.get(command.getPosition()).getNumber());
			mSelectedNumbers.put(command.getPosition(), true);
		}
		
		if(command.getCommand() == REMOVE_CONTACT){
			mManager.removePhoneContactByContactId(mPhoneContacts.get(command.getPosition()).getId());
			mSelectedNumbers.remove(command.getPosition());
		}
		((Activity)getContext()).runOnUiThread(new Runnable(){

			@Override
			public void run() {
				notifyDataSetChanged();
			}
			
		});
		commandInProgress = false;
		
		invoke();
		
	}

	@Override
	public int getCount() {
		if(mPhoneContacts == null 
				|| mBeaconSmsContacts == null){
			return 0;
		}
		
		return mPhoneContacts.size();
	}
	
	@Override
	public PhoneContact getItem(int position) {
		
		if(mPhoneContacts == null
				|| mBeaconSmsContacts == null){
			return null;
		}
		
		return mPhoneContacts.get(position);
	}
	
	private class Command extends Pair<Integer, Integer>{

		public Command(Integer first, Integer second) {
			super(first, second);
		}
		
		public int getCommand(){
			return first;
		}
		
		public int getPosition(){
			return second;
		}
		
	}

	class ViewHolder{
		TextView title;
		TextView subtitle;
		CheckBox checkbox;
	}

}
