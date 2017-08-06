package com.together.linkalk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * Created by kimhj on 2017-07-06.
 */

public class MainChatFragment extends Fragment {

    ListView lvChat;
    ChatList_Adapter clAdapter;

    public MainChatFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.main_chat_activity, container, false);
        lvChat = (ListView)layout.findViewById(R.id.lvChat);
        clAdapter = new ChatList_Adapter(getActivity().getApplicationContext());
        lvChat.setAdapter(clAdapter);

        return layout;
    }
}
