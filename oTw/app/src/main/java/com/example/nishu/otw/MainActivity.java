package com.example.nishu.otw;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String URL = "https://fathomless-falls-12110.herokuapp.com/";
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences urls = getSharedPreferences("url", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = urls.edit();
        editor1.putString("URL", URL);
        editor1.commit();

        client = new OkHttpClient();
        getRoute();

    }

    public void getRoute() {
        Request request = new Request.Builder()
                .url(URL+"showRoutes")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_LONG).show();
                        finish();
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
                                String json = response.body().string();
                                SharedPreferences routes = getSharedPreferences("routeData", MODE_PRIVATE);
                                SharedPreferences.Editor editor2 = routes.edit();
                                editor2.putString("route", json);
                                editor2.commit();
                                Intent next = new Intent(getApplicationContext(), SelectActivity.class);
//                                Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                                startActivity(next);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
}
