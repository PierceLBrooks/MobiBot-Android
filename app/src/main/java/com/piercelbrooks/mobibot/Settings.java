
// Author: Piece Brooks

package com.piercelbrooks.mobibot;

import com.piercelbrooks.common.Family;
import com.piercelbrooks.common.Governor;
import com.piercelbrooks.common.Preferences;

import java.util.List;

public class Settings extends Preferences
{
    public enum Setting
    {
        INCOMING_MAIL_PROPERTIES,
        OUTGOING_MAIL_PROPERTIES
    }

    private static final String TAG = "MB-Settings";
    private static final String INCOMING_MAIL_PROPERTIES_KEY = "INCOMING_MAIL_PROPERTIES";
    private static final String OUTGOING_MAIL_PROPERTIES_KEY = "OUTGOING_MAIL_PROPERTIES";
    private static final String MAIL_PROPERTIES_DELIMITER = "\n";

    public Settings()
    {
        super(((MainApplication)(Governor.getInstance().getCitizen(Family.APPLICATION))).getPreferences());
    }

    public List<String> getSettingSerialization(Setting setting)
    {
        if (setting == null)
        {
            return null;
        }
        List<String> serialization = null;
        if (serialization != null)
        {
            serialization.add(0, "{");
            serialization.add(0, setting.name());
            serialization.add(" ");
            serialization.add("}");
        }
        return serialization;
    }
}
