package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.nio.charset.Charset;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class DashboardReturnItemActivity extends AppCompatActivity {
    ImageView qrImage,searchButton;
    TextView clickAny;
    String itemCode = "";
    int x = 10; // seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_return_item);
        itemCode = (String) getIntent().getSerializableExtra("itemCode");
        qrImage = findViewById(R.id.qrImage);

        clickAny = findViewById(R.id.clickAny);
        searchButton = findViewById(R.id.searchButton);

        CheckProgressInFirestore(itemCode);

        if(!itemCode.equals("")){
            CheckInFirestore(itemCode);
        }
    }

    public void ScanCodeItem(View view){
        Intent intent = new Intent(DashboardReturnItemActivity.this, ReturnCheckCodeItemActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void CheckInFirestore(String itemCode){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Item").document(itemCode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String studentId = document.getString("StudentId");
                        if(studentId.isEmpty()){
                            AlertDialog.Builder alert = new AlertDialog.Builder(DashboardReturnItemActivity.this);
                            alert.setTitle("Alert");
                            alert.setMessage("Equipment is not borrowed by anyone.");
                            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Borrow TRUE | Confirmed
                                    return;
                                }
                            });
                            alert.create().show();
                        }else{
                            UpdateDynamicId(itemCode);
                        }
                    }
                    else{
                        AlertDialog.Builder alert = new AlertDialog.Builder(DashboardReturnItemActivity.this);
                        alert.setTitle("Alert");
                        alert.setMessage("Equipment code not found in the database. Make sure the QR code is from the list you borrowed.");
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
            }
        });
    }

    public void UpdateDynamicId(String itemCode){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String randomId = GenerateRandomId();

        db.collection("Item")
                .document(itemCode)
                .update(
                        "ItemQRcodeDynamic", randomId
                )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(DashboardReturnItemActivity.this,"You have " + x + " seconds to scan the QR code." ,Toast.LENGTH_SHORT).show();
                            GenerateDynamicQRCode(randomId);
                            setNullAfter_Second();
                        }else{
                            AlertDialog.Builder alert = new AlertDialog.Builder(DashboardReturnItemActivity.this);
                            alert.setTitle("Warning");
                            alert.setMessage("Dynamic QR Code Failed to generate.");
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

    public void setNullAfter_Second(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!itemCode.equals("")){
                    UpdateDynamicIdToNull();
                }
            }
        }, x * 1000); // x seconds
    }

    public void UpdateDynamicIdToNull(){
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
                            itemCode = "";
                            qrImage.setImageResource(android.R.color.transparent);
                            return;
                        }else{
                            AlertDialog.Builder alert = new AlertDialog.Builder(DashboardReturnItemActivity.this);
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

    public void GenerateDynamicQRCode(String randomId){
        QRGEncoder qrgEncoder = new QRGEncoder(randomId,null, QRGContents.Type.TEXT,500);
        Bitmap qrBits = qrgEncoder.getBitmap();
        qrImage.setImageBitmap(qrBits);
    }

    public void CheckProgressInFirestore(String itemCode){
        if(!itemCode.equals("")){
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
                        String stuId = snapshot.getString("StudentId");
                        String dynamicQRC = snapshot.getString("ItemQRcodeDynamic");
                        if(stuId.equals("") && dynamicQRC.equals("")){
                            qrImage.setImageResource(android.R.color.transparent);
                        }
                    }
                }
            });
        }
        return;
    }

    public String GenerateRandomId(){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }

    @Override
    public void onBackPressed() {
        IsFinish("Are you sure want to close this section?");
    }

    public void Exit(View view){
        IsFinish("Are you sure want to close this section?");
    }

    public void IsFinish(String alertmessage) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(DashboardReturnItemActivity.this,StaffSignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardReturnItemActivity.this);
        builder.setMessage(alertmessage)
                .setTitle("Confirmation")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}