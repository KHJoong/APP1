package com.together.linkalk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by kimhj on 2017-08-22.
 */

public class ChoiceChatMember extends AppCompatActivity {

    TextView test;
    TextView test2;
    TextView test3;
    TextView test4;

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_mem_choice);

        test = (TextView)findViewById(R.id.test);
        test2 = (TextView)findViewById(R.id.test2);
        test3 = (TextView)findViewById(R.id.test3);
        test4 = (TextView)findViewById(R.id.test4);
        btn = (Button)findViewById(R.id.btn);

        final JSONObject object = new JSONObject();

        ArrayList<String> list = new ArrayList<String>();
        list.add("apple");
        try {
            object.put("sender", "tester");
            object.put("receiver", new JSONArray(list));
            object.put("msg", "This is test");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String jsonOb = object.toString();
        test.setText(jsonOb);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject object1 = new JSONObject(jsonOb);
                    JSONArray array = object1.getJSONArray("receiver");

                    ArrayList<String> al = new ArrayList<String>();
                    for(int i=0; i<array.length(); i++){
                        al.add(array.getString(i));
                    }
                    Collections.sort(al, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o1.compareToIgnoreCase(o2);
                        }
                    });

                    for(int i=0; i<array.length(); i++){
                        Toast.makeText(getApplicationContext(), al.get(i), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });



    }
}
