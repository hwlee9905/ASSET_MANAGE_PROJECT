package com.mcnc.bizmob.plugin.nat;

import android.os.Bundle;

import com.mcnc.bizmob.manage.ntv.NativeSlideFragmentActivity;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.view.fragment.BMCFragmentActivity;
import com.mcnc.bizmob.view.ntv.BMCNativeFragment;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 01.클래스 설명 : 네이티브 화면 호출 / 닫기기능을 수행하는 메소드를 호출 하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 타이틀바 버튼에 뱃지 버튼 기능을 수행하는 메소드를 호출.<br>
 * 04.관련 API/화면/서비스 : BMCNativeFragment, NativeSlideFragmentActivity, NativeInit <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2017-01-13                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class ShowNativePlugin extends BMCPlugin {

    @Override
    public void executeWithParam(JSONObject data) {

        try {
            String commandID = data.getString("id");
            JSONObject param = data.getJSONObject("param");

            if (commandID.equals("SHOW_NATIVE")) { // SHOW NATIVE FRAGEMNT
                openNativeView(param);
            } else if (commandID.equals("POP_NATIVE")) {
                closeNativeView(param);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openNativeView(JSONObject param) throws JSONException {
        String className = "";
        JSONObject message = new JSONObject();
        String orientation = "portrait";
        String pageName = "";
        Bundle bundle = new Bundle();

        if (param.has("class_name")) {
            className = param.getString("class_name");
            bundle.putString("class_name", className);
        }

        if (param.has("message")) {
            message = param.getJSONObject("message");
            bundle.putString("data", message.toString());
        }

        if (param.has("orientation")) {
            orientation = param.getString("orientation");
            bundle.putString("orientation", orientation);

        }

        if (param.has("page_name")) {
            pageName = param.getString("page_name");
            bundle.putString("page_name", pageName);
        }

        BMCNativeFragment bmcNativeFragment;
        try {
            if (className.length() > 0) {
                Class fragmentClass = Class.forName(className);
                bmcNativeFragment = (BMCNativeFragment) fragmentClass.newInstance();
            } else {
                Logger.d("ShowNativePlugin", "Native fragment class name is not found.");
                return;
            }

            bmcNativeFragment.setArguments(bundle);

            if (((BMCFragmentActivity) getActivity()) instanceof NativeSlideFragmentActivity) {
                NativeSlideFragmentActivity nativeSlideFragmentActivity = (NativeSlideFragmentActivity) getActivity();
                nativeSlideFragmentActivity.openNativeView(bmcNativeFragment, param);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void closeNativeView(JSONObject param) {
        if (((BMCFragmentActivity) getActivity()) instanceof NativeSlideFragmentActivity) {
            NativeSlideFragmentActivity nativeSlideFragmentActivity = (NativeSlideFragmentActivity) getActivity();
            nativeSlideFragmentActivity.closeNativeView(param);
        }
    }
}
