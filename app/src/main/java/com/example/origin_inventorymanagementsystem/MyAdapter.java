package com.example.origin_inventorymanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Item> list;
    String studentId;

    public MyAdapter(Context context, ArrayList<Item> list, String studentId) {
        this.context = context;
        this.list = list;
        this.studentId = studentId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item =list.get(position);
        String fine_status = "";
        holder.itemName.setText(item.getItem_name());
        holder.itemId.setText(item.getItem_id());
        holder.borrowDate.setText(item.getItem_borrowDate());
        try {
            fine_status = CheckFine(item.getItem_returnDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.returnDate.setText(fine_status);
        holder.buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GenerateItemCodeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("student_id", studentId);
                intent.putExtra("itemCode", item.getItem_id());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemId, borrowDate, returnDate;
        ImageView buttonReturn;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            itemName = itemView.findViewById(R.id.nameText);
            itemId = itemView.findViewById(R.id.idText);
            borrowDate = itemView.findViewById(R.id.borrowText);
            returnDate = itemView.findViewById(R.id.returnText);
            buttonReturn = itemView.findViewById(R.id.returnButton);
        }
    }

    public String CheckFine(String str_returnDate) throws ParseException {
        String DATE_FORMAT = "dd/MM/yyyy";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        Date today = new Date();
        String str_today = dateFormat.format(today);

        Date currentDate = new SimpleDateFormat(DATE_FORMAT).parse(str_today);
        Date returnDate= new SimpleDateFormat(DATE_FORMAT).parse(str_returnDate);

        int result = currentDate.compareTo(returnDate);

        DecimalFormat df2 = new DecimalFormat("#.##");

        if(result == 1){
            // current date > return date | EXPIRED
            double fine = 0.0;
            long differentTime = currentDate.getTime() - returnDate.getTime();
            long differenceDay = (differentTime / (1000 * 60 * 60 * 24)) % 365;
            long differentYear = TimeUnit.MILLISECONDS.toDays(differentTime) / 365l;
            fine = differenceDay * 0.20;
            if(differentYear != 0){
                fine = fine + (365l * differentYear * 0.20);
            }
            fine = Double.parseDouble(df2.format(fine));
            String str_fine = "Fine: RM" + fine;
            return str_fine;
        }
        else{
            // current date < return date | NOT EXPIRED
            String str_range = "";
            long differentTime = returnDate.getTime() - currentDate.getTime();
            long differenceDay = (differentTime / (1000 * 60 * 60 * 24)) % 365;
            if(differenceDay == 1 || differenceDay == 0){
                str_range = differenceDay + " day left!";
            }else{
                str_range = differenceDay + " days left.";
            }
            return str_range;
        }
    }
}
