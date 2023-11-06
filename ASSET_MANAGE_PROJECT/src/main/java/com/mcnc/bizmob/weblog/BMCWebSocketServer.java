package com.mcnc.bizmob.weblog;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by lenovo on 2016-03-22.
 */
public class BMCWebSocketServer extends WebSocketServer {

    private  BMCWebSocketServer bmcWebSocketServer = null;
    private  String TAG = "BMCWebSocketServer";
    private boolean whileFlag = true;
    private Thread thread;

    private ArrayList<WebSocket> webSocketArrayList = new ArrayList<WebSocket>();

    public BMCWebSocketServer(InetSocketAddress address) {
        super(address);

    }


    @Override
    public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
        Log.d(TAG, "onClose " + arg2);

    }

    @Override
    public void onError(WebSocket arg0, Exception arg1) {
        Log.d(TAG, "onError " + arg1.getMessage());

    }

    public void setWhileFlag(boolean whileFlag) {
        this.whileFlag = whileFlag;
    }

    @Override
    public void onMessage(WebSocket user, String msg) {
        Log.d(TAG, "new onMessage =" + msg);

        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String[] command = new String[]{"logcat", "-v", "threadtime"};

                        Process process = Runtime.getRuntime().exec(command);
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));

                        while (whileFlag) {
                            String line = null;
                            line = bufferedReader.readLine();
                            if (line != null) {
                                for (int i = 0; i < webSocketArrayList.size(); i++) {
                                    WebSocket eachUser = webSocketArrayList.get(i);
                                    if(eachUser.isClosed()){
                                        webSocketArrayList.remove(eachUser);
                                    }else{
                                        if(!eachUser.isClosing()) {
                                            eachUser.send(line);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {

                    }
                }
            });
            thread.start();
        }
    }


    @Override
    public void onOpen(WebSocket user, ClientHandshake handshake) {
        webSocketArrayList.add(user);
        Log.d(TAG, "new connection to " + user.getRemoteSocketAddress());

    }

}
