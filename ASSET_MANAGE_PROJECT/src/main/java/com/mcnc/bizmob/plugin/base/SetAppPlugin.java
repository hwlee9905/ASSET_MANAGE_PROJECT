package com.mcnc.bizmob.plugin.base;

import androidx.fragment.app.Fragment;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.view.fragment.BMCWebFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 타이틀바 버튼에 뱃지 버튼 기능을 수행하는 메소드를 호출하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 타이틀바 버튼에 뱃지 버튼 기능을 수행하는 메소드를 호출.<br>
 * 04.관련 API/화면/서비스 : BMCWebFragment <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-22                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class SetAppPlugin extends BMCPlugin {


    @Override
    public void executeWithParam(JSONObject data) {

        try {
            String commandID = data.getString("id");
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (commandID.equals("BADGE_BUTTON")) {
                    badgeButton(param);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void badgeButton(final JSONObject param) throws JSONException {

        handler.post(new Runnable() {

            @Override
            public void run() {

                String id = "";
                String callback = "";
                int count = 0;

                try {

                    if (param.has("button_id")) {
                        id = param.getString("button_id");
                    }
                    if (param.has("count")) {
                        count = param.getInt("count");
                    }
                    if (param.has("callback")) {
                        callback = param.getString("callback");
                    }

                    if (getView() instanceof Fragment) {
                        ((BMCWebFragment) getFragment()).setBadge(id, count, callback);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
