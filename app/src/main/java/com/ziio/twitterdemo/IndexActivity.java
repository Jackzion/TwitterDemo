package com.ziio.twitterdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.ziio.twitterdemo.model.Ticket;

import java.util.ArrayList;
import java.util.List;

public class IndexActivity extends AppCompatActivity {

    private List<Ticket> ticketList;
    private ListView lvTweets;
    private MyTweetAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        lvTweets = findViewById(R.id.lvTweets);
        ticketList = new ArrayList<Ticket>();
        // dummy data
        ticketList.add(new Ticket("0" , "him" , "url" , "add"));
        ticketList.add(new Ticket("0" , "him" , "url" , "ziio"));
        ticketList.add(new Ticket("0" , "him" , "url" , "ziio"));
        ticketList.add(new Ticket("0" , "him" , "url" , "ziio"));

        adapter = new MyTweetAdapter(ticketList,this);
        lvTweets.setAdapter(adapter);
    }

    class MyTweetAdapter extends BaseAdapter {

        private List<Ticket> listNotesAdapter = new ArrayList<Ticket>();

        private Context context;

        public MyTweetAdapter(List listNotesAdpater, Context context) {
            this.listNotesAdapter = listNotesAdpater;
            this.context = context;
        }

        @Override
        public int getCount() {
            return listNotesAdapter.size();
        }

        @Override
        public Object getItem(int i) {
            return listNotesAdapter.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Ticket myTweet = listNotesAdapter.get(i);
            if(myTweet.getTweetPersonUID().equals("add")){
                View myView = LayoutInflater.from(context).inflate(R.layout.add_ticket, null);
                // load add ticket

                return myView;
            }else{
                View myView = LayoutInflater.from(context).inflate(R.layout.tweets_ticket, null);
                // todo : work
                // load tweet ticket
                return myView;
            }
        }
    }
}