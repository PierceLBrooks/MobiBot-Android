
// Author: Pierce Brooks;

package com.piercelbrooks.common;

import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.californium.scandium.dtls.cipher.ECDHECryptography;

import java.net.InetSocketAddress;

public class Server extends Connection {
    private static final String TAG = "PLB-Server";

    private ServerListener listener;
    private boolean isFirst;

    public Server(@NonNull ServerListener listener, @NonNull InetSocketAddress address, @NonNull ECDHECryptography key) {
        super(address, key);
        this.listener = listener;
        this.isFirst = true;
    }

    @Override
    public void run() {
        if (isFirst) {
            isFirst = false;
            listener.onFirstServerRun(this);
        }
    }

    @Override
    protected boolean getRole() {
        return true;
    }

    @Override
    public void onConnect(@NonNull Session session) {
        Log.d(TAG, "onConnect");
    }

    @Override
    public void onDisconnect(@NonNull Session session) {
        Log.d(TAG, "onDisconnect");
    }
}
