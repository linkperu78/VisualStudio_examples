package com.example.uhf.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.uhf.R;
import com.example.uhf.UhfInfo;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.NumberTool;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UHFReadTagFragment extends KeyDwonFragment {
    private static final String TAG = "UHFReadTagFragment";
    private boolean loopFlag = false;
    private List<String> tempDatas = new ArrayList<>();
    private List<String> tempData_realEPC = new ArrayList<>();
    MyAdapter adapter;
    Button BtClear;
    TextView tvTime;
    TextView tv_count;
    TextView tv_total;
    RadioButton RbInventorySingle;
    RadioButton RbInventoryLoop;

    Button BtInventory;
    ListView LvTags;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;

    private int total;
    private long time;


    private String[] estado_lector = {"Start","Stop","Clear"};
    private int estado_int;

    public static final String TAG_EPC = "tagEPC";
    public static final String TAG_EPC_TID = "tagEpcTID";
    public static final String TAG_COUNT = "tagCount";
    public static final String TAG_RSSI = "tagRssi";

    public ArrayList<String[]> data_from_file = new ArrayList<>();
    public String[] header_from_file;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            UHFTAGInfo info = (UHFTAGInfo) msg.obj;
            addDataToList(info.getEPC(),EPC_norm(info.getEPC()), info.getRssi());
            setTotalTime();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadTagFragment.onCreateView");
        return inflater.inflate(R.layout.uhf_readtag_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        estado_int=0;
        mContext = (UHFMainActivity) getActivity();
        mContext.currentFragment=this;
        BtClear = (Button) getView().findViewById(R.id.BtClear);
        tvTime = (TextView) getView().findViewById(R.id.tvTime);
        tvTime.setText("0s");
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        tv_total = (TextView) getView().findViewById(R.id.tv_total);

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);

        LvTags = (ListView) getView().findViewById(R.id.LvTags);
        adapter=new MyAdapter(mContext);
        LvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectItem(position);
                adapter.notifyDataSetInvalidated();
            }
        });
        LvTags.setAdapter(adapter);
        BtClear.setOnClickListener(new BtClearClickListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());


        //clearData();
        tv_count.setText(mContext.tagList.size()+"");
        tv_total.setText(total+"");
        Log.i(TAG, "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
    }


    @Override
    public void onPause() {
        Log.i(TAG, "UHFReadTagFragment.onPause");
        super.onPause();

        // 停止识别
        stopInventory();
    }

    /**
     * En esta funcion se guardan los datos en el diccionario "map"
     * y luego se agregan a tempData
     *
     */
    private void addDataToList(String epc,String epcAndTidUser, String rssi) {
        if (StringUtils.isNotEmpty(epc)) {
            String epc_tag = epc.substring(4,19);
            int index = checkIsExist(epc,tempDatas);
            int index_2 = checkIsExist(epc_tag,tempData_realEPC);
            map = new HashMap<String, String>();
            map.put(TAG_EPC, epc);
            map.put(TAG_EPC_TID, epcAndTidUser);
            map.put(TAG_COUNT, String.valueOf(1));
            map.put(TAG_RSSI, rssi);
            if (index   ==  -1) {
                mContext.tagList.add(map);
                tempDatas.add(epc);
                tv_count.setText(String.valueOf(adapter.getCount()));
                if(index_2  ==  -1) {
                    tempData_realEPC.add(epc_tag);
                }else{
                    int tagCount = Integer.parseInt(mContext.tagList.get(index).get(TAG_COUNT), 10) + 1;
                    map.put(TAG_COUNT, String.valueOf(tagCount));
                    map.put(TAG_EPC_TID, epcAndTidUser);
                    mContext.tagList.set(index, map);
                }
            }
            tv_total.setText(String.valueOf(++total));
            adapter.notifyDataSetChanged();

            //----------
            mContext.uhfInfo.setTempDatas(tempDatas);
            mContext.uhfInfo.setTagList(mContext.tagList);
            mContext.uhfInfo.setCount(total);
            mContext.uhfInfo.setTagNumber(adapter.getCount());
        }
    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
            selectItem=-1;
            mContext.uhfInfo=new UhfInfo();
        }
    }

    private void clearData() {
        tv_count.setText("0");
        tv_total.setText("0");
        tvTime.setText("0s");
        total = 0;
        mContext.tagList.clear();
        tempDatas.clear();
        tempData_realEPC.clear();
        adapter.notifyDataSetChanged();
    }



    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {

        switch (estado_int) {
            case 0: // Se presiona una vez el gatillo, comienza la lectura
                if (mContext.mReader.startInventoryTag()) {
                    loopFlag = true;
                    setViewEnabled(false);
                    time = System.currentTimeMillis();
                    new TagThread().start(); // Se inicia la lectura de los RFIDS en segundo plano
                    } else {
                    stopInventory();
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_open_fail);
                }
                break;
            case 1: // Se presiona dos veces el gatillo, se pausa la lectura
                stopInventory();
                setTotalTime();
                break;
            case 2: // Se presiona tres veces el gatillo, se reinicia la lectura
                clearData();
                selectItem=-1;
                mContext.uhfInfo=new UhfInfo();
                break;
            default:
                break;
        }

        estado_int++; estado_int = estado_int%3;
        BtInventory.setText(estado_lector[estado_int]);
    }

    /*      STOP READ
            stopInventory();
            setTotalTime();
     */

    private void setTotalTime() {
        float useTime = (System.currentTimeMillis() - time) / 1000.0F;
        tvTime.setText(NumberTool.getPointDouble(1, useTime) + "s");
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                BtInventory.setText(mContext.getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    /**
     * 判断EPC是否在列表中
     *
     * @param epc 索引
     * @return
     */
    public int checkIsExist(String epc, List<String> ListString_input) {
        if (StringUtils.isEmpty(epc)) {
            return -1;
        }
        return binarySearch(ListString_input, epc);
    }

    /**
     * 二分查找，找到该值在数组中的下标，否则为-1
     */
    static int binarySearch(List<String> array, String src) {
        int left = 0;
        int right = array.size() - 1;
        // 这里必须是 <=
        while (left <= right) {
            if (compareString(array.get(left), src)) {
                return left;
            } else if (left != right) {
                if (compareString(array.get(right), src))
                    return right;
            }
            left++;
            right--;
        }
        return -1;
    }



    static boolean compareString(String str1, String str2) {
        if (str1.length() != str2.length()) {
            return false;
        } else if (str1.hashCode() != str2.hashCode()) {
            return false;
        } else {
            char[] value1 = str1.toCharArray();
            char[] value2 = str2.toCharArray();
            int size = value1.length;
            for (int k = 0; k < size; k++) {
                if (value1[k] != value2[k]) {
                    return false;
                }
            }
            return true;
        }
    }

    class TagThread extends Thread {
        public void run() {
            UHFTAGInfo uhftagInfo;
            Message msg;
            while (loopFlag) {
                uhftagInfo = mContext.mReader.readTagFromBuffer();
                if (uhftagInfo != null) {
                    msg = handler.obtainMessage();
                    msg.obj = uhftagInfo;
                    handler.sendMessage(msg);
                    mContext.playSound(1);
                }
            }
        }
    }

    /** AQUI ESTA LA ESTRUCTURA DE COMO SE RECIBEN LOS DATOS PARA
     *  ALMACENARLOS

     */
    private String EPC_norm(String epc) {
        String data=epc;             // El numero EPC
        return  data;
    }

    @Override
    public void myOnKeyDwon() {
        readTag();
    }


    //-----------------------------
    private int  selectItem=-1;
    public final class ViewHolder {
        public TextView tvEPCTID;
        public TextView tvTagCount;
        public TextView tvTagRssi;
    }

    /**
     * ESTA CLASS ES LA QUE INGRESA DATOS AL LISTVIEW.
     * adapter = new MyAdapter(Context context);
     */

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        public int getCount() {
            // TODO Auto-generated method stub
            return mContext.tagList.size();
        }
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return mContext.tagList.get(arg0);
        }
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listtag_items, null);
                holder.tvEPCTID = (TextView) convertView.findViewById(R.id.TvTagUii);
                holder.tvTagCount = (TextView) convertView.findViewById(R.id.TvTagCount);
                holder.tvTagRssi = (TextView) convertView.findViewById(R.id.TvTagRssi);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvEPCTID.setText((String) mContext.tagList.get(position).get(TAG_EPC_TID));
            holder.tvTagCount.setText((String) mContext.tagList.get(position).get(TAG_COUNT));
            holder.tvTagRssi.setText((String) mContext.tagList.get(position).get(TAG_RSSI));

            if (position == selectItem) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lfile_colorPrimary));
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }
        public  void setSelectItem(int select) {
            if(selectItem==select){
                selectItem=-1;
                mContext.uhfInfo.setSelectItem("");
                mContext.uhfInfo.setSelectIndex(selectItem);
            }else {
                selectItem = select;
                mContext.uhfInfo.setSelectItem(mContext.tagList.get(select).get(TAG_EPC));
                mContext.uhfInfo.setSelectIndex(selectItem);
            }

        }
    }




}
