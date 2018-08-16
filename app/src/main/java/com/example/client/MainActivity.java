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
