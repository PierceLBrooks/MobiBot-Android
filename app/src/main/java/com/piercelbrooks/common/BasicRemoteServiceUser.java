
// Author: Pierce Brooks

package com.piercelbrooks.common;

public interface BasicRemoteServiceUser<T extends BasicRemoteService<T>>
{
    public Class<?> getServiceClass();
    public T getService();
}
