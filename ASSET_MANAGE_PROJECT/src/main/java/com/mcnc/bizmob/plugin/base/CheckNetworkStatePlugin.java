package com.mcnc.bizmob.plugin.base;

import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.mcnc.bizmob.manage.SlideFragmentActivity;
import com.mcnc.bizmob.manage.ntv.NativeSlideFragmentActivity;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.receiver.CheckConnectionReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 음영지역 기능을 수행하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 단말기의 네트워크 상태 변화를 알려주는 기능 <br>
 * 04.관련 API/화면/서비스 : CheckConnectionReceiver <br>
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
public class CheckNetworkStatePlugin extends BMCPlugin {

    /**
     * callback 명
     */
    private String callback = "";

    /**
     * event callback 명
     */
    private String eCallback = "networkstatechange";

    /**
     * 클라이언트 부터 전달 받은 데이터를 가공하여 음영지역 기능을 실행함.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 음영지역에 대한 정보 값 보유 <br><br>
     */
    @Override
    public void executeWithParam(JSONObject data) {
        JSONObject jResult = new JSONObject();
        boolean bResult = true;
        String errorMessage = "";

        String type = "";
        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (param.has("callback")) {
                    callback = param.getString("callback");
                }

                if (param.has("type")) {
                    type = param.getString("type"); // "regist" / "unregist"
                }

                if (type.equals("regist")) {
                    CheckConnectionReceiver checkConnectionReceiver;
                    if (getActivity() instanceof NativeSlideFragmentActivity) {
                        checkConnectionReceiver = ((NativeSlideFragmentActivity) getActivity()).getCheckConnectionReceiver();
                    } else {
                        checkConnectionReceiver = ((SlideFragmentActivity) getActivity()).getCheckConnectionReceiver();
                    }
                    checkConnectionReceiver.setOnChangeNetworkStatusListener(new CheckConnectionReceiver.onChangeNetworkStatusListener() {
                        @Override
                        public void onChanged(int status) {
                            JSONObject jResult = new JSONObject();
                            String type = "";
                            switch (status) {
                                case CheckConnectionReceiver.MOBILE_NETWORK_STATE_CONNECTED:
                                    type = "mobile";
                                    break;
                                case CheckConnectionReceiver.WIFI_NETWORK_STATE_CONNECTED:
                                    type = "wifi";
                                    break;
                                case CheckConnectionReceiver.NETWORK_STATE_DISCONNECTED:
                                    type = "none";
                                    break;
                            }

                            try {
                                jResult.put("type", type);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            listener.resultCallback("event", "on" + eCallback, jResult);

                        }
                    });
                    //리시버 등록
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    getActivity().registerReceiver(checkConnectionReceiver, intentFilter);

                } else if (type.equals("unregist")) {
                    CheckConnectionReceiver checkConnectionReceiver;
                    if (getActivity() instanceof NativeSlideFragmentActivity) {
                        checkConnectionReceiver = ((NativeSlideFragmentActivity) getActivity()).getCheckConnectionReceiver();
                    } else {
                        checkConnectionReceiver = ((SlideFragmentActivity) getActivity()).getCheckConnectionReceiver();
                    }
                    if (checkConnectionReceiver != null) {
                        //리시버 해제.
                        getActivity().unregisterReceiver(checkConnectionReceiver);
                    }

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            bResult = false;
            errorMessage = e.getMessage();
        }

        try {
            jResult.put("result", bResult);
            jResult.put("error_message", errorMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listener.resultCallback(callback, "callback", jResult);
    }
}
