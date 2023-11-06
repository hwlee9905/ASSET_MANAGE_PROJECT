package com.mcnc.bizmob.view.filebrowser;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mcnc.bizmob.core.util.res.RUtil;

import java.io.File;
import java.util.List;


/**
 * 01.클래스 설명 : 파일 리스트를 표현하는 FileBrowserActivity에 사용되는 ArrayAdapter..<br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 :ArrayAdapter <br>
 * 04.관련 API/화면/서비스 : FileBrowserActivity에, RUtil <br>
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
public class FileBrowserAdapter extends ArrayAdapter<FileBrowserActivity.Option> {

    /**
     * Activity 객체.
     */
    private FileBrowserActivity activity;
    /**
     * TextView의 Resource Id 값.
     */
    private int id;

    /**
     * file 정보 값을 보유한 Option(DTO) 객체 를 보유한 List.
     */
    private List<FileBrowserActivity.Option> items;

    /**
     * 기본 생성자. <br>
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
     * @param activity           activity 객체.
     * @param textViewResourceId TextView의 resourceId 값.
     * @param objects            파일 정보를 갖고 있는 list.
     */
    public FileBrowserAdapter(FileBrowserActivity activity, int textViewResourceId,
                              List<FileBrowserActivity.Option> objects) {
        super(activity.getBaseContext(), textViewResourceId, objects);
        this.activity = activity;
        id = textViewResourceId;
        items = objects;
    }

    /**
     * ArrayAdapter class의 default method로 position 번째의 Object를 반환함.<br>
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
     * @param position 표시할 position
     * @return 반환할 position 번째의 Option(DTO) 객체.
     */
    @Override
    public FileBrowserActivity.Option getItem(int position) {
        return items.get(position);
    }

    /**
     * ArrayAdapter class의 default method로 position 번째의 view를 생성 / 반환함.<br>
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
     * @param position    표시할 position
     * @param convertView 반환 될 view 객체.
     * @param parent      ViewGroup 객체.
     * @return 반환할 View 객체.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(id, null);
        }
        final FileBrowserActivity.Option o = items.get(position);
        if (o != null) {
            TextView fileName = (TextView) view.findViewById(RUtil.getIdR(activity, "tv_file_name"));
            if (fileName != null) {
                fileName.setText(o.getName());
            }
            ImageView folder = (ImageView) view.findViewById(RUtil.getIdR(activity, "iv_folder"));
            if (o.getData().equalsIgnoreCase("folder")) {
                folder.setVisibility(View.VISIBLE);
            } else {
                folder.setVisibility(View.GONE);
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileBrowserActivity.Option o = getItem(position);
                if (o.getData().equalsIgnoreCase("folder")) {
                    activity.dirStack.push(activity.currentDir);
                    activity.currentDir = new File(o.getPath());
                    activity.fillFileList(activity.currentDir);
                } else if (o.getData().equalsIgnoreCase("parent_directory")) {
                    try {
                        activity.currentDir = activity.dirStack.pop();
                    } catch (Exception e) {
                        Toast.makeText(activity, "This is root Directory ", Toast.LENGTH_SHORT).show();
                    }
                    activity.fillFileList(activity.currentDir);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("filePath", o.getPath());
                    activity.setResult(activity.RESULT_OK, intent);
                    activity.finish();
                }
            }
        });

        return view;
    }
}
