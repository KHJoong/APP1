package com.together.linkalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    LinearLayout parent_of_viewpager;
    ViewPager viewPager;
    TextView btn_friend;
    TextView btn_newfriend;
    TextView btn_chat;
    TextView btn_setting;

    static String type;
    static String nickname;

    static Socket socket;
    static DataOutputStream out;
    static DataInputStream in;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
        nickname = sharedPreferences.getString("nickname", "");
        type = sharedPreferences.getString("type", "");

        socket = new Socket();
        if(!socket.isConnected()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ip = "115.71.232.230"; // IP
                    int port = 9999; // PORT번호

                    // 서버에 연결할 때 보낼 내 닉네임
                    SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
                    String my_nickname = sharedPreferences.getString("nickname", "");

                    try {
                        SocketAddress sock_addr = new InetSocketAddress(ip, port);
                        socket.connect(sock_addr);
                        out = new DataOutputStream(socket.getOutputStream());
                        in = new DataInputStream(socket.getInputStream());

                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(my_nickname);
                        dos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        parent_of_viewpager = (LinearLayout)findViewById(R.id.parent_of_viewpager);
        viewPager = (ViewPager)findViewById(R.id.main_viewpager);
        btn_friend = (TextView)findViewById(R.id.btn_friend);
        btn_newfriend = (TextView)findViewById(R.id.btn_newfriend);
        btn_chat = (TextView)findViewById(R.id.btn_chat);
        btn_setting = (TextView)findViewById(R.id.btn_setting);

        viewPager.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(0);

        btn_friend.setOnClickListener(movePageListener);
        btn_friend.setTag(0);
        btn_newfriend.setOnClickListener(movePageListener);
        btn_newfriend.setTag(1);
        btn_chat.setOnClickListener(movePageListener);
        btn_chat.setTag(2);
        btn_setting.setOnClickListener(movePageListener);
        btn_setting.setTag(3);

        btn_friend.setSelected(true);

        // ViewPager 스크롤 이동
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int i = 0;
                while(i<4) {
                    if(position==i) {
                        parent_of_viewpager.findViewWithTag(i).setSelected(true);
                    } else {
                        parent_of_viewpager.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    } // onCreate End

    @Override
    protected void onStart() {
        super.onStart();
        String status = NetWorkStatusCheck.getConnectivityStatusString(getApplicationContext());
        if(status.equals("WIFI") || status.equals("MOBILE")){
            // 유저의 마지막 로그인 시간을 업데이트하는 asynctask
            TimeUpdate timeUpdate = new TimeUpdate();
            timeUpdate.execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String status = NetWorkStatusCheck.getConnectivityStatusString(getApplicationContext());
        if(status.equals("WIFI") || status.equals("MOBILE")) {
            // 유저의 친구 목록을 업데이트하는 asynctask
            GetMyFriend gmf = new GetMyFriend();
            gmf.execute();
        }
    }

    // ViewPager 클릭 이동
    View.OnClickListener movePageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (int) v.getTag();
            int i = 0;
            while(i<4){
                if(tag==i){
                    parent_of_viewpager.findViewWithTag(i).setSelected(true);
                } else {
                    parent_of_viewpager.findViewWithTag(i).setSelected(false);
                }
                i++;
            }
            viewPager.setCurrentItem(tag);
        }
    };

    // ViewPager Adapter
    private class pagerAdapter extends FragmentStatePagerAdapter {
        public pagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new MainFriendFragment();
                case 1:
                    return new MainNewFriendFragment();
                case 2:
                    return new MainChatFragment();
                case 3:
                    return new MainSettingFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    // TimeUpdate AsyncTask Start
    class TimeUpdate extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String logindata = "";
            SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
            String data = sharedPreferences.getString("nickname", "");
            JSONObject object = new JSONObject();
            try {
                object.put("nickname", data);
                logindata = object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("login json data : ", logindata);
            try{
                URL url = new URL("http://o-ddang.com/linkalk/updateTime.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setDefaultUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(logindata.getBytes());
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
            Log.i("updatetime query", s);

        }
    } // TimeUpdate AsyncTask End

    // 서버에서 친구 목록 받아오는 Asynctask
    class GetMyFriend extends AsyncTask<Void, Void, String>{

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
        protected String doInBackground(Void... params) {
            try{
                // 서버에서 친구 목록을 받기 위해 요청하는 부분
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String sessionID = sharedPreferences.getString("sessionID", "");

                Log.i("sessionID",sessionID);

                URL url = new URL("http://www.o-ddang.com/linkalk/myFriendListUpdate.php");
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

                httpURLConnection.connect();

                // 서버에서 친구 목록 데이터를 받아오는 부분
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
            Log.i("newMemberList", s);
            SharedPreferences myfriend = getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
            SharedPreferences.Editor myFriendEditor = myfriend.edit();
            int n;

            try {
                JSONObject object = new JSONObject(s);
                String update = object.getString("update");
                JSONArray array = object.getJSONArray("profile");

                if(update.equals("false")){

                } else if(update.equals("success")){
                    myfriend.edit().clear().commit();
                    for(n=0; n<array.length(); n++){
                        String s_obj = array.getString(n);
                        JSONObject obj = new JSONObject(s_obj);
                        if(!TextUtils.isEmpty(obj.getString("nickname"))){
                            nickname = obj.getString("nickname");
                        }
                        if(!TextUtils.isEmpty(language = obj.getString("language"))){
                            language = obj.getString("language");
                        }
                        if(!TextUtils.isEmpty(lasttime = obj.getString("lasttime"))) {
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
                        myFriendEditor.putString(String.valueOf(n), s_obj);
                    }
                    myFriendEditor.commit();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
