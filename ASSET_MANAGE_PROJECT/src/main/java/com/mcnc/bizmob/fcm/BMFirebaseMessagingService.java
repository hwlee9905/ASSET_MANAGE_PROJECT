package com.mcnc.bizmob.fcm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mcnc.bizmob.manage.Init;
import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.config.AppConfigReader;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;
import com.mcnc.bizmob.view.FcmControlActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mcnc.bizmob.core.application.BMCInit.context;

public class BMFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Push History를 저장할 최대 사이즈 값.
     */
    private final int MAX_HISTORY_COUNT = 20;

    /**
     * PowerManager 객체
     */
    private PowerManager pm = null;

    /**
     * PowerManager가 보유한 wake / lock 기능을 수행하는 객체
     */
    private PowerManager.WakeLock wl = null;

    private static final String TAG = BMFirebaseMessagingService.class.getSimpleName();

    /**
     * push 작업과 관련된 수행 결과를 올려 줄 callback function 명.
     */
    private static String resultPushData = "";

    private static String mChannelId = "default";
    private static String mChannelName = "default";
    private static String mChannelDescription = "channel_description";

    public static int mLightColor = Color.GREEN;
    public static long[] mVibrationPattern = new long[]{100, 200, 100, 200};

    public static void setChannelId(String channelId) {
        mChannelId = channelId;
    }
    public static void setChannelName(String channelName) {
        mChannelName = channelName;
    }
    public static void setChannelDescription(String channelDescription) {
        mChannelDescription = channelDescription;
    }
    public static void setLightColor(int colorARGB) {
        mLightColor = colorARGB;
    }
    public static String getChannelId() {
        return mChannelId;
    }
    public static String getChannelName() {
        return mChannelName;
    }
    public static String getChannelDescription() {
        return mChannelDescription;
    }
    public static int getLightColor() {
        return mLightColor;
    }
    public static void setVibrationPattern(long[] vibrationPattern) {
        mVibrationPattern = vibrationPattern;
    }
    public static long[] getVibrartionPattern() {
        return mVibrationPattern;
    }

    /**
     * WakeRock 해제 기능을 수행할 Runnalbe 객체
     */
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (wl != null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wl.release();
            }
        }
    };

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("registration_id", token);
        editor.commit();
    }

    // 메시지 수신
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Logger.d(TAG, "FCM onMessageReceived");
        Logger.d(TAG, "FCM, From : " + remoteMessage.getFrom());

        sendNotification(remoteMessage);
    }

    @SuppressLint("InvalidWakeLockTag")
    private void sendNotification(RemoteMessage remoteMessage) {
        Logger.d(TAG, "FCM sendNotification");

        String from = remoteMessage.getFrom();
        if (from != null && from.equals("google.com/iid")) {
            return;
        }

        String title = "";
        String body = "";
        RemoteMessage.Notification remoteMessageNotification =  remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();

        if(remoteMessageNotification != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();

            String clickAction = remoteMessage.getNotification().getClickAction();

            if (clickAction == null) {
                clickAction = "";
            }

            Logger.d(TAG, " onMessageReceived () ------------ CLICK ACTION : " + clickAction);
        }else {
            title = getString(data, "title", "알림");
            body = getString(data, "message", "");
        }

        String count = getString(data, "badge", "0");
        String hasImage = getString(data, "hasBin", "");
        String messageId = getString(data, "messageId", "");
        String image = getString(data, "image","");
        Logger.d(TAG, " onMessageReceived () ------------ COME TITLE: " + title);
        Logger.d(TAG, " onMessageReceived () ------------ COME BODY: " + body);
        Logger.d(TAG, " onMessageReceived () ------------ MESSAGE ID : " + messageId);

        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wl = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | PowerManager.ACQUIRE_CAUSES_WAKEUP, "FCMWakeLock");
        }
        wl.acquire();

        String type = "";
        String url = "";

        if (hasImage != null && hasImage.equals("true")) {
            type = "bigPicture";

            String strUrl = getPushUrl(context);

            int len = strUrl.length();
            int lastIndex = strUrl.lastIndexOf("/");

            if (len != lastIndex + 1) {
                strUrl += "/";
            }

            url = strUrl + "messages/" + messageId + "/binaries/0/download/pushimage.jpg";
        }

        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = null;

        // notiId
        int notiID = (int) System.currentTimeMillis();
        Intent in = new Intent(Intent.ACTION_MAIN);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.addCategory(Intent.CATEGORY_LAUNCHER);
        in.setComponent(new ComponentName(context, FcmControlActivity.class));
        in.putExtra("push", body);
        in.putExtra("push_noti_id", notiID);
        in.putExtra("message_id", messageId);
        in.putExtra("fromPush", "push");

        in.putExtra("badge", count);
        in.putExtra("message", body);
        in.putExtra("hasBin", hasImage);

        in.putExtra("image",image);
        in.putExtra("fromPush", "bizPush");

        int badgeCount = 0;

        try {
            badgeCount = Integer.parseInt(count);
        } catch (Exception e) {
            badgeCount = 0;
        }

        if (type.equals("bigText")) {
            notification = bigTextStyle(context, title, body, notiID, in, badgeCount);
        } else if (type.equals("bigPicture")) {
            notification = bigImageStyle(context, title, body, url, notiID, in, badgeCount);
        } else {
            notification = normalStyle(context, title, body, notiID, in, badgeCount);
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 전체 제거 고려
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        } else {
            // 진동 효과 구성
            long[] vibrate = {1000, 1000, 1000, 1000, 1000};
            notification.vibrate = vibrate;
        }
        nm.notify(003123, notification);

        run.run();

        //뱃지 카운팅은 26(오레오)이전에서 밖에 동작 안함.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent badgeIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            badgeIntent.putExtra("badge_count", badgeCount);
            badgeIntent.putExtra("badge_count_package_name", context.getPackageName());
            badgeIntent.putExtra("badge_count_class_name", Init.LAUNCHER_ACTIVITY.getName());
            context.sendBroadcast(badgeIntent);
        }

        //push callback
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(1);
        ComponentName topActivity = list.get(0).topActivity;
        String runningPackageName = topActivity.getPackageName();
        String thisPackageName = context.getApplicationContext().getPackageName();

        ArrayList<BMCFragment> fragmentList = BMCInit.getFragmentList();

        if (runningPackageName.equals(thisPackageName) && fragmentList.size() > 0) {
            //앱실행중
            final BMCFragment fragment = (BMCFragment) fragmentList.get((fragmentList.size() - 1));
            JSONObject pushData = new JSONObject();
            try {
                pushData.put("badge", count);
                pushData.put("message", body);
                pushData.put("hasBin", hasImage);
                pushData.put("messageId", messageId);

                pushData.put("image",image);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            resultPushData = pushData.toString();

            fragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (fragment.fWebView != null) {
                        if (resultPushData != null) {
                            Logger.d("startActivity", "fragment.event(\"onPush\",resultPushData)" + "resultPushData : " + resultPushData);
                            fragment.event("onPush", resultPushData);
                        } else {
                            Logger.d("startActivity", "fragment.event(\"onPush\",resultPushData)" + "resultPushData : " + "{}");
                            fragment.event("onPush", "{}");
                        }
                    }
                }
            });
        }
    }

    /**
     * 전달 받은 데이터를 가공하여 normal 스타일의 notification 을 띄우는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param title   notification 제목 문자열.
     * @param message notification 내용 문자열.
     * @param notiID  notification 고유 Id 값.
     * @param in      이동할 화면 정보를 갖고 있는 intent 객체.
     * @return 생성된  normal 스타일의 Notification 객체.
     */
    private Notification normalStyle(Context context, String title, String message, int notiID, Intent in, int number) {
        if (title == null || (title != null && title.equals(""))) {
            title = context.getResources().getString(RUtil.getStringR(context, "app_name"));
        }

        int notiIconId = RUtil.getDrawableR(context, "noti_icon");

        Logger.d("notiIconId", "notiIconId = " + notiIconId);
        if (notiIconId == 0) {
            notiIconId = RUtil.getDrawableR(context, "app_icon");
        }

        Notification notification;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notiID, in, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannelId())
                .setSmallIcon(notiIconId)
                .setTicker("새 메시지 도착")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setNumber(number);

        notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL; //선택시 자동삭제

        return notification;
    }

    /**
     * 전달 받은 데이터를 가공하여 big text 스타일의 notification 을 띄우는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param title   notification 제목 문자열.
     * @param message notification 내용 문자열.
     * @param notiID  notification 고유 Id 값.
     * @param in      이동할 화면 정보를 갖고 있는 intent 객체.
     * @return 생성된  normal 스타일의 Notification 객체.
     */
    private Notification bigTextStyle(Context context, String title, String message, int notiID, Intent in, int number) {

        if (title == null || (title != null && title.equals(""))) {
            title = context.getResources().getString(RUtil.getStringR(context, "app_name"));
        }

        int notiIconId = RUtil.getDrawableR(context, "noti_icon");

        Logger.d("notiIconId", "notiIconId = " + notiIconId);
        if (notiIconId == 0) {
            notiIconId = RUtil.getDrawableR(context, "app_icon");
        }

        Notification notification;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notiID, in, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannelId())
                .setSmallIcon(notiIconId)
                .setTicker("새 메시지 도착")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        // big text style
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(title);
        style.bigText(message);

        builder.setStyle(style);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setNumber(number);

        notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL; //선택시 자동삭제

        return notification;
    }

    /**
     * 전달 받은 데이터를 가공하여 big image 스타일의 notification 을 띄우는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param title   notification 제목 문자열.
     * @param message notification 내용 문자열.
     * @param url     이미지 url.
     * @param notiID  notification 고유 Id 값.
     * @param in      이동할 화면 정보를 갖고 있는 intent 객체.
     * @return 생성된  normal 스타일의 Notification 객체.
     */
    private Notification bigImageStyle(Context context, String title, String message, String url, int notiID, Intent in, int number) {
        if (title == null || (title != null && title.equals(""))) {
            title = context.getResources().getString(RUtil.getStringR(context, "app_name"));
        }

        int notiIconId = RUtil.getDrawableR(context, "noti_icon");
        Logger.d("notiIconId", "bigImageStyle");

        Logger.d("notiIconId", "notiIconId = " + notiIconId);
        if (notiIconId == 0) {
            notiIconId = RUtil.getDrawableR(context, "app_icon");
        }

        Notification notification;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notiID, in, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannelId())
                .setSmallIcon(notiIconId)
                .setTicker("새 메시지 도착")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        Bitmap bitmap = null;
        try {

            bitmap = drawableFromUrl(url).getBitmap();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            // big image style
            NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
            style.setBigContentTitle(title);
            style.setSummaryText(message);

            style.bigPicture(bitmap);


            builder.setStyle(style);
            Logger.d("notiIconId", "bigImageStyle bitmap != null");
        } else {

            Logger.d("notiIconId", "bigImageStyle bitmap == null");
        }
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setNumber(number);

        notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL; //선택시 자동삭제

        return notification;
    }

    /**
     * 이미지 url 로 부터 BitmapDrawable 객체를 반환해주는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param url 이미지 url.
     * @return 이미지 url로 부터 생성된 BitmapDrawable 객체
     */
    private BitmapDrawable drawableFromUrl(String url) throws IOException {
        Bitmap bitmap = null;
        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            input = connection.getInputStream();

            Logger.d("PUSH", "connection.getContentLength() : " + connection.getContentLength());
            Logger.d("PUSH", "connection.getResponseCode() : " + connection.getResponseCode());
            Logger.d("PUSH", "connection.getResponseMessage() : " + connection.getResponseMessage());

            bitmap = BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (bitmap == null) {
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.connect();
                    input = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (connection != null) {
                        try {
                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                Logger.d("PUSH", "connection.getContentLength() : " + connection.getContentLength());
                Logger.d("PUSH", "connection.getResponseCode() : " + connection.getResponseCode());
                Logger.d("PUSH", "connection.getResponseMessage() : " + connection.getResponseMessage());


                if (bitmap == null) {
                    Logger.d("PUSH", "The bitmap is still null. So the image will be removed from the notification.");
                }
            }
        }
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    private String getString(Map<String, String> data, String key, String defaultValue){
        String result = data.get(key);

        if(TextUtils.isEmpty(result)){
            result = defaultValue;
        }

        return result;
    }

    private String getPushUrl(Context context) {
        if (AppConfig.PUSH_URL.equals("")) {
            AppConfigReader.LoadAppConfig(context, "bizMOB/config/app.config");
            return AppConfig.PUSH_URL;
        }
        return AppConfig.PUSH_URL;
    }
}
