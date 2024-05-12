package com.example.to_doapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_doapp.Adapter.ToDoAdapter;
import com.example.to_doapp.model.ToDoModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class To_do extends AppCompatActivity implements OnDialogCloseListener {
    private static final String LOG_TAG = To_do.class.getName();
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private Button addBtn;
    private FirebaseFirestore firestore;
    private ToDoAdapter adapter;
    private List<ToDoModel> list;
    private Query query;
    private ListenerRegistration listenerRegistration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_to_do);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            Log.d(LOG_TAG, "Authenticated User");

        }else{
            Log.d(LOG_TAG, "Unauthenticated User");
            finish();
        }

        recyclerView = findViewById(R.id.recyclerView);
        addBtn = findViewById(R.id.addBtn);
        firestore = FirebaseFirestore.getInstance();

        SharedPreferences preferences = getSharedPreferences(RegisterActivity.PREF_KEY, MODE_PRIVATE);
        String username = preferences.getString("username", "User");
        TextView greetingTextView = findViewById(R.id.greeting);
        greetingTextView.setText("Hello, " + username + "!");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(To_do.this));

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(),  AddNewTask.TAG);
            }
        });
        list = new ArrayList<>();
        adapter = new ToDoAdapter(this, list);


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        showData();
        recyclerView.setAdapter(adapter);
        SimpleDateFormat sdf = new SimpleDateFormat("dd, MMM, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        TextView dateTextView = findViewById(R.id.textView6);
        dateTextView.setText(currentDate);
    }
    private void showData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            query = firestore.collection("task").whereEqualTo("userId", userId).orderBy("time", Query.Direction.DESCENDING);

            listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.w(LOG_TAG, "Listen failed.", error);
                        return;
                    }

                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            String id = documentChange.getDocument().getId();
                            ToDoModel toDoModel = documentChange.getDocument().toObject(ToDoModel.class).withId(id);

                            list.add(toDoModel);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    listenerRegistration.remove();
                }
            });
        } else {
            Log.d(LOG_TAG, "No user signed in");
            // Handle the case where no user is signed in
        }
    }



    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        list.clear();
        showData();
        adapter.notifyDataSetChanged();
    }
}