package br.com.criativasoft.intellihouse.io;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import br.com.criativasoft.opendevice.nanohttp.server.ControlServer;
import br.com.criativasoft.opendevice.nanohttp.server.NanoHTTPD;
import br.com.criativasoft.opendevice.nanohttp.server.RequestHandler;

public class AssertFileHandler implements RequestHandler {
	
	private static final String TAG = "AssertFileHandler";
	
	private Context context;

	public AssertFileHandler(Context context) {
		super();
		this.context = context;
	}
	
	private InputStream getFile(String name){
		AssetManager assetManager = context.getResources().getAssets();
		InputStream inputStream = null;

		try {
			inputStream = assetManager.open(name);
			if (inputStream != null) Log.d("AssertFileHandler", "It worked!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputStream;
	}

	@Override
	public NanoHTTPD.Response processRequest(String uri, Map<String, String> parms, ControlServer server) {
		
		String fileName = null;
		if(uri.endsWith("/")){
			fileName = "index.html";
		}else{
			int index = uri.lastIndexOf('/') + 1;
			fileName = uri.substring(index);
		}
		
		String mimeType = NanoHTTPD.getMimeType(fileName);
		
		Log.d(TAG, "Serving :: Type=" + mimeType + ", File:" + fileName);
		
		return new NanoHTTPD.Response( NanoHTTPD.Response.Status.OK, mimeType, getFile(fileName));
	}

}
