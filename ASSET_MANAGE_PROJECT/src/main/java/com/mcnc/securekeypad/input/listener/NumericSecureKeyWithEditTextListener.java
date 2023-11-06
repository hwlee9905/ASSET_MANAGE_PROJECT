package com.mcnc.securekeypad.input.listener;

import android.inputmethodservice.KeyboardView;
import android.widget.EditText;

/**
 * @author phanha_uy
 * @version 0.0.0, 2017-02-04
 */
public class NumericSecureKeyWithEditTextListener extends BaseNumericSecureKeyListener implements ISecureKeypadWithEditTextListener {

    private EditText mEditText;

    public NumericSecureKeyWithEditTextListener(KeyboardView keyboardView, EditText editText) {
        super(keyboardView);
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
        return NumericSecureKeyWithEditTextListener.this;
    }

    /**
     * On Char Key
     * insert keyText to @see {@link #getEditText()}
     * with selection start
     * <p>
     * called when char key pressed {@see #onKey}
     */
    @Override
    public void onCharCodeKey(char code) {
        if (getEditText() != null) {
            int indexToInsert = getEditText().getSelectionStart();
            getEditText().getEditableText().insert(indexToInsert, String.valueOf(code));
        }
    }

    /**
     * On Delete Key
     * delete keyText from @see {@link #getEditText()}
     * with selection start
     * <p>
     * called when char key pressed {@see #onKey}
     */
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
