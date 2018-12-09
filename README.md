[客户端GitHub地址](https://github.com/PdKingLiu/AndroidChatRoomClient)
[服务器GitHub地址](https://github.com/PdKingLiu/AndroidChatRoomServer)
[Android多人聊天室—服务器](https://blog.csdn.net/CodeFarmer__/article/details/81747541)
### 先上图
![这里写图片描述](https://img-blog.csdn.net/20180816195733221?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0NvZGVGYXJtZXJfXw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
![这里写图片描述](https://img-blog.csdn.net/20180816195740738?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0NvZGVGYXJtZXJfXw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
![这里写图片描述](https://img-blog.csdn.net/20180816195748295?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0NvZGVGYXJtZXJfXw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

### 主活动接收用户信息并登录

```
package com.example.client;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button enter;
    private EditText name_input;
    private EditText ip_input;
    private EditText port_input;
    private String name;
    private String ip;
    private String port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        enter = findViewById(R.id.enter);
        name_input = findViewById(R.id.name_input);
        ip_input = findViewById(R.id.ip_input);
        port_input = findViewById(R.id.port_input);
        name_input.setText("Lpp");
        ip_input.setText("222.24.34.118");
        port_input.setText("12580");
        enter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        name = name_input.getText().toString();
        ip = ip_input.getText().toString();
        port = port_input.getText().toString();
        Intent intent = new Intent(MainActivity.this, ChatRoom.class);
        intent.putExtra("name", name);
        intent.putExtra("ip", ip);
        intent.putExtra("port", port);
        startActivity(intent);
    }
}

```
### 活动ChatRoom负责向服务器发送信息和接收信息

```
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
```

### 消息类Msg

```
package com.example.client;

public class Msg {

    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 0;
    public String content;
    private int type;

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
}

```
### RecyclerView适配器 MsgAdapter

```
package com.example.client;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private List<Msg> mMsgList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leftLayout = itemView.findViewById(R.id.left_layout);
            rightLayout = itemView.findViewById(R.id.right_layout);
            leftMsg = itemView.findViewById(R.id.left_msg);
            rightMsg = itemView.findViewById(R.id.right_msg);
        }
    }

    public MsgAdapter(List<Msg> mMsgList) {
        this.mMsgList = mMsgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.msg_item, viewGroup, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Msg msg = mMsgList.get(i);

        if (msg.getType() == Msg.TYPE_RECEIVED) {
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.leftMsg.setText(msg.getContent());
        } else {
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }
}
```
### 主活动登录布局 activity_main.xml

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.percent.PercentRelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/pinone"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <ImageView
        android:layout_width="266dp"
        android:layout_height="164dp"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="64dp"
        android:src="@drawable/pic" />

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:layout_marginBottom="73dp"
        android:orientation="vertical">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/name"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="用户名　 "
                android:textSize="20sp" />

            <EditText
                android:id="@+id/name_input"
                android:layout_width="150dp"
                android:layout_height="wrap_content" />


        </LinearLayout>

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center">

            <TextView
                android:id="@+id/ipname"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="服务器　 "
                android:textSize="20sp" />

            <EditText
                android:id="@+id/ip_input"
                android:layout_width="150dp"
                android:layout_height="wrap_content" />

        </LinearLayout>

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">

            <TextView
                android:id="@+id/portname"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="端口 　　"
                android:textSize="20sp" />

            <EditText
                android:id="@+id/port_input"
                android:layout_width="150dp"
                android:layout_height="wrap_content" />

        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center">

            <Button
                android:id="@+id/enter"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:alpha="0.5"
                android:background="#B0E0E6"
                android:text="登录"
                android:textSize="20sp" />
        </LinearLayout>

    </LinearLayout>

</android.support.percent.PercentRelativeLayout>
```
### 聊天室布局 chatroom.xml

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/pinone"
    android:orientation="vertical">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="?android:attr/actionBarSize"
        android:background="@color/colorPrimary">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="多人聊天室"
            android:textColor="#F5F5F5"
            android:textSize="20dp" />

        <Button
            android:id="@+id/edit_up"
            android:layout_width="25dp"
            android:layout_height="25dp"
            android:layout_alignParentLeft="true"
            android:layout_centerVertical="true"
            android:layout_marginLeft="10dp"
            android:background="@mipmap/edit" />

    </RelativeLayout>

    <android.support.v7.widget.RecyclerView
        android:id="@+id/msg_recycle_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <EditText
            android:id="@+id/input_text"
            android:layout_width="0dp"
            android:layout_height="37dp"
            android:layout_weight="1"
            android:background="@drawable/input"
            android:hint="  输入聊天内容"
            android:maxLines="2" />

        <Button
            android:id="@+id/send"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:alpha="0.5"
            android:background="#F5DEB3"
            android:text="发送"
            android:textSize="17dp" />

    </LinearLayout>

</LinearLayout>

```

### RecyclerView子项布局msg_item.xml

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="10dp">

    <LinearLayout
        android:id="@+id/left_layout"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="left"
        android:background="@drawable/message_left">

        <TextView
            android:id="@+id/left_msg"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_margin="10dp"
            android:maxWidth="170dp"
            android:textColor="#fff" />

    </LinearLayout>

    <LinearLayout
        android:id="@+id/right_layout"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="right"
        android:background="@drawable/message_right">

        <TextView
            android:id="@+id/right_msg"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_margin="10dp"
            android:maxWidth="170dp" />

    </LinearLayout>

</LinearLayout>
```
### 关于 AndroidManifest.xml

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.client">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ppa"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity
            android:name=".MainActivity"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".ChatRoom"
            android:screenOrientation="portrait">

        </activity>
    </application>

</manifest>
```
##### 其中加入联网权限和禁止转屏
`<uses-permission android:name="android.permission.INTERNET" />`

`android:screenOrientation="portrait"`

### app/build.gradle 加入依赖库

```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:28.0.0-alpha3'
    implementation 'com.android.support:percent:28.0.0-alpha3'
    implementation 'com.android.support.constraint:constraint-layout:1.1.2'
    implementation 'com.android.support:recyclerview-v7:28.0.0-alpha3'

    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
}
```
