package io.cloink.client;

import io.cloink.client.tool.RouteChangeListener;
import io.cloink.gomobile.android.NetworkArray;
import io.cloink.gomobile.android.PeerInfoArray;

public interface ServiceAccessor {
    // Add methods to interact with your service
    void switchConnection(boolean isConnected);
    PeerInfoArray getPeersList();

    NetworkArray getNetworks();
    void stopEngine();

    void selectRoute(String route) throws Exception;
    void deselectRoute(String route) throws Exception;

    void addRouteChangeListener(RouteChangeListener listener);
    void removeRouteChangeListener(RouteChangeListener listener);

    String debugBundle(boolean anonymize) throws Exception;
}