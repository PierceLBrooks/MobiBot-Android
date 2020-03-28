
// Author: Pierce Brooks

package com.piercelbrooks.mobibot;

public interface SerialComparable <T extends Enum<T>, U extends Serial<T> & Comparable<U>> extends Serial<T>, Comparable<U>
{
    public SerialComparator<T, U> getComparator();
}
