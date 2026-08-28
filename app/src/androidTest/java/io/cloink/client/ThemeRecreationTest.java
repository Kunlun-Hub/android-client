package io.cloink.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.cloink.client.ui.PreferenceUI;
import io.cloink.client.ui.dialog.ComposeDialogs;

@RunWith(AndroidJUnit4.class)
public class ThemeRecreationTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @Before
    public void prepareApp() {
        PreferenceUI.setFirstLaunchDone(context);
        ThemePreferences.saveThemeMode(context, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @After
    public void resetTheme() {
        ThemePreferences.saveThemeMode(context, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Test
    public void advancedScreenSurvivesThemeChangesAndColdRelaunch() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            navigateToAdvanced(scenario);
            applyThemeAndAwaitRecreation(scenario, AppCompatDelegate.MODE_NIGHT_YES);
            applyThemeAndAwaitRecreation(scenario, AppCompatDelegate.MODE_NIGHT_NO);
            applyThemeAndAwaitRecreation(scenario, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        try (ActivityScenario<MainActivity> relaunched = ActivityScenario.launch(MainActivity.class)) {
            navigateToAdvanced(relaunched);
            relaunched.onActivity(activity -> {
                assertFalse(activity.isFinishing());
                ComposeDialogs.showUpdatePrompt(activity, "0.77.2", () -> { });
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            relaunched.onActivity(activity -> assertFalse(activity.isFinishing()));
        }
    }

    private void navigateToAdvanced(ActivityScenario<MainActivity> scenario) {
        scenario.onActivity(activity -> {
            NavHostFragment navHost = (NavHostFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHost == null) {
                throw new AssertionError("Main navigation host is missing");
            }
            if (navHost.getNavController().getCurrentDestination() == null
                    || navHost.getNavController().getCurrentDestination().getId() != R.id.nav_advanced) {
                navHost.getNavController().navigate(R.id.nav_advanced);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private void applyThemeAndAwaitRecreation(ActivityScenario<MainActivity> scenario, int mode) {
        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
            if (!ThemePreferences.saveThemeMode(activity, mode)) {
                throw new AssertionError("Unable to persist theme mode " + mode);
            }
            activity.getWindow().getDecorView().post(() -> AppCompatDelegate.setDefaultNightMode(mode));
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        scenario.onActivity(activity -> {
            assertFalse(activity.isFinishing());
            assertEquals(mode, ThemePreferences.getThemeMode(activity));
            Fragment navHost = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);
            assertFalse(navHost == null);
        });
    }
}
