package com.example.appv2.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.TypedArrayUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appv2.MainActivity;
import com.example.appv2.R;
import com.example.appv2.classes.Data_from_path_generator;
import com.example.appv2.classes.TableDinamic;

import java.io.File;
import java.util.Arrays;


public class Setting extends Fragment implements View.OnClickListener {

    private static final String TAG = "TAG";
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;

    // Fragment assets
    FragmentActivity listener;
    private Button setting_btn;
    private Button close_btn;
    private TextView text_list;
    private TextView noFilesText;
    private RecyclerView rv_setting;
    private ActivityResultLauncher<Intent> resultLauncher;
    private Data_from_path_generator my_path_generator;

    private Intent data;
    public String file_path_fixed="";
    public String folder_path_fixed="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View setting_fragment_view = inflater.inflate(R.layout.fragment_setting, container, false);
        return setting_fragment_view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        setting_btn = view.findViewById(R.id.settin_btn);
        close_btn = view.findViewById(R.id.close_btn);
        text_list = view.findViewById(R.id.textView);
        noFilesText = view.findViewById(R.id.nonFiles);
        rv_setting = view.findViewById(R.id.recycler_view);
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            data = result.getData();
                            file_path_fixed = fix_path(data.getData().getPath());
                            // Comentario
                            Bundle result1 = new Bundle();
                            result1.putString("df1", file_path_fixed);
                            Scan fragment_scan = new Scan();
                            getParentFragmentManager().setFragmentResult("dataFrom1",result1);

                        }
                    }
                });

        checkPermisos();




        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(checkPermisos()){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                resultLauncher.launch(intent);
            }else{
                RequestPermissions();
            }
        //  Anuncio emergente
        //  Toast.makeText(mcontext, "Permission (already) Granted!",Toast.LENGTH_SHORT).show();
            }
        });

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "adasdsadsadas",Toast.LENGTH_SHORT).show();
                System.exit(0);
                                           }                    });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!file_path_fixed.equals("")){
            folder_path_fixed = fix_folder_path(file_path_fixed);
            text_list.setText(folder_path_fixed);
            File folder = new File(folder_path_fixed);
            File[] in_folder_list = folder.listFiles();
            File[] folder_list_only;

            int k = 0;
            int count_files=0;

            for(int j= 0; j<in_folder_list.length; j++){
                File temp_file = in_folder_list[j];
                if(!temp_file.isDirectory()){
                    count_files++;
                }
            }
            folder_list_only = new File[count_files];

            for(int j= 0; j<in_folder_list.length; j++){
                File temp_file = in_folder_list[j];
                if(!temp_file.isDirectory()){
                    folder_list_only[k]=temp_file;
                    k++;
                }
            }

            if(in_folder_list==null || in_folder_list.length == 0){
                noFilesText.setVisibility(View.VISIBLE);
                return;
            }
            noFilesText.setVisibility(View.INVISIBLE);
            rv_setting.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_setting.setAdapter(new MyAdapter(getContext(),folder_list_only));

        }
        else{
            Log.d(TAG, "onStart: "+"NO IMPORTAR DATA, SALDRA ERROR");
        }
    }



    @Override
    public void onClick(View view) {
    }

    public void AlertaPermisos(String title,String message, final String permiso, final int permissionRequestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title).setMessage(message).
                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{permiso},permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private boolean checkPermisos() {
        // Permisos que requiere la app
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //  Permisos que tiene la app
        int realPermission = PackageManager.PERMISSION_GRANTED;
        if (permissionCheck != realPermission) {
            return false;
        } else {
            return true;
        }
    }

    private void RequestPermissions(){
        AlertaPermisos("Permission Needed", "Escritura de archivos",
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
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
        return  data_file_name;
    }

    public void listFilesForFolder(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                //rv_setting.add
                Log.d(TAG, "listFilesForFolder: " + fileEntry.getName());
                //System.out.println(fileEntry.getName());
            }
        }
    }


}


// getActivity()
// Devuelve la actividad en el fragment

//  getActivity().getApplicationContext()
// Para obtener el context()