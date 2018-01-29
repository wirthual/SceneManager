package edu.teco.scenemanager.adfmanagerclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by raphael on 10.08.16.
 */
public class AdfListAdapter extends ArrayAdapter<AdfDescription>{


    private List<AdfDescription> itemList;
    private Context context;

    public AdfListAdapter(List<AdfDescription> itemList, Context ctx) {
        super(ctx, android.R.layout.simple_list_item_1, itemList);
        this.itemList = itemList;
        this.context = ctx;
    }

    public int getCount() {
        if (itemList != null)
            return itemList.size();
        return 0;
    }

    public AdfDescription getItem(int position) {
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public long getItemId(int position) {
        if (itemList != null)
            return itemList.get(position).hashCode();
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.adf_row, parent);
        }

        AdfDescription adf = itemList.get(position);

        v.setTag(R.id.myId,adf);

        TextView text0 = (TextView) v.findViewById(R.id.name);
        text0.setText(adf.getDescription());

        TextView text = (TextView) v.findViewById(R.id.description);
        text.setText(adf.getName());

        TextView txt_uuid = (TextView) v.findViewById(R.id.uuid);
        txt_uuid.setText(adf.getUuid());

        TextView text1 = (TextView) v.findViewById(R.id.lat);
        text1.setText(String.valueOf(adf.getLat()));

        TextView text2 = (TextView) v.findViewById(R.id.lng);
        text2.setText(String.valueOf(adf.getLng()));

        TextView text3 = (TextView) v.findViewById(R.id.lvl);
        text3.setText(String.valueOf(adf.getLvl()));

        return v;

    }

    public List<AdfDescription> getItemList() {
        return itemList;
    }

    public void setItemList(List<AdfDescription> itemList) {
        this.itemList = itemList;
    }
}
