package com.together.linkalk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kimhj on 2017-08-22.
 */

public class ChoiceChatMember extends AppCompatActivity {

    TextView test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_mem_choice);

        test = (TextView)findViewById(R.id.test);

        JSONObject object = new JSONObject();

        ArrayList<String> list = new ArrayList<String>();
        list.add("momo");
        list.add("mo2mo2");
        list.add("3mo3mo");
        try {
            object.put("sender", "tester");
            object.put("receiver", new JSONArray(list));
            object.put("msg", "This is test");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        test.setText(object.toString());

    }
}
