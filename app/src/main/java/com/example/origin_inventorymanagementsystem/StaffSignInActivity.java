package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StaffSignInActivity extends AppCompatActivity {
    EditText idText, passwordText;
    Switch switchReturnDashboard;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    int countSignin = 0;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_sign_in);
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
        int destination = 0;
        idText = findViewById(R.id.idText);
        passwordText = findViewById(R.id.passwordText);
        switchReturnDashboard = (Switch) findViewById(R.id.switchReturnDashboard);
        String staffId = idText.getText().toString().trim();
        String staffPassword = passwordText.getText().toString().trim();
        staffId = staffId.replaceAll("/","");

        if(TextUtils.isEmpty(staffId)){
            idText.setError("Staff ID is required.");
            return;
        }
        if(TextUtils.isEmpty(staffPassword)){
            passwordText.setError("Password is required.");
            return;
        }
        if(switchReturnDashboard.isChecked()){
            destination = 1; // goto return item dashboard
        }else{
            destination = 0; // goto staff dashboard
        }
        if(countSignin > 5){
            if(countSignin > 6){
                countSignin = countSignin + 2;
            }
            Toast.makeText(StaffSignInActivity.this,"Too many attempt, wait until " + (countSignin * 10) + " seconds",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    countSignin = 0;
                }
            }, countSignin * 10000);
        }else{
            startLoading();
            ValidateId(staffId,staffPassword,destination);
        }
    }

    public void ValidateId(String staffId, String staffPassword, int destination){
        //-------- Firebase Firestore --------
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Staff").document(staffId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String email = document.getString("StaffEmail");
                        String name = document.getString("StaffName");
                        AuthenticateUser(email,staffPassword, name,staffId, destination);
                    }
                    else{
                        endLoading();
                        countSignin++;
                        Toast.makeText(StaffSignInActivity.this,"Failed to sign in.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    endLoading();
                }
            }
        });
    }

    public void AuthenticateUser(String email, String password, String name, String staffId, int destination){
        String arr[] = name.split(" ", 2);
        String firstName = arr[0];
        //-------- Firebase Authentication --------
        fAuth=FirebaseAuth.getInstance();
        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    endLoading();
                    countSignin = 0;
                    FirebaseUser user = fAuth.getCurrentUser();
                    updateUI(user,staffId, destination);
                }else{
                    endLoading();
                    countSignin++;
                    Toast.makeText(StaffSignInActivity.this,"Failed to sign in.",Toast.LENGTH_SHORT).show();
                    updateUI(null, null, -1);
                }
            }
        });
    }

    public void updateUI(FirebaseUser account, String staffId, int destination){
        if(account != null){
            idText.setText("");
            passwordText.setText("");
            // goto return item dashboard
            if(destination == 1){
                Intent intent = new Intent(StaffSignInActivity.this,DashboardReturnItemActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("itemCode", "");
                startActivity(intent);
            }
            // goto staff dashboard
            else if(destination == 0){
                Intent intent = new Intent(StaffSignInActivity.this,DashboardStaffActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("staff_id", staffId);
                startActivity(intent);
            }
        }
    }

    public void StudentSignIn(View view){
        Intent intent = new Intent(StaffSignInActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}