package com.together.linkalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by kimhj on 2017-07-06.
 */

public class MainFriendFragment extends Fragment {

    ListView lvMyFriend;
    MyFriendList_Adapter mfAdapter;
    View header;

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


    public MainFriendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 서버의 친구목록과 동기화
//        GetMyFriend gmf = new GetMyFriend();
//        gmf.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.main_friend_activity, container, false);

        // 친구목록 ListView 셋팅
        lvMyFriend = (ListView)layout.findViewById(R.id.lvMyFriend);
        mfAdapter = new MyFriendList_Adapter(getActivity().getApplicationContext());
        header = getActivity().getLayoutInflater().inflate(R.layout.main_friend_header, null, false);
//        lvMyFriend.setAdapter(mfAdapter);

        myFriendLoad();

        // 친구 목록에서 한 명을 클릭했을 때 화면전환
        lvMyFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member)parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), MyFriendDetailProfile.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        return layout;
    }   // onCreateView 끝

    @Override
    public void onResume() {
        super.onResume();
        myFriendLoad();

        if(mfAdapter.mfItem.size()==0){
            lvMyFriend.addHeaderView(header, null, false);
        } else {
            lvMyFriend.removeHeaderView(header);
        }
        // 어댑터 새로고침
        lvMyFriend.setAdapter(mfAdapter);
    }

    // 추가한 친구 목록 불러오기
    public void myFriendLoad(){
        mfAdapter.mfItem.clear();

        // 친구 목록 출력
        for(int i=0; ; i++){
            SharedPreferences myFriendShared = this.getActivity().getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
            if(myFriendShared.contains(String.valueOf(i))){
                myFriend_tmp = myFriendShared.getString(String.valueOf(i), "");
                try {
                    JSONObject friendObject = new JSONObject(myFriend_tmp);
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
                    mfAdapter.addItem(member);
                    Log.i("myfriendinfo", String.valueOf(mfAdapter.mfItem.get(i).getNickname()));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            } else {
                break;
            }
        }

    }   // myFriendLoad 끝

    // 서버에서 친구 목록 받아오는 Asynctask
//    class GetMyFriend extends AsyncTask<Void, Void, String>{
//
//        String nickname;
//        String language;
//        String lasttime;
//        String location;
//        String introduce;
//        String hobby1;
//        String hobby2;
//        String hobby3;
//        String hobby4;
//        String hobby5;
//
//        @Override
//        protected String doInBackground(Void... params) {
//            try{
//                // 서버에서 친구 목록을 받기 위해 요청하는 부분
//                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("maintain", Context.MODE_PRIVATE);
//                String sessionID = sharedPreferences.getString("sessionID", "");
//
//                Log.i("sessionID",sessionID);
//
//                URL url = new URL("http://www.o-ddang.com/linkalk/myFriendListUpdate.php");
//                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
//
//                httpURLConnection.setReadTimeout(5000);
//                httpURLConnection.setConnectTimeout(5000);
//                httpURLConnection.setDefaultUseCaches(false);
//                httpURLConnection.setDoInput(true);
//                httpURLConnection.setDoOutput(true);
//                httpURLConnection.setRequestMethod("POST");
//
//                httpURLConnection.setInstanceFollowRedirects( false );
//                if(!TextUtils.isEmpty(sessionID)) {
//                    httpURLConnection.setRequestProperty( "cookie", sessionID) ;
//                }
//
//                httpURLConnection.connect();
//
//                // 서버에서 친구 목록 데이터를 받아오는 부분
//                int responseStatusCode = httpURLConnection.getResponseCode();
//                Log.d("responseStatusCode", "response code - " + responseStatusCode);
//
//                InputStream inputStream;
//                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
//                    inputStream = httpURLConnection.getInputStream();
//                } else{
//                    inputStream = httpURLConnection.getErrorStream();
//                }
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder sb = new StringBuilder();
//                String line;
//
//                while((line = bufferedReader.readLine()) != null){
//                    sb.append(line);
//                }
//                bufferedReader.close();
//                return sb.toString().trim();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                return e.getMessage();
//            } catch (ProtocolException e) {
//                e.printStackTrace();
//                return e.getMessage();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                return e.getMessage();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return e.getMessage();
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            Log.i("newMemberList", s);
//            SharedPreferences myfriend = getActivity().getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
//            SharedPreferences.Editor myFriendEditor = myfriend.edit();
//            int n;
//
//            try {
//                JSONObject object = new JSONObject(s);
//                String update = object.getString("update");
//                JSONArray array = object.getJSONArray("profile");
//
//                if(update.equals("false")){
//
//                } else if(update.equals("success")){
//                    mfAdapter.mfItem.clear();
//                    for(n=0; n<array.length(); n++){
//                        String s_obj = array.getString(n);
//                        JSONObject obj = new JSONObject(s_obj);
//                        if(!TextUtils.isEmpty(obj.getString("nickname"))){
//                            nickname = obj.getString("nickname");
//                        }
//                        if(!TextUtils.isEmpty(language = obj.getString("language"))){
//                            language = obj.getString("language");
//                        }
//                        if(!TextUtils.isEmpty(lasttime = obj.getString("lasttime"))) {
//                            lasttime = obj.getString("lasttime");
//                        }
//                        if(!TextUtils.isEmpty(location = obj.getString("location"))){
//                            location = obj.getString("location");
//                        }
//                        if(!TextUtils.isEmpty(introduce = obj.getString("introduce"))){
//                            introduce = obj.getString("introduce");
//                        }
//                        if(!TextUtils.isEmpty(hobby1 = obj.getString("hobby1"))){
//                            hobby1 = obj.getString("hobby1");
//                        }
//                        if(!TextUtils.isEmpty(hobby2 = obj.getString("hobby2"))){
//                            hobby2 = obj.getString("hobby2");
//                        }
//                        if(!TextUtils.isEmpty(hobby3 = obj.getString("hobby3"))){
//                            hobby3 = obj.getString("hobby3");
//                        }
//                        if(!TextUtils.isEmpty(hobby4 = obj.getString("hobby4"))){
//                            hobby4 = obj.getString("hobby4");
//                        }
//                        if(!TextUtils.isEmpty(hobby5 = obj.getString("hobby5"))){
//                            hobby5 = obj.getString("hobby5");
//                        }
//                        myFriendEditor.putString(String.valueOf(n), s_obj);
//                    }
//                    myFriendEditor.commit();
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

}
