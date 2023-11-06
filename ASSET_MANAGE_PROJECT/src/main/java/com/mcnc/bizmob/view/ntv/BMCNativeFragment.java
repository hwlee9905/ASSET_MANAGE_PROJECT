package com.mcnc.bizmob.view.ntv;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.core.view.fragment.BMCFragmentActivity;
import com.mcnc.bizmob.core.view.fragment.BMCSlideFragmentActivity;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public abstract class BMCNativeFragment extends BMCFragment {

    private final String TAG = this.getClass().getName();
    private BMCPlugin runningTask;
    String orientation = "none";

    public JSONObject message;

    public BMCManager manager;

    private boolean isFirst = false;

    public View wrapper;


    protected Dialog getProgressDialog() {
        BMCFragmentActivity bmcFragmentActivity = ((BMCFragmentActivity) getActivity());
        if (bmcFragmentActivity != null) {
            return bmcFragmentActivity.getProgressDialog();
        } else {
            return null;
        }
    }

    protected Dialog getProgressBar() {
        BMCFragmentActivity bmcFragmentActivity = ((BMCFragmentActivity) getActivity());
        if (bmcFragmentActivity != null) {
            return bmcFragmentActivity.getProgressBar();
        } else {
            return null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Bundle extra = getArguments();

        if (extra != null) {

            pageName = extra.getString("page_name");

            if (extra.getString("orientation") != null) {
                String strOr = extra.getString("orientation");
                orientation = strOr;
                if (strOr.startsWith("land")) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (strOr.startsWith("portrait")) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (orientation.equals("sensorLandscape")) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else if (orientation.equals("sensorPortrait")) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }

        super.onActivityCreated(savedInstanceState);
    }

    public void startActivityForResult(BMCPlugin task, Intent intent, int requestCode) {
        runningTask = task;
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (runningTask != null) {
            runningTask.onActivityResult(requestCode, resultCode, data);
            runningTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        manager = BMCManager.getInstance();
        manager.setActivity(null);
        manager.setWebView(null);
        manager.setFragment(this);
        manager.setFWebView(fWebView);
    }

    /**
     * @param assetsFilePath assets에 위치한 JSON 파일 경로를 넣고 JSONObject를 return 받습니다 (ex : "/json/TR0001.json").
     * @return JSONObject
     * @author Jaemin, Park
     */
    public JSONObject getJSONFromAssets(String assetsFilePath) throws JSONException {
        String json = null;
        InputStream inputStream = null;
        try {
            inputStream = getActivity().getAssets().open(assetsFilePath);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        JSONObject jsonObj = new JSONObject();

        jsonObj = new JSONObject(json);


        return jsonObj;
    }


    /**
     * JSONObject로 plugin을 실행하는 메소드
     *
     * @param data     assets/json 형식 참조.
     * @param listener null 가능
     * @author JaeMin, Park
     */
    public void excutePluginWithJSON(JSONObject data, CompleteListener listener) {

        if (data == null) {
            return;
        }

        if (manager == null) {
            manager = BMCManager.getInstance();
        }

        CompleteListener defaultListener = new CompleteListener() {
            @Override
            public void resultCallback(String type, String callback, JSONObject param) {
                //do something
            }
        };

        if (listener != null) {
            defaultListener = listener;
        }
        String callId = "";

        try {
            if (data.has("id")) {
                callId = data.getString("id");
            }
            manager.executeInterfaceFromID(callId, data, defaultListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    @Override
    public void showFragment() {
        super.showFragment();

        onResume();

        if (callbackName != "") {
            callback(callbackName, callbackMessage, "");

            callbackName = "";
            callbackMessage = "";
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {

        super.onHiddenChanged(hidden);

        if (hidden) {
            this.onPause();
        } else {
            Log.v("web frag", "orientation = " + orientation);

            if (fWebView != null) {
                if (fWebView.isLoaded()) {
                    try {
                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (callbackName != "") {
                callback(callbackName, callbackMessage, "");

                callbackName = "";
                callbackMessage = "";
            }
        }
    }

    public void setWebview(BMCWebView fWebView) {
        this.fWebView = fWebView;
    }

    public BMCWebView getWebview() {
        return fWebView;
    }

    private boolean isHome(String url) {

        boolean bResult = false;
        try {
            if (AppConfig.HOME_PAGE_LIST != null) {
                int size = AppConfig.HOME_PAGE_LIST.length();
                for (int i = 0; i < size; i++) {
                    if (AppConfig.HOME_PAGE_LIST.getString(i).contains(url)) {
                        bResult = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bResult;
    }

    @Override
    public void event(final String eventName, final String result) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (showingPopupView) {
                    Logger.d("BMCWebViewClient", "javascript:bizMOBCore.EventManager.responser({eventname:'" + eventName + "'}, {message:" + result + "});");
                    BMCUtil.loadUrlOrEvaluateJavascript(popupWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'" + eventName + "'}, {message:" + result + "});");
                } else {
                    Logger.d("BMCWebViewClient", "javascript:bizMOBCore.EventManager.responser({eventname:'" + eventName + "'}, {message:" + result + "});");
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'" + eventName + "'}, {message:" + result + "});");
                }
            }
        });
    }

    @Override
    public void callback(final String callback, final String result, final String serviceInfo) {
        if (getActivity() != null) {
            if (!getActivity().isFinishing()) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        BMCSlideFragmentActivity bmcSlideFragmentActivity = (BMCSlideFragmentActivity) getActivity();
                        if (callback != null && !callback.equals("")) {
                            WebView callbackWebView = null;

                            if (showingPopupView) {
                                if (popupWebView != null) {
                                    callbackWebView = popupWebView;
                                }
                            } else if (bmcSlideFragmentActivity.leftWebView != null && bmcSlideFragmentActivity.isLeftWebViewOpened) {
                                callbackWebView = bmcSlideFragmentActivity.leftWebView;
                            } else if (bmcSlideFragmentActivity.rightWebView != null && bmcSlideFragmentActivity.isRightWebViewOpened) {
                                callbackWebView = bmcSlideFragmentActivity.rightWebView;
                            } else {
                                if (fWebView != null) {
                                    callbackWebView = fWebView;
                                }
                            }

                            String callbackUrl = "";

                            if (callback.equals("error") || callback.equals("exception")) {
                                callbackUrl = "javascript:bizMOBCore.CallbackManager.responser({callback:'exception'}, {message:{}, error:" + result + "});";
                            } else {
                                if (serviceInfo != null && serviceInfo.length() > 0) {
                                    callbackUrl = "javascript:bizMOBCore.CallbackManager.responser({callback:'" + callback + "'}, {message:" + result + "}, " + serviceInfo + ");";
                                } else {
                                    callbackUrl = "javascript:bizMOBCore.CallbackManager.responser({callback:'" + callback + "'}, {message:" + result + "});";
                                }
                            }

                            Logger.i("BMCWebFragment", "================================== " + "javascript:" + callbackUrl);
                            BMCUtil.loadUrlOrEvaluateJavascript(callbackWebView, callbackUrl);
                        }
                    }
                });
            } else {
                Logger.d(TAG, "Activity does not exist, cannot call rect_sky callback method. Do not press the back button while processing.");
            }
        } else {
            Logger.d(TAG, "Activity does not exist, cannot call rect_sky callback method.");
        }
    }

    public void setApp(JSONObject param) {
    }

    public void setBadge(String id, int count, String callback) throws JSONException {
    }

    public void onCreateContents() {
    }

    public void pageFinished(WebView view, String url) {
    }

    @Override
    public void resume() {

        try {

            if (isFirst) {

                manager = BMCManager.getInstance();
                manager.setActivity(null);
                manager.setWebView(null);
                manager.setFragment(this);
                manager.setFWebView(fWebView);
                if (fWebView != null) {
                    if (fWebView.isLoaded()) {
                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    }
                }

                isFirst = false;

            } else {

                manager = BMCManager.getInstance();
                manager.setActivity(null);
                manager.setWebView(null);
                manager.setFragment(this);
                manager.setFWebView(fWebView);
                if (fWebView != null) {
                    if (fWebView.isLoaded()) {
                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                        Logger.d("event call", "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backPressed() {
        if (backAction) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Logger.d("onBackPressed", "javascript:bizMOBCore.EventManager.responser({eventname:'onBackButton'}, {message:{}});");
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onBackButton'}, {message:{}});");
                }
            });
        } else {
            ((BMCFragmentActivity) getActivity()).backPressed();
        }
    }

    public void setMessage(JSONObject message) {
        this.message = message;
    }

    public JSONObject getMessage() {
        return message;
    }

    public LinearLayout getHeaderLayout() {
        return null;
    }

    public LinearLayout getHeaderLeftLayout() {
        return null;
    }

    public LinearLayout getHeaderTitleLayout() {
        return null;
    }

    public LinearLayout getHeaderRightLayout() {
        return null;
    }

    public boolean getTitleVisible() {
        return false;
    }

    public boolean getTitleHidden() {
        return false;
    }

    public boolean getToolVisible() {
        return false;
    }

    public boolean getToolHidden() {
        return false;
    }

    public void btnEvent(View view) {
    }

    public LinearLayout getToolbarLayout() {
        return null;
    }

    public boolean getIsHomeActivity() {
        return false;
    }

    /**
     * @param htmlFilePath 오픈할 웹 리소스의 경로 (ex. "/contents/LGN/html/LGN0100.html")
     */
    private void openWebFragment(String htmlFilePath) {
        openWebFragment(htmlFilePath, null, null, null);
    }

    /**
     * @param htmlFilePath     오픈할 웹 리소스의 경로 (ex. "/contents/LGN/html/LGN0100.html")
     * @param pageName         고유한 화면 명칭 없을 시 (page+현재 화면 갯수로 임의 지정 됨)
     * @param message          화면 오픈시 전달할 data
     * @param completeListener 화면 오픈 수행 후 결과를 전달 받을 listener
     */
    private void openWebFragment(String htmlFilePath, @Nullable String pageName, @Nullable JSONObject message, @Nullable CompleteListener completeListener) {
        openWebFragment(htmlFilePath, true, true, null, null, message, null, pageName, null, completeListener);
    }

    /**
     * @param htmlFilePath        오픈할 웹 리소스의 경로 (ex. "/contents/LGN/html/LGN0100.html")
     * @param hardwareAccelerator 하드웨어 가속 적용 여부.
     * @param animation           화면 오픈시 애니메이션 적용 여부
     * @param targetPageParam     사용되고 있지 않는 것 같음.
     * @param message             화면 오픈시 전달할 data
     * @param orientation         화면 오픈시 설정 될 화면 방향 (ex. "portrait", "land"(or "landscape")
     * @param pageName            고유한 화면 명칭 없을 시 (page+현재 화면 갯수로 임의 지정 됨)
     * @param effect              애니메이션 방향 및 효과 지정 (ex. "top", " bottom", "right", "left", "fade" , "none") default : "right"
     * @param completeListener    화면 오픈 수행 후 결과를 전달 받을 listener
     */
    private void openWebFragment(String htmlFilePath, boolean hardwareAccelerator, boolean animation, @Nullable String targetPageParam, @Nullable String fixLayer, @Nullable JSONObject message, @Nullable String orientation, @Nullable String pageName, @Nullable String effect, @Nullable CompleteListener completeListener) {

        if (htmlFilePath == null) {
            return;
        }

        try {

            JSONObject data = new JSONObject();
            JSONObject param = new JSONObject();

            data.put("id", "SHOW_WEB");
            data.put("param", param);

            param.put("target_page", htmlFilePath);
            if (message == null) {
                message = new JSONObject();
            }
            param.put("message", message);

            if (orientation == null) {
                orientation = "portrait";
            } else if (orientation.equals("landscape")) {
                orientation = "land";
            } else if (orientation.equals("sensorPortrait")) {
                orientation = "sensorPortrait";
            } else if (orientation.equals("sensorLandscape")) {
                orientation = "sensorLandscape";
            } else {
                if (!orientation.equals("portrait") && !orientation.equals("land") && !orientation.equals("landscape")) {
                    orientation = "portrait";
                }
            }

            param.put("orientation", orientation); // The screen orientation, "portrait" = vertical, "land" = horizontal

            if (pageName == null) {
                pageName = "page" + BMCInit.getFragmentList().size();
            }
            param.put("page_name", pageName); // A unique page name need to be defined.

            param.put("animation", animation); // true or false (to set animation effect or not.)

            if (effect == null) {
                effect = "right";
            } else {
                if (!effect.equals("top") && !effect.equals("bottom") && !effect.equals("right") && !effect.equals("left") && !effect.equals("fade") && !effect.equals("none")) {
                    effect = "right";
                }
            }
            param.put("effect", effect); // The animation effect direction, "top", " bottom", "right", "left", "fade" , "none"


            if (targetPageParam != null) {
                param.put("target_page_param", targetPageParam);
            }

            param.put("hardware_accelator", hardwareAccelerator);

            if (fixLayer != null) {
                param.put("fixlayer", fixLayer);
            }


            manager = BMCManager.getInstance();
            manager.setFragment(this);

            if (completeListener == null) {
                completeListener = new CompleteListener() {

                    @Override
                    public void resultCallback(String type, String callback, JSONObject param) {
                    }
                };
            }
            manager.executeInterfaceFromID("SHOW_WEB", data, completeListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param className NativeFragment의 풀 경로 (ex. "com.mcnc.bizmob.view.ntv.NativeHomeFragment")
     */
    private void openNativeFragment(String className) {
        openNativeFragment(className, null);
    }

    /**
     * @param className NativeFragment의 풀 경로 (ex. "com.mcnc.bizmob.view.ntv.NativeHomeFragment")
     * @param pageName  고유한 화면 명칭 없을 시 (page+현재 화면 갯수로 임의 지정 됨)
     */
    private void openNativeFragment(String className, @Nullable String pageName) {
        openNativeFragment(className, true, null, null, pageName, null, null);
    }

    /**
     * @param className        NativeFragment의 풀 경로 (ex. "com.mcnc.bizmob.view.ntv.NativeHomeFragment")
     * @param animation        화면 오픈시 애니메이션 적용 여부
     * @param message          화면 오픈시 전달할 data
     * @param orientation      화면 오픈시 설정 될 화면 방향 (ex. "portrait", "land"(or "landscape")
     * @param pageName         고유한 화면 명칭 없을 시 (page+현재 화면 갯수로 임의 지정 됨)
     * @param effect           애니메이션 방향 및 효과 지정 (ex. "top", " bottom", "right", "left", "fade" , "none") default : "right"
     * @param completeListener 화면 오픈 수행 후 결과를 전달 받을 listener
     */
    private void openNativeFragment(String className, boolean animation, @Nullable JSONObject message, @Nullable String orientation, @Nullable String pageName, @Nullable String effect, @Nullable CompleteListener completeListener) {

        if (className == null) {
            return;
        }

        try {

            JSONObject data = new JSONObject();
            JSONObject param = new JSONObject();

            data.put("id", "SHOW_NATIVE");
            data.put("param", param);

            param.put("class_name", className); // The specific full class name of it.
            if (message == null) {
                message = new JSONObject();
            }
            param.put("message", message);

            if (orientation == null) {
                orientation = "portrait";
            } else if (orientation.equals("landscape")) {
                orientation = "land";
            } else if (orientation.equals("sensorPortrait")) {
                orientation = "sensorPortrait";
            } else if (orientation.equals("sensorLandscape")) {
                orientation = "sensorLandscape";
            } else {
                if (!orientation.equals("portrait") && !orientation.equals("land") && !orientation.equals("landscape")) {
                    orientation = "portrait";
                }
            }

            param.put("orientation", orientation); // The screen orientation, "portrait" = vertical, "land" = horizontal

            if (pageName == null) {
                pageName = "page" + BMCInit.getFragmentList().size();
            }
            param.put("page_name", pageName); // A unique page name need to be defined.

            param.put("animation", animation); // true or false (to set animation effect or not.)

            if (effect == null) {
                effect = "right";
            } else {
                if (!effect.equals("top") && !effect.equals("bottom") && !effect.equals("right") && !effect.equals("left") && !effect.equals("fade") && !effect.equals("none")) {
                    effect = "right";
                }
            }
            param.put("effect", effect); // The animation effect direction, "top", " bottom", "right", "left", "fade" , "none"

            manager = BMCManager.getInstance();
            manager.setFragment(this);

            if (completeListener == null) {
                completeListener = new CompleteListener() {

                    @Override
                    public void resultCallback(String type, String callback, JSONObject param) {
                    }
                };
            }
            manager.executeInterfaceFromID("SHOW_NATIVE", data, completeListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
