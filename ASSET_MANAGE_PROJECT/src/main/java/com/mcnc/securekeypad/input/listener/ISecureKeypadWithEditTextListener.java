package com.mcnc.securekeypad.input.listener;

import android.inputmethodservice.KeyboardView;
import android.widget.EditText;

/**
 * Listener for CustomKeypad using with {@link EditText}
 */
public interface ISecureKeypadWithEditTextListener {

    void setEditText(EditText editText);

    EditText getEditText();

    KeyboardView.OnKeyboardActionListener getKeyboardActionListener();
}
