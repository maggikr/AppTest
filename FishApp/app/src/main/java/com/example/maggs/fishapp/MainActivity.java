package com.example.maggs.fishapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.item_search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        return super.onCreateOptionsMenu(menu);
    }

    //Legger til "onClick" funksjonalitet på menyelementer.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
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

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng halden = new LatLng(59.12478, 11.38754);
        googleMap.addMarker(new MarkerOptions().position(halden)
                .title("Marker in Halden"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(halden));
    }
}
