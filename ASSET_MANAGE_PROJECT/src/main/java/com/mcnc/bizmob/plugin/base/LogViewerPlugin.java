package com.mcnc.bizmob.plugin.base;

import android.content.Intent;

import com.mcnc.bizmob.manage.SlideFragmentActivity;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.view.log.LogViewerActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 로그뷰어 관련 기능을 제어하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 로그뷰어 기능 제어. <br>
 * 04.관련 API/화면/서비스 : LogViewerActivity <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-01                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class LogViewerPlugin extends BMCPlugin {

    /**
     * callback 명.
     */
    private String callback = "";
    /**
     * 로그뷰어 기능 type 값.
     */
    private String type = "";

    /**
     * 에러 메시지;
     */
    private String errorMessage = "";

    /**
     * 클라이언트 부터 전달 받은 데이터를 가공하여 로깅 기능 수행 / 중지 / 삭제 / 뷰어 호출 등을 수행함.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-01                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 로깅 기능 수행(record) / 중지(pause) / 삭제(delete) / 뷰어(viewer)에 대한 호출 type 및 callback 값 보유. <br>
     */
    @Override
    public void executeWithParam(JSONObject data) {

        JSONObject jResult = new JSONObject();
        boolean bResult = true;

        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");
                if (param.has("callback")) {
                    callback = param.getString("callback");
                }

                if (param.has("type")) {
                    type = param.getString("type");
                }

                if (type.equals("record")) {
                    if (!Logger.logging) {
                        Logger.logging = true;
                    } else {
                        bResult = false;
                        errorMessage = "이미 로깅 중입니다.";
                    }
                } else if (type.equals("pause")) {
                    if (Logger.logging) {
                        Logger.logging = false;
                    } else {
                        bResult = false;
                        errorMessage = "로깅중이 아닙니다.";

                    }
                } else if (type.equals("delete")) {
                    Logger.logBuffer.delete(0, Logger.logBuffer.length());

                } else if (type.equals("viewer")) {
                    Intent intent = new Intent(getActivity(), LogViewerActivity.class);
                    getActivity().startActivity(intent);

                } else if (type.equals("menu")) {
                    final SlideFragmentActivity slideFragmentActivity = (SlideFragmentActivity) getActivity();
                    BMCManager.getInstance().getSetting().setLogViewerMenuOn(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            slideFragmentActivity.setLogViewerMenu();
                        }
                    });

                }

            }

            jResult.put("result", bResult);
            jResult.put("error_message", errorMessage);
            listener.resultCallback("callback", callback, jResult);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
