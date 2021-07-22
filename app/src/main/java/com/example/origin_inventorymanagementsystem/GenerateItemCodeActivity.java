package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class GenerateItemCodeActivity extends AppCompatActivity {
    boolean firstCheck = true;
    ImageView qrImage;
    String itemCode = "";
    String student_id = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_item_code);
        itemCode = (String) getIntent().getSerializableExtra("itemCode");
        student_id = (String) getIntent().getSerializableExtra("student_id");

        if(firstCheck){
            SetQRCtoNull(itemCode);
            firstCheck = false;
        }

        qrImage = findViewById(R.id.qrImage);
        qrImage.setImageResource(android.R.color.transparent);
        GenerateQRCode(itemCode);
        CheckDynamicQRCode(itemCode,student_id);
    }

    public void GenerateQRCode(String itemCode) {
        QRGEncoder qrgEncoder = new QRGEncoder(itemCode, null, QRGContents.Type.TEXT, 500);
        Bitmap qrBits = qrgEncoder.getBitmap();
        qrImage.setImageBitmap(qrBits);
    }

    public void CheckDynamicQRCode(String itemCode, String student_id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Item").document(itemCode);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String dynamicQRC = snapshot.getString("ItemQRcodeDynamic");
                    String stuId = snapshot.getString("StudentId");
                    if(stuId.equals("")){
                        Intent intent = new Intent(GenerateItemCodeActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("student_id", student_id);
                        startActivity(intent);
                    }
                    if(!dynamicQRC.equals("")){

                        if(stuId.equals(student_id)){
                            Intent intent = new Intent(GenerateItemCodeActivity.this, ReturnActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("student_id", student_id);
                            intent.putExtra("itemCode", itemCode);
                            startActivity(intent);
                        }
                    }
                }
            }
        });
    }
    public void SetQRCtoNull(String itemCode){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Item")
                .document(itemCode)
                .update(
                        "ItemQRcodeDynamic", ""
                )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            return;
                        }else{
                            AlertDialog.Builder alert = new AlertDialog.Builder(GenerateItemCodeActivity.this);
                            alert.setTitle("Warning");
                            alert.setMessage("Dynamic QR Code Failed to remove.");
                            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Borrow TRUE | Confirmed
                                    return;
                                }
                            });
                            alert.create().show();
                        }
                    }
                });
    }
}