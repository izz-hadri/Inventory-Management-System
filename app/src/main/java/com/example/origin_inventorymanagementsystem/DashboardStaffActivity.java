package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class DashboardStaffActivity extends AppCompatActivity {
    TextView textHello;
    String staffId = "";
    RecyclerView recyclerView;
    StaffItemAdapter myAdapter;
    ArrayList<Item> list;
    FirebaseFirestore db;
    ImageView qrcImage;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_staff);
        staffId = (String) getIntent().getSerializableExtra("staff_id");
        pb=findViewById(R.id.pb);
        pb.setVisibility(View.INVISIBLE);

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Staff").document(staffId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String staffName = snapshot.getString("StaffName");
                    DisplayStaffDetails(staffName);
                    DisplayItemDetails(staffId);
                }
                else{
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(DashboardStaffActivity.this,"Logged out.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DashboardStaffActivity.this, StaffSignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }
    public void DisplayItemDetails(String staffId){
        recyclerView = findViewById(R.id.item_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list =  new ArrayList<>();
        myAdapter = new StaffItemAdapter(this,list, staffId);
        recyclerView.setAdapter(myAdapter);

        db = FirebaseFirestore.getInstance();
        db.collection("Item")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String itemId = document.getId();
                                String itemName = document.getString("ItemName");
                                String borrowedBy= document.getString("StudentId");
                                String category = document.getString("ItemCategory");
                                if(category.length() == 0){
                                    category = "Unborrowed";
                                }
                                String staffId = document.getString("StaffId");
                                Item item = new Item(itemId, itemName, category, borrowedBy, staffId);
                                list.add(item);
                            }
                            myAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(DashboardStaffActivity.this,"Error getting the documents.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void DisplayStaffDetails(String name){
        name = CapitalizeWords(name);
        String arr[] = name.split(" ", 2);
        String firstName = arr[0];
        textHello = findViewById(R.id.textHello);
        textHello.setText("Hello " + firstName + "!");
    }

    public void AddNewItem(View view){
        Intent intent = new Intent(DashboardStaffActivity.this, AddItemActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("staff_id", staffId);
        startActivity(intent);
    }

    public void signOut(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(DashboardStaffActivity.this);
        alert.setTitle("Confirmation");
        alert.setMessage("Are you sure want to log out?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DashboardStaffActivity.this, StaffSignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alert.create().show();
    }

    public void searchCode(View view){
        TextView searchCodeText = findViewById(R.id.searchCodeText);
        String searchCode = searchCodeText.getText().toString().trim();
        searchCode = searchCode.replaceAll("/","");

        if(TextUtils.isEmpty(searchCode)){
            searchCodeText.setError("Required.");
            return;
        }
        startLoading();
        searchCodeFirestore(searchCode);
    }

    private void searchCodeFirestore(String searchCode) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Item").document(searchCode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        endLoading();
                        Intent intent = new Intent(DashboardStaffActivity.this, ItemDetailActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("staff_id", staffId);
                        intent.putExtra("item_code", searchCode);
                        startActivity(intent);
                    }
                    else{
                        endLoading();
                        Toast.makeText(DashboardStaffActivity.this,"Not found.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    endLoading();
                }
            }
        });
    }

    public void AddStudent(View view){
        Intent intent = new Intent(DashboardStaffActivity.this, AddStudentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("staff_id", staffId);
        startActivity(intent);
    }

    public void AddStaff(View view){
        Intent intent = new Intent(DashboardStaffActivity.this, AddStaffActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("staff_id", staffId);
        startActivity(intent);
    }

    public String CapitalizeWords(String name){
        name = name.toLowerCase();
        char[] charArray = name.toCharArray();
        boolean foundSpace = true;
        for(int i = 0; i < charArray.length; i++) {
            if(Character.isLetter(charArray[i])) {
                if(foundSpace) {
                    charArray[i] = Character.toUpperCase(charArray[i]);
                    foundSpace = false;
                }
            }
            else {
                foundSpace = true;
            }
        }
        name = String.valueOf(charArray);
        return name;
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

    public void SaveQRCode(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(DashboardStaffActivity.this);
        alert.setTitle("Info");
        alert.setMessage("All QR code images will be saved in your device, continue?");
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startLoading();
                db = FirebaseFirestore.getInstance();
                db.collection("Item")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    int total_task = task.getResult().size();
                                    if(total_task == 0){
                                        endLoading();
                                        Toast.makeText(DashboardStaffActivity.this, "No equipment in the list.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    for(QueryDocumentSnapshot document : task.getResult()){
                                        String itemId = document.getId();
                                        QRGEncoder qrgEncoder = new QRGEncoder(itemId, null, QRGContents.Type.TEXT, 500);
                                        Bitmap bitmap = qrgEncoder.getBitmap();
                                        qrcImage = findViewById(R.id.qrcImage);
                                        qrcImage.setImageBitmap(bitmap);
                                        SaveImage(itemId);
                                    }
                                    endLoading();
                                    Toast.makeText(DashboardStaffActivity.this, "All image has been saved.", Toast.LENGTH_SHORT).show();
                                }else{
                                    endLoading();
                                    Toast.makeText(DashboardStaffActivity.this,"Error getting documents.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
                out.flush();
                out.close();
            } catch (Exception e) {
                Toast.makeText(DashboardStaffActivity.this, "Image failed to save.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, itemCode, String.valueOf(System.currentTimeMillis()));
        }
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
}