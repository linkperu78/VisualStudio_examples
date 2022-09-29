package com.example.uhf.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.fragment.SettingFragment;
import com.example.uhf.fragment.UHFReadTagFragment;
import com.example.uhf.tools.Data_from_file;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyRvAdapter extends RecyclerView.Adapter<MyRvAdapter.ViewHolder>{

    Context context;
    File files;
    File[] filesAndFolders;
    String default_path;
    String file_path;
    String[] header_file;
    ArrayList<String[]> data_file;
    private Data_from_file my_data_from_file;


    public MyRvAdapter(Context context, String path, SettingFragment a){
        this.context = context;
        this.default_path=path;
        files = new File(default_path);
        this.filesAndFolders = files.listFiles();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reclycer_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File selectedFile = filesAndFolders[position];
        holder.textView.setText(selectedFile.getName());
        if(selectedFile.isDirectory()){
            holder.imageView.setImageResource(R.drawable.ic_baseline_folder_24);
        }else{
            holder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selectedFile.isDirectory()){
                    Log.d("TAG", "onClick: " + selectedFile.getAbsolutePath());
                    my_data_from_file = new Data_from_file(selectedFile.getAbsolutePath());
                    header_file = my_data_from_file.header;
                    data_file = my_data_from_file.data;
                    UHFMainActivity.data = data_file;
                    UHFMainActivity.header =header_file;
                    UHFMainActivity.file_name = "Archivo seleccionado:\n" + selectedFile.getName();
                    Toast.makeText(context, "INFORMACION SUBIDA", Toast.LENGTH_SHORT).show();

                }
                else{
                    file_path= selectedFile.getAbsolutePath();
                    Log.d("TAG", "onClick: " + selectedFile.getAbsolutePath());

                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Long last_modified = new File(default_path).lastModified();
                Toast.makeText(v.getContext(), "Last Modified: "+ day_month_year(last_modified),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return filesAndFolders.length;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name_text_view);
            imageView = itemView.findViewById(R.id.icon_view);
        }
    }

    public String getFile_path(){
        return file_path;
    }

    public String day_month_year(long Unix_value){
        String date = "No date detected";
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(Unix_value));
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int month = cal.get(Calendar.MONTH);
        int year  = cal.get(Calendar.YEAR);
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo", "Junio","Julio",
                          "Agosto","Setiembre","Octubre","Noviembre","Diciembre"};
        date = (day) + "/" + meses[month]+"/"+year;
        return date;
    }

    private int get_pos_string(String[] dataset, String name){
        int j=0;
        for (int i =0; i < dataset.length; i++){
            if(dataset[i].equals(name)){
                j =i;
            }
            else{
                j=-1;
            }
        }
        return j;
    }
}
