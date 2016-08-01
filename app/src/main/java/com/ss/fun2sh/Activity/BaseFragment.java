package com.ss.fun2sh.Activity;

import android.support.v4.app.Fragment;

import java.lang.reflect.Field;

/**
 * Created by Karan on 7/26/2016.
 */

public class BaseFragment extends Fragment {
    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
