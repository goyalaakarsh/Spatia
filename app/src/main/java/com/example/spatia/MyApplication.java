package com.example.spatia;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "deuv3jnzu");
        config.put("api_key", "156814411182892");      
        config.put("api_secret", "ZOhx5zc5voT8gdtXJcueTFvyKQ4");
        config.put("secure", "true"); 
        MediaManager.init(this, config);
    }
}