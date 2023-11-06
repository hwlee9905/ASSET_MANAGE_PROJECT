package com.mcnc.securekeypad.input.listener;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;

import com.mcnc.securekeypad.input.BaseSecureKeypad;

/**
 * @author phanha_uy
 * @version 2016-10-25
 */
public abstract class BaseSecureKeyListener implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView mKeyboardView;
    private boolean isPressed = false;


    public BaseSecureKeyListener(KeyboardView keyboardView) {
        this.mKeyboardView = keyboardView;
    }

    public KeyboardView getKeyboardView() {
        return mKeyboardView;
    }

    /**
     * On Key
     * <p>
     * Key is pressed
     */
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                onDeleteKey();
                break;
            default:
                char code = (char) primaryCode;
                if (getKeyboardView() != null && getKeyboardView().isShifted()) {
                    code = Character.toUpperCase(code);
                }
                onCharCodeKey(code);
                break;
        }
    }

    /**
     * On Char Key
     * <p>
     * called when char key pressed {@see #onKey}
     */
    public abstract void onCharCodeKey(char code);

    /**
     * On Char Key
     * <p>
     * called when delete key pressed {@see #onKey}
     */
    public abstract void onDeleteKey();

    @Override
    public void onPress(int primaryCode) {
        isPressed = true;
        if (primaryCode == Keyboard.KEYCODE_DELETE
                || primaryCode == BaseSecureKeypad.KEYCODE_GAP
                || primaryCode == BaseSecureKeypad.KEYCODE_SHIFT
                || primaryCode == BaseSecureKeypad.KEYCODE_MODE_CHANGE
                || primaryCode == BaseSecureKeypad.KEYCODE_RE_ARRANGE
                || primaryCode == 32) {
            getKeyboardView().setPreviewEnabled(false);
        } else {
            getKeyboardView().setPreviewEnabled(true);
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        isPressed = false;
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!BaseSecureKeyListener.this.isPressed) {
                    BaseSecureKeyListener.this.getKeyboardView().setPopupOffset(0, 0);
                }
            }
        }, 100L);
        getKeyboardView().setPreviewEnabled(false);
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeUp() {
    }

    /**
     * Should called when set visibility of KeyboardView
     */
    public void onKeyboardVisible() {
    }
}
