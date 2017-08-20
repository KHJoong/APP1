package com.together.linkalk;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by kimhj on 2017-08-16.
 */

public class SocketService extends Service{

    String ip = "115.71.232.230"; // IP
    int port = 9999; // PORT번호

    static Socket socket;
    Thread receiver;
    static DataOutputStream dos;
    static DataInputStream in;

    String my_nickname;

    MsgDBHelper msgDBHelper;
    String dis1;
    String dis2;
    String sender;
    String msg;
    String time;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 메시지 수신 쓰레드 실행
        receiver = new Thread(new MsgReceiver(getApplicationContext()));

        // 토큰 업데이트 쓰레드 실행
        UpdateMyToken umt = new UpdateMyToken();
        umt.execute();

        // 소켓 연결 안되었을 때 저장된 메시지 받아오는 asynctask 실행
        GetTmpMsg getTmpMsg = new GetTmpMsg(getApplicationContext());
        getTmpMsg.execute();

        // 새로운 소켓 선언
        socket = new Socket();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 서버에 연결할 때 보낼 내 닉네임
                SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
                my_nickname = sharedPreferences.getString("nickname", "");

                try {
                    SocketAddress sock_addr = new InetSocketAddress(ip, port);
                    socket.connect(sock_addr);

                    dos = new DataOutputStream(socket.getOutputStream());
                    in = new DataInputStream(socket.getInputStream());
                    dos.writeUTF(my_nickname);
                    dos.flush();

                    receiver.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        receiver.interrupt();
        try {
            dos.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메시지 받는 쓰레드
    class MsgReceiver extends Thread{
        Context mContext;

        Handler handler = new Handler();

        public MsgReceiver(Context context) {
            this.mContext = context;
        }

        public void run() {
            while(in != null) {//서버에서 받은 데이터를 뽑아냅니다.
                try {
                    final String getString = in.readUTF();    //1
                    System.out.println("settttt" + getString);
                    if(!TextUtils.isEmpty(getString)){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
                                time = sdfNow.format(new Date(System.currentTimeMillis()));
                                sender = "";
                                String receiver = "";
                                msg = "비어있음";
                                int sync = 1;
                                try {
                                    JSONObject obj = new JSONObject(getString);
                                    sender = obj.getString("sender");
                                    receiver = obj.getString("receiver");
                                    msg = obj.getString("msg");
                                    sync = 1;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                dis1 = sender+ "/" +receiver;
                                dis2 = receiver + "/" + sender;

                                msgDBHelper = new MsgDBHelper(mContext);
//                                msgDBHelper.insertMsg(dis1, dis2, sender, msg, time, 1, 1);

                                // 메시지가 왔는데 기존에 없는 방일 경우
                                String checkExistRoom = "SELECT * FROM chat_room WHERE relation = '"+dis1+"' or relation = '"+dis2+"';";
                                SQLiteDatabase db = msgDBHelper.getReadableDatabase();
                                Cursor c = db.rawQuery(checkExistRoom, null);
                                int existRoom = c.getCount();
                                if(existRoom==0){
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("sender", sender);
                                        jsonObject.put("receiver", receiver);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    // 채팅방 DB 생성하기
                                    GetChatRoom gcr = new GetChatRoom();
                                    gcr.execute(jsonObject.toString());
                                } else {
                                    msgDBHelper.insertMsg(dis1, dis2, sender, msg, time, 1, 1);

                                    // 새로운 메시지가 추가됐음을 알리기 위한 Broadcast
                                    // 이 Broadcast를 받아서 채팅방의 순서를 재정렬함
                                    // MainChatFragment
                                    Intent intent = new Intent();
                                    intent.setAction("com.together.broadcast.room.integer");
                                    intent.putExtra("reload", 1);
                                    sendBroadcast(intent);
                                }

                                // 새로운 메시지가 추가됐음을 알리기 위한 Broadcast
                                // 이 Broadcast를 받아서 새로 온 메시지를 리스트에 추가함
                                // InChattingActivity
                                Intent intent2 = new Intent();
                                intent2.setAction("com.together.broadcast.chat.integer");
                                intent2.putExtra("plus", 1);
                                intent2.putExtra("msg", msg);
                                intent2.putExtra("Receiver", sender);
                                sendBroadcast(intent2);

                                // 현재 보여주고 있는 최상위 Activity가 뭔지 출력해주는 부분/ 채팅방에 들어가있는 상태가 아니면 노티 띄워줌
                                // com.together.linkalk.InChattingActivity
                                ActivityManager am = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
                                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                                Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                                // 방 번호 찾는 쿼리
                                String query = "SELECT roomNo FROM chat_room WHERE relation='"+dis1+"' OR relation='"+dis2+"'";
                                db = msgDBHelper.getReadableDatabase();
                                c = db.rawQuery(query, null);
                                int rn = 0;
                                if(c.moveToFirst()){
                                    rn =  c.getInt(c.getColumnIndex("roomNo"));
                                }
                                if(!taskInfo.get(0).topActivity.getClassName().equals("com.together.linkalk.InChattingActivity")){
                                    NotificationManager notificationManager= (NotificationManager)mContext.getSystemService(NOTIFICATION_SERVICE);
                                    Intent intent = new Intent(mContext, InChattingActivity.class);
                                    intent.putExtra("Receiver", sender);
                                    Notification.Builder builder = new Notification.Builder(mContext);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int)System.currentTimeMillis()/1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    builder.setSmallIcon(R.mipmap.ic_launcher_round)
                                            .setTicker("Linkalk")
                                            .setWhen(System.currentTimeMillis())
                                            .setContentTitle(sender).setContentText(msg)
                                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true)
                                            .setOngoing(false);
                                    notificationManager.notify(rn, builder.build());
                                }
                                c.close();
                                db.close();
                            }
                        });
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }   // client receiver end

    // 상대방이 말을 걸었을 경우 채팅방 번호 받아서 추가하는 Asynctask
    class GetChatRoom extends AsyncTask<String, Void, String> {

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

            msgDBHelper.insertMsg(dis1, dis2, sender, msg, time, 1, 1);

            // 새로운 메시지가 추가됐음을 알리기 위한 Broadcast
            // 이 Broadcast를 받아서 채팅방의 순서를 재정렬함
            // MainChatFragment
            Intent intent = new Intent();
            intent.setAction("com.together.broadcast.room.integer");
            intent.putExtra("reload", 1);
            sendBroadcast(intent);
        }
    }   // 상대방이 먼저 대화 건 채팅방 생성 Asynctask

    // 혹시 바꼈을 지 모르는 Token 값을 서버에 업데이트 하는 Asynctask
    class UpdateMyToken extends AsyncTask<Void, Void, Void> {

        JSONObject object = new JSONObject();
        String sessionID;
        String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SharedPreferences sharedPreferences = getSharedPreferences("maintain", Context.MODE_PRIVATE);
            sessionID = sharedPreferences.getString("sessionID", "");

            try {
                object.put("token", FirebaseInstanceId.getInstance().getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            token = object.toString();
        } // onPreExecute 끝

        @Override
        protected Void doInBackground(Void... params) {
            try{
                // 서버에 개인 프로필을 업데이트하기 위해 요청하는 부분
                URL url = new URL("http://www.o-ddang.com/linkalk/updateMyToken.php");
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
                os.write(token.getBytes());
                os.flush();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }   // onDoing 끝
    }   // My Token Update 끝

    // 소켓 연결이 끊겨서 받지 못했던 메시지를 받아오는 Asynctask
    class GetTmpMsg extends AsyncTask<Void, Void, String> {

        Context mContext;

        JSONObject object = new JSONObject();
        String sessionID;
        String mynickname;

        public GetTmpMsg(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SharedPreferences sharedPreferences = getSharedPreferences("maintain", Context.MODE_PRIVATE);
            sessionID = sharedPreferences.getString("sessionID", "");
            mynickname = sharedPreferences.getString("nickname", "");

        } // onPreExecute 끝

        @Override
        protected String doInBackground(Void... params) {
            try{
                String getData = "1";
                object.put("request", getData);
                object.put("nickname", mynickname);

                URL url = new URL("http://www.o-ddang.com/linkalk/getTmpMsg.php");
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
                os.write(object.toString().getBytes());
                os.flush();

                // 서버 리턴
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
        }   // onDoing 끝

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            Handler handler = new Handler();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        int n;

                        if(!TextUtils.isEmpty(s)){
                            JSONObject object1 = new JSONObject(s);
                            JSONArray array = object1.getJSONArray("tmpmsg");

                            for(n=0; n<array.length(); n++) {
                                String s_obj = array.getString(n);
                                JSONObject obj2 = new JSONObject(s_obj);

                                String msgJson = obj2.getString("message");
                                time = obj2.getString("time");
                                JSONObject obj3 = new JSONObject(msgJson);
                                sender = obj3.getString("sender");
                                String receiver = obj3.getString("receiver");
                                msg = obj3.getString("msg");

                                dis1 = sender+ "/" +receiver;
                                dis2 = receiver + "/" + sender;

                                msgDBHelper = new MsgDBHelper(mContext);

                                // 메시지가 왔는데 기존에 없는 방일 경우
                                String checkExistRoom = "SELECT * FROM chat_room WHERE relation = '"+dis1+"' or relation = '"+dis2+"';";
                                SQLiteDatabase db = msgDBHelper.getReadableDatabase();
                                Cursor c = db.rawQuery(checkExistRoom, null);
                                int existRoom = c.getCount();
                                if(existRoom==0){
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("sender", sender);
                                        jsonObject.put("receiver", receiver);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    // 채팅방 DB 생성하기
                                    GetChatRoom gcr = new GetChatRoom();
                                    gcr.execute(jsonObject.toString());
                                } else {
                                    msgDBHelper.insertMsg(dis1, dis2, sender, msg, time, 1, 1);

                                    // 새로운 메시지가 추가됐음을 알리기 위한 Broadcast
                                    // 이 Broadcast를 받아서 채팅방의 순서를 재정렬함
                                    // MainChatFragment
                                    Intent intent = new Intent();
                                    intent.setAction("com.together.broadcast.room.integer");
                                    intent.putExtra("reload", 1);
                                    sendBroadcast(intent);
                                }

                                // 새로운 메시지가 추가됐음을 알리기 위한 Broadcast
                                // 이 Broadcast를 받아서 새로 온 메시지를 리스트에 추가함
                                // InChattingActivity
                                Intent intent2 = new Intent();
                                intent2.setAction("com.together.broadcast.chat.integer");
                                intent2.putExtra("plus", 1);
                                intent2.putExtra("msg", msg);
                                intent2.putExtra("Receiver", sender);
                                sendBroadcast(intent2);

                                // 현재 보여주고 있는 최상위 Activity가 뭔지 출력해주는 부분
                                // com.together.linkalk.InChattingActivity
                                ActivityManager am = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
                                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                                Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                                // 방 번호 찾는 쿼리
                                String query = "SELECT roomNo FROM chat_room WHERE relation='"+dis1+"' OR relation='"+dis2+"'";
                                db = msgDBHelper.getReadableDatabase();
                                c = db.rawQuery(query, null);
                                int rn = 0;
                                if(c.moveToFirst()){
                                    rn =  c.getInt(c.getColumnIndex("roomNo"));
                                }
                                if(!taskInfo.get(0).topActivity.getClassName().equals("com.together.linkalk.InChattingActivity")){
                                    NotificationManager notificationManager= (NotificationManager)mContext.getSystemService(NOTIFICATION_SERVICE);
                                    Intent intent = new Intent(mContext, InChattingActivity.class);
                                    intent.putExtra("Receiver", sender);
                                    Notification.Builder builder = new Notification.Builder(mContext);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int)System.currentTimeMillis()/1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    builder.setSmallIcon(R.mipmap.ic_launcher_round)
                                            .setTicker("Linkalk")
                                            .setWhen(System.currentTimeMillis())
                                            .setContentTitle(sender).setContentText(msg)
                                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true)
                                            .setOngoing(false);
                                    notificationManager.notify(rn, builder.build());
                                }
                                c.close();
                                db.close();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

    }   // GetTmpMsg 끝

}
