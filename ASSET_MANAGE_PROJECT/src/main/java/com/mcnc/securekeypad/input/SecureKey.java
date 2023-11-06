package com.mcnc.securekeypad.input;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;

/**
 * @author phanha_uy
 * @version 2016-10-19
 */
public class SecureKey extends Keyboard.Key {



    public SecureKey(Keyboard.Row parent) {
        super(parent);
    }

    public SecureKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
        super(res, parent, x, y, parser);
    }

    private final static int[] KEY_STATE_NORMAL_ON = {
            android.R.attr.state_checkable,
            android.R.attr.state_checked
    };

    private final static int[] KEY_STATE_PRESSED_ON = {
            android.R.attr.state_pressed,
            android.R.attr.state_checkable,
            android.R.attr.state_checked
    };

    private final static int[] KEY_STATE_NORMAL_OFF = {
            android.R.attr.state_checkable
    };

    private final static int[] KEY_STATE_PRESSED_OFF = {
            android.R.attr.state_pressed,
            android.R.attr.state_checkable
    };

    private final static int[] KEY_STATE_FUNCTION = {
            android.R.attr.state_single
    };

    private final static int[] KEY_STATE_FUNCTION_PRESSED = {
            android.R.attr.state_pressed,
            android.R.attr.state_single
    };

    private final static int[] KEY_STATE_NORMAL = {
    };

    private final static int[] KEY_STATE_PRESSED = {
            android.R.attr.state_pressed
    };

    /**
     * Returns the drawable state for the key, based on the current state and type of the key.
     *
     * @return the drawable state of the key.
     * @see android.graphics.drawable.StateListDrawable#setState(int[])
     */
    @Override
    public int[] getCurrentDrawableState() {
        int[] states = KEY_STATE_NORMAL;

        if (on) {
            if (modifier) {
                if (pressed) {
                    states = KEY_STATE_FUNCTION_PRESSED;
                } else {
                    states = KEY_STATE_FUNCTION;
                }
            } else {
                if (pressed) {
                    states = KEY_STATE_PRESSED_ON;
                } else {
                    states = KEY_STATE_NORMAL_ON;
                }
            }
        } else {
            if (sticky) {
                if (pressed) {
                    states = KEY_STATE_PRESSED_OFF;
                } else {
                    states = KEY_STATE_NORMAL_OFF;
                }
            } else if (modifier) {
                if (pressed) {
                    states = KEY_STATE_FUNCTION_PRESSED;
                } else {
                    states = KEY_STATE_FUNCTION;
                }
            } else {
                if (pressed) {
                    states = KEY_STATE_PRESSED;
                } else {

                }
            }
        }
        return states;
    }
}
