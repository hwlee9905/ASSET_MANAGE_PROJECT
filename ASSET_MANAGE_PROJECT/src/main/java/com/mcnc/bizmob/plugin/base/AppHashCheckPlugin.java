package com.mcnc.bizmob.plugin.base;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;

import androidx.core.content.pm.PackageInfoCompat;

import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.setting.SettingModel;
import com.mcnc.bizmob.core.util.JsonUtil;
import com.mcnc.bizmob.core.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 01.클래스 설명 : 앱 위변조 체크하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 앱 위변조 체크 <br>
 * 04.관련 API/화면/서비스 : BMCManager, BMCPlugin, CompleteListener, Logger<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-09                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class AppHashCheckPlugin extends BMCPlugin {
    /**
     * 위변조 체크 통과 여부.
     */
    private boolean bResult;

    /**
     * Hash Key
     */
    private String cert = "";


    /**
     * 클라이언트로 부터 전달 받은 데이터로 HTTP Plugin을 통해 ZZ0008 전문통신 (앱 위변조 체크) 을 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-09                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, ZZ0008 전문통신 (앱 위변조 체크) 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {

        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (param.has("callback")) {
                    final String callback = param.getString("callback");

                    final JSONObject jResult = new JSONObject();

                    boolean hasHashKey = false;

                    PackageManager pm = getActivity().getPackageManager();
                    String packageName = getActivity().getPackageName();

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                            android.content.pm.Signature certSignature = packageInfo.signingInfo.getApkContentsSigners()[0];
                            MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
                            msgDigest.update(certSignature.toByteArray());
                            cert = new String(Base64.encode(msgDigest.digest(), Base64.DEFAULT | Base64.NO_WRAP));
                        } else {
                            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                            android.content.pm.Signature certSignature = packageInfo.signatures[0];
                            MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
                            msgDigest.update(certSignature.toByteArray());
                            cert = new String(Base64.encode(msgDigest.digest(), Base64.DEFAULT | Base64.NO_WRAP));
                        }
                        hasHashKey = true;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }


                    if (hasHashKey) {
                        final BMCManager manager = BMCManager.getInstance();
                        //위변조 체크 로직 추가.
                        try {
                            Logger.d("AppHashCheckPlugin", "앱 위변조 체크 전문 통신 (ZZ0008) 시작.");
                            JSONObject reqJson = new JSONObject();
                            JSONObject reqParam = new JSONObject();
                            JSONObject ZZ0008Obj = JsonUtil.addRequestHeader(null, "ZZ0008", "");


                            int appMajorVersion = 1;
                            int appMinorVersion = 0;
                            int appBuildVersion = 0;

                            try {
                                appBuildVersion = (int)PackageInfoCompat.getLongVersionCode(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0));
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
                                    appBuildVersion = (int)PackageInfoCompat.getLongVersionCode(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0));
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                            SettingModel model = manager.getSetting();

                            String appkey = model.getAppKey();

                            ZZ0008Obj = JsonUtil.addRequestBody(ZZ0008Obj, "appbuildversion", appBuildVersion);
                            ZZ0008Obj = JsonUtil.addRequestBody(ZZ0008Obj, "appminorversion", appMinorVersion);
                            ZZ0008Obj = JsonUtil.addRequestBody(ZZ0008Obj, "appmajorversion", appMajorVersion);
                            ZZ0008Obj = JsonUtil.addRequestBody(ZZ0008Obj, "verificationCode", cert);
                            ZZ0008Obj = JsonUtil.addRequestBody(ZZ0008Obj, "appKey", appkey);
                            reqParam.put("message", ZZ0008Obj);
                            reqParam.put("trcode", "ZZ0008");
                            reqParam.put("read_timeout", 10000);
                            reqParam.put("progress", true);
                            reqJson.put("param", reqParam);

                            //HttpPlugin 호출
                            manager.executeInterfaceFromID("RELOAD_WEB", reqJson, new CompleteListener() {
                                @Override
                                public void resultCallback(String s, String s1, JSONObject result) {
                                    Logger.d("AppHashCheckPlugin", "[RESPONSE] 앱 위변조 체크 전문 통신 (ZZ0008) resultCallback(), result = " + result.toString());
                                    String errorCode = "";
                                    String errorTxt = "";
                                    try {
                                        if (result.has("header")) {
                                            JSONObject headObj = result.getJSONObject("header");
                                            if (headObj.has("result")) { // 성공여부.

                                                if (headObj.getBoolean("result")) {
                                                    bResult = true;
                                                } else {
                                                    bResult = false;
                                                    errorCode = headObj.getString("error_code");
                                                    errorTxt = headObj.getString("error_text");
                                                }
                                            }
                                            jResult.put("result", bResult);
                                            jResult.put("app_hash", cert);
                                            jResult.put("error_code", errorCode);
                                            jResult.put("error_text", errorTxt);
                                            listener.resultCallback("callback", callback, jResult);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else { // 해쉬키 추출 실패
                        jResult.put("result", bResult);
                        jResult.put("app_hash", cert);
                        listener.resultCallback("callback", callback, jResult);
                    }

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
