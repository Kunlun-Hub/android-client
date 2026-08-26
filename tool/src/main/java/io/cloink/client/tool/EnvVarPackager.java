package io.cloink.client.tool;

import io.cloink.gomobile.android.Android;
import io.cloink.gomobile.android.EnvList;

public class EnvVarPackager {
    public static EnvList getEnvironmentVariables(Preferences preferences) {
        var envList = new EnvList();

        envList.put(Android.getEnvKeyNBForceRelay(), String.valueOf(preferences.isConnectionForceRelayed()));

        return envList;
    }
}
