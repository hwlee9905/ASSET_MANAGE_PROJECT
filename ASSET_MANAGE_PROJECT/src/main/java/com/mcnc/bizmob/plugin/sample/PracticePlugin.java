package com.mcnc.bizmob.plugin.sample;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import static com.mcnc.bizmob.plugin.sample.ValuesClass.TO;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mcnc.bizmob.manageapp.R;
import com.mcnc.bizmob.core.plugin.BMCPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class PracticePlugin extends BMCPlugin {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
//    private static final String BASE_URL = "http://my-json-server.typicode.com"; // 서버 URL로 변경
//    String SERVER_KEY = ""; // 서버 키
//    String PUSH_KEY = ""; // 푸쉬 키
//    String callback = "";
//    public static final String BASE_URL = "https://fcm.googleapis.com/";

    @Override
    protected void executeWithParam(JSONObject param) {
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        String callback = "";
        try {

            callback = param.getJSONObject("param").getString("callback");



            listener.resultCallback("callback", callback, param);

        } catch (Exception e) {
            e.printStackTrace();
        }
        sendNoti();
        msg();


    }

    private void sendNoti() {
        ApiUtils.getClients().sendNotification(new PushNotification(new NotificationData("title","message"),TO))
                .enqueue(new Callback<PushNotification>() {
                    @Override
                    public void onResponse(Call<PushNotification> call, Response<PushNotification> response) {
                        if(response.isSuccessful()){
                            Log.d("success","notification sent");

                        }
                        else{
                            Log.d("fail","fail");
                        }
                    }

                    @Override
                    public void onFailure(Call<PushNotification> call, Throwable t) {

                    }
                });
    }

    private void msg() {
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseMessaging.getInstance()
                                    .subscribeToTopic("All");
                        }
                    });

        }else{
            FirebaseMessaging.getInstance()
                    .subscribeToTopic("All");
        }

    }


}
