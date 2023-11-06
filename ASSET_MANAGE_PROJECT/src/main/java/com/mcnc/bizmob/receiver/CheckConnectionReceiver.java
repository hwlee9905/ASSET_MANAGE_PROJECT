package com.mcnc.bizmob.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.mcnc.bizmob.core.util.log.Logger;

/**
 * 01.클래스 설명 : 연결 상태 정보를 전달 수신하는 BroadcastReceiver class. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 :  통신 연결 상태를 수신 <br>
 * 04.관련 API/화면/서비스 : BroadcastReceiver <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-26                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class CheckConnectionReceiver extends BroadcastReceiver {

    private final String TAG = "CheckConnectionReceiver";


    /**
     * mobile 네트워크 연결됨.
     */
    public final static int MOBILE_NETWORK_STATE_CONNECTED = 0;
    /**
     * wifi 네트워크 연결됨.
     */
    public final static int WIFI_NETWORK_STATE_CONNECTED = 1;

    /**
     * 네트워크 연결 해제됨. ( 연결된 네트워크 없음 )
     */
    public final static int NETWORK_STATE_DISCONNECTED = -1;

    /**
     * Wifi 정보를 보유한 WifiManager 객체
     */
    private WifiManager wifiManager = null;

    /**
     * ConnectivityManager 객체
     */
    private ConnectivityManager connManager = null;
    /**
     * 네트워크 상태 정보를 수신할 리스너,
     */
    private onChangeNetworkStatusListener onChangeNetworkStatusListener = null;


    /**
     * Context 객체로 부터 Wifi Manager 및 Connectivity Manager를 셋팅하는 기본 생성자.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-26                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    public CheckConnectionReceiver(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public interface onChangeNetworkStatusListener {
        void onChanged(int status);
    }

    public void setOnChangeNetworkStatusListener(onChangeNetworkStatusListener listener) {
        onChangeNetworkStatusListener = listener;
    }

    /**
     * 네트워크 상태 정보 수신 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-26                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param context Context 객체.
     * @param intent  Wifi 상태 정보를 갖고 있는 Intent 객체.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isWifi = false;

            Network network = connManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isWifi = true;
            }
            Logger.d(TAG, "isWifi = " + isWifi);

            boolean isConnected = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    | networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            Logger.d(TAG, "isConnected = " + isConnected);

            if (onChangeNetworkStatusListener == null) {
                Logger.d(TAG, "onChangeNetworkStatusListener = " + onChangeNetworkStatusListener);
                return;
            }

            if (isConnected) {
                if (isWifi) {
                    onChangeNetworkStatusListener.onChanged(WIFI_NETWORK_STATE_CONNECTED);
                    Logger.d(TAG, "connected type = WIFI");
                } else {
                    onChangeNetworkStatusListener.onChanged(MOBILE_NETWORK_STATE_CONNECTED);
                    Logger.d(TAG, "connected type = MOBILE");
                }
            } else {
                onChangeNetworkStatusListener.onChanged(NETWORK_STATE_DISCONNECTED);
            }
        } else {
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

            boolean isConnected = (networkInfo != null && networkInfo.isConnected());
            Logger.d(TAG, "isConnected = " + isConnected);

            if (onChangeNetworkStatusListener == null) {
                Logger.d(TAG, "onChangeNetworkStatusListener = " + onChangeNetworkStatusListener);
                return;
            }

            if (isConnected) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        onChangeNetworkStatusListener.onChanged(MOBILE_NETWORK_STATE_CONNECTED);
                        Logger.d(TAG, "connected type = MOBILE");
                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        onChangeNetworkStatusListener.onChanged(WIFI_NETWORK_STATE_CONNECTED);
                        Logger.d(TAG, "connected type = WIFI");
                    }
                }
            } else {
                onChangeNetworkStatusListener.onChanged(NETWORK_STATE_DISCONNECTED);
            }
        }


    }
}
