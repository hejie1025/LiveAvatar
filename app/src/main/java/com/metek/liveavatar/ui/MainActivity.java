package com.metek.liveavatar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.metek.liveavatar.R;
import com.metek.liveavatar.User;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void user1(View view) {
        User.getInstance().setUserId("666");
        startActivity(new Intent(this, ChatActivity.class));
        finish();
    }

    public void user2(View view) {
        User.getInstance().setUserId("233");
        startActivity(new Intent(this, ChatActivity.class));
        finish();
    }
}
