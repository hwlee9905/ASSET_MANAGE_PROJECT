package com.mcnc.bizmob.plugin.base;

import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.config.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 기존의 프로그레스 플러그인 (ProgressPlugin)과 커스텀 프로그레스 플러그인(CustomProgressPlugin)을 제어하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : Progress Plugin 제어 <br>
 * 04.관련 API/화면/서비스 : ProgressPlugin, CustomProgressPlugin <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-05                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class ProgressControllerPlugin extends BMCPlugin {
    /**
     * callback 명
     */
    private String callback = "";

    /**
     * 클라이언트로 부터 전달 받은 데이터를 각각의 요청에 맞는 프로그레스 관련 기능을 수행하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 프로그레스 기능 수행에 필요한 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {
        final JSONObject requestObj = new JSONObject();
        JSONObject requestParam = new JSONObject();
        String callId = "";
        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (param.has("callback")) {
                    callback = param.getString("callback");
                }

                if (param.has("type")) {
                    String type = param.getString("type");


                    if (type.equals("show")) {


                        if (AppConfig.PROGRESS_IMAGEPATH.length() > 0) {
                            callId = "SHOW_CUSTOM_PROGRESS";
                            requestParam.put("path", AppConfig.PROGRESS_IMAGEPATH);
                        } else {
                            callId = "SHOW_PROGRESS";
                            requestParam.put("type", "default");
                        }


                    } else if (type.equals("close")) {

                        if (AppConfig.PROGRESS_IMAGEPATH.length() > 0) {
                            callId = "DISMISS_CUSTOM_PROGRESS";
                            if (getProgressDialog() != null) {
                                if (getProgressDialog().isShowing()) {
                                    getProgressDialog().dismiss();
                                }
                            }
                        } else {
                            callId = "DISMISS_PROGRESS";
                            requestParam.put("type", "default");
                        }

                    }


                    requestParam.put("callback", callback);
                    requestObj.put("id", callId);
                    requestObj.put("param", requestParam);

                    getManager().executeInterfaceFromID(callId, requestObj, new CompleteListener() {
                        @Override
                        public void resultCallback(String s, String s1, JSONObject jsonObject) {
                            JSONObject result = new JSONObject();
                            try {
                                result.put("result", true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.resultCallback("callback", callback, result);
                        }
                    });


                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
