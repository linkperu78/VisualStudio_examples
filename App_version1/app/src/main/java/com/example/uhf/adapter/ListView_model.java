package com.example.uhf.adapter;

import android.widget.ListView;

public class ListView_model {
    private String header;
    private String data;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public ListView_model(String name_header, String value_data){
        this.header=name_header;
        this.data=value_data;
    }
}
