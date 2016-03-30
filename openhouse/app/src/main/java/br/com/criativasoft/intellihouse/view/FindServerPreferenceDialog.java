package br.com.criativasoft.intellihouse.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import br.com.criativasoft.intellihouse.io.IOUtils;
import br.com.criativasoft.intellihouse.io.NetworkServerDiscoverer;
import br.com.criativasoft.intellihouse.io.NetworkServerDiscoverer.DiscoveryListener;

public class FindServerPreferenceDialog extends DialogPreference implements
		OnClickListener, DiscoveryListener {
	
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private EditText mValueText;
	private Button btnFind;
	private TextView mSplashText;
	private Context mContext;
	private ProgressDialog progress = null; 

	private String mDialogMessage;
	private String mDefault, mValue = null;

	public FindServerPreferenceDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mDefault = attrs.getAttributeValue(androidns, "defaultValue");

	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		mSplashText = new TextView(mContext);
		mSplashText.setTextColor(Color.WHITE);
		
		if (mDialogMessage != null)
			mSplashText.setText(mDialogMessage);
		
		layout.addView(mSplashText);

		mValueText = new EditText(mContext);
		layout.addView(mValueText, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		btnFind = new Button(mContext);
		btnFind.setText("Scanear Rede ...");
		btnFind.setOnClickListener(this);

		layout.addView(btnFind, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

//		mValueText = new TextView(mContext);
//		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
//		mValueText.setTextSize(32);
//		params = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.FILL_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);
//		layout.addView(mValueText, params);

		// mSeekBar = new SeekBar(mContext);
		// mSeekBar.setOnSeekBarChangeListener(this);
		// layout.addView(mSeekBar, new
		// LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));

		if (shouldPersist())
			mValue = getPersistedString(mDefault);

		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mValueText.setText(mValue);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedString(mDefault) : null;
		else
			mValue = (String) defaultValue;
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (shouldPersist() && positiveResult) persistString(mValueText.getText().toString());
	}


	@Override
	public void onClick(View v) {
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		if(IOUtils.hasWifi(mContext)){
			NetworkServerDiscoverer discoverer = new NetworkServerDiscoverer(wifiManager);
			discoverer.setListener(this);
			discoverer.start();
			progress = ProgressDialog.show(mContext, "Servidor", "Buscando na Rede Local...", true, true);
		}else{
			Toast.makeText(mContext, "Wifi não conectado !", Toast.LENGTH_SHORT).show();
		}
	} 
	
	@Override
	public void onDiscovererServer(final String server) {
		
		mValueText.post(new Runnable() {
			@Override
			public void run() {
				if(progress != null && progress.isShowing()) progress.hide();
				mValueText.setText(server);
				mValue = server;
			}
		});
	}
	
	@Override
	public void onDiscovererTimeout() {
		mValueText.post(new Runnable() {
			@Override
			public void run() {
				if(progress != null && progress.isShowing()) progress.hide();
				Toast.makeText(mContext, "Nenhum servidor encontrado !", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
}
