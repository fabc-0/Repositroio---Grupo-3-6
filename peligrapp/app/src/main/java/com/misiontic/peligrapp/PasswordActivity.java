package com.misiontic.peligrapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordActivity extends AppCompatActivity {

    private TextView textTitle;
    private ImageView imageLock;
    private Button buttonReset;
    private TextInputEditText emailReset;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        emailReset = findViewById(R.id.editTextForget);
        textTitle = findViewById(R.id.textTitle);
        imageLock = findViewById(R.id.imageView3);
        buttonReset = findViewById(R.id.buttonForget);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailReset.getText().toString();
                if(!email.equals("")){
                    passwordReset(email);
                }
            }
        });

        auth = FirebaseAuth.getInstance();
    }

    public void passwordReset(String email){

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(PasswordActivity.this, "Revice su correo", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(PasswordActivity.this, "Revice su correo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
