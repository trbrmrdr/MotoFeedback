package me.motofeedback.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;

/**
 * Created by admin on 30.07.2016.
 */
public class BaseSettings {

    protected Context mContext;

    private static final String RESOURCE_STRING = "string";
    private static final String RESOURCE_DRAWABLE = "drawable";
    private static final String RESOURCE_MIPMAP = "mipmap";
    private static final String RESOURCE_ANIM = "anim";
    private static final String RESOURCES_INTEGER = "integer";
    private static final String RESOURCES_BOOLEAN = "bool";

    private static final String VERSION_TYPE = "Settings.VERSION_TYPE_1";

    public BaseSettings(final Context context) {
        mContext = context;
    }

    public int getStringId(final String key) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return getResourceId(RESOURCE_STRING, key);
    }

    public int getIntegerId(final String key) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return getResourceId(RESOURCES_INTEGER, key);
    }

    public int getBooleanId(final String key) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return getResourceId(RESOURCES_BOOLEAN, key);
    }

    public int getAnimId(final String key) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return getResourceId(RESOURCE_ANIM, key);
    }

    public int getDrawableId(final String key) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return getResourceId(RESOURCE_DRAWABLE, key);
    }

    public int getMipmapId(final String key) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return getResourceId(RESOURCE_MIPMAP, key);
    }

    public int getDrawableId(final String key, int defValue) {
        int ret;
        try {
            ret = getResourceId(RESOURCE_DRAWABLE, key);
        } catch (Exception e) {
            ret = defValue;
        }
        return ret;
    }

    public int getResourceId(final String type, final String key) throws NoSuchFieldException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        return getResourceId(mContext, type, key);
    }

    public static int getResourceId(final Context context, final String type, final String key) throws NoSuchFieldException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        String rc = context.getPackageName() + ".R$" + type;
        Field f = Class.forName(rc).getField(key);
        return f.getInt(null);
    }

    //#################################################################

    public String getSetting(final String key, String defValue) {
        String ret = defValue;
        try {
            ret = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE).getString(key, defValue);
        } catch (Exception e) {
            setSetting(key, defValue);
        }
        return ret;
    }

    public int getSetting(final String key, int defValue) {
        int ret = defValue;
        try {
            ret = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE).getInt(key, defValue);
        } catch (Exception e) {
            setSetting(key, defValue);
        }
        return ret;
    }

    public long getSetting(final String key, long defValue) {
        long ret = defValue;
        try {
            ret = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE).getLong(key, defValue);
        } catch (Exception e) {
            setSetting(key, defValue);
        }
        return ret;
    }

    public float getSetting(final String key, float defValue) {
        float ret = defValue;
        try {
            return mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE).getFloat(key, defValue);
        } catch (Exception e) {
            setSetting(key, defValue);
        }
        return ret;
    }

    public boolean getSetting(final String key, boolean defValue) {
        boolean ret = defValue;
        try {
            ret = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE).getBoolean(key, defValue);
        } catch (Exception e) {
            setSetting(key, defValue);
        }
        return ret;
    }

    public boolean setSetting(final String key, final String value) {
        SharedPreferences prefs = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (value != null && value.length() != 0)
            editor.putString(key, value);
        else
            editor.remove(key);
        return editor.commit();
    }

    public boolean setSetting(final String key, final int value) {
        SharedPreferences prefs = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean setSetting(final String key, final float value) {
        SharedPreferences prefs = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    public boolean setSetting(final String key, final boolean value) {
        SharedPreferences prefs = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public boolean setSetting(final String key, final long value) {
        SharedPreferences prefs = mContext.getSharedPreferences(VERSION_TYPE, mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        return editor.commit();
    }
}
