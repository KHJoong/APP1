package com.together.linkalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by kimhj on 2017-08-27.
 */

public class DialogInChattingActivity extends Activity {

    ListView lvChatRoomInfo;
    DialogInChatting_Adapter dcAdapter;

    String myFriend_tmp;
    String nickname;
    String language;
    String lasttime;
    String location;
    String introduce;
    String hobby1;
    String hobby2;
    String hobby3;
    String hobby4;
    String hobby5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.in_chat_room_info);

        lvChatRoomInfo = (ListView)findViewById(R.id.lvChatRoomInfo);
        dcAdapter = new DialogInChatting_Adapter(getApplicationContext());
        lvChatRoomInfo.setAdapter(dcAdapter);

        Intent intent = getIntent();
        String rel = intent.getStringExtra("relation");

        String[] rel_array = rel.split("/");
        for(int i=0; ; i++){
            SharedPreferences myFriendShared = getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
            if(myFriendShared.contains(String.valueOf(i))){
                myFriend_tmp = myFriendShared.getString(String.valueOf(i), "");
                try {
                    JSONObject friendObject = new JSONObject(myFriend_tmp);
                    nickname = friendObject.getString("nickname");
                    if(Arrays.asList(rel_array).contains(nickname)){
                        location = friendObject.getString("location");
                        language = friendObject.getString("language");
                        lasttime = friendObject.getString("lasttime");
                        introduce = friendObject.getString("introduce");
                        hobby1 = friendObject.getString("hobby1");
                        hobby2 = friendObject.getString("hobby2");
                        hobby3 = friendObject.getString("hobby3");
                        hobby4 = friendObject.getString("hobby4");
                        hobby5 = friendObject.getString("hobby5");

                        Member member = new Member(nickname, location, language, lasttime, introduce, hobby1, hobby2, hobby3, hobby4, hobby5);
                        dcAdapter.addItem(member);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            } else {
                break;
            }
        }
        // 어댑터 새로고침
        lvChatRoomInfo.setAdapter(dcAdapter);
    }
}
