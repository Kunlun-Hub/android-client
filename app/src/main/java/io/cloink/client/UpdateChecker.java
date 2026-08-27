package io.cloink.client;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import io.cloink.gomobile.android.Android;
import io.cloink.client.ui.dialog.ComposeDialogs;

final class UpdateChecker {
    private static final String LOGTAG = "CloinkUpdateChecker";
    private static final AtomicBoolean CHECKED = new AtomicBoolean(false);

    private UpdateChecker() {
    }

    static void check(MainActivity activity) {
        if (!CHECKED.compareAndSet(false, true)) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                PackageInfo packageInfo = activity.getPackageManager()
                        .getPackageInfo(activity.getPackageName(), 0);
                JSONObject release = new JSONObject(
                        Android.latestSignedRelease("android", "universal"));
                String availableVersion = release.getString("version");
                String downloadUrl = release.getString("downloadUrl");
                if (!isNewer(availableVersion, packageInfo.versionName)) {
                    return;
                }
                activity.runOnUiThread(() -> showPrompt(
                        activity,
                        availableVersion,
                        downloadUrl));
            } catch (Exception e) {
                Log.w(LOGTAG, "Signed update check failed", e);
            }
        });
    }

    private static void showPrompt(
            MainActivity activity,
            String version,
            String downloadUrl) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        ComposeDialogs.showUpdatePrompt(activity, version, () -> {
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)));
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to open signed update URL", e);
            }
        });
    }

    static boolean isNewer(String available, String current) {
        int[] availableParts = versionParts(available);
        int[] currentParts = versionParts(current);
        for (int i = 0; i < Math.max(availableParts.length, currentParts.length); i++) {
            int left = i < availableParts.length ? availableParts[i] : 0;
            int right = i < currentParts.length ? currentParts[i] : 0;
            if (left != right) {
                return left > right;
            }
        }
        return false;
    }

    private static int[] versionParts(String value) {
        String normalized = value == null ? "" : value.trim().replaceFirst("^[vV]", "");
        String core = normalized.split("[-+]", 2)[0];
        String[] parts = core.split("\\.");
        int[] parsed = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                parsed[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {
                parsed[i] = 0;
            }
        }
        return parsed;
    }
}
