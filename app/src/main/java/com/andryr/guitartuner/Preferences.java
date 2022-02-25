

package com.andryr.guitartuner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Preferences {
    public static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getPreferences(context).getBoolean(key, defaultValue);
    }

    public static String getString(Context context, String key, String defaultValue) {//получается какие то значения в зависимости от настроек, используется для имени строя
        return getPreferences(context).getString(key, defaultValue);
    }
}
