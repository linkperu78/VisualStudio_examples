package com.example.appv2.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.appv2.R;
import com.example.appv2.classes.Data_from_path_generator;
import com.example.appv2.classes.TableDinamic;

import java.util.ArrayList;

public class Scan extends Fragment {
    View view;
    public TextView text_missing;
    private String data_receiver="Missing";
    private String file_path="";
    private TableLayout table_data;
    private String[] Headers_table;
    private ArrayList<String[]> dataArray;
    private TableDinamic tableDinamic;
    private Data_from_path_generator data_path_generator;


    private TextView txtCell;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scan, container, false);
        getParentFragmentManager().setFragmentResultListener("dataFrom1", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String data = result.getString("df1");
                file_path = data;
                TextView textView = view.findViewById(R.id.txt_is_missing);
                textView.setText(data);

                if(!file_path.equals("")){
                    data_path_generator = new Data_from_path_generator(file_path);

                    Headers_table = data_path_generator.file_header;
                    dataArray = data_path_generator.file_data;
                }

                table_data = view.findViewById(R.id.table_layout);
                Log.d("TAG", " ----------- SE ESTAN INTENTANDO CREAR LA TABLA PERO NO SALE: ONCREATED");

                if(!file_path.equals("")){
                    Log.d("TAG", " ----------- SE ESTAN INTENTANDO CREAR LA TABLA PERO NO SALE: ONCREATED");
                    TableRow row = new TableRow(getContext());
                    for (int j = 0; j < 4 ; j++){
                        txtCell = new TextView(getContext());
                        txtCell.setGravity(Gravity.CENTER);
                        txtCell.setPadding(4,4,4,4);
                        txtCell.setTextSize(25);
                        txtCell.setText("HOLA");
                        row.addView(txtCell);
                    }
                    table_data.addView(row);

                }
                else {
                    Log.d("TAG", " ----------- NO SE LOGRO ONCREATED");
                }



            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    public void onStart() {
        super.onStart();

    }
}