
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import org.eclipse.californium.elements.DtlsEndpointContext;
import org.eclipse.californium.elements.auth.RawPublicKeyIdentity;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Session implements MessageListener {
    private static final String TAG = "PLB-Session";

    private InetSocketAddress address;
    private PublicKey key;
    private String id;
    private List<SessionListener> listeners;
    private List<Message> messages;

    public Session(@NonNull InetSocketAddress address, @NonNull RawPublicKeyIdentity key) {
        this(address, key.getKey());
    }

    public Session(@NonNull InetSocketAddress address, @NonNull PublicKey key) {
        this.address = address;
        this.key = key;
        this.id = Base64.encodeToString(toString().getBytes(), 0);
        this.listeners = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public static String getName(@NonNull InetSocketAddress address, @NonNull PublicKey key) {
        return "$"+address.getAddress().getHostAddress()+":"+address.getPort()+"@"+Base64.encodeToString(key.getEncoded(), 0)+"#"+key.getAlgorithm()+"%"+key.getFormat();
    }

    public Message getMessage(int name) {
        Message message = null;
        try {
            message = messages.get(name);
        } catch (Exception exception) {
            exception.printStackTrace();
            message = null;
        }
        return message;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public DtlsEndpointContext getEndpoint(@NonNull CipherSuite cipher) {
        return new DtlsEndpointContext(address, new RawPublicKeyIdentity(key), id, String.valueOf(0), cipher.name(), String.valueOf(System.currentTimeMillis())) {
            @Override
            public boolean hasCriticalEntries() {
                return false;
            }
        };
    }

    public int getNext() {
        return messages.size();
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public PublicKey getKey() {
        return key;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return getName(address, key);
    }

    public String getMessageName(@Nullable Message message) {
        if (message == null) {
            return String.valueOf(-1);
        }
        return String.valueOf(messages.indexOf(message));
    }

    public void removeListener(@NonNull SessionListener listener) {
        listeners.remove(listener);
    }

    public void addListener(@NonNull SessionListener listener) {
        listeners.add(listener);
    }

    public void addMessage(@NonNull Message message) {
        messages.add(message);
    }

    public void onConnect() {
        for (int i = 0; i != listeners.size(); ++i) {
            listeners.get(i).onConnect(this);
        }
    }

    public void onDisconnect() {
        for (int i = 0; i != listeners.size(); ++i) {
            listeners.get(i).onDisconnect(this);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void onDelivery(@NonNull Message message) {
        Log.d(TAG, "onDelivery\n"+getMessageName(message)+getName());
    }

    @Override
    public void onResponse(@NonNull Message message, @NonNull JSONObject content) {
        Log.d(TAG, "onResponse\n"+getMessageName(message)+getName());
    }
}
