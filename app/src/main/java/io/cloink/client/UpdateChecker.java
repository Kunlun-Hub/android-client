package io.cloink.client;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import io.cloink.gomobile.android.Android;
import io.cloink.client.ui.dialog.ComposeDialogs;

final class UpdateChecker {
    private static final String LOGTAG = "CloinkUpdateChecker";
    private static final String PREFS = "cloink_update";
    private static final String KEY_DOWNLOAD_ID = "download_id";
    private static final String KEY_VERSION = "version";
    private static final String KEY_SHA256 = "sha256";
    private static final String KEY_PENDING_INSTALL = "pending_install";
    private static final AtomicBoolean CHECKED = new AtomicBoolean(false);
    private static final AtomicBoolean RECEIVER_REGISTERED = new AtomicBoolean(false);

    private UpdateChecker() {
    }

    static void check(MainActivity activity) {
        registerDownloadReceiver(activity.getApplicationContext());
        if (!CHECKED.compareAndSet(false, true)) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                PackageInfo packageInfo = activity.getPackageManager()
                        .getPackageInfo(activity.getPackageName(), 0);
                cleanupInstalledUpdate(activity.getApplicationContext(), packageInfo.versionName);
                JSONObject release = new JSONObject(
                        Android.latestSignedRelease("android", "universal"));
                String availableVersion = release.getString("version");
                if (!isNewer(availableVersion, packageInfo.versionName)) {
                    return;
                }
                activity.runOnUiThread(() -> showPrompt(activity, release));
            } catch (Exception e) {
                Log.w(LOGTAG, "Signed update check failed", e);
            }
        });
    }

    static void onResume(Activity activity) {
        registerDownloadReceiver(activity.getApplicationContext());
        Context context = activity.getApplicationContext();
        SharedPreferences prefs = prefs(context);

        String pendingPath = prefs.getString(KEY_PENDING_INSTALL, null);
        if (pendingPath != null) {
            File pending = new File(pendingPath);
            if (pending.isFile() && canInstallPackages(context)) {
                installApk(context, pending);
            }
        }

        resumePendingDownload(context);
    }

    private static void showPrompt(Activity activity, JSONObject release) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        String version = release.optString("version");
        ComposeDialogs.showUpdatePrompt(activity, version, () -> startDownload(activity, release));
    }

    private static void startDownload(Context context, JSONObject release) {
        try {
            String version = release.getString("version");
            String downloadUrl = release.getString("downloadUrl");
            String expectedSha = release.optString("sha256", "").toLowerCase();
            File target = updateFile(context, version);
            if (target == null) {
                toast(context, R.string.update_download_failed);
                return;
            }

            if (target.isFile() && expectedSha.equalsIgnoreCase(sha256(target))) {
                installApk(context, target);
                return;
            }
            if (target.isFile() && !target.delete()) {
                Log.w(LOGTAG, "Unable to remove stale update file " + target);
            }

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) {
                toast(context, R.string.update_download_failed);
                return;
            }
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle("Cloink " + version);
            request.setDescription(context.getString(R.string.update_downloading));
            request.setMimeType("application/vnd.android.package-archive");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, target.getName());
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);

            long id = manager.enqueue(request);
            prefs(context).edit()
                    .putLong(KEY_DOWNLOAD_ID, id)
                    .putString(KEY_VERSION, version)
                    .putString(KEY_SHA256, expectedSha)
                    .apply();
            toast(context, R.string.update_downloading);
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to start signed update download", e);
            toast(context, R.string.update_download_failed);
        }
    }

    private static void resumePendingDownload(Context context) {
        SharedPreferences prefs = prefs(context);
        long id = prefs.getLong(KEY_DOWNLOAD_ID, -1);
        if (id <= 0) {
            return;
        }
        String version = prefs.getString(KEY_VERSION, "");
        String expectedSha = prefs.getString(KEY_SHA256, "");
        File target = updateFile(context, version);
        if (target != null && target.isFile() && expectedSha.equalsIgnoreCase(sha256(target))) {
            clearDownload(context);
            installApk(context, target);
            return;
        }

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) {
            clearDownload(context);
            return;
        }
        int status = downloadStatus(manager, id);
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            verifyAndInstall(context, target, expectedSha);
        } else if (status == DownloadManager.STATUS_FAILED) {
            clearDownload(context);
            toast(context, R.string.update_download_failed);
        }
    }

    private static void registerDownloadReceiver(Context context) {
        if (!RECEIVER_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context receiverContext, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                SharedPreferences prefs = prefs(receiverContext);
                if (id <= 0 || id != prefs.getLong(KEY_DOWNLOAD_ID, -1)) {
                    return;
                }
                String version = prefs.getString(KEY_VERSION, "");
                String expectedSha = prefs.getString(KEY_SHA256, "");
                File target = updateFile(receiverContext, version);
                DownloadManager manager = (DownloadManager) receiverContext.getSystemService(Context.DOWNLOAD_SERVICE);
                if (manager == null) {
                    clearDownload(receiverContext);
                    return;
                }
                int status = downloadStatus(manager, id);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    verifyAndInstall(receiverContext, target, expectedSha);
                } else if (status == DownloadManager.STATUS_FAILED) {
                    clearDownload(receiverContext);
                    toast(receiverContext, R.string.update_download_failed);
                }
            }
        };
        ContextCompat.registerReceiver(
                context,
                receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private static void verifyAndInstall(Context context, File target, String expectedSha) {
        if (target == null || !target.isFile()) {
            clearDownload(context);
            toast(context, R.string.update_download_failed);
            return;
        }
        if (expectedSha == null || expectedSha.isEmpty()) {
            clearDownload(context);
            toast(context, R.string.update_verify_failed);
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            String actualSha = sha256(target);
            if (!expectedSha.equalsIgnoreCase(actualSha)) {
                Log.w(LOGTAG, "Update checksum mismatch: expected " + expectedSha + " got " + actualSha);
                if (!target.delete()) {
                    Log.w(LOGTAG, "Unable to remove failed update file " + target);
                }
                clearDownload(context);
                runOnMainThread(() -> toast(context, R.string.update_verify_failed));
                return;
            }
            clearDownload(context);
            runOnMainThread(() -> installApk(context, target));
        });
    }

    private static void installApk(Context context, File file) {
        try {
            if (!file.isFile()) {
                toast(context, R.string.update_download_failed);
                return;
            }
            if (!canInstallPackages(context)) {
                prefs(context).edit().putString(KEY_PENDING_INSTALL, file.getAbsolutePath()).apply();
                Intent settings = new Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + context.getPackageName()));
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settings);
                toast(context, R.string.update_install_permission);
                return;
            }

            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(uri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(install);
            prefs(context).edit().remove(KEY_PENDING_INSTALL).apply();
        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to launch package installer", e);
            toast(context, R.string.update_download_failed);
        }
    }

    private static boolean canInstallPackages(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true;
        }
        return context.getPackageManager().canRequestPackageInstalls();
    }

    private static int downloadStatus(DownloadManager manager, long id) {
        try (Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id))) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            }
        } catch (Exception e) {
            Log.w(LOGTAG, "Unable to query update download", e);
        }
        return DownloadManager.STATUS_PENDING;
    }

    private static File updateFile(Context context, String version) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            return null;
        }
        String safeVersion = version == null ? "" : version.replaceAll("[^0-9A-Za-z._-]", "_");
        return new File(dir, "cloink-" + safeVersion + ".apk");
    }

    private static void cleanupInstalledUpdate(Context context, String currentVersion) {
        SharedPreferences prefs = prefs(context);
        if (prefs.contains(KEY_DOWNLOAD_ID) || prefs.contains(KEY_PENDING_INSTALL)) {
            return;
        }
        File installedPackage = updateFile(context, currentVersion);
        if (installedPackage != null && installedPackage.isFile() && !installedPackage.delete()) {
            Log.w(LOGTAG, "Unable to remove installed update file " + installedPackage);
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static void clearDownload(Context context) {
        prefs(context).edit()
                .remove(KEY_DOWNLOAD_ID)
                .remove(KEY_VERSION)
                .remove(KEY_SHA256)
                .apply();
    }

    private static void toast(Context context, int message) {
        runOnMainThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    private static void runOnMainThread(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
            return;
        }
        new Handler(Looper.getMainLooper()).post(action);
    }

    private static String sha256(File file) {
        try (InputStream input = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            StringBuilder hex = new StringBuilder(digest.getDigestLength() * 2);
            for (byte value : digest.digest()) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (Exception e) {
            Log.w(LOGTAG, "Unable to hash update file", e);
            return "";
        }
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
