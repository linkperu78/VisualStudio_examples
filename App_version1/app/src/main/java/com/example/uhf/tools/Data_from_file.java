package com.example.uhf.tools;

import android.util.Log;

import com.google.android.gms.common.util.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Data_from_file {
    public String file_path;
    public String[] header;
    public ArrayList<String[]>data = new ArrayList<>();
    public String data_raw;

    public Data_from_file(String input_file){
        this.file_path=input_file;
        data_raw = readText(file_path);
        header = data_raw.split("\n");
        header= filt_null(header);


        data = new ArrayList<>(header.length-1);
        String[] header_copy = header;

        header = header[0].split("\t");
        int h = header.length +1;
        header = Arrays.copyOf(header,h);
        header[h-1]="EPC Number";

        for (int i = 1; i <header_copy.length;i++){
            String[] data_nextline = header_copy[i].split("\t");
            data_nextline = EPC_generate(data_nextline,header);
            data.add(data_nextline);
        }
    }

    private String readText(String input)
    {
        File archivo = new File(input);
        StringBuilder text = new StringBuilder();
        
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(archivo));
            String line;
            while((line = bufferedReader.readLine() )!=null){
                    text.append(line);
                    text.append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }


    public String[] filt_null(String[] a){
        String[] data_filtered;
        int l = a.length;
        int len_data_filtered = l;
        for(int i=0; i<l;i++){
            String[] b = a[i].split("\t");
            if(b.length ==0)
            {
                len_data_filtered--;
            }
        }
        data_filtered = new String[len_data_filtered];
        int j = 0;
        for(int i=0; i<l;i++){
            String[] b = a[i].split("\t");
            if(!(b.length ==0))
            {
                data_filtered[j] = a[i];
                j++;
            }
        }
        return data_filtered;
    }


    private String[] EPC_generate(String[] data_without_EPC, String[] header_example){
        String[] data_with_EPC;
        String value_UPC;
        int len_1 = data_without_EPC.length;
        int pos_UPC = 0;
        for (int i =0 ; i < header_example.length ; i++)
        {
            if(header_example[i].equals("UPC Number")){
                pos_UPC = i;}
        }
        value_UPC = data_without_EPC[pos_UPC];
       // Log.d("TAG", "EPC_generate: -------------------  " + value_UPC);

        int UPC_high = 4*Integer.parseInt(value_UPC.substring(0,6));
        int UPC_low = 4*Integer.parseInt(value_UPC.substring(6,11));

        String UPC_high_text = Integer.toHexString(UPC_high);
        String UPC_low_text = Integer.toHexString(UPC_low);

        String EPC ="3034"+ UPC_high_text+""+UPC_low_text;
        EPC = EPC.toUpperCase(Locale.ROOT);

        data_with_EPC = Arrays.copyOf(data_without_EPC,len_1+1);
        data_with_EPC[len_1]=EPC;

        return data_with_EPC;
    }
}
