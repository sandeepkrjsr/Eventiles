package com.kodexlabs.eventiles;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    private Fragment fragment;
    private FragmentManager fragmentManager;
    private Class fragmentClass;

    private FloatingActionButton fab;

    private FirebaseAuth mAuth;

    private String idFacebook, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent bundle = getIntent();
        idFacebook = bundle.getStringExtra("idFacebook");
        username = bundle.getStringExtra("name");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ScrollingActivity_Editor.class);
                intent.putExtra("idFacebook", idFacebook);
                intent.putExtra("username", username);
                startActivity(intent);
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        View header = navigationView.getHeaderView(0);
        final CircleImageView user_picture = (CircleImageView)header.findViewById(R.id.user_picture);
        TextView user_name = (TextView)header.findViewById(R.id.user_name);
        final ImageButton user_logout = (ImageButton)header.findViewById(R.id.user_logout);
        user_name.setText(username);
        Picasso.with(getBaseContext()).load("http://graph.facebook.com/"+idFacebook+"/picture?type=square").into(user_picture);
        user_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                LoginManager.getInstance().logOut();

                Login_Facebook.facebook_logout();
                Intent intent = new Intent(getBaseContext(), Login_Facebook.class);
                startActivity(intent);
                finish();
            }
        });
        //todo offline
        /*Picasso.with(getBaseContext()).load("http://graph.facebook.com/"+idFacebook+"/picture?type=square").networkPolicy(NetworkPolicy.OFFLINE).into(user_picture, new Callback() {
            @Override
            public void onSuccess() {
                Picasso.with(getBaseContext()).load("http://graph.facebook.com/"+idFacebook+"/picture?type=square").into(user_picture);
            }
            @Override
            public void onError() {
                Picasso.with(getBaseContext()).load(R.drawable.niklaus).into(user_picture);
            }
        });*/

        fragment = null;
        fragmentClass = Recycler.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {}
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        internet_status(header);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up box_gray_item, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            //mAuth = FirebaseAuth.getInstance();
            //mAuth.signOut();
            //LoginManager.getInstance().logOut();

            //Intent intent = new Intent(getBaseContext(), Login_Facebook.class);
            //startActivity(intent);
            //finish();

            return true;
        }

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        fragment = null;
        fragmentClass = null;

        switch (id) {
            case R.id.nav_home:
                fab.setVisibility(View.VISIBLE);
                fragmentClass = Recycler.class;
                break;
            case R.id.nav_bookmarks:
                fab.setVisibility(View.INVISIBLE);
                fragmentClass = Bookmark.class;
                break;
            case R.id.nav_personalise:
                fab.setVisibility(View.INVISIBLE);
                fragmentClass = Personalise.class;
                break;
            /*case R.id.nav_settings:
                fab.setVisibility(View.INVISIBLE);
                fragmentClass = Menu_settings.class;
                break;*/
            case R.id.nav_share:
                fab.setVisibility(View.VISIBLE);
                fragmentClass = Recycler.class;
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, "Eventiles");
                send.putExtra(Intent.EXTRA_TEXT, "Don't miss out any event\nDownload *Eventiles* Now\n"+"https://play.google.com/store/apps/details?id=com.hastago.hastago&hl=en");
                startActivity(Intent.createChooser(send, "Share"));
                break;
            case R.id.nav_rateus:
                fab.setVisibility(View.VISIBLE);
                fragmentClass = Recycler.class;
                Intent rateus = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.hastago.hastago&hl=en"));
                startActivity(rateus);
                break;
            case R.id.nav_feedback:
                fab.setVisibility(View.VISIBLE);
                fragmentClass = Recycler.class;
                String deviceInfo="Name : " + username + "\n";
                deviceInfo += "\nDevice Info :-";
                deviceInfo += "\n OS Version: " + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")";
                deviceInfo += "\n OS API Level: " + android.os.Build.VERSION.SDK_INT;
                deviceInfo += "\n Device: " + android.os.Build.DEVICE;
                deviceInfo += "\n Model: " + android.os.Build.MODEL + "\n \n \n";
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","sandeepkr.jsr1@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, deviceInfo);
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        item.setChecked(true);
        setTitle(item.getTitle());

        if (item.getTitle().equals("Home") || item.getTitle().equals("Share This App") || item.getTitle().equals("Rate Us") || item.getTitle().equals("Feedback"))
            setTitle(R.string.app_name);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (fragmentClass == Recycler.class){
            super.onBackPressed();
        }
        if (fragmentClass != Recycler.class){
            fragmentClass = Recycler.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {}
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            navigationView.getMenu().getItem(0).setChecked(true);
            setTitle("Eventiles");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        toggle.onConfigurationChanged(newConfig);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,  R.string.navigation_drawer_close);
    }

    private void internet_status(View header){
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        final ImageView internet_status = (ImageView)header.findViewById(R.id.internet_status);
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Picasso.with(getBaseContext()).load(R.drawable.net_online).into(internet_status);
                    //Toast.makeText(getBaseContext(), "Online", Toast.LENGTH_SHORT).show();
                } else {
                    Picasso.with(getBaseContext()).load(R.drawable.net_offline).into(internet_status);
                    //Toast.makeText(getBaseContext(), "Offline", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getBaseContext(), "Error in Listener", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
