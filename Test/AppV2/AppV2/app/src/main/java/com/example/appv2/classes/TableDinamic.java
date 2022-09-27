package com.example.appv2.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class TableDinamic extends TableRow {
    private TableLayout tableLayout;
    private Context context;
    private String[] header;
    private ArrayList<String[]> data;
    private TableRow tableRow;
    private TextView txtCell;
    private int indexC;
    private int indexR;


    public TableDinamic(TableLayout tableLayout, Context context) {
        super(context);
        this.tableLayout = tableLayout;
        this.context = context;
    }

    public void addHeader(String[] header){
        this.header=header;
        CreateHeader();
    }

    public void addData(ArrayList<String[]> data){
        this.data = data;
        createDataTable();
    }

    private void newRow(){
        tableRow=new TableRow(context);
    }

    private void newCell(){
        txtCell = new TextView(context);
        txtCell.setGravity(Gravity.CENTER);
        txtCell.setPadding(4,4,4,4);
        txtCell.setTextSize(25);
    }

    private void CreateHeader(){
        indexC=0;
        newRow();
        while(indexC < header.length){
            newCell();
            txtCell.setText(header[indexC++]);
            txtCell.setBackgroundColor(Color.BLUE);
            tableRow.addView(txtCell,newTableRowParams());
            tableRow.setBackgroundColor(Color.BLACK);
            tableRow.setPadding(5,5,5,10);
        }
        tableLayout.addView(tableRow);
    }

    private void createDataTable(){
        String info;
        for(indexR = 1; indexR <= data.size(); indexR++){
            //Log.d("TAG", "createData " +indexR +" adasda :" + data.size());
            newRow();
            for (indexC=0;indexC <= header.length;indexC++){
                newCell();
                String[] colums=data.get(indexR-1);
                info = (indexC<colums.length)?colums[indexC]:"";
                txtCell.setText(info);
                txtCell.setBackgroundColor(Color.WHITE);
                tableRow.addView(txtCell,newTableRowParams());
                tableRow.setBackgroundColor(Color.BLACK);
                tableRow.setPadding(5,5,5,5);
            }
            tableLayout.addView(tableRow);
        }
    }

    private TableRow.LayoutParams newTableRowParams(){
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(1,1,1,1);
        params.weight=1;
        return  params;
    }
}



