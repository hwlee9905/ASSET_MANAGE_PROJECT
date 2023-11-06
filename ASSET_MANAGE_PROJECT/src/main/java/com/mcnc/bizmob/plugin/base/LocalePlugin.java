package com.mcnc.bizmob.plugin.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;

import com.mcnc.bizmob.core.def.Def;
import com.mcnc.bizmob.core.plugin.BMCPlugin;
import com.mcnc.bizmob.core.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class LocalePlugin extends BMCPlugin {
    SharedPreferences pref;

    @Override
    protected void executeWithParam(JSONObject data) {
        // TODO Auto-generated method stub
        String callback = "";
        String type = "";

        JSONObject result = new JSONObject();
        try {
            JSONObject param = data.getJSONObject("param");
            if (param.has("callback")) {
                callback = param.getString("callback");
            }
            if (param.has("type")) {
                type = param.getString("type");
            }
            pref = getActivity().getSharedPreferences(
                    "localePref", Context.MODE_PRIVATE);
            if (type.equals("set")) {

                String locale = param.getString("locale");
                String lang = "";
                String country = "";

                if (locale.contains("_")) {
                    String splitStr[] = locale.split("_");
                    lang = splitStr[0];
                    country = splitStr[1];
                    country = country.toUpperCase();
                } else if (locale.contains("-")) {
                    String splitStr[] = locale.split("-");
                    lang = splitStr[0];
                    country = splitStr[1];
                    country = country.toUpperCase();
                } else {
                    lang = locale;
                }

                Locale newLocale = null;

                if (!lang.equals("") && !country.equals("")) {
                    newLocale = new Locale(lang, country);
                } else if (!lang.equals("")) {
                    newLocale = new Locale(lang);
                }

                if (!lang.equals("") && !country.equals("")) {
                    newLocale = new Locale(lang, country);
                } else if (!lang.equals("")) {
                    newLocale = new Locale(lang);
                }

                if (newLocale != null) {
                    //pref에 저장된 값은 "_"바로 표기된 값 이어야함.

                    Editor editor = pref.edit();
                    editor.putString("locale", newLocale.toString());

                    result.put("result", editor.commit());
                    result.put("locale", locale);

                    Configuration config = new Configuration();
                    config.locale = newLocale;

                    Def.LOCALE = newLocale.toString();

                    Logger.d("LocalePlugin", "reqeust locale = " + locale + ", Def.LOCALE = " + Def.LOCALE);

                    getActivity().getResources().updateConfiguration(config,
                            getActivity().getResources().getDisplayMetrics());
                } else {
                    result.put("result", false);
                    result.put("locale", pref.getString("locale", ""));
                }

            } else if (type.equals("get")) {

                String locale = pref.getString("locale", "");

                result.put("result", true);
                result.put("locale", locale);
            }

            listener.resultCallback("callback", callback, result);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                result.put("result", false);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            listener.resultCallback("callback", callback, result);
        }

    }

}
