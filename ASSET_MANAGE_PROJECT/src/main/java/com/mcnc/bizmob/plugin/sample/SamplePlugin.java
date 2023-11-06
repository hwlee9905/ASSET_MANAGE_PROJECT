package com.mcnc.bizmob.plugin.sample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import android.util.Log;

import com.mcnc.bizmob.core.plugin.BMCPlugin;

import org.json.JSONObject;

public class SamplePlugin extends BMCPlugin {

	@Override
	public void executeWithParam(JSONObject data) {
		String callback = "";
		try {

			callback = data.getJSONObject("param").getString("callback");

			callCamera();

			listener.resultCallback("callback", callback, data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void callCamera(){
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		try{
			PackageManager pm = getActivity().getPackageManager();
			final ResolveInfo mInfo = pm.resolveActivity(i, 0);

			Intent intent = new Intent();
			intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			getActivity().startActivity(intent);
		} catch(Exception e){
			Log.i("TAG","Unable to Launch camera: " + e);
		}
	}
}
