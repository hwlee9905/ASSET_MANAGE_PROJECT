package com.mcnc.bizmob.weblog;

import android.content.res.AssetManager;
import android.util.Log;

import com.mcnc.bizmob.core.application.BMCInit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by lenovo on 2016-03-22.
 */
public class WebServer extends NanoHTTPD {
    private String ip = "192.168.0.1";

    public WebServer(int port, String ip) throws IOException {
        super(port);
        this.ip = ip;
    }

    @Override
    public Response serve(IHTTPSession session) {
        StringBuilder answer = new StringBuilder();
        String uri = session.getUri();
        InputStream in = null;
        try {

            AssetManager assetManager = BMCInit.context.getAssets();
            if(uri.contains(".css")){
                in = assetManager.open(uri.substring(1));
                return newChunkedResponse(Response.Status.OK, "text/css", in);
            }else if(uri.contains(".png")){
                in = assetManager.open(uri.substring(1));
                return newChunkedResponse(Response.Status.OK,"image/png",in);
            }else {
                in = assetManager.open("weblog/index.html");
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    if(line.contains("insertIpHere")){
                        answer.append("var wsUri = \"ws://"+ip+":38301\";\n");
                    }
                    answer.append(line);
                }
            }

        } catch (IOException ioe) {
            Log.w("Httpd", ioe.toString());
        }


        return newFixedLengthResponse(answer.toString());
    }
}
