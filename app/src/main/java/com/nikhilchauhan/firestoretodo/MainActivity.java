package com.nikhilchauhan.firestoretodo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private  String currentUserID;
    FirebaseFirestore db;

    List<ToDo> toDoList = new ArrayList<>();
    RecyclerView listItem;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;

    public MaterialEditText textTitle, textDescription; //public so that can access from ListAdapter

    public  boolean isUpdate = false; // flag to check is update or new add
    public String idUpdate=""; //id of item need to update

    ListItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        db=FirebaseFirestore.getInstance();

        textTitle = findViewById(R.id.text_Title);
        textDescription = findViewById(R.id.text_Desc);
        fab = findViewById(R.id.fab);
        listItem = findViewById(R.id.list_Todo);
        listItem.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listItem.setLayoutManager(layoutManager);
        adapter = new ListItemAdapter(MainActivity.this,toDoList);
        listItem.setAdapter(adapter);

        loadData(); //Load data from Firestore


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add new
                if(!isUpdate){
                    if ((textTitle.getText().toString().trim()).isEmpty()||(textDescription.getText().toString().trim()).isEmpty()){
                        Toast.makeText(MainActivity.this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
                    }else{
                    setData(textTitle.getText().toString().trim(),textDescription.getText().toString().trim());
                    }
                }else{
                    updateData(textTitle.getText().toString(),textDescription.getText().toString());
                    isUpdate=!isUpdate; //Reseting flag
                }
            }
        });

    }


    private void loadData() {
        //final ProgressDialog loading = ProgressDialog.show(MainActivity.this, "Loading Item", "Please wait...");
        //loading.setCanceledOnTouchOutside(false);
        //loading.show();
        if(toDoList.size()>0)
            toDoList.clear(); //Remove old value
            db.collection(currentUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc:task.getResult()){
                            ToDo todo = new ToDo(doc.getString("id"),
                                    doc.getString("title"),
                                    doc.getString("description"));
                            toDoList.add(todo);
                        }
                        listItem.setAdapter(adapter);
                        //loading.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setData(String title, String description) {
        //Random id
        String id = UUID.randomUUID().toString();
        Map<String,Object> todo = new HashMap<>();
        todo.put("id",id);
        todo.put("title",title);
        todo.put("description",description);

        db.collection(currentUserID).document(id)
                .set(todo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                textTitle.setText(null);
                textDescription.setText(null);
                //Refresh data
                loadData();
            }
        });

    }


    private void deleteItem(int index) {
        db.collection(currentUserID)
                .document(toDoList.get(index).getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadData();
                    }
                });
    }

    private void updateData(final String title, final String description) {
        db.collection(currentUserID).document(idUpdate)
                .update("title",title,"description",description)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    }
                });
        //Realtime update refresh data
        db.collection(currentUserID).document(idUpdate)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        textTitle.setText(null);
                        textDescription.setText(null);
                        //Refresh data
                        loadData();
                    }
                });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals("DELETE"))
            deleteItem(item.getOrder());
        return super.onContextItemSelected(item);
    }


    @Override
    public void onStart(){
        super.onStart();
        //If User is already logged-in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            Intent loginIntent= new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }


}
