package com.mcnc.bizmob.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.def.ActivityRequestCode;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCWebFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 화면 오픈시 베이스로 활용되는 Fragment로써 BMCWebFragment를 상속받는다. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : base 화면 <br>
 * 04.관련 API/화면/서비스 : SlideFragmentActivity, ActivityRequestCode, BMCManager, CompleteListener, AppConfig, ImageWrapper, RUtil, BMCInit, BMCWebFragment<br>
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
public class MainFragment extends BMCWebFragment {

    /**
     * 네트워크 설정화면 진입 여부.
     */
    boolean touched;

    /**
     * start page file이 존재하는 path 값
     */
    final String startPage = "login/html/login.html";

    public KeyboardView mSecureKeypad;
    public KeyboardView.OnKeyboardActionListener mSecureKeypadListener;

    /**
     * Fragment의 라이프 사이클 중 하나에 해당하는 Method로 View를 inflate 하는 작업을 수행한다.<br>
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
     * @param inflater           LayoutInflater 객체
     * @param container          ViewGroup 객체
     * @param savedInstanceState Bundle 객체
     * @return root view 객체
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.i("Fragment Lifecycle", "onCreateView");
        wrapper = inflater.inflate(RUtil.getLayoutR(getActivity(), "activity_main"), null);

        if (savedInstanceState != null) {
            needToBlockLogic = true;
        }

        return wrapper;
    }

    /**
     * Fragment의 라이프 사이클 중 하나에 해당하는 Method로 Fragment가 destory 될 때 호출 된다. <br>
     * 현재 fragment가 보유하고 있는 webview 객체를 메모리 해제 하는 작업을 수행 한다. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    @Override
    public void onDestroy() {
        if (fWebView != null && fWebView.getUrl() != null) {

            fWebView.clearAnimation();
            fWebView.clearHistory();
            fWebView.clearView();
            fWebView.destroyDrawingCache();
            fWebView.removeAllViews();
            ViewGroup viewGroup = (ViewGroup) fWebView.getParent();
            viewGroup.removeView(fWebView);

            fWebView.destroy();
            fWebView = null;
        }
        super.onDestroy();
    }

    /**
     * SettingViewPlugin을 수행하여 호출된 NetworkSettingActivity (네트워크 설정 화면)으로 이동한 경우, 결과 값을 리턴 받는 메소드.<br>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param requestCode 요청 코드
     * @param resultCode  응답 코드
     * @param data        리턴 결과 data로 네트워크 설정값 셋팅 후, "exit" 앱종료, "restart" 재시작, "refresh" 화면갱신 수행에 해당하는 type 값
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ActivityRequestCode.NETWORK_SETTING) {

            touched = false;

            if (resultCode == Activity.RESULT_OK) {

                String type = data.getStringExtra("type");

                if (type.equals("kill")) {

                    try {

                        JSONObject message = new JSONObject();
                        JSONObject param = new JSONObject();

                        message.put("param", param);
                        param.put("kill_type", "kill");

                        BMCManager.getInstance().executeInterfaceFromID("APPLICATION_EXIT", message, new CompleteListener() {

                            @Override
                            public void resultCallback(String type, String callback, JSONObject param) {

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (type.equals("restart")) {

                    try {

                        JSONObject message = new JSONObject();
                        JSONObject param = new JSONObject();

                        message.put("param", param);
                        param.put("kill_type", "restart");

                        BMCManager.getInstance().executeInterfaceFromID("APPLICATION_EXIT", message, new CompleteListener() {

                            @Override
                            public void resultCallback(String type, String callback, JSONObject param) {

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (type.equals("refresh")) {

                    String url = fWebView.getUrl();
                    fWebView.loadUrl(url);
                }
            }
        }
    }

    /**
     * 디바이스의 터치 이벤트를 감지하는 메소드로 3 점식 터치를 통한 네트워크 화면 이동을 위한 메소드.<br>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param cb    Callback 객체
     * @param event MotionEvent 객체 touch event에 대한 정보를 갖고 있다.
     * @return event 전달 여부.
     */
    @Override
    public boolean dispatchTouchEvent(Window.Callback cb, MotionEvent event) {
        int touchCount = event.getPointerCount();

        //환경 설정 접근하기 위한 히든 제스쳐 2 : 3점식 터치
        if (touchCount == 3 && touched == false) {
            if (!BMCInit.BUILD_MODE.equals("release")) {
                touched = true;
                try {
                    JSONObject json = new JSONObject();
                    json.put("isDev", true);

                    BMCManager manager = BMCManager.getInstance();
                    manager.executeInterfaceFromID("SHOW_SETTING_VIEW", json, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return cb.dispatchTouchEvent(event);
    }

    /**
     * Fragment의 라이프 사이클 중 하나에 해당하는 onActivityCreated() Method에서, 호출 하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    @Override
    public void onCreateContents() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                wrapper.setVisibility(View.INVISIBLE);
            }
        });
    }

}
