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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kimhj on 2017-07-23.
 */

public class NewFriendDetailProfile extends AppCompatActivity {

    // Profile Detail activity 레이아웃
    TextView nickname;
    TextView location;
    TextView language;
    TextView lasttime;
    TextView introduce;
    TextView ho1;
    TextView ho2;
    TextView ho3;
    TextView ho4;
    TextView ho5;
    TextView hobby1;
    TextView hobby2;
    TextView hobby3;
    TextView hobby4;
    TextView hobby5;
    Button btn_plus_myfriend;

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
        setContentView(R.layout.newfriend_detail_profile);

        // 추천받은 친구 목록 저장해둔 쉐어드
        SharedPreferences newMemberShared = getSharedPreferences("newMember", MODE_PRIVATE);

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

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        Log.i("DetailProfile_position", String.valueOf(position));
        try {
            JSONObject object = new JSONObject(newMemberShared.getString(String.valueOf(position), ""));
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

        btn_plus_myfriend = (Button)findViewById(R.id.btn_newfriend_plus);
        btn_plus_myfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFriendPlus mfp = new MyFriendPlus();
                mfp.execute();
            }
        });
    }   // onCreate 끝

    // 내 친구로 추가하는 Asynctask
    class MyFriendPlus extends AsyncTask<Void, Void, String> {

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

                URL url = new URL("http://www.o-ddang.com/linkalk/myFriendPlus.php");
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
            int count = 0;  // plus
            try {
                JSONObject resultObject = new JSONObject(s);
                result = resultObject.getString("plus");
                count = resultObject.getInt("count");   // plus
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(result.equals("false")){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewFriendDetailProfile.this);
                alertDialogBuilder.setMessage("친구 등록에 실패하였습니다. 잠시 후 다시 시도해주세요.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            } else if(result.equals("success")){
                JSONObject friendObject = new JSONObject();
                SharedPreferences newMemberShared = getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
                SharedPreferences.Editor newMemberEditor = newMemberShared.edit();
                try {
                    friendObject.put("nickname", mem_nickname);
                    friendObject.put("location", mem_location);
                    friendObject.put("language", mem_language);
                    friendObject.put("lasttime", mem_lasttime);
                    friendObject.put("introduce", mem_introduce);
                    friendObject.put("hobby1", mem_hobby1);
                    friendObject.put("hobby2", mem_hobby2);
                    friendObject.put("hobby3", mem_hobby3);
                    friendObject.put("hobby4", mem_hobby4);
                    friendObject.put("hobby5", mem_hobby5);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String friendPlus = friendObject.toString();
//                int change_position;
//                for(int i=0; ; i++){
//                    if(!newMemberShared.contains(String.valueOf(i))){
//                        change_position = i;
//                        break;
//                    }
//                }
//                for(int i=change_position; i>=0; i-- ){
//                    if(i>0){
//                        newMemberEditor.putString(String.valueOf(i), newMemberShared.getString(String.valueOf(i-1), ""));
//                    } else if(i==0){
//                        newMemberEditor.putString(String.valueOf(i), friendPlus);
//                    }
//                    newMemberEditor.commit();
//                    Log.i("FrProfilePlusIntoShared", friendPlus);
//                }
                int change_position = 0;
                for(int i=0; ; i++){
                    if(!newMemberShared.contains(String.valueOf(i))){
                        change_position = i;
                        break;
                    }
                }
                for(int i=change_position; i>count;i--){
                    if(i==0){
                        newMemberEditor.putString(String.valueOf(i), friendPlus);
                        break;
                    }
                    if(count == (i-1)){
                        String movingValue = newMemberShared.getString(String.valueOf(count), "");
                        newMemberEditor.putString(String.valueOf(count+1), movingValue);
                        newMemberEditor.putString(String.valueOf(count), friendPlus);
                        break;
                    } else {
                        newMemberEditor.putString(String.valueOf(i), newMemberShared.getString(String.valueOf(i-1), ""));
                    }
                }
                if(change_position == count){
                    newMemberEditor.putString(String.valueOf(change_position), friendPlus);
                }
                newMemberEditor.commit();
                Toast.makeText(getApplicationContext(), "친구가 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else if(result.equals("already")){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewFriendDetailProfile.this);
                alertDialogBuilder.setMessage("이미 등록된 친구입니다.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        }).create().show();
            }
        }
    }   // 친구 추가 asynctask 끝
}
