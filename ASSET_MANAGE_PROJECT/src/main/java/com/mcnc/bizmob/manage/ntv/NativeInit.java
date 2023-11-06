package com.mcnc.bizmob.manage.ntv;

import android.content.Context;

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
import com.mcnc.bizmob.view.DummyActivity;
import com.mcnc.bizmob.view.MainFragment;
import com.mcnc.bizmob.view.ntv.NativeLauncherFragment;

import java.io.File;

public class NativeInit extends BMCInit {
    private String URI_PREFIX = "";
    private String URI_LOCALFILE = "";


    @Override
    public void onCreate() {
        BIZMOB_FRAMEWORK_VERSION = 301701041551L; // Do not edit it, unless otherwise instructed.

        useIntroBg = false; //1. intro bg 효과 적용할 여부. (default = false)
        encryptDeviceId = true; //2. device id (IMEI) 암호화 여부 (default = true)
        useAndbzDeviceId = true; //3. device id 앞에 광고 식별자 "andbz_"를 붙일지 여부 (default = true)
        AppConfig.checkRooted = false; // 4. 앱 루팅 여부 체크
        AppConfig.checkAppHashKey = false; // 5. 앱 위변조 체크

        String[] arr = getPackageName().split("\\.");
        String mode = arr[arr.length - 1];

        if (mode.equals("dev")) {
            SettingModel.useEncryptedFStorage = false; // 4. FStorage영역 암호화 여부.
            mode = "dev";
        } else {
            mode = "release";
        }

        BUILD_MODE = mode;

        CONFIG_VERSION = 1;

        LAUNCHER_ACTIVITY = NativeSlideFragmentActivity.class;
        LAUNCHER_FRAGMENT = NativeLauncherFragment.class;

        PUSH_ACTIVITY = DummyActivity.class;
        MAIN_FRAGMENT = MainFragment.class;

        URI_PREFIX = getApplicationInfo().dataDir + "/";
        URI_LOCALFILE = getPackageName() + ".provider";

        BMCLocalFileProvider.setProviderPath(URI_PREFIX);
        BMCLocalFileProvider.setProviderName(URI_LOCALFILE);
        ImageUtil.ROOT_PATH = URI_PREFIX;

        //크래쉬 로그 파일 저장 경로를 정한다.
        logFilePath = getFilesDir() + "/crash_logs/";

        //크래쉬 로그 핸들러 init
        CrashHandler.projectCode ="BASE35";
        CrashHandler.companyCode = "MOBILECNC";
        CrashHandler.init(this);

        super.onCreate();

        BMCManager manager = BMCManager.getInstance();
        ImageWrapper.setMode(Integer.parseInt(manager.getSetting().getContentsMode()));
        Logger.setDefaultLogLevel();

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
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
