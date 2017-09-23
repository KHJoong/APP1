package com.together.linkalk;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.CRC32;

/**
 * Created by kimhj on 2017-07-06.
 */

public class MainChatFragment extends Fragment {

    ListView lvRoom;
    ChatList_Adapter clAdapter;
    View header;

    // 채팅방 목록 띄워주는 Thread
    ShowRoom showRoom;
    // 새로운 메시지가 도착하면 채팅방 순서 재정렬해주는 broadcast
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;

    public MainChatFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.together.broadcast.room.integer");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int i = 0;
                if(intent.getAction().equals("com.together.broadcast.room.integer")){
                    i = intent.getIntExtra("reload", 0);
                    if(i == 1){
                        showRoom = new ShowRoom(getActivity().getApplicationContext(), lvRoom, clAdapter, header);
                        showRoom.start();
                    }
                }
            }
        } ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.main_chat_activity, container, false);
        lvRoom = (ListView)layout.findViewById(R.id.lvRoom);
        clAdapter = new ChatList_Adapter(getActivity().getApplicationContext());
        header = getActivity().getLayoutInflater().inflate(R.layout.main_chat_header, null, false);
        lvRoom.setAdapter(clAdapter);

        lvRoom.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Room room = (Room)parent.getItemAtPosition(position);
                AlertDialog.Builder chatRoomExitDialogBuilder = new AlertDialog.Builder(getActivity());
                chatRoomExitDialogBuilder.setTitle("채팅방 삭제");
                chatRoomExitDialogBuilder
                        .setMessage(room.getRoomName()+"을 나가시겠습니까?")
                        .setCancelable(true)
                        .setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ChatRoomExit(room.getRoomNum(), position);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog chatRoomExitDialog = chatRoomExitDialogBuilder.create();
                chatRoomExitDialog.show();
                return true;
            }
        });

        lvRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room room = (Room)parent.getItemAtPosition(position);

                String relation = room.getRoomRelation();
                System.out.println("chatroomclick : "+relation);
                String[] rel = relation.split("/");
                ArrayList<String> list = new ArrayList<String>();
                for(int i=0; i<rel.length; i++){
                    list.add(rel[i]);
                }

                Intent intent = new Intent(getActivity().getApplicationContext(), InChattingActivity.class);
                intent.putStringArrayListExtra("Receiver", list);
                startActivity(intent);
            }
        });

        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pluschat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.plus_chat:
                Intent intent = new Intent(getActivity().getApplicationContext(), ChoiceChatMember.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        showRoom = new ShowRoom(getActivity().getApplicationContext(), lvRoom, clAdapter, header);
        showRoom.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    public void ChatRoomExit(int roomNo, int position){

        int rn = roomNo;
        final int po = position;

        MsgDBHelper dbHelper = new MsgDBHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String deleteMsgQuery = "DELETE FROM chat_msg WHERE roomNo='"+rn+"';";
        db.execSQL(deleteMsgQuery);

        String deleteRoomQuery = "DELETE FROM chat_room WHERE roomNo='"+rn+"';";
        db.execSQL(deleteRoomQuery);

        db.close();
        dbHelper.close();

        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                clAdapter.claItem.remove(po);
                if(clAdapter.claItem.size()==0){
                    lvRoom.addHeaderView(header);
                }
                lvRoom.setAdapter(clAdapter);
            }
        });
    }

    // 저장된 채팅방 처음에 불러오는 쓰레드
    static class ShowRoom extends Thread{
        Context mContext;
        ListView listView;
        ChatList_Adapter cla;
        View head;

        Handler handler = new Handler();

        public ShowRoom(Context context, ListView lv, ChatList_Adapter ad, View h){
            mContext = context;
            listView = lv;
            cla = ad;
            head = h;
        }

        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    cla.claItem.clear();
                    cla.notifyDataSetChanged();

                    MsgDBHelper dbHelper = null;
                    SQLiteDatabase db = null;
                    Cursor c1 = null;

                    dbHelper = new MsgDBHelper(mContext);
                    db = dbHelper.getReadableDatabase();

                    String selectQuery = "SELECT * FROM chat_room ORDER BY ordered ASC;";
                    c1 = db.rawQuery(selectQuery, null);

                    while (c1.moveToNext()){
                        String roomName = "";
                        int numMember = 0;
                        int type = 0;
                        String lastCom = "";
                        String lastComTime = "";
                        int numUnread = 0;

                        int roomNo = c1.getInt(c1.getColumnIndex("roomNo"));
                        String relation = c1.getString(c1.getColumnIndex("relation"));

                        SharedPreferences sp = mContext.getSharedPreferences("maintain", Context.MODE_PRIVATE);
                        String my_nickname = sp.getString("nickname", "");

                        String[] rel = relation.split("/");
                        if((rel.length-1) == 1){
                            numMember = rel.length - 1;
                        } else {
                            numMember = rel.length;
                        }
                        for(int i=0; i<rel.length; i++){
                            if(!rel[i].equals(my_nickname)){
                                if(roomName.equals("")){
                                    roomName = roomName + rel[i];
                                } else {
                                    roomName = roomName + ", " + rel[i];
                                }
                            }
                        }
                        if(roomName.length()>20){
                            roomName = roomName.substring(0, 20) + "...";
                        }

                        String selectQuery2 = "SELECT * FROM chat_msg WHERE roomNo='"+ roomNo +"'";
                        Cursor c2 = db.rawQuery(selectQuery2, null);
                        if(c2.getCount()!=0){
                            int lastMsgNum = c2.getCount();
                            while(c2.moveToNext()){
                                if(c2.getInt(c2.getColumnIndex("msgNo")) == (lastMsgNum-1)){
                                    type = c2.getInt(c2.getColumnIndex("type"));
                                    lastCom = c2.getString(c2.getColumnIndex("transmsg"));
                                    lastComTime = c2.getString(c2.getColumnIndex("time"));
                                }
                            }

                            String selectQuery3 = "SELECT * FROM chat_msg WHERE roomNo='"+roomNo+"' and readed='1'";
                            Cursor c3 = db.rawQuery(selectQuery3, null);
                            numUnread = c3.getCount();

                            if(type ==1){
                                Room room = new Room(roomNo, roomName, relation, numMember, lastCom, lastComTime, numUnread);
                                cla.addItem(room);
                            } else if(type ==2){
                                Room room = new Room(roomNo, roomName, relation, numMember, "(사진)", lastComTime, numUnread);
                                cla.addItem(room);
                            }

                            c2.close();
                            c3.close();
                        }
                    }
                    if(cla.claItem.size()==0){
                        listView.addHeaderView(head, null, false);
                    } else {
                        listView.removeHeaderView(head);
                    }
                    listView.setAdapter(cla);

                    c1.close();
                    db.close();
                    dbHelper.close();
                }
            });
        }
    } // 채팅방 불러오는 Thread End
}
