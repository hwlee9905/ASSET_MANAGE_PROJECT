package com.mcnc.bizmob.plugin.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * 01.클래스 설명 : 루팅 체크 기능을 수행하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : <br>
 * 04.관련 API/화면/서비스 : <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-22                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class RootingCheckPlugin extends BMCPlugin {

    /**
     * 루팅 Paths
     */
    private String[] rootingPaths = {
            "/su/bin/su",
            "/su/xbin/su",
            "/su/bin/.user/.su",
            "/system/xbin/su",
            "/system/bin/su",
            "/system/bin/.user/.su",
            "/dev/com.noshufou.android.su",
            "/data/data/com.tegrak.lagfix",
            "/data/data/eu.chainfire.supersu",
            "/data/data/com.noshufou.android.su",
            "/data/data/com.jrummy.root.browserfree",
            "/system/app/Superuser.apk/",
            "/data/app/com.tegrak.lagfix.apk",
            "/data/app/eu.chainfire.supersu.apk",
            "/data/app/com.noshufou.android.su.apk",
            "/data/app/com.jrummy.root.browserfree.apk"
    };

    /**
     * 루팅 Apps
     */
    private String[] rootingApps = {
            "com.tegrak.lagfix",
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.jrummy.root.browserfree",
            "com.jrummy.busybox.installer",
            "me.blog.markan.UnRooting",
            "com.formyhm.hideroot"
    };

    /**
     * 클라이언트로 부터 전달 받은 데이터를 각각의 요청에 맞는 루팅 관련 기능을 수행하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-10-05                         박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 루팅 관련 이벤트 수행에 필요한 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {
        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (param.has("callback")) {
                    String callback = param.getString("callback");

                    JSONObject jResult = new JSONObject();

                    boolean isRooted = false;
                    // 1단계 "su" command가 실행 되는지 확인.

                    isRooted = checkRootingCommand();

                    // 1단계 검사에서 루팅 폰이 아니라 판별 되었다면.
                    if (!isRooted) {

                        // 2단계 루팅관련된 paths 검색
                        isRooted = checkRootingPaths(rootingPaths);

                        // 2단계 검사에서 루팅 폰이 아니라 판별 되었다면.
                        if (!isRooted) {

                            // 3단계 루팅앱의 설치 여부 확인
                            isRooted = checkRootingAppInstalled(rootingApps, getActivity());
                        }
                    }

                    jResult.put("result", isRooted);
                    Logger.d("RootingCheckPlugin", "is rooted = " + isRooted);
                    listener.resultCallback("callback", callback, jResult);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * su 명령어를 실행하여 명령어 성공 여부를 확인하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     */
    private boolean checkRootingCommand() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            Logger.d("RootingCheckPlugin", "checkRootingCommand, \"su\" command executed");
        } catch (IOException e) {
            e.printStackTrace();
            Logger.d("RootingCheckPlugin", "checkRootingCommand, \"su\" command is not executed");
            return false;
        }
        return true;
    }


    /**
     * 루팅 관련된 path 검사를 실행하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param paths 루팅 관련된 paths.
     */
    private boolean checkRootingPaths(String[] paths) {
        boolean found = false;
        if (!found) {
            for (int i = 0; i < paths.length; i++) {
                if (new File(paths[i]).exists()) {
                    found = true;
                    Logger.d("RootingCheckPlugin", "checkRootingPaths, found Paths = " + paths[i]);
                    break;
                } else {
                    Logger.d("RootingCheckPlugin", "checkRootingPaths, \"" + paths[i] + "\" is not found");

                }
            }
        }
        return found;
    }

    /**
     * 루팅 앱이 설치되어 있는지 확인하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param packageNames 루팅 앱의 packageNames.
     * @param context      해당 context.
     */

    private boolean checkRootingAppInstalled(String[] packageNames, Context context) {
        PackageManager packageManager = context.getPackageManager();

        boolean found = false;
        for (int i = 0; i < packageNames.length; i++) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageNames[i], PackageManager.GET_ACTIVITIES);
                if (packageInfo.packageName != null) {
                    Logger.d("RootingCheckPlugin", "checkRootingAppInstalled, found app name = " + packageNames[i]);
                    found = true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Logger.d("RootingCheckPlugin", "checkRootingAppInstalled, \"" + packageNames[i] + "\" is not installed");
                found = false;
            }
        }
        return found;
    }

}
