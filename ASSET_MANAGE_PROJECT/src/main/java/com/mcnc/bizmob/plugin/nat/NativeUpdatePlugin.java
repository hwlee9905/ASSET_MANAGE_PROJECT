package com.mcnc.bizmob.plugin.nat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import androidx.core.content.FileProvider;
import androidx.core.content.pm.PackageInfoCompat;

import com.mcnc.bizmob.core.def.Def;
import com.mcnc.bizmob.core.download.AbstractDownloadService;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.file.ZIPUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.ImageUtil;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.view.ntv.NativeLauncherFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * 01.클래스 설명 : 네이티브 화면에서 앱, 컨텐츠 업데이트 기능을 수행하는 플러그인. <br>
 * 02.제품구분 : bizMob 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 네이티브 앱, 컨텐츠 업데이트 <br>
 * 04.관련 API/화면/서비스 : Def, AbstractDownloadService, BMCUtil, JsonUtil, ZIPUtil, Logger, ImageUtil, RUtil, DownloadPlugin, HttpPlugin <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-21                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class NativeUpdatePlugin extends BMCPlugin {

    /**
     * class 명
     */
    private final String TAG = "UpgradePlugin";

    /**
     * internal 영역의 root 경로
     */
    private final String RESOURCE_ROOT = ImageUtil.ROOT_PATH;

    /**
     * internal 영역의 root path + "contents/" 경로
     */
    private final String CONTENT_ROOT = ImageUtil.ROOT_PATH + ImageUtil.CONTENT_ROOT + "/";

    /**
     * SD Card 영역의 download 폴더 경로
     */
    private final String DOWNLOAD_ROOT = AbstractDownloadService.getDownloadPath();

    /**
     * SD Card 영역의 meta 폴더 경로
     */
    private final String META_ROOT = Environment.getExternalStorageDirectory() + "/meta/";

    /**
     * 백업용 컨텐츠 Zip 파일의 접미사
     */
    private final String BACKUP_CONTENT = "_cbak.zip";

    /**
     * 백업용 메타 Zip 파일의 접미사
     */
    private final String BACKUP_META = "_mbak.zip";

    /**
     * content type 임을 나타내는 문자열
     */
    private final String CONTEXT_CONTENT = "content";

    /**
     * application type 임을 나타내는 문자열
     */
    private final String CONTEXT_APP = "app";

    /**
     * 업데이트 백업기능 사용여부
     */
    private final boolean backup_flag = false;

    /**
     * 로그용 문자열
     */
    private static final String APP_LOCATION = "app Location :";
    /**
     * 로그용 문자열
     */
    private static final String APP_UPGRADE = "AppUpgrade";

    /**
     * 기본 버퍼 사이즈
     */
    private static final int DEFAULT_BUFFER_SIZE = 8000;

    /**
     * 폰인지 타블렛이지의 값 "PHONE" or "TABLET"
     */
    private static String display_type = "";

    /**
     * Major인지 아닌지 여부
     */
    boolean bMajar = false;

    /**
     * 앱 파일의 이름
     */
    String appFilename;

    /**
     * Major Contents 파일의 이름
     */
    String contentMajorFilename;

    /**
     * Minor Contents 파일의 이름
     */
    String contentMinorFilename;

    /**
     * Major meta 파일의 이름
     */
    String metaMajorFilename;

    /**
     * Minor meta 파일의 이름
     */
    String metaMinorFilename;

    /**
     * 앱 Major 버전 값
     */
    int appMajor;

    /**
     * 앱 Minor 버전 값
     */
    int appMinor;

    /**
     * 앱 Build 버전 값
     */
    int appBuild;

    /**
     * contents Major 버전 값
     */
    int contentMajor;

    /**
     * contents Minor 버전 값
     */
    int contentMinor;

    /**
     * meta Major 버전 값
     */
    int metaMajor;

    /**
     * meta Minor 버전 값
     */
    int metaMinor;

    /**
     * 앱 업데이트 필요 여부
     */
    boolean appResult;

    /**
     * contents 업데이트 필요 여부
     */
    boolean contentResult;

    /**
     * meta 업데이트 필요 여부
     */
    boolean metaResult;

    /**
     * 업데이트 (ZZ0006) 전문 통신 후 결과
     */
    JSONObject httpResult;

    /**
     * callback 줄 function 명
     */
    String resultCallback = "";

    /**
     * server ip
     */
    String root_url_ip = "";

    /**
     * server port
     */
    String root_url_port = "";

    /**
     * server context
     */
    String root_url_dir = "";

    /**
     * server ssl 사용 여부
     */
    boolean is_ssl = false;

    /**
     * update server ip
     */
    String root_download_url_ip = "";

    /**
     * update server port
     */
    String root_download_url_port = "";

    /**
     * update server context
     */
    String root_download_url_dir = "";

    /**
     * update server ssl 사용 여부
     */
    boolean is_ssl_download = false;

    NativeLauncherFragment launcherFragment;

    /**
     * 서버 / 업데이트 서버 정보 / 업데이트 유무 체크에 버전체크용 오브젝트 생성 후 HttpPlugin에 업데이트 전문(ZZ0006)을 요청하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data callback function 정보
     */
    @Override
    public void executeWithParam(JSONObject data) {

        launcherFragment = (NativeLauncherFragment) getFragment();


        JSONObject param = null;

        try {
            param = data.getJSONObject("param");
            if (param.has("callback")) {
                resultCallback = param.getString("callback");
            }
        } catch (Exception e) {

        }

        // 반드시 IP를 되돌려 놓아야 함.
        // 1. backup
        root_url_ip = getManager().getSetting().getServerIP();
        root_url_port = getManager().getSetting().getServerPort();
        root_url_dir = getManager().getSetting().getServerContext();
        is_ssl = getManager().getSetting().getServerSSL();
        root_download_url_ip = getManager().getSetting().getUpdateIP();
        root_download_url_port = getManager().getSetting().getUpdatePort();
        root_download_url_dir = getManager().getSetting().getUpdateContext();
        is_ssl_download = getManager().getSetting().getUpdateSSL();

        try {

            ipBackup();

            JSONObject reqJson = new JSONObject();
            JSONObject reqParam = new JSONObject();

            // 버전 체크 데이터를 만듬
            JSONObject sendObj = makeVersionCheckObject(getActivity());

            reqParam.put("message", sendObj);
            reqParam.put("trcode", "ZZ0006");
            reqParam.put("read_timeout", 10000);
            reqParam.put("progress", false);

            //프로그레스 대신 체크 한다는 화면만 보여줌.
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    launcherFragment.progressBarDescription.setVisibility(View.VISIBLE);
                }
            });

            reqJson.put("param", reqParam);

            //HttpPlugin 호출
            getManager().executeInterfaceFromID("RELOAD_WEB", reqJson, new httpCallback());

        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "response::fail::" + e);

            IPRollback();
        }
    }


    /**
     * IP 정보를 백업 시키는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    private void ipBackup() {

        // 2. 변경
        getManager().getSetting().setServerIP(root_download_url_ip);
        getManager().getSetting().setServerPort(root_download_url_port);
        getManager().getSetting().setServerContext(root_download_url_dir);
        getManager().getSetting().setServerSSL(is_ssl_download);

        Def.SERVER_IP = Def.UPDATE_URL_IP;
        Def.SERVER_PORT = Def.UPDATE_URL_PORT;
        Def.SERVER_CONTEXT = Def.UPDATE_URL_DIR;
        Def.IS_SSL = Def.UPDATE_URL_SSL;
        Def.DOWNLOAD_URL_IP = Def.UPDATE_URL_IP;
        Def.DOWNLOAD_URL_PORT = Def.UPDATE_URL_PORT;
        Def.DOWNLOAD_URL_DIR = Def.UPDATE_URL_DIR;
        Def.IS_SSL_DOWNLOAD = Def.UPDATE_URL_SSL;
        Logger.i(TAG, "IP UPDATE로 변경!!!!");
    }

    /**
     * IP 정보를 원복 시키는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    private void IPRollback() {

        // 3. 원복  finally
        getManager().getSetting().setServerIP(root_url_ip);
        getManager().getSetting().setServerPort(root_url_port);
        getManager().getSetting().setServerContext(root_url_dir);
        getManager().getSetting().setServerSSL(is_ssl);

        Def.SERVER_IP = root_url_ip;
        Def.SERVER_PORT = root_url_port;
        Def.SERVER_CONTEXT = root_url_dir;
        Def.IS_SSL = is_ssl;
        Def.DOWNLOAD_URL_IP = root_download_url_ip;
        Def.DOWNLOAD_URL_PORT = root_download_url_port;
        Def.DOWNLOAD_URL_DIR = root_download_url_dir;
        Def.IS_SSL_DOWNLOAD = is_ssl_download;
        Logger.i(TAG, "IP 원복!!!!");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                launcherFragment.progressBarDescription.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * 업데이트 체크에 필요한 앱 버전, 컨텐츠 버전 정보등을 담은 JSONObject를 생성하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param context TELEPHONY_SERVICE 이용을 위한 context 객체
     * @return 버전 체크용 JSONObject
     */
    private JSONObject makeVersionCheckObject(Context context) {
        // 사용자가 폴더 지웠을 경우 version 0으로...
        File f = new File(CONTENT_ROOT);
        if (!f.exists()) {
            getManager().getSetting().setContentsMajorVersion("0");
            getManager().getSetting().setContentsMinorVersion("0");
        }

        int content_major_version = Integer.parseInt(getManager().getSetting().getContentsMajorVersion());
        int content_minor_version = Integer.parseInt(getManager().getSetting().getContentsMinorVersion());
        int meta_major_version = Integer.parseInt(getManager().getSetting().getMetaMajorVersion());
        int meta_minor_version = Integer.parseInt(getManager().getSetting().getMetaMinorVersion());
        boolean app_tester = getManager().getSetting().getAppTester();

        String os_version = "";

        JSONObject sendObj = null;
        try {
            os_version = Build.VERSION.RELEASE; //ex) 4.4.1
            String[] arr = os_version.split("\\.");
            if (arr.length == 1) {
                os_version += ".0";
            } else {
                os_version = arr[0] + "." + arr[1];
            }

            // App Version Menifest.xml android:versionName 을   x.x.x 로 표시하기 바람.
            // major 와 minor의 경우 versionName 을 따라감 주의 요망
            int appMajorVersion = 1;
            int appMinorVersion = 0;
            int appBuildVersion = 0;
            try {
                appBuildVersion = (int) PackageInfoCompat.getLongVersionCode(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0));
                String appVerString = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                java.util.StringTokenizer strToken = new java.util.StringTokenizer(appVerString, ".");
                if (strToken.hasMoreTokens()) {
                    String token = strToken.nextToken();
                    appMajorVersion = Integer.parseInt(token.trim());
                }
                if (strToken.hasMoreTokens()) {
                    String token = strToken.nextToken();
                    appMinorVersion = Integer.parseInt(token.trim());
                }
                // Version String 우선으로 변경.
                if (strToken.hasMoreTokens()) {
                    String token = strToken.nextToken();
                    appBuildVersion = Integer.parseInt(token.trim());
                } else {
                    appBuildVersion = (int) PackageInfoCompat.getLongVersionCode(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0));
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            display_type = BMCUtil.isTablet(context) ? "TABLET" : "PHONE";

            sendObj = JsonUtil.addRequestHeader(sendObj, "ZZ0006", "");
            sendObj = JsonUtil.addRequestBody(sendObj, "app_key", getManager().getSetting().getAppKey());
            sendObj = JsonUtil.addRequestBody(sendObj, "content_minor_version", content_minor_version);
            sendObj = JsonUtil.addRequestBody(sendObj, "app_build_version", appBuildVersion);
            sendObj = JsonUtil.addRequestBody(sendObj, "app_tester", app_tester);
            sendObj = JsonUtil.addRequestBody(sendObj, "app_minor_version", appMinorVersion);
            sendObj = JsonUtil.addRequestBody(sendObj, "os_version", os_version);
            sendObj = JsonUtil.addRequestBody(sendObj, "content_major_version", content_major_version);
            sendObj = JsonUtil.addRequestBody(sendObj, "app_major_version", appMajorVersion);
            sendObj = JsonUtil.addRequestBody(sendObj, "meta_minor_version", meta_minor_version);
            sendObj = JsonUtil.addRequestBody(sendObj, "os_type", Def.OS_TYPE_NAME);
            sendObj = JsonUtil.addRequestBody(sendObj, "display_type", display_type);
            sendObj = JsonUtil.addRequestBody(sendObj, "meta_major_version", meta_major_version);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendObj;
    }

    /**
     * 파일 다운로드에 필요한 정보를 담은 JSONObject를 생성하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param filename 파일명
     * @param uri      서버 정보
     * @param method   사용하지 않음
     * @param message  app, content major, minor 다운 여부에 대한 문자열 (로깅 위해 사용)
     * @return 파일 다운로드 정보를 담은 JSONObject
     */
    private JSONObject makeDownloadFileObject(String filename, String uri,
                                              String method, String message) {
        JSONObject sendObj = null;
        try {
            // sendObj = JsonUtil.addRequestHeader(sendObj, Def.TRCODE_LOGIN ,
            // "sessionid1");
            sendObj = JsonUtil.addRequestHeader(sendObj, "", "sessionid1");
            JSONObject paramObj = new JSONObject();
            sendObj.put("param", paramObj);
            paramObj.put("display_name", filename);
            paramObj.put("uri", uri);

            JSONObject jo = new JSONObject();
            JSONArray ja = new JSONArray();

            jo.put("uri", uri);
            jo.put("file_id", 0);
            jo.put("target_path", DOWNLOAD_ROOT + filename);
            jo.put("target_path_type", "external");
            jo.put("overwrite", true);

            ja.put(jo);

            paramObj.put("uri_list", ja);
            Logger.d("UpdatePlugin", "makeDownloadFileObject() message = " + message);

            JSONObject progProg = new JSONObject();
            progProg.put("title", "");
            progProg.put("message", message);
            progProg.put("provider", "native");

            paramObj.put("callback", "");
            paramObj.put("type", "full_list");
            paramObj.put("progress", progProg);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendObj;
    }

    /**
     * 파일 다운로드용 오브젝트 생성하는 메소드를 호출하고, DownloadPlugin을 호출 하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param json       사용하지 않음.
     * @param fileName   파일명
     * @param subContext 앱인지 컨텐츠인지 종류 ("app" or "content")
     * @param msg        다운로드 받을 파일의 종류 표기 ( app major, minor, contents major, minor)
     * @param listener   파일 다운로드 수행후 결과를 받을 listener.
     * @return 로직 수행 성공여부
     */
    private boolean DownLoadReqeust(JSONObject json, String fileName, String subContext, final String msg, CompleteListener listener) throws Exception {
        boolean result = true;

        try {

            JSONObject jsonObj = makeDownloadFileObject(fileName, Def.CONTENT_UPGRADE_URI
                            + "/" + subContext + "?file_name="
                            + fileName + "&mode=1" + "&" + Def.OS_TYPE + "=" + Def.OS_TYPE_NAME + "&display_type=" + display_type,
                    "direct",
                    msg);

            Logger.i(TAG, jsonObj.toString());

            getManager().executeInterfaceFromID("NATIVE_DOWNLOAD", jsonObj, listener);
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 다운로드된 파일을 삭제하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param fileName 파일명
     */
    private void DeleteDownloadFile(String fileName) {
        String path = DOWNLOAD_ROOT + fileName;
        File f = new File(path);
        if (f.exists()) {
            Logger.i(TAG, fileName + "  Exist File Delete");
            f.delete();
        }
        return;
    }

    /**
     * 기존 파일을 백업하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param json      사용핳지 않음.
     * @param srcDir    백업할 폴더 경로
     * @param tagetpath 백업한 zip파일을 저장할 경로
     * @return 로직 수행 성공여부
     */
    private boolean Backup(JSONObject json, String srcDir, String tagetpath) {
        // 백업기능 사용안할 경우
        if (backup_flag == false)
            return true;

        File sDir = new File(srcDir);
        File tFile = new File(tagetpath);

        if (tFile.exists() == true) {
            tFile.delete();
        }

        // 압축할 파일이 없으면 true
        if (sDir.exists() == false) {
            return true;
        }
        try {
            showDialog("progress", getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_title")), getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_backup")));
            ZIPUtil.compress(sDir, tFile);
            dismissDialog("progress");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 기존 파일을 복구하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param file 백업 파일의 경로
     * @return 로직 수행 성공여부
     */
    private boolean Restore(String file) {
        boolean result = true;

        // 백업기능 사용안할 경우
        if (backup_flag == false)
            return true;

        String zipPath = "";
        String unzipPath = "";

        if (file.equals(BACKUP_CONTENT)) {
            zipPath = DOWNLOAD_ROOT + BACKUP_CONTENT;
            unzipPath = CONTENT_ROOT;
        } else {
            zipPath = DOWNLOAD_ROOT + BACKUP_META;
            unzipPath = META_ROOT;
        }

        File zip = new File(zipPath);
        File unzip = new File(unzipPath);

        try {
//			result = ZIPUtil.decompress(zip, unzip);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * 다운로드 받은 파일의 압축을 풀고, 압축 파일을 삭제하는 메소드를 호출하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param dir      압축 해제할 폴더 경로
     * @param fileName 다운로드 받은 파일 이름
     * @return 로직 수행 성공여부
     */
    private boolean updateSave(String dir, String fileName) {
        boolean result = true;
        String zipPath = DOWNLOAD_ROOT + fileName;
        String unzipPath = dir;

        File zip = new File(zipPath);
        File unzip = new File(unzipPath);

        try {
            result = ZIPUtil.decompress(zip, unzip);
            // 다운로드 받은 파일은 지워버림
            DeleteDownloadFile(fileName);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
//			activity.removeDialog(AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
        }
        return result;
    }

    /**
     * 앱 버전 업그레이드(설치) 기능을 수행하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param activity openFileOutput()메소드 활용을 위한 activity 객체
     * @param fileName 업데이트할 상위 버전의 apk 파일 이름
     */
    private void AppUpgrade(Activity activity, String fileName) throws Exception {
        InputStream fileInputStream = null;
        BufferedOutputStream bos = null;
        File realFilePath = null;
        String targetPath = Environment.getExternalStorageDirectory() + "/" + Def.INSTALL_APP_NAME;
        File f = new File(DOWNLOAD_ROOT + fileName);
        try {
            fileInputStream = new FileInputStream(f);
            // 6MB가 넘는 파일은 sd로 복사 후 설치함.
            if (fileInputStream.available() > 6 * 1024 * 1024) {
                copyFile(f.getAbsolutePath(), targetPath);
                realFilePath = new File(targetPath);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                targetPath = getActivity().getExternalFilesDir("Download").getPath() + "/" + Def.INSTALL_APP_NAME;
                copyFile(f.getAbsolutePath(), targetPath);
                realFilePath = new File(targetPath);
            } else {
                int nRead = 0;
                bos = new BufferedOutputStream(activity.openFileOutput(Def.INSTALL_APP_NAME,
                        Context.MODE_PRIVATE), DEFAULT_BUFFER_SIZE);
                byte[] buf = new byte[10240];
                while ((nRead = fileInputStream.read(buf)) != -1) {
                    bos.write(buf, 0, nRead);
                }
                bos.flush();
                bos.close();
                bos = null;

                fileInputStream.close();
                fileInputStream = null;

                realFilePath = activity.getBaseContext().getFileStreamPath(Def.INSTALL_APP_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Close the BufferedOutputStream
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            f.delete();
        }
        Logger.i(APP_UPGRADE, APP_LOCATION + realFilePath);

        appInstalledFromApk(realFilePath);
    }

    private void appInstalledFromApk(File realFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(realFile);
        //24 이상인 경우.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", realFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.setDataAndType(uri,
                Def.APPLICATION_VND_ANDROID_PACKAGE_ARCHIVE);
        getActivity().startActivity(intent);
    }

    /**
     * 파일을 복사하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param sourcePath 원본 파일 경로
     * @param targetPath 복사할 파일 경로
     * @return 로직 수행 결과
     */
    private boolean copyFile(String sourcePath, String targetPath) throws Exception {
        File f = new File(sourcePath);
        if (!f.exists()) {
            Logger.i(TAG, "File not Found : " + sourcePath);
            return false;
        }
        FileInputStream inputStream = null; //임시파일을 복사할 때 원본 파일을 읽어오기 위해 선언
        FileOutputStream outputStream = null; //임시파일을 복사할 때 원본 파일을 임시파일에 쓰기 위해 선언

        File fromFile = new File(sourcePath);
        File toFile = new File(targetPath);
        //FileUtils.fileCopy(path, newPath);
        try {
            inputStream = new FileInputStream(fromFile);
            outputStream = new FileOutputStream(toFile);
            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();
            long size = fcin.size();
            fcin.transferTo(0, size, fcout);
            fcout.close();
            fcin.close();
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return true;
    }

    /**
     * 파일을 삭제하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param file 삭제할 파일 경로
     */
    private void delete(File file) {
        Logger.i("FileUtil", "file.getAbsolutePath()" + file.getAbsolutePath());
        if (file.getAbsolutePath() == "") {
            Logger.i("FileUtil", "Error delete file" + file);
            return;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
                Logger.i("FileUtil", "delete directory" + f.getAbsolutePath()
                        + ", name:" + f.getName());
                try {
                    f.delete(); // delete dir
                } catch (Exception e) {
                }
            }
        } else {
            try {
                Logger.i("FileUtil", "delete file" + file.getAbsolutePath()
                        + ", name:" + file.getName());
                file.delete();
            } catch (Exception e) {
            }
        }
    }


    /**
     * 프로세스를 강제로 종료 시키는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param context ACTIVITY_SERVICE 서비스 이용을 위한 activity 객체
     */
    private void requestKillProcess(final Activity context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                String name = context.getApplicationInfo().processName;

                while (true) {
                    List<RunningAppProcessInfo> list = am
                            .getRunningAppProcesses();
                    for (RunningAppProcessInfo i : list) {
                        if (i.processName.equals(name) == true) {
                            if (i.importance >= RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                                String pname = context.getPackageName();
                                am.killBackgroundProcesses(pname);
                            } else {
                                Thread.yield();
                            }
                            break;
                        }
                    }
                }
            }
        }, "Process Killer").start();
    }


    /**
     * 01.클래스 설명 : HttpPlugin 실행 후 결과 값을 받는 listener. <br>
     * 02.제품구분 : bizMob 3.0 Android Container <br>
     * 03.기능(콤퍼넌트) 명 : listener <br>
     * 04.관련 API/화면/서비스 : BMCManager <br>
     * 05.수정이력  <br>
     * <pre>
     * **********************************************************************************************************************************<br>
     *  수정일                                          이름                          변경 내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                                     박재민                         최초 작성<br>
     * **********************************************************************************************************************************<br>
     * </pre>
     *
     * @author 박재민
     * @version 1.0
     */
    final class httpCallback implements CompleteListener {

        /**
         * HttpPlugin을 통해 update 필요 유무 확인 작업을 거친 후,파일 다운로드 메소드를 호출 하는 메소드. <br>
         * <p>
         * <pre>
         * 수정이력 <br>
         * **********************************************************************************************************************************<br>
         *  수정일                               이름                               변경내용</br>
         * **********************************************************************************************************************************<br>
         *  2016-09-21                           박재민                             최초 작성</br>
         *
         *
         * @param type Callback Type // 미사용.
         * @param callback callback 명.
         * @param result HttpPlugin을 실행 후 리턴 받은 JSONObject 데이터 업데이트 필요 여부 값 (ex. 포탈에 올라간 버전 정보 등)<br> <br>
         * Return : (JSONObject) 에러 발생시 에러데이터
         */
        @Override
        public void resultCallback(String type, String callback, JSONObject result) {

            boolean isContentsUpdate = false;
            httpResult = result;
            JSONObject rtnResult = new JSONObject();

            try {

                if (result != null) {
                    JSONObject header = result.getJSONObject("header");
                    Logger.i(TAG, "header::" + header);
                    if (header != null) {
                        if (header.getBoolean("result")) {
                            // 업데이트 버전 결과 체크
                            Logger.i(TAG, "result:trCode:" + result.toString());
                            JSONObject body = null;
                            body = result.getJSONObject("body");

                            // App
                            appResult = body.getBoolean("app_result");
                            appFilename = body.getString("app_filename");
                            appMajor = body.getInt("app_major_version");
                            appMinor = body.getInt("app_minor_version");
                            appBuild = body.getInt("app_build_version");

                            // Contents
                            contentResult = body.getBoolean("content_result");
                            contentMajorFilename = body.getString("content_major_filename");
                            contentMinorFilename = body.getString("content_minor_filename");
                            contentMajor = body.getInt("content_major_version");
                            contentMinor = body.getInt("content_minor_version");

                            // Meta
                            metaResult = body.getBoolean("meta_result");
                            metaMajorFilename = body.getString("meta_major_filename");
                            metaMinorFilename = body.getString("meta_minor_filename");
                            metaMajor = body.getInt("meta_major_version");
                            metaMinor = body.getInt("meta_minor_version");

                            StringBuffer buffer = new StringBuffer();
                            buffer.append("============== Version Result ==================\n");
                            buffer.append("appResult: " + appResult + "\n");
                            buffer.append("appFilename: " + appFilename + "\n");
                            buffer.append("appMajor: " + appMajor + " - "
                                    + getManager().getSetting().getAppMajorVersion() + "\n");
                            buffer.append("appMinor: " + appMinor + " - "
                                    + getManager().getSetting().getAppMinorVersion() + "\n");
                            buffer.append("appBuild: " + appBuild + " - "
                                    + getManager().getSetting().getAppMajorVersion() + "\n");
                            buffer.append("contentResult: " + contentResult
                                    + "\n");
                            buffer.append("contentMajorFilename: "
                                    + contentMajorFilename + "\n");
                            buffer.append("contentMinorFilename: "
                                    + contentMinorFilename + "\n");
                            buffer.append("contentMajor: " + contentMajor
                                    + " - " + getManager().getSetting().getContentsMajorVersion()
                                    + "\n");
                            buffer.append("contentMinor: " + contentMinor
                                    + " - " + getManager().getSetting().getContentsMinorVersion()
                                    + "\n");
                            buffer.append("metaResult: " + metaResult + "\n");
                            buffer.append("metaMajorFilename: "
                                    + metaMajorFilename + "\n");
                            buffer.append("metaMinorFilename: "
                                    + metaMinorFilename + "\n");
                            buffer.append("metaMajor: " + metaMajor + " - "
                                    + getManager().getSetting().getContentsMajorVersion() + "\n");
                            buffer.append("metaMinor: " + metaMinor + " - "
                                    + getManager().getSetting().getContentsMinorVersion() + "\n");
                            buffer.append("============== Version Result ==================\n");
                            Logger.i(TAG, buffer.toString());

                            /*
                             * 1. App Update Flag 체크 1-1. 업데이트를 해야 하는 경우 다운로드 후
                             * Install Process 1-2. 업데이트 없는 경우 다음 상황으로 진행 2.
                             * Content Update Flag 체크 2-1. 업데이트 버전이 있는 경우 다운로드
                             * 2-2. Major 업데이트 파일이 있는 경우 Contnet Root 폴더 삭제 2-3.
                             * Major 다운 받은 폴더 Copy 2-4. Minor 업데이트 파일이 있는 경우
                             * Minor 다운 받은 파일 덮어쓰기 3. Meta Update Flag 체크 3-1.
                             * 업데이트 버전이 있는 경우 다운로드 3-2. Major 업데이트 파일이 있는 경우
                             * Meta Root 폴더 삭제 3-3. Major 다운 받은 폴더 Copy 3-4.
                             * Minor 업데이트 파일이 있는 경우 Minor 다운 받은 파일 덮어쓰기 3. Login
                             * Activity 이동
                             */

                            // App Version Menifest.xml android:versionName 을   x.x.x 로 표시하기 바람.
                            // major 와 minor의 경우 versionName 을 따라감 주의 요망
                            int appMajorVersion = 1;
                            int appMinorVersion = 0;
                            int appBuildVersion = 0;
                            try {
                                appBuildVersion = (int) PackageInfoCompat.getLongVersionCode(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0));
                                String appVerString = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
                                java.util.StringTokenizer strToken = new java.util.StringTokenizer(appVerString, ".");
                                if (strToken.hasMoreTokens()) {
                                    String token = strToken.nextToken();
                                    appMajorVersion = Integer.parseInt(token.trim());
                                }
                                if (strToken.hasMoreTokens()) {
                                    String token = strToken.nextToken();
                                    appMinorVersion = Integer.parseInt(token.trim());
                                }
                                // Version String 우선으로 변경.
                                if (strToken.hasMoreTokens()) {
                                    String token = strToken.nextToken();
                                    appBuildVersion = Integer.parseInt(token.trim());
                                } else {
                                    appBuildVersion = (int) PackageInfoCompat.getLongVersionCode(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0));
                                }
                            } catch (NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            boolean bAppUp = false;
                            // App Version Update Check
                            if (appMajor > appMajorVersion) {
                                bAppUp = true;
                            } else {
                                if (appMinor > appMinorVersion) {
                                    bAppUp = true;
                                } else {
                                    if (appBuild > appBuildVersion) {
                                        bAppUp = true;
                                    }
                                }
                            }

                            //다운로드 로직...............

                            if (bAppUp && appFilename.length() > 0) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        launcherFragment.progressBarDescription.setVisibility(View.VISIBLE);
                                        launcherFragment.progressBarDescription.setText(getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_appDown")));
                                    }
                                });
                                DownLoadReqeust(result, appFilename, CONTEXT_APP, getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_appDown")), new CompleteListener() {

                                    @Override
                                    public void resultCallback(String type, String callback, JSONObject result) {

                                        // 외부 스토리지 일경우 그냥 설치
                                        if (DOWNLOAD_ROOT.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                                            File realFilePath = new File(DOWNLOAD_ROOT + appFilename);
                                            appInstalledFromApk(realFilePath);
                                        } else {
                                            try {
                                                AppUpgrade(getActivity(), appFilename);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        requestKillProcess(getActivity());
                                        getActivity().finish();
                                    }
                                });

                            } else {

                                // 기존에 업그레이시 다운받은 파일이 있으면 지움
                                File before = getActivity().getBaseContext().getFileStreamPath(Def.INSTALL_APP_NAME);
                                if (before.exists())
                                    before.delete();

                                boolean bMajar = false;
                                // 포탈 메이저 버전이 현재 버전보다 큰 경우
                                if (contentMajor > Integer.parseInt(getManager().getSetting().getContentsMajorVersion()) &&
                                        !contentMajorFilename.equals("")) {


                                    DeleteDownloadFile(contentMajorFilename);

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            launcherFragment.progressBarDescription.setVisibility(View.VISIBLE);
                                            launcherFragment.progressBarDescription.setText(getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_majorDown")));
                                        }
                                    });
                                    DownLoadReqeust(result, contentMajorFilename, CONTEXT_CONTENT, getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_majorDown")), new majorMinorDownloadCallback());
                                    //****************@#$%
                                    // 포탈 마이너 버전이 현재 버전보다 큰 경우
                                } else if (contentMinor > Integer.parseInt(getManager().getSetting().getContentsMinorVersion()) &&
                                        contentMajor == Integer.parseInt(getManager().getSetting().getContentsMajorVersion())) {

                                    DeleteDownloadFile(contentMajorFilename);

                                    if (contentMinor > Integer.parseInt(getManager().getSetting().getContentsMinorVersion()) &&
                                            contentMajor >= Integer.parseInt(getManager().getSetting().getContentsMajorVersion())) {

                                        DeleteDownloadFile(contentMinorFilename);
                                        DeleteDownloadFile(BACKUP_CONTENT);
                                        if (!bMajar)
                                            Backup(httpResult, CONTENT_ROOT,
                                                    DOWNLOAD_ROOT + BACKUP_CONTENT);

                                        Logger.i(
                                                TAG,
                                                "App contentMinor update: start version - "
                                                        + getManager().getSetting().getContentsMinorVersion()
                                                        + contentMinorFilename);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                launcherFragment.progressBarDescription.setVisibility(View.VISIBLE);
                                                launcherFragment.progressBarDescription.setText(getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_minorDown")));
                                            }
                                        });
                                        DownLoadReqeust(httpResult, contentMinorFilename, CONTEXT_CONTENT, getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_minorDown")), new minorDownloadCallback());
                                    }

                                } else {

                                    IPRollback();
                                    //returnCallback(new JSONObject());
                                    returnCallback(httpResult);
                                }

                                rtnResult.put("result", isContentsUpdate);
                            }

                        } else {
                            // fail
                            IPRollback();

                            Logger.i(TAG, "result::false");
                            rtnResult.put("result", isContentsUpdate);
                            rtnResult.put("error_text", header.getString("error_text"));

                            returnCallback(rtnResult);
                        }
                    } else {

                        IPRollback();

                        Logger.i(TAG, "header::notFound");
                        rtnResult.put("result", isContentsUpdate);
                        rtnResult.put("error_text", "ZZ0006 response :: header is not found");

                        returnCallback(rtnResult);
                    }
                } else {
                    IPRollback();
                    Logger.i(TAG, "resultNotFound");
                    rtnResult.put("result", isContentsUpdate);
                    rtnResult.put("error_text", "ZZ0006 response :: result is not found");

                    returnCallback(rtnResult);
                }

            } catch (Exception e) {

                IPRollback();

                e.printStackTrace();
                listener.resultCallback("callback", "", rtnResult);
            }
        }
    }

    /**
     * 클라이언트로 결과값을 callback 해주는 메소드를 호출 하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용</br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성</br>
     *
     *
     * @param data Callback 데이터.
     * Return : (JSONObject) Update 로직 수행 결과
     */
    private void returnCallback(final JSONObject data) {

        handler.post(new Runnable() {

            @Override
            public void run() {

                try {

                    listener.resultCallback("callback", "", data);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 01.클래스 설명 : DownloadPlugin 실행 후 (Major 다운로드 한경우) 결과 값을 받고 minor 업데이트가 필요시 minor 업데이트를 호출하는 listener. <br>
     * 02.제품구분 : bizMob 3.0 Android Container <br>
     * 03.기능(콤퍼넌트) 명 : listener <br>
     * 04.관련 API/화면/서비스 : BMCManager <br>
     * 05.수정이력  <br>
     * <pre>
     * **********************************************************************************************************************************<br>
     *  수정일                                          이름                          변경 내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                                     박재민                         최초 작성<br>
     * **********************************************************************************************************************************<br>
     * </pre>
     *
     * @author 박재민
     * @version 1.0
     */
    final class majorMinorDownloadCallback implements CompleteListener {

        /**
         * DownloadPlugin 통해 Major 업데이트 결과를 받고 minor update 필요 유무 확인 작업을 거친 후, minor 파일 다운로드 메소드를 호출 하는 메소드. <br>
         * <p>
         * <pre>
         * 수정이력 <br>
         * **********************************************************************************************************************************<br>
         *  수정일                               이름                               변경내용</br>
         * **********************************************************************************************************************************<br>
         *  2016-09-21                           박재민                             최초 작성</br>
         *
         *
         * @param type Callback Type // 미사용.
         * @param callback callback 명.
         * @param result plugin 수행 결과 data // 미사용 <br> <br>
         * Return : (JSONObject) Major 업데이트 정보 반환
         */
        @Override
        public void resultCallback(String type, String callback, JSONObject result) {
            try {
                bMajar = true;
                DeleteDownloadFile(BACKUP_CONTENT);
                Backup(result, CONTENT_ROOT, DOWNLOAD_ROOT + BACKUP_CONTENT);

                Logger.i(
                        TAG,
                        "App contentMajor update: start version - "
                                + getManager().getSetting().getContentsMajorVersion()
                                + contentMajorFilename);

                File f = new File(CONTENT_ROOT);
                delete(f);

                if (updateSave(RESOURCE_ROOT,
                        contentMajorFilename)) {
                    getManager().getSetting().setContentsMajorVersion(String.valueOf(contentMajor));
                    getManager().getSetting().setContentsMinorVersion("0");
                    getManager().getSetting().save("contents_major_version", String.valueOf(contentMajor));
                    getManager().getSetting().save("contents_minor_version", "0");
                    Logger.i(TAG,
                            "App contentMajor update: true version - "
                                    + getManager().getSetting().getContentsMajorVersion());
                } else {
                    Restore(BACKUP_CONTENT);
                    Logger.i(TAG,
                            "App contentMajor update: false");
                }

                if (contentMinor > Integer.parseInt(getManager().getSetting().getContentsMinorVersion()) &&
                        contentMajor >= Integer.parseInt(getManager().getSetting().getContentsMajorVersion()) &&
                        !contentMinorFilename.equals("")) {

                    DeleteDownloadFile(contentMinorFilename);
                    if (!bMajar)
                        Backup(httpResult, CONTENT_ROOT,
                                DOWNLOAD_ROOT + BACKUP_CONTENT);

                    Logger.i(
                            TAG,
                            "App contentMinor update: start version - "
                                    + getManager().getSetting().getContentsMinorVersion()
                                    + contentMinorFilename);
                    httpResult.put("isMajorUpdated", bMajar);
                    DownLoadReqeust(httpResult, contentMinorFilename, CONTEXT_CONTENT, getActivity().getString(RUtil.getStringR(getActivity(), "u2t_txt_minorDown")), new minorDownloadCallback());
                } else {

                    IPRollback();
                    JSONObject majorDownloadCallbckObj = new JSONObject();
                    majorDownloadCallbckObj.put("download_type", "major");
                    listener.resultCallback("callback", "", majorDownloadCallbckObj);
                }

            } catch (Exception e) {
                IPRollback();
                e.printStackTrace();
            }
        }
    }

    /**
     * 01.클래스 설명 : DownloadPlugin 실행 후 (minor 다운로드한 경우) 결과 값을 받는 listener. <br>
     * 02.제품구분 : bizMob 3.0 Android Container <br>
     * 03.기능(콤퍼넌트) 명 : listener <br>
     * 04.관련 API/화면/서비스 : BMCManager <br>
     * 05.수정이력  <br>
     * <pre>
     * **********************************************************************************************************************************<br>
     *  수정일                                          이름                          변경 내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                                     박재민                         최초 작성<br>
     * **********************************************************************************************************************************<br>
     * </pre>
     *
     * @author 박재민
     * @version 1.0
     */
    final class minorDownloadCallback implements CompleteListener {

        /**
         * DownloadPlugin을 통해 minor 업데이트 후 호출 되는 메소드로 IP 원복 등의 작업 수행. <br>
         * <p>
         * <pre>
         * 수정이력 <br>
         * **********************************************************************************************************************************<br>
         *  수정일                               이름                               변경내용</br>
         * **********************************************************************************************************************************<br>
         *  2016-09-21                           박재민                             최초 작성</br>
         *
         *
         * @param type Callback Type // 미사용.
         * @param callback callback 명.
         * @param result plugin 수행 결과 data // 미사용 <br> <br>
         * Return : (JSONObject) minor 업데이트 정보 반환
         */
        @Override
        public void resultCallback(String type, String callback, JSONObject result) {

            try {
                if (updateSave(RESOURCE_ROOT,
                        contentMinorFilename)) {
                    getManager().getSetting().setContentsMinorVersion(String.valueOf(contentMinor));
                    getManager().getSetting().save("contents_minor_version", String.valueOf(contentMinor));
//					isContentsUpdate = true;
                    Logger.i(
                            TAG,
                            "App contentMinor update: true version - "
                                    + getManager().getSetting().getContentsMinorVersion());

                } else {
                    Restore(BACKUP_CONTENT);
                    Logger.i(TAG,
                            "App contentMajor update: false");
                }

                IPRollback();
                //listener.resultCallback("callback", "", new JSONObject());
                httpResult.put("download_type", "minor");
                listener.resultCallback("callback", "", httpResult);
            } catch (Exception e) {
                IPRollback();
                e.printStackTrace();
            }
        }
    }

    /**
     * 프로그레스 타입에 따라 실제 프로그레스를 보여주는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param type    프로그레스 종류, "progress", "progress_bar".
     * @param title   제목.
     * @param message 컨텐츠.
     */
    private void showDialog(final String type, final String title, final String message) throws Exception {
        handler.post(new Runnable() {

            @Override
            public void run() {

                if (type.equals("progress")) {
                    if (getProgressDialog() != null) {

                        getProgressDialog().setTitle(title);
                        ((ProgressDialog) getProgressDialog()).setMessage(message);
                        getProgressDialog().show();
                    }

                } else if (type.equals("progress_bar")) {
                    if (getProgressBar() != null) {

                        getProgressBar().setTitle(title);
                        ((ProgressDialog) getProgressBar()).setMessage(message);
                        getProgressBar().show();
                    }
                }
            }
        });

    }

    /**
     * 요청 타입에 따른 프로그레스를 닫는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-21                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param type 프로그레스 종류, "progress", "progress_bar".
     */
    private void dismissDialog(final String type) throws Exception {
        handler.post(new Runnable() {

            @Override
            public void run() {

                if (type.equals("progress")) {
                    if (getProgressDialog() != null) {
                        getProgressDialog().dismiss();
                    }

                } else if (type.equals("progress_bar")) {
                    if (getProgressBar() != null) {
                        ((ProgressDialog) getProgressBar()).setProgress(0);
                        getProgressBar().dismiss();
                    }
                }
            }
        });

    }

}
