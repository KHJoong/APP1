package com.together.linkalk;

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
import android.widget.ListView;

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
                                sendBroadcast(intent2);
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
}