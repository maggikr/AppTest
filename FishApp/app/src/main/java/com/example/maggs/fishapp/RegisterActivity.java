package com.example.maggs.fishapp;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {
    EditText coords;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        if(getIntent().hasExtra("LatLng")){
            LatLng latLng = getIntent().getExtras().getParcelable("LatLng");
            coords = (EditText) findViewById(R.id.locText);
            coords.setText(latLng.latitude +", "+latLng.longitude);
        }

    }

    public void onClickRegister(View view){
        EditText idText = (EditText) findViewById(R.id.idText);
        String id = idText.getText().toString();

        EditText fTypeText = findViewById(R.id.fishTypeText);
        String fType = fTypeText.getText().toString();

        String[] splitCoords;
        splitCoords = coords.getText().toString().split(",");

        //LatLng loc = new LatLng(Double.parseDouble(splitCoords[0]),Double.parseDouble(splitCoords[1]));
        Double lat = Double.parseDouble(splitCoords[0]);
        Double lng = Double.parseDouble(splitCoords[1]);
        FishLoc testLoc = new FishLoc(id, fType, lat, lng);

            myRef.child("fishLocations").child(id).setValue(testLoc);


        Log.v("LoggMaggi 1",id +", "+ fType +", "+  splitCoords[0] + splitCoords[1]);
        Log.v("LoggMaggi 2","verdi :"+ FishLoc.getFishLocList().get(0).getFishType());
        startActivity(new Intent(this, MainActivity.class));

    }
}
