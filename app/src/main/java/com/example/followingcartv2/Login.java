 package com.example.followingcartv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    boolean logged;
    String shopId;
    String cartId;
    Button login;
    TextView id, shop;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = (Button) findViewById(R.id.login);
        id = (TextView) findViewById(R.id.id);
        shop = (TextView) findViewById(R.id.shopId);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        logged = pref.getBoolean("loginStatus", false);

        if (logged){
            Intent intent = new Intent(Login.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndSet();
            }
        });

    }

    void getAndSet() {
        shopId = String.valueOf(shop.getText());
        cartId = String.valueOf(id.getText());
        if (!shopId.isEmpty() && !cartId.isEmpty()) {
            myRef = database.getReference(shopId + "/carts/" + cartId + "/bluetoothId");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null && !snapshot.getValue().toString().isEmpty()){
                        String bluetoothId = snapshot.getValue().toString();
                        editor = pref.edit();
                        editor.putBoolean("loginStatus", true);
                        editor.putString("bluetoothId",bluetoothId);
                        editor.putString("shopId",shopId);
                        editor.putString("cartId",cartId);
                        editor.apply();
                        Intent intent = new Intent(Login.this,MainActivity.class);
                        startActivity(intent);
                        finish();

                    }else if (!snapshot.exists()){
                        Toast.makeText(Login.this, "Wrong Credentials please try again", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Login.this, "Connection error please try again", Toast.LENGTH_SHORT).show();

                }
            });

        } else {
            Toast.makeText(Login.this, "Please enter valid credentials for all fields", Toast.LENGTH_SHORT).show();
        }


    }
}