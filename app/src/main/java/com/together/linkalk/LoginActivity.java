package com.together.linkalk;

import android.app.Activity;
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
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * Created by kimhj on 2017-07-05.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    static String TAG = "LoginActivity";

    int RC_SIGN_IN = 1001;

    String persongivenname;
    String personfamilyname;
    String personemail;
    String personid;

    String isLogged;
    String nickname;
    String type;

    Button to_member_join_btn;
    Button to_member_login_btn;
    EditText login_id;
    EditText login_pw;

    SignInButton google_login_btn;
    Button google_logout_btn;
    LoginButton facebook_login_btn;
    GoogleApiClient mGoogleApiClient;
    CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;


    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    type = "FACEBOOK";

                    SharedPreferences sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    try {
                        editor.putString("from", "FACEBOOK");
                        editor.putString("id", object.getString("id"));
                        if(object.has("first_name")){
                            editor.putString("givenName", object.getString("first_name"));
                        }
                        if(object.has("last_name")){
                            editor.putString("familyName", object.getString("last_name"));
                        }
                        if (object.has("email")){
                            editor.putString("Email", object.getString("email"));
                        }
                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JSONObject jobject = new JSONObject();
                    try{
                        jobject.put("type", "FACEBOOK");
                        jobject.put("id", object.getString("id"));
                        jobject.put("token", FirebaseInstanceId.getInstance().getToken());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String data = jobject.toString();
                    LoginOtherApi loa = new LoginOtherApi();
                    loa.execute(data);

                    SharedPreferences sharedPreferences2 = getSharedPreferences("maintain", MODE_PRIVATE);

                    if(sharedPreferences2.getString("isLogged", "").equals("YES")){
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG,object.toString());
                        Log.e(TAG,response.toString());
//                        SharedPreferences sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        try {
//                            Intent intent = new Intent(getApplicationContext(), MemberJoinActivity.class);
//                            editor.putString("from", "FACEBOOK");
//                            editor.putString("id", object.getString("id"));
//                            if(object.has("first_name")){
//                                editor.putString("givenName", object.getString("first_name"));
//                            }
//                            if(object.has("last_name")){
//                                editor.putString("familyName", object.getString("last_name"));
//                            }
//                            if (object.has("email")){
//                                editor.putString("Email", object.getString("email"));
//                            }
//                            editor.commit();
//
//                            startActivity(intent);
//                            finish();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            });
            //Here we put the requested fields to be returned from the JSONObject
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, first_name, last_name, email, birthday, gender");
            request.setParameters(parameters);
            request.executeAsync();



        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
        if(sharedPreferences.getString("isLogged", "").equals("YES")){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        login_id = (EditText)findViewById(R.id.login_id);
        login_pw = (EditText)findViewById(R.id.login_pw);

        to_member_join_btn = (Button)findViewById(R.id.to_member_join_btn);
        to_member_join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MemberJoinActivity.class);
                SharedPreferences sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("from", "NEWMEMBER");
                editor.commit();
                startActivity(intent);
                finish();
            }
        });

        to_member_login_btn = (Button)findViewById(R.id.to_member_login_btn);
        to_member_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(login_id.getText().toString().equals("") && login_pw.getText().toString().equals("")){

                } else if(login_id.getText().toString().equals("")){

                } else if(login_pw.getText().toString().equals("")){

                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("from", "NEWMEMBER");
                    editor.commit();

                    String logintype = sharedPreferences.getString("from","");

                    // Login Task
                    if(logintype.equals("NEWMEMBER")){
                        Intent intent = getIntent();
                        JSONObject data = new JSONObject();
                        try {
                            data.put("type", logintype);
                            data.put("email", login_id.getText().toString());
                            data.put("pword", login_pw.getText().toString());
                            data.put("token", FirebaseInstanceId.getInstance().getToken());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        LoginMember ctask = new LoginMember();
                        ctask.execute(data.toString());
                    } else {
                        JSONObject data = new JSONObject();
                        try {
                            data.put("type", logintype);
                            data.put("id", sharedPreferences.getString("id", ""));
                            data.put("token", FirebaseInstanceId.getInstance().getToken());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        LoginMember ctask = new LoginMember();
                        ctask.execute(data.toString());
                    }
                }

            }
        });

        // --------------------Google Sign-in Start--------------------
        google_logout_btn = (Button)findViewById(R.id.google_logout_btn);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, LoginActivity.this /* OnConnectionFailedListener */).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        // [END build_client]

        google_login_btn = (SignInButton) findViewById(R.id.google_login_btn);
        google_login_btn.setSize(SignInButton.SIZE_STANDARD);
        google_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
        // --------------------Google Sign-in End--------------------

        // --------------------Google Sign-out Start--------------------
        google_logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        // --------------------Google Sign-out End--------------------

        // --------------------Facebook Sign-in Start--------------------
        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        facebook_login_btn = (LoginButton)findViewById(R.id.facebook_login_btn);
        facebook_login_btn.setReadPermissions("public_profile", "email");
        facebook_login_btn.registerCallback(callbackManager, callback);
        accessTokenTracker= new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {

            }
        };
        accessTokenTracker.startTracking();
        // --------------------Facebook Sign-in End--------------------

    } // onCreate End

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            // Facebook Sign-in
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Google Sign In
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            type = "GOOGLE";

            GoogleSignInAccount acct = result.getSignInAccount();

            persongivenname = acct.getGivenName();
            personfamilyname = acct.getFamilyName();
            personemail = acct.getEmail();
            personid = acct.getId();
            SharedPreferences sharedPreferences3 = getSharedPreferences("Login", MODE_PRIVATE);
            SharedPreferences.Editor editor3 = sharedPreferences3.edit();
            editor3.putString("from", type);
            editor3.putString("id", personid);
            editor3.putString("givenName", persongivenname);
            editor3.putString("familyName", personfamilyname);
            editor3.putString("Email", personemail);
            editor3.commit();

            JSONObject object = new JSONObject();
            try{
                object.put("type", "GOOGLE");
                object.put("id", acct.getId());
                object.put("token", FirebaseInstanceId.getInstance().getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String data = object.toString();
            LoginOtherApi loa = new LoginOtherApi();
            loa.execute(data);

            SharedPreferences sharedPreferences2 = getSharedPreferences("maintain", MODE_PRIVATE);
            if(sharedPreferences2.getString("isLogged", "").equals("YES")){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {

            }
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    // [START Google signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START Google signOut]
    private void signOut() {Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // Google button image change
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            google_login_btn.setVisibility(View.INVISIBLE);
            google_logout_btn.setVisibility(View.VISIBLE);
        } else {
            google_login_btn.setVisibility(View.VISIBLE);
            google_logout_btn.setVisibility(View.GONE);
        }
    }

    // Google Sign In Failed
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    // login AsyncTask Start
    class LoginOtherApi extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String logindata = params[0];
            Log.i("login json data : ", logindata);
            try{
                URL url = new URL("http://www.o-ddang.com/linkalk/loggedCheck.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setDefaultUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                //session2
//                httpURLConnection.setInstanceFollowRedirects( false );
//                if(TextUtils.isEmpty(sharedPreferences.getString("sessionID", ""))) {
//                    httpURLConnection.setRequestProperty( "cookie", sharedPreferences.getString("sessionID", "")) ;
//                }
                //session2

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(logindata.getBytes());
                os.flush();



//                String sessionID = null;
//                Map< String, List<String>> imap = httpURLConnection.getHeaderFields( ) ;
//                /// 그리고 거길 뒤져서 쿠키를 찾아냄
//                if( imap.containsKey( "Set-Cookie" ) ) {
//                    /// 쿠키를 스트링으로 쫙 저장함
//                    List<String> stringSession = imap.get( "Set-Cookie" ) ;
//                    for( int i = 0 ; i < stringSession.size() ; i++ ) {
//                        sessionID += stringSession.get( i ) ;
//                    }
//                }
//                SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("sessionID", sessionID);
//                editor.commit();
//                Log.i("Login sessionID", sessionID);



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
            String isLogged= null;
            String nickname= null;
            String type= null;
            String sessionID = null;
            try {
                JSONObject jsonObject = new JSONObject(s);
                isLogged = jsonObject.getString("isLogged");
                if(isLogged.equals("YES")){
                    nickname = jsonObject.getString("nickname");
                    type = jsonObject.getString("type");
                    sessionID = jsonObject.getString("PHPSESSID");  //session2
                    Log.i("loginactivity sessionid", sessionID);
                } else {
                    type = jsonObject.getString("type");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(isLogged.equals("YES")){
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("isLogged", isLogged);
                editor.putString("nickname", nickname);
                editor.putString("type", type);
                editor.putString("sessionID", "PHPSESSID="+sessionID);  //session2
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else if(isLogged.equals("NO")) {
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("isLogged", isLogged);
                editor.commit();
                if(type.equals("FACEBOOK")){
                    Intent intent = new Intent(getApplicationContext(), MemberJoinActivity.class);
                    startActivity(intent);
                    finish();
                } else if(type.equals("GOOGLE")){
                    Intent intent = new Intent(getApplicationContext(), MemberJoinActivity.class);
                    startActivity(intent);
                    updateUI(true);
                    finish();
                }
            }

        }
    } // login AsyncTask End

    // login AsyncTask Start
    class LoginMember extends AsyncTask<String, Void, String>{

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String logindata = params[0];
            SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Log.i("login json data : ", logindata);
            try{
                URL url = new URL("http://www.o-ddang.com/linkalk/loggedCheck.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setDefaultUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-type", "application/json");

                //session2
//                httpURLConnection.setInstanceFollowRedirects( false );
//                if(!TextUtils.isEmpty(sharedPreferences.getString("sessionID", ""))) {
//                    httpURLConnection.setRequestProperty( "cookie", sharedPreferences.getString("sessionID", "")) ;
//                }
                //session2

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(logindata.getBytes());
                os.flush();

                //session 2
//                String sessionID = null;
//                Map< String, List<String>> imap = httpURLConnection.getHeaderFields( ) ;
//                /// 그리고 거길 뒤져서 쿠키를 찾아냄
//                if( imap.containsKey( "Set-Cookie" ) ) {
//                    /// 쿠키를 스트링으로 쫙 저장함
//                    List<String> stringSession = imap.get( "Set-Cookie" ) ;
//                    for( int i = 0 ; i < stringSession.size() ; i++ ) {
//                        sessionID += stringSession.get( i ) ;
//                    }
//                }
//                editor.putString("sessionID", sessionID);
//                editor.commit();
//                Log.i("Login sessionID", String.valueOf(imap));
                //session2

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
            String isLogged= null;
            String nickname= null;
            String type= null;
            String wrongPwd= null;
            String sessionID=null;                                     //session2
            try {
                JSONObject jsonObject = new JSONObject(s);
                isLogged = jsonObject.getString("isLogged");
                if(isLogged.equals("YES")){
                    nickname = jsonObject.getString("nickname");
                    type = jsonObject.getString("type");
                    sessionID = jsonObject.getString("PHPSESSID");  //session2
                    Log.i("loginactivity sessionid", sessionID);    //session2
                } else if(isLogged.equals("NO")){
                    wrongPwd = jsonObject.getString("wrongPwd");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(isLogged.equals("YES")){
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("isLogged", isLogged);
                editor.putString("nickname", nickname);
                editor.putString("type", type);
                editor.putString("sessionID", "PHPSESSID="+sessionID);  //session2
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else if(isLogged.equals("NO") || TextUtils.isEmpty(isLogged)) {
                if(wrongPwd == null){

                }else if(wrongPwd.equals("YES")){
                    // AlertDialog 셋팅
                    alertDialogBuilder.setMessage("아이디 또는 패스워드를 다시 확인해주세요.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();

//                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                    startActivity(intent);
//                    finish();
                }
            }

        }
    } // login AsyncTask End

}
