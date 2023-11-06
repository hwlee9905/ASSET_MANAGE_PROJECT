package com.mcnc.bizmob.plugin.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.core.view.fragment.BMCFragmentActivity;
import com.mcnc.bizmob.view.internal.InternalBrowserFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 내부에서 브라우저를 호출하는 플러그인. <br>
 * 02.제품구분 : bizMob 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 내부 브라우저 호출 <br>
 * 04.관련 API/화면/서비스 : BMCUtil, BMCFragment, BMCFragmentActivity, InternalBrowserFragment<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-20                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class InternalBrowserPlugin extends BMCPlugin {

    /**
     * Request Code
     */
    private final int INTERNAL_BROWSER = 22334;

    /**
     * 실제 플러그인 수행에 필요한 data를 담고 있는 JSONObject
     */
    JSONObject param;

    /**
     * callback 줄 function 명
     */
    String callback = "";

    /**
     * 클라이언트로 부터 전달 받은 데이터를 가공하여 내부 브라우져를 실행함.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-20                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 내부 브라우져를 실행하기 위한 정보 값 보유 (ex. 내부 부라우저의 툴바, 타이틀바, hardware accelator 적용 여부,) <br><br>
     *             <p>
     *             Return : (JSONObject) 수행 결과 실패시 에러값 반환
     */
    @Override
    protected void executeWithParam(JSONObject data) {

        try {

            Bundle bundle = new Bundle();

            if (data.has("param")) {

                JSONObject param = data.getJSONObject("param");

                if (param.has("hardware_accelator")) {
                    bundle.putBoolean("hardware_accelator", param.getBoolean("hardware_accelator"));
                }

                if (param.has("callback")) {
                    bundle.putString("callback", param.getString("callback"));
                    this.callback = param.getString("callback");
                }

                if (param.has("basic")) {

                    JSONObject basic = param.getJSONObject("basic");

                    bundle.putString("type", "basic");
                    bundle.putString("url", basic.getString("url"));

                    if (basic.has("title")) {

                        JSONObject title = basic.getJSONObject("title");

                        if (title.has("raw")) {
                            bundle.putString("raw", title.getString("raw"));
                        }
                        if (title.has("text_color")) {
                            bundle.putString("text_color", title.getString("text_color"));
                        }
                        if (title.has("background_image")) {
                            bundle.putString("title_bg_image", title.getString("background_image"));
                        }
                        if (title.has("background_color")) {
                            bundle.putString("title_bg_color", title.getString("background_color"));
                        }
                        if (title.has("close_image")) {
                            bundle.putString("close_image", title.getString("close_image"));
                        }
                    }

                    if (basic.has("toolbar")) {

                        JSONObject tool = basic.getJSONObject("toolbar");

                        if (tool.has("background_image")) {
                            bundle.putString("tool_bg_image", tool.getString("background_image"));
                        }
                        if (tool.has("background_color")) {
                            bundle.putString("tool_bg_color", tool.getString("background_color"));
                        }
                        if (tool.has("historyback_image")) {
                            bundle.putString("historyback_image", tool.getString("historyback_image"));
                        }
                        if (tool.has("historyforward_image")) {
                            bundle.putString("historyforward_image", tool.getString("historyforward_image"));
                        }
                        if (tool.has("refresh_image")) {
                            bundle.putString("refresh_image", tool.getString("refresh_image"));
                        }
                    }

                } else {

                    if (param.has("search")) {

                        JSONObject search = param.getJSONObject("search");

                        bundle.putString("type", "search");

                        if (search.has("url")) {
                            bundle.putString("url", search.getString("url"));
                        }
                        if (search.has("close_image")) {
                            bundle.putString("close_image", search.getString("close_image"));
                        }
                        if (search.has("background_image")) {
                            bundle.putString("search_bg_image", search.getString("background_image"));
                        }
                        if (search.has("background_color")) {
                            bundle.putString("search_bg_color", search.getString("background_color"));
                        }
                        if (search.has("historyback_image")) {
                            bundle.putString("historyback_image", search.getString("historyback_image"));
                        }
                        if (search.has("historyforward_image")) {
                            bundle.putString("historyforward_image", search.getString("historyforward_image"));
                        }
                    }
                }

                BMCFragment bmcFragment;
                try {
                    bmcFragment = InternalBrowserFragment.class.newInstance();

                    bmcFragment.setArguments(bundle);

                    ((BMCFragmentActivity) getActivity()).openView(bmcFragment, param);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            try {
                JSONObject errorResult = BMCUtil.exceptionMessageParser(param, e.getMessage());
                listener.resultCallback("error", "", errorResult);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);

        if (reqCode == INTERNAL_BROWSER && resCode == Activity.RESULT_OK) {

            try {

                if (callback != null) {

                    JSONObject resultJSON = new JSONObject();
                    listener.resultCallback("callback", callback, resultJSON);
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    JSONObject errorResult = BMCUtil.exceptionMessageParser(param, e.getMessage());
                    listener.resultCallback("error", "", errorResult);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
