package com.quickblox.q_municate_core.crud;

/**
 * Created by CRUD on 8/7/2016.
 */

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.request.QBRequestBuilder;

public class QBMessageUpdateBuilder extends QBRequestBuilder {
    public QBMessageUpdateBuilder() {
    }

    public QBMessageUpdateBuilder updateText(String text) {
        this.addRule("message", text);
        return this;
    }

    public QBMessageUpdateBuilder markRead() {
        this.addRule("read", Integer.valueOf(1));
        return this;
    }

    public QBMessageUpdateBuilder markDelivered() {
        this.addRule("delivered", Integer.valueOf(1));
        return this;
    }

    public static QBRequestCanceler updateMessage(String messageId, String dialogId, QBMessageUpdateBuilder messageUpdateBuilder, QBEntityCallback<Void> callback) {
        QueryUpdateMessage updateDialogMessage = new QueryUpdateMessage(messageId, dialogId, messageUpdateBuilder);
        return new QBRequestCanceler(updateDialogMessage.performAsyncWithCallback(callback));
    }
}
