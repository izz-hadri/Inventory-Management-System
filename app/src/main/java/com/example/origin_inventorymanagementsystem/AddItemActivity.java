package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class AddItemActivity extends AppCompatActivity {
    String staffId = "";
    EditText nameText,codeText,recodeText,categoryText,descriptionText;
    private FirebaseFirestore db;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        staffId = (String) getIntent().getSerializableExtra("staff_id");
        pb=findViewById(R.id.pb);
        pb.setVisibility(View.INVISIBLE);
        if (isStoragePermissionGranted()) {
            System.out.println("Permission granted.");
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
    public void addItem(View view){
        nameText = findViewById(R.id.nameText);
        categoryText = findViewById(R.id.categoryText);
        descriptionText = findViewById(R.id.descriptionText);
        codeText = findViewById(R.id.codeText);
        recodeText = findViewById(R.id.recodeText);

        String name = nameText.getText().toString().trim();
        String category = categoryText.getText().toString().trim();
        String description = descriptionText.getText().toString().trim();
        String code = codeText.getText().toString().trim();
        String recode = recodeText.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            nameText.setError("Equipment name cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(category)){
            categoryText.setError("Equipment category cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(code)){
            codeText.setError("Equipment code cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(recode)){
            recodeText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(recode)){
            recodeText.setError("cannot be null.");
            return;
        }
        if(!recode.equals(code)){
            recodeText.setError("The code is different, please retype again.");
            return;
        }

        code = code.replaceAll("/", "-");
        code = code.replaceAll("__","--");

        startLoading();
        ValidateItem(code,name,category,description,1);
    }

    private void ValidateItem(String code, String name, String category, String description, int func) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Item").document(code);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(func == 1){
                            endLoading();
                            codeText.setError("Equipment code " + code + " already exist.");
                        }
                        return;
                    }
                    else{
                        StoreFireStore(code,name,category,description,func);
                    }
                }
                else{
                    if(func == 1){
                        endLoading();
                    }
                }
            }
        });
    }

    private void StoreFireStore(String code, String name, String category, String description, int func) {
        db = FirebaseFirestore.getInstance();
        Map<String, Object> addItemMap = new HashMap<>();
        addItemMap.put("BorrowDate","");
        addItemMap.put("ItemName",name.toUpperCase());
        addItemMap.put("ItemCategory",category.toUpperCase());
        addItemMap.put("ItemDescription",description.toUpperCase());
        addItemMap.put("ItemQRcodeDynamic","");
        addItemMap.put("ReturnDate","");
        addItemMap.put("StaffId",staffId);
        addItemMap.put("StudentId","");

        db.collection("Item").document(code)
                .set(addItemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(AddItemActivity.this,"Sign up successful.", Toast.LENGTH_SHORT).show();
                        System.out.println(code+" added.");
                        if(func==1){
                            // add one item
                            endLoading();
                            Toast.makeText(AddItemActivity.this,"Equipment has been added", Toast.LENGTH_SHORT).show();
                            gotoDashboard();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        endLoading();
                        Toast.makeText(AddItemActivity.this,"Failed to add.", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    public void ResetForm(View view){
        nameText = findViewById(R.id.nameText);
        categoryText = findViewById(R.id.categoryText);
        descriptionText = findViewById(R.id.descriptionText);
        codeText = findViewById(R.id.codeText);
        recodeText = findViewById(R.id.recodeText);

        nameText.setText("");
        categoryText.setText("");
        descriptionText.setText("");
        codeText.setText("");
        recodeText.setText("");
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

    public void AddBundle(View view){
        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.example);
        AlertDialog.Builder alert = new AlertDialog.Builder(AddItemActivity.this);
        alert.setTitle("Info");
        alert.setMessage("Make sure the file format is '.xls', set it compatible to workbook and follow the format as in the figure.");
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startLoading();
                GetFile();
            }
        }).setView(image);
        alert.create().show();
    }


    public void GetFile() {
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.xls$"))
                .withFilterDirectories(false)
                .withTitle("Select .xsl file")
                .withRequestCode(1)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            try{
                File file = new File(filePath);
                Workbook wb = Workbook.getWorkbook(file);
                Sheet s = wb.getSheet(0);
                int row = s.getRows();
                int col = s.getColumns();

                if(col != 4){
                    endLoading();
                    Toast.makeText(AddItemActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                    return;
                }
                // ROW 0 = HEADER
                for(int j=1; j<col; j++){
                    // Check header
                    String header = s.getCell(j,0).getContents();
                    if(j == 0){
                        if(!header.equalsIgnoreCase("SERIAL_CODE")){
                            endLoading();
                            Toast.makeText(AddItemActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 1){
                        if(!header.equalsIgnoreCase("NAME")){
                            endLoading();
                            Toast.makeText(AddItemActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 2){
                        if(!header.equalsIgnoreCase("CATEGORY")){
                            endLoading();
                            Toast.makeText(AddItemActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 3){
                        if(!header.equalsIgnoreCase("DESCRIPTION")){
                            endLoading();
                            Toast.makeText(AddItemActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                for(int j=1; j<row; j++){
                    String item_id = s.getCell(0,j).getContents().trim(); // Equipment ID
                    if(item_id.length() == 0){
                        continue;
                    }
                    String item_name = s.getCell(1,j).getContents().trim(); // Equipment Name
                    String item_category = s.getCell(2,j).getContents().trim(); // Equipment Category
                    String item_Description = s.getCell(3,j).getContents().trim(); // Equipment ID

                    item_id = item_id.replaceAll("/", "-");
                    item_id = item_id.replaceAll("__","--");

                    ValidateItem(item_id,item_name,item_category,item_Description,2);
                }
                endLoading();
                Toast.makeText(AddItemActivity.this,"All equipment has been added", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                endLoading();
                Toast.makeText(AddItemActivity.this,"Failed to select file, make sure to grant the storage permission.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (BiffException e) {
                endLoading();
                Toast.makeText(AddItemActivity.this,"Unsupported format. Make sure the file format is '.xls' and not corrupt.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            gotoDashboard();
        }else{
            endLoading();
        }
    }

    public void gotoDashboard(){
        Intent intent = new Intent(AddItemActivity.this,DashboardStaffActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("staff_id", staffId);
        startActivity(intent);
    }
}