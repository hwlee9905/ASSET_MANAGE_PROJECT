package com.mcnc.bizmob.plugin.nat;

import android.app.ProgressDialog;
import android.util.Log;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.http.HttpClientUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 01.클래스 설명 : 멀티 (sync) 통신을 위한 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 멀티 통신 기능 <br>
 * 04.관련 API/화면/서비스 : HttpClientUtil, Logger, RUtil, Apache http library <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-05                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class CommunicatorPlugin extends BMCPlugin {
    /**
     * class 명
     */
    private final String TAG = this.getClass().getName();

    /**
     * callback 명
     */
    private String callback = "";

    /**
     * 멀티 통신 정보를 탐을 배열
     */
    private JSONArray commArrays = new JSONArray();

    /**
     * 시작 시간
     */
    long start = 0;

    /**
     * 멀티 통신후 응답을 건 바이 건으로 받을 것인지, 한번에 받을 것인지
     */
    private boolean isMultipleResponse = false;

    /**
     * 최종적으로 reponse 보내는 객체
     */
    private JSONObject multipleResultObj = new JSONObject();

    /**
     * 개별 response들을 담을 배열
     */
    private JSONArray multipleArrays = new JSONArray();

    /**
     * 개별 response result를 담는 객체
     */
    private JSONObject singleResult;

    /**
     * 멀티 통신 개수
     */
    private int cnt;

    @Override
    public void executeWithParam(JSONObject data) {
        // 병렬 통신 여부 (async), 미지원
        boolean isMultiple = false;
        // 프로그레스 표시 여부.
        boolean hasProgress = false;
        int readTimeout = 6000;

        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");

                if (param.has("multiple")) {
                    isMultiple = param.getBoolean("multiple");
                }

                if (param.has("progress")) {
                    hasProgress = param.getBoolean("progress");
                }

                if (param.has("read_timeout")) {
                    readTimeout = param.getInt("read_timeout");
                }

                if (param.has("callback")) {
                    callback = param.getString("callback");
                }

                if (param.has("multiple_response")) {
                    isMultipleResponse = param.getBoolean("multiple_response");
                }

                if (param.has("comm")) {
                    commArrays = param.getJSONArray("comm");
                }


                if (hasProgress) {
                    showProgress();
                }
                // 프로그레스가 있으면 맨마지막에 한번에 배열로 담아서 처리
                // 프로그레스가 없으면 배열없이 오브젝트만. 서버에서 헤더 바디만 옴..

                HttpClientUtil.setReadTimeOut(readTimeout);

                if (commArrays.length() == 1) { //통신이 1건인경우. 순차 병렬 상관 없음.
                    start = System.currentTimeMillis();

                    JSONObject singleComm = commArrays.getJSONObject(0);
                    final String trCode = singleComm.getString("trcode");

                    //fake 통신 여부 확인 (fake 통신은 서버 미구현 시 원활한 개발을 위해 사용됨)
                    boolean isFake = false;
                    if(singleComm.has("fake")){
                        isFake = singleComm.getBoolean("fake");
                    }

                    if (isFake) { //fake 통신이다.
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                //cabllck을 준다.
                                JSONObject fakeReuslt = null;
                                try {
                                    fakeReuslt = getJSONFromAssets("json/response/" + trCode + ".json");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                listener.resultCallback("callback", callback, fakeReuslt);
                            }
                        });
                    } else {

                        singleComm.put("requestURL", getManager().getSetting().getServerURL() + trCode + ".json");
                        String msg = singleComm.getJSONObject("message").toString();
                        String postURL = singleComm.getString("requestURL");

                        Logger.i("HttpPlugin", "single requestURL : " + postURL);
                        Logger.i("HttpPlugin", "single message : " + msg.toString());

                        HttpPost post = new HttpPost(postURL);

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("message", msg.toString()));
                        UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, String.valueOf(StandardCharsets.UTF_8));
                        post.setEntity(ent);

                        HttpClientUtil.getHttpClient().execute(post, singleHttpResponseHandler);
                    }

                } else if (commArrays.length() > 1 && !isMultiple) { //통신이 1건 이상인 경우 + 순차통신
                    start = System.currentTimeMillis();

                    for (int i = 0; i < commArrays.length(); i++) {
                        JSONObject singleComm = commArrays.getJSONObject(i);
                        String trCode = singleComm.getString("trcode");
                        singleComm.put("requestURL", getManager().getSetting().getServerURL() + trCode + ".json");
                        String msg = singleComm.getJSONObject("message").toString();
                        String postURL = singleComm.getString("requestURL");

                        Logger.i("HttpPlugin", "multiple (" + i + ") requestURL : " + postURL);
                        Logger.i("HttpPlugin", "multiple (" + i + ") message : " + msg.toString());

                        HttpPost post = new HttpPost(postURL);

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("message", msg.toString()));
                        UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, String.valueOf(StandardCharsets.UTF_8));
                        post.setEntity(ent);

                        HttpClientUtil.getHttpClient().execute(post, multiHttpResponseHandler);
                    }

                } else if (commArrays.length() > 1 && isMultiple) { //통신이 1건 이상인 경우 + 병렬 통신
                    // TODO: 2016-06-02
                    //아직 미지원
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject root = new JSONObject();
            JSONObject header = new JSONObject();
            try {
                root.put("header", header);
                root.put("body", new JSONObject());
                header.put("result", false);
                if (e instanceof HttpResponseException) {            // HTTP
                    header.put("error_code", "HE0" + ((HttpResponseException) e).getStatusCode());
                    header.put("error_text", e.getMessage());
                } else if (e instanceof ConnectTimeoutException) {    // Connect
                    header.put("error_code", "NE0001");
                    header.put("error_text", e.getMessage());
                } else if (e instanceof HttpHostConnectException) {    // Connect
                    header.put("error_code", "NE0002");
                    header.put("error_text", e.getMessage());
                } else if (e instanceof SocketTimeoutException) {    // Read
                    header.put("error_code", "NE0003");
                    header.put("error_text", "SocketTimeoutException");
                } else if (e instanceof IOException) {            // 기타 예상치못한 네트워크 에러
                    header.put("error_code", "NE0004");
                    header.put("error_text", e.getMessage());
                } else if (e instanceof NullPointerException) {
                    header.put("error_code", "CE0001");                // 기타 컨테이너 에러
                    header.put("error_text", "NullPointerException");
                } else {
                    header.put("error_code", "CE0002");                // 기타 컨테이너 에러
                    header.put("error_text", e.getMessage());
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            JSONObject errorObj = new JSONObject();
            JSONArray errorArray = new JSONArray();
            errorArray.put(root);
            try {
                errorObj.put("response", errorArray);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            listener.resultCallback("callback", callback, errorObj);
//            if (getActivity() instanceof BMCFragmentActivity) {
//                BMCFragmentActivity bmcFragmentActivity = (BMCFragmentActivity) getActivity();
//                bmcFragmentActivity.networkError(getActivity(), root);
//            } else if (getActivity() instanceof BMCActivity) {
//                BMCActivity bmcActivity = (BMCActivity) getActivity();
//                bmcActivity.networkError(getActivity(), root);
//            }
        }
    }

    /**
     * 멀티 통신 개수가 1건 이상인 경우 사용하는 responseHandler
     */
    ResponseHandler<String> multiHttpResponseHandler = new ResponseHandler<String>() {
        @Override
        public String handleResponse(final HttpResponse res) throws ClientProtocolException, IOException {
            try {
                if (res.getStatusLine().getStatusCode() == 200) {

                    JSONObject singleResponse = new JSONObject(EntityUtils.toString(res.getEntity()));

                    if (!isMultipleResponse) { // 통신 완료 후, 건바이 건으로 리절트를 넘겨 받기 원하는 경우.
                        singleResult = new JSONObject();
                        JSONArray array = new JSONArray();
                        array.put(singleResponse);
                        singleResult.put("response", array);

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                cnt++;
                                if (cnt >= commArrays.length()) {

                                    try {
                                        dismissProgress();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    cnt = 0;
                                }
                                listener.resultCallback("callback", callback, singleResult);
                            }
                        });


                    } else { // 통신 완료 후, 모든 통신에 대해 한번에 콜백을 받기 원하는 경우.
                        multipleArrays.put(singleResponse);
                        cnt++;

                        if (cnt >= commArrays.length()) {

                            multipleResultObj.put("response", multipleArrays);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.resultCallback("callback", callback, multipleResultObj);
                                    multipleArrays = new JSONArray();
                                    multipleResultObj = new JSONObject();
                                }
                            });
                            dismissProgress();
                            cnt = 0;
                        }
                    }


                } else {
                    JSONObject singleResponse = new JSONObject();
                    JSONObject header = new JSONObject();

                    singleResponse.put("header", header);
                    singleResponse.put("body", new JSONObject());
                    header.put("result", false);

                    int resCode = res.getStatusLine().getStatusCode();

                    header.put("error_code", "HE0" + resCode);
                    header.put("error_text", "HTTP Response Error");

                    if (!isMultipleResponse) {
                        singleResult = new JSONObject();
                        JSONArray array = new JSONArray();
                        array.put(singleResponse);
                        singleResult.put("response", array);

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                cnt++;
                                listener.resultCallback("callback", callback, singleResult);
                            }

                        });

                        if (cnt >= commArrays.length()) {
                            dismissProgress();
                            cnt = 0;
                        }
                    } else {
                        multipleArrays.put(singleResponse);
                        cnt++;
                        if (cnt >= commArrays.length()) {
                            multipleResultObj.put("response", multipleArrays);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.resultCallback("callback", callback, multipleResultObj);
                                    multipleArrays = new JSONArray();
                                    multipleResultObj = new JSONObject();
                                }
                            });

                            dismissProgress();
                            cnt = 0;
                        }

                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    /**
     * 멀티 통신 개수가 1개 인경우 사용할 ResponseHandler
     */
    ResponseHandler<String> singleHttpResponseHandler = new ResponseHandler<String>() {
        @Override
        public String handleResponse(final HttpResponse res) throws ClientProtocolException, IOException {

            try {

                if (res.getStatusLine().getStatusCode() == 200) {
                    JSONObject singleResponse = new JSONObject(EntityUtils.toString(res.getEntity()));
                    singleResult = new JSONObject();
                    JSONArray array = new JSONArray();
                    array.put(singleResponse);
                    singleResult.put("response", array);

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            listener.resultCallback("callback", callback, singleResult);
                        }
                    });
                } else {

                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            JSONObject singleResponse = new JSONObject();
                            JSONObject header = new JSONObject();
                            JSONArray array = new JSONArray();
                            try {
                                singleResponse.put("header", header);
                                singleResponse.put("body", new JSONObject());
                                header.put("result", false);

                                int resCode = res.getStatusLine().getStatusCode();

                                header.put("error_code", "HE0" + resCode);
                                header.put("error_text", "HTTP Response Error");
                                singleResult = new JSONObject();
                                array.put(singleResponse);
                                singleResult.put("response", array);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            listener.resultCallback("callback", callback, singleResult);

                        }
                    });
                }

            } catch (
                    ParseException e1
                    )

            {
                e1.printStackTrace();
                getManager().sendCallback("error", "", new JSONObject());
            } catch (
                    JSONException e
                    )

            {
                e.printStackTrace();
                getManager().sendCallback("error", "", new JSONObject());
            } finally

            {

                // 전문이 너무 빨리 끝날 경우 프로그레스가 닫히지 않을 수가 있음.
                long end = System.currentTimeMillis();
                long processTime = end - start;
                if (processTime < 200 && processTime > 0) {
                    try {
                        Thread.sleep(200 - processTime);
                    } catch (InterruptedException e) {
                    }
                }

                try {
                    dismissProgress();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    };

    private void showProgress() throws JSONException {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (getActivity() != null) {
                    if (!getActivity().isFinishing()) {
                        if (getProgressDialog() != null) {
                            getProgressDialog().setTitle("");
                            String msg = getActivity().getString(RUtil.getStringR(getActivity(), "pleaseWait"));
                            //Log.d("progress","progress :"+ msg);
                            ((ProgressDialog) getProgressDialog()).setMessage(msg);
                            getProgressDialog().show();
                        }
                    } else {
                        Log.d(TAG, "Activity does not exist, cannot call a showProgress method. Do not press the back button while processing.");
                    }
                } else {
                    Log.d(TAG, "Activity does not exist, cannot call a showProgress method.");
                }
            }
        });
    }

    private void dismissProgress() throws JSONException {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (getProgressDialog() != null) {
                    getProgressDialog().dismiss();
                }
            }
        });
    }

    private JSONObject getJSONFromAssets(String assetsFilePath) throws JSONException {
        String jsonStr = null;
        InputStream inputStream = null;
        try {
            inputStream = getActivity().getAssets().open(assetsFilePath);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            jsonStr = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    inputStream = null;
                }
            }
        }
        JSONObject jsonObj = null;
        jsonObj = new JSONObject(jsonStr);
        return jsonObj;
    }


}