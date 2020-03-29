
// Author: Pierce Brooks

package com.piercelbrooks.common;

public interface BasicLocalServiceUser<T extends BasicLocalService<T>>
{
    public Class<?> getServiceClass();
    public T getService();
}
