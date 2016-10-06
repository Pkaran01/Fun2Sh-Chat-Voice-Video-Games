package com.ss.fun2sh.ui.activities.groupcall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.HexagonImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.users.model.QBUser;
import com.ss.fun2sh.R;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsAdapter extends BaseAdapter {

    private List<QBUser> opponents;
    private LayoutInflater inflater;
    public static int i;
    public List<QBUser> selected = new ArrayList<>();

    public OpponentsAdapter(Context context, List<QBUser> users) {
        this.opponents = users;
        this.inflater = LayoutInflater.from(context);
    }

    public List<QBUser> getSelected() {
        return selected;
    }

    public int getCount() {
        return opponents.size();
    }

    public QBUser getItem(int position) {
        return opponents.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private int getNumber(List<QBUser> opponents, QBUser user) {
        return opponents.indexOf(user);
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_list_item_opponents, null);
            holder = new ViewHolder();
            holder.avatar_imageview = (HexagonImageView) convertView.findViewById(R.id.avatar_imageview);
            holder.opponentsName = (TextView) convertView.findViewById(R.id.opponentsName);
            holder.opponentsRadioButton = (CheckBox) convertView.findViewById(R.id.opponentsCheckBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = opponents.get(position);


        if (user != null) {

            holder.opponentsName.setText(user.getFullName());
            //get Avatar by trick
            displayAvatarImage(user.getExternalId(), holder.avatar_imageview);
            holder.opponentsRadioButton.setOnCheckedChangeListener(null);
            holder.opponentsRadioButton.setChecked(selected.contains(user));

            holder.opponentsRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        i = user.getId();
                        selected.add(user);
                    } else {
                        if (i == user.getId()) {
                            i = 0;
                        }
                        selected.remove(user);
                    }
                }
            });
        }

        return convertView;
    }

    protected void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    public static class ViewHolder {
        HexagonImageView avatar_imageview;
        TextView opponentsName;
        CheckBox opponentsRadioButton;
    }

}
