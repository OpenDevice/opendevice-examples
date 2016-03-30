package br.com.criativasoft.intellihouse.view;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RemoteViews;
import android.widget.Spinner;
import br.com.criativasoft.intellihouse.IntelliHouseIntent;
import br.com.criativasoft.intellihouse.R;
import br.com.criativasoft.intellihouse.service.DeviceManagerService;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.Device;

public class DeviceWidgetConfigure extends Activity {
	  private static final String TAG = "DeviceWidgetConfigure";
	  private static final String PREFS_WIDGETS = "widgets";
	  
	  private int mAppWidgetId;
	  private ArrayAdapter<String> adapter;
	  
	  public ArrayAdapter<String> getListAdapter(){
		  
		if (adapter == null) {
			List<String> list = new LinkedList<String>();
			List<Device> devices = DeviceManagerService.getDevicesLoaded();

			for (Device device : devices) {
				list.add(device.getUid() + ":" + device.getName());
			}

			adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,list);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		}
		
		return adapter;
		  
	  }
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent intent = getIntent();
	    Bundle extras = intent.getExtras();
	    if (extras != null) {
	        mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	    }
	    
	    Log.d(TAG, "onCreate :: mAppWidgetId == " + mAppWidgetId);
	    
	    setContentView(R.layout.device_widget_configure);
	    final Spinner spinner = (Spinner) findViewById(R.id.device_configure_id);
	    final CheckBox enabled = (CheckBox) findViewById(R.id.device_configure_enabled);
	    
	    spinner.setAdapter(getListAdapter());
	    
	    Button ok = (Button) findViewById(R.id.device_configure_ok);
	    Button cancel = (Button) findViewById(R.id.device_configure_cancel);
	    
	    ok.setOnClickListener(new OnClickListener() {
	      @Override
	      public void onClick(View v) {

	    	String deviceStr = (String) spinner.getSelectedItem();
	    	int deviceID = Integer.parseInt(deviceStr.split(":")[0]);
	    	  
	    	SharedPreferences prefs = getSharedPreferences(PREFS_WIDGETS, Context.MODE_PRIVATE);
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putBoolean(textEnableProperty(mAppWidgetId), enabled.isChecked());
	        editor.putInt(textDeviceProperty(mAppWidgetId), deviceID);
	        editor.commit();
	        Log.d(TAG, "Widget " + mAppWidgetId + " configured");

	        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(DeviceWidgetConfigure.this);
	        updateWidget(DeviceWidgetConfigure.this, appWidgetManager, mAppWidgetId);
	        Intent resultValue = new Intent();
	        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	        setResult(RESULT_OK, resultValue);
	        finish();
	      }
	    });
	    cancel.setOnClickListener(new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	        finish();
	      }
	    });
	    
	    enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	      @Override
	      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	        spinner.setEnabled(isChecked);
	      }
	    });
	    
	    Intent resultValue = new Intent();
	    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	    setResult(RESULT_CANCELED, resultValue);
	  }
	  
	  static String textEnableProperty(int id) {
	    return "textEnable:" + Integer.toString(id);
	  }
	  
	  static String textDeviceProperty(int id) {
	    return "textDevice:" + Integer.toString(id);
	  }
	  
	  static void deleteWidget(Context context, int id) {
	    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_WIDGETS, Context.MODE_PRIVATE).edit();
	    editor.remove(textEnableProperty(id));
	    editor.remove(textDeviceProperty(id));
	    editor.commit();
	  }
	  
	  static void updateWidget(Context context, AppWidgetManager appWidgetManager, int id) {
	    SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGETS, Context.MODE_PRIVATE);
	    boolean textEnable = prefs.getBoolean(textEnableProperty(id), true);
	    int deviceID = prefs.getInt(textDeviceProperty(id), 0);
	    Device device =  BaseDeviceManager.getInstance().findDeviceByUID(deviceID);
	    String text = "NotFound";
	    int icon = R.drawable.ic_power_widget_off;
	    
	    RemoteViews view = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.device_widget_layout);
	  
	    if(device != null){
	    	text = device.getName();
	    	if(device.getValue() == Device.VALUE_HIGH){
	    		icon = R.drawable.ic_power_widget;
	    	}
	    }
	    
	    if (!textEnable) view.setViewVisibility(R.id.widget_text, View.GONE);
	    else view.setTextViewText(R.id.widget_text, text);
	    
	    view.setImageViewResource(R.id.widget_icon, icon);
	    
	    if(device != null){

			device.toggle();

//	    	DeviceCommand command = new DeviceCommand(CommandType.DIGITAL, device.getUid(), (device.getValue() == Device.VALUE_HIGH ? Device.VALUE_LOW : Device.VALUE_HIGH));
//			Intent intent = new Intent(IntelliHouseIntent.ACTION_SEND);
//			intent.putExtra(IntelliHouseIntent.EXTRA_COMMAND, command);
//			intent.putExtra(IntelliHouseIntent.EXTRA_DEVICE_ID, command.getDeviceID());
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
//
//			// FIXME: not icon, but all widget
//			view.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);
//			Log.d(TAG, "ClickPendingIntent :: for device = " + deviceID);
	    }
		
		
	    appWidgetManager.updateAppWidget(id, view);
	  } 

}
