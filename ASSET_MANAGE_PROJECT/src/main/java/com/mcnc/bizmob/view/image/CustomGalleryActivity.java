package com.mcnc.bizmob.view.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 01.클래스 설명 : 갤러리 기능을 수행하는 멀티 초이스 기능이 구현된 Activity 클래스.<br>
 * 02.제품구분 : bizMob 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 :멀티 초이스 갤러리 <br>
 * 04.관련 API/화면/서비스 : AppConfig, ImageWrapper, RUtil, ImageGridActivity <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-26                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class CustomGalleryActivity extends Activity implements View.OnClickListener {

    /**
     * image 객체를 담고 있는 arrayList.
     */
    private static CopyOnWriteArrayList<Media> totalMediaList = null;

    private static CopyOnWriteArrayList<Media> singleMediaList = null;

    /**
     * 앨범 리스트
     */
    private ArrayList<String> albumTitleList = null;

    /**
     * 이미지 caching용 LruCache 객체.
     */
    private LruCache<String, Bitmap> thumbnailCache = null;

    /**
     * 갤러리 파일 타입 list ("movie" or "image")
     */
    private JSONArray typeList = null;

    /**
     * image 인지 여부.
     */
    private boolean imageYN = false;

    /**
     * video 인지 여부.
     */
    private boolean videoYN = false;

    /**
     * 최대 선택 개수.
     */
    private int maxCount = 1;

    /**
     * GideView에 표현할 이미지 정보를 갖고 있는 GridAdapter 객체.
     */
    private GridAdapter adapter = null;

    /**
     * image를 표현할 GrideView 객체.
     */
    private GridView mGridView = null;

    /**
     * pre load 할 image의 개수.
     */
    final static int PRE_LOAD_IMAGE = 20;

    /**
     * 가로 모드의 컬럼 개수.
     */
    final static int LANDSCAPE_COL_NUM = 5;
    /**
     * 세로 모드의 컬럼 개수.
     */
    final static int PORTRAIT_COL_NUM = 3;

    /**
     * Image 정보를 담고 있는 list.
     */
    ArrayList<Media> totalSavedMediaList = new ArrayList<>();

    ArrayList<Media> singleSavedMediaList = new ArrayList<>();

    private boolean isShowAllMode = true;

    private int mOrientation;

    private String txtBtnSelectOk;

    private TextView btnSelectOk;
    private TextView btnCancel;

    private boolean isAofBoxShown = false;

    private LinearLayout llAofWrapper;

    private ImageButton btnBack;

    private ImageButton btnSelectType;
    private RelativeLayout rlTvAll;
    private RelativeLayout rlTvPicture;
    private RelativeLayout rlTvVideo;
    private TextView tvAll;
    private TextView tvPicture;
    private TextView tvVideo;
    private TextView tvGalleryTitle;

    private String currentAlbumName = "";
    private TextView tvAlbumTitle;
    private RelativeLayout rlSpinner;
    private ImageButton btnSelectAlbum;
    private ListView listView;

    private boolean isListViewShown;

    private int titleItemHeight;

    private ArrayAdapter<String> listArrayAdapter;

    private int firstVisibleItem = -1;
    private int mSelectPostion = 0;
    private int mSelectRotate = 0;

    private ArrayList<String> mAlbumIdList = new ArrayList<>();

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == btnSelectType.getId()) {
            if (isAofBoxShown) {
                llAofWrapper.setVisibility(View.INVISIBLE);
                isAofBoxShown = false;
            } else {
                if (isListViewShown) {
                    listView.setVisibility(View.INVISIBLE);
                    isListViewShown = false;
                }
                llAofWrapper.setVisibility(View.VISIBLE);
                isAofBoxShown = true;
            }
        } else if (btnCancel != null && viewId == btnCancel.getId()) {
            onBackPressed();
        } else if (btnSelectOk != null && viewId == btnSelectOk.getId()) {
            onOkPressed();
        } else if (viewId == rlTvAll.getId() || viewId == tvAll.getId()) {
            if (isAofBoxShown) {
                llAofWrapper.setVisibility(View.INVISIBLE);
                isAofBoxShown = false;
            }

            if (btnSelectOk != null) {
                btnSelectOk.setText(txtBtnSelectOk);
            }

            videoYN = true;
            imageYN = true;
            mSelectRotate = 0;
            btnSelectAlbum.animate().rotation(mSelectRotate).setInterpolator(new AccelerateDecelerateInterpolator());
            changeAlbum(currentAlbumName);

        } else if (viewId == rlTvPicture.getId() || viewId == tvPicture.getId()) {
            if (isAofBoxShown) {
                llAofWrapper.setVisibility(View.INVISIBLE);
                isAofBoxShown = false;
            }
            if (btnSelectOk != null) {
                btnSelectOk.setText(txtBtnSelectOk);
            }
            videoYN = false;
            imageYN = true;
            mSelectRotate = 0;
            btnSelectAlbum.animate().rotation(mSelectRotate).setInterpolator(new AccelerateDecelerateInterpolator());
            changeAlbum(currentAlbumName);


        } else if (viewId == rlTvVideo.getId() || viewId == tvVideo.getId()) {
            if (isAofBoxShown) {
                llAofWrapper.setVisibility(View.INVISIBLE);
                isAofBoxShown = false;
            }
            if (btnSelectOk != null) {
                btnSelectOk.setText(txtBtnSelectOk);
            }

            videoYN = true;
            imageYN = false;
            mSelectRotate = 0;
            btnSelectAlbum.animate().rotation(mSelectRotate).setInterpolator(new AccelerateDecelerateInterpolator());
            changeAlbum(currentAlbumName);

        } else if (viewId == btnBack.getId()) {
            onBackPressed();
        } else if (viewId == tvAlbumTitle.getId() || viewId == rlSpinner.getId() || viewId == btnSelectAlbum.getId()) {
//            float deg = btnSelectAlbum.getRotation() + 180F;
            float deg = mSelectRotate + 180F;
            btnSelectAlbum.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
            if(mSelectRotate == 0 ) {
                mSelectRotate = -180;
            } else if(mSelectRotate == -180) {
                mSelectRotate = 0;
            }

            if (isListViewShown) {
                listView.setVisibility(View.INVISIBLE);
                isListViewShown = false;
            } else {
                if (isAofBoxShown) {
                    llAofWrapper.setVisibility(View.INVISIBLE);
                    isAofBoxShown = false;
                }
                listView.setVisibility(View.VISIBLE);
                isListViewShown = true;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.setIndicatorBgColor(this, RUtil.getR(this, "color", "lollipop_indicator_bg"));
        setContentView(RUtil.getLayoutR(this, "activity_multi_gallery_album"));
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mOrientation = getResources().getConfiguration().orientation;
        currentAlbumName = getString(RUtil.getStringR(this, "txt_multi_gallery_show_all"));
        //use cache to store image thumbnail to improve performance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            thumbnailCache = new LruCache<String, Bitmap>(cacheSize);
        }

        btnSelectType = (ImageButton) findViewById(RUtil.getIdR(this, "btn_select_type"));
        tvGalleryTitle = (TextView) findViewById((RUtil.getIdR(this, "tv_gallery_title")));

        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            maxCount = bundle.getInt("max_count");
            typeList = new JSONArray(bundle.getString("typeList"));

            if (typeList != null) {
                for (int i = 0; i < typeList.length(); i++) {
                    String type = typeList.getString(i);
                    if (type.equals("image")) {
                        imageYN = true;
                    } else if (type.equals("video")) {
                        videoYN = true;
                    }
                }
            } else {
                imageYN = true;
            }

            if (imageYN && videoYN) {
                btnSelectType.setVisibility(View.VISIBLE);
            }

            if (imageYN && !videoYN) {
                tvGalleryTitle.setText(getString(RUtil.getStringR(this, "txt_multi_gallery_title_picture")));
            }

            if (!imageYN && videoYN) {
                tvGalleryTitle.setText(getString(RUtil.getStringR(this, "txt_multi_gallery_title_video")));
            }

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        //multi select Mode
        if (maxCount > 1) {
            LinearLayout llBottomWrapper = (LinearLayout) findViewById(RUtil.getIdR(this, "ll_bottom_wrapper"));
            llBottomWrapper.setVisibility(View.VISIBLE);
            btnCancel = (TextView) findViewById(RUtil.getIdR(this, "btn_cancel"));
            btnCancel.setOnClickListener(this);
            btnSelectOk = (TextView) findViewById(RUtil.getIdR(this, "btn_select_ok"));
            btnSelectOk.setOnClickListener(this);
            txtBtnSelectOk = btnSelectOk.getText().toString();
        }

        llAofWrapper = (LinearLayout) findViewById(RUtil.getIdR(this, "ll_aof_wrapper"));

        //타이틀 영역 백 버튼, 타입 선택 버튼
        btnBack = (ImageButton) findViewById(RUtil.getIdR(this, "btn_back"));
        btnBack.setOnClickListener(this);

        btnSelectType.setOnClickListener(this);


        //갤러리 타입 선택 드롭다운 메뉴
        rlTvAll = (RelativeLayout) findViewById(RUtil.getIdR(this, "rl_tv_all"));
        rlTvAll.setOnClickListener(this);

        tvAll = (TextView) findViewById(RUtil.getIdR(this, "tv_all"));
        tvAll.setOnClickListener(this);

        rlTvPicture = (RelativeLayout) findViewById(RUtil.getIdR(this, "rl_tv_picture"));
        rlTvPicture.setOnClickListener(this);

        tvPicture = (TextView) findViewById(RUtil.getIdR(this, "tv_picture"));
        tvPicture.setOnClickListener(this);

        rlTvVideo = (RelativeLayout) findViewById(RUtil.getIdR(this, "rl_tv_video"));
        rlTvVideo.setOnClickListener(this);

        tvVideo = (TextView) findViewById(RUtil.getIdR(this, "tv_video"));
        tvVideo.setOnClickListener(this);


        //앨범선택
        rlSpinner = (RelativeLayout) findViewById(RUtil.getIdR(this, "rl_spinner"));
        rlSpinner.setOnClickListener(this);

        btnSelectAlbum = (ImageButton) findViewById(RUtil.getIdR(this, "ib_select_album"));
        btnSelectAlbum.setOnClickListener(this);

        tvAlbumTitle = (TextView) findViewById(RUtil.getIdR(this, "tv_album_title"));
        tvAlbumTitle.setOnClickListener(this);

        listView = (ListView) findViewById(RUtil.getIdR(this, "listView"));
        //사진 or 동영상이 포함 된 앨범타이틀 리스트 획득.
        albumTitleList = getAlbumTitleList();

        // dropdown spinner에 넣을 앨범 타이틀 어댑터를 생성한다.
        listArrayAdapter = new ArrayAdapter<String>(this, RUtil.getLayoutR(this, "row_spinner_item"), albumTitleList) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(RUtil.getLayoutR(CustomGalleryActivity.this, "row_spinner_item"), parent, false);
                }

                if (position == 0) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        convertView.setBackgroundDrawable(getResources().getDrawable(RUtil.getDrawableR(CustomGalleryActivity.this, "image_title_rectangle")));
                    } else {
                        convertView.setBackground(getResources().getDrawable(RUtil.getDrawableR(CustomGalleryActivity.this, "image_title_rectangle")));

                    }
                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        convertView.setBackgroundDrawable(getResources().getDrawable(RUtil.getDrawableR(CustomGalleryActivity.this, "image_title_rectangle_without_top")));
                    } else {
                        convertView.setBackground(getResources().getDrawable(RUtil.getDrawableR(CustomGalleryActivity.this, "image_title_rectangle_without_top")));
                    }
                }

                if (titleItemHeight == 0) {
                    titleItemHeight = convertView.getLayoutParams().height;

                    //목록이 5개 이상인 경우
                    if (albumTitleList.size() > 5) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) listView.getLayoutParams();
                        int maxHeight = titleItemHeight * 5;
                        layoutParams.height = maxHeight;
                        listView.setLayoutParams(layoutParams);
                    }
                }
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectPostion = position;
                        float deg = mSelectRotate + 180F;
                        btnSelectAlbum.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
                        if(mSelectRotate == 0 ) {
                            mSelectRotate = -180;
                        } else if(mSelectRotate == -180) {
                            mSelectRotate = 0;
                        }

                        changeAlbum(getItem(position));
                        listView.setSelection(position);
                    }
                });


                TextView tvTitle = (TextView) convertView.findViewById(RUtil.getIdR(CustomGalleryActivity.this, "tv_title"));

                tvTitle.setText(getItem(position));
                if (currentAlbumName.equals(getItem(position))) {
                    if(mSelectPostion == position)
                        tvTitle.setTextColor(getResources().getColor(RUtil.getR(CustomGalleryActivity.this, "color", "gallery_selected_title")));
                    else
                        tvTitle.setTextColor(getResources().getColor(RUtil.getR(CustomGalleryActivity.this, "color", "gallery_not_selected_title")));
                } else {
                    tvTitle.setTextColor(getResources().getColor(RUtil.getR(CustomGalleryActivity.this, "color", "gallery_not_selected_title")));

                }

                return convertView;
            }
        };

        listView.setAdapter(listArrayAdapter);

        //일단 전체 보기 화면으로 그리드 (미디어 객체를 보여즐)뷰를 선택 한다.
        mGridView = (GridView) findViewById(RUtil.getIdR(this, "gridView"));
        mGridView.setVerticalScrollBarEnabled(false);
        mGridView.setHorizontalScrollBarEnabled(false);

        //화면 오리엔테이션에 따라 각각 최대 컬럼수를 설정 한다.
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mGridView.setNumColumns(PORTRAIT_COL_NUM);
        } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGridView.setNumColumns(LANDSCAPE_COL_NUM);
        }

        if (totalMediaList == null) {
            totalMediaList = getMediaList(isShowAllMode, getString(RUtil.getStringR(this, "txt_multi_gallery_show_all")));
        }

        adapter = new GridAdapter(this, totalMediaList);
        mGridView.setAdapter(adapter);
        mGridView.setOnScrollListener(onScrollListener);

        //remember scroll position when screen is rotated
        if (firstVisibleItem != -1)
            mGridView.smoothScrollToPosition(firstVisibleItem);

    }

    private void changeAlbum(String albumName) {

//        float deg = btnSelectAlbum.getRotation() + 180F;
//        float deg = mSelectRotate + 180F;
//        btnSelectAlbum.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());
//        if(mSelectRotate == 0 ) {
//            mSelectRotate = -180;
//        } else if(mSelectRotate == -180) {
//            mSelectRotate = 0;
//        }

        listView.setVisibility(View.INVISIBLE);
        isListViewShown = false;


        //앨범 초기화
        totalSavedMediaList = new ArrayList<>();
        singleSavedMediaList = new ArrayList<>();

        if (btnSelectOk != null) {
            btnSelectOk.setText(txtBtnSelectOk);
        }

        currentAlbumName = albumName;

        tvAlbumTitle.setText(albumName);
        tvAlbumTitle.setTextColor(getResources().getColor(RUtil.getR(this, "color", "gallery_selected_album")));

        if (albumName.equalsIgnoreCase("show all") || albumName.equals("전체보기")) {
            isShowAllMode = true;
            totalMediaList = getMediaList(isShowAllMode, albumName);
            adapter.setItems(totalMediaList);

        } else {
            isShowAllMode = false;
            singleMediaList = getMediaList(isShowAllMode, albumName);
            adapter.setItems(singleMediaList);
        }
        listArrayAdapter.notifyDataSetChanged();
        listView.setAdapter(listArrayAdapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        adapter.notifyDataSetChanged();
        firstVisibleItem = -1;
        mGridView.setAdapter(adapter);
    }

    private class Media {
        private String uri = "";
        private boolean isVideo = false;
        private int id;
        private boolean isChecked = false;
        private int mediaIndex = 0;
        private View textView;
        private int itemIndex = 0;
        private long modifiedDate = 0;


    }

    private class GridAdapter extends BaseAdapter {

        private CopyOnWriteArrayList<Media> items;

        public void setItems(CopyOnWriteArrayList<Media> items) {
            this.items = items;
        }

        private LayoutInflater inflater;

        private Context context;

        public GridAdapter(Context context, CopyOnWriteArrayList<Media> items) {
            inflater = LayoutInflater.from(context);
            this.items = items;
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public View getView(final int index, View view, ViewGroup viewGroup) {
            View v = view;
            final ImageView picture;

            if (v == null) {
                v = inflater.inflate(RUtil.getLayoutR(context, "item_multi_gallery_ablum_image_frame"), viewGroup, false);
                picture = (ImageView) v.findViewById(RUtil.getIdR(context, "grid_image"));
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                ViewGroup.LayoutParams pictureParam = picture.getLayoutParams();
                pictureParam.width = (metrics.widthPixels) / mGridView.getNumColumns();
                float margin = Utils.getPixelOfDpi(CustomGalleryActivity.this, 23);
                pictureParam.height = (int) (pictureParam.width - margin);
                picture.setLayoutParams(pictureParam);
                v.setTag(picture);

                final RelativeLayout imageOver = (RelativeLayout) v.findViewById(RUtil.getIdR(context, "rl_image_over"));
                RelativeLayout imageOverFrame = (RelativeLayout) v.findViewById(RUtil.getIdR(context, "rl_image_over_frame"));
                RelativeLayout.LayoutParams imageOverFrameParam = (RelativeLayout.LayoutParams) imageOverFrame.getLayoutParams();
                imageOverFrameParam.height = pictureParam.height;
                imageOverFrameParam.width = pictureParam.width;
                RelativeLayout.LayoutParams imageOverParam = (RelativeLayout.LayoutParams) imageOver.getLayoutParams();
                imageOverParam.height = pictureParam.height;
                imageOverParam.width = pictureParam.width;
                int frameMargin = (int) Utils.getPixelOfDpi(CustomGalleryActivity.this, 4);
                imageOverFrameParam.setMargins(frameMargin, frameMargin, frameMargin, frameMargin);
                imageOverFrame.setLayoutParams(imageOverFrameParam);
                imageOver.setLayoutParams(imageOverParam);

                picture.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int index = (Integer) v.getTag();
                        Media media = items.get(index);
                        ArrayList<Media> savedList = null;
                        CopyOnWriteArrayList<Media> mediaList = null;
                        if (isShowAllMode) {
                            savedList = totalSavedMediaList;
                            mediaList = totalMediaList;
                        } else {
                            savedList = singleSavedMediaList;
                            mediaList = singleMediaList;
                        }

                        if (!media.isChecked) {
                            media.itemIndex = index;

                            if (savedList.size() < maxCount) {
                                if (savedList.size() <= mediaList.size()) {
                                    media.mediaIndex = savedList.size() + 1;
                                }

                                media.isChecked = true;

                                imageOver.setVisibility(View.VISIBLE);

                                savedList.add(media);

                                if (btnSelectOk != null) {
                                    int selectedCnt = savedList.size();
                                    if (selectedCnt == 0) {
                                        btnSelectOk.setText(txtBtnSelectOk);
                                    } else {
                                        btnSelectOk.setText(txtBtnSelectOk + " (" + selectedCnt + ")");
                                    }
                                }

                            } else if (maxCount <= 1) {

                                if (savedList.size() > 0) {
                                    //기존에 선택된거 초기화
                                    Media preSelectedImg = savedList.get(0);
                                    Media image = items.get(preSelectedImg.itemIndex);
                                    image.isChecked = false;
                                    items.set(preSelectedImg.itemIndex, image);
                                    savedList.remove(0);
                                    notifyDataSetChanged();
                                }

                                media.isChecked = true;

                                if (savedList.size() <= mediaList.size()) {
                                    media.mediaIndex = savedList.size() + 1;
                                }

                                imageOver.setVisibility(View.VISIBLE);
                                savedList.add(media);
                            }

                            if (maxCount == 1) {
                                onOkPressed();
                            }
                        } else {
                            for (int i = media.mediaIndex; i < savedList.size(); i++) {
                                int savedIndex = savedList.get(i).mediaIndex;
                                savedList.get(i).mediaIndex = savedIndex - 1;
                            }
                            if (savedList.size() != 0) {
                                savedList.remove(media.mediaIndex - 1);
                            }
                            imageOver.setVisibility(View.INVISIBLE);

                            media.isChecked = false;

                            if (btnSelectOk != null) {
                                int selectedCnt = savedList.size();
                                if (selectedCnt == 0) {
                                    btnSelectOk.setText(txtBtnSelectOk);
                                } else {
                                    btnSelectOk.setText(txtBtnSelectOk + " (" + selectedCnt + ")");
                                }
                            }

                        }
                    }
                });


            } else {
                picture = (ImageView) v.getTag();

            }

            final Media media = (Media) getItem(index);
            picture.setImageBitmap(null);
            picture.setTag(index);
            ImageView videoIcon = (ImageView) v.findViewById(RUtil.getIdR(CustomGalleryActivity.this, "iv_video_icon"));

            if (media.isVideo) {
                videoIcon.setVisibility(View.VISIBLE);
            } else {
                videoIcon.setVisibility(View.GONE);
            }

            //preload some images to fill the screen
            //other images are loaded until they are already in cache
            //otherwise, those images shall be load onScrollStop in thread
            if (thumbnailCache != null) {
                Bitmap bitmap = thumbnailCache.get(media.uri);
                if (bitmap != null) {
                    picture.setImageBitmap(bitmap);
                } else {
                    if (index <= PRE_LOAD_IMAGE) {
                        loadImageTaskByVersion(CustomGalleryActivity.this, index, picture);
                    }
                }
            } else {
                if (index <= PRE_LOAD_IMAGE) {
                    loadImageTaskByVersion(CustomGalleryActivity.this, index, picture);
                }
            }

            RelativeLayout imageOver = (RelativeLayout) v.findViewById(RUtil.getIdR(context, "rl_image_over"));

            if (media.isChecked) {
                imageOver.setVisibility(View.VISIBLE);

            } else {
                imageOver.setVisibility(View.INVISIBLE);

            }

            return v;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

    }

    @Override
    protected void onDestroy() {
        int newOrientation = getResources().getConfiguration().orientation;
        if (newOrientation != mOrientation
                && Settings.System.getInt(
                getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            //on screen rotations
            if (mGridView != null && adapter != null && adapter.getCount() > 0)
                firstVisibleItem = mGridView.getFirstVisiblePosition();
        } else {
            //on activity finish
            if (thumbnailCache != null) {
                thumbnailCache.evictAll();
                thumbnailCache = null;
            }
            totalMediaList = null;
            singleMediaList = null;
            firstVisibleItem = -1;
        }
        super.onDestroy();
    }

    private void loadImageTaskByVersion(Context context, int i, ImageView imageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new ImageLoadTask(context, i, imageView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ImageLoadTask(context, i, imageView).execute();
        }
    }

    private final AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0) {
                //load image on scroll stop
                int from = mGridView.getFirstVisiblePosition();
                int to = mGridView.getLastVisiblePosition();
                if (to > PRE_LOAD_IMAGE) {
                    for (int i = from; i <= to; i++) {
                        if (thumbnailCache != null) {
                            Bitmap bitmap = thumbnailCache.get(String.valueOf(i));
                            if (bitmap == null) {
                                View v = mGridView.getChildAt(i - from);
                                final ImageView picture = (ImageView) v
                                        .findViewById(RUtil.getIdR(CustomGalleryActivity.this, "grid_image"));
                                loadImageTaskByVersion(CustomGalleryActivity.this, i, picture);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
        }
    };


    private CopyOnWriteArrayList<Media> getMediaList(boolean isAll, String albumName) {
        CopyOnWriteArrayList<Media> mediaList = new CopyOnWriteArrayList<>();
        Logger.d("getMediaList", "imageYN : " + imageYN);
        Logger.d("getMediaList", "videoYN : " + videoYN);
        if (imageYN) {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_MODIFIED};
            String selection = null;

            //개별 앨범 조회시 (null인 경우 전체 이미지 조회)
            if (!isAll) {
                String albumId = null;
                if (mSelectPostion == 0) albumId = mAlbumIdList.get(mSelectPostion);
                else albumId = mAlbumIdList.get(mSelectPostion -1);
                selection = MediaStore.Images.ImageColumns.BUCKET_ID + "='" + albumId + "'" ;
            }

            Cursor cursor = getContentResolver().query(uri, columns, selection,
                    null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");
            Media media = null;
            int uriIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int modifiedDateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
            while (cursor.moveToNext()) {
                media = new Media();
                media.uri = cursor.getString(uriIndex);
                media.id = cursor.getInt(idIndex);
                media.modifiedDate = cursor.getLong(modifiedDateIndex);
                mediaList.add(media);
            }
            cursor.close();
        }

        if (videoYN) {
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] columns = {MediaStore.Video.Thumbnails._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_MODIFIED};
            String selection = null;

            //개별 앨범 조회시 (null인 경우 전체 동영상 조회)
            if (!isAll) {
                String albumId = null;
                if (mSelectPostion == 0) albumId = mAlbumIdList.get(mSelectPostion);
                else albumId = mAlbumIdList.get(mSelectPostion -1);
                selection = MediaStore.Images.ImageColumns.BUCKET_ID + "='" + albumId + "'" ;
            }
            Cursor cursor = getContentResolver().query(uri, columns, selection,
                    null, MediaStore.Video.Media.DATE_MODIFIED + " DESC");

            Media media = null;
            int uriIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails._ID);
            int modifiedDateIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED);
            while (cursor.moveToNext()) {
                media = new Media();
                media.uri = cursor.getString(uriIndex);
                media.id = cursor.getInt(idIndex);
                media.modifiedDate = cursor.getLong(modifiedDateIndex);
                media.isVideo = true;
                if (imageYN && mediaList != null && mediaList.size() > 0) {
                    for (int i = 0; i < mediaList.size(); i++) {
                        Media addedImage = mediaList.get(i);
                        if (addedImage.modifiedDate < media.modifiedDate) {
                            mediaList.add(i, media);
                            break;
                        }
                    }
                } else {
                    mediaList.add(media);
                }
            }
            cursor.close();
        }
        return mediaList;
    }


    @Override
    protected void onPause() {
        if (thumbnailCache != null)
            thumbnailCache.evictAll();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        JSONArray photos = new JSONArray();
        resultIntent.putExtra("result", true);
        resultIntent.putExtra("images", photos.toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void onOkPressed() {
        CopyOnWriteArrayList<Media> images = adapter.items;
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = null;
        File file = null;
        for (Media media : images) {
            if (media.isChecked) {
                file = new File(media.uri);
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("filename", file.getName());
                    jsonObject.put("uri", media.uri);
                    jsonObject.put("size", Long.toString(file.length()));
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("result", true);
        resultIntent.putExtra("images", jsonArray.toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    private ArrayList<String> getAlbumTitleList() {

        ArrayList<String> list = new ArrayList<>();

        mAlbumIdList = new ArrayList<>();
        Cursor cur = null;
        if (imageYN) {
            //전체 보기 앨범 추가.
            String showAllAlbumTitle = getString(RUtil.getStringR(this, "txt_multi_gallery_show_all"));
            list.add(showAllAlbumTitle);

            //이미지 앨범 검색
            // SDK 29 DISTINCT 사용 불가로 getAlbumList 추가
            getAlbumList(this, true, list);
        }

        if (videoYN) {
            if (!imageYN) {
                //전체보기 앨범 추가.
                String showAllAlbumTitle = getString(RUtil.getStringR(this, "txt_multi_gallery_show_all"));
                list.add(showAllAlbumTitle);
            }

            //동영상 앨범 추가.
            // SDK 29 DISTINCT 사용 불가로 getAlbumList 추가
            getAlbumList(this, false, list);
        }

        return list;
    }

    @NonNull
    public ArrayList<String> getAlbumList(@NonNull final Context context, boolean IsImage, ArrayList<String> albumNameList)
    {
        Logger.d("CustomGalleryActivity", "getAlbumList----------------------------------------------------");
        final HashMap<String, String>  output = new HashMap<>();
        Uri contentUri;
        String[] projection = null;

        if(IsImage) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        } else {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_ID};
        }

        try (final Cursor cursor = context.getContentResolver().query(contentUri,
                projection, null, null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
        {
            if ((cursor != null) && (cursor.moveToFirst() == true))
            {
                final int columnBucketName = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                final int columnBucketId   = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);

                do
                {
                    String bucketName = cursor.getString(columnBucketName);
                    if(bucketName == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        bucketName = "0";
                    }

                    final String bucketId   = cursor.getString(columnBucketId);
                    if (output.containsKey(bucketId) == false)
                    {
                        final int count = getCount(context, contentUri, bucketId);

                        String str = "bucketName : " + bucketName + " bucketId : " + bucketId + " count : " + count;
                        Logger.d("CustomGalleryActivity", "str = " + str);

                        if(IsImage) {
                            if(bucketName != null) {
                                albumNameList.add(bucketName);
                                mAlbumIdList.add(bucketId);
                            }
                        } else {
                            boolean alreadyAddedAlbumName = false;
                            for (int i = 0; i < albumNameList.size()-1; i++) {
                                if(bucketName != null) {
                                    String addedAlbumName = albumNameList.get(i);
                                    if (bucketName.equals(addedAlbumName)) {
                                        alreadyAddedAlbumName = true;
                                    }
                                }
                            }

                            if (!alreadyAddedAlbumName && bucketName != null) {
                                albumNameList.add(bucketName);
                                mAlbumIdList.add(bucketId);
                            }
                        }

                        output.put(bucketId, bucketName);
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return albumNameList;
    }

    private static int getCount(@NonNull final Context context, @NonNull final Uri contentUri,
                                @NonNull final String bucketId)
    {
        try (final Cursor cursor = context.getContentResolver().query(contentUri,
                null, MediaStore.Video.Media.BUCKET_ID + "=?", new String[]{bucketId}, null))
        {
            return ((cursor == null) || (cursor.moveToFirst() == false)) ? 0 : cursor.getCount();
        }
    }

    private class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {

        private final int index;

        private final WeakReference<ImageView> imageViewReference;

        private Media media;

        private Context context;

        public ImageLoadTask(Context context, int index, ImageView img) {
            this.context = context;
            this.index = index;
            imageViewReference = new WeakReference<ImageView>(img);
            CopyOnWriteArrayList<Media> mediaList = null;
            if (isShowAllMode) {
                mediaList = totalMediaList;
            } else {
                mediaList = singleMediaList;
            }
            this.media = mediaList.get(index);
        }

        @Override
        protected Bitmap doInBackground(String... arg0) {
            Bitmap bitmap = null;
            try {

                int orientationDegree = ImageViewUtil.exifRotationDegrees(this.media.uri);

                if (media.isVideo) {

                    bitmap = ImageViewUtil.getVideoThumbnailBitmap(this.context.getContentResolver(),
                            this.media.id, orientationDegree, 2);
                } else {

                    bitmap = ImageViewUtil.getImageThumbnailBitmap(this.context.getContentResolver(),
                            this.media.id, orientationDegree, 2);
                }

                if (bitmap != null && thumbnailCache != null) {
                    thumbnailCache.put(media.uri, bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (imageView.getTag().equals(this.index)) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(RUtil.getDrawableR(context, "multi_gallery_image_not_exist"));
                    }
                }
            }
        }
    }
}