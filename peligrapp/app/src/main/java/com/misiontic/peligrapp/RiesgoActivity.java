package com.misiontic.peligrapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.misiontic.peligrapp.models.RiesgoModel;

import java.io.Serializable;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiesgoActivity extends AppCompatActivity implements Serializable {

    private Button btnReportar, btnCancelar;
    private EditText txtDescripcion;
    private ImageView imagenRiesgo;
    boolean imageControl = false;
    //private Integer idRiesgo;
    private Bundle bolsa;
    private Boolean seAgrego;
    private String idRiesgo;
    private Uri imageUri;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseReference mDatabase;
    String latitud, longitud, user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riesgo);

        btnReportar = findViewById(R.id.btnReportar);
        btnCancelar = findViewById(R.id.btnCancelar);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        imagenRiesgo = findViewById(R.id.imageViewCircle);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imagenRiesgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                elegirImagen();

            }
        });

        bolsa = getIntent().getExtras();
        if(bolsa != null){
            if (!bolsa.getString("user", "").equalsIgnoreCase("") || !bolsa.getString("user", "").equalsIgnoreCase("null")){
                user = bolsa.getString("user", "");
                //idRiesgo = Integer.valueOf(bolsa.getString("registros", ""));
                idRiesgo = bolsa.getString("idRiesgo");
            }
            latitud = bolsa.getString("lat","");
            longitud = bolsa.getString("long","");
            Log.i("BOLSAM","lat: "+bolsa.getString("lat","")+" long: "+bolsa.getString("long","")+" user: "+bolsa.getString("user", ""));
            Log.i("BOLSAMvar","lat: "+latitud+" long: "+longitud+" user: "+user);

        }

        btnReportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                subirImagen();
                guardarReporte(txtDescripcion.getText().toString(), latitud, longitud, user);
                Intent returnIntent = new Intent();
                setResult(RiesgoActivity.RESULT_OK,returnIntent);
                //finish();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndRemoveTask();
            }
        });
    }

    public void elegirImagen(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && data != null && data.getData() != null){
            imageUri = data.getData();
            Log.i("UPLOAD_onActivityResult",imageUri.toString());
            Picasso.get().load(imageUri).into(imagenRiesgo);
            imageControl = true;
        }
        else{
            imageControl = false;
        }
    }

    public void subirImagen(){
        Log.i("UPLOAD","Entró "+imageUri.toString());
        if (imageControl){
            Log.i("UPLOAD","ImageUri is not null ");
            String imageName = "images/"+idRiesgo+".jpg";
            storageReference.child(imageName).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(RiesgoActivity.this, "Imagen guardada exitosamente", Toast.LENGTH_SHORT).show();
                    StorageReference myStorageRef = firebaseStorage.getReference(imageName);
                    myStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String filepath = uri.toString();
                            mDatabase.child("riesgos").child(idRiesgo).child("imagen").setValue(filepath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(RiesgoActivity.this, "Imagen en base de datos.", Toast.LENGTH_SHORT).show();
                                    //Cerrar Actividad
                                    finishAndRemoveTask();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(RiesgoActivity.this, "Error, URL no obtenid", Toast.LENGTH_SHORT).show();
                                }
                            });
                            
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RiesgoActivity.this, "Error en imagen", Toast.LENGTH_SHORT).show();
                }
            });

            }
        else {
            Toast.makeText(RiesgoActivity.this, "Por favor seleccione una imagen", Toast.LENGTH_SHORT).show();
            Log.i("UPLOAD","ImageUri might be null ");
        }
    }

    public void guardarReporte(String descr, String lat, String longi, String iduser) {
        Log.i("NO_INFO", "Esto es null? "+ txtDescripcion.getText().toString());
            String estado = "Activo";
            String imagen = " ";
            RiesgoModel riesgo = new RiesgoModel(idRiesgo, descr, lat, longi, iduser, estado, imagen);
            mDatabase.child("riesgos").child(idRiesgo).setValue(riesgo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(RiesgoActivity.this, "Riesgo añadido", Toast.LENGTH_SHORT).show();
                    Log.i("UPLOAD_RISK_SUCC", "Riesgo añadido " + idRiesgo);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("UPLOAD_RISK_FAIL", "Error");
                    Toast.makeText(RiesgoActivity.this, "Ocurrió un error, por favor intente de nuevo", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
