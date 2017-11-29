package com.example.maggs.fishapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;

import android.Manifest;
import android.widget.Toast;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText coords;
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("fishLocations"); //Connects to firebase and returns stored data under "fishLocations"
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String TAG = "FISHLOC MESSAGE";
    private ImageView fishImg;
    private String id;
    private TextInputEditText timeText;
    private static final int CAMERA_PERMISSION = 1;
    private String[] cameraPermission = {Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.main_toolbar);                              //Adds custom toolbar/Actionbar
        setSupportActionBar(toolbar);                                                   //

        ActionBar ab = getSupportActionBar();                                           //Enables Up button
        ab.setDisplayHomeAsUpEnabled(true);
        fishImg = (ImageView) findViewById(R.id.fishImg);
        timeText = (TextInputEditText) findViewById(R.id.timeText);
        Calendar currentTime = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(currentTime.getTime());
        timeText.addTextChangedListener(timeWatcher);
        timeText.setText(formattedDate);


        coords = (TextInputEditText) findViewById(R.id.locText);
        coords.addTextChangedListener(coordsWatcher);
        if(getIntent().hasExtra("LatLng")){                                             //Checks for latlng object and sets coordinates to edittext field
            LatLng latLng = getIntent().getExtras().getParcelable("LatLng");

            coords.setText(latLng.latitude +", "+latLng.longitude);
        }

    }

    private final TextWatcher timeWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            timeText.setError("Required");
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //timeText.setVisibility(View.VISIBLE);
        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                Toast.makeText(getBaseContext(),"No date enter", Toast.LENGTH_LONG).show();
                timeText.setError("Required");
                //timeText.setVisibility(View.GONE);

            } else if(!s.toString().matches("[0-9-? ]*")){
                timeText.setError("Only numbers and \"-\"");
                //timeText.setText("You have entered : " + timeText.getText());
            }
        }
    };

    private final TextWatcher coordsWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            coords.setError("Required");
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //coords.setVisibility(View.VISIBLE);
        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                //Toast.makeText(getBaseContext(),"No date enter", Toast.LENGTH_LONG).show();
                coords.setError("Required");
                //timeText.setVisibility(View.GONE);

            } else if(!s.toString().matches("[0-9-., ? ]*")){
                coords.setError("Two numbers divided by .");
                //timeText.setText("You have entered : " + timeText.getText());
            }
        }
    };


    public void onClickRegister(View view){                                             //Recieves input, creates fishloc object, stores object in DB, returns user to mainActivity
        TextInputEditText fTypeText = findViewById(R.id.fishTypeText);
        String fType = fTypeText.getText().toString();

        TextInputEditText baitText = (TextInputEditText) findViewById(R.id.baitText);
        String bait = baitText.getText().toString();

        timeText = (TextInputEditText) findViewById(R.id.timeText);
        String time = timeText.getText().toString();




        EditText commentText = (TextInputEditText) findViewById(R.id.commentText);
        String comment = commentText.getText().toString();

        String[] splitCoords;
        splitCoords = coords.getText().toString().split(",");

        Double lat = Double.parseDouble(splitCoords[0]);
        Double lng = Double.parseDouble(splitCoords[1]);

        Random rand = new Random();
        int n = rand.nextInt(9999999);
        id = time + n;
        FishLoc testLoc = new FishLoc(id, fType, lat, lng, bait, time, comment);
        if(fishImg.getDrawable() != null){
            Log.v(TAG,"Bilde finnes");
            storeImage();

        }


        myRef.child(id).setValue(testLoc);

        finishActivity(0);
        finish();
        //startActivity(new Intent(this, MainActivity.class));
    }
    public void storeImage(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference().child(id + ".jpg");;
        /*
        // Create a reference to "mountains.jpg"
        StorageReference mountainsRef = storageRef.child();

        // Create a reference to 'images/mountains.jpg'
        StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");

        // While the file names are the same, the references point to different files
        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false
        */
        fishImg.setDrawingCacheEnabled(true);
        fishImg.buildDrawingCache();
        Bitmap bitmap = fishImg.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.v(TAG, "Image upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v(TAG, "Image upload successful");
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }
                                         //Runs permission check, requests permission if not yet granted
    public void onClickGetImage(View view) {
        testMethod();

    }
    @AfterPermissionGranted(CAMERA_PERMISSION)
    private void testMethod(){
        if (EasyPermissions.hasPermissions(this, cameraPermission)) {
            dispatchTakePictureIntent();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.no_camera_permission),
                    CAMERA_PERMISSION, cameraPermission);
        }
    }

/*
    private void dispatchTakePictureIntent() {                                          //Opens camera app
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {     //If image is chosen a bitmap version is loaded into image view
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView fishImg = (ImageView) findViewById(R.id.fishImg);
            fishImg.setImageBitmap(imageBitmap);*/
            setPic();
        }
    }
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

        // Get the dimensions of the View
        /*int targetW = fishImg.getWidth();
        int targetH = fishImg.getHeight();*/

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/100, photoH/150);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        //fishImg.setScaleType(ImageView.ScaleType.FIT_XY);
        fishImg.setImageBitmap(bitmap);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    /*@AfterPermissionGranted(RC_CAMERA)                                        //Runs permission check, requests permission if not yet granted
    private void setLocationEnabled() {

        if (EasyPermissions.hasPermissions(this, locationPermission)) {

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.no_location_permission),
                    LOCATION_PERMISSION, locationPermission);
        }
    }*/
}