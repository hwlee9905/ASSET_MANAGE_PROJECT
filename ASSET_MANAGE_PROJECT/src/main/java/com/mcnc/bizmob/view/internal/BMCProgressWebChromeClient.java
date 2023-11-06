package com.mcnc.bizmob.view.internal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.mcnc.bizmob.core.def.ActivityRequestCode;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

public class BMCProgressWebChromeClient extends WebChromeClient {

    private long MAX_QUOTA = 100 * 1024 * 1024;
    private BMCWebView webView;
    private Context context;

    // the video progress view
    private View mVideoProgressView;
    
    // File Chooser
    public ValueCallback<Uri> mUploadMessage;
    
    private ProgressBar progressBar;

    public BMCProgressWebChromeClient(Context context, BMCWebView webView){
    	this.context = context;
    	this.webView = webView;
    }
    
    public BMCProgressWebChromeClient(Context context, BMCWebView webView, ProgressBar progressBar){
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
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    result.confirm();
                    return false;
                }
                else
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
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    result.cancel();
                    return false;
                }
                else
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
     *
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

    @Override
    /**
     * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified origin.
     *
     * @param origin
     * @param callback
     */
    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
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

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType ) {
        this.openFileChooser(uploadMsg, acceptType, null);
    }
    
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
    {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        ((Activity)context).startActivityForResult(Intent.createChooser(i, "File Browser"),
                ActivityRequestCode.FILECHOOSER_RESULTCODE);
    }
    
    public ValueCallback<Uri> getValueCallback() {
        return this.mUploadMessage;
    }
    
    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {


    	return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }
    
    @Override
    public void onCloseWindow(WebView window) {

    	window.setVisibility(View.GONE);
    	
    	super.onCloseWindow(window);
    }
}
