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

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends KeyDwonFragment{

    private UHFMainActivity mContext;
    private static final String TAG = "UHFReadTagFragment";
    private View my_setting_view;

    public TextView tv_data;
    public Button btn_clear;
    private Button btn_select;
    private Button btn_reset;
    private TextView tv_folder_path;
    private RecyclerView setting_rv;
    private ActivityResultLauncher<Intent> resultLauncher;

    private Intent data;
    public String defaul_folder_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    public String my_actual_file_path;
    public String folder_path_fixed;

    public String[] data_header;
    public ArrayList<String[]> data_list = new ArrayList<>();
    MyRvAdapter rv_adapter;



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
        btn_reset=getView().findViewById(R.id.btn_reset);
        btn_select=getView().findViewById(R.id.btn_select);
        setting_rv=getView().findViewById(R.id.setting_rv);
        tv_folder_path=getView().findViewById(R.id.tv_nonFiles);
        my_actual_file_path=defaul_folder_path;
        folder_path_fixed=defaul_folder_path;

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            data = result.getData();
                            my_actual_file_path = fix_path(data.getData().getPath());
                            folder_path_fixed = fix_folder_path(my_actual_file_path);
                            UHFMainActivity.folder_path=folder_path_fixed;

                        }
                    }
                });

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                resultLauncher.launch(intent);
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        folder_path_fixed = UHFMainActivity.folder_path;
        rv_adapter = new MyRvAdapter(getContext(),folder_path_fixed,SettingFragment.this);
        refresh_reciclerview(folder_path_fixed);

        Log.d(TAG, "onResume: " + my_actual_file_path);

    }

    public void refresh_reciclerview(String folder_path){
        tv_folder_path.setText(folder_path);
        setting_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        setting_rv.setAdapter(rv_adapter);

    }

    private String fix_path(String bug_path){
        String fixed_fixed = "";
        String[] index = bug_path.split(":");
        fixed_fixed = Environment.getExternalStorageDirectory() + "/"+index[1];
        return  fixed_fixed;
    }

    private String fix_folder_path(String fixed_path){
        int index_ext = fixed_path.lastIndexOf("/");
        String data_file_name = "/storage/";
        if(index_ext>0){
            data_file_name = fixed_path.substring(index_ext+1);
        }
        index_ext = fixed_path.length()-data_file_name.length();
        data_file_name = fixed_path.substring(0,index_ext);
        Log.d(TAG, "fix_folder_path: " + data_file_name);
        return  data_file_name;
    }




}
