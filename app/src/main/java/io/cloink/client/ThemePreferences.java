package io.cloink.client;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import io.cloink.client.ui.theme.ThemeRuntime;

public final class ThemePreferences {
    static final String PREFERENCES_NAME = "settings";
    static final String THEME_MODE_KEY = "theme_mode";

    private ThemePreferences() {
    }

    public static int getThemeMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        int mode;
        try {
            mode = preferences.getInt(THEME_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } catch (ClassCastException ignored) {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        if (!isSupported(mode)) {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        Object storedMode = preferences.getAll().get(THEME_MODE_KEY);
        if (!(storedMode instanceof Integer) || ((Integer) storedMode) != mode) {
            preferences.edit().putInt(THEME_MODE_KEY, mode).commit();
        }
        return mode;
    }

    public static boolean saveThemeMode(Context context, int mode) {
        if (!isSupported(mode)) {
            return false;
        }
        boolean saved = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(THEME_MODE_KEY, mode)
                .commit();
        if (saved) {
            ThemeRuntime.update(mode);
        }
        return saved;
    }

    public static void applySavedTheme(Context context) {
        int mode = getThemeMode(context);
        ThemeRuntime.update(mode);
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    public static boolean isSupported(int mode) {
        return mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                || mode == AppCompatDelegate.MODE_NIGHT_NO
                || mode == AppCompatDelegate.MODE_NIGHT_YES;
    }
}
