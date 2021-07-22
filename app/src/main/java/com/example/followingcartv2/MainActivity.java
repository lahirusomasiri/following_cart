package com.example.followingcartv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 123;
    boolean logged;
    String bluetoothId;
    String shopId;
    String cartId;
    Button connect, disconnect, logOut, show;

    TextView textView, textView2;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Intent serviceIntent;
    LocationRequest locationRequest;
    private LocationSettingsRequest.Builder builder;


    FirebaseDatabase firebaseDatabase ;
    DatabaseReference databaseReference ;
    ValueEventListener valueEventListener2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        logged = pref.getBoolean("loginStatus", false);
        shopId = pref.getString("shopId", null);
        cartId = pref.getString("cartId", null);
        bluetoothId = pref.getString("bluetoothId", null);

        logOut = (Button) findViewById(R.id.logout);
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);
        textView = (TextView)findViewById(R.id.first);
        textView2 = (TextView)findViewById(R.id.second);
        serviceIntent = new Intent(this, LocationService.class);

        locationRequest = new LocationRequest().setFastestInterval(1500).setInterval(2000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
                Toast.makeText(getApplicationContext(), "GPS Service is Starts...", Toast.LENGTH_LONG).show();
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
//                textView.setText("0");
//                textView2.setText("0");
            }
        });
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(shopId+"/carts/"+cartId);
        valueEventListener2 = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double latitude = Double.valueOf(String.valueOf(snapshot.child("Latitude").getValue()));
                Double longitude =Double.valueOf(String.valueOf(snapshot.child("Longitude").getValue()));

                //Log.d("value", String.valueOf(snapshot));
                //Log.d("latitude", String.valueOf(latitude));

                textView.setText(String.valueOf(latitude));
                textView2.setText(String.valueOf(longitude));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8989) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Toast.makeText(getApplicationContext(), "turned on", Toast.LENGTH_LONG).show();
                    startService(serviceIntent);
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), "you have to turn on gps please try again", Toast.LENGTH_LONG).show();


            }
        }
    }

    void logOut() {
        editor = pref.edit();
        editor.putBoolean("loginStatus", false);
        editor.putString("bluetoothId", null);
        editor.putString("shopId", null);
        editor.putString("cartId", null);
        editor.apply();
        stopService(serviceIntent);
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }



    public void startService() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGps();
        } else {
            requestPermission();
        }
    }


    public void stopService() {
        //Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
        Toast.makeText(getApplicationContext(), "GPS Service is Stopped...", Toast.LENGTH_LONG).show();
    }


    public void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 120);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

               // break;
            case 120:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    checkGps();
                } else {
                    Toast.makeText(getApplicationContext(), "you have to grant permissions please try again", Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    public void checkGps() {
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    //Toast.makeText(getApplicationContext(), "already on", Toast.LENGTH_LONG).show();
                    //startService(serviceIntent);
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    if (response.getLocationSettingsStates().isGpsPresent()){
                        Toast.makeText(getApplicationContext(), "already on", Toast.LENGTH_LONG).show();
                        startService(serviceIntent);
                    }
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 8989);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }
}