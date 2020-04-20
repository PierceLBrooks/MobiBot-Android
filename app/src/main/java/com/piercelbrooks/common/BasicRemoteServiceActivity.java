
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.californium.scandium.dtls.cipher.ECDHECryptography;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BasicRemoteServiceActivity<T extends Enum<T>, U extends BasicRemoteService<U>> extends BasicActivity<T> implements BasicRemoteServiceUser<U>, ClientListener
{
    private static final String TAG = "PLB-BaseRemServeAct";

    private Object firstClientRun;
    private Client client;
    private AtomicBoolean isBound;
    private U service;

    public BasicRemoteServiceActivity()
    {
        super();
        isBound = new AtomicBoolean();
        firstClientRun = new Object();
        client = null;
        service = null;
    }

    public void onServiceConnected(U service)
    {
        if (isBound.get())
        {
            Log.w(TAG, "Already bound.");
            return;
        }
        this.isBound.set(true);
        this.service = service;
    }

    public void onServiceDisconnected()
    {
        if (!isBound.get())
        {
            Log.w(TAG, "No binding.");
            return;
        }
        this.isBound.set(false);
        this.service = null;
    }

    public boolean getIsServiceBound()
    {
        return isBound.get();
    }

    public boolean getIsServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (getServiceClass().getName().equals(service.service.getClassName()))
            {
                if (!service.foreground)
                {
                    Log.v(TAG, "Service not foregrounded...");
                    /*if (service.started)
                    {
                        Log.v(TAG, "Service not started...");
                        stopService();
                    }*/
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public boolean beginService()
    {
        if (!getIsServiceRunning())
        {
            Log.d(TAG, "Starting service...");
            startService();
            Log.d(TAG, "Started service!");
        }
        return bindService();
    }

    public boolean endService()
    {
        if (!getIsServiceRunning())
        {
            Log.v(TAG, "No service.");
            return false;
        }
        unbindService();
        return stopService();
    }

    public boolean bindService()
    {
        if (!getIsServiceBound())
        {
            boolean success = false;
            Log.d(TAG, "Binding service...");
            if (this.client == null)
            {
                Client client = new Client(this, new InetSocketAddress(InetAddress.getLoopbackAddress(), Constants.REMOTE_SERVICE_CLIENT_PORT), getKey());
                client.birth();
                while (true)
                {
                    synchronized (this.firstClientRun)
                    {
                        try
                        {
                            this.firstClientRun.wait();
                        }
                        catch (Exception exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                    if (this.client == client)
                    {
                        success = true;
                        break;
                    }
                }
            }
            Log.d(TAG, "Bound service!");
            return success;
        }
        return true;
    }

    public boolean unbindService()
    {
        if (getIsServiceBound())
        {
            boolean success = false;
            Log.d(TAG, "Unbinding service...");
            if (client != null)
            {
                client.death();
                client = null;
                success = true;
            }
            Log.d(TAG, "Unbound service!");
            return success;
        }
        return false;
    }

    public boolean startService()
    {
        Log.v(TAG, "Starting service...");
        startService(getServiceIntent());
        return true;
    }

    public boolean stopService()
    {
        Log.v(TAG, "Stopping service...");
        return stopService(getServiceIntent());
    }

    public Intent getServiceIntent()
    {
        return new Intent(getApplicationContext(), getServiceClass());
    }

    public static ECDHECryptography getKey()
    {
        return ECDHECryptography.fromNamedCurveId(ECDHECryptography.SupportedGroup.secp256r1.getId());
    }

    @Override
    public U getService()
    {
        return service;
    }

    @Override
    protected void onPause()
    {
        if (getIsServiceRunning())
        {
            unbindService();
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        if (getIsServiceRunning())
        {
            bindService();
        }
        super.onResume();
    }

    @Override
    public void onFirstClientRun(@NonNull Client client)
    {
        Utilities.sleep(250);
        client.send(new InetSocketAddress(InetAddress.getLoopbackAddress(), Constants.REMOTE_SERVICE_SERVER_PORT), getKey().getPublicKey(), new JSONObject(), null, null);
        synchronized (this.firstClientRun)
        {
            this.client = client;
            this.firstClientRun.notifyAll();
        }
    }
}
