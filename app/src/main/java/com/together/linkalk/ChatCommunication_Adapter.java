package com.together.linkalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kimhj on 2017-08-01.
 */

public class ChatCommunication_Adapter extends BaseAdapter {

    Context ccaContext;

    ArrayList<Chat> ccaItem;

    ViewHolderChat viewHolder;

    String savedDate;
    String[] savedDateDis;
    String savedYear;
    String savedMonth;
    String savedDay;
    String savedHour;
    String savedMin;
    String saved2Date;
    String[] saved2DateDis;
    String saved2Year;
    String saved2Month;
    String saved2Day;

    RelativeLayout.LayoutParams my_msg_params;
    RelativeLayout.LayoutParams my_imgMsg_params;
    RelativeLayout.LayoutParams my_time_params_left_msg;
    RelativeLayout.LayoutParams my_time_params_left_imgMsg;
    RelativeLayout.LayoutParams other_pic_params;
    RelativeLayout.LayoutParams other_msg_params_right_pic;
    RelativeLayout.LayoutParams other_imgMsg_params_right_pic;
    RelativeLayout.LayoutParams other_time_params_right_msg;
    RelativeLayout.LayoutParams other_time_params_right_imgMsg;


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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String sender = ccaItem.get(position).getSender();
        SharedPreferences sharedPreferences = ccaContext.getSharedPreferences("maintain", ccaContext.MODE_PRIVATE);
        String nickname = sharedPreferences.getString("nickname", "");

        if(view == null){
            view = ((LayoutInflater)ccaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatcommuni_me_frame, null);
            viewHolder = new ViewHolderChat();

            viewHolder.pic = (ImageView)view.findViewById(R.id.iv_my_pic);
            viewHolder.sender = (TextView)view.findViewById(R.id.tv_my_nick);
            viewHolder.imgMsg = (ImageView)view.findViewById(R.id.iv_chat_img);
            viewHolder.msg = (TextView)view.findViewById(R.id.tv_my_communi);
            viewHolder.time = (TextView)view.findViewById(R.id.tv_my_time);
            viewHolder.date = (TextView)view.findViewById(R.id.tv_date);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderChat) view.getTag();
        }

        // 채팅방 날짜 변하는날 날짜 알려주는 부분
        if(position==0){
            savedDate = ccaItem.get(position).getTime();
            savedDateDis = savedDate.split("/");
            savedYear = savedDateDis[0];
            savedMonth = savedDateDis[1];
            savedDay = savedDateDis[2];
            savedHour = savedDateDis[3];
            savedMin = savedDateDis[4];

            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.date.setText(savedYear + "년 " + savedMonth + "월 " + savedDay + "일");
        } else {
            saved2Date = ccaItem.get(position-1).getTime();
            saved2DateDis = saved2Date.split("/");
            saved2Year = saved2DateDis[0];
            saved2Month = saved2DateDis[1];
            saved2Day = saved2DateDis[2];

            savedDate = ccaItem.get(position).getTime();
            savedDateDis = savedDate.split("/");
            savedYear = savedDateDis[0];
            savedMonth = savedDateDis[1];
            savedDay = savedDateDis[2];
            savedHour = savedDateDis[3];
            savedMin = savedDateDis[4];

            if(saved2Year.equals(savedYear) && saved2Month.equals(savedMonth) && saved2Day.equals(savedDay)){
                viewHolder.date.setVisibility(View.GONE);
            } else {
                viewHolder.date.setVisibility(View.VISIBLE);
                viewHolder.date.setText(savedYear + "년 " + savedMonth + "월 " + savedDay + "일");
            }
        }


        if(ccaItem.get(position).getType()==1){
            my_msg_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            my_msg_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

            my_time_params_left_msg = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            my_time_params_left_msg.addRule(RelativeLayout.LEFT_OF, viewHolder.msg.getId());
            my_time_params_left_msg.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            my_time_params_left_msg.rightMargin = 15;

            other_msg_params_right_pic = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            other_msg_params_right_pic.leftMargin = 110;

            other_time_params_right_msg = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            other_time_params_right_msg.addRule(RelativeLayout.RIGHT_OF, viewHolder.msg.getId());
            other_time_params_right_msg.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            other_time_params_right_msg.leftMargin = 15;

        } else if(ccaItem.get(position).getType()==2) {
            my_imgMsg_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            my_imgMsg_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            my_imgMsg_params.rightMargin = 10;

            my_time_params_left_imgMsg = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            my_time_params_left_imgMsg.addRule(RelativeLayout.LEFT_OF, viewHolder.imgMsg.getId());
            my_time_params_left_imgMsg.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            my_time_params_left_imgMsg.rightMargin = 15;

            other_imgMsg_params_right_pic = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            other_imgMsg_params_right_pic.leftMargin = 120;

            other_time_params_right_imgMsg = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            other_time_params_right_imgMsg.addRule(RelativeLayout.RIGHT_OF, viewHolder.imgMsg.getId());
            other_time_params_right_imgMsg.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            other_time_params_right_imgMsg.leftMargin = 15;
        }

        other_pic_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        other_pic_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        other_pic_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        other_pic_params.width=90;
        other_pic_params.height=90;
        other_pic_params.leftMargin = 10;

        RequestOptions op = new RequestOptions();
        op.override(510, 340);

        // 채팅방 메시지 띄워주는 부분
        if(sender.equals(nickname)){
            viewHolder.pic.setVisibility(View.GONE);
            viewHolder.sender.setText(ccaItem.get(position).getSender());
            viewHolder.time.setText(savedHour + " : " + savedMin);
            viewHolder.sender.setGravity(Gravity.RIGHT);
            if(ccaItem.get(position).getType()==1){
                viewHolder.msg.setVisibility(View.VISIBLE);
                viewHolder.imgMsg.setVisibility(View.GONE);

                viewHolder.msg.setText(ccaItem.get(position).getTransmsg());
                viewHolder.msg.setBackgroundResource(R.drawable.mine);
                viewHolder.msg.setLayoutParams(my_msg_params);

                viewHolder.time.setLayoutParams(my_time_params_left_msg);
                viewHolder.time.setGravity(Gravity.CENTER_VERTICAL);
            } else if(ccaItem.get(position).getType()==2) {
                viewHolder.imgMsg.setVisibility(View.VISIBLE);
                viewHolder.msg.setVisibility(View.GONE);

                String chat_img_path = ccaItem.get(position).getTransmsg();
                Uri chat_img_uri = Uri.parse("http://www.o-ddang.com/linkalk/"+chat_img_path);
                Glide.with(ccaContext).load(chat_img_uri).apply(op).into(viewHolder.imgMsg);
                viewHolder.imgMsg.setLayoutParams(my_imgMsg_params);

                viewHolder.time.setLayoutParams(my_time_params_left_imgMsg);
            }
        }  else if(!sender.equals(nickname)){
            viewHolder.pic.setVisibility(View.VISIBLE);
            viewHolder.pic.setLayoutParams(other_pic_params);
            String path = ccaItem.get(position).getPath();
            Uri uri = Uri.parse("http://www.o-ddang.com/linkalk/"+path);
            Glide.with(ccaContext).load(uri).apply(RequestOptions.circleCropTransform()).into(viewHolder.pic);
            viewHolder.sender.setText(ccaItem.get(position).getSender());
            viewHolder.sender.setGravity(Gravity.LEFT);
            if(ccaItem.get(position).getType()==1){
                viewHolder.msg.setVisibility(View.VISIBLE);
                viewHolder.imgMsg.setVisibility(View.GONE);

                if(ccaItem.get(position).getIsTrans()){
                    viewHolder.msg.setText(ccaItem.get(position).getTransmsg());
                } else {
                    viewHolder.msg.setText(ccaItem.get(position).getMsg());
                }
                viewHolder.msg.setBackgroundResource(R.drawable.other);
                viewHolder.msg.setLayoutParams(other_msg_params_right_pic);

                viewHolder.time.setText(savedHour + " : " + savedMin);
                viewHolder.time.setLayoutParams(other_time_params_right_msg);
                viewHolder.time.setGravity(Gravity.CENTER_VERTICAL);
            } else if(ccaItem.get(position).getType()==2) {
                viewHolder.imgMsg.setVisibility(View.VISIBLE);
                viewHolder.msg.setVisibility(View.GONE);

                String chat_img_path = ccaItem.get(position).getTransmsg();
                Uri chat_img_uri = Uri.parse("http://www.o-ddang.com/linkalk/"+chat_img_path);
                Glide.with(ccaContext).load(chat_img_uri).apply(op).into(viewHolder.imgMsg);
                viewHolder.imgMsg.setLayoutParams(other_imgMsg_params_right_pic);

                viewHolder.time.setText(savedHour + " : " + savedMin);
                viewHolder.time.setLayoutParams(other_time_params_right_imgMsg);
            }
        }

        return view;
    }

    public void addItem(Chat chat){
        ccaItem.add(chat);
    }

    public void add(Chat chat) { ccaItem.add(0, chat); }
}
