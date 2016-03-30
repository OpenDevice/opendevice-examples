package br.com.criativasoft.intellihouse.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.criativasoft.intellihouse.IntelliHouseIntent;
import br.com.criativasoft.intellihouse.R;
import br.com.criativasoft.intellihouse.io.NetworkServerDiscoverer;
import br.com.criativasoft.intellihouse.service.DeviceManagerService;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.Sector;

public class SectorsActivity extends SherlockFragmentActivity 
							   implements ActionBar.TabListener, 
											ViewPager.OnPageChangeListener, 
											DeviceListFragment.OnDeviceSelectedListener {
	 
	private static final String TAG = "SectorsActivity";
	
	private ViewPager mViewPager;
	private ActionMode mMode;
	 
	private List<Sector> sectors;
	private Map<Integer, DeviceListFragment> fragments = new HashMap<Integer, DeviceListFragment>();
	 
	 @Override
	public void onCreate(Bundle savedInstanceState) {
		// setTheme(R.style.Theme_Sherlock_Light); //Used for theme switching in
		// samples
		super.onCreate(savedInstanceState);
		
		sectors = DeviceManagerService.getSectors();

		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_home);
		FragmentManager fm = getSupportFragmentManager();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		if (mViewPager != null) {
			// Phone setup
			mViewPager.setAdapter(new SectorsPagerAdapter(getSupportFragmentManager()));
			mViewPager.setOnPageChangeListener(this);

			// mViewPager.setPageMarginDrawable(R.drawable.grey_border_inset_lr);
			// mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin_width));

			final ActionBar actionBar = getSupportActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			
			for (Sector sector : sectors) {
				actionBar.addTab(actionBar.newTab().setText(sector.getName()).setTabListener(this));	
			}

		} else {
			// mExploreFragment = (ExploreFragment)
			// fm.findFragmentById(R.id.fragment_tracks);
			// mMyScheduleFragment = (MyScheduleFragment) fm.findFragmentById(
			// R.id.fragment_my_schedule);
			// mSocialStreamFragment = (SocialStreamFragment)
			// fm.findFragmentById(R.id.fragment_stream);
			//
		}
		getSupportActionBar().setHomeButtonEnabled(false);
		
		Intent service = new Intent(this, DeviceManagerService.class);
		startService(service);		
		
	}
	 
	@Override
	protected void onResume() {
		super.onResume();
		
		// TODO: Monitora comandos recebidos pelo arduino(módulo) ou wifi
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(IntelliHouseIntent.EVENT_DEVICE_UPDATED);
//		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
//		// android.bluetooth.device.action.ACL_DISCONNECTED
//		registerReceiver(RECEIVER, filter);

		Collection<DeviceListFragment> values = fragments.values();
		for (DeviceListFragment deviceListFragment : values) {
			if(deviceListFragment.isVisible()){
				deviceListFragment.updateDeviceStates();	
			}
		}
	}
	 
	@Override
	protected void onPause() {
		super.onPause();
	} 
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	/** FIXME: Como solução temporário foi implementada uma custom view com R.layout.header, para colocar mais
	 * icones na ActionBar, pois ele so estava suportanto mais de 2 */
	private View setupActionModeBar(){
		LinearLayout customView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.header, null);
		
		View.OnClickListener listener = new MenuAddActionMode();

		// Add listener
		int childCount = customView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childAt = customView.getChildAt(i);
			childAt.setOnClickListener(listener);
		}

		return customView;
	}
	
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    switch(keycode) {
	        case KeyEvent.KEYCODE_MENU:
				View view = setupActionModeBar();
				mMode = startActionMode(new MenuAddActionMode());
				if(view != null){
					mMode.setCustomView(view);
				}
	            return true;
	    }

	    return super.onKeyDown(keycode, e);
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case R.id.menuSettings:
				openSettings();
				break;
			case R.id.submenu_add:
				
//				getSherlock().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
//				ActionBar actionBar = getSupportActionBar();
//				actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,ActionBar.DISPLAY_SHOW_CUSTOM);
//				 actionBar.setCustomView(v,
//				            new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
//				                    ActionBar.LayoutParams.WRAP_CONTENT,
//				                    Gravity.CENTER_VERTICAL | Gravity.RIGHT));				
				
				View view = setupActionModeBar();
				mMode = startActionMode(new MenuAddActionMode());
				if(view != null){
					mMode.setCustomView(view);
				}
				
//				mMode.setTitle("Adicionar:");
				break;
			case R.id.menuConsole:
				
				WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
				NetworkServerDiscoverer discoverer = new NetworkServerDiscoverer(wifiManager);
				discoverer.start();
				
				break;
			case R.id.menuExit:
				DeviceManagerService.stopService(this);
				this.finish();
				break;
		}
		
		return true;
	}

	private void openSettings(){
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
    /** Quando alguma informação for recebida do "arduino" ou pelo webservice */
	private void notifyCommandReceived(Command command) {

		if(command instanceof DeviceCommand){
			
			Device device = findDeviceByID(((DeviceCommand) command).getDeviceID());
			DeviceCommand deviceCommand = (DeviceCommand) command;
			
			CommandType type = command.getType();

			if (device == null)
				Toast.makeText(this, "Device NOT FOND", Toast.LENGTH_SHORT).show();

			if (device != null && (CommandType.DIGITAL == type || CommandType.ANALOG == type)) {

				device.setValue(deviceCommand.getValue()); 
				
				DeviceListFragment fragment = findFragmentForDevice(device.getUid());
				
				if(fragment != null){
					
					fragment.updateDeviceStates();
					
//					DeviceListAdapter deviceListAdapter = (DeviceListAdapter) fragment.getListAdapter();
//					
//					int index = deviceListAdapter.getDevices().indexOf(device);
//					View view = fragment.getListView().getChildAt(index);
	//
//					TextView tfName = (TextView) view.findViewById(R.id.txt_name);
//					tfName.setTextColor((device.getValue() == 1 ? Color.BLACK: Color.GREEN));	
				}else{
					Toast.makeText(this, "FRAGMENT NOT FOND", Toast.LENGTH_SHORT).show();
				}

			}
			
		}
		
	

	}
	
	// TODO: verificar se não irá ter problemas de performace, ou não funcionar pelo fato dos fragments serem criados
	// em tempo de execução, e a medida que foram clicados.
	private DeviceListFragment findFragmentForDevice(int deviceID){
		
		Collection<DeviceListFragment> values = fragments.values();
		
		for (DeviceListFragment fragment : values) {
			Device device = fragment.getDevice(deviceID);
			if(device != null) return fragment;
		}
		
		return null;
		
	}
	
	private Device findDeviceByID(int deviceID){
		return BaseDeviceManager.getInstance().findDeviceByUID(deviceID);
	}

	/**
	 * Chamado quando algum dispositivo da lista for clicado.
	 */
	@Override
	public void onDeviceSelected(Device device, DeviceListFragment fragment) {
		
		int uid = device.getUid();
		
		if (device.getValue() == Device.VALUE_HIGH) {
			device.setValue(Device.VALUE_LOW);
		} else {
			device.setValue(Device.VALUE_HIGH);
		}

		Log.d(TAG, "onDeviceSelected :: deviceID = " + uid + ", value="+device.getValue());
		
		fragment.updateDeviceStates();

	}
    

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction transaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public void onPageScrolled(int i, float v, int i1) {

	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageSelected(int position) {
		getSupportActionBar().setSelectedNavigationItem(position);
	}

	private class SectorsPagerAdapter extends FragmentPagerAdapter {
		public SectorsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			DeviceListFragment fragment = fragments.get(position);
			Sector sector = sectors.get(position);
			
			if(fragment == null){
				fragment = new DeviceListFragment();
				fragments.put(position, fragment);
			}
			
			fragment.setSector(sector);
			
			return fragment;
		}

		@Override
		public int getCount() {
			return sectors.size();
		}
	}
	    

    private final class MenuAddActionMode implements ActionMode.Callback, View.OnClickListener {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //Used to put dark icons on light action bar
        	boolean isLight = false;

            menu.add("Outros")
                .setIcon(isLight ? R.drawable.abs__ic_menu_moreoverflow_holo_light: R.drawable.abs__ic_menu_moreoverflow_holo_dark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            handlerClick(item.getItemId());
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        // NOTA: Ações da Custom View
		@Override
		public void onClick(View v) {
			handlerClick(v.getId());
		}
		
		private void handlerClick(int id){
			Toast.makeText(SectorsActivity.this, "Item ID: " + id, Toast.LENGTH_SHORT).show();
		}
    }
}
