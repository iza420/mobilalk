package com.example.to_doapp.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_doapp.AddNewTask;
import com.example.to_doapp.R;
import com.example.to_doapp.To_do;
import com.example.to_doapp.model.ToDoModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> toDoList;
    private To_do activity;
    private FirebaseFirestore firestore;


    public ToDoAdapter(To_do todoActivity, List<ToDoModel> toDoList) {
        this.toDoList = toDoList;
        this.activity = todoActivity;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.tasks, parent, false);
        firestore=FirebaseFirestore.getInstance();

        return new MyViewHolder(view);

    }
    public void deleteTask(int position){
        ToDoModel toDoModel=toDoList.get(position);
        firestore.collection("task").document(toDoModel.TaskId).delete();
        toDoList.remove(position);
        notifyItemRemoved(position);
    }
    public Context getContext(){
        return activity;
    }

    public void editTask(int position){
        ToDoModel toDoModel=toDoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task",toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("id", toDoModel.TaskId);

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());

    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = toDoList.get(position);
        if (toDoModel.getDue() != null && !toDoModel.getDue().equals("")){
            holder.DueDate.setText("Due On: "+ toDoModel.getDue());
        } else {
            holder.DueDate.setText("");
        }
        holder.checkbox.setText(toDoModel.getTask());

        holder.checkbox.setChecked(toBoolean(toDoModel.getStatus()));

        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    firestore.collection("task").document(toDoModel.TaskId).update("status",1);
                    deleteTask(holder.getAdapterPosition());
                }else{
                    firestore.collection("task").document(toDoModel.TaskId).update("status",0);
                }
            }
        });
    }

    private boolean toBoolean(int status){
        return status != 0;
    }
    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView DueDate;
        CheckBox checkbox;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            DueDate = itemView.findViewById(R.id.due_date);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}
