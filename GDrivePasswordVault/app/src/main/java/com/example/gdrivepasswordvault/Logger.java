package com.example.gdrivepasswordvault;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

public class Logger {
    private static TextView statusBar;
    private static final  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static Activity activity;
    public static void setup(Activity act, TextView bar){
        statusBar = bar;
        activity = act;

    }
    public static void log(String s, boolean toast){
        activity.runOnUiThread(() -> {
            if(toast){
                Toast.makeText(activity.getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
            statusBar.append(String.format("%s::\t%s\n",dateFormat.format(new Date()), s));
        });

    }
}
