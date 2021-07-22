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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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

public class AddStudentActivity extends AppCompatActivity {
    String staffId = "";
    EditText idText,reIdText,nameText,emailText,passwordText,repasswordText,programText,semesterText,classText,reEmailText;

    private FirebaseAuth fAuth;
    private FirebaseFirestore db;
    ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
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
    public void AddStudent(View view){
        idText = findViewById(R.id.idText);
        reIdText = findViewById(R.id.reIdText);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        repasswordText = findViewById(R.id.repasswordText);

        programText = findViewById(R.id.programText);
        semesterText = findViewById(R.id.semesterText);
        classText = findViewById(R.id.classText);
        reEmailText = findViewById(R.id.reEmailText);

        String id = idText.getText().toString().trim();
        String reId = reIdText.getText().toString().trim();
        String name = nameText.getText().toString().trim();
        String email= emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String repassword = repasswordText.getText().toString().trim();

        String program = programText.getText().toString().trim();
        String semester = semesterText.getText().toString().trim();
        String class_ = classText.getText().toString().trim();
        String reEmail= reEmailText.getText().toString().trim();

        if(TextUtils.isEmpty(id)){
            idText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(reId)){
            reIdText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(name)){
            nameText.setError("cannot be null.");
            return;
        }

        if(TextUtils.isEmpty(program)){
            programText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(semester)){
            semesterText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(class_)){
            classText.setError("cannot be null.");
            return;
        }

        if(TextUtils.isEmpty(email)){
            emailText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(password)){
            passwordText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(repassword)){
            repasswordText.setError("cannot be null.");
            return;
        }


        if(!reId.equals(id)){
            reIdText.setError("The Id is different, please retype again.");
            return;
        }
        if(!reEmail.equals(email)){
            reEmailText.setError("The email is different, please retype again.");
            return;
        }
        if(!repassword.equals(password)){
            repasswordText.setError("The Password is different, please retype again.");
            return;
        }
        if(password.length() < 6){
            passwordText.setError("Password must be more than 6 characters.");
            return;
        }

        id = id.toUpperCase();
        id = id.replaceAll("/","");
        name = name.toUpperCase();
        program = program.toUpperCase();
        class_ = class_.toUpperCase();
        id = id.replaceAll("__","--");

        startLoading();
        ValidateId(id,name,email,password,program,semester,class_,1);

    }

    private void ValidateId(String id, String name, String email, String password, String program, String semester, String class_, int func) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Student").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(func==1){
                            endLoading();
                            idText.setError("Student ID " + id + " already exist.");
                        }
                        return;
                    }
                    else{
                        StoreFireStore(id,name,email,password,program,semester,class_,func);
                    }
                }else {
                    if(func==1) {
                        endLoading();
                    }
                }
            }
        });
    }

    private void StoreFireStore(String id, String name, String email, String password, String program, String semester, String class_, int func) {
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        Map<String, Object> signUpMap = new HashMap<>();

        signUpMap.put("Creator",staffId);
        signUpMap.put("StudentClass",class_);
        signUpMap.put("StudentEmail",email);

        signUpMap.put("StudentName",name);
        signUpMap.put("StudentProgram",program);
        signUpMap.put("StudentSemester",semester);

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    db.collection("Student").document(id)
                            .set(signUpMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if(func==1){
                                        // add one item
                                        endLoading();
                                        Toast.makeText(AddStudentActivity.this,"Student sign up successful.", Toast.LENGTH_SHORT).show();
                                        GotoDashboard();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if(func==1){
                                        endLoading();
                                        Toast.makeText(AddStudentActivity.this,"Failed to sign up student.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    if(func==1) {
                        endLoading();
                        emailText.setError("Authentication failed. " + email + " already exist");
                    }
                    return;
                }
            }
        });

    }
    public String SetDefaultPassword(String id, String name){
        name = name.toLowerCase();
        String defaultPassword = "";
        String first3name = "";
        String last3id = "";

        if (name.length() > 3)
        {
            first3name = name.substring(0, 3);
        }
        else
        {
            first3name = name; // get all name if name < 3
        }

        if (id.length() > 3)
        {
            last3id = id.substring(id.length() - 3);
        }
        else
        {
            last3id = id; // get all Id if Id < 3
        }

        defaultPassword = first3name + last3id;
        return defaultPassword;
    }

    public void DefaultPassword(View view){
        idText = findViewById(R.id.idText);
        reIdText = findViewById(R.id.reIdText);
        nameText = findViewById(R.id.nameText);

        passwordText = findViewById(R.id.passwordText);
        repasswordText = findViewById(R.id.repasswordText);

        String id = idText.getText().toString().trim();
        String reId = reIdText.getText().toString().trim();
        String name = nameText.getText().toString().trim().toLowerCase();

        if(TextUtils.isEmpty(id)){
            idText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(reId)){
            reIdText.setError("cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(name)){
            nameText.setError("cannot be null.");
            return;
        }
        if(!reId.equals(id)){
            reIdText.setError("The Id is different, please retype again.");
            return;
        }

        String defaultPassword = SetDefaultPassword(id,name);

        passwordText.setText(defaultPassword);
        repasswordText.setText(defaultPassword);
    }

    public void GotoDashboard(){
        Intent intent = new Intent(AddStudentActivity.this, DashboardStaffActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("staff_id", staffId);
        startActivity(intent);
    }

    public void ResetForm(View view){
        idText = findViewById(R.id.idText);
        reIdText = findViewById(R.id.reIdText);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        repasswordText = findViewById(R.id.repasswordText);

        programText = findViewById(R.id.programText);
        semesterText = findViewById(R.id.semesterText);
        classText = findViewById(R.id.classText);
        reEmailText = findViewById(R.id.reEmailText);


        idText.setText("");
        reIdText.setText("");
        nameText.setText("");
        emailText.setText("");
        passwordText.setText("");
        repasswordText.setText("");

        programText.setText("");
        semesterText.setText("");
        classText.setText("");
        reEmailText.setText("");
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
        image.setImageResource(R.drawable.example2);
        AlertDialog.Builder alert = new AlertDialog.Builder(AddStudentActivity.this);
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

    private void GetFile() {
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

                if(col != 6){
                    endLoading();
                    Toast.makeText(AddStudentActivity.this,"Make sure the file is following the format given.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // ROW 0 = HEADER
                for(int j=1; j<col; j++){
                    // Check header
                    String header = s.getCell(j,0).getContents();
                    if(j == 0){
                        if(!header.equalsIgnoreCase("STUDENT_NAME")){
                            endLoading();
                            Toast.makeText(AddStudentActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 1){
                        if(!header.equalsIgnoreCase("STUDENT_ID")){
                            endLoading();
                            Toast.makeText(AddStudentActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 2){
                        if(!header.equalsIgnoreCase("PROGRAM_CODE")){
                            endLoading();
                            Toast.makeText(AddStudentActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 3){
                        if(!header.equalsIgnoreCase("SEMESTER")){
                            endLoading();
                            Toast.makeText(AddStudentActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 4){
                        if(!header.equalsIgnoreCase("CLASS")){
                            endLoading();
                            Toast.makeText(AddStudentActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else if(j == 5){
                        if(!header.equalsIgnoreCase("STUDENT_EMAIL")){
                            endLoading();
                            Toast.makeText(AddStudentActivity.this,"Make sure the file is following the format given", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                for(int j=1; j<row; j++){
                    String s_id = s.getCell(1,j).getContents().trim(); // ID
                    if(s_id.length() == 0){
                        continue;
                    }
                    String s_name = s.getCell(0,j).getContents(); // Name
                    String s_program = s.getCell(2,j).getContents(); // Equipment Category
                    String s_semester = s.getCell(3,j).getContents(); // Equipment ID
                    String s_class = s.getCell(4,j).getContents(); // Equipment Name
                    String s_email = s.getCell(5,j).getContents(); // Equipment Category

                    s_id = s_id.replaceAll("/", "-");
                    s_id = s_id.replaceAll("__","--");

                    String password = SetDefaultPassword(s_id,s_name); // set default password

                    ValidateId(s_id,s_name,s_email,password,s_program,s_semester,s_class,2);
                }
                endLoading();
                Toast.makeText(AddStudentActivity.this,"All equipment has been added.", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                endLoading();
                Toast.makeText(AddStudentActivity.this,"Failed to select file, make sure to grant the storage permission.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (BiffException e) {
                endLoading();
                Toast.makeText(AddStudentActivity.this,"Unsupported format. Make sure the file format is '.xls' and not corrupt.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            GotoDashboard();
        }else{
            endLoading();
        }
    }
}