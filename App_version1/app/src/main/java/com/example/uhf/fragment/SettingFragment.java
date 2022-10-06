package com.example.uhf.fragment;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.adapter.MyRvAdapter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingFragment extends KeyDwonFragment{

    private UHFMainActivity mContext;
    private static final String TAG = "UHFReadTagFragment";
    private View my_setting_view;

    public TextView tv_data;
    private Button btn_select;
    public TextView tv_modified;
    public static TextView tv_folder_path;
    public static RecyclerView setting_rv;

    public String defaul_folder_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    public String my_actual_file_path;
    public String folder_path_fixed;
    public static MyRvAdapter rv_adapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(my_setting_view == null){
            my_setting_view = inflater.inflate(R.layout.setting_fragment,container,false);
        }
        return my_setting_view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        folder_path_fixed = my_actual_file_path;
        mContext = (UHFMainActivity) getActivity();
        mContext.currentFragment = this;
        tv_data= getView().findViewById(R.id.read_file);
        tv_modified = getView().findViewById(R.id.tv_date_modified);
        btn_select=getView().findViewById(R.id.btn_select);
        setting_rv=getView().findViewById(R.id.setting_rv);
        tv_folder_path=getView().findViewById(R.id.tv_nonFiles);
        my_actual_file_path=defaul_folder_path;
        folder_path_fixed=defaul_folder_path;

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folder_path_fixed = UHFMainActivity.folder_path;
                if(folder_path_fixed.equals(
                        Environment.getExternalStorageDirectory().getAbsolutePath())){
                    return;
                }
                String previous_folder_path = "";
                String[] parts_folder_path = folder_path_fixed.split("/");
                Log.d(TAG, "onClick: " + Arrays.toString(parts_folder_path));
                int parts_length = parts_folder_path.length;
                for (int i =1 ; i < parts_length-1; i++){
                    previous_folder_path+="/"+parts_folder_path[i];
                }

                rv_adapter = new MyRvAdapter(getContext(),previous_folder_path,SettingFragment.this);
                refresh_reciclerview(previous_folder_path);
                UHFMainActivity.folder_path = previous_folder_path;
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        folder_path_fixed = UHFMainActivity.folder_path;
        rv_adapter = new MyRvAdapter(getContext(),folder_path_fixed,SettingFragment.this);
        refresh_reciclerview(folder_path_fixed);

    }

    public void refresh_reciclerview(String folder_path){
        tv_folder_path.setText(folder_path);
        setting_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        setting_rv.setAdapter(rv_adapter);

    }

}
