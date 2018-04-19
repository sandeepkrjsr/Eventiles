package com.kodexlabs.eventiles;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ScrollingActivity extends AppCompatActivity {

    private TextView title, type, institute, venue, date, description, contact, mail, editorname, timestamp;
    private ImageView poster, layout;
    private Button website, facebook;
    private FloatingActionButton fab_bookmark, fab_share;
    private CircleImageView editorimg;

    private boolean favSelected = false;

    private List<String> bookmarks;

    private String card_id, card_title;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

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
        if (id == R.id.action_report_false) {
            return true;
        }
        /*if (id == R.id.action_delete) {
            databaseReference.child(card_id).removeValue();
            //Todo delete images
            //storageReference = FirebaseStorage.getInstance().getReference().child(String.valueOf(poster));
            //storageReference.delete();
            //Toast.makeText(this, ""+storageReference, Toast.LENGTH_SHORT).show();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bookmarks = new ArrayList<>();
        final DB_Bookmark db = new DB_Bookmark(this);
        Cursor c;
        db.open();
        c = db.getAllBookmark();
        if (c.moveToFirst()){
            do {
                DisplayBookmarks(c);
            } while (c.moveToNext());
        }
        db.close();

        fab_bookmark = (FloatingActionButton)findViewById(R.id.fab_bookmark);
        fab_share = (FloatingActionButton)findViewById(R.id.fab_share);

        Bundle bundle = getIntent().getExtras();
        card_id = bundle.getString("card_id");
        card_title = bundle.getString("card_title");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Eventiles/Events");
        databaseReference.keepSynced(true);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        title = (TextView) findViewById(R.id.title);
        type = (TextView) findViewById(R.id.type);
        institute = (TextView) findViewById(R.id.institute);
        venue = (TextView) findViewById(R.id.venue);
        date = (TextView) findViewById(R.id.date);
        description = (TextView) findViewById(R.id.description);
        contact = (TextView) findViewById(R.id.contact);
        mail = (TextView)findViewById(R.id.mail);
        poster = (ImageView) findViewById(R.id.poster);
        website = (Button) findViewById(R.id.website);
        facebook = (Button)findViewById(R.id.facebook);
        editorname = (TextView)findViewById(R.id.editor_name);
        editorimg = (CircleImageView)findViewById(R.id.editor_image);
        timestamp = (TextView)findViewById(R.id.timestamp);
        layout = (ImageView) findViewById(R.id.layout);

        setTitle(card_title);
        databaseReference.child(card_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();

                String get_type = map.get("type");
                String get_institute = map.get("institute");
                String get_venue = map.get("city")+", "+map.get("state");
                String get_date_start = map.get("date_start");
                String get_date_ends = map.get("date_ends");
                String get_description = map.get("description");
                String get_contact = map.get("contact");
                String get_mail = map.get("mail");
                String get_editorid = map.get("editorid");
                String get_editorname = map.get("editorname");
                String get_timestamp = map.get("timestamp");
                final String get_poster = map.get("poster");
                final String get_website = map.get("website");
                final String get_facebook = map.get("facebook");

                type.setText(get_type);
                institute.setText(get_institute);
                venue.setText(get_venue);
                date.setText("Starts at "+get_date_start+"\n\nEnds at "+get_date_ends);
                description.setText(get_description);
                contact.setText(get_contact);
                mail.setText(get_mail);
                editorname.setText(get_editorname);
                timestamp.setText(get_timestamp);

                Picasso.with(getBaseContext()).load("http://graph.facebook.com/"+get_editorid+"/picture?type=square").into(editorimg);

                Picasso.with(getBaseContext()).load(get_poster).centerCrop().fit().transform(new GradientTransformation()).networkPolicy(NetworkPolicy.OFFLINE).into(poster, new Callback() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onError() {
                        Picasso.with(getBaseContext()).load(get_poster).centerCrop().fit().transform(new GradientTransformation()).into(poster);
                    }
                });
                //Picasso.with(getBaseContext()).load(get_poster).centerCrop().fit().transform(new GradientTransformation()).into(poster);

                website.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(get_website));
                        startActivity(browser);
                    }
                });
                facebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(get_facebook));
                        startActivity(browser);
                    }
                });

                poster.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout.setVisibility(View.VISIBLE);
                        Picasso.with(getBaseContext()).load(get_poster).networkPolicy(NetworkPolicy.OFFLINE).into(layout, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                Picasso.with(getBaseContext()).load(get_poster).into(layout);
                            }
                        });
                        //Picasso.with(getBaseContext()).load(get_poster).into(layout);
                        //PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(layout);
                        //photoViewAttacher.update();
                    }
                });
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout.setVisibility(View.GONE);
                    }
                });

                Clear_Layout(get_date_start, get_date_ends, get_description, get_contact, get_mail, get_website, get_facebook);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (bookmarks.contains(card_id)){
            favSelected = true;
            fab_bookmark.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        }
        fab_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favSelected == false){
                    favSelected = true;
                    fab_bookmark.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                    db.open();
                    db.insertBookmark(card_id);
                    db.close();
                    bookmarks.add(card_id);
                    Snackbar.make(view, "This Event is Bookmarked", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }else {
                    favSelected = false;
                    fab_bookmark.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    db.open();
                    Cursor c = db.searchBookmark("'"+card_id+"'");
                    db.deleteBookmark(Integer.parseInt(c.getString(0)));
                    db.close();
                    bookmarks.remove(card_id);
                    Snackbar.make(view, "Bookmark is Removed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, "Eventiles");
                send.putExtra(Intent.EXTRA_TEXT, "Checkout this Event *"+card_title+"* on\n*Eventiles*"+" \nhttps://play.google.com/store/apps/details?id=com.hastago.hastago&hl=en");
                startActivity(Intent.createChooser(send, "Share"));
            }
        });
    }

    private void Clear_Layout(String get_date_start, String get_date_ends, String get_description, String get_contact, String get_mail, String get_website, String get_facebook) {
        View line1 = (View)findViewById(R.id.line1);
        View line2 = (View)findViewById(R.id.line2);

        if (get_date_ends.isEmpty()){
            date.setText("Starts at "+get_date_start);
        }

        if (get_website.isEmpty()){
            website.setVisibility(View.GONE);
        }if (get_facebook.isEmpty()){
            facebook.setVisibility(View.GONE);
        }

        if (get_description.isEmpty()){
            description.setText("No Description Available");
        }if (get_contact.isEmpty()){
            contact.setVisibility(View.GONE);
            line1.setVisibility(View.GONE);
        }if (get_mail.isEmpty()){
            mail.setVisibility(View.GONE);
            line2.setVisibility(View.GONE);
        }
    }

    private void DisplayBookmarks(Cursor c) {
        bookmarks.add(c.getString(1));
        //Toast.makeText(this, bookmarks.toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "id: " + c.getString(0) +"\n" + "Name: " + c.getString(1), Toast.LENGTH_SHORT).show();
    }
}
