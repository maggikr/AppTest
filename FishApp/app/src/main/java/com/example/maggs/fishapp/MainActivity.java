package com.example.maggs.fishapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap gMap;                                                             //Will hold google map instance
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("fishLocations"); //Connects to firebase and returns stored data under "fishLocations"

    private View bottomSheet;                                                           //
    private BottomSheetBehavior bottomSheetBehavior;                                    //Used to control bottomsheet

    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;                                    //Variable for search/autocomplete methods

    private String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};   //
    private static final int LOCATION_PERMISSION = 1;                                   //variables to check permission
    private static final String TAG = "FISHLOC MESSAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.main_toolbar);                              //Adds custom toolbar/Actionbar
        setSupportActionBar(toolbar);                                                   //

        bottomSheet = findViewById(R.id.bottom_sheet);                                  //
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);                    //
        bottomSheetBehavior.setPeekHeight(400);                                         //Adds bottom sheet, sets peek height(initial height on click) and hidden state on startup
        bottomSheetBehavior.setHideable(true);                                          //
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);                 //

    }


    public void onClickSearch(){                                                        //Opens search/autocomplete field
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {     //Handles selected location from search/autocomplete
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                gMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .title("here!"));
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(place.getLatLng(), 15, 0, 0)));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                     //Sets main_menu as actionbar menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                               //Sets onClick functionality to menu items and search button

        switch (item.getItemId()) {

            case R.id.item_search:
                onClickSearch();
                return true;

            case R.id.item_login:
                startActivity(new Intent(this, LoginActivity.class));
                return true;

            case R.id.item_regEvent:
                startActivity(new Intent(this, RegisterActivity.class));
                return true;

            case R.id.item_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;

            case R.id.item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.item_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {                                       //Adds markers/functionality to map at startup when loaded
        gMap = googleMap;

        LatLng halden = new LatLng(59.12478, 11.38754);
        gMap.addMarker(new MarkerOptions().position(halden)
                .title("Marker in Halden"));
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(halden, 15, 0, 0)));


        myRef.addValueEventListener(new ValueEventListener() {                          //Adds database listener, runs at startup and when data is updated

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fishLocsData: dataSnapshot.getChildren()) {           //Loads fishlocations and places markers
                    FishLoc fishLoc = fishLocsData.getValue(FishLoc.class);
                    Log.d(TAG, "Value is: " + dataSnapshot.getChildrenCount());
                    gMap.addMarker(new MarkerOptions().position(new LatLng(fishLoc.getLat(),fishLoc.getLng()))
                            .title(fishLoc.getFishType())
                            .snippet("ID: " + fishLoc.getId()));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });

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


    private void updateBottomSheetContent(Marker marker) {                              //Updates bottom sheet text views with info from marker and displays the bottom sheet
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


    @AfterPermissionGranted(LOCATION_PERMISSION)                                        //Runs permission check, requests permission if not yet granted
    private void setLocationEnabled() {

        if (EasyPermissions.hasPermissions(this, locationPermission)) {
            gMap.setMyLocationEnabled(true);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.no_location_permission),
                    LOCATION_PERMISSION, locationPermission);
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng) {                                         //Starts registerActivity on long clicks and brings latlng
        startActivity(new Intent(this, RegisterActivity.class)
                .putExtra("LatLng", latLng));
    }
}