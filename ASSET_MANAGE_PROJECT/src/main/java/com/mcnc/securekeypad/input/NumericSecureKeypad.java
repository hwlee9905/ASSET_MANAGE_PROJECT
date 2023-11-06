package com.mcnc.securekeypad.input;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author phanha_uy
 * @version 2016-10-19
 */
public class NumericSecureKeypad extends BaseSecureKeypad {

    public NumericSecureKeypad(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    @Override
    protected Keyboard.Key createKeyFromXml(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
        return new SecureKey(res, parent, x, y, parser);
    }

    /**
     * Re-Arrange Gaps Key by @see {@link #getRandomKeyCodes()}
     * and prepare the key for right position of keys
     */
    @Override
    public void reArrangeKeys() {
        List<Integer> numberKeyCodes = getRandomKeyCodes();

        int actualIndex = 0;
        for (int i = 0; i < getKeys().size(); i++) {
            Keyboard.Key key = getKeys().get(i);
            if (key.codes[0] >= 48 && key.codes[0] <= 57) {
                String number = numberKeyCodes.get(actualIndex).toString();
                key.codes[0] = Integer.valueOf(number.charAt(0));
                key.label = number;
                actualIndex += 1;
            }
        }
    }

    /**
     * Get Randomized KeyCodes
     * for {@Link reArrangeKeys()}
     *
     * @return list of {@link Integer} of randomized key codes
     */
    private List<Integer> getRandomKeyCodes() {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        return numbers;
    }
}
