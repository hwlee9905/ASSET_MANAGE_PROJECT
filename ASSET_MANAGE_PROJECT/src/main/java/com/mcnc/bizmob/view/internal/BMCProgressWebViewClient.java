package com.mcnc.bizmob.view.internal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

public class BMCProgressWebViewClient extends WebViewClient {

	private static final String TAG = "BaseWebClient";

	private BMCFragment activity;
	private BMCWebView webView;
	private ProgressBar progressBar;
	
	private String errorURL = "";

	public BMCProgressWebViewClient(BMCFragment activity) {
		this.activity = activity;
	}

	public BMCProgressWebViewClient(BMCFragment activity, BMCWebView view) {
		this.activity = activity;
		this.webView = view;
	}

	public BMCProgressWebViewClient(BMCFragment activity, BMCWebView view, ProgressBar progressBar) {
		this.activity = activity;
		this.webView = view;
		this.progressBar = progressBar;
	}

	public void setWebView(BMCWebView view) {
		this.webView = view;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {

		if(url.toLowerCase().startsWith("http://") || 
				url.toLowerCase().startsWith("https://") || 
				url.toLowerCase().startsWith("file://")){
			
			view.loadUrl(url);
		} else if(url.toLowerCase().startsWith("mcnc://")) {
			activity.overrideUrlLoading(view, url);
		} else{
			try {

				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				activity.getActivity().startActivity(intent);

			} catch (Exception e) {
				e.printStackTrace();
				return super.shouldOverrideUrlLoading(view, url);
			}
		}
		return true;
	}

	@Override
	public void onLoadResource(WebView view, String url) {

		super.onLoadResource(view, url);
	}

	@Override
	public void onPageFinished(final WebView view, String url) {
		
		progressBar.setVisibility(View.GONE);

		super.onPageFinished(view, url);
	}

	@Override
	public void onPageStarted(final WebView view, String url, Bitmap favicon) {

		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);
		super.onPageStarted(view, url, favicon);
	}
	
	/**
	 * Report an error to the host application. These errors are unrecoverable
	 * (i.e. the main resource is unavailable). The errorCode parameter
	 * corresponds to one of the ERROR_* constants.
	 * 
	 * @param view
	 *            The WebView that is initiating the callback.
	 * @param errorCode
	 *            The error code corresponding to an ERROR_* value.
	 * @param description
	 *            A String describing the error.
	 * @param failingUrl
	 *            The url that failed to load.
	 */
	@Override
	public void onReceivedError(final WebView view, int errorCode,
								String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);
	}

	// SSL 예외처리
	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler,
								   SslError error) {
		if (error.getPrimaryError() == SslError.SSL_IDMISMATCH) {
			Logger.e(TAG, "SslError.SSL_IDMISMATCH");
			handler.cancel();
		} else if (error.getPrimaryError() == SslError.SSL_EXPIRED) {
			Logger.e(TAG, "SslError.SSL_EXPIRED");
			handler.cancel();
		} else if (error.getPrimaryError() == SslError.SSL_NOTYETVALID) {
			Logger.e(TAG, "SslError.SSL_NOTYETVALID");
			handler.cancel();
		} else if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
			Logger.e(TAG, "SslError.SSL_UNTRUSTED");
			handler.cancel();
		} else {
			Logger.e(TAG, "error.getPrimaryError()");
			handler.cancel();
		}
	}
}
