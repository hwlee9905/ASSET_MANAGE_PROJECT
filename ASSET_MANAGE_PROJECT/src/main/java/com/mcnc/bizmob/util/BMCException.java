package com.mcnc.bizmob.util;

import com.mcnc.bizmob.core.view.activity.BMCWebActivity;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

import org.json.JSONException;
import org.json.JSONObject;

public class BMCException extends Exception {

    private static final long serialVersionUID = 5268768997478331128L;
    BMCWebActivity activity;
    BMCWebView webview;

    String serviceName = "";
    String param = "";

    public BMCException(BMCWebActivity activity, BMCWebView webView) {
        this.activity = activity;
        this.webview = webView;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();

        JSONObject result = new JSONObject();

        try {
            result.put("message", new JSONObject());

            JSONObject error = new JSONObject();
            result.put("error", error);

            error.put("service_name", serviceName);
            error.put("param", param);
            error.put("error_message", getMessage());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        webview.loadUrl("javascript:bizMOBCore.CallbackManager.responser({callback:'error'}, {message:{}, error:{}});");
    }
}
