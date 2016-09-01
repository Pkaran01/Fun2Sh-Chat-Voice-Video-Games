package com.quickblox.q_municate_core.crud;

import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.chat.query.QueryAbsMessage;
import com.quickblox.core.RestMethod;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.rest.RestRequest;

import java.util.Set;

/**
 * Created by Karan on 8/25/2016.
 */
public class QueryDeleteMessages extends QueryAbsMessage {
    protected boolean forceDelete;
    private Set<String> messageIDs;

    public QueryDeleteMessages(Set<String> messageIDs, boolean forceDelete) {
        this.messageIDs = messageIDs;
        this.forceDelete = forceDelete;
    }

    public QueryDeleteMessages(Set<String> messageIDs) {
        this(messageIDs, false);
    }

    public String getUrl() {
        String relateDomain = super.getRelateDomain();
        String comSeparetedMessagesIDs = TextUtils.join(",", this.messageIDs);
        return this.buildQueryUrl(new Object[]{relateDomain, comSeparetedMessagesIDs});
    }

    protected void setParams(RestRequest request) {
        if(this.forceDelete) {
            request.getParameters().put("force", Integer.valueOf(1));
        }

    }

    protected void setMethod(RestRequest request) {
        request.setMethod(RestMethod.DELETE);
    }

    public static Void deleteMessages(Set<String> messageIDs, boolean forceDelete) throws QBResponseException {
        QueryDeleteMessages queryDeleteDialogMessages = new QueryDeleteMessages(messageIDs, forceDelete);
        return (Void)queryDeleteDialogMessages.perform((Bundle)null);
    }
}
