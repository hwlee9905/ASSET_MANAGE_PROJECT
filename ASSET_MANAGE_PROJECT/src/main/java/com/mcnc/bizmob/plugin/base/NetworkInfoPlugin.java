package com.mcnc.bizmob.plugin.base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mcnc.bizmob.core.plugin.BMCPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkInfoPlugin extends BMCPlugin {
	
	String callback = "";

	@Override
	protected void executeWithParam(JSONObject data) {
		
		try{
			
			if(data.has("param")){
				
				JSONObject param = data.getJSONObject("param");
				callback = param.getString("callback");
				
				listener.resultCallback("callback", callback, getNetworkInfo());
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String getIpAddress() throws Exception{
		
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			
			NetworkInterface intf = en.nextElement();
			
			for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();){
				
				 InetAddress inetAddress = enumIpAddr.nextElement();
				 
				 if(!inetAddress.isLoopbackAddress()){

					 if (inetAddress instanceof Inet4Address) {
						 
						 return inetAddress.getHostAddress().toString();
					 }
				 }
			}
		}
		
		return null;
	}
	
	private JSONObject getNetworkInfo(){
		
		ConnectivityManager manager =  (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);  

		NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
		NetworkInfo ni = manager.getActiveNetworkInfo();  

		JSONObject root = new JSONObject();
		
		String activate = "";
		String wvalue = wifi.isConnected() ? "connected" : "disconnected";
		String mvalue = mobile.isConnected() ? "connected" : "disconnected";
		
		if (ni != null ) {
			activate = ni.getTypeName().toLowerCase();
		}
		
		try {
			
			root.put("result", true);
			root.put("exception_msg", "");
			
			if(activate.equals("mobile")){
				
				root.put("mobile", true);
				root.put("mobile_address", getIpAddress());
				root.put("wifi", false);
				root.put("wifi_address", "");
				
			} else if(activate.equals("wifi")){
				
				root.put("mobile", false);
				root.put("mobile_address", "");
				root.put("wifi", true);
				root.put("wifi_address", getIpAddress());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			try{
				
				root.put("result", false);
				root.put("exception_msg", e.getMessage());
				
			} catch(JSONException e1){
				e1.printStackTrace();
			}
		}
		
		return root;
	}
}
