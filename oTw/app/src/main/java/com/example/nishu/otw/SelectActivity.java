package com.example.nishu.otw;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectActivity extends AppCompatActivity {

    String json;
    String URL;
    ListView lv;
    TextView tv;
    OkHttpClient client;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);



        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting location..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        SharedPreferences urls = getSharedPreferences("url", MODE_PRIVATE);
        URL = urls.getString("URL", "");

        SharedPreferences route = getSharedPreferences("routeData", MODE_PRIVATE);
        json = route.getString("route", "");
        lv = findViewById(R.id.routes);
        tv = findViewById(R.id.txt);

        client = new OkHttpClient();

        try {
            JSONArray array = new JSONArray(json);
            String[] routeArray = new String[array.length()];
            final String[] routeID = new String[array.length()];
            for (int i=0; i<array.length(); i++){
                String details = array.getString(i);
                JSONObject mainObj = new JSONObject(details);
                String routename = mainObj.getString("route_name");
                String routeId = mainObj.getString("route_id");
                routeArray[i] = routename;
                routeID[i] = routeId;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, routeArray);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final String id = routeID[i];

                    progressDialog.show();
                    Request request = new Request.Builder()
                            .url(URL+"getLocation")
                            .post(RequestBody.create(MediaType.parse("application/json"), "{\n" +
                                    "\t\"route_id\" : \""+id+"\"\n" +
                                    "}"))
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            if (response.isSuccessful()){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            progressDialog.dismiss();
                                            String json = response.body().string();
//                                            SharedPreferences location = getSharedPreferences("locationData", MODE_PRIVATE);
//                                            SharedPreferences.Editor editor = location.edit();
//                                            editor.putString("coordinates", json);
//                                            editor.commit();
                                            JSONObject mainObj = new JSONObject(json);
                                            String lat, lng;
                                            lat = mainObj.getString("latitude");
                                            lng = mainObj.getString("longitude");
                                            Intent next = new Intent(getApplicationContext(), MapsActivity.class);
                                            next.putExtra("route_id", id);
                                            next.putExtra("lat", lat);
                                            next.putExtra("lng", lng);
                                            startActivity(next);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
