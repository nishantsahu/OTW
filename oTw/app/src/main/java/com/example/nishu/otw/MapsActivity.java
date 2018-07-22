package com.example.nishu.otw;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    OkHttpClient client;
    String URL;
    double lng, lat;
    String id;
    Button refresh;
    ProgressDialog progressDialog;
    String jsonData;
    TextView streetTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        refresh = findViewById(R.id.refresh);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Geting Location..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        streetTxt = findViewById(R.id.streetTxt);
        id = getIntent().getExtras().getString("route_id");

        SharedPreferences urls = getSharedPreferences("url", MODE_PRIVATE);
        URL = urls.getString("URL", "");

        client = new OkHttpClient();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getCity() throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
        String street = addresses.get(0).getAddressLine(0);
        Toast.makeText(getApplicationContext(), street, Toast.LENGTH_LONG).show();
        streetTxt.setText(street);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        SharedPreferences location = getSharedPreferences("locationData", MODE_PRIVATE);
        jsonData = location.getString("coordinates", "");
//        try {
////            JSONObject mainObj = new JSONObject(jsonData);
////            lat = Double.parseDouble(mainObj.getString("latitude"));
////            lng = Double.parseDouble(mainObj.getString("longitude"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        lat = Double.parseDouble(getIntent().getExtras().getString("lat"));
        lng = Double.parseDouble(getIntent().getExtras().getString("lng"));

        try {
            getCity();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Toast.makeText(getApplicationContext(), lat + "\n" + lng, Toast.LENGTH_LONG).show();

        // Add a marker in Sydney and move the camera
        LatLng current = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(current).title("Here is your bus"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    public void getLocation() {
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

    @Override
    public void onBackPressed() {
        Intent back = new Intent(getApplicationContext(), SelectActivity.class);
        startActivity(back);
    }
}
