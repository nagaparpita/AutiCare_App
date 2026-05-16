package com.example.auticare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CommunicationAdapter extends ArrayAdapter<CommunicationItem> {

    public CommunicationAdapter(@NonNull Context context, @NonNull List<CommunicationItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }

        CommunicationItem item = getItem(position);

        ImageView itemImage = convertView.findViewById(R.id.item_image);
        TextView itemName = convertView.findViewById(R.id.item_name);

        if (item != null) {
            itemImage.setImageResource(item.getImageResource());
            itemName.setText(item.getName());
        }

        return convertView;
    }
}