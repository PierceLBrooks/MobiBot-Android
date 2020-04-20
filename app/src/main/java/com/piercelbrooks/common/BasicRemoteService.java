
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.eclipse.californium.scandium.dtls.cipher.ECDHECryptography;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PublicKey;

public abstract class BasicRemoteService<T extends BasicRemoteService<T>> extends BasicService<T> implements BasicRemoteServiceUser<T>, Citizen, ServerListener
{
    private static final String TAG = "PLB-BaseRemServe";

    private Server server;
    private Object firstServerRun;

    public BasicRemoteService()
    {
        super();
        server = null;
        firstServerRun = new Object();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        birth();
        create();
    }

    @Override
    public void onDestroy()
    {
        destroy();
        death();
        super.onDestroy();
    }

    @Override
    public T getService()
    {
        return (T)this;
    }

    @Override
    public Class<?> getCitizenClass()
    {
        return getServiceClass();
    }

    @Override
    public Family getFamily()
    {
        return Family.SERVICE;
    }

    @Override
    public void birth()
    {
        Governor.getInstance().register(this);
        if (this.server == null)
        {
            Server server = new Server(this, new InetSocketAddress(InetAddress.getLoopbackAddress(), Constants.REMOTE_SERVICE_SERVER_PORT), getKey());
            server.birth();
            while (true)
            {
                synchronized (this.firstServerRun)
                {
                    try
                    {
                        this.firstServerRun.wait();
                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
                if (this.server == server)
                {
                    break;
                }
            }
        }
    }

    @Override
    public void death()
    {
        if (server != null)
        {
            server.death();
            server = null;
        }
        Governor.getInstance().unregister(this);
    }

    @Override
    public void onFirstServerRun(@NonNull Server server)
    {
        //Utilities.sleep(250);
        synchronized (this.firstServerRun)
        {
            this.server = server;
            this.firstServerRun.notifyAll();
        }
    }

    public Context getContext()
    {
        return getApplicationContext();
    }

    public static ECDHECryptography getKey()
    {
        return ECDHECryptography.fromNamedCurveId(ECDHECryptography.SupportedGroup.secp256r1.getId());
    }
}
