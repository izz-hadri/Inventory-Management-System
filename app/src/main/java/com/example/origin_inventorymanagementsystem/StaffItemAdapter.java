package com.example.origin_inventorymanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.ArrayList;

public class StaffItemAdapter extends RecyclerView.Adapter<StaffItemAdapter.MyViewHolder>{
    Context context;
    ArrayList<Item> list;
    String staffId;

    public StaffItemAdapter(Context context, ArrayList<Item> list, String staffId) {
        this.context = context;
        this.list = list;
        this.staffId = staffId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_staff,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item =list.get(position);
        holder.itemName.setText(item.getItem_name());
        holder.itemId.setText(item.getItem_id());
        holder.borrow.setText(item.getItem_studentId());
        holder.creator.setText(item.getItem_staffId());
        holder.buttonDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ItemDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("staff_id", staffId);
                intent.putExtra("item_code", item.getItem_id());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemId, borrow, creator;
        ImageView buttonDetail;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            itemName = itemView.findViewById(R.id.nameText);
            itemId = itemView.findViewById(R.id.idText);
            borrow = itemView.findViewById(R.id.borrowText);
            creator = itemView.findViewById(R.id.creatorText);
            buttonDetail = itemView.findViewById(R.id.detailButton);
        }
    }
}
