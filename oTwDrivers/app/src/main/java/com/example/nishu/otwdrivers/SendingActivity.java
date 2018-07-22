package com.example.nishu.otwdrivers;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SendingActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    CardView cv;
    double lat, lng;
    String routeID, routeName;
    String URL = "https://fathomless-falls-12110.herokuapp.com/";
    OkHttpClient client;
    TextView tv, stat;
    ProgressDialog progressDialog;
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Location, please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        img = findViewById(R.id.img);
        cv = findViewById(R.id.cv);
        client = new OkHttpClient();
        routeID = getIntent().getExtras().getString("route_id");
        routeName = getIntent().getExtras().getString("route_name");

        tv = findViewById(R.id.msg);
        stat = findViewById(R.id.status);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                stat.setText(lat + ", "+lng+"\n"+routeName);
                sendLocation();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            }
            return;
        }
        else{
            configureLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    configureLocation();
                }
                return;
        }
    }

    private void configureLocation(){
        locationManager.requestLocationUpdates("gps", 1, 0, locationListener);
    }

    public void sendLocation() {

        Request request = new Request.Builder().url(URL+"getGps")
                .post(RequestBody.create(MediaType.parse("application/json"), "{\n" +
                        "\t\"route_id\" : \""+routeID+"\",\n" +
                        "\t\"latitude\" : \""+lat+"\",\n" +
                        "\t\"longitude\" : \""+lng+"\"\n" +
                        "}"))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        tv.setText("There is an error on transmitting you location, Please restart your application.");
                        img.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gps_off_black_24dp));
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
                                JSONObject mainObj = new JSONObject(json);
                                String status = mainObj.getString("status");
                                if (status.equals("success")){
                                    tv.setText("Your location is transmitting, Please don't close the application.");
                                    img.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gps_fixed_black_24dp));
                                }
                                else{
                                    tv.setText("There is an error on transmitting you location, Please restart your application.");
                                    img.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gps_off_black_24dp));
                                }
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Do you want to leave the session?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
