package com.mcnc.bizmob.view.internal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window.Callback;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mcnc.bizmob.core.def.ActivityRequestCode;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.ImageWrapper;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.core.view.fragment.BMCFragmentActivity;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class InternalBrowserFragment extends BMCFragment implements OnClickListener {

    private final String TAG = "InternalBrowserFragment";

    public boolean modal;
    private BMCManager manager;

    public View wrapper;

    private LinearLayout headerLayout, toolbarLayout;

    private boolean isFirst = false;
    private int popupCount = 0;

    private LinearLayout mainView;
    private ArrayList<WebView> childList;

    private BMCProgressWebChromeClient1 webChromeClient;

    private float yPosition = 0f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        wrapper = inflater.inflate(RUtil.getLayoutR(getActivity(), "activity_internal_web_browser"), null);
        return wrapper;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        wrapper.setClickable(true);

        mainView = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "main_view"));
        fWebView = new BMCWebView(this);

        ProgressBar pb = (ProgressBar) wrapper.findViewById(RUtil.getIdR(getActivity(), "progress"));

        fWebView.getSettings().setSupportMultipleWindows(true);
        fWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        fWebView.setWebViewClient(new BMCProgressWebViewClient(this, fWebView, pb));
        webChromeClient = new BMCProgressWebChromeClient1(getActivity(), fWebView, pb);
        fWebView.setWebChromeClient(webChromeClient);

        fWebView.setOnScrollChangedCallback(new BMCWebView.OnScrollChangedCallback() {

            @Override
            public void onScroll(int l, int t) {

                float newY = fWebView.getScrollY();
                float diff = 0f;

                if (newY >= yPosition) {
                    diff = newY - yPosition;
                } else {
                    diff = yPosition - newY;
                }

                if (newY >= yPosition) {

                    if (headerLayout.getVisibility() == View.VISIBLE && diff > 150) {

                        headerLayout.setVisibility(View.GONE);
                        toolbarLayout.setVisibility(View.GONE);
                    }

                } else {

                    if (headerLayout.getVisibility() == View.GONE && diff > 150) {

                        headerLayout.setVisibility(View.VISIBLE);
                        toolbarLayout.setVisibility(View.VISIBLE);

                    } else if (newY == 0) {

                        headerLayout.setVisibility(View.VISIBLE);
                        toolbarLayout.setVisibility(View.VISIBLE);
                    }
                }
                yPosition = newY;
            }
        });

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mainView.addView(fWebView, lp);

        Bundle extra = getArguments();

        if (extra != null) {


            String targetPage = extra.getString("url");

            if (!targetPage.startsWith("http://")) {
                targetPage = "http://" + targetPage;
            }

            fWebView.loadUrl(targetPage);

            setBody();

            if (extra.getString("type").equals("basic")) {

                String text = extra.getString("raw");
                String textColor = extra.getString("text_color");
                String bgImage = extra.getString("title_bg_image");
                String bgColor = extra.getString("title_bg_color");
                String closeImage = extra.getString("close_image");

                setHeader(text, textColor, bgImage, bgColor, closeImage);

                String toolBgImage = extra.getString("tool_bg_image");
                String toolBgColor = extra.getString("tool_bg_color");
                String backImage = extra.getString("historyback_image");
                String forwardImage = extra.getString("historyforward_image");
                String refreshImage = extra.getString("refresh_image");

                setBottom(toolBgImage, toolBgColor, backImage, forwardImage, refreshImage);

            } else if (extra.getString("type").equals("search")) {

                String bgImage = extra.getString("search_bg_image");
                String bgColor = extra.getString("search_bg_color");
                String closeImage = extra.getString("close_image");
                String backImage = extra.getString("historyback_image");
                String forwardImage = extra.getString("historyforward_image");

                setSearch(bgImage, bgColor, closeImage, backImage, forwardImage);
                toolbarLayout = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "bottom_view"));
                toolbarLayout.setVisibility(View.GONE);
            }

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

    @Override
    public void onResume() {
        super.onResume();

        manager = BMCManager.getInstance();
        manager.setActivity(null);
        manager.setWebView(null);
        manager.setFragment(this);
        manager.setFWebView(fWebView);
    }

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

    @Override
    public void resume() {

        try {

            if (isFirst) {

                manager = BMCManager.getInstance();
                manager.setActivity(null);
                manager.setWebView(null);
                manager.setFragment(this);
                manager.setFWebView(fWebView);

                if (fWebView.isLoaded()) {
                    Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                }

                isFirst = false;
            } else {
                manager = BMCManager.getInstance();
                manager.setActivity(null);
                manager.setWebView(null);
                manager.setFragment(this);
                manager.setFWebView(fWebView);

                if (fWebView.isLoaded()) {
                    Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                    Logger.d("event call", "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showFragment() {
        super.showFragment();

        if (fWebView.isLoaded()) {

            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    try {

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(fWebView.getWindowToken(), 0);

                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        Logger.i("BMCActivity", "================================== " + "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));
                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(manager.getMStorage()).toString());
                        BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(manager.getFStorage()));

                        //onResume 두번 작동 하는 이유로 주석처리함, BMCFragmentActivity에서 FragmentTransaction.hide()를 사용함으로써,
                        //onHiddenChanged()메소드를 호출하게 되었는데 이 안에서도 아래 주석 2줄에 해당하는 onResume 호출을 하고 있어 두번 중복
                        //호출 하는 문제가 발생하였음,, 허나 그전까지는 hide()를 사용하지 않아 onHiddenChanged()를 호출한 일이 없을 거라 사료됨.
//						fWebView.loadUrl("javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
//						Logger.d("event call", "javascript:bizMOBCore.EventManager.responser({eventname:'onResume'}, {message:{}});");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

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

        } else {
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
                        if (callback != null && !callback.equals("")) {
                            WebView callbackWebView = null;

                            if (showingPopupView) {
                                if (popupWebView != null) {
                                    callbackWebView = popupWebView;
                                }
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


    public void setHeader(String text, String textColor, String bgImage, String bgColor, String closeImage) {

        headerLayout = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "header_view"));

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(RUtil.getLayoutR(getActivity(), "view_internal_web_browser_header"), null);
        setBackgroundImage(headerLayout, bgImage);

        Button close = (Button) v.findViewById(RUtil.getIdR(getActivity(), "close_button"));
        close.setOnClickListener(this);
        setBackgroundImage(close, closeImage);

        TextView title = (TextView) v.findViewById(RUtil.getIdR(getActivity(), "title"));
        title.setTextColor((int) Long.parseLong(textColor, 16));
        title.setText(text);

        headerLayout.addView(v);
    }

    public void setSearch(String bgImage, String bgColor, String closeImage, String backImage, String forwardImage) {

        headerLayout = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "header_view"));

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(RUtil.getLayoutR(getActivity(), "view_internal_web_browser_search"), null);
        setBackgroundImage(headerLayout, bgImage);

        final Drawable refreshImg = getResources().getDrawable(RUtil.getDrawableR(getActivity(), "internal_web_browser_btn_refresh"));

        final EditText et = (EditText) v.findViewById(RUtil.getIdR(getActivity(), "search"));
        et.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                    case EditorInfo.IME_ACTION_GO:

                        String url = et.getText().toString();

                        if (!url.startsWith("http://")) {
                            url = "http://" + url;
                        }

                        fWebView.loadUrl(url);

                        break;

                    default:
                        break;
                }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                return false;
            }
        });

        et.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (et.getCompoundDrawables()[2] == null)
                    return false;

                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;

                if (event.getX() > et.getWidth() - et.getPaddingRight() - refreshImg.getIntrinsicWidth())
                    fWebView.reload();

                return false;
            }
        });

		/*Button search = (Button) v.findViewById(RUtil.getIdR(getActivity(), "search_button"));
        search.setOnClickListener(this);*/

        Button close = (Button) v.findViewById(RUtil.getIdR(getActivity(), "close_button"));
        close.setOnClickListener(this);
        setBackgroundImage(close, closeImage);

        Button back = (Button) v.findViewById(RUtil.getIdR(getActivity(), "back_button"));
        back.setOnClickListener(this);
        setBackgroundImage(back, backImage);

        Button forward = (Button) v.findViewById(RUtil.getIdR(getActivity(), "forward_button"));
        forward.setOnClickListener(this);
        setBackgroundImage(forward, forwardImage);

        headerLayout.addView(v);
    }

    public void setBody() {

//		fWebView.setBackgroundColor(AppConfig.WEBVIEW_BACKGROUNDCOLOR_NORMAL);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            fWebView.setBackgroundColor(Color.argb(1, 0, 0, 0));
        }
    }

    public void setBottom(String toolBgImage, String toolBgColor, String backImage, String forwardImage, String refreshImage) {

        toolbarLayout = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "bottom_view"));

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(RUtil.getLayoutR(getActivity(), "view_internal_web_browser_bottom"), null);
        setBackgroundImage(toolbarLayout, toolBgImage);

        Button back = (Button) v.findViewById(RUtil.getIdR(getActivity(), "back_button"));
        back.setOnClickListener(this);
        setBackgroundImage(back, backImage);

        Button forward = (Button) v.findViewById(RUtil.getIdR(getActivity(), "forward_button"));
        forward.setOnClickListener(this);
        setBackgroundImage(forward, forwardImage);

        Button refresh = (Button) v.findViewById(RUtil.getIdR(getActivity(), "refresh_button"));
        refresh.setOnClickListener(this);
        setBackgroundImage(refresh, refreshImage);

        toolbarLayout.addView(v);
    }

    private void setBackgroundImage(View v, String image) {

        try {
            if (Build.VERSION.SDK_INT >= 16) {
                v.setBackground(ImageWrapper.getDrawable(image, getActivity()));
            } else {
                v.setBackgroundDrawable(ImageWrapper.getDrawable(image, getActivity()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == RUtil.getIdR(getActivity(), "close_button")) {

            ((BMCFragmentActivity) getActivity()).backPressed();

        } else if (v.getId() == RUtil.getIdR(getActivity(), "back_button")) {

            if (fWebView.canGoBack()) {
                fWebView.goBack();
            }

        } else if (v.getId() == RUtil.getIdR(getActivity(), "forward_button")) {

            if (fWebView.canGoForward()) {
                fWebView.goForward();
            }

        } else if (v.getId() == RUtil.getIdR(getActivity(), "refresh_button")) {

            fWebView.reload();

        } else {

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private Bitmap drawableToBitmap(Drawable d) {

        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        } else {

            Bitmap bm = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.draw(canvas);

            return bm;
        }
    }

    public String getOrientation() {
        return orientation;
    }

    private class CustomImageView extends ImageView {

        public CustomImageView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            setScaleType(ScaleType.MATRIX);

            Drawable d = getDrawable();

            if (d != null) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
                setMeasuredDimension(width, height);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    public void showPopupview(final String targetPage, final int width, final int height, final JSONObject data) {

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                showingPopupView = true;

                LinearLayout main_view = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "main_view"));
                main_view.setClickable(false);

//                ScrollView sc = (ScrollView) wrapper.findViewById(RUtil.getIdR(getActivity(), "pop_scroll"));
                RelativeLayout rl = (RelativeLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "popup"));

                if (!targetPage.equals("")) {

                    popupWebView = new BMCWebView(InternalBrowserFragment.this);

                    try {
                        if (data.has("hardware_accelator")) {

                            if (data.getBoolean("hardware_accelator")) {
                                popupWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                            } else {
                                popupWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                            }
                            data.remove("hardware_accelator");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    LayoutParams lp = new LayoutParams(width, height);
                    rl.addView(popupWebView, lp);

                    getArguments().putString("data", data.toString());

                    String url = ImageWrapper.getUri(targetPage);
                    popupWebView.loadUrl(url);
                }

                rl.bringToFront();
                rl.setGravity(Gravity.CENTER);
                rl.setVisibility(View.VISIBLE);
                rl.setClickable(true);

//                sc.setVisibility(View.VISIBLE);
            }
        });
    }

    public void closePopupview(final String callback, final JSONObject data) {

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                showingPopupView = false;

                LinearLayout main_view = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "main_view"));
                main_view.setClickable(true);

//                ScrollView sc = (ScrollView) wrapper.findViewById(RUtil.getIdR(getActivity(), "pop_scroll"));
                RelativeLayout rl = (RelativeLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "popup"));
                rl.setVisibility(View.GONE);
                popupWebView = null;
                rl.removeAllViews();
//                sc.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(rl.getWindowToken(), 0);

                if (fWebView != null && !callback.equals("")) {
                    Logger.d("event call", "javascript:bizMOBCore.CallbackManager.responser({callback:'" + callback + "'}, {message:" + data.toString() + "});");
                    BMCUtil.loadUrlOrEvaluateJavascript(fWebView, "javascript:bizMOBCore.CallbackManager.responser({callback:'" + callback + "'}, {message:" + data.toString() + "});");
                }
            }
        });
    }

    @Override
    public void setBadge(String s, int i, String s1) throws JSONException {

    }

    @Override
    public void onCreateContents() {
    }

    @Override
    public boolean dispatchTouchEvent(Callback cb, MotionEvent event) {
        return cb.dispatchTouchEvent(event);
    }

    @Override
    public void pageFinished(WebView view, String url) {
    }

    @Override
    public void backPressed() {

        if (popupCount > 0) {

            if (childList.get(popupCount - 1).canGoBack()) {
                childList.get(popupCount - 1).goBack();
            } else {
                webChromeClient.onCloseWindow(childList.get(popupCount - 1));
            }

        } else {

            if (fWebView.canGoBack()) {
                fWebView.goBack();
            } else {
                ((BMCFragmentActivity) getActivity()).backPressed();
            }
        }

    }

    class BMCProgressWebChromeClient1 extends WebChromeClient {

        private long MAX_QUOTA = 100 * 1024 * 1024;
        private BMCWebView webView;
        private Context context;

        // the video progress view
        private View mVideoProgressView;

        // File Chooser
        public ValueCallback<Uri> mUploadMessage;

        private ProgressBar progressBar;

        private View mCustomView;
        private Activity mActivity;

        private int mOriginalOrientation;
        private FullscreenHolder mFullscreenContainer;
        private CustomViewCallback mCustomViewCallback;

        public BMCProgressWebChromeClient1(Context context, BMCWebView webView) {
            this.context = context;
            this.webView = webView;
        }

        public BMCProgressWebChromeClient1(Context context, BMCWebView webView, ProgressBar progressBar) {
            this.context = context;
            this.webView = webView;
            this.progressBar = progressBar;
        }

        public void setWebView(Context context, BMCWebView webView) {
            this.context = context;
            this.webView = webView;
        }

        /**
         * Tell the client to display a javascript alert dialog.
         *
         * @param view
         * @param url
         * @param message
         * @param result
         */
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setMessage(message);
            dlg.setTitle("Alert");
            //Don't let alerts break the back button
            dlg.setCancelable(true);
            dlg.setPositiveButton(android.R.string.ok,
                    new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    });
            dlg.setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            result.cancel();
                        }
                    });
            dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
                //DO NOTHING
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        result.confirm();
                        return false;
                    } else
                        return true;
                }
            });
            dlg.create();
            dlg.show();
            return true;
        }

        /**
         * Tell the client to display a confirm dialog to the user.
         *
         * @param view
         * @param url
         * @param message
         * @param result
         */
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setMessage(message);
            dlg.setTitle("Confirm");
            dlg.setCancelable(true);
            dlg.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    });
            dlg.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    });
            dlg.setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            result.cancel();
                        }
                    });
            dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
                //DO NOTHING
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        result.cancel();
                        return false;
                    } else
                        return true;
                }
            });
            dlg.create();
            dlg.show();
            return true;
        }

        /**
         * Tell the client to display a prompt dialog to the user.
         * If the client returns true, WebView will assume that the client will
         * handle the prompt dialog and call the appropriate JsPromptResult method.
         * <p>
         * Since we are hacking prompts for our own purposes, we should not be using them for
         * this purpose, perhaps we should hack console.log to do this instead!
         *
         * @param view
         * @param url
         * @param message
         * @param defaultValue
         * @param result
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Logger.f("", consoleMessage.message() + " -- From line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
            return super.onConsoleMessage(consoleMessage);
        }

        /**
         * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified origin.
         *
         * @param origin
         * @param callback
         */
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }

        @Override
        /**
         * Ask the host application for a custom progress view to show while
         * a <video> is loading.
         * @return View The progress view.
         */
        public View getVideoLoadingProgressView() {

            if (mVideoProgressView == null) {
                // Create a new Loading view programmatically.

                // create the linear layout
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                layout.setLayoutParams(layoutParams);
                // the proress bar
                ProgressBar bar = new ProgressBar(context);
                LinearLayout.LayoutParams barLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                barLayoutParams.gravity = Gravity.CENTER;
                bar.setLayoutParams(barLayoutParams);
                layout.addView(bar);

                mVideoProgressView = layout;
            }
            return mVideoProgressView;
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            this.openFileChooser(uploadMsg, "*/*");
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            this.openFileChooser(uploadMsg, acceptType, null);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            ((Activity) context).startActivityForResult(Intent.createChooser(i, "File Browser"),
                    ActivityRequestCode.FILECHOOSER_RESULTCODE);
        }

        public ValueCallback<Uri> getValueCallback() {
            return this.mUploadMessage;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mOriginalOrientation = mActivity.getRequestedOrientation();

            FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();

            mFullscreenContainer = new FullscreenHolder(mActivity);
            mFullscreenContainer.addView(view, LayoutParams.MATCH_PARENT);
            decor.addView(mFullscreenContainer, LayoutParams.MATCH_PARENT);
            mCustomView = view;
            mCustomViewCallback = callback;
            mActivity.setRequestedOrientation(mOriginalOrientation);

        }

        @Override
        public void onHideCustomView() {

            if (mCustomView == null) {
                return;
            }

            FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
            decor.removeView(mFullscreenContainer);
            mFullscreenContainer = null;
            mCustomView = null;
            mCustomViewCallback.onCustomViewHidden();

            mActivity.setRequestedOrientation(mOriginalOrientation);

        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {

            if (childList == null) {
                childList = new ArrayList<WebView>();
            }

//	    	BMCWebView2 child = new BMCWebView2(InternalBrowserFragment.this);
            WebView child = new WebView(getActivity());
            child.getSettings().setJavaScriptEnabled(true);
            child.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            child.getSettings().setDomStorageEnabled(true);
            child.getSettings().setSupportMultipleWindows(true);

            child.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            child.setWebViewClient(new setWebViewClient());
            child.setWebChromeClient(this);

            FrameLayout wrapperView = (FrameLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "wrapper"));
//            wrapperView.addView(child);

            LinearLayout lay = new LinearLayout(getActivity());
            lay.setLayoutParams(new LayoutParams(800, 1000));
            lay.addView(child);

            wrapperView.addView(lay);


            LinearLayout parent = (LinearLayout) wrapper.findViewById(RUtil.getIdR(getActivity(), "parent"));
            parent.setClickable(false);
            lay.bringToFront();

            WebView.WebViewTransport transport;
            transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(child);
            resultMsg.sendToTarget();

            popupCount++;
            childList.add(child);


            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {

            if (window != null && window.getUrl() != null) {

                window.clearAnimation();
                window.clearHistory();
                window.clearView();
                window.destroyDrawingCache();

                window.removeAllViews();
                ViewGroup viewGroup = (ViewGroup) window.getParent();
                viewGroup.removeView(window);

                window.destroy();
                window = null;
            }

            popupCount--;
            childList.remove(popupCount);

            super.onCloseWindow(window);
        }
    }

    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    class setWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.startsWith("file://")) {
                return super.shouldOverrideUrlLoading(view, url);
            } else {
                view.loadUrl(url);
            }
            return true;
        }
    }

    @Override
    public void setApp(JSONObject param) {
    }

}
