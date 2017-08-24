package com.together.linkalk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by kimhj on 2017-08-01.
 */

public class InChattingActivity extends AppCompatActivity {

    ListView lvChat;
    ChatCommunication_Adapter ccAdapter;

    EditText etMsg;
    ImageView btnSend;

    MsgDBHelper msgDBHelper;
    String other_nickname = "";
    ArrayList<String> other_nickname_array;   // test
    String my_nickname;
    String my_language;
    Thread sender;
    Thread showMsg;
    IntentFilter intentFilter2;
    BroadcastReceiver broadcastReceiver2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_chat_room);

        SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
        my_nickname = sharedPreferences.getString("nickname", "");
        my_language = sharedPreferences.getString("language", "");

        // 대화 상대방의 닉네임을 받아오는 intent
        Intent intent = getIntent();
        // --------------- test ---------------
        other_nickname_array = intent.getStringArrayListExtra("Receiver");
        Collections.sort(other_nickname_array, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        for(int i=0; i<other_nickname_array.size(); i++){
            if(!other_nickname_array.get(i).equals(my_nickname)){
                if(other_nickname.equals("")){
                    other_nickname = other_nickname + other_nickname_array.get(i);
                } else {
                    other_nickname = other_nickname + ", " + other_nickname_array.get(i);
                }
            }
        }
        // --------------- test ---------------
//        if(!TextUtils.isEmpty(intent.getStringExtra("Receiver"))){
//            other_nickname = intent.getStringExtra("Receiver");
//        }

        // 액션 바에 있는 이름을 대화 상대의 닉네임으로 표시
        getSupportActionBar().setTitle(other_nickname);

        // SQLite 설정
        msgDBHelper = new MsgDBHelper(getApplicationContext());

        // 메시지 입력 & 전송 버튼
        View view = findViewById(R.id.include);
        etMsg = (EditText) view.findViewById(R.id.etSendedMsg);
        btnSend = (ImageView) view.findViewById(R.id.btnSendMsg);

        // 메시지 목록 보여줄 리스트뷰 설정
        lvChat = (ListView)findViewById(R.id.lvCommunication);
        lvChat.setDivider(null);
        lvChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        ccAdapter = new ChatCommunication_Adapter(getApplicationContext());

        // --------------- test ---------------
        Collections.sort(other_nickname_array, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        other_nickname = "";
        for(int i=0; i<other_nickname_array.size(); i++){
            if(other_nickname.equals("")){
                other_nickname = other_nickname + other_nickname_array.get(i);
            } else {
                other_nickname = other_nickname + "/" + other_nickname_array.get(i);
            }
        }
        // 저장된 메시지 불러오는 Thread 실행
        showMsg = new Thread(new ShowMsg(getApplicationContext(), my_nickname, other_nickname, lvChat, ccAdapter));
        showMsg.start();
        // --------------- test ---------------

        // 저장된 메시지 불러오는 Thread 실행
//        showMsg = new Thread(new ShowMsg(getApplicationContext(), my_nickname, other_nickname, lvChat, ccAdapter));
//        showMsg.start();

        // 방 번호 찾는 쿼리
        // --------------- test ---------------
        String query = "SELECT roomNo FROM chat_room WHERE relation='"+other_nickname+"'";
        // --------------- test ---------------
//        String dis1 = my_nickname+"/"+other_nickname;
//        String dis2 = other_nickname+"/"+my_nickname;
//        String query = "SELECT roomNo FROM chat_room WHERE relation='"+dis1+"' OR relation='"+dis2+"'";
        SQLiteDatabase db = msgDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        int rn = 0;
        if(c.moveToFirst()){
            rn =  c.getInt(c.getColumnIndex("roomNo"));
        }
        c.close();
        db.close();
        // notification 매니저 생성
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 등록된 notification 을 제거 한다.
        nm.cancel(rn);

        final Handler handler = new Handler();
        intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.together.broadcast.chat.integer");
        broadcastReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int i = 0;
                if(intent.getAction().equals("com.together.broadcast.chat.integer")){
                    i = intent.getIntExtra("plus", 0);
                    if(i == 1){
                        MsgDBHelper msgDBHelper = new MsgDBHelper(getApplicationContext());
                        msgDBHelper.continueSelectMsg(handler, my_nickname, other_nickname, lvChat, ccAdapter);
                    }
                }

//                String dis1 = my_nickname+ "/" +other_nickname;
//                String dis2 = other_nickname + "/" + my_nickname;

                // InChattingActivity에 있을 때 도착한 메시지가 보고 있는 방의 메시지가 아니면 노티 띄우기
                String msg_sender = intent.getStringExtra("Receiver");
                String dis1 = intent.getStringExtra("dis1");
                String dis2 = intent.getStringExtra("dis2");
                String msg = intent.getStringExtra("msg");
                ArrayList<String> receiver_array = intent.getStringArrayListExtra("ReceiverArray");
                Log.i("notification msg_sender", msg_sender);
                Log.i("notification msg", msg);

                // 방 번호 찾는 쿼리
                // --------------- test ---------------
                String query = "SELECT * FROM chat_room WHERE relation='"+other_nickname+"'";
                // --------------- test ---------------
//                String query = "SELECT roomNo FROM chat_room WHERE relation='"+dis1+"' OR relation='"+dis2+"'";
                SQLiteDatabase db = msgDBHelper.getReadableDatabase();
                Cursor c = db.rawQuery(query, null);
                int rn = 0;
                String rela = "";
                if(c.moveToFirst()){
                    rn =  c.getInt(c.getColumnIndex("roomNo"));
                    rela = c.getString(c.getColumnIndex("relation"));
                }
                c.close();
                db.close();
//                if(!msg_sender.equals(my_nickname) && !other_nickname_array.contains(msg_sender)){
                if(!msg_sender.equals(my_nickname) && (!rela.equals(dis1) && !rela.equals(dis2))){
                    NotificationManager notificationManager= (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                    Intent noti_intent = new Intent(getApplicationContext(), InChattingActivity.class);
                    noti_intent.putStringArrayListExtra("Receiver", receiver_array);
                    noti_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Notification.Builder builder = new Notification.Builder(getApplicationContext());
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int)System.currentTimeMillis()/1000, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setSmallIcon(R.mipmap.ic_launcher_round)
                            .setTicker("Linkalk")
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle(msg_sender).setContentText(msg)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setOngoing(false);
                    notificationManager.notify(rn, builder.build());
                }
            }
        } ;

        // 메시지 전송 버튼
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(etMsg.getText().toString())){
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("sender", my_nickname);
                        obj.put("receiver", new JSONArray(other_nickname_array));
                        obj.put("msg", etMsg.getText().toString());
                        obj.put("language", my_language);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String sdata = obj.toString();
                    sender = new Thread(new ClientSender(SocketService.socket, SocketService.dos, sdata));
                    sender.start();

                    etMsg.setText(null);
                }
            }
        });

    }   // onCreate 끝

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver2, intentFilter2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver2);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!showMsg.isInterrupted()){
            showMsg.interrupt();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("page", 2);
        startActivity(intent);
        finish();
    }

    // 메시지 보내는 쓰레드
    static class ClientSender extends Thread{
        private Socket sockett;
        private DataOutputStream out;
        private String send_data_tmp;
        private byte[] send_data;

        // sdata 는 보낼 메시지
        public ClientSender(Socket socket, DataOutputStream dataOutputStream, String sdata) {
            send_data_tmp = sdata;
            out = dataOutputStream;
            this.sockett = socket;
        }

        // 보낼 메시지를 바이트로 변환하여 서버로 전송하는 run
        public void run() {
            BufferedReader br = null;
            try {
                send_data = send_data_tmp.getBytes();
                InputStream inputStream = new ByteArrayInputStream(send_data);
                br = new BufferedReader(new InputStreamReader(inputStream));

                //서버쪽으로 보내겠다는 이야기임.
                String line;
                while((line = br.readLine()) != null && sockett.isConnected()) {//지속적으로 널이 아닐때 사용자의 입력을 보냅니다.
                    out.writeUTF(line);
                }
                send_data = null;
            } catch(IOException e) {
//                e.printStackTrace();
            } finally {
                if (br != null) try { br.close(); } catch(IOException e) {}
            }
        }
    }   // client sender end

    // 저장된 메시지 처음에 불러오는 쓰레드
    class ShowMsg extends Thread{
        // --------------- test ---------------
        Context mContext;
        String my_nick;
        String dis;
        ListView listView;
        ChatCommunication_Adapter cca;

        Handler handler = new Handler();

        public ShowMsg(Context context, String my, String dist, ListView lv, ChatCommunication_Adapter ad){
            mContext = context;
            my_nick = my;
            dis = dist;
            listView = lv;
            cca = ad;
        }

        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MsgDBHelper msgDBHelper = new MsgDBHelper(mContext);
                    msgDBHelper.selectMsg(my_nick, dis, listView, cca);
                    listView.setAdapter(cca);
                    listView.setSelection(cca.getCount()-1);
                }
            });
        }
        // --------------- test ---------------

//        Context mContext;
//        String peo1;
//        String peo2;
//        ListView listView;
//        ChatCommunication_Adapter cca;
//
//        Handler handler = new Handler();
//
//        public ShowMsg(Context context, String p1, String p2, ListView lv, ChatCommunication_Adapter ad){
//            mContext = context;
//            peo1 = p1;
//            peo2 = p2;
//            listView = lv;
//            cca = ad;
//        }
//
//        public void run() {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    MsgDBHelper msgDBHelper = new MsgDBHelper(mContext);
//                    msgDBHelper.selectMsg(peo1, peo2, listView, cca);
//                    listView.setAdapter(cca);
//                    listView.setSelection(cca.getCount()-1);
//                }
//            });
//        }
    }

}