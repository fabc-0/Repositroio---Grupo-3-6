package com.misiontic.peligrapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.misiontic.peligrapp.databinding.ActivityMapsBinding;
import com.misiontic.peligrapp.models.RiesgoModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Serializable {


    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton addBtn;
    private FloatingActionButton recenterButton;
    public Integer REQ_CODE = 200;
    public Integer permisoCoarse, permisoFine, permisoInternet;
    public Double latitud, longitud, latriesgo, longriesgo;
    private Marker miUbicacion, riesgosMarker, miUbicacionRiesgo;
    private Bundle bolsa;
    private String user, registros;
    private static final int EDIT_CODE = 31;

    private List <RiesgoModel> riesgolList = new ArrayList<>();

    DatabaseReference database = FirebaseDatabase.getInstance().getReference("riesgos");
    //DatabaseReference riesgosT = database.getReference("riesgos");
    private List<RiesgoModel> riesgolLista = new ArrayList<>();
    FirebaseAuth auth;
    private boolean reloadNedeed=false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addBtn = findViewById(R.id.btnAdd);
        recenterButton = findViewById(R.id.recenterButton);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bolsa = getIntent().getExtras();
        if(bolsa != null){
            Log.i("BOLSAlogin",bolsa.getString("user",""));
            user = bolsa.getString("user","");
        }

       /* Recibir listado de riesgos */

        getRiesgos();


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latriesgo != null && longriesgo != null){
                    final Intent intent = new Intent(MapsActivity.this, RiesgoActivity.class);
                    final Bundle bolsa = new Bundle();
                    bolsa.putString("lat", String.valueOf(latriesgo));
                    bolsa.putString("long", String.valueOf(longriesgo));
                    bolsa.putString("user",user);
                    bolsa.putString("registros",registros);
                    bolsa.putString("idRiesgo", miUbicacionRiesgo.getTag().toString());
                    Log.i("RIESGO_ID_BOLSA",miUbicacionRiesgo.getTag().toString());
                    intent.putExtras(bolsa);
                    //startActivityForResult(new Intent(intent), EDIT_CODE);
                    startActivity(intent);
                    miUbicacionRiesgo.remove();
                    miUbicacion.setDraggable(true);
                    Log.i("BOLSALocation",bolsa.toString());
                }
                else {
                    Toast.makeText(MapsActivity.this, "Para añadir un riesgo, por favor arrastre el puntero hasta la ubicación deseada", Toast.LENGTH_LONG).show();
                }

            }
        });

        recenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng point = new LatLng(latitud, longitud);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,16));
            }
        });

        Log.i("RiesgosList item 1", String.valueOf(riesgolList.toString()));




    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void verificarPermisos(){
        permisoCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        Log.i("COARSE",permisoCoarse.toString());
        permisoFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        Log.i("FINE",permisoFine.toString());
        permisoInternet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        Log.i("INTERNET",permisoInternet.toString());

        if (permisoFine == PackageManager.PERMISSION_GRANTED && permisoCoarse == PackageManager.PERMISSION_GRANTED  && permisoInternet == PackageManager.PERMISSION_GRANTED ) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    crearRiesgosDB();
                    if (location != null) {
                        Log.i("PUNTO: ", location.toString());
                        Log.i("PUNTOS: ", location.getLatitude() + " Long: " + location.getLongitude());
                        latitud = location.getLatitude();
                        longitud = location.getLongitude();
                        miUbicacion=crearPunto(latitud, longitud, 122, "Mi Ubicación (Yo)", true);
                        miUbicacion.setTag("Ubicación Actual");
                        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                            @Override
                            public void onMarkerDrag(@NonNull Marker marker) {

                            }

                            @Override
                            public void onMarkerDragEnd(@NonNull Marker marker) {
                                if (marker.equals(miUbicacion)) {
                                    latriesgo = miUbicacion.getPosition().latitude;
                                    longriesgo = miUbicacion.getPosition().longitude;
                                    miUbicacionRiesgo = crearPunto(latriesgo, longriesgo, 54, "Ubicación Riesgo", true);
                                    miUbicacionRiesgo.setTag(UUID.randomUUID().toString());
                                    Log.i("RIESGO_ID_CREACION",miUbicacionRiesgo.getId());
                                    LatLng point = new LatLng(latitud, longitud);
                                    miUbicacion.setPosition(point);
                                    miUbicacion.setDraggable(false);
                                    Log.i("DRAGREF", String.valueOf(latriesgo + " " + longriesgo));
                                }
                                else {
                                    latriesgo = miUbicacionRiesgo.getPosition().latitude;
                                    longriesgo = miUbicacionRiesgo.getPosition().longitude;
                                    Log.i("DRAGREF2", String.valueOf(latriesgo + " " + longriesgo));
                                }
                            }

                            @Override
                            public void onMarkerDragStart(@NonNull Marker marker) {




                            }
                        });
                    }
                    else{
                        Log.i("ELSE1", "Location might be null");
                    }
                }
            });
        }
        else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, REQ_CODE);
            Log.i("ELSE2", "Lat:  Long: ");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        verificarPermisos();


        //Hacer clic en los punteros
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Log.i("MARKER_ID",marker.getTag().toString());
                if (marker.equals(miUbicacion) || marker.equals(miUbicacionRiesgo)){
                    Toast.makeText(MapsActivity.this, "No reporte asociado", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(MapsActivity.this,RiskReportActivity.class);
                    bolsa.putString("user",user);
                    bolsa.putString("idriesgo",marker.getTag().toString());
                    intent.putExtras(bolsa);
                    startActivity(intent);
                }


                return false;
            }
        });

    }

    public Marker crearPunto(double latitud, double longitud, float color, String titulo, Boolean drag){

        LatLng point = new LatLng(latitud, longitud);
        Marker punto = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(titulo)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(drag));
        //.icon(BitmapDescriptorFactory.fromPath("C:\\Users\\ds010109\\AndroidStudioProjects\\Peligro\\app\\src\\main\\res\\drawable\\ic_baseline_near_me_24.bmp")));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        mMap.setTrafficEnabled(true);
        registros = String.valueOf(riesgolList.size());
        Log.i("PuntoCreado", "PUNTO CREADO");
        return punto;

    }

    public void crearRiesgosDB(){

        Log.i("RIESGODB crearRiesgo", "Entro");
        Log.i("RIESGODB crearRiesgo", String.valueOf(riesgolList.size()));
        for (int i = 0; i < riesgolList.size(); i++)
        {
            Marker dibujar = crearPunto(Double.valueOf(riesgolList.get(i).latitud), Double.valueOf(riesgolList.get(i).longitud),14,"",false);
            dibujar.setTag(riesgolList.get(i).idriesgo);
            Log.i("RIESGODB ",riesgolList.get(i).idriesgo+" "+riesgolList.get(i).latitud+" "+riesgolList.get(i).longitud);
        }

    }

    public void getRiesgos(){
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RiesgoModel riesgo = snapshot.getValue(RiesgoModel.class);
                riesgolList.add(riesgo);
                Log.i("RiesgosList size", String.valueOf(riesgolList.size()));
                Log.i("RiesgosList contenido", String.valueOf(riesgolList.toString()));


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRiesgos();
        crearRiesgosDB();
    }

    @Override
    public void onBackPressed(){
        auth.getInstance().signOut();
        finish();
        Intent intent = new Intent(MapsActivity.this,LoginActivity.class);
        startActivity(intent);
    }

}