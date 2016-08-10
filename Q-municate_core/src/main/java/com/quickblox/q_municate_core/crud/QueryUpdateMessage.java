package com.quickblox.q_municate_core.crud;

/**
 * Created by CRUD on 8/7/2016.
 */

import android.text.TextUtils;

import com.quickblox.chat.query.QueryAbsMessage;
import com.quickblox.core.RestMethod;
import com.quickblox.core.rest.RestRequest;

import java.util.Map;

public class QueryUpdateMessage extends QueryAbsMessage {
    private final String messagesId;
    private String dialogId;
    private QBMessageUpdateBuilder messageUpdateBuilder;

    public QueryUpdateMessage(String messagesId, String dialogId, QBMessageUpdateBuilder messageUpdateBuilder) {
        this.messagesId = messagesId;
        this.dialogId = dialogId;
        this.messageUpdateBuilder = messageUpdateBuilder;
    }

    protected void setParams(RestRequest request) {
        Map parametersMap = request.getParameters();
        this.messageUpdateBuilder.fillParametersMap(parametersMap);
        if(!TextUtils.isEmpty(this.dialogId)) {
            parametersMap.put("chat_dialog_id", this.dialogId);
        }

    }

    public String getUrl() {
        String relateDomain = super.getRelateDomain();
        return this.buildQueryUrl(new Object[]{relateDomain, this.messagesId});
    }

    protected void setMethod(RestRequest request) {
        request.setMethod(RestMethod.PUT);
    }


}
