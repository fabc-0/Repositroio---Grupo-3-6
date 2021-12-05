package com.misiontic.peligrapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.LauncherApps;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

public class RiskReportActivity extends AppCompatActivity {

    private Bundle bolsa;
    private TextView riskReportDescription;
    private Button buttonConfirmReport;
    private ImageView riskReportImage;



    FirebaseDatabase mDatabase;
    DatabaseReference reportInfo;
    String currentUser, reporterUser, idRiesgo, descripcionReporte, urlImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risk_report);


        mDatabase = FirebaseDatabase.getInstance();
        reportInfo = mDatabase.getReference();
        riskReportDescription = findViewById(R.id.riskReportDescription);
        buttonConfirmReport = findViewById(R.id.buttonConfirmReport);
        riskReportImage = findViewById(R.id.riskReportImage);


        //Obtener datos del marcador y del usuario conectado
        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            currentUser = bolsa.getString("user", "");
            idRiesgo = bolsa.getString("idriesgo", "");
        }
        Log.i("NULL_1","Esto no es null: "+idRiesgo);

        //Conseguir la información de la base de datos
        getReportInfo();






    }

    private void getReportInfo() {
        Log.i("NULL_2","Esto no es null: "+idRiesgo);
        reportInfo.child("riesgos").child(idRiesgo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                descripcionReporte = snapshot.child("descripcion").getValue().toString();
                urlImagen = snapshot.child("imagen").getValue().toString();
                reporterUser = snapshot.child("idusuario").getValue().toString();

                //Set Description
                riskReportDescription.setText(descripcionReporte);

                //Set Image
                if (urlImagen.equals("null")){
                    riskReportImage.setImageResource(R.drawable.ic_palceholder_pokemon_24);
                }
                else{
                    Picasso.get().load(urlImagen).into(riskReportImage);
                }

                //Desactivar Boton de Confirmar
                if (currentUser.equals(reporterUser)){
                    buttonConfirmReport.setEnabled(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}