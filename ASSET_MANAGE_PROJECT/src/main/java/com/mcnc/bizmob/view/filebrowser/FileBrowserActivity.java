package com.mcnc.bizmob.view.filebrowser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;
import android.widget.Toast;

import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * 01.클래스 설명 : 파일 브라우저 역할을 수행하는 Activity 클래스.<br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 파일 브라우저 <br>
 * 04.관련 API/화면/서비스 : RUtil, FileBrowserAdapter <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-01                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class FileBrowserActivity extends Activity {
    /**
     * 클래스명
     */

    public static final String TAG = "FileBrowserActivity";

    /**
     * 현재 폴더를 가리키는 File 객체.
     */
    public File currentDir;

    /**
     * 파일 브라우져를 표현할 Custom ArrayAdapter.
     */
    public FileBrowserAdapter adapter;

    /**
     * Folder Stack 관리를 위한 Stack 객체.
     */
    public Stack<File> dirStack = new Stack<File>();

    /**
     * sdCard 영역 root path.
     */
    public String rootPath;

    /**
     * ListActivity class의 Life Cycle 중 하나에 속하는 메소드로 아래와 같은 기능을 수행함.<br>
     * 1. File folder 경로 셋팅 <br>
     * 2. 파일 리스트 생성 메소드 호출 <br>
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
     * @param savedInstanceState bundle 객체.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(RUtil.getLayoutR(this, "activity_file_browser"));


        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        currentDir = new File(rootPath);
        fillFileList(currentDir);
    }

    /**
     * 폴더 내부의 파일 정보를 list화 하는 메소드.<br>
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
     * @param currentDir 폴더의 File 객체.
     */
    public void fillFileList(File currentDir) {
        File[] dirs = new File[0];
        try {
            dirs = currentDir.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Option> dir = new ArrayList<Option>();
        List<Option> fls = new ArrayList<Option>();

        if (dirs == null) {
            Toast.makeText(this, "There is no file or directory.", Toast.LENGTH_SHORT).show();
        } else {

            try {
                for (File ff : dirs) {
                    if (ff.isDirectory())
                        dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath()));
                    else {
                        fls.add(new Option(ff.getName(), "" + ff.length(), ff.getAbsolutePath()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Collections.sort(dir);
            Collections.sort(fls);
            dir.addAll(fls);
        }

        Logger.d(TAG, "currentDir.getParent() = " + currentDir.getParent());
        if (!currentDir.getParent().equals("/storage/emulated")) {
            dir.add(0, new Option("...", "parent_directory", currentDir.getParent()));
        }

        adapter = new FileBrowserAdapter(this, RUtil.getLayoutR(this, "row_file_browser"), dir);
        ListView listView = (ListView) findViewById(RUtil.getIdR(this, "lv_file_browser"));
        listView.setAdapter(adapter);
    }


    /**
     * 01.클래스 설명 : 파일 정보를 담고 있는 DTO Class <br>
     * 02.제품구분 : bizMOB 3.0 Android Container <br>
     * 03.기능(콤퍼넌트) 명 : 파일 DTO <br>
     * 04.관련 API/화면/서비스 :  <br>
     * 05.수정이력  <br>
     * <pre>
     * **********************************************************************************************************************************<br>
     *  수정일                                          이름                          변경 내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-01                                    박재민                         최초 작성<br>
     * **********************************************************************************************************************************<br>
     * </pre>
     *
     * @author 박재민
     * @version 1.0
     */
    public class Option implements Comparable<Option> {
        public String name;
        public String data;
        public String path;

        public Option(String name, String data, String path) {
            this.name = name;
            this.data = data;
            this.path = path;
        }

        public String getData() {
            return data;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        @Override
        public int compareTo(Option another) {
            if (this.name != null)
                return this.name.toLowerCase().compareTo(another.getName().toLowerCase());
            else
                throw new IllegalArgumentException();
        }
    }

    /**
     * Hardware back button event 발생시 호출 되는 default method로, 상위 폴더 여부에 따라 파일 리스트 갱신 또는  화면 종료 기능을 수행하는 메소드.<br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-12-01                           박재민                             최초 작성<br>
     *
     * </pre>
     */
    @Override
    public void onBackPressed() {
        if (dirStack.size() == 0) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        currentDir = dirStack.pop();
        fillFileList(currentDir);
    }
}
