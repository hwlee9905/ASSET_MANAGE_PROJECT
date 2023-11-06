package com.mcnc.securekeypad.input.listener;

import android.inputmethodservice.KeyboardView;
import android.widget.EditText;

import com.mcnc.securekeypad.input.CharsSecureKeypad;

/**
 * @author phanha_uy
 * @version 0.0.0, 2017-02-04
 */
public class CharsSecureKeyWithEditTextListener extends BaseCharsSecureKeyListener implements ISecureKeypadWithEditTextListener {

    private EditText mEditText;

    public CharsSecureKeyWithEditTextListener(KeyboardView keyboardView, CharsSecureKeypad qwertyKeyboard, CharsSecureKeypad symbolsKeyboard, CharsSecureKeypad symbolsShiftKeyboard, EditText editText) {
        super(keyboardView, qwertyKeyboard, symbolsKeyboard, symbolsShiftKeyboard);
        this.mEditText = editText;
    }

    @Override
    public void setEditText(EditText mEditText) {
        this.mEditText = mEditText;
    }

    @Override
    public EditText getEditText() {
        return mEditText;
    }

    @Override
    public KeyboardView.OnKeyboardActionListener getKeyboardActionListener() {
        return CharsSecureKeyWithEditTextListener.this;
    }

    @Override
    public void onCharCodeKey(char code) {
        if (getEditText() != null) {
            int indexToInsert = getEditText().getSelectionStart();
            getEditText().getEditableText().insert(indexToInsert, String.valueOf(code));
        }
    }

    @Override
    public void onDeleteKey() {
        if (getEditText() != null) {
            if (getEditText().length() > 0) {
                int indexSelectionStart = getEditText().getSelectionStart();
                int indexSelectionEnd = getEditText().getSelectionEnd();

                if (indexSelectionEnd == indexSelectionStart) {
                    getEditText().getEditableText().delete(indexSelectionStart - (indexSelectionStart == 0 ? 0 : 1), indexSelectionEnd);
                } else {
                    getEditText().getEditableText().delete(indexSelectionStart, indexSelectionEnd);
                }
            }
        }
    }
}
