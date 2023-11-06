package com.mcnc.securekeypad.input;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * @author phanha_uy
 * @version 0.0.2, 2016-12-27
 */
public class CharsSecureKeypad extends BaseSecureKeypad {

    /**
     * Variable for random key gaps
     */
    private HashMap<Integer, List<Key>> mMapKeysToRandomGap;
    private HashMap<Integer, List<Key>> mMapKeysGaps;

    public CharsSecureKeypad(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    /**
     * Map of Keys to random gaps
     *
     * @return object of randomized keys of {@link HashMap<Integer, List<Key>>}
     */
    public HashMap<Integer, List<Key>> getMapKeysToRandomGap() {
        if (mMapKeysToRandomGap == null) {
            mMapKeysToRandomGap = new HashMap<>();
        }
        return mMapKeysToRandomGap;
    }

    /**
     * Get Map of Gaps Key
     *
     * @return object of {@link HashMap<Integer, List<Key>>}
     */
    public HashMap<Integer, List<Key>> getMapKeysGaps() {
        if (mMapKeysGaps == null) {
            mMapKeysGaps = new HashMap<>();
        }
        return mMapKeysGaps;
    }

    /**
     * Create Key from XML and Store to @see {@link #mMapKeysToRandomGap}
     * and {@Link #mMapKeysGaps}
     */
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        SecureKey key = new SecureKey(res, parent, x, y, parser);
        if (key.edgeFlags != Keyboard.EDGE_LEFT && !key.modifier) {
            if (!getMapKeysToRandomGap().containsKey(y)) {
                getMapKeysToRandomGap().put(y, new ArrayList<Key>());
            }
            if (key.codes[0] == BaseSecureKeypad.KEYCODE_GAP) {
                if (!getMapKeysGaps().containsKey(y)) {
                    getMapKeysGaps().put(y, new ArrayList<Key>());
                }
                getMapKeysGaps().get(y).add(key);
            } else {
                getMapKeysToRandomGap().get(y).add(key);
            }
        }
        return key;
    }

    /**
     * Re-Arrange Gaps Key by @see {@link #getGapKeys()}
     * and prepare the key for right position of keys
     */
    @Override
    public void reArrangeKeys() {
        // last key uses for start x of next key
        Key lastKey = null;
        int currentY = getKeys().get(0).y;
        int currentGapIndex = 0;

        HashMap<Integer, List<Key>> gapRandomKeys = getGapKeys();

        for (Key key : getKeys()) {
            if (currentY != key.y) {
                // New row
                lastKey = null;
                currentY = key.y;
                currentGapIndex = 0;
            }

            if (lastKey != null && key.codes[0] != KEYCODE_GAP) {
                key.x = lastKey.x + lastKey.width;
            }

            if (gapRandomKeys.get(key.y) != null && gapRandomKeys.get(key.y).contains(key)) {
                if (getMapKeysGaps().get(key.y) != null && getMapKeysGaps().get(key.y).size() > currentGapIndex) {
                    Key gapKey = getMapKeysGaps().get(key.y).get(currentGapIndex);

                    gapKey.x = key.x;
                    key.x = gapKey.x + gapKey.width;
                    currentGapIndex += 1;
                }
            }
            if (key.codes[0] != KEYCODE_GAP) {
                lastKey = key;
            }
        }
    }

    /**
     * Get Gaps key
     *
     * @return map of randomized gap keys
     */
    private HashMap<Integer, List<Key>> getGapKeys() {
        HashMap<Integer, List<Key>> gapKey = new HashMap<>();

        for (Integer key : getMapKeysToRandomGap().keySet()) {
            List<Key> keys = getMapKeysToRandomGap().get(key);
            Collections.shuffle(keys);
            gapKey.put(key, keys.subList(0, getMapKeysGaps().get(key) == null ? 1 : getMapKeysGaps().get(key).size()));
        }

        return gapKey;
    }
}
