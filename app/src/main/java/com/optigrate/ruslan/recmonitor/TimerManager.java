package com.optigrate.ruslan.recmonitor;

import android.util.Log;

public class TimerManager {

    private int f2t = 0;

    public int stringParser(String txt) {
        if (txt != null) {
            try {
                this.f2t = Integer.parseInt(txt);
            } catch (NumberFormatException e) {
                Log.e("TimeManager", e.getMessage());
            }
        }
        return this.f2t;
    }

    public String getTime(int time) {
        String result = "";
        int hours = time / 3600;
        int minutes = (time - (hours * 3600)) / 60;
        int seconds = (time - (hours * 3600)) - (minutes * 60);
        result = String.valueOf(hours) + ":";
        if (minutes < 10) {
            result = new StringBuilder(String.valueOf(result)).append("0").append(String.valueOf(minutes)).append(":").toString();
        } else {
            result = new StringBuilder(String.valueOf(result)).append(String.valueOf(minutes)).append(":").toString();
        }
        if (seconds < 10) {
            return new StringBuilder(String.valueOf(result)).append("0").append(String.valueOf(seconds)).toString();
        }
        return new StringBuilder(String.valueOf(result)).append(String.valueOf(seconds)).toString();
    }
}