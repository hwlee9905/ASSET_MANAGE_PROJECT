package com.mcnc.bizmob.util;

import android.content.Context;

import com.mcnc.bizmob.core.util.res.RUtil;
import com.mcnc.securekeypad.input.CharsSecureKeypad;
import com.mcnc.securekeypad.input.NumericSecureKeypad;

/**
 * @author phanha_uy
 * @version 0.0.1, 2017-02-04
 */
public class SecureKeypadUtils {

    /*
    * getQwertyKeyboard
    *
    * @package Context context
    *
    * @return CharsSecureKeypad of @xml R.xml.secure_keypad_qwerty
    * */
    public static CharsSecureKeypad getQwertyKeyboard(Context context) {
        if (context == null) {
            return null;
        }
        return new CharsSecureKeypad(context, RUtil.getR(context,"xml","secure_keypad_qwerty"));
    }

    /*
  * getSymbolsKeyboard
  *
  * @package Context context
  *
  * @return CharsSecureKeypad of @xml R.xml.secure_keypad_symbols
  * */
    public static CharsSecureKeypad getSymbolsKeyboard(Context context) {
        if (context == null) {
            return null;
        }
        return new CharsSecureKeypad(context, RUtil.getR(context,"xml","secure_keypad_symbols"));
    }

    /*
    * getSymbolsShiftKeyboard
    *
    * @package Context context
    *
    * @return CharsSecureKeypad of @xml R.xml.secure_keypad_symbols_shift
    * */
    public static CharsSecureKeypad getSymbolsShiftKeyboard(Context context) {
        if (context == null) {
            return null;
        }
        return new CharsSecureKeypad(context, RUtil.getR(context,"xml","secure_keypad_symbols_shift"));
    }

    /*
    * getNumericKeypad
    *
    * @package Context context
    *
    * @return CharsSecureKeypad of @xml R.xml.secure_keypad_numeric
    * */
    public static NumericSecureKeypad getNumericKeypad(Context context) {
        if (context == null) {
            return null;
        }
        return new NumericSecureKeypad(context, RUtil.getR(context,"xml","secure_keypad_numeric"));
    }

}
