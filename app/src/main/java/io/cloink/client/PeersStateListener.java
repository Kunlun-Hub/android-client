package io.cloink.client;

public interface PeersStateListener {
    void onPeersChanged(long totalPeers);
}
