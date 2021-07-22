package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class StudentProfileActivity extends AppCompatActivity {
    String student_id = "";
    TextView nameText, idText, programText, emailText, oldPassText, newPassText, ConfirmNewPassText, semesterText, classText;
    ImageView submitButton;
    FirebaseFirestore db;
    Student student = new Student();
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        student_id = (String) getIntent().getSerializableExtra("student_id");

        pb=findViewById(R.id.pb);
        pb.setVisibility(View.INVISIBLE);

        nameText=findViewById(R.id.nameText);
        idText=findViewById(R.id.idText);
        programText=findViewById(R.id.programText);
        emailText=findViewById(R.id.emailText);

        semesterText=findViewById(R.id.semesterText);
        classText=findViewById(R.id.classText);


        oldPassText=findViewById(R.id.oldPassText);
        newPassText=findViewById(R.id.newPassText);
        ConfirmNewPassText=findViewById(R.id.ConfirmNewPassText);

        submitButton=findViewById(R.id.submitButton);

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Student").document(student_id);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String studentName = snapshot.getString("StudentName");
                    String studentEmail = snapshot.getString("StudentEmail");
                    String studentProgram = snapshot.getString("StudentProgram");
                    String studentSemester = snapshot.getString("StudentSemester");
                    String studentClass = snapshot.getString("StudentClass");

                    student = new Student(student_id,studentName,studentEmail,studentProgram,studentSemester,studentClass);

                    nameText.setText(student.getStudent_name());
                    idText.setText(student.getStudent_id());
                    programText.setText(student.getStudent_program());
                    emailText.setText(student.getStudent_email());
                    semesterText.setText(student.getStudent_semester());
                    classText.setText(student.getStudent_class());

                }
                else{
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(StudentProfileActivity.this,"Logged out.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(StudentProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }

    public void submitPassword(View view){

        oldPassText=findViewById(R.id.oldPassText);
        newPassText=findViewById(R.id.newPassText);
        ConfirmNewPassText=findViewById(R.id.ConfirmNewPassText);

        String emailText_str = student.getStudent_email().trim();

        String oldPassText_str = oldPassText.getText().toString().trim();
        String newPassText_str = newPassText.getText().toString().trim();
        String ConfirmNewPassText_str = ConfirmNewPassText.getText().toString().trim();


        if(TextUtils.isEmpty(oldPassText_str)){
            oldPassText.setError("Required");
            return;
        }
        if(TextUtils.isEmpty(newPassText_str)){
            newPassText.setError("Required");
            return;
        }
        if(TextUtils.isEmpty(ConfirmNewPassText_str)){
            ConfirmNewPassText.setError("Required");
            return;
        }

        if(newPassText_str.length() < 6){
            newPassText.setError("Password length must be more than 6 characters.");
            return;
        }
        if(newPassText_str.equals(oldPassText_str)){
            newPassText.setError("New password must be different with old password.");
            return;
        }
        if(!newPassText_str.equals(ConfirmNewPassText_str)){
            ConfirmNewPassText.setError("Password is different with new password.");
            return;
        }

        startLoading();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(emailText_str, oldPassText_str);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassText_str).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        endLoading();
                                        Toast.makeText(StudentProfileActivity.this,"Password has been updated", Toast.LENGTH_LONG).show();
                                        oldPassText.setText("");
                                        newPassText.setText("");
                                        ConfirmNewPassText.setText("");
                                    } else {
                                        endLoading();
                                        Toast.makeText(StudentProfileActivity.this,"Password failed to update", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            endLoading();
                            oldPassText.setError("Incorrect password");
                        }
                    }
                });
    }

    public void scanBorrowItem(View view){
        Intent intent = new Intent(StudentProfileActivity.this, BorrowActivity.class);
        intent.putExtra("student_id", student.getStudent_id());
        startActivity(intent);
    }
    public void gotoList(View view){
        Intent intent = new Intent(StudentProfileActivity.this, DashboardActivity.class);
        intent.putExtra("student_id", student.getStudent_id());
        startActivity(intent);
    }
    public void signOut(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(StudentProfileActivity.this);
        alert.setTitle("Confirmation");
        alert.setMessage("Are you sure want to log out?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(StudentProfileActivity.this, MainActivity.class);
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