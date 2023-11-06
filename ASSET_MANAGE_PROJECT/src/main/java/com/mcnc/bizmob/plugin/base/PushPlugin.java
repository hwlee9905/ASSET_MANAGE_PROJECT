package com.mcnc.bizmob.plugin.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.mcnc.bizmob.manage.Init;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.config.AppConfigReader;
import com.mcnc.bizmob.core.util.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.mcnc.bizmob.core.application.BMCInit.context;

/**
 * 01.클래스 설명 : 푸쉬 (GMC) 관련 기능을 수행하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : Push key, Push Product Id 등록,BizPushManager 호출 <br>
 * 04.관련 API/화면/서비스 : BMCManager, BMCPlugin, CompleteListener, AppConfig Logger<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-22                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class PushPlugin extends BMCPlugin {

    /**
     * 클라이언트로 부터 전달 받은 데이터를 각각의 요청에 맞는 push 관련 기능을 수행하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 푸시 관련 이벤트 수행에 필요한 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {
        try {
            String commandID = data.getString("id");

            if (commandID.equals("GET_PUSHKEY")) {
                getPushKey(data);
            } else if (commandID.equals("PUSH_ALARM_SETTING_INFO")) {
                pushAlarmSettingInfo(data);
            } else if (commandID.equals("PUSH_REGISTRATION")) {
                pushRegistration(data);
            } else if (commandID.equals("PUSH_UPDATE_ALARM_SETTING")) {
                pushUpdateAlarmSetting(data);
            } else if (commandID.equals("PUSH_GET_MESSAGES")) {
                pushGetMessages(data);
            } else if (commandID.equals("PUSH_MARK_AS_READ")) {
                pushMarkAsRead(data);
            } else if (commandID.equals("PUSH_GET_UNREAD_PUSH_MESSAGE_COUNT")) {
                pushGetUnreadMessageCount(data);
            } else if (commandID.equals("SET_BADGE_COUNT")) {
                setBadgeCount(data);
            } else if (commandID.equals("SEND_PUSH_MESSAGE")) {
                sendMessages(data);
            } else if (commandID.equals("CHECK_PUSH_RECEIVED")) {
                pushReceivedCheck(data);
            } else if (commandID.equals("GET_PUSH_MESSAGE_TYPE_LIST")) {
                getPushMessageTypeList(data);
            } else if (commandID.equals("GET_PUSH_AGREEMENT_INFO")) {
                getPushAgreementInfo(data);
            } else if (commandID.equals("UPDATE_PUSH_AGREEMENT_INFO")) {
                updatePushAgreementInfo(data);
            } else {
                Logger.d("PushPlugin", "this unknow type");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Push Product ID 및 Push Key 를 등록 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 푸시 키 반환에 필요한 callback 값 보유 <br><br>
     */
    private void getPushKey(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");
            String callback = param.getString("callback");

            SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
            String fcmToken = preferences.getString("registration_id", "");

            if (!TextUtils.isEmpty(fcmToken)) {
                result.put("result", true);
                result.put("type", "fcm");
                result.put("resultCode", "0000");
                result.put("resultMessage", fcmToken);
            } else {
                try {
                    result.put("result", false);
                    result.put("type", "fcm");
                    result.put("resultCode", "9999");
                    result.put("resultMessage", fcmToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Logger.d("PushPlugin", "push fcm registration id : " + fcmToken);

            listener.resultCallback("callback", callback, result);
        }
    }

    /**
     * 디바이스를 bizPush 수신이 가능하도록 등록 하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터,  bizPush Server에 디바이스 등록에 필요한 값 보유 <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지
     */

    private void pushRegistration(final JSONObject data) throws JSONException {

        if (data.has("param")) {

            BMCManager.getInstance().executeInterfaceFromID("GET_DEVICEINFO", new JSONObject(), new CompleteListener() {

                @Override
                public void resultCallback(String type2, String callback2, JSONObject device) {

                    JSONObject result = new JSONObject();

                    try {

                        JSONObject param = data.getJSONObject("param");
                        String callback = param.getString("callback");

                        String type = param.getString("type");

                        String pushKey = param.getString("push_key");
                        String userID = param.getString("user_id");
                        String appName = param.getString("app_name");

                        String major = device.getString("app_major_version");
                        String minor = device.getString("app_minor_version");
                        String build = device.getString("app_build_version");
                        String appVersion = major + "." + minor + "." + build;

                        String deviceOsType = device.getString("device_os_type");
                        String deviceOsVersion = device.getString("device_os_version");
                        String deviceUID = device.getString("device_id");
                        String deviceType = device.getString("device_type");
                        String deviceModel = device.getString("model");
                        String pushProviderType = "FCM";
                        String carrierCode = device.getString("carrier_code");

                        try {
                            if (type.equals("push")) {
                                result = pushRegistDeviceInterface(getActivity(), pushKey, userID, appName, appVersion, deviceOsType, pushProviderType, deviceOsVersion, deviceUID, deviceType, deviceModel, carrierCode);
                            } else if (type.equals("bizpush")) {
                                result = bizPushRegistDeviceInterface(getActivity(), pushKey, userID, appName, appVersion, deviceOsType, pushProviderType, deviceOsVersion, deviceUID, deviceType, deviceModel, carrierCode);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            result.put("result", false);
                        }

                        listener.resultCallback("callback", callback, result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 설정된 푸시 알람을 갖고 오는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터,  설정된 푸시 알람을 갖고 오는데 필요한 값 보유(ex. push key, callback, user id) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지
     */
    private void pushAlarmSettingInfo(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {

            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");

            String pushKey = param.getString("push_key");
            String userId = param.getString("user_id");

            try {
                result = alarmSettingInfo(getActivity(), pushKey, userId);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }

        listener.resultCallback("callback", callback, result);
    }


    /**
     * 푸시 알람을 설정 하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 푸시 알람을 설정 하는데 필요한 값 보유(ex. push key, callback, 푸시 알람 수신여부 (true or false)) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지
     */
    private void pushUpdateAlarmSetting(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            String pushKey = param.getString("push_key");
            String userId = param.getString("user_id");
            boolean enabled = param.getBoolean("enabled");

            try {
                result = updateAlarmSetting(getActivity(), pushKey, userId, enabled);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }

        listener.resultCallback("callback", callback, result);
    }

    /**
     * Push 메시지를 수신 여부를 체크하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, Push 메시지를 수신 여부를 체크 하는데 필요한 값 보유(ex. message id, user id) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지
     */
    private void pushReceivedCheck(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            String messageId = param.getString("message_id");
            String userId = param.getString("user_id");
            //수신확인
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String date = sdf.format(Calendar.getInstance().getTime());

            try {
                result = receivedCheck(getActivity(), messageId, userId, "CHECKED", date, "true");
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }

        listener.resultCallback("callback", callback, result);
    }

    /**
     * 푸시 메세지 목록을 가져오는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, Push 메시지 목록을 가져 오는데 필요한 값 보유(ex. page_index(푸시 메세지를 가져올 페이지 번호), item_count(해당 페이지에서 가져올 푸시 메세지 개수), user id, app name) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지
     */
    private void pushGetMessages(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            int pageIndex = param.getInt("page_index");
            int itemCount = param.getInt("item_count");
            String userId = param.getString("user_id");
            String appName = param.getString("app_name");

            try {
                result = getMessages(getActivity(), pageIndex, itemCount, userId, appName);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }

        listener.resultCallback("callback", callback, result);
    }


    /**
     * Push 메시지 읽음 처리를 하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, Push 메시지 읽음 처리를 하는데 필요한 값 보유(ex. trx_id(푸시 메세지 아이디), user id, trx_day(푸시를 읽은 날짜.), callback 명) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지.
     */
    private void pushMarkAsRead(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            String trxDay = param.getString("trx_day");
            String userId = param.getString("user_id");
            String trxId = param.getString("trx_id");
            boolean read = param.getBoolean("read");

            try {
                result = markAsRead(getActivity(), trxDay, userId, trxId, read);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }

        listener.resultCallback("callback", callback, result);
    }

    /**
     * 읽지 않은 푸시 메세지 개수를 가져오는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 읽지 않은 푸시 메세지 개수를 가져오는데 필요한 값 보유(ex. app name, user id) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지.
     */
    private void pushGetUnreadMessageCount(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            String appName = param.getString("app_name");
            String userId = param.getString("user_id");

            try {
                result = getUnreadPushMessage(getActivity(), userId, appName);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }

        listener.resultCallback("callback", callback, result);
    }

    /**
     * 뱃지 카운트를 설정하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 뱃지 카운트를 설정하는데 필요한 값 보유(ex.count, callback) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부
     */
    private void setBadgeCount(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            int badgeCount = param.getInt("badge_count");

            Intent badgeIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            badgeIntent.putExtra("badge_count", badgeCount);
            badgeIntent.putExtra("badge_count_package_name", getActivity().getPackageName());
            badgeIntent.putExtra("badge_count_class_name", Init.LAUNCHER_ACTIVITY.getName());
            getActivity().sendBroadcast(badgeIntent);
        }

        listener.resultCallback("callback", callback, result);
    }

    /**
     * 푸시 메세지를 전송 하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 푸시 메세지를 전송 하는데 필요한 값 보유(ex. app name, user id, 사용자 id, 제목, 내용, 날짜, callback 등) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지.
     */
    private void sendMessages(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            String trxType = param.getString("trx_type");
            String scheduleDate = param.getString("schedule_date");
            String appName = param.getString("app_name");
            JSONArray toUsers = param.getJSONArray("to_users");
            JSONArray toGroups = param.getJSONArray("to_groups");
            boolean toAll = param.getBoolean("to_all");
            String fromUser = param.getString("from_user");
            String messageSubject = param.getString("message_subject");
            String messageCategory = param.getString("message_category");
            String messageContent = param.getString("message_content");
            JSONObject messagePayload = param.getJSONObject("message_payload");

            try {
                result = sendMessages(getActivity(), trxType, scheduleDate, appName, toUsers, toGroups, toAll, fromUser, messageSubject, messageCategory, messageContent, messagePayload);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }
        listener.resultCallback("callback", callback, result);
    }

    /**
     * 푸시 메세지의 type list를 받아오는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 푸시 메세지의 type list를 받아오는데 필요한 값 보유(ex. agreeFlag(수신동의여부), deletedFlag(삭제 여부), messageTypeIds(메세지 종류), callback 등) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지.
     */
    public void getPushMessageTypeList(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            boolean agreeFlag = param.getBoolean("agreeFlag");
            boolean deletedFlag = param.getBoolean("deletedFlag");
            JSONArray messageTypeIds = param.getJSONArray("messageTypeIds");

            try {
                result = getMessageTypeList(getActivity(), agreeFlag, deletedFlag, messageTypeIds);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }
        listener.resultCallback("callback", callback, result);
    }

    /**
     * 푸시 수신 동의 정보를 받아오는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 푸시 수신 동의 정보를 받아오는데 필요한 값 보유(ex. user Id, app name, messageTypeIds(메세지 종류), callback 등) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지.
     */
    public void getPushAgreementInfo(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            String userId = param.getString("userId");
            String appName = param.getString("appName");
            JSONArray messageTypeIds = param.getJSONArray("messageTypeIds");

            try {
                result = getAgreementInfo(getActivity(), userId, appName, messageTypeIds);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }
        listener.resultCallback("callback", callback, result);
    }

    /**
     * 푸시 수신 동의 정보를 업데이트 하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 푸시 수신 동의 정보를 업데이트 하는데 필요한 값 보유(ex. user Id, app name, messageTypeIds(메세지 종류), agreeFlag(동의여부), callback 등) <br><br>
     *             Return : (JSONObject) 작업 수행 성공 여부, 결과 코드, 결과 메세지.
     */
    public void updatePushAgreementInfo(JSONObject data) throws JSONException {
        JSONObject result = new JSONObject();
        String callback = "";

        if (data.has("param")) {
            JSONObject param = data.getJSONObject("param");

            callback = param.getString("callback");
            String userId = param.getString("userId");
            String messageTypeId = param.getString("messageTypeId");
            boolean agreeFlag = param.getBoolean("agreeFlag");
            String appName = param.getString("appName");


            try {
                result = updateAgreementInfo(getActivity(), userId, messageTypeId, agreeFlag, appName);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("result", false);
            }
        }
        listener.resultCallback("callback", callback, result);
    }

    /**
     * DTO(RDataDTO object)를 JSONObject 형태로 반환해 주는 메소드.<br>
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
     * @return JSONObject 형태의 RDataDTO.
     * @dto Push 관련 정보를 갖고 있는 RDataDTO 객체
     */
    private JSONObject toRDataJSONObject(RDataDTO dto) throws JSONException {
        JSONObject rDataObj = new JSONObject();
        if (dto != null && dto.getKeyValueData() != null) {
            JSONArray arrayObj = new JSONArray();
            for (KeyValueDataDTO keyValueDataDTO : dto.getKeyValueData()) {
                JSONObject keyValueDataObj = new JSONObject();
                keyValueDataObj.put("Key", keyValueDataDTO.getKey());
                keyValueDataObj.put("Value", keyValueDataDTO.getValue());
                arrayObj.put(keyValueDataObj);
            }
            rDataObj.put("KeyValueData", arrayObj);
        } else {
            rDataObj.put("KeyValueData", null);
        }
        return rDataObj;
    }

    /**
     * 일반 Push Server에 디바이스를 등록해주는 메소드.<br>
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
     * @return 등록 성공 여부와 result code 값 반환
     * @context context 객체
     * @pushKey 푸시 키.
     * @userId user Id.
     * @appName 앱 명칭.
     * @appVersion 앱 버전 정보
     * @deviceOsType Device의 OS 종률 ( 안드로이드 인지 IOS 인지).
     * @pushProviderType Push Provider의 종류
     * @deviceOsVersion Device의 OS 버전 정보
     * @deviceUid Device 고유 ID.
     * @deviceType 디바이스 종류
     * @deviceModel Device의 Model 정보.
     * @carrierCode 통신사 명칭.
     */
    private JSONObject pushRegistDeviceInterface(Context context, String pushKey, String userId, String appName,
                                                 String appVersion, String deviceOsType, String pushProviderType, String deviceOsVersion,
                                                 String deviceUid, String deviceType, String deviceModel, String carrierCode) throws Exception {

        byte[] postData = null;
        int responseCode;
        JSONObject responseJSON = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String pushUrl = getPushUrl(context);

        int len = pushUrl.length();
        int lastIndex = pushUrl.lastIndexOf("/");

        if (len != lastIndex + 1) {
            pushUrl += "/";
        }

        JSONObject RData = new JSONObject();
        JSONObject KeyValueData = new JSONObject();

        RDataDTO dto = new RDataDTO();
        dto.addKeyValueData(new KeyValueDataDTO("email", ""));
        dto.addKeyValueData(new KeyValueDataDTO("password", ""));
        dto.addKeyValueData(new KeyValueDataDTO("device_model", deviceModel));
        dto.addKeyValueData(new KeyValueDataDTO("device_os", "Android"));
        dto.addKeyValueData(new KeyValueDataDTO("os_version", deviceOsVersion));
        dto.addKeyValueData(new KeyValueDataDTO("device_id", deviceUid));
        dto.addKeyValueData(new KeyValueDataDTO("app_name", appName));
        dto.addKeyValueData(new KeyValueDataDTO("app_version", appVersion));
        dto.addKeyValueData(new KeyValueDataDTO("push_key", pushKey));

        KeyValueData = toRDataJSONObject(dto);

        RData.put("RData", KeyValueData);
        RData.put("UserID", userId);

        Logger.i("json Data ---------------- ", RData.toString());

        postData = RData.toString().getBytes();

        URL url;
        try {
            url = new URL(pushUrl + "/RegistPushDeviceServlet");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));

            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            responseCode = conn.getResponseCode();

            if (responseCode == 200) {

                responseJSON.put("result", true);
                responseJSON.put("resultCode", "C" + responseCode);
                responseJSON.put("resultMessage", "등록 성공하였습니다.");

                editor.putString("push_type", "normal_push");
                editor.commit();
            } else {
                responseJSON.put("result", false);
                responseJSON.put("resultCode", "9999");
                responseJSON.put("resultMessage", "http response error (" + responseCode + ")");
            }

        } catch (Exception e) {
            throw e;
        }

        return responseJSON;
    }

    /**
     * bizPush Server에 디바이스를 등록해주는 메소드.<br>
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
     * @return 등록 성공 여부와 result code 값 반환
     * @context context 객체
     * @pushKey 푸시 키.
     * @userId user Id.
     * @appName 앱 명칭.
     * @appVersion 앱 버전 정보
     * @deviceOsType Device의 OS 종률 ( 안드로이드 인지 IOS 인지).
     * @pushProviderType Push Provider의 종류
     * @deviceOsVersion Device의 OS 버전 정보
     * @deviceUid Device 고유 ID.
     * @deviceType 디바이스 종류
     * @deviceModel Device의 Model 정보.
     * @carrierCode 통신사 명칭.
     */
    private JSONObject bizPushRegistDeviceInterface(Context context, String pushKey, String userId, String appName,
                                                    String appVersion, String deviceOsType, String pushProviderType, String deviceOsVersion,
                                                    String deviceUid, String deviceType, String deviceModel, String carrierCode) throws Exception {

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String pushUrl = preferences.getString("push_url", "");

        byte[] postData = null;

        JSONObject RData = new JSONObject();

        RData.put("pushKey", pushKey);
        RData.put("userId", userId);
        RData.put("appName", appName);
        RData.put("appVersion", appVersion);
        RData.put("deviceOsType", deviceOsType);
        RData.put("pushProviderType", pushProviderType);
        RData.put("deviceOsVersion", deviceOsVersion);
        RData.put("deviceUid", deviceUid);
        RData.put("deviceType", deviceType);
        RData.put("deviceModel", deviceModel);
        RData.put("carrierCode", carrierCode);

        postData = RData.toString().getBytes();

        JSONObject responseJSON = new JSONObject();

        if (pushKey == null || userId == null || appName == null || appVersion == null || deviceOsType == null ||
                pushProviderType == null || deviceOsVersion == null || deviceUid == null) {
            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

        } else {
            responseJSON = pushConnect(context, "regist", pushUrl, postData);

            if (responseJSON.getBoolean("result")) {
                editor.putString("push_type", "biz_push");
                editor.commit();
            }
        }

        return responseJSON;
    }

    /**
     * Push 메시지를 수신 여부를 체크하는 메소드.<br>
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
     * @param context    context 객체
     * @param messageID  push message의 고유 id
     * @param userID     user id.
     * @param checkType  수신 및 확인 여부.
     * @param time       확인 시간.
     * @param msgRequest 요청 메세지.
     * @return 푸시 수신 여부 체크 처리 결과 반환
     */
    private JSONObject receivedCheck(Context context, String messageID, String userID, String checkType, String time, String msgRequest) throws Exception {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("userId", userID);
        RData.put("messageId", messageID);

        if (checkType.equals("RECEIVED")) {
            RData.put("arrived", true);
            RData.put("checked", false);
        } else {
            RData.put("arrived", false);
            RData.put("checked", true);
        }

        RData.put("messageRequest", msgRequest);
        RData.put("checkedDate", time);
        RData.put("arrivedDate", time);

        postData = RData.toString().getBytes();

        return pushConnect(context, "check", pushUrl, postData);
    }


    /**
     * 읽지 않은 푸시 메세지 개수를 가져오는 메소드.<br>
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
     * @param context context 객체
     * @param userId  user id.
     * @param appName 앱 이름.
     * @return 처리 결과 및 읽지 않은 푸시 메세지 개수에 대한 정보 반환
     */
    private JSONObject getUnreadPushMessage(Context context, String userId, String appName) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("userId", userId);
        RData.put("appName", appName);

        postData = RData.toString().getBytes();

        if (userId == null || appName == null) {

            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;

        } else {
            return pushConnect(context, "getUnreadPushMessage", pushUrl, postData);
        }

    }

    /**
     * 설정된 푸시 알람을 갖고 오는 메소드.<br>
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
     * @param context context 객체
     * @param pushKey push key.
     * @param userId  user id.
     * @return 처리 결과 및 푸시 알람 수신 정보 반환
     */
    private JSONObject alarmSettingInfo(Context context, String pushKey, String userId) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("pushKey", pushKey);
        RData.put("userId", userId);

        postData = RData.toString().getBytes();

        if (pushKey == null || userId == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;

        } else {
            return pushConnect(context, "alarmSettingInfo", pushUrl, postData);
        }
    }

    /**
     * 푸시 알람을 설정 하는 메소드.<br>
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
     * @param context context 객체
     * @param pushKey push key.
     * @param userId  user id.
     * @param enabled 푸시 알람 허용 여부.
     * @return 처리 결과 및 셋팅된 푸시 알람 수신 정보 반환
     */
    private JSONObject updateAlarmSetting(Context context, String pushKey, String userId, boolean enabled) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("pushKey", pushKey);
        RData.put("userId", userId);
        RData.put("enabled", enabled);

        postData = RData.toString().getBytes();

        if (pushKey == null || userId == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;
        } else {
            return pushConnect(context, "updateAlarmSetting", pushUrl, postData);
        }
    }

    /**
     * Push 메시지 읽음 처리를 하는 메소드.<br>
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
     * @param context context 객체
     * @param trxDay  읽은 날짜.
     * @param userId  user id.
     * @param trxId   푸시 메세지 Id.
     * @return 처리 결과 및 메시지 읽음 처리 정보 반환
     */
    private JSONObject markAsRead(Context context, String trxDay, String userId, String trxId, boolean read) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("trxDay", trxDay);
        RData.put("trxId", trxId);
        RData.put("userId", userId);
        RData.put("read", read);

        postData = RData.toString().getBytes();

        if (trxDay == null || trxId == null || userId == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;
        } else {
            return pushConnect(context, "markAsRead", pushUrl, postData);
        }
    }

    /**
     * 푸시 메세지 목록을 가져오는 메소드.<br>
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
     * @param context   context 객체
     * @param pageIndex 푸시 메세지를 가져올 페이지 번호
     * @param itemCount 해당 페이지에서 가져올 푸시 메세지 개수
     * @param userId    user id
     * @param appName   앱 이름
     * @return 처리 결과 및푸시 메세지 목록 정보 반환
     */
    private JSONObject getMessages(Context context, int pageIndex, int itemCount, String userId, String appName) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("pageIndex", pageIndex);
        RData.put("itemCount", itemCount);
        RData.put("appName", appName);
        RData.put("userId", userId);

        postData = RData.toString().getBytes();

        if (pageIndex == 0 || itemCount == 0 || appName == null || userId == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;
        } else {
            return pushConnect(context, "getMessages", pushUrl, postData);
        }
    }

    /**
     * 푸시 메세지 전송하는 메소드.<br>
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
     * @param context         context 객체
     * @param trxType         메세지 type
     * @param scheduleDate    발송 예약 일시
     * @param appName         앱 이름
     * @param toUsers         푸시 메세지를 보낼 user list
     * @param toGroups        푸시 메세지를 보낼 group list
     * @param toAll           모든 사람에게 보낼지 여부.
     * @param fromUser        보내는 사람 정보.
     * @param messageSubject  메세지 주제
     * @param messageCategory 메세지 category
     * @param messageContent  메세지 내용
     * @param messagePayload  payload 관련 정보.
     * @return 푸시 메세지 전송 처리 정보 반환
     */
    private JSONObject sendMessages(Context context, String trxType, String scheduleDate, String appName,
                                    JSONArray toUsers, JSONArray toGroups, boolean toAll, String fromUser, String messageSubject,
                                    String messageCategory, String messageContent, JSONObject messagePayload) throws JSONException {

        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("trxType", trxType);
        RData.put("scheduleDate", scheduleDate);
        RData.put("appName", appName);
        RData.put("toUsers", toUsers);
        RData.put("toGroups", toGroups);
        RData.put("toAll", toAll);
        RData.put("fromUser", fromUser);
        RData.put("messageSubject", messageSubject);
        RData.put("messageCategory", messageCategory);
        RData.put("messageContent", messageContent);
        RData.put("messagePayload", messagePayload);

        postData = RData.toString().getBytes();

        if (appName == null || messageSubject == null || messageCategory == null || messageContent == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;
        } else {
            return pushConnect(context, "messageSend", pushUrl, postData);
        }
    }

    /**
     * 푸시 메세지의 type list를 받아오는 메소드.<br>
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
     * @param context        context 객체
     * @param agreeFlag      수신 동의 여부.
     * @param deletedFlag    삭제 여부.
     * @param messageTypeIds message type의 id 목록
     * @return 푸시 메세지 type list 정보 반환
     */
    private JSONObject getMessageTypeList(Context context, boolean agreeFlag, boolean deletedFlag, JSONArray messageTypeIds) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("agreeFlag", agreeFlag);
        RData.put("deletedFlag", deletedFlag);
        RData.put("messageTypeIds", messageTypeIds);

        postData = RData.toString().getBytes();

        if (messageTypeIds == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;
        } else {
            return pushConnect(context, "getMessageTypeList", pushUrl, postData);
        }

    }

    /**
     * 푸시 수신 동의 정보를 받아오는 메소드를.<br>
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
     * @param context        context 객체
     * @param userId         user id.
     * @param appName        앱 명칭.
     * @param messageTypeIds message type의 id 목록
     * @return 푸시 메세지 수신 동의 정보 반환
     */
    private JSONObject getAgreementInfo(Context context, String userId, String appName, JSONArray messageTypeIds) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("userId", userId);
        RData.put("appName", appName);
        RData.put("messageTypeIds", messageTypeIds);

        postData = RData.toString().getBytes();

        if (userId == null || appName == null || messageTypeIds == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;
        } else {
            return pushConnect(context, "getAgreementInfo", pushUrl, postData);
        }
    }

    /**
     * 푸시 수신 동의 정보를 업데이트 하는 메소드.<br>
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
     * @param context       context 객체
     * @param userId        user id.
     * @param messageTypeId message type의 개별 id 값.
     * @param agreeFlag     수신 동의 여부.
     * @param appName       앱 명칭.
     * @return 푸시 메세지 수신 동의 업데이트 정보 반환
     */
    private JSONObject updateAgreementInfo(Context context, String userId, String messageTypeId, boolean agreeFlag, String appName) throws JSONException {
        byte[] postData = null;

        JSONObject RData = new JSONObject();

        SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
        String pushUrl = preferences.getString("push_url", "");

        RData.put("userId", userId);
        RData.put("messageTypeId", messageTypeId);
        RData.put("agreeFlag", agreeFlag);
        RData.put("appName", appName);

        postData = RData.toString().getBytes();

        if (userId == null || appName == null || messageTypeId == null) {
            JSONObject responseJSON = new JSONObject();

            responseJSON.put("result", false);
            responseJSON.put("resultCode", "9999");
            responseJSON.put("resultMessage", "유효하지 않은 입력값 입니다.");

            return responseJSON;
        } else {
            return pushConnect(context, "updateAgreementInfo", pushUrl, postData);
        }

    }


    /**
     * 푸시 이벤트 별로 url을 조합해서 실제 서버로 요청을 보낸후 처리 결과를 반환해 주는 메소드.<br>
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
     * @param context  context 객체
     * @param type     푸시 이벤트 종류 (ex. "regist", "check", "getUnreadPushMessage", "updateAlarmSetting", "markAsRead", "alarmSettingInfo", "messageSend", "getMessageTypeList","getAgreementInfo","updateAgreementInfo")
     * @param strUrl   push server url.
     * @param postData 이벤트별 요청 데이터.
     * @return 이벤트 별 처리 결과를 JSONObject로 반환
     */
    private JSONObject pushConnect(Context context, String type, String strUrl, byte[] postData) {
        int responseCode;
        URL url;

        String response = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;

        JSONObject responseJSON = new JSONObject();

        try {
            strUrl = getPushUrl(context);

            int len = strUrl.length();
            int lastIndex = strUrl.lastIndexOf("/");

            if (len != lastIndex + 1) {
                strUrl += "/";
            }

            if (type.equals("regist")) {
                url = new URL(strUrl + "pushkeys");
            } else if (type.equals("check")) {
                url = new URL(strUrl + "messages/updateReceiptNotification");
            } else if (type.equals("getUnreadPushMessage")) {
                url = new URL(strUrl + "messages/getUnreadPushMessagesCount");
            } else if (type.equals("updateAlarmSetting")) {
                url = new URL(strUrl + "pushkeys/updateAlarmSetting");
            } else if (type.equals("markAsRead")) {
                url = new URL(strUrl + "messages/markAsRead");
            } else if (type.equals("alarmSettingInfo")) {
                url = new URL(strUrl + "pushkeys/getAlarmSetting");
            } else if (type.equals("messageSend")) {
                url = new URL(strUrl + "messages/fireMessages");
            } else if (type.equals("getMessageTypeList")) {
                url = new URL(strUrl + "messages/messageType/getMessageTypeList");
            } else if (type.equals("getAgreementInfo")) {
                url = new URL(strUrl + "messages/messageType/getAgreementInfo");
            } else if (type.equals("updateAgreementInfo")) {
                url = new URL(strUrl + "messages/messageType/updateAgreementInfo");
            } else {
                url = new URL(strUrl + "messages/getMessages");
            }

            Logger.i("pushManager", url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));

            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            responseCode = conn.getResponseCode();
            Logger.i("PushManager", "ResponseCode : " + responseCode);

            if (responseCode == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }

                byteData = baos.toByteArray();
                response = new String(byteData);
                responseJSON = new JSONObject(response);

                Logger.i("PushManager", "response data : " + responseJSON.toString());

                return responseJSON;
            } else {
                responseJSON.put("result", false);
                responseJSON.put("resultCode", "9999");
                responseJSON.put("resultMessage", "http response error (" + responseCode + ")");

                Logger.i("PushManager", "response data : " + responseJSON.toString());

                return responseJSON;
            }
        } catch (Exception e) {
            e.printStackTrace();

            try {
                responseJSON.put("result", false);
                responseJSON.put("resultCode", "9999");
                responseJSON.put("resultMessage", e.getClass().getName());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        return responseJSON;
    }

    /**
     * 01.클래스 설명 : Request Data를 key 와 value를 담은 DTO 클래스. <br>
     * 02.제품구분 : bizMOB 3.0 Android Container <br>
     * 03.기능(콤퍼넌트) 명 : DTO <br>
     * 04.관련 API/화면/서비스 : <br>
     * 05.수정이력  <br>
     * <pre>
     * **********************************************************************************************************************************<br>
     *  수정일                                          이름                          변경 내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                                     박재민                         최초 작성<br>
     * **********************************************************************************************************************************<br>
     * </pre>
     *
     * @author 박재민
     * @version 1.0
     */
    private class KeyValueDataDTO {
        private String key;
        private String value;

        public KeyValueDataDTO() {
        }

        private KeyValueDataDTO(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private void setKey(String key) {
            this.key = key;
        }

        private String getValue() {
            return value;
        }

        private void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 01.클래스 설명 : KeyValueDataDTO 를 담는 클래스. <br>
     * 02.제품구분 : bizMOB 3.0 Android Container <br>
     * 03.기능(콤퍼넌트) 명 : DTO <br>
     * 04.관련 API/화면/서비스 : <br>
     * 05.수정이력  <br>
     * <pre>
     * **********************************************************************************************************************************<br>
     *  수정일                                          이름                          변경 내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-22                                     박재민                         최초 작성<br>
     * **********************************************************************************************************************************<br>
     * </pre>
     *
     * @author 박재민
     * @version 1.0
     */
    private class RDataDTO {
        private List<KeyValueDataDTO> keyValueData;

        private RDataDTO() {
            keyValueData = new ArrayList<KeyValueDataDTO>();
        }

        private List<KeyValueDataDTO> getKeyValueData() {
            return keyValueData;
        }

        private void setKeyValueData(List<KeyValueDataDTO> keyValueData) {
            this.keyValueData = keyValueData;
        }

        private void addKeyValueData(KeyValueDataDTO data) {
            keyValueData.add(data);
        }

        private void removeKeyValueData(KeyValueDataDTO data) {
            keyValueData.remove(data);
        }

        private void clearAllKeyValueData() {
            keyValueData.clear();
        }
    }

    /**
     * Push url을 반환해주는 메소드.<br>
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
     * @return Push url
     * @context context 객체
     */
    private String getPushUrl(Context context) {
        if (AppConfig.PUSH_URL.equals("")) {
            AppConfigReader.LoadAppConfig(context, "bizMOB/config/app.config");
            return AppConfig.PUSH_URL;
        }
        return AppConfig.PUSH_URL;
    }
}
