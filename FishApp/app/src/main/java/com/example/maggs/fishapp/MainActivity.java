package com.example.maggs.fishapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

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
    private int FILTER_ACTIVATED;           //Filter activated
    private boolean RESTORED;               //Activity has been restored
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
        FILTER_ACTIVATED = 0;

        //Adds bottom sheet, sets peek height(initial height on click) and hidden state on startup
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(450);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Check if network is available
        isNetworkAvailable();
    }

    /** Checks if there are any network connections available*/
    private void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        //if network object does NOT exists AND is NOT connected informs user width alarm dialog
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setMessage(getResources().getString(R.string.no_network_available));

            //Sets button with onClick function which gives user option to open wireless settings
            ad.setPositiveButton(getResources().getString(R.string.change_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(myIntent);
                }
            });
            ad.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Toast.makeText(getBaseContext(), "Unable to load new data without network access",Toast.LENGTH_LONG).show();

                }
            });
            ad.show();
        }
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
        Log.v(TAG, filterList.size()+" typer");

        //IF user has activated filter, iterate through fishtype list
        if (FILTER_ACTIVATED == 1) {
            for (int i = 0; i < fishTypes.size(); i++) {
                Log.v(TAG, "228");
                //IF the fishtype is the checked in filter, set option checked
                if (filterList.contains(fishTypes.get(i))) {
                    popupMenu.getMenu().add(1, i, 0, fishTypes.get(i)).setCheckable(true).setChecked(true);
                    Log.v(TAG, "232");
                //IF a new fishtype has been registered since last filter build
                } else if (fishTypes.get(i).equals(newType)) {
                    //IF RESTORED is true (activity has been restored) add newType as filter option and set to unchecked
                    //Seems to only be needed by earlier API levels like 19, without it could set an unchecked filter from before the restorestate as checked
                    if(RESTORED) {
                        popupMenu.getMenu().add(1, i, 0, fishTypes.get(i)).setCheckable(true).setChecked(false);
                        RESTORED = false;
                        Log.v(TAG, "Added newType skip = true");
                    //If not restored add newType as filter option and set checked
                    } else {
                        popupMenu.getMenu().add(1, i, 0, fishTypes.get(i)).setCheckable(true).setChecked(true);
                        filterList.add(fishTypes.get(i));
                        Log.v(TAG, "Added newType skip = false"+fishTypes.get(i));
                    }

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
                Log.v(TAG, "258");
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

    /** Sets listener to filter menu*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //if item is checked, sets unchecked and vice versa
                item.setChecked(!item.isChecked());

                //IF item is checked
                if(item.isChecked()){

                    //IF filterlist does not contain the fishtype, add it
                    if(!filterList.contains(item.getTitle())) {
                        filterList.add((String) item.getTitle());
                    }
                //IF item is NOT checked
                } else if(!item.isChecked()){
                    //IF filterlist contains the fishtype, remove it
                    if(filterList.contains(item.getTitle())){
                        filterList.remove(item.getTitle());
                    }
                }

                //IF item clicked equals id 999 (filter button), set FILTER_ACTIVATED and run filterMarkers()
                if(item.getItemId()==999){
                    FILTER_ACTIVATED = 1;
                    filterMarkers();
                }

                // Prevents filter menu from closing when clicking items in menu
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getBaseContext()));

                return false;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    /** adds onClick functionality to option items */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            //If filtermenu clicked
            case R.id.item_filter:
                //If any fishtypes are registered, show menu
                if(FishLoc.getFishTypeList().size()!=0){
                    popupMenu.show();

                //Else inform user
                }else{
                    Toast.makeText(this, "No fish types available", Toast.LENGTH_SHORT);
                }
                return true;

                //If search button clicked, start autocomplete/search method
            case R.id.item_search:
                onClickSearch();
                return true;

                //If Registrer lokasjon clicked, start RegisterActivity
            case R.id.item_regEvent:
                startActivity(new Intent(this, RegisterActivity.class));
                return true;

                //If hjelp clicked, start HelpActivity
            case R.id.item_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

/** Adds markers/functionality to map when ready */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        markerList = new ArrayList<>();

        //IF code is restored (been paused/orientation change) move camera to the stored position from before the pause
        if (RESTORED) {
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(restoredCP,15,0,0)));

        //Else run setLocationCode which checks permission and moves camera accordingly
        }
        else{
            setLocationEnabled();
        }

        //Sets child event listener to firebase DB
        myRef.addChildEventListener(new ChildEventListener() {
            //Adds children(objects) as soon as it detects a new child (Adds all children at startup)
            @Override
            public void onChildAdded(DataSnapshot fishLocsData, String prevChildKey) {

                //Initiates FishLoc objects
                FishLoc fishLoc = fishLocsData.getValue(FishLoc.class);
                //Creates Marker object, stores certain values from fishloc object in markers title and snippet, and sets custom icon
                Marker marker = gMap.addMarker(new MarkerOptions().position(new LatLng(fishLoc.getLat(),fishLoc.getLng()))
                        .title(fishLoc.getFishType())
                        .snippet("Dato: " + fishLoc.getTime() + "\nAgn: " + fishLoc.getBait() + "\nKommentar: " + fishLoc.getComment())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_fish)));

                //sets Id as tag, cannot be done while initiating marker object
                marker.setTag(fishLoc.getId());
                //Stores markers inn array for later
                markerList.add(marker);

                //IF filter has been activated the currently added object has been added while user have
                //been using app, so it's stored in newType variable as it's needed to add new filter option correctly
                if(FILTER_ACTIVATED==1){
                    newType = fishLoc.getFishType();

                    Log.v(TAG, "Added newType CHILD ADDED");
                }
                //Force onCreateOptionsMenu to rebuild filter menu
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

        //MarkerClickListener runs updateBottomSheetContent onClick
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {           //Sets marker listener which loads marker data to bottom sheet
            @Override
            public boolean onMarkerClick(Marker marker) {
                updateBottomSheetContent(marker);
                return true;
            }
        });

        //Hides bottomSheet when user clicks somewhere on map
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {                 //Hides bottom sheet on map clicks
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        //Sets longClickListener which starts RegisterActivity and sends latLng object as extra
        gMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                startActivityForResult(new Intent(getBaseContext(), RegisterActivity.class)
                        .putExtra("LatLng", latLng),0);
                //startActivity(new Intent(getBaseContext(), RegisterActivity.class)
                //        .putExtra("LatLng", latLng));
            }
        });

        //MyLocationButtonClick runs checkLocationEnabled() to inform user if location not available
        // (if position access have been disabled after app started it will still move camera to last known location)
        gMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                checkLocationEnabled();
                return false;
            }
        });

        //Enables zoom control
        gMap.getUiSettings().setZoomControlsEnabled(true);

    }

    /**checks if location is enabled through GPS or network*/
    private void checkLocationEnabled(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception ex){Log.v(TAG, ex.getMessage());}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception ex){Log.v(TAG, ex.getMessage());}

        //If both GPS and Network positioning are disabled, display AlertDialog
        if(!gps_enabled && !network_enabled){
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setMessage(getResources().getString(R.string.no_location_access));
            //Sets button with text and onClick function which opens location settings
            ad.setPositiveButton(getResources().getString(R.string.change_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            //Sets another button which displays a toast when clicked
            ad.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Toast.makeText(getBaseContext(),"Cannot update last known location", Toast.LENGTH_SHORT).show();

                }
            });
            ad.show();
        }
    }

    /** Final step of the filtration process. Sets markers visibility to true/false based on filters checked*/
    public void filterMarkers(){
        FILTER_ACTIVATED=1;
        for(Marker m : markerList){
                if(filterList.contains(m.getTitle())){
                    m.setVisible(true);
                }else{
                    m.setVisible(false);
                }
        }
    }

    /**Adds data to bottomsheet */
    private void updateBottomSheetContent(Marker marker) {
        //Creates connection to firebaseStorage, reference to Url and reference to image/child that matches marker tag(fishloc obj id)
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        StorageReference mStorageRef = mFirebaseStorage.getReferenceFromUrl("gs://fishapp-ee352.appspot.com");
        StorageReference urlChild = mStorageRef.child(marker.getTag()+".jpg");

        //Uses Glide to load image into imageview
        ImageView imageView = (ImageView) bottomSheet.findViewById(R.id.markerImage);
        GlideApp.with(this)
                .load(urlChild)
                .into(imageView);
        //Adds data from markers title and snippet, into textviews
        TextView title = (TextView) bottomSheet.findViewById(R.id.marker_title);
        TextView snippet = (TextView) bottomSheet.findViewById(R.id.marker_snippet);
        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());
        //Sets state of bottomSheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /** Stores filter data and coordinates from camera position to avoid it resetting on screen rotation*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("filterlist",filterList);
        outState.putInt("filter_activated",FILTER_ACTIVATED);

        outState.putDouble("lat",gMap.getCameraPosition().target.latitude);
        outState.putDouble("lon",gMap.getCameraPosition().target.longitude);
    }
    /** Restores the saved data, also sets RESTORED variable to true*/
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        filterList = savedInstanceState.getStringArrayList("filterlist");
        FILTER_ACTIVATED = savedInstanceState.getInt("filter_activated");
        restoredCP = new LatLng(savedInstanceState.getDouble("lat"),savedInstanceState.getDouble("lon"));
        RESTORED = true;
    }

    /** Handles the result of permission requests*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /** Checks if location permission is granted*/
    @AfterPermissionGranted(LOCATION_PERMISSION)
    private void setLocationEnabled() {

        //IF permission granted, enables my location button, and if users last known location
        //is available moves camera to position, else moves camera to Haldens coordinates
        if (EasyPermissions.hasPermissions(this, locationPermission)) {
            gMap.setMyLocationEnabled(true);
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                Log.v(TAG, location.toString());
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latitude,longitude), 15, 0, 0)));
            } else{
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(59.12478, 11.38754), 15, 0, 0)));
            }
        } else {
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(59.12478, 11.38754), 15, 0, 0)));
            EasyPermissions.requestPermissions(this, getString(R.string.no_location_permission),
                    LOCATION_PERMISSION, locationPermission);
        }
    }
}