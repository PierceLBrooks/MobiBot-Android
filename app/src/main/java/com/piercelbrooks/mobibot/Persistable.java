
// Author: Pierce Brooks

package com.piercelbrooks.mobibot;

public interface Persistable <T extends Enum<T>> extends Serial<T>
{
    public boolean save(String path);
    public boolean load(String path);
}
