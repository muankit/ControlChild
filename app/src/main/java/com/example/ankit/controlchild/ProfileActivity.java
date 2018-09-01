package com.example.ankit.controlchild;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileActivity extends AppCompatActivity {

    // toolbar instance
    private Toolbar mToolbar;
    RelativeLayout rootLayout;

    //Array for invite code digits
    public char inviteArray[] = new char[7];

    // Text view Instances
    private TextView mProfileUsernameView;
    private TextView mProfileEmailView;
    private Button mInviteCodeShareBtn;

    public TextView mInviteCodetext1;
    public TextView mInviteCodetext2;
    public TextView mInviteCodetext3;
    public TextView mInviteCodetext4;
    public TextView mInviteCodetext5;
    public TextView mInviteCodetext6;
    public TextView mInviteCodetext7;


    //Firebase
    private DatabaseReference mUserDataBaseRef;
    private FirebaseAuth mAuth;


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

        setContentView(R.layout.activity_profile);

        //Custom Toolbar
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar()
                .setTitle("Profile");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProfileUsernameView = (TextView) findViewById(R.id.profile_name_view);
        mProfileEmailView = (TextView) findViewById(R.id.profile_email_view);
        mInviteCodeShareBtn = (Button) findViewById(R.id.profile_invite_share_btn);

        //Invite code Text view
        mInviteCodetext1 = (TextView) findViewById(R.id.profile_invite_code1);
        mInviteCodetext2 = (TextView) findViewById(R.id.profile_invite_code2);
        mInviteCodetext3 = (TextView) findViewById(R.id.profile_invite_code3);
        mInviteCodetext4 = (TextView) findViewById(R.id.profile_invite_code4);
        mInviteCodetext5 = (TextView) findViewById(R.id.profile_invite_code5);
        mInviteCodetext6 = (TextView) findViewById(R.id.profile_invite_code6);
        mInviteCodetext7 = (TextView) findViewById(R.id.profile_invite_code7);

        mInviteCodetext1.setBackground(getResources().getDrawable(R.drawable.edittext_background));
        mInviteCodetext2.setBackground(getResources().getDrawable(R.drawable.edittext_background));
        mInviteCodetext3.setBackground(getResources().getDrawable(R.drawable.edittext_background));
        mInviteCodetext4.setBackground(getResources().getDrawable(R.drawable.edittext_background));
        mInviteCodetext5.setBackground(getResources().getDrawable(R.drawable.edittext_background));
        mInviteCodetext6.setBackground(getResources().getDrawable(R.drawable.edittext_background));
        mInviteCodetext7.setBackground(getResources().getDrawable(R.drawable.edittext_background));

    }

    @Override
    protected void onStart() {
        super.onStart();


        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            mUserDataBaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mAuth.getCurrentUser().getUid());
            mUserDataBaseRef.keepSynced(true);
        }

        mUserDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                final String inviteCode = dataSnapshot.child("uniqueID").getValue().toString();

                mProfileUsernameView.setText(username);
                mProfileEmailView.setText(email);

                for (int i = 0; i < inviteCode.length(); ++i) {
                    inviteArray[i] = inviteCode.charAt(i); // This array giving me correct result
                }

//                for (int i=0 ; i<inviteArray.length; ++i){
//                    inviteTextView[i].setText("" + inviteArray[i]);
//                }

                mInviteCodetext1.setText(""+inviteArray[0]);
                mInviteCodetext2.setText(""+inviteArray[1]);
                mInviteCodetext3.setText(""+inviteArray[2]);
                mInviteCodetext4.setText(""+inviteArray[3]);
                mInviteCodetext5.setText(""+inviteArray[4]);
                mInviteCodetext6.setText(""+inviteArray[5]);
                mInviteCodetext7.setText(""+inviteArray[6]);

                mInviteCodeShareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        //sharingIntent.setPackage("com.whatsapp");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, inviteCode);
                        try {
                            startActivity(sharingIntent);
                        }catch (android.content.ActivityNotFoundException ex){
                            Toast.makeText(ProfileActivity.this, "Whatsapp not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
