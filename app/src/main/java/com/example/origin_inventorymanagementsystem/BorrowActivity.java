package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BorrowActivity extends AppCompatActivity {
    CodeScanner codeScanner;
    CodeScannerView scannerView;
    TextView resultData,clickAny;
    String studentId = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow);
        studentId = (String) getIntent().getSerializableExtra("student_id");
        scannerView = findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this,scannerView);
        resultData = findViewById(R.id.resultOfQr);
        clickAny = findViewById(R.id.clickAny);
        clickAny.setText("Scan the QR code to borrow an equipment.");
        clickAny.setVisibility(View.VISIBLE);

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String itemCode = result.toString();
                        itemCode = itemCode
                                .replace("/","")
                                .replace("..","");
                        CheckItemCode(itemCode);
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                codeScanner.startPreview();
                resultData.setText(null);
                clickAny.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestForCamera();
    }

    @Override
    public void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    private void requestForCamera(){
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                codeScanner.startPreview();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(BorrowActivity.this,"Camera permission is required.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    public void CheckItemCode(String itemCode){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Item").document(itemCode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String itemName = document.getString("ItemName");
                        String studentId_item = document.getString("StudentId");
                        //resultData.setText("Item name: " + itemName);
                        AlertDialog.Builder alert = new AlertDialog.Builder(BorrowActivity.this);
                        if(studentId_item.isEmpty()){
                            // Borrow TRUE | Confirmation
                            alert.setTitle("Confirmation");
                            alert.setMessage("Are you sure want to borrow this equipment?");
                            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Borrow TRUE | Confirmed
                                    UpdateBorrowingItem(studentId,itemCode);
                                    gotoDashboard(studentId);
                                }
                            });
                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Borrow TRUE | Cancelled
                                    clickAny.setText("Press any to continue");
                                    clickAny.setVisibility(View.VISIBLE);
                                    return;
                                }
                            });
                        }
                        else{
                            // Borrow FALSE
                            alert.setTitle("Alert");
                            alert.setMessage("Unable to borrow. The item is still borrowed by a student.");
                            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clickAny.setText("Press any to continue");
                                    clickAny.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        alert.create().show();
                    }
                    else{
                        resultData.setText( itemCode + " not found in database.");
                        if(resultData != null){
                            clickAny.setText("Press any to continue");
                            clickAny.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }
    public void UpdateBorrowingItem(String stuID, String itemCode){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String DATE_FORMAT = "dd/MM/yyyy";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date currentDate = new Date();
        LocalDateTime localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        localDateTime = localDateTime.plusYears(0).plusMonths(0).plusDays(7);
        Date currentDatePlusSevenDay = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        db.collection("Item")
                .document(itemCode)
                .update(
                        "StudentId", stuID,
                        "BorrowDate", dateFormat.format(currentDate),
                        "ReturnDate", dateFormat.format(currentDatePlusSevenDay)
                )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(BorrowActivity.this,"Equipment is borrowed." ,Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(BorrowActivity.this,itemCode + " is failed to borrow." ,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void gotoDashboard(String stu_id){
        Intent intent = new Intent(BorrowActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("student_id", stu_id);
        startActivity(intent);
    }
}