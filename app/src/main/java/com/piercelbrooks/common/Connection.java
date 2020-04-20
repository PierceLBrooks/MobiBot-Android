
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import org.eclipse.californium.elements.DtlsEndpointContext;
import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.RelaxedDtlsEndpointContextMatcher;
import org.eclipse.californium.elements.auth.AdditionalInfo;
import org.eclipse.californium.elements.auth.PreSharedKeyIdentity;
import org.eclipse.californium.elements.auth.RawPublicKeyIdentity;
import org.eclipse.californium.scandium.AlertHandler;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.auth.ApplicationLevelInfoSupplier;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.AlertMessage;
import org.eclipse.californium.scandium.dtls.CertificateMessage;
import org.eclipse.californium.scandium.dtls.DTLSSession;
import org.eclipse.californium.scandium.dtls.HandshakeException;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.cipher.ECDHECryptography;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.eclipse.californium.scandium.dtls.x509.CertificateVerifier;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Connection extends AsyncTask<Void, Void, Void> implements Runnable, Lock, Mortal, ApplicationLevelInfoSupplier, CertificateVerifier, RawDataChannel, AlertHandler, ThreadFactory, SessionListener {
    private static final String TAG = "PLB-Connect";
    private static final String MESSAGE_DATA_CODE = "DATA";
    private static final String MESSAGE_NAME_CODE = "NAME";
    private static final String MESSAGE_RE_CODE = "RE";
    private static final int THREADS = 4;

    private ExecutorService executor;
    private DTLSConnector connector;
    private ReentrantLock lock;
    private AtomicBoolean isRunning;
    private InetSocketAddress address;
    private TreeMap<String, Session> sessions;
    private ECDHECryptography key;

    protected abstract boolean getRole();

    public Connection(@NonNull InetSocketAddress address, @NonNull ECDHECryptography key) {
        super();
        this.connector = null;
        this.lock = new ReentrantLock();
        this.isRunning = new AtomicBoolean(false);
        this.address = address;
        this.sessions = new TreeMap<>();
        this.key = key;
    }

    @Override
    public void birth() {
        executor = Executors.newFixedThreadPool(THREADS, this);
        lock();
        try {
            if (connector == null) {
                DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder();
                //builder.setIdentity(getKey().getPrivateKey(), getKey().getPublicKey());
                builder.setApplicationLevelInfoSupplier(this);
                //builder.setCertificateVerifier(this);
                builder.setPskStore(new StaticPskStore(Constants.REMOTE_SERVICE_IDENTITY, Constants.REMOTE_SERVICE_SECRET.getBytes()));
                builder.setSupportedCipherSuites(getCipher());
                builder.setConnectionThreadCount(1);
                //builder.setRpkTrustAll();
                //builder.setClientAuthenticationRequired(false);
                //builder.setClientAuthenticationWanted(false);
                if (getRole()) {
                    builder.setServerOnly(true);
                } else {
                    builder.setClientOnly();
                }
                builder.setAddress(address);
                connector = new DTLSConnector(builder.build());
                connector.setRawDataReceiver(this);
                connector.setAlertHandler(this);
                connector.setEndpointContextMatcher(new RelaxedDtlsEndpointContextMatcher() {
                    @Override
                    public boolean isToBeSent(EndpointContext messageContext, EndpointContext connectionContext) {
                        if ((messageContext == null) || (connectionContext == null)) {
                            return true;
                        }
                        return super.isToBeSent(messageContext, connectionContext);
                    }
                });
                connector.setExecutor(executor);
                connector.start();
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
        Log.d(TAG, "receiveData");
        if (raw != null) {
            Session session = null;
            JSONObject content = null;
            int response = -1;
            boolean success = true;
            byte[] data = Base64.decode(raw.getBytes(), 0);
            //JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data))));
            try {
                JSONObject reception = new JSONObject(new String(data));
                content = reception.getJSONObject(MESSAGE_DATA_CODE);
                response = Integer.parseInt(reception.getString(MESSAGE_RE_CODE));
                session = getSession(raw);
            } catch (Exception exception) {
                exception.printStackTrace();
                success = false;
            }
            if (session == null) {
                if (getRole()) {
                    session = new Session(raw.getInetSocketAddress(), new RawPublicKeyIdentity(getKey().getPublicKey()));
                } else {
                    success = false;
                }
            }
            if (!success) {
                Log.e(TAG, Utilities.getIdentifier(content));
                return;
            }
            if (response >= 0) {
                Message message = session.getMessage(response);
                if (message != null) {
                    message.onResponse(content);
                }
                return;
            }
            session.onConnect();
        }
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

    public InetSocketAddress getAddress() {
        return address;
    }

    public ECDHECryptography getKey() {
        return key;
    }

    public Executor getExecutor() {
        return executor;
    }

    public Session getSession(@NonNull RawData raw) {
        return getSession(raw.getInetSocketAddress(), raw.getSenderIdentity());
    }

    public Session getSession(@NonNull InetSocketAddress address, @NonNull Principal principal) {
        if (principal instanceof RawPublicKeyIdentity) {
            return getSessionRPK(address, (RawPublicKeyIdentity)principal);
        }
        if (principal instanceof PreSharedKeyIdentity) {
            return getSessionPSK(address, (PreSharedKeyIdentity)principal);
        }
        return null;
    }

    public Session getSession(@NonNull InetSocketAddress address, @NonNull PublicKey key) {
        return getSession(Session.getName(address, key), address, key);
    }

    public Session getSession(@NonNull String name, @NonNull InetSocketAddress address, @NonNull PublicKey key) {
        if (!sessions.containsKey(name)) {
            Session session = new Session(address, key);
            session.addListener(this);
            sessions.put(name, session);
            return session;
        }
        return sessions.get(name);
    }

    public Session getSessionRPK(@NonNull InetSocketAddress address, @NonNull RawPublicKeyIdentity identity) {
        return getSession(address, identity.getKey());
    }

    public Session getSessionPSK(@NonNull InetSocketAddress address, @NonNull PreSharedKeyIdentity identity) {
        return getSession(identity.getIdentity(), address, getKey().getPublicKey());
    }

    public static String getData(@NonNull Session session, @NonNull JSONObject content, @Nullable Message response) {
        JSONObject data = new JSONObject();
        try {
            data.put(MESSAGE_DATA_CODE, content);
            data.put(MESSAGE_NAME_CODE, String.valueOf(session.getNext()));
            data.put(MESSAGE_RE_CODE, session.getMessageName(response));
        } catch (Exception exception) {
            exception.printStackTrace();
            data = null;
        }
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data.toString().getBytes(), 0);
    }

    public static CipherSuite getCipher() {
        return CipherSuite.TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256;
    }

    public boolean send(@NonNull InetSocketAddress address, @NonNull PublicKey key, @NonNull JSONObject content, @Nullable Message response, @Nullable MessageListener listener) {
        return send(getSession(address, key), content, response, listener);
    }

    public boolean send(@NonNull Session session, @NonNull JSONObject content, @Nullable Message response, @Nullable MessageListener listener) {
        boolean success = true;
        try {
            String data = getData(session, content, response);
            if (data != null) {
                DtlsEndpointContext endpoint = session.getEndpoint(getCipher());
                Message message = new Message(session);
                RawData raw = RawData.outbound(data.getBytes(), endpoint, message, false);
                message.setContent(content);
                if (listener != null) {
                    message.addListener(listener);
                }
                Log.d(TAG, data);
                connector.send(raw);
            } else {
                success = false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            success = false;
        }
        return success;
    }
}
