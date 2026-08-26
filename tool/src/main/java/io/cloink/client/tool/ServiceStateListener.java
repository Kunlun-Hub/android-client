package io.cloink.client.tool;

public interface ServiceStateListener {
    void onStarted();
    void onStopped();
    void onError(String msg);

}
