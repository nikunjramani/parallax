package com.parrallax.parallax;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract,ItemMoveCallback.ItemListner {

    private ArrayList<String> data;
    private Context context;
    private ItemMoveCallback.ItemListner itemListner;

    @Override
    public void onMove(ArrayList<String> arrayList) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View rowView;
        ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            imageView = itemView.findViewById(R.id.card_image);
        }
    }

    public RecyclerViewAdapter(Context context, ArrayList<String> data, ItemMoveCallback.ItemListner itemListner) {
        this.context=context;
        this.data = data;
        this.itemListner = itemListner;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        try {
            holder.imageView.setImageBitmap(uriToBitmap(Uri.parse(data.get(position))));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public Bitmap uriToBitmap(Uri uri) throws IOException {
        return  MediaStore.Images.Media.getBitmap(context.getContentResolver() , uri);
    }
    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        itemListner.onMove(data);
    }

    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.WHITE);

    }
}