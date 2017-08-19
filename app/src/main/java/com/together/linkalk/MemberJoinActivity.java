package com.together.linkalk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Created by kimhj on 2017-07-12.
 */

public class MemberJoinActivity extends AppCompatActivity {

    String personname;
    String persongivenname;
    String personfamilyname;
    String personemail;
    String personid;
    String logintype;
    int can_click_okbtn;

    TextView member_join_title;
    TextView no_email;
    TextView no_pwd;
    TextView no_pwdcon;
    TextView no_name;
    TextView no_lan;
    TextView ok_nick;
    TextView no_nick;
    TextView tv_nickname;
    Button btn_nickname_modify;
    LinearLayout namell;
    LinearLayout mailll;
    LinearLayout pwdll;
    LinearLayout pwdconll;
    EditText et_given_name;
    EditText et_family_name;
    EditText et_mail;
    EditText et_password;
    EditText et_password_con;
    EditText et_nickname;
    Spinner et_language;
    Button btn_mb_check;
    Button btn_mb_join;

    String sd_email;
    String sd_id;
    String sd_pwd;
    String sd_pwd1;
    String sd_pwd2;
    String sd_name;
    String sd_fname;
    String sd_gname;
    String sd_nickname;
    String sd_language;
    String sd_token;
    String sd_timezone;

    JSONObject jsonObject = new JSONObject();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_join_activity);

        can_click_okbtn = 0;

        member_join_title = (TextView)findViewById(R.id.member_join_title);
        no_email = (TextView)findViewById(R.id.no_email_msg);
        no_pwd = (TextView)findViewById(R.id.no_pwd_msg);
        no_pwdcon = (TextView)findViewById(R.id.no_pwdcon_msg);
        no_name = (TextView)findViewById(R.id.no_name_msg);
        ok_nick = (TextView)findViewById(R.id.ok_nick_msg);
        no_nick = (TextView)findViewById(R.id.no_nick_msg);
        no_lan = (TextView)findViewById(R.id.no_lan_msg);
        namell = (LinearLayout)findViewById(R.id.namell);
        mailll = (LinearLayout)findViewById(R.id.mailll);
        pwdll = (LinearLayout)findViewById(R.id.pwll);
        pwdconll = (LinearLayout)findViewById(R.id.pwconll);
        et_given_name = (EditText)findViewById(R.id.member_given_name);
        et_family_name = (EditText)findViewById(R.id.member_family_name);
        et_mail = (EditText)findViewById(R.id.member_email);
        et_password = (EditText)findViewById(R.id.member_password);
        et_password_con = (EditText)findViewById(R.id.member_password_con);
        et_nickname = (EditText)findViewById(R.id.member_nickname);
        et_language = (Spinner)findViewById(R.id.member_language);
        tv_nickname = (TextView)findViewById(R.id.member_nickname_ok);
        btn_nickname_modify = (Button)findViewById(R.id.member_nickname_check_btn_ok);
        btn_mb_check = (Button)findViewById(R.id.member_nickname_check_btn);
        btn_mb_join = (Button)findViewById(R.id.member_join);

        no_email.setVisibility(View.GONE);
        no_pwd.setVisibility(View.VISIBLE);
        no_pwd.setText("영문, 숫자, 특수문자를 포함한 12자 이상");
        no_pwdcon.setVisibility(View.GONE);
        no_name.setVisibility(View.GONE);
        ok_nick.setVisibility(View.GONE);
        no_nick.setVisibility(View.GONE);
        no_lan.setVisibility(View.GONE);

        // language spinner
        et_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sd_language = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final SharedPreferences sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
        logintype = sharedPreferences.getString("from","");

        // data get from login api
        Log.i("thisistype : ", logintype);
        if(logintype.equals("GOOGLE")){
            persongivenname = sharedPreferences.getString("givenName", "");
            personfamilyname = sharedPreferences.getString("familyName", "");
            personemail = sharedPreferences.getString("email", "");
            personid = sharedPreferences.getString("id", "");
            personname = persongivenname + " " + personfamilyname;
        } else if(logintype.equals("FACEBOOK")){
            persongivenname = sharedPreferences.getString("givenName", "");
            personfamilyname = sharedPreferences.getString("familyName", "");
            personemail = sharedPreferences.getString("email", "");
            personid = sharedPreferences.getString("id", "");
            personname = persongivenname + " " + personfamilyname;
        }

        // make activity by using data
        if(logintype.equals("GOOGLE")){
            member_join_title.setText(persongivenname + " " + personfamilyname);
            namell.setVisibility(View.GONE);
            mailll.setVisibility(View.GONE);
            pwdll.setVisibility(View.GONE);
            pwdconll.setVisibility(View.GONE);
            no_pwd.setVisibility(View.GONE);
            et_given_name.setText(persongivenname);
            et_family_name.setText(personfamilyname);
        } else if(logintype.equals("FACEBOOK")){
            member_join_title.setText(persongivenname + " " + personfamilyname);
            namell.setVisibility(View.GONE);
            mailll.setVisibility(View.GONE);
            pwdll.setVisibility(View.GONE);
            pwdconll.setVisibility(View.GONE);
            no_pwd.setVisibility(View.GONE);
            et_given_name.setText(persongivenname);
            et_family_name.setText(personfamilyname);
        }

        btn_nickname_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_nickname.setVisibility(View.GONE);
                btn_nickname_modify.setVisibility(View.GONE);
                et_nickname.setVisibility(View.VISIBLE);
                btn_mb_check.setVisibility(View.VISIBLE);
                et_nickname.setText(tv_nickname.getText());
            }
        });

        btn_mb_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nn = et_nickname.getText().toString();
                if(nn.indexOf(" ")==0 || nn.length()==0){
                    no_nick.setVisibility(View.VISIBLE);
                    ok_nick.setVisibility(View.GONE);
                } else {
                    CheckNick ctask = new CheckNick();
                    ctask.execute(nn);
                }
            }
        });

        // click the save button
        btn_mb_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(can_click_okbtn == 0){
                    Toast.makeText(getApplicationContext(), "CHECK 버튼을 클릭해주세요.", Toast.LENGTH_SHORT).show();
                } else if(can_click_okbtn == 1){
                    Toast.makeText(getApplicationContext(), "사용할 수 있는 닉네임으로 바꿔주세요.", Toast.LENGTH_SHORT).show();
                } else if(can_click_okbtn == 2){
                    if(logintype.equals("NEWMEMBER")){
                        // init
                        int complete[]={0,0,0,0,0};
                        no_email.setVisibility(View.GONE);
                        no_pwd.setVisibility(View.GONE);
                        no_pwdcon.setVisibility(View.GONE);
                        no_name.setVisibility(View.GONE);
                        ok_nick.setVisibility(View.GONE);
                        no_nick.setVisibility(View.GONE);
                        no_lan.setVisibility(View.GONE);

                        // data get
                        sd_email = et_mail.getText().toString();
                        sd_pwd1 = et_password.getText().toString();
                        sd_pwd2 = et_password_con.getText().toString();
                        sd_fname = et_family_name.getText().toString();
                        sd_gname = et_given_name.getText().toString();
                        sd_nickname = tv_nickname.getText().toString();
                        sd_name = sd_gname + " " + sd_fname;
                        sd_token = FirebaseInstanceId.getInstance().getToken();
                        sd_timezone = TimeZone.getDefault().getID();

                        // email check
                        if(sd_email.indexOf(" ")==0 || sd_email.length()==0 || sd_email.indexOf("@")==0){
                            no_email.setVisibility(View.VISIBLE);
                        } else if((!sd_email.contains(".com") && !sd_email.contains(".net") && !sd_email.contains(".co.kr")) || !sd_email.contains("@")){
                            no_email.setVisibility(View.VISIBLE);
                        } else {
                            complete[0]=1;
                        }

                        // pwd check
                        if(sd_pwd1.length()<12){
                            no_pwd.setVisibility(View.VISIBLE);
                            no_pwd.setText("NO!!! 비밀번호 형식에 맞춰주세요.");
                        } else {
                            if(!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()=-])(?=.*[a-zA-Z]).{12,64}$", sd_pwd1)) {
                                no_pwd.setVisibility(View.VISIBLE);
                                no_pwd.setText("NO!!! 비밀번호 형식에 맞춰주세요.");
                            } else {
                                complete[1] = 1;
                            }
                        }

                        // pwd match check
                        if(!sd_pwd1.equals(sd_pwd2)){
                            no_pwdcon.setVisibility(View.VISIBLE);
                        } else {
                            sd_pwd = sd_pwd1;
                            complete[2]=1;
                        }

                        // name check
                        if(sd_name.indexOf(" ")==0 || sd_name.length()==0){
                            no_name.setVisibility(View.VISIBLE);
                        } else {
                            complete[3]=1;
                        }

                        // language check
                        if(sd_language.equals("Select your language")){
                            no_lan.setVisibility(View.VISIBLE);
                        } else {
                            complete[4]=1;
                        }

                        try {
                            jsonObject.put("email", sd_email);
                            jsonObject.put("id", sd_id);
                            jsonObject.put("pwd", sd_pwd);
                            jsonObject.put("name", sd_name);
                            jsonObject.put("nickname", sd_nickname);
                            jsonObject.put("language", sd_language);
                            jsonObject.put("type", logintype);
                            jsonObject.put("token", sd_token);
                            jsonObject.put("timezone", sd_timezone);

                            String senddata = jsonObject.toString();
                            Log.i("member info json : ", senddata);

                            if(complete[0]==1 && complete[1]==1 && complete[2]==1 && complete[3]==1 && complete[4]==1){
                                InsertData task = new InsertData();
                                task.execute(senddata);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("nickname", sd_nickname);
                                editor.commit();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // init
                        int complete = 0;
                        ok_nick.setVisibility(View.GONE);
                        no_nick.setVisibility(View.GONE);
                        no_lan.setVisibility(View.GONE);

                        // get data
                        sd_email = personemail;
                        sd_id = personid;
                        sd_name = personname;
                        sd_nickname = tv_nickname.getText().toString();
                        sd_token = FirebaseInstanceId.getInstance().getToken();
                        sd_timezone = TimeZone.getDefault().getID();

                        // language check
                        if(sd_language.equals("Select your language")){
                            no_lan.setVisibility(View.VISIBLE);
                        } else {
                            complete = 1;
                        }

                        try {
                            jsonObject.put("email", sd_email);
                            jsonObject.put("id", sd_id);
                            jsonObject.put("pwd", sd_pwd);
                            jsonObject.put("name", sd_name);
                            jsonObject.put("nickname", sd_nickname);
                            jsonObject.put("language", sd_language);
                            jsonObject.put("type", logintype);
                            jsonObject.put("token", sd_token);
                            jsonObject.put("timezone", sd_timezone);

                            String senddata = jsonObject.toString();
                            Log.i("member info json : ", senddata);

                            if(complete == 1){
                                InsertData task = new InsertData();
                                task.execute(senddata);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("nickname", sd_nickname);
                                editor.commit();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


    } // onCreate End

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(logintype.equals("NEWMEMBER")){
            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent2);
        } else {
            finish();
        }
    } // onBackPressed End

    // InsertData AsyncTask Start
    class InsertData extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String data = params[0];
            Log.i("doinbackground json : ", data);
            try{
                URL url = new URL("http://o-ddang.com/linkalk/memberSave.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setDefaultUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(data.getBytes());
                os.flush();

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
            Log.i("post execute : ", s);
            if(s.trim().equals("que_failed")){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "회원가입에 실패하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                finish();
            } else if(s.trim().equals("que_complete_new")){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String sessionID = null;
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    sessionID = jsonObject.getString("PHPSESSID");  //session2
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(logintype.equals("FACEBOOK") || logintype.equals("GOOGLE")){
                    editor.putString("isLogged", "YES");
                    editor.commit();
                }
                editor.putString("nickname", sd_nickname);
                editor.putString("type", logintype);
                editor.putString("sessionID", "PHPSESSID="+sessionID);  //session2
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "환영합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    } // InsertData AsyncTask End

    // check nickname AsyncTask Start
    class CheckNick extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String nickname = params[0];
            try{
                URL url = new URL("http://www.o-ddang.com/linkalk/nicknameCheck.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setDefaultUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                StringBuffer buffer = new StringBuffer();
                buffer.append("nickname").append("=").append(nickname);

                Log.i("nickname asynctask : ", nickname);

                OutputStreamWriter outStream = new OutputStreamWriter(httpURLConnection.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                Log.i("buffer : ", buffer.toString());
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
            Log.i("nick post execute : ", s);
            if(s.trim().equals("already")){
                no_nick.setVisibility(View.VISIBLE);
                ok_nick.setVisibility(View.GONE);
                can_click_okbtn = 1;
            } else if(s.trim().equals("useok")) {
                tv_nickname.setVisibility(View.VISIBLE);
                tv_nickname.setText(et_nickname.getText().toString());
                ok_nick.setVisibility(View.VISIBLE);
                no_nick.setVisibility(View.GONE);
                can_click_okbtn = 2;
                btn_nickname_modify.setVisibility(View.VISIBLE);
                et_nickname.setVisibility(View.GONE);
                btn_mb_check.setVisibility(View.GONE);
            }

        }
    } // check nick AsyncTask End



}
