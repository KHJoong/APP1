package com.together.linkalk;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kimhj on 2017-08-24.
 */

public class ChatStart_Adapter extends BaseAdapter {

    Context csContext;

    ArrayList<Member> csItem;

    ViewHolderChoiceChatMember viewHolder;

    ChatStart_Adapter(Context context){
        super();
        csContext = context;
        csItem = new ArrayList<Member>();
    }

    @Override
    public int getCount() {
        return csItem.size();
    }

    @Override
    public Object getItem(int position) {
        return csItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = ((LayoutInflater)csContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chat_mem_frame, null);
            viewHolder = new ViewHolderChoiceChatMember();

            viewHolder.checked = (CheckBox)view.findViewById(R.id.chat_mem_checked);
            viewHolder.nick = (TextView)view.findViewById(R.id.chat_mem_nick);
            viewHolder.lan = (TextView)view.findViewById(R.id.chat_mem_lan);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderChoiceChatMember)view.getTag();
        }

        viewHolder.checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    csItem.get(position).setChecked(true);
                    Log.i("myfriendChecked", csItem.get(position).getNickname() + " : " +String.valueOf(csItem.get(position).getChecked()));
                } else {
                    csItem.get(position).setChecked(false);
                    Log.i("myfriendChecked", csItem.get(position).getNickname() + " : " +String.valueOf(csItem.get(position).getChecked()));
                }
            }
        });
        viewHolder.checked.setChecked(csItem.get(position).getChecked());
        viewHolder.nick.setText(csItem.get(position).getNickname());
        viewHolder.lan.setText(csItem.get(position).getLanguage());

        return view;
    }

    public void addItem(Member member){
        csItem.add(member);
    }
}
