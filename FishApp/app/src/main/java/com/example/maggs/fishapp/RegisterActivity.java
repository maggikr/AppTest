package com.example.maggs.fishapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private EditText coords;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("fishLocations"); //Connects to firebase and returns stored data under "fishLocations"
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
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
        EditText fTypeText = findViewById(R.id.fishTypeText);
        String fType = fTypeText.getText().toString();

        EditText baitText = (EditText) findViewById(R.id.baitText);
        String bait = baitText.getText().toString();

        EditText timeText = (EditText) findViewById(R.id.timeText);
        String time = timeText.getText().toString();
        String id = time;

        EditText commentText = (EditText) findViewById(R.id.commentText);
        String comment = commentText.getText().toString();

        String[] splitCoords;
        splitCoords = coords.getText().toString().split(",");

        Double lat = Double.parseDouble(splitCoords[0]);
        Double lng = Double.parseDouble(splitCoords[1]);
        FishLoc testLoc = new FishLoc(id, fType, lat, lng, bait, time, comment);

        myRef.child(id).setValue(testLoc);

        startActivity(new Intent(this, MainActivity.class));
    }


    public void onClickGetImage(View view){
        dispatchTakePictureIntent();
    }

/*
    private void dispatchTakePictureIntent() {                                          //Opens camera app
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {     //If image is chosen a bitmap version is loaded into image view
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView fishImg = (ImageView) findViewById(R.id.fishImg);
            fishImg.setImageBitmap(imageBitmap);
            setPic();
        }
    }*/
    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.v(TAG, "Error creatings file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.v(TAG, "File created!");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.maggs.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private void setPic() {
        ImageView fishImg = (ImageView) findViewById(R.id.fishImg);
        // Get the dimensions of the View
        int targetW = fishImg.getWidth();
        int targetH = fishImg.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        fishImg.setImageBitmap(bitmap);
    }
}