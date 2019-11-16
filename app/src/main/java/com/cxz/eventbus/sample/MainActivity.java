package com.cxz.eventbus.sample;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cxz.eventbuslib.EventBus;
import com.cxz.eventbuslib.Subscribe;
import com.cxz.eventbuslib.ThreadMode;
import com.cxz.livedatabus.LiveDataBus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);

        LiveDataBus.get()
                .with("key_test", String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Log.e(TAG, "LiveDataBus------>>" + s);
                    }
                });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMessage(EventBean bean) {
        Log.e(TAG, "EventBus------>>" + Thread.currentThread().getName());
        Log.e(TAG, "EventBus------>>" + bean.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
