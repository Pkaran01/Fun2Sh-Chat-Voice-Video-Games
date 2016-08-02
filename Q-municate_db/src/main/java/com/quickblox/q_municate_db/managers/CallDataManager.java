package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Call;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class CallDataManager extends BaseManager<Call> {

    private static final String TAG = CallDataManager.class.getSimpleName();
    private Dao<User, Long> userDao;

    public CallDataManager(Dao<Call, Long> dao, Dao<User, Long> userDao) {
        super(dao, CallDataManager.class.getSimpleName());
        this.userDao = userDao;
    }

    public void deleteByCallId(int callId) {
        try {
            DeleteBuilder<Call, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(Call.Column.ID, callId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        notifyObservers(OBSERVE_KEY);
    }

    public List<Call> getAllSorted() {
        List<Call> callsList = Collections.emptyList();
        try {
            QueryBuilder<Call, Long> friendQueryBuilder = dao.queryBuilder();

            QueryBuilder<User, Long> userQueryBuilder = userDao.queryBuilder();
            userQueryBuilder.orderByRaw(User.Column.FULL_NAME + " COLLATE NOCASE");

            friendQueryBuilder.join(userQueryBuilder);

            PreparedQuery<Call> preparedQuery = friendQueryBuilder.prepare();

            callsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return callsList;
    }

}