package com.example.jongho.newproject_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    private GoogleApiClient mGoogleApiClient;
    private  GoogleMap googleMap;
    private Location mLocation = null;
    private LatLng MJU;

    // Firebase 객체 생성
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFireDB = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // 클릭 이벤트 삽입
        this.googleMap.setOnMapClickListener(this);

        // 지도에 명지대의 범위만 보여주기 위해 결정한 범위
        LatLngBounds MJU_BOUND = new LatLngBounds(new LatLng(37.2172, 127.180), new LatLng(37.2245, 127.1919));

        // 나침반이 보이게 설정
        this.googleMap.getUiSettings().setCompassEnabled(true);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        this.googleMap.getUiSettings().setMapToolbarEnabled(true);

        // Camera
        this.googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        this.googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        this.googleMap.setMinZoomPreference(17.0f);
        this.googleMap.setMaxZoomPreference(18.0f);
        this.googleMap.setLatLngBoundsForCameraTarget(MJU_BOUND);

        // Click이벤트
        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMapLongClickListener(this);

        display();

    }

    public void pressGps(View view) {
        String[] permissions = new String[] {Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for(String permission:permissions) {
                int result = PermissionChecker.checkSelfPermission(this, permission);
                if(result == PermissionChecker.PERMISSION_GRANTED) ;
                else {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                }
            }
        }
        if(Build.VERSION.SDK_INT >= 24 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        LocationRequest request = new LocationRequest();
        request.setInterval(3000);
        request.setFastestInterval(3000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.mLocation = location;
        Log.e("SEX",  "Lat : " + mLocation.getLatitude() + ", Lon : " + mLocation.getLongitude());
        MJU = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        this.googleMap.addMarker(new MarkerOptions().position(MJU).title("Current My Position"));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(MJU));

    }
    // 클릭 이벤트
    @Override
    public void onMapClick(LatLng latLng) {
        Point clickPoint = this.googleMap.getProjection().toScreenLocation(latLng);
        LatLng point = this.googleMap.getProjection().fromScreenLocation(clickPoint);

//        Toast.makeText(this, "Click Point Lat : " + point.latitude + " Lon : " + point.longitude, Toast.LENGTH_LONG).show();

        // getItemActivity 전환 인텐트
        Intent intent = new Intent(this, getItemActivity.class);
        intent.putExtra("lat", point.latitude);
        intent.putExtra("lng", point.longitude);
        startActivity(intent);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Toast.makeText(this, "LongClick", Toast.LENGTH_LONG).show();
    }

    // Firebase 변화 수신
    private void display() {
        mFireDB.getReference("getItem/"+mFirebaseAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {

                    // 리스트의 아이템을 검색하거나 아이템 추가가 있을 때 수신
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        getItem getitem = dataSnapshot.getValue(com.example.jongho.newproject_1.getItem.class);

                        // 구글맵에 마커 추가
                        MapsActivity.this.googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(getitem.getLat(), getitem.getLng()))
                                .title(getitem.getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_announcement)));

                        Toast.makeText(MapsActivity.this, getitem.getTitle(), Toast.LENGTH_SHORT).show();
                    }

                    // 아이템 변화가 있을 때 수신
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    // 아이템이 삭제 되었을 때 수신
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    // 순서가 있는 리스트에서 순서가 변경 되었을 때 수신
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
