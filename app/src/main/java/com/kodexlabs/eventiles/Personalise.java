package com.kodexlabs.eventiles;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 1505560 on 09-Sep-17.
 */

public class Personalise extends Fragment {

    private TextView filter_eventtype,filter_state, filter_tags, filter_city;
    private Button apply;
    private FlexboxLayout flexboxLayout;

    private List<String> filter_item;

    private boolean[] itemsChecked;/* = {false, false, false, false, false, false};*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personalise, container, false);

        filter_item = new ArrayList<>();
        DB_Filter db = new DB_Filter(getContext());
        Cursor c;
        db.open();
        c = db.getAllFilter();
        if (c.moveToFirst()){
            do {
                DisplayFilters(c);
            } while (c.moveToNext());
        }
        db.close();

        flexboxLayout = (FlexboxLayout)view.findViewById(R.id.flexboxlayout);
        filter_eventtype = (TextView) view.findViewById(R.id.filter_eventtype);
        filter_state = (TextView)view.findViewById(R.id.filter_states);
        filter_tags = (TextView)view.findViewById(R.id.filter_tags);
        apply = (Button) view.findViewById(R.id.apply);

        Display_Keywords();

        filter_eventtype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsChecked = new boolean[getResources().getStringArray(R.array.event_type).length];
                CharSequence choices[] = getResources().getStringArray(R.array.event_type);
                Select_Option("Select Event Types", choices, "type");
            }
        });

        filter_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsChecked = new boolean[getResources().getStringArray(R.array.indian_states).length];
                CharSequence choices[] = getResources().getStringArray(R.array.indian_states);
                Select_Option("Select States", choices, "State");
            }
        });

        filter_tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog_Input();
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = null;
                Class fragmentClass = Recycler.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            }
        });

        return view;
    }

    private void Display_Keywords() {
        flexboxLayout.removeAllViews();
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 8, 8, 8);
        for (int i=0; i<filter_item.size(); i++){

            final TextView textView = new TextView(getContext());
            textView.setLayoutParams(layoutParams);
            textView.setText(filter_item.get(i));
            textView.setTextColor(Color.WHITE);
            textView.setPadding(20,12,20,12);
            textView.setBackgroundResource(R.drawable.box_gray_item);
            flexboxLayout.addView(textView);
            //Toast.makeText(getApplication(), ""+filter_item.get(i), Toast.LENGTH_SHORT).show();

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DB_Filter db = new DB_Filter(getContext());
                    db.open();
                    Cursor c = db.searchFilter("'"+textView.getText()+"'");
                    db.deleteFilter(Integer.parseInt(c.getString(0)));
                    filter_item.remove(textView.getText());
                    db.close();
                    Display_Keywords();
                }
            });
        }
    }

    private void Select_Option(final String header, final CharSequence[] choices, final String filter_type) {
        final DB_Filter db = new DB_Filter(getContext());

        for(int i = 0; i < itemsChecked.length; i++){
            itemsChecked[i] = false;
            if(filter_item.contains(choices[i]))
                itemsChecked[i] = true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(header);
        builder.setMultiChoiceItems(choices, itemsChecked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked){
                    db.open();
                    db.insertFilter(filter_type, choices[which].toString());
                    db.close();
                    filter_item.add(choices[which].toString());
                    //Toast.makeText(getBaseContext(), choices[which], Toast.LENGTH_SHORT).show();
                }else {
                    db.open();
                    Cursor c = db.searchFilter("'"+choices[which]+"'");
                    db.deleteFilter(Integer.parseInt(c.getString(0)));
                    itemsChecked[which] = false;
                    filter_item.remove(choices[which]);
                    db.close();
                }
            }
        });
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Display_Keywords();
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void Dialog_Input() {
        final DB_Filter db = new DB_Filter(getContext());

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_input, null);
        dialogBuilder.setView(dialogView);
        final EditText input = (EditText) dialogView.findViewById(R.id.input);
        dialogBuilder.setTitle("Enter a Tag / Keyword");
        dialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with input.getText().toString();
                String str = input.getText().toString();
                db.open();
                db.insertFilter("Tags", str);
                db.close();
                filter_item.add(str);
                Display_Keywords();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.show();
    }

    private void DisplayFilters(Cursor c) {
        filter_item.add(c.getString(2));
        //Toast.makeText(this, filters.toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "id: " + c.getString(0) + " Type: " + c.getString(1) + " Item: " + c.getString(2), Toast.LENGTH_SHORT).show();
    }
}
