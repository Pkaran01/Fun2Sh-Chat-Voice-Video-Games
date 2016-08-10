package com.quickblox.q_municate_db.managers;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDataManager extends BaseManager<User> {

    private static final String TAG = UserDataManager.class.getSimpleName();

    private Dao<DialogOccupant, Long> dialogOccupantDao;

    public UserDataManager(Dao<User, Long> userDao, Dao<DialogOccupant, Long> dialogOccupantDao) {
        super(userDao, UserDataManager.class.getSimpleName());
        this.dialogOccupantDao = dialogOccupantDao;
    }

    public boolean isUserOwner(String email) {
        User user = null;

        try {
            QueryBuilder<User, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.LOGIN, email);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            user = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return user != null && user.getRole() == User.Role.OWNER;
    }

    public User getOwner() {
        User user = null;

        try {
            QueryBuilder<User, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.ROLE, User.Role.OWNER);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            user = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return user;
    }

    public List<User> getAllByIds(List<Integer> idsList) {
        List<User> usersList = Collections.emptyList();

        try {
            QueryBuilder<User, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(User.Column.ID, idsList);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            usersList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

    public List<User> getUsersForGroupChat(String dialogId, List<Integer> idsList) {
        List<User> usersList = Collections.emptyList();

        try {
            QueryBuilder<User, Long> userQueryBuilder = dao.queryBuilder();
            userQueryBuilder.where().in(User.Column.ID, idsList);

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();
            Where<DialogOccupant, Long> where = dialogOccupantQueryBuilder.where();
            where.and(
                    where.eq(Dialog.Column.ID, dialogId),
                    where.eq(DialogOccupant.Column.STATUS, DialogOccupant.Status.ACTUAL)
            );

            userQueryBuilder.join(dialogOccupantQueryBuilder);
            userQueryBuilder.distinct();

            userQueryBuilder.orderBy(User.Column.FULL_NAME, true);

            PreparedQuery<User> preparedQuery = userQueryBuilder.prepare();
            usersList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

    //blocked user
    public List<User> getBlockedContact() {
        List<User> messagesList = new ArrayList<>();
        try {
            QueryBuilder<User, Long> messageQueryBuilder = dao.queryBuilder();
            messageQueryBuilder.where().eq(User.Column.isBlocked, 1);
            PreparedQuery<User> preparedQuery = messageQueryBuilder.prepare();
            messagesList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return messagesList;
    }


    public int updateFriend(int friendId, long value) {
        try {
            UpdateBuilder<User, Long> updateBuilder = dao.updateBuilder();
            updateBuilder.updateColumnValue(User.Column.isBlocked, value);
            updateBuilder.where().eq(User.Column.ID, friendId);
            return updateBuilder.update();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return 0;
    }

    public User getFriendById(int friendId) {
        User message = null;

        try {
            QueryBuilder<User, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.ID, friendId).and().eq(User.Column.isBlocked, 1);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            Log.e("query", preparedQuery.getStatement());
            message = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return message;
    }

    public boolean isBlocked(int friendId) {
        return getFriendById(friendId) != null;
    }
}