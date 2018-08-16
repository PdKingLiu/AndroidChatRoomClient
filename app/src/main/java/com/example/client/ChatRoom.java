package com.example.client;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatRoom extends AppCompatActivity {

    private List<Msg> msgList;
    private EditText inputText;
    private Button send;
    private Button edit;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private String name;
    private String ip;
    private String port;
    private Socket socketSend = null;
    private InputStream receiveInput;
    private OutputStream sendOutput;
    private String content;
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom);
        inputText = findViewById(R.id.input_text);
        send = findViewById(R.id.send);
        edit = findViewById(R.id.edit_up);
        msgRecyclerView = findViewById(R.id.msg_recycle_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
        msgList = new ArrayList<>();
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        try {
            socketSend = new Socket(ip, Integer.parseInt(port));
            sendOutput = socketSend.getOutputStream();
            receiveInput = socketSend.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (socketSend == null) {
            Toast.makeText(this, "连接服务器失败",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] b = new byte[1024 * 3];
                    int len;
                    String receiveMsg;
                    while (true) {
                        while ((len = receiveInput.read(b)) != -1) {
                            receiveMsg = new String(b, 0, len);
                            final Msg msg = new Msg(receiveMsg, Msg.TYPE_RECEIVED);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    msgList.add(msg);
                                    adapter.notifyItemInserted(msgList.size() - 1);
                                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dia = new AlertDialog.Builder(ChatRoom.this);
                dia.setTitle("退出");
                dia.setMessage("确定退出登录吗？");
                dia.setCancelable(true);
                dia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dia.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dia.show();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    content = inputText.getText().toString();
                    if (!"".equals(content)) {
                        @SuppressLint("SimpleDateFormat")
                        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                        sb.append(content).append("\n\n来自：").append(name).
                                append("\n" + date);
                        content = sb.toString();
                        sendOutput.write(content.getBytes());
                        sb.delete(0, sb.length());
                        Msg msg = new Msg(content, Msg.TYPE_SENT);
                        msgList.add(msg);
                        adapter.notifyItemInserted(msgList.size() - 1);
                        msgRecyclerView.scrollToPosition(msgList.size() - 1);
                        inputText.setText("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder dia = new AlertDialog.Builder(ChatRoom.this);
            dia.setTitle("退出");
            dia.setMessage("确定退出登录吗？");
            dia.setCancelable(true);
            dia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dia.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dia.show();
        }
        return false;
    }
}