package com.misiontic.peligrapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private CircleImageView circleImageView;
    private TextInputEditText editTextEmailSignUp,editTextPasswordSignUp,editTextUsername;
    private Button buttonRegistrer;
    boolean imageControl = false;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;
    FirebaseUser firebaseUser;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        circleImageView = findViewById(R.id.imageViewCircle);
        editTextEmailSignUp = findViewById(R.id.editTextEmailSignUp1);
        editTextPasswordSignUp = findViewById(R.id.editTextPasswordSignUp1);
        editTextUsername = findViewById(R.id.editTextUsername1);
        buttonRegistrer = findViewById(R.id.buttonRegistrer);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imageChooser();

            }
        });

        buttonRegistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = editTextEmailSignUp.getText().toString();
                String password = editTextPasswordSignUp.getText().toString();
                String username = editTextUsername.getText().toString();
                Log.i("SIGNUP",email+" "+password+" "+username);

                if (!email.equals("") && !password.equals("") && !username.equals(""))
                {
                    Log.i("SIGNUP",email+" "+password+" "+username);
                    signUp(username,email,password);
                    editTextEmailSignUp.getText().clear();
                    editTextUsername.getText().clear();;
                    editTextPasswordSignUp.getText().clear();
                }

            }
        });
    }

    public void imageChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();
            Picasso.get().load(imageUri).into(circleImageView);
            imageControl=true;
        }
        else
        {
            imageControl=false;
        }
    }

    public void signUp(String userName, String email, String password){
        Log.i("SIGNUP",email+" "+password+" "+userName);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    reference.child("Users").child(auth.getUid()).child("userName").setValue(userName);

                    if(imageControl){

                        UUID randomId = UUID.randomUUID();
                        String imageName = "images/"+randomId+".jpg";
                        storageReference.child(imageName).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                StorageReference myStorageReference = firebaseStorage.getReference(imageName);
                                myStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();
                                        reference.child("Users").child(auth.getUid()).child("image").setValue(filePath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(SignUpActivity.this, "Se agrego exitosamente a la DB.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignUpActivity.this, "No se pudo agregar a la DB.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.getLocalizedMessage();
                                    }
                                });
                            }
                        });
                    }
                    else{
                        reference.child("Users").child(auth.getUid()).child("image").setValue("null");
                    }

                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String email = firebaseUser.getEmail();
                    Intent intent = new Intent(SignUpActivity.this,MapsActivity.class);
                    final Bundle bolsa = new Bundle();
                    bolsa.putString("user",email);
                    intent.putExtras(bolsa);
                    Log.i("USER_IN_BUNDLE",bolsa.getString("user"));
                    startActivity(intent);
                }
                else{

                    Toast.makeText(SignUpActivity.this, "Ocurrio un problema", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}