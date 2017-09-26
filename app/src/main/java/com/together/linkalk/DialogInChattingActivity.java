package com.together.linkalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kimhj on 2017-08-27.
 */

public class DialogInChattingActivity extends Activity {

    ListView lvChatRoomInfo;
    DialogInChatting_Adapter dcAdapter;
    ProgressBar pb_memberLoading;

    Button lvChatRoomInfoBtn;
    String rel;

    String my_nickname;
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

    ArrayList<String> sendArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.in_chat_room_info);

        pb_memberLoading = (ProgressBar)findViewById(R.id.pb_member_loading);
        pb_memberLoading.setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
        my_nickname = sharedPreferences.getString("nickname", "");

        lvChatRoomInfo = (ListView)findViewById(R.id.lvChatRoomInfo);
        dcAdapter = new DialogInChatting_Adapter(getApplicationContext());
        lvChatRoomInfo.setAdapter(dcAdapter);
        lvChatRoomInfo.setVisibility(View.INVISIBLE);

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

        // 친구 아닌 사람 누군지 정리하는 부분
        sendArray = new ArrayList<String>();
        if((rel_array.length-1) != dcAdapter.dcItem.size()){
            int k=0;
            for(int i=0; i<rel_array.length; i++){
                for(int j=0; j<dcAdapter.dcItem.size(); j++){
                    if(!rel_array[i].equals(dcAdapter.dcItem.get(j).getNickname())){
                        k++;
                    }
                }
                if((k==dcAdapter.dcItem.size()) && !my_nickname.equals(rel_array[i])){
                    sendArray.add(rel_array[i]);
                }
                k = 0;
            }
            // 정리한 내용을 보내서 친구 아닌 사람의 정보를 받아오는 부분
            getNotFriendProfile gnfp = new getNotFriendProfile(sendArray, dcAdapter, lvChatRoomInfo);
            gnfp.execute();
        } else {
            // 친구 아닌사람 없으면 걍 고고
            lvChatRoomInfo.setVisibility(View.VISIBLE);
            pb_memberLoading.setVisibility(View.GONE);
            lvChatRoomInfo.setAdapter(dcAdapter);
        }

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

        lvChatRoomInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intt;
                Member member = (Member)parent.getItemAtPosition(position);
                if(sendArray.contains(member.getNickname())){
                    intt = new Intent(getApplicationContext(), NewFriendDetailProfile.class);
                } else {
                    intt = new Intent(getApplicationContext(), MyFriendDetailProfile.class);
                }
                intt.putExtra("from", 2);
                intt.putExtra("nickname", member.getNickname());
                intt.putExtra("location", member.getLocation());
                intt.putExtra("language", member.getLanguage());
                intt.putExtra("lasttime", member.getLastTime());
                intt.putExtra("introduce", member.getIntroduce());
                intt.putExtra("hobby1", member.getHobby1());
                intt.putExtra("hobby2", member.getHobby2());
                intt.putExtra("hobby3", member.getHobby3());
                intt.putExtra("hobby4", member.getHobby4());
                intt.putExtra("hobby5", member.getHobby5());
                intt.putExtra("imgpath", member.getImgpath());
                startActivity(intt);
            }
        });
    }   // onCreate 끝

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    class getNotFriendProfile extends AsyncTask<Void, Void, String>{

        DialogInChatting_Adapter adapter;
        ListView listView;

        ArrayList<String> sendArray;
        String sendData;

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

        getNotFriendProfile(ArrayList<String> al, DialogInChatting_Adapter ad, ListView lv){
            sendArray = al;
            sendData = "";

            adapter = ad;
            listView = lv;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Collections.sort(sendArray);
            for(int i=0; i<sendArray.size(); i++){
                if(TextUtils.isEmpty(sendData)){
                    sendData = sendArray.get(i);
                } else {
                    sendData = sendData + "/" + sendArray.get(i);
                }
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                // 서버에서 추천 친구 목록을 받기 위해 요청하는 부분
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String sessionID = sharedPreferences.getString("sessionID", "");

                URL url = new URL("http://www.o-ddang.com/linkalk/getNotFriendProfile.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setDefaultUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(2000);
                httpURLConnection.setReadTimeout(2000);

                httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                httpURLConnection.setInstanceFollowRedirects( false );
                if(!TextUtils.isEmpty(sessionID)) {
                    httpURLConnection.setRequestProperty( "cookie", sessionID) ;
                }

                StringBuffer buffer = new StringBuffer();
                buffer.append("notFriNick").append("=").append(sendData);

                Log.i("notFriNick : ", sendData);

                OutputStreamWriter outStream = new OutputStreamWriter(httpURLConnection.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();

                InputStreamReader tmp = new InputStreamReader(httpURLConnection.getInputStream(), "EUC-KR");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder builder = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    builder.append(str + "\n");
                }
                String myResult = builder.toString();
                Log.i("process result : ", myResult);
                return myResult;
            } catch (IOException e) {
                e.printStackTrace();
                return "networkError";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("newMemberList", s);
            int n;

            if(!TextUtils.isEmpty(s) && !s.equals("networkError")){
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray array = object.getJSONArray("profile");

                    for(n=0; n<array.length(); n++){
                        String s_obj = array.getString(n);
                        JSONObject obj = new JSONObject(s_obj);

                        if(!TextUtils.isEmpty(obj.getString("nickname"))){
                            nickname = obj.getString("nickname");
                        }
                        if(!TextUtils.isEmpty(language = obj.getString("language"))){
                            language = obj.getString("language");
                        }
                        if(!TextUtils.isEmpty(lasttime = obj.getString("lasttime"))){
                            lasttime = obj.getString("lasttime");
                        }
                        if(!TextUtils.isEmpty(location = obj.getString("location"))){
                            location = obj.getString("location");
                        }
                        if(!TextUtils.isEmpty(introduce = obj.getString("introduce"))){
                            introduce = obj.getString("introduce");
                        }
                        if(!TextUtils.isEmpty(hobby1 = obj.getString("hobby1"))){
                            hobby1 = obj.getString("hobby1");
                        }
                        if(!TextUtils.isEmpty(hobby2 = obj.getString("hobby2"))){
                            hobby2 = obj.getString("hobby2");
                        }
                        if(!TextUtils.isEmpty(hobby3 = obj.getString("hobby3"))){
                            hobby3 = obj.getString("hobby3");
                        }
                        if(!TextUtils.isEmpty(hobby4 = obj.getString("hobby4"))){
                            hobby4 = obj.getString("hobby4");
                        }
                        if(!TextUtils.isEmpty(hobby5 = obj.getString("hobby5"))){
                            hobby5 = obj.getString("hobby5");
                        }
                        if(!TextUtils.isEmpty(imgpath = obj.getString("imgpath"))){
                            imgpath = obj.getString("imgpath");
                        }

                        Member member = new Member(nickname, location, language, lasttime, introduce, hobby1, hobby2, hobby3, hobby4, hobby5, imgpath);
                        adapter.addItem(member);
                    }
                    listView.setVisibility(View.VISIBLE);
                    listView.setAdapter(adapter);
                    pb_memberLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(s.equals("networkError")) {
                Toast.makeText(getApplicationContext(), "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
