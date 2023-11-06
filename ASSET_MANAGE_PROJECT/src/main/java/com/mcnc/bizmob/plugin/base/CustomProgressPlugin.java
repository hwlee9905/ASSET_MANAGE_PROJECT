package com.mcnc.bizmob.plugin.base;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCFragmentActivity;
import com.mcnc.bizmob.progress.CustomProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 커스텀 프로그레스 다이얼로그를 호출 하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 커스텀 프로그레스 다이얼로그 생성, 닫기 <br>
 * 04.관련 API/화면/서비스 :  CustomProgressDialog<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-5                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */

public class CustomProgressPlugin extends BMCPlugin {

    /**
     * id 값의 key값
     */
    private final String COMMAND_ID = "id";

    /**
     * callback 줄 function 명
     */
    String callback = "";

    String imgPath = "";

    /**
     * CustomProgressDialog
     */
//    public static CustomProgressDialog customProgressDialog;

    /**
     * 클라이언트로 부터 전달 받은 데이터를 가공하여 커스텀 프로그레스 다이얼로그를 생성하는 메소드를 호출 하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param json 클라이언트로 부터 전달 받은 데이터, 프로그레스 생성, 닫기, 업데이트에 필요한 값 보유(ex. 제목, 메세지, 콜백, 진행률 등) <br><br>
     *             <p>
     *             Return : (JSONObject) 수행 성공여부, 요청 data 원형 반환
     */
    @Override
    public void executeWithParam(JSONObject json) {

        JSONObject param = new JSONObject();

        try {
            String commandID = json.getString(COMMAND_ID);
            String title = "";
            String message = "";

            param = json.getJSONObject("param");

            if (param.has("path")) { //커스텀 프로그레스 이미지 path
                imgPath = param.getString("path");
            }
            if (param.has("title")) {
                title = param.getString("title");
            }
            if (param.has("message")) {
                message = param.getString("message");
            }
            if (param.has("callback")) {
                callback = param.getString("callback");
            }
            if (commandID.equals("SHOW_CUSTOM_PROGRESS")) {

                showProgress(imgPath, title, message);
            } else if (commandID.equals("DISMISS_CUSTOM_PROGRESS")) {

                dismissProgress();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject resData = new JSONObject();
        try {
            resData.put("result", true);
            resData.put("data", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.resultCallback("callback", callback, resData);
        }
    }

    /**
     * 커스텀 프로그레스를 보여주는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param path    커스텀 프로그레스의 이미지 path.
     * @param title   제목.
     * @param message 컨텐츠.
     */
    private void showProgress(final String path, final String title, final String message) throws Exception {

        handler.post(new Runnable() {

                         @Override
                         public void run() {

                             setCustomProgressDialog();
                             CustomProgressDialog customProgressDialog = (CustomProgressDialog) getProgressDialog();
                             if (customProgressDialog != null) {
                                 customProgressDialog.setTitle(title);
                                 customProgressDialog.setMessage(message);
                                 customProgressDialog.show();
                             }

                         }
                     }

        );
    }

    /**
     * 커스텀 프로그레스를 생성하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     */
    private void setCustomProgressDialog() {
        if (imgPath != null) {
            if (getProgressDialog() == null || !(getProgressDialog() instanceof CustomProgressDialog)) {
                if (getActivity() != null) {
                    ((BMCFragmentActivity) getActivity()).setProgressDialog(new CustomProgressDialog(getActivity(), RUtil.getR(getActivity(), "style", "CustomProgressDialog"), imgPath));
                    if (getProgressDialog() != null) {
                        getProgressDialog().setCancelable(false);
                    }
                }
            }
        } else {
            if (getProgressDialog() == null || !(getProgressDialog() instanceof CustomProgressDialog)) {
                if (getActivity() != null) {
                    ((BMCFragmentActivity) getActivity()).setProgressDialog(new CustomProgressDialog(getActivity(), RUtil.getR(getActivity(), "style", "CustomProgressDialog"), imgPath));
                    if (getProgressDialog() != null) {
                        getProgressDialog().setCancelable(false);
                    }
                }
            }
        }
    }


    /**
     * 요청 타입에 따른 커스텀 프로그레스를 닫는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                         박재민                             최초 작성<br>
     *
     * </pre>
     */
    private void dismissProgress() throws Exception {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (getProgressDialog() != null)
                    getProgressDialog().dismiss();
            }
        });
    }


}
