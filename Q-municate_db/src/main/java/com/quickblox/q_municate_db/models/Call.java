package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.Call.Column.CALL_DURETION;
import static com.quickblox.q_municate_db.models.Call.Column.CREATED_DATE;
import static com.quickblox.q_municate_db.models.Call.Column.ID;
import static com.quickblox.q_municate_db.models.Call.Column.STATUS;
import static com.quickblox.q_municate_db.models.Call.Column.TABLE_NAME;
import static com.quickblox.q_municate_db.models.Call.Column.TYPE;


@DatabaseTable(tableName = TABLE_NAME)
public class Call implements Serializable {

    @DatabaseField(
            generatedId = true,
            unique = true,
            columnName = ID)
    private int callId;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            columnName = User.Column.ID)
    private User user;

    @DatabaseField(
            columnName = CREATED_DATE)
    private long createdDate;

    @DatabaseField(
            columnName = TYPE)
    private int callType;

    @DatabaseField(
            columnName = CALL_DURETION)
    private long callDuration;

    @DatabaseField(
            columnName = STATUS)
    private long status;

    public int getCallId() {
        return callId;
    }

    public void setCallId(int callId) {
        this.callId = callId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public long getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(long callDuration) {
        this.callDuration = callDuration;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public Call() {
    }

    public interface Column {
        String TABLE_NAME = "call";
        public static final String ID = "call_id";
        public static final String CREATED_DATE = "created_date";
        public static final String TYPE = "call_type";
        public static final String STATUS = "status";
        public static final String CALL_DURETION = "call_direction";
    }
}