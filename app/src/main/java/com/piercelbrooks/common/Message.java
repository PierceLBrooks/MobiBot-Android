
// Author: Pierce Brooks

package com.piercelbrooks.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.californium.elements.MessageCallback;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Message implements MessageCallback {
    private static final String TAG = "PLB-Message";

    private Session session;
    private JSONObject content;
    private List<MessageListener> listeners;

    public Message(@NonNull Session session) {
        this.session = session;
        this.content = null;
        this.listeners = new ArrayList<>();
        addListener(session);
        session.addMessage(this);
    }

    public String getName() {
        return session.getMessageName(this);
    }

    public Session getSession() {
        return session;
    }

    public void removeListener(@NonNull MessageListener listener) {
        listeners.remove(listener);
    }

    public void addListener(@NonNull MessageListener listener) {
        listeners.add(listener);
    }

    public void setContent(@Nullable JSONObject content) {
        this.content = content;
    }

    public JSONObject getContent() {
        return content;
    }

    public void onResponse(@NonNull JSONObject content) {
        for (int i = 0; i != listeners.size(); ++i) {
            listeners.get(i).onResponse(this, content);
        }
    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDtlsRetransmission(int flight) {

    }

    @Override
    public void onContextEstablished(EndpointContext context) {

    }

    @Override
    public void onSent() {
        for (int i = 0; i != listeners.size(); ++i) {
            listeners.get(i).onDelivery(this);
        }
    }

    @Override
    public void onError(Throwable error) {

    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
