package com.mcnc.bizmob.plugin.base;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class RunApplicationPlugin extends BMCPlugin {

	final String TAG = "RunApplicationPlugin";
	String callback = "";
	
	@Override
	protected void executeWithParam(JSONObject jsonData) {
		
		String method = "url";

		JSONObject root = new JSONObject();
		try {
			root.put("result", true);
			root.put("error_text", "");
			
			JSONObject param = jsonData.getJSONObject("param");
			if (param.has("callback"))
				callback = param.getString("callback");
			if (param.has("method"))
				method = param.getString("method");
			
			if ( method.equals("url") ) {
				JSONObject url = param.getJSONObject("url");
				String strUrl = url.getString("url");
				Uri uri = Uri.parse( strUrl );
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getActivity().startActivity(intent);
			} else if ( method.equals("action")) {
				JSONObject action = param.getJSONObject("action");
				String strAction = action.getString("action");
				String data = action.getString("data");
				if ( data.length() > 0 ) {
					Uri uri = Uri.parse( data );
					Intent intent = new Intent( strAction , uri);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getActivity().startActivity(intent);
					Logger.d(TAG, "action : " + action);
					Logger.d(TAG, "data : " + data);
				} else {
					Intent intent = new Intent( strAction);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getActivity().startActivity(intent);
					Logger.d(TAG, "action : " + action);
				}
			} else if ( method.equals("package")) {
				String str_class_name = "";
				String str_package_name = "";
				JSONObject package_name = param.getJSONObject("package");
				JSONObject data = new JSONObject();
				if ( package_name.has("class_name")) {
					str_class_name = package_name.getString("class_name");
				}
				if ( package_name.has("package_name")) {
					str_package_name = package_name.getString("package_name");
				}
				if ( package_name.has("data")) {
					data = package_name.getJSONObject("data");
				}
				
				Intent intent = null;
				if ( str_class_name.length() > 0 ) {
					intent = new Intent();
					intent.setClassName( str_package_name, str_class_name );
				} else {
					intent = getActivity().getPackageManager().getLaunchIntentForPackage( str_package_name );
				}
				if ( intent != null ) {
					try {
				        Iterator<?> keys = data.keys();
				        while( keys.hasNext() ){
				            String key = (String)keys.next();
				            if ( data.get(key) instanceof String){
				            	intent.putExtra(key, data.getString(key));
				            } else if ( data.get(key) instanceof Integer){
				            	intent.putExtra(key, data.getInt(key));
				            } else if ( data.get(key) instanceof Long){
				            	intent.putExtra(key, data.getLong(key));
				            } else if ( data.get(key) instanceof Double){
				            	intent.putExtra(key, data.getDouble(key));
				            } else if ( data.get(key) instanceof Boolean){
				            	intent.putExtra(key, data.getBoolean(key));
				            } else if ( data.get(key) instanceof Float){
				            	intent.putExtra(key, (float)data.getDouble(key));
				            } 
				        }
					} catch (Exception e) {
						e.printStackTrace();
					}
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getActivity().startActivity(intent);
				} else {
					root.put("result", false);
					root.put("error_text", "앱이 존재하지 않거나 시작 화면이 없습니다.");
				}
			} else {
				root.put("result", false);
				root.put("error_text", "method NOT define");
			}
		} catch ( ActivityNotFoundException e) {
			try {
				root.put("result", false);
				root.put("error_text", "실행 시킬 수있는 앱이 없습니다.");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				root.put("result", false);
				root.put("error_text", "앱을 실행 시킬 수 없습니다.");
			} catch (JSONException e1) {
			}
		}
		
		listener.resultCallback("callback", callback, root);
	}
}
