package com.example.maggs.fishapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap gMap;

    //Connects to firebase and returns stored data under "fishLocations"
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("fishLocations");

    //Used to control bottomsheet
    private View bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    //Variable for search/autocomplete method
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    //Handles camera permission
    private String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int LOCATION_PERMISSION = 1;

    private static final String TAG = "FISHLOC MESSAGE";

    private PopupMenu popupMenu;            //filtermenu
    private ArrayList<String> filterList;
    private ArrayList<Marker> markerList;
    private int FILTER_ACTIVATED = 0;       //Filter activated
    private boolean RESTORED;               //Activity has been restored
    private boolean SKIP;                   //Activity has been restored2
    private LatLng restoredCP;              //Stores cameraposition on save state
    private String newType;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creates mapFragment and initializes map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Adds custom toolbar/Actionbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //Set icon as overflow/menu button
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_white_24dp);
        toolbar.setOverflowIcon(drawable);

        filterList = new ArrayList<>();

        //Adds bottom sheet, sets peek height(initial height on click) and hidden state on startup
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(450);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Check if network is available
        if(!isNetworkAvailable()){
            Toast.makeText(this, "No Internet connection detected", Toast.LENGTH_LONG).show();
        }

    }

    /** Checks if there are any network connections available, returns true/false if network object exists AND is connected */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /** Creates Autocomplete/search field through intent*/
    public void onClickSearch(){

        //Builds AutocompleteFilter used to narrow down search results to Norway
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("NO")
                .build();

        //Starts autocomplete intent and informs user if google play services are not installed/up to date,
        // Or if google play service is not available
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {                             //Displays an error dialog informing the user that google play services are not installed or up to date
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 ).show();
        } catch (GooglePlayServicesNotAvailableException e) {                           //Returns a toast and log message
            String errorMsg = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);
            Log.e(TAG, errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    /**Handles selected location from search/autocomplete */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            //Creates place object from selected location and moves "camera" to selected objects position
            //Or prints status message in Log on error
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(place.getLatLng(), 15, 0, 0)));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            }
        }
    }

    /** Builds Option menu by setting main_menu as the menu and runs the wall of If tests to build filter correctly*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //Creates view variable from menu item item_filter and use it to set up popupMenu(filter)
        final View menuItemView = findViewById(R.id.item_filter); // SAME ID AS MENU ID
        popupMenu = new PopupMenu(this, menuItemView);

        //Stores fishtype array from FishLoc into fishTypes array
        ArrayList<String> fishTypes = FishLoc.getFishTypeList();
        Log.v(TAG, fishTypes.size()+" typer");


        //IF user has activated filter, iterate through fishtype list
        if (FILTER_ACTIVATED == 1) {
            for (int i = 0; i < fishTypes.size(); i++) {

                //IF the fishtype is the checked in filter, set option checked
                if (filterList.contains(fishTypes.get(i))) {
                    popupMenu.getMenu().add(1, i, 0, fishTypes.get(i)).setCheckable(true).setChecked(true);

                //IF a new fishtype has been registered since last filter build add new checked filter option to menu and add to filterlist
                } else if (fishTypes.get(i).equals(newType)) {
                    popupMenu.getMenu().add(1, i, 0, fishTypes.get(i)).setCheckable(true).setChecked(true);
                    filterList.add(fishTypes.get(i));
                    newType = null;

                //if not in the selected filter list, set option unchecked
                } else {
                    popupMenu.getMenu().add(1, i, 0, fishTypes.get(i)).setCheckable(true).setChecked(false);
                }
            }
            //Filtrate markers according the checked/unchecked options in filter menu
            filterMarkers();

        //IF filter has not been activated (app just started/filter not used)
        } else {
            //Add all fishtypes as filter options and set checked
            for (int i = 0; i < fishTypes.size(); i++) {
                popupMenu.getMenu().add(1, i, 0, fishTypes.get(i)).setCheckable(true).setChecked(true);
                //IF not already in filterlist, add it
                if (!filterList.contains(fishTypes.get(i))) {
                    filterList.add(fishTypes.get(i));
                }
            }
        }
        //Add filter button last
        popupMenu.getMenu().add(1, 999, 0, "Filtrer");
        Log.v(TAG, "popup laget");
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.v(TAG, "onMenuITemClicked");
                item.setChecked(!item.isChecked());

                // Do other stuff
                if(item.isChecked()){
                    Log.v(TAG,"Item is checked");
                    if(filterList.contains(item.getTitle())) {

                    }
                    else {
                        filterList.add((String) item.getTitle());
                    }
                }
                else if(!item.isChecked()){
                    Log.v(TAG,"Item is not checked");
                    if(filterList.contains(item.getTitle())){
                        filterList.remove(item.getTitle());
                    }

                }


                if(item.getItemId()==999){

                    FILTER_ACTIVATED = 1;
                    filterMarkers();
                }
                // Keep the popup menu open
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getBaseContext()));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                return false;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                               //Sets onClick functionality to menu items and search button

        switch (item.getItemId()) {

            case R.id.item_filter:
                Log.v(TAG, "OnOptionsItemSelected");
                if(FishLoc.getFishTypeList().size()!=0){
                    popupMenu.show();
                }
                return true;

            case R.id.item_search:
                onClickSearch();
                return true;

            case R.id.item_regEvent:
                startActivity(new Intent(this, RegisterActivity.class));
                return true;

            case R.id.item_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {                                       //Adds markers/functionality to map at startup when loaded
        gMap = googleMap;
        markerList = new ArrayList<>();

        LatLng halden = new LatLng(59.12478, 11.38754);
        /*gMap.addMarker(new MarkerOptions().position(halden)
                .title("Marker in Halden"));*/
        if (RESTORED) {
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(restoredCP,15,0,0)));

        }
        else{
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(halden, 15, 0, 0)));
        }


        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot fishLocsData, String prevChildKey) {
                FishLoc fishLoc = fishLocsData.getValue(FishLoc.class);

                Marker marker = gMap.addMarker(new MarkerOptions().position(new LatLng(fishLoc.getLat(),fishLoc.getLng()))
                        .title(fishLoc.getFishType())
                        .snippet("Dato: " + fishLoc.getTime() + "\nAgn: " + fishLoc.getBait() + "\nKommentar: " + fishLoc.getComment())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_fish)));
                /*gMap.addMarker(new MarkerOptions().position(new LatLng(fishLoc.getLat(),fishLoc.getLng()))
                        .title(fishLoc.getFishType())
                        .snippet("Dato: " + fishLoc.getTime() + "\nAgn: " + fishLoc.getBait() + "\nKommentar: " + fishLoc.getComment())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_fish)))
                        .setTag(fishLoc.getId());*/
                Log.v(TAG, "Mark added");

                //gMap.addMarker(markerOpt).setTag(fishLoc.getId());
                marker.setTag(fishLoc.getId());
                /*if(FILTER_ACTIVATED==1){
                    if(filterList.contains(fishLoc.getFishType())){
                        marker.setVisible(true);
                        markerList.add(marker);
                    }
                    else{
                        marker.setVisible(false);
                        markerList.add(marker);
                    }
                }
                else {
                    markerList.add(marker);
                }*/
                markerList.add(marker);

                if(FILTER_ACTIVATED==1){

                        newType = fishLoc.getFishType();

                    filterMarkers();
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        /*
        myRef.addValueEventListener(new ValueEventListener() {                          //Adds database listener, runs at startup and when data is updated

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fishLocsData: dataSnapshot.getChildren()) {           //Loads fishlocations and places markers
                    FishLoc fishLoc = fishLocsData.getValue(FishLoc.class);
                    Log.d(TAG, "Value is: " + dataSnapshot.getChildrenCount());
                    gMap.addMarker(new MarkerOptions().position(new LatLng(fishLoc.getLat(),fishLoc.getLng()))
                            .title(fishLoc.getFishType())
                            .snippet("Dato: " + fishLoc.getTime() + "\nAgn: " + fishLoc.getBait() + "\nKommentar: " + fishLoc.getComment())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_fish)))
                            .setTag(fishLoc.getId());
                }
                invalidateOptionsMenu(); //Runs onPrepareOptionsMenu() to add fishtypes to filter button
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });*/

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {           //Sets marker listener which loads marker data to bottom sheet
            @Override
            public boolean onMarkerClick(Marker marker) {
                updateBottomSheetContent(marker);
                return true;
            }
        });


        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {                 //Hides bottom sheet on map clicks
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        gMap.setOnMapLongClickListener(this);

        setLocationEnabled();
        setDefaultUiSettings();


    }

    public void filterMarkers(){
        //gMap.clear();
        if(!RESTORED){
            FILTER_ACTIVATED=1;
            for(Marker m : markerList){
                //if(FILTER_ACTIVATED==1){
                    if(filterList.contains(m.getTitle())){
                        Log.v(TAG,m.getTitle()+" is visible");
                        m.setVisible(true);

                    }
                    else{
                        m.setVisible(false);
                        //Log.v(TAG,"Filtrert" + m.getTitle() + " " + filterList.get(0));
                    }

            }
        }
        RESTORED = false;
    }



    private void updateBottomSheetContent(Marker marker) {                              //Updates bottom sheet text views with info from marker and displays the bottom sheet
        /*FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child("mountains.jpg");

        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference gsReference = storage.getReferenceFromUrl("gs://fishapp-ee352.appspot.com/");
        StorageReference urlChild = gsReference.child("mountains.jpg");
        // Create a reference from an HTTPS URL
        // Note that in the URL, characters are URL escaped!
        StorageReference httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/b/bucket/o/images%20stars.jpg");

        // Reference to an image file in Firebase Storage
        StorageReference storageReference ;*/
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        StorageReference mStorageRef = mFirebaseStorage.getReferenceFromUrl("gs://fishapp-ee352.appspot.com");
        StorageReference urlChild = mStorageRef.child(marker.getTag()+".jpg");

        // ImageView in your Activity
                ImageView imageView = (ImageView) bottomSheet.findViewById(R.id.markerImage);

        // Load the image using Glide
        GlideApp.with(this /* context */)
                .load(urlChild)
                .into(imageView);
        TextView title = (TextView) bottomSheet.findViewById(R.id.marker_title);
        TextView snippet = (TextView) bottomSheet.findViewById(R.id.marker_snippet);
        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    private void setDefaultUiSettings() {                                               //Adds map ui buttons
        UiSettings uiSettings = gMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);

    }





    @Override
    public void onMapLongClick(LatLng latLng) {                                         //Starts registerActivity on long clicks and brings latlng
        startActivity(new Intent(this, RegisterActivity.class)
                .putExtra("LatLng", latLng));
    }

    @Override
    protected void onResume() {
        //activityReopened = true;
        //invalidateOptionsMenu();
        super.onResume();
        /*if (cp != null) {
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            cp = null;
        }*/
        Log.v(TAG," resuming STATE");
    }

    @Override
    protected void onPause() {

        Log.v(TAG," SAVING STATE");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("filterlist",filterList);
        outState.putInt("filter_activated",FILTER_ACTIVATED);
        Log.v(TAG," SAVING STATE");
        //LatLng cpll = cp.target;
        //cp = gMap.getCameraPosition();
        outState.putDouble("lat",gMap.getCameraPosition().target.latitude);
        outState.putDouble("lon",gMap.getCameraPosition().target.longitude);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filterList = savedInstanceState.getStringArrayList("filterlist");
        FILTER_ACTIVATED = savedInstanceState.getInt("filter_activated");
        restoredCP = new LatLng(savedInstanceState.getDouble("lat"),savedInstanceState.getDouble("lon"));
        Log.v(TAG," RESTORING STATE");
        RESTORED = true;
        SKIP = true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @AfterPermissionGranted(LOCATION_PERMISSION)                                      //Runs permission check, requests permission if not yet granted
    private void setLocationEnabled() {

        if (EasyPermissions.hasPermissions(this, locationPermission)) {
            gMap.setMyLocationEnabled(true);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.no_location_permission),
                    LOCATION_PERMISSION, locationPermission);
        }
    }


}