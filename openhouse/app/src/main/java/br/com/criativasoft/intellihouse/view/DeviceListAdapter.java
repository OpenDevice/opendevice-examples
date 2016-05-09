/*
  Amarino - A prototyping software toolkit for Android and Arduino
  Copyright (c) 2010 Bonifaz Kaufmann.  All right reserved.
  
  This application and its library is free software; you can redistribute
  it and/or modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package br.com.criativasoft.intellihouse.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import br.com.criativasoft.intellihouse.R;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Sector;

/**
 * List of Devices for Sect
 */
public class DeviceListAdapter extends BaseAdapter implements DeviceListener {

	private Context context;
    private Sector sector;
	
	public DeviceListAdapter(Context context, Sector sector){
		this.context = context;
        this.sector = sector;
    }
	

	public void addDevice(Device device){
		device.addListener(this);
		notifyDataSetChanged();
	}

    @Override
    public int getCount() {
        return getDevices().size();
    }

    public Object getItem(int position) {
		return getDevices().get(position);
	}
	
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout view = null;

		//((AbsListView)parent).setCacheColorHint(R.color.background);
		if (convertView == null) {
			view = new LinearLayout(context);
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vi.inflate(R.layout.device_row_view, view, true);
		} else {
			view = (LinearLayout) convertView;
		}
 
		TextView name = (TextView) view.findViewById(R.id.txt_name);
		TextView tfID = (TextView) view.findViewById(R.id.txt_id);
		TextView tfType = (TextView) view.findViewById(R.id.txt_type);
		ImageView icon = (ImageView) view.findViewById(R.id.img_user);
		
		Device device = getDevices().get(position);
		
		name.setText(device.getName());
		tfID.setText(""+device.getUid());
		tfType.setText(device.getType().getDescription());
		
		// TODO: remover, código estatico.
//		if(device.getCategory() == DeviceCategory.LAMP)
//			icon.setImageResource(R.drawable.user_image1);
//		if(device.getCategory() == DeviceCategory.FAN)
//			icon.setImageResource(R.drawable.user_image3);
//		if(device.getCategory() == DeviceCategory.GENERIC)
//			icon.setImageResource(R.drawable.user_image4);
		
		if(device.getCategory() == DeviceCategory.LAMP){
			if(device.getValue() == Device.VALUE_LOW) icon.setImageResource(R.drawable.lamp_off);
			if(device.getValue() > 0) icon.setImageResource(R.drawable.lamp_on);
			
		}else{
			if(device.getValue() == Device.VALUE_LOW) icon.setImageResource(R.drawable.switch_off_48);
			if(device.getValue() > 0) icon.setImageResource(R.drawable.switch_on_48);
				
		}
		
		return view;
	}
	
	public List<Device> getDevices() {
		return new LinkedList<>(sector.getDevices());
	}

	@Override
	public void onDeviceChanged(Device device) {
        notifyDataSetChanged();
	}
}


