package com.mcnc.bizmob.plugin.base;

import android.graphics.Color;
import android.inputmethodservice.KeyboardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.log.Logger;
import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.bizmob.core.view.fragment.BMCWebFragment;

import com.mcnc.bizmob.util.SecureKeypadUtils;
import com.mcnc.bizmob.util.Utils;
import com.mcnc.bizmob.view.MainFragment;
import com.mcnc.securekeypad.input.BaseSecureKeypad;
import com.mcnc.securekeypad.input.CharsSecureKeypad;
import com.mcnc.securekeypad.input.NumericSecureKeypad;
import com.mcnc.securekeypad.input.listener.BaseCharsSecureKeyListener;
import com.mcnc.securekeypad.input.listener.BaseNumericSecureKeyListener;
import com.mcnc.securekeypad.input.listener.BaseSecureKeyListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author phanha_uy
 * @version 2017-02-07
 */
public class GetSecureKeypadPlugin extends BMCPlugin {

    private final String TAG = this.getClass().getSimpleName();

    private final String PARAM = "param";
    private final String INPUT_TYPE = "input_type";
    private final String KEY_TYPE = "key_type";
    private final String CHAR = "char";
    private final String DELETE = "delete";
    private final String CALLBACK = "callback";
    private final String RESULT = "result";
    private final String MESSAGE = "message";

    private String inputType = "text";
    private String returnCallback = "";

    @Override
    protected void executeWithParam(JSONObject data) {
        JSONObject resultData = new JSONObject();
        try {
            // This plugin just only for Web Client Page
            if (getFragment() instanceof BMCWebFragment) {
                JSONObject param = data.getJSONObject(PARAM);
                if (param.has(CALLBACK)) {
                    returnCallback = param.getString(CALLBACK);
                }
                if (data.getString("id").equals("SHOW_SECURE_KEYPAD")) {
                    showSecureKeypad(param);
                } else if (data.getString("id").equals("HIDE_SECURE_KEYPAD")) {
                    hideSecureKeypad();
                }
                return;
            } else {
                resultData.put(RESULT, false);
                resultData.put(MESSAGE, "This plugin is only for Web Client Page");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listener.resultCallback(CALLBACK, returnCallback, resultData);
    }

    /**
     * To show secure keypad view
     *
     * @param param the param of type of keypad
     * @see BaseSecureKeypad
     * @see #animateShowKeypads(View, KeyboardView)
     */
    private void showSecureKeypad(JSONObject param) throws JSONException {
        if (param.has(INPUT_TYPE)) {
            inputType = param.getString(INPUT_TYPE);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject resultData = new JSONObject();
                try {
                    if (getFragment() instanceof MainFragment) {
                        RelativeLayout fragmentRelativeLayout = (RelativeLayout) getFragment().getView();
                        final View webWrapper = fragmentRelativeLayout.findViewById(RUtil.getIdR(getActivity(), "wrapper"));

                        // If Secure keypad already added to main page, check if it's shown or not
                        if (((MainFragment) getFragment()).mSecureKeypad != null) {

                            BaseSecureKeypad keyboard = (BaseSecureKeypad) ((MainFragment) getFragment()).mSecureKeypad.getKeyboard();
                            KeyboardView keyboardView = ((MainFragment) getFragment()).mSecureKeypad;

                            // If SecureKeypad in the same page, show it if INVISIBLE or GONE
                            if (((MainFragment) getFragment()).mSecureKeypad.getParent().equals(fragmentRelativeLayout)) {
                                //현재 키보드 타입과 같은 경우에만 재활용.
                                if (keyboard.getKeyboardType().equals(inputType)) {
                                    // if keyboard view is invisible or gone, show it
                                    if (keyboardView.getVisibility() == View.GONE || keyboardView.getVisibility() == View.INVISIBLE) {
                                        animateShowKeypads(webWrapper, keyboardView);
                                    }
                                    // Reset keyboard callback
                                    if (((MainFragment) getFragment()).mSecureKeypadListener instanceof CharsSecureKeyListener) {
                                        ((CharsSecureKeyListener) ((MainFragment) getFragment()).mSecureKeypadListener).setCallback(returnCallback);
                                        ((CharsSecureKeyListener) ((MainFragment) getFragment()).mSecureKeypadListener).onKeyboardVisible();
                                    } else if (((MainFragment) getFragment()).mSecureKeypadListener instanceof NumericSecureKeyListener) {
                                        ((NumericSecureKeyListener) ((MainFragment) getFragment()).mSecureKeypadListener).setCallback(returnCallback);
                                        ((NumericSecureKeyListener) ((MainFragment) getFragment()).mSecureKeypadListener).onKeyboardVisible();
                                    }
                                    return;
                                } else {
                                    // 키보드 타입이 다른 경우 && 현재 보여지고 있으면, 숨기기
                                    if (keyboardView.getVisibility() == View.VISIBLE) {
                                        hideSecureKeypad();
                                    }
                                }
                            }
                        }

                        // Else, initialize new SecureKeypad for the client page
                        KeyboardView keyboardView;
                        KeyboardView.OnKeyboardActionListener keyboardActionListener;

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        if (inputType.equals("number")) {
                            // if input type is number, then inflate number keyboard
                            View rootView = inflater.inflate(RUtil.getLayoutR(getActivity(), "layout_secure_keypad_number"), null);
                            keyboardView = (KeyboardView) rootView.findViewById(RUtil.getIdR(getActivity(), "keyboard_number"));

                            NumericSecureKeypad numericKeypad = SecureKeypadUtils.getNumericKeypad(getActivity());
                            numericKeypad.setKeyboardType("number");

                            keyboardView.setKeyboard(numericKeypad);
                            keyboardActionListener = getNumericKeyListener(keyboardView);
                        } else {
                            // else, inflate text keyboard
                            View rootView = inflater.inflate(RUtil.getLayoutR(getActivity(), "layout_secure_keypad_text"), null);
                            keyboardView = (KeyboardView) rootView.findViewById(RUtil.getIdR(getActivity(), "keyboard_text"));

                            // Declare required keypads for CharSecureKeypad
                            CharsSecureKeypad qwertyKeypad = SecureKeypadUtils.getQwertyKeyboard(getActivity());
                            CharsSecureKeypad symbolKeypad = SecureKeypadUtils.getSymbolsKeyboard(getActivity());
                            CharsSecureKeypad symbolShiftKeypad = SecureKeypadUtils.getSymbolsShiftKeyboard(getActivity());
                            qwertyKeypad.setKeyboardType("text");

                            keyboardView.setKeyboard(qwertyKeypad);
                            keyboardActionListener = getCharsSecureKeyListener(keyboardView, qwertyKeypad, symbolKeypad, symbolShiftKeypad);
                        }
                        int padding = (int) Utils.getPixelOfDpi(getActivity(), 5);

                        // Get View from Fragment page to add keyboard
                        fragmentRelativeLayout.addView(keyboardView);
                        keyboardView.setPadding(0, padding, 0, padding);
                        keyboardView.setOnKeyboardActionListener(keyboardActionListener);

                        // Set layout position to Keypad
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        keyboardView.setLayoutParams(layoutParams);

                        animateShowKeypads(webWrapper, keyboardView);
                        ((MainFragment) getFragment()).mSecureKeypad = keyboardView;
                        ((MainFragment) getFragment()).mSecureKeypadListener = keyboardActionListener;
                        return;
                    }

                    // If it reached here, it means failed to open SecureKeypad
                    resultData.put(RESULT, false);
                    resultData.put(MESSAGE, "Failed to show SecureKeypad");
                } catch (JSONException e) {
                    Logger.e(TAG, e.getMessage());
                } catch (Exception e1) {
                    Logger.e(TAG, e1.getMessage());
                }
                listener.resultCallback(CALLBACK, returnCallback, resultData);
            }
        });
    }

    /**
     * Animate Show Keypads
     *
     * @param keyboardView the keyboard view to be animate
     */
    private void animateShowKeypads(final View webWrapper, KeyboardView keyboardView) {
        if (keyboardView != null && keyboardView.getVisibility() != View.VISIBLE) {

            Animation pushUpIn = AnimationUtils.loadAnimation(getActivity(), RUtil.getR(getActivity(), "anim", "anim_secure_keypad_push_up_in"));
            keyboardView.startAnimation(pushUpIn);
            keyboardView.setVisibility(View.VISIBLE);

//            keyboardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    int keypadHeight = ((MainFragment) getFragment()).mSecureKeypad.getHeight();
//
//                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) webWrapper.getLayoutParams();
//                    marginLayoutParams.bottomMargin = keypadHeight;
//                    webWrapper.setLayoutParams(marginLayoutParams);
//
//                    ((MainFragment) getFragment()).mSecureKeypad.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                }
//            });

            if (getFragment() instanceof MainFragment) {
                BaseSecureKeyListener keyListener = (BaseSecureKeyListener) ((MainFragment) getFragment()).mSecureKeypadListener;
                if (keyListener != null) {
                    keyListener.onKeyboardVisible();
                }
            }

            if (keyboardView.getKeyboard() instanceof BaseSecureKeypad) {
                ((BaseSecureKeypad) keyboardView.getKeyboard()).reArrangeKeys();
                keyboardView.invalidateAllKeys();
            }

            JSONObject resultData = new JSONObject();

            try {
                resultData.put(RESULT, true);
                resultData.put(MESSAGE, "Successful to show SecureKeypad");
            } catch (JSONException e) {
                Logger.e(TAG, e.getMessage());
            }

            listener.resultCallback(CALLBACK, returnCallback, resultData);

            // prevent screen capture
            Utils.preventScreenShot(getActivity());
        }
    }

    /**
     * hideSecureKeypad
     *
     * @see #animateShowKeypads(View, KeyboardView)
     */
    private void hideSecureKeypad() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject resultData = new JSONObject();
                try {
                    if (getFragment() instanceof MainFragment) {
                        RelativeLayout fragmentRelativeLayout = (RelativeLayout) getFragment().getView();
                        // If Secure keypad already added to main page, check if it's shown or not
                        if (((MainFragment) getFragment()).mSecureKeypad != null) {

                            // If SecureKeypad in the same page, show it if INVISIBLE or GONE
                            if (((MainFragment) getFragment()).mSecureKeypad.getParent().equals(fragmentRelativeLayout)) {

                                KeyboardView keyboardView = ((MainFragment) getFragment()).mSecureKeypad;

                                if (keyboardView.getVisibility() == View.VISIBLE) {

                                    final View webWrapper = fragmentRelativeLayout.findViewById(RUtil.getIdR(getActivity(), "wrapper"));

                                    keyboardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) webWrapper.getLayoutParams();
                                            marginLayoutParams.bottomMargin = 0;
                                            webWrapper.setLayoutParams(marginLayoutParams);

                                            ((MainFragment) getFragment()).mSecureKeypad.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                        }
                                    });

                                    Animation pushUpOut = AnimationUtils.loadAnimation(getActivity(), RUtil.getR(getActivity(), "anim", "anim_secure_keypad_push_up_out"));
                                    keyboardView.startAnimation(pushUpOut);
                                    keyboardView.setVisibility(View.GONE);
                                    resultData.put(RESULT, true);
                                    resultData.put(MESSAGE, "Successful hide Secure Keypad");
                                    listener.resultCallback(CALLBACK, returnCallback, resultData);

                                    // allow screen capture
                                    Utils.allowScreenShot(getActivity());
                                    return;
                                }
                            }
                        }
                    }
                    resultData.put(RESULT, false);
                    resultData.put(MESSAGE, "There is no Secure Keypad to close.");
                } catch (JSONException e) {
                    Logger.e(TAG, e.getMessage());
                }
                listener.resultCallback(CALLBACK, returnCallback, resultData);
            }
        });
    }

    /**
     * getCharsSecureKeyListener
     *
     * @param keyboardView      the keyboard view to display
     * @param qwertyKeyboard    the keyboard of secure_keypad_qwerty type
     * @param symbolKeypad      the keyboard of symbol type
     * @param symbolShiftKeypad the keyboard of shift symbol type
     * @return BaseCharsSecureKeyListener
     * @see SecureKeypadUtils
     * @see BaseCharsSecureKeyListener
     */
    private CharsSecureKeyListener getCharsSecureKeyListener(KeyboardView keyboardView, CharsSecureKeypad qwertyKeyboard,
                                                             CharsSecureKeypad symbolKeypad, CharsSecureKeypad symbolShiftKeypad) {
        return new CharsSecureKeyListener(keyboardView, qwertyKeyboard, symbolKeypad, symbolShiftKeypad, returnCallback);
    }

    /**
     * BaseNumericSecureKeyListener
     *
     * @param keyboardView the keyboard view to display
     * @return BaseNumericSecureKeyListener
     * @see BaseCharsSecureKeyListener
     */
    private NumericSecureKeyListener getNumericKeyListener(KeyboardView keyboardView) {
        return new NumericSecureKeyListener(keyboardView, returnCallback);
    }

    private class CharsSecureKeyListener extends BaseCharsSecureKeyListener {
        private String mCallback;

        public CharsSecureKeyListener(KeyboardView keyboardView, CharsSecureKeypad qwertyKeyboard,
                                      CharsSecureKeypad symbolsKeyboard, CharsSecureKeypad symbolsShiftKeyboard, String callback) {
            super(keyboardView, qwertyKeyboard, symbolsKeyboard, symbolsShiftKeyboard);
            this.mCallback = callback;
        }

        public void setCallback(String callback) {
            this.mCallback = callback;
        }

        @Override
        public void onCharCodeKey(char code) {
            super.onCharCodeKey(code);
            JSONObject data = new JSONObject();
            try {
                data.put(RESULT, true);
                data.put(KEY_TYPE, CHAR);
                data.put(CHAR, String.valueOf(code));
            } catch (JSONException e) {
                Logger.e(TAG, e.getMessage());
            } finally {
                listener.resultCallback(CALLBACK, mCallback, data);
            }
        }

        @Override
        public void onDeleteKey() {
            JSONObject data = new JSONObject();
            try {
                data.put(RESULT, true);
                data.put(KEY_TYPE, DELETE);
            } catch (JSONException e) {
                Logger.e(TAG, e.getMessage());
            }
            listener.resultCallback(CALLBACK, mCallback, data);
        }

        @Override
        public void onKeyboardVisible() {
            super.onKeyboardVisible();
        }
    }

    private class NumericSecureKeyListener extends BaseNumericSecureKeyListener {
        private String mCallback;

        public NumericSecureKeyListener(KeyboardView keyboardView, String callback) {
            super(keyboardView);
            this.mCallback = callback;
        }

        public void setCallback(String callback) {
            this.mCallback = callback;
        }

        @Override
        public void onCharCodeKey(char code) {
            JSONObject data = new JSONObject();
            try {
                data.put(RESULT, true);
                data.put(KEY_TYPE, CHAR);
                data.put(CHAR, String.valueOf(code));
            } catch (JSONException e) {
                Logger.e(TAG, e.getMessage());
            } finally {
                listener.resultCallback(CALLBACK, mCallback, data);
            }
        }

        @Override
        public void onDeleteKey() {
            JSONObject data = new JSONObject();
            try {
                data.put(RESULT, true);
                data.put(KEY_TYPE, DELETE);
            } catch (JSONException e) {
                Logger.e(TAG, e.getMessage());
            }
            listener.resultCallback(CALLBACK, mCallback, data);
        }
    }
}