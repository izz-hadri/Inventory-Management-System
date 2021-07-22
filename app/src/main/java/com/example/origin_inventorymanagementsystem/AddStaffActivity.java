package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddStaffActivity extends AppCompatActivity {
    String staffId = "";
    FirebaseFirestore db;
    EditText idText,reIdText,nameText,emailText,passwordText,repasswordText;
    private FirebaseAuth fAuth;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);
        staffId = (String) getIntent().getSerializableExtra("staff_id");
        pb=findViewById(R.id.pb);
        pb.setVisibility(View.INVISIBLE);
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

    public void AddStaff(View view){
        idText = findViewById(R.id.idText);
        reIdText = findViewById(R.id.reIdText);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        repasswordText = findViewById(R.id.repasswordText);

        String id = idText.getText().toString().trim();
        String reId = reIdText.getText().toString().trim();
        String name = nameText.getText().toString().trim();
        String email= emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String repassword = repasswordText.getText().toString().trim();

        if(TextUtils.isEmpty(id)){
            idText.setError("Equipment name cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(reId)){
            reIdText.setError("Equipment category cannot be null.");
            return;
        }
        if(TextUtils.isEmpty(name)){
            nameText.setError("Equipment code cannot be null.");
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
        if(!repassword.equals(password)){
            repasswordText.setError("The Password is different, please retype again.");
            return;
        }
        if(password.length() < 6){
            passwordText.setError("Password must be more than 6 characters.");
            return;
        }
        name = name.toUpperCase();
        id = id.toUpperCase();
        id = id.replaceAll("/","");
        id = id.replaceAll("__","--");
        startLoading();
        ValidateId(id,name,email,password);
    }

    private void ValidateId(String id, String name, String email, String password) {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Staff").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        endLoading();
                        idText.setError("Staff ID " + id + " already exist.");
                        return;
                    }
                    else{
                        StoreFireStore(id,name,email,password);
                    }
                }else{
                    endLoading();
                }
            }
        });
    }

    private void StoreFireStore(String id, String name, String email, String password) {
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        Map<String, Object> signUpMap = new HashMap<>();
        signUpMap.put("StaffName",name);
        signUpMap.put("StaffCreator",staffId);
        signUpMap.put("StaffEmail",email);

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    db.collection("Staff").document(id)
                            .set(signUpMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    endLoading();
                                    Toast.makeText(AddStaffActivity.this,"Sign up successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AddStaffActivity.this,DashboardStaffActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("staff_id", staffId);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    endLoading();
                                    Toast.makeText(AddStaffActivity.this,"Failed to sign up.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }else{
                    endLoading();
                    emailText.setError("Authentication failed. " + email + " already exist");
                    return;
                }
            }
        });
    }

    public void ResetForm(View view){
        idText = findViewById(R.id.idText);
        reIdText = findViewById(R.id.reIdText);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        repasswordText = findViewById(R.id.repasswordText);

        idText.setText("");
        reIdText.setText("");
        nameText.setText("");
        emailText.setText("");
        passwordText.setText("");
        repasswordText.setText("");
    }
}