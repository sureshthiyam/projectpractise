package com.sureshthiyam.taskmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sureshthiyam.taskmanager.utility.DataUrls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TasksListActivity extends AppCompatActivity {


    ListView taskList;
    CustomAdapter adapter;
    TaskDB db;
    ProgressBar progress;
    ArrayList<TaskItem> task;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_list);
        taskList=(ListView) findViewById(R.id.listView_task);
        db=new TaskDB(getApplicationContext());
        progress=(ProgressBar) findViewById(R.id.progressBartask);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_add_task_menu:
                Intent addTask=new Intent("suresh.addTask");
                startActivity(addTask);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void populateTask(){

        adapter= new CustomAdapter(task,TasksListActivity.this);
        taskList.setAdapter(adapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        String url= DataUrls.getTaskUrl;
        new AsyncHttpTask().execute(url);
      /*  taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView title=view.findViewById(R.id.txt_taskName);
                TextView desc=view.findViewById(R.id.txt_task_desc);
                TextView summary=view.findViewById(R.id.txt_task_summary);
                TextView taskId=view.findViewById(R.id.txt_task_id);

                MainActivity.TaskID=taskId.getText().toString();
                MainActivity.taskName=title.getText().toString();
                MainActivity.TaskDesc=desc.getText().toString();
                MainActivity.TaskSummary=summary.getText().toString();

                Intent addTask=new Intent("suresh.addTask");
                startActivity(addTask);

            }
        });*/
        taskList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {

                final TextView title=view.findViewById(R.id.txt_taskName);
                TextView desc=view.findViewById(R.id.txt_task_desc);
                TextView summary=view.findViewById(R.id.txt_task_summary);
                final TextView taskId=view.findViewById(R.id.txt_task_id);

                MainActivity.TaskID=taskId.getText().toString();
                MainActivity.taskName=title.getText().toString();
                MainActivity.TaskDesc=desc.getText().toString();
                MainActivity.TaskSummary=summary.getText().toString();
                AlertDialog.Builder builder=new AlertDialog.Builder(TasksListActivity.this,R.style.Theme_AppCompat_Light_Dialog);
                builder.setTitle("Message");
                builder.setMessage("Choose your action.");
                builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent addTask=new Intent("suresh.addTask");
                        startActivity(addTask);
                    }
                });
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //db.DeleteTask(taskId.getText().toString());
                        new AsyncHttpTaskDelete().execute(DataUrls.DeleteTask+taskId.getText().toString());
                        String url= DataUrls.getTaskUrl;
                        new AsyncHttpTask().execute(url);
                    }
                });

               AlertDialog alert= builder.create();
                alert.show();
                return false;
            }
        });
    }

    public class CustomAdapter extends ArrayAdapter<TaskItem> {

        private ArrayList<TaskItem> dataSet;
        Context mContext;

        // View lookup cache
        private class ViewHolder {
            TextView txtName;
            TextView txtDesc;
            TextView txtSummary;
            TextView txtTaskId;

        }

        public CustomAdapter(ArrayList<TaskItem> data, Context context) {
            super(context, R.layout.task_list_item, data);
            this.dataSet = data;
            this.mContext=context;
        }

        private int lastPosition = -1;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            TaskItem dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            final View result;

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.task_list_item, parent, false);
                viewHolder.txtName = (TextView) convertView.findViewById(R.id.txt_taskName);
                viewHolder.txtDesc = (TextView) convertView.findViewById(R.id.txt_task_desc);
                viewHolder.txtSummary = (TextView) convertView.findViewById(R.id.txt_task_summary);
                viewHolder.txtTaskId=convertView.findViewById(R.id.txt_task_id);
                result=convertView;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result=convertView;
            }
            lastPosition = position;

            viewHolder.txtName.setText(dataModel.TaskTitle);
            viewHolder.txtDesc.setText(dataModel.TaskDesc);
            viewHolder.txtSummary.setText(dataModel.TaskSummary);
            viewHolder.txtTaskId.setText(dataModel.ID);

            // Return the completed view to render on screen
            return convertView;
        }


    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            taskList.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);

        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    parseResult(response.toString());
                    Log.d("-----------------------",url.toString());
                    Log.d("-----------------------",response.toString());
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("-----------------------","EROROROROROOR");
                // Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            taskList.setVisibility(View.VISIBLE);
            progress.setVisibility(View.INVISIBLE);
            if (result == 1) {

                populateTask();
            } else {


                Toast.makeText(TasksListActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void parseResult(String result) {
        try {
            Log.d("-----------------------",result);
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("task");
            task=new ArrayList<TaskItem>();
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);


                TaskItem tItem=new TaskItem();
                tItem.ID = post.optString("id");
                tItem.TaskTitle = post.optString("title");
                tItem.TaskDesc = post.optString("discription");
                tItem.TaskSummary = post.optString("summary");

                task.add(tItem);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public class AsyncHttpTaskDelete extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(TasksListActivity.this,null,"loading...",true,false);

        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    result= parseResultDelete(response.toString());
                    Log.d("-----------------------",url.toString());
                    Log.d("-----------------------",response.toString());

                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("-----------------------","EROROROROROOR");
                // Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressDialog.dismiss();
            if (result == 1) {
                Toast.makeText(TasksListActivity.this, "Successfully deleted!", Toast.LENGTH_SHORT).show();

            } else {


                Toast.makeText(TasksListActivity.this, "Fail to update data!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private int parseResultDelete(String result) {
        int retn=0;
        try {
            Log.d("-----------------------",result);
            JSONObject response = new JSONObject(result);
            String status = response.optString("status");
            if(status.equals("success")){
                retn=1;
            }else{
                retn=0;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retn;
    }
}
