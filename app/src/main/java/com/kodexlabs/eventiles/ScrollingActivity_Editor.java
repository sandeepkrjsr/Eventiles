package com.kodexlabs.eventiles;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 1505560 on 20-May-17.
 */

public class ScrollingActivity_Editor extends AppCompatActivity {

    private EditText title, institute, city, description, contact, mail;
    private TextView type, state, date_start, date_ends;
    private ImageView poster;
    private ImageButton img;
    private Button website, facebook;
    private Button upload;

    private String get_title, get_type, get_institute, get_city, get_state, get_date_start, get_date_ends, get_description, get_contact, get_mail, get_website, get_facebook, get_idFacebook, get_username, get_timestamp;
    private String post_total, post_live;
    private String datecard, datetype;
    private RelativeLayout geturl;
    private WebView webView;
    private FloatingActionButton url;

    private Uri imguri = null;
    private static final int GALLERY_REQUEST = 1;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_scrolling_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Editor's Mode");

        Intent bundle = getIntent();
        get_idFacebook = bundle.getStringExtra("idFacebook");
        get_username = bundle.getStringExtra("username");

        storageReference = FirebaseStorage.getInstance().getReference().child("Event_Images");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Eventiles/Events");

        progressDialog = new ProgressDialog(this);

        title = (EditText)findViewById(R.id.title);
        type = (TextView)findViewById(R.id.type);
        institute = (EditText)findViewById(R.id.institute);
        city = (EditText)findViewById(R.id.city);
        state = (TextView) findViewById(R.id.state);
        date_start = (TextView)findViewById(R.id.date_start);
        date_ends = (TextView)findViewById(R.id.date_ends);
        description = (EditText)findViewById(R.id.description);
        contact = (EditText)findViewById(R.id.contact);
        mail = (EditText)findViewById(R.id.mail);
        poster = (ImageView)findViewById(R.id.poster);
        img = (ImageButton)findViewById(R.id.img);
        website = (Button)findViewById(R.id.website);
        facebook = (Button)findViewById(R.id.facebook);
        upload = (Button)findViewById(R.id.upload);

        geturl = (RelativeLayout)findViewById(R.id.geturl);
        webView = (WebView)findViewById(R.id.webview);
        url = (FloatingActionButton)findViewById(R.id.url);
        website.setTag("");
        facebook.setTag("");

        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CharSequence choices[] = new CharSequence[] {"College Fest","Startup"};
                CharSequence choices[] = getResources().getStringArray(R.array.event_type);
                Select_Option("Select Event Type", choices);
            }
        });

        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence choices[] = getResources().getStringArray(R.array.indian_states);
                Select_Option("Select State", choices);
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST);
            }
        });

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geturl.setVisibility(View.VISIBLE);
                webView.setWebViewClient(new Callback());
                webView.loadUrl("http://www.google.com");
                url.setTag("website");
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geturl.setVisibility(View.VISIBLE);
                webView.setWebViewClient(new Callback());
                webView.loadUrl("http://www.facebook.com");
                url.setTag("facebook");
            }
        });

        date_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datetype = "date_start";
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR), mMonth = c.get(Calendar.MONTH), mDay = c.get(Calendar.DATE);
                DatePickerDialog dialog = new DatePickerDialog(ScrollingActivity_Editor.this, new mDateSetListener(), mYear, mMonth, mDay);
                dialog.show();
            }
        });

        date_ends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datetype = "date_ends";
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR), mMonth = c.get(Calendar.MONTH), mDay = c.get(Calendar.DATE);
                DatePickerDialog dialog = new DatePickerDialog(ScrollingActivity_Editor.this, new mDateSetListener(), mYear, mMonth, mDay);
                dialog.show();
            }
        });

        url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = url.getTag().toString();
                if (tag.compareTo("website")==0){
                    website.setTag(webView.getUrl());
                    website.setTextColor(Color.GRAY);
                }else if (tag.compareTo("facebook")==0){
                    facebook.setTag(webView.getUrl());
                    facebook.setTextColor(Color.GRAY);
                }
                geturl.setVisibility(View.GONE);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event_Firebase();
                //User_Update();
            }
        });
    }

    private void Select_Option(final String header, final CharSequence[] choices) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(header);
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (header.compareTo("Select Event Type")==0){
                    type.setText(choices[which]);
                }else if (header.compareTo("Select State")==0){
                    state.setText(choices[which]);
                }
            }
        });
        builder.create().show();
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }
    }

    class mDateSetListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker view, int mYear, int mMonth, int mDay) {
            Date date = new Date(mYear-1900, mMonth, mDay);
            SimpleDateFormat simpledateformat = new SimpleDateFormat("EEEE, dd MMMM yyyy");
            SimpleDateFormat card_dateformat = new SimpleDateFormat("yyyy-MM-dd");
            //SimpleDateFormat card_dateformat = new SimpleDateFormat("dd MMM\nEEE");
            if (datetype.compareTo("date_start")==0){
                date_start.setText(simpledateformat.format(date));
                datecard = card_dateformat.format(date);
            }else if (datetype.compareTo("date_ends")==0){
                date_ends.setText(simpledateformat.format(date));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imguri = data.getData();
            Picasso.with(getBaseContext()).load(imguri).centerCrop().fit().transform(new GradientTransformation()).into(poster);
            title.setTextColor(Color.WHITE);
            title.setHintTextColor(Color.GRAY);
        }
    }

    private void Event_Firebase() {
        progressDialog.setMessage("Uploading!");
        progressDialog.show();

        get_title = title.getText().toString();
        get_type = type.getText().toString();
        get_institute = institute.getText().toString();
        get_city = city.getText().toString();
        get_state = state.getText().toString();
        get_date_start = date_start.getText().toString();
        get_date_ends = date_ends.getText().toString();
        get_description = description.getText().toString();
        get_contact = contact.getText().toString();
        get_mail = mail.getText().toString();
        get_website = website.getTag().toString();
        get_facebook = facebook.getTag().toString();
        get_timestamp = new SimpleDateFormat("dd MMM yyyy").format(new Date());

        if (!get_title.isEmpty() && !get_institute.isEmpty() && !get_city.isEmpty() && !get_date_start.isEmpty() && imguri != null /*&& !get_website.isEmpty() && !get_facebook.isEmpty()*/) {
            StorageReference filepath = storageReference.child(imguri.getLastPathSegment());
            filepath.putFile(imguri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloaduri = taskSnapshot.getDownloadUrl();

                    DatabaseReference newPost = databaseReference.push();
                    newPost.child("id").setValue(newPost.getKey());
                    newPost.child("title").setValue(get_title);
                    newPost.child("type").setValue(get_type);
                    newPost.child("institute").setValue(get_institute);
                    newPost.child("city").setValue(get_city);
                    newPost.child("state").setValue(get_state);
                    newPost.child("date_start").setValue(get_date_start);
                    newPost.child("date_ends").setValue(get_date_ends);
                    newPost.child("description").setValue(get_description);
                    newPost.child("contact").setValue(get_contact);
                    newPost.child("mail").setValue(get_mail);
                    newPost.child("website").setValue(get_website);
                    newPost.child("facebook").setValue(get_facebook);
                    newPost.child("status").setValue("1");
                    newPost.child("date").setValue(datecard);
                    newPost.child("editorid").setValue(get_idFacebook);
                    newPost.child("editorname").setValue("by  "+ get_username);
                    newPost.child("timestamp").setValue("posted on " + get_timestamp);
                    newPost.child("poster").setValue(downloaduri.toString());
                    Toast.makeText(getBaseContext(), "Uploaded Successfully !", Toast.LENGTH_SHORT).show();

                    progressDialog.dismiss();
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("idFacebook", get_idFacebook);
                    intent.putExtra("username", get_username);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }else {
            progressDialog.dismiss();
            Toast.makeText(getBaseContext(), "Something is missing.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(getBaseContext(), "Cancelled !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("idFacebook", get_idFacebook);
                intent.putExtra("name", get_username);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Todo-------
    /*private void User_Update() {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Eventiles/Users");
        //DatabaseReference oldUser = userReference.child(get_facebook);
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    String userid = user.getId();
                    Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                    if (userid.equals(get_idFacebook)) {
                        post_total = map.get("post_total");
                        post_live = map.get("post_live");
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Toast.makeText(getBaseContext(), post_total, Toast.LENGTH_SHORT).show();
        //newUser.child("post_total").setValue(Integer.parseInt(post_total) + 1);
        //newUser.child("post_live").setValue(Integer.parseInt(post_live) + 1);
    }*/
}
