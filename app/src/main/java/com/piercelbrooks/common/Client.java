
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.support.annotation.NonNull;

public class Client extends Connection {
    private static final String TAG = "PLB-Client";

    ClientListener listener;
    private boolean isFirst;

    public Client(@NonNull ClientListener listener) {
        super();
        this.listener = listener;
        this.isFirst = false;
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
}
