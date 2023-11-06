package com.mcnc.bizmob.plugin.base;

import com.mcnc.bizmob.core.plugin.BMCPlugin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : FStorage, MStorage 초기화 기능을 수행하는 메소드 호출 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : FStorage, MStorage 초기화 기능을 수행하는 메소드 호출 <br>
 * 04.관련 API/화면/서비스 : <br>
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
public class StoragePlugin extends BMCPlugin {

    @Override
    public void executeWithParam(JSONObject json) {

        try {

            String commandID = json.getString("id");

            if (commandID.equals("CLEAR_FSTORAGE")) {
                getManager().clearFStorage();
            } else if (commandID.equals("CLEAR_MSTORAGE")) {
                getManager().clearMStorage();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
