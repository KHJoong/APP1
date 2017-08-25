package com.together.linkalk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.ArrayList;

/**
 * Created by kimhj on 2017-07-27.
 */

public class MyFriendDetailProfile extends AppCompatActivity {

    // 아이템 클릭한 포지션 번호(SharedPreferences에 저장된 key 값과 같음)
    int position;

    SharedPreferences myFriendShared;
    SharedPreferences.Editor myFriendEditor;

    TextView nickname;
    TextView location;
    TextView language;
    TextView lasttime;
    TextView introduce;
    TextView hobby1;
    TextView hobby2;
    TextView hobby3;
    TextView hobby4;
    TextView hobby5;
    TextView ho1;
    TextView ho2;
    TextView ho3;
    TextView ho4;
    TextView ho5;
    Button btn_myfriend_chat;
    Button btn_nmyfriend_del;

    String mem_nickname;
    String mem_language;
    String mem_lasttime;
    String mem_location;
    String mem_introduce;
    String mem_hobby1;
    String mem_hobby2;
    String mem_hobby3;
    String mem_hobby4;
    String mem_hobby5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myfriend_detail_profile);

        myFriendShared = getSharedPreferences("MyFriend", MODE_PRIVATE);
        myFriendEditor = myFriendShared.edit();

        nickname = (TextView)findViewById(R.id.detail_profile_nickname);
        location = (TextView)findViewById(R.id.detail_profile_location);
        language = (TextView)findViewById(R.id.detail_profile_language);
        lasttime = (TextView)findViewById(R.id.detail_profile_lasttime);
        introduce = (TextView)findViewById(R.id.detail_profile_introduce);
        hobby1 = (TextView)findViewById(R.id.detail_profile_hobby1);
        hobby2 = (TextView)findViewById(R.id.detail_profile_hobby2);
        hobby3 = (TextView)findViewById(R.id.detail_profile_hobby3);
        hobby4 = (TextView)findViewById(R.id.detail_profile_hobby4);
        hobby5 = (TextView)findViewById(R.id.detail_profile_hobby5);
        ho1 = (TextView)findViewById(R.id.ho1);
        ho2 = (TextView)findViewById(R.id.ho2);
        ho3 = (TextView)findViewById(R.id.ho3);
        ho4 = (TextView)findViewById(R.id.ho4);
        ho5 = (TextView)findViewById(R.id.ho5);
        btn_myfriend_chat = (Button)findViewById(R.id.btn_myfriend_chat);
        btn_nmyfriend_del = (Button)findViewById(R.id.btn_nmyfriend_del);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        Log.i("MyFriendDetail_position", String.valueOf(position));

        try {
            JSONObject object = new JSONObject(myFriendShared.getString(String.valueOf(position), ""));
            Log.i("FriendDetailJson",  String.valueOf(object));
            mem_nickname = object.getString("nickname");
            mem_language = object.getString("language");
            mem_lasttime = object.getString("lasttime");
            mem_location = object.getString("location");
            mem_introduce = object.getString("introduce");
            mem_hobby1 = object.getString("hobby1");
            mem_hobby2 = object.getString("hobby2");
            mem_hobby3 = object.getString("hobby3");
            mem_hobby4 = object.getString("hobby4");
            mem_hobby5 = object.getString("hobby5");
            if(!TextUtils.isEmpty(mem_nickname)){
                nickname.setText(mem_nickname);
            }
            if(!TextUtils.isEmpty(mem_location)){
                location.setText(mem_location);
            } else {
                location.setText("아직 알려주지 않은 정보입니다.");
            }
            if(!TextUtils.isEmpty(mem_language)){
                language.setText(mem_language);
            }
            if(!TextUtils.isEmpty(mem_lasttime)){
                lasttime.setText(mem_lasttime);
            }
            if(!TextUtils.isEmpty(mem_introduce)){
                introduce.setText(mem_introduce);
            } else {
                introduce.setText("아직 알려주지 않은 정보입니다.");
            }
            if(!TextUtils.isEmpty(mem_hobby1)){
                hobby1.setText(mem_hobby1);
            } else {
                hobby1.setVisibility(View.GONE);
                ho1.setVisibility(View.GONE);
            }
            if(!TextUtils.isEmpty(mem_hobby2)){
                hobby2.setText(mem_hobby2);
            } else {
                hobby2.setVisibility(View.GONE);
                ho2.setVisibility(View.GONE);
            }
            if(!TextUtils.isEmpty(mem_hobby3)){
                hobby3.setText(mem_hobby3);
            } else {
                hobby3.setVisibility(View.GONE);
                ho3.setVisibility(View.GONE);
            }
            if(!TextUtils.isEmpty(mem_hobby4)){
                hobby4.setText(mem_hobby4);
            } else {
                hobby4.setVisibility(View.GONE);
                ho4.setVisibility(View.GONE);
            }
            if(!TextUtils.isEmpty(mem_hobby5)){
                hobby5.setText(mem_hobby5);
            } else {
                hobby5.setVisibility(View.GONE);
                ho5.setVisibility(View.GONE);
            }
            if(TextUtils.isEmpty(mem_hobby1) && TextUtils.isEmpty(mem_hobby2) && TextUtils.isEmpty(mem_hobby3) && TextUtils.isEmpty(mem_hobby4) && TextUtils.isEmpty(mem_hobby5) ){
                hobby1.setVisibility(View.VISIBLE);
                hobby1.setText("아직 알려주지 않은 정보입니다.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("newFriendDetailJson", e.getMessage());
        }

        // 대화하기 버튼 클릭 메소드
        btn_myfriend_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("maintain", MODE_PRIVATE);
                String my_nickname = sp.getString("nickname", "");
                ArrayList<String> list = new ArrayList<String>();
                list.add(mem_nickname);
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

                // 채팅방 띄우기
                Intent intent = new Intent(getApplicationContext(), InChattingActivity.class);
                intent.putStringArrayListExtra("Receiver", list);
                startActivity(intent);
                finish();
            }
        });

        // 친구 삭제 버튼 클릭 메소드
        btn_nmyfriend_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyFriendDetailProfile.this);
                alertDialogBuilder.setMessage("정말로 등록된 친구를 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                MyFriendDel mfd = new MyFriendDel();
                                mfd.execute();
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            }
        });

    }   // onCreate 끝

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goMainActivity();
    }

    public void goMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("page", 0);
        startActivity(intent);
        finish();
    }

    // 친구 삭제 Asynctask
    class MyFriendDel extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try{
                // 서버에서 추천 친구 목록을 받기 위해 요청하는 부분
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String sessionID = sharedPreferences.getString("sessionID", "");

                Log.i("sessionID",sessionID);
                JSONObject object = new JSONObject();
                object.put("friendNickname", mem_nickname);
                String send = object.toString();

                URL url = new URL("http://www.o-ddang.com/linkalk/myFriendDel.php");
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

                // 서버에서 추천 친구 목록 데이터를 받아오는 부분
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
            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("MyFriendProfile", s);
            String result = "";
            try {
                JSONObject resultObject = new JSONObject(s);
                result = resultObject.getString("del");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(result.equals("false")){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyFriendDetailProfile.this);
                alertDialogBuilder.setMessage("친구 삭제에 실패하였습니다. 잠시 후 다시 시도해주세요.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                goMainActivity();
                            }
                        }).create().show();
            } else if(result.equals("success")){
                myFriendEditor.remove(String.valueOf(position));
                myFriendEditor.commit();
                for(int i=position+1;;i++){
                    if(myFriendShared.contains(String.valueOf(i))){
                        myFriendEditor.putString(String.valueOf(i-1), myFriendShared.getString(String.valueOf(i), ""));
                    } else {
                        Log.i("myFriendIthPosition", myFriendShared.getString(String.valueOf(i-1), ""));
                        myFriendEditor.remove(String.valueOf(i-1));
                        Log.i("myFriendIthPosition", myFriendShared.getString(String.valueOf(i-1), ""));
                        break;
                    }
                }
                myFriendEditor.commit();
                goMainActivity();
            }
        }
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

        }
    }   // 채팅방 생성 Asynctask
}
