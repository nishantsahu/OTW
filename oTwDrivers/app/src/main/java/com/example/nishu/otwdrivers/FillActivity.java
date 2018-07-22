package com.example.nishu.otwdrivers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FillActivity extends AppCompatActivity {

    String json;
    ListView lv;
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill);

        SharedPreferences route = getSharedPreferences("routejson", MODE_PRIVATE);
        json = route.getString("routes", "");
        lv = findViewById(R.id.routes);
        tv = findViewById(R.id.txt);

        try {
            JSONArray array = new JSONArray(json);
            final String[] routeArray = new String[array.length()];
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
                    String id = routeID[i];
                    String name = routeArray[i];
                    Intent next = new Intent(getApplicationContext(), SendingActivity.class);
                    next.putExtra("route_id", id);
                    next.putExtra("route_name", name);
                    startActivity(next);
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