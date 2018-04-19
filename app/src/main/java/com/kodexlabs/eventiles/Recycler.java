package com.kodexlabs.eventiles;

import android.app.MediaRouteButton;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by 1505560 on 20-May-17.
 */

public class Recycler extends Fragment {

    private RecyclerView recyclerView;

    private static DatabaseReference databaseReference;
    private static StorageReference storageReference;

    private ProgressDialog progressDialog;

    private List<String> card_id, card_title, filters;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.content_main, container, false);

        filters = new ArrayList<>();
        DB_Filter db = new DB_Filter(getContext());
        Cursor c;
        db.open();
        c = db.getAllFilter();
        if (c.moveToFirst()){
            do {
                DisplayBookmarks(c);
            } while (c.moveToNext());
        }
        db.close();

        databaseReference = FirebaseDatabase.getInstance().getReference("Eventiles/Events");
        databaseReference.keepSynced(true);

        recyclerView = (RecyclerView)view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        card_id = new ArrayList<>();
        card_title = new ArrayList<>();
        Query query = databaseReference.orderByChild("date");
        final FirebaseRecyclerAdapter<Event, Card_holder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, Card_holder>(
                Event.class, R.layout.cardview_ui, Card_holder.class, query
        ) {
            @Override
            protected void populateViewHolder(Card_holder viewHolder, Event model, final int position) {
                String isDeleted = viewHolder.Event_Populate(model);
                if (isDeleted.compareTo("deleted")!=0){
                    card_id.add(model.getId());
                    card_title.add(model.getTitle());
                }
                progressDialog.dismiss();
                if (model.getStatus().contains("1")&&((filters.isEmpty()||filters.contains(model.getType())||filters.contains(model.getState())||filters.contains(model.getCity())||filters.contains(model.getTitle())))){
                    //Toast.makeText(getBaseContext(), card_id.toString(), Toast.LENGTH_SHORT).show();
                    //viewHolder.Event_Populate(model);
                    //nocardhere.setVisibility(View.INVISIBLE);
                    viewHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), ScrollingActivity.class);
                            intent.putExtra("card_id", card_id.get(position));
                            intent.putExtra("card_title", card_title.get(position));
                            startActivity(intent);
                        }
                    });
                }else {
                    //Recycler.Card_holder.cardview.setVisibility(View.INVISIBLE);
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) viewHolder.view.getLayoutParams();
                    params.height = 0;
                    params.width = 0;
                    viewHolder.view.setLayoutParams(params);
                }
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        return view;
    }

    public static class Card_holder extends RecyclerView.ViewHolder {

        View view;

        public Card_holder(final View itemView) {
            super(itemView);
            view = itemView;
        }

        public String Event_Populate(final Event model) {
            String checkDate = checkDate(model.getDate(), model.getId(), model.getPoster());
            if (checkDate.compareTo("deleted")==0)
                return "deleted";

            /*if (model.getType().compareTo("College Fest")==0)
                Layout(model, checkDate);
            else if (model.getType().compareTo("Hackathon")==0)
                Layout_2(model, checkDate);
            else if ((model.getType().compareTo("Seminar")==0)||(model.getType().compareTo("Conference")==0))
                Layout_3(model, checkDate);
            else*/
                Layout(model,checkDate);

            return checkDate;
        }

        private void Layout(final Event model, String checkDate) {
            RelativeLayout card = (RelativeLayout) view.findViewById(R.id.layout);
            final TextView title = (TextView)view.findViewById(R.id.title);
            TextView date = (TextView)view.findViewById(R.id.date);
            final ImageView poster = (ImageView)view.findViewById(R.id.poster);

            card.setVisibility(View.VISIBLE);
            title.setText(model.getTitle());
            date.setText(checkDate);

            Picasso.with(view.getContext()).load(model.getPoster()).centerCrop().fit().transform(new GradientTransformation()).networkPolicy(NetworkPolicy.OFFLINE).into(poster, new Callback() {
                @Override
                public void onSuccess() {
                    title.setTextColor(Color.WHITE);
                }
                @Override
                public void onError() {
                    title.setTextColor(Color.WHITE);
                    Picasso.with(view.getContext()).load(model.getPoster()).centerCrop().fit().transform(new GradientTransformation()).into(poster);
                }
            });
            //Picasso.with(view.getContext()).load(model.getPoster()).centerCrop().fit().transform(new GradientTransformation()).into(poster);
        }

        private void Layout_2(Event model, String checkDate) {
            RelativeLayout card = (RelativeLayout)view.findViewById(R.id.layout2);
            final TextView title = (TextView)view.findViewById(R.id.title2);
            TextView date = (TextView)view.findViewById(R.id.date2);
            final ImageView poster = (ImageView)view.findViewById(R.id.poster2);

            card.setVisibility(View.VISIBLE);
            title.setText(model.getTitle());
            date.setText(checkDate);

            Picasso.with(view.getContext()).load(model.getPoster()).centerCrop().fit().transform(new GradientTransformation()).into(poster);
        }

        private void Layout_3(Event model, String checkDate) {
            RelativeLayout card = (RelativeLayout)view.findViewById(R.id.layout3);
            final TextView title = (TextView)view.findViewById(R.id.title3);
            TextView date = (TextView)view.findViewById(R.id.date3);

            card.setVisibility(View.VISIBLE);
            title.setText(model.getTitle());
            date.setText(checkDate);
        }

        private String checkDate(String date, String id, String poster) {
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat outputFormat = new SimpleDateFormat("dd MMM\nEEE");
            DateFormat strEventDate = new SimpleDateFormat("yyyyMMdd");
            String inputDateStr = date;
            Date dateparse = null;
            try {
                dateparse = inputFormat.parse(inputDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String outputDateStr = outputFormat.format(dateparse);

            Calendar c = Calendar.getInstance();
            String currentDate = null;
            if (((c.get(Calendar.MONTH)+1)<10) && (c.get(Calendar.DAY_OF_MONTH)<10)){
                currentDate = c.get(Calendar.YEAR)+"0"+(c.get(Calendar.MONTH)+1)+"0"+c.get(Calendar.DAY_OF_MONTH);
            } else if ((c.get(Calendar.MONTH)+1)<10){
                currentDate = c.get(Calendar.YEAR)+"0"+(c.get(Calendar.MONTH)+1)+""+c.get(Calendar.DAY_OF_MONTH);
            } else if (c.get(Calendar.DAY_OF_MONTH)<10){
                currentDate = c.get(Calendar.YEAR)+""+(c.get(Calendar.MONTH)+1)+"0"+c.get(Calendar.DAY_OF_MONTH);
            } else {
                currentDate = c.get(Calendar.YEAR)+""+(c.get(Calendar.MONTH)+1)+""+c.get(Calendar.DAY_OF_MONTH);
            }
            int intCurrentDate = Integer.parseInt(currentDate);
            int intEventDate = Integer.parseInt(strEventDate.format(dateparse));
            if ((intCurrentDate - intEventDate) > 0){
                databaseReference.child(id).removeValue();
                //Todo delete images
                return "deleted";
            }

            return outputDateStr;
        }
    }

    private void DisplayBookmarks(Cursor c) {
        filters.add(c.getString(2));
        //Toast.makeText(this, bookmarks.toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "id: " + c.getString(0) +"\n" + "Name: " + c.getString(1), Toast.LENGTH_SHORT).show();
    }
}