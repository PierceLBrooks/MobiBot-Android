
// Author: Pierce Brooks

package com.piercelbrooks.mobibot;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.piercelbrooks.common.BasicRemoteService;

import java.util.ArrayList;
import java.util.List;

public class MainService extends BasicRemoteService<MainService>
{
    private static final String TAG = "MB-MainServe";
    private static final String KILL = "com.piercelbrooks.mobibot.KILL";
    private static final int KILL_CODE = 2;
    private static final int NOTIFICATION_CODE = 1;

    public MainService()
    {
        super();
    }

    @Override
    protected void create()
    {

    }

    @Override
    protected void destroy()
    {
        //((MainApplication)getApplication()).getActivity().finish();
    }

    @Override
    public Class<?> getServiceClass()
    {
        return MainService.class;
    }

    @Override
    protected Class<?> getActivityClass()
    {
        return MainActivity.class;
    }

    @Override
    public String getName()
    {
        return TAG;
    }

    @Override
    protected Integer getNotification()
    {
        return getNotificationCode();
    }

    @Override
    protected List<NotificationCompat.Action> getNotificationActions()
    {
        ArrayList<NotificationCompat.Action> actions = new ArrayList<>();
        PendingIntent action = PendingIntent.getBroadcast(getContext(), getKillCode(), (new Intent(getKill())).addCategory(Intent.CATEGORY_DEFAULT), 0);
        actions.add(new NotificationCompat.Action(R.drawable.empty, "KILL", action));
        return actions;
    }

    @Override
    public String getDescription()
    {
        return TAG;
    }

    public static String getKill()
    {
        return KILL;
    }

    public static int getKillCode()
    {
        return KILL_CODE;
    }

    public static int getNotificationCode()
    {
        return NOTIFICATION_CODE;
    }
}
