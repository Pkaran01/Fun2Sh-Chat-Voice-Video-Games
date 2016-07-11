package com.ss.fun2sh.CRUD;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;

/**
 * Created by CRUD Technology on 10/4/2015.
 */

public class UserAccount {
    //for EditText Refrance
    public static EditText EditTextPointer;
    public static String errorMessage;
    private EditText userName, password;
    private Context mCont;

    public UserAccount(Context mCont, EditText un, EditText pw) {
        this.userName = un;
        this.password = pw;
        this.mCont = mCont;
        isLoginInit(userName, password);
    }

    private static void isLoginInit(EditText userName, EditText password) {
        int maxLength = 15;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        //this is for userName
        userName.setHint("This is my username");
        userName.setSingleLine(true);
        userName.setMaxLines(1);
        userName.setFilters(fArray);
        //this is for setDrawable left
        // userName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_outline_white_24dp, 0, 0, 0);

        //this is for password
        password.setHint("This is my password");
        password.setSingleLine(true);
        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setMaxLines(1);
        password.setFilters(fArray);
        //this is for setDrawable left
        // password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white_24dp, 0, 0, 0);
    }

    public static boolean isEmailValid(EditText tv) {
        //add your own logic
        if (TextUtils.isEmpty(tv.getText())) {
            return false;
        } else {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(tv.getText()).matches()) {
                return true;
            } else {
                EditTextPointer = tv;
                errorMessage = "Invalid Email Id";
                return false;
            }
        }
    }

    public static boolean isPasswordValid(EditText tv) {
        //add your own logic
        if (tv.getText().toString().length() >= 3) {
            return true;
        } else {
            EditTextPointer = tv;
            errorMessage = "Greater than 6 char";
            return false;
        }
    }

    public static boolean isEmpty(EditText... arg) {
        for (int i = 0; i < arg.length; i++) {
            if (arg[i].getText().length() <= 0) {
                EditTextPointer = arg[i];
                return false;
            }

        }
        return true;
    }
}
