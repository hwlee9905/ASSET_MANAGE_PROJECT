package com.mcnc.bizmob.plugin.nat;

import com.mcnc.bizmob.manage.ntv.NativeSlideFragmentActivity;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.view.fragment.BMCFragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 네이티브 화면의 navigation 기능 수행을 위한 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 네이티브 화면 navigation <br>
 * 04.관련 API/화면/서비스 : NativeSlideFragmentActivity, BMCFragmentActivity<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-05                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class NativeNavigationPlugin extends BMCPlugin {

    @Override
    public void executeWithParam(JSONObject data) {

        try {
            JSONObject param = data.getJSONObject("param");

            BMCFragmentActivity bmcFragmentActivity = (BMCFragmentActivity) getActivity();

            String callback = "";
            JSONObject message = new JSONObject();
            String pageName = "";
            String type = "";
            int index = 0;

            if (param.has("page_name")) {
                pageName = param.getString("page_name");
            }

            if (param.has("callback")) {
                callback = param.getString("callback");
            }

            if (param.has("message")) {
                message = param.getJSONObject("message");
            }

            if (param.has("type")) {
                type = param.getString("type");
            }

            if (param.has("index")) {
                index = param.getInt("index");
            }

            if (bmcFragmentActivity instanceof NativeSlideFragmentActivity) {
                NativeSlideFragmentActivity nativeSlideFragmentActivity = (NativeSlideFragmentActivity) bmcFragmentActivity;
                if (type.equals("index")) {
                    nativeSlideFragmentActivity.moveNativeView(index, message, param);
                } else {
                    nativeSlideFragmentActivity.moveNativeView(pageName, message, param);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
