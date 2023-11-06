package com.mcnc.bizmob.manage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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

import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.core.view.fragment.BMCSlideFragmentActivity;
import com.mcnc.bizmob.core.view.fragment.BMCWebFragment;
import com.mcnc.bizmob.receiver.CheckConnectionReceiver;
import com.mcnc.bizmob.view.LauncherFragment;
import com.mcnc.bizmob.view.internal.InternalBrowserFragment;
import com.mcnc.bizmob.view.log.LogViewerActivity;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * 01.클래스 설명 : App의 Launcher Activity로 BCMSlideFragmentActivity를 상속 받는 FragementActivity 클래스. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : LauncherFragment 초기화, 백버튼 제어, Fragment Resume 제어 <br>
 * 04.관련 API/화면/서비스 : Logger, BMCManager, RUtil, BMCFragment, BMCInit, BMCSlideFragmentActivity, BMCWebFragment, LauncherFragment<br>
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
public class SlideFragmentActivity extends BMCSlideFragmentActivity {


    private WebView permissionWebView = null;

    /**
     * Left SlidingMenu webview의 init 여부.
     */
    public boolean leftInit = false;
    /**
     * Left SlidingMenu 화면의 존재 여부.
     */
    public boolean hasLeft = false;
    /**
     * Right SlidingMenu webview의 init 여부.
     */
    public boolean rightInit = false;
    /**
     * Right SlidingMenu 화면의 존재 여부.
     */
    public boolean hasRight = false;
    /**
     * center webview의 init 여부.
     */
    public boolean centerInit = false;
    /**
     * class 명
     */
    private final String TAG = "SlideFragmentActivity";

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

    private CheckConnectionReceiver checkConnectionReceiver;

    private RelativeLayout rlIntroBg;

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

    /**
     * FragmentActivity class의 Life Cycle 중 하나에 속하는 메소드로 아래와 같은 기능을 수행함.<br>
     * 1.타이틀 제거. <br>
     * 2.Thread 정책 설정. <br>
     * 3.LauncherFragment(초기 진입화면) 초기화 및 Show <br>
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
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setDefaultLocaleList = true;

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // set the Above View
        setContentView(RUtil.getLayoutR(getApplicationContext(), "activity_slide_fragment"));



        if (Build.VERSION.SDK_INT >= 23) {

            if(Build.VERSION.SDK_INT > 29) {
                LauncherFragment.requiredPermissions = new String[]{
                        Manifest.permission.READ_PHONE_NUMBERS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }

            if (!hasPermissions(this, LauncherFragment.requiredPermissions)) {
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

        if (!Init.useIntroBg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rlIntroBg = (RelativeLayout) findViewById(RUtil.getIdR(SlideFragmentActivity.this, "rl_intro_bg"));
                    rlIntroBg.setVisibility(View.GONE);
                }
            });
        }

        Intent intent = getIntent();
        LauncherFragment fragment = new LauncherFragment();

        Bundle bundle = new Bundle();
        bundle.putString("orientation", "portrait");
        bundle.putString("page_name", "index");
        bundle.putString("callback", "onReady");
        bundle.putString("data", "");
        bundle.putString("dataKey", "");
        bundle.putString("external_callback", intent.getStringExtra("external_callback"));
        bundle.putString("external_data", intent.getStringExtra("external_data"));
        fragment.setArguments(bundle);


        getSupportFragmentManager()
                .beginTransaction()
                .replace(RUtil.getIdR(getApplicationContext(), "content_frame"), fragment, "center")
                .commit();

        BMCInit.addFragment(fragment);
    }

    public CheckConnectionReceiver getCheckConnectionReceiver() {

        if (checkConnectionReceiver == null) {
            checkConnectionReceiver = new CheckConnectionReceiver(this);
        }

        return checkConnectionReceiver;
    }

    /**
     * 로그 뷰어 메뉴를 접근 가능하게 설정 해주는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    public void setLogViewerMenu() {
        if (logViewerMenuWrapper == null) {

            logViewerMenuWrapper = (RelativeLayout) findViewById(RUtil.getIdR(this, "ll_log_viewer_menu_wrapper"));
            btnRecordAndStopLogging = (ImageButton) findViewById(RUtil.getIdR(this, "ib_record_and_stop_logging"));

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
                                btnRecordAndStopLogging.setBackground(getResources().getDrawable(RUtil.getDrawableR(SlideFragmentActivity.this, "img_log_btn_stop")));
                                Toast.makeText(SlideFragmentActivity.this, getString(RUtil.getStringR(SlideFragmentActivity.this, "txt_logging_start")), Toast.LENGTH_SHORT).show();
                            } else {
                                btnRecordAndStopLogging.setBackground(getResources().getDrawable(RUtil.getDrawableR(SlideFragmentActivity.this, "img_log_btn_record")));
                                Toast.makeText(SlideFragmentActivity.this, getString(RUtil.getStringR(SlideFragmentActivity.this, "txt_logging_end")), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            });
            btnClearLog = (ImageButton) findViewById(RUtil.getIdR(SlideFragmentActivity.this, "ib_clear_log"));
            btnClearLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.logBuffer.delete(0, Logger.logBuffer.length());
                    Toast.makeText(SlideFragmentActivity.this, getString(RUtil.getStringR(SlideFragmentActivity.this, "txt_log_delete")), Toast.LENGTH_SHORT).show();
                }
            });
            btnShowLogVIewer = (ImageButton) findViewById(RUtil.getIdR(SlideFragmentActivity.this, "ib_show_log_viewer"));
            btnShowLogVIewer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show.
                    Intent intent = new Intent(SlideFragmentActivity.this, LogViewerActivity.class);
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

    /**
     * 백그라운드에서 포어그라운드로 넘어오는 경우에 호출 되는 resume 로직 처리. <br>
     * 화면 이동 간 수행되는 resume 로직은 BCMFramgent의 onHiddenChanged()에 있음 <br>
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
    protected void onResumeFragments() {
        super.onResumeFragments();
        Logger.d(TAG, "onResumeFragments() called");
        if (leftWebView != null && isLeftWebViewOpened) {
            try {
                BMCManager manager = BMCManager.getInstance();
                manager.setActivity(null);
                manager.setWebView(null);
                manager.setFWebView(leftWebView);

                if (leftWebView.isLoaded()) {
                    Logger.i(TAG, "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    Logger.i(TAG, "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    BMCUtil.loadUrlOrEvaluateJavascript(leftWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    BMCUtil.loadUrlOrEvaluateJavascript(leftWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                    Logger.d(TAG, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
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
                    Logger.i(TAG, "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    Logger.i(TAG, "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    BMCUtil.loadUrlOrEvaluateJavascript(rightWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    BMCUtil.loadUrlOrEvaluateJavascript(rightWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                    Logger.d(TAG, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
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
                } else {
                    Logger.e(TAG, "Please cast a prefer type of class for the bmcFragment, See the line 94 on com.mcnc.bizmob.view.SlideFragmentActivity ");
                }
            }

        }
        setLogViewerMenu();
    }


    /**
     * Hardware Back button 이벤트 발생시 제어 여부에 따라 제어권을 웹으로 넘기거나 기본 기능 백버튼 기능을 동작하게 하는 메소드.<br>
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
    public void onBackPressed() {
        Logger.d(TAG, "onBackPressed() called");

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

            //공지사항 체크 로직 추가. (REQ_MP_0012)
            if (fragment.isHomeFragment) {
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
                        super.onBackPressed();
                        try {
                            LauncherFragment launcherFragment = (LauncherFragment) fragment;
                            launcherFragment.loadAppconfig();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                        super.onBackPressed();
                    }
                }

            } else {

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
                        super.onBackPressed();
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
                        super.onBackPressed();
                    }

                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(Window.Callback cb, MotionEvent event) {
        Logger.d(TAG, "dispatchTouchEvent() called");
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