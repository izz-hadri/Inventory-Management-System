package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    EditText idText, passwordText;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
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
    public void signIn(View view){
        idText=findViewById(R.id.idText);
        passwordText=findViewById(R.id.passwordText);
        String studentId = idText.getText().toString().trim();
        String studentPassword = passwordText.getText().toString().trim();
        studentId = studentId.replaceAll("/","");
        if(TextUtils.isEmpty(studentId)){
            idText.setError("Student ID is required.");
            return;
        }
        if(TextUtils.isEmpty(studentPassword)){
            passwordText.setError("Password is required.");
            return;
        }
        startLoading();
        ValidateId(studentId,studentPassword);
    }
    public void ValidateId(String studentId, String studentPassword){
        //-------- Firebase Firestore --------
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Student").document(studentId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String email = document.getString("StudentEmail");
                        String name = document.getString("StudentName");
                        AuthenticateUser(email,studentPassword, name,studentId);
                    }
                    else{
                        endLoading();
                        Toast.makeText(MainActivity.this,"Failed to sign in.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    endLoading();
                }
            }
        });
    }
    public void AuthenticateUser(String email, String password, String name, String studentId){
        String arr[] = name.split(" ", 2);
        String firstName = arr[0];
        //-------- Firebase Authentication --------
        fAuth=FirebaseAuth.getInstance();
        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    endLoading();
                    FirebaseUser user = fAuth.getCurrentUser();
                    updateUI(user,studentId);
                }else{
                    endLoading();
                    Toast.makeText(MainActivity.this,"Failed to sign in.",Toast.LENGTH_SHORT).show();
                    updateUI(null, null);
                }
            }
        });
    }
    public void StaffSignIn(View view){
        Intent intent = new Intent(MainActivity.this,StaffSignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public void updateUI(FirebaseUser account, String studentId){
        if(account != null){
            Intent intent = new Intent(MainActivity.this,DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("student_id", studentId);
            startActivity(intent);
        }
    }

}