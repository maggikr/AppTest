package com.example.maggs.fishapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
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

import android.Manifest;
import android.widget.Toast;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText coords;
    private TextInputEditText timeText;
    private TextInputEditText fTypeText;
    private ImageView fishImg;
    private String id;

    //Used to control if input values are acceptable
    private boolean timeChecked = false;
    private boolean fTypeChecked = false;
    private boolean coordsChecked = false;

    //Stores reference to parent "fishLocations" in firebase DB
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("fishLocations");

    private static final String TAG = "FISHLOC MESSAGE";

    //To handle camera permission
    private static final int CAMERA_PERMISSION = 1;
    private String[] cameraPermission = {Manifest.permission.CAMERA};

    //Variables used to take and store images
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Adds custom toolbar/Actionbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //Enables Up button
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        fTypeText = (TextInputEditText) findViewById(R.id.fishTypeText);
        coords = (TextInputEditText) findViewById(R.id.locText);
        timeText = (TextInputEditText) findViewById(R.id.timeText);
        fishImg = (ImageView) findViewById(R.id.fishImg);

        //Sets textwatchers as listeners
        fTypeText.addTextChangedListener(fishTypeWatcher);
        coords.addTextChangedListener(coordsWatcher);
        timeText.addTextChangedListener(timeWatcher);

        //Stores system date in timeText field with a specific format
        Calendar currentTime = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(currentTime.getTime());
        timeText.setText(formattedDate);

        //Checks for latlng object and sets coordinates to input field
        if(getIntent().hasExtra("LatLng")){
            LatLng latLng = getIntent().getExtras().getParcelable("LatLng");

            coords.setText(latLng.latitude +", "+latLng.longitude);
        }

    }

    /**Simple Textwatchers for the more important input fields, checks if any chars are entered
     * and that they match the allowed characters. If conditions are met, sets checked variable to true*/

    private final TextWatcher fishTypeWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            fTypeChecked = false;
        }
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                fTypeText.setError("Required");
            } else if(!s.toString().matches("[a-zA-Z? ]*")){
                fTypeText.setError("Only letters");
            }else {
                fTypeChecked = true;
            }
        }
    };

    private final TextWatcher timeWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            timeChecked = false;
        }
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                timeText.setError("Required");
            } else if(!s.toString().matches("[0-9-? ]*")){
                timeText.setError("Only numbers and \"-\"");
            } else {
                timeChecked = true;
            }
        }
    };

    private final TextWatcher coordsWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            coordsChecked = false;
        }
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                coords.setError("Required");
            } else if(!s.toString().matches("[0-9-., ? ]*")){
                coords.setError("Two numbers divided by ,");
            } else {
                coordsChecked = true;
            }
        }
    };

    /**On register button click, checks if the textwatchers conditions have been met, else warns user*/
    public void onClickRegister(View view){

        if(fTypeChecked && coordsChecked && timeChecked){

            TextInputEditText baitText = (TextInputEditText) findViewById(R.id.baitText);
            EditText commentText = (TextInputEditText) findViewById(R.id.commentText);

            String fType = fTypeText.getText().toString();
            String bait = baitText.getText().toString();
            String time = timeText.getText().toString();
            String comment = commentText.getText().toString();

            //Splits coordinates to store lat and lng as separate values(latlng objects are difficult to read from firebase)
            String[] splitCoords;
            splitCoords = coords.getText().toString().split(",");
            Double lat = Double.parseDouble(splitCoords[0]);
            Double lng = Double.parseDouble(splitCoords[1]);

            //Creates a more unique id by adding random number between 0-9999999, to date
            Random rand = new Random();
            int n = rand.nextInt(9999999);
            id = time + n;

            //Initiate an object and store under our database reference
            FishLoc testLoc = new FishLoc(id, fType, lat, lng, bait, time, comment);
            myRef.child(id).setValue(testLoc);

            //If user have selected an image, run storeImage()
            if(fishImg.getDrawable() != null){
                Log.v(TAG,"Bilde finnes");
                storeImage();
            }

            //Finishes activity, user returns to MainActivity
            finish();

        } else{
            Toast.makeText(this,"Please fill out the required fields",Toast.LENGTH_LONG).show();
            fTypeText.setError("Required");
            coords.setError("Required");
            timeText.setError("Required");
        }
    }

    /**Stores image from imageview in firebaseStorage*/
    public void storeImage(){

        //Creates a reference to FirebaseStorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(id + ".jpg");

        //receives image from imageview as bitmap,
        fishImg.setDrawingCacheEnabled(true);
        fishImg.buildDrawingCache();
        Bitmap bitmap = fishImg.getDrawingCache();

        //Compresses bitmap and uploads with ByteArrayStream
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
            }
        });
    }

    /**OnClick method for "Ta bilde", directs to checkCameraPermission. Setting this method as the
     * @AfterPermissionGranted(CAMERA_PERMISSION) method, grants error because it takes a parameter*/
    public void onClickGetImage(View view) {
        checkCameraPermission();
    }

    /**Checks if camera permission is granted, and acts accordingly*/
    @AfterPermissionGranted(CAMERA_PERMISSION)
    private void checkCameraPermission(){
        if (EasyPermissions.hasPermissions(this, cameraPermission)) {
            dispatchTakePictureIntent();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.no_camera_permission),
                    CAMERA_PERMISSION, cameraPermission);
        }
    }

    /** Starts an intent to take a picture */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Checks if there's a camera activity to handle intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Creates a file for the picture
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.v(TAG, "Error creatings file");
            }

            //Creates URI and stores it as extra in Intent
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

    /** Creates filename and file */
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

        //Stores path to image
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /** If picture is taken and accepted in camera activity, runs setPic() */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
        }
    }

    /** Scales down picture to reduce filesize based on picture size and custom measurements to suit imageview*/
    private void setPic() {

        //Receives image size
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        //ScaleFactor based on image size and custom measurements
        int scaleFactor = Math.min(photoW/100, photoH/150);

        // Decode image to bitmap size
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        //Sets the smaller decoded image in fishImg imageview
        fishImg.setImageBitmap(bitmap);
    }

    /** Handles the result of permission requests*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}