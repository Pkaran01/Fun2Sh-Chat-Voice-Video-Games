package com.ss.fun2sh.ui.adapters.base;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.users.model.QBUser;
import com.ss.fun2sh.ui.activities.base.BaseActivity;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;
import com.ss.fun2sh.utils.listeners.OnRecycleItemClickListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<T, VH extends BaseClickListenerViewHolder> extends RecyclerView.Adapter<VH> {

    private List<T> objectsList;
    protected final BaseActivity baseActivity;
    protected final LayoutInflater layoutInflater;
    protected final Resources resources;
    protected QBUser currentQbUser;

    // Package private because we need access in BaseViewHolder but not in child classes
    OnRecycleItemClickListener<T> onRecycleItemClickListener;

    public BaseRecyclerViewAdapter(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
        this.layoutInflater = LayoutInflater.from(baseActivity);
        resources = baseActivity.getResources();
        objectsList = new ArrayList<>();
    }

    public BaseRecyclerViewAdapter(BaseActivity baseActivity, List<T> objectsList) {
        this(baseActivity);
        this.objectsList = objectsList;
        currentQbUser = AppSession.getSession().getUser();
    }

    public void setList(List<T> items) {
        objectsList = items;
        this.notifyDataSetChanged();
    }

    public void addItem(T item) {
        objectsList.add(item);
        notifyItemInserted(objectsList.size() - 1);
    }

    public void addItem(int position, T item) {
        objectsList.add(position, item);
        notifyItemInserted(position);
    }

    public void addAll(Collection<T> collection) {
        objectsList.addAll(collection);
        notifyItemRangeChanged(objectsList.size() - collection.size(), collection.size());
    }

    public void removeItem(int position) {
        objectsList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItem(T item) {
        int position = objectsList.indexOf(item);
        if (position != -1) {
            objectsList.remove(item);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        objectsList.clear();
        notifyDataSetChanged();
    }

    public void setFilter(List<T> newData) {
        objectsList.clear();
        objectsList.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return objectsList.size();
    }

    public T getItem(int position) {
        try {
            return objectsList.get(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectsList.get(0);
    }

    public List<T> getAllItems() {
        return objectsList;
    }

    public boolean isEmpty() {
        return objectsList.size() == 0;
    }

    public void setOnRecycleItemClickListener(OnRecycleItemClickListener<T> onRecycleItemClickListener) {
        this.onRecycleItemClickListener = onRecycleItemClickListener;
    }

    protected void displayAvatarImage(String uri, HexagonImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    protected void displayGroupPhotoImage(String uri, HexagonImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    public void setNewData(List<T> newData) {
        objectsList = newData;
        notifyDataSetChanged();
    }
}