package br.com.criativasoft.intellihouse.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.Sector;

public class DeviceListFragment extends SherlockListFragment {
	
	private static final String TAG = "DeviceListFragment";

	private OnDeviceSelectedListener mCallback;
	
	private Sector sector;
	
	private DeviceListAdapter deviceListAdapter;
	
	public interface OnDeviceSelectedListener {
		public void onDeviceSelected(Device device, DeviceListFragment fragment);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Set<Device> devices = sector.getDevices();

		deviceListAdapter = new DeviceListAdapter(getActivity(), sector);
		setListAdapter(deviceListAdapter);
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			
			mCallback = (OnDeviceSelectedListener) activity;
			
		} catch (ClassCastException e) {
			 throw new ClassCastException(activity.toString()+ " must implement OnDeviceSelectedListener");
		}
		
		
	}
	
	public void setSector(Sector sector) {
		this.sector = sector;
	}
	
	public Sector getSector() {
		return sector;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
  	  // Get the HashMap of the clicked item
  	  Device device = getDevices().get(position);

  	  if(mCallback != null) mCallback.onDeviceSelected(device, this);
  	  else Toast.makeText(getActivity(), "No Select calback", Toast.LENGTH_SHORT).show();
	}
	
	
	public void updateDeviceStates(){
		BaseAdapter listAdapter = (BaseAdapter) getListAdapter();
		listAdapter.notifyDataSetChanged();
	}
	
	
	public Device getDevice(int deviceID){
		for (Device device : sector.getDevices()) {
			if(device.getUid() == deviceID){
				return device;
			}
		}
		
		return null;
	}

	public List<Device> getDevices() {
		return new LinkedList<>(sector.getDevices());
	}
    
	
 

}
