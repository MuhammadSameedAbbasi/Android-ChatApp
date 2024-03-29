package com.omex.serverchat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.slice.Slice;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String hostname = "192.168.0.57";
    int port = Integer.parseInt("12000");
    static String user_name ;
    EditText chat_box;
    static Socket socket ;
    OutputStream output;
    PrintWriter writer;
    BufferedReader reader;
    InputStream input;
    public static String chat_partner_name;
    String xmlstring;
    static MainActivity instance;


    RecyclerView chatboxview;


    ArrayList<String> active_users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable
                = new ColorDrawable(getResources().getColor( R.color.tempcolor));
        actionBar.setBackgroundDrawable(colorDrawable);

        user_name= LoginPage.username;
        Objects.requireNonNull(getSupportActionBar()).setTitle("COSA OMEX");
        setTitle(user_name);
        instance=this;
        Bundle extras = getIntent().getExtras();
        /*if (extras != null) {
            xmlstring = extras.getString("xmlfiledata");
            Log.d("xml ", "onCreate;;;;;"+xmlstring);
        }*/

        //RecyclerView recyclerView = findViewById(R.id.chat_box_recycler);
/*

        // Create and set the layout manager
        // For the RecyclerView.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<ItemClass> itemClasses = new ArrayList<>();

        // pass the arguments
        itemClasses.add(new ItemClass(ItemClass.LayoutOne,
                "Item Type 1"));
        itemClasses.add(new ItemClass( ItemClass.LayoutTwo,
                "Item Type 2", "Text"));

        AdapterClass adapter = new AdapterClass(itemClasses);

        // set the adapter
        recyclerView.setAdapter(adapter);

*/


        /*try
        {socket = new Socket(hostname, port);
            output = socket.getOutputStream();
            writer = new PrintWriter(output, true);


        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error hai: " + ex.getMessage());
        }*/
        /*
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chat_partner_name = extras.getString("username");
            if (chat_partner_name!= null){
                getSupportActionBar().setTitle(chat_partner_name);
            }
            //The key argument here must match that used in the other activity
        }*/ //bundle
       /* try {
            socket = new Socket(hostname, port);
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try  {
            socket = new Socket(hostname, port);
            output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            input = socket.getInputStream();

            /*InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
           */
        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error hai: " + ex.getMessage());
        } catch (Exception e){
            runOnUiThread(new Runnable() {

                public void run() {

                    Toast.makeText(getApplicationContext(), "SERVER Not Found", Toast.LENGTH_SHORT).show();

                }
            });
        }

        /*Thread msg_update_thread = new Thread(new  update_chatbox());
        msg_update_thread.start();*/

        chatboxview= findViewById(R.id.chat_box_recycler);
        chat_box =  findViewById(R.id.chat_box);
        chat_box.setTextColor(Color.WHITE);
        //chat_box.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        listen_to_messages();
        register_name();



        Log.d("oncreate", "onCreate: run: register name end");
        //listen_to_messages();
        Log.d("oncreate", "onCreate run: finished");
    }

    /*public static MainActivity getInstance() {
        return instance;
    }*/
    @Override
    protected void onResume()
    {
        super.onResume();
        on_openchat();
    }

    public void on_openchat(){
        Objects.requireNonNull(getSupportActionBar()).setTitle(chat_partner_name);
        chat_box.setText("");
    }
    Handler handler= new Handler();
    /*private void update_chatbox(){
        while (true){
            if (printlock){
                //chat_box.setText(chat_box.getText()+got_msg);
                chat_box.append(System.getProperty("line.separator") + got_msg_update);
                got_msg_update="";
            }
        }
    }*/
    /*private class update_chatbox implements Runnable {
        public void run() {
            while (true){
                if (printlock){
                    //chat_box.setText(chat_box.getText()+got_msg);
                    chat_box.append(System.getProperty("line.separator") + got_msg_update);
                    got_msg_update="";
                }
            }
        }

    }*/
    private void listen_to_messages() {
        Log.d("listen to msgs", "listen_to_messages:run: ");
        Thread msg_listener_thread = new Thread(new  message_listener());
        msg_listener_thread.start();
    }
    private class message_listener implements Runnable {
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String charsRead = "";
                char[] buffer = new char[1000];
                while (true) {

                    Log.i("Message from Server 0", "run: 0 " );

                    charsRead = in.readLine();

                    //in.close();
                    String serverMessage = charsRead;
                    if (serverMessage != null) {

                        String got_msg = chk_msg(serverMessage);
                        Log.i("Message from Server 2", "run: " + got_msg);

                        handler.post(new Runnable() {
                            public void run() {
                                //chat_box.setText(chat_box.getText()+got_msg);
                                chat_box.append(System.getProperty("line.separator") + got_msg);

                            }
                        });


                        Log.i("Message from Server 3", "run: " + got_msg);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

/*
    private class update_chatbox extends AsyncTask <String,Void,Void>{


        protected void onProgressUpdate(String... got_msg) {
            chat_box.append(System.getProperty("line.separator") + got_msg);
        }

        @Override
        protected Void doInBackground(String... got_msg) {
            publishProgress();
            return null;
        }
    }*/


    private void register_name() {

        Thread name_thread = new Thread(new thread_send_name());
        name_thread.start();

    }
    class thread_send_name implements Runnable {

        public thread_send_name(){

        }
        public void run()
        {

            try  {

                Log.d("send name", "run: sock 1 "+ socket.isConnected());
                String text = "&$##*" + user_name;

                writer.println(text);
                writer.flush();
                output.flush();

                //InputStream input = socket.getInputStream();
                Log.d("send name", "run: sock2 "+ socket.isConnected());

                String returned_msg =""; //= reader.readLine();

                /*BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                while(true) {
                    if (reader.ready()) {
                        Log.d("send name", "run: sock3 "+ socket.isConnected());
                        returned_msg = reader.readLine();

                        Log.d("name send", "run: read text name 3 /" + returned_msg);
                        returned_msg = chk_msg(returned_msg);

                        Log.d("name send", "run: read text name4 /" + returned_msg);
                        chat_box.append(returned_msg);
                        chat_box.append(System.getProperty("line.separator"));

                        //input.close();
                        break;

                    }
                    //Log.d("name send", "run: read text name 4.55 //" + returned_msg);
                }
                Log.d("send name", "run: sock4 "+ socket.isConnected());
                //listen_to_messages();
                Log.d("name send", "run: sock5 read text name5 /" + returned_msg);
                //output.close();
                */
            } catch (UnknownHostException ex) {

                System.out.println("Server not found: " + ex.getMessage());

            } catch (IOException ex) {

                System.out.println("I/O error hai: " + ex.getMessage());
            }

        }

    }


    String[] clientset;
    private String chk_msg(String msg){

        Log.d("AG",msg+"//chk_msg :>");
        if (msg.startsWith("#"))
        {
            clientset = msg.split("#");

            /*for (int count = 1; count<clientset.length -1; count++){
                if (active_users.indexOf(clientset[count])==-1){
                    active_users.add(clientset[count]);
                    Log.d("server activeuser", "chk_msg : 2.5 userlist"+ clientset[count]);
                }
            }*/

            int lenght= clientset.length;

            return clientset[lenght-1];

        }
        return msg;
    }

    public void orderbtn(View v2){

        Intent intent = new Intent(MainActivity.this,MakeOrder.class);
        intent.putExtra("xmlfiledata",xmlstring);
        startActivity(intent);

    }

    public void userlistbtn(View v2){


        Intent intent = new Intent(MainActivity.this,ClientList.class);
        if (clientset!= null) {
            String[] templist = Arrays.copyOfRange(clientset, 0, clientset.length - 1);
            intent.putExtra("username_list", templist /*active_users*/);
        }
        startActivity(intent);
        //this.finish();

    }


    public void sendbtn(View v1){

        String rcvrcode ="$%$";
        String send_to = "Friend";

        if (chat_partner_name!= null){
            send_to=chat_partner_name;
        }

        Log.d("sendbtn", "sendbtn: before thread");

        EditText message_box_text =  findViewById(R.id.message_box);
        //String text = txt1.getText().toString();

        for (int i =  send_to.length(); i < 20; i++)        //test it
        {
            send_to += " ";
        }

        String msg_to_be_sent = rcvrcode + send_to + user_name + ": " + message_box_text.getText().toString();
        Log.d("send btn message", "sendbtn run: >>>"+msg_to_be_sent);

        chat_box.setText(new StringBuilder().append(chat_box.getText().toString()).append(
                System.getProperty("line.separator")).append(message_box_text.getText().toString()).toString());


        Thread sender_thread = new Thread(new thread_message(msg_to_be_sent));
        sender_thread.start();
        Log.d("send btn", "run: left  read text //" );
        message_box_text.getText().clear();


        //sendbtnasync();
    }
    class thread_message implements Runnable {
        String text;
        EditText txt1;
        public thread_message(String msg){
            text = msg;
        }
        public void run()
        {
            try {

                writer.println(text);
                output.flush();
                Log.d("thread_message snd", "run: read text 1///" + text);


                /*BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                Log.d("thread_message reader", "run: read text 3//" + text);
                String returned_msg="";
                while(true){
                    if (reader.ready()) {

                        returned_msg = reader.readLine();
                        Log.d("thread_message reader",returned_msg+"//chk_msg://");

                        //returned_msg = chk_msg(returned_msg);

                        Log.d("thread_message reader", "run: returned text msg 4 >>" + returned_msg);
                        chat_box.append( System.getProperty("line.separator") + returned_msg);
                        output.flush();
                        break;
                    }
                }*/
                //output.close();
                //input.close();
                //Log.d("thread_message reader", "run: returned msg 5 ");


            }catch(Exception e){
                System.out.println("text reader" + e.getMessage());
            }
        }
    }
}