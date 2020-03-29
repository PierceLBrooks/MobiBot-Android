
// Author: Pierce Brooks;

package com.piercelbrooks.common;

import android.support.annotation.NonNull;

public class Server extends Connection {
    private static final String TAG = "PLB-Server";

    private ServerListener listener;
    private boolean isFirst;

    public Server(@NonNull ServerListener listener) {
        super();
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
}
