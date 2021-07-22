package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ReturnActivity extends AppCompatActivity {
    CodeScanner codeScanner;
    CodeScannerView scannerView;
    TextView resultData,clickAny;
    String studentId = null;
    String itemCode = null;
    int x = 10; // seconds
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return);
        studentId = (String) getIntent().getSerializableExtra("student_id");
        itemCode = (String) getIntent().getSerializableExtra("itemCode");

        scannerView = findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this,scannerView);
        resultData = findViewById(R.id.resultOfQr);
        clickAny = findViewById(R.id.clickAny);
        clickAny.setText("You have " + x + " seconds to scan the QR code to return the equipment.");
        clickAny.setVisibility(View.VISIBLE);

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String returnItemCode = result.toString();
                        returnItemCode = returnItemCode
                                .replace("/","")
                                .replace("..","");
                        CheckItemCode(studentId,itemCode,returnItemCode);
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
                Toast.makeText(ReturnActivity.this,"Camera permission is required.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }
    public void CheckItemCode(String stuId, String itemCode, String returnItemCode){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        resultData.setText("Item code: " + itemCode);
        DocumentReference docRef = db.collection("Item").document(itemCode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String studentId_item = document.getString("StudentId");
                        String itemName = document.getString("ItemName");
                        String returnDate = document.getString("ReturnDate");
                        AlertDialog.Builder alert = new AlertDialog.Builder(ReturnActivity.this);

                        if(studentId_item.equals(stuId)){
                            // Validate student borrowed the item TRUE
                            double fine = 0.00;
                            try {
                                fine = CheckFine(returnDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(fine == 0){
                                String dynamicQRcode = document.getString("ItemQRcodeDynamic");
                                if(returnItemCode.equals(dynamicQRcode)){
                                    // Return item GRANTED
                                    UpdateReturnItem(stuId,itemCode);
                                    gotoDashboard(studentId);
                                }
                                else{
                                    // Return item DENIED
                                    alert.setTitle("Alert");
                                    alert.setMessage("Incorrect QR code.");
                                    alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            clickAny.setText("Press any to continue");
                                            clickAny.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    alert.create().show();
                                }
                            }else{
                                alert.setTitle("Warning");
                                alert.setMessage("Please pay a fine of RM" + fine + " to the staff before returning the equipment, thank you.");
                                alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        gotoDashboard(studentId);
                                    }
                                });
                                alert.create().show();
                            }
                        }
                        else{
                            // Validate student borrowed the item FALSE
                            alert.setTitle("Alert");
                            alert.setMessage("Your Id not found in the equipment details.");
                            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    gotoDashboard(studentId);
                                }
                            });
                            alert.create().show();
                        }

                    }
                }
            }
        });
    }
    public double CheckFine(String str_returnDate) throws ParseException {
        String DATE_FORMAT = "dd/MM/yyyy";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        Date today = new Date();
        String str_today = dateFormat.format(today);

        Date currentDate = new SimpleDateFormat(DATE_FORMAT).parse(str_today);
        Date returnDate= new SimpleDateFormat(DATE_FORMAT).parse(str_returnDate);

        int result = currentDate.compareTo(returnDate);

        if(result == 1){
            // current date > return date | EXPIRED
            double fine = 0.0;
            DecimalFormat df2 = new DecimalFormat("#.##");
            long differentTime = currentDate.getTime() - returnDate.getTime();
            long differenceDay = (differentTime / (1000 * 60 * 60 * 24)) % 365;
            long differentYear = TimeUnit.MILLISECONDS.toDays(differentTime) / 365l;
            fine = differenceDay * 0.20;
            if(differentYear != 0){
                fine = fine + (365l * differentYear * 0.20);
            }
            fine = Double.parseDouble(df2.format(fine));
            return fine;
        }
        else{
            // current date < return date | NOT EXPIRED
            return 0;
        }
    }
    public void UpdateReturnItem(String stuID, String itemCode){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Item")
                .document(itemCode)
                .update(
                        "StudentId", "",
                        "BorrowDate", "",
                        "ReturnDate", "",
                        "ItemQRcodeDynamic", ""
                )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ReturnActivity.this, "The equipment has been returned." ,Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ReturnActivity.this, "Item " + itemCode + " is failed to return." ,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void gotoDashboard(String stu_id){
        //UpdateDynamicIdToNull(itemCode);
        Intent intent = new Intent(ReturnActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("student_id", stu_id);
        startActivity(intent);
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
                        gotoDashboard(studentId);

                    case DialogInterface.BUTTON_NEGATIVE:
                        return;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ReturnActivity.this);
        builder.setMessage(alertmessage)
                .setTitle("Confirmation")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}

