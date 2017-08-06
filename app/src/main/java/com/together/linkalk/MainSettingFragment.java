package com.together.linkalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

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

/**
 * Created by kimhj on 2017-07-06.
 */

public class MainSettingFragment extends Fragment {

    Button member_modify_button;

    public MainSettingFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.main_setting_activity, container, false);
        member_modify_button = (Button)layout.findViewById(R.id.member_modify_button);
        member_modify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 핸드폰 네트워크 상태 체크
                String status = NetWorkStatusCheck.getConnectivityStatusString(getActivity().getApplicationContext());
                if(status.equals("WIFI") || status.equals("MOBILE")){
                    // 네트워크 상태가 연결되어 있으면, 서버에서 내 정보 받아와서 출력
                    GetMyProfile getMyProfile = new GetMyProfile();
                    getMyProfile.execute();
                } else if(status.equals("NOT")){
                    SharedPreferences myProfilerShared = getActivity().getSharedPreferences("MyProfile", Context.MODE_PRIVATE);
                    SharedPreferences loggedMemberShared = getActivity().getSharedPreferences("maintain", Context.MODE_PRIVATE);

                    if(myProfilerShared.getString("nickname", "").equals(loggedMemberShared.getString("nickname", ""))){
                        // 네트워크 상태가 끊어져 있으면, 제일 마지막에 저장했던 내 정보를 불러오거나
                        Intent intent = new Intent(getActivity().getApplicationContext(), MyProfileModify.class);
                        startActivity(intent);
                    } else {
                        // 네트워크 상태가 끊어져 있으면, 제일 마지막에 저장했던 정보도 없을 경우 메시지 출력력
                        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setMessage("네트워크 연결이 되어있지 않아 정보를 불러올 수 없습니다.")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).create().show();
                    }
                }

            }
        });
        return layout;
    }

    // 회원 정보 수정을 위해 서버에 등록된 나의 정보를 받아오는 Async
    class GetMyProfile extends AsyncTask<Void, Void, String> {

        String nickname = null;
        String language = null;
        String location = null;
        String introduce = null;
        String hobby1 = null;
        String hobby2 = null;
        String hobby3 = null;
        String hobby4 = null;
        String hobby5 = null;

        @Override
        protected String doInBackground(Void... params) {
            try{
                // 서버에서 추천 친구 목록을 받기 위해 요청하는 부분
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String sessionID = sharedPreferences.getString("sessionID", "");

                Log.i("sessionID",sessionID);
                JSONObject object = new JSONObject();
                object.put("sessionID", sessionID);
                String send = object.toString();

                URL url = new URL("http://www.o-ddang.com/linkalk/sendMyDetailProfile.php");
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
            Log.i("newMemberList", s);
            SharedPreferences newMemberShared = getActivity().getSharedPreferences("MyProfile", Context.MODE_PRIVATE);
            SharedPreferences.Editor MyProfileEditor = newMemberShared.edit();

            try {
                JSONObject object = new JSONObject(s);

                if(!TextUtils.isEmpty(object.getString("nickname"))){
                    nickname = object.getString("nickname");
                }
                if(!TextUtils.isEmpty(object.getString("language"))){
                    language = object.getString("language");
                }
                if(!TextUtils.isEmpty(object.getString("location"))){
                    location = object.getString("location");
                }
                if(!TextUtils.isEmpty(object.getString("introduce"))){
                    introduce = object.getString("introduce");
                }
                if(!TextUtils.isEmpty(object.getString("hobby1"))){
                    hobby1 = object.getString("hobby1");
                }
                if(!TextUtils.isEmpty(object.getString("hobby2"))){
                    hobby2 = object.getString("hobby2");
                }
                if(!TextUtils.isEmpty(object.getString("hobby3"))){
                    hobby3 = object.getString("hobby3");
                }
                if(!TextUtils.isEmpty(object.getString("hobby4"))){
                    hobby4 = object.getString("hobby4");
                }
                if(!TextUtils.isEmpty(object.getString("hobby5"))){
                    hobby5 = object.getString("hobby5");
                }

                MyProfileEditor.putString("nickname", nickname);
                MyProfileEditor.putString("language", language);
                MyProfileEditor.putString("location", location);
                MyProfileEditor.putString("introduce", introduce);
                MyProfileEditor.putString("hobby1", hobby1);
                MyProfileEditor.putString("hobby2", hobby2);
                MyProfileEditor.putString("hobby3", hobby3);
                MyProfileEditor.putString("hobby4", hobby4);
                MyProfileEditor.putString("hobby5", hobby5);
                MyProfileEditor.commit();

                Intent intent = new Intent(getActivity().getApplicationContext(), MyProfileModify.class);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
