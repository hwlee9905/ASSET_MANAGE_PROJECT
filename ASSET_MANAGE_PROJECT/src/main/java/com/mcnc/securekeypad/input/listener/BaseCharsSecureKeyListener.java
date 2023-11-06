package com.mcnc.securekeypad.input.listener;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;

import com.mcnc.securekeypad.input.BaseSecureKeypad;
import com.mcnc.securekeypad.input.CharsSecureKeypad;

/**
 * @author phanha_uy
 * @version 0.0.3, 2016-12-26
 */
public abstract class BaseCharsSecureKeyListener extends BaseSecureKeyListener {

    /*Required Keyboards variable*/
    private CharsSecureKeypad mQwertyKeyboard, mSymbolsKeyboard, mSymbolsShiftKeyboard;

    /*State of Qwerty Keyboard*/
    private boolean isCape = false;
    /*State of Symbol Keyboard*/
    private boolean isSymbolKey = false;

    public BaseCharsSecureKeyListener(KeyboardView keyboardView, CharsSecureKeypad qwertyKeyboard, CharsSecureKeypad symbolsKeyboard, CharsSecureKeypad symbolsShiftKeyboard) {
        super(keyboardView);
        if (qwertyKeyboard == null || symbolsKeyboard == null || symbolsShiftKeyboard == null) {
            throw new IllegalArgumentException("Keyboards cannot be null");
        }
        this.mQwertyKeyboard = qwertyKeyboard;
        this.mSymbolsKeyboard = symbolsKeyboard;
        this.mSymbolsShiftKeyboard = symbolsShiftKeyboard;
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
                handleShift();
                break;
            case BaseSecureKeypad.KEYCODE_RE_ARRANGE:
                mQwertyKeyboard.reArrangeKeys();
                mSymbolsKeyboard.reArrangeKeys();
                mSymbolsShiftKeyboard.reArrangeKeys();
                getKeyboardView().invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                isSymbolKey = !isSymbolKey;
                if (isSymbolKey) {
                    getKeyboardView().setKeyboard(mSymbolsKeyboard);
                    mSymbolsKeyboard.setShifted(false);
                } else {
                    getKeyboardView().setKeyboard(mQwertyKeyboard);
                }
                getKeyboardView().invalidateAllKeys();
                break;
            case BaseSecureKeypad.KEYCODE_GAP:
                return;
            default:
                super.onKey(primaryCode, keyCodes);
                break;
        }
    }

    public void onCharCodeKey(char code) {
        this.switchShiftToNormal();
    }


    /**
     * Handle Shift Key Pressed
     * and Change Keyboard
     *
     * @see #getKeyboardView()
     */
    private void handleShift() {
        Keyboard currentKeyboard = getKeyboardView().getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            isCape = !isCape;
            mQwertyKeyboard.setShifted(isCape);
            getKeyboardView().invalidateAllKeys();
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            getKeyboardView().setKeyboard(mSymbolsShiftKeyboard);
            mSymbolsShiftKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftKeyboard) {
            mSymbolsKeyboard.setShifted(false);
            getKeyboardView().setKeyboard(mSymbolsKeyboard);
            mSymbolsShiftKeyboard.setShifted(false);
        }
    }

    /**
     * Should called when set visibility of KeyboardView
     */
    @Override
    public void onKeyboardVisible() {
        isSymbolKey = false;
        if (getKeyboardView() != null) {
            getKeyboardView().setKeyboard(mQwertyKeyboard);
            mQwertyKeyboard.setShifted(isCape ? isCape = !isCape : isCape);
            getKeyboardView().invalidateAllKeys();
            getKeyboardView().invalidateKey(getKeyboardView().getKeyboard().getShiftKeyIndex());
        }
    }

    private void switchShiftToNormal() {
        Keyboard currentKeyboard = this.getKeyboardView().getKeyboard();
        if(this.mQwertyKeyboard == currentKeyboard && this.isCape) {
            this.mQwertyKeyboard.setShifted(this.isCape = !this.isCape);
            this.getKeyboardView().invalidateAllKeys();
            this.getKeyboardView().invalidateKey(this.getKeyboardView().getKeyboard().getShiftKeyIndex());
        }

    }
}
