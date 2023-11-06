package com.mcnc.securekeypad.input;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;


/**
 * @author phanha_uy
 * @version 0.0.2, 2016-12-26
 */
public abstract class BaseSecureKeypad extends Keyboard {

    /**
     * New Key Code for Additional Actions
     * <p>
     * KEYCODE_RE_ARRANGE the key to re arrange of gaps
     * KEYCODE_GAP non-action key
     */
    public static final int KEYCODE_RE_ARRANGE = -7;
    public static final int KEYCODE_GAP = -8;

    public String keyboardType = "text";

    public String getKeyboardType() {
        return keyboardType;
    }

    public void setKeyboardType(String keyboardType) {
        this.keyboardType = keyboardType;
    }

    public BaseSecureKeypad(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
        reArrangeKeys();
    }

    public BaseSecureKeypad(Context context, int xmlLayoutResId, int modeId, int width, int height) {
        super(context, xmlLayoutResId, modeId, width, height);
    }

    public BaseSecureKeypad(Context context, int xmlLayoutResId, int modeId) {
        super(context, xmlLayoutResId, modeId);
    }

    public BaseSecureKeypad(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    /**
     * Create Key From
     *
     * @return object of {@link SecureKey}
     */
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        return new SecureKey(res, parent, x, y, parser);
    }

    /**
     * In case to re arrange key gaps
     */
    public abstract void reArrangeKeys();
}
