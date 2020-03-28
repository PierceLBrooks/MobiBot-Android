
// Author: Pierce Brooks

package com.piercelbrooks.mobibot;

import java.util.List;

public interface Serial <T extends Enum<T>>
{
    public Class<?> getSerialClass();
    public Serial<T> getDeserialization(List<String> source);
    public String getIdentifier();
    public List<String> getSerialization();
    public String getMemberIdentifier(T member);
    public List<String> getMemberSerialization(T member);
}
