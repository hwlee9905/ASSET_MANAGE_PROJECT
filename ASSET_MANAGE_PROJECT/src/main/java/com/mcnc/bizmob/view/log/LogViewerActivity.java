package com.mcnc.bizmob.view.log;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;

/**
 * Created by lenovo on 2016-11-17.
 */

public class LogViewerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(RUtil.getLayoutR(this, "activity_log_viewer"));

        Button btnClose = (Button) findViewById(RUtil.getIdR(this, "bt_close"));

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView tv = (TextView) findViewById(RUtil.getIdR(this, "tv_log"));
        tv.setText(Logger.logBuffer.toString());

    }
}
