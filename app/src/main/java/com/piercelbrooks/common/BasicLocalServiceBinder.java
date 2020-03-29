
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.os.Binder;

public abstract class BasicLocalServiceBinder<T extends BasicLocalService<T>> extends Binder implements BasicLocalServiceUser<T>
{
    private static final String TAG = "PLB-BaseLocServeBind";

    private T service;

    public BasicLocalServiceBinder(T service)
    {
        super();
        this.service = service;
    }

    @Override
    public T getService()
    {
        return service;
    }
}
