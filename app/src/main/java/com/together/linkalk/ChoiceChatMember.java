package com.together.linkalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by kimhj on 2017-08-22.
 */

public class ChoiceChatMember extends AppCompatActivity {

    ListView lvChoice;
    ChatStart_Adapter csAdapter;

    Button start_chat_btn;

    ArrayList<String> list;

    String mf_tmp;
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
        setContentView(R.layout.chat_mem_choice);

        lvChoice = (ListView)findViewById(R.id.start_chat_list);
        start_chat_btn = (Button)findViewById(R.id.start_chat_btn);

        csAdapter = new ChatStart_Adapter(getApplicationContext());
        lvChoice.setAdapter(csAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 친구 목록 출력
                for(int i=0; ; i++){
                    SharedPreferences myFriendShared = getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
                    if(myFriendShared.contains(String.valueOf(i))){
                        mf_tmp = myFriendShared.getString(String.valueOf(i), "");
                        try {
                            JSONObject friendObject = new JSONObject(mf_tmp);
                            nickname = friendObject.getString("nickname");
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
                            csAdapter.addItem(member);
                            Log.i("myfriendinfo", String.valueOf(csAdapter.csItem.get(i).getNickname()));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        break;
                    }

                }
                for(int i=0; i<csAdapter.csItem.size(); i++){
                    csAdapter.csItem.get(i).setChecked(false);
                    Log.i("myfriendChecked", csAdapter.csItem.get(i).getNickname() + " : " +String.valueOf(csAdapter.csItem.get(i).getChecked()));
                }
                // 어댑터 새로고침
                lvChoice.setAdapter(csAdapter);
            }
        }).start();

        start_chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("maintain", MODE_PRIVATE);
                String my_nickname = sp.getString("nickname", "");
                list = new ArrayList<String>();
                for(int i=0; i<csAdapter.csItem.size(); i++){
                    if(csAdapter.csItem.get(i).getChecked()){
                        list.add(csAdapter.csItem.get(i).getNickname());
                    }
                }
                list.add(my_nickname);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("sender", my_nickname);
                    jsonObject.put("receiver", new JSONArray(list));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 채팅방 DB 생성하기
                PlusChatRoom pcr = new PlusChatRoom();
                pcr.execute(jsonObject.toString());
            }
        });


    }   // onCreate 끝

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("page", 2);
        startActivity(intent);
        finish();
    }

    // 채팅방 생성 Asynctask
    class PlusChatRoom extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try{
                String send = params[0];

                // 서버와 연결하기 위해 세션 아이디 불러와서 커넥트
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String sessionID = sharedPreferences.getString("sessionID", "");

                Log.i("sessionID",sessionID);

                URL url = new URL("http://www.o-ddang.com/linkalk/checkChatRoom.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setDefaultUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setInstanceFollowRedirects( false );
                if(!TextUtils.isEmpty(sessionID)) {
                    httpURLConnection.setRequestProperty( "cookie", sessionID) ;
                }

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(send.getBytes());
                os.flush();

                // 서버에서 채팅룸 번호랑 대화 상대 전달
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("responseStatusCode", "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString().trim();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (ProtocolException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("MyFriendProfile", String.valueOf(s));
            int roomNo = 0;
            String relation = null;
            try {
                JSONObject object = new JSONObject(s);
                roomNo = object.getInt("roomNo");
                relation = object.getString("relation");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // SQLite에 추가할 부분 chat_room table;
            MsgDBHelper mdbHelper = new MsgDBHelper(getApplicationContext());
            mdbHelper.insertRoom(roomNo, relation);

            String[] rel = relation.split("/");
            ArrayList<String> listed = new ArrayList<String>();
            for(int i=0; i<rel.length; i++){
                listed.add(rel[i]);
            }

            // 기존에 메시지 주고 받던 채팅방인지 아닌지 확인
            MsgDBHelper msgDBHelper = new MsgDBHelper(getApplicationContext());
            SQLiteDatabase db = msgDBHelper.getReadableDatabase();
            String qu = "SELECT msgNo FROM chat_msg WHERE roomNo='"+roomNo+"'";
            Cursor cursor = db.rawQuery(qu, null);
            int msgNo = cursor.getCount();

            // 채팅방 띄우기
            Intent intent = new Intent(getApplicationContext(), InChattingActivity.class);
            intent.putStringArrayListExtra("Receiver", listed);
            if(msgNo == 0){
                intent.putExtra("comment", 1);
            } else {
                intent.putExtra("comment", 0);
            }
            startActivity(intent);
            finish();
        }
    }   // 채팅방 생성 Asynctask

}
