package com.mcnc.bizmob.view.ntv;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mcnc.bizmob.manage.ntv.NativeSlideFragmentActivity;
import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.def.Def;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.setting.SettingModel;
import com.mcnc.bizmob.core.util.BMCPermission;
import com.mcnc.bizmob.core.util.ConnectivityReceiver;
import com.mcnc.bizmob.core.util.DeviceRegistUtil;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.config.AppConfigReader;
import com.mcnc.bizmob.core.util.file.FileUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.ImageUtil;
import com.mcnc.bizmob.core.util.res.ImageWrapper;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.crash.CrashHandler;
import com.mcnc.bizmob.core.view.deviceregist.DeviceRegistActivity;
import com.mcnc.bizmob.core.view.webview.BMCWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class NativeLauncherFragment extends NativeMainFragment {

    public static final String[] requiredPermissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * app.config file이 존재하는 path 값.
     */
    final String APP_CONFIG = "bizMOB/config/app.config";

    public ProgressBar progressBar;
    public TextView progressBarDescription;

    private final String TAG = this.toString();

    ConnectivityReceiver receiver;
    final int REQUEST_CODE_DEVICE_REGIST = 1020;

    /**
     * notice 바디
     */
    private JSONObject bodyObj;

    private BMCPermission bmcRequiredPermission;
    private static final int REQ_CODE_PERMISSION_REQUIRED = 123;

    private boolean needToBlockLogic = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isHomeFragment = true;
        wrapper = inflater.inflate(RUtil.getLayoutR(getActivity(), "fragment_native_launcher"), null);

        if (savedInstanceState != null) {
            needToBlockLogic = true;
        }

        return wrapper;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setDefaultProgress() {
        this.progressBar = (ProgressBar) wrapper.findViewById(RUtil.getIdR(getActivity(), "pb_native_update"));
        this.progressBarDescription = (TextView) wrapper.findViewById(RUtil.getIdR(getActivity(), "tv_progress_description"));
        this.progressBar.setMax(100);
    }

    public ProgressBar getDownloadProgressBar() {
        return this.progressBar;
    }

    public TextView getDownloadDesTextView() {
        return this.progressBarDescription;
    }

    @Override
    public void onCreateContents() {
        if (!needToBlockLogic) {
            Logger.d("savedInstanceState", "savedInstanceState = null");
            bmcRequiredPermission = new BMCPermission.Builder(getActivity(), REQ_CODE_PERMISSION_REQUIRED, requiredPermissions)
                    .setFragment(NativeLauncherFragment.this)
                    .setDeniedMessage(getActivity().getString(RUtil.getStringR(getActivity(), "txt_required_permission_guide"))).build();
            if (bmcRequiredPermission.checkPermissions()) {
                afterPermissionCheck();
            }
        } else {
            Logger.d("LauncherFragment", "needToBlockLogic, savedInstanceState is not null");
            if (fWebView != null) {
                fWebView = null;
            }
        }
    }

    public void afterPermissionCheck() {

        if (AppConfig.checkRooted) {
            try {
                Logger.d(TAG, "디바이스 루팅 체크 시작.");
                JSONObject reqJson = new JSONObject();
                JSONObject reqParam = new JSONObject();
                reqParam.put("callback", "");
                reqJson.put("id", "CHECK_ROOTED");
                reqJson.put("param", reqParam);

                manager.executeInterfaceFromID("CHECK_ROOTED", reqJson, new CompleteListener() {
                    @Override
                    public void resultCallback(String s, String s1, JSONObject jsonObject) {
                        Logger.d(TAG, "[RESPONSE] RootingCheckPlugin, resultCallback() , result = " + jsonObject.toString());
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.has("result")) {
                                    if (!jsonObject.getBoolean("result")) { // 루팅 되지 않은 경우
                                        appHashCheck();
                                    } else { // true 인경우 루팅.
                                        showTerminateAlarm(getActivity().getString(RUtil.getStringR(getActivity(), "txt_rooting_detected")), "");
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            appHashCheck();
        }
    }

    /**
     * ZZ0008 (앱 위변조 체크) 전문통신을 수행하는 메소드.<br>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-09                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    public void appHashCheck() {
        final BMCManager manager = BMCManager.getInstance();
        manager.setFragment(this);
        manager.setFWebView(null);

        AppConfigReader.LoadAppConfig(getActivity(), APP_CONFIG);

        if (AppConfig.checkAppHashKey) {
            try {
                Logger.d(TAG, "앱위변조 체크, 앱위변조 체크 전문 통신 (ZZ0008) 시작.");
                JSONObject reqJson = new JSONObject();
                JSONObject reqParam = new JSONObject();
                reqParam.put("callback", "");
                reqJson.put("id", "APP_HASH_CHECK");
                reqJson.put("param", reqParam);

                manager.executeInterfaceFromID("APP_HASH_CHECK", reqJson, new CompleteListener() {
                    @Override
                    public void resultCallback(String s, String s1, JSONObject jsonObject) {
                        Logger.d(TAG, "[RESPONSE] AppHashCheckPlugin, resultCallback() , result = " + jsonObject.toString());
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.has("result")) {
                                    if (jsonObject.getBoolean("result")) {
                                        initAESKey();
                                    } else {
                                        showTerminateAlarm(jsonObject.getString("error_text"), "ERROR");
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            initAESKey();
        }
    }

    /**
     * (AES 암호화 키 생성) ZZ0010 전문통신을 수행하는 메소드.<br>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    public void initAESKey() {
        if (AppConfig.useEncryptedComm) {
            BMCManager manager = BMCManager.getInstance();
            try {
                Logger.d(TAG, "암복호화, 키 생성 전문 통신 (ZZ0010) 시작.");
                JSONObject reqJson = new JSONObject();
                JSONObject reqParam = new JSONObject();
                JSONObject ZZ0010Obj = JsonUtil.addRequestHeader(null, "ZZ0010", "");

                ZZ0010Obj.put(Def.JSON_BODY, new JSONObject());

                reqParam.put("message", ZZ0010Obj);
                reqParam.put("trcode", "ZZ0010");
                reqParam.put("read_timeout", 10000);
                reqParam.put("progress", true);
                reqJson.put("param", reqParam);

                //HttpPlugin 호출
                manager.executeInterfaceFromID("RELOAD_WEB", reqJson, new CompleteListener() {
                    @Override
                    public void resultCallback(String s, String s1, JSONObject jsonObject) {
                        boolean bResult = false;

                        try {
                            if (jsonObject.has("header")) {
                                JSONObject headerObj = jsonObject.getJSONObject("header");
                                if (headerObj.has("result")) {
                                    bResult = headerObj.getBoolean("result");
                                    if (bResult) {
                                        if (jsonObject.has("body")) {
                                            JSONObject bodyObj = jsonObject.getJSONObject("body");
                                            if (bodyObj.has("cipherKey")) {
                                                String cipherKey = bodyObj.getString("cipherKey");
                                                BMCInit.AES_KEY = cipherKey;
                                                if (BMCInit.AES_KEY.length() > 0) {
                                                    doUpdateCheck();
                                                }
                                            }
                                        }
                                    } else {
                                        showTerminateAlarm(headerObj.getString("error_text"), "ERROR");
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            doUpdateCheck();
        }
    }

    /**
     * updateCheck 로직을 수행하는 메소드. <br>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    public void doUpdateCheck() {
        setDefaultProgress();
        runIntroBgAnimation();

        //CONTENTS 모드 TYPE
        int imageMode = Integer.parseInt(BMCManager.getInstance().getSetting().getContentsMode());
        // DEVICE 인증 모드
        boolean registMode = BMCManager.getInstance().getSetting().getDeviceRegistMode();

        if (BMCInit.BUILD_MODE.equals("dev")) {
            //디바이스 등록여부 확인
            if (!DeviceRegistUtil.isResisted() && registMode) {
                // 등록 페이지 옮기기
                Intent intent = new Intent(getActivity(), DeviceRegistActivity.class);
                //intent.putExtra("usePhone",true); // please use this code, if you want to show the phone number on DeviceAuthPage.

                startActivityForResult(intent, REQUEST_CODE_DEVICE_REGIST);
            } else {
                //content mode가 local 일경우 updateCheck를 진행한다.
                if (imageMode == ImageUtil.MODE_LOCAL) {
                    updateCheck();
                } else {
                    ImageUtil.setExternalResourceUrl(SettingModel.getModel(getActivity()).getContentsExternalRoot() + "/");
                    checkNotice();
                }
            }

        } else {
            if (!DeviceRegistUtil.isResisted() && registMode) {
                // 등록 페이지 옮기기
                Intent intent = new Intent(getActivity(), DeviceRegistActivity.class);
                //intent.putExtra("usePhone",true); // please use this code, if you want to show the phone number on DeviceAuthPage.
                startActivityForResult(intent, REQUEST_CODE_DEVICE_REGIST);
            } else {
                updateCheck();
            }
        }

        receiver = new ConnectivityReceiver(getActivity());
        receiver.setOnChangeNetworkStatusListener(listener);
        getActivity().registerReceiver(receiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        //크래쉬 로그 파일 30일이 지난 크래쉬 로그 파일은 삭제.
        CrashHandler.removeCrashLogFiles(30);
    }

    /**
     * 공지사항 전문(ZZ0007) 통신을 수행하는 메소드. <br>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    public void checkNotice() {
        if (AppConfig.useNotice) {
            final BMCManager manager = BMCManager.getInstance();
            manager.setFragment(this);
            manager.setFWebView(null);

            try {
                Logger.d(TAG, "공지사항, 공지사항 전문 통신 (ZZ0007) 시작.");
                JSONObject reqJson = new JSONObject();
                JSONObject reqParam = new JSONObject();
                JSONObject ZZ0007Obj = JsonUtil.addRequestHeader(null, "ZZ0007", "");
                ZZ0007Obj = JsonUtil.addRequestBody(ZZ0007Obj, "appKey", manager.getSetting().getAppKey());

                reqParam.put("message", ZZ0007Obj);
                reqParam.put("trcode", "ZZ0007");
                reqParam.put("read_timeout", 10000);
                reqParam.put("progress", true);
                reqJson.put("param", reqParam);

                //HttpPlugin 호출
                manager.executeInterfaceFromID("RELOAD_WEB", reqJson, new CompleteListener() {
                            /**
                             * HttpPlugin 수행 후 결과값을 리턴 받는 메소드. <br>
                             * <pre>
                             * 수정이력 <br>
                             * **********************************************************************************************************************************<br>
                             *  수정일                               이름                               변경내용<br>
                             * **********************************************************************************************************************************<br>
                             *  2016-09-22                           박재민                             최초 작성<br>
                             *
                             * </pre>
                             *
                             * @param type     사용하지 않음.
                             * @param callback 사용하지 않음.
                             * @param result   수행 성공 여부.
                             */
                            @Override
                            public void resultCallback(String type, String callback, JSONObject result) {
                                try {
                                    if (result.has("header")) {
                                        JSONObject headObj = result.getJSONObject("header");
                                        if (headObj.has("result")) { //통신 성공여부.
                                            boolean bResult = headObj.getBoolean("result");
                                            if (bResult) {
                                                if (result.has("body")) { // 바디 있음
                                                    bodyObj = result.getJSONObject("body");

                                                    if (bodyObj.has("list")) { // 공지 목록 있음.
                                                        JSONArray listArray = bodyObj.getJSONArray("list");

                                                        if (listArray != null) { // 공지 목록이 null이 아님
                                                            int listLength = listArray.length();

                                                            if (listLength > 0) { // 공지 목록이 1개 이상임

                                                                BMCManager manager = BMCManager.getInstance();
                                                                String strObj = manager.getFStorage().getString("AppShareArea", "{}");
                                                                JSONObject noticeObj = new JSONObject(strObj);

                                                                Logger.d(TAG, "### FStorage, AppShareArea data = " + strObj);

                                                                boolean bShowNotice = false;

                                                                for (int i = 0; i < listLength; i++) {
                                                                    String noticeId = listArray.getJSONObject(i).getString("noticeId");
                                                                    String toDay = bodyObj.getString("toDay");
                                                                    Long today = Long.parseLong(toDay);

                                                                    if (noticeObj.has(noticeId)) {
                                                                        Logger.d(TAG, noticeId + " notice Id와 일치하는 noticeId 있음");
                                                                        boolean bSkip = false;  //검색된 게시물을 스킵할지 여부.
                                                                        bSkip = checkSkipDay(noticeObj.getJSONObject(noticeId), today);

                                                                        if (!bSkip) { //하나라도 스킵안할 대상이 있다면,
                                                                            bShowNotice = true; // 공지창을 띄워야 한다.
                                                                            Logger.d(TAG, noticeId + " notice Id는 skip date 구간에 포함 되지 않음");
                                                                            break;
                                                                        }

                                                                    } else {
                                                                        Logger.d(TAG, noticeId + " notice Id와 일치하는 noticeId 없음");
                                                                        bShowNotice = true;
                                                                        break;
                                                                    }
                                                                }

                                                                if (bShowNotice) {
                                                                    showNotification(bodyObj);
                                                                } else {
                                                                    loadAppconfig();
                                                                }

                                                            } else {
                                                                loadAppconfig();
                                                            }
                                                        }
                                                    }

                                                }
                                            }
                                        } else {
                                            loadAppconfig();
                                        }
                                    } else {
                                        loadAppconfig();
                                    }
                                } catch (
                                        Exception e
                                        )

                                {
                                    loadAppconfig();
                                    e.printStackTrace();
                                }
                            }
                        }

                );

            } catch (Exception e) {
                e.printStackTrace();
                Logger.d(TAG, "공지사항, response::fail::" + e);
                loadAppconfig();
            }
        } else {
            loadAppconfig();
        }

    }

    /**
     * 공지사항 정보가 SkipDay 기간에 포함되는지 확인 하는 메소드.<br>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param jsonObject skip day 구간에 해당하는 start, end date 정보.
     * @param today      오늘 날짜 (yyyyMMddhhmmss)
     * @return true(포함), false(포함 되지 않음)
     */
    public boolean checkSkipDay(JSONObject jsonObject, long today) throws JSONException {
        boolean result = false;
        long startDate = 20161121000000L;
        long endDate = 20161123000000L;
        if (jsonObject.has("startDate")) {
            startDate = Long.parseLong(jsonObject.getString("startDate"));
        }

        if (jsonObject.has("endDate")) {
            endDate = Long.parseLong(jsonObject.getString("endDate"));
        }
        //시작일보다 크거나 같고, 종료일보다 작거나 같은 경우 (해당 기간내에 today가 포함되는 경우 true!)
        if (startDate <= today && today <= endDate) {
            result = true;
        }

        Logger.d(TAG, "check skipDay(" + result + ") = jsonObject =" + jsonObject.toString() + " , today = " + today);

        return result;
    }

    /**
     * 공지사항 정보를 가공하고, 공지사항 관련 이미지 다운 및 skip Day를 호출 하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param bodyObj 서버로 부터 전달 받은 공지사항 정보.
     */
    public void showNotification(JSONObject bodyObj) throws JSONException {
        JSONArray listArray = bodyObj.getJSONArray("list");
        Logger.d(TAG, "공지사항, showNotification() 진입, listArray = " + listArray.toString());

        ArrayList<JSONObject> imgUrlList = new ArrayList<JSONObject>();

        BMCManager manager = BMCManager.getInstance();

        String contentsMode = manager.getSetting().getContentsMode();
        String targetPathType = "internal";

        if (contentsMode.equals("1")) { // "local" 모드
            targetPathType = "internal";
        } else if (contentsMode.equals("2")) {  // "assets" 모드 (지원 못함)
            targetPathType = "assets";
        } else if (contentsMode.equals("4")) { //"external" 모드
            targetPathType = "external";  //이미지 다운 성공.
        } else if (contentsMode.equals("3")) {  //"web" 모드 (지원 못함)
            targetPathType = "web";
        }

        try {
            for (int i = 0; i < listArray.length(); i++) {
                JSONObject jsonObject = listArray.getJSONObject(i);  // single notice information.
                String imgUrl = "";
                String imgType = "UPLOAD";
                String contentType = "T";
                if (jsonObject.has("imgUrl")) {
                    imgUrl = jsonObject.getString("imgUrl");
                }

                String imgUpdateData = "";
                if (jsonObject.has("imgUpdateDate")) {
                    imgUpdateData = jsonObject.getString("imgUpdateDate");
                }

                if (jsonObject.has("imgType")) {
                    imgType = jsonObject.getString("imgType");
                }


                if (jsonObject.has("contentType")) {
                    contentType = jsonObject.getString("contentType");
                }

                if (imgUrl != null && (imgUrl.length() > 0) && imgType.equals("UPLOAD") && !contentType.equals("T")) {
                    JSONObject imgObj = new JSONObject();
                    imgObj.put("imgUrl", imgUrl);
                    imgObj.put("imgUpdateDate", imgUpdateData);
                    imgObj.put("noticeId", jsonObject.getString("noticeId"));
                    if (!contentsMode.equals(2) && !contentsMode.equals(3)) { //assets 모드와 web 모드 인경우. download 하지 않음.
                        imgUrlList.add(imgObj);
                    }

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.d(TAG, "공지사항, showNotification(), JSONException 발생 error_text = " + e.getMessage());
        }


        if (imgUrlList.size() > 0) {
            try {

                JSONObject reqObj = new JSONObject();
                JSONObject paramObj = new JSONObject();
                JSONObject progressObj = new JSONObject();
                progressObj.put("provider", "native"); // native 이면 프로그레스 보임.
                progressObj.put("title", "다운중..");
                progressObj.put("message", "공지이미지가 다운중입니다.");

                JSONArray uriListArray = new JSONArray();
                int imgUrlListSize = imgUrlList.size();
                JSONArray noticeList = bodyObj.getJSONArray("list");
                int noticeListLen = noticeList.length();

                JSONArray fileNames = new JSONArray();

                for (int i = 0; i < imgUrlListSize; i++) {

                    JSONObject imgObj = imgUrlList.get(i);
                    String imgUrl = imgObj.getString("imgUrl");
                    String imageFileName = imgObj.getString("imgUpdateDate");
                    String noticeId = imgObj.getString("noticeId");

                    //url에서 파일명+확장자만 빼오기.
                    imgUrl = imgUrl.split("&")[0];
                    int endIndex = imgUrl.length();
                    int startIndex = imgUrl.lastIndexOf(".");
                    String ext = imgUrl.substring(startIndex, endIndex);
                    String rootPath = "";
                    if (contentsMode.equals("4")) {
                        rootPath = ImageUtil.EXTERNAL_RESOURCE_URL + "/";
                    }
                    String targetPath = rootPath + "contents/bizMOB/notice/download/" + noticeId + "_" + imageFileName + ext;

                    String fullPath = FileUtil.getAbsolutePath(targetPathType, targetPath);

                    boolean isFileExist = FileUtil.fileExist(fullPath);

                    if (!isFileExist) {
                        Logger.d(TAG, "공지사항 이미지, " + targetPath + " 파일은 존재 하지 않습니다, 다운로드 항목에 추가 합니다.");
                        JSONObject singleDownloadInfoObj = new JSONObject();
                        singleDownloadInfoObj.put("uri", imgUrl); // String
                        singleDownloadInfoObj.put("file_id", 0); // int
                        singleDownloadInfoObj.put("overwrite", true); // boolean
                        singleDownloadInfoObj.put("target_path", targetPath); // String
                        singleDownloadInfoObj.put("target_path_type", targetPathType); // String

                        uriListArray.put(singleDownloadInfoObj);
                        String dirPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
                        JSONObject dirInfo = FileUtil.getDirectoryInfo(dirPath);
                        JSONArray dirArray = dirInfo.getJSONArray("directory_info");
                        int dirArrayLen = dirArray.length();

                        for (int k = 0; k < dirArrayLen; k++) {
                            JSONObject singleFileInfo = dirArray.getJSONObject(k);
                            String filePath = singleFileInfo.getString("file_path");
                            boolean isDirectory = singleFileInfo.getBoolean("is_directory");

                            if (!isDirectory) {
                                int sIndex = filePath.lastIndexOf("/");
                                int lIndex = filePath.length();
                                String curFileName = filePath.substring(sIndex + 1, lIndex);
                                String[] spiltedCurFileName = curFileName.split("_");
                                String splitedNoticeId = spiltedCurFileName[0];

                                //noticeId가 일치하는지 비교
                                if (splitedNoticeId.equals(noticeId)) {

                                    String splitedUpdateTime = spiltedCurFileName[1];
                                    int lastPoint = splitedUpdateTime.lastIndexOf(".");
                                    splitedUpdateTime = splitedUpdateTime.substring(0, lastPoint);
                                    Long oldTime = Long.parseLong(splitedUpdateTime);
                                    Long newTime = Long.parseLong(imageFileName);
                                    if (oldTime < newTime) { // 오래된 파일 삭제.
                                        File delFile = new File(filePath);
                                        if (delFile != null && delFile.exists()) {
                                            FileUtil.delete(delFile);
                                            Logger.d(TAG, "공지사항 이미지, 이전 버전을 삭제합니다. deleted file path = " + filePath);
                                        }
                                    }
                                }

                            }
                        }
                    } else {
                        Logger.d(TAG, "공지사항 이미지, " + targetPath + ", 파일은 이미 존재 합니다.");
                    }

                    JSONObject singleData = new JSONObject();
                    singleData.put("noticeId", noticeId);
                    singleData.put("imageFileName", imageFileName);
                    singleData.put("ext", ext);
                    fileNames.put(singleData);
                }

                //기존 리스트와 일치하는 noticeId를 갖은 녀석에게 imgName 값을 넣어준다.
                for (int j = 0; j < noticeListLen; j++) {
                    JSONObject jsonObject = noticeList.getJSONObject(j);
                    String oriNoticeId = jsonObject.getString("noticeId");
                    String imgName = "";

                    for (int h = 0; h < fileNames.length(); h++) {
                        JSONObject data = fileNames.getJSONObject(h);
                        String notiId = data.getString("noticeId");
                        if (notiId.equals(oriNoticeId)) {
                            String imageFileName = data.getString("imageFileName");
                            String ext = data.getString("ext");
                            imgName = notiId + "_" + imageFileName + ext;
                        }
                    }
                    jsonObject.put("imgName", imgName);
                    noticeList.put(j, jsonObject);
                }

                //이미지 저장 경로를 추가한다.
                this.bodyObj.put("list", noticeList);

                //이미 다운 받은 파일이 존재하는경우 파일 다운을 하지 않는다.
                if (uriListArray.length() > 0) {
                    Logger.d(TAG, "공지사항, 다운받을 이미지 파일이 존재합니다. 이미지를 다운로드 합니다. uri list = " + uriListArray.toString());
                    paramObj.put("callback", "callback");
                    paramObj.put("type", "full_list"); // "each_list" or "full_list";
                    paramObj.put("progress", progressObj); //프로그레스가 포함되면 바 형태로 진행률 표시가 됨.
                    paramObj.put("uri_list", uriListArray);
                    reqObj.put("param", paramObj);

                    manager.executeInterfaceFromID("DOWNLOAD", reqObj, new CompleteListener() {
                        @Override
                        public void resultCallback(String s, String s1, JSONObject jsonObject) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showNotice();
                                }
                            });

                        }
                    });
                } else {
                    Logger.d(TAG, "공지사항, 다운받을 이미지 파일이 존재하지 않습니다.");
                    showNotice();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            showNotice();
        }
    }

    /**
     * 실제 노티스 화면을 담당하는 웹뷰를 화면에 노출 시키는 메소드.<br>
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
    public void showNotice() {

        AppConfigReader.LoadAppConfig(getActivity(), APP_CONFIG);

        String noticeUrl = "bizMOB/notice/html/notice.html";
        int width = getSizePercent("width", 90);
        int height = getSizePercent("height", 90);
        showPopupview(noticeUrl, width, height, bodyObj);
        popupBackAction = true;
        popupWebView.isNoticeWebView = true;
    }

    public void showPopupview(final String targetPage, final int width, final int height, final JSONObject data) {
        this.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                NativeLauncherFragment.this.showingPopupView = true;
                LinearLayout main_view = (LinearLayout) NativeLauncherFragment.this.wrapper.findViewById(RUtil.getIdR(NativeLauncherFragment.this.getActivity(), "main_view"));
                main_view.setClickable(false);
                RelativeLayout rl = (RelativeLayout) NativeLauncherFragment.this.wrapper.findViewById(RUtil.getIdR(NativeLauncherFragment.this.getActivity(), "popup"));
                rl.setBackgroundColor(AppConfig.WEBVIEW_DIALOGOUTSIDECOLOR);
                if (!targetPage.equals("")) {
                    NativeLauncherFragment.this.popupWebView = new BMCWebView(NativeLauncherFragment.this);
                    try {
                        if (data.has("hardware_accelator")) {
                            if (!data.getBoolean("hardware_accelator")) {
                                NativeLauncherFragment.this.popupWebView.setLayerType(1, (Paint) null);
                            }
                            data.remove("hardware_accelator");
                        }
                    } catch (JSONException var5) {
                        var5.printStackTrace();
                    }

                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(width, height);
                    rl.addView(NativeLauncherFragment.this.popupWebView, lp);
                    NativeLauncherFragment.this.getArguments().putString("data", data.toString());
                    String url = ImageWrapper.getUri(targetPage);
                    NativeLauncherFragment.this.popupWebView.loadUrl(url);
                }

                rl.bringToFront();
                rl.setGravity(Gravity.CENTER);
                rl.setVisibility(View.VISIBLE);
                rl.setClickable(true);
                NativeLauncherFragment.this.popupWebView.setBackgroundColor(0);
            }
        });
    }

    /**
     * %를 int로 변환해주는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param type    가로 인지 세로 인지 "width", "height".
     * @param percent % 값(0~100).
     * @return int로 변환된  % 값.
     */

    private int getSizePercent(String type, int percent) {

        int screen_width = 0;
        int screen_height = 0;

        // % 적용
        Configuration config = getActivity().getResources().getConfiguration();

        int statusBarHeight = 0;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        statusBarHeight = (int) Math.ceil(25 * getActivity().getResources().getDisplayMetrics().density);

        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            screen_width = displayMetrics.widthPixels;
            screen_height = displayMetrics.heightPixels - statusBarHeight;
        } else {
            screen_width = displayMetrics.heightPixels - statusBarHeight;
            screen_height = displayMetrics.widthPixels;
        }
        // End

        int returnValue = 0;
        if (type.equals("width")) {
            returnValue = screen_width * percent / 100;
        } else if (type.equals("height")) {
            returnValue = screen_height * percent / 100;
        }

        return returnValue;
    }


    ConnectivityReceiver.onChangeNetworkStatusListener listener = new ConnectivityReceiver.onChangeNetworkStatusListener() {

        @Override
        public void onChanged(int status) {

            switch (status) {
                case ConnectivityReceiver.WIFI_STATE_DISABLED:
                    Log.i("", "[WifiMonitor] WIFI_STATE_DISABLED");
                    break;
                case ConnectivityReceiver.WIFI_STATE_DISABLING:
                    Log.i("", "[WifiMonitor] WIFI_STATE_DISABLING");
                    break;
                case ConnectivityReceiver.WIFI_STATE_ENABLED:
                    Log.i("", "[WifiMonitor] WIFI_STATE_ENABLED");
                    break;
                case ConnectivityReceiver.WIFI_STATE_ENABLING:
                    Log.i("", "[WifiMonitor] WIFI_STATE_ENABLING");
                    break;
                case ConnectivityReceiver.WIFI_STATE_UNKNOWN:
                    Log.i("", "[WifiMonitor] WIFI_STATE_UNKNOWN");
                    break;
                case ConnectivityReceiver.NETWORK_STATE_CONNECTED:
                    Log.i("", "[WifiMonitor] NETWORK_STATE_CONNECTED");
                    break;
                case ConnectivityReceiver.NETWORK_STATE_CONNECTING:
                    Log.i("", "[WifiMonitor] NETWORK_STATE_CONNECTING");
                    break;
                case ConnectivityReceiver.NETWORK_STATE_DISCONNECTED:
                    Log.i("", "[WifiMonitor] NETWORK_STATE_DISCONNECTED");
                    break;
                case ConnectivityReceiver.NETWORK_STATE_DISCONNECTING:
                    Log.i("", "[WifiMonitor] NETWORK_STATE_DISCONNECTING");
                    break;
                case ConnectivityReceiver.NETWORK_STATE_SUSPENDED:
                    Log.i("", "[WifiMonitor] NETWORK_STATE_SUSPENDED");
                    break;
                case ConnectivityReceiver.NETWORK_STATE_UNKNOWN:
                    Log.i("", "[WifiMonitor] WIFI_STATE_DISABLED");
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DEVICE_REGIST) {

            if (resultCode == Activity.RESULT_OK) {
                updateCheck();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                getActivity().finish();
            }
            // 권한 거부 -> 에서 설정 버튼 눌러 진입했다가 다시 들어온 경우에 대한 처리
        } else if (requestCode == REQ_CODE_PERMISSION_REQUIRED) {
            bmcRequiredPermission = new BMCPermission.Builder(getActivity(), REQ_CODE_PERMISSION_REQUIRED, requiredPermissions)
                    .setFragment(NativeLauncherFragment.this)
                    .setDeniedTitle("퍼미션 경고")
                    .setDeniedMessage("해당 권한은 앱 구동을 위해 필수로 필요한 권한입니다. \n(설정→권한→전화, 저장→On)").build();
            if (bmcRequiredPermission.checkPermissions()) {
                afterPermissionCheck();
            }
        }
    }

    private void updateCheck() {
        try {
            BMCManager manager = BMCManager.getInstance();
            manager.setFragment(this);
            manager.executeInterfaceFromID("NATIVE_UPDATE", new JSONObject(), new updateCallback());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    final class updateCallback implements CompleteListener {

        @Override
        public void resultCallback(String type, String callback, JSONObject result) {
            try {
                boolean offline = SettingModel.getModel(getActivity()).getOfflineMode();
                if (result.has("result")) {
                    if (result.getBoolean("result") == false) {
                        String error_text = result.getString("error_text");
                        if (offline == false) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("ERROR")
                                    .setMessage(error_text)
                                    .setPositiveButton(getActivity().getString(RUtil.getStringR(getActivity(), "txt_ok")),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    terminate();
                                                }
                                            }).create().show();
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    moveToHomeFragment();
                                }
                            });
                        }

                        Logger.d(TAG, "offline mode " + offline);
                        Logger.d(TAG, "error_text : " + error_text);
                    }
                } else {
                    //Launcher 화면이후 이동할 Fragment 명시.
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            moveToHomeFragment();
                        }
                    }, 1000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void moveToHomeFragment() {
        Logger.i(TAG, "#################### moveToHomeFragment #########");

        try {

            JSONObject data = new JSONObject();
            JSONObject param = new JSONObject();

            data.put("id", "SHOW_NATIVE");
            data.put("param", param);

            param.put("class_name", "com.mcnc.bizmob.view.HomeFragment"); // The specific full class name of it.
            param.put("message", new JSONObject());
            param.put("orientation", "portrait"); // The screen orientation, "portrait" = vertical, "land" = horizontal
            param.put("page_name", "locale"); // A unique page name need to be defined.
            param.put("animation", true); // true or false (to set animation effect or not.)
            param.put("effect", "right"); // The animation effect direction, "top", " bottom", "right", "left", "fade" , "none"

            manager = BMCManager.getInstance();
            manager.setFragment(this);
            manager.executeInterfaceFromID("SHOW_NATIVE", data, new CompleteListener() {

                @Override
                public void resultCallback(String type, String callback, JSONObject param) {
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void runIntroBgAnimation() {
        if (BMCInit.useIntroBg) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), RUtil.getR(getActivity(),"anim","anim_fade_out"));
                    fadeOutAnim.setDuration(300);
                    fadeOutAnim.setFillAfter(true);
                    fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ((NativeSlideFragmentActivity)getActivity()).getRlIntroBg().setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    ((NativeSlideFragmentActivity)getActivity()).getRlIntroBg().startAnimation(fadeOutAnim);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        Logger.d(TAG, "requestCode = " + requestCode + " , permissions = " + permissions + " , grantResults = " + grantResults.toString());
        if (requestCode == REQ_CODE_PERMISSION_REQUIRED) {
            ArrayList<String> deniedPermissionList = new ArrayList<String>();
            String deniedPermission = null;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermission = permissions[i];
                    deniedPermissionList.add(deniedPermission);
                }
            }

            if (deniedPermissionList.size() > 0) {
                bmcRequiredPermission.showDeniedDialog();
            } else {
                afterPermissionCheck();
            }
        }else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    /**
     * 앱종료시 얼럿 다이얼로그를 호출하는 메소드.<br>
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
    public void showTerminateAlarm(final String message, final String title) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(getActivity().getString(RUtil.getStringR(getActivity(), "txt_ok")),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        terminate();
                                        dialog.dismiss();
                                    }
                                }

                        )
                        .setCancelable(false)
                        .create().show();
            }
        });
    }
}
