package com.example.appv2.classes;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Data_from_path_generator {
    private String TAG = "TAG";
    public ArrayList<String[]> file_data;
    public String[] file_header;

    public String file_folder_path;
    public String file_extension;
    public String file_path;
    public String file_name;

    // La clase recibe una ruta "folder/folder2/file.tsv" para generar data

    public Data_from_path_generator(String file_path_input){
        this.file_path = file_path_input;
        try {
            generate_data();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void generate_data() throws IOException {
        // Intentamos leer el archivo, si no es readable se lanza catch
        FileInputStream fileInputStream = null;
        fileInputStream = new FileInputStream(file_path);
        BufferedReader bfr =new BufferedReader(new InputStreamReader(fileInputStream));

        file_data = new ArrayList<>();
        String dataRow = bfr.readLine();
        String temp_header_data = dataRow;
        // Obtenemos los strings de las cabeceras de la primera linea del archivo
        file_header = temp_header_data.split("\t");

        while (dataRow != null){
            String[] dataRowVector = dataRow.split("\t"); // convertimos la linea separada por tabulaciones en vector String
            file_data.add(dataRowVector);                       // agregamos la linea leida convertida en vector
            dataRow = bfr.readLine();                           // pasamos a la siguiente linea en el texto
        }
        Log.d(TAG, "get_data_from_file: ------------ : SUCESS");

        // Tenemos la data en " file_data "
        // Tenemos las cabeceras en "file_header"
    }

    private void path_data_generate(){
        String data_real_path = file_path;
        String data_extension = null;
        String data_file_name="";
        String data_folder_path="";

        // Get extension of file selected
        int index_ext = data_real_path.lastIndexOf(".");
        if(index_ext>0){
            data_extension = data_real_path.substring(index_ext +1);
        }
        data_extension = "."+data_extension;

        // Get name of file with extension
        index_ext = data_real_path.lastIndexOf("/");
        if(index_ext>0){
            data_file_name = data_real_path.substring(index_ext+1);
        }

        // Get path of the folder where is the file selected
        index_ext = data_real_path.length()-data_file_name.length();
        data_folder_path = data_real_path.substring(0,index_ext);
        file_folder_path = data_folder_path;
        file_name = data_file_name;
        file_extension = data_extension;
    }


}
    /*
        String[] index = data_real_path.split(":");
        data_real_path = Environment.getExternalStorageDirectory() + "/"+index[1];

     */