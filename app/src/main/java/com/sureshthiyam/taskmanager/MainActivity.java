package com.sureshthiyam.taskmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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


public class MainActivity extends AppCompatActivity {

    EditText title,desc,summary;
    Button btnSave;
    TaskDB db;
    public static String taskName="";
    public static String TaskDesc="";
    public static String TaskSummary="";
    public static String TaskID="0";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title=(EditText) findViewById(R.id.edt_taskName);
        desc=(EditText) findViewById(R.id.edt_desc);
        summary=(EditText) findViewById(R.id.edt_summary);
      //  db=new TaskDB(getApplicationContext());
        btnSave=(Button) findViewById(R.id.btn_save);
        title.setText(taskName);
        desc.setText(TaskDesc);
        summary.setText(TaskSummary);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url= DataUrls.AddTask+TaskID+"&title="+title.getText().toString()+"&desc="+desc.getText().toString()+"&summary="+summary.getText().toString();
                url=url.replaceAll("\n","%0D%0A");
                url=url.replaceAll("\r","%0D%0A");
                url=url.replaceAll(" ","%20");
                new AsyncHttpTask().execute(url);

            }
        });
    }
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
          progressDialog= ProgressDialog.show(MainActivity.this,null,"loading...",true,false);

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
                    result= parseResult(response.toString());
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
                Toast.makeText(MainActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();
                taskName="";
                TaskDesc="";
                TaskSummary="";
                TaskID="0";
                finish();
            } else {


                Toast.makeText(MainActivity.this, "Fail to update data!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private int parseResult(String result) {
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
