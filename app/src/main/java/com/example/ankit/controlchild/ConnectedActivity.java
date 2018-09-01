package com.example.ankit.controlchild;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ankit.controlchild.Model.Connected;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ConnectedActivity extends AppCompatActivity {

    private RecyclerView mConnectedList;
    private android.support.v7.widget.Toolbar mToolbar;


    private DatabaseReference mConnectedUserDatabase , mUserDatabse;
    private FirebaseAuth mAuth;

    private String mCurrentUser , mConnectedUserId;
    private TextView mEmptyTextView;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        //set contentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        //Custom Toolbar
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.connected_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Connected");


        mConnectedList = (RecyclerView) findViewById(R.id.connectedUsersList);
        mEmptyTextView = (TextView) findViewById(R.id.emptyTextView);

        mAuth = FirebaseAuth.getInstance();

        mConnectedUserId = getIntent().getStringExtra("connectedUserId");


        if(mAuth.getCurrentUser() != null){
            mCurrentUser = mAuth.getCurrentUser().getUid();
            Log.d("CurrentUser", mCurrentUser);
        }
        mConnectedUserDatabase = FirebaseDatabase.getInstance().getReference().child("Connected").child(mCurrentUser);
        mUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users");

        mConnectedList.setHasFixedSize(true);
        mConnectedList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Connected , ConnectedViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Connected, ConnectedViewHolder>(
                Connected.class,
                R.layout.single_connected_layout,
                ConnectedViewHolder.class,
                mConnectedUserDatabase

        ) {
            @Override
            protected void populateViewHolder(final ConnectedViewHolder viewHolder, Connected model, int position) {


                final String currentUserId = getRef(position).getKey();

                mUserDatabse.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userEmail = dataSnapshot.child("email").getValue().toString();

                        viewHolder.setName(userName);
                        viewHolder.setEmail(userEmail);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence remove[]  = new CharSequence[]{"Remove Connection"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(ConnectedActivity.this);

                                builder.setTitle("Select Option");
                                builder.setItems(remove, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click events are here

                                        if(i==0){
                                            removeConnection(mCurrentUser);
                                        }

                                    }
                                });
                                builder.show();
                            }

                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                mEmptyTextView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
            }
        };

        mConnectedList.setAdapter(firebaseRecyclerAdapter);
    }

    private void removeConnection(String currentUserId) {

        DatabaseReference currentUserConnection = FirebaseDatabase.getInstance().getReference().child("Connected").child(currentUserId);
        DatabaseReference connectUserConnection = FirebaseDatabase.getInstance().getReference().child("Connected").child(mConnectedUserId);
        currentUserConnection.removeValue();
        connectUserConnection.removeValue();
    }


    public static class ConnectedViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public ConnectedViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setEmail(String email){
            TextView userEmail = (TextView) mView.findViewById(R.id.userEmailText);
            userEmail.setText(email);
        }

        public void setName(String name){
            TextView userName = (TextView) mView.findViewById(R.id.userNameText);
            userName.setText(name);
        }
    }

}
