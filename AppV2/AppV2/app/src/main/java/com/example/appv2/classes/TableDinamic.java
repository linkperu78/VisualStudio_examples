package com.example.appv2.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class TableDinamic extends TableRow {
    public TableDinamic(Context context, String user, String date, String score){
        super(context);

        TextView userView = new TextView(context);
        userView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        userView.setText(user);
        userView.setTextColor(0);
        userView.setTextSize(22);

        TextView dateView = new TextView(context);
        dateView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        dateView.setText(date);
        dateView.setTextColor(0);
        dateView.setTextSize(22);

        TextView scoreView = new TextView(context);
        scoreView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        scoreView.setText(score);
        scoreView.setTextColor(0);
        scoreView.setTextSize(22);

        addView(userView);
        addView(dateView);
        addView(scoreView);
    }
}




