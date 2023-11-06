package com.mcnc.bizmob.plugin.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;


import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.BMCPermission;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 01.클래스 설명 : 디바이스 위치 정보(GPS) 기능을 호출하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 디바이스 위치 정보(GPS) 탐색 <br>
 * 04.관련 API/화면/서비스 : Logger <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-20                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class NewLocationPlugin extends BMCPlugin {
    private String type = "location";
    private String progressTitle = "";
    private String progressMessage = "";
    private String provider = "";
    private boolean showProgress = false;
    private JSONObject data;
    private BMCPermission locationPermission;
    private final int REQ_CODE_PERMISSION_LOCATION = 124;
    private final int REQ_CODE_GPS_SETTING = 224;
    private final int REQ_CODE_PERMISSION_CHECK = 225;

    private final String TAG = this.toString();
    private double myLat, myLng;
    private boolean isGetLocation = false;
    private boolean isFirst = true;
    private String address = "";
    private String callback = "";
    private LocationManager myLocationManager = null;
    private LocationListener myLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            provider = location.getProvider();
            Logger.d(TAG, "Provider :: " + provider + ", Lat:" + location.getLatitude() + "  Lng:" + location.getLongitude());

            myLat = location.getLatitude();
            myLng = location.getLongitude();
            isGetLocation = true;
            if (isFirst) {
                isFirst = false;
                JSONObject root = new JSONObject();
                address = getAddress(getActivity(), myLat, myLng);
                try {
                    root.put("result", isGetLocation);
                    root.put("gps_enabled", isGpsEnabled());
                    root.put("provider", provider);
                    root.put("latitude", myLat);
                    root.put("longitude", myLng);
                    root.put("address", address);
                    root.put("isLastKnownLoc", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listener.resultCallback("callback", callback, root);
                hideProgress();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * 디바이스의 위치 정보를 갖고 오는 메소드를 실행하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-20                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 디바이스 위치정보 조회를 위한 정보 값 보유 <br><br>
     *             <p>
     *             Return : (JSONObject) 로직 수행 성공여부, 위도, 경도, 주소 반환
     */
    @Override
    protected void executeWithParam(JSONObject data) {
        this.data = data;
        try {
            if (data.has("param")) {

                JSONObject param = data.getJSONObject("param");
                if (param.has("type")) {
                    type = param.getString("type");
                }
                if (param.has("callback")) {
                    callback = param.getString("callback");
                }
                if (type.equalsIgnoreCase("location")) {
                    if (param.has("progress")) {
                        showProgress = param.getBoolean("progress");
                    }
                    if (param.has("progress_title")) {
                        progressTitle = param.getString("progress_title");
                    }
                    if (param.has("progress_message")) {
                        progressMessage = param.getString("progress_message");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        locationPermission = new BMCPermission.Builder(getActivity(), REQ_CODE_PERMISSION_LOCATION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
                .setFragment(getFragment())
                .setPlugin(this)
                .setNegativeButton(getActivity().getResources().getString(RUtil.getStringR(getActivity(), "txt_setting")), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + getActivity().getPackageName()));
                            startActivityForResultFromPlugin(intent, REQ_CODE_PERMISSION_CHECK);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).setDeniedMessage(getActivity().getString(RUtil.getStringR(getActivity(), "txt_location_permission_guide")))
                .setPositiveButton(getActivity().getString(RUtil.getStringR(getActivity(), "txt_close")), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject root = new JSONObject();
                        try {
                            root.put("result", false);
                            root.put("gps_enabled", isGpsEnabled());
                            root.put("error_code", "ERR0001");
                            root.put("error_message", "Gps permission denied, cannot proceed.");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        listener.resultCallback("callback", callback, root);
                    }
                })
                .build();
        if (type.equalsIgnoreCase("location")) {
            if (locationPermission.checkPermissions()) {
                afterPermissionCheck(data);
            }
        } else {
            if (type.equalsIgnoreCase("check")) {
                boolean enabled = isGpsEnabled();
                try {
                    JSONObject root = new JSONObject();
                    root.put("result", true);
                    root.put("gps_enabled", enabled);
                    listener.resultCallback("callback", callback, root);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (type.equalsIgnoreCase("setting")) {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResultFromPlugin(intent, REQ_CODE_GPS_SETTING);
            }
        }
    }

    private boolean isGpsEnabled() {
        boolean isGpsEnabled = false;
        myLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (myLocationManager != null) {
            isGpsEnabled = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return isGpsEnabled;
    }

    private void afterPermissionCheck(JSONObject data) {
        JSONObject root = new JSONObject();
        if (showProgress) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getProgressDialog() != null) {
                            if (progressTitle.length() > 0) {
                                getProgressDialog().setTitle(progressTitle);
                            }
                            if (progressMessage.length() > 0) {
                                ((ProgressDialog) getProgressDialog()).setMessage(progressMessage);
                            }
                            getProgressDialog().show();
                        }
                    }
                });
            }
        }

        try {

            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");
                int delayTime = 2;

                final boolean isGpsEnabled = requestGPSLocation(getActivity());
                boolean isNetworkEnabled = requestNetworkLocation(getActivity());

                //GPS 위치 정보 수신
                if (isGpsEnabled) {
                    // 쥐피에스로 부터 위치 변경이 올 경우 업데이트 하도록 설정
                    if (myLocationManager != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, myLocationListener);
                            }
                        });
                    }

                }

                //Network(통신사) 위치 정보 수신
                if (isNetworkEnabled) {
                    if (isGpsEnabled) { //GPS가 사용가능하다면 대기시간만큼 기다려보자.
                        if (param.has("waitting_time")) {
                            delayTime = param.getInt("waitting_time");
                        }
                    }
                    if (myLocationManager != null && !isGetLocation) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!isGetLocation) {
                                    myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, myLocationListener);
                                }
                            }
                        }, delayTime * 1000);
                    }
                }

                //둘다 사용 불가 할 경우는 마지막에 얻은 위치 정보를 수동으로 얻어옴.
                if (!isGpsEnabled && !isNetworkEnabled) {
                    Location loc = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if(loc !=null) {
                        myLat = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                        myLng = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
                    }else{
                        Logger.i(TAG, "GPS_PROVIDER's location is null");
                        loc = myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        myLat = myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
                        myLng = myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
                    }

                    if (loc != null) {
                        isGetLocation = true;
                        address = getAddress(getActivity(), myLat, myLng);
                    } else {
                        Logger.i(TAG, "NETWORK_PROVIDER's location is null");
                    }

                    root.put("result", isGetLocation);
                    root.put("gps_enabled", isGpsEnabled());
                    root.put("provider", provider);
                    root.put("latitude", myLat);
                    root.put("longitude", myLng);
                    root.put("address", address);
                    root.put("isLastKnownLoc", true);
                    listener.resultCallback("callback", callback, root);

                    hideProgress();
                }

//                if (!checkWriteExternalPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    root.put("error_code", "ERR0001");
//                    root.put("error_message", "Gps permission denied, cannot proceed");
//                    listener.resultCallback("callback", callback, root);
//                    hideProgress();
//                } else {
//                    if (!isGpsEnabled()) {
//                        root.put("error_code", "ERR0002");
//                        root.put("error_message", "Gps is off.");
//                        listener.resultCallback("callback", callback, root);
//                        hideProgress();
//                    }
//                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void hideProgress() {
        if (myLocationManager != null && myLocationListener != null) {
            myLocationManager.removeUpdates(myLocationListener);
            myLocationManager = null;
            myLocationListener = null;
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getProgressDialog() != null) {
                        getProgressDialog().dismiss();
                    }
                }
            });
        }
    }

    /**
     * GPS가 켜져있는지 확인 하고, 켜져 있을 시 GPS 위치를 업데이트를 요청하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-20                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param context LOCATION_SERVICE 이용을 위한 context 객체.
     * @return GPS_PROVIDER 가 이용가능한지 여부.
     */

    private boolean requestGPSLocation(Context context) {
        boolean bResult = false;
        // GPS가 켜져있는지 확인한다.
        if (myLocationManager == null) {
            myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        if (myLocationManager != null && myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) /*&& myLocationManager.getLastKnownLocation(provider) != null*/) {
            bResult = true;
        }
        Logger.i(TAG, "requestGPSLocation :: " + bResult);
        return bResult;
    }

    /**
     * 네트워크 프로바이더(통신사 통신망)이 사용가능한지 확인 하고, 가능 할 시 네트워크 프로바이더를 통해 위치를 업데이트를 요청하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-20                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param context 로케이션 서비스 이용을 위한 context 객체.
     * @return 네트워크 프로바이더가 이용가능한지 여부.
     */
    private boolean requestNetworkLocation(Context context) {
        boolean bResult = false;
        if (myLocationManager == null) {
            myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        if (myLocationManager != null && myLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            bResult = true;
        }
        Logger.i(TAG, "requestNetworkLocation :: " + bResult);
        return bResult;
    }

    /**
     * 위도 경도를 기준으로 주소 정보를 갖고오는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-20                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param context 로케이션 서비스 이용을 위한 context 객체.
     * @param lat     위도 값.
     * @param lng     경도 값.
     * @return 위도, 경도 값으로 조회된 주소.
     */
    private static String getAddress(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.KOREAN);
        String addressName = "NO Location";
        try {
            List<Address> address = geocoder.getFromLocation(lat, lng, 1);
            addressName = address.get(0).getAddressLine(0);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return addressName;
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        if (reqCode == REQ_CODE_GPS_SETTING) {
            JSONObject root = new JSONObject();
            try {
                root.put("result", true);
                root.put("gps_enabled", isGpsEnabled());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listener.resultCallback("callback", callback, root);
        } else if (reqCode == REQ_CODE_PERMISSION_CHECK) {
            JSONObject root = new JSONObject();


            if (checkWriteExternalPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (type.equalsIgnoreCase("location")) {
                    if (locationPermission.checkPermissions()) {
                        afterPermissionCheck(data);
                    }
                } else {
                    if (type.equalsIgnoreCase("check") || type.equalsIgnoreCase("setting")) {
                        try {
                            root.put("result", true);
                            root.put("gps_enabled", isGpsEnabled());
                            listener.resultCallback("callback", callback, root);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                try {
                    root.put("result", false);
                    root.put("gps_enabled", isGpsEnabled());
                    root.put("error_code", "ERR0001");
                    root.put("error_message", "Gps permission denied, cannot proceed.");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listener.resultCallback("callback", callback, root);
            }
        }
    }

    private boolean checkWriteExternalPermission(String permission) {
        int res = getActivity().checkCallingPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,  String[] permissions,  final int[] grantResults) {
        boolean needToWaitUntilResumed = true;
        if (getFragment().isResumed()) {
            needToWaitUntilResumed = false;
        }

        //퍼미션 창이 안 뜬 경우 기존화면 resume 될 때까지 기다릴 필요 없음
        if (!needToWaitUntilResumed) {
            if (requestCode == REQ_CODE_PERMISSION_LOCATION) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    afterPermissionCheck(data);
                } else {
                    locationPermission.showDeniedDialog();
                }
            }
            //퍼미션 창이 뜬 경우 기존 화면이 resume 될 때까지 기다려야함
        } else {
            new Thread(new Runnable() {
                boolean keepGoing = true;

                @Override
                public void run() {
                    while (keepGoing) {

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (getFragment().isResumed()) {
                            keepGoing = false;
                        }

                        if (getFragment().isResumed()) {
                            if (requestCode == REQ_CODE_PERMISSION_LOCATION) {
                                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                                    if (type.equalsIgnoreCase("location")) {
                                        afterPermissionCheck(data);
                                    } else if (type.equalsIgnoreCase("check")) {
                                        try {
                                            JSONObject root = new JSONObject();
                                            root.put("result", true);
                                            root.put("gps_enabled", isGpsEnabled());
                                            listener.resultCallback("callback", callback, root);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (type.equalsIgnoreCase("setting")) {
                                        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivityForResultFromPlugin(intent, REQ_CODE_GPS_SETTING);
                                    }

                                } else {

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            locationPermission.showDeniedDialog();
                                        }
                                    });

                                }
                            }
                        }

                    }
                }
            }).start();
        }
    }
}
