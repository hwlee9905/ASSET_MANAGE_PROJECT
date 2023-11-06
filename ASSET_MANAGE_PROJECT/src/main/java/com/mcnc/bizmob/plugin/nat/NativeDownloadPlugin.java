package com.mcnc.bizmob.plugin.nat;

import android.app.Activity;
import android.app.ProgressDialog;

import com.mcnc.bizmob.core.download.AbstractDownloadService;
import com.mcnc.bizmob.core.download.DownloadServiceImpl;
import com.mcnc.bizmob.core.download.IDownload;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.BMCUtil;
import com.mcnc.bizmob.core.util.file.FileUtil;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.view.LauncherFragment;
import com.mcnc.bizmob.view.ntv.NativeLauncherFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 01.클래스 설명 : 파일 다운로드 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 파일 다운로드 <br>
 * 04.관련 API/화면/서비스 : AbstractDownloadService, DownloadServiceImpl, IDownload, BMCUtil, FileUtil, Logger <br>
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
public class NativeDownloadPlugin extends BMCPlugin {

    /**
     * Class 명
     */
    private final String TAG = this.toString();

    /**
     * callback data의 key 값
     */
    final String CALLBACK = "callback";

    /**
     * progress data의 key 값
     */
    final String PROGRESS = "progress";

    /**
     * provider data의 key 값
     */
    final String PROVIDER = "provider";

    /**
     * native data의 key 값
     */
    final String NATIVE = "native";

    /**
     * type data의 key 값
     */
    final String TYPE = "type";

    /**
     * uri_list data의 key 값
     */
    final String URI_LIST = "uri_list";

    /**
     * uri data의 key 값
     */
    final String URI = "uri";

    /**
     * target_path data의 key 값
     */
    final String TARGET_PATH = "target_path";

    /**
     * target_path_type data의 key 값
     */
    final String TARGET_PATH_TYPE = "target_path_type";

    /**
     * overwrite data의 key 값
     */
    final String OVERWRITE = "overwrite";

    /**
     * file_id data의 key 값
     */
    final String FILE_ID = "file_id";

    /**
     * result data의 key 값
     */
    final String RESULT = "result";

    /**
     * file_path data의 key 값
     */
    final String FILE_PATH = "file_path";

    /**
     * current_count data의 key 값
     */
    final String CURRENT_COUNT = "current_count";

    /**
     * total_count data의 key 값
     */
    final String TOTAL_COUNT = "total_count";

    /**
     * error_text data의 key 값
     */
    final String ERROR_TEXT = "error_text";

    /**
     * error_code data의 key 값
     */
    final String ERROR_CODE = "error_code";

    /**
     * 파일다운로드 버퍼의 사이즈 값
     */
    private final int BUFFER_SIZE = 8192;

    /**
     * 파일다운로드용 byte array
     */
    byte[] buffer = new byte[BUFFER_SIZE];

    /**
     * 실제 플러그인 수행에 필요한 data를 담고 있는 JSONObject
     */
    JSONObject param;

    /**
     * 다운로드 된 파일
     */
    File file;

    /**
     * 다운로드 된 파일의 경로
     */
    String return_download_path = "";

    /**
     * callback 줄 function 명
     */
    String resultCallback = "";

    /**
     * callback 반환값 유형
     */
    String callbackType = "each_list";

    /**
     * 프로그레스바 title에 표시될 문자열
     */
    String progressTitle = "";

    /**
     * 프로그레스바 Content에 표시될 문자열
     */
    String progressMsg = "";

    /**
     * 프로그레스바 표시 여부
     */
    boolean progressbar_visible = false;

    /**
     * 프로그레스바 진행률
     */
    int progress = 0;

    /**
     * 다운로드 된 파일 리스트
     */
    JSONArray fullList = new JSONArray();


    /**
     * 클라이언트로 부터 전달 받은 데이터를 가공하여 파일 다운로드 기능을 실행함.<br>
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
     * @param json 클라이언트로 부터 전달 받은 데이터, 파일 다운로드에 필요한 URI list, 프로그레스 표시 여부, 리턴 유형, 프로그레스 제목, 컨텐츠 문자열등에 대한 정보 값 보유 <br><br>
     *             <p>
     *             Return : (JSONObject) 다운로드 성공여부, 다운로드된 파일 경로 리스트 반환
     */
    @Override
    protected void executeWithParam(JSONObject json) {

        JSONObject progress = null;
        JSONArray uri_list = null;

        long start = 0;

        try {
            param = json.getJSONObject("param");
            if (param.has(CALLBACK)) {
                resultCallback = param.getString(CALLBACK);
            }
            if (param.has(TYPE)) {
                callbackType = param.getString(TYPE);
            }
            if (param.has(PROGRESS)) {
                progress = param.getJSONObject(PROGRESS);
            }
            if (param.has(URI_LIST)) {
                uri_list = param.getJSONArray(URI_LIST);
                Logger.d(TAG, "uri_list = " + uri_list.length());
            }

            // progressbar 생성 판단.
            if (progress != null) {
                if (progress.has(PROVIDER)) {
                    String provider = progress.getString(PROVIDER);
                    if (provider.equals(NATIVE)) {
                        progressbar_visible = true;
                        start = System.currentTimeMillis();
                    }
                }
                if (progress.has("title")) {
                    progressTitle = progress.getString("title");
                } else {
                    progressTitle = getActivity().getString(RUtil.getStringR(getActivity(), "txt_download_title"));
                }

                if (progress.has("message")) {
                    progressMsg = progress.getString("message");
                } else {
                    progressMsg = getActivity().getString(RUtil.getStringR(getActivity(), "txt_downloading"));
                }
            }


            // progressbar 생성
            showProgress();

            if (uri_list != null && uri_list.length() > 0) {

                JSONObject item = null;
                boolean overwrite = false;
                String uri = "";
                String target_path = "";
                String target_path_type = "";
                String fullPath = "";
                int file_id = -1;
                int length = uri_list.length();
                for (int i = 0; length > i; i++) {

                    int index = i + 1;

                    item = uri_list.getJSONObject(i);
                    uri = item.getString(URI);
                    file_id = item.getInt(FILE_ID);
                    overwrite = item.getBoolean(OVERWRITE);
                    target_path = item.getString(TARGET_PATH);
                    target_path_type = item.getString(TARGET_PATH_TYPE);
                    fullPath = FileUtil.getAbsolutePath(target_path_type, target_path);
                    if (!overwrite) {
                        // 파일이 존재 하면 그냥 리턴해줌
                        File f = new File(fullPath);
                        if (f.exists()) {
                            JSONObject root = new JSONObject();
                            root.put(RESULT, true);
                            root.put(FILE_PATH, fullPath);
                            root.put(FILE_ID, file_id);
                            // 파일이 모두 존재 할 경우 너무 빨리 끝날 수 있으므로 약간의 딜레이 추가
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
//							sendCallback (callback, root, activity, method );

                            if (callbackType.equals("each_list")) {

//								root = BMCUtil.resultMessageParser(true, root, "");
                                listener.resultCallback("callback", resultCallback, root);
                            } else {
                                fullList.put(root);
                            }
                            continue;
                        }
                    }

                    if (uri.startsWith("https://") || uri.startsWith("http://")) {
                        // 외부 서버  full url
                        JSONObject root = externalDownload(getActivity(), uri, fullPath);
                        root.put(FILE_ID, file_id);
                        root.put(TOTAL_COUNT, length);
                        root.put(CURRENT_COUNT, index);

                        if (callbackType.equals("each_list")) {
                            listener.resultCallback("callback", resultCallback, root);
                        } else {
                            fullList.put(root);
                        }

//						sendCallback (callback, root, activity, method );
                    } else if (uri.startsWith("ftp://") || uri.startsWith("svn://")) {
                        JSONObject root = new JSONObject();
                        root.put(RESULT, false);
                        root.put(ERROR_CODE, "WE0001");
                        root.put(ERROR_TEXT, "not support protocal");
                        root.put(FILE_ID, file_id);
                        root.put(TOTAL_COUNT, length);
                        root.put(CURRENT_COUNT, index);

                        if (callbackType.equals("each_list")) {
                            dismissProgress();
                            listener.resultCallback("callback", resultCallback, root);
                        } else {
                            fullList.put(root);
                        }
//						sendCallback (callback, root, activity, method );
                    } else {

                        JSONObject root = internalDownload(getActivity(), uri, fullPath, false, file_id, length, index);

                        if (callbackType.equals("each_list")) {
                            listener.resultCallback("callback", resultCallback, root);
                        } else {
                            fullList.put(root);
                        }
                    }
                }

                dismissProgress();

                if (callbackType.equals("full_list")) {

                    JSONObject root = new JSONObject();
                    root.put("list", fullList);

//					root = BMCUtil.resultMessageParser(true, root, "");
                    listener.resultCallback("callback", resultCallback, root);
                }

            } else {

                if (progressbar_visible) {
                    long end = System.currentTimeMillis();
                    long processTime = end - start;
                    if (processTime < 200 && processTime > 0) {
                        try {
                            Thread.sleep(200 - processTime);
                        } catch (InterruptedException e) {
                        }
                    }
                    dismissProgress();

                }

                JSONObject root = new JSONObject();
                root.put(RESULT, false);
                root.put(ERROR_CODE, "WE0002");
                root.put(ERROR_TEXT, "check list length");
                root.put(TOTAL_COUNT, -1);
                root.put(CURRENT_COUNT, -1);

//				root = BMCUtil.resultMessageParser(false, root, "check list length");
                listener.resultCallback("callback", resultCallback, root);
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (progressbar_visible) {
                long end = System.currentTimeMillis();
                long processTime = end - start;
                if (processTime < 200 && processTime > 0) {
                    try {
                        Thread.sleep(200 - processTime);
                    } catch (InterruptedException e1) {
                    }
                }
                dismissProgress();

            }

            try {
                JSONObject data = BMCUtil.exceptionMessageParser(param, e.getMessage());
                listener.resultCallback("callback", resultCallback, data);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } finally {

            if (progressbar_visible) {
                long end = System.currentTimeMillis();
                long processTime = end - start;
                if (processTime < 200 && processTime > 0) {
                    try {
                        Thread.sleep(200 - processTime);
                    } catch (InterruptedException e1) {
                    }
                }
                dismissProgress();

            }
        }
    }

    /**
     * http:// 또는 https:// 를 제외한  url로 파일을 다운로드 할 경우 사용되는 메소드로.<br>
     * 앱에 셋팅된 기본 서버 정보를 root Uri로 이용한다.
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
     * @param activity              사용되지 않음
     * @param uri                   다운받을 파일의 uri
     * @param target_path           파일을 저장할 경로
     * @param isReplaceDownloadFile 다운로드 파일을 대체 할지 여부
     * @param file_id               파일의 고유 id
     * @param length                파일 사이즈
     * @param index                 다운로드 회차
     * @return 파일다운로드 정보를 담은  JSONObject
     */
    private JSONObject internalDownload(Activity activity, String uri, String target_path, final boolean isReplaceDownloadFile,
                                        int file_id, int length, int index) {

        RandomAccessFile fos = null;
        BufferedInputStream bis = null;
        JSONObject jsonData = null;

        String tempFileName = "";
        String destFileName = "";
        int nDownloadMode = 1;        // 1 : 신규, 3.이어받기
        boolean bSuccess = false;    // 다운로드 성공 유무

        JSONObject root = new JSONObject();

        String org_download_path = AbstractDownloadService.getDownloadPath();
        try {
            // 디렉토리 생성
            String download_path = makeDir(target_path);
            // 파일 이름 추출
            int idx = target_path.lastIndexOf("/");
            idx = idx > 0 ? idx + 1 : 0;
            String file_name = target_path.substring(idx);

            Logger.i(TAG, "uri:" + uri);
            AbstractDownloadService.setDownloadPath(download_path);

            if (param.has("mode")) {
                nDownloadMode = param.getInt("mode");
            }

            File downloadDest = new File(download_path);
            if (!downloadDest.exists()) {
                downloadDest.mkdirs();
            }

            JSONObject requestObj = new JSONObject();
            JSONObject param = new JSONObject();
            requestObj.put("param", param);
            param.put("display_name", file_name);
            param.put("uri", uri);
            param.put("method", "direct");

            final JSONObject request = requestObj;

            String fileName = null;
            if (!isReplaceDownloadFile) {
                // FIXME 파일확장자가 이상해지는 경우가 있음 임시로 중복허용막음
                File temp = new File(downloadDest, param.getString("display_name"));
                if (temp.exists()) {
                    temp.delete();
                }
                if (nDownloadMode == 3) {
                    fileName = (new File(downloadDest, "temp_" + param.getString("display_name"))).getAbsolutePath();
                    tempFileName = fileName;
                    destFileName = (new File(downloadDest, param.getString("display_name"))).getAbsolutePath();
                } else {
                    fileName = (new File(downloadDest, param.getString("display_name"))).getAbsolutePath();
                }
            } else {
                // 삭제후 다시 받음.
                File temp = new File(downloadDest, param.getString("display_name"));
                if (temp.exists()) {
                    temp.delete();
                }
                fileName = (new File(downloadDest, param.getString("display_name"))).getAbsolutePath();
            }

            file = new File(target_path);
            if (!file.exists()) {
                file.createNewFile();

                Logger.i("file download", file.getAbsolutePath());
            }

            IDownload downloadService = new DownloadServiceImpl();
            HttpResponse res = downloadService.syncDownload(request, httpResponseHandler, isReplaceDownloadFile);


            fos = new RandomAccessFile(file.getAbsolutePath(), "rw");
            final long offset = fos.length();
            fos.seek(offset);


            bis = new BufferedInputStream(res.getEntity().getContent(), 8192);

            if (res.containsHeader("error_code")) {
                throw new Exception(URLDecoder.decode(res
                        .getFirstHeader("info_text").getValue(), "utf-8"));
            }
            if (res.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Http Error " + res.getStatusLine().getStatusCode()
                        + " " + res.getStatusLine().getReasonPhrase());
            }
            final int fileSize = Integer.parseInt(res.getFirstHeader("file_size").getValue());
            Logger.i(TAG, "file_size" + fileSize);

            int len = 0;
            int totalSize = 0;
            int loopCnt = 0; // loopCount
            double skipCnt = Math.ceil((fileSize / 100) / (double) BUFFER_SIZE);
            Logger.i(TAG, "skip_cnt" + skipCnt);

            totalSize = (int) offset;

            if (fileSize == 0)
                throw new Exception("file size is 0");

            while ((len = bis.read(buffer)) != -1) {

                fos.write(buffer, 0, len);
                totalSize = totalSize + len;

                float viewTotalSize = totalSize;

                if (progressbar_visible) {

                    if ((loopCnt++ % skipCnt == 0) || (fileSize == (totalSize - offset))) {

                        final int per = (int) ((viewTotalSize / fileSize) * 100);

                        if (per > progress) {

                            try {

                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (getFragment() != null) {
                                            ((NativeLauncherFragment) getFragment()).getDownloadProgressBar().setProgress(per);
                                        }
                                    }
                                });

                                progress = per;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            Logger.i(TAG, "SET download path : " + download_path);

            return_download_path = target_path;

            root.put(RESULT, true);
            root.put(FILE_PATH, target_path);
            root.put(FILE_ID, file_id);
            root.put(TOTAL_COUNT, length);
            root.put(CURRENT_COUNT, index);

        } catch (Exception e) {
            try {
                // 파일이 생성되어 있다면 삭제
                File f = new File(target_path);
                if (f.exists()) {
                    f.delete();
                }
                root.put(RESULT, false);
                if (e instanceof HttpResponseException) {            // HTTTP
                    root.put(ERROR_CODE, "HE0" + ((HttpResponseException) e).getStatusCode());
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof ConnectTimeoutException) {    // Connect
                    root.put(ERROR_CODE, "NE0001");
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof SocketTimeoutException) {    // Read
                    root.put(ERROR_CODE, "NE0002");
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof IOException) {            // 기타 예상치못한 네트워크 에러
                    root.put(ERROR_CODE, "NE0003");
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof NullPointerException) {
                    root.put(ERROR_CODE, "CE0001");                // 기타 컨테이너 에러
                    root.put(ERROR_TEXT, "NullPointerException");
                } else {
                    root.put(ERROR_CODE, "CE0001");                // 기타 컨테이너 에러
                    root.put(ERROR_TEXT, e.getMessage());
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        } finally {
            // download path 원복
            AbstractDownloadService.setDownloadPath(org_download_path);
            Logger.i(TAG, "Restore download path : " + org_download_path);
        }

        return root;
    }

    /**
     * http:// 또는 https:// 를 포함한  url로 파일을 다운로드 할 경우 사용되는 메소드.<br>
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
     * @param activity 사용되지 않음
     * @param url      다운받을 파일의 uri
     * @param path     파일을 저장할 경로
     * @return 파일다운로드 정보를 담은  JSONObject <br><br>
     * Return : (JSONObject) 다운로드 후 성공 여부 및 경로 반환
     */
    private JSONObject externalDownload(final Activity activity, String url, String path) {

        JSONObject root = new JSONObject();

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            // 디렉토리 생성
            makeDir(path);

            File outputFile = new File(path);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoInput(true);
            conn.connect();

            fos = new FileOutputStream(outputFile);
            bis = new BufferedInputStream(conn.getInputStream(), 8192);
            bos = new BufferedOutputStream(fos, 8192);

            float totalSize = conn.getContentLength();

            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            int progress = 0;
            float appSize = 0;
            while ((bytesRead = bis.read(buffer)) != -1) {
                appSize = appSize + bytesRead;
                bos.write(buffer, 0, bytesRead);

                final int per = (int) ((appSize / totalSize) * 100);

                if (per > progress) {

                    try {

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                if (getFragment() != null) {
                                    ((NativeLauncherFragment) getFragment()).getDownloadProgressBar().setProgress(per);
                                }
                            }
                        });

                        progress = per;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            root.put(RESULT, true);
            root.put(FILE_PATH, path);
        } catch (Exception e) {
            try {
                // 파일이 생성되어 있다면 삭제
                File f = new File(path);
                if (f.exists()) {
                    f.delete();
                }
                root.put(RESULT, false);
                if (e instanceof HttpResponseException) {            // HTTTP
                    root.put(ERROR_CODE, "HE0" + ((HttpResponseException) e).getStatusCode());
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof ConnectTimeoutException) {    // Connect
                    root.put(ERROR_CODE, "NE0001");
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof SocketTimeoutException) {    // Read
                    root.put(ERROR_CODE, "NE0002");
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof IOException) {            // 기타 예상치못한 네트워크 에러
                    root.put(ERROR_CODE, "NE0003");
                    root.put(ERROR_TEXT, e.getMessage());
                } else if (e instanceof NullPointerException) {
                    root.put(ERROR_CODE, "CE0001");                // 기타 컨테이너 에러
                    root.put(ERROR_TEXT, "NullPointerException");
                } else {
                    root.put(ERROR_CODE, "CE0001");                // 기타 컨테이너 에러
                    root.put(ERROR_TEXT, e.getMessage());
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return root;
    }

    /**
     * 디렉토리를 생성하는 메소드 <br>
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
     * @param target_file_name 생성할 폴더명
     * @return 생선된 폴더 경로
     */
    private String makeDir(String target_file_name) {
        String directory = "";
        int idx = target_file_name.lastIndexOf('/');
        if (idx > 0) {
            directory = target_file_name.substring(0, idx);
        }
        File f = new File(directory);
        if (!f.exists()) {
            f.mkdirs();
        }
        return directory;
    }

    ResponseHandler<String> httpResponseHandler = new ResponseHandler<String>() {

        RandomAccessFile fos = null;
        BufferedInputStream bis = null;
        JSONObject jsonData = null;

        String tempFileName = "";
        String destFileName = "";
        int nDownloadMode = 1;        // 1 : 신규, 3.이어받기
        boolean bSuccess = false;    // 다운로드 성공 유무

        /**
         * 실제 다운로드 작업 을 수행하는 메소드<br>
         *
         * <pre>
         * 수정이력 <br>
         * **********************************************************************************************************************************<br>
         *  수정일                               이름                               변경내용<br>
         * **********************************************************************************************************************************<br>
         *  2016-09-20                           박재민                             최초 작성<br>
         *
         *</pre>
         *
         * @param res 다운로드 통신후 수신 결과
         * @return null <br><br>
         * Result : (JSONObject) 다운로드 작업 후 수행 여부 및 파일 경로 반환
         */
        @Override
        public String handleResponse(final HttpResponse res) throws ClientProtocolException, IOException {

            JSONObject root = new JSONObject();

            try {

                fos = new RandomAccessFile(file.getAbsolutePath(), "rw");
                final long offset = fos.length();
                fos.seek(offset);


                bis = new BufferedInputStream(res.getEntity().getContent(), 8192);

                if (res.containsHeader("error_code")) {
                    throw new Exception(URLDecoder.decode(res
                            .getFirstHeader("info_text").getValue(), "utf-8"));
                }
                if (res.getStatusLine().getStatusCode() != 200) {
                    throw new Exception("Http Error " + res.getStatusLine().getStatusCode()
                            + " " + res.getStatusLine().getReasonPhrase());
                }
                final int fileSize = Integer.parseInt(res.getFirstHeader("file_size").getValue());
                Logger.i(TAG, "file_size" + fileSize);

                int len = 0;
                int totalSize = 0;
                int loopCnt = 0; // loopCount
                double skipCnt = Math.ceil((fileSize / 100) / (double) BUFFER_SIZE);
                Logger.i(TAG, "skip_cnt" + skipCnt);

                totalSize = (int) offset;

                if (fileSize == 0)
                    throw new Exception("file size is 0");

                while ((len = bis.read(buffer)) != -1) {

                    fos.write(buffer, 0, len);
                    totalSize = totalSize + len;

                    float viewTotalSize = totalSize;

                    if (progressbar_visible) {

                        if ((loopCnt++ % skipCnt == 0) || (fileSize == (totalSize - offset))) {

                            final int per = (int) ((viewTotalSize / fileSize) * 100);

                            if (per > progress) {

                                try {

                                    handler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            if (getFragment() != null) {

                                                ((NativeLauncherFragment) getFragment()).getDownloadProgressBar().setProgress(per);
                                            }
                                        }
                                    });
                                    progress = per;

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                long stop = System.currentTimeMillis();
                //					Log.i(TAG, ELAPSED_TIME + (stop - start) + MS);

                bSuccess = true;

                try {
                    root.put("result", bSuccess);
                    root.put("path", return_download_path);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {

                e.printStackTrace();

                File f = new File(return_download_path);
                if (f.exists()) {
                    f.delete();
                }

                try {

                    root.put(RESULT, false);
                    if (e instanceof HttpResponseException) {            // HTTTP
                        root.put(ERROR_CODE, "HE0" + ((HttpResponseException) e).getStatusCode());
                        root.put(ERROR_TEXT, e.getMessage());
                    } else if (e instanceof ConnectTimeoutException) {    // Connect
                        root.put(ERROR_CODE, "NE0001");
                        root.put(ERROR_TEXT, e.getMessage());
                    } else if (e instanceof SocketTimeoutException) {    // Read
                        root.put(ERROR_CODE, "NE0002");
                        root.put(ERROR_TEXT, e.getMessage());
                    } else if (e instanceof IOException) {            // 기타 예상치못한 네트워크 에러
                        root.put(ERROR_CODE, "NE0003");
                        root.put(ERROR_TEXT, e.getMessage());
                    } else if (e instanceof NullPointerException) {
                        root.put(ERROR_CODE, "CE0001");                // 기타 컨테이너 에러
                        root.put(ERROR_TEXT, "NullPointerException");
                    } else {
                        root.put(ERROR_CODE, "CE0001");                // 기타 컨테이너 에러
                        root.put(ERROR_TEXT, e.getMessage());
                    }

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            } finally {
                dismissProgress();
                listener.resultCallback("callback", resultCallback, root);
            }

            return null;
        }
    };


    private void showProgress() {
        if (progressbar_visible) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    if (getFragment() != null) {
                        ((NativeLauncherFragment) getFragment()).setDefaultProgress();
                        ((NativeLauncherFragment) getFragment()).getDownloadDesTextView().setText(progressMsg);
                    }
                }
            });
        }
    }

    private void dismissProgress() {
        if (progressbar_visible) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    if (getFragment() != null) {
                        ((NativeLauncherFragment) getFragment()).getDownloadProgressBar().setProgress(0);
                        ((NativeLauncherFragment) getFragment()).getDownloadDesTextView().setText("");
                    }
                }
            });
        }
    }
}
