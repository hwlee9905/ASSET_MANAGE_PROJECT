package com.mcnc.bizmob.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 01.클래스 설명 : Fcm Notification click_action에 정의 된 Activity. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : FCM notification 수신 후 화면 이동  <br>
 * 04.관련 API/화면/서비스 : Logger, BMCFragment, BMCInit <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2018-08-03                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class FcmControlActivity extends Activity {

    /**
     * class 명
     */
    private String TAG = this.toString();

    /**
     * callback 줄 function 명
     */
    private String callback = null;

    /**
     * 특정 정보를 담고 담고 있는 문자열
     */
    private String data = null;


    /**
     * 외부로 부터 전달 받은 호출에 반응하여, 특정화면을 호출하며, Push 호출인 경우 push event 발생을 웹에게 알려주는 메소드를 호출 하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param savedInstanceState Bundle 객체. 로직과 상관 없음.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");
        final Intent intent = getIntent();

        callback = "onPush";
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bundle = intent.getExtras();

                    String badge = bundle.getString("badge");
                    String message = bundle.getString("message");
                    String hasBin = bundle.getString("hasBin");
                    String meesageId = bundle.getString("message_id");

                    JSONObject pushObj = new JSONObject();
                    pushObj.put("badge", badge);
                    pushObj.put("message", message);
                    pushObj.put("hasBin", hasBin);
                    pushObj.put("messageId", meesageId);

                    Logger.d(TAG, "intent data : " + pushObj.toString());

                    data = pushObj.toString();
                    startActivity(callback, data);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();


        this.finish();
    }

    /**
     * 현재 앱의 화면 개수를 판단하여, 앱이 실행 중 인지 여부를 판단하며, 앱실행 또는 현재 화면에 event noti를 주는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param callback 클라이언트의 callback function 명.
     * @param data     callback 줄 data 값.
     */
    private void startActivity(final String callback, final String data) {

        ArrayList<BMCFragment> fragmentList = BMCInit.getFragmentList();
        if (fragmentList.size() > 0) {
            final BMCFragment fragment = (BMCFragment) fragmentList.get((fragmentList.size() - 1));
            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (fragment.fWebView != null) {
                        String callbackUrl = "";
                        if (callback != null && callback.length() > 0) {
                            if (callback.equals("onPush")) {
                                if (data != null) {
                                    callbackUrl = "javascript:bizMOBCore.EventManager.responser({eventname:'onPush'}, {message:" + data + "});";
                                } else {
                                    callbackUrl = "javascript:bizMOBCore.EventManager.responser({eventname:'onPush'}, {message:{}});";
                                }
                            } else {
                                if (data != null) {
                                    callbackUrl = "javascript:" + callback + "(" + data + ")";
                                } else {
                                    callbackUrl = "javascript:" + callback + "()";
                                }
                            }
                            Logger.d("startActivity", callbackUrl);
                            BMCUtil.loadUrlOrEvaluateJavascript(fragment.fWebView, callbackUrl);
                        }
                    }
                }
            });
        } else {
            Intent intent = new Intent(this, BMCInit.LAUNCHER_ACTIVITY);
            Logger.d(TAG, "callback:" + callback + "(" + data + ")");
            if (callback != null && callback.length() > 0) {
                intent.putExtra("external_callback", callback);
            }
            if (data != null) {
                intent.putExtra("external_data", data);
            }
            startActivity(intent);
        }
    }
}
