package com.mcnc.bizmob.plugin.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentProviderResult;
import android.content.DialogInterface;
import android.content.pm.PackageManager;


import com.mcnc.bizmob.core.def.Def;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.BMCPermission;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.util.contact.SearchHelperForDeviceContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 연락처 삭제 / 추가 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 연락처 삭제  / 추가 기능 (조회 기능은 Core 있음) <br>
 * 04.관련 API/화면/서비스 : SearchHelperForDeviceContact, BMCPermission <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-05                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class ContactPlugin extends BMCPlugin {

    final String PARAM = "param";
    final String RESULT = "result";

    final String CONTACT_NAME = "contact_name";
    final String PHONE_NUMBER = "phone_number";
    final String TEL_NUMBER = "tel_number";
    final String GROUP_ID = "group_id";
    final String GROUP_NAME = "group_name";

    final String CONTACT_UID = "contact_uid";

    private JSONObject data;

    private String callback = "";

    private BMCPermission contactPermission;

    private final int REQ_CODE_PERMISSION_CONTACT = 125;

    @Override
    public void executeWithParam(JSONObject data) {
        this.data = data;

        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");
                if (param.has("callback")) {
                    callback = param.getString("callback");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        contactPermission = new BMCPermission.Builder(getActivity(), REQ_CODE_PERMISSION_CONTACT, new String[]{Manifest.permission.READ_CONTACTS})
                .setFragment(getFragment())
                .setPlugin(this)
                .setDeniedMessage(getActivity().getString(RUtil.getStringR(getActivity(), "txt_contact_permission_guide")))
                .setPositiveButton(getActivity().getString(RUtil.getStringR(getActivity(), "txt_close")), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject result = null;
                        try {
                            result = new JSONObject();
                            result.put(Def.CONTACT_TOTAL_COUNT, 0);
                            result.put(Def.CONTACT_COUNT, 0);
                            result.put("result", false);
                            result.put("list", new JSONArray());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        listener.resultCallback("callback", callback, result);
                    }
                })
                .build();

        if (contactPermission.checkPermissions()) {
            afterPermissionCheck(data);
        }
    }

    private void afterPermissionCheck(JSONObject data) {

        try {
            String commandID = data.getString("id");

            if (commandID.equals("ADD_CONTACTS")) {
                addContatct(data);
            } else if (commandID.equals("DEL_CONTACT")) {
                delContatct(data);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addContatct(JSONObject jsonData) throws Exception {

        String errorMessage = "";

        String contactName = "";
        String phoneNumber = "";
        String telNumber = "";
        String groupID = "";
        String groupName = "";
        String callback = "";
        boolean flag = false;
        ContentProviderResult[] resultData = null;
        boolean result = true;
        JSONObject responseData = new JSONObject();

        long start = System.currentTimeMillis();

        int contackCount = 0;

        try {
            responseData.put(RESULT, false);
            JSONObject param = jsonData.getJSONObject(PARAM);

            JSONArray array = param.getJSONArray("contacts");
            contackCount = array.length();
            flag = param.getBoolean("fail_flag");
            callback = param.getString("callback");

            showProgress();

            JSONArray resArr = new JSONArray();
            responseData.put("result_array", resArr);

            if (flag) {
                for (int i = 0; i < array.length(); i++) {
                    if (array.getJSONObject(i).has(CONTACT_NAME)) {
                        contactName = array.getJSONObject(i).getString(CONTACT_NAME);
                    }
                    if (array.getJSONObject(i).has(PHONE_NUMBER)) {
                        phoneNumber = array.getJSONObject(i).getString(PHONE_NUMBER);
                    }
                    if (array.getJSONObject(i).has(TEL_NUMBER)) {
                        telNumber = array.getJSONObject(i).getString(TEL_NUMBER);
                    }
                    if (array.getJSONObject(i).has(GROUP_ID)) {
                        groupID = array.getJSONObject(i).getString(GROUP_ID);
                    }
                    if (array.getJSONObject(i).has(GROUP_NAME)) {
                        groupName = array.getJSONObject(i).getString(GROUP_NAME);
                    }

                    resultData = SearchHelperForDeviceContact.addPersonContact(getActivity(), Def.ACCOUNT_TYPE, Def.ACCOUNT_NAME, contactName, phoneNumber, telNumber, groupID);

                    if (resultData.length > 0) {
                        resArr.put(true);
                    } else {
                        result = false;
                        resArr.put(false);
                    }
                }
            } else {
                for (int i = 0; i < array.length(); i++) {
                    if (array.getJSONObject(i).has(CONTACT_NAME)) {
                        contactName = array.getJSONObject(i).getString(CONTACT_NAME);
                    }
                    if (array.getJSONObject(i).has(PHONE_NUMBER)) {
                        phoneNumber = array.getJSONObject(i).getString(PHONE_NUMBER);
                    }
                    if (array.getJSONObject(i).has(TEL_NUMBER)) {
                        telNumber = array.getJSONObject(i).getString(TEL_NUMBER);
                    }
                    if (array.getJSONObject(i).has(GROUP_ID)) {
                        groupID = array.getJSONObject(i).getString(GROUP_ID);
                    }
                    if (array.getJSONObject(i).has(GROUP_NAME)) {
                        groupName = array.getJSONObject(i).getString(GROUP_NAME);
                    }

                    resultData = SearchHelperForDeviceContact.addPersonContact(getActivity(), Def.ACCOUNT_TYPE, Def.ACCOUNT_NAME, contactName, phoneNumber, telNumber, groupID);

                    if (resultData.length > 0) {
                        resArr.put(true);
                    } else {
                        result = false;
                        resArr.put(false);
                        break;
                    }
                }
            }

            responseData.put(RESULT, result);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                responseData.put(RESULT, result);
                errorMessage = e.getMessage();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (!responseData.has("result_array")) {
                    JSONArray array = new JSONArray();
                    jsonData.put("result_array", array);
                }
                JSONArray array2 = jsonData.getJSONArray("result_array");
                int count = array2.length();
                if (contackCount > count) {
                    for (int i = 0; i < contackCount - count; i++) {
                        array2.put(false);
                    }
                }
            } catch (JSONException e) {
            }

            // 전문이 너무 빨리 끝날 경우 프로그레스가 닫히지 않을 수가 있음.
            long end = System.currentTimeMillis();
            long processTime = end - start;
            if (processTime < 200 && processTime > 0) {
                try {
                    Thread.sleep(200 - processTime);
                } catch (InterruptedException e) {
                }
            }

//			responseData.remove(RESULT);
//			responseData = BMCUtil.resultMessageParser(result, responseData, errorMessage);
            listener.resultCallback("callback", callback, responseData);

            dismissProgress();
        }

    }

    private void delContatct(JSONObject jsonData) throws Exception {

        String errorMessage = "";

        String contactUid = "";
        String contactName = "";
        String callback = "";
        int resultData = -1;
        boolean result = false;
        JSONObject responseData = new JSONObject();

        try {
            responseData.put(RESULT, false);
            JSONObject param = jsonData.getJSONObject(PARAM);
            if (param.has(CONTACT_UID)) {
                contactUid = param.getString(CONTACT_UID);
            }
            if (param.has(CONTACT_NAME)) {
                contactName = param.getString(CONTACT_NAME);
            }

            callback = jsonData.getJSONObject(PARAM).getString("callback");
            resultData = SearchHelperForDeviceContact.deletePerson(getActivity(), contactUid, contactName);
            result = resultData != -1 ? true : false;
            responseData.put(RESULT, result);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                responseData.put(RESULT, result);
                errorMessage = e.getMessage();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        listener.resultCallback("callback", callback, responseData);
    }

    private void showProgress() throws JSONException {

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (getProgressDialog() != null) {
                    getProgressDialog().setTitle("");
                    ((ProgressDialog) getProgressDialog()).setMessage(getActivity().getString(RUtil.getStringR(getActivity(), "pleaseWait")));
                    getProgressDialog().show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        if (requestCode == REQ_CODE_PERMISSION_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                afterPermissionCheck(data);
            } else {
                contactPermission.showDeniedDialog();
            }
        }
    }

}
