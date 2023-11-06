package com.mcnc.bizmob.manage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;
import android.webkit.WebView;

import androidx.multidex.MultiDex;

import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.def.Def;
import com.mcnc.bizmob.core.download.AbstractDownloadService;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.provider.BMCLocalFileProvider;
import com.mcnc.bizmob.core.setting.SettingModel;
import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.file.FileUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.ImageUtil;
import com.mcnc.bizmob.core.util.res.ImageWrapper;
import com.mcnc.bizmob.core.view.crash.CrashHandler;
import com.mcnc.bizmob.fcm.BMFirebaseMessagingService;
import com.mcnc.bizmob.view.DummyActivity;
import com.mcnc.bizmob.view.LauncherFragment;
import com.mcnc.bizmob.view.MainFragment;

import java.io.File;

/**
 * 01.클래스 설명 : 앱 구동에 필요한 기초 정보와 Fragment에 대한 list를 보유한 BMCInit을 상속받은 Application 클래스. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : Application Class<br>
 * 04.관련 API/화면/서비스 : Def, AbstractDownloadService, BMCManager, BMCLocalFileProvider, BMCUtil, FileUtil, Logger, ImageUtil, ImageWrapper, BMCInit, DummyActivity, LauncherFragment, MainFragment<br>
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
public class Init extends BMCInit {

    /**
     * internal 저장소 root path.
     */
    private String URI_PREFIX = "";
    /**
     * ContentProvider의 class path.
     */
    private String URI_LOCALFILE = "";

    /**
     * Application class의 Life Cycle 중 하나에 속하는 메소드로 아래와 같은 기능을 수행함.<br>
     * 1.bizMOB 3.0 Framework 버전 setting <br>
     * 2.App의 build 타입 확인 (운영인지 개발인지) <br>
     * 3.default 화면 class에 대한 정보 <br>
     * 4.content mode setting <br>
     * 5.temporary folder 영역 관리(삭제) <br>
     * 6. 크래쉬 로그 핸들러 init <br>
     * 7. 크래쉬 로그 파일 저장 경로 설정 <br>
     * 8.locale 설정 등의 작업 <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    @Override
    public void onCreate() {
        BIZMOB_FRAMEWORK_VERSION = 301702021522L; // Do not edit it, unless otherwise instructed.

        useIntroBg = false; //1. intro bg 효과 적용할 여부. (default = false)
        encryptDeviceId = true; //2. device id (IMEI) 암호화 여부 (default = true)
        useAndbzDeviceId = true; //3. device id 앞에 광고 식별자 "andbz_"를 붙일지 여부 (default = true)
        AppConfig.checkRooted = false; // 4. 앱 루팅 여부 체크
        AppConfig.checkAppHashKey = false; // 5. 앱 위변조 체크
        URI_PREFIX = getApplicationInfo().dataDir + "/";
        URI_LOCALFILE = getPackageName() + ".provider";

        String[] arr = getPackageName().split("\\.");
        String mode = arr[arr.length - 1];

        SettingModel.useEncryptedFStorage = false; // 4. FStorage영역 암호화 여부.

        if (mode.equals("dev")) {
            mode = "dev";
        } else {
            mode = "release";
        }

        BUILD_MODE = mode;

        CONFIG_VERSION = 2;
        LAUNCHER_ACTIVITY = SlideFragmentActivity.class;
        LAUNCHER_FRAGMENT = LauncherFragment.class;
        PUSH_ACTIVITY = DummyActivity.class;
        MAIN_FRAGMENT = MainFragment.class;

        BMCLocalFileProvider.setProviderPath(URI_PREFIX);
        BMCLocalFileProvider.setProviderName(URI_LOCALFILE);
        ImageUtil.ROOT_PATH = URI_PREFIX;

        //크래쉬 로그 파일 저장 경로를 정한다.
        logFilePath = getFilesDir() + "/crash_logs/";

        //크래쉬 로그 핸들러 init
        CrashHandler.projectCode = "BASE35";
        CrashHandler.companyCode = "MOBILECNC";
        CrashHandler.init(this);

        super.onCreate();

        BMCManager manager = BMCManager.getInstance();
        ImageWrapper.setMode(Integer.parseInt(manager.getSetting().getContentsMode()));
        Logger.setDefaultLogLevel();

        WebView.setWebContentsDebuggingEnabled(true);
        AbstractDownloadService.setDownloadPath(URI_PREFIX + Def.ROOT_DOWNLOAD_DIR);

        // 로컬 저장일 경우 폴더를 반드시 지운다...
        String path = URI_PREFIX + Def.ROOT_DOWNLOAD_DIR;
        File f = new File(path);
        if (f.exists()) {
            FileUtil.delete(f);
            Logger.d("Init", "delete : " + URI_PREFIX + Def.ROOT_DOWNLOAD_DIR);
        }

        //Locale 설정.
        initDefLocale("ko_KR");

        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(BMFirebaseMessagingService.getChannelId(), BMFirebaseMessagingService.getChannelName(), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(BMFirebaseMessagingService.getChannelDescription());
            notificationChannel.setShowBadge(true);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(BMFirebaseMessagingService.getLightColor());
            notificationChannel.setVibrationPattern(BMFirebaseMessagingService.getVibrartionPattern());
            notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
