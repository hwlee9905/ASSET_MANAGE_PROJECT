package com.mcnc.bizmob.plugin.base;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.setting.SettingModel;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 01.클래스 설명 : 앱 위변조 체크하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 앱 위변조 체크 <br>
 * 04.관련 API/화면/서비스 : BMCManager, BMCPlugin, CompleteListener, Logger<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-09                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class GetRegisteredCallIdsPlugin extends BMCPlugin {


    @Override
    protected void executeWithParam(JSONObject data) {

        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (param.has("callback")) {
                    String callback = param.getString("callback");
                    JSONObject result = new JSONObject();
                    result.put("list", BMCManager.getInstance().getPluginList());
                    listener.resultCallback("callback", callback, result);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}