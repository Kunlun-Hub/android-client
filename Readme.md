# Cloink Android client

The Cloink Android client connects Android phones, tablets, ChromeOS devices,
and Android TV devices to a Cloink private network. The application uses the
Go mobile bindings from the `Kunlun-Hub/cloink-server` submodule.

## Building from source

### Requirements

- Java 17
- Android SDK 35
- Android NDK `23.1.7779620`
- Go version declared by `netbird/go.mod`
- Android Studio, or the command-line Android SDK tools

### Prepare the development environment

```shell
git clone --recurse-submodules https://github.com/Kunlun-Hub/android-client.git cloink-android
cd cloink-android

export ANDROID_HOME=/path/to/Android/sdk
export JAVA_HOME=/path/to/jdk-17
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install "ndk;23.1.7779620"
```

### Build a debug APK and AAB

```shell
./build-android-lib.sh v0.77.1
./gradlew assembleDebug bundleDebug -PversionCode=1 -PversionName=0.77.1
```

The application ID is `io.cloink.client`, and the default management server is
`https://cloink.4w.ink`.

This project is distributed under the GNU General Public License v3.0. See
[LICENSE](LICENSE).
