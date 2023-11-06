package com.mcnc.bizmob.plugin.base;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.mcnc.bizmob.core.plugin.BMCPlugin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 01.클래스 설명 : 파일 브라우져를 통해 선택된 파일 경로를 받을수 있는 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 파일 브라우저 호출. <br>
 * 04.관련 API/화면/서비스 : FileBrowserActivity <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-01                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class FileBrowserPlugin extends BMCPlugin {

    /**
     * callback 명.
     */
    private String callback = "";

    public static int REQ_CODE_FILE_BROWSER = 8211;

    /**
     * 클라이언트 부터 전달 받은 데이터를 가공하여 파일 브라우저 화면을 호출하는 기능을 실행함.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-01                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 파일 브라우저를 이용후 선택된 파일의 path를 전달받을 callback 값 보유. <br>
     */
    @Override
    public void executeWithParam(JSONObject data) {

        try {
            if (data.has("param")) {
                JSONObject param = data.getJSONObject("param");
                if (param.has("callback")) {
                    callback = param.getString("callback");
                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResultFromPlugin(intent, REQ_CODE_FILE_BROWSER);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 파일 브라우저 실행 후 선택된 파일 결과를 리턴 받고 결과값을 웹으로 올려주는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-01                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param reqCode 요청코드 {@code FileBrowserPlugin.REQ_CODE_FILE_BROWSER} 파일브라우저 요청코드,
     * @param resCode 응답코드 {@code Activity.RESULT_OK} 응답코드 값이 참,
     * @param intent  응답과 함께 전달된 데이터
     */
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        JSONObject jResult = new JSONObject();
        boolean bResult = true;
        String filePath = "";
        String errorMessage = "";
        if (reqCode == REQ_CODE_FILE_BROWSER && resCode == Activity.RESULT_OK) {
            bResult = true;
            Uri uri = intent.getData();
            filePath = getRealPathFromURI(uri);
        } else if (reqCode == REQ_CODE_FILE_BROWSER && reqCode == Activity.RESULT_CANCELED) {
        }

        try {
            jResult.put("result", bResult);
            jResult.put("error_message", errorMessage);
            jResult.put("file_path", filePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listener.resultCallback("callback", callback, jResult);
    }

    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = {MediaStore.Files.FileColumns.DATA};
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return "";
    }
}
