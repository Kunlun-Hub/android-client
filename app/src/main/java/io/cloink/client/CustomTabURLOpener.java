package io.cloink.client;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import io.cloink.gomobile.android.URLOpener;

public class CustomTabURLOpener implements URLOpener {
    private static final String TAG = "CustomTabURLOpener";
    private final AppCompatActivity context;
    private final ActivityResultLauncher<Intent> customTabLauncher;
    private final OnLaunchFailure launchFailureCallback;

    private boolean isOpened = false;

    public interface OnLaunchFailure {
        void onLaunchFailed();
    }

    public CustomTabURLOpener(AppCompatActivity activity, OnLaunchFailure launchFailureCallback) {
        this.context = activity;
        this.launchFailureCallback = launchFailureCallback;

        this.customTabLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), o -> {
                    isOpened = false;
                    // Returning from a Custom Tab is not an authentication
                    // cancellation signal. On Android 16 this callback can run
                    // before MainActivity.onNewIntent receives the OAuth URI.
                    Log.d(TAG, "Custom Tab returned; waiting for OAuth callback or flow timeout");
                }
        );
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void onCallbackReceived() {
        isOpened = false;
    }

    @Override
    public void onLoginSuccess() {
        Log.d(TAG, "onLoginSuccess fired.");

        if (isOpened) {
            Intent i = new Intent(this.context, MainActivity.class);
            i.setAction(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            this.context.startActivity(i);
        }
    }

    @Override
    public void open(String url, String userCode) {
        isOpened = true;
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            Intent intent = customTabsIntent.intent;
            intent.setData(Uri.parse(url));
            customTabLauncher.launch(intent);
        } catch (Exception e) {
            isOpened = false;
            Log.e(TAG, "Failed to launch CustomTab: " + e.getMessage());
            launchFailureCallback.onLaunchFailed();
        }
    }
}
