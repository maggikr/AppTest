package com.example.maggs.fishapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText coords;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("fishLocations"); //Connects to firebase and returns stored data under "fishLocations"
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "FISHLOC MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.main_toolbar);                              //Adds custom toolbar/Actionbar
        setSupportActionBar(toolbar);                                                   //

        ActionBar ab = getSupportActionBar();                                           //Enables Up button
        ab.setDisplayHomeAsUpEnabled(true);

        if(getIntent().hasExtra("LatLng")){                                             //Checks for latlng object and sets coordinates to edittext field
            LatLng latLng = getIntent().getExtras().getParcelable("LatLng");
            coords = (EditText) findViewById(R.id.locText);
            coords.setText(latLng.latitude +", "+latLng.longitude);
        }
    }


    public void onClickRegister(View view){                                             //Recieves input, creates fishloc object, stores object in DB, returns user to mainActivity
        EditText idText = (EditText) findViewById(R.id.idText);
        String id = idText.getText().toString();

        EditText fTypeText = findViewById(R.id.fishTypeText);
        String fType = fTypeText.getText().toString();

        String[] splitCoords;
        splitCoords = coords.getText().toString().split(",");

        Double lat = Double.parseDouble(splitCoords[0]);
        Double lng = Double.parseDouble(splitCoords[1]);
        FishLoc testLoc = new FishLoc(id, fType, lat, lng);

        myRef.child("fishLocations").child(id).setValue(testLoc);

        startActivity(new Intent(this, MainActivity.class));
    }


    public void onClickGetImage(View view){
        dispatchTakePictureIntent();
    }


    private void dispatchTakePictureIntent() {                                          //Opens camera app
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {     //If image is chosen a bitmap version is loaded into image view
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView fishImg = (ImageView) findViewById(R.id.fishImg);
            fishImg.setImageBitmap(imageBitmap);
        }
    }
}