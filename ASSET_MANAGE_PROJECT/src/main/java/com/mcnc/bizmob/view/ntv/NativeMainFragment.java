package com.mcnc.bizmob.view.ntv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.mcnc.bizmob.core.application.BMCInit;
import com.mcnc.bizmob.core.def.ActivityRequestCode;
import com.mcnc.bizmob.core.manager.BMCManager;
import com.mcnc.bizmob.core.plugin.CompleteListener;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class NativeMainFragment extends BMCNativeFragment {

    private final String TAG = this.toString();

    boolean touched;

    //Fragment LifeCycle 시작, Fragment가 Activity(SlideFragmentActivity)에 붙을 때 호출 된다.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //UI 작업 불가.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //Fragment 재시작 start Point (Layout을 inflater하여 View작업을 하는 곳)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        wrapper = inflater.inflate(RUtil.getLayoutR(getActivity(), "your xml name here!"), null);
        return wrapper;
    }

    //UI 작업 가능, (Activity에서 Fragment를 모두 생성하고 난다음 호출 된다. Activity의 onCreate()에서 setContentView()한 다음과 같음)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    //Fragment가 화면에 표시될 때 호출된다. 사용자의 Action과 상호 작용 할 수 없다.
    @Override
    public void onStart() {
        super.onStart();
    }

    //Fragment가 완전히 화면에 그려진 상태, 사용자의 Action과 상호 작용이 가능하다.
    @Override
    public void onResume() {
        super.onResume();
        BMCManager.getInstance().setFragment(this);
    }

    //Fragment가 사용자의 Action과 상호 작용을 중지한다.
    @Override
    public void onPause() {
        super.onPause();
    }

    //Fragment가 화면에서 더이상 보여지지 않게 되며, Fragment기능이 중지 되었을때 호출 된다.
    //View 리소스를 해제 할수 있도록 호출된다.
    //backstack을 사용 했다면 Fragment를 다시 돌아 갈때 onCreateView()가 호출 된다.
    @Override
    public void onStop() {
        super.onStop();
    }


    //Fragment 재시작 Point here to onCreateView()
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    //Fragment상태를 완전히 종료 할 수 있도록 호출 한다.
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //Fragment가 Activity와 연결이 완전히 끊기기 직전에 호출 된다.
    @Override
    public void onDetach() {
        super.onDetach();
    }

    //onSaveInstanceState()
    //Activity와 동일하게 Fragment가 사라질떄 호출되며 상태를 Bundle로 저장할수 있도록 호출 된다.

    @Override
    public boolean dispatchTouchEvent(Window.Callback cb, MotionEvent event) {

        int touchCount = event.getPointerCount();

        if (touchCount == 3 && touched == false) {
            if (BMCInit.BUILD_MODE.equals("dev")) {
                touched = true;
                try {
                    JSONObject json = new JSONObject();
                    json.put("isDev", true);
                    BMCManager manager = BMCManager.getInstance();
                    manager.executeInterfaceFromID("SHOW_SETTING_VIEW", json, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return cb.dispatchTouchEvent(event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ActivityRequestCode.NETWORK_SETTING) {
            touched = false;
            if (resultCode == Activity.RESULT_OK) {
                String type = data.getStringExtra("type");
                if (type.equals("exit")) {
                    try {
                        JSONObject message = new JSONObject();
                        JSONObject param = new JSONObject();

                        message.put("param", param);
                        param.put("kill_type", "exit");

                        BMCManager.getInstance().executeInterfaceFromID("APPLICATION_EXIT", message, new CompleteListener() {

                            @Override
                            public void resultCallback(String type, String callback, JSONObject param) {

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (type.equals("restart")) {

                    try {

                        JSONObject message = new JSONObject();
                        JSONObject param = new JSONObject();

                        message.put("param", param);
                        param.put("kill_type", "restart");

                        BMCManager.getInstance().executeInterfaceFromID("APPLICATION_EXIT", message, new CompleteListener() {

                            @Override
                            public void resultCallback(String type, String callback, JSONObject param) {

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (type.equals("refresh")) {
                    BMCFragment bmcFragment = BMCInit.getFragmentList().get(BMCInit.getFragmentList().size() - 1);
                    bmcFragment.onResume();
                }
            }
        }
    }
}
