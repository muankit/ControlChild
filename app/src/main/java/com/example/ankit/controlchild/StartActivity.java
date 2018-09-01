package com.example.ankit.controlchild;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ankit.controlchild.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class StartActivity extends AppCompatActivity {

    Button signInBtn , registerBtn , forgotPasswordBtn;
    RelativeLayout rootLayout;

    //firebase section
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set contentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        setContentView(R.layout.activity_start);

        //init view
        forgotPasswordBtn = (Button) findViewById(R.id.forgotPassBtn);
        registerBtn = (Button) findViewById(R.id.btnRegister);
        signInBtn = (Button) findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        //init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        //Button events
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetPasswordDialog();
            }
        });

    }

    private void showResetPasswordDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please enter your registered Email");

        LayoutInflater inflater = LayoutInflater.from(this);
        View forgotPass_layout = inflater.inflate(R.layout.forgotpass_layout , null);

        final MaterialEditText forgotEmail = forgotPass_layout.findViewById(R.id.forgotEmail);

        dialog.setView(forgotPass_layout);

        final android.app.AlertDialog waitingDialog = new SpotsDialog(StartActivity.this , "Sending Email");

        //dialog button
        dialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                waitingDialog.show();

                if (TextUtils.isEmpty(forgotEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    //waitingDialog.dismiss();
                    return;
                }

                // sending reset password link to email
                users.orderByChild("email").equalTo(forgotEmail.getText().toString())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            auth.sendPasswordResetEmail(forgotEmail.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                Toast.makeText(StartActivity.this, "Reset Link send to Your Email", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(StartActivity.this, "Email Not Exist", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                            waitingDialog.dismiss();
                        } else {

                            Toast.makeText(StartActivity.this, "Email Not Exist", Toast.LENGTH_SHORT).show();
                            waitingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showLoginDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign In");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.login_layout , null);

        final MaterialEditText editEmail = login_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = login_layout.findViewById(R.id.editPassword);

        dialog.setView(login_layout);

        //dialog button
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //set btn disble while processing
                signInBtn.setEnabled(false);

                //validation
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (editPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too short !!! ", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                final android.app.AlertDialog waitingDialog = new SpotsDialog(StartActivity.this , "Logging In");
                waitingDialog.show();

                // firebase login
                auth.signInWithEmailAndPassword(editEmail.getText().toString() , editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();
                                startActivity(new Intent(StartActivity.this  , MapsActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout , "Failed" + e.getMessage() , Snackbar.LENGTH_SHORT)
                                .show();

                        //active btn again
                        signInBtn.setEnabled(true);
                    }
                });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.register_layout , null);

        final MaterialEditText editEmail = register_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = register_layout.findViewById(R.id.editPassword);
        final MaterialEditText editName = register_layout.findViewById(R.id.editName);
        final MaterialEditText editPhone = register_layout.findViewById(R.id.editPhone);
        final String inviteCode = createRandomCode(7);

        dialog.setView(register_layout);

        //dialog button
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //validation
                if(TextUtils.isEmpty(editEmail.getText().toString())){
                    Snackbar.make(rootLayout , "Please enter email address" , Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if(TextUtils.isEmpty(editPhone.getText().toString())){
                    Snackbar.make(rootLayout , "Please enter Phone number" , Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if(TextUtils.isEmpty(editPassword.getText().toString())){
                    Snackbar.make(rootLayout , "Please enter password" , Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if(editPassword.getText().toString().length() < 6 ){
                    Snackbar.make(rootLayout , "Password too short !!! " , Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                final android.app.AlertDialog waitingDialog = new SpotsDialog(StartActivity.this , "Registering");
                waitingDialog.show();

                //register to firebase
                auth.createUserWithEmailAndPassword(editEmail.getText().toString() , editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();

                                Map usermap = new HashMap();
                                usermap.put("email" , editEmail.getText().toString());
                                usermap.put("name" , editName.getText().toString());
                                usermap.put("phone" , editPhone.getText().toString());
                                usermap.put("password" , editPassword.getText().toString());
                                usermap.put("uniqueID" , inviteCode);

                                users.child(auth.getCurrentUser().getUid()).setValue(usermap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    waitingDialog.dismiss();
                                                    Snackbar.make(rootLayout , "Registered Successfully !!! " , Snackbar.LENGTH_SHORT)
                                                            .show();
                                                    Intent mapsIntent = new Intent(StartActivity.this , MapsActivity.class);
                                                    startActivity(mapsIntent);
                                                    finish();
                                                }else {
                                                    waitingDialog.dismiss();
                                                    Snackbar.make(rootLayout , " Not Registered " , Snackbar.LENGTH_SHORT)
                                                            .show();
                                                }
                                            }
                                        });
//                                user.setEmail(editEmail.getText().toString());
//                                user.setName(editName.getText().toString());
//                                user.setPhone(editPhone.getText().toString());
//                                user.setPassword(editPassword.getText().toString());
//                                user.setUniqueID(inviteCode);

//                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                        .setValue(user)
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//
//                                                waitingDialog.dismiss();
//                                                Snackbar.make(rootLayout , "Registered Successfully !!! " , Snackbar.LENGTH_SHORT)
//                                                        .show();
//                                                Intent mapsIntent = new Intent(StartActivity.this , MapsActivity.class);
//                                                startActivity(mapsIntent);
//                                                finish();
//                                            }
//                                        })
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                waitingDialog.dismiss();
//                                                Snackbar.make(rootLayout , "Failed " + e.getMessage() , Snackbar.LENGTH_SHORT)
//                                                        .show();
//                                            }
//                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootLayout , "Failed " + e.getMessage() , Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();

        if(currentUser != null){
            Intent startIntent = new Intent(StartActivity.this  , MapsActivity.class);
            startActivity(startIntent);
            finish();
        }
    }

    public String createRandomCode(int codeLength){
        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output ;
    }

}
