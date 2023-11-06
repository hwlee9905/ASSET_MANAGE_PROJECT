package com.mcnc.bizmob.view.barcode;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.util.Utils;

/**
 *
 */
public class CaptureActivity extends Activity {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private RelativeLayout rlGuideWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(RUtil.getLayoutR(this, "activity_custom_scanner"));

        barcodeScannerView = (DecoratedBarcodeView) findViewById(RUtil.getIdR(this, "zxing_barcode_scanner"));

        rlGuideWrapper = (RelativeLayout) findViewById(RUtil.getIdR(this, "rl_guide_wrapper"));

        RelativeLayout rlBtnBackkWrapper = (RelativeLayout) findViewById(RUtil.getIdR(this, "rl_ib_back_wrapper"));
        rlBtnBackkWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ImageButton ibBack = (ImageButton) findViewById(RUtil.getIdR(this, "ib_back"));
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        capture = new CaptureManager(this, barcodeScannerView);
        int orientation = Integer.parseInt(getIntent().getStringExtra("orientation"));
        capture.setOrientation(orientation);
        capture.initializeFromIntent(getIntent(), savedInstanceState);

        capture.decode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("result", true);
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (rlGuideWrapper != null) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rlGuideWrapper.getLayoutParams();
                layoutParams.height = (int) Utils.getPixelOfDpi(this, 105);
                rlGuideWrapper.setLayoutParams(layoutParams);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (rlGuideWrapper != null) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rlGuideWrapper.getLayoutParams();
                layoutParams.height = (int) Utils.getPixelOfDpi(this, 69);
                rlGuideWrapper.setLayoutParams(layoutParams);

            }
        }
    }

}
