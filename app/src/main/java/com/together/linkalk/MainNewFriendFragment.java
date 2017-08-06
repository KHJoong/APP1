package com.together.linkalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by kimhj on 2017-07-06.
 */

public class MainNewFriendFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;

    ListView lvNewFriend;
    NewMemberList_Adapter nmAdapter;

    String[] nowArray;
    String nowYear;
    String nowMonth;
    String nowDay;
    String nowHour;
    String nowMin;
    String nowSec;
    String saveYear;
    String saveMonth;
    String saveDay;
    String saveHour;
    String saveMin;
    String saveSec;

    public MainNewFriendFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.main_newfriend_activity, container, false);
        lvNewFriend = (ListView)layout.findViewById(R.id.lvNewFriend);
        nmAdapter = new NewMemberList_Adapter(getActivity().getApplicationContext());
        lvNewFriend.setAdapter(nmAdapter);

        // 아래로 당겨서 추천 친구 목록 새로고침하기
        swipeRefreshLayout = (SwipeRefreshLayout)layout.findViewById(R.id.srlNewFriend);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NewMemberLoad nml = new NewMemberLoad();
                nml.execute();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // 핸드폰 네트워크 상태 체크
        String status = NetWorkStatusCheck.getConnectivityStatusString(getActivity().getApplicationContext());
        if(status.equals("WIFI") || status.equals("MOBILE")){
            // 네트워크 상태가 연결되어 있으면, 서버에서 받아오는 목록 출력
            Log.i("NewFriendNetStatus", status);
            NewMemberLoad nml = new NewMemberLoad();
            nml.execute();
        } else if(status.equals("NOT")){
            // 네트워크 상태가 끊어져 있으면, 제일 마지막에 저장했던 목록 출력
            Log.i("NewFriendNetStatus", status);
            SharedPreferences newMemberShared = getActivity().getSharedPreferences("newMember", Context.MODE_PRIVATE);

            // 현재 시간 정보 받아와서 년, 월, 일, 시간, 분, 초로 분리-------------------------------------
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date dt = new Date();

            Log.i("newFriendDateFormat", sdf.format(dt).toString());
            nowArray = sdf.format(dt).toString().split("/");

            nowYear = nowArray[0];
            nowMonth = nowArray[1];
            nowDay = nowArray[2];
            nowHour = nowArray[3];
            nowMin = nowArray[4];
            nowSec = nowArray[5];
            Log.i("nowYear", nowYear);
            Log.i("nowMonth", nowMonth);
            Log.i("nowDay", nowDay);
            Log.i("nowHour", nowHour);
            Log.i("nowMin", nowMin);
            Log.i("nowSec", nowSec);
            // 현재 시간 정보 받아와서 년, 월, 일, 시간, 분, 초로 분리 여기까지-------------------------------------

            for(int i=0;;i++){
                if(newMemberShared.getString(String.valueOf(i), "")!=null || newMemberShared.getString(String.valueOf(i), "")!=""){
                    JSONObject object = null;
                    try {
                        object = new JSONObject(newMemberShared.getString(String.valueOf(i), ""));
                        String nickname = object.getString("nickname");
                        String language = object.getString("language");
                        String lasttime = object.getString("lasttime");
                        String[] savetime = lasttime.split("/");
                        saveYear = savetime[0];
                        saveMonth = savetime[1];
                        saveDay = savetime[2];
                        saveHour = savetime[3];
                        saveMin = savetime[4];
                        saveSec = savetime[5];
                        Log.i("saveYear", saveYear);
                        Log.i("saveMonth", saveMonth);
                        Log.i("saveDay", saveDay);
                        Log.i("saveHour", saveHour);
                        Log.i("saveMin", saveMin);
                        Log.i("saveSec", saveSec);
                        if(nowYear.equals(saveYear)){
                            if(nowMonth.equals(saveMonth)){
                                if(nowDay.equals(saveDay)){
                                    if(nowHour.equals(saveHour)) {
                                        if (nowMin.equals(saveMin)) {
                                            lasttime =  Integer.toString(Integer.parseInt(nowSec)-Integer.parseInt(saveSec))+"초 전";
                                        } else {
                                            lasttime =  Integer.toString(Integer.parseInt(nowMin)-Integer.parseInt(saveMin))+"분 전";
                                        }
                                    } else {
                                        lasttime =  Integer.toString(Integer.parseInt(nowHour)-Integer.parseInt(saveHour))+"시간 전";
                                    }
                                } else {
                                    lasttime =  Integer.toString(Integer.parseInt(nowDay)-Integer.parseInt(saveDay))+"일 전";
                                }
                            } else {
                                lasttime =  Integer.toString(Integer.parseInt(nowMonth)-Integer.parseInt(saveMonth))+"달 전";
                            }
                        } else {
                            lasttime =  Integer.toString(Integer.parseInt(nowYear)-Integer.parseInt(saveYear))+"년 전";
                        }
                        String location = object.getString("location");
                        String introduce = object.getString("introduce");
                        String hobby1 = object.getString("hobby1");
                        String hobby2 = object.getString("hobby2");
                        String hobby3 = object.getString("hobby3");
                        String hobby4 = object.getString("hobby4");
                        String hobby5 = object.getString("hobby5");
                        Member member = new Member(nickname, location, language, lasttime, introduce, hobby1, hobby2, hobby3, hobby4, hobby5);
                        nmAdapter.addItem(member);
                    } catch (JSONException e) {
                        break;
                    }
                }
            }
            lvNewFriend.setAdapter(nmAdapter);
        }

        // 추천 친구 목록에서 한 명을 클릭했을 때 화면 전환
        lvNewFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member)parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), NewFriendDetailProfile.class);
                intent.putExtra("position", position);
                startActivity(intent);

            }
        });

        return layout;
    } // onCreateView 끝

    @Override
    public void onResume() {
        super.onResume();
        NewMemberLoad nml = new NewMemberLoad();
        nml.execute();
    }

    // 서버에 추천 친구 목록 데이터를 요청하고 받아와서 리스트뷰로 뿌리는 AsyncTask
    class NewMemberLoad extends AsyncTask<Void, Void, String>{

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
        protected void onPreExecute() {
            super.onPreExecute();

            // 현재 시간 정보 받아와서 년, 월, 일, 시간, 분, 초로 분리-------------------------------------
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date dt = new Date();

            Log.i("newFriendDateFormat", sdf.format(dt).toString());
            nowArray = sdf.format(dt).toString().split("/");

            nowYear = nowArray[0];
            nowMonth = nowArray[1];
            nowDay = nowArray[2];
            nowHour = nowArray[3];
            nowMin = nowArray[4];
            nowSec = nowArray[5];
            Log.i("nowYear", nowYear);
            Log.i("nowMonth", nowMonth);
            Log.i("nowDay", nowDay);
            Log.i("nowHour", nowHour);
            Log.i("nowMin", nowMin);
            Log.i("nowSec", nowSec);
            // 현재 시간 정보 받아와서 년, 월, 일, 시간, 분, 초로 분리 여기까지-------------------------------------
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                // 서버에서 추천 친구 목록을 받기 위해 요청하는 부분
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String sessionID = sharedPreferences.getString("sessionID", "");

                Log.i("sessionID",sessionID);
//                JSONObject object = new JSONObject();
//                object.put("sessionID", sessionID);
//                String send = object.toString();

                URL url = new URL("http://www.o-ddang.com/linkalk/newMemberList.php");
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

                httpURLConnection.connect();    // plus
//                httpURLConnection.setRequestProperty("Accept", "application/json");
//                httpURLConnection.setRequestProperty("Content-type", "application/json");
//
//                OutputStream os = httpURLConnection.getOutputStream();
//                os.write(send.getBytes());
//                os.flush();

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
            }
//            catch (JSONException e) {
//                e.printStackTrace();
//                return e.getMessage();
//            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("newMemberList", s);
            SharedPreferences newMemberShared = getActivity().getSharedPreferences("newMember", Context.MODE_PRIVATE);
            SharedPreferences.Editor newMemberEditor = newMemberShared.edit();
            int n;

            try {
                JSONObject object = new JSONObject(s);
                JSONArray array = object.getJSONArray("profile");

                nmAdapter.nmItem.clear();
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
                        String[] savetime = lasttime.split("/");
                        saveYear = savetime[0];
                        saveMonth = savetime[1];
                        saveDay = savetime[2];
                        saveHour = savetime[3];
                        saveMin = savetime[4];
                        saveSec = savetime[5];
                        Log.i("saveYear", saveYear);
                        Log.i("saveMonth", saveMonth);
                        Log.i("saveDay", saveDay);
                        Log.i("saveHour", saveHour);
                        Log.i("saveMin", saveMin);
                        Log.i("saveSec", saveSec);
                        if(nowYear.equals(saveYear)){
                           if(nowMonth.equals(saveMonth)){
                               if(nowDay.equals(saveDay)){
                                   if(nowHour.equals(saveHour)) {
                                       if (nowMin.equals(saveMin)) {
                                           lasttime =  Integer.toString(Integer.parseInt(nowSec)-Integer.parseInt(saveSec))+"초 전";
                                       } else {
                                           lasttime =  Integer.toString(Integer.parseInt(nowMin)-Integer.parseInt(saveMin))+"분 전";
                                       }
                                   } else {
                                       lasttime =  Integer.toString(Integer.parseInt(nowHour)-Integer.parseInt(saveHour))+"시간 전";
                                   }
                               } else {
                                   lasttime =  Integer.toString(Integer.parseInt(nowDay)-Integer.parseInt(saveDay))+"일 전";
                               }
                           } else {
                               lasttime =  Integer.toString(Integer.parseInt(nowMonth)-Integer.parseInt(saveMonth))+"달 전";
                           }
                        } else {
                            lasttime =  Integer.toString(Integer.parseInt(nowYear)-Integer.parseInt(saveYear))+"년 전";
                        }
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

                    Member member = new Member(nickname, location, language, lasttime, introduce, hobby1, hobby2, hobby3, hobby4, hobby5);
                    nmAdapter.addItem(member);
                    newMemberEditor.putString(String.valueOf(n), s_obj);
                }
                lvNewFriend.setAdapter(nmAdapter);
                newMemberEditor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
