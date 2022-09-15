package com.example.appv2.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.appv2.R;
import com.example.appv2.classes.Data_from_path_generator;
import com.example.appv2.classes.TableDinamic;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class Scan extends Fragment {

    View rootView;
    LayoutInflater inflaterGlobal;

    private TableDinamic tableDinamic;
    private ScrollView scrollViewV;
    private HorizontalScrollView scrollViewH;
    private TableLayout table_data;
    private TableRow tableRow;
    private TextView text_table;


    public TextView text_missing;
    private String data_receiver="Missing";

    private String file_path="";
    private String[] Headers_table;
    private ArrayList<String[]> dataArray;
    private Data_from_path_generator data_path_generator;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        inflaterGlobal = inflater;

        scrollViewV = rootView.findViewById(R.id.ScrollV);
        scrollViewH = rootView.findViewById(R.id.ScrollH);
        table_data  = rootView.findViewById(R.id.table_data);

        getParentFragmentManager().setFragmentResultListener("dataFrom1", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String data = result.getString("df1");
                file_path = data;
                TextView textView = rootView.findViewById(R.id.txt_is_missing);
                textView.setText(data);
                if(!file_path.equals("")){
                    data_path_generator = new Data_from_path_generator(file_path);
                    Headers_table = data_path_generator.file_header;
                    dataArray = data_path_generator.file_data;
                }

            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    public void onResume() {
        super.onResume();

        if(!file_path.equals("")) {
            Context my_table_context = this.getActivity();

            TableRow row = new TableRow(my_table_context);
            table_data.invalidate();
            table_data.removeAllViews();
            table_data.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(Color.BLACK);
            row.setPadding(10,10,10,10);

            for (int i = 0; i < Headers_table.length; i++) {
                TextView txt = new TextView(my_table_context);
                txt.setGravity(Gravity.CENTER);
                txt.setTextColor(Color.BLACK);
                txt.setBackgroundColor(Color.RED);
                txt.setTextSize(30);
                txt.setText(Headers_table[i]);
                txt.setPadding(20,20,20,20);

                TableRow.LayoutParams params =new TableRow.LayoutParams();
                params.setMargins(4,4,4,4);
                params.weight=1;
                row.addView(txt,params);
            }
            table_data.addView(row);
        }
    }




}