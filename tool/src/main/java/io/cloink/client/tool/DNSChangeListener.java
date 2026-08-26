package io.cloink.client.tool;

import io.cloink.gomobile.android.DNSList;

interface DNSChangeListener {
    void onChanged(DNSList dnsServers) throws Exception;
}
