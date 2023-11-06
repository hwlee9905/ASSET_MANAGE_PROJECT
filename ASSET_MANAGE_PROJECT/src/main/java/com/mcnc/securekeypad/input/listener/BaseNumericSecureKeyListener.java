package com.mcnc.securekeypad.input.listener;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;

import com.mcnc.securekeypad.input.BaseSecureKeypad;

/**
 * @author phanha_uy
 * @version 2016-10-25
 */
public abstract class BaseNumericSecureKeyListener extends BaseSecureKeyListener {

    public BaseNumericSecureKeyListener(KeyboardView keyboardView) {
        super(keyboardView);
    }

    /**
     * On Key
     * <p>
     * Key is pressed
     */
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        switch (primaryCode) {
            case Keyboard.KEYCODE_SHIFT:
                break;
            case BaseSecureKeypad.KEYCODE_RE_ARRANGE:
                if (getKeyboardView().getKeyboard() instanceof BaseSecureKeypad) {
                    ((BaseSecureKeypad) getKeyboardView().getKeyboard()).reArrangeKeys();
                    getKeyboardView().invalidateAllKeys();
                }
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                break;
            default:
                super.onKey(primaryCode, keyCodes);
                break;
        }
    }

    @Override
    public void onKeyboardVisible() {
        super.onKeyboardVisible();
    }
}
