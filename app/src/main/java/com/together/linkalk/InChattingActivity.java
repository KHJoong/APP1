package com.together.linkalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

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
    String other_nickname;
    String my_nickname;
    Thread sender;
    Thread showMsg;
    IntentFilter intentFilter2;
    BroadcastReceiver broadcastReceiver2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_chat_room);

        // 대화 상대방의 닉네임을 받아오는 intent
        Intent intent = getIntent();
        if(!TextUtils.isEmpty(intent.getStringExtra("Receiver"))){
            other_nickname = intent.getStringExtra("Receiver");
        }

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

        SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
        my_nickname = sharedPreferences.getString("nickname", "");

        // 저장된 메시지 불러오는 Thread 실행
        showMsg = new Thread(new ShowMsg(getApplicationContext(), my_nickname, other_nickname, lvChat, ccAdapter));
        showMsg.start();

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
            }
        } ;

        // 메시지 전송 버튼
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("sender", my_nickname);
                    obj.put("receiver", other_nickname);
                    obj.put("msg", etMsg.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String sdata = obj.toString();
                sender = new Thread(new ClientSender(SocketService.socket, SocketService.dos, sdata));
                sender.start();

                etMsg.setText(null);
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

        // name 은 아이디 sdata 는 보낼 메시지
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
        Context mContext;
        String peo1;
        String peo2;
        ListView listView;
        ChatCommunication_Adapter cca;

        Handler handler = new Handler();

        public ShowMsg(Context context, String p1, String p2, ListView lv, ChatCommunication_Adapter ad){
            mContext = context;
            peo1 = p1;
            peo2 = p2;
            listView = lv;
            cca = ad;
        }

        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MsgDBHelper msgDBHelper = new MsgDBHelper(mContext);
                    msgDBHelper.selectMsg(peo1, peo2, listView, cca);
                    listView.setAdapter(cca);
                    listView.setSelection(cca.getCount()-1);
                }
            });
        }
    }

}