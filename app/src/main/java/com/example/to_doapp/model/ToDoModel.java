package com.example.to_doapp.model;

public class ToDoModel extends TaskId{
    private String task, due;
    private boolean done; int status;

    public String getTask() {
        return task;
    }

    public String getDue() {
        return due;
    }

    public int getStatus() {
        return status;
    }
}
