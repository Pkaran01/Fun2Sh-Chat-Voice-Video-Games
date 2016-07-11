package com.ss.fun2sh.CRUD;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import com.afollestad.materialdialogs.MaterialDialog;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by CRUD Technology on 9/17/2015.
 */
public class M {
    public static void I(Context cx, Class<?> startActivity, Bundle data) {
        Intent i = new Intent(cx, startActivity);
        if (data != null)
            i.putExtras(data);
        cx.startActivity(i);
    }

    public static void E(String msg) {
        if (true)
            Log.e("Log.E By CRUD", msg);
    }


    public static void T(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }
    public static void dSimple(Context c, String title) {
        new SweetAlertDialog(c)
                .setTitleText(title)
                .show();
    }


    public static void dError(Context c, String msg) {
        try {
            new SweetAlertDialog(c, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText(msg)
                    .show();
        } catch (Exception e) {
            E(e.getMessage());
        }
    }

    public static void dSuccess(Context c, String title, String msg) {
        new SweetAlertDialog(c, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .show();
    }

    public static SweetAlertDialog dConfiremSuccess(Context c, String msg, String confirmText, String cancleText) {

        SweetAlertDialog s = new SweetAlertDialog(c, SweetAlertDialog.SUCCESS_TYPE);
        s.setTitleText("Congratulation.!");
        s.setContentText(msg);
        s.setConfirmText(confirmText);
        s.setCancelText(cancleText);
                /*s.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                });*/
        s.show();
        return s;
    }

    public static SweetAlertDialog dConfirem(Context c, String msg, String confirmText, String cancleText) {

        SweetAlertDialog s = new SweetAlertDialog(c, SweetAlertDialog.WARNING_TYPE);
//        s.setTitleText("Are you sure ?");
//        s.setContentText(msg);
        s.setTitleText(msg);
        s.setContentText("Are you sure ?");
        s.setConfirmText(confirmText);
        s.setCancelText(cancleText);
                /*s.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                });*/
        s.show();
        return s;
    }

    public static MaterialDialog initProgressDialog(Context c) {
        return new MaterialDialog.Builder(c)
                .title("Please wait...")
                .autoDismiss(false)
                .content("Wait for a moment.")
                .progress(true, 0)
                .show();
    }
}