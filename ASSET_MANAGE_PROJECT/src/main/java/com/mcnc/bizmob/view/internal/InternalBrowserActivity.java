package com.mcnc.bizmob.view.internal;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.ImageWrapper;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.activity.BMCActivity;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class InternalBrowserActivity extends BMCActivity {
	private String TAG = this.toString();
	private String callback = "";
	private JSONObject message = null;
	
	private ImageView prevButton = null;
	private ImageView nextButton = null;
	private ImageView exitButton = null;
	private ImageView smsButton = null;
	private boolean prevEnable = false;
	private boolean nextEnable = false;
	private ValueCallback<Uri> mUploadMessage;
	private int FILECHOOSER_RESULTCODE = 22334; 
	private String FILECHOOSER_MESSAGE = "업로드할 파일을 선택하세요";
	
	private LinearLayout headerLayout;
	private LinearLayout toolbarLayout;
	
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(RUtil.getLayoutR(this, "activity_main"));
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
	    
		super.onCreate(savedInstanceState);
		
		setHeader();
		setBody();
		setBottom();
	}
	  
	private void setHeader() {
		
		Intent intent = getIntent();
		headerLayout = (LinearLayout)findViewById(RUtil.getIdR(getApplicationContext(), "header_view"));
		
		int height = AppConfig.DEFAULT_TITLEBAR_HEIGHT;
		int title_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
		
		LayoutParams buttonMargineParam = new LayoutParams( 10, title_height);
		LinearLayout margineL = new LinearLayout(this);
		LinearLayout margineM = new LinearLayout(this);
		LinearLayout margineS = new LinearLayout(this);
		LinearLayout margineR = new LinearLayout(this);

		LayoutParams headerLayoutParam = new LayoutParams(
				LayoutParams.MATCH_PARENT, title_height, 0 );
		LayoutParams headerInParam = new LayoutParams(
				600, LayoutParams.WRAP_CONTENT, 1);
		LayoutParams headerCenterParam = new LayoutParams(
				LayoutParams.MATCH_PARENT, title_height, 1);

		Logger.d(TAG, "headerLayout : " + headerLayout);

		LinearLayout headerLeftLayout = new LinearLayout(this);
		headerLeftLayout.setVisibility(View.INVISIBLE);
		LinearLayout headerTitleLayout = new LinearLayout(this);
		LinearLayout headerRightLayout = new LinearLayout(this);
		headerRightLayout.setPadding(0, 0, 20, 0);

		headerLayout.addView(headerLeftLayout, headerInParam);
		headerLayout.addView(headerTitleLayout, headerCenterParam);
		headerLayout.addView(headerRightLayout, headerInParam);

		TextView tv = new TextView(this);
		if(intent.hasExtra("title")){
			tv.setText(intent.getStringExtra("title"));
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(16);
			tv.setGravity(Gravity.CENTER_VERTICAL);
		} else {
			tv.setText(intent.getStringExtra("url"));
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(16);
			tv.setGravity(Gravity.CENTER_VERTICAL);
		}
		headerTitleLayout.addView(tv, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0));

		try {

			headerLayout.setBackground(ImageWrapper.getDrawableAsset("native/bg_title.png", this));

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 이전 버튼
		prevButton = new ImageView(this);
		try {

			float rate = 0;
			float newWidth = 0;
			float newHeight = title_height;

			Drawable d = ImageWrapper.getDrawableAsset("native/bizmob_browser_back_off.png", this);

			Bitmap bm = drawableToBitmap(d);

			rate = ((float)title_height / (float)bm.getHeight());
			newWidth = ((float)bm.getWidth() * rate);
			newHeight = ((float)bm.getHeight() * rate);

			bm = Bitmap.createScaledBitmap(bm, (int)newWidth, (int)newHeight, true);
			d = new BitmapDrawable(getResources(), bm);

			prevButton.setImageDrawable(d);

		} catch (IOException e) {
			e.printStackTrace();
		}

		headerLeftLayout.addView(margineL, buttonMargineParam);
		headerLeftLayout.addView(prevButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headerLeftLayout.setGravity(Gravity.LEFT | Gravity.CENTER);

		// 다음
		nextButton = new ImageView(this);
		try {

			float rate = 0;
			float newWidth = 0;
			float newHeight = title_height;

			Drawable d = ImageWrapper.getDrawableAsset("native/bizmob_browser_foword_off.png", this);

			Bitmap bm = drawableToBitmap(d);

			rate = ((float)title_height / (float)bm.getHeight());
			newWidth = ((float)bm.getWidth() * rate);
			newHeight = ((float)bm.getHeight() * rate);

			bm = Bitmap.createScaledBitmap(bm, (int)newWidth, (int)newHeight, true);
			d = new BitmapDrawable(getResources(), bm);

			nextButton.setImageDrawable(d);

		} catch (IOException e) {
			e.printStackTrace();
		}
		headerLeftLayout.addView(margineM, buttonMargineParam);
		headerLeftLayout.addView(nextButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headerLeftLayout.setGravity(Gravity.LEFT | Gravity.CENTER);

		// SMS
		smsButton = new ImageView(this);
		try {

			float rate = 0;
			float newWidth = 0;
			float newHeight = title_height;

			Drawable d = ImageWrapper.getDrawableAsset("native/bizmob_browser_sms_off.png", this);

			Bitmap bm = drawableToBitmap(d);

			rate = ((float)title_height / (float)bm.getHeight());
			newWidth = ((float)bm.getWidth() * rate);
			newHeight = ((float)bm.getHeight() * rate);

			bm = Bitmap.createScaledBitmap(bm, (int)newWidth, (int)newHeight, true);
			d = new BitmapDrawable(getResources(), bm);

			smsButton.setImageDrawable(d);

		} catch (IOException e) {
			e.printStackTrace();
		}
		headerLeftLayout.addView(margineS, buttonMargineParam);
		headerLeftLayout.addView(smsButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headerLeftLayout.setGravity(Gravity.LEFT | Gravity.CENTER);

		// 종료
		exitButton = new ImageView(this);
		try {

			float rate = 0;
			float newWidth = 0;
			float newHeight = title_height;

			Drawable d = ImageWrapper.getDrawableAsset("native/bizmob_browser_close_off.png", this);

			Bitmap bm = drawableToBitmap(d);

			rate = ((float)title_height / (float)bm.getHeight());
			newWidth = ((float)bm.getWidth() * rate);
			newHeight = ((float)bm.getHeight() * rate);

			bm = Bitmap.createScaledBitmap(bm, (int)newWidth, (int)newHeight, true);
			d = new BitmapDrawable(getResources(), bm);

			exitButton.setImageDrawable(d);

		} catch (IOException e) {
			e.printStackTrace();
		}
		headerRightLayout.addView(exitButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headerRightLayout.addView(margineR, buttonMargineParam);
		headerRightLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);

		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( nextEnable ) {
					webView.goBackOrForward(1);
					prevEnable = false;
					nextEnable = false;
				}
			}
		});
		prevButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( prevEnable ) {
					webView.goBack();
					prevEnable = false;
					nextEnable = false;
				}
			}
		});

		smsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( webView != null && webView.getUrl() != null ) {
					String url = webView.getUrl();
//					TASK_ID_SMS ("", url);
				} else {
					new AlertDialog.Builder(context)
					 .setMessage("전달 할 수 있는 URL이 없습니다.")
					 .setPositiveButton(android.R.string.ok, null)
					 .setCancelable(false).create().show();
				}
			}
		});

		// 옵션에 따라 sms 버튼을 보이거나 안보이게 한다.
		if (intent.hasExtra("sms_button")){
			boolean b = intent.getBooleanExtra("sms_button", false );
			if ( ! b ) {
				smsButton.setVisibility(View.GONE);
			}
		}

		exitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(RUtil.getR(context, "anim", "hold"), RUtil.getR(context, "anim", "push_right_out"));
			}
		});

		headerLayout.setVisibility(View.VISIBLE);
	}

	protected void setBody() {

		LinearLayout lin = (LinearLayout) findViewById(RUtil.getIdR(this, "main_view"));
		webView = new BMCWebView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lin.addView(webView, lp);

		webView.setId(100);
		webView.setInitialScale(100);
		webView.setVerticalScrollBarEnabled(true);
		webView.requestFocusFromTouch();
		webView.setFocusable(true);

		// Enable JavaScript
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);

		settings.setSupportMultipleWindows(true);

		// openFileChooser 가 가능한 CromeClient로 변경
		context = this;
		webView.setWebViewClient(new BaseWebClient(this));
		webView.setWebChromeClient( new BaseWebChromeClient ());

		Intent intent = getIntent();

		if (intent.hasExtra("url")) {

			String targetPage = intent.getExtras().getString("url");
			String url = ImageWrapper.getUri(targetPage);

			webView.loadUrl(url);
		}

		if (intent.hasExtra("orientation")) {
			String strOr = intent.getExtras().getString("orientation");
			if ( strOr.startsWith("land") ){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else if ( strOr.startsWith("portrait") ) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if (strOr.equals("sensorLandscape")) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			} else if (strOr.equals("sensorPortrait")) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

	protected void setBottom() {

		toolbarLayout = (LinearLayout)findViewById(RUtil.getIdR(getApplicationContext(), "bottom_view"));

		int height = AppConfig.DEFAULT_BOTTOM_TOOLBAR_HEIGHT;
		int tool_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());

		LayoutParams buttonMargineParam = new LayoutParams( 10, tool_height, 1);
		LinearLayout margineL = new LinearLayout(this);
		LinearLayout margineM = new LinearLayout(this);
		LinearLayout margineS = new LinearLayout(this);
		LinearLayout margineR = new LinearLayout(this);

		LayoutParams headerLayoutParam = new LayoutParams(
				LayoutParams.MATCH_PARENT, tool_height, 0 );
		LayoutParams headerInParam = new LayoutParams(
				/*Def.TOP_BUTTON_W*/400, tool_height, 0);
		LayoutParams headerCenterParam = new LayoutParams(
				LayoutParams.MATCH_PARENT, tool_height, 0);

		LinearLayout headerLeftLayout = new LinearLayout(this);
		LinearLayout headerTitleLayout = new LinearLayout(this);
		LinearLayout headerRightLayout = new LinearLayout(this);

		toolbarLayout.addView(headerTitleLayout, headerCenterParam);

		try {
			toolbarLayout.setBackground(ImageWrapper.getDrawableAsset("native/bg_title.png", this));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 이전 버튼
		prevButton = new ImageView(this);

		try {

			float rate = 0;
			float newWidth = 0;
			float newHeight = tool_height;

			Drawable d = ImageWrapper.getDrawableAsset("native/bizmob_browser_back_off.png", this);

			Bitmap bm = drawableToBitmap(d);

			rate = ((float)tool_height / (float)bm.getHeight());
			newWidth = ((float)bm.getWidth() * rate);
			newHeight = ((float)bm.getHeight() * rate);

			bm = Bitmap.createScaledBitmap(bm, (int)newWidth, (int)newHeight, true);
			d = new BitmapDrawable(getResources(), bm);

			prevButton.setImageDrawable(d);

		} catch (IOException e) {
			e.printStackTrace();
		}

		headerTitleLayout.addView(margineL, buttonMargineParam);
		headerTitleLayout.addView(prevButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headerTitleLayout.setGravity(Gravity.LEFT | Gravity.CENTER);

		// 다음
		nextButton = new ImageView(this);
		try {

			float rate = 0;
			float newWidth = 0;
			float newHeight = tool_height;

			Drawable d = ImageWrapper.getDrawableAsset("native/bizmob_browser_foword_off.png", this);

			Bitmap bm = drawableToBitmap(d);

			rate = ((float)tool_height / (float)bm.getHeight());
			newWidth = ((float)bm.getWidth() * rate);
			newHeight = ((float)bm.getHeight() * rate);

			bm = Bitmap.createScaledBitmap(bm, (int)newWidth, (int)newHeight, true);
			d = new BitmapDrawable(getResources(), bm);

			nextButton.setImageDrawable(d);

		} catch (IOException e) {
			e.printStackTrace();
		}
		headerTitleLayout.addView(margineM, buttonMargineParam);
		headerTitleLayout.addView(nextButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headerTitleLayout.setGravity(Gravity.LEFT | Gravity.CENTER);

		// SMS
		smsButton = new ImageView(this);
		try {

			float rate = 0;
			float newWidth = 0;
			float newHeight = tool_height;

			Drawable d = ImageWrapper.getDrawableAsset("native/bizmob_browser_sms_off.png", this);

			Bitmap bm = drawableToBitmap(d);

			rate = ((float)tool_height / (float)bm.getHeight());
			newWidth = ((float)bm.getWidth() * rate);
			newHeight = ((float)bm.getHeight() * rate);

			bm = Bitmap.createScaledBitmap(bm, (int)newWidth, (int)newHeight, true);
			d = new BitmapDrawable(getResources(), bm);

			smsButton.setImageDrawable(d);

		} catch (IOException e) {
			e.printStackTrace();
		}
		headerTitleLayout.addView(margineS, buttonMargineParam);
		headerTitleLayout.addView(smsButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		headerTitleLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);

		headerTitleLayout.addView(margineR, buttonMargineParam);

		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( nextEnable ) {
					webView.goBackOrForward(1);
					prevEnable = false;
					nextEnable = false;
				}
			}
		});
		prevButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( prevEnable ) {
					webView.goBack();
					prevEnable = false;
					nextEnable = false;
				}
			}
		});

		smsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				webView.reload();
			}
		});

		Intent intent = getIntent();
		if(intent.getBooleanExtra("toolbar_visible", false)){
			toolbarLayout.setVisibility(View.VISIBLE);
		} else {
			toolbarLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void setButton () {
		try {
			if ( webView.canGoBack() ) {
				prevEnable = true;
			} else {
				prevEnable = false;
				Logger.d(TAG, "Can't go back!!");
			}
			if ( webView.canGoForward()) {
				nextEnable = true;
			} else {
				nextEnable = false;
				Logger.d(TAG, "Can't go Forward!!");
			}
		} catch (Exception e) {
		}
	}

	public void loadUrl(final String url) {
		this.webView.loadUrl(url);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==FILECHOOSER_RESULTCODE ){
            if (null == mUploadMessage){
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            Logger.d("raw", mUploadMessage.toString() + "   " + result);
            mUploadMessage = null;
            return;
        }
		super.onActivityResult(requestCode, resultCode, intent);
	}

	private Bitmap drawableToBitmap(Drawable d){

		if(d instanceof BitmapDrawable){
			return ((BitmapDrawable)d).getBitmap();
		} else {

			Bitmap bm = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(bm);
			d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			d.draw(canvas);

			return bm;
		}
	}

	class BaseWebClient extends WebViewClient {

		BMCActivity activity;
		BMCWebView webView;

		public BaseWebClient(BMCActivity activity) {
			this.activity = activity;
		}

		public BaseWebClient(BMCActivity activity, BMCWebView view) {
			this.activity = activity;
			this.webView = view;
		}

		public void setWebView(BMCWebView view) {
			this.webView = view;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			view.loadUrl(url);
			return true;
		}

		@Override
		public void onLoadResource(WebView view, String url) {

			super.onLoadResource(view, url);
		}

		@Override
		public void onPageFinished(final WebView view, String url) {

			setButton();
			try {
				if (url.endsWith(".mp3")|| url.endsWith(".mp4") || url.endsWith(".3gp")) {
		            Intent intent = new Intent(Intent.ACTION_VIEW);
		            intent.setDataAndType(Uri.parse(url), "audio/*");
		            view.getContext().startActivity(intent);
		        }
			} catch (ActivityNotFoundException e) {
			}

			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {

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

			if(errorCode == ERROR_FILE_NOT_FOUND || errorCode == ERROR_TIMEOUT){

				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						view.loadUrl("file:///android_asset/webkit/android-weberror.html");
					}
				});
			} else {

				super.onReceivedError(view, errorCode, description, failingUrl);
			}
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

	class BaseWebChromeClient extends WebChromeClient {

		// testCode for phoneGap
		private String TAG = "BaseWebChromeClient";
		private long MAX_QUOTA = 100 * 1024 * 1024;

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin,
				Callback callback) {
			// TODO Auto-generated method stub
			super.onGeolocationPermissionsShowPrompt(origin, callback);
			callback.invoke(origin, true, false);
		}

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			Logger.f("", consoleMessage.message() + " -- From line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
			return super.onConsoleMessage(consoleMessage);
		}

		// webview alert
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
								 final android.webkit.JsResult result) {

			Logger.f(TAG, "onJsAlert");
			Logger.f(TAG, "view::" + view);
			Logger.f(TAG, "url::" + url);
			Logger.f(TAG, "message::" + message);

			new AlertDialog.Builder(context)
					//.setTitle("AlertDialog")
					.setMessage(message)
					.setPositiveButton(android.R.string.ok,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									result.confirm();
								}
							}).setCancelable(false).create().show();

			return true;
		};

		// webview confirm
		@Override
		public boolean onJsConfirm(WebView view, String url, String message,
								   final android.webkit.JsResult result) {

			Logger.f(TAG, "onJsConfirm");
			Logger.f(TAG, "view::" + view);
			Logger.f(TAG, "url::" + url);
			Logger.f(TAG, "message::" + message);
			Logger.f(TAG, "result::" + result);

			new AlertDialog.Builder(context)
					//.setTitle("ConfirmDialog")
					.setMessage(message)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									result.confirm();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									result.cancel();
								}
							}).setCancelable(false).create().show();

			return true;
		};

		// For Android < 3.0
	    public void openFileChooser( ValueCallback<Uri> uploadMsg ){
	    	mUploadMessage = uploadMsg;
	        openFileChooser( uploadMsg, "" );
	    }

	    // For Android 3.0+
	    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType ){
	    	mUploadMessage = uploadMsg;
	        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
	        //i.addCategory(Intent.CATEGORY_OPENABLE);
	        i.setType("*/*");
	        i.putExtra("return-data", true);

	        PackageManager pkManager = getPackageManager();
	        List<ResolveInfo> activities = pkManager.queryIntentActivities(i, 0);
			if (activities.size() > 1) {
				startActivityForResult(Intent.createChooser(i, FILECHOOSER_MESSAGE), FILECHOOSER_RESULTCODE);
			} else {
				startActivityForResult(i, FILECHOOSER_RESULTCODE);
			}
	    }
	    // For Android 4.1+
	    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
	    	mUploadMessage = uploadMsg;
	        openFileChooser( uploadMsg, "" );
	    }
	}

	
	@Override
	public void callback(String s, String s1, String s2) {

	}

	@Override
	public void setApp(JSONObject param) {

	}

	@Override
	public void onCreateContents(Bundle savedInstanceState) {

	}

	@Override
	public boolean dispatchTouchEvent(Window.Callback cb,
			MotionEvent event) {
		return false;
	}
}
