package com.kodexlabs.eventiles.Interncamp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kodexlabs.eventiles.R;

import java.util.Map;

/**
 * Created by Niklaus on 28-Feb-17.
 */

public class Iternary extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iternary);

        final WebView webview = (WebView)findViewById(R.id.webview);
        webview.setWebViewClient(new Callback());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(false);
        webview.setBackgroundColor(Color.parseColor("#FFFFFF"));
        //webview.loadUrl("https://drive.google.com/file/d/1llLU-Fpo-K3W0i2bnA-1lYNPdFGeZ9wM/view");
        //webview.loadUrl("http://docs.google.com/gview?embedded=true&url=" + "http://kiitecell.hol.es/ShopStalk_Privacy_Policy.doc");
        //webview.loadUrl("https://docs.google.com/spreadsheets/d/e/2PACX-1vQIa5GB-vGXOMx9wl530GJ7WosDRusH03dJ4STMhCJnQ3YyKTgz7R9sSUOX1h6Mb78HAsVxPXfCReRH/pubhtml?gid=1752925598&single=true");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Eventiles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> map = (Map<String, String>) dataSnapshot.getValue();
                String url = map.get("Interncamp");
                webview.loadUrl(url);
                //Toast.makeText(getBaseContext(),""+url,Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }
    }
}