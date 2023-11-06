package com.mcnc.bizmob.plugin.base;

import android.content.Intent;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.view.image.CustomGalleryActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 안드로이드 기능본 갤러리 기능 외에 사진을 여러장 선택하기 위한 커스텀 갤러리 호출 기능을 수행하는 플러그인. <br>
 * 02.제품구분 : bizMob 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 :멀티 셀렉트 갤러리 호출 <br>
 * 04.관련 API/화면/서비스 : CustomGalleryActivity  <br>
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

public class CustomGalleryPlugin extends BMCPlugin {

    /**
     * Request Code
     */
    public static final int CUSTOM_GALLERY_CODE = 2222;
    /**
     * callback 줄 function 명
     */
    private String callback = "callback";


    /**
     * 클라이언트로 부터 전달 받은 데이터를 가공하여 CustomGallreyActivity (사진을 여러장 선택 가능한 커스텀 갤러리)를 실행함.<br>
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
     * @param data 클라이언트로 부터 전달 받은 데이터, 갤러리 멀티 초이스에 대한 정보 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {

        try {

            JSONArray typeList = new JSONArray();
            int maxCount = 1;
            boolean include_index = false;
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");
                typeList = param.getJSONArray("type_list");
                callback = param.getString("callback");
                if (param.has("max_count")) {
                    maxCount = param.getInt("max_count");
                }
            }

            Intent intent = new Intent(getActivity(), CustomGalleryActivity.class);
            intent.putExtra("typeList", typeList.toString());
            intent.putExtra("max_count", maxCount);
            startActivityForResultFromPlugin(intent, CUSTOM_GALLERY_CODE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * CustomGallreyActivity 실행 후 결과 값을 리턴 받는 메소드.<br>
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
     * @param reqCode 요청 코드
     * @param resCode 응답 코드
     * @param intent  리턴 결과 data <br> <br>
     *                <p>
     *                Return : (JSONObject) 선택 여부, 선택된 사진이 파일이 저장된 경로 리스트 반환
     */
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);

        if (reqCode == CUSTOM_GALLERY_CODE) {
            JSONObject jsonObject = new JSONObject();

            try {

                jsonObject.put("result", intent.getBooleanExtra("result", false));
                jsonObject.put("images", new JSONArray(intent.getStringExtra("images")));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            listener.resultCallback("callback", callback, jsonObject);
        }
    }


}
