package com.together.linkalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kimhj on 2017-08-01.
 */

public class ChatCommunication_Adapter extends BaseAdapter {

    Context ccaContext;

    ArrayList<Chat> ccaItem;

    ViewHolderChat viewHolder;

    ChatCommunication_Adapter(Context context){
        super();
        ccaContext = context;
        ccaItem = new ArrayList<Chat>();
    }

    @Override
    public int getCount() {
        return ccaItem.size();
    }

    @Override
    public Object getItem(int position) {
        return ccaItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String sender = ccaItem.get(position).getSender();
        SharedPreferences sharedPreferences = ccaContext.getSharedPreferences("maintain", ccaContext.MODE_PRIVATE);
        String nickname = sharedPreferences.getString("nickname", "");

        if(view == null){
//            if(sender.equals(nickname)){
//                view = ((LayoutInflater)ccaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatcommuni_me_frame, null);
//                viewHolder = new ViewHolderChat();
//
//                viewHolder.sender = (TextView)view.findViewById(R.id.tv_my_nick);
//                viewHolder.msg = (TextView)view.findViewById(R.id.tv_my_communi);
//                viewHolder.time = (TextView)view.findViewById(R.id.tv_my_time);
//            } else if(!sender.equals(nickname)){
//                view = ((LayoutInflater)ccaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatcommuni_other_frame, null);
//                viewHolder = new ViewHolderChat();
//
//                viewHolder.ot_sender = (TextView)view.findViewById(R.id.tv_other_nick);
//                viewHolder.ot_msg = (TextView)view.findViewById(R.id.tv_other_communi);
//                viewHolder.ot_time = (TextView)view.findViewById(R.id.tv_other_time);
//            }

            //--------------------------------------------------------------------------------------------
            view = ((LayoutInflater)ccaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatcommuni_me_frame, null);
            viewHolder = new ViewHolderChat();

            viewHolder.sender = (TextView)view.findViewById(R.id.tv_my_nick);
            viewHolder.msg = (TextView)view.findViewById(R.id.tv_my_communi);
            viewHolder.time = (TextView)view.findViewById(R.id.tv_my_time);
            //--------------------------------------------------------------------------------------------

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderChat) view.getTag();
        }

        //--------------------------------------------------------------------------------------------
        if(sender.equals(nickname)){
            viewHolder.sender.setText(ccaItem.get(position).getSender());
            viewHolder.msg.setText(ccaItem.get(position).getMsg());
            viewHolder.time.setText(ccaItem.get(position).getTime());

            viewHolder.msg.setBackgroundResource(R.drawable.mine);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            viewHolder.msg.setLayoutParams(params);

            viewHolder.sender.setGravity(Gravity.RIGHT);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.LEFT_OF, viewHolder.msg.getId());
            params.rightMargin = 15;
            viewHolder.time.setLayoutParams(params);
            viewHolder.time.setGravity(Gravity.CENTER_VERTICAL);

        }  else if(!sender.equals(nickname)){
            viewHolder.sender.setText(ccaItem.get(position).getSender());
            viewHolder.msg.setText(ccaItem.get(position).getMsg());
            viewHolder.time.setText(ccaItem.get(position).getTime());

            viewHolder.msg.setBackgroundResource(R.drawable.other);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.RIGHT_OF, viewHolder.msg.getId());
            params.leftMargin = 15;
            viewHolder.time.setLayoutParams(params);
            viewHolder.time.setGravity(Gravity.CENTER_VERTICAL);

            viewHolder.sender.setGravity(Gravity.LEFT);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.leftMargin = 40;
            viewHolder.msg.setLayoutParams(params);

        }
        //--------------------------------------------------------------------------------------------


//        if(sender.equals(nickname)){
//            viewHolder.sender.setText(ccaItem.get(position).getSender());
//            viewHolder.msg.setText(ccaItem.get(position).getMsg());
//            viewHolder.time.setText(ccaItem.get(position).getTime());
//        } else if(!sender.equals(nickname)){
//            viewHolder.ot_sender.setText(ccaItem.get(position).getSender());
//            viewHolder.ot_msg.setText(ccaItem.get(position).getMsg());
//            viewHolder.ot_time.setText(ccaItem.get(position).getTime());
//        }

        return view;
    }

    public void addItem(Chat chat){
        ccaItem.add(chat);
    }
}
