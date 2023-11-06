package com.mcnc.bizmob.plugin.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;

import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.setting.SettingModel;
import com.mcnc.bizmob.core.util.BMCPermission;
import com.mcnc.bizmob.core.util.file.FileUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.networksetting.NetworkSettingActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 01.클래스 설명 : 화면 녹화 위한 플러그인 (5.0 이상 제공). <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 화면녹화 <br>
 * 04.관련 API/화면/서비스 : SettingModel, BMCPermission, FileUtil , Logger<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-05                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class ScreenRecorderPlugin extends BMCPlugin {


    /**
     * Param 객체
     */
    private JSONObject param;

    /**
     * callback 명
     */
    private String callback = "";

    /**
     * 파일 path
     */
    private String fullPath = "";
    /**
     * 저장 폴더명 (default)
     */
    private String targetPath = "bizmob_videos";

    /**
     * 파일 저장 위치 (default)
     */
    private String targetPathType = "external";

    /**
     * reqeust 코드.
     */
    private final int SCREEN_RECORD_REQUEST_PERMISSION = 13252;

    /**
     * screen density
     */
    private static int mScreenDensity;

    /**
     * MediaPrjection Manager
     */
    private static MediaProjectionManager mProjectionManager;

    /**
     * 화면 녹화 가로 사이즈.
     */
    private static final int DISPLAY_WIDTH = 720;

    /**
     * 화면 녹화 세로 사이즈.
     */
    private static final int DISPLAY_HEIGHT = 1280;

    /**
     * MediaProjection 객체
     */
    private static MediaProjection mMediaProjection;

    /**
     * VirtualDisplay 객체
     */
    private static VirtualDisplay mVirtualDisplay;

    /**
     * MediaProjectionCallback 객체
     */
    private static MediaProjectionCallback mMediaProjectionCallback;

    /**
     * MediaRecorder 객체
     */
    private static MediaRecorder mMediaRecorder;

    /**
     * SparseIntArray 객체
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Audio record 권한 request code
     */
    private final int REQ_CODE_PERMISSION_AUDIO_RECORD = 128;

    /**
     * BMCPermission 객체.
     */
    private BMCPermission audioRecordPermission;

    /**
     * client로 부터 넘어온 request data.
     */
    private JSONObject data;

    private String commandID;

    @Override
    public void executeWithParam(JSONObject data) {
        this.data = data;

        try {
            commandID = data.getString("id");
            if (data.has("param")) {
                param = data.getJSONObject("param");
                if (param.has("callback")) {
                    callback = param.getString("callback");
                }
                if (param.has("target_path")) {
                    targetPath = param.getString("target_path");
                }

                if (param.has("target_path_type")) {
                    targetPathType = param.getString("target_path_type");
                }

                if (targetPathType.length() > 0) {
                    fullPath = FileUtil.getAbsolutePath(targetPathType, targetPath);
                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (commandID.equals("SCREEN_RECORDER_START")) { //Screen Recording
            audioRecordPermission = new BMCPermission.Builder(getActivity(), REQ_CODE_PERMISSION_AUDIO_RECORD, new String[]{Manifest.permission.RECORD_AUDIO})
                    .setFragment(getFragment())
                    .setActivity(getActivity())
                    .setPlugin(this)
                    .setDeniedMessage(getActivity().getString(RUtil.getStringR(getActivity(),"txt_screen_record_guide")))
                    .setPositiveButton(getActivity().getString(RUtil.getStringR(getActivity(),"txt_close")), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JSONObject result = null;
                            try {
                                result = new JSONObject();
                                result.put("result", false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.resultCallback("callback", callback, result);
                        }
                    })
                    .build();

            if (audioRecordPermission.checkPermissions()) {
                afterPermissionCheck(data);
            }
        } else if (commandID.equals("SCREEN_RECORDER_END")) {
            stopScreenRecording();
        }


    }

    private void afterPermissionCheck(JSONObject data) {
        if (commandID.equals("SCREEN_RECORDER_START")) { //Screen Recording
            startScreenRecording();
        }
    }

    private void startScreenRecording() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        if (mMediaProjection == null) {
            mMediaRecorder = new MediaRecorder();
            mProjectionManager = (MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            if (mMediaProjection == null) {
                startActivityForResultFromPlugin(mProjectionManager.createScreenCaptureIntent(), SCREEN_RECORD_REQUEST_PERMISSION);
            }
        }
    }

    private void stopScreenRecording() {
        if (mMediaProjection != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();

            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();

                if (mMediaProjection != null) {
                    mMediaProjection.unregisterCallback(mMediaProjectionCallback);
                    mMediaProjection.stop();
                    mMediaProjection = null;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(RUtil.getStringR(getActivity(),"txt_screen_record_end")), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            JSONObject result = new JSONObject();
            try {
                result.put("result", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            listener.resultCallback("callback", callback, result);

        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (SettingModel.isScreenRecording) {
                stopScreenRecording();
            }
        }

    }

    private String getCurrentDateAndTime() { //현재 날짜 및 시간을 1606241055의 포맷으로 반환해주는 메소드
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyMMddHHmm");
        Date today = new Date();
        String curDate = timeStampFormat.format(today);
        return curDate;
    }

    private boolean initRecorder() {

        try {

            String videoPath = Environment.getExternalStorageDirectory() + "/bizmob_videos";

            if (fullPath != null && fullPath.length() > 0) {
                videoPath = fullPath;
            }

            File videoDir = new File(videoPath);

            //폴더 존재 유무 확인
            if (!videoDir.exists()) {
                videoDir.mkdir();
            }

            ApplicationInfo applicationInfo = getActivity().getApplicationInfo();
            int stringId = applicationInfo.labelRes;
            String applicationName = getActivity().getString(stringId);
            String fileName = "/" + applicationName + "_" + getCurrentDateAndTime() + ".mp4";
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(videoDir + fileName);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        JSONObject result = new JSONObject();
        if (requestCode == SCREEN_RECORD_REQUEST_PERMISSION) {
            if (resultCode != Activity.RESULT_OK) {
                try {
                    result.put("result", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SettingModel.isScreenRecording = false;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() instanceof NetworkSettingActivity) {
                            ((NetworkSettingActivity) getActivity()).screenRecorderRadioFalseButton.setChecked(true);
                        }
                        Toast.makeText(getActivity(),
                                getActivity().getString(RUtil.getStringR(getActivity(),"txt_screen_record_permission_denied")), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (initRecorder()) {
                    mMediaProjectionCallback = new MediaProjectionCallback();
                    mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                    mMediaProjection.registerCallback(mMediaProjectionCallback, null);
                    mVirtualDisplay = mMediaProjection.createVirtualDisplay("SlideFragmentActivity",
                            DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                            mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
                    mMediaRecorder.start();
                    SettingModel.isScreenRecording = true;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(RUtil.getStringR(getActivity(),"txt_screen_record_start")), Toast.LENGTH_SHORT).show();
                        }
                    });
                    try {
                        result.put("result", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    SettingModel.isScreenRecording = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(RUtil.getStringR(getActivity(),"txt_screen_record_cancel")), Toast.LENGTH_SHORT).show();
                        }
                    });

                    try {
                        result.put("result", false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            listener.resultCallback("callback", callback, result);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        if (requestCode == REQ_CODE_PERMISSION_AUDIO_RECORD) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                afterPermissionCheck(data);
            } else {
                if (getActivity() instanceof NetworkSettingActivity) {
                    SettingModel.isScreenRecording = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((NetworkSettingActivity) getActivity()).screenRecorderRadioFalseButton.setChecked(true);
                        }
                    });
                }
                audioRecordPermission.showDeniedDialog();
            }
        }
    }

}
