package io.cloink.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ThemePreferencesTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @After
    public void resetTheme() {
        ThemePreferences.saveThemeMode(context, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Test
    public void invalidIntegerFallsBackToSystemTheme() {
        context.getSharedPreferences(ThemePreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(ThemePreferences.THEME_MODE_KEY, 12345)
                .commit();

        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemePreferences.getThemeMode(context));
    }

    @Test
    public void legacyStringFallsBackToSystemTheme() {
        context.getSharedPreferences(ThemePreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(ThemePreferences.THEME_MODE_KEY, "dark")
                .commit();

        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemePreferences.getThemeMode(context));
    }

    @Test
    public void onlySupportedModesArePersisted() {
        assertTrue(ThemePreferences.saveThemeMode(context, AppCompatDelegate.MODE_NIGHT_YES));
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemePreferences.getThemeMode(context));
        assertTrue(ThemePreferences.saveThemeMode(context, AppCompatDelegate.MODE_NIGHT_NO));
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemePreferences.getThemeMode(context));
        assertFalse(ThemePreferences.saveThemeMode(context, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY));
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemePreferences.getThemeMode(context));
    }
}
