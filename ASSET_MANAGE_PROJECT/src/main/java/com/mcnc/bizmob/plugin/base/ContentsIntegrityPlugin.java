package com.mcnc.bizmob.plugin.base;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.db.SqliteInterface;
import com.mcnc.bizmob.core.util.file.FileUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * 01.클래스 설명 : 컨텐츠 위변조 체크 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 컨텐츠 위변조 체크. <br>
 * 04.관련 API/화면/서비스 : SqliteInterface, FileUtil, BMCPlugin <br>
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
public class ContentsIntegrityPlugin extends BMCPlugin {

    /**
     * Class 명
     */
    private static final String TAG = "ContentsIntegrityPlugin";

    /**
     * table 명
     */
    public static final String tableName = "Integrity";

    /**
     * default type 정보.
     */
    private String type = "new";// new or updateQuery

    /**
     * error 메세지
     */
    private String errorMsg = "";

    /**
     * callback 명.
     */
    private String callback = "";

    /**
     * 위변조 체크 성공 여부.
     */
    boolean mIsSuccess = true;

    ProgressDialog progressDialog;


    /**
     * 클라이언트로 부터 전달 받은 데이터를 가공하여, 컨텐츠 위변조 체크 기능을 수행하는 메소드를 호출 하는 메소드.<br>
     * <p/>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param data 클라이언트로 부터 전달 받은 데이터, 컨텐츠 위변조 체크 기능 수행에 필요한 값 보유 <br><br>
     */
    @Override
    protected void executeWithParam(JSONObject data) {


        try {
            if (data.has("param")) {
                JSONObject jsonObj = data.getJSONObject("param");

                try {
                    if (jsonObj.has("type")) {
                        type = jsonObj.getString("type");
                    }

                    progressDialog = (ProgressDialog) getProgressDialog();
//                    if (!type.equals("check")) {
                    if (getActivity() != null && progressDialog != null) {
                        if (progressDialog.isShowing()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog != null) {
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null) {
                                    progressDialog.setMessage(getActivity().getString(RUtil.getStringR(getActivity(), "txt_contents_faking_checking")));
                                    progressDialog.show();
                                }
                            }
                        });
                    }

                    if (jsonObj.has("callback")) {
                        callback = jsonObj.getString("callback");
                    }

                    // contents folder 로 수정해야함
                    String contentsPath = FileUtil.getAbsolutePath("contents", "");
                    ArrayList<File> files = new ArrayList<File>();
                    getContentList(contentsPath, files);

                    if (files.size() <= 0) {
                        // error
                        //return;
                        mIsSuccess = false;
                        errorMsg = "contents list up error ";
                    }
                    if (type.equals("new")) {
                        // db 파일 존재시 삭제.
                        File integrityFile = new File(FileUtil.getAbsolutePath(
                                "internal", "contentsList"));

                        if (integrityFile.exists()) {
                            integrityFile.delete();
                        }
                        if (SqliteInterface.getInstance().openDatabase("contentsList")) {
                            String res = "";
                            String query = "create table if not exists "
                                    + tableName
                                    + "( fileName text PRIMARY KEY NOT NULL, lastDate text, uniqueValue text, fileSize long )";
                            res = SqliteInterface.getInstance().executeSql(query);
                            Logger.d(TAG, "create db result :" + res);
                            // insert data
                            SimpleDateFormat lastModDate = new SimpleDateFormat(
                                    "MM/dd/yyyy HH:mm:ss");
                            JSONArray bindArray = new JSONArray();
                            for (File file : files) {
                                JSONArray list = new JSONArray();
                                list.put(file.getAbsolutePath());
                                list.put(lastModDate.format(file.lastModified()));
                                list.put(getMD5(file));
                                list.put(file.length());
                                bindArray.put(list);
                            }
                            String insertQuery = "insert into "
                                    + tableName
                                    + " (fileName, lastDate, uniqueValue, fileSize) values (?, ?, ?, ?)";

                            String data1 = SqliteInterface.getInstance()
                                    .executeBatchInsert(insertQuery, bindArray);
                            Logger.d(TAG, "insert db result :" + data1);
                            SqliteInterface.getInstance().closeDatabase();
                        } else {
                            // error
                            mIsSuccess = false;
                            errorMsg = "new error";
                        }
                    } else if (type.equals("update")) {
                        if (SqliteInterface.getInstance().openDatabase("contentsList")) {
                            SQLiteDatabase database = SqliteInterface.getInstance()
                                    .getDatabaseHandle();
                            SimpleDateFormat lastModDate = new SimpleDateFormat(
                                    "MM/dd/yyyy HH:mm:ss");
//                            JSONArray updateArrays = new JSONArray();
//                            JSONArray insertArrays = new JSONArray();

                            for (File file : files) {
                                try {
                                    String fileName = file.getAbsolutePath().trim();
                                    String fromFileMd5 = getMD5(file);


                                    String selectQuery = "select * from " + tableName
                                            + " where fileName = '" + fileName + "';";
                                    Cursor selectCur = database.rawQuery(selectQuery,
                                            null);
                                    selectCur.moveToFirst();
                                    //Logger.d(TAG, "type update: " + selectQuery);


                                    if (selectCur.getCount() > 0) {
                                        // exist
                                        String pKey = selectCur.getString(selectCur.getColumnIndex("fileName"));
                                        String saveMd5 = selectCur.getString(selectCur.getColumnIndex("uniqueValue"));

                                        if (!saveMd5.equals(fromFileMd5)) {

//                                            JSONArray array = new JSONArray();
//
//                                            array.put((lastModDate.format(file.lastModified())).toString());
//                                            array.put(fromFileMd5);
//                                            array.put(String.valueOf(file.length()));
//                                            array.put(pKey);
//                                            updateArrays.put(array);
                                            // 같은 파일이 없는 경우 db 업데이트.
                                            String updateQuery = "update "
                                                    + tableName
                                                    + " set lastDate = '"
                                                    + lastModDate.format(file
                                                    .lastModified()) + "'"
                                                    + ",uniqueValue = '" + fromFileMd5
                                                    + "',fileSize = '" + file.length()
                                                    + "' where fileName = '" + pKey + "'";
                                            database.execSQL(updateQuery);


                                        } else {
                                            // MD5 값이 동일한 파일인 경우 DB에 쓰지 않음. (skip)
                                            continue;
                                        }
                                    } else {
                                        // not exist!
                                        // insert
//                                        JSONArray array = new JSONArray();
//                                        array.put(fileName);
//                                        array.put((lastModDate.format(file
//                                                .lastModified())).toString());
//                                        array.put(fromFileMd5);
//                                        array.put(String.valueOf(file.length()));
//                                        insertArrays.put(array);

                                        String insertQuery = "insert into "
                                                + tableName
                                                + " "
                                                + "(fileName, lastDate, uniqueValue, fileSize) "
                                                + "values ('"
                                                + fileName
                                                + "', '"
                                                + lastModDate.format(file
                                                .lastModified()) + "', '"
                                                + fromFileMd5 + "', '" + file.length()
                                                + "')";
                                        database.execSQL(insertQuery);
                                        Logger.d(TAG, "type update : " + insertQuery);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }

//                            String updateQuery = "update " + tableName + " set lastDate = ?, uniqueValue = ?, fileSize = ? where fileName = ?";
//                            String insertQuery = "insert into " + tableName + " (fileName, lastDate, uniqueValue, fileSize) values (?, ?, ?, ?)";
//
//                            if (insertArrays.length() > 0) {
//                                Logger.d(TAG, "insert db data :" + insertArrays.toString());
//                                String insertResult = SqliteInterface.getInstance()
//                                        .executeBatchInsert(insertQuery, insertArrays);
//                                Logger.d(TAG, "insert db result :" + insertResult);
//                                SqliteInterface.getInstance().closeDatabase();
//                            }
//
//
//                            if (updateArrays.length() > 0) {
//                                Logger.d(TAG, "update db data :" + updateArrays.toString());
//                                String updateResult = SqliteInterface.getInstance()
//                                        .executeBatchSql(updateQuery, updateArrays);
//                                Logger.d(TAG, "update db result :" + updateResult);
//                                SqliteInterface.getInstance().closeDatabase();
//                            }

                            database.close();
                            mIsSuccess = IntegrityCheck(files);
                            Logger.d(TAG, " content Integrity check success : " + mIsSuccess);
                        } else {
                            // error
                            mIsSuccess = false;
                            errorMsg = "update error";
                        }
                    } else if (type.equals("check")) {
                        mIsSuccess = IntegrityCheck(files);
                        Logger.d(TAG, " content Integrity check success : " + mIsSuccess);
                    } else {
                        mIsSuccess = false;
                        errorMsg = "undefine type : new , update , check";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jResult = new JSONObject();
                jResult.put("result", mIsSuccess);
                jResult.put("error_message", errorMsg);
                listener.resultCallback("callback", callback, jResult);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                });
            }

        }
    }

    /**
     * 켄텐츠 위변조 체크 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param files 컨텐츠 파일 정보.
     * @return 위변조 체크 통과 여부. (false, 변조됨, true,  변조 되지 않음.)
     */
    private boolean IntegrityCheck(final ArrayList<File> files) {
        boolean bSuccess = true;

        if (files.size() <= 0) {
            Logger.d(TAG, "file info is null");
            return false;
        }
        if (SqliteInterface.getInstance().openDatabase("contentsList")) {
            SQLiteDatabase database = SqliteInterface.getInstance().getDatabaseHandle();
            String sql = "select * from " + tableName + ";";
            Cursor results = database.rawQuery(sql, null);
            results.moveToFirst();
            while (!results.isAfterLast()) {
                String pKey = results.getString(results.getColumnIndex("fileName"));
                pKey.trim();
                String saveMd5 = results.getString(results.getColumnIndex("uniqueValue"));
                saveMd5.trim();

                if (!bSuccess) {
                    break;
                }
                for (File file : files) {
                    String fileName = file.getAbsolutePath().trim();
                    if (pKey.equals(fileName)) {
                        // 검증 실패
                        if (!saveMd5.equals(getMD5(file))) {
                            Logger.d(TAG, "Integrity Fail : " + pKey + " is not mathing MD5!! from file");
                            bSuccess = false;
                            break;
                        }
                        break;
                    }
                }

                results.moveToNext();
            }
            database.close();
        } else {
            bSuccess = false;
        }
        return bSuccess;
    }

    /**
     * 켄텐츠 폴더의 파일 리스트 정보를 얻어오는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param directoryName 폴더 명,
     * @param files         파일 정보를 담을 arrayList.
     */
    private void getContentList(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            int dot = file.getName().lastIndexOf(".");
            String ext = file.getName().substring(dot + 1);

            if (file.isFile() && (ext.equals("js") || ext.equals("html"))) {
                files.add(file);
            } else if (file.isDirectory()) {
                getContentList(file.getAbsolutePath(), files);
            }
        }
    }

    /**
     * 파일에 대한 MD5 값을 추출하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param file MD5 파일을 추출할 파일.
     * @return 추출된 MD5 값.
     */
    private static String getMD5(File file) {
        String ret = null;
        try {
            MessageDigest digest1 = MessageDigest.getInstance("MD5");

            FileInputStream in = new FileInputStream(file);
            byte[] buf = new byte[1024 * 8];

            while (true) {
                int readed = in.read(buf);
                if (readed == -1)
                    break;
                digest1.update(buf, 0, readed);

            }
            in.close();

            byte key[] = digest1.digest();
            ret = byteToString(key);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    /**
     * byte[] 값을 String으로 변환 하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-05                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param bytes String으로 변환할 byte[] 값.
     * @return 변환된 String 값.
     */
    private static String byteToString(byte[] bytes) {
        int v;
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < bytes.length; i++) {
            v = (int) bytes[i];
            v = v < 0 ? v + 0x100 : v;

            if (v <= 0xf)
                sb.append('0');
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

}
