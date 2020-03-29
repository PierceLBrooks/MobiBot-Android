
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.Service;
import android.support.v4.app.NotificationCompat;

import java.util.List;

public abstract class BasicService<T extends BasicService<T>> extends Service
{
    private static final String TAG = "PLB-BasicServe";

    protected abstract void create();
    protected abstract void destroy();
    protected abstract Class<?> getActivityClass();
    protected abstract Integer getNotification();
    protected abstract List<NotificationCompat.Action> getNotificationActions();
    public abstract String getDescription();
    public abstract String getName();

    public BasicService()
    {
        super();
    }
}
