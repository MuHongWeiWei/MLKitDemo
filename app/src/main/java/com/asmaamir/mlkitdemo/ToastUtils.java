package com.asmaamir.mlkitdemo;

import android.app.Application;
import android.widget.Toast;

/**
 * Author: FlyWei
 * E-mail: tony91097@gmail.com
 * Date: 2021/4/15
 */

public class ToastUtils {
    private static Toast toast;
    private static Application sContext;
    public static void init(Application application) {
        sContext = application;
    }

    public static void showShort(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_SHORT);
        } else {
            toast.setText(sequence);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public static void showLong(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_LONG);
        } else {
            toast.cancel();
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.show();
    }

}