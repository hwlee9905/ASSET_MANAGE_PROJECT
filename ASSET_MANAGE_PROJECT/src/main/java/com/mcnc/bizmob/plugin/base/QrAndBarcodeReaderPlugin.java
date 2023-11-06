package com.mcnc.bizmob.plugin.base;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.view.barcode.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 각종 코드를 읽을수 있는 zxing library를 이용하는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : Code reader <br>
 * 04.관련 API/화면/서비스 : Zxing library <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-28                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class QrAndBarcodeReaderPlugin extends BMCPlugin {

    private String callback = "";

    /**
     * QR and Barcode reader (CaptureActivity) 화면을 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-28                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 콜백 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {
        String type = "1"; //0 = auto, 1 = vertical, 2 = landscape;  (default = 2)
        boolean lockOrientation = true;
        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");
                if (param.has("callback")) {
                    callback = param.getString("callback");

                    if (param.has("type")) {
                        String sType = param.getString("type");
                        if (sType.equals("portrait") || sType.equals("vertical")) { //세로
                            type = "1";
                        } else if (sType.equals("land") || sType.equals("landscape") || sType.equals("horizontal")) { //가로
                            type = "2";
                        } else if (sType.equals("auto")) { //자동
                            type = "0";
                        }
                    }

                    if (type.equals("0")) {
                        lockOrientation = false;
                    }

                    //Qr and Barcode Scan 화면 호출.
                    Intent intent = new Intent(getActivity(), CaptureActivity.class);
                    intent.putExtra(Intents.Scan.ORIENTATION_LOCKED, lockOrientation);
                    intent.putExtra("orientation", type);
                    startActivityForResultFromPlugin(intent, IntentIntegrator.REQUEST_CODE);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * CaptureActivity 실행 후 코드 리딩 결과 값을 리턴 받는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-28                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param requestCode 요청 코드
     * @param resultCode  응답 코드
     * @param data        code reading 결과
     *                    <p>
     *                    Return : (JSONObject) 코드 촬영 여부, 코드 reading 값.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        JSONObject callbackResult = new JSONObject();
        boolean bResult = true;
        String readData = "";
        if (result != null) {
            if (resultCode == Activity.RESULT_CANCELED) {
                try {
                    callbackResult.put("result", true);
                    callbackResult.put("resultCode", "CNL0001");
                    callbackResult.put("resultMessage", "QR code reader closed");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                listener.resultCallback("callback", callback, callbackResult);
            } else if (requestCode == IntentIntegrator.REQUEST_CODE) {

                if (result.getContents() != null) {
                    readData = result.getContents();
                }

                try {
                    callbackResult.put("result", bResult);
                    callbackResult.put("resultCode", "");
                    callbackResult.put("resultMessage", "Barcode scanned");
                    callbackResult.put("data", readData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                listener.resultCallback("callback", callback, callbackResult);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
