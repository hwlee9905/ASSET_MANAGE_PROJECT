package com.mcnc.bizmob.view.popup;

import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

import org.json.JSONObject;

public class SlidePopupView {
    private static SlidePopupView sideViewInstance;
    private BMCWebView leftView;
    private BMCWebView rightView;
    private BMCWebView topView;
    private BMCWebView bottomView;
    private BMCWebView centerView;

    private SlidePopupView() {
    }

    // 클래스의 인스턴스를 만들어서 리턴해 준다.
    public static SlidePopupView getInstance() {
        if (sideViewInstance == null) {
            sideViewInstance = new SlidePopupView();
        }
        return sideViewInstance;
    }

    public BMCWebView getSlideView(String position, String uri, BMCFragment fragment, JSONObject data) {
        BMCWebView webview = null;
        String webUrl = "";
        try {
            if (position.equals("left")) {
                if (leftView == null) {
                    leftView = new BMCWebView(fragment);
                } else {
                    leftView.setFragment(fragment);
                }

                webUrl = leftView.getUrl();
                if (webUrl == null || webUrl.equals("")) {
                    leftView.loadUrl(uri);
                } else {
                    if (!webUrl.equals(uri)) {
                        leftView.loadUrl(uri);
                    } else {
                        // callback
                        fragment.popupWebView = leftView;
                        fragment.callback("slidePopupOpen", data.toString(), "");
                    }
                }
                webview = leftView;
            } else if (position.equals("right")) {
                if (rightView == null) {
                    rightView = new BMCWebView(fragment);
                } else {
                    rightView.setFragment(fragment);
                }
                webUrl = rightView.getUrl();
                if (webUrl == null || webUrl.equals("")) {
                    rightView.loadUrl(uri);
                } else {
                    if (!webUrl.equals(uri)) {
                        rightView.loadUrl(uri);
                    } else {
                        // callback
                        fragment.popupWebView = rightView;
                        fragment.callback("slidePopupOpen", data.toString(), "");
                    }
                }
                webview = rightView;
            } else if (position.equals("top")) {
                if (topView == null) {
                    topView = new BMCWebView(fragment);
                } else {
                    topView.setFragment(fragment);
                }
                webUrl = topView.getUrl();
                if (webUrl == null || webUrl.equals("")) {
                    topView.loadUrl(uri);
                } else {
                    if (!webUrl.equals(uri)) {
                        topView.loadUrl(uri);
                    } else {
                        // callback//
                        fragment.popupWebView = topView;
                        fragment.callback("slidePopupOpen", data.toString(), "");
                    }
                }
                webview = topView;
            } else if (position.equals("bottom")) {
                if (bottomView == null) {
                    bottomView = new BMCWebView(fragment);
                } else {
                    bottomView.setFragment(fragment);
                }
                webUrl = bottomView.getUrl();
                if (webUrl == null || webUrl.equals("")) {
                    bottomView.loadUrl(uri);
                } else {
                    if (!webUrl.equals(uri)) {
                        bottomView.loadUrl(uri);
                    } else {
                        // callback//
                        fragment.popupWebView = bottomView;
                        fragment.callback("slidePopupOpen", data.toString(), "");
                    }
                }
                webview = bottomView;
            } else if (position.equals("center")) {
                if (centerView == null) {
                    centerView = new BMCWebView(fragment);
                } else {
                    centerView.setFragment(fragment);
                }
                webUrl = centerView.getUrl();
                if (webUrl == null || webUrl.equals("")) {
                    centerView.loadUrl(uri);
                } else {
                    if (!webUrl.equals(uri)) {
                        centerView.loadUrl(uri);
                    } else {
                        // callback//
                        fragment.popupWebView = centerView;
                        fragment.callback("slidePopupOpen", data.toString(), "");
                    }
                }
                webview = centerView;
            } else {
                webview = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webview;
    }
}
