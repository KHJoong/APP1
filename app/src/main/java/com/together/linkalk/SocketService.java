package com.together.linkalk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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

                                String dis1 = sender+ "/" +receiver;
                                String dis2 = receiver + "/" + sender;

                                MsgDBHelper msgDBHelper = new MsgDBHelper(mContext);
                                msgDBHelper.insertMsg(dis1, dis2, sender, msg, time, 1, 1);

                                // 새로운 메시지가 추가됐음을 알리기 위한 Broadcast
                                // 이 Broadcast를 받아서 채팅방의 순서를 재정렬함
                                Intent intent = new Intent();
                                intent.setAction("com.together.broadcast.room.integer");
                                intent.putExtra("reload", 1);
                                sendBroadcast(intent);

                                // 새로운 메시지가 추가됐음을 알리기 위한 Broadcast
                                // 이 Broadcast를 받아서 새로 온 메시지를 리스트에 추가함
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
}
