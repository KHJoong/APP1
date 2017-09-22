package com.together.linkalk;

import android.app.ActivityManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kimhj on 2017-08-01.
 */

public class InChattingActivity extends AppCompatActivity {

    ListView lvChat;
    ChatCommunication_Adapter ccAdapter;
    View header;

    TextView header_inviter;
    TextView header_invited;

    TextView tvComment;
    EditText etMsg;
    ImageView btnSend;
    Button btnSendOther;

    MsgDBHelper msgDBHelper;
    String other_nickname = "";
    ArrayList<String> other_nickname_array;
    String my_nickname;
    String my_language;
    String comment;

    Thread sender;
    Thread showMsg;
    IntentFilter imgIntentFilter;
    BroadcastReceiver imgBroadcastReceiver;
    IntentFilter intentFilter2;
    BroadcastReceiver broadcastReceiver2;

    int nextStartMsgPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_chat_room);

        // 메시지 목록 보여줄 리스트뷰 설정
        lvChat = (ListView)findViewById(R.id.lvCommunication);
        lvChat.setDivider(null);
        lvChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        ccAdapter = new ChatCommunication_Adapter(getApplicationContext());

        // 단체 채팅방인 경우 누구누구 초대할 건지 보여주는 텍스트뷰
        tvComment = (TextView)findViewById(R.id.tvComment);
        tvComment.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getSharedPreferences("maintain", MODE_PRIVATE);
        my_nickname = sharedPreferences.getString("nickname", "");
        my_language = sharedPreferences.getString("language", "");

        // 대화 상대방의 닉네임을 받아오는 intent
        Intent intent = getIntent();
        other_nickname_array = intent.getStringArrayListExtra("Receiver");
        if((intent.getIntExtra("comment", 0) ==1) && (other_nickname_array.size()>2)){
            for(int i=0; i<other_nickname_array.size(); i++) {
                if(!my_nickname.equals(other_nickname_array.get(i))){
                    if(i==0 || TextUtils.isEmpty(comment)){
                        comment = other_nickname_array.get(i);
                    } else if((i <= (other_nickname_array.size()-1)) && (i != 0 )){
                        comment = comment + ", " + other_nickname_array.get(i);
                    }
                }
                if(i==(other_nickname_array.size()-1)){
                    comment = comment + " 님을 초대할 예정입니다.";
                }
            }
            lvChat.setVisibility(View.GONE);
            tvComment.setVisibility(View.VISIBLE);
            tvComment.setText(comment);
        }

        // 친구 아닌 사람의 프로필 사진 업데이트
        NotFriendPicUpdate nfpu = new NotFriendPicUpdate(getApplicationContext(), other_nickname_array);
        nfpu.start();

        for(int i=0; i<other_nickname_array.size(); i++){
            if(!other_nickname_array.get(i).equals(my_nickname)){
                if(other_nickname.equals("")){
                    other_nickname = other_nickname + other_nickname_array.get(i);
                } else {
                    other_nickname = other_nickname + ", " + other_nickname_array.get(i);
                }
            }
        }

        // 액션 바에 있는 이름을 대화 상대의 닉네임으로 표시
        getSupportActionBar().setTitle(other_nickname);

        // SQLite 설정
        msgDBHelper = new MsgDBHelper(getApplicationContext());

        // 메시지 입력 & 전송 버튼
        View view = findViewById(R.id.include);
        etMsg = (EditText) view.findViewById(R.id.etSendedMsg);
        btnSend = (ImageView) view.findViewById(R.id.btnSendMsg);
        btnSendOther = (Button)view.findViewById(R.id.btnSendOther);

        // 리스트뷰 클릭하면 메시지 원본<->번역 변경
        lvChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int po, long id) {
                int ct = 0;
                if(other_nickname_array.size()>2){
                    ct = po-1;
                } else {
                    ct = po;
                }
                if(!ccAdapter.ccaItem.get(ct).getSender().equals(my_nickname)){
                    final TextView tv = (TextView)view.findViewById(R.id.tv_my_communi);
                    final Handler handler = new Handler();
                    final int finalCt = ct;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(ccAdapter.ccaItem.get(finalCt).getIsTrans()){
                                ccAdapter.ccaItem.get(finalCt).setIsTrans();
                                tv.setText(ccAdapter.ccaItem.get(finalCt).getMsg());
                            } else {
                                ccAdapter.ccaItem.get(finalCt).setIsTrans();
                                tv.setText(ccAdapter.ccaItem.get(finalCt).getTransmsg());
                            }
                        }
                    });
                }
            }
        });

        // 리스트뷰 헤더 설정
        if(other_nickname_array.size()>2){
            header = getLayoutInflater().inflate(R.layout.chatcommuni_header, null, false);
            header_inviter = (TextView)header.findViewById(R.id.chat_header_inviter);
            header_invited = (TextView)header.findViewById(R.id.chat_header_invited);
            String st_inviter = my_nickname+" 님이";
            String st_invited = "";
            for(int i=0; i<other_nickname_array.size(); i++) {
                if(!my_nickname.equals(other_nickname_array.get(i))){
                    if(i==0 || TextUtils.isEmpty(st_invited)){
                        st_invited = other_nickname_array.get(i);
                    } else if((i <= (other_nickname_array.size()-1)) && (i != 0 )){
                        st_invited = st_invited + ", " + other_nickname_array.get(i);
                    }
                }
                if(i==(other_nickname_array.size()-1)){
                    st_invited = st_invited + " 님을 초대하였습니다.";
                }
            }
            header_inviter.setText(st_inviter);
            header_invited.setText(st_invited);
            lvChat.addHeaderView(header);
        }

        other_nickname = "";
        Collections.sort(other_nickname_array);
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

        // 방 번호 찾는 쿼리
        String query = "SELECT roomNo FROM chat_room WHERE relation='"+other_nickname+"'";
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
//        imgIntentFilter = new IntentFilter();
//        imgIntentFilter.addAction("com.together.broadcast.chat.img");
//        imgBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if(intent.getAction().equals("com.together.broadcast.chat.img")){
//                    SharedPreferences s = getSharedPreferences("chatImg", MODE_PRIVATE);
//                    String path = s.getString("path", "");
//                    JSONObject obj = new JSONObject();
//                    try {
//                        obj.put("sender", my_nickname);
//                        obj.put("receiver", new JSONArray(other_nickname_array));
//                        obj.put("type", "2");
//                        obj.put("msg", path);
//                        obj.put("language", my_language);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    String sdata = obj.toString();
//                    sender = new Thread(new ClientSender(SocketService.socket, SocketService.dos, sdata));
//                    sender.start();
//                }
//            }
//        };

        intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.together.broadcast.chat.integer");
        intentFilter2.addAction("com.together.broadcast.chat.img");
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

                    // InChattingActivity에 있을 때 도착한 메시지가 보고 있는 방의 메시지가 아니면 노티 띄우기
                    String msg_sender = intent.getStringExtra("Receiver");
                    String dis1 = intent.getStringExtra("dis1");
                    String dis2 = intent.getStringExtra("dis2");
                    String msg = intent.getStringExtra("msg");
                    ArrayList<String> receiver_array = intent.getStringArrayListExtra("ReceiverArray");
                    Log.i("notification msg_sender", msg_sender);
                    Log.i("notification msg", msg);

                    // 방 번호 찾는 쿼리
                    String query = "SELECT * FROM chat_room WHERE relation='"+other_nickname+"'";
                    SQLiteDatabase db = msgDBHelper.getReadableDatabase();
                    Cursor c = db.rawQuery(query, null);
                    String rela = "";
                    if(c.moveToFirst()){
                        rela = c.getString(c.getColumnIndex("relation"));
                    }
                    query = "SELECT * FROM chat_room WHERE relation='"+dis1+"' or relation='"+dis2+"'";
                    c = db.rawQuery(query, null);
                    int rn = 0;
                    if(c.moveToFirst()){
                        rn =  c.getInt(c.getColumnIndex("roomNo"));
                    }
                    c.close();
                    db.close();
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
                } else if(intent.getAction().equals("com.together.broadcast.chat.img")) {
                    // 사진 전송 시 받는 브로드캐스트
                    SharedPreferences s = getSharedPreferences("chatImg", MODE_PRIVATE);
                    String path = s.getString("path", "");
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("sender", my_nickname);
                        obj.put("receiver", new JSONArray(other_nickname_array));
                        obj.put("type", "2");
                        obj.put("msg", path);
                        obj.put("language", my_language);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String sdata = obj.toString();
                    sender = new Thread(new ClientSender(SocketService.socket, SocketService.dos, sdata));
                    sender.start();
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
                        obj.put("type", "1");
                        obj.put("msg", etMsg.getText().toString());
                        obj.put("language", my_language);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String sdata = obj.toString();
                    sender = new Thread(new ClientSender(SocketService.socket, SocketService.dos, sdata));
                    sender.start();

                    etMsg.setText(null);
                    lvChat.setVisibility(View.VISIBLE);
                    tvComment.setVisibility(View.GONE);
                }
            }
        });

        // 메시지 왜 다른 거 추가하는 버튼
        btnSendOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DialogInChattingActivityOtherSend.class);
                startActivity(intent);
            }
        });

    }   // onCreate 끝

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dialoginchat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.dialoginchat:
                Intent intent = new Intent(getApplicationContext(), DialogInChattingActivity.class);
                intent.putExtra("relation", other_nickname);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        String com = taskInfo.get(0).topActivity.getClassName();
        Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        if(!(com.equals("com.sec.android.app.camera.Camera")
                || com.equals("com.sec.android.gallery3d.app.Gallery")
                || com.equals("com.sec.android.gallery3d.app.CropImage")
                || com.equals("com.together.linkalk.PhotoFilterActivity2"))){
            finish();
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
        Context mContext;
        String my_nick;
        String dis;
        ListView listView;
        ChatCommunication_Adapter cca;

        Handler handler = new Handler();
        MsgDBHelper msgDBHelper;

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
                    msgDBHelper = new MsgDBHelper(mContext);
                    msgDBHelper.selectMsg(my_nick, dis, listView, cca);
                    listView.setAdapter(cca);
                    listView.setSelection(cca.getCount()-1);

                    // Listview 저장 메시지 불러오는 페이징
                    nextStartMsgPosition = 15;
                    lvChat.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {

                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            if(firstVisibleItem<5 && firstVisibleItem!=0){
                                SQLiteDatabase db = msgDBHelper.getReadableDatabase();
                                String qu = "SELECT roomNo FROM chat_room WHERE relation='"+other_nickname+"'";
                                Cursor cursor = db.rawQuery(qu, null);
                                int rn = 0;
                                if(cursor.moveToFirst()){
                                    rn =  cursor.getInt(0);
                                }
                                cursor.close();

                                qu = "SELECT msgNo FROM chat_msg WHERE roomNo='"+rn+"'";
                                cursor = db.rawQuery(qu, null);
                                int mn = cursor.getCount();
                                cursor.close();

                                if(nextStartMsgPosition<=mn){
                                    int ct = 0;
                                    qu = "SELECT * FROM chat_msg WHERE roomNo='"+rn+"' ORDER BY msgNo DESC LIMIT 15 OFFSET "+nextStartMsgPosition+"";
                                    cursor = db.rawQuery(qu, null);
                                    while(cursor.moveToNext()){
                                        final String sender = cursor.getString(2);
                                        final String msg = cursor.getString(3);
                                        final String transmsg = cursor.getString(4);
                                        final String time = cursor.getString(5);

                                        String nick="";
                                        String imgpath="";
                                        if(!sender.equals(my_nickname)){
                                            for(int i=0; ; i++) {
                                                SharedPreferences myFriendShared = getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
                                                if (myFriendShared.contains(String.valueOf(i))) {
                                                    String myFriend_tmp = myFriendShared.getString(String.valueOf(i), "");
                                                    try {
                                                        JSONObject friendObject = new JSONObject(myFriend_tmp);
                                                        nick = friendObject.getString("nickname");
                                                        if(sender.equals(nick)){
                                                            imgpath = friendObject.getString("imgpath");
                                                            break;
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                        Chat chat = new Chat(sender, other_nickname, msg, transmsg, time, 1, imgpath);
                                        ccAdapter.add(chat);
                                        ct = ct+1;


//                                        Chat chat = new Chat(sender, other_nickname, msg, transmsg, time, 1);
//                                        ccAdapter.add(chat);
//                                        ct = ct+1;
                                    }
                                    ccAdapter.notifyDataSetChanged();
                                    int set = ct + firstVisibleItem;
                                    view.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                                    view.setSelection(set);
                                    cursor.close();
                                    db.close();
                                    nextStartMsgPosition = nextStartMsgPosition+15;
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    // 친구 아닌 애들 프사 업뎃됬는지 다시 받아오기
    class NotFriendPicUpdate extends Thread{
        Context mContext;
        ArrayList<String> other_nickname_array;
        ArrayList<String> tmp_nickname_array;
        String send;
        String getdata;

        NotFriendPicUpdate(Context con, ArrayList<String> other_nick_array){
            mContext = con;
            other_nickname_array = other_nick_array;
            tmp_nickname_array = new ArrayList<String>();
            send = "";
        }

        @Override
        public void run() {
            super.run();
            SharedPreferences shPreferences = mContext.getSharedPreferences("tmpFriendPicPath", Context.MODE_PRIVATE);
            for(int i=0; i<other_nickname_array.size(); i++){
                if(!TextUtils.isEmpty(shPreferences.getString(other_nickname_array.get(i), ""))){
                    if(TextUtils.isEmpty(send)){
                        send = other_nickname_array.get(i);
                        tmp_nickname_array.add(other_nickname_array.get(i));
                    } else {
                        send = send + other_nickname_array.get(i);
                        tmp_nickname_array.add(other_nickname_array.get(i));
                    }
                }
            }

            if(!TextUtils.isEmpty(send)){
                try {

                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("maintain", Context.MODE_PRIVATE);
                    String sessionID = sharedPreferences.getString("sessionID", "");

                    URL url = new URL("http://www.o-ddang.com/linkalk/updateNotFriendPic.php");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setDefaultUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("accept-charset", "UTF-8");
                    conn.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=utf-8");

                    conn.setInstanceFollowRedirects( false );
                    if(!TextUtils.isEmpty(sessionID)) {
                        conn.setRequestProperty( "cookie", sessionID) ;
                    }

                    String signal = URLEncoder.encode("nickname", "UTF-8")+"="+URLEncoder.encode(send, "UTF-8");
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                    osw.write(signal);
                    osw.flush();
                    osw.close();

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    if(serverResponseCode == 200){

                    }

                    BufferedReader rd = null;

                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = rd.readLine()) != null){
                        sb.append(line);
                    }
                    rd.close();
                    getdata = sb.toString().trim();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    JSONObject ob = new JSONObject(getdata);
                    SharedPreferences.Editor ed = shPreferences.edit();
                    for(int i=0; i<tmp_nickname_array.size(); i++){
                        ed.putString(tmp_nickname_array.get(i), ob.getString(tmp_nickname_array.get(i)));
                    }
                    ed.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}