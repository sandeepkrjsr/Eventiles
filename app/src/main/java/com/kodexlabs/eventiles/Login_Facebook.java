package com.kodexlabs.eventiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Demonstrate Firebase Authentication using a Facebook access token.
 */
public class Login_Facebook extends AppCompatActivity {

    private static final String TAG = "FacebookLogin";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private CallbackManager mCallbackManager;

    //private ProgressDialog progressDialog;

    private String idFacebook, name, email, gender, birthday, timestamp;

    private static SharedPreferences preferences;
    private String prefName = "MyPref";
    private static final String UID = "UID";
    private static final String USER = "USER";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_facebook);

        timestamp = new SimpleDateFormat("dd MMM yyyy").format(new Date());

        preferences = getSharedPreferences(prefName, MODE_PRIVATE);
        String loggedin = preferences.getString(UID, "UID");
        String user = preferences.getString(USER, "USER");

        if (loggedin.equals("UID"))
            facebook_login();
        else
            nextActivity(loggedin, user);

        //progressDialog = new ProgressDialog(this);
        //progressDialog.setMessage("Loading...");

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    private void facebook_login(){
        // [START initialize_fblogin]
        // Initialize Facebook Login box_gray_item
        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        getFacebookData(object);
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, gender, birthday, location");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
            @Override
            public void onError(FacebookException error) {
                internet_status();
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
        // [END initialize_fblogin]

        final Button custom_fb = (Button) findViewById(R.id.custom_fb);
        custom_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
                //custom_fb.setVisibility(View.GONE);
                //Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fade_out);
                //custom_fb.startAnimation(animation);
            }
        });
    }

    private void getFacebookData(JSONObject object) {
        try {
            if(object.has("id"))
                idFacebook = object.getString("id");
            if(object.has("name"))
                name = object.getString("name");
            if(object.has("email"))
                email = object.getString("email");
            if(object.has("gender"))
                gender = object.getString("gender");
            if(object.has("birthday"))
                birthday = object.getString("birthday");
            //bundle.putString("location", object.getJSONObject("location").getString("name"));
            //Toast.makeText(getBaseContext(), "Uploaded Successfully!", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(UID, idFacebook);
            editor.putString(USER, name);
            editor.commit();

            User_Firebase();
            User_SQLDB();
            //SendDataToServer(idFacebook, name, email, gender, birthday);
            nextActivity(idFacebook, name);

        } catch (JSONException e) {}
    }

    private void nextActivity(String idFacebook, String name){
        if(idFacebook != null){
            findViewById(R.id.button_facebook_login).setVisibility(View.GONE);
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("idFacebook", idFacebook);
            intent.putExtra("name", name);
            startActivity(intent);
            finish();
        }
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    // [START on_activity_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    // [END on_activity_result]

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        //progressDialog.show();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login_Facebook.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        //progressDialog.hide();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_facebook]

    private void updateUI(FirebaseUser user) {
        //progressDialog.show();
        if (user != null) {
            /*findViewById(R.id.button_facebook_login).setVisibility(View.GONE);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);*/
        } else {
            findViewById(R.id.button_facebook_login).setVisibility(View.VISIBLE);
        }
    }

    private void User_Firebase() {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Eventiles/Users");
        final DatabaseReference newUser = userReference.child(idFacebook);
        newUser.child("id").setValue(idFacebook);
        newUser.child("login").setValue("facebook");
        newUser.child("name").setValue(name);
        newUser.child("email").setValue(email);
        newUser.child("dob").setValue(birthday);
        newUser.child("gender").setValue(gender);
        newUser.child("status").setValue("1");
        newUser.child("post_right").setValue("1");
        newUser.child("post_total").setValue("0");
        newUser.child("post_live").setValue("0");
        newUser.child("post_reject").setValue("0");
        newUser.child("timestamp").setValue(timestamp);
        newUser.child("lastlogin").setValue(timestamp);
        //Toast.makeText(getBaseContext(), ""+newUser, Toast.LENGTH_SHORT).show();
    }

    private void User_SQLDB(){
        final String DataParseUrl = "http://kiitecell.hol.es/BrandAd_User_Upload.php";
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                List<NameValuePair> data = new ArrayList<NameValuePair>();
                data.add(new BasicNameValuePair("idFacebook", idFacebook));
                data.add(new BasicNameValuePair("name", name));
                data.add(new BasicNameValuePair("email", email));
                data.add(new BasicNameValuePair("gender", gender));
                data.add(new BasicNameValuePair("birthday", birthday));
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(DataParseUrl);
                    httpPost.setEntity(new UrlEncodedFormEntity(data));
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                } catch (ClientProtocolException e) {
                } catch (IOException e) {
                }
                return "Data Submit Successfully";
            }
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(idFacebook, name, email, gender, birthday);
    }

    private void internet_status(){
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    //Toast.makeText(getBaseContext(), "Online", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "No Internet Connection !", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getBaseContext(), "Error in Listener", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static void facebook_logout(){
        LoginManager.getInstance().logOut();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(UID, "UID");
        editor.commit();
    }
}