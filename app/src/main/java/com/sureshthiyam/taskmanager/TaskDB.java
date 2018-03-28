package com.sureshthiyam.taskmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Cyb3r El3c7r0 on 09-02-2018.
 */

public class TaskDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="TaskDB";
    public static final int DATABASE_VERSION=1;

    public TaskDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE Task(ID INTEGER PRIMARY KEY,title CHAR,description CHAR,summary CHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Task");
            onCreate(db);
    }

    public void AddTask(String title,String desc,String Summary){

        getWritableDatabase().execSQL("INSERT INTO TASK(title,description,summary) VALUES('"+title+"','"+desc+"','"+Summary+"')");
    }
    public void UpdateTask(String id,String title,String desc,String Summary){

        getWritableDatabase().execSQL("UPDATE Task SET title='"+title+"',description='"+desc+"',summary='"+Summary+"' WHERE ID="+id);
    }
    public void DeleteTask(String id){

        getWritableDatabase().execSQL("DELETE FROM Task WHERE ID="+id);
    }
    public Cursor getTask(){
        Cursor taskList=getReadableDatabase().rawQuery("SELECT * FROM Task ORDER BY ID DESC",null);
        return taskList;
    }



}
