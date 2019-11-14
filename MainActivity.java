package com.example.login;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText etpassword,etusername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etusername = (EditText) findViewById(R.id.etusername);
        etpassword = (EditText)findViewById(R.id.etpassword);
    }


    public void onLogin(View view) {
        String username = etusername.getText().toString();
        String password = etpassword.getText().toString();
        String type = "login";

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type,username,password);

    }
}
