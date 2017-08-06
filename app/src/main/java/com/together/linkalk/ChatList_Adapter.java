package com.together.linkalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kimhj on 2017-08-01.
 */

public class ChatList_Adapter extends BaseAdapter {

    Context claContext;

    ArrayList<Room> claItem;

    ViewHolderRoom viewHolder;

    ChatList_Adapter(Context context){
        super();
        claContext = context;
        claItem = new ArrayList<Room>();
    }

    @Override
    public int getCount() {
        return claItem.size();
    }

    @Override
    public Object getItem(int position) {
        return claItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = ((LayoutInflater)claContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatlist_frame, null);
            viewHolder = new ViewHolderRoom();

            viewHolder.roomName = (TextView)view.findViewById(R.id.roomName);
            viewHolder.numMember = (TextView)view.findViewById(R.id.numMember);
            viewHolder.lastCommu = (TextView)view.findViewById(R.id.lastCommu);
            viewHolder.lastCommuTime = (TextView)view.findViewById(R.id.lastCommuTime);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderRoom) view.getTag();
        }

        viewHolder.roomName.setText(claItem.get(position).getRoomName());
        viewHolder.numMember.setText(claItem.get(position).getNumMember());
        viewHolder.lastCommu.setText(claItem.get(position).getLastCommunication());
        viewHolder.lastCommuTime.setText(claItem.get(position).getLastCommunicationTime());

        return view;
    }

    public void addItem(Room room){
        claItem.add(room);
    }
}
