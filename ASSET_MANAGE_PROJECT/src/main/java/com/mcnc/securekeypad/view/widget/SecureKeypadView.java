package com.mcnc.securekeypad.view.widget;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import com.mcnc.securekeypad.input.BaseSecureKeypad;

/**
 * @author phanha_uy
 * @version 0.02, 2016-12-26
 */
public class SecureKeypadView extends KeyboardView {

    public SecureKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SecureKeypadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setKeyboard(Keyboard keyboard) {
        super.setKeyboard(keyboard);
    }

    /**
     * Override {@Link #super.invalidateKey}
     * <p>
     * by specialize with shift key
     */
    @Override
    public void invalidateKey(int keyIndex) {
        if (keyIndex >= 0 && keyIndex < getKeyboard().getKeys().size()) {

            Key key = getKeyboard().getKeys().get(keyIndex);
            if (key.codes[0] == Keyboard.KEYCODE_DELETE
                    || key.codes[0] == BaseSecureKeypad.KEYCODE_GAP
                    || key.codes[0] == BaseSecureKeypad.KEYCODE_SHIFT
                    || key.codes[0] == BaseSecureKeypad.KEYCODE_MODE_CHANGE
                    || key.codes[0] == BaseSecureKeypad.KEYCODE_RE_ARRANGE
                    || key.codes[0] == 32) {
                setPreviewEnabled(false);
                setPopupOffset(0, 0);
            } else {
                setPreviewEnabled(true);
            }

            if (key.icon != null) {
                if (key.pressed) {
                    key.icon.setState(new int[]{android.R.attr.state_pressed});
                } else {
                    key.icon.setState(new int[]{});
                    if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
                        if (getKeyboard().isShifted()) {
                            key.icon.setState(new int[]{android.R.attr.state_selected});
                        }
                    }
                }
            }
        }
        super.invalidateKey(keyIndex);
    }
}
