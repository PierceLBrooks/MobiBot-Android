
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.support.annotation.NonNull;

public interface SessionListener {
    public void onConnect(@NonNull Session session);
    public void onDisconnect(@NonNull Session session);
}
