package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ItemDetailActivity extends AppCompatActivity {

    ImageView qrcImage;
    String staffId = "";
    String itemCode = "";
    TextView borrowView, fineView, fineInfoView;
    EditText creatorText, borrowText, fineText, nameText, codeText, categoryText, descriptionText;
    Button payButton;
    FirebaseFirestore db;
    double fine = 0.0;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        staffId = (String) getIntent().getSerializableExtra("staff_id");
        itemCode = (String) getIntent().getSerializableExtra("item_code");
        pb=findViewById(R.id.pb);
        pb.setVisibility(View.INVISIBLE);

        creatorText = findViewById(R.id.creatorText);
        borrowText = findViewById(R.id.borrowText);
        fineText = findViewById(R.id.fineText);

        nameText = findViewById(R.id.nameText);
        categoryText = findViewById(R.id.categoryText);
        descriptionText = findViewById(R.id.descriptionText);
        codeText = findViewById(R.id.codeText);

        borrowView = findViewById(R.id.borrowView);
        fineView = findViewById(R.id.fineView);
        payButton = findViewById(R.id.payButton);
        fineInfoView = findViewById(R.id.fineInfoView);


        QRGEncoder qrgEncoder = new QRGEncoder(itemCode, null, QRGContents.Type.TEXT, 500);
        Bitmap bitmap = qrgEncoder.getBitmap();
        qrcImage = findViewById(R.id.qrcImage);
        qrcImage.setImageBitmap(bitmap);

        setReadOnly();
        setGone();
        GetDetails(itemCode);
    }

    public void endLoading(){
        pb.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public void startLoading(){
        pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void GetDetails(String itemCode) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Item").document(itemCode);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String name = snapshot.getString("ItemName");
                    String code = itemCode;
                    String category = snapshot.getString("ItemCategory");
                    String description = snapshot.getString("ItemDescription");
                    String staff = snapshot.getString("StaffId");
                    String borrowedBy = snapshot.getString("StudentId");
                    String returnDate = snapshot.getString("ReturnDate");

                    try {
                        DisplayDetails(name, code, category, description, staff, borrowedBy, returnDate);
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                }
            }
        });
    }

    private void DisplayDetails(String name, String code, String category, String description, String staff, String borrowedBy, String returnDate) throws ParseException {
        nameText.setText(name);
        codeText.setText(code);
        categoryText.setText(category);
        descriptionText.setText(description);

        creatorText.setText(staff);
        if (borrowedBy.length() != 0) {
            borrowView.setVisibility(View.VISIBLE);
            fineView.setVisibility(View.VISIBLE);
            borrowText.setVisibility(View.VISIBLE);
            fineText.setVisibility(View.VISIBLE);
            payButton.setVisibility(View.VISIBLE);
            fineInfoView.setVisibility(View.VISIBLE);

            borrowText.setText(borrowedBy);
            double fine = CheckFine(returnDate);
            if (fine == 0) {
                fineText.setText("RM0.00");
            } else {
                fineText.setText("RM" + fine);
            }
        }
    }

    private double CheckFine(String str_returnDate) throws ParseException {
        String DATE_FORMAT = "dd/MM/yyyy";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        Date today = new Date();
        String str_today = dateFormat.format(today);

        Date currentDate = new SimpleDateFormat(DATE_FORMAT).parse(str_today);
        Date returnDate = new SimpleDateFormat(DATE_FORMAT).parse(str_returnDate);

        int result = currentDate.compareTo(returnDate);

        DecimalFormat df2 = new DecimalFormat("#.##");

        if (result == 1) {
            // current date > return date | EXPIRED
            long differentTime = currentDate.getTime() - returnDate.getTime();
            long differenceDay = (differentTime / (1000 * 60 * 60 * 24)) % 365;
            long differentYear = TimeUnit.MILLISECONDS.toDays(differentTime) / 365l;
            fine = differenceDay * 0.20;
            if (differentYear != 0) {
                fine = fine + (365l * differentYear * 0.20);
            }
            fine = Double.parseDouble(df2.format(fine));
            return fine;
        }
        return fine;
    }

    public void UpdateFine(View view) {
        if (fine == 0) {
            Toast.makeText(ItemDetailActivity.this, "No fine detected.", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(ItemDetailActivity.this);
            alert.setTitle("Confirmation");
            alert.setMessage("Are you sure want to update the fine as paid?");
            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Update TRUE | Confirmed
                    startLoading();
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
                                    if (task.isSuccessful()) {
                                        endLoading();
                                        borrowText.setText("");
                                        setGone();
                                        fine = 0;
                                        Toast.makeText(ItemDetailActivity.this, "The fine has been paid.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        endLoading();
                                        Toast.makeText(ItemDetailActivity.this, "Paid failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Update TRUE | Cancelled
                    return;
                }
            });
            alert.create().show();
        }
    }

    public void UpdateDetails(View view) {

        nameText = findViewById(R.id.nameText);
        categoryText = findViewById(R.id.categoryText);
        descriptionText = findViewById(R.id.descriptionText);

        String name = nameText.getText().toString().trim();
        String category = categoryText.getText().toString().trim();
        String description = descriptionText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameText.setError("Equipment name cannot be null.");
            return;
        }
        if (TextUtils.isEmpty(category)) {
            categoryText.setError("Equipment category cannot be null.");
            return;
        }

        name = name.toUpperCase();
        category = category.toUpperCase();
        description = description.toUpperCase();

        startLoading();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Item")
                .document(itemCode)
                .update(
                        "ItemName", name,
                        "ItemCategory", category,
                        "ItemDescription", description
                )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            endLoading();
                            Toast.makeText(ItemDetailActivity.this, "The equipment has been updated.", Toast.LENGTH_SHORT).show();
                        } else {
                            endLoading();
                            Toast.makeText(ItemDetailActivity.this, "The equipment failed to update.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void DeleteDetails(View view) {
        borrowText = findViewById(R.id.borrowText);
        String studentId = borrowText.getText().toString().trim();
        AlertDialog.Builder alert = new AlertDialog.Builder(ItemDetailActivity.this);
        if (studentId.length() != 0) {
            alert.setTitle("Warning");
            alert.setMessage("The equipment is still being used. Make sure the item is returned then try again.");
            alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
        } else {
            alert.setTitle("Confirmation");
            alert.setMessage("Are you sure want to Delete this equipment?");
            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete TRUE | Confirmed
                    startLoading();
                    DeleteItem(itemCode);
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete TRUE | Cancelled
                    return;
                }
            });
        }
        alert.create().show();
    }

    private void DeleteItem(String itemCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Item").document(itemCode)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        endLoading();
                        Toast.makeText(ItemDetailActivity.this, "The equipment has been deleted.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ItemDetailActivity.this, DashboardStaffActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("staff_id", staffId);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        endLoading();
                        Toast.makeText(ItemDetailActivity.this, "The equipment failed to delete.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setReadOnly() {
        creatorText.setEnabled(false);
        borrowText.setEnabled(false);
        fineText.setEnabled(false);
        codeText.setEnabled(false);
    }

    private void setGone() {
        borrowText.setVisibility(View.GONE);
        fineText.setVisibility(View.GONE);
        borrowView.setVisibility(View.GONE);
        fineView.setVisibility(View.GONE);
        payButton.setVisibility(View.GONE);
        fineInfoView.setVisibility(View.GONE);
    }

    public void SaveQRC(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ItemDetailActivity.this);
        alert.setTitle("Info");
        alert.setMessage("This image will be saved in your device, continue?");
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startLoading();
                SaveImage(itemCode);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alert.create().show();
    }

    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");

                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

//    public boolean isReadStoragePermissionGranted() {
//        String TAG = "Storage Permission";
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG, "Permission is granted");
//
//                return true;
//            } else {
//                Log.v(TAG, "Permission is revoked");
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//                return false;
//            }
//        } else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG, "Permission is granted");
//            return true;
//        }
//    }

    private void SaveImage(String itemCode) {
        qrcImage = findViewById(R.id.qrcImage);
        BitmapDrawable drawable = (BitmapDrawable) qrcImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        String root = this.getApplicationContext().getExternalFilesDir(null).toString();
        if (isStoragePermissionGranted()) { // check or ask permission
            File myDir = new File(root, "/QR_code_images");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            String fname = itemCode + ".jpg";
            File file = new File(myDir, fname);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile(); // if file already exists will do nothing
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Toast.makeText(ItemDetailActivity.this, "Image saved.", Toast.LENGTH_SHORT).show();
                out.flush();
                out.close();
            } catch (Exception e) {
                endLoading();
                Toast.makeText(ItemDetailActivity.this, "Image failed to save.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            endLoading();
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, itemCode , String.valueOf(System.currentTimeMillis()));
        }else{
            endLoading();
        }
    }
}