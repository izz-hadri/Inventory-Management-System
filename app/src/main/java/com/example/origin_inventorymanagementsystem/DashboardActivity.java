package com.example.origin_inventorymanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {
    TextView textHello,empty;
    Student student = new Student();
    String studentId = "";
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ArrayList<Item> list;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        studentId = (String) getIntent().getSerializableExtra("student_id");
        empty=findViewById(R.id.empty);

        //-------- Firebase Firestore --------

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Student").document(studentId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String studentName = snapshot.getString("StudentName");
                    student = new Student(studentId,studentName,null,null,null,null);
                    DisplayStudentDetails(student);
                    DisplayItemBorrowDetails(studentId);
                }
                else{
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(DashboardActivity.this,"Logged out.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }
    public void DisplayItemBorrowDetails(String studentId){
        recyclerView = findViewById(R.id.item_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list =  new ArrayList<>();
        myAdapter = new MyAdapter(this,list, studentId);
        recyclerView.setAdapter(myAdapter);

        db.collection("Item")
                .whereEqualTo("StudentId",studentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int total = task.getResult().size();
                            if(total == 0){
                                empty.setVisibility(View.VISIBLE);
                            }else{
                                empty.setVisibility(View.INVISIBLE);
                            }
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String itemId = document.getId();
                                String itemName = document.getString("ItemName");
                                String borrowDate= document.getString("BorrowDate");
                                String returnDate = document.getString("ReturnDate");
                                Item item = new Item(itemId,itemName,borrowDate,returnDate);
                                list.add(item);
                            }
                            myAdapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(DashboardActivity.this,"Error getting documents.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void signOut(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(DashboardActivity.this);
        alert.setTitle("Confirmation");
        alert.setMessage("Are you sure want to log out?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
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
    public void DisplayStudentDetails(Student student){
        String name = student.getStudent_name();
        name = CapitalizeWords(name);
        String arr[] = name.split(" ", 2);
        String firstName = arr[0];
        textHello = (TextView) findViewById(R.id.textHello);
        textHello.setText("Hello " + firstName + "!");
    }
    public void scanBorrowItem(View view){
        Intent intent = new Intent(DashboardActivity.this, BorrowActivity.class);
        intent.putExtra("student_id", student.getStudent_id());
        startActivity(intent);
    }
    public void profileDetails(View view){
        Intent intent = new Intent(DashboardActivity.this, StudentProfileActivity.class);
        intent.putExtra("student_id", student.getStudent_id());
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
}