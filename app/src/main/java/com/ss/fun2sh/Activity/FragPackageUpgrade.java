package com.ss.fun2sh.Activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.R;

public class FragPackageUpgrade extends Fragment {

    String url = "https://fun-joy.co.uk/FMA_PayNow.aspx?";
    ImageView img_package;
    String reg_type;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_package_upgrade, container, false);
        super.onCreate(savedInstanceState);

        TextView pay_CO = (TextView) v.findViewById(R.id.pay_CO);
        TextView pay_neteller = (TextView) v.findViewById(R.id.pay_neteller);
        img_package = (ImageView) v.findViewById(R.id.img_package);

        reg_type = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.reg_type);
        if (reg_type.contains("FREE")) {
            img_package.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.gift2)));
        } else if (reg_type.contains("STANDARD")) {
            img_package.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.gift)));
        } else if (reg_type.contains("PREMIUM")) {
            M.T(getActivity(), "PREMIUM");
        } else {

        }
        final String reg = PrefsHelper.getPrefsHelper().getPref(Const.App_Ver.reg_id);

        pay_CO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url1 = url + "ID=" + reg + "&PAY=CHECKOUT";
                Intent i = new Intent(getActivity(), WebsiteLandScap.class);
                i.putExtra("url", url1);//
                getActivity().startActivity(i);
            }
        });

        pay_neteller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url2 = url + "ID=" + reg + "&PAY=NETELLER";
                Intent i = new Intent(getActivity(), WebsiteLandScap.class);
                i.putExtra("url", url2);//
                getActivity().startActivity(i);
            }
        });

        return v;
    }
}
