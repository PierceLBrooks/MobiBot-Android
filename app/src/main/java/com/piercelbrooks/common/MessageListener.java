
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.support.annotation.NonNull;

import org.json.JSONObject;

public interface MessageListener {
    public void onDelivery(@NonNull Message message);
    public void onResponse(@NonNull Message message, @NonNull JSONObject content);
}
