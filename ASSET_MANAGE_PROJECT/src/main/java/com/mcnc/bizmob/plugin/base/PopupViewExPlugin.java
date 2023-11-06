package com.mcnc.bizmob.plugin.base;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.config.AppConfig;
import com.mcnc.bizmob.core.util.res.ImageWrapper;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCWebFragment;
import com.mcnc.bizmob.core.view.webview.BMCWebView;
import com.mcnc.bizmob.view.popup.SlidePopupView;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 01.클래스 설명 : 확장 팝업뷰 기능 수행을 위한 플러그인. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 팝업 뷰 생성시 다양한 애니메이션 지원 <br>
 * 04.관련 API/화면/서비스 : SlidePopupView, BMCWebView, BMCWebFragment, AppConfig, ImageWrapper, RUtil<br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-12-05                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class PopupViewExPlugin extends BMCPlugin {

    /**
     * 클래스 명
     */
    private final String TAG = this.getClass().getName();

    /**
     * 현재 최상위 화면
     */
    private BMCWebFragment fView;

    /**
     * 팝업웹뷰
     */
    private BMCWebView popupWebView = null;


    @Override
    public void executeWithParam(JSONObject data) {
        try {
            JSONObject param = data.getJSONObject("param");
            JSONObject typeParam = param.getJSONObject("type_param");
            String type = "";
            String subType = "";
            String callback = "";

            if (param.has("type")) {
                type = param.getString("type");
            }

            if (param.has("sub_type")) {
                subType = param.getString("sub_type");
            }

            if (param.has("callback")) {
                callback = param.getString("callback");
            }

            if (type.equals("add_view")) {
                fView = (BMCWebFragment) getView();
                if (subType.equals("show")) {
                    if (!fView.showingPopupView) {
                        fView.showingPopupView = true;
                        showPopupview(typeParam, param);
                    }
                } else if (subType.equals("hide")) {
                    if (fView.showingPopupView) {
                        fView.showingPopupView = false;
                        hidePopupView(typeParam);
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void showPopupview(final JSONObject typeParam, final JSONObject param) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                LinearLayout main_view = (LinearLayout) fView.wrapper.findViewById(RUtil.getIdR(getActivity(), "main_view"));
                main_view.setClickable(false);

                RelativeLayout rl = (RelativeLayout) fView.wrapper.findViewById(RUtil.getIdR(getActivity(), "popup"));
                rl.setBackgroundColor(AppConfig.WEBVIEW_DIALOGOUTSIDECOLOR);

                //기본 값 setting
                boolean animation = true;
                boolean useHardwareAccelerator = true;
                String animationType = "push";
                String targetPage = "";
                int widthPercent = 100;
                int heightPercent = 100;
                int width = 0;
                int height = 0;
                int duration = 0;
                String direction = "left";

                //기본 값 setting
                try {
                    if (typeParam.has("width")) {
                        width = typeParam.getInt("width");
                    }
                    if (typeParam.has("height")) {
                        height = typeParam.getInt("height");
                    }
                    targetPage = typeParam.getString("target_page");

                    if (width <= 0) {
                        if (typeParam.has("width_percent")) {
                            widthPercent = typeParam.getInt("width_percent");
                            width = getSizePercent("width", widthPercent);
                        }
                    }

                    if (height <= 0) {
                        if (typeParam.has("height_percent")) {
                            heightPercent = typeParam.getInt("height_percent");
                            height = getSizePercent("height", heightPercent);

                        }
                    }

                    if (typeParam.has("base_size_orientation")) {
                        String baseSizeOrientation = param.getString("base_size_orientation");
                        if (baseSizeOrientation.equals("horizontal")) { //가로 모드이면 가로 세로 값을 바꾼다.
                            int temp = width;
                            width = height;
                            height = temp;
                        }
                    }


                    if (typeParam.has("animation")) {
                        animation = typeParam.getBoolean("animation");
                    }

                    if (typeParam.has("animation_type")) { // "push", "fade"
                        animationType = typeParam.getString("animation_type");
                    }

                    if (typeParam.has("duration")) {
                        duration = typeParam.getInt("duration");
                    }

                    if (typeParam.has("hardware_accelator")) {
                        useHardwareAccelerator = typeParam.getBoolean("hardware_accelator");
                    }

                    if (typeParam.has("direction")) {
                        direction = typeParam.getString("direction");
                    }

                    if (!targetPage.equals("")) {
                        String url = ImageWrapper.getUri(targetPage);
                        popupWebView = SlidePopupView.getInstance().getSlideView(direction, url, fView, typeParam);
                        popupWebView.isPopupWebView = true;
                        popupWebView.setBackgroundColor(ContextCompat.getColor(getActivity(), RUtil.getR(getActivity(), "color", "transparent")));
                        if (!useHardwareAccelerator) {
                            popupWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        }

                        fView.popupWebView = popupWebView;
                        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(width, height);
                        rl.addView(popupWebView, lp);

                        fView.getArguments().putString("data", param.toString());
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWebView != null) {
                            fView.backPressed();
                        }
                    }
                });


                rl.bringToFront();
                rl.setClickable(true);

                if (direction.equals("left")) {
                    rl.setGravity(Gravity.LEFT);
                } else if (direction.equals("right")) {
                    rl.setGravity(Gravity.RIGHT);
                } else if (direction.equals("top")) {
                    rl.setGravity(Gravity.TOP);
                } else if (direction.equals("bottom")) {
                    rl.setGravity(Gravity.BOTTOM);
                } else if (direction.equals("center")) {
                    rl.setHorizontalGravity(Gravity.CENTER_VERTICAL);
                    rl.setGravity(Gravity.CENTER);
                }

                rl.setVisibility(View.VISIBLE);
//                sc.setVisibility(View.VISIBLE);


                if (animation) {
                    final Animation animationObj = getAnimation(animationType, direction, true);
                    if (animationObj != null) {
                        animationObj.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                popupWebView.clearAnimation();

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        if (duration > 0) {
                            animationObj.setDuration(duration);
                        }
                        popupWebView.startAnimation(animationObj);

                    }
                }
            }
        });
    }

    private void hidePopupView(final JSONObject typeParam) {

        boolean animation = true;
        String animationType = "push";
        int duration = 0;
        String direction = "left";
        try {

            if (typeParam.has("animation")) {
                animation = typeParam.getBoolean("animation");
            }

            if (typeParam.has("animation_type")) {
                animationType = typeParam.getString("animation_type");
            }

            if (typeParam.has("duration")) {
                duration = typeParam.getInt("duration");
            }

            if (typeParam.has("direction")) {
                direction = typeParam.getString("direction");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (animation) {
            final Animation animationObj = getAnimation(animationType, direction, false);
            if (animationObj != null) {
                animationObj.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fView.popupWebView.clearAnimation();
                        closePopup(typeParam);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                if (duration > 0) {
                    animationObj.setDuration(duration);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fView.popupWebView.startAnimation(animationObj);
                    }
                });

            }
        } else {
            closePopup(typeParam);
            popupWebView = null;
        }


    }

    private void closePopup(JSONObject typeParam) {
        try {
            String callback = "";
            JSONObject message = new JSONObject();
            if (typeParam.has("message")) {
                message = typeParam.getJSONObject("message");
            }
            if (typeParam.has("callback")) {
                callback = typeParam.getString("callback");
            }
            if (fView != null) {
                fView.closePopupview(callback, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 애니메이션 생성
    private Animation getAnimation(String animType, String direction, Boolean isIn) {

        float fromXDelta = 0.0f;
        float toXDelta = 0.0f;
        float fromYDelta = 0.0f;
        float toYDelta = 0.0f;
        float fromAlpha = 0.0f;
        float toAlpha = 0.0f;

        Animation animation = null;

        if (isIn) { // animation 진행방향이 In인경우
            fromAlpha = 0.0f;
            toAlpha = 1.0f;
            if (direction.equalsIgnoreCase("center")) {
                fromYDelta = 1.0f;
                toYDelta = 0.0f;
            } else if (direction.equalsIgnoreCase("left")) {
                fromXDelta = -1.0f;
                toXDelta = 0.0f;
            } else if (direction.equalsIgnoreCase("right")) {
                fromXDelta = 1.0f;
                toXDelta = 0.0f;
            } else if (direction.equalsIgnoreCase("top")) {
                fromYDelta = -1.0f;
                toYDelta = 0.0f;
            } else if (direction.equalsIgnoreCase("bottom")) {
                fromYDelta = 1.0f;
                toYDelta = 0.0f;
            }
        } else { //animation 진행방향이 Out인경우
            fromAlpha = 1.0f;
            toAlpha = 0.0f;
            if (direction.equalsIgnoreCase("center")) {
                fromYDelta = 0.0f;
                toYDelta = 1.0f;
            } else if (direction.equalsIgnoreCase("left")) {
                fromXDelta = 0.0f;
                toXDelta = -1.0f;
            } else if (direction.equalsIgnoreCase("right")) {
                fromXDelta = 0.0f;
                toXDelta = 1.0f;
            } else if (direction.equalsIgnoreCase("top")) {
                fromYDelta = 0.0f;
                toYDelta = -1.0f;
            } else if (direction.equalsIgnoreCase("bottom")) {
                fromYDelta = 0.0f;
                toYDelta = 1.0f;
            }
        }

        if (animType.equalsIgnoreCase("push")) {
            animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, fromXDelta, Animation.RELATIVE_TO_PARENT, toXDelta, Animation.RELATIVE_TO_PARENT, fromYDelta, Animation.RELATIVE_TO_PARENT, toYDelta);
        } else if (animType.equalsIgnoreCase("fade")) {
            animation = new AlphaAnimation(fromAlpha, toAlpha);
        }

        return animation;
    }

    // 퍼센트로 받은 수치로 pixcel 사이즈를 구하는 메소드
    private int getSizePercent(String type, int percent) {

        int screen_width = 0;
        int screen_height = 0;

        // % 적용
        Configuration config = getActivity().getResources().getConfiguration();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);

        int statusBarHeight = 0;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        statusBarHeight = (int) Math.ceil(25 * getActivity().getResources().getDisplayMetrics().density);

        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            screen_width = displayMetrics.widthPixels;
            screen_height = displayMetrics.heightPixels - statusBarHeight;
        } else {
            screen_width = displayMetrics.heightPixels - statusBarHeight;
            screen_height = displayMetrics.widthPixels;
        }

        int returnValue = 0;

        if (type.equals("width")) {
            returnValue = screen_width * percent / 100;
        } else if (type.equals("height")) {
            returnValue = screen_height * percent / 100;
        }

        return returnValue;
    }
}
