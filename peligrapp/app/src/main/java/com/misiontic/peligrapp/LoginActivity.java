package com.misiontic.peligrapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.misiontic.peligrapp.models.RiesgoModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail,inputPassword;
    private Button buttonLogin,buttonSignUp;
    private TextView textForgotPassword;
    private Bundle bolsa;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    private List<RiesgoModel> riesgolList = new ArrayList<>();

    DatabaseReference database = FirebaseDatabase.getInstance().getReference("riesgos");

    @Override
    protected void onStart(){
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            String email = firebaseUser.getEmail();
            Intent intent = new Intent(LoginActivity.this,MapsActivity.class);
            final Bundle bolsa = new Bundle();
            bolsa.putString("user",email);
            intent.putExtras(bolsa);
            Log.i("USER_IN_BUNDLE",bolsa.getString("user"));
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.InputEmail1);
        inputPassword = findViewById(R.id.InputPassword1);
        buttonLogin =  findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textForgotPassword = findViewById(R.id.textForgotPassword);

        auth = FirebaseAuth.getInstance();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                if(!email.equals("") && !password.equals(""))
                {
                    signin(email,password);
                    inputPassword.getText().clear();
                    inputEmail.getText().clear();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Por favor ingrese credenciales validas.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

        textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,PasswordActivity.class);
                startActivity(intent);
            }
        });

    }

    public void signin(String email,String password)
    {
        getRiesgos();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final Bundle bolsa = new Bundle();
                    Toast.makeText(LoginActivity.this, "Login Exitoso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    bolsa.putString("user",email);
                    intent.putExtras(bolsa);
                    Log.i("USER_IN_BUNDLE",bolsa.getString("user"));
                    startActivity(intent);
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "No se pudo hacer el Login", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
}