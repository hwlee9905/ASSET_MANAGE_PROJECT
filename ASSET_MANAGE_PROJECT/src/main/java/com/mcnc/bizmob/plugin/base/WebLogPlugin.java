package com.mcnc.bizmob.plugin.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.weblog.BMCWebSocketServer;
import com.mcnc.bizmob.weblog.WebServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import fi.iki.elonen.NanoHTTPD;


/**
 * 01.클래스 설명 : PC Web browser(Chrome)과 디바이스를 연동시켜 로그 확인이 가능하게 해주는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : PC Web browser(Chrome)과 디바이스를 연동시켜 로깅 확인 기능 <br>
 * 04.관련 API/화면/서비스 : BMCWebSocketServer, WebServer, Logger, BMCUtil <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2017-01-13                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class WebLogPlugin extends BMCPlugin {

    /**
     * 클래스 명.
     */
    private final String TAG = "WebLogPlugin";

    /**
     * 웹서버 객체
     */
    private WebServer webServer;

    /**
     * 웹소켓 서버 객체
     */
    private BMCWebSocketServer bmcWebSocketServer;

    @Override
    protected void executeWithParam(JSONObject data) {
        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (param.has("type")) {
                    String type = param.getString("type");

                    if (type.equals("start")) {
                        webLogStart();
                    } else if (type.equals("stop")) {
                        webLogStop();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void webLogStart() {
        if (webServer == null) {
            //Web Server On
            String ip = getCurrentWIFIAddress(getActivity());

            //Wifi상태 체크
            //Wifi가 연결이 되어 있지 않을시
            if (!BMCUtil.isWifiConnected(getActivity())) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("ERROR")
                                .setMessage(RUtil.getStringR(getActivity(), "txt_working_on_wifi_only"))
                                .setCancelable(false)
                                .setPositiveButton(getActivity().getString(RUtil.getStringR(getActivity(), "txt_ok")),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which
                                            ) {
                                                dialog.dismiss();
                                            }
                                        }).create().show();
                    }
                });
                return;
            }

            int port = 8080;
            try {
                //Web Server On!
                webServer = new WebServer(port, ip);
                webServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                Logger.d(TAG, "Web Server On!! connect to http://" + ip + ":" + port + "/");

                //Web SocketServer On
                if (bmcWebSocketServer == null) {
                    int inetSocketPort = 38301;
                    InetSocketAddress inetSockAddress = new InetSocketAddress(ip, inetSocketPort);
                    bmcWebSocketServer = new BMCWebSocketServer(inetSockAddress);
                    bmcWebSocketServer.start();
                    Logger.d(TAG, "Web Socket Server On! bmcWebSocketServer ip= " + ip + ":" + port);
                }
                JSONObject result = new JSONObject();
                JSONObject param = new JSONObject();
                param.put("ip", ip);
                param.put("port", port);
                result.put("result", true);
                result.put("param", param);
                listener.resultCallback("callback", "", result);

            } catch (IOException e) {
                Log.d(TAG, "Couldn't start server:" + e);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    private void webLogStop() {
        //Web Server Close First!!
        if (webServer != null) {
            webServer.stop();
            webServer = null;
            Logger.d("NetworkSettingActivity", "Web Server Closed!");


            //BMC WebSocketSever close
            if (bmcWebSocketServer != null) {
                //Stop thread first, before closing BMC WebSocketServer !!
                bmcWebSocketServer.setWhileFlag(false);

                try {
                    bmcWebSocketServer.stop();
                    bmcWebSocketServer = null;
                    Logger.d("NetworkSettingActivity", "Web Socket Server Closed!");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            JSONObject result = new JSONObject();
            try {
                result.put("result", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listener.resultCallback("callback", "callback", result);
        }
    }

    //현재 접속중인 WIFI의 IP를 반환 ex) "192.168.0.1"
    private String getCurrentWIFIAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.e(TAG, "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }
}