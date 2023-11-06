package com.mcnc.bizmob.manage.ntv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.core.view.fragment.BMCSlideFragmentActivity;
import com.mcnc.bizmob.core.view.fragment.BMCWebFragment;
import com.mcnc.bizmob.receiver.CheckConnectionReceiver;
import com.mcnc.bizmob.view.internal.InternalBrowserFragment;
import com.mcnc.bizmob.view.log.LogViewerActivity;
import com.mcnc.bizmob.view.ntv.BMCNativeFragment;
import com.mcnc.bizmob.view.ntv.NativeLauncherFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NativeSlideFragmentActivity extends BMCSlideFragmentActivity {

    private WebView permissionWebView = null;

    private final String TAG = this.getClass().getName();

    private boolean isDoubleBackPressed = false;

    /**
     * 로그뷰어 메뉴의 root 뷰.
     */
    private RelativeLayout logViewerMenuWrapper;

    /**
     * 로그뷰어 로깅 / 중지 버튼
     */
    private ImageButton btnRecordAndStopLogging;

    /**
     * 로깅 정보 삭제 버튼
     */
    private ImageButton btnClearLog;

    /**
     * 로그뷰어 호출 버튼
     */
    private ImageButton btnShowLogVIewer;

    /**
     * 로그뷰어 메뉴를 보여줄지 여부.
     */
    private boolean isLogViewerMenuOn = false;

    private RelativeLayout rlIntroBg;
    private CheckConnectionReceiver checkConnectionReceiver;

    public CheckConnectionReceiver getCheckConnectionReceiver() {

        if (checkConnectionReceiver == null) {
            checkConnectionReceiver = new CheckConnectionReceiver(this);
        }

        return checkConnectionReceiver;
    }

    public RelativeLayout getRlIntroBg() {
        return rlIntroBg;
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(RUtil.getLayoutR(getApplicationContext(), "activity_slide_fragment"));

        if (!BMCInit.useIntroBg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rlIntroBg = (RelativeLayout) findViewById(RUtil.getIdR(NativeSlideFragmentActivity.this, "rl_intro_bg"));
                    rlIntroBg.setVisibility(View.GONE);
                }
            });
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasPermissions(this, NativeLauncherFragment.requiredPermissions)) {
                permissionWebView = new WebView(this);
                permissionWebView.setBackgroundColor(Color.TRANSPARENT);
                permissionWebView.setWebViewClient(new WebViewClient());
                WebSettings settings = permissionWebView.getSettings();
                settings.setJavaScriptEnabled(true);

                permissionWebView.addJavascriptInterface(this, "Android");
                permissionWebView.loadUrl("file:///android_asset/permission/html/guide02.html");
                addContentView(permissionWebView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                permissionGranted();
            }
        } else {
            permissionGranted();
        }

    }

    @JavascriptInterface
    public void permissionDenied() {
        super.terminate();
    }

    @JavascriptInterface
    public void permissionGranted() {
        if (permissionWebView != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewManager) permissionWebView.getParent()).removeView(permissionWebView);
                    permissionWebView = null;
                }
            });
        }

        // 첫 화면에 올라올 Fragment 정의 합니다.
        NativeLauncherFragment fragment = new NativeLauncherFragment();
        Bundle bundle = new Bundle();
        String pageName = "launcher";
        bundle.putString("orientation", "portrait");
        bundle.putString("page_name", pageName);
        bundle.putString("data", "{}");
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(RUtil.getIdR(getApplicationContext(), "content_frame"), fragment, pageName);
        fragmentTransaction.commit();

        BMCInit.addFragment(fragment);
    }


    //백그라운드에서 포어그라운드로 넘어오는 경우에 호출 되는 resume 로직 처리.
    //화면 이동 간 수행되는 resume 로직은 BCMFramgent의 onHiddenChanged()에 있음.
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (leftWebView != null && isLeftWebViewOpened) {
            try {
                BMCManager manager = BMCManager.getInstance();
                manager.setActivity(null);
                manager.setWebView(null);
                manager.setFWebView(leftWebView);

                if (leftWebView.isLoaded()) {
                    Logger.i("BMCWebFragment", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    Logger.i("BMCWebFragment", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    BMCUtil.loadUrlOrEvaluateJavascript(leftWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    BMCUtil.loadUrlOrEvaluateJavascript(leftWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                    Logger.d("event call", "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                    BMCUtil.loadUrlOrEvaluateJavascript(leftWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (rightWebView != null && isRightWebViewOpened) {
            try {
                BMCManager manager = BMCManager.getInstance();
                manager.setActivity(null);
                manager.setWebView(null);
                manager.setFWebView(rightWebView);

                if (rightWebView.isLoaded()) {
                    Logger.i("BMCWebFragment", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    Logger.i("BMCWebFragment", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    BMCUtil.loadUrlOrEvaluateJavascript(rightWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    BMCUtil.loadUrlOrEvaluateJavascript(rightWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    Logger.d("event call", "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                    BMCUtil.loadUrlOrEvaluateJavascript(rightWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ArrayList<BMCFragment> fragments = BMCInit.getFragmentList();
            if (fragments.size() > 0) {
                BMCFragment bmcFragment = fragments.get(fragments.size() - 1);
                if (bmcFragment instanceof BMCWebFragment) {
                    BMCWebFragment fragment = (BMCWebFragment) bmcFragment;
                    if (fragment.showingPopupView) {
                        if (fragment.popupWebView != null) {
                            BMCUtil.loadUrlOrEvaluateJavascript(fragment.popupWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                            Logger.d(TAG, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                        }
                    } else {
                        fragment.resume();
                    }
                } else if (bmcFragment instanceof InternalBrowserFragment) {
                    InternalBrowserFragment fragment = (InternalBrowserFragment) bmcFragment;
                    if (fragment.showingPopupView) {
                        if (fragment.popupWebView != null) {
                            BMCUtil.loadUrlOrEvaluateJavascript(fragment.popupWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                            Logger.d(TAG, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                        }
                    } else {
                        fragment.resume();
                    }
                } else if (bmcFragment instanceof BMCNativeFragment) {
                    BMCNativeFragment fragment = (BMCNativeFragment) bmcFragment;

                    fragment.onResume();
                } else {
                    Logger.e(TAG, "Please cast a prefer type of class for the bmcFragment, See the line 94 on com.mcnc.bizmob.view.SlideFragmentActivity ");
                }
            }
        }

        setLogViewerMenu();
    }

    private void setLogViewerMenu() {
        if (logViewerMenuWrapper == null) {

            logViewerMenuWrapper = findViewById(RUtil.getIdR(this, "ll_log_viewer_menu_wrapper"));
            btnRecordAndStopLogging = findViewById(RUtil.getIdR(this, "ib_record_and_stop_logging"));

            if (Logger.logging) { //로깅중
                btnRecordAndStopLogging.setBackground(getResources().getDrawable(RUtil.getDrawableR(this, "img_log_btn_stop")));
            } else { //로깅중이 아님.
                btnRecordAndStopLogging.setBackground(getResources().getDrawable(RUtil.getDrawableR(this, "img_log_btn_record")));
            }

            btnRecordAndStopLogging.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Logger.logging = !Logger.logging;
                            if (Logger.logging) { //로깅중
                                btnRecordAndStopLogging.setBackground(getResources().getDrawable(RUtil.getDrawableR(NativeSlideFragmentActivity.this, "img_log_btn_stop")));
                                Toast.makeText(NativeSlideFragmentActivity.this, getString(RUtil.getStringR(NativeSlideFragmentActivity.this, "txt_logging_start")), Toast.LENGTH_SHORT).show();
                            } else {
                                btnRecordAndStopLogging.setBackground(getResources().getDrawable(RUtil.getDrawableR(NativeSlideFragmentActivity.this, "img_log_btn_record")));
                                Toast.makeText(NativeSlideFragmentActivity.this, getString(RUtil.getStringR(NativeSlideFragmentActivity.this, "txt_logging_end")), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            });
            btnClearLog = (ImageButton) findViewById(RUtil.getIdR(NativeSlideFragmentActivity.this, "ib_clear_log"));
            btnClearLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.logBuffer.delete(0, Logger.logBuffer.length());
                    Toast.makeText(NativeSlideFragmentActivity.this, getString(RUtil.getStringR(NativeSlideFragmentActivity.this, "txt_log_delete")), Toast.LENGTH_SHORT).show();
                }
            });
            btnShowLogVIewer = findViewById(RUtil.getIdR(NativeSlideFragmentActivity.this, "ib_show_log_viewer"));
            btnShowLogVIewer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show.
                    Intent intent = new Intent(NativeSlideFragmentActivity.this, LogViewerActivity.class);
                    startActivity(intent);
                }
            });
        }
        isLogViewerMenuOn = BMCManager.getInstance().getSetting().isLogViewerMenuOn();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isLogViewerMenuOn) {
                    logViewerMenuWrapper.setVisibility(View.VISIBLE);
                } else {
                    logViewerMenuWrapper.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (BMCInit.getFragmentList().size() == 0) {
            finish();
            return;
        }

        if (dlLeftView != null && drawerLayout.isDrawerOpen(dlLeftView)) {
            drawerLayout.closeDrawer(dlLeftView);
        } else if (dlRightView != null && drawerLayout.isDrawerOpen(dlRightView)) {
            drawerLayout.closeDrawer(dlRightView);
        } else {

            final ArrayList<BMCFragment> fragments = BMCInit.getFragmentList();
            BMCFragment fragment = fragments.get(fragments.size() - 1);

            if (fragment.showingPopupView) {
                if (fragment.popupBackAction) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Logger.d(TAG, "javascript:bizMOBCore.EventManager.responser({eventname:'onBackButton'}, {message:{}});");
                            BMCUtil.loadUrlOrEvaluateJavascript(fragments.get(fragments.size() - 1).popupWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onBackButton'}, {message:{}});");
                        }
                    });
                } else {
                    backPressed();
                }


            } else {
                if (fragment.backAction) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Logger.d(TAG, "javascript:bizMOBCore.EventManager.responser({eventname:'onBackButton'}, {message:{}});");
                            BMCUtil.loadUrlOrEvaluateJavascript(fragments.get(fragments.size() - 1).fWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onBackButton'}, {message:{}});");
                        }
                    });

                } else {
                    backPressed();
                }

            }
        }
    }


    //2번 누르면 종료 정의
    @Override
    public void backPressed() {
        BMCFragment fragment = BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 1);

        if (fragment.showingPopupView) {
            fragment.closePopupview("", new JSONObject());
        } else {
            if (BMCInit.getFragmentList().size() > 1) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "zoom_exit"))
                        .remove(BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 1))
                        .show(BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 2))
                        .commit();

                BMCManager.getInstance().setFragment(BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 2));
                BMCManager.getInstance().setFWebView(BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 2).fWebView);

                BMCInit.getFragmentList().remove(BMCInit.getFragmentList().size() - 1);

                if (!(BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 1) instanceof BMCNativeFragment)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BMCInit.list.get(BMCInit.list.size() - 1).onHiddenChanged(false);
                        }
                    });
                }

                BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 1).showFragment();
            } else {
                if (isDoubleBackPressed) {
                    terminate();
                    BMCInit.getFragmentList().remove(BMCInit.getFragmentList().size() - 1);
                    return;
                }

                isDoubleBackPressed = true;

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(NativeSlideFragmentActivity.this, "Press back button again to exit", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 50);
                        toast.show();
                    }
                });

                //2초 뒤엔 다시 초기화.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDoubleBackPressed = false;
                    }
                }, 2000);
            }
        }

    }

//    //네트워크 에러시 해당 메소드를 호출함.
//    @Override
//    public void networkError(Context context, JSONObject jsonObject) {
//        if (jsonObject == null) {
//            return;
//        }
//        Logger.e("CommunicatorPlugin", "network Error message= " + jsonObject.toString());
//
//        try {
//            JSONObject headObj = jsonObject.getJSONObject("header");
//
//            String errorCode = headObj.getString("error_code");
//
//            if (errorCode.startsWith("HE0")) { //HttpResponseException (뒤에 숫자는 서버 상태에 따라 다름 , 300 ,404, 500 등);
//
//            } else if (errorCode.equals("NE0001")) { // ConnectTimeoutException
//
//            } else if (errorCode.equals("NE0002")) { // HttpHostConnectException
//
//            } else if (errorCode.equals("NE0003")) { // SocketTimeoutException
//
//            } else if (errorCode.equals("NE0004")) { // IOException
//
//            } else if (errorCode.equals("CE0001")) { // 기타 컨테이너 에러 (NullPointerException)
//
//            } else if (errorCode.equals("CE0002")) { // 기타 컨테이너 에러
//
//            } else {
//
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }


    //Native code
    public void moveNativeView(int index, JSONObject message, JSONObject param) {

        int size = BMCInit.list.size();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        boolean isAnim = true;
        if (param.has("animation")) {
            try {
                isAnim = param.getBoolean("animation");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (param.has("effect")) {
            try {
                String type = param.getString("effect");

                if (type.equals("top")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_top"));
                } else if (type.equals("bottom")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_bottom"));
                } else if (type.equals("right")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_right"));
                } else if (type.equals("left")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_left"));
                } else if (type.equals("fade")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_fade_in"));
                } else if (type.equalsIgnoreCase("none")) {
                    //none일시 이펙트 없음
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (isAnim) {
                fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "zoom_exit"));
            }
        }
        if (Math.abs(index) > size) {

            while (BMCInit.list.size() > 1) {

                fragmentTransaction.remove(BMCInit.list.get(BMCInit.list.size() - 1));
                BMCInit.list.remove(BMCInit.list.size() - 1);
            }

            fragmentTransaction.commit();
            BMCFragment bmcFragment = BMCInit.list.get(0);

            if (bmcFragment instanceof BMCNativeFragment) {
                ((BMCNativeFragment) bmcFragment).setMessage(message);
            }
            BMCInit.list.get(0).showFragment();
        } else {

            int depth = size + index;

            while (BMCInit.list.size() > depth) {

                fragmentTransaction.remove(BMCInit.list.get(BMCInit.list.size() - 1));
                BMCInit.list.remove(BMCInit.list.size() - 1);
            }
            fragmentTransaction.show(BMCInit.list.get(depth - 1));
            fragmentTransaction.commit();

            BMCFragment bmcFragment = BMCInit.list.get(BMCInit.list.size() - 1);

            if (bmcFragment instanceof BMCNativeFragment) {
                ((BMCNativeFragment) bmcFragment).setMessage(message);
            }

            BMCInit.list.get(depth - 1).showFragment();
        }
    }

    public void moveNativeView(String pageName, JSONObject message, JSONObject param) {

        int depth = -1;
        int size = BMCInit.list.size();

        for (int i = size; i > 0; i--) {

            String currentPageName = BMCInit.list.get(i - 1).pageName;

            if (pageName.equals(currentPageName)) {
                depth = i;
                break;
            }
        }

        if (depth > -1) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            boolean isAnim = true;
            if (param.has("animation")) {
                try {
                    isAnim = param.getBoolean("animation");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (param.has("effect")) {
                try {
                    String type = param.getString("effect");

                    if (type.equals("top")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_top"));
                    } else if (type.equals("bottom")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_bottom"));
                    } else if (type.equals("right")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_right"));
                    } else if (type.equals("left")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_left"));
                    } else if (type.equals("fade")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_fade_in"));
                    } else if (type.equalsIgnoreCase("none")) {
                        //none일시 이펙트 없음
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if (isAnim) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "zoom_exit"));
                }
            }

            while (BMCInit.list.size() > depth) {

                fragmentTransaction.remove(BMCInit.list.get(BMCInit.list.size() - 1));
                BMCInit.list.remove(BMCInit.list.size() - 1);
            }
            fragmentTransaction.show(BMCInit.list.get(depth - 1));
            fragmentTransaction.commit();

            BMCFragment bmcFragment = BMCInit.list.get(BMCInit.list.size() - 1);
            if (bmcFragment instanceof BMCNativeFragment) {
                ((BMCNativeFragment) bmcFragment).setMessage(message);
            }

            BMCInit.list.get(depth - 1).showFragment();
        } else {
            //못찾은 경우.
        }
    }

    public void closeNativeView(JSONObject param) {
        String callback = "";
        String message = "";

        try {

            if (param.has("callback")) {
                callback = param.getString("callback");
            }
            if (param.has("message")) {
                message = param.getJSONObject("message").toString();
            }

            boolean isAnim = true;
            if (param.has("animation")) {
                try {
                    isAnim = param.getBoolean("animation");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            if (param.has("effect")) {
                try {
                    String type = param.getString("effect");

                    if (type.equals("top")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_top"));
                    } else if (type.equals("bottom")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_bottom"));
                    } else if (type.equals("right")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_right"));
                    } else if (type.equals("left")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_slide_out_left"));
                    } else if (type.equals("fade")) {
                        fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "anim_fade_in"));
                    } else if (type.equalsIgnoreCase("none")) {
                        //none 일시 이펙트 없음
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if (isAnim) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "hold"), RUtil.getR(getApplicationContext(), "anim", "zoom_exit"));
                }
            }

            if (BMCInit.list.size() > 1) {
                fragmentTransaction.remove(BMCInit.list.get(BMCInit.list.size() - 1))
                        .show(BMCInit.list.get(BMCInit.list.size() - 2))
                        .commit();

                BMCManager.getInstance().setFragment(BMCInit.list.get(BMCInit.list.size() - 2));
                BMCManager.getInstance().setFWebView(BMCInit.list.get(BMCInit.list.size() - 2).fWebView);

                BMCInit.list.get(BMCInit.list.size() - 2).setCallback(callback, message);
                BMCInit.list.remove(BMCInit.list.size() - 1);

                BMCInit.list.get(BMCInit.list.size() - 1).showFragment();

            } else {
                terminate();
                BMCInit.list.remove(BMCInit.list.size() - 1);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void openNativeView(BMCFragment fragment, JSONObject param) throws JSONException {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        boolean isAnim = true;
        if (param.has("animation")) {
            try {
                isAnim = param.getBoolean("animation");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (param.has("effect")) {
            try {
                String type = param.getString("effect");

                if (type.equals("top")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "anim_slide_in_top"), RUtil.getR(getApplicationContext(), "anim", "hold"));
                } else if (type.equals("bottom")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "anim_slide_in_bottom"), RUtil.getR(getApplicationContext(), "anim", "hold"));
                } else if (type.equals("right")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "anim_slide_in_right"), RUtil.getR(getApplicationContext(), "anim", "hold"));
                } else if (type.equals("left")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "anim_slide_in_left"), RUtil.getR(getApplicationContext(), "anim", "hold"));
                } else if (type.equals("fade")) {
                    fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "anim_fade_in"), RUtil.getR(getApplicationContext(), "anim", "hold"));
                } else if (type.equalsIgnoreCase("none")) {
                    //애니메이션 효과 없음.
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (isAnim) {
                fragmentTransaction.setCustomAnimations(RUtil.getR(getApplicationContext(), "anim", "push_left_in"), RUtil.getR(getApplicationContext(), "anim", "hold"));
            }
        }


        int viewCount = BMCInit.list.size() - 1;
        if (AppConfig.MAX_ACTIVITY_STACK_SIZE < viewCount) {
            for (int i = 0; i < viewCount; i++) {
                if (BMCInit.list.get(i).isHomeFragment) {
                    fragmentTransaction.remove(BMCInit.list.get(i + 1));
                    BMCInit.list.remove(i + 1);
                    break;
                }
            }
        }
        String pageName = "";
        if (param.has("page_name")) {
            pageName = param.getString("page_name");
        }
        fragmentTransaction.hide(BMCInit.list.get(BMCInit.list.size() - 1));
        fragmentTransaction.add(RUtil.getIdR(this, "content_frame"), fragment, pageName);
        fragmentTransaction.commitAllowingStateLoss();
        BMCInit.list.add(fragment);
    }

    @Override
    public boolean dispatchTouchEvent(Window.Callback cb, MotionEvent event) {
        if (BMCInit.list.size() > 0) {
            return super.dispatchTouchEvent(cb, event);
        } else {
            return cb.dispatchTouchEvent(event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (checkConnectionReceiver != null) {
            unregisterReceiver(checkConnectionReceiver);
        }
    }

}
