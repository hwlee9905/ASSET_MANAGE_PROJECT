package com.mcnc.bizmob.progress;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mcnc.bizmob.manageapp.R;
import com.mcnc.bizmob.core.util.res.ImageWrapper;

/**
 * 01.클래스 설명 : 커스텀 프로그레스 다이얼로그를 생성. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 커스텀 프로그레스 다이얼로그 생성, 닫기, 업데이트 <br>
 * 04.관련 API/화면/서비스 :  <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-10-05                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */


public class CustomProgressDialog extends Dialog {

    private Context mContext;

    /**
     * CustomProgressDialog 이미지 path
     */
    private String img_path = "";

    /**
     * CustomProgressDialog 메시지뷰
     */
    private TextView mTvMessage = null;


    /**
     * CustomProgressDialog 생성자
     */
    public CustomProgressDialog(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        init();

    }

    public CustomProgressDialog(Context context, String path){
        super(context);
        mContext = context;
        img_path = path;
        init();
        createProgressLoading();
    }

    public CustomProgressDialog(Context context, int theme, String path){
        super(context, theme);
        mContext = context;
        img_path = path;
        init();
        createProgressLoading();
    }

    /**
     * CustomProgressDialog 타이틀 설정
     */
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    /**
     * CustomProgressDialog 메시지 설정
     */
    public void setMessage(CharSequence message){
        mTvMessage.setText(message);
    }

    /**
     * CustomProgressDialog의 기본 설정값을 initialize 하는 메소드
     */
    public void init() {
        setWindowAttributes();
        initLayout();

    }

    /**
     * CustomProgressDialog의 배경화면 설정
     */
    private void setWindowAttributes(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    /**
     * CustomProgressDialog의 레이아웃 생성
     */
    private void initLayout(){
        setContentView(R.layout.loading);
        mTvMessage = (TextView) findViewById(R.id.message);

    }

    /**
     * CustomProgressDialog의 로딩 gif 생성
     */
    private void createProgressLoading(){
        if(img_path != null){
            String imgUrl = ImageWrapper.getUri(img_path);

            ImageView iv = (ImageView)findViewById(R.id.loading);
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv);
            Glide.with(mContext).load(imgUrl).into(imageViewTarget);
        }
    }

}
