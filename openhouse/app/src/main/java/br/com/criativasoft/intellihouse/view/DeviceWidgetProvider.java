package br.com.criativasoft.intellihouse.view;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import br.com.criativasoft.intellihouse.IntelliHouseIntent;
import br.com.criativasoft.intellihouse.R;
import br.com.criativasoft.opendevice.core.command.Command;

public class DeviceWidgetProvider extends AppWidgetProvider {
	
	private static final String TAG = "DeviceWidgetProvider";
	
	/** Called when the activity is first created. */
	private String batteryLevel = "init";
	private int widgetImageFrame = R.drawable.ic_power_widget;
	private AppWidgetManager appWidgetManager;
	
	private List<Integer> widgetIds = new ArrayList<Integer>();

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		context.getApplicationContext().registerReceiver(this,new IntentFilter(IntelliHouseIntent.EVENT_DEVICE_UPDATED));
		this.appWidgetManager = appWidgetManager;
		for (int id : appWidgetIds) {
			Log.d(TAG, "Widget " + id + " update");
			widgetIds.add(Integer.valueOf(id));
			DeviceWidgetConfigure.updateWidget(context, appWidgetManager, id);		
		}
		
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		for (int id : appWidgetIds) {
			boolean deleted = widgetIds.remove(Integer.valueOf(id));
			Log.d(TAG, "Widget " + id + " deleted:"+deleted);
		    DeviceWidgetConfigure.deleteWidget(context, id);			
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Command command = (Command) intent.getSerializableExtra(IntelliHouseIntent.EXTRA_COMMAND);
		Log.d(TAG, "widget commandReceived = " + command);
		
		if(command !=null){
			Context applicationContext = context.getApplicationContext();
			int widgetIDs[] = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(new ComponentName(applicationContext, DeviceWidgetProvider.class));

			for (int id : widgetIDs){
				 DeviceWidgetConfigure.updateWidget(context,appWidgetManager, id);
			}
		
		}else{
		}
		
		super.onReceive(context, intent);
	}
	
//	public void updateView(Context context) {
//		RemoteViews thisViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.device_widget_layout);
//		thisViews.setTextViewText(R.id.widget_text, "XXX");
//		thisViews.setImageViewResource(R.id.widget_icon, widgetImageFrame);
//		ComponentName thisWidget = new ComponentName(context,DeviceWidgetProvider.class);
//		AppWidgetManager.getInstance(context).updateAppWidget(thisWidget,thisViews);
//	}

}
