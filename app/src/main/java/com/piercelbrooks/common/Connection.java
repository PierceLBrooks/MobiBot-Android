
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.eclipse.californium.elements.KeySetEndpointContextMatcher;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.auth.AdditionalInfo;
import org.eclipse.californium.scandium.AlertHandler;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.auth.ApplicationLevelInfoSupplier;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.AlertMessage;
import org.eclipse.californium.scandium.dtls.CertificateMessage;
import org.eclipse.californium.scandium.dtls.DTLSSession;
import org.eclipse.californium.scandium.dtls.HandshakeException;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.eclipse.californium.scandium.dtls.x509.CertificateVerifier;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Connection extends AsyncTask<Void, Void, Void> implements Runnable, Lock, Mortal, ApplicationLevelInfoSupplier, CertificateVerifier, RawDataChannel, AlertHandler, ThreadFactory {
    private static final String TAG = "PLB-Connect";
    private static final int THREADS = 4;

    private ExecutorService executor;
    private DTLSConnector connector;
    private ReentrantLock lock;
    private AtomicBoolean isRunning;

    protected abstract boolean getRole();

    public Connection() {
        super();
        connector = null;
        lock = new ReentrantLock();
        isRunning = new AtomicBoolean(false);
    }

    @Override
    public void birth() {
        executor = Executors.newFixedThreadPool(THREADS, this);
        lock();
        try {
            if (connector == null) {
                DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder();
                builder.setAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), Constants.REMOTE_SERVICE_PORT));
                builder.setApplicationLevelInfoSupplier(this);
                //builder.setCertificateVerifier(this);
                builder.setPskStore(new StaticPskStore("identity", "secret".getBytes()));
                builder.setSupportedCipherSuites(CipherSuite.TLS_PSK_WITH_AES_128_GCM_SHA256);
                //builder.setClientAuthenticationRequired(false);
                //builder.setClientAuthenticationWanted(false);
                if (getRole()) {
                    builder.setServerOnly(true);
                } else {
                    builder.setClientOnly();
                }
                connector = new DTLSConnector(builder.build());
                connector.setRawDataReceiver(this);
                connector.setAlertHandler(this);
                connector.setEndpointContextMatcher(new KeySetEndpointContextMatcher(TAG, new String[]{}, false){});
                connector.setExecutor(executor);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            unlock();
        }
        executeOnExecutor(executor);
    }

    @Override
    public void death() {
        lock();
        try {
            if (connector != null) {
                connector.destroy();
                connector = null;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            unlock();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DTLSConnector connector = this.connector;
        if (connector == null) {
            return null;
        }
        while (true) {
            isRunning.set(true);
            lock();
            try {
                if (connector != this.connector) {
                    isRunning.set(false);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                unlock();
            }
            if (!isRunning.get()) {
                break;
            }
            run();
        }
        return null;
    }

    @Override
    public AdditionalInfo getInfo(Principal peerIdentity) {
        return AdditionalInfo.empty();
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{new ValidCertificate(new Date())};
    }

    @Override
    public void verifyCertificate(CertificateMessage message, DTLSSession session) throws HandshakeException {

    }

    @Override
    public void receiveData(RawData raw) {
        byte[] data = raw.getBytes();
        Log.d(TAG, Base64.encodeToString(data, 0, data.length, 0));
    }

    @Override
    public void onAlert(InetSocketAddress peer, AlertMessage alert) {
        if (alert == null) {
            return;
        }
        if (alert.getDescription() == null) {
            return;
        }
        switch (alert.getLevel()) {
            case FATAL:
                Log.e(TAG, alert.getDescription().getDescription());
                break;
            case WARNING:
            default:
                Log.w(TAG, alert.getDescription().getDescription());
                break;
        }
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable);
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
        return lock.tryLock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public Condition newCondition() {
        return lock.newCondition();
    }

    public boolean getIsRunning() {
        return isRunning.get();
    }

    public DTLSConnector getConnector() {
        return connector;
    }

    public Executor getExecutor() {
        return executor;
    }
}
