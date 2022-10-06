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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.fragment.SettingFragment;
import com.example.uhf.fragment.UHFReadTagFragment;
import com.example.uhf.tools.Data_from_file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    private SettingFragment settingFragment;


    public MyRvAdapter(Context context, String path, SettingFragment a){
        this.context = context;
        this.default_path=path;
        this.settingFragment=a;
        files = new File(default_path);
        this.filesAndFolders = files.listFiles();
        int lon_array=filesAndFolders.length;
        int[] position_valid_files = new int[lon_array];
        int[] position_folder = new int[lon_array];
        int pos_valid_files = 0;
        int pos_valid_folder = 0;
        if(filesAndFolders.length>0){
            for(int i=0; i<lon_array; i++){
                String extension= "";
                File ab = filesAndFolders[i];
                String name = ab.getName();
                int pos = name.lastIndexOf(".");
                if(!(pos>0)){
                    position_folder[pos_valid_folder] =i;
                    pos_valid_folder++; continue;}
                extension = name.substring(pos);
                Log.d("TAG", "MyRvAdapter: " + extension);
                if(extension.equals(".txt")||extension.equals(".tsv")){
                    position_valid_files[pos_valid_files]=i;
                    pos_valid_files++;
                }
            }
        }
        position_valid_files=Arrays.copyOf(position_valid_files,pos_valid_files);
        position_folder=Arrays.copyOf(position_folder,pos_valid_folder);
        File[] temp_files_and_folder = new File[pos_valid_folder+pos_valid_files];
        int position_temp =0;
        for(int i =0; i<pos_valid_folder; i++){
            int lugar = position_folder[i];
            temp_files_and_folder[position_temp] = filesAndFolders[lugar];
            position_temp++;
        }
        for(int i =0; i<pos_valid_files; i++){
            int lugar = position_valid_files[i];
            temp_files_and_folder[position_temp] = filesAndFolders[lugar];
            position_temp++;
        }
        this.filesAndFolders = Arrays.copyOf(temp_files_and_folder,temp_files_and_folder.length);

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
                    my_data_from_file = new Data_from_file(selectedFile.getAbsolutePath());
                    if(!my_data_from_file.correct_lecture){
                        Toast.makeText(context, "Archivo no valido\nNo hay UPC Number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    header_file = my_data_from_file.header;
                    data_file = my_data_from_file.data;
                    UHFMainActivity.data = data_file;
                    UHFMainActivity.header =header_file;
                    UHFMainActivity.file_name = "Archivo seleccionado:\n" + selectedFile.getName();
                    Long last_modified = selectedFile.lastModified();
                    String mensaje_date = selectedFile.getName() + "\n"+ day_month_year(last_modified);
                    settingFragment.tv_modified.setText(mensaje_date);
                    Toast.makeText(context, "INFORMACION SUBIDA", Toast.LENGTH_SHORT).show();

                }
                else{
                    String folder_path = selectedFile.getAbsolutePath();
                    SettingFragment.rv_adapter = new MyRvAdapter(context,folder_path,settingFragment);
                    SettingFragment.rv_adapter.notifyDataSetChanged();
                    SettingFragment.tv_folder_path.setText(folder_path);
                    UHFMainActivity.folder_path=folder_path;
                    SettingFragment.setting_rv.setLayoutManager(new LinearLayoutManager(context));
                    SettingFragment.setting_rv.setAdapter(SettingFragment.rv_adapter);
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

    private String day_month_year(long Unix_value){
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

}
