
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.californium.scandium.dtls.cipher.ECDHECryptography;

import java.net.InetSocketAddress;

public class Client extends Connection {
    private static final String TAG = "PLB-Client";

    ClientListener listener;
    private boolean isFirst;

    public Client(@NonNull ClientListener listener, @NonNull InetSocketAddress address, @NonNull ECDHECryptography key) {
        super(address, key);
        this.listener = listener;
        this.isFirst = true;
    }

    @Override
    public void run() {
        if (isFirst) {
            isFirst = false;
            listener.onFirstClientRun(this);
        }
    }

    @Override
    protected boolean getRole() {
        return false;
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
