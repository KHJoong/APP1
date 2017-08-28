package com.together.linkalk;

import android.app.FragmentTransaction;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

/**
 * Created by kimhj on 2017-07-24.
 */

public class MyProfileModify extends AppCompatActivity {

    TextView nickname;
    EditText location;
    Spinner language;
    EditText introduce;
    EditText hobby1;
    EditText hobby2;
    EditText hobby3;
    EditText hobby4;
    EditText hobby5;
    Button btn_myprofile_modify;

    // 받아온 혹은 저장해둔 나의 프로필을 담아둘 변수
    int lan_select;
    String my_nickname;
    String my_language;
    String my_location;
    String my_introduce;
    String my_hobby1;
    String my_hobby2;
    String my_hobby3;
    String my_hobby4;
    String my_hobby5;

    // 업데이트 된 나의 프로필을 담아둘 변수
    String get_language;
    String get_location;
    String get_introduce;
    String get_hobby1;
    String get_hobby2;
    String get_hobby3;
    String get_hobby4;
    String get_hobby5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile_modify);

        nickname = (TextView)findViewById(R.id.my_profile_nickname);
        location = (EditText)findViewById(R.id.my_profile_location);
        language = (Spinner)findViewById(R.id.my_profile_language);
        introduce = (EditText)findViewById(R.id.my_profile_introduce);
        hobby1 = (EditText)findViewById(R.id.my_profile_hobby1);
        hobby2 = (EditText)findViewById(R.id.my_profile_hobby2);
        hobby3 = (EditText)findViewById(R.id.my_profile_hobby3);
        hobby4 = (EditText)findViewById(R.id.my_profile_hobby4);
        hobby5 = (EditText)findViewById(R.id.my_profile_hobby5);
        btn_myprofile_modify = (Button)findViewById(R.id.btn_myprofile_modify);

        // 네트워크 연결 끊어졌을 경우에도 정보를 불러올 수 있게
        // 저장해둔 SharedPreferences에서 불러온다.
        // (한 번도 저장된 적이 없으면, MainSettingFragment Activity에서 네트워크 확인 알람창을 띄운다.)
        SharedPreferences profileShared = getSharedPreferences("MyProfile", Context.MODE_PRIVATE);
        my_nickname = profileShared.getString("nickname", "");
        my_language = profileShared.getString("language", "");
        my_location = profileShared.getString("location", "");
        my_introduce = profileShared.getString("introduce", "");
        my_hobby1 = profileShared.getString("hobby1", "");
        my_hobby2 = profileShared.getString("hobby2", "");
        my_hobby3 = profileShared.getString("hobby3", "");
        my_hobby4 = profileShared.getString("hobby4", "");
        my_hobby5 = profileShared.getString("hobby5", "");

        if(my_language.equals("Chinese")){
            lan_select = 1;
        } else if(my_language.equals("English")){
            lan_select = 2;
        } else if(my_language.equals("Japanese")){
            lan_select = 3;
        } else if(my_language.equals("Korean")){
            lan_select = 4;
        }

        nickname.setText(my_nickname);
        if(TextUtils.isEmpty(my_location) || my_location.equals("null")){
            location.setHint("현재 지내고 있는 장소를 입력해주세요.");
        } else {
            location.setText(my_location);
        }
        language.setSelection(lan_select);
        if(TextUtils.isEmpty(my_introduce) || my_introduce.equals("null")){
            introduce.setHint("자신을 마음껏 소개해주세요.");
        } else {
            introduce.setText(my_introduce);
        }
        if(TextUtils.isEmpty(my_hobby1) || my_hobby1.equals("null")){
            hobby1.setHint("첫번째 취미를 넣어주세요.");
        } else {
            hobby1.setText(my_hobby1);
        }
        if(TextUtils.isEmpty(my_hobby2) || my_hobby2.equals("null")){
            hobby2.setHint("두번째 취미를 넣어주세요.");
        } else {
            hobby2.setText(my_hobby2);
        }
        if(TextUtils.isEmpty(my_hobby3) || my_hobby3.equals("null")){
            hobby3.setHint("세번째 취미를 넣어주세요.");
        } else {
            hobby3.setText(my_hobby3);
        }
        if(TextUtils.isEmpty(my_hobby4) || my_hobby4.equals("null")){
            hobby4.setHint("네번째 취미를 넣어주세요.");
        } else {
            hobby4.setText(my_hobby4);
        }
        if(TextUtils.isEmpty(my_hobby5) || my_hobby5.equals("null")){
            hobby5.setHint("다섯번째 취미를 넣어주세요.");
        } else {
            hobby5.setText(my_hobby5);
        }

        // language spinner
        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                get_language = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_myprofile_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 핸드폰 네트워크 상태 체크
                String status = NetWorkStatusCheck.getConnectivityStatusString(getApplicationContext());
                if(status.equals("WIFI") || status.equals("MOBILE")){
                    // 와이파이거나 모바일네트워크 연결되어 있으면 프로필 수정 !
                    UpdateMyProfile ump = new UpdateMyProfile();
                    ump.execute();

                    goMainActivity();
                } else if(status.equals("NOT")){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyProfileModify.this);
                    // AlertDialog 셋팅
                    alertDialogBuilder.setMessage("프로필 수정에 실패하였습니다. 네트워크 상태를 확인해주세요.")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    goMainActivity();
                                }
                            }).create().show();
                }
            }
        });

    } // onCreate 끝

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goMainActivity();
    }

    public void goMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("page", 3);
        startActivity(intent);
        finish();
    }

    // 업데이트 된 개인 프로필을 서버에 업데이트 하는 Asynctask
    class UpdateMyProfile extends AsyncTask<Void, Void, String> {

        JSONObject object = new JSONObject();
        String sessionID;
        String sendUpdateDate;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SharedPreferences sharedPreferences = getSharedPreferences("maintain", Context.MODE_PRIVATE);
            sessionID = sharedPreferences.getString("sessionID", "");

            get_location = location.getText().toString();
            get_introduce = introduce.getText().toString();
            get_hobby1 = hobby1.getText().toString();
            get_hobby2 = hobby2.getText().toString();
            get_hobby3 = hobby3.getText().toString();
            get_hobby4 = hobby4.getText().toString();
            get_hobby5 = hobby5.getText().toString();

            try {
                object.put("sessionID", sessionID);
                object.put("nickname", my_nickname);
                object.put("location", get_location);
                object.put("language", get_language);
                object.put("introduce", get_introduce);
                object.put("hobby1", get_hobby1);
                object.put("hobby2", get_hobby2);
                object.put("hobby3", get_hobby3);
                object.put("hobby4", get_hobby4);
                object.put("hobby5", get_hobby5);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendUpdateDate = object.toString();
            Log.i("MyProfilePut", sendUpdateDate);
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                // 서버에 개인 프로필을 업데이트하기 위해 요청하는 부분
                URL url = new URL("http://www.o-ddang.com/linkalk/updateMyProfile.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

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
                os.write(sendUpdateDate.getBytes());
                os.flush();

                // 서버에서 프로필 업데이트 결과를 얻어오는 부분
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
        }   // onDoing 끝

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("myProfileUpdateResult", s);

            // 핸드폰 네트워크 상태 체크
            String status = NetWorkStatusCheck.getConnectivityStatusString(getApplicationContext());

            if(s.equals("updateFalse") || status.equals("NOT")){
                // 프로필 업데이트가 실패하거나, 네트워크 상태가 끊어져있으면 경고메시지 출력
                android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getApplicationContext());
                alertDialogBuilder.setMessage("업데이트할 수 없습니다. 네트워크 연결을 확인해주세요.")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            } else if(s.equals("updateSuccess")){
                // 업데이트 된 프로필을 네트워크 끊어졌을 때를 대비해 SharedPreferences 에 저장
                SharedPreferences newMemberShared = getSharedPreferences("MyProfile", Context.MODE_PRIVATE);
                SharedPreferences.Editor MyProfileEditor = newMemberShared.edit();
                if(!TextUtils.isEmpty(get_language)){
                    MyProfileEditor.putString("language", get_language);
                    SharedPreferences mtShared = getSharedPreferences("maintain", MODE_PRIVATE);
                    SharedPreferences.Editor mtSharedEditor = mtShared.edit();
                    mtSharedEditor.putString("language", get_language);
                    mtSharedEditor.commit();

                }
                if(!TextUtils.isEmpty(get_location)){
                    MyProfileEditor.putString("location", get_location);
                }
                if(!TextUtils.isEmpty(get_introduce)){
                    Log.i("get_introduce", get_introduce);
                    MyProfileEditor.putString("introduce", get_introduce);
                }
                if(!TextUtils.isEmpty(get_hobby1)){
                    MyProfileEditor.putString("hobby1", get_hobby1);
                }
                if(!TextUtils.isEmpty(get_hobby2)){
                    MyProfileEditor.putString("hobby2", get_hobby2);
                }
                if(!TextUtils.isEmpty(get_hobby2)){
                    MyProfileEditor.putString("hobby3", get_hobby3);
                }
                if(!TextUtils.isEmpty(get_hobby2)){
                    MyProfileEditor.putString("hobby4", get_hobby4);
                }
                if(!TextUtils.isEmpty(get_hobby2)){
                    MyProfileEditor.putString("hobby5", get_hobby5);
                }
                MyProfileEditor.commit();
            }
        }   // onPost 끝

    }   // Profile Update Asyntask 끝
}
