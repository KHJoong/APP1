package com.together.linkalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

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

    Socket socket;
    DataOutputStream dos;
    static DataInputStream in;
    String ip = "115.71.232.230"; // IP
    int port = 9999; // PORT번호

    ListView lvChat;
    ChatCommunication_Adapter ccAdapter;

    EditText etMsg;
    ImageView btnSend;

    MsgDBHelper msgDBHelper;
    String other_nickname;
    String my_nickname;
    Thread sender;
    Thread receiver;

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

        // 메시지 수신 쓰레드 실행
        receiver = new Thread(new ClientReceiver(getApplicationContext(), ccAdapter, lvChat));

        // 서버 소켓 연결 쓰레드
        socket = new Socket();
        if(!socket.isConnected()){
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

                        if(in == null)
                            System.out.println("disChatnull" + in);
                        else
                            System.out.println("disChatnotnull" + in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Thread showMsg = new Thread(new ShowMsg(getApplicationContext(), my_nickname, other_nickname, lvChat, ccAdapter));
        showMsg.start();

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
                sender = new Thread(new ClientSender(socket, dos, sdata));
                sender.start();

                etMsg.setText(null);
            }
        });

    }   // onCreate 끝

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        receiver.interrupt();
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

    // 메시지 받는 쓰레드
    static class ClientReceiver extends Thread{
        Context mContext;

        ChatCommunication_Adapter chatCommunication_adapter;
        ListView lvChat;

        Handler handler = new Handler();

        public ClientReceiver(Context context, ChatCommunication_Adapter adapter, ListView listView) {
            this.mContext = context;
            this.chatCommunication_adapter = adapter;
            this.lvChat = listView;
        }

        public void run() {
            // in 이 비어있는지 확인합니다.
            if(in == null)
                System.out.println("inChatnull" + in);
            else
                System.out.println("inChatnotnull" + in);

            while(in != null) {//서버에서 받은 데이터를 뽑아냅니다.
                try {
                    final String getString = in.readUTF();    //1
                    System.out.println("settttt" + getString);
                    if(!TextUtils.isEmpty(getString)){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
                                String time = sdfNow.format(new Date(System.currentTimeMillis()));
                                String sender = "";
                                String receiver = "";
                                String msg = "비어있음";
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

                                System.out.println("in chat sender" + sender);
                                System.out.println("in chat receiver" + receiver);
                                System.out.println("in chat msg" + msg);
                                Chat chat = new Chat(sender, receiver, msg, time, sync);
                                chatCommunication_adapter.addItem(chat);
                                lvChat.setAdapter(chatCommunication_adapter);
                                lvChat.setSelection(chatCommunication_adapter.getCount()-1);

                                String dis1 = sender+ "/" +receiver;
                                String dis2 = receiver + "/" + sender;

                                MsgDBHelper msgDBHelper = new MsgDBHelper(mContext);
                                msgDBHelper.insertMsg(dis1, dis2, sender, msg, time, 1, 1);
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

    // 저장된 메시지 처음에 불러오는 쓰레드
    static class ShowMsg extends Thread{
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
