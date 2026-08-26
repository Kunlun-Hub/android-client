package io.cloink.client;

public interface StateListenerRegistry {
    void registerServiceStateListener(StateListener listener);
    void unregisterServiceStateListener(StateListener listener);
}