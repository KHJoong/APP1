package com.together.linkalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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

    Button lvChatRoomInfoBtn;
    String rel;

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
    String imgpath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.in_chat_room_info);

        lvChatRoomInfo = (ListView)findViewById(R.id.lvChatRoomInfo);
        dcAdapter = new DialogInChatting_Adapter(getApplicationContext());
        lvChatRoomInfo.setAdapter(dcAdapter);

        lvChatRoomInfoBtn = (Button)findViewById(R.id.lvChatRoomInfoBtn);

        Intent intent = getIntent();
        rel = intent.getStringExtra("relation");

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
                        imgpath = friendObject.getString("imgpath");

                        Member member = new Member(nickname, location, language, lasttime, introduce, hobby1, hobby2, hobby3, hobby4, hobby5, imgpath);
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

        lvChatRoomInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder chatRoomExitDialogBuilder = new AlertDialog.Builder(DialogInChattingActivity.this);
                chatRoomExitDialogBuilder.setTitle("채팅방 삭제");
                chatRoomExitDialogBuilder
                        .setMessage("이 채팅방을 정말로 나가시겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MsgDBHelper dbHelper = new MsgDBHelper(getApplicationContext());
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                // 방 번호 찾는 쿼리
                                String query = "SELECT roomNo FROM chat_room WHERE relation='"+rel+"'";
                                Cursor c = db.rawQuery(query, null);
                                int rn = 0;
                                if(c.moveToFirst()){
                                    rn =  c.getInt(c.getColumnIndex("roomNo"));
                                }

                                String deleteMsgQuery = "DELETE FROM chat_msg WHERE roomNo='"+rn+"';";
                                db.execSQL(deleteMsgQuery);

                                String deleteRoomQuery = "DELETE FROM chat_room WHERE roomNo='"+rn+"';";
                                db.execSQL(deleteRoomQuery);

                                c.close();
                                db.close();
                                dbHelper.close();

                                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                intent1.putExtra("page", 2);
                                startActivity(intent1);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog chatRoomExitDialog = chatRoomExitDialogBuilder.create();
                chatRoomExitDialog.show();
            }
        });
    }   // onCreate 끝

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
